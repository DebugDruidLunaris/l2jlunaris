package jts.gameserver.model.matching;

import jts.gameserver.model.CommandChannel;
import jts.gameserver.model.Party;
import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.ExDissmissMpccRoom;
import jts.gameserver.network.serverpackets.ExManageMpccRoomMember;
import jts.gameserver.network.serverpackets.ExMpccRoomInfo;
import jts.gameserver.network.serverpackets.ExMpccRoomMember;
import jts.gameserver.network.serverpackets.L2GameServerPacket;
import jts.gameserver.network.serverpackets.components.SystemMsg;

public class CCMatchingRoom extends MatchingRoom
{
	public CCMatchingRoom(Player leader, int minLevel, int maxLevel, int maxMemberSize, int lootType, String topic)
	{
		super(leader, minLevel, maxLevel, maxMemberSize, lootType, topic);

		leader.sendPacket(SystemMsg.THE_COMMAND_CHANNEL_MATCHING_ROOM_WAS_CREATED);
	}

	@Override
	public SystemMsg notValidMessage()
	{
		return SystemMsg.YOU_CANNOT_ENTER_THE_COMMAND_CHANNEL_MATCHING_ROOM_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS;
	}

	@Override
	public SystemMsg enterMessage()
	{
		return SystemMsg.C1_ENTERED_THE_COMMAND_CHANNEL_MATCHING_ROOM;
	}

	@Override
	public SystemMsg exitMessage(boolean toOthers, boolean kick)
	{
		if(!toOthers)
			return kick ? SystemMsg.YOU_WERE_EXPELLED_FROM_THE_COMMAND_CHANNEL_MATCHING_ROOM : SystemMsg.YOU_EXITED_FROM_THE_COMMAND_CHANNEL_MATCHING_ROOM;
		else
			return null;
	}

	@Override
	public SystemMsg closeRoomMessage()
	{
		return SystemMsg.THE_COMMAND_CHANNEL_MATCHING_ROOM_WAS_CANCELLED;
	}

	@Override
	public L2GameServerPacket closeRoomPacket()
	{
		return ExDissmissMpccRoom.STATIC;
	}

	@Override
	public L2GameServerPacket infoRoomPacket()
	{
		return new ExMpccRoomInfo(this);
	}

	@Override
	public L2GameServerPacket addMemberPacket(Player $member, Player active)
	{
		return new ExManageMpccRoomMember(ExManageMpccRoomMember.ADD_MEMBER, this, active);
	}

	@Override
	public L2GameServerPacket removeMemberPacket(Player $member, Player active)
	{
		return new ExManageMpccRoomMember(ExManageMpccRoomMember.REMOVE_MEMBER, this, active);
	}

	@Override
	public L2GameServerPacket updateMemberPacket(Player $member, Player active)
	{
		return new ExManageMpccRoomMember(ExManageMpccRoomMember.UPDATE_MEMBER, this, active);
	}

	@Override
	public L2GameServerPacket membersPacket(Player active)
	{
		return new ExMpccRoomMember(this, active);
	}

	@Override
	public int getType()
	{
		return CC_MATCHING;
	}

	@Override
	public void disband()
	{
		Party party = _leader.getParty();
		if(party != null)
		{
			CommandChannel commandChannel = party.getCommandChannel();
			if(commandChannel != null)
				commandChannel.setMatchingRoom(null);
		}

		super.disband();
	}

	@Override
	public int getMemberType(Player member)
	{
		Party party = _leader.getParty();
		CommandChannel commandChannel = party.getCommandChannel();
		if(member == _leader)
			return MatchingRoom.UNION_LEADER;
		else if(member.getParty() == null)
			return MatchingRoom.WAIT_NORMAL;
		else if(member.getParty() == party || commandChannel.getParties().contains(member.getParty()))
			return MatchingRoom.UNION_PARTY;
		else if(member.getParty() != null)
			return MatchingRoom.WAIT_PARTY;
		else
			return MatchingRoom.WAIT_NORMAL;
	}
}