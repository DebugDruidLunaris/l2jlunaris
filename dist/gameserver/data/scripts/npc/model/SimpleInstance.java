package npc.model;

import jts.gameserver.data.htm.HtmCache;
import jts.gameserver.data.xml.holder.MultiSellHolder;
import jts.gameserver.model.Player;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.network.serverpackets.NpcHtmlMessage;
import jts.gameserver.templates.npc.NpcTemplate;
import events.Simple.Simple;

public class SimpleInstance extends NpcInstance
{
	private static final long serialVersionUID = 1L;
	private static final String WELCOME = "events/simple/welcome.htm";
	private static final String FAQ = "events/simple/faq.htm";

	public SimpleInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void showChatWindow(Player player, int val, Object... args)
	{
		player.sendPacket(new NpcHtmlMessage(player, this, HtmCache.getInstance().getNotNull(WELCOME, player), val));
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		NpcHtmlMessage welcome = new NpcHtmlMessage(player, this);
		welcome.setFile(WELCOME);

		NpcHtmlMessage faq = new NpcHtmlMessage(player, this);
		faq.setFile(FAQ);

		if(!canBypassCheck(player, this))
			return;

		if(command.equalsIgnoreCase("simple_shop"))
		{
			player.sendPacket(welcome);
			MultiSellHolder.getInstance().SeparateAndSend(Simple.EVENT_MANAGER_MULTISELL, player, 0);
		}
		else if(command.startsWith("simple_index"))
		{
			player.sendPacket(welcome);
		}
		else if(command.startsWith("simple_faq"))
		{
			faq.replace("<?monster_level_min?>", String.valueOf(Simple.MONSTER_MIN_LEVEL));
			faq.replace("<?monster_level_max?>", String.valueOf(Simple.MONSTER_MAX_LEVEL));
			faq.replace("<?monster_items_min?>", String.valueOf(Simple.ITEM_MIN_COUNT));
			faq.replace("<?monster_items_min?>", String.valueOf(Simple.ITEM_MAX_COUNT));
			faq.replace("<?monster_items_chance?>", String.valueOf(Simple.ITEM_CHANCE));
			player.sendPacket(faq);
		}
		else
			super.onBypassFeedback(player, command);
	}
}