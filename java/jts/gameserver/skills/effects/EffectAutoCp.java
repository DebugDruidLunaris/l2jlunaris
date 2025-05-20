package jts.gameserver.skills.effects;

import jts.gameserver.model.Effect;
import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.SystemMessage;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.skills.EffectType;
import jts.gameserver.stats.Env;
import jts.gameserver.utils.ItemFunctions;

public final class EffectAutoCp extends Effect
{
	public EffectAutoCp(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	public void onStart()
	{
		super.onStart();	
		Player player = (Player) getEffected();
		
		if(player.isInOlympiadMode())
		{
			player.sendPacket(SystemMsg.YOU_CANNOT_USE_THAT_SKILL_IN_A_GRAND_OLYMPIAD_MATCH);
			player.getEffectList().stopEffects(EffectType.AutoCp);
		}
		else if(ItemFunctions.getItemCount(player, 5592) == 0 && ItemFunctions.getItemCount(player, 5591) == 0)
		{
			player.sendPacket(SystemMsg.THERE_ARE_NOT_ENOUGH_NECESSARY_ITEMS_TO_USE_THE_SKILL);
			player.getEffectList().stopEffects(EffectType.AutoCp);
		}
		else
			player.runAutoCPUse();
	}

	public void onExit()
	{
		super.onExit();
		Player player = (Player) getEffected();
			
		player.endAutoCPUse();
		player.sendPacket(new SystemMessage(SystemMessage.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(_displayId, _displayLevel));
	}

	public boolean onActionTime()
	{
		if(getEffected().isDead())
			return false;

		return true;
	}
}