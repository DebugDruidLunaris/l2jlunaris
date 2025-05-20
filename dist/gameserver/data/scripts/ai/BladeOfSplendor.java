package ai;
 
import jts.commons.util.Rnd;
import jts.gameserver.data.xml.holder.NpcHolder;
import jts.gameserver.ai.CtrlEvent;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.instances.MonsterInstance;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.scripts.Functions;

 
public class BladeOfSplendor extends RndTeleportFighter
{
	private static final int[] CLONES = { 21525 };

	private boolean _firstTimeAttacked = true;

	public BladeOfSplendor(NpcInstance actor)
	{
		super(actor);
		this.AI_TASK_ATTACK_DELAY = 1000;
		this.AI_TASK_ACTIVE_DELAY = 100000;
	}

	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if (actor == null)
			return;
		if ((!actor.isDead()) && (this._firstTimeAttacked))
		{
			this._firstTimeAttacked = false;
			Functions.npcSay(actor, "Now I Know Why You Wanna Hate Me");
			for (int bro : CLONES)
				try
			{
					MonsterInstance npc = (MonsterInstance) NpcHolder.getInstance().getTemplate(bro).getNewInstance();
					npc.setSpawnedLoc(((MonsterInstance)actor).getMinionPosition());
					npc.setReflection(actor.getReflection());
					npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp(), true);
					npc.spawnMe(npc.getSpawnedLoc());
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Integer.valueOf(Rnd.get(1, 1000)));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		super.onEvtAttacked(attacker, damage);
	}

	protected void onEvtDead(Player killer)
	{
		this._firstTimeAttacked = true;
		super.onEvtDead(killer);
	}
}