package quests;

import jts.commons.util.Rnd;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

public class _295_DreamsOfTheSkies extends Quest implements ScriptFile
{
	public static int FLOATING_STONE = 1492;
	public static int RING_OF_FIREFLY = 1509;

	public static int Arin = 30536;
	public static int MagicalWeaver = 20153;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _295_DreamsOfTheSkies()
	{
		super(false);

		addStartNpc(Arin);
		addTalkId(Arin);
		addKillId(MagicalWeaver);

		addQuestItem(FLOATING_STONE);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("elder_arin_q0295_03.htm"))
		{
			st.setCond(1);
			st.setState(STARTED);
			st.soundEffect(SOUND_ACCEPT);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int id = st.getState();

		if(id == CREATED)
			st.setCond(0);
		int cond = st.getCond();
		if(cond == 0)
		{
			if(st.getPlayer().getLevel() >= 11)
			{
				htmltext = "elder_arin_q0295_02.htm";
				return htmltext;
			}
			htmltext = "elder_arin_q0295_01.htm";
			st.exitQuest(true);
		}
		else if(cond == 1 || st.ownItemCount(FLOATING_STONE) < 50)
			htmltext = "elder_arin_q0295_04.htm";
		else if(cond == 2 && st.ownItemCount(FLOATING_STONE) == 50)
		{
			st.addExpAndSp(0, 500);
			st.soundEffect(SOUND_FINISH);
			st.exitQuest(true);
			if(st.ownItemCount(RING_OF_FIREFLY) < 1)
			{
				htmltext = "elder_arin_q0295_05.htm";
				st.giveItems(RING_OF_FIREFLY, 1);
			}
			else
			{
				htmltext = "elder_arin_q0295_06.htm";
				st.giveItems(ADENA_ID, 2400);
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 1 && st.ownItemCount(FLOATING_STONE) < 50)
			if(Rnd.chance(25))
			{
				st.giveItems(FLOATING_STONE, 1);
				if(st.ownItemCount(FLOATING_STONE) == 50)
				{
					st.soundEffect(SOUND_MIDDLE);
					st.setCond(2);
				}
				else
					st.soundEffect(SOUND_ITEMGET);
			}
			else if(st.ownItemCount(FLOATING_STONE) >= 48)
			{
				st.giveItems(FLOATING_STONE, 50 - st.ownItemCount(FLOATING_STONE));
				st.soundEffect(SOUND_MIDDLE);
				st.setCond(2);
			}
			else
			{
				st.giveItems(FLOATING_STONE, 2);
				st.soundEffect(SOUND_ITEMGET);
			}
		return null;
	}
}