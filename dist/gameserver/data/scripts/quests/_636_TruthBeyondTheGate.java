package quests;

import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

/**
 * Based on official H5
 * @author Magister
 */
public class _636_TruthBeyondTheGate extends Quest implements ScriptFile
{
	// npc
	private final static int priest_eliyah = 31329;
	private final static int falsepriest_flauron = 32010;

	// questitem
	private final static int q_mark_of_heresy = 8067;
	private final static int q_mark_of_sacrifice = 8064;
	private final static int q_faded_mark_of_sac = 8065;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _636_TruthBeyondTheGate()
	{
		super(false);
		addStartNpc(priest_eliyah);
		addTalkId(falsepriest_flauron);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		int npcId = npc.getNpcId();

		if(npcId == priest_eliyah)
		{
			if(event.equalsIgnoreCase("quest_accept"))
			{
				st.setCond(1);
				st.setMemoState("truth_behind_the_door", String.valueOf(1), true);
				st.setState(STARTED);
				st.soundEffect(SOUND_ACCEPT);
				htmltext = "priest_eliyah_q0636_05.htm";
			}
			else if(event.equalsIgnoreCase("reply_1"))
				htmltext = "priest_eliyah_q0636_04.htm";
		}
		else if(npcId == falsepriest_flauron)
			if(event.equalsIgnoreCase("reply_1"))
			{
				st.giveItems(q_mark_of_sacrifice, 1);
				st.removeMemo("truth_behind_the_door");
				st.soundEffect(SOUND_FINISH);
				st.exitQuest(true);
				htmltext = "falsepriest_flauron_q0636_02.htm";
			}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int GetMemoState = st.getInt("truth_behind_the_door");
		int npcId = npc.getNpcId();
		int id = st.getState();

		switch(id)
		{
			case CREATED:
				if(npcId == priest_eliyah)
					if(st.getPlayer().getLevel() >= 73 && st.ownItemCount(q_mark_of_sacrifice) == 0 && st.ownItemCount(q_faded_mark_of_sac) == 0 && st.ownItemCount(q_mark_of_heresy) == 0)
						htmltext = "priest_eliyah_q0636_01.htm";
					else if(st.getPlayer().getLevel() >= 73 && (st.ownItemCount(q_mark_of_sacrifice) >= 1 || st.ownItemCount(q_faded_mark_of_sac) >= 1 || st.ownItemCount(q_mark_of_heresy) >= 1))
					{
						htmltext = "priest_eliyah_q0636_02.htm";
						st.exitQuest(true);
					}
					else if(st.getPlayer().getLevel() < 73)
					{
						htmltext = "priest_eliyah_q0636_03.htm";
						st.exitQuest(true);
					}
				break;
			case STARTED:
				if(npcId == priest_eliyah)
				{
					if(GetMemoState == 1)
						htmltext = "priest_eliyah_q0636_06.htm";
				}
				else if(npcId == falsepriest_flauron)
				{
					if(GetMemoState == 1)
						htmltext = "falsepriest_flauron_q0636_01.htm";
				}
				break;
		}
		return htmltext;
	}
}