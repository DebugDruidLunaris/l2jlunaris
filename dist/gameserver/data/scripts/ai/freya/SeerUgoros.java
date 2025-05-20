package ai.freya;

import jts.gameserver.ai.CtrlEvent;
import jts.gameserver.ai.Mystic;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.tables.SkillTable;
import jts.gameserver.utils.Location;
import quests._288_HandleWithCare;

public class SeerUgoros extends Mystic
{
	private int _weeds = 0;
	
	// questitem
	private static final int q_scale_of_lizard_good = 15498;
	private static final int q_scale_of_lizard_highest = 15497;
	
	private static final Skill _skill = SkillTable.getInstance().getInfo(6426, 1);

	public SeerUgoros(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive()
	{
		super.thinkActive();
		if(!getActor().getReflection().isDefault() && !getActor().getReflection().getPlayers().isEmpty())
			for(Player p : getActor().getReflection().getPlayers())
				notifyEvent(CtrlEvent.EVT_AGGRESSION, p, 5000);
		return true;
	}

	@Override
	protected void thinkAttack()
	{
		NpcInstance actor = getActor();
		if(!actor.isMuted(_skill) && actor.getCurrentHpPercents() < 80)
			for(NpcInstance n : actor.getAroundNpc(2000, 300))
				if(n.getNpcId() == 18867 && !n.isDead())
				{
					actor.doCast(_skill, n, true);
					actor.setCurrentHp(actor.getMaxHp(), false);
					actor.broadcastCharInfo();
					_weeds++;
					return;
				}
		super.thinkAttack();
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		QuestState qs = killer.getPlayer().getQuestState(_288_HandleWithCare.class);
		if(qs != null && qs.getCond() == 1)
			if(_weeds < 5)
			{
				qs.giveItems(q_scale_of_lizard_highest, 1);
				qs.setCond(3);
				qs.setMemoState("take_care_please", String.valueOf(2), true);
			}
			else
			{
				qs.giveItems(q_scale_of_lizard_good, 1);
				qs.setCond(2);
				qs.setMemoState("take_care_please", String.valueOf(2), true);
			}
		_weeds = 0;
		if(!getActor().getReflection().isDefault())
			getActor().getReflection().addSpawnWithoutRespawn(32740, new Location(95688, 85688, -3757, 0), 0);
		super.onEvtDead(killer);
	}
}