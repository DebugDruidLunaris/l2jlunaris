package quests;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jts.commons.threading.RunnableImpl;
import jts.commons.util.Rnd;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.data.xml.holder.InstantZoneHolder;
import jts.gameserver.data.xml.holder.ResidenceHolder;
import jts.gameserver.instancemanager.ReflectionManager;
import jts.gameserver.model.Party;
import jts.gameserver.model.Player;
import jts.gameserver.model.entity.Reflection;
import jts.gameserver.model.entity.residence.Fortress;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.pledge.Clan;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.network.serverpackets.SystemMessage;
import jts.gameserver.scripts.ScriptFile;
import jts.gameserver.templates.InstantZone;
import jts.gameserver.utils.Location;

public class _511_AwlUnderFoot extends Quest implements ScriptFile
{
	private final static int INSTANCE_ZONE_ID = 22; // Fortress Dungeon

	private final static int DungeonLeaderMark = 9797;
	private final static int RewardMarksCount = 1000; // цифра с потолка
	private final static int KnightsEpaulette = 9912;

	private static final Map<Integer, Prison> _prisons = new ConcurrentHashMap<Integer, Prison>();

	private static final int HagerTheOutlaw = 25572;
	private static final int AllSeeingRango = 25575;
	private static final int Jakard = 25578;

	private static final int Helsing = 25579;
	private static final int Gillien = 25582;
	private static final int Medici = 25585;
	private static final int ImmortalMuus = 25588;

	private static final int BrandTheExile = 25589;
	private static final int CommanderKoenig = 25592;
	private static final int GergTheHunter = 25593;

	private static final int[] type1 = new int[] { HagerTheOutlaw, AllSeeingRango, Jakard };
	private static final int[] type2 = new int[] { Helsing, Gillien, Medici, ImmortalMuus };
	private static final int[] type3 = new int[] { BrandTheExile, CommanderKoenig, GergTheHunter };

	public _511_AwlUnderFoot()
	{
		super(false);

		// Detention Camp Wardens
		addStartNpc(35666, 35698, 35735, 35767, 35804, 35835, 35867, 35904, 35936, 35974, 36011, 36043, 36081, 36118, 36149, 36181, 36219, 36257, 36294, 36326, 36364);
		addQuestItem(DungeonLeaderMark);
		addKillId(HagerTheOutlaw, AllSeeingRango, Jakard, Helsing, Gillien, Medici, ImmortalMuus, BrandTheExile, CommanderKoenig, GergTheHunter);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("gludio_fort_a_campkeeper_q0511_03.htm") || event.equalsIgnoreCase("gludio_fort_a_campkeeper_q0511_06.htm"))
		{
			st.setCond(1);
			st.setState(STARTED);
			st.soundEffect(SOUND_ACCEPT);
		}
		else if(event.equalsIgnoreCase("exit"))
		{
			st.exitQuest(true);
			return null;
		}
		else if(event.equalsIgnoreCase("enter"))
			if(st.getState() == CREATED || !check(st.getPlayer()))
				return "gludio_fort_a_campkeeper_q0511_01a.htm";
			else
				return enterPrison(st.getPlayer());
		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		if(!check(st.getPlayer()))
			return "gludio_fort_a_campkeeper_q0511_01a.htm";
		if(st.getState() == CREATED)
			return "gludio_fort_a_campkeeper_q0511_01.htm";
		if(st.ownItemCount(DungeonLeaderMark) > 0)
		{
			st.giveItems(KnightsEpaulette, st.ownItemCount(DungeonLeaderMark));
			st.takeItems(DungeonLeaderMark, -1);
			st.soundEffect(SOUND_FINISH);
			return "gludio_fort_a_campkeeper_q0511_09.htm";
		}
		return "gludio_fort_a_campkeeper_q0511_10.htm";
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		for(Prison prison : _prisons.values())
			if(prison.getReflectionId() == npc.getReflectionId())
			{
				switch(npc.getNpcId())
				{
					case HagerTheOutlaw:
					case AllSeeingRango:
					case Jakard:
						prison.initSpawn(type2[Rnd.get(type2.length)], false);
						break;
					case Helsing:
					case Gillien:
					case Medici:
					case ImmortalMuus:
						prison.initSpawn(type3[Rnd.get(type3.length)], false);
						break;
					case BrandTheExile:
					case CommanderKoenig:
					case GergTheHunter:
						Party party = st.getPlayer().getParty();
						if(party != null)
							for(Player member : party.getPartyMembers())
							{
								QuestState qs = member.getQuestState(getClass());
								if(qs != null && qs.isStarted())
								{
									qs.giveItems(DungeonLeaderMark, RewardMarksCount / party.getMemberCount());
									qs.soundEffect(SOUND_ITEMGET);
									qs.getPlayer().sendPacket(new SystemMessage(SystemMessage.THIS_DUNGEON_WILL_EXPIRE_IN_S1_MINUTES).addNumber(5));
								}
							}
						else
						{
							st.giveItems(DungeonLeaderMark, RewardMarksCount);
							st.soundEffect(SOUND_ITEMGET);
							st.getPlayer().sendPacket(new SystemMessage(SystemMessage.THIS_DUNGEON_WILL_EXPIRE_IN_S1_MINUTES).addNumber(5));
						}
						Reflection r = ReflectionManager.getInstance().get(prison.getReflectionId());
						if(r != null)
							r.startCollapseTimer(300000); // Всех боссов убили, запускаем коллапс через 5 минут
						break;
				}
				break;
			}

		return null;
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
		return true;
	}

	private String enterPrison(Player player)
	{
		Fortress fort = ResidenceHolder.getInstance().getResidenceByObject(Fortress.class, player);
		if(fort == null || fort.getOwner() != player.getClan())
			return "gludio_fort_a_campkeeper_q0511_01a.htm";

		// Крепость должна быть независимой
		if(fort.getContractState() != 1)
			return "gludio_fort_a_campkeeper_q0511_13.htm";
		if(!areMembersSameClan(player))
			return "gludio_fort_a_campkeeper_q0511_01a.htm";
		if(player.canEnterInstance(INSTANCE_ZONE_ID))
		{
			InstantZone iz = InstantZoneHolder.getInstance().getInstantZone(INSTANCE_ZONE_ID);
			Prison prison = null;
			if(!_prisons.isEmpty())
			{
				prison = _prisons.get(fort.getId());
				if(prison != null && prison.isLocked())
				{
					// TODO правильное сообщение
					player.sendPacket(new SystemMessage(SystemMessage.C1_MAY_NOT_RE_ENTER_YET).addName(player));
					return null;
				}
			}
			prison = new Prison(fort.getId(), iz);
			_prisons.put(prison.getFortId(), prison);

			Reflection r = ReflectionManager.getInstance().get(prison.getReflectionId());

			r.setReturnLoc(player.getLoc());

			for(Player member : player.getParty().getPartyMembers())
			{
				if(member != player)
					newQuestState(member, STARTED);
				member.setReflection(r);
				member.teleToLocation(iz.getTeleportCoord());
				member.setVar("backCoords", r.getReturnLoc().toXYZString(), -1);
				member.setInstanceReuse(iz.getId(), System.currentTimeMillis());
			}

			player.getParty().setReflection(r);
			r.setParty(player.getParty());
			r.startCollapseTimer(iz.getTimelimit() * 60 * 1000L);
			player.getParty().broadCast(new SystemMessage(SystemMessage.THIS_DUNGEON_WILL_EXPIRE_IN_S1_MINUTES).addNumber(iz.getTimelimit()));

			prison.initSpawn(type1[Rnd.get(type1.length)], true);
		}
		return null;
	}

	private class Prison
	{
		private int _fortId;
		private int _reflectionId;
		private long _lastEnter;

		private class PrisonSpawnTask extends RunnableImpl
		{
			int _npcId;

			public PrisonSpawnTask(int npcId)
			{
				_npcId = npcId;
			}

			@Override
			public void runImpl() throws Exception
			{
				addSpawnToInstance(_npcId, new Location(53304, 245992, -6576, 25958), 0, _reflectionId);
			}
		}

		public Prison(int id, InstantZone iz)
		{
			try
			{
				Reflection r = new Reflection();
				r.init(iz);
				_reflectionId = r.getId();
				_fortId = id;
				_lastEnter = System.currentTimeMillis();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		public void initSpawn(int npcId, boolean first)
		{
			ThreadPoolManager.getInstance().schedule(new PrisonSpawnTask(npcId), first ? 60000 : 180000);
		}

		public int getReflectionId()
		{
			return _reflectionId;
		}

		public int getFortId()
		{
			return _fortId;
		}

		public boolean isLocked()
		{
			return System.currentTimeMillis() - _lastEnter < 4 * 60 * 60 * 1000L;
		}
	}

	private boolean areMembersSameClan(Player player)
	{
		if(player.getParty() == null)
			return true;
		for(Player p : player.getParty().getPartyMembers())
			if(p.getClan() != player.getClan())
				return false;
		return true;
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