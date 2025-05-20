package jts.gameserver.network.serverpackets;

import jts.gameserver.model.instances.NpcInstance;

public final class ServerObjectInfo extends L2GameServerPacket
{
	private final NpcInstance _activeChar;
	private final int _x, _y, _z, _heading;
	private final int _idTemplate;
	private boolean _isAttackable;
	private final int _collisionHeight, _collisionRadius;

	public ServerObjectInfo(final NpcInstance activeChar, final Character actor)
	{
		_activeChar = activeChar;
		_idTemplate = _activeChar.getTemplate().displayId;
		if(actor == null)
			_isAttackable = false;
		else
		_isAttackable = _activeChar.isAutoAttackable(activeChar);
		_collisionHeight = (int) _activeChar.getCollisionHeight();
		_collisionRadius = (int) _activeChar.getCollisionRadius();
		_x = _activeChar.getX();
		_y = _activeChar.getY();
		_z = _activeChar.getZ();
		_heading = _activeChar.getHeading();
	}

	   
	protected final void writeImpl()
	{
		writeC(0x92);
		writeD(_activeChar.getObjectId());
		writeD(_idTemplate + 1000000);
		writeS(""); // name
		writeD(_isAttackable ? 1 : 0);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_heading);
		writeF(1.0); // movement multiplier
		writeF(1.0); // attack speed multiplier
		writeF(_collisionRadius);
		writeF(_collisionHeight);
		writeD((int) (_isAttackable ? _activeChar.getCurrentHp() : 0));
		writeD(_isAttackable ? _activeChar.getMaxHp() : 0);
		writeD(0x01); // object type
		writeD(0x00); // special effects
	}
}