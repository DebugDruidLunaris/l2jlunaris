package npc.model.residences.dominion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jts.commons.util.Rnd;
import jts.gameserver.Config;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.ai.CtrlEvent;
import jts.gameserver.model.AggroList;
import jts.gameserver.model.Creature;
import jts.gameserver.model.GameObjectTasks;
import jts.gameserver.model.Playable;
import jts.gameserver.model.Player;
import jts.gameserver.model.entity.events.impl.DominionSiegeEvent;
import jts.gameserver.model.instances.residences.SiegeToggleNpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestEventType;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.templates.npc.NpcTemplate;

@SuppressWarnings("serial")
public class CatapultInstance extends SiegeToggleNpcInstance
{
	public CatapultInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onDeathImpl(Creature lastAttacker)
	{
		DominionSiegeEvent siegeEvent = getEvent(DominionSiegeEvent.class);
		if(siegeEvent == null)
			return;

		ThreadPoolManager.getInstance().execute(new GameObjectTasks.NotifyAITask(this, CtrlEvent.EVT_DEAD, lastAttacker, null));

		Player killer = lastAttacker.getPlayer();
		if(killer == null)
			return;

		Map<Playable, AggroList.HateInfo> aggroMap = getAggroList().getPlayableMap();

		Quest[] quests = getTemplate().getEventQuests(QuestEventType.MOB_KILLED_WITH_QUEST);
		if(quests != null && quests.length > 0)
		{
			List<Player> players = null; // массив с игроками, которые могут быть заинтересованы в квестах
			if(isRaid() && Config.ALT_NO_LASTHIT) // Для альта на ластхит берем всех игроков вокруг
			{
				players = new ArrayList<Player>();
				for(Playable pl : aggroMap.keySet())
					if(!pl.isDead() && (isInRangeZ(pl, Config.ALT_PARTY_DISTRIBUTION_RANGE) || killer.isInRangeZ(pl, Config.ALT_PARTY_DISTRIBUTION_RANGE)))
						players.add(pl.getPlayer());
			}
			else if(killer.getParty() != null) // если пати то собираем всех кто подходит
			{
				players = new ArrayList<Player>(killer.getParty().getMemberCount());
				for(Player pl : killer.getParty().getPartyMembers())
					if(!pl.isDead() && (isInRangeZ(pl, Config.ALT_PARTY_DISTRIBUTION_RANGE) || killer.isInRangeZ(pl, Config.ALT_PARTY_DISTRIBUTION_RANGE)))
						players.add(pl);
			}

			for(Quest quest : quests)
			{
				Player toReward = killer;
				if(quest.getParty() != Quest.PARTY_NONE && players != null)
					if(isRaid() || quest.getParty() == Quest.PARTY_ALL) // если цель рейд или квест для всей пати награждаем всех участников
					{
						for(Player pl : players)
						{
							QuestState qs = pl.getQuestState(quest.getName());
							if(qs != null && !qs.isCompleted())
								quest.notifyKill(this, qs);
						}
						toReward = null;
					}
					else
					{ // иначе выбираем одного
						List<Player> interested = new ArrayList<Player>(players.size());
						for(Player pl : players)
						{
							QuestState qs = pl.getQuestState(quest.getName());
							if(qs != null && !qs.isCompleted()) // из тех, у кого взят квест
								interested.add(pl);
						}

						if(interested.isEmpty())
							continue;

						toReward = interested.get(Rnd.get(interested.size()));
						if(toReward == null)
							toReward = killer;
					}

				if(toReward != null)
				{
					QuestState qs = toReward.getQuestState(quest.getName());
					if(qs != null && !qs.isCompleted())
						quest.notifyKill(this, qs);
				}
			}
		}
	}
}