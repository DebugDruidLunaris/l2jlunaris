package quests;

import jts.gameserver.model.base.Race;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

/**
 * Рейты не учитываются, награда специфичная
 */
public class _006_StepIntoTheFuture extends Quest implements ScriptFile
{
	//NPC
	private static final int Roxxy = 30006;
	private static final int Baulro = 30033;
	private static final int Windawood = 30311;
	//Quest Item
	private static final int BaulrosLetter = 7571;
	//Items
	private static final int ScrollOfEscapeGiran = 7126;
	private static final int MarkOfTraveler = 7570;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _006_StepIntoTheFuture()
	{
		super(false);
		addStartNpc(Roxxy);

		addTalkId(Baulro);
		addTalkId(Windawood);

		addQuestItem(BaulrosLetter);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("rapunzel_q0006_0104.htm"))
		{
			st.setCond(1);
			st.setState(STARTED);
			st.soundEffect(SOUND_ACCEPT);
		}
		else if(event.equalsIgnoreCase("baul_q0006_0201.htm"))
		{
			st.giveItems(BaulrosLetter, 1, false);
			st.setCond(2);
			st.soundEffect(SOUND_MIDDLE);
		}
		else if(event.equalsIgnoreCase("sir_collin_windawood_q0006_0301.htm"))
		{
			st.takeItems(BaulrosLetter, -1);
			st.setCond(3);
			st.soundEffect(SOUND_MIDDLE);
		}
		else if(event.equalsIgnoreCase("rapunzel_q0006_0401.htm"))
		{
			st.giveItems(ScrollOfEscapeGiran, 1, false);
			st.giveItems(MarkOfTraveler, 1, false);
			st.removeMemo("cond");
			st.soundEffect(SOUND_FINISH);
			st.exitQuest(false);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == Roxxy)
		{
			if(cond == 0)
				if(st.getPlayer().getRace() == Race.human && st.getPlayer().getLevel() >= 3)
					htmltext = "rapunzel_q0006_0101.htm";
				else
				{
					htmltext = "rapunzel_q0006_0102.htm";
					st.exitQuest(true);
				}
			else if(cond == 1)
				htmltext = "rapunzel_q0006_0105.htm";
			else if(cond == 3)
				htmltext = "rapunzel_q0006_0301.htm";
		}
		else if(npcId == Baulro)
		{
			if(cond == 1)
				htmltext = "baul_q0006_0101.htm";
			else if(cond == 2 && st.ownItemCount(BaulrosLetter) > 0)
				htmltext = "baul_q0006_0202.htm";
		}
		else if(npcId == Windawood)
			if(cond == 2 && st.ownItemCount(BaulrosLetter) > 0)
				htmltext = "sir_collin_windawood_q0006_0201.htm";
			else if(cond == 2 && st.ownItemCount(BaulrosLetter) == 0)
				htmltext = "sir_collin_windawood_q0006_0302.htm";
			else if(cond == 3)
				htmltext = "sir_collin_windawood_q0006_0303.htm";
		return htmltext;
	}
}
