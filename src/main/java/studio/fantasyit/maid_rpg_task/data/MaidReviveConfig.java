package studio.fantasyit.maid_rpg_task.data;

import com.github.tartaricacid.touhoulittlemaid.api.entity.data.TaskDataKey;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import studio.fantasyit.maid_rpg_task.MaidRpgTask;

public class MaidReviveConfig implements TaskDataKey<MaidReviveConfig.Data> {
    public static final class Data implements IConfigSetter {
        private boolean ownerOnly;
        private boolean healOwner;
        private boolean healAllies;
        private boolean healSelf;
        private boolean buffOwner;
        private boolean buffAllies;
        private boolean buffSelf;
        private boolean feedOwner;

        public Data(boolean ownerOnly, boolean healOwner, boolean healAllies, boolean healSelf,
                     boolean buffOwner, boolean buffAllies, boolean buffSelf, boolean feedOwner) {
            this.ownerOnly = ownerOnly;
            this.healOwner = healOwner;
            this.healAllies = healAllies;
            this.healSelf = healSelf;
            this.buffOwner = buffOwner;
            this.buffAllies = buffAllies;
            this.buffSelf = buffSelf;
            this.feedOwner = feedOwner;
        }

        public static Data getDefault() {
            return new Data(false, true, true, true, true, true, true, false);
        }

        public boolean ownerOnly() {
            return ownerOnly;
        }

        public void ownerOnly(boolean ownerOnly) {
            this.ownerOnly = ownerOnly;
        }

        public boolean healOwner() {
            return healOwner;
        }

        public void healOwner(boolean healOwner) {
            this.healOwner = healOwner;
        }

        public boolean healAllies() {
            return healAllies;
        }

        public void healAllies(boolean healAllies) {
            this.healAllies = healAllies;
        }

        public boolean healSelf() {
            return healSelf;
        }

        public void healSelf(boolean healSelf) {
            this.healSelf = healSelf;
        }

        public boolean buffOwner() {
            return buffOwner;
        }

        public void buffOwner(boolean buffOwner) {
            this.buffOwner = buffOwner;
        }

        public boolean buffAllies() {
            return buffAllies;
        }

        public void buffAllies(boolean buffAllies) {
            this.buffAllies = buffAllies;
        }

        public boolean buffSelf() {
            return buffSelf;
        }

        public void buffSelf(boolean buffSelf) {
            this.buffSelf = buffSelf;
        }

        public boolean feedOwner() {
            return feedOwner;
        }

        public void feedOwner(boolean feedOwner) {
            this.feedOwner = feedOwner;
        }

        @Override
        public void setConfigValue(String name, String value) {
            boolean v = Boolean.parseBoolean(value);
            switch (name) {
                case "ownerOnly":
                    ownerOnly = v;
                    break;
                case "healOwner":
                    healOwner = v;
                    break;
                case "healAllies":
                    healAllies = v;
                    break;
                case "healSelf":
                    healSelf = v;
                    break;
                case "buffOwner":
                    buffOwner = v;
                    break;
                case "buffAllies":
                    buffAllies = v;
                    break;
                case "buffSelf":
                    buffSelf = v;
                    break;
                case "feedOwner":
                    feedOwner = v;
                    break;
            }
        }
    }

    public static TaskDataKey<Data> KEY = null;
    public static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(MaidRpgTask.MODID, "revive");

    @Override
    public ResourceLocation getKey() {
        return LOCATION;
    }

    @Override
    public CompoundTag writeSaveData(Data data) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("ownerOnly", data.ownerOnly);
        tag.putBoolean("healOwner", data.healOwner);
        tag.putBoolean("healAllies", data.healAllies);
        tag.putBoolean("healSelf", data.healSelf);
        tag.putBoolean("buffOwner", data.buffOwner);
        tag.putBoolean("buffAllies", data.buffAllies);
        tag.putBoolean("buffSelf", data.buffSelf);
        tag.putBoolean("feedOwner", data.feedOwner);
        return tag;
    }

    @Override
    public Data readSaveData(CompoundTag compound) {
        Data def = Data.getDefault();
        return new Data(
                compound.getBoolean("ownerOnly"),
                compound.contains("healOwner") ? compound.getBoolean("healOwner") : def.healOwner(),
                compound.contains("healAllies") ? compound.getBoolean("healAllies") : def.healAllies(),
                compound.contains("healSelf") ? compound.getBoolean("healSelf") : def.healSelf(),
                compound.contains("buffOwner") ? compound.getBoolean("buffOwner") : def.buffOwner(),
                compound.contains("buffAllies") ? compound.getBoolean("buffAllies") : def.buffAllies(),
                compound.contains("buffSelf") ? compound.getBoolean("buffSelf") : def.buffSelf(),
                compound.contains("feedOwner") ? compound.getBoolean("feedOwner") : def.feedOwner()
        );
    }
}
