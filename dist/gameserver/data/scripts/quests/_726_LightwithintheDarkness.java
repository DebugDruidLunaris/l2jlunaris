package quests;

import jts.gameserver.data.xml.holder.ResidenceHolder;
import jts.gameserver.model.GameObjectsStorage;
import jts.gameserver.model.Party;
import jts.gameserver.model.Player;
import jts.gameserver.model.entity.residence.Fortress;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.pledge.Clan;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.network.serverpackets.SystemMessage;
import jts.gameserver.scripts.ScriptFile;

/**
 * @author: pchayka
 * @date: 26.09.2010
 */
public class _726_LightwithintheDarkness extends Quest implements ScriptFile
{
	// ITEMS
	private static int KnightsEpaulette = 9912;

	// MOB's
	private static int KanadisGuide3 = 25661;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _726_LightwithintheDarkness()
	{
		super(true);

		addStartNpc(35666, 35698, 35735, 35767, 35804, 35835, 35867, 35904, 35936, 35974, 36011, 36043, 36081, 36118, 36149, 36181, 36219, 36257, 36294, 36326, 36364);
		addKillId(KanadisGuide3);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		int cond = st.getCond();
		String htmltext = event;
		Player player = st.getPlayer();

		if(event.equals("dcw_q726_4.htm") && cond == 0)
		{
			st.setCond(1);
			st.setState(STARTED);
			st.soundEffect(SOUND_ACCEPT);
		}
		else if(event.equals("reward") && cond == 1 && player.getVar("q726").equalsIgnoreCase("done"))
		{
			player.unsetVar("q726");
			st.giveItems(KnightsEpaulette, 152);
			st.soundEffect(SOUND_FINISH);
			st.exitQuest(true);
			return null;
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int cond = st.getCond();
		Player player = st.getPlayer();
		QuestState qs727 = player.getQuestState(_727_HopewithintheDarkness.class);

		if(!check(st.getPlayer()))
		{
			st.exitQuest(true);
			return "dcw_q726_1a.htm";
		}
		if(qs727 != null)
		{
			st.exitQuest(true);
			return "dcw_q726_1b.htm";
		}
		else if(cond == 0)
		{
			if(st.getPlayer().getLevel() >= 70)
				htmltext = "dcw_q726_1.htm";
			else
			{
				htmltext = "dcw_q726_0.htm";
				st.exitQuest(true);
			}
		}
		else if(cond == 1)
			if(player.getVar("q726") != null && player.getVar("q726").equalsIgnoreCase("done"))
				htmltext = "dcw_q726_6.htm";
			else
				htmltext = "dcw_q726_5.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		Player player = st.getPlayer();
		Party party = player.getParty();

		if(cond == 1 && npcId == KanadisGuide3 && checkAllDestroyed(KanadisGuide3, player.getReflectionId()))
		{
			if(player.isInParty())
				for(Player member : party.getPartyMembers())
					if(!member.isDead() && member.getParty().isInReflection())
					{
						member.sendPacket(new SystemMessage(SystemMessage.THIS_DUNGEON_WILL_EXPIRE_IN_S1_MINUTES).addNumber(5));
						member.setVar("q726", "done", -1);
					}
			player.getReflection().startCollapseTimer(5 * 60 * 1000L);
		}
		return null;
	}

	private static boolean checkAllDestroyed(int mobId, int refId)
	{
		for(NpcInstance npc : GameObjectsStorage.getAllByNpcId(mobId, true))
			if(npc.getReflectionId() == refId)
				return false;
		return true;
	}

	private boolean check(Player player)
	{
		Fortress fort = ResidenceHolder.getInstance().getResidenceByObject(Fortress.class, player);
		if(fort == null)
			return false;
		Clan clan = player.getClan();
		if(clan == null)
			return false;
		if(clan.getClanId() != fort.getOwnerId())
			return false;
		if(fort.getContractState() != 1)
			return false;
		return true;
	}
}