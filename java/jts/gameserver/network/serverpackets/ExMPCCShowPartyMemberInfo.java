package jts.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import jts.gameserver.model.Party;
import jts.gameserver.model.Player;

public class ExMPCCShowPartyMemberInfo extends L2GameServerPacket
{
	private List<PartyMemberInfo> members;

	public ExMPCCShowPartyMemberInfo(Party party)
	{
		members = new ArrayList<PartyMemberInfo>();
		for(Player _member : party.getPartyMembers())
			members.add(new PartyMemberInfo(_member.getName(), _member.getObjectId(), _member.getClassId().getId()));
	}

	@Override
	protected final void writeImpl()
	{
		writeEx(0x4b);
		writeD(members.size()); // Количество членов в пати

		for(PartyMemberInfo member : members)
		{
			writeS(member.name); // Имя члена пати
			writeD(member.object_id); // object Id члена пати
			writeD(member.class_id); // id класса члена пати
		}

		members.clear();
	}

	static class PartyMemberInfo
	{
		public String name;
		public int object_id, class_id;

		public PartyMemberInfo(String _name, int _object_id, int _class_id)
		{
			name = _name;
			object_id = _object_id;
			class_id = _class_id;
		}
	}
}