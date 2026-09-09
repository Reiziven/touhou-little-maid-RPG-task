package studio.fantasyit.maid_rpg_task.event;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import studio.fantasyit.maid_rpg_task.Config;
import studio.fantasyit.maid_rpg_task.MaidRpgTask;
import studio.fantasyit.maid_rpg_task.data.MaidMageData;
import studio.fantasyit.maid_rpg_task.entity.SphereShieldEntity;
import studio.fantasyit.maid_rpg_task.task.MaidMageTask;
import studio.fantasyit.maid_rpg_task.task.MaidMasterTask;

import java.util.List;

@Mod.EventBusSubscriber(modid = MaidRpgTask.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SphereShieldEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onMaidHurt(LivingHurtEvent event) {
        SphereShieldEntity shield = getActiveShield(event.getEntity());
        if (shield == null) return;

        event.setCanceled(true);
        ((EntityMaid) event.getEntity()).hurtTime = 0;

        shield.absorbDamage(event.getAmount());

        if (shield.getShieldHp() <= 0) {
            shield.breakShield();
            EntityMaid maid = (EntityMaid) event.getEntity();
            MaidMageData.Data data = maid.getOrCreateData(MaidMageData.KEY, MaidMageData.Data.getDefault());
            boolean isMaster = maid.getTask() != null && maid.getTask().getUid().equals(MaidMasterTask.UID);
            data.setShieldCooldown(isMaster ? Config.masterShieldCooldown : Config.mageShieldCooldown);
            data.setShieldHp(-1); // reset so next shield spawns full
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onMaidKnockback(LivingKnockBackEvent event) {
        if (getActiveShield(event.getEntity()) != null) event.setCanceled(true);
    }

    /** Returns the active shield for a maid on a task with shielding enabled (Mage or Master), or null. */
    private static SphereShieldEntity getActiveShield(net.minecraft.world.entity.LivingEntity entity) {
        if (!(entity instanceof EntityMaid maid)) return null;
        if (maid.getTask() == null) return null;
        ResourceLocation uid = maid.getTask().getUid();
        boolean isMage = uid.equals(MaidMageTask.UID) && Config.mageShieldEnabled;
        boolean isMaster = uid.equals(MaidMasterTask.UID) && Config.masterShieldEnabled;
        if (!isMage && !isMaster) return null;
        if (!(maid.level() instanceof ServerLevel level)) return null;

        List<SphereShieldEntity> shields = level.getEntitiesOfClass(SphereShieldEntity.class,
                maid.getBoundingBox().inflate(4),
                s -> maid.getUUID().equals(s.getMaidUuid()) && s.isActive() && s.getShieldHp() > 0);
        return shields.isEmpty() ? null : shields.get(0);
    }
}
