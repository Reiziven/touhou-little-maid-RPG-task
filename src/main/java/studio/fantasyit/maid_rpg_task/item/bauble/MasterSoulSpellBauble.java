package studio.fantasyit.maid_rpg_task.item.bauble;

import com.github.tartaricacid.touhoulittlemaid.api.bauble.IMaidBauble;

/**
 * Bauble behavior for Master Soul Spell.
 * Registered with BaubleManager so the item is accepted in the maid bauble slot.
 * The actual store/summon logic lives in MasterSoulSpellEvent.
 */
public class MasterSoulSpellBauble implements IMaidBauble {
    // No tick behavior — everything is driven by SmartSlab events in MasterSoulSpellEvent.
}
