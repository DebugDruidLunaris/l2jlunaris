package services;

import jts.gameserver.Config;
import jts.gameserver.common.DifferentMethods;
import jts.gameserver.data.htm.HtmCache;
import jts.gameserver.model.Player;
import jts.gameserver.model.entity.Hero;
import jts.gameserver.network.serverpackets.SocialAction;
import jts.gameserver.network.serverpackets.components.CustomMessage;
import jts.gameserver.scripts.Functions;

public class FakeHero extends Functions
{
	private static final int[] ITEM = Config.SERVICES_HERO_SELL_ITEM;
	private static final int[] PRICE = Config.SERVICES_HERO_SELL_PRICE;
	private static final int[] DAY = Config.SERVICES_HERO_SELL_DAY;

	public void list()
	{
		Player player = getSelf();

		String html = null;

		if(!player.isHero() || !player.getVarB("hasFakeHero"))
		{
			html = HtmCache.getInstance().getNotNull("scripts/services/FakeHero/index.htm", player);
			String content = "";
			for(int i = 0; i < DAY.length; i++)
			{
				content += "<button value=\"" + new CustomMessage("scripts.services.FakeHero.button", player).addNumber(DAY[i]).addString(DifferentMethods.declension(player, (int) DAY[i], "Days")) + "\" action=\"bypass -h scripts_services.FakeHero:buy " + i + "\" width=200 height=29 back=\"L2UI_CT1.OlympiadWnd_DF_HeroConfirm_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_HeroConfirm\"><br1>" + new CustomMessage("scripts.services.cost", player).addNumber(PRICE[i]).addString(DifferentMethods.getItemName(ITEM[i])) + "";
			}
			html = html.replace("<?content?>", content);
		}
		else
		{
			html = HtmCache.getInstance().getNotNull("scripts/services/FakeHero/already.htm", player);
			player.sendMessage(new CustomMessage("scripts.services.FakeHero.ishero", player));
		}

		if(!Config.SERVICES_HERO_SELL_ENABLED)
		{
			html = HtmCache.getInstance().getNotNull("scripts/services/off.htm", player);
			player.sendMessage(new CustomMessage("scripts.services.off", player));
		}

		show(html, player);
	}

	public void buy(String[] param)
	{
		Player player = getSelf();

		if(player == null)
			return;

		int i = Integer.parseInt(param[0]);

		if(i > Config.SERVICES_HERO_SELL_DAY.length)
			DifferentMethods.clear(player);

		if(!Config.SERVICES_HERO_SELL_ENABLED)
		{
			player.sendMessage(new CustomMessage("scripts.services.off", player));
			return;
		}

		if(player.isHero() || player.getVarB("hasFakeHero"))
		{
			player.sendMessage(new CustomMessage("scripts.services.FakeHero.ishero", player));
			return;
		}

		if(DifferentMethods.getPay(player, ITEM[i], PRICE[i], true))
		{
			long day = DifferentMethods.addDay(DAY[i]);
			long time = System.currentTimeMillis() + day;
			try
			{
				player.setVar("hasFakeHero", time, time);
				player.sendChanges();
				player.broadcastCharInfo();

				if(Config.SERVICES_HERO_SELL_SKILL)
					Hero.addSkills(player);

				player.getPlayer().broadcastPacket(new SocialAction(player.getPlayer().getObjectId(), SocialAction.GIVE_HERO));
				player.sendMessage(new CustomMessage("scripts.services.FakeHero.congratulations", player).addNumber(DAY[i]).addString(DifferentMethods.declension(player, DAY[i], "Days")));
			}
			catch(Exception e)
			{
				player.sendMessage(new CustomMessage("common.Error", player));
			}
		}
	}
}