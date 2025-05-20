package quests;

import jts.commons.util.Rnd;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

public class _652_AnAgedExAdventurer extends Quest implements ScriptFile
{
	//NPC
	private static final int Tantan = 32012;
	private static final int Sara = 30180;
	//Item
	private static final int SoulshotCgrade = 1464;
	private static final int ScrollEnchantArmorD = 956;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _652_AnAgedExAdventurer()
	{
		super(false);

		addStartNpc(Tantan);
		addTalkId(Sara);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("retired_oldman_tantan_q0652_03.htm") && st.ownItemCount(SoulshotCgrade) >= 100)
		{
			st.setCond(1);
			st.setState(STARTED);
			st.takeItems(SoulshotCgrade, 100);
			st.soundEffect(SOUND_ACCEPT);
			htmltext = "retired_oldman_tantan_q0652_04.htm";
		}
		else
		{
			htmltext = "retired_oldman_tantan_q0652_03.htm";
			st.exitQuest(true);
			st.soundEffect(SOUND_GIVEUP);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = "noquest";
		int cond = st.getCond();
		if(npcId == Tantan)
		{
			if(cond == 0)
				if(st.getPlayer().getLevel() < 46)
				{
					htmltext = "retired_oldman_tantan_q0652_01a.htm";
					st.exitQuest(true);
				}
				else
					htmltext = "retired_oldman_tantan_q0652_01.htm";
		}
		else if(npcId == Sara && cond == 1)
		{
			htmltext = "sara_q0652_01.htm";
			st.giveItems(ADENA_ID, 10000, true);
			if(Rnd.chance(50))
				st.giveItems(ScrollEnchantArmorD, 1, false);
			st.soundEffect(SOUND_FINISH);
			st.exitQuest(true);
		}
		return htmltext;
	}
}