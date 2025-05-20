package quests;

import jts.commons.util.Rnd;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

/**
 * Based on official H5
 * @author Magister
 */
public class _452_FindingtheLostSoldiers extends Quest implements ScriptFile
{
	// npc
	private static final int gracia_soldier_corpse1 = 32769;
	private static final int gracia_soldier_corpse2 = 32770;
	private static final int gracia_soldier_corpse3 = 32771;
	private static final int gracia_soldier_corpse4 = 32772;
	private static final int zaykhan = 32773;

	// questitem
	private static final int q_id_tag_of_gracian_soldier = 15513;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _452_FindingtheLostSoldiers()
	{
		super(false);
		addStartNpc(zaykhan);
		addTalkId(zaykhan);
		addTalkId(gracia_soldier_corpse1, gracia_soldier_corpse2, gracia_soldier_corpse3, gracia_soldier_corpse4);
		addQuestItem(q_id_tag_of_gracian_soldier);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		int GetMemoState = st.getInt("searching_for_lost_soldiers");

		if(event.equalsIgnoreCase("quest_accept"))
		{
			st.setCond(1);
			st.setMemoState("searching_for_lost_soldiers", String.valueOf(1), true);
			st.setMemoState("searching_for_lost_soldiers_ex", String.valueOf(1), true); //?
			st.setState(STARTED);
			st.soundEffect(SOUND_ACCEPT);
			htmltext = "zaykhan_q0452_05.htm";
		}
		else if(event.equalsIgnoreCase("reply_1"))
			if(GetMemoState == 1)
			{
				int i0 = Rnd.get(10);
				if(i0 < 5)
				{
					st.setCond(2);
					st.setMemoState("searching_for_lost_soldiers", String.valueOf(2), true);
					st.giveItems(q_id_tag_of_gracian_soldier, 1);
					st.soundEffect(SOUND_MIDDLE);
					htmltext = "gracia_soldier_corpse1_q0452_02.htm";
					npc.deleteMe();
				}
				else
					htmltext = "gracia_soldier_corpse1_q0452_04.htm";
			}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int GetMemoState = st.getInt("searching_for_lost_soldiers");
		int npcId = npc.getNpcId();
		int id = st.getState();

		switch(id)
		{
			case CREATED:
				if(npcId == zaykhan)
					if(st.getPlayer().getLevel() >= 84)
					{
						if(st.isDailyQuest())
							htmltext = "zaykhan_q0452_01.htm";
						else
						{
							htmltext = "zaykhan_q0452_03.htm";
							st.exitQuest(this);
						}
					}
					else
					{
						htmltext = "zaykhan_q0452_02.htm";
						st.exitQuest(this);
					}
				break;
			case STARTED:
				if(npcId == zaykhan)
				{
					if(GetMemoState == 1 && st.ownItemCount(q_id_tag_of_gracian_soldier) == 0)
						htmltext = "zaykhan_q0452_06.htm";
					else if(GetMemoState == 2 && st.ownItemCount(q_id_tag_of_gracian_soldier) == 1)
					{
						st.giveItems(ADENA_ID, 95200);
						st.addExpAndSp(435024, 50366);
						st.takeItems(q_id_tag_of_gracian_soldier, -1);
						st.removeMemo("searching_for_lost_soldiers");
						st.removeMemo("searching_for_lost_soldiers_ex");
						htmltext = "zaykhan_q0452_07.htm";
						st.soundEffect(SOUND_FINISH);
						st.exitQuest(this);
					}
				}
				else if(npcId == gracia_soldier_corpse1 || npcId == gracia_soldier_corpse2 || npcId == gracia_soldier_corpse3 || npcId == gracia_soldier_corpse4)
					if(GetMemoState == 1)
						htmltext = "gracia_soldier_corpse1_q0452_01.htm";
				break;
		}
		return htmltext;
	}
}