package jts.gameserver.network.clientpackets;

import jts.gameserver.model.Player;
import jts.gameserver.model.World;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.network.serverpackets.ExGMViewQuestItemList;
import jts.gameserver.network.serverpackets.GMHennaInfo;
import jts.gameserver.network.serverpackets.GMViewCharacterInfo;
import jts.gameserver.network.serverpackets.GMViewItemList;
import jts.gameserver.network.serverpackets.GMViewPledgeInfo;
import jts.gameserver.network.serverpackets.GMViewQuestInfo;
import jts.gameserver.network.serverpackets.GMViewSkillInfo;
import jts.gameserver.network.serverpackets.GMViewWarehouseWithdrawList;

public class RequestGMCommand extends L2GameClientPacket
{
	private String _targetName;
	private int _command;

	@Override
	protected void readImpl()
	{
		_targetName = readS();
		_command = readD();
		// readD();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		Player target = World.getPlayer(_targetName);
		if(player == null || target == null)
			return;
		if(!player.getPlayerAccess().CanViewChar)
			return;

		switch(_command)
		{
			case 1:
				player.sendPacket(new GMViewCharacterInfo(target));
				player.sendPacket(new GMHennaInfo(target));
				break;
			case 2:
				if(target.getClan() != null)
					player.sendPacket(new GMViewPledgeInfo(target));
				break;
			case 3:
				player.sendPacket(new GMViewSkillInfo(target));
				break;
			case 4:
				player.sendPacket(new GMViewQuestInfo(target));
				break;
			case 5:
				ItemInstance[] items = target.getInventory().getItems();
				int questSize = 0;
				for(ItemInstance item : items)
					if(item.getTemplate().isQuest())
						questSize++;
				player.sendPacket(new GMViewItemList(target, items, items.length - questSize));
				player.sendPacket(new ExGMViewQuestItemList(target, items, questSize));

				player.sendPacket(new GMHennaInfo(target));
				break;
			case 6:
				player.sendPacket(new GMViewWarehouseWithdrawList(target));
				break;
		}
	}
}