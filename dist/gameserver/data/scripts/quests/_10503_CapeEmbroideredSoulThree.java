package quests;

import jts.commons.util.Rnd;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

public class _10503_CapeEmbroideredSoulThree extends Quest implements ScriptFile
{
	// NPC's
	private static final int OLF_ADAMS = 32612;
	// Mob's
	private static final int FRINTEZZA = 29047;
	// Quest Item's
	private static final int SOUL_FRINTEZZA = 21724;
	// Item's
	private static final int CLOAK_FRINTEZZA = 21721;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _10503_CapeEmbroideredSoulThree()
	{
		super(PARTY_ALL);
		addStartNpc(OLF_ADAMS);
		addTalkId(OLF_ADAMS);
		addKillId(FRINTEZZA);
		addQuestItem(SOUL_FRINTEZZA);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("olf_adams_q10503_02.htm"))
		{
			st.setCond(1);
			st.setState(STARTED);
			st.soundEffect(SOUND_ACCEPT);
		}
		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int cond = st.getCond();
		if(cond == 0)
		{
			if(st.getPlayer().getLevel() >= 80)
				htmltext = "olf_adams_q10503_01.htm";
			else
			{
				htmltext = "olf_adams_q10503_00.htm";
				st.exitQuest(true);
			}
		}
		else if(cond == 1)
			htmltext = "olf_adams_q10503_03.htm";
		else if(cond == 2)
			if(st.ownItemCount(SOUL_FRINTEZZA) < 20)
			{
				st.setCond(1);
				htmltext = "olf_adams_q10503_03.htm";
			}
			else
			{
				st.takeItems(SOUL_FRINTEZZA, -1);
				st.giveItems(CLOAK_FRINTEZZA, 1);
				st.soundEffect(SOUND_FINISH);
				htmltext = "olf_adams_q10503_04.htm";
				st.exitQuest(false);
			}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(cond == 1 && npcId == FRINTEZZA)
		{
			if(st.ownItemCount(SOUL_FRINTEZZA) < 20)
				st.giveItems(SOUL_FRINTEZZA, Rnd.get(1, 3), false);
			if(st.ownItemCount(SOUL_FRINTEZZA) >= 20)
			{
				st.setCond(2);
				st.soundEffect(SOUND_MIDDLE);
			}
		}
		return null;
	}
}