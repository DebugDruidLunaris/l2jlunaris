package quests;

import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

import org.apache.commons.lang3.ArrayUtils;

/**
 * @author pchayka
 *         Daily
 */
public class _902_ReclaimOurEra extends Quest implements ScriptFile
{
	private static final int Mathias = 31340;
	private static final int[] OrcsSilenos = { 25309, 25312, 25315, 25299, 25302, 25305 };
	private static final int[] CannibalisticStakatoChief = { 25667, 25668, 25669, 25670 };
	private static final int Anais = 25701;

	private static final int ShatteredBones = 21997;
	private static final int CannibalisticStakatoLeaderClaw = 21998;
	private static final int AnaisScroll = 21999;

	public _902_ReclaimOurEra()
	{
		super(PARTY_ALL);
		addStartNpc(Mathias);
		addKillId(OrcsSilenos);
		addKillId(CannibalisticStakatoChief);
		addKillId(Anais);
		addQuestItem(ShatteredBones, CannibalisticStakatoLeaderClaw, AnaisScroll);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("mathias_q902_04.htm"))
		{
			st.setState(STARTED);
			st.setCond(1);
			st.soundEffect(SOUND_ACCEPT);
		}
		else if(event.equalsIgnoreCase("mathias_q902_05.htm"))
			st.setCond(2);
		else if(event.equalsIgnoreCase("mathias_q902_06.htm"))
			st.setCond(3);
		else if(event.equalsIgnoreCase("mathias_q902_07.htm"))
			st.setCond(4);
		else if(event.equalsIgnoreCase("mathias_q902_09.htm"))
		{
			if(st.takeAllItems(ShatteredBones) > 0)
			{
				st.giveItems(21750, 1);
				st.giveItems(ADENA_ID, 134038);
			}
			else if(st.takeAllItems(CannibalisticStakatoLeaderClaw) > 0)
			{
				st.giveItems(21750, 3);
				st.giveItems(ADENA_ID, 210119);
			}
			else if(st.takeAllItems(AnaisScroll) > 0)
			{
				st.giveItems(21750, 3);
				st.giveItems(ADENA_ID, 348155);
			}
			st.setState(COMPLETED);
			st.soundEffect(SOUND_FINISH);
			st.exitQuest(this);
		}

		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int cond = st.getCond();
		if(npc.getNpcId() == Mathias)
			switch(st.getState())
			{
				case CREATED:
					if(st.isDailyQuest())
					{
						if(st.getPlayer().getLevel() >= 80)
							htmltext = "mathias_q902_01.htm";
						else
						{
							htmltext = "mathias_q902_00.htm";
							st.exitQuest(true);
						}
					}
					else
						htmltext = "mathias_q902_00a.htm";
					break;
				case STARTED:
					if(cond == 1)
						htmltext = "mathias_q902_04.htm";
					else if(cond == 2)
						htmltext = "mathias_q902_05.htm";
					else if(cond == 3)
						htmltext = "mathias_q902_06.htm";
					else if(cond == 4)
						htmltext = "mathias_q902_07.htm";
					else if(cond == 5)
						htmltext = "mathias_q902_08.htm";
					break;
			}

		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();
		if(cond == 2 && ArrayUtils.contains(OrcsSilenos, npc.getNpcId()))
		{
			st.giveItems(ShatteredBones, 1);
			st.setCond(5);
		}
		else if(cond == 3 && ArrayUtils.contains(CannibalisticStakatoChief, npc.getNpcId()))
		{
			st.giveItems(CannibalisticStakatoLeaderClaw, 1);
			st.setCond(5);
		}
		else if(cond == 4 && npc.getNpcId() == Anais)
		{
			st.giveItems(AnaisScroll, 1);
			st.setCond(5);
		}
		return null;
	}

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}
}