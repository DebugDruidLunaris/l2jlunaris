package quests;

import jts.gameserver.model.base.Race;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;
import jts.gameserver.utils.Location;

/**
 * Based on official H5
 * @author Magister
 */
public class _182_NewRecruits extends Quest implements ScriptFile
{
	// NPC's
	private static int kekrops = 32138;
	private static int mother_nornil = 32258;
	// ITEMS
	private static int ring_of_devotion_q = 10124;
	private static int red_cresent_earing_q = 10122;

	private static final Location exit_loc = new Location(-74058, 52040, -3680);

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _182_NewRecruits()
	{
		super(false);
		addStartNpc(kekrops);
		addTalkId(kekrops, mother_nornil);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		int GetMemoState = st.getInt("the_invitiation_of_helper");

		if(event.equals("quest_accept"))
		{
			st.setCond(1);
			st.setMemoState("the_invitiation_of_helper", String.valueOf(1), true);
			st.setState(STARTED);
			st.soundEffect(SOUND_ACCEPT);
			htmltext = "kekrops_q0182_05.htm";
		}
		else if(event.equals("reply_5") && GetMemoState == 1)
		{
			st.giveItems(red_cresent_earing_q, 2);
			st.soundEffect(SOUND_FINISH);
			st.removeMemo("the_invitiation_of_helper");
			htmltext = "mother_nornil_q0182_04.htm";
			st.exitQuest(false);
		}
		else if(event.equals("reply_6") && GetMemoState == 1)
		{
			st.giveItems(ring_of_devotion_q, 2);
			st.soundEffect(SOUND_FINISH);
			st.removeMemo("the_invitiation_of_helper");
			htmltext = "mother_nornil_q0182_05.htm";
			st.exitQuest(false);
		}
		else if(event.equals("reply_3"))
		{
			st.getPlayer().teleToLocation(exit_loc, 0);
			return null;
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int GetMemoState = st.getInt("the_invitiation_of_helper");
		int id = st.getState();

		switch(id)
		{
			case CREATED:
				if(npcId == kekrops)
					if(st.getPlayer().getRace() == Race.kamael)
					{
						htmltext = "kekrops_q0182_03.htm";
						st.exitQuest(true);
					}
					else if(st.getPlayer().getLevel() >= 17 && st.getPlayer().getLevel() <= 21 && st.getPlayer().getClassId().getLevel() == 1)
						htmltext = "kekrops_q0182_01.htm";
					else if(st.getPlayer().getLevel() < 17)
					{
						htmltext = "kekrops_q0182_02.htm";
						st.exitQuest(true);
					}
					else if(st.getPlayer().getLevel() > 21 || st.getPlayer().getClassId().getLevel() != 1)
					{
						htmltext = "kekrops_q0182_02a.htm";
						st.exitQuest(true);
					}
				break;
			case STARTED:
				if(npcId == kekrops && GetMemoState == 1)
					htmltext = "kekrops_q0182_06.htm";
				else if(npcId == mother_nornil && GetMemoState == 1)
					htmltext = "mother_nornil_q0182_01.htm";
				break;
		}
		return htmltext;
	}
}