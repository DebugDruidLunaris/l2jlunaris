package jts.gameserver.network.serverpackets;

import jts.gameserver.instancemanager.CursedWeaponsManager;

public class ExCursedWeaponList extends L2GameServerPacket
{
	private int[] cursedWeapon_ids;

	public ExCursedWeaponList()
	{
		cursedWeapon_ids = CursedWeaponsManager.getInstance().getCursedWeaponsIds();
	}

	@Override
	protected final void writeImpl()
	{
		writeEx(0x46);
		writeDD(cursedWeapon_ids, true);
	}
}