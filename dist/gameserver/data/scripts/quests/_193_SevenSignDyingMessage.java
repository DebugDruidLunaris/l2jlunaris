package quests;

import java.util.HashMap;
import java.util.Map;

import jts.gameserver.ai.CtrlEvent;
import jts.gameserver.model.GameObjectsStorage;
import jts.gameserver.model.Player;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.network.serverpackets.ExStartScenePlayer;
import jts.gameserver.scripts.Functions;
import jts.gameserver.scripts.ScriptFile;

public class _193_SevenSignDyingMessage extends Quest implements ScriptFile
{
	// NPCs
	private static int Hollint = 30191;
	private static int Cain = 32569;
	private static int Eric = 32570;
	private static int SirGustavAthebaldt = 30760;

	// MOBs
	private static int ShilensEvilThoughts = 27343;

	// ITEMS
	private static int JacobsNecklace = 13814;
	private static int DeadmansHerb = 13813;
	private static int SculptureofDoubt = 14352;

	private static Map<Integer, Integer> spawns = new HashMap<Integer, Integer>();

	public _193_SevenSignDyingMessage()
	{
		super(false);

		addStartNpc(Hollint);
		addTalkId(Cain, Eric, SirGustavAthebaldt);
		addKillId(ShilensEvilThoughts);
		addQuestItem(JacobsNecklace, DeadmansHerb, SculptureofDoubt);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		Player player = st.getPlayer();
		String htmltext = event;
		if(event.equalsIgnoreCase("30191-02.htm"))
		{
			st.setCond(1);
			st.setState(STARTED);
			st.soundEffect(SOUND_ACCEPT);
			st.giveItems(JacobsNecklace, 1);
		}
		else if(event.equalsIgnoreCase("32569-05.htm"))
		{
			st.setCond(2);
			st.soundEffect(SOUND_MIDDLE);
		}
		else if(event.equalsIgnoreCase("32570-02.htm"))
		{
			st.setCond(3);
			st.giveItems(DeadmansHerb, 1);
			st.soundEffect(SOUND_MIDDLE);
		}
		else if(event.equalsIgnoreCase("30760-02.htm"))
		{
			if(player.getBaseClassId() == player.getActiveClassId())
			{
				st.addExpAndSp(52518015, 5817677);
				st.setState(COMPLETED);
				st.exitQuest(false);
				st.soundEffect(SOUND_FINISH);
			}
			else
				return "subclass_forbidden.htm";
		}
		else if(event.equalsIgnoreCase("close_your_eyes"))
		{
			st.setCond(4);
			st.takeItems(DeadmansHerb, -1);
			st.soundEffect(SOUND_MIDDLE);
			player.showQuestMovie(ExStartScenePlayer.SCENE_SSQ_DYING_MASSAGE);
			return "";
		}
		else if(event.equalsIgnoreCase("32569-09.htm"))
		{
			htmltext = "32569-09.htm";
			Functions.npcSay(npc, st.getPlayer().getName() + "! Это чужой, он должен быть побежден. Это последняя помощь мне!");
			NpcInstance mob = st.addSpawn(ShilensEvilThoughts, 82425, 47232, -3216, 0, 0, 180000);
			spawns.put(player.getObjectId(), mob.getObjectId());
			mob.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, player, 100000);
		}
		else if(event.equalsIgnoreCase("32569-13.htm"))
		{
			st.setCond(6);
			st.takeItems(SculptureofDoubt, -1);
			st.soundEffect(SOUND_MIDDLE);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		int id = st.getState();
		Player player = st.getPlayer();
		if(npcId == Hollint)
		{
			if(id == CREATED)
			{
				if(player.getLevel() < 79)
				{
					st.exitQuest(true);
					return "30191-00.htm";
				}
				QuestState qs = player.getQuestState(_192_SevenSignSeriesOfDoubt.class);
				if(qs == null || !qs.isCompleted())
				{
					st.exitQuest(true);
					return "noquest";
				}
				return "30191-01.htm";
			}
			else if(cond == 1)
				return "30191-03.htm";
		}
		else if(npcId == Cain)
		{
			if(cond == 1)
				return "32569-01.htm";
			else if(cond == 2)
				return "32569-06.htm";
			else if(cond == 3)
				return "32569-07.htm";
			else if(cond == 4)
			{
				Integer obj_id = spawns.get(player.getObjectId());
				NpcInstance mob = obj_id != null ? GameObjectsStorage.getNpc(obj_id) : null;
				if(mob == null || mob.isDead())
					return "32569-08.htm";
				else
					return "32569-09.htm";
			}
			else if(cond == 5)
				return "32569-10.htm";
			else if(cond == 6)
				return "32569-13.htm";
		}
		else if(npcId == Eric)
		{
			if(cond == 2)
				return "32570-01.htm";
			else if(cond == 3)
				return "32570-03.htm";
		}
		else if(npcId == SirGustavAthebaldt)
			if(cond == 6)
				return "30760-01.htm";
		return "noquest";
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		Player player = st.getPlayer();
		if(player == null)
			return null;

		if(npcId == ShilensEvilThoughts && cond == 4)
		{
			Integer obj_id = spawns.get(player.getObjectId());
			if(obj_id != null && obj_id.intValue() == npc.getObjectId())
			{
				spawns.remove(player.getObjectId());
				st.setCond(5);
				st.soundEffect(SOUND_ITEMGET);
				st.giveItems(SculptureofDoubt, 1);
			}
		}
		return null;
	}

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}
}