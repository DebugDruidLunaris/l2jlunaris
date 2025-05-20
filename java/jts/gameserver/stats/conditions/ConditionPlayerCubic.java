package jts.gameserver.stats.conditions;

import jts.gameserver.cache.Msg;
import jts.gameserver.model.Player;
import jts.gameserver.stats.Env;
import jts.gameserver.stats.Stats;

public class ConditionPlayerCubic extends Condition
{
	private int _id;

	public ConditionPlayerCubic(int id)
	{
		_id = id;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		if(env.target == null || !env.target.isPlayer())
			return false;

		Player targetPlayer = (Player) env.target;
		if(targetPlayer.getCubic(_id) != null)
			return true;

		int size = (int) targetPlayer.calcStat(Stats.CUBICS_LIMIT, 1);
		if(targetPlayer.getCubics().size() >= size)
		{
			if(env.character == targetPlayer)
				targetPlayer.sendPacket(Msg.CUBIC_SUMMONING_FAILED); //todo un hard code it

			return false;
		}

		return true;
	}
}