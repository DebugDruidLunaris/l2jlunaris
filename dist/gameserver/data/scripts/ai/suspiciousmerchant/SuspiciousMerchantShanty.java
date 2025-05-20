//
// Suspicious Merchant - Shanty Fortress (35659).
//
package ai.suspiciousmerchant;

import jts.commons.util.Rnd;
import jts.gameserver.ai.DefaultAI;
import jts.gameserver.model.Creature;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.scripts.Functions;
import jts.gameserver.utils.Location;

public class SuspiciousMerchantShanty extends DefaultAI
{
	static final Location[] points = 
	{
		new Location(-58672, 154703, -2688),
		new Location(-58672, 154703, -2688),
		new Location(-57522, 156523, -2576),
		new Location(-55226, 157117, -2064),
		new Location(-57528, 156515, -2576),
		new Location(-58660, 154706, -2688),
		new Location(-60174, 156182, -2832),
		new Location(-61834, 157703, -3264),
		new Location(-62761, 159101, -3584),
		new Location(-63472, 159672, -3680),
		new Location(-64072, 160631, -3760),
		new Location(-64387, 161877, -3792),
		new Location(-63842, 163092, -3840),
		new Location(-64397, 161831, -3792),
		new Location(-64055, 160587, -3760),
		new Location(-63461, 159656, -3680),
		new Location(-62744, 159095, -3584),
		new Location(-61831, 157693, -3256),
		new Location(-60152, 156167, -2824),
		new Location(-58652, 154707, -2688) 
	};

	private int current_point = -1;
	private long wait_timeout = 0;
	private boolean wait = false;

	public SuspiciousMerchantShanty(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	public boolean isGlobalAI()
	{
		return true;
	}

	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(actor.isDead())
			return true;

		if(_def_think)
		{
			doTask();
			return true;
		}

		if(actor.isMoving)
			return true;

		if(System.currentTimeMillis() > wait_timeout && (current_point > -1 || Rnd.chance(5)))
		{
			if(!wait)
				switch(current_point)
				{
					case 0:
						wait_timeout = System.currentTimeMillis() + 30000;
						wait = true;
						return true;
					case 2:
						wait_timeout = System.currentTimeMillis() + 15000;
						switch(Rnd.get(4))
						{
							case 0:
								Functions.npcSayCustomMessage(getActor(), "scripts.ai.SuspiciousMerchantShanty.1");
								break;
							case 1:
								Functions.npcSayCustomMessage(getActor(), "scripts.ai.SuspiciousMerchantShanty.2");
								break;
							case 2:
								Functions.npcSayCustomMessage(getActor(), "scripts.ai.SuspiciousMerchantShanty.3");
								break;
							case 3:
								Functions.npcSayCustomMessage(getActor(), "scripts.ai.SuspiciousMerchantShanty.4");
								break;
						}
						wait = true;
						return true;
					case 12:
						wait_timeout = System.currentTimeMillis() + 15000;
						Functions.npcSayCustomMessage(getActor(), "scripts.ai.SuspiciousMerchantShanty.5");
						wait = true;
						return true;
				}
			else
				switch(current_point)
				{
					case 0:
						Functions.npcSayCustomMessage(getActor(), "scripts.ai.SuspiciousMerchantShanty.6");
						break;
					case 2:
						Functions.npcSayCustomMessage(getActor(), "scripts.ai.SuspiciousMerchantShanty.7");
						break;
					case 12:
						Functions.npcSayCustomMessage(getActor(), "scripts.ai.SuspiciousMerchantShanty.8");
						break;
				}

			wait_timeout = 0;
			wait = false;
			current_point++;

			if(current_point >= points.length)
				current_point = 0;

			addTaskMove(points[current_point], false);
			doTask();
			return true;
		}

		if(randomAnimation())
			return true;

		return false;
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage) {}

	@Override
	protected void onEvtAggression(Creature target, int aggro) {}
}