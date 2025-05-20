package npc.model;

import java.util.StringTokenizer;

import jts.gameserver.Config;
import jts.gameserver.cache.Msg;
import jts.gameserver.data.xml.holder.ItemHolder;
import jts.gameserver.model.Player;
import jts.gameserver.model.base.ClassId;
import jts.gameserver.model.instances.MerchantInstance;
import jts.gameserver.network.serverpackets.NpcHtmlMessage;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.templates.item.ItemTemplate;
import jts.gameserver.templates.npc.NpcTemplate;
import jts.gameserver.utils.HtmlUtils;
import jts.gameserver.utils.Util;

@SuppressWarnings("serial")
public final class ClassMasterInstance extends MerchantInstance
{
	public ClassMasterInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	private String makeMessage(Player player)
	{
		ClassId classId = player.getClassId();

		int jobLevel = classId.getLevel();
		int level = player.getLevel();

		StringBuilder html = new StringBuilder();

		if(Config.ALLOW_CLASS_MASTERS_LIST.isEmpty() || !Config.ALLOW_CLASS_MASTERS_LIST.contains(jobLevel))
			jobLevel = 4;
		if((level >= 20 && jobLevel == 1 || level >= 40 && jobLevel == 2 || level >= 76 && jobLevel == 3) && Config.ALLOW_CLASS_MASTERS_LIST.contains(jobLevel))
		{
			ItemTemplate item = ItemHolder.getInstance().getTemplate(Config.CLASS_MASTERS_PRICE_ITEM);
			if(Config.CLASS_MASTERS_PRICE_LIST[jobLevel] > 0)
				html.append("Price: ").append(Util.formatAdena(Config.CLASS_MASTERS_PRICE_LIST[jobLevel])).append(" ").append(item.getName()).append("<br1>");
			for(ClassId cid : ClassId.VALUES)
			{
				// Инспектор является наследником trooper и warder, но сменить его как профессию нельзя,
				// т.к. это сабкласс. Наследуется с целью получения скилов родителей.
				if(cid == ClassId.inspector)
					continue;
				if(cid.childOf(classId) && cid.getLevel() == classId.getLevel() + 1)
					html.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_change_class ").append(cid.getId()).append(" ").append(Config.CLASS_MASTERS_PRICE_LIST[jobLevel]).append("\">").append(HtmlUtils.htmlClassName(cid.getId())).append("</a><br>");
			}
			player.sendPacket(new NpcHtmlMessage(player, this).setHtml(html.toString()));
		}
		else
			switch(jobLevel)
			{
				case 1:
					html.append("Вернись, когда вы достигнешь 20 уровня, чтобы изменить свой класс.");
					break;
				case 2:
					html.append("Вернись, когда вы достигнешь 40 уровня, чтобы изменить свой класс..");
					break;
				case 3:
					html.append("Вернись, когда вы достигнешь 76 уровня, чтобы изменить свой класс..");
					break;
				case 4:
					html.append("Больше нет не каких изменений для вашего класса.");
					break;
			}
		return html.toString();
	}

	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		NpcHtmlMessage msg = new NpcHtmlMessage(player, this);
		msg.setFile("custom/31860.htm");
		msg.replace("%classmaster%", makeMessage(player));
		player.sendPacket(msg);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{

		StringTokenizer st = new StringTokenizer(command);
		if(st.nextToken().equals("change_class"))
		{
			int val = Integer.parseInt(st.nextToken());
			long price = Long.parseLong(st.nextToken());
			if(player.getInventory().destroyItemByItemId(Config.CLASS_MASTERS_PRICE_ITEM, price))
				changeClass(player, val);
			else if(Config.CLASS_MASTERS_PRICE_ITEM == 57)
				player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			else
				player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
		}
		else
			super.onBypassFeedback(player, command);
	}

	private void changeClass(Player player, int val)
	{
		if(player.getClassId().getLevel() == 3)
			player.sendPacket(Msg.YOU_HAVE_COMPLETED_THE_QUEST_FOR_3RD_OCCUPATION_CHANGE_AND_MOVED_TO_ANOTHER_CLASS_CONGRATULATIONS); // для 3 профы
		else
			player.sendPacket(Msg.CONGRATULATIONS_YOU_HAVE_TRANSFERRED_TO_A_NEW_CLASS); // для 1 и 2 профы

		player.setClassId(val, false, false);
		if(Config.SERVICES_3_CLASS_CHANGE_REWARD_ENABLED && player.getClassId().getLevel() == 3)
			if(player.isMageClass())
				for(int i = 0; i < Config.SERVICES_3_CLASS_MAGE_CHANGE_REWARD.length; i += 2)
					player.getInventory().addItem(Config.SERVICES_3_CLASS_MAGE_CHANGE_REWARD[i], Config.SERVICES_3_CLASS_MAGE_CHANGE_REWARD[i + 1]);
			else
				for(int i = 0; i < Config.SERVICES_3_CLASS_FIGHTER_CHANGE_REWARD.length; i += 2)
					player.getInventory().addItem(Config.SERVICES_3_CLASS_FIGHTER_CHANGE_REWARD[i], Config.SERVICES_3_CLASS_FIGHTER_CHANGE_REWARD[i + 1]);
		player.broadcastCharInfo();
	}
}