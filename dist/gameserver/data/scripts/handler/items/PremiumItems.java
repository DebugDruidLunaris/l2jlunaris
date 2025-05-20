package handler.items;

import jts.commons.util.Rnd;
import jts.gameserver.Config;
import jts.gameserver.dao.AccountBonusDAO;
import jts.gameserver.loginservercon.LoginServerCommunication;
import jts.gameserver.loginservercon.gspackets.BonusRequest;
import jts.gameserver.model.Player;
import jts.gameserver.model.actor.instances.player.Bonus;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.network.serverpackets.ExBR_PremiumState;
import jts.gameserver.network.serverpackets.MagicSkillUse;
import jts.gameserver.network.serverpackets.SystemMessage;

public class PremiumItems extends SimpleItemHandler
{
	@Override
	public int[] getItemIds()
	{
		return Config.ITEMS_GET_PREMIUM;
	}

	@Override
	protected boolean useItemImpl(Player player, ItemInstance item, boolean ctrl)
	{
		int itemId = item.getItemId();

		if(Config.SERVICES_RATE_TYPE == Bonus.BONUS_GLOBAL_ON_LOGINSERVER && LoginServerCommunication.getInstance().isShutdown())
			return false;

		if(player.isInOlympiadMode())
		{
			player.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(itemId));
			return false;
		}

		if(Config.ITEMS_GET_PREMIUM_DAYS[0] <= 0)
			return false;

		if(player.isOutOfControl() || player.isDead() || player.isStunned() || player.isSleeping() || player.isParalyzed())
			return false;

		if(!useItem(player, item, 1))
			return false;

		if(player.hasBonus())
			return false;

		int days = 0, bonusExpire = 0;
		double bonus = 0;

		switch(Config.ITEMS_GET_PREMIUM.length)
		{
			case 0:
				bonus = Config.ITEMS_GET_PREMIUM_VALUE[0];
				if(Config.ITEMS_GET_PREMIUM_RANDOM_DAYS[0] <= 0)
					days = Config.ITEMS_GET_PREMIUM_DAYS[0];
				else
					days = Rnd.get(Config.ITEMS_GET_PREMIUM_RANDOM_DAYS[0], Config.ITEMS_GET_PREMIUM_RANDOM_DAYS[1]);
				bonusExpire = (int) (System.currentTimeMillis() / 1000L) + days * 24 * 60 * 60;
				break;
			case 1:
				bonus = Config.ITEMS_GET_PREMIUM_VALUE[1];
				if(Config.ITEMS_GET_PREMIUM_RANDOM_DAYS[0] <= 0)
					days = Config.ITEMS_GET_PREMIUM_DAYS[1];
				else
					days = Rnd.get(Config.ITEMS_GET_PREMIUM_RANDOM_DAYS[0], Config.ITEMS_GET_PREMIUM_RANDOM_DAYS[1]);
				bonusExpire = (int) (System.currentTimeMillis() / 1000L) + days * 24 * 60 * 60;
				break;
			case 2:
				bonus = Config.ITEMS_GET_PREMIUM_VALUE[2];
				if(Config.ITEMS_GET_PREMIUM_RANDOM_DAYS[0] <= 0)
					days = Config.ITEMS_GET_PREMIUM_DAYS[2];
				else
					days = Rnd.get(Config.ITEMS_GET_PREMIUM_RANDOM_DAYS[0], Config.ITEMS_GET_PREMIUM_RANDOM_DAYS[1]);
				bonusExpire = (int) (System.currentTimeMillis() / 1000L) + days * 24 * 60 * 60;
				break;
			case 3:
				bonus = Config.ITEMS_GET_PREMIUM_VALUE[3];
				if(Config.ITEMS_GET_PREMIUM_RANDOM_DAYS[0] <= 0)
					days = Config.ITEMS_GET_PREMIUM_DAYS[3];
				else
					days = Rnd.get(Config.ITEMS_GET_PREMIUM_RANDOM_DAYS[0], Config.ITEMS_GET_PREMIUM_RANDOM_DAYS[1]);
				bonusExpire = (int) (System.currentTimeMillis() / 1000L) + days * 24 * 60 * 60;
				break;
			default:
				break;
		}

		if(days <= 0)
			return false;

		switch(Config.SERVICES_RATE_TYPE)
		{
			case Bonus.BONUS_GLOBAL_ON_LOGINSERVER:
				LoginServerCommunication.getInstance().sendPacket(new BonusRequest(player.getAccountName(), bonus, bonusExpire));
				break;
			case Bonus.BONUS_GLOBAL_ON_GAMESERVER:
				AccountBonusDAO.getInstance().insert(player.getAccountName(), bonus, bonusExpire);
				break;
		}

		player.getNetConnection().setBonus(bonus);
		player.getNetConnection().setBonusExpire(bonusExpire);
		player.stopBonusTask();
		player.startBonusTask();
		if(player.getParty() != null)
			player.getParty().recalculatePartyData();
		player.sendPacket(new ExBR_PremiumState(player, true));
		player.broadcastPacket(new MagicSkillUse(player, player, 6176, 1, 1000, 0));
		return true;
	}
}