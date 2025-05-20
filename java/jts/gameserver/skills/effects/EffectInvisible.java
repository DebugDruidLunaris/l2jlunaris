package jts.gameserver.skills.effects;

import jts.gameserver.model.Creature;
import jts.gameserver.model.Effect;
import jts.gameserver.model.Player;
import jts.gameserver.model.World;
import jts.gameserver.model.base.InvisibleType;
import jts.gameserver.stats.Env;

public final class EffectInvisible extends Effect
{
	private InvisibleType _invisibleType = InvisibleType.NONE;

	public EffectInvisible(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean checkCondition()
	{
		if(!_effected.isPlayer())
			return false;
		Player player = (Player) _effected;
		if(player.isInvisible())
			return false;
		if(player.getActiveWeaponFlagAttachment() != null)
			return false;
		return super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		Player player = (Player) _effected;

		World.removeObjectFromPlayers(player);
		
		for(Creature creature : World.getAroundNpc(player, 500, 100))
		{
			if(creature.getCastingTarget() != null && creature.getCastingTarget().equals(player))
				creature.abortCast(true, true);
		}
		
		_invisibleType = player.getInvisibleType();
		player.setInvisibleType(InvisibleType.EFFECT);
	}

	@Override
	public void onExit()
	{
		super.onExit();
		Player player = (Player) _effected;
		if(!player.isInvisible())
			return;

		player.setInvisibleType(_invisibleType);

		player.broadcastUserInfo(true);
		if(player.getPet() != null)
			player.getPet().broadcastCharInfo();
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}