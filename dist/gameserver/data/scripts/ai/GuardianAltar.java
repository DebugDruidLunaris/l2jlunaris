package ai;

import java.util.List;

import jts.commons.util.Rnd;
import jts.gameserver.ai.CtrlEvent;
import jts.gameserver.ai.DefaultAI;
import jts.gameserver.data.xml.holder.NpcHolder;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.SimpleSpawner;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.scripts.Functions;
import jts.gameserver.utils.Location;

public class GuardianAltar extends DefaultAI
{
	private static final int DarkShamanVarangka = 18808;

	public GuardianAltar(NpcInstance actor)
	{
		super(actor);
		actor.setIsInvul(true);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if(attacker == null)
			return;

		Player player = attacker.getPlayer();

		if(Rnd.chance(40) && player.getInventory().destroyItemByItemId(14848, 1L))
		{
			List<NpcInstance> around = actor.getAroundNpc(1500, 300);
			if(around != null && !around.isEmpty())
				for(NpcInstance npc : around)
					if(npc.getNpcId() == 18808)
					{
						Functions.npcSayCustomMessage(getActor(), "scripts.ai.GuardianAltar");
						return;
					}

			try
			{
				SimpleSpawner sp = new SimpleSpawner(NpcHolder.getInstance().getTemplate(DarkShamanVarangka));
				sp.setLoc(Location.findPointToStay(actor, 400, 420));
				NpcInstance npc = sp.doSpawn(true);
				if(attacker.isPet() || attacker.isSummon())
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Rnd.get(2, 100));
				npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker.getPlayer(), Rnd.get(1, 100));
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}

		}
		else if(Rnd.chance(5))
		{
			List<NpcInstance> around = actor.getAroundNpc(1000, 300);
			if(around != null && !around.isEmpty())
				for(NpcInstance npc : around)
					if(npc.getNpcId() == 22702)
						return;

			for(int i = 0; i < 2; i++)
				try
				{
					SimpleSpawner sp = new SimpleSpawner(NpcHolder.getInstance().getTemplate(22702));
					sp.setLoc(Location.findPointToStay(actor, 150, 160));
					NpcInstance npc = sp.doSpawn(true);
					if(attacker.isPet() || attacker.isSummon())
						npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Rnd.get(2, 100));
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker.getPlayer(), Rnd.get(1, 100));
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
		}
		return;
	}

	@Override
	protected boolean randomWalk()
	{
		return false;
	}
}