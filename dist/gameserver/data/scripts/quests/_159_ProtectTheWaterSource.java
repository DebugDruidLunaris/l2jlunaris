package quests;

import jts.commons.util.Rnd;
import jts.gameserver.model.base.Race;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

public class _159_ProtectTheWaterSource extends Quest implements ScriptFile
{
	int PLAGUE_DUST_ID = 1035;
	int HYACINTH_CHARM1_ID = 1071;
	int HYACINTH_CHARM2_ID = 1072;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _159_ProtectTheWaterSource()
	{
		super(false);

		addStartNpc(30154);

		addKillId(27017);

		addQuestItem(new int[] { PLAGUE_DUST_ID, HYACINTH_CHARM1_ID, HYACINTH_CHARM2_ID });
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("1"))
		{
			st.setCond(1);
			st.setState(STARTED);
			st.soundEffect(SOUND_ACCEPT);
			if(st.ownItemCount(HYACINTH_CHARM1_ID) == 0)
			{
				st.giveItems(HYACINTH_CHARM1_ID, 1);
				htmltext = "30154-04.htm";
			}
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int cond = st.getCond();
		if(cond == 0)
		{
			if(st.getPlayer().getRace() != Race.elf)
			{
				htmltext = "30154-00.htm";
				st.exitQuest(true);
			}
			else if(st.getPlayer().getLevel() >= 12)
			{
				htmltext = "30154-03.htm";
				return htmltext;
			}
			else
			{
				htmltext = "30154-02.htm";
				st.exitQuest(true);
			}
		}
		else if(cond == 1)
			htmltext = "30154-05.htm";
		else if(cond == 2)
		{
			st.takeItems(PLAGUE_DUST_ID, -1);
			st.takeItems(HYACINTH_CHARM1_ID, -1);
			st.giveItems(HYACINTH_CHARM2_ID, 1);
			st.setCond(3);
			htmltext = "30154-06.htm";
		}
		else if(cond == 3)
			htmltext = "30154-07.htm";
		else if(cond == 4)
		{
			st.takeItems(PLAGUE_DUST_ID, -1);
			st.takeItems(HYACINTH_CHARM2_ID, -1);
			st.giveItems(ADENA_ID, 18250);
			st.soundEffect(SOUND_FINISH);
			htmltext = "30154-08.htm";
			st.exitQuest(false);
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();

		if(cond == 1 && Rnd.chance(60))
		{
			st.giveItems(PLAGUE_DUST_ID, 1);
			st.setCond(2);
			st.soundEffect(SOUND_MIDDLE);
		}
		else if(cond == 3 && Rnd.chance(60))
			if(st.ownItemCount(PLAGUE_DUST_ID) == 4)
			{
				st.giveItems(PLAGUE_DUST_ID, 1);
				st.setCond(4);
				st.soundEffect(SOUND_MIDDLE);
			}
			else
			{
				st.giveItems(PLAGUE_DUST_ID, 1);
				st.soundEffect(SOUND_ITEMGET);
			}
		return null;
	}
}