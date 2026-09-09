package studio.fantasyit.maid_rpg_task.event;

import com.github.tartaricacid.touhoulittlemaid.api.event.InteractMaidEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitDataComponent;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.github.tartaricacid.touhoulittlemaid.init.InitItems;
import com.github.tartaricacid.touhoulittlemaid.util.PlaceHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import studio.fantasyit.maid_rpg_task.MaidRpgTask;
import studio.fantasyit.maid_rpg_task.registry.DataComponentRegistry;
import studio.fantasyit.maid_rpg_task.registry.ItemRegistry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * STORE is handled the normal, documented TLM-API way (InteractMaidEvent).
 * <p>
 * SUMMON is no longer handled here as a PlayerInteractEvent.RightClickBlock
 * listener. That approach raced against TLM's own ItemSmartSlab#useOn() for
 * the same ItemStack and was the source of the "other maids vanish" bug.
 * Summon is now injected directly into useOn() via ItemSmartSlabMixin, which
 * calls {@link #trySummonMultiMaid(UseOnContext)} below.
 */
@EventBusSubscriber(modid = MaidRpgTask.MODID)
public class MasterSoulSpellEvent {

    private static final int MAX_MAIDS = 4;
    private static final int SCAN_RANGE = 24;

    // -------------------------------------------------------------------------
    // STORE  (right-click maid)
    // -------------------------------------------------------------------------

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onInteractMaid(InteractMaidEvent event) {
        Player player = event.getPlayer();
        EntityMaid clickedMaid = event.getMaid();
        ItemStack held = event.getStack();

        if (!clickedMaid.isOwnedBy(player)) return;
        if (!hasBaubleEquipped(clickedMaid)) return;

        boolean emptyHeld = held.is(InitItems.SMART_SLAB_EMPTY.get());
        boolean filledHeld = held.is(InitItems.SMART_SLAB_HAS_MAID.get());

        if (!emptyHeld && !filledHeld) return;

        Item cooldownItem = emptyHeld ? InitItems.SMART_SLAB_EMPTY.get() : InitItems.SMART_SLAB_HAS_MAID.get();
        if (player.getCooldowns().isOnCooldown(cooldownItem)) {
            event.setCanceled(true);
            return;
        }

        if (player.isShiftKeyDown()) {
            // Shift + click on a baubled maid:
            //   empty slab  → let TLM store that one maid normally
            //   filled slab → append this maid to whatever is already stored
            if (emptyHeld) return; // let TLM handle it

            if (!event.getWorld().isClientSide()) {
                List<CompoundTag> list = getMaidList(held);
                if (list.size() >= MAX_MAIDS) {
                    event.setCanceled(true);
                    return;
                }
                CompoundTag data = new CompoundTag();
                clickedMaid.saveWithoutId(data);
                List<CompoundTag> newList = new ArrayList<>();
                newList.add(data);
                newList.addAll(list);
                held.set(DataComponentRegistry.MULTI_MAIDS, newList);
                syncPrimaryMaidData(held, data);

                clickedMaid.setHomeModeEnable(false);
                clickedMaid.spawnExplosionParticle();
                clickedMaid.playSound(SoundEvents.PLAYER_SPLASH, 1.0F,
                        event.getWorld().getRandom().nextFloat() * 0.1F + 0.9F);
                clickedMaid.discard();
                player.getCooldowns().addCooldown(InitItems.SMART_SLAB_HAS_MAID.get(), 20);
            }
            event.setCanceled(true);
            return;
        }

        // Normal click with empty slab → scan and store up to MAX_MAIDS nearby baubled maids
        if (!emptyHeld) return;

        if (!event.getWorld().isClientSide()) {
            Level level = event.getWorld();

            AABB box = clickedMaid.getBoundingBox().inflate(SCAN_RANGE);
            List<EntityMaid> candidates = level.getEntitiesOfClass(EntityMaid.class, box,
                            m -> m.isOwnedBy(player) && hasBaubleEquipped(m))
                    .stream()
                    .sorted(Comparator.comparingDouble(m -> m.distanceToSqr(clickedMaid)))
                    .limit(MAX_MAIDS)
                    .toList();

            if (candidates.isEmpty()) return;

            ItemStack output = InitItems.SMART_SLAB_HAS_MAID.get().getDefaultInstance();
            List<CompoundTag> list = new ArrayList<>();
            for (EntityMaid m : candidates) {
                CompoundTag data = new CompoundTag();
                m.saveWithoutId(data);
                list.add(data);
                m.setHomeModeEnable(false);
                m.spawnExplosionParticle();
                m.playSound(SoundEvents.PLAYER_SPLASH, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
                m.discard();
            }

            if (list.size() > 1) {
                output.set(DataComponentRegistry.MULTI_MAIDS, list);
            }
            // Always mirror index 0 into TLM's own MAID_INFO component: for a
            // single candidate this makes the slab behave exactly like a normal
            // TLM-stored maid, with our system never touching it at all.
            syncPrimaryMaidData(output, list.get(0));

            player.setItemInHand(InteractionHand.MAIN_HAND, output);
            player.getCooldowns().addCooldown(InitItems.SMART_SLAB_HAS_MAID.get(), 20);
        }

        event.setCanceled(true);
    }

    // -------------------------------------------------------------------------
    // SUMMON  (called from ItemSmartSlabMixin, injected into useOn())
    // -------------------------------------------------------------------------

    /**
     * Called by {@code ItemSmartSlabMixin} at the head of
     * {@code ItemSmartSlab#useOn(UseOnContext)}, only when the held stack is a
     * HAS_MAID slab that actually carries multi-maid data. Fully replaces TLM's
     * own useOn() logic for that one call — never falls back into it — so there
     * is exactly one code path handling the interaction, not two racing ones.
     */
    public static InteractionResult trySummonMultiMaid(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        ItemStack held = context.getItemInHand();

        if (player.getCooldowns().isOnCooldown(InitItems.SMART_SLAB_HAS_MAID.get())) {
            return InteractionResult.FAIL;
        }
        if (context.getClickedFace() != Direction.UP
                || PlaceHelper.notSuitableForPlaceMaid(level, context.getClickedPos())) {
            if (level.isClientSide()) {
                player.sendSystemMessage(Component.translatable("message.touhou_little_maid.photo.not_suitable_for_place_maid"));
            }
            return InteractionResult.FAIL;
        }

        List<CompoundTag> list = held.get(DataComponentRegistry.MULTI_MAIDS);
        if (list == null || list.isEmpty()) {
            // Shouldn't happen (mixin already checked hasMultiMaidData), but
            // fail safe rather than silently doing nothing.
            return InteractionResult.PASS;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            // Client-side prediction: don't mutate/spawn here, just report success
            // so the arm swing / sound feel responsive. The server call (which
            // does hit the branch below) is authoritative and will correct state.
            return InteractionResult.sidedSuccess(true);
        }

        BlockPos spawnBase = context.getClickedPos().above();

        if (player.isShiftKeyDown()) {
            // Shift+click: spawn only the first (selected) maid.
            spawnMaid(serverLevel, list.get(0), spawnBase, 0);

            List<CompoundTag> remaining = list.size() > 1
                    ? new ArrayList<>(list.subList(1, list.size()))
                    : List.of();

            if (remaining.isEmpty()) {
                player.setItemInHand(context.getHand(), InitItems.SMART_SLAB_EMPTY.get().getDefaultInstance());
                player.getCooldowns().addCooldown(InitItems.SMART_SLAB_EMPTY.get(), 20);
            } else {
                if (remaining.size() > 1) {
                    held.set(DataComponentRegistry.MULTI_MAIDS, remaining);
                } else {
                    held.remove(DataComponentRegistry.MULTI_MAIDS);
                }
                syncPrimaryMaidData(held, remaining.get(0));
                player.getCooldowns().addCooldown(InitItems.SMART_SLAB_HAS_MAID.get(), 20);
            }
        } else {
            // Normal click: spawn every maid.
            for (int i = 0; i < list.size(); i++) {
                spawnMaid(serverLevel, list.get(i), spawnBase, i);
            }
            player.setItemInHand(context.getHand(), InitItems.SMART_SLAB_EMPTY.get().getDefaultInstance());
            player.getCooldowns().addCooldown(InitItems.SMART_SLAB_EMPTY.get(), 20);
        }

        return InteractionResult.sidedSuccess(false);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static void spawnMaid(ServerLevel level, CompoundTag data, BlockPos base, int index) {
        EntityMaid maid = InitEntities.MAID.get().create(level);
        if (maid == null) return;
        maid.load(data);
        // saveWithoutId() still writes the original UUID into `data`. Each maid
        // stored in the slab was saved (and discarded) independently so their
        // UUIDs are normally distinct and safe to reuse — but we assign a fresh
        // one anyway as cheap insurance against edge cases like item duplication.
        maid.setUUID(UUID.randomUUID());
        maid.moveTo(
                base.getX() + 0.5 + (index % 2) * 0.5,
                base.getY(),
                base.getZ() + 0.5 + (index / 2) * 0.5,
                level.getRandom().nextFloat() * 360f, 0f);
        level.addFreshEntity(maid);
        maid.spawnExplosionParticle();
        maid.playSound(SoundEvents.PLAYER_SPLASH, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
    }

    /** Directly overwrites TLM's MAID_INFO component (no no-op guard, unlike AbstractStoreMaidItem#storeMaidData). */
    public static void syncPrimaryMaidData(ItemStack stack, CompoundTag data) {
        stack.set(InitDataComponent.MAID_INFO, CustomData.of(data));
    }

    /**
     * Returns the multi-maid list from the item, building it from TLM's own
     * MAID_INFO component if our component isn't set yet (e.g. slab stored
     * normally by TLM's own SlabClickEvent with exactly one maid).
     */
    private static List<CompoundTag> getMaidList(ItemStack stack) {
        List<CompoundTag> existing = stack.get(DataComponentRegistry.MULTI_MAIDS);
        if (existing != null && !existing.isEmpty()) {
            return new ArrayList<>(existing);
        }
        List<CompoundTag> list = new ArrayList<>();
        CustomData maidInfo = stack.get(InitDataComponent.MAID_INFO);
        if (maidInfo != null) {
            list.add(maidInfo.copyTag());
        }
        return list;
    }

    public static boolean hasBaubleEquipped(EntityMaid maid) {
        var baubleInv = maid.getMaidBauble();
        for (int i = 0; i < baubleInv.getSlots(); i++) {
            if (baubleInv.getStackInSlot(i).is(ItemRegistry.MASTER_SOUL_SPELL.get())) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasMultiMaidData(ItemStack stack) {
        List<CompoundTag> list = stack.get(DataComponentRegistry.MULTI_MAIDS);
        return list != null && !list.isEmpty();
    }

    /** Full multi-maid list, or an empty list if this stack doesn't carry one. Used by the rotate/tooltip/HUD features. */
    public static List<CompoundTag> getMultiMaidList(ItemStack stack) {
        List<CompoundTag> list = stack.get(DataComponentRegistry.MULTI_MAIDS);
        return list == null ? List.of() : list;
    }

    /** Writes the multi-maid list back onto the stack, keeping MAID_INFO in sync with the new index 0. Used by the rotate feature. */
    public static void setMultiMaidList(ItemStack stack, List<CompoundTag> list) {
        if (list.size() > 1) {
            stack.set(DataComponentRegistry.MULTI_MAIDS, list);
        } else {
            stack.remove(DataComponentRegistry.MULTI_MAIDS);
        }
        if (!list.isEmpty()) {
            syncPrimaryMaidData(stack, list.get(0));
        }
    }
}
