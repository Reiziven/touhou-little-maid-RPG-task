package studio.fantasyit.maid_rpg_task.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ICondition;
import studio.fantasyit.maid_rpg_task.Config;
import studio.fantasyit.maid_rpg_task.MaidRpgTask;

public record BaubleCraftableCondition(String bauble) implements ICondition {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(MaidRpgTask.MODID, "bauble_craftable");

    public static final MapCodec<BaubleCraftableCondition> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.STRING.fieldOf("bauble").forGetter(BaubleCraftableCondition::bauble)
            ).apply(instance, BaubleCraftableCondition::new));

    @Override
    public boolean test(ICondition.IContext context) {
        return switch (bauble) {
            case "master_soul_spell"     -> Config.MASTER_SOUL_SPELL_CRAFTABLE.get();
            case "master_soul_spell_alt" -> Config.MASTER_SOUL_SPELL_ALT_CRAFTABLE.get();
            default -> true;
        };
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
