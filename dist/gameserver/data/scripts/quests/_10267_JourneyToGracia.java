package quests;

import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

/**
 * Based on official H5
 * @author Magister
 */
public class _10267_JourneyToGracia extends Quest implements ScriptFile
{
	// NPC's
	private final static int highpriest_orven = 30857;
	private final static int kserth = 32548;
	private final static int magister_papiku = 32564;

	// Quest Items
	private final static int q_letter_of_orven = 13810;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _10267_JourneyToGracia()
	{
		super(false);
		addStartNpc(highpriest_orven);
		addTalkId(kserth, magister_papiku);
		addQuestItem(q_letter_of_orven);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;

		if(event.equalsIgnoreCase("quest_accept"))
		{
			st.setCond(1);
			st.setMemoState("journey_to_gracia", String.valueOf(1), true);
			st.setState(STARTED);
			st.soundEffect(SOUND_ACCEPT);
			st.giveItems(q_letter_of_orven, 1);
			htmltext = "highpriest_orven_q10267_08.htm";
		}
		else if(event.equalsIgnoreCase("reply_1"))
		{
			st.setCond(2);
			st.setMemoState("journey_to_gracia", String.valueOf(2), true);
			st.soundEffect(SOUND_MIDDLE);
			htmltext = "magister_papiku_q10267_02.htm";
		}
		else if(event.equalsIgnoreCase("reply_1a"))
		{
			st.giveItems(ADENA_ID, 92500);
			st.takeItems(q_letter_of_orven, -1);
			st.addExpAndSp(75480, 7570);
			st.removeMemo("journey_to_gracia");
			st.exitQuest(false);
			st.soundEffect(SOUND_FINISH);
			htmltext = "kserth_q10267_03.htm";
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int GetMemoState = st.getInt("journey_to_gracia");

		switch(st.getState())
		{
			case CREATED:
				if(npcId == highpriest_orven)
					if(st.getPlayer().getLevel() >= 75)
						htmltext = "highpriest_orven_q10267_01.htm";
					else
						htmltext = "highpriest_orven_q10267_02.htm";
				break;
			case COMPLETED:
				if(npcId == highpriest_orven)
					htmltext = "highpriest_orven_q10267_03.htm";
				else if(npcId == kserth)
					htmltext = "kserth_q10267_02.htm";
				break;
			case STARTED:
				if(npcId == highpriest_orven)
					htmltext = "highpriest_orven_q10267_09.htm";
				else if(npcId == magister_papiku)
				{
					if(GetMemoState == 1 && st.ownItemCount(q_letter_of_orven) >= 1)
						htmltext = "magister_papiku_q10267_01.htm";
					else if(GetMemoState == 2)
						htmltext = "magister_papiku_q10267_03.htm";
				}
				else if(npcId == kserth)
					if(GetMemoState == 2 && st.ownItemCount(q_letter_of_orven) >= 1)
						htmltext = "kserth_q10267_01.htm";
				break;
		}
		return htmltext;
	}
}