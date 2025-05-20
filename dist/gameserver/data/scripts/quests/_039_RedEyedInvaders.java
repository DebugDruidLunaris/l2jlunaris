package quests;

import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

public class _039_RedEyedInvaders extends Quest implements ScriptFile
{
	int BBN = 7178;
	int RBN = 7179;
	int IP = 7180;
	int GML = 7181;
	int[] REW = { 6521, 6529, 6535 };

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _039_RedEyedInvaders()
	{
		super(false);

		addStartNpc(30334);

		addTalkId(30332);

		addKillId(20919);
		addKillId(20920);
		addKillId(20921);
		addKillId(20925);

		addQuestItem(new int[] { BBN, IP, RBN, GML });
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("guard_babenco_q0039_0104.htm"))
		{
			st.setCond(1);
			st.setState(STARTED);
			st.soundEffect(SOUND_ACCEPT);
		}
		else if(event.equals("captain_bathia_q0039_0201.htm"))
		{
			st.setCond(2);
			st.soundEffect(SOUND_ACCEPT);
		}
		else if(event.equals("captain_bathia_q0039_0301.htm"))
		{
			if(st.ownItemCount(BBN) == 100 && st.ownItemCount(RBN) == 100)
			{
				st.setCond(4);
				st.takeItems(BBN, -1);
				st.takeItems(RBN, -1);
				st.soundEffect(SOUND_ACCEPT);
			}
			else
				htmltext = "captain_bathia_q0039_0203.htm";
		}
		else if(event.equals("captain_bathia_q0039_0401.htm"))
			if(st.ownItemCount(IP) == 30 && st.ownItemCount(GML) == 30)
			{
				st.takeItems(IP, -1);
				st.takeItems(GML, -1);
				st.giveItems(REW[0], 60);
				st.giveItems(REW[1], 1);
				st.giveItems(REW[2], 500);
				st.addExpAndSp(62366, 2783);
				st.setCond(0);
				st.soundEffect(SOUND_FINISH);
				st.exitQuest(false);
			}
			else
				htmltext = "captain_bathia_q0039_0304.htm";
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = "noquest";
		int cond = st.getCond();
		if(npcId == 30334)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() < 20)
				{
					htmltext = "guard_babenco_q0039_0102.htm";
					st.exitQuest(true);
				}
				else if(st.getPlayer().getLevel() >= 20)
					htmltext = "guard_babenco_q0039_0101.htm";
			}
			else if(cond == 1)
				htmltext = "guard_babenco_q0039_0105.htm";
		}
		else if(npcId == 30332)
			if(cond == 1)
				htmltext = "captain_bathia_q0039_0101.htm";
			else if(cond == 2 && (st.ownItemCount(BBN) < 100 || st.ownItemCount(RBN) < 100))
				htmltext = "captain_bathia_q0039_0203.htm";
			else if(cond == 3 && st.ownItemCount(BBN) == 100 && st.ownItemCount(RBN) == 100)
				htmltext = "captain_bathia_q0039_0202.htm";
			else if(cond == 4 && (st.ownItemCount(IP) < 30 || st.ownItemCount(GML) < 30))
				htmltext = "captain_bathia_q0039_0304.htm";
			else if(cond == 5 && st.ownItemCount(IP) == 30 && st.ownItemCount(GML) == 30)
				htmltext = "captain_bathia_q0039_0303.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(cond == 2)
		{
			if((npcId == 20919 || npcId == 20920) && st.ownItemCount(BBN) <= 99)
				st.giveItems(BBN, 1);
			else if(npcId == 20921 && st.ownItemCount(RBN) <= 99)
				st.giveItems(RBN, 1);
			st.soundEffect(SOUND_ITEMGET);
			if(st.ownItemCount(BBN) + st.ownItemCount(RBN) == 200)
			{
				st.setCond(3);
				st.soundEffect(SOUND_MIDDLE);
			}
		}

		if(cond == 4)
		{
			if((npcId == 20920 || npcId == 20921) && st.ownItemCount(IP) <= 29)
				st.giveItems(IP, 1);
			else if(npcId == 20925 && st.ownItemCount(GML) <= 29)
				st.giveItems(GML, 1);
			st.soundEffect(SOUND_ITEMGET);
			if(st.ownItemCount(IP) + st.ownItemCount(GML) == 60)
			{
				st.setCond(5);
				st.soundEffect(SOUND_MIDDLE);
			}
		}
		return null;
	}
}