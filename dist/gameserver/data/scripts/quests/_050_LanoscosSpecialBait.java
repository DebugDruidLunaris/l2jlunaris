package quests;

import jts.commons.util.Rnd;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

public class _050_LanoscosSpecialBait extends Quest implements ScriptFile
{
	// NPC
	int Lanosco = 31570;
	int SingingWind = 21026;
	// Items
	int EssenceofWind = 7621;
	int WindFishingLure = 7610;
	// Skill
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

	public _050_LanoscosSpecialBait()
	{
		super(false);

		addStartNpc(Lanosco);

		addTalkId(Lanosco);

		addKillId(SingingWind);

		addQuestItem(EssenceofWind);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("fisher_lanosco_q0050_0104.htm"))
		{
			st.setState(STARTED);
			st.setCond(1);
			st.soundEffect(SOUND_ACCEPT);
		}
		else if(event.equals("fisher_lanosco_q0050_0201.htm"))
			if(st.ownItemCount(EssenceofWind) < 100)
				htmltext = "fisher_lanosco_q0050_0202.htm";
			else
			{
				st.removeMemo("cond");
				st.takeItems(EssenceofWind, -1);
				st.giveItems(WindFishingLure, 4);
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
		if(npcId == Lanosco)
			if(id == CREATED)
			{
				if(st.getPlayer().getLevel() < 27)
				{
					htmltext = "fisher_lanosco_q0050_0103.htm";
					st.exitQuest(true);
				}
				else if(st.getPlayer().getSkillLevel(FishSkill) >= 8)
					htmltext = "fisher_lanosco_q0050_0101.htm";
				else
				{
					htmltext = "fisher_lanosco_q0050_0102.htm";
					st.exitQuest(true);
				}
			}
			else if(cond == 1 || cond == 2)
				if(st.ownItemCount(EssenceofWind) < 100)
				{
					htmltext = "fisher_lanosco_q0050_0106.htm";
					st.setCond(1);
				}
				else
					htmltext = "fisher_lanosco_q0050_0105.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if(npcId == SingingWind && st.getCond() == 1)
			if(st.ownItemCount(EssenceofWind) < 100 && Rnd.chance(30))
			{
				st.giveItems(EssenceofWind, 1);
				if(st.ownItemCount(EssenceofWind) == 100)
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