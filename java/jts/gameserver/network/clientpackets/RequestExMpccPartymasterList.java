package jts.gameserver.network.clientpackets;

import java.util.HashSet;
import java.util.Set;

import jts.gameserver.model.Player;
import jts.gameserver.model.matching.MatchingRoom;
import jts.gameserver.network.serverpackets.ExMpccPartymasterList;

public class RequestExMpccPartymasterList extends L2GameClientPacket
{
	@Override
	protected void readImpl() {}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		MatchingRoom room = player.getMatchingRoom();
		if(room == null || room.getType() != MatchingRoom.CC_MATCHING)
			return;

		Set<String> set = new HashSet<String>();
		for(Player $member : room.getPlayers())
			if($member.getParty() != null)
				set.add($member.getParty().getPartyLeader().getName());

		player.sendPacket(new ExMpccPartymasterList(set));
	}
}