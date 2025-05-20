package services.community;

import jts.gameserver.Config;
import jts.gameserver.common.DifferentMethods;
import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.ExShowScreenMessage;
import jts.gameserver.network.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import jts.gameserver.network.serverpackets.components.CustomMessage;

public class CommunityBoardWashSins
{
	public static void wash(Player player)
	{
		CustomMessage WashSins = new CustomMessage("communityboard.clear.pk", player);
		CustomMessage WashSinsImpossible = new CustomMessage("communityboard.wash.sins.impossible", player);

		if(player.isDead() || player.isAlikeDead() || player.isCastingNow() || player.isInCombat() || player.isAttackingNow() || player.isFlying())
		{
			player.sendMessage(WashSinsImpossible);
			player.sendPacket(new ExShowScreenMessage(WashSinsImpossible.toString(), 3000, ScreenMessageAlign.TOP_CENTER, true));
			return;
		}
		else
		{
			if(!DifferentMethods.getPay(player, Config.BBS_WASH_SINS_PRICE_ITEM_ID, Config.BBS_WASH_SINS_PRICE, true))
				return;

			player.setKarma(0);
			player.sendMessage(WashSins);
			player.sendPacket(new ExShowScreenMessage(WashSins.toString(), 3000, ScreenMessageAlign.TOP_CENTER, true));
		}
	}
}