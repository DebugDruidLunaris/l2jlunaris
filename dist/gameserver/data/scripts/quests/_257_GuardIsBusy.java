package quests;

import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.network.serverpackets.ExShowScreenMessage;
import jts.gameserver.network.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import jts.gameserver.scripts.ScriptFile;

/**
 * Квест проверен и работает.
 * Рейты прописаны путем повышения шанса получения квестовых вещей.
 */
public class _257_GuardIsBusy extends Quest implements ScriptFile
{
	int GLUDIO_LORDS_MARK = 1084;
	int ORC_AMULET = 752;
	int ORC_NECKLACE = 1085;
	int WEREWOLF_FANG = 1086;
	int ADENA = 57;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _257_GuardIsBusy()
	{
		super(false);

		addStartNpc(30039);
		addKillId(20130, 20131, 20132, 20342, 20343, 20006, 20093, 20096, 20098);
		addQuestItem(ORC_AMULET, ORC_NECKLACE, WEREWOLF_FANG, GLUDIO_LORDS_MARK);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("gilbert_q0257_03.htm"))
		{
			st.setCond(1);
			st.setState(STARTED);
			st.soundEffect(SOUND_ACCEPT);
			st.takeItems(GLUDIO_LORDS_MARK, -1);
			st.giveItems(GLUDIO_LORDS_MARK, 1);
		}
		else if(event.equalsIgnoreCase("257_2"))
		{
			htmltext = "gilbert_q0257_05.htm";
			st.takeItems(GLUDIO_LORDS_MARK, -1);
			st.soundEffect(SOUND_FINISH);
			st.exitQuest(true);
		}
		else if(event.equalsIgnoreCase("257_3"))
			htmltext = "gilbert_q0257_06.htm";
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int cond = st.getCond();

		if(cond == 0)
		{
			if(st.getPlayer().getLevel() >= 6)
			{
				htmltext = "gilbert_q0257_02.htm";
				return htmltext;
			}
			htmltext = "gilbert_q0257_01.htm";
			st.exitQuest(true);
		}
		else if(cond == 1 && st.ownItemCount(ORC_AMULET) < 1 && st.ownItemCount(ORC_NECKLACE) < 1 && st.ownItemCount(WEREWOLF_FANG) < 1)
			htmltext = "gilbert_q0257_04.htm";
		else if(cond == 1 && (st.ownItemCount(ORC_AMULET) > 0 || st.ownItemCount(ORC_NECKLACE) > 0 || st.ownItemCount(WEREWOLF_FANG) > 0))
		{
			st.giveItems(ADENA, 12 * st.ownItemCount(ORC_AMULET) + 20 * st.ownItemCount(ORC_NECKLACE) + 25 * st.ownItemCount(WEREWOLF_FANG), false);

			if(st.getPlayer().getClassId().getLevel() == 1 && !st.getPlayer().getVarB("p1q2"))
			{
				st.getPlayer().setVar("p1q2", "1", -1);
				st.getPlayer().sendPacket(new ExShowScreenMessage("Acquisition of Soulshot for beginners complete.\n                  Go find the Newbie Guide.", 5000, ScreenMessageAlign.TOP_CENTER, true));
				QuestState qs = st.getPlayer().getQuestState(_255_Tutorial.class);
				if(qs != null && qs.getInt("Ex") != 10)
				{
					st.showQuestionMark(26);
					qs.setMemoState("Ex", "10");
					if(st.getPlayer().getClassId().isMage())
					{
						st.playTutorialVoice("tutorial_voice_027");
						st.giveItems(5790, 3000);
					}
					else
					{
						st.playTutorialVoice("tutorial_voice_026");
						st.giveItems(5789, 6000);
					}
				}
			}

			st.takeItems(ORC_AMULET, -1);
			st.takeItems(ORC_NECKLACE, -1);
			st.takeItems(WEREWOLF_FANG, -1);
			htmltext = "gilbert_q0257_07.htm";
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if(st.ownItemCount(GLUDIO_LORDS_MARK) > 0 && st.getCond() > 0)
			if(npcId == 20130 || npcId == 20131 || npcId == 20006)
				st.rollAndGive(ORC_AMULET, 1, 50);
			else if(npcId == 20093 || npcId == 20096 || npcId == 20098)
				st.rollAndGive(ORC_NECKLACE, 1, 50);
			else if(npcId == 20132)
				st.rollAndGive(WEREWOLF_FANG, 1, 33);
			else if(npcId == 20343)
				st.rollAndGive(WEREWOLF_FANG, 1, 50);
			else if(npcId == 20342)
				st.rollAndGive(WEREWOLF_FANG, 1, 75);
		return null;
	}
}