package quests;

import jts.gameserver.model.base.Race;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

public class _161_FruitsOfMothertree extends Quest implements ScriptFile
{
	private static final int ANDELLRIAS_LETTER_ID = 1036;
	private static final int MOTHERTREE_FRUIT_ID = 1037;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _161_FruitsOfMothertree()
	{
		super(false);

		addStartNpc(30362);
		addTalkId(30371);

		addQuestItem(new int[] { MOTHERTREE_FRUIT_ID, ANDELLRIAS_LETTER_ID });
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("1"))
		{
			st.setMemoState("id", "0");
			htmltext = "30362-04.htm";
			st.giveItems(ANDELLRIAS_LETTER_ID, 1);
			st.setCond(1);
			st.setState(STARTED);
			st.soundEffect(SOUND_ACCEPT);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = "noquest";
		int id = st.getState();
		if(id == CREATED)
		{
			st.setState(STARTED);
			st.setCond(0);
			st.setMemoState("id", "0");
		}
		if(npcId == 30362 && st.getCond() == 0)
		{
			if(st.getCond() < 15)
			{
				if(st.getPlayer().getRace() != Race.elf)
					htmltext = "30362-00.htm";
				else if(st.getPlayer().getLevel() >= 3)
					return "30362-03.htm";
				else
				{
					htmltext = "30362-02.htm";
					st.exitQuest(true);
				}
			}
			else
			{
				htmltext = "30362-02.htm";
				st.exitQuest(true);
			}
		}
		else if(npcId == 30362 && st.getCond() > 0)
		{
			if(st.ownItemCount(ANDELLRIAS_LETTER_ID) == 1 && st.ownItemCount(MOTHERTREE_FRUIT_ID) == 0)
				htmltext = "30362-05.htm";
			else if(st.ownItemCount(MOTHERTREE_FRUIT_ID) == 1)
			{
				htmltext = "30362-06.htm";
				st.giveItems(ADENA_ID, 1000);
				st.addExpAndSp(1000, 0);
				st.takeItems(MOTHERTREE_FRUIT_ID, 1);
				st.soundEffect(SOUND_FINISH);
				st.exitQuest(false);
			}
		}
		else if(npcId == 30371 && st.getCond() == 1)
			if(st.ownItemCount(ANDELLRIAS_LETTER_ID) == 1)
			{
				if(st.getInt("id") != 161)
				{
					st.setMemoState("id", "161");
					htmltext = "30371-01.htm";
					st.giveItems(MOTHERTREE_FRUIT_ID, 1);
					st.takeItems(ANDELLRIAS_LETTER_ID, 1);
				}
			}
			else if(st.ownItemCount(MOTHERTREE_FRUIT_ID) == 1)
				htmltext = "30371-02.htm";
		return htmltext;
	}
}