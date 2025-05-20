package quests;

import jts.gameserver.data.htm.HtmCache;
import jts.gameserver.model.base.ClassId;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

/**
 * Based on official H5
 * @author Magister
 */
public class _061_LawEnforcement extends Quest implements ScriptFile
{
	// npc
	private static final int grandmaste_piane = 32222;
	private static final int kekrops = 32138;
	private static final int subelder_aientburg = 32469;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _061_LawEnforcement()
	{
		super(false);
		addStartNpc(grandmaste_piane);
		addTalkId(kekrops, subelder_aientburg);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		int GetMemoState = st.getInt("rule_of_judicator");
		int npcId = npc.getNpcId();

		if(npcId == grandmaste_piane)
		{
			if(event.equalsIgnoreCase("quest_accept"))
			{
				st.setCond(1);
				st.setMemoState("rule_of_judicator", String.valueOf(1), true);
				st.setState(STARTED);
				st.soundEffect(SOUND_ACCEPT);
				htmltext = "grandmaste_piane_q0061_05.htm";
			}
			else if(event.equalsIgnoreCase("reply_1"))
				htmltext = "grandmaste_piane_q0061_04.htm";
		}
		else if(npcId == kekrops)
		{
			if(event.equalsIgnoreCase("reply_2"))
			{
				if(GetMemoState == 1)
				{
					st.setMemoState("rule_of_judicator", String.valueOf(2), true);
					htmltext = "kekrops_q0061_03.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_3"))
			{
				if(GetMemoState == 1)
				{
					st.setMemoState("rule_of_judicator", String.valueOf(3), true);
					htmltext = "kekrops_q0061_04.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_4"))
			{
				if(GetMemoState >= 2 && GetMemoState <= 3)
					htmltext = "kekrops_q0061_06.htm";
			}
			else if(event.equalsIgnoreCase("reply_5"))
			{
				if(GetMemoState >= 2 && GetMemoState <= 3)
					htmltext = "kekrops_q0061_07.htm";
			}
			else if(event.equalsIgnoreCase("reply_6"))
			{
				if(GetMemoState >= 2 && GetMemoState <= 3)
					htmltext = "kekrops_q0061_08.htm";
			}
			else if(event.equalsIgnoreCase("reply_7"))
				if(GetMemoState >= 2 && GetMemoState <= 3)
				{
					st.setCond(2);
					st.setMemoState("rule_of_judicator", String.valueOf(4), true);
					st.soundEffect(SOUND_MIDDLE);
					htmltext = "kekrops_q0061_09.htm";
				}
		}
		else if(npcId == subelder_aientburg)
			if(event.equalsIgnoreCase("reply_1"))
			{
				if(GetMemoState == 4)
				{
					st.setMemoState("rule_of_judicator", String.valueOf(5), true);
					htmltext = "subelder_aientburg_q0061_02.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_2"))
			{
				if(GetMemoState == 5)
				{
					st.giveItems(ADENA_ID, 26000);
					st.getPlayer().setClassId(ClassId.judicator.ordinal(), false, true);
					st.getPlayer().broadcastCharInfo();
					st.removeMemo("rule_of_judicator");
					st.exitQuest(false);
					htmltext = "subelder_aientburg_q0061_08.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_3"))
				if(GetMemoState == 5)
				{
					st.giveItems(ADENA_ID, 26000);
					st.getPlayer().setClassId(ClassId.judicator.ordinal(), false, true);
					st.getPlayer().broadcastCharInfo();
					st.removeMemo("rule_of_judicator");
					st.exitQuest(false);
					htmltext = "subelder_aientburg_q0061_09.htm";
				}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int GetMemoState = st.getInt("rule_of_judicator");
		int talker_occupation = st.getPlayer().getClassId().getId();
		String talker_name = st.getPlayer().getName();
		int inspector = 0x87;
		int npcId = npc.getNpcId();
		int id = st.getState();

		switch(id)
		{
			case CREATED:
				if(npcId == grandmaste_piane)
					if(st.getPlayer().getLevel() >= 76)
					{
						if(talker_occupation == inspector)
						{
							htmltext = HtmCache.getInstance().getNotNull("quests/_061_LawEnforcement/grandmaste_piane_q0061_01.htm", st.getPlayer());
							htmltext = htmltext.replace("<?name?>", talker_name);
						}
						else
						{
							htmltext = "grandmaste_piane_q0061_03.htm";
							st.exitQuest(true);
						}
					}
					else
					{
						htmltext = "grandmaste_piane_q0061_02.htm";
						st.exitQuest(true);
					}
				break;
			case STARTED:
				if(npcId == grandmaste_piane)
				{
					if(GetMemoState == 1)
						htmltext = "grandmaste_piane_q0061_06.htm";
				}
				else if(npcId == kekrops)
				{
					if(GetMemoState == 1)
						htmltext = "kekrops_q0061_01.htm";
					else if(GetMemoState == 2)
						htmltext = "kekrops_q0061_03.htm";
					else if(GetMemoState == 3)
						htmltext = "kekrops_q0061_04.htm";
					else if(GetMemoState == 4)
						htmltext = "kekrops_q0061_10.htm";
				}
				else if(npcId == subelder_aientburg)
					if(GetMemoState == 4)
					{
						htmltext = HtmCache.getInstance().getNotNull("quests/_061_LawEnforcement/subelder_aientburg_q0061_01.htm", st.getPlayer());
						htmltext = htmltext.replace("<?name?>", talker_name);
					}
					else if(GetMemoState == 5)
						htmltext = "subelder_aientburg_q0061_02.htm";
				break;
		}
		return htmltext;
	}
}