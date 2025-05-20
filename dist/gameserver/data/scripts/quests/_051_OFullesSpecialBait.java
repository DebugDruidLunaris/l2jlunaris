package quests;

import jts.commons.util.Rnd;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

public class _051_OFullesSpecialBait extends Quest implements ScriptFile
{
	int OFulle = 31572;
	int FetteredSoul = 20552;

	int LostBaitIngredient = 7622;
	int IcyAirFishingLure = 7611;

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

	public _051_OFullesSpecialBait()
	{
		super(false);

		addStartNpc(OFulle);

		addTalkId(OFulle);

		addKillId(FetteredSoul);

		addQuestItem(LostBaitIngredient);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("fisher_ofulle_q0051_0104.htm"))
		{
			st.setState(STARTED);
			st.setCond(1);
			st.soundEffect(SOUND_ACCEPT);
		}
		else if(event.equals("fisher_ofulle_q0051_0201.htm"))
			if(st.ownItemCount(LostBaitIngredient) < 100)
				htmltext = "fisher_ofulle_q0051_0202.htm";
			else
			{
				st.removeMemo("cond");
				st.takeItems(LostBaitIngredient, -1);
				st.giveItems(IcyAirFishingLure, 4);
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
		if(npcId == OFulle)
			if(id == CREATED)
			{
				if(st.getPlayer().getLevel() < 36)
				{
					htmltext = "fisher_ofulle_q0051_0103.htm";
					st.exitQuest(true);
				}
				else if(st.getPlayer().getSkillLevel(FishSkill) >= 11)
					htmltext = "fisher_ofulle_q0051_0101.htm";
				else
				{
					htmltext = "fisher_ofulle_q0051_0102.htm";
					st.exitQuest(true);
				}
			}
			else if(cond == 1 || cond == 2)
				if(st.ownItemCount(LostBaitIngredient) < 100)
				{
					htmltext = "fisher_ofulle_q0051_0106.htm";
					st.setCond(1);
				}
				else
					htmltext = "fisher_ofulle_q0051_0105.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if(npcId == FetteredSoul && st.getCond() == 1)
			if(st.ownItemCount(LostBaitIngredient) < 100 && Rnd.chance(30))
			{
				st.giveItems(LostBaitIngredient, 1);
				if(st.ownItemCount(LostBaitIngredient) == 100)
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
