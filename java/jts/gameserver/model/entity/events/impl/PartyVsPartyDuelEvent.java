package jts.gameserver.model.entity.events.impl;

import java.util.Iterator;
import java.util.List;

import jts.commons.collections.CollectionUtils;
import jts.commons.collections.JoinedIterator;
import jts.commons.collections.MultiValueSet;
import jts.gameserver.ai.CtrlEvent;
import jts.gameserver.data.xml.holder.InstantZoneHolder;
import jts.gameserver.model.GameObject;
import jts.gameserver.model.Party;
import jts.gameserver.model.Player;
import jts.gameserver.model.Request;
import jts.gameserver.model.World;
import jts.gameserver.model.base.TeamType;
import jts.gameserver.model.entity.Reflection;
import jts.gameserver.model.entity.events.objects.DuelSnapshotObject;
import jts.gameserver.network.serverpackets.ExDuelAskStart;
import jts.gameserver.network.serverpackets.ExDuelEnd;
import jts.gameserver.network.serverpackets.ExDuelReady;
import jts.gameserver.network.serverpackets.SocialAction;
import jts.gameserver.network.serverpackets.SystemMessage2;
import jts.gameserver.network.serverpackets.components.IStaticPacket;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.templates.InstantZone;

public class PartyVsPartyDuelEvent extends DuelEvent
{
	public PartyVsPartyDuelEvent(MultiValueSet<String> set)
	{
		super(set);
	}

	protected PartyVsPartyDuelEvent(int id, String name)
	{
		super(id, name);
	}

	@Override
	public void stopEvent()
	{
		clearActions();

		updatePlayers(false, false);

		for(DuelSnapshotObject d : this)
		{
			d.getPlayer().sendPacket(new ExDuelEnd(this));
			GameObject target = d.getPlayer().getTarget();
			if(target != null)
				d.getPlayer().getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, target);
		}

		switch(_winner)
		{
			case NONE:
				sendPacket(SystemMsg.THE_DUEL_HAS_ENDED_IN_A_TIE);
				break;
			case RED:
			case BLUE:
				List<DuelSnapshotObject> winners = getObjects(_winner.name());
				List<DuelSnapshotObject> lossers = getObjects(_winner.revert().name());

				DuelSnapshotObject winner = CollectionUtils.safeGet(winners, 0);
				if(winner != null)
				{
					sendPacket(new SystemMessage2(SystemMsg.C1S_PARTY_HAS_WON_THE_DUEL).addName(winners.get(0).getPlayer()));

					for(DuelSnapshotObject d : lossers)
						d.getPlayer().broadcastPacket(new SocialAction(d.getPlayer().getObjectId(), SocialAction.BOW));
				}
				else
					sendPacket(SystemMsg.THE_DUEL_HAS_ENDED_IN_A_TIE);
				break;
		}

		updatePlayers(false, true);
		removeObjects(RED_TEAM);
		removeObjects(BLUE_TEAM);
	}

	@Override
	public void teleportPlayers(String name)
	{
		InstantZone instantZone = InstantZoneHolder.getInstance().getInstantZone(1);

		Reflection reflection = new Reflection();
		reflection.init(instantZone);

		List<DuelSnapshotObject> team = getObjects(BLUE_TEAM);

		for(int i = 0; i < team.size(); i++)
		{
			DuelSnapshotObject $member = team.get(i);

			$member.getPlayer().addEvent(this);
			$member.getPlayer()._stablePoint = $member.getLoc();
			$member.getPlayer().teleToLocation(instantZone.getTeleportCoords().get(i), reflection);
		}

		team = getObjects(RED_TEAM);

		for(int i = 0; i < team.size(); i++)
		{
			DuelSnapshotObject $member = team.get(i);

			$member.getPlayer().addEvent(this);
			$member.getPlayer()._stablePoint = $member.getLoc();
			$member.getPlayer().teleToLocation(instantZone.getTeleportCoords().get(9 + i), reflection);
		}
	}

	@Override
	public boolean canDuel(Player player, Player target, boolean first)
	{
		if(player.getParty() == null)
		{
			player.sendPacket(SystemMsg.YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME);
			return false;
		}

		if(target.getParty() == null)
		{
			player.sendPacket(SystemMsg.SINCE_THE_PERSON_YOU_CHALLENGED_IS_NOT_CURRENTLY_IN_A_PARTY_THEY_CANNOT_DUEL_AGAINST_YOUR_PARTY);
			return false;
		}

		Party party1 = player.getParty();
		Party party2 = target.getParty();
		if(player != party1.getPartyLeader() || target != party2.getPartyLeader())
		{
			player.sendPacket(SystemMsg.YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME);
			return false;
		}

		Iterator<Player> iterator = new JoinedIterator<Player>(party1.iterator(), party2.iterator());
		while(iterator.hasNext())
		{
			Player $member = iterator.next();

			IStaticPacket packet = null;
			if((packet = canDuel0(player, $member)) != null)
			{
				player.sendPacket(packet);
				target.sendPacket(packet);
				return false;
			}
		}
		return true;
	}

	@Override
	public void askDuel(Player player, Player target)
	{
		Request request = new Request(Request.L2RequestType.DUEL, player, target).setTimeout(10000L);
		request.set("duelType", 1);
		player.setRequest(request);
		target.setRequest(request);

		player.sendPacket(new SystemMessage2(SystemMsg.C1S_PARTY_HAS_BEEN_CHALLENGED_TO_A_DUEL).addName(target));
		target.sendPacket(new SystemMessage2(SystemMsg.C1S_PARTY_HAS_CHALLENGED_YOUR_PARTY_TO_A_DUEL).addName(player), new ExDuelAskStart(player.getName(), 1));
	}

	@Override
	public void createDuel(Player player, Player target)
	{
		PartyVsPartyDuelEvent duelEvent = new PartyVsPartyDuelEvent(getDuelType(), player.getObjectId() + "_" + target.getObjectId() + "_duel");
		cloneTo(duelEvent);

		for(Player $member : player.getParty())
			duelEvent.addObject(BLUE_TEAM, new DuelSnapshotObject($member, TeamType.BLUE));

		for(Player $member : target.getParty())
			duelEvent.addObject(RED_TEAM, new DuelSnapshotObject($member, TeamType.RED));

		duelEvent.sendPacket(new ExDuelReady(this));
		duelEvent.reCalcNextTime(false);
	}

	@Override
	public void playerExit(Player player)
	{
		for(DuelSnapshotObject $snapshot : this)
		{
			if($snapshot.getPlayer() == player)
				removeObject($snapshot.getTeam().name(), $snapshot);

			List<DuelSnapshotObject> objects = getObjects($snapshot.getTeam().name());
			if(objects.isEmpty())
			{
				_winner = $snapshot.getTeam().revert();
				stopEvent();
			}
		}
	}

	@Override
	public void packetSurrender(Player player)
	{
		//
	}

	@Override
	public void onDie(Player player)
	{
		TeamType team = player.getTeam();
		if(team == TeamType.NONE || _aborted)
			return;

		sendPacket(SystemMsg.THE_OTHER_PARTY_IS_FROZEN, team.revert().name());

		player.stopAttackStanceTask();
		player.startFrozen();
		player.setTeam(TeamType.NONE);

		for(Player $player : World.getAroundPlayers(player))
		{
			$player.getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, player);
			if(player.getPet() != null)
				$player.getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, player.getPet());
		}
		player.sendChanges();

		boolean allDead = true;
		List<DuelSnapshotObject> objs = getObjects(team.name());
		for(DuelSnapshotObject obj : objs)
		{
			if(obj.getPlayer() == player)
				obj.setDead();

			if(!obj.isDead())
				allDead = false;
		}

		if(allDead)
		{
			_winner = team.revert();

			stopEvent();
		}
	}

	@Override
	public int getDuelType()
	{
		return 1;
	}

	@Override
	protected long startTimeMillis()
	{
		return System.currentTimeMillis() + 30000L;
	}
}