package studio.fantasyit.maid_rpg_task.data;

import com.github.tartaricacid.touhoulittlemaid.api.entity.data.TaskDataKey;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import studio.fantasyit.maid_rpg_task.MaidRpgTask;

public class MaidMageData implements TaskDataKey<MaidMageData.Data> {

    public static final class Data {
        private int elementalCooldown;
        private int spellIndex;
        private int shieldCooldown;
        private int shieldHp; // stored ×10, -1 = full (not yet initialized)

        public Data(int elementalCooldown, int spellIndex, int shieldCooldown, int shieldHp) {
            this.elementalCooldown = elementalCooldown;
            this.spellIndex = spellIndex;
            this.shieldCooldown = shieldCooldown;
            this.shieldHp = shieldHp;
        }

        public static Data getDefault() { return new Data(0, 0, 0, -1); }

        public int getElementalCooldown()          { return elementalCooldown; }
        public void setElementalCooldown(int v)    { this.elementalCooldown = v; }
        public int getSpellIndex()                 { return spellIndex; }
        public void setSpellIndex(int v)           { this.spellIndex = v; }
        public int getShieldCooldown()             { return shieldCooldown; }
        public void setShieldCooldown(int v)       { this.shieldCooldown = v; }
        public int getShieldHp()                   { return shieldHp; }
        public void setShieldHp(int v)             { this.shieldHp = v; }
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
        tag.putInt("shieldCooldown", data.shieldCooldown);
        tag.putInt("shieldHp", data.shieldHp);
        return tag;
    }

    @Override
    public Data readSaveData(CompoundTag compound) {
        return new Data(
                compound.getInt("elementalCooldown"),
                compound.getInt("spellIndex"),
                compound.getInt("shieldCooldown"),
                compound.contains("shieldHp") ? compound.getInt("shieldHp") : -1);
    }
}
