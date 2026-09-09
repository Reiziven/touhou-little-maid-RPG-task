package studio.fantasyit.maid_rpg_task.util;

import net.minecraft.network.chat.Component;

public class TranslateUtil {
    public static Component getBooleanTranslate(boolean b) {
        return b ? Component.translatable("gui.maid_rpg_task.yes")
                 : Component.translatable("gui.maid_rpg_task.no");
    }
}
