package jts.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.List;

import jts.gameserver.instancemanager.CursedWeaponsManager;
import jts.gameserver.model.Creature;
import jts.gameserver.model.CursedWeapon;
import jts.gameserver.network.serverpackets.ExCursedWeaponLocation;
import jts.gameserver.network.serverpackets.ExCursedWeaponLocation.CursedWeaponInfo;
import jts.gameserver.utils.Location;

public class RequestCursedWeaponLocation extends L2GameClientPacket
{
	@Override
	protected void readImpl() {}

	@Override
	protected void runImpl()
	{
		Creature activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		List<CursedWeaponInfo> list = new ArrayList<CursedWeaponInfo>();
		for(CursedWeapon cw : CursedWeaponsManager.getInstance().getCursedWeapons())
		{
			Location pos = cw.getWorldPosition();
			if(pos != null)
				list.add(new CursedWeaponInfo(pos, cw.getItemId(), cw.isActivated() ? 1 : 0));
		}

		activeChar.sendPacket(new ExCursedWeaponLocation(list));
	}
}