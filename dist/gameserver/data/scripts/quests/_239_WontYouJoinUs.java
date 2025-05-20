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
public class _239_WontYouJoinUs extends Quest implements ScriptFile
{
	// NPC
	private static final int minehr = 32643;

	// MOBS
	private static final int eggs_of_murcrokian = 18805;
	private static final int bdzlem_suppressor = 22656;
	private static final int bdzlem_exterminator = 22657;

	// ITEMS
	private static final int q_letter_of_confirmation_elf = 14866;
	private static final int q_parts_of_reclaiming_worker = 14869;
	private static final int q_piece_of_helldozer = 14870;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _239_WontYouJoinUs()
	{
		super(false);
		addStartNpc(minehr);
		addKillId(eggs_of_murcrokian, bdzlem_suppressor, bdzlem_exterminator);
		addQuestItem(q_parts_of_reclaiming_worker, q_piece_of_helldozer);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		int GetMemoState = st.getInt("Wont_you_join_us");
		if(event.equalsIgnoreCase("quest_accept") && st.ownItemCount(q_letter_of_confirmation_elf) >= 1)
		{
			st.setCond(1);
			st.setMemoState("Wont_you_join_us", String.valueOf(1), true);
			st.setState(STARTED);
			st.soundEffect(SOUND_ACCEPT);
			htmltext = "minehr_q0239_06.htm";
		}
		else if(event.equalsIgnoreCase("reply_2") && GetMemoState == 3)
		{
			st.setCond(3);
			st.setMemoState("Wont_you_join_us", String.valueOf(4), true);
			st.soundEffect(SOUND_MIDDLE);
			htmltext = "minehr_q0239_11.htm";
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int id = st.getState();
		int GetMemoState = st.getInt("Wont_you_join_us");
		switch(id)
		{
			case CREATED:
				if(npcId == minehr)
					if(st.getPlayer().getLevel() < 82 || !st.getPlayer().isQuestCompleted(_237_WindsOfChange.class))
						htmltext = "minehr_q0239_03.htm";
					else
						htmltext = "minehr_q0239_01.htm";
				else if(st.ownItemCount(q_letter_of_confirmation_elf) < 1)
					htmltext = "minehr_q0239_02.htm";
				else if(GetMemoState == 1 && st.ownItemCount(q_parts_of_reclaiming_worker) < 1)
					htmltext = "minehr_q0239_07.htm";
				else if(GetMemoState == 2 && st.ownItemCount(q_parts_of_reclaiming_worker) >= 10)
				{
					st.setMemoState("Wont_you_join_us", String.valueOf(3), true);
					st.takeItems(q_parts_of_reclaiming_worker, -1);
					htmltext = "minehr_q0239_09.htm";
				}
				else if(GetMemoState == 1 && st.ownItemCount(q_parts_of_reclaiming_worker) < 1)
					htmltext = "minehr_q0239_07.htm";
				else if(GetMemoState == 3)
					htmltext = "minehr_q0239_10.htm";
				else if(GetMemoState == 4 && st.ownItemCount(q_piece_of_helldozer) < 1)
					htmltext = "minehr_q0239_12.htm";
				else if((GetMemoState == 4 || GetMemoState == 5) && st.ownItemCount(q_piece_of_helldozer) >= 1 && st.ownItemCount(q_piece_of_helldozer) < 20)
				{
					st.setCond(3);
					htmltext = "minehr_q0239_13.htm";
					st.soundEffect(SOUND_MIDDLE);
				}
				else if(GetMemoState == 5 && st.ownItemCount(q_piece_of_helldozer) >= 20)
				{
					st.giveItems(ADENA_ID, 283346);
					st.addExpAndSp(1319736, 103553);
					st.takeItems(q_piece_of_helldozer, -1);
					st.takeItems(q_letter_of_confirmation_elf, -1);
					st.removeMemo("Wont_you_join_us");
					st.soundEffect(SOUND_FINISH);
					htmltext = "minehr_q0239_14.htm";
					st.exitQuest(false);
				}
				break;
			case COMPLETED:
				if(npcId == minehr)
					htmltext = "minehr_q0239_04.htm";
				break;
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int GetMemoState = st.getInt("Wont_you_join_us");
		if(GetMemoState == 1 || GetMemoState == 2 && npc.getNpcId() == eggs_of_murcrokian)
		{
			if(st.ownItemCount(q_parts_of_reclaiming_worker) < 9)
			{
				st.giveItems(q_parts_of_reclaiming_worker, 1);
				st.soundEffect(SOUND_ITEMGET);
			}
			else if(st.ownItemCount(q_parts_of_reclaiming_worker) == 9)
			{
				st.giveItems(q_parts_of_reclaiming_worker, 1);
				st.setCond(2);
				st.setMemoState("Wont_you_join_us", String.valueOf(2), true);
				st.soundEffect(SOUND_MIDDLE);
			}
		}
		else if(GetMemoState == 4 || GetMemoState == 5 && npc.getNpcId() == bdzlem_suppressor)
		{
			int i0 = Rnd.get(1000);
			if(i0 < 774)
				if(st.ownItemCount(q_piece_of_helldozer) < 19)
				{
					st.giveItems(q_piece_of_helldozer, 1);
					st.soundEffect(SOUND_ITEMGET);
				}
				else if(st.ownItemCount(q_piece_of_helldozer) == 19)
				{
					st.giveItems(q_piece_of_helldozer, 1);
					st.setCond(4);
					st.setMemoState("Wont_you_join_us", String.valueOf(5), true);
					st.soundEffect(SOUND_MIDDLE);
				}
		}
		else if(GetMemoState == 4 || GetMemoState == 5 && npc.getNpcId() == bdzlem_exterminator)
		{
			int i0 = Rnd.get(1000);
			if(i0 < 793)
				if(st.ownItemCount(q_piece_of_helldozer) < 19)
				{
					st.giveItems(q_piece_of_helldozer, 1);
					st.soundEffect(SOUND_ITEMGET);
				}
				else if(st.ownItemCount(q_piece_of_helldozer) == 19)
				{
					st.giveItems(q_piece_of_helldozer, 1);
					st.setCond(4);
					st.setMemoState("Wont_you_join_us", String.valueOf(5), true);
					st.soundEffect(SOUND_MIDDLE);
				}
		}
		return null;
	}
}