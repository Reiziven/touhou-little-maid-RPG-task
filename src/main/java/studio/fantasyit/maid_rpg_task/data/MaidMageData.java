package studio.fantasyit.maid_rpg_task.data;

import com.github.tartaricacid.touhoulittlemaid.api.entity.data.TaskDataKey;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import studio.fantasyit.maid_rpg_task.MaidRpgTask;

public class MaidMageData implements TaskDataKey<MaidMageData.Data> {

    public static final class Data {
        private int elementalCooldown;
        private int spellIndex;

        public Data(int elementalCooldown, int spellIndex) {
            this.elementalCooldown = elementalCooldown;
            this.spellIndex = spellIndex;
        }

        public static Data getDefault() {
            return new Data(0, 0);
        }

        public int getElementalCooldown() {
            return elementalCooldown;
        }

        public void setElementalCooldown(int cooldown) {
            this.elementalCooldown = cooldown;
        }

        public int getSpellIndex() {
            return spellIndex;
        }

        public void setSpellIndex(int index) {
            this.spellIndex = index;
        }
    }

    public static TaskDataKey<Data> KEY = null;
    public static final ResourceLocation LOCATION = new ResourceLocation(MaidRpgTask.MODID, "mage");

    @Override
    public ResourceLocation getKey() {
        return LOCATION;
    }

    @Override
    public CompoundTag writeSaveData(Data data) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("elementalCooldown", data.elementalCooldown);
        tag.putInt("spellIndex", data.spellIndex);
        return tag;
    }

    @Override
    public Data readSaveData(CompoundTag compound) {
        return new Data(compound.getInt("elementalCooldown"), compound.getInt("spellIndex"));
    }
}
