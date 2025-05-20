package services.community;

import jts.gameserver.Config;
import jts.gameserver.common.DifferentMethods;
import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.ExShowScreenMessage;
import jts.gameserver.network.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import jts.gameserver.network.serverpackets.components.CustomMessage;

public class CommunityBoardClearPK
{
	public static void clear(Player player)
	{
		CustomMessage ClearPk = new CustomMessage("communityboard.clear.pk", player);
		CustomMessage ClearPkImpossible = new CustomMessage("communityboard.wash.sins.impossible", player);

		if(player.isDead() || player.isAlikeDead() || player.isCastingNow() || player.isInCombat() || player.isAttackingNow() || player.isFlying())
		{
			player.sendMessage(ClearPkImpossible);
			player.sendPacket(new ExShowScreenMessage(ClearPkImpossible.toString(), 3000, ScreenMessageAlign.TOP_CENTER, true));
			return;
		}
		else
		{
			if(!DifferentMethods.getPay(player, Config.BBS_CLEAR_PK_PRICE_ITEM_ID, Config.BBS_CLEAR_PK_PRICE, true))
				return;

			int pkCount = player.getPkKills() - Config.BBS_CLEAR_PK_COUNT;

			// Se os pontos de pk mais do que o número de vezes que ele é levantado para remover a configuração, se menor que 0 conjunto.
			if(pkCount >= 0)
				player.setPkKills(pkCount);
			else
				player.setPkKills(0);

			player.sendPacket(new ExShowScreenMessage(ClearPk.toString(), 3000, ScreenMessageAlign.TOP_CENTER, true));
			player.sendMessage(ClearPk);
		}
	}
}