package quests;

import jts.commons.util.Rnd;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

public class _325_GrimCollector extends Quest implements ScriptFile
{
	int ZOMBIE_HEAD = 1350;
	int ZOMBIE_HEART = 1351;
	int ZOMBIE_LIVER = 1352;
	int SKULL = 1353;
	int RIB_BONE = 1354;
	int SPINE = 1355;
	int ARM_BONE = 1356;
	int THIGH_BONE = 1357;
	int COMPLETE_SKELETON = 1358;
	int ANATOMY_DIAGRAM = 1349;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _325_GrimCollector()
	{
		super(false);

		addStartNpc(30336);

		addTalkId(30336);
		addTalkId(30342);
		addTalkId(30434);

		addKillId(20026);
		addKillId(20029);
		addKillId(20035);
		addKillId(20042);
		addKillId(20045);
		addKillId(20457);
		addKillId(20458);
		addKillId(20051);
		addKillId(20514);
		addKillId(20515);

		addQuestItem(new int[] {
				ZOMBIE_HEAD,
				ZOMBIE_HEART,
				ZOMBIE_LIVER,
				SKULL,
				RIB_BONE,
				SPINE,
				ARM_BONE,
				THIGH_BONE,
				COMPLETE_SKELETON,
				ANATOMY_DIAGRAM });
	}

	private long pieces(QuestState st)
	{
		return st.ownItemCount(ZOMBIE_HEAD) + st.ownItemCount(SPINE) + st.ownItemCount(ARM_BONE) + st.ownItemCount(ZOMBIE_HEART) + st.ownItemCount(ZOMBIE_LIVER) + st.ownItemCount(SKULL) + st.ownItemCount(RIB_BONE) + st.ownItemCount(THIGH_BONE) + st.ownItemCount(COMPLETE_SKELETON);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("guard_curtiz_q0325_03.htm"))
		{
			st.setCond(1);
			st.setState(STARTED);
			st.soundEffect(SOUND_ACCEPT);
		}
		else if(event.equalsIgnoreCase("samed_q0325_03.htm"))
			st.giveItems(ANATOMY_DIAGRAM, 1);
		else if(event.equalsIgnoreCase("samed_q0325_06.htm"))
		{
			if(pieces(st) > 0)
			{
				st.giveItems(ADENA_ID, 30 * st.ownItemCount(ZOMBIE_HEAD) + 20 * st.ownItemCount(ZOMBIE_HEART) + 20 * st.ownItemCount(ZOMBIE_LIVER) + 50 * st.ownItemCount(SKULL) + 15 * st.ownItemCount(RIB_BONE) + 10 * st.ownItemCount(SPINE) + 10 * st.ownItemCount(ARM_BONE) + 10 * st.ownItemCount(THIGH_BONE) + 2000 * st.ownItemCount(COMPLETE_SKELETON));
				st.takeItems(ZOMBIE_HEAD, -1);
				st.takeItems(ZOMBIE_HEART, -1);
				st.takeItems(ZOMBIE_LIVER, -1);
				st.takeItems(SKULL, -1);
				st.takeItems(RIB_BONE, -1);
				st.takeItems(SPINE, -1);
				st.takeItems(ARM_BONE, -1);
				st.takeItems(THIGH_BONE, -1);
				st.takeItems(COMPLETE_SKELETON, -1);
			}
			st.takeItems(ANATOMY_DIAGRAM, -1);
			st.soundEffect(SOUND_FINISH);
			st.exitQuest(true);
		}
		else if(event.equalsIgnoreCase("samed_q0325_07.htm") && pieces(st) > 0)
		{
			st.giveItems(ADENA_ID, 30 * st.ownItemCount(ZOMBIE_HEAD) + 20 * st.ownItemCount(ZOMBIE_HEART) + 20 * st.ownItemCount(ZOMBIE_LIVER) + 50 * st.ownItemCount(SKULL) + 15 * st.ownItemCount(RIB_BONE) + 10 * st.ownItemCount(SPINE) + 10 * st.ownItemCount(ARM_BONE) + 10 * st.ownItemCount(THIGH_BONE) + 2000 * st.ownItemCount(COMPLETE_SKELETON));
			st.takeItems(ZOMBIE_HEAD, -1);
			st.takeItems(ZOMBIE_HEART, -1);
			st.takeItems(ZOMBIE_LIVER, -1);
			st.takeItems(SKULL, -1);
			st.takeItems(RIB_BONE, -1);
			st.takeItems(SPINE, -1);
			st.takeItems(ARM_BONE, -1);
			st.takeItems(THIGH_BONE, -1);
			st.takeItems(COMPLETE_SKELETON, -1);
		}
		else if(event.equalsIgnoreCase("samed_q0325_09.htm"))
		{
			st.giveItems(ADENA_ID, 2000 * st.ownItemCount(COMPLETE_SKELETON));
			st.takeItems(COMPLETE_SKELETON, -1);
		}
		else if(event.equalsIgnoreCase("varsak_q0325_03.htm"))
			if(st.ownItemCount(SPINE) != 0 && st.ownItemCount(ARM_BONE) != 0 && st.ownItemCount(SKULL) != 0 && st.ownItemCount(RIB_BONE) != 0 && st.ownItemCount(THIGH_BONE) != 0)
			{
				st.takeItems(SPINE, 1);
				st.takeItems(SKULL, 1);
				st.takeItems(ARM_BONE, 1);
				st.takeItems(RIB_BONE, 1);
				st.takeItems(THIGH_BONE, 1);
				if(Rnd.chance(80))
					st.giveItems(COMPLETE_SKELETON, 1);
				else
					htmltext = "varsak_q0325_04.htm";
			}
			else
				htmltext = "varsak_q0325_02.htm";
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int id = st.getState();
		int cond = st.getCond();
		if(id == CREATED)
			st.setCond(0);
		if(npcId == 30336 && cond == 0)
		{
			if(st.getPlayer().getLevel() >= 15)
			{
				htmltext = "guard_curtiz_q0325_02.htm";
				return htmltext;
			}
			htmltext = "guard_curtiz_q0325_01.htm";
			st.exitQuest(true);
		}
		else if(npcId == 30336 && cond > 0)
			if(st.ownItemCount(ANATOMY_DIAGRAM) == 0)
				htmltext = "guard_curtiz_q0325_04.htm";
			else
				htmltext = "guard_curtiz_q0325_05.htm";
		else if(npcId == 30434 && cond > 0)
		{
			if(st.ownItemCount(ANATOMY_DIAGRAM) == 0)
				htmltext = "samed_q0325_01.htm";
			else if(st.ownItemCount(ANATOMY_DIAGRAM) != 0 && pieces(st) == 0)
				htmltext = "samed_q0325_04.htm";
			else if(st.ownItemCount(ANATOMY_DIAGRAM) != 0 && pieces(st) > 0 && st.ownItemCount(COMPLETE_SKELETON) == 0)
				htmltext = "samed_q0325_05.htm";
			else if(st.ownItemCount(ANATOMY_DIAGRAM) != 0 && pieces(st) > 0 && st.ownItemCount(COMPLETE_SKELETON) > 0)
				htmltext = "samed_q0325_08.htm";
		}
		else if(npcId == 30342 && cond > 0 && st.ownItemCount(ANATOMY_DIAGRAM) > 0)
			htmltext = "varsak_q0325_01.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if(st.ownItemCount(ANATOMY_DIAGRAM) == 0)
			return null;
		int n = Rnd.get(100);
		if(npcId == 20026)
		{
			if(n < 90)
			{
				st.soundEffect(SOUND_ITEMGET);
				if(n < 40)
					st.giveItems(ZOMBIE_HEAD, 1);
				else if(n < 60)
					st.giveItems(ZOMBIE_HEART, 1);
				else
					st.giveItems(ZOMBIE_LIVER, 1);
			}
		}
		else if(npcId == 20029)
		{
			st.soundEffect(SOUND_ITEMGET);
			if(n < 44)
				st.giveItems(ZOMBIE_HEAD, 1);
			else if(n < 66)
				st.giveItems(ZOMBIE_HEART, 1);
			else
				st.giveItems(ZOMBIE_LIVER, 1);
		}
		else if(npcId == 20035)
		{
			if(n < 79)
			{
				st.soundEffect(SOUND_ITEMGET);
				if(n < 5)
					st.giveItems(SKULL, 1);
				else if(n < 15)
					st.giveItems(RIB_BONE, 1);
				else if(n < 29)
					st.giveItems(SPINE, 1);
				else
					st.giveItems(THIGH_BONE, 1);
			}
		}
		else if(npcId == 20042)
		{
			if(n < 86)
			{
				st.soundEffect(SOUND_ITEMGET);
				if(n < 6)
					st.giveItems(SKULL, 1);
				else if(n < 19)
					st.giveItems(RIB_BONE, 1);
				else if(n < 69)
					st.giveItems(ARM_BONE, 1);
				else
					st.giveItems(THIGH_BONE, 1);
			}
		}
		else if(npcId == 20045)
		{
			if(n < 97)
			{
				st.soundEffect(SOUND_ITEMGET);
				if(n < 9)
					st.giveItems(SKULL, 1);
				else if(n < 59)
					st.giveItems(SPINE, 1);
				else if(n < 77)
					st.giveItems(ARM_BONE, 1);
				else
					st.giveItems(THIGH_BONE, 1);
			}
		}
		else if(npcId == 20051)
		{
			if(n < 99)
			{
				st.soundEffect(SOUND_ITEMGET);
				if(n < 9)
					st.giveItems(SKULL, 1);
				else if(n < 59)
					st.giveItems(RIB_BONE, 1);
				else if(n < 79)
					st.giveItems(SPINE, 1);
				else
					st.giveItems(ARM_BONE, 1);
			}
		}
		else if(npcId == 20514)
		{
			if(n < 51)
			{
				st.soundEffect(SOUND_ITEMGET);
				if(n < 2)
					st.giveItems(SKULL, 1);
				else if(n < 8)
					st.giveItems(RIB_BONE, 1);
				else if(n < 17)
					st.giveItems(SPINE, 1);
				else if(n < 18)
					st.giveItems(ARM_BONE, 1);
				else
					st.giveItems(THIGH_BONE, 1);
			}
		}
		else if(npcId == 20515)
		{
			if(n < 60)
			{
				st.soundEffect(SOUND_ITEMGET);
				if(n < 3)
					st.giveItems(SKULL, 1);
				else if(n < 11)
					st.giveItems(RIB_BONE, 1);
				else if(n < 22)
					st.giveItems(SPINE, 1);
				else if(n < 24)
					st.giveItems(ARM_BONE, 1);
				else
					st.giveItems(THIGH_BONE, 1);
			}
		}
		else if(npcId == 20457 || npcId == 20458)
		{
			st.soundEffect(SOUND_ITEMGET);
			if(n < 42)
				st.giveItems(ZOMBIE_HEAD, 1);
			else if(n < 67)
				st.giveItems(ZOMBIE_HEART, 1);
			else
				st.giveItems(ZOMBIE_LIVER, 1);
		}
		return null;
	}
}