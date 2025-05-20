package quests;

import jts.commons.util.Rnd;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

public class _246_PossessorOfaPreciousSoul3 extends Quest implements ScriptFile
{
	private static final int CARADINES_LETTER_2_PART = 7678;
	private static final int RING_OF_GODDESS_WATERBINDER = 7591;
	private static final int NECKLACE_OF_GODDESS_EVERGREEN = 7592;
	private static final int STAFF_OF_GODDESS_RAIN_SONG = 7593;
	private static final int CARADINES_LETTER = 7679;
	private static final int RELIC_BOX = 7594;
	private static final int STAFF_OF_GODDES = 21725;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _246_PossessorOfaPreciousSoul3()
	{
		super(true);

		addStartNpc(31740);

		addTalkId(31741);
		addTalkId(30721);

		addKillId(21541);
		addKillId(21544);
		addKillId(25325);
		for(int i = 21535; i <= 21540; i++)
			addKillId(i);

		addQuestItem(new int[] { RING_OF_GODDESS_WATERBINDER, NECKLACE_OF_GODDESS_EVERGREEN, STAFF_OF_GODDESS_RAIN_SONG, STAFF_OF_GODDES });
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("caradine_q0246_0104.htm"))
		{
			st.setCond(1);
			st.takeItems(CARADINES_LETTER_2_PART, 1);
			st.setState(STARTED);
			st.soundEffect(SOUND_ACCEPT);
		}
		else if(event.equals("ossian_q0246_0201.htm"))
		{
			st.setCond(2);
			st.soundEffect(SOUND_MIDDLE);
		}
		else if(event.equals("ossian_q0246_0401.htm"))
		{
			st.takeItems(RING_OF_GODDESS_WATERBINDER, 1);
			st.takeItems(NECKLACE_OF_GODDESS_EVERGREEN, 1);
			st.takeItems(STAFF_OF_GODDESS_RAIN_SONG, 1);
			st.setCond(6);
			st.giveItems(RELIC_BOX, 1);
			st.soundEffect(SOUND_MIDDLE);
		}
		else if(event.equals("magister_ladd_q0246_0501.htm"))
		{
			st.takeItems(RELIC_BOX, 1);
			st.giveItems(CARADINES_LETTER, 1);
			st.addExpAndSp(719843, 0);
			st.removeMemo("cond");
			st.exitQuest(false);
		}
		else if(event.equals("ossian_q0246_0301rb.htm"))
		{
			st.setCond(4);
			st.soundEffect(SOUND_MIDDLE);
			st.setMemoState("staff_select", 0);
		}
		else if(event.equals("ossian_q0246_0301mb.htm"))
		{
			st.setCond(4);
			st.soundEffect(SOUND_MIDDLE);
			st.setMemoState("staff_select", 1);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		if(!st.getPlayer().isSubClassActive())
			return "Subclass only!";

		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 31740)
		{
			if(cond == 0)
			{
				QuestState previous = st.getPlayer().getQuestState(_242_PossessorOfaPreciousSoul2.class);
				if(previous != null && previous.getState() == COMPLETED && st.getPlayer().getLevel() >= 65)
					htmltext = "caradine_q0246_0101.htm";
				else
				{
					htmltext = "caradine_q0246_0102.htm";
					st.exitQuest(true);
				}
			}
			else if(cond == 1)
				htmltext = "caradine_q0246_0105.htm";
		}
		else if(npcId == 31741)
		{
			if(cond == 1)
				htmltext = "ossian_q0246_0101.htm";
			else if((cond == 2 || cond == 3) && (st.ownItemCount(RING_OF_GODDESS_WATERBINDER) < 1 || st.ownItemCount(NECKLACE_OF_GODDESS_EVERGREEN) < 1))
				htmltext = "ossian_q0246_0203.htm";
			else if(cond == 3 && st.ownItemCount(RING_OF_GODDESS_WATERBINDER) == 1 && st.ownItemCount(NECKLACE_OF_GODDESS_EVERGREEN) == 1)
				htmltext = "ossian_q0246_0202.htm";
			else if(cond == 4)
				htmltext = "ossian_q0246_0301.htm";
			else if((cond == 4 || cond == 5) && st.ownItemCount(STAFF_OF_GODDESS_RAIN_SONG) < 1)
				htmltext = "ossian_q0246_0402.htm";
			else if(cond == 5 && st.ownItemCount(RING_OF_GODDESS_WATERBINDER) == 1 && st.ownItemCount(NECKLACE_OF_GODDESS_EVERGREEN) == 1 && st.ownItemCount(STAFF_OF_GODDESS_RAIN_SONG) == 1)
				htmltext = "ossian_q0246_0303.htm";
			else if(cond == 6)
				htmltext = "ossian_q0246_0403.htm";
		}
		else if(npcId == 30721)
			if(cond == 6 && st.ownItemCount(RELIC_BOX) == 1)
				htmltext = "magister_ladd_q0246_0401.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(!st.getPlayer().isSubClassActive())
			return null;

		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(cond == 2)
		{
			if(Rnd.chance(30))
				if(npcId == 21541 && st.ownItemCount(RING_OF_GODDESS_WATERBINDER) == 0)
				{
					st.giveItems(RING_OF_GODDESS_WATERBINDER, 1);
					if(st.ownItemCount(RING_OF_GODDESS_WATERBINDER) == 1 && st.ownItemCount(NECKLACE_OF_GODDESS_EVERGREEN) == 1)
					{
						st.setCond(3);
						st.soundEffect(SOUND_MIDDLE);
					}
					else
						st.soundEffect(SOUND_ITEMGET);
				}
				else if(npcId == 21544 && st.ownItemCount(NECKLACE_OF_GODDESS_EVERGREEN) == 0)
				{
					st.giveItems(NECKLACE_OF_GODDESS_EVERGREEN, 1);
					if(st.ownItemCount(RING_OF_GODDESS_WATERBINDER) == 1 && st.ownItemCount(NECKLACE_OF_GODDESS_EVERGREEN) == 1)
					{
						st.setCond(3);
						st.soundEffect(SOUND_MIDDLE);
					}
					else
						st.soundEffect(SOUND_ITEMGET);
				}
		}
		else if(cond == 4)
			if(npcId == 25325 && st.ownItemCount(STAFF_OF_GODDESS_RAIN_SONG) == 0 && st.getInt("staff_select") == 0)
			{
				st.giveItems(STAFF_OF_GODDESS_RAIN_SONG, 1);
				st.setCond(5);
				st.soundEffect(SOUND_MIDDLE);
			}
			else if(npcId >= 21535 && npcId <= 21540 && st.ownItemCount(STAFF_OF_GODDES) < 100 && st.getInt("staff_select") == 1)
			{
				st.giveItems(STAFF_OF_GODDES, 1);
				if(st.ownItemCount(STAFF_OF_GODDES) >= 100)
				{
					st.takeItems(STAFF_OF_GODDES, -1);
					st.giveItems(STAFF_OF_GODDESS_RAIN_SONG, 1);
					st.setCond(5);
					st.soundEffect(SOUND_MIDDLE);
				}
				else
					st.soundEffect(SOUND_ITEMGET);
			}
		return null;
	}
}