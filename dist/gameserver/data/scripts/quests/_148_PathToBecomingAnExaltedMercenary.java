package quests;

import jts.gameserver.model.Player;
import jts.gameserver.model.entity.events.impl.DominionSiegeEvent;
import jts.gameserver.model.entity.residence.Castle;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.network.serverpackets.ExShowScreenMessage;
import jts.gameserver.network.serverpackets.components.NpcString;
import jts.gameserver.scripts.ScriptFile;

/**
 * @author pchayka
 */
public class _148_PathToBecomingAnExaltedMercenary extends Quest implements ScriptFile
{
	private final int[] MERCENARY_CAPTAINS = { 36481, 36482, 36483, 36484, 36485, 36486, 36487, 36488, 36489 };

	private final int[] CATAPULTAS = { 36499, 36500, 36501, 36502, 36503, 36504, 36505, 36506, 36507 };

	public _148_PathToBecomingAnExaltedMercenary()
	{
		super(PARTY_ALL);
		addStartNpc(MERCENARY_CAPTAINS);
		addKillId(CATAPULTAS);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("gludio_merc_cap_q0148_06.htm"))
		{
			st.setCond(1);
			st.setState(STARTED);
			st.soundEffect(SOUND_ACCEPT);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		Player player = st.getPlayer();
		Castle castle = npc.getCastle();
		String htmlText = NO_QUEST_DIALOG;

		int cond = st.getCond();
		if(cond == 0)
		{
			if(player.getClan() != null)
				if(player.getClan().getCastle() == castle.getId())
					return "gludio_merc_cap_q0148_01.htm";
				else if(player.getClan().getCastle() > 0)
					return "gludio_merc_cap_q0148_02.htm";

			if(player.getLevel() < 40 || player.getClassId().getLevel() <= 2)
				htmlText = "gludio_merc_cap_q0148_03.htm";
			else if(st.ownItemCount(13767) < 1)
				htmlText = "gludio_merc_cap_q0148_03a.htm";
			else
				htmlText = "gludio_merc_cap_q0148_04.htm";
		}
		else if(cond == 1 || cond == 2 || cond == 3)
			htmlText = "gludio_merc_cap_q0148_07.htm";
		else if(cond == 4)
		{
			htmlText = "gludio_merc_cap_q0148_08.htm";
			st.takeAllItems(13767);
			st.giveItems(13768, 1);
			st.setState(COMPLETED);
			st.soundEffect(SOUND_FINISH);
			st.exitQuest(false);
		}

		return htmlText;
	}

	@Override
	public String onKill(Player killed, QuestState st)
	{
		if(st.getCond() == 1 || st.getCond() == 3)
			if(isValidKill(killed, st.getPlayer()))
			{
				int killedCount = st.getInt("enemies");
				int maxCount = 30;
				killedCount++;
				if(killedCount < maxCount)
				{
					st.setMemoState("enemies", killedCount);
					st.getPlayer().sendPacket(new ExShowScreenMessage(NpcString.YOU_HAVE_DEFEATED_S2_OF_S1_ENEMIES, 4000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true, String.valueOf(maxCount), String.valueOf(killedCount)));
				}
				else
				{
					if(st.getCond() == 1)
						st.setCond(2);
					else if(st.getCond() == 3)
						st.setCond(4);
					st.removeMemo("enemies");
					st.getPlayer().sendPacket(new ExShowScreenMessage(NpcString.YOU_WEAKENED_THE_ENEMYS_ATTACK, 4000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true));
				}
			}
		return null;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(isValidNpcKill(st.getPlayer(), npc))
		{
			int killedCatapultasCount = st.getInt("catapultas");
			int maxCatapultasCount = 2;
			killedCatapultasCount++;
			if(killedCatapultasCount < maxCatapultasCount)
				st.setMemoState("catapultas", killedCatapultasCount);
			else
			{
				if(st.getCond() == 1)
					st.setCond(3);
				else if(st.getCond() == 2)
					st.setCond(4);
				st.removeMemo("catapultas");
			}
		}
		return null;
	}

	private boolean isValidKill(Player killed, Player killer)
	{
		DominionSiegeEvent killedSiegeEvent = killed.getEvent(DominionSiegeEvent.class);
		DominionSiegeEvent killerSiegeEvent = killer.getEvent(DominionSiegeEvent.class);

		if(killedSiegeEvent == null || killerSiegeEvent == null)
			return false;
		if(killedSiegeEvent == killerSiegeEvent)
			return false;
		if(killed.getLevel() < 61)
			return false;
		return true;
	}

	private boolean isValidNpcKill(Player killer, NpcInstance npc)
	{
		DominionSiegeEvent npcSiegeEvent = npc.getEvent(DominionSiegeEvent.class);
		DominionSiegeEvent killerSiegeEvent = killer.getEvent(DominionSiegeEvent.class);

		if(npcSiegeEvent == null || killerSiegeEvent == null)
			return false;
		if(npcSiegeEvent == killerSiegeEvent)
			return false;
		return true;
	}

	@Override
	public void onCreate(QuestState qs)
	{
		super.onCreate(qs);
		qs.addPlayerOnKillListener();
	}

	@Override
	public void onAbort(QuestState qs)
	{
		qs.removePlayerOnKillListener();
		super.onAbort(qs);
	}

	@Override
	public void onLoad()
	{

	}

	@Override
	public void onReload()
	{

	}

	@Override
	public void onShutdown()
	{

	}
}
