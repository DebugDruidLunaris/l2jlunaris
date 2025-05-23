package quests;

import jts.gameserver.Config;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.network.serverpackets.ExShowScreenMessage;
import jts.gameserver.network.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import jts.gameserver.network.serverpackets.components.NpcString;
import jts.gameserver.scripts.ScriptFile;

public class _001_LettersOfLove extends Quest implements ScriptFile
{
	private final static int DARIN = 30048;
	private final static int ROXXY = 30006;
	private final static int BAULRO = 30033;

	private final static int DARINGS_LETTER = 687;
	private final static int ROXXY_KERCHIEF = 688;
	private final static int DARINGS_RECEIPT = 1079;
	private final static int BAULS_POTION = 1080;
	private final static int NECKLACE = 906;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _001_LettersOfLove()
	{
		super(false);

		addStartNpc(DARIN);
		addTalkId(ROXXY);
		addTalkId(BAULRO);
		addQuestItem(DARINGS_LETTER);
		addQuestItem(ROXXY_KERCHIEF);
		addQuestItem(DARINGS_RECEIPT);
		addQuestItem(BAULS_POTION);
	}

	@Override
	public String onEvent(String event, QuestState qs, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("quest_accept"))
		{
			htmltext = "daring_q0001_06.htm";
			qs.setCond(1);
			qs.setState(STARTED);
			qs.giveItems(DARINGS_LETTER, 1, false);
			qs.soundEffect(SOUND_ACCEPT);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		switch(npcId)
		{
			case DARIN:
				if(cond == 0)
				{
					if(st.getPlayer().getLevel() >= 2)
						htmltext = "daring_q0001_02.htm";
					else
					{
						htmltext = "daring_q0001_01.htm";
						st.exitQuest(true);
					}
				}
				else if(cond == 1)
					htmltext = "daring_q0001_07.htm";
				else if(cond == 2 && st.ownItemCount(ROXXY_KERCHIEF) == 1)
				{
					htmltext = "daring_q0001_08.htm";
					st.takeItems(ROXXY_KERCHIEF, -1);
					st.giveItems(DARINGS_RECEIPT, 1, false);
					st.setCond(3);
					st.soundEffect(SOUND_MIDDLE);
				}
				else if(cond == 3)
					htmltext = "daring_q0001_09.htm";
				else if(cond == 4 && st.ownItemCount(BAULS_POTION) == 1)
				{
					htmltext = "daring_q0001_10.htm";
					st.takeItems(BAULS_POTION, -1);
					st.giveItems(NECKLACE, 1, false);
					if(st.getPlayer().getClassId().getLevel() == 1 && !st.getPlayer().getVarB("ng1"))
						st.getPlayer().sendPacket(new ExShowScreenMessage(NpcString.DELIVERY_DUTY_COMPLETE, 5000, ScreenMessageAlign.TOP_CENTER, true));
					st.giveItems(ADENA_ID, (int) ((Config.RATE_QUESTS_REWARD - 1) * 1200 + 2466 * Config.RATE_QUESTS_REWARD), false); // T2
					st.getPlayer().addExpAndSp(5672, 446);
					st.soundEffect(SOUND_FINISH);
					st.exitQuest(false);
				}
				break;
			case ROXXY:
				if(cond == 1 && st.ownItemCount(ROXXY_KERCHIEF) == 0 && st.ownItemCount(DARINGS_LETTER) > 0)
				{
					htmltext = "rapunzel_q0001_01.htm";
					st.takeItems(DARINGS_LETTER, -1);
					st.giveItems(ROXXY_KERCHIEF, 1, false);
					st.setCond(2);
					st.soundEffect(SOUND_MIDDLE);
				}
				else if(cond == 2 && st.ownItemCount(ROXXY_KERCHIEF) > 0)
					htmltext = "rapunzel_q0001_02.htm";
				else if(cond > 2 && (st.ownItemCount(BAULS_POTION) > 0 || st.ownItemCount(DARINGS_RECEIPT) > 0))
					htmltext = "rapunzel_q0001_03.htm";
				break;
			case BAULRO:
				if(cond == 3 && st.ownItemCount(DARINGS_RECEIPT) == 1)
				{
					htmltext = "baul_q0001_01.htm";
					st.takeItems(DARINGS_RECEIPT, -1);
					st.giveItems(BAULS_POTION, 1, false);
					st.setCond(4);
					st.soundEffect(SOUND_MIDDLE);
				}
				else if(cond == 4)
					htmltext = "baul_q0001_02.htm";
				break;
		}
		return htmltext;
	}
}