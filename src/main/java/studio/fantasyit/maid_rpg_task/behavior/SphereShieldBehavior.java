package studio.fantasyit.maid_rpg_task.behavior;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.AABB;
import studio.fantasyit.maid_rpg_task.Config;
import studio.fantasyit.maid_rpg_task.data.MaidMageData;
import studio.fantasyit.maid_rpg_task.entity.SphereShieldEntity;
import studio.fantasyit.maid_rpg_task.registry.EntityRegistry;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;

/**
 * Spawns and manages a maid's personal damage-absorbing sphere shield.
 * <p>
 * Originally Mage-only; now reusable by any task via the second constructor, which lets the
 * caller supply its own enable/health config instead of Mage's. Shield HP/cooldown are still
 * persisted under {@link MaidMageData} regardless of which task owns the behavior — it's just
 * a per-maid "shield charge" store, not something that needs its own data key per task.
 */
public class SphereShieldBehavior extends Behavior<EntityMaid> {
    private final BooleanSupplier enabled;
    private final DoubleSupplier health;

    public SphereShieldBehavior() {
        this(() -> Config.mageShieldEnabled, () -> Config.mageShieldHealth);
    }

    public SphereShieldBehavior(BooleanSupplier enabled, DoubleSupplier health) {
        super(Map.of(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT));
        this.enabled = enabled;
        this.health = health;
    }

    @Override protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) { return enabled.getAsBoolean(); }
    @Override protected boolean canStillUse(ServerLevel level, EntityMaid maid, long t)       { return enabled.getAsBoolean(); }
    @Override protected boolean timedOut(long t)                                               { return false; }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long gameTime) {
        MaidMageData.Data data = maid.getOrCreateData(MaidMageData.KEY, MaidMageData.Data.getDefault());

        // Cooldown: hide any shield and wait
        int cooldown = data.getShieldCooldown();
        if (cooldown > 0) {
            data.setShieldCooldown(cooldown - 1);
            findShield(level, maid).ifPresent(s -> s.setActive(false));
            return;
        }

        SphereShieldEntity shield = getOrSpawnShield(level, maid, data);
        if (shield == null) return;

        // Persist HP from entity → data every tick so it survives unloads
        data.setShieldHp(shield.getShieldHpRaw());

        shield.setActive(isBeingTargeted(level, maid));
    }

    // ── Shield entity management ───────────────────────────────────────────────

    private SphereShieldEntity getOrSpawnShield(ServerLevel level, EntityMaid maid, MaidMageData.Data data) {
        Optional<SphereShieldEntity> existing = findShield(level, maid);
        if (existing.isPresent()) return existing.get();

        // Spawn fresh shield, restoring saved HP if available
        SphereShieldEntity shield = EntityRegistry.SPHERE_SHIELD.get().create(level);
        if (shield == null) return null;
        int savedHp = data.getShieldHp(); // -1 means "full"
        shield.init(maid, (float) health.getAsDouble(), savedHp);
        level.addFreshEntity(shield);
        return shield;
    }

    /** Level-wide search by maid UUID — survives teleports. Kills duplicates. */
    private Optional<SphereShieldEntity> findShield(ServerLevel level, EntityMaid maid) {
        List<SphereShieldEntity> all = level.getEntitiesOfClass(
                SphereShieldEntity.class,
                new AABB(-30000, -300, -30000, 30000, 300, 30000),
                s -> maid.getUUID().equals(s.getMaidUuid()));
        // Kill duplicates (can happen after teleport edge cases)
        for (int i = 1; i < all.size(); i++) all.get(i).discard();
        return all.isEmpty() ? Optional.empty() : Optional.of(all.get(0));
    }

    // ── Targeting detection ────────────────────────────────────────────────────

    private boolean isBeingTargeted(ServerLevel level, EntityMaid maid) {
        for (Entity e : level.getEntities(maid, AABB.ofSize(maid.position(), 32, 16, 32))) {
            if (e instanceof Mob mob && mob.isAlive()) {
                LivingEntity t = mob.getTarget();
                if (t != null && t.getUUID().equals(maid.getUUID())) return true;
            }
        }
        return false;
    }
}
