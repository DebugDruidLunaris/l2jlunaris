package quests;

import jts.gameserver.model.Player;
import jts.gameserver.model.entity.olympiad.OlympiadGame;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

public class _551_OlympiadStarter extends Quest implements ScriptFile
{
	// NPCs
	private static final int OLYMPIAD_MANAGER = 31688;

	// Items
	private static final int MEDAL_OF_GLORY = 21874;
	private static final int OLYMPIAD_CHEST = 17169;
	private static final int OLYMPIAD_CERT1 = 17238;
	private static final int OLYMPIAD_CERT2 = 17239;
	private static final int OLYMPIAD_CERT3 = 17240;

	public _551_OlympiadStarter()
	{
		super(false);

		addStartNpc(OLYMPIAD_MANAGER);
		addTalkId(OLYMPIAD_MANAGER);
		addQuestItem(OLYMPIAD_CERT1, OLYMPIAD_CERT2, OLYMPIAD_CERT3);
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		switch(npcId)
		{
			case OLYMPIAD_MANAGER:
				Player player = st.getPlayer();
				if(!player.isNoble() || player.getLevel() < 75 || player.getClassId().getLevel() < 4)
					return "olympiad_operator_q0551_08.htm";

				if(st.isCreated())
				{
					if(st.isDailyQuest())
						return "olympiad_operator_q0551_01.htm";
					else
						return "olympiad_operator_q0551_06.htm";
				}
				else if(st.isStarted())
				{
					if(st.ownItemCount(OLYMPIAD_CERT1, OLYMPIAD_CERT2, OLYMPIAD_CERT3) == 0)
						return "olympiad_operator_q0551_04.htm";

					if(st.ownItemCount(OLYMPIAD_CERT3) > 0)
					{
						st.giveItems(OLYMPIAD_CHEST, 4);
						st.giveItems(MEDAL_OF_GLORY, 5);
						st.takeItems(OLYMPIAD_CERT1, -1);
						st.takeItems(OLYMPIAD_CERT2, -1);
						st.takeItems(OLYMPIAD_CERT3, -1);
						st.soundEffect(SOUND_FINISH);
						st.exitQuest(this);
						return "olympiad_operator_q0551_07.htm";
					}
					else
						return "olympiad_operator_q0551_05.htm";
				}
				break;
		}

		return null;
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("olympiad_operator_q0551_03.htm"))
		{
			st.setCond(1);
			st.setState(STARTED);
			st.soundEffect(SOUND_ACCEPT);
		}
		else if(event.equalsIgnoreCase("olympiad_operator_q0551_07.htm"))
			if(st.ownItemCount(OLYMPIAD_CERT3) > 0)
			{
				st.giveItems(OLYMPIAD_CHEST, 4);
				st.giveItems(MEDAL_OF_GLORY, 5);
				st.takeItems(OLYMPIAD_CERT1, -1);
				st.takeItems(OLYMPIAD_CERT2, -1);
				st.takeItems(OLYMPIAD_CERT3, -1);
				st.soundEffect(SOUND_FINISH);
				st.exitQuest(this);
			}
			else if(st.ownItemCount(OLYMPIAD_CERT2) > 0)
			{
				st.giveItems(OLYMPIAD_CHEST, 2);
				st.giveItems(MEDAL_OF_GLORY, 3); // от балды
				st.takeItems(OLYMPIAD_CERT1, -1);
				st.takeItems(OLYMPIAD_CERT2, -1);
				st.takeItems(OLYMPIAD_CERT3, -1);
				st.soundEffect(SOUND_FINISH);
				st.exitQuest(this);
			}
			else if(st.ownItemCount(OLYMPIAD_CERT1) > 0)
			{
				st.giveItems(OLYMPIAD_CHEST, 1);
				//st.giveItems(MEDAL_OF_GLORY, 5); ??
				st.takeItems(OLYMPIAD_CERT1, -1);
				st.takeItems(OLYMPIAD_CERT2, -1);
				st.takeItems(OLYMPIAD_CERT3, -1);
				st.soundEffect(SOUND_FINISH);
				st.exitQuest(this);
			}
		return event;
	}

	@Override
	public void onOlympiadEnd(OlympiadGame og, QuestState qs)
	{
		if(qs.getCond() == 1)
		{
			int count = qs.getInt("count") + 1;
			qs.setMemoState("count", count);
			if(count == 3)
			{
				qs.giveItems(OLYMPIAD_CERT1, 1);
				qs.soundEffect(SOUND_ITEMGET);
			}
			else if(count == 5)
			{
				qs.giveItems(OLYMPIAD_CERT2, 1);
				qs.soundEffect(SOUND_ITEMGET);
			}
			else if(count == 10)
			{
				qs.giveItems(OLYMPIAD_CERT3, 1);
				qs.setCond(2);
				qs.soundEffect(SOUND_MIDDLE);
			}
		}
	}

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}
}