package studio.fantasyit.maid_rpg_task.condition;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import studio.fantasyit.maid_rpg_task.Config;
import studio.fantasyit.maid_rpg_task.MaidRpgTask;

public class BaubleCraftableCondition implements ICondition {

    public static final ResourceLocation ID = new ResourceLocation(MaidRpgTask.MODID, "bauble_craftable");

    private final String bauble;

    public BaubleCraftableCondition(String bauble) {
        this.bauble = bauble;
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean test(IContext context) {
        return switch (bauble) {
            case "master_soul_spell" -> Config.MASTER_SOUL_SPELL_CRAFTABLE.get();
            case "master_soul_spell_alt" -> Config.MASTER_SOUL_SPELL_ALT_CRAFTABLE.get();
            default -> true;
        };
    }

    public static class Serializer implements IConditionSerializer<BaubleCraftableCondition> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public ResourceLocation getID() {
            return ID;
        }

        @Override
        public BaubleCraftableCondition read(JsonObject json) {
            return new BaubleCraftableCondition(json.get("bauble").getAsString());
        }

        @Override
        public void write(JsonObject json, BaubleCraftableCondition value) {
            json.addProperty("bauble", value.bauble);
        }
    }
}
