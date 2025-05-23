package quests;

import jts.gameserver.model.base.ClassId;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

public class _062_PathOfTheTrooper extends Quest implements ScriptFile
{
	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	int Shubain = 32194;
	int Gwain = 32197;

	int FelimLizardmanWarrior = 20014;
	int VenomousSpider = 20038;
	int TumranBugbear = 20062;

	int FelimHead = 9749;
	int VenomousSpiderLeg = 9750;
	int TumranBugbearHeart = 9751;
	int ShubainsRecommendation = 9752;
	int GwainsRecommendation = 9753;

	public _062_PathOfTheTrooper()
	{
		super(false);

		addStartNpc(Gwain);
		addTalkId(Gwain);
		addTalkId(Shubain);
		addKillId(FelimLizardmanWarrior);
		addKillId(VenomousSpider);
		addKillId(TumranBugbear);
		addQuestItem(FelimHead);
		addQuestItem(VenomousSpiderLeg);
		addQuestItem(ShubainsRecommendation);
		addQuestItem(TumranBugbearHeart);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("master_tbwain_q0062_06.htm"))
		{
			st.setState(STARTED);
			st.setCond(1);
			st.soundEffect(SOUND_ACCEPT);
		}
		else if(event.equals("master_shubain_q0062_02.htm"))
			st.setCond(2);
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int id = st.getState();
		int cond = st.getCond();
		if(npcId == Gwain)
		{
			if(id == CREATED)
			{
				if(st.getPlayer().getClassId() != ClassId.maleSoldier)
				{
					htmltext = "master_tbwain_q0062_02.htm";
					st.exitQuest(true);
				}
				else if(st.getPlayer().getLevel() < 18)
				{
					htmltext = "master_tbwain_q0062_03.htm";
					st.exitQuest(true);
				}
				else
					htmltext = "master_tbwain_q0062_01.htm";
			}
			else if(cond == 4)
			{
				st.takeItems(ShubainsRecommendation, -1);
				st.setCond(5);
				htmltext = "master_tbwain_q0062_08.htm";
			}
			else if(cond == 5 && st.ownItemCount(TumranBugbearHeart) > 0)
			{
				st.takeItems(TumranBugbearHeart, -1);
				if(st.getPlayer().getClassId().getLevel() == 1)
				{
					st.giveItems(GwainsRecommendation, 1);
					if(!st.getPlayer().getVarB("prof1"))
					{
						st.getPlayer().setVar("prof1", "1", -1);
						st.addExpAndSp(160267, 11023);
						//FIXME [G1ta0] дать адены, только если первый чар на акке
						st.giveItems(ADENA_ID, 81900);
					}
				}
				st.soundEffect(SOUND_FINISH);
				st.exitQuest(true);
				htmltext = "master_tbwain_q0062_10.htm";
			}
		}
		else if(npcId == Shubain)
			if(cond == 1)
				htmltext = "master_shubain_q0062_01.htm";
			else if(cond == 2 && st.ownItemCount(FelimHead) >= 5)
			{
				st.takeItems(FelimHead, -1);
				st.setCond(3);
				htmltext = "master_shubain_q0062_04.htm";
			}
			else if(cond == 3 && st.ownItemCount(VenomousSpiderLeg) >= 10)
			{
				st.takeItems(VenomousSpiderLeg, -1);
				st.giveItems(ShubainsRecommendation, 1);
				st.setCond(4);
				htmltext = "master_shubain_q0062_06.htm";
			}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int id = npc.getNpcId();
		int cond = st.getCond();
		if(id == FelimLizardmanWarrior && cond == 2)
		{
			long count = st.ownItemCount(FelimHead);
			if(count < 5)
			{
				st.giveItems(FelimHead, 1);
				if(count == 4)
					st.soundEffect(SOUND_MIDDLE);
				else
					st.soundEffect(SOUND_ITEMGET);
			}
		}
		if(id == VenomousSpider && cond == 3)
		{
			long count = st.ownItemCount(VenomousSpiderLeg);
			if(count < 10)
			{
				st.giveItems(VenomousSpiderLeg, 1);
				if(count == 9)
					st.soundEffect(SOUND_MIDDLE);
				else
					st.soundEffect(SOUND_ITEMGET);
			}
		}
		if(id == TumranBugbear && cond == 5)
			if(st.ownItemCount(TumranBugbearHeart) == 0)
			{
				st.giveItems(TumranBugbearHeart, 1);
				st.soundEffect(SOUND_MIDDLE);
			}
		return null;
	}
}