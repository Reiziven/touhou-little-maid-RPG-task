package studio.fantasyit.maid_rpg_task.mixin;

import com.github.tartaricacid.touhoulittlemaid.item.ItemSmartSlab;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import studio.fantasyit.maid_rpg_task.event.MasterSoulSpellEvent;

/**
 * Injects directly into TLM's own useOn() rather than trying to intercept
 * PlayerInteractEvent.RightClickBlock from outside. The old event-based
 * approach had to guess whether cancelling that upstream event would
 * reliably stop ItemSmartSlab#useOn() from also running afterward — that
 * guess was the source of the "other maids vanish" bug. Injecting straight
 * into useOn() means our code and TLM's are never racing for the same
 * stack; whichever one runs, runs once, atomically, on the real call.
 */
@Mixin(ItemSmartSlab.class)
public abstract class ItemSmartSlabMixin {

    @Final
    @Shadow
    private ItemSmartSlab.Type type;

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void maidRpgTask$interceptMultiMaidSummon(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (this.type != ItemSmartSlab.Type.HAS_MAID) {
            return;
        }
        if (context.getPlayer() == null) {
            return;
        }
        if (!MasterSoulSpellEvent.hasMultiMaidData(context.getItemInHand())) {
            // Not a multi-maid slab (0 or 1 maid stored) — let TLM's original
            // useOn() run completely untouched, exactly like a normal slab.
            return;
        }
        cir.setReturnValue(MasterSoulSpellEvent.trySummonMultiMaid(context));
    }
}
