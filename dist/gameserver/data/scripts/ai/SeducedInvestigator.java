package ai;

import java.util.List;

import jts.commons.util.Rnd;
import jts.gameserver.ai.CtrlEvent;
import jts.gameserver.ai.Fighter;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.World;
import jts.gameserver.model.entity.Reflection;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.network.serverpackets.ExShowScreenMessage;
import jts.gameserver.network.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import jts.gameserver.tables.SkillTable;

import org.apache.commons.lang3.ArrayUtils;

public class SeducedInvestigator extends Fighter
{
	private int[] _allowedTargets = { 25659, 25660, 25661, 25662, 25663, 25664 };
	private long _reuse = 0;

	public SeducedInvestigator(NpcInstance actor)
	{
		super(actor);
		actor.startImmobilized();
		actor.startHealBlocked();
		AI_TASK_ACTIVE_DELAY = 5000;
	}

	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(actor.isDead())
			return false;

		List<NpcInstance> around = actor.getAroundNpc(1000, 300);
		if(around != null && !around.isEmpty())
			for(NpcInstance npc : around)
				if(ArrayUtils.contains(_allowedTargets, npc.getNpcId()))
					actor.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, npc, 300);

		if(Rnd.chance(0.1) && _reuse + 30000 < System.currentTimeMillis())
		{
			List<Player> players = World.getAroundPlayers(actor, 500, 200);
			if(players == null || players.size() < 1)
				return false;
			Player player = players.get(Rnd.get(players.size()));
			if(player.getReflectionId() == actor.getReflectionId())
			{
				_reuse = System.currentTimeMillis();
				int[] buffs = { 5970, 5971, 5972, 5973 };
				if(actor.getNpcId() == 36562)
					actor.doCast(SkillTable.getInstance().getInfo(buffs[0], 1), player, true);
				else if(actor.getNpcId() == 36563)
					actor.doCast(SkillTable.getInstance().getInfo(buffs[1], 1), player, true);
				else if(actor.getNpcId() == 36564)
					actor.doCast(SkillTable.getInstance().getInfo(buffs[2], 1), player, true);
				else
					actor.doCast(SkillTable.getInstance().getInfo(buffs[3], 1), player, true);
			}
		}

		return true;
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		NpcInstance actor = getActor();
		Reflection r = actor.getReflection();
		List<Player> players = r.getPlayers();
		for(Player p : players)
			p.sendPacket(new ExShowScreenMessage("The Investigator has been killed. The mission is failed.", 3000, ScreenMessageAlign.TOP_CENTER, true));

		r.startCollapseTimer(5 * 1000L);

		super.onEvtDead(killer);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if(attacker == null)
			return;

		if(attacker.isPlayable())
			return;

		if(attacker.getNpcId() == 25659 || attacker.getNpcId() == 25660 || attacker.getNpcId() == 25661)
			actor.getAggroList().addDamageHate(attacker, 0, 20);

		super.onEvtAttacked(attacker, damage);
	}

	@Override
	protected void onEvtAggression(Creature target, int aggro)
	{
		if(target.isPlayer() || target.isPet() || target.isSummon())
			return;

		super.onEvtAggression(target, aggro);
	}

	@Override
	public boolean checkAggression(Creature target)
	{
		if(target.isPlayable())
			return false;

		return super.checkAggression(target);
	}
}