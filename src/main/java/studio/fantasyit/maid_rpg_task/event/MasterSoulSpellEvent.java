package studio.fantasyit.maid_rpg_task.event;

import com.github.tartaricacid.touhoulittlemaid.api.event.InteractMaidEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.github.tartaricacid.touhoulittlemaid.init.InitItems;
import com.github.tartaricacid.touhoulittlemaid.item.ItemSmartSlab;
import com.github.tartaricacid.touhoulittlemaid.util.PlaceHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import studio.fantasyit.maid_rpg_task.registry.ItemRegistry;

import java.util.Comparator;
import java.util.List;

@Mod.EventBusSubscriber
public class MasterSoulSpellEvent {

    private static final String MULTI_MAIDS_KEY = "mss_multi_maids";
    private static final int MAX_MAIDS = 4;
    private static final int SCAN_RANGE = 24;

    // -------------------------------------------------------------------------
    // STORE  (right-click maid)
    // -------------------------------------------------------------------------

    @SubscribeEvent
    public static void onInteractMaid(InteractMaidEvent event) {
        Player player = event.getPlayer();
        EntityMaid clickedMaid = event.getMaid();
        ItemStack held = event.getStack();

        if (!clickedMaid.isOwnedBy(player)) return;
        if (!hasBaubleEquipped(clickedMaid)) return;

        boolean emptyHeld  = held.is(InitItems.SMART_SLAB_EMPTY.get());
        boolean filledHeld = held.is(InitItems.SMART_SLAB_HAS_MAID.get());

        if (!emptyHeld && !filledHeld) return;

        // Check cooldown before acting
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
                // Build/get the existing list — even if TLM stored the slab without our tag
                ListTag list = getMaidList(held);
                if (list.size() >= MAX_MAIDS) {
                    event.setCanceled(true);
                    return;
                }
                CompoundTag data = new CompoundTag();
                clickedMaid.saveWithoutId(data);
                // Insert at index 0 so this maid becomes the new "selected" first entry
                ListTag newList = new ListTag();
                newList.add(data);
                for (int i = 0; i < list.size(); i++) newList.add(list.get(i));
                held.getOrCreateTag().put(MULTI_MAIDS_KEY, newList);
                // Sync TLM's own maid data to the new first entry
                EntityMaid tmp = InitEntities.MAID.get().create((ServerLevel) event.getWorld());
                if (tmp != null) {
                    tmp.load(data);
                    ItemSmartSlab.storeMaidData(held, tmp);
                    held.getOrCreateTag().put(MULTI_MAIDS_KEY, newList);
                }

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

        // Normal click with empty slab → scan and store up to 4 nearby baubled maids
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
            EntityMaid primary = candidates.get(0);
            primary.setHomeModeEnable(false);
            ItemSmartSlab.storeMaidData(output, primary);

            ListTag list = new ListTag();
            for (EntityMaid m : candidates) {
                CompoundTag data = new CompoundTag();
                m.saveWithoutId(data);
                list.add(data);
                m.setHomeModeEnable(false);
                m.spawnExplosionParticle();
                m.playSound(SoundEvents.PLAYER_SPLASH, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
                m.discard();
            }
            output.getOrCreateTag().put(MULTI_MAIDS_KEY, list);

            player.setItemInHand(InteractionHand.MAIN_HAND, output);
            player.getCooldowns().addCooldown(InitItems.SMART_SLAB_HAS_MAID.get(), 20);
        }

        event.setCanceled(true);
    }

    // -------------------------------------------------------------------------
    // SUMMON  (right-click block)
    // -------------------------------------------------------------------------

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        ItemStack held = event.getItemStack();

        if (!held.is(InitItems.SMART_SLAB_HAS_MAID.get())) return;
        if (!hasMultiMaidData(held)) return;

        // Check cooldown
        if (player.getCooldowns().isOnCooldown(InitItems.SMART_SLAB_HAS_MAID.get())) {
            event.setCanceled(true);
            return;
        }

        Level level = event.getLevel();
        Direction face = event.getFace();
        BlockPos clickedPos = event.getPos();

        if (face != Direction.UP) {
            event.setCanceled(true);
            return;
        }
        if (PlaceHelper.notSuitableForPlaceMaid(level, clickedPos)) {
            event.setCanceled(true);
            return;
        }

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            ListTag list = held.getOrCreateTag().getList(MULTI_MAIDS_KEY, Tag.TAG_COMPOUND);
            BlockPos spawnBase = clickedPos.above();

            if (player.isShiftKeyDown()) {
                // Shift+click: spawn only the first (selected) maid
                if (list.isEmpty()) return;
                spawnMaid(serverLevel, list.getCompound(0), spawnBase, 0);

                ListTag remaining = new ListTag();
                for (int i = 1; i < list.size(); i++) remaining.add(list.get(i));

                if (remaining.isEmpty()) {
                    player.setItemInHand(event.getHand(), InitItems.SMART_SLAB_EMPTY.get().getDefaultInstance());
                    player.getCooldowns().addCooldown(InitItems.SMART_SLAB_EMPTY.get(), 20);
                } else {
                    held.getOrCreateTag().put(MULTI_MAIDS_KEY, remaining);
                    EntityMaid tmp = InitEntities.MAID.get().create(serverLevel);
                    if (tmp != null) {
                        tmp.load(remaining.getCompound(0));
                        ItemSmartSlab.storeMaidData(held, tmp);
                        held.getOrCreateTag().put(MULTI_MAIDS_KEY, remaining);
                    }
                    player.getCooldowns().addCooldown(InitItems.SMART_SLAB_HAS_MAID.get(), 20);
                }
            } else {
                // Normal click: spawn all maids
                for (int i = 0; i < list.size(); i++) {
                    spawnMaid(serverLevel, list.getCompound(i), spawnBase, i);
                }
                player.setItemInHand(event.getHand(), InitItems.SMART_SLAB_EMPTY.get().getDefaultInstance());
                player.getCooldowns().addCooldown(InitItems.SMART_SLAB_EMPTY.get(), 20);
            }
        }

        event.setCanceled(true);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static void spawnMaid(ServerLevel level, CompoundTag data, BlockPos base, int index) {
        EntityMaid maid = InitEntities.MAID.get().create(level);
        if (maid == null) return;
        maid.load(data);
        maid.moveTo(base.getX() + 0.5 + (index % 2) * 0.5,
                base.getY(),
                base.getZ() + 0.5 + (index / 2) * 0.5,
                level.getRandom().nextFloat() * 360f, 0f);
        level.addFreshEntity(maid);
        maid.spawnExplosionParticle();
        maid.playSound(SoundEvents.PLAYER_SPLASH, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
    }

    /**
     * Returns the mss_multi_maids list from the item, building it from TLM's
     * MaidInfo if the tag doesn't exist yet (slab stored normally by TLM).
     */
    private static ListTag getMaidList(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains(MULTI_MAIDS_KEY, Tag.TAG_LIST)) {
            ListTag existing = tag.getList(MULTI_MAIDS_KEY, Tag.TAG_COMPOUND);
            if (!existing.isEmpty()) return existing;
        }
        // Slab was filled by TLM's normal system — migrate MaidInfo into our list
        ListTag list = new ListTag();
        CompoundTag maidInfo = ItemSmartSlab.getMaidData(stack);
        if (!maidInfo.isEmpty()) {
            list.add(maidInfo.copy());
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
        if (!stack.hasTag()) return false;
        CompoundTag tag = stack.getTag();
        return tag != null
                && tag.contains(MULTI_MAIDS_KEY, Tag.TAG_LIST)
                && !tag.getList(MULTI_MAIDS_KEY, Tag.TAG_COMPOUND).isEmpty();
    }
}
