package quests;

import jts.gameserver.model.base.Race;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

/**
 * Based on official H5
 * @author Magister
 */
public class _003_WilltheSealbeBroken extends Quest implements ScriptFile
{
	// npc
	private final static int redry = 30141;

	// mobs
	private final static int onyx_beast = 20031;
	private final static int tainted_zombie = 20041;
	private final static int stink_zombie = 20046;
	private final static int least_succubus = 20048;
	private final static int least_succubus_turen = 20052;
	private final static int least_succubus_tilfo = 20057;

	// etcitem
	private final static int scrl_of_ench_am_d = 956;

	// questitem
	private final static int onyx_beast_eye = 1081;
	private final static int taint_stone = 1082;
	private final static int succubus_blood = 1083;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _003_WilltheSealbeBroken()
	{
		super(false);
		addStartNpc(redry);
		addKillId(onyx_beast, tainted_zombie, stink_zombie, least_succubus, least_succubus_turen, least_succubus_tilfo);
		addQuestItem(onyx_beast_eye, taint_stone, succubus_blood);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		int npcId = npc.getNpcId();

		if(npcId == redry)
			if(event.equalsIgnoreCase("quest_accept"))
			{
				st.setCond(1);
				st.setMemoState("release_darkelf_elder1", String.valueOf(1), true);
				st.setState(STARTED);
				st.soundEffect(SOUND_ACCEPT);
				htmltext = "redry_q0003_03.htm";
			}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int GetMemoState = st.getInt("release_darkelf_elder1");
		int npcId = npc.getNpcId();
		int id = st.getState();

		switch(id)
		{
			case CREATED:
				if(npcId == redry)
					if(st.getPlayer().getLevel() >= 16)
					{
						if(st.getPlayer().getRace() != Race.darkelf)
						{
							st.exitQuest(true);
							htmltext = "redry_q0003_00.htm";
						}
						else
							htmltext = "redry_q0003_02.htm";
					}
					else
						htmltext = "redry_q0003_01.htm";
				break;
			case STARTED:
				if(npcId == redry)
					if(GetMemoState == 1 && st.ownItemCount(onyx_beast_eye) >= 1 && st.ownItemCount(taint_stone) >= 1 && st.ownItemCount(succubus_blood) >= 1)
					{
						st.giveItems(scrl_of_ench_am_d, 1);
						st.takeItems(onyx_beast_eye, -1);
						st.takeItems(taint_stone, -1);
						st.takeItems(succubus_blood, -1);
						st.removeMemo("release_darkelf_elder1");
						st.soundEffect(SOUND_FINISH);
						st.exitQuest(false);
						htmltext = "redry_q0003_06.htm";
					}
					else
						htmltext = "redry_q0003_04.htm";
				break;
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int GetMemoState = st.getInt("release_darkelf_elder1");
		int npcId = npc.getNpcId();

		if(GetMemoState == 1)
			if(npcId == onyx_beast)
			{
				st.giveItems(onyx_beast_eye, 1);
				st.soundEffect(SOUND_MIDDLE);

				if(st.ownItemCount(taint_stone) >= 1 && st.ownItemCount(succubus_blood) >= 1)
					st.setCond(2);
			}
			else if(npcId == tainted_zombie || npcId == stink_zombie)
			{
				st.giveItems(taint_stone, 1);
				st.soundEffect(SOUND_MIDDLE);

				if(st.ownItemCount(onyx_beast_eye) >= 1 && st.ownItemCount(succubus_blood) >= 1)
					st.setCond(2);
			}
			else if(npcId == least_succubus || npcId == least_succubus_turen || npcId == least_succubus_tilfo)
			{
				st.giveItems(succubus_blood, 1);
				st.soundEffect(SOUND_MIDDLE);

				if(st.ownItemCount(onyx_beast_eye) >= 1 && st.ownItemCount(taint_stone) >= 1)
					st.setCond(2);
			}
		return null;
	}
}