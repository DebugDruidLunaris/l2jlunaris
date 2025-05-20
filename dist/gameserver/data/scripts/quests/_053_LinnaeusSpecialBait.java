package quests;

import jts.commons.util.Rnd;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

public class _053_LinnaeusSpecialBait extends Quest implements ScriptFile
{
	int Linnaeu = 31577;
	int CrimsonDrake = 20670;
	int HeartOfCrimsonDrake = 7624;
	int FlameFishingLure = 7613;
	Integer FishSkill = 1315;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _053_LinnaeusSpecialBait()
	{
		super(false);

		addStartNpc(Linnaeu);

		addTalkId(Linnaeu);

		addKillId(CrimsonDrake);

		addQuestItem(HeartOfCrimsonDrake);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("fisher_linneaus_q0053_0104.htm"))
		{
			st.setState(STARTED);
			st.setCond(1);
			st.soundEffect(SOUND_ACCEPT);
		}
		else if(event.equals("fisher_linneaus_q0053_0201.htm"))
			if(st.ownItemCount(HeartOfCrimsonDrake) < 100)
				htmltext = "fisher_linneaus_q0053_0202.htm";
			else
			{
				st.removeMemo("cond");
				st.takeItems(HeartOfCrimsonDrake, -1);
				st.giveItems(FlameFishingLure, 4);
				st.soundEffect(SOUND_FINISH);
				st.exitQuest(false);
			}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = "noquest";
		int cond = st.getCond();
		int id = st.getState();
		if(npcId == Linnaeu)
			if(id == CREATED)
			{
				if(st.getPlayer().getLevel() < 60)
				{
					htmltext = "fisher_linneaus_q0053_0103.htm";
					st.exitQuest(true);
				}
				else if(st.getPlayer().getSkillLevel(FishSkill) >= 21)
					htmltext = "fisher_linneaus_q0053_0101.htm";
				else
				{
					htmltext = "fisher_linneaus_q0053_0102.htm";
					st.exitQuest(true);
				}
			}
			else if(cond == 1 || cond == 2)
				if(st.ownItemCount(HeartOfCrimsonDrake) < 100)
				{
					htmltext = "fisher_linneaus_q0053_0106.htm";
					st.setCond(1);
				}
				else
					htmltext = "fisher_linneaus_q0053_0105.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if(npcId == CrimsonDrake && st.getCond() == 1)
			if(st.ownItemCount(HeartOfCrimsonDrake) < 100 && Rnd.chance(30))
			{
				st.giveItems(HeartOfCrimsonDrake, 1);
				if(st.ownItemCount(HeartOfCrimsonDrake) == 100)
				{
					st.soundEffect(SOUND_MIDDLE);
					st.setCond(2);
				}
				else
					st.soundEffect(SOUND_ITEMGET);
			}
		return null;
	}
}