package quests;

import jts.gameserver.ai.CtrlEvent;
import jts.gameserver.model.Player;
import jts.gameserver.model.entity.Reflection;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.network.serverpackets.ExStartScenePlayer;
import jts.gameserver.scripts.Functions;
import jts.gameserver.scripts.ScriptFile;
import jts.gameserver.utils.Location;
import jts.gameserver.utils.ReflectionUtils;

public class _198_SevenSignsEmbryo extends Quest implements ScriptFile
{
	// NPCs
	private static int Wood = 32593;
	private static int Franz = 32597;
	private static int Jaina = 32582;
	private static int ShilensEvilThoughtsCapt = 27346;

	// ITEMS
	private static int PieceOfDoubt = 14355;
	private static int DawnsBracelet = 15312;
	private static int AncientAdena = 5575;

	private static final int izId = 113;

	Location setcloc = new Location(-23734, -9184, -5384, 0);

	public _198_SevenSignsEmbryo()
	{
		super(false);

		addStartNpc(Wood);
		addTalkId(Wood, Franz, Jaina);
		addKillId(ShilensEvilThoughtsCapt);
		addQuestItem(PieceOfDoubt);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		Player player = st.getPlayer();
		String htmltext = event;
		if(event.equalsIgnoreCase("wood_q198_2.htm"))
		{
			st.setCond(1);
			st.setState(STARTED);
			st.soundEffect(SOUND_ACCEPT);
		}
		else if(event.equalsIgnoreCase("wood_q198_3.htm"))
		{
			enterInstance(player);
			if(st.get("embryo") != null)
				st.removeMemo("embryo");
		}
		else if(event.equalsIgnoreCase("franz_q198_3.htm"))
		{
			NpcInstance embryo = player.getReflection().addSpawnWithoutRespawn(ShilensEvilThoughtsCapt, setcloc, 0);
			st.setMemoState("embryo", 1);
			Functions.npcSay(npc, player.getName() + "!Вы должны убить этого монстра! Я постараюсь помочь!");
			Functions.npcSay(embryo, "Тебе не убить меня.");
			embryo.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, player, 500);
		}
		else if(event.equalsIgnoreCase("wood_q198_8.htm"))
			enterInstance(player);
		else if(event.equalsIgnoreCase("franz_q198_5.htm"))
		{
			Functions.npcSay(npc, "Мы будем с вами всегда ...");
			st.takeItems(PieceOfDoubt, -1);
			st.setCond(3);
			st.soundEffect(SOUND_MIDDLE);
		}
		else if(event.equalsIgnoreCase("jaina_q198_2.htm"))
			player.getReflection().collapse();
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		Player player = st.getPlayer();
		String htmltext = "noquest";
		if(npcId == Wood)
		{
			QuestState qs = player.getQuestState(_197_SevenSignsTheSacredBookofSeal.class);
			if(cond == 0)
			{
				if(player.getLevel() >= 79 && qs != null && qs.isCompleted())
					htmltext = "wood_q198_1.htm";
				else
				{
					htmltext = "wood_q198_0.htm";
					st.exitQuest(true);
				}
			}
			else if(cond == 1 || cond == 2)
				htmltext = "wood_q198_2a.htm";
			else if(cond == 3)
				if(player.getBaseClassId() == player.getActiveClassId())
				{
					st.addExpAndSp(315108090, 34906059);
					st.giveItems(DawnsBracelet, 1);
					st.giveItems(AncientAdena, 1500000);
					st.setState(COMPLETED);
					st.soundEffect(SOUND_FINISH);
					st.exitQuest(false);
					htmltext = "wood_q198_4.htm";
				}
				else
					htmltext = "subclass_forbidden.htm";
		}
		else if(npcId == Franz)
		{
			if(cond == 1)
			{
				if(st.get("embryo") == null || Integer.parseInt(st.get("embryo")) != 1)
					htmltext = "franz_q198_1.htm";
				else
					htmltext = "franz_q198_3a.htm";
			}
			else if(cond == 2)
				htmltext = "franz_q198_4.htm";
			else
				htmltext = "franz_q198_6.htm";
		}
		else if(npcId == Jaina)
			htmltext = "jaina_q198_1.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		Player player = st.getPlayer();
		if(player == null)
			return null;

		if(npcId == ShilensEvilThoughtsCapt && cond == 1)
		{
			Functions.npcSay(npc, player.getName() + ", Я ухожу. Но мы еще встретимся!");
			st.setMemoState("embryo", 2);
			st.setCond(2);
			st.giveItems(PieceOfDoubt, 1);
			player.showQuestMovie(ExStartScenePlayer.SCENE_SSQ_EMBRYO);
		}
		return null;
	}

	private void enterInstance(Player player)
	{
		Reflection r = player.getActiveReflection();
		if(r != null)
		{
			if(player.canReenterInstance(izId))
				player.teleToLocation(r.getTeleportLoc(), r);
		}
		else if(player.canEnterInstance(izId))
			ReflectionUtils.enterReflection(player, izId);
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