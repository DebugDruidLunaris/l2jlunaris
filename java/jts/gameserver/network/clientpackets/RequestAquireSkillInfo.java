package jts.gameserver.network.clientpackets;

import jts.commons.lang.ArrayUtils;
import jts.gameserver.data.xml.holder.SkillAcquireHolder;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.SkillLearn;
import jts.gameserver.model.base.AcquireType;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.network.serverpackets.AcquireSkillInfo;
import jts.gameserver.tables.SkillTable;

public class RequestAquireSkillInfo extends L2GameClientPacket
{
	private int _id;
	private int _level;
	private AcquireType _type;

	@Override
	protected void readImpl()
	{
		_id = readD();
		_level = readD();
		_type = ArrayUtils.valid(AcquireType.VALUES, readD());
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null || player.getTransformation() != 0 || SkillTable.getInstance().getInfo(_id, _level) == null || _type == null)
			return;

		NpcInstance trainer = player.getLastNpc();
		if((trainer == null || player.getDistance(trainer.getX(), trainer.getY()) > Creature.INTERACTION_DISTANCE) && !player.isGM())
			return;

		SkillLearn skillLearn = SkillAcquireHolder.getInstance().getSkillLearn(player, _id, _level, _type);
		if(skillLearn == null)
			return;

		sendPacket(new AcquireSkillInfo(_type, skillLearn));
	}
}