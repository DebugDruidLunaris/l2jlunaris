package services.community;

import java.util.StringTokenizer;

import jts.gameserver.Config;
import jts.gameserver.common.DifferentMethods;
import jts.gameserver.data.htm.HtmCache;
import jts.gameserver.data.xml.holder.ItemHolder;
import jts.gameserver.handler.bbs.CommunityBoardManager;
import jts.gameserver.handler.bbs.ICommunityBoardHandler;
import jts.gameserver.model.Player;
import jts.gameserver.model.base.Element;
import jts.gameserver.model.items.Inventory;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.network.serverpackets.InventoryUpdate;
import jts.gameserver.network.serverpackets.ShowBoard;
import jts.gameserver.network.serverpackets.components.CustomMessage;
import jts.gameserver.scripts.ScriptFile;
import jts.gameserver.templates.item.ItemTemplate.Grade;
import jts.gameserver.templates.item.WeaponTemplate.WeaponType;
import jts.gameserver.utils.Log;

public class CommunityBoardForge implements ScriptFile, ICommunityBoardHandler
{

	@Override
	public void onLoad()
	{
		if(Config.COMMUNITYBOARD_ENABLED)
		{
			CommunityBoardManager.getInstance().registerHandler(this);
		}
	}

	@Override
	public void onReload()
	{
		if(Config.COMMUNITYBOARD_ENABLED)
			CommunityBoardManager.getInstance().removeHandler(this);
	}

	@Override
	public void onShutdown() {}

	@Override
	public String[] getBypassCommands()
	{
		return new String[] 
		{
			"_bbsforge",
			"_bbsforge:enchant:list",
			"_bbsforge:enchant:item",
			"_bbsforge:attribute:list",
			"_bbsforge:attribute:item",
			"_bbsforge:enchant:",
			"_bbsforge:attribute:" 
		};
	}

	@Override
	public void onBypassCommand(Player player, String command)
	{
		String content = "";
		if(command.equals("_bbsforge"))
		{
			content = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/forge/index.htm", player);
		}
		else if(command.equals("_bbsforge:enchant:list"))
		{
			content = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/forge/itemlist.htm", player);

			String noicon = "icon.NOIMAGE";
			String slotclose = "L2UI_CT1.ItemWindow_DF_SlotBox_Disable";
			String dot = "<font color=\"FF0000\">...</font>";
			String maxenchant = new CustomMessage("communityboard.forge.enchant.max", player).toString();
			String picenchant = "l2ui_ch3.multisell_plusicon";
			String pvp = "icon.pvp_tab";

			String HeadButton = dot;
			String HeadIcon = noicon;
			String HeadPic = slotclose;
			String HeadName = new CustomMessage("communityboard.forge.enchant.max", player).toString();

			String ChestButton = dot;
			String ChestIcon = noicon;
			String ChestPic = slotclose;
			String ChestName = new CustomMessage("common.item.not.clothed.chest", player).toString();

			String LegsButton = dot;
			String LegsIcon = noicon;
			String LegsPic = slotclose;
			String LegsName = new CustomMessage("common.item.not.clothed.legs", player).toString();

			String FeetButton = dot;
			String FeetIcon = noicon;
			String FeetPic = slotclose;
			String FeetName = new CustomMessage("common.item.not.clothed.feet", player).toString();

			String GlovesButton = dot;
			String GlovesIcon = noicon;
			String GlovesPic = slotclose;
			String GlovesName = new CustomMessage("common.item.not.clothed.gloves", player).toString();

			String LEarButton = dot;
			String LEarIcon = noicon;
			String LEarPic = slotclose;
			String LEarName = new CustomMessage("common.item.not.clothed.lear", player).toString();

			String REarButton = dot;
			String REarIcon = noicon;
			String REarPic = slotclose;
			String REarName = new CustomMessage("common.item.not.clothed.rear", player).toString();

			String NeckButton = dot;
			String NeckIcon = noicon;
			String NeckPic = slotclose;
			String NeckName = new CustomMessage("common.item.not.clothed.neck", player).toString();

			String LRingButton = dot;
			String LRingIcon = noicon;
			String LRingPic = slotclose;
			String LRingName = new CustomMessage("common.item.not.clothed.lring", player).toString();

			String RRingButton = dot;
			String RRingIcon = noicon;
			String RRingPic = slotclose;
			String RRingName = new CustomMessage("common.item.not.clothed.rring", player).toString();

			String WeaponButton = dot;
			String WeaponIcon = noicon;
			String WeaponPic = slotclose;
			String WeaponName = new CustomMessage("common.item.not.clothed.weapon", player).toString();

			String ShieldButton = dot;
			String ShieldIcon = noicon;
			String ShieldPic = slotclose;
			String ShieldName = new CustomMessage("common.item.not.clothed.shield", player).toString();

			ItemInstance head = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_HEAD);
			ItemInstance chest = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
			ItemInstance legs = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS);
			ItemInstance gloves = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_GLOVES);
			ItemInstance feet = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_FEET);

			ItemInstance lhand = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
			ItemInstance rhand = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);

			ItemInstance lfinger = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LFINGER);
			ItemInstance rfinger = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RFINGER);
			ItemInstance neck = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_NECK);
			ItemInstance lear = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEAR);
			ItemInstance rear = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_REAR);

			if(head != null)
			{
				HeadIcon = head.getTemplate().getIcon();
				HeadName = head.getName() + " " + (head.getEnchantLevel() > 0 ? "+" + head.getEnchantLevel() : "");

				if(head.getEnchantLevel() == Config.BBS_ENCHANT_MAX[1])
				{
					HeadButton = maxenchant;
					HeadPic = slotclose;
				}
				else
				{
					HeadButton = "<button action=\"bypass _bbsforge:enchant:item:" + Inventory.PAPERDOLL_HEAD + "\" value=\"" + new CustomMessage("common.enchant", player).toString() + "\"width=120 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
					HeadPic = picenchant;
				}
			}

			if(chest != null)
			{
				ChestIcon = chest.getTemplate().getIcon();
				ChestName = chest.getName() + " " + (chest.getEnchantLevel() > 0 ? "+" + chest.getEnchantLevel() : "");

				if(chest.getEnchantLevel() == Config.BBS_ENCHANT_MAX[1])
				{
					ChestButton = maxenchant;
					ChestPic = slotclose;
				}
				else
				{
					ChestButton = "<button action=\"bypass _bbsforge:enchant:item:" + Inventory.PAPERDOLL_CHEST + "\" value=\"" + new CustomMessage("common.enchant", player).toString() + "\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
					if(chest.getTemplate().isPvP())
						ChestPic = pvp;
					else
						ChestPic = picenchant;
				}
			}

			if(legs != null)
			{
				LegsIcon = legs.getTemplate().getIcon();
				LegsName = legs.getName() + " " + (legs.getEnchantLevel() > 0 ? "+" + legs.getEnchantLevel() : "");

				if(legs.getEnchantLevel() == Config.BBS_ENCHANT_MAX[1])
				{
					LegsButton = maxenchant;
					LegsPic = slotclose;
				}
				else
				{
					LegsButton = "<button action=\"bypass _bbsforge:enchant:item:" + Inventory.PAPERDOLL_LEGS + "\" value=\"" + new CustomMessage("common.enchant", player).toString() + "\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
					LegsPic = picenchant;
				}
			}

			if(gloves != null)
			{
				GlovesIcon = gloves.getTemplate().getIcon();
				GlovesName = gloves.getName() + " " + (gloves.getEnchantLevel() > 0 ? "+" + gloves.getEnchantLevel() : "");

				if(gloves.getEnchantLevel() == Config.BBS_ENCHANT_MAX[1])
				{
					GlovesButton = maxenchant;
					GlovesPic = slotclose;
				}
				else
				{
					GlovesButton = "<button action=\"bypass _bbsforge:enchant:item:" + Inventory.PAPERDOLL_GLOVES + "\" value=\"" + new CustomMessage("common.enchant", player).toString() + "\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
					GlovesPic = picenchant;
				}
			}

			if(feet != null)
			{
				FeetIcon = feet.getTemplate().getIcon();
				FeetName = feet.getName() + " " + (feet.getEnchantLevel() > 0 ? "+" + feet.getEnchantLevel() : "");

				if(feet.getEnchantLevel() == Config.BBS_ENCHANT_MAX[1])
				{
					FeetButton = maxenchant;
					FeetPic = slotclose;
				}
				else
				{
					FeetButton = "<button action=\"bypass _bbsforge:enchant:item:" + Inventory.PAPERDOLL_FEET + "\" value=\"" + new CustomMessage("common.enchant", player).toString() + "\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
					FeetPic = picenchant;
				}
			}

			if(rhand != null)
			{
				WeaponIcon = rhand.getTemplate().getIcon();
				WeaponName = rhand.getName() + " " + (rhand.getEnchantLevel() > 0 ? "+" + rhand.getEnchantLevel() : "");

				if(rhand.getEnchantLevel() == Config.BBS_ENCHANT_MAX[0])
				{
					WeaponButton = maxenchant;
					WeaponPic = slotclose;
				}
				else
				{
					WeaponButton = "<button action=\"bypass _bbsforge:enchant:item:" + Inventory.PAPERDOLL_RHAND + "\" value=\"" + new CustomMessage("common.enchant", player).toString() + "\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
					if(rhand.getTemplate().isPvP())
						WeaponPic = pvp;
					else
						WeaponPic = picenchant;
				}

				if(rhand.getTemplate().getItemType() == WeaponType.BIGBLUNT || rhand.getTemplate().getItemType() == WeaponType.BOW || rhand.getTemplate().getItemType() == WeaponType.DUALDAGGER || rhand.getTemplate().getItemType() == WeaponType.ANCIENTSWORD || rhand.getTemplate().getItemType() == WeaponType.CROSSBOW || rhand.getTemplate().getItemType() == WeaponType.BIGBLUNT || rhand.getTemplate().getItemType() == WeaponType.BIGSWORD || rhand.getTemplate().getItemType() == WeaponType.DUALFIST || rhand.getTemplate().getItemType() == WeaponType.DUAL || rhand.getTemplate().getItemType() == WeaponType.POLE || rhand.getTemplate().getItemType() == WeaponType.FIST)
				{
					ShieldButton = dot;
					ShieldIcon = rhand.getTemplate().getIcon();
					ShieldName = rhand.getName() + " " + (rhand.getEnchantLevel() > 0 ? "+" + rhand.getEnchantLevel() : "");
					ShieldPic = slotclose;
				}
			}

			if(lhand != null)
			{
				ShieldIcon = lhand.getTemplate().getIcon();
				ShieldName = lhand.getName() + " " + (lhand.getEnchantLevel() > 0 ? "+" + lhand.getEnchantLevel() : "");

				if(!lhand.getTemplate().isArrow())
				{
					if(lhand.getEnchantLevel() == Config.BBS_ENCHANT_MAX[1])
					{
						ShieldButton = maxenchant;
						ShieldPic = slotclose;
					}
					else
					{
						ShieldButton = "<button action=\"bypass _bbsforge:enchant:item:" + Inventory.PAPERDOLL_LHAND + "\" value=\"" + new CustomMessage("common.enchant", player).toString() + "\"  width=120 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
						ShieldPic = picenchant;
					}
				}
				else
				{
					ShieldButton = dot;
					ShieldPic = slotclose;
				}
			}

			if(lfinger != null)
			{
				LRingIcon = lfinger.getTemplate().getIcon();
				LRingName = lfinger.getName() + " " + (lfinger.getEnchantLevel() > 0 ? "+" + lfinger.getEnchantLevel() : "");

				if(lfinger.getEnchantLevel() == Config.BBS_ENCHANT_MAX[2])
				{
					LRingButton = maxenchant;
					LRingPic = slotclose;
				}
				else
				{
					LRingButton = "<button action=\"bypass _bbsforge:enchant:item:" + Inventory.PAPERDOLL_LFINGER + "\" value=\"" + new CustomMessage("common.enchant", player).toString() + "\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
					LRingPic = picenchant;
				}
			}

			if(rfinger != null)
			{
				RRingIcon = rfinger.getTemplate().getIcon();
				RRingName = rfinger.getName() + " " + (rfinger.getEnchantLevel() > 0 ? "+" + rfinger.getEnchantLevel() : "");

				if(rfinger.getEnchantLevel() == Config.BBS_ENCHANT_MAX[2])
				{
					RRingButton = maxenchant;
					RRingPic = slotclose;
				}
				else
				{
					RRingButton = "<button action=\"bypass _bbsforge:enchant:item:" + Inventory.PAPERDOLL_RFINGER + "\" value=\"" + new CustomMessage("common.enchant", player).toString() + "\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
					RRingPic = picenchant;
				}
			}

			if(neck != null)
			{
				NeckIcon = neck.getTemplate().getIcon();
				NeckName = neck.getName() + " " + (neck.getEnchantLevel() > 0 ? "+" + neck.getEnchantLevel() : "");

				if(neck.getEnchantLevel() == Config.BBS_ENCHANT_MAX[2])
				{
					NeckButton = maxenchant;
					NeckPic = slotclose;
				}
				else
				{
					NeckButton = "<button action=\"bypass _bbsforge:enchant:item:" + Inventory.PAPERDOLL_NECK + "\" value=\"" + new CustomMessage("common.enchant", player).toString() + "\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
					NeckPic = picenchant;
				}
			}

			if(lear != null)
			{
				LEarIcon = lear.getTemplate().getIcon();
				LEarName = lear.getName() + " " + (lear.getEnchantLevel() > 0 ? "+" + lear.getEnchantLevel() : "");

				if(lear.getEnchantLevel() == Config.BBS_ENCHANT_MAX[2])
				{
					LEarButton = maxenchant;
					LEarPic = slotclose;
				}
				else
				{
					LEarButton = "<button action=\"bypass _bbsforge:enchant:item:" + Inventory.PAPERDOLL_LEAR + "\" value=\"" + new CustomMessage("common.enchant", player).toString() + "\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
					LEarPic = picenchant;
				}
			}

			if(rear != null)
			{
				REarIcon = rear.getTemplate().getIcon();
				REarName = rear.getName() + " " + (rear.getEnchantLevel() > 0 ? "+" + rear.getEnchantLevel() : "");

				if(rear.getEnchantLevel() == Config.BBS_ENCHANT_MAX[2])
				{
					REarButton = maxenchant;
					REarPic = slotclose;
				}
				else
				{
					REarButton = "<button action=\"bypass _bbsforge:enchant:item:" + Inventory.PAPERDOLL_REAR + "\" value=\"" + new CustomMessage("common.enchant", player).toString() + "\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
					REarPic = picenchant;
				}
			}

			content = content.replace("<?content?>", page());

			content = content.replace("<?head_name?>", HeadName);
			content = content.replace("<?head_icon?>", HeadIcon);
			content = content.replace("<?head_pic?>", HeadPic);
			content = content.replace("<?head_button?>", HeadButton);

			content = content.replace("<?chest_name?>", ChestName);
			content = content.replace("<?chest_icon?>", ChestIcon);
			content = content.replace("<?chest_pic?>", ChestPic);
			content = content.replace("<?chest_button?>", ChestButton);

			content = content.replace("<?legs_name?>", LegsName);
			content = content.replace("<?legs_icon?>", LegsIcon);
			content = content.replace("<?legs_pic?>", LegsPic);
			content = content.replace("<?legs_button?>", LegsButton);

			content = content.replace("<?gloves_name?>", GlovesName);
			content = content.replace("<?gloves_icon?>", GlovesIcon);
			content = content.replace("<?gloves_pic?>", GlovesPic);
			content = content.replace("<?gloves_button?>", GlovesButton);

			content = content.replace("<?feet_name?>", FeetName);
			content = content.replace("<?feet_icon?>", FeetIcon);
			content = content.replace("<?feet_pic?>", FeetPic);
			content = content.replace("<?feet_button?>", FeetButton);

			content = content.replace("<?lear_name?>", LEarName);
			content = content.replace("<?lear_icon?>", LEarIcon);
			content = content.replace("<?lear_pic?>", LEarPic);
			content = content.replace("<?lear_button?>", LEarButton);

			content = content.replace("<?rear_name?>", REarName);
			content = content.replace("<?rear_icon?>", REarIcon);
			content = content.replace("<?rear_pic?>", REarPic);
			content = content.replace("<?rear_button?>", REarButton);

			content = content.replace("<?neck_name?>", NeckName);
			content = content.replace("<?neck_icon?>", NeckIcon);
			content = content.replace("<?neck_pic?>", NeckPic);
			content = content.replace("<?neck_button?>", NeckButton);

			content = content.replace("<?lring_name?>", LRingName);
			content = content.replace("<?lring_icon?>", LRingIcon);
			content = content.replace("<?lring_pic?>", LRingPic);
			content = content.replace("<?lring_button?>", LRingButton);

			content = content.replace("<?rring_name?>", RRingName);
			content = content.replace("<?rring_icon?>", RRingIcon);
			content = content.replace("<?rring_pic?>", RRingPic);
			content = content.replace("<?rring_button?>", RRingButton);

			content = content.replace("<?weapon_name?>", WeaponName);
			content = content.replace("<?weapon_icon?>", WeaponIcon);
			content = content.replace("<?weapon_pic?>", WeaponPic);
			content = content.replace("<?weapon_button?>", WeaponButton);

			content = content.replace("<?shield_name?>", ShieldName);
			content = content.replace("<?shield_icon?>", ShieldIcon);
			content = content.replace("<?shield_pic?>", ShieldPic);
			content = content.replace("<?shield_button?>", ShieldButton);
		}
		else if(command.startsWith("_bbsforge:enchant:item:"))
		{
			StringTokenizer st = new StringTokenizer(command, ":");
			st.nextToken();
			st.nextToken();
			st.nextToken();
			int item = Integer.parseInt(st.nextToken());
			String name = ItemHolder.getInstance().getTemplate(Config.BBS_ENCHANT_ITEM).getName();

			name.replace(" {PvP}", "");

			if(name.isEmpty())
				name = new CustomMessage("common.item.no.name", player).toString();

			if(item < 1 || item > 12)
				return;

			ItemInstance _item = player.getInventory().getPaperdollItem(item);
			if(_item == null)
			{
				player.sendMessage(new CustomMessage("communityboard.forge.item.null", player).toString());
				DifferentMethods.communityNextPage(player, "_bbsforge:enchant:list");
				return;
			}

			if(_item.getTemplate().isArrow())
			{
				player.sendMessage(new CustomMessage("communityboard.forge.item.arrow", player));
				DifferentMethods.communityNextPage(player, "_bbsforge:enchant:list");
				return;
			}

			content = HtmCache.getInstance().getNotNull("/scripts/services/community/" + Config.BBS_FOLDER + "/forge/enchant.htm", player);
			StringBuilder html = new StringBuilder("");

			html.append("<br><table border=0 cellspacing=0 cellpadding=0 width=240 height=330 background=\"l2ui_ct1.Windows_DF_TooltipBG\">");
			html.append("<tr>");
			html.append("<td width=230 align=center valign=top>");
			html.append("<table border=0 cellspacing=0 cellpadding=0 width=127 height=100 background=\"l2ui_ch3.refinegrade2_00\">");
			html.append("<tr>");
			html.append("<td width=92 height=22></td>");
			html.append("<td width=30 height=31></td>");
			html.append("<td width=100 height=22></td>");
			html.append("</tr>");
			html.append("<tr>");
			html.append("<td width=92 height=32 align=right valign=top></td>");
			html.append("<td width=32 height=32 align=center valign=top>");
			html.append("<img src=\"" + _item.getTemplate().getIcon() + "\" width=\"32\" height=\"32\">");
			html.append("</td>");
			html.append("<td width=110 height=32 align=left valign=top></td>");
			html.append("</tr>");
			html.append("</table>");
			html.append("<table border=0 cellspacing=0 cellpadding=0 width=230 height=20>");
			html.append("<tr>");
			html.append("<td width=230 align=center valign=top>");
			html.append("<font name=\"hs9\" color=\"LEVEL\">" + _item.getName() + "</font><font name=\"hs9\" color=\"LEVEL\">" + (_item.getEnchantLevel() <= 0 ? "</font>" : " +" + _item.getEnchantLevel()) + "</font>");
			html.append("</td>");
			html.append("</tr>");
			html.append("<tr>");
			html.append("<td width=230 align=center valign=top height=20>");
			html.append("<font name=\"hs9\">" + new CustomMessage("communityboard.forge.enchant.select", player) + "</font>");
			html.append("</td>");
			html.append("</tr>");

			int[] level = _item.getTemplate().isWeapon() ? Config.BBS_WEAPON_ENCHANT_LVL : _item.getTemplate().isArmor() ? Config.BBS_ARMOR_ENCHANT_LVL : Config.BBS_JEWELS_ENCHANT_LVL;
			for(int i = 0; i < level.length; i++)
			{
				if(_item.getEnchantLevel() < level[i])
				{
					html.append("<tr>");
					html.append("<td width=230 align=center valign=top height=35>");
					html.append("<button action=\"bypass _bbsforge:enchant:" + i * item + ":" + item + "\" value=\"+" + level[i] + " (" + (_item.getTemplate().isWeapon() ? Config.BBS_ENCHANT_PRICE_WEAPON[i] : _item.getTemplate().isArmor() ? Config.BBS_ENCHANT_PRICE_ARMOR[i] : Config.BBS_ENCHANT_PRICE_JEWELS[i]) + " " + name + ")\" width=200 height=31 back=\"L2UI_CT1.OlympiadWnd_DF_Fight3None_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Fight3None\">");
					html.append("</td>");
					html.append("</tr>");
				}
			}

			html.append("</table>");
			html.append("</td>");
			html.append("</tr>");
			html.append("</table>");

			content = content.replace("<?content?>", html.toString());
		}
		else if(command.startsWith("_bbsforge:enchant:"))
		{
			StringTokenizer st = new StringTokenizer(command, ":");
			st.nextToken();
			st.nextToken();
			int val = Integer.parseInt(st.nextToken());
			int item = Integer.parseInt(st.nextToken());

			int conversion = val / item;

			ItemInstance _item = player.getInventory().getPaperdollItem(item);

			int[] level = _item.getTemplate().isWeapon() ? Config.BBS_WEAPON_ENCHANT_LVL : _item.getTemplate().isArmor() ? Config.BBS_ARMOR_ENCHANT_LVL : Config.BBS_JEWELS_ENCHANT_LVL;
			int Value = level[conversion];

			int max = _item.getTemplate().isWeapon() ? Config.BBS_ENCHANT_MAX[0] : _item.getTemplate().isArmor() ? Config.BBS_ENCHANT_MAX[1] : Config.BBS_ENCHANT_MAX[2];
			if(Value > max)
			{
				DifferentMethods.clear(player);
				return;
			}

			if(_item.getTemplate().isArrow())
			{
				player.sendMessage(new CustomMessage("communityboard.forge.item.arrow", player));
				DifferentMethods.communityNextPage(player, "_bbsforge:enchant:list");
				return;
			}

			int price = _item.isWeapon() ? Config.BBS_ENCHANT_PRICE_WEAPON[conversion] : _item.getTemplate().isArmor() ? Config.BBS_ENCHANT_PRICE_ARMOR[conversion] : Config.BBS_ENCHANT_PRICE_JEWELS[conversion];

			if(_item != null)
			{
				if(DifferentMethods.getPay(player, Config.BBS_ENCHANT_ITEM, price, true))
				{
					player.getInventory().unEquipItem(_item);
					_item.setEnchantLevel(Value);
					player.getInventory().equipItem(_item);

					player.sendPacket(new InventoryUpdate().addModifiedItem(_item));
					player.broadcastUserInfo(true);

					player.sendMessage(new CustomMessage("communityboard.forge.enchant.success", player).addString(_item.getName()).addNumber(Value));
					Log.add("enchant item " + _item.getName() + " at +" + Value + "", "CommunityBoardForge", player);
				}
			}

			DifferentMethods.communityNextPage(player, "_bbsforge:enchant:list");
			return;
		}
		else if(command.equals("_bbsforge:attribute:list"))
		{
			content = HtmCache.getInstance().getNotNull("/scripts/services/community/" + Config.BBS_FOLDER + "/forge/attributelist.htm", player);

			String noicon = "icon.NOIMAGE";
			String slotclose = "L2UI_CT1.ItemWindow_DF_SlotBox_Disable";
			String dot = "<font color=\"FF0000\">...</font>";
			String immposible = new CustomMessage("communityboard.forge.attribute.immposible", player).toString();
			String maxenchant = new CustomMessage("communityboard.forge.attribute.maxenchant", player).toString();
			String heronot = new CustomMessage("communityboard.forge.attribute.heronot", player).toString();
			String picenchant = "l2ui_ch3.multisell_plusicon";
			String pvp = "icon.pvp_tab";

			String HeadButton = dot;
			String HeadIcon = noicon;
			String HeadPic = slotclose;
			String HeadName = new CustomMessage("common.item.not.clothed.head", player).toString();

			String ChestButton = dot;
			String ChestIcon = noicon;
			String ChestPic = slotclose;
			String ChestName = new CustomMessage("common.item.not.clothed.chest", player).toString();

			String LegsButton = dot;
			String LegsIcon = noicon;
			String LegsPic = slotclose;
			String LegsName = new CustomMessage("common.item.not.clothed.legs", player).toString();

			String FeetButton = dot;
			String FeetIcon = noicon;
			String FeetPic = slotclose;
			String FeetName = new CustomMessage("common.item.not.clothed.feet", player).toString();

			String GlovesButton = dot;
			String GlovesIcon = noicon;
			String GlovesPic = slotclose;
			String GlovesName = new CustomMessage("common.item.not.clothed.gloves", player).toString();

			String LEarButton = dot;
			String LEarIcon = noicon;
			String LEarPic = slotclose;
			String LEarName = new CustomMessage("common.item.not.clothed.lear", player).toString();

			String REarButton = dot;
			String REarIcon = noicon;
			String REarPic = slotclose;
			String REarName = new CustomMessage("common.item.not.clothed.rear", player).toString();

			String NeckButton = dot;
			String NeckIcon = noicon;
			String NeckPic = slotclose;
			String NeckName = new CustomMessage("common.item.not.clothed.neck", player).toString();

			String LRingButton = dot;
			String LRingIcon = noicon;
			String LRingPic = slotclose;
			String LRingName = new CustomMessage("common.item.not.clothed.lring", player).toString();

			String RRingButton = dot;
			String RRingIcon = noicon;
			String RRingPic = slotclose;
			String RRingName = new CustomMessage("common.item.not.clothed.rring", player).toString();

			String WeaponButton = dot;
			String WeaponIcon = noicon;
			String WeaponPic = slotclose;
			String WeaponName = new CustomMessage("common.item.not.clothed.weapon", player).toString();

			String ShieldButton = dot;
			String ShieldIcon = noicon;
			String ShieldPic = slotclose;
			String ShieldName = new CustomMessage("common.item.not.clothed.shield", player).toString();

			ItemInstance head = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_HEAD);
			ItemInstance chest = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
			ItemInstance legs = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS);
			ItemInstance gloves = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_GLOVES);
			ItemInstance feet = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_FEET);

			ItemInstance lhand = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
			ItemInstance rhand = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);

			ItemInstance lfinger = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LFINGER);
			ItemInstance rfinger = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RFINGER);
			ItemInstance neck = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_NECK);
			ItemInstance lear = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEAR);
			ItemInstance rear = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_REAR);

			if(head != null)
			{
				HeadIcon = head.getTemplate().getIcon();
				HeadName = head.getName() + " " + (head.getEnchantLevel() > 0 ? "+" + head.getEnchantLevel() : "");

				if(itemCheckGrade(player, head))
				{
					if(((head.getAttributes().getFire() | head.getAttributes().getWater()) & (head.getAttributes().getWind() | head.getAttributes().getEarth()) & (head.getAttributes().getHoly() | head.getAttributes().getUnholy())) >= Config.BBS_ENCHANT_ARMOR_ATTRIBUTE_MAX)
					{
						HeadButton = maxenchant;
						HeadPic = slotclose;
					}
					else
					{
						if(Config.BBS_ENCHANT_HEAD_ATTRIBUTE)
						{
							HeadButton = "<button action=\"bypass _bbsforge:attribute:item:" + Inventory.PAPERDOLL_HEAD + "\" value=\"" + new CustomMessage("common.enchant.attribute", player).toString() + "\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
							HeadPic = picenchant;
						}
						else
						{
							HeadButton = immposible;
							HeadPic = slotclose;
						}
					}
				}
				else
				{
					HeadButton = immposible;
					HeadPic = slotclose;
				}
			}

			if(chest != null)
			{
				ChestIcon = chest.getTemplate().getIcon();
				ChestName = chest.getName() + " " + (chest.getEnchantLevel() > 0 ? "+" + chest.getEnchantLevel() : "");

				if(itemCheckGrade(player, chest))
				{
					if(((chest.getAttributes().getFire() | chest.getAttributes().getWater()) & (chest.getAttributes().getWind() | chest.getAttributes().getEarth()) & (chest.getAttributes().getHoly() | chest.getAttributes().getUnholy())) >= Config.BBS_ENCHANT_ARMOR_ATTRIBUTE_MAX)
					{
						ChestButton = maxenchant;
						ChestPic = slotclose;
					}
					else
					{
						ChestButton = "<button action=\"bypass _bbsforge:attribute:item:" + Inventory.PAPERDOLL_CHEST + "\" value=\"" + new CustomMessage("common.enchant.attribute", player).toString() + "\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
						if(chest.getTemplate().isPvP())
							ChestPic = pvp;
						else
							ChestPic = picenchant;
					}
				}
				else
				{
					ChestButton = immposible;
					ChestPic = slotclose;
				}
			}

			if(legs != null)
			{
				LegsIcon = legs.getTemplate().getIcon();
				LegsName = legs.getName() + " " + (legs.getEnchantLevel() > 0 ? "+" + legs.getEnchantLevel() : "");

				if(itemCheckGrade(player, legs))
				{
					if(((legs.getAttributes().getFire() | legs.getAttributes().getWater()) & (legs.getAttributes().getWind() | legs.getAttributes().getEarth()) & (legs.getAttributes().getHoly() | legs.getAttributes().getUnholy())) >= Config.BBS_ENCHANT_ARMOR_ATTRIBUTE_MAX)
					{
						LegsButton = maxenchant;
						LegsPic = slotclose;
					}
					else
					{
						LegsButton = "<button action=\"bypass _bbsforge:attribute:item:" + Inventory.PAPERDOLL_LEGS + "\" value=\"" + new CustomMessage("common.enchant.attribute", player).toString() + "\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
						LegsPic = picenchant;
					}
				}
				else
				{
					LegsButton = immposible;
					LegsPic = slotclose;
				}
			}

			if(gloves != null)
			{
				GlovesIcon = gloves.getTemplate().getIcon();
				GlovesName = gloves.getName() + " " + (gloves.getEnchantLevel() > 0 ? "+" + gloves.getEnchantLevel() : "");

				if(itemCheckGrade(player, gloves))
				{
					if(((gloves.getAttributes().getFire() | gloves.getAttributes().getWater()) & (gloves.getAttributes().getWind() | gloves.getAttributes().getEarth()) & (gloves.getAttributes().getHoly() | gloves.getAttributes().getUnholy())) >= Config.BBS_ENCHANT_ARMOR_ATTRIBUTE_MAX)
					{
						GlovesButton = maxenchant;
						GlovesPic = slotclose;
					}
					else
					{
						GlovesButton = "<button action=\"bypass _bbsforge:attribute:item:" + Inventory.PAPERDOLL_GLOVES + "\" value=\"" + new CustomMessage("common.enchant.attribute", player).toString() + "\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
						GlovesPic = picenchant;
					}
				}
				else
				{
					GlovesButton = immposible;
					GlovesPic = slotclose;
				}
			}

			if(feet != null)
			{
				FeetIcon = feet.getTemplate().getIcon();
				FeetName = feet.getName() + " " + (feet.getEnchantLevel() > 0 ? "+" + feet.getEnchantLevel() : "");

				if(itemCheckGrade(player, feet))
				{
					if(((feet.getAttributes().getFire() | feet.getAttributes().getWater()) & (feet.getAttributes().getWind() | feet.getAttributes().getEarth()) & (feet.getAttributes().getHoly() | feet.getAttributes().getUnholy())) >= Config.BBS_ENCHANT_ARMOR_ATTRIBUTE_MAX)
					{
						FeetButton = maxenchant;
						FeetPic = slotclose;
					}
					else
					{
						FeetButton = "<button action=\"bypass _bbsforge:attribute:item:" + Inventory.PAPERDOLL_FEET + "\" value=\"" + new CustomMessage("common.enchant.attribute", player).toString() + "\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
						FeetPic = picenchant;
					}
				}
				else
				{
					FeetButton = immposible;
					FeetPic = slotclose;
				}
			}

			if(rhand != null)
			{
				WeaponIcon = rhand.getTemplate().getIcon();
				WeaponName = rhand.getName() + " " + (rhand.getEnchantLevel() > 0 ? "+" + rhand.getEnchantLevel() : "");

				if(rhand.getAttributes().getValue() >= Config.BBS_ENCHANT_WEAPON_ATTRIBUTE_MAX)
				{
					WeaponButton = maxenchant;
					WeaponPic = slotclose;
				}
				else
				{
					if(itemCheckGrade(player, rhand))
					{
						if(rhand.getTemplate().isHeroWeapon())
						{
							WeaponButton = heronot;
							WeaponPic = slotclose;
						}
						else
						{
							WeaponButton = "<button action=\"bypass _bbsforge:attribute:item:" + Inventory.PAPERDOLL_RHAND + "\" value=\"" + new CustomMessage("common.enchant.attribute", player).toString() + "\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
							if(rhand.getTemplate().isPvP())
								WeaponPic = pvp;
							else
								WeaponPic = picenchant;
						}
					}
					else
					{
						WeaponButton = immposible;
						WeaponPic = slotclose;
					}
				}

				if(rhand.getTemplate().getItemType() == WeaponType.BIGBLUNT || rhand.getTemplate().getItemType() == WeaponType.BOW || rhand.getTemplate().getItemType() == WeaponType.DUALDAGGER || rhand.getTemplate().getItemType() == WeaponType.ANCIENTSWORD || rhand.getTemplate().getItemType() == WeaponType.CROSSBOW || rhand.getTemplate().getItemType() == WeaponType.BIGBLUNT || rhand.getTemplate().getItemType() == WeaponType.BIGSWORD || rhand.getTemplate().getItemType() == WeaponType.DUALFIST || rhand.getTemplate().getItemType() == WeaponType.DUAL || rhand.getTemplate().getItemType() == WeaponType.POLE || rhand.getTemplate().getItemType() == WeaponType.FIST)
				{
					ShieldButton = dot;
					ShieldIcon = rhand.getTemplate().getIcon();
					ShieldName = rhand.getName() + " " + (rhand.getEnchantLevel() > 0 ? "+" + rhand.getEnchantLevel() : "");
					ShieldPic = slotclose;
				}
			}

			if(lhand != null)
			{
				ShieldIcon = lhand.getTemplate().getIcon();
				ShieldName = lhand.getName() + " " + (lhand.getEnchantLevel() > 0 ? "+" + lhand.getEnchantLevel() : "");

				ShieldButton = immposible;
				ShieldPic = slotclose;
			}

			if(lfinger != null)
			{
				LRingIcon = lfinger.getTemplate().getIcon();
				LRingName = lfinger.getName() + " " + (lfinger.getEnchantLevel() > 0 ? "+" + lfinger.getEnchantLevel() : "");

				LRingButton = immposible;
				LRingPic = slotclose;
			}

			if(rfinger != null)
			{
				RRingIcon = rfinger.getTemplate().getIcon();
				RRingName = rfinger.getName() + " " + (rfinger.getEnchantLevel() > 0 ? "+" + rfinger.getEnchantLevel() : "");

				RRingButton = immposible;
				RRingPic = slotclose;
			}

			if(neck != null)
			{
				NeckIcon = neck.getTemplate().getIcon();
				NeckName = neck.getName() + " " + (neck.getEnchantLevel() > 0 ? "+" + neck.getEnchantLevel() : "");

				NeckButton = immposible;
				NeckPic = slotclose;
			}

			if(lear != null)
			{
				LEarIcon = lear.getTemplate().getIcon();
				LEarName = lear.getName() + " " + (lear.getEnchantLevel() > 0 ? "+" + lear.getEnchantLevel() : "");
				LEarButton = immposible;
				LEarPic = slotclose;
			}

			if(rear != null)
			{
				REarIcon = rear.getTemplate().getIcon();
				REarName = rear.getName() + " " + (rear.getEnchantLevel() > 0 ? "+" + rear.getEnchantLevel() : "");
				REarButton = immposible;
				REarPic = slotclose;
			}

			content = content.replace("<?content?>", page());

			content = content.replace("<?head_name?>", HeadName);
			content = content.replace("<?head_icon?>", HeadIcon);
			content = content.replace("<?head_pic?>", HeadPic);
			content = content.replace("<?head_button?>", HeadButton);

			content = content.replace("<?chest_name?>", ChestName);
			content = content.replace("<?chest_icon?>", ChestIcon);
			content = content.replace("<?chest_pic?>", ChestPic);
			content = content.replace("<?chest_button?>", ChestButton);

			content = content.replace("<?legs_name?>", LegsName);
			content = content.replace("<?legs_icon?>", LegsIcon);
			content = content.replace("<?legs_pic?>", LegsPic);
			content = content.replace("<?legs_button?>", LegsButton);

			content = content.replace("<?gloves_name?>", GlovesName);
			content = content.replace("<?gloves_icon?>", GlovesIcon);
			content = content.replace("<?gloves_pic?>", GlovesPic);
			content = content.replace("<?gloves_button?>", GlovesButton);

			content = content.replace("<?feet_name?>", FeetName);
			content = content.replace("<?feet_icon?>", FeetIcon);
			content = content.replace("<?feet_pic?>", FeetPic);
			content = content.replace("<?feet_button?>", FeetButton);

			content = content.replace("<?lear_name?>", LEarName);
			content = content.replace("<?lear_icon?>", LEarIcon);
			content = content.replace("<?lear_pic?>", LEarPic);
			content = content.replace("<?lear_button?>", LEarButton);

			content = content.replace("<?rear_name?>", REarName);
			content = content.replace("<?rear_icon?>", REarIcon);
			content = content.replace("<?rear_pic?>", REarPic);
			content = content.replace("<?rear_button?>", REarButton);

			content = content.replace("<?neck_name?>", NeckName);
			content = content.replace("<?neck_icon?>", NeckIcon);
			content = content.replace("<?neck_pic?>", NeckPic);
			content = content.replace("<?neck_button?>", NeckButton);

			content = content.replace("<?lring_name?>", LRingName);
			content = content.replace("<?lring_icon?>", LRingIcon);
			content = content.replace("<?lring_pic?>", LRingPic);
			content = content.replace("<?lring_button?>", LRingButton);

			content = content.replace("<?rring_name?>", RRingName);
			content = content.replace("<?rring_icon?>", RRingIcon);
			content = content.replace("<?rring_pic?>", RRingPic);
			content = content.replace("<?rring_button?>", RRingButton);

			content = content.replace("<?weapon_name?>", WeaponName);
			content = content.replace("<?weapon_icon?>", WeaponIcon);
			content = content.replace("<?weapon_pic?>", WeaponPic);
			content = content.replace("<?weapon_button?>", WeaponButton);

			content = content.replace("<?shield_name?>", ShieldName);
			content = content.replace("<?shield_icon?>", ShieldIcon);
			content = content.replace("<?shield_pic?>", ShieldPic);
			content = content.replace("<?shield_button?>", ShieldButton);
		}
		else if(command.startsWith("_bbsforge:attribute:item:"))
		{
			StringTokenizer st = new StringTokenizer(command, ":");
			st.nextToken();
			st.nextToken();
			st.nextToken();
			int item = Integer.parseInt(st.nextToken());

			if(item < 1 || item > 12)
				return;

			ItemInstance _item = player.getInventory().getPaperdollItem(item);
			if(_item == null)
			{
				player.sendMessage(new CustomMessage("communityboard.forge.item.null", player).toString());
				DifferentMethods.communityNextPage(player, "_bbsforge:attribute:list");
				return;
			}

			if(!itemCheckGrade(player, _item))
			{
				player.sendMessage(new CustomMessage("communityboard.forge.grade.incorrect", player).toString());
				DifferentMethods.communityNextPage(player, "_bbsforge:attribute:list");
				return;
			}

			if(_item.isHeroWeapon())
			{
				player.sendMessage(new CustomMessage("communityboard.forge.item.hero", player).toString());
				DifferentMethods.communityNextPage(player, "_bbsforge:attribute:list");
				return;
			}

			content = HtmCache.getInstance().getNotNull("/scripts/services/community/" + Config.BBS_FOLDER + "/forge/attribute.htm", player);
			StringBuilder html = new StringBuilder("");

			html.append("<br><table border=0 cellspacing=0 cellpadding=0 width=240 height=330 background=\"l2ui_ct1.Windows_DF_TooltipBG\">");
			html.append("<tr>");
			html.append("<td width=230 align=center valign=top>");
			html.append("<table border=0 cellspacing=0 cellpadding=0 width=127 height=100 background=\"l2ui_ch3.refinegrade2_00\">");
			html.append("<tr>");
			html.append("<td width=92 height=22></td>");
			html.append("<td width=30 height=31></td>");
			html.append("<td width=100 height=22></td>");
			html.append("</tr>");
			html.append("<tr>");
			html.append("<td width=92 height=32 align=right valign=top></td>");
			html.append("<td width=32 height=32 align=center valign=top>");
			html.append("<img src=\"" + _item.getTemplate().getIcon() + "\" width=\"32\" height=\"32\">");
			html.append("</td>");
			html.append("<td width=110 height=32 align=left valign=top></td>");
			html.append("</tr>");
			html.append("</table>");
			html.append("<table border=0 cellspacing=0 cellpadding=0 width=230 height=20>");
			html.append("<tr>");
			html.append("<td width=230 align=center valign=top>");
			html.append("<font name=\"hs9\" color=\"LEVEL\">" + _item.getName() + "</font><font name=\"hs9\" color=\"LEVEL\">" + (_item.getEnchantLevel() <= 0 ? "</font>" : " +" + _item.getEnchantLevel()) + "</font>");
			html.append("</td>");
			html.append("</tr>");
			html.append("<tr>");
			html.append("<td width=230 align=center valign=top height=30>");
			html.append("<font name=\"hs9\">" + new CustomMessage("communityboard.forge.attribute.select", player) + "</font>");
			html.append("</td>");
			html.append("</tr>");

			String slotclose = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
			String buttonFire = "<button action=\"bypass _bbsforge:attribute:element:0:" + item + "\" width=34 height=34 back=\"L2UI_CT1.ItemWindow_DF_Frame_Down\" fore=\"L2UI_CT1.ItemWindow_DF_Frame\"/>";
			String buttonWater = "<button action=\"bypass _bbsforge:attribute:element:1:" + item + "\" width=34 height=34 back=\"L2UI_CT1.ItemWindow_DF_Frame_Down\" fore=\"L2UI_CT1.ItemWindow_DF_Frame\"/>";
			String buttonWind = "<button action=\"bypass _bbsforge:attribute:element:2:" + item + "\" width=34 height=34 back=\"L2UI_CT1.ItemWindow_DF_Frame_Down\" fore=\"L2UI_CT1.ItemWindow_DF_Frame\"/>";
			String buttonEarth = "<button action=\"bypass _bbsforge:attribute:element:3:" + item + "\" width=34 height=34 back=\"L2UI_CT1.ItemWindow_DF_Frame_Down\" fore=\"L2UI_CT1.ItemWindow_DF_Frame\"/>";
			String buttonHoly = "<button action=\"bypass _bbsforge:attribute:element:4:" + item + "\" width=34 height=34 back=\"L2UI_CT1.ItemWindow_DF_Frame_Down\" fore=\"L2UI_CT1.ItemWindow_DF_Frame\"/>";
			String buttonUnholy = "<button action=\"bypass _bbsforge:attribute:element:5:" + item + "\" width=34 height=34 back=\"L2UI_CT1.ItemWindow_DF_Frame_Down\" fore=\"L2UI_CT1.ItemWindow_DF_Frame\"/>";

			if(_item.isWeapon())
			{
				if(_item.getAttributes().getFire() > 0)
				{
					buttonWater = slotclose;
					buttonWind = slotclose;
					buttonEarth = slotclose;
					buttonHoly = slotclose;
					buttonUnholy = slotclose;
				}
				if(_item.getAttributes().getWater() > 0)
				{
					buttonFire = slotclose;
					buttonWind = slotclose;
					buttonEarth = slotclose;
					buttonHoly = slotclose;
					buttonUnholy = slotclose;
				}
				if(_item.getAttributes().getWind() > 0)
				{
					buttonWater = slotclose;
					buttonFire = slotclose;
					buttonEarth = slotclose;
					buttonHoly = slotclose;
					buttonUnholy = slotclose;
				}
				if(_item.getAttributes().getEarth() > 0)
				{
					buttonWater = slotclose;
					buttonWind = slotclose;
					buttonFire = slotclose;
					buttonHoly = slotclose;
					buttonUnholy = slotclose;
				}
				if(_item.getAttributes().getHoly() > 0)
				{
					buttonWater = slotclose;
					buttonWind = slotclose;
					buttonEarth = slotclose;
					buttonFire = slotclose;
					buttonUnholy = slotclose;
				}
				if(_item.getAttributes().getUnholy() > 0)
				{
					buttonWater = slotclose;
					buttonWind = slotclose;
					buttonEarth = slotclose;
					buttonHoly = slotclose;
					buttonFire = slotclose;
				}
			}

			if(_item.isArmor())
			{
				if(_item.getAttributes().getFire() > 0)
				{
					if(_item.getAttributes().getFire() >= Config.BBS_ENCHANT_ARMOR_ATTRIBUTE_MAX)
					{
						buttonFire = slotclose;
					}
					buttonWater = slotclose;

				}
				if(_item.getAttributes().getWater() > 0)
				{
					if(_item.getAttributes().getWater() >= Config.BBS_ENCHANT_ARMOR_ATTRIBUTE_MAX)
					{
						buttonWater = slotclose;
					}
					buttonFire = slotclose;
				}
				if(_item.getAttributes().getWind() > 0)
				{
					if(_item.getAttributes().getWind() >= Config.BBS_ENCHANT_ARMOR_ATTRIBUTE_MAX)
					{
						buttonWind = slotclose;
					}
					buttonEarth = slotclose;
				}
				if(_item.getAttributes().getEarth() > 0)
				{
					if(_item.getAttributes().getEarth() >= Config.BBS_ENCHANT_ARMOR_ATTRIBUTE_MAX)
					{
						buttonEarth = slotclose;
					}
					buttonWind = slotclose;
				}
				if(_item.getAttributes().getHoly() > 0)
				{
					if(_item.getAttributes().getHoly() >= Config.BBS_ENCHANT_ARMOR_ATTRIBUTE_MAX)
					{
						buttonHoly = slotclose;
					}
					buttonUnholy = slotclose;
				}
				if(_item.getAttributes().getUnholy() > 0)
				{
					if(_item.getAttributes().getUnholy() >= Config.BBS_ENCHANT_ARMOR_ATTRIBUTE_MAX)
					{
						buttonUnholy = slotclose;
					}
					buttonHoly = slotclose;
				}
			}

			html.append("<tr>");
			html.append("	<td width=250 align=center valign=top height=20>");
			html.append("<table border=0 cellspacing=0 cellpadding=0 width=30 height=20>");
			html.append("<tr>");
			html.append("<td width=32 height=45 align=center valign=top>");
			html.append("<table border=0 cellspacing=0 cellpadding=0 width=32 height=32 background=\"icon.etc_fire_crystal_i00\">");
			html.append("<tr>");
			html.append("<td width=32 align=center valign=top>");
			html.append(buttonFire);
			html.append("</td>");
			html.append("</tr>");
			html.append("</table>");
			html.append("</td>");
			html.append("<td width=32 height=10></td>");
			html.append("<td width=32 height=45 align=center valign=top>");
			html.append("<table border=0 cellspacing=0 cellpadding=0 width=32 height=32 background=\"icon.etc_water_crystal_i00\">");
			html.append("<tr>");
			html.append("<td width=32 align=center valign=top>");
			html.append(buttonWater);
			html.append("</td>");
			html.append("</tr>");
			html.append("</table>");
			html.append("</td>");
			html.append("</tr>");
			html.append("<tr>");
			html.append("<td width=32 height=45 align=center valign=top>");
			html.append("<table border=0 cellspacing=0 cellpadding=0 width=32 height=32 background=\"icon.etc_earth_crystal_i00\">");
			html.append("<tr>");
			html.append("<td width=32 align=center valign=top>");
			html.append(buttonEarth);
			html.append("</td>");
			html.append("</tr>");
			html.append("</table>");
			html.append("</td>");
			html.append("<td width=32 height=10></td>");
			html.append("<td width=32 height=45 align=center valign=top>");
			html.append("<table border=0 cellspacing=0 cellpadding=0 width=32 height=32 background=\"icon.etc_wind_crystal_i00\">");
			html.append("<tr>");
			html.append("<td width=32 align=center valign=top>");
			html.append(buttonWind);
			html.append("</td>");
			html.append("</tr>");
			html.append("</table>");
			html.append("</td>");
			html.append("</tr>");
			html.append("<tr>");
			html.append("<td width=32 height=45 align=center valign=top>");
			html.append("<table border=0 cellspacing=0 cellpadding=0 width=32 height=32 background=\"icon.etc_holy_crystal_i00\">");
			html.append("<tr>");
			html.append("<td width=32 align=center valign=top>");
			html.append(buttonHoly);
			html.append("</td>");
			html.append("</tr>");
			html.append("</table>");
			html.append("</td>");
			html.append("<td width=32 height=10></td>");
			html.append("<td width=32 height=45 align=center valign=top>");
			html.append("<table border=0 cellspacing=0 cellpadding=0 width=32 height=32 background=\"icon.etc_unholy_crystal_i00\">");
			html.append("<tr>");
			html.append("<td width=32 align=center valign=top>");
			html.append(buttonUnholy);
			html.append("</td>");
			html.append("</tr>");
			html.append("</table>");
			html.append("</td>");
			html.append("</tr>");
			html.append("</table>");
			html.append("</td>");
			html.append("</tr>");

			html.append("</table>");
			html.append("</td>");
			html.append("</tr>");
			html.append("</table>");

			content = content.replace("<?content?>", html.toString());
		}
		else if(command.startsWith("_bbsforge:attribute:element:"))
		{
			StringTokenizer st = new StringTokenizer(command, ":");
			st.nextToken();
			st.nextToken();
			st.nextToken();
			int element = Integer.parseInt(st.nextToken());
			String elementName = "";
			if(element == 0)
				elementName = new CustomMessage("common.element.0", player).toString();
			else if(element == 1)
				elementName = new CustomMessage("common.element.1", player).toString();
			else if(element == 2)
				elementName = new CustomMessage("common.element.2", player).toString();
			else if(element == 3)
				elementName = new CustomMessage("common.element.3", player).toString();
			else if(element == 4)
				elementName = new CustomMessage("common.element.4", player).toString();
			else if(element == 5)
				elementName = new CustomMessage("common.element.5", player).toString();

			int item = Integer.parseInt(st.nextToken());

			String name = ItemHolder.getInstance().getTemplate(Config.BBS_ENCHANT_ITEM).getName();

			if(name.isEmpty())
				name = new CustomMessage("common.item.no.name", player).toString();

			name.replace(" {PvP}", "");

			ItemInstance _item = player.getInventory().getPaperdollItem(item);

			if(_item == null)
			{
				player.sendMessage(new CustomMessage("communityboard.forge.item.null", player).toString());
				DifferentMethods.communityNextPage(player, "_bbsforge:attribute:list");
				return;
			}

			if(!itemCheckGrade(player, _item))
			{
				player.sendMessage(new CustomMessage("communityboard.forge.grade.incorrect", player).toString());
				DifferentMethods.communityNextPage(player, "_bbsforge:attribute:list");
				return;
			}

			if(_item.isHeroWeapon())
			{
				player.sendMessage(new CustomMessage("communityboard.forge.item.hero", player).toString());
				DifferentMethods.communityNextPage(player, "_bbsforge:attribute:list");
				return;
			}

			content = HtmCache.getInstance().getNotNull("/scripts/services/community/" + Config.BBS_FOLDER + "/forge/attribute.htm", player);
			StringBuilder html = new StringBuilder("");

			html.append("<br><table border=0 cellspacing=0 cellpadding=0 width=240 height=330 background=\"l2ui_ct1.Windows_DF_TooltipBG\">");
			html.append("<tr>");
			html.append("<td width=230 align=center valign=top>");
			html.append("<table border=0 cellspacing=0 cellpadding=0 width=127 height=100 background=\"l2ui_ch3.refinegrade2_00\">");
			html.append("<tr>");
			html.append("<td width=92 height=22></td>");
			html.append("<td width=30 height=31></td>");
			html.append("<td width=100 height=22></td>");
			html.append("</tr>");
			html.append("<tr>");
			html.append("<td width=92 height=32 align=right valign=top></td>");
			html.append("<td width=32 height=32 align=center valign=top>");
			html.append("<img src=\"" + _item.getTemplate().getIcon() + "\" width=\"32\" height=\"32\">");
			html.append("</td>");
			html.append("<td width=110 height=32 align=left valign=top></td>");
			html.append("</tr>");
			html.append("</table>");
			html.append("<table border=0 cellspacing=0 cellpadding=0 width=230 height=20>");
			html.append("<tr>");
			html.append("<td width=230 align=center valign=top>");
			html.append("<font name=\"hs9\" color=\"LEVEL\">" + _item.getName() + "</font><font name=\"hs9\" color=\"LEVEL\">" + (_item.getEnchantLevel() <= 0 ? "</font>" : " +" + _item.getEnchantLevel()) + "</font>");
			html.append("</td>");
			html.append("</tr>");
			html.append("<tr>");
			html.append("<td width=230 align=center valign=top height=30>");
			html.append("<font name=\"hs9\">" + new CustomMessage("communityboard.forge.attribute.selected", player).addString(elementName) + "</font>");
			html.append("</td>");
			html.append("</tr>");

			for(int i = 0; i < (_item.isWeapon() ? Config.BBS_ENCHANT_ATRIBUTE_LVL_WEAPON.length : Config.BBS_ENCHANT_ATRIBUTE_LVL_ARMOR.length); i++)
			{
				if(_item.getAttributeElementValue(Element.getElementById(element), false) < (_item.isWeapon() ? Config.BBS_ENCHANT_ATRIBUTE_LVL_WEAPON[i] : Config.BBS_ENCHANT_ATRIBUTE_LVL_ARMOR[i]))
				{
					html.append("<tr>");
					html.append("<td width=230 align=center valign=top height=35>");
					html.append("<button action=\"bypass _bbsforge:attribute:" + i * item + ":" + item + ":" + element + "\" value=\"+" + (_item.isWeapon() ? Config.BBS_ENCHANT_ATRIBUTE_LVL_WEAPON[i] : Config.BBS_ENCHANT_ATRIBUTE_LVL_ARMOR[i]) + " (" + (_item.isWeapon() ? Config.BBS_ENCHANT_ATRIBUTE_PRICE_WEAPON[i] : Config.BBS_ENCHANT_ATRIBUTE_PRICE_ARMOR[i]) + " " + name + ")\" width=200 height=31 back=\"L2UI_CT1.OlympiadWnd_DF_Fight3None_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Fight3None\">");
					html.append("</td>");
					html.append("</tr>");
				}
			}

			html.append("</table>");
			html.append("</td>");
			html.append("</tr>");
			html.append("</table>");

			content = content.replace("<?content?>", html.toString());
		}
		else if(command.startsWith("_bbsforge:attribute:"))
		{
			StringTokenizer st = new StringTokenizer(command, ":");
			st.nextToken();
			st.nextToken();
			int val = Integer.parseInt(st.nextToken());
			int item = Integer.parseInt(st.nextToken());
			int att = Integer.parseInt(st.nextToken());

			ItemInstance _item = player.getInventory().getPaperdollItem(item);

			if(_item == null)
			{
				player.sendMessage(new CustomMessage("communityboard.forge.item.null", player).toString());
				DifferentMethods.communityNextPage(player, "_bbsforge:attribute:list");
				return;
			}

			if(!itemCheckGrade(player, _item))
			{
				player.sendMessage(new CustomMessage("communityboard.forge.grade.incorrect", player).toString());
				DifferentMethods.communityNextPage(player, "_bbsforge:attribute:list");
				return;
			}

			if(_item.isHeroWeapon())
			{
				player.sendMessage(new CustomMessage("communityboard.forge.item.hero", player).toString());
				DifferentMethods.communityNextPage(player, "_bbsforge:attribute:list");
				return;
			}

			if(_item.isArmor() && !canEnchantArmorAttribute(att, _item))
			{
				player.sendMessage(new CustomMessage("communityboard.forge.attribute.terms.incorrect", player).toString());
				DifferentMethods.communityNextPage(player, "_bbsforge:attribute:list");
				return;
			}

			int conversion = val / item;

			int Value = _item.isWeapon() ? Config.BBS_ENCHANT_ATRIBUTE_LVL_WEAPON[conversion] : Config.BBS_ENCHANT_ATRIBUTE_LVL_ARMOR[conversion];

			if(Value > (_item.isWeapon() ? Config.BBS_ENCHANT_WEAPON_ATTRIBUTE_MAX : Config.BBS_ENCHANT_ARMOR_ATTRIBUTE_MAX))
			{
				DifferentMethods.clear(player);
				return;
			}

			int price = _item.isWeapon() ? Config.BBS_ENCHANT_ATRIBUTE_PRICE_WEAPON[conversion] : Config.BBS_ENCHANT_ATRIBUTE_PRICE_ARMOR[conversion];

			if(DifferentMethods.getPay(player, Config.BBS_ENCHANT_ITEM, price, true))
			{
				player.getInventory().unEquipItem(_item);

				_item.setAttributeElement(Element.getElementById(att), Value);

				player.getInventory().equipItem(_item);

				player.sendPacket(new InventoryUpdate().addModifiedItem(_item));
				player.broadcastUserInfo(true);

				String elementName = "";
				if(att == 0)
					elementName = new CustomMessage("common.element.0", player).toString();
				else if(att == 1)
					elementName = new CustomMessage("common.element.1", player).toString();
				else if(att == 2)
					elementName = new CustomMessage("common.element.2", player).toString();
				else if(att == 3)
					elementName = new CustomMessage("common.element.3", player).toString();
				else if(att == 4)
					elementName = new CustomMessage("common.element.4", player).toString();
				else if(att == 5)
					elementName = new CustomMessage("common.element.5", player).toString();

				player.sendMessage(new CustomMessage("communityboard.forge.enchant.attribute.success", player).addString(_item.getName()).addString(elementName).addNumber(Value));
				Log.add("enchant item:" + _item.getName() + " val: " + Value + " AtributType:" + att, "CommunityBoardForge", player);
			}

			DifferentMethods.communityNextPage(player, "_bbsforge:attribute:list");
			return;
		}
		ShowBoard.separateAndSend("<html><body><br><br><center>" + new CustomMessage("communityboard.notdone", player).addString(command) + "</center><br><br></body></html>", player);

		ShowBoard.separateAndSend(content, player);
	}

	private String page()
	{
		StringBuilder html = new StringBuilder("");

		html.append("<center><table border=0 cellpadding=3 cellspacing=3 width=500 height=425><tr><td><center><table><tr><td height=62><table border=0 cellspacing=1 cellpadding=1 height=55 width=220 background=\"l2ui_ct1.EditBox_DF_bg\"><tr><td width=50 align=left valign=top><table border=0 cellspacing=1 cellpadding=1 width=50 height=50 background=\"l2ui_ct1.Windows_DF_TooltipBG\"><tr><td width=70 height=9></td></tr><tr><td width=70 align=center valign=top><table border=0 cellspacing=0 cellpadding=0 width=30 height=32 background=\"<?head_icon?>\"><tr><td width=32 align=center valign=top><img src=\"<?head_pic?>\" width=\"32\" height=\"32\"></td></tr></table></td></tr></table></td><td width=220 align=center valign=top><table border=0 cellspacing=1 cellpadding=1 width=220 height=50><tr><td width=220 align=center valign=top><font name=\"hs9\" color=\"LEVEL\"><?head_name?></font></td></tr><tr><td width=220 align=center valign=top><?head_button?></td></tr></table></td></tr></table></td><td height=62><table border=0 cellspacing=1 cellpadding=1 height=55 width=220 background=\"l2ui_ct1.EditBox_DF_bg\"><tr><td width=50 align=left valign=top><table border=0 cellspacing=1 cellpadding=1 width=50 height=50 background=\"l2ui_ct1.Windows_DF_TooltipBG\"><tr><td width=70 height=9></td></tr><tr><td width=70 align=center valign=top><table border=0 cellspacing=0 cellpadding=0 width=30 height=32 background=\"<?lear_icon?>\"><tr><td width=32 align=center valign=top><img src=\"<?lear_pic?>\" width=\"32\" height=\"32\"></td></tr></table></td> </tr></table></td><td width=220 align=center valign=top><table border=0 cellspacing=1 cellpadding=1 width=220 height=50><tr><td width=220 align=center valign=top><font name=\"hs9\" color=\"LEVEL\"><?lear_name?></font></td></tr><tr><td width=220 align=center valign=top><?lear_button?></td></tr></table></td></tr></table></td></tr><tr><td height=62><table border=0 cellspacing=1 cellpadding=1 height=55 width=220 background=\"l2ui_ct1.EditBox_DF_bg\"><tr><td width=50 align=left valign=top><table border=0 cellspacing=1 cellpadding=1 width=50 height=50 background=\"l2ui_ct1.Windows_DF_TooltipBG\"><tr><td width=70 height=9></td></tr><tr><td width=70 align=center valign=top><table border=0 cellspacing=0 cellpadding=0 width=30 height=32 background=\"<?chest_icon?>\"><tr><td width=32 align=center valign=top><img src=\"<?chest_pic?>\" width=\"32\" height=\"32\"></td></tr></table></td></tr></table></td><td width=220 align=center valign=top><table border=0 cellspacing=1 cellpadding=1 width=220 height=50><tr><td width=220 align=center valign=top><font name=\"hs9\" color=\"LEVEL\"><?chest_name?></font></td></tr><tr><td width=220 align=center valign=top><?chest_button?></td></tr></table></td></tr></table></td><td height=62><table border=0 cellspacing=1 cellpadding=1 height=55 width=220 background=\"l2ui_ct1.EditBox_DF_bg\"><tr><td width=50 align=left valign=top><table border=0 cellspacing=1 cellpadding=1 width=50 height=50 background=\"l2ui_ct1.Windows_DF_TooltipBG\"><tr><td width=70 height=9></td></tr><tr><td width=70 align=center valign=top><table border=0 cellspacing=0 cellpadding=0 width=30 height=32 background=\"<?rear_icon?>\"><tr><td width=32 align=center valign=top><img src=\"<?rear_pic?>\" width=\"32\" height=\"32\"></td></tr></table></td></tr></table></td><td width=220 align=center valign=top><table border=0 cellspacing=1 cellpadding=1 width=220 height=50><tr><td width=220 align=center valign=top><font name=\"hs9\" color=\"LEVEL\"><?rear_name?></font></td></tr><tr><td width=220 align=center valign=top><?rear_button?></td></tr></table></td></tr></table></td></tr><tr><td height=62><table border=0 cellspacing=1 cellpadding=1 height=55 width=220 background=\"l2ui_ct1.EditBox_DF_bg\"><tr><td width=50 align=left valign=top><table border=0 cellspacing=1 cellpadding=1 width=50 height=50 background=\"l2ui_ct1.Windows_DF_TooltipBG\"><tr><td width=70 height=9></td></tr><tr><td width=70 align=center valign=top><table border=0 cellspacing=0 cellpadding=0 width=30 height=32 background=\"<?legs_icon?>\"><tr><td width=32 align=center valign=top><img src=\"<?legs_pic?>\" width=\"32\" height=\"32\"></td></tr></table></td></tr></table></td><td width=220 align=center valign=top><table border=0 cellspacing=1 cellpadding=1 width=220 height=50><tr><td width=220 align=center valign=top><font name=\"hs9\" color=\"LEVEL\"><?legs_name?></font></td></tr><tr><td width=220 align=center valign=top><?legs_button?></td></tr></table></td></tr></table></td><td height=62><table border=0 cellspacing=1 cellpadding=1 height=55 width=220 background=\"l2ui_ct1.EditBox_DF_bg\"><tr><td width=50 align=left valign=top><table border=0 cellspacing=1 cellpadding=1 width=50 height=50 background=\"l2ui_ct1.Windows_DF_TooltipBG\"><tr><td width=70 height=9></td></tr><tr><td width=70 align=center valign=top><table border=0 cellspacing=0 cellpadding=0 width=30 height=32 background=\"<?neck_icon?>\"><tr><td width=32 align=center valign=top><img src=\"<?neck_pic?>\" width=\"32\" height=\"32\"></td></tr></table></td></tr></table></td><td width=220 align=center valign=top><table border=0 cellspacing=1 cellpadding=1 width=220 height=50><tr><td width=220 align=center valign=top><font name=\"hs9\" color=\"LEVEL\"><?neck_name?></font></td></tr><tr><td width=220 align=center valign=top><?neck_button?></td></tr></table></td></tr></table></td></tr><tr><td height=62><table border=0 cellspacing=1 cellpadding=1 height=55 width=220 background=\"l2ui_ct1.EditBox_DF_bg\"><tr><td width=50 align=left valign=top><table border=0 cellspacing=1 cellpadding=1 width=50 height=50 background=\"l2ui_ct1.Windows_DF_TooltipBG\"><tr><td width=70 height=9></td></tr><tr><td width=70 align=center valign=top><table border=0 cellspacing=0 cellpadding=0 width=30 height=32 background=\"<?gloves_icon?>\"><tr><td width=32 align=center valign=top><img src=\"<?gloves_pic?>\" width=\"32\" height=\"32\"></td></tr></table></td></tr></table></td><td width=220 align=center valign=top><table border=0 cellspacing=1 cellpadding=1 width=220 height=50><tr><td width=220 align=center valign=top><font name=\"hs9\" color=\"LEVEL\"><?gloves_name?></font></td></tr><tr><td width=220 align=center valign=top><?gloves_button?></td></tr></table></td></tr></table></td><td height=62><table border=0 cellspacing=1 cellpadding=1 height=55 width=220 background=\"l2ui_ct1.EditBox_DF_bg\"><tr><td width=50 align=left valign=top><table border=0 cellspacing=1 cellpadding=1 width=50 height=50 background=\"l2ui_ct1.Windows_DF_TooltipBG\"><tr><td width=70 height=9></td></tr><tr><td width=70 align=center valign=top><table border=0 cellspacing=0 cellpadding=0 width=30 height=32 background=\"<?lring_icon?>\"><tr><td width=32 align=center valign=top><img src=\"<?lring_pic?>\" width=\"32\" height=\"32\"></td></tr></table></td></tr></table></td><td width=220 align=center valign=top><table border=0 cellspacing=1 cellpadding=1 width=220 height=50><tr><td width=220 align=center valign=top><font name=\"hs9\" color=\"LEVEL\"><?lring_name?></font></td></tr><tr><td width=220 align=center valign=top><?lring_button?></td></tr></table></td></tr></table></td></tr><tr><td height=62><table border=0 cellspacing=1 cellpadding=1 height=55 width=220 background=\"l2ui_ct1.EditBox_DF_bg\"><tr><td width=50 align=left valign=top><table border=0 cellspacing=1 cellpadding=1 width=50 height=50 background=\"l2ui_ct1.Windows_DF_TooltipBG\"><tr><td width=70 height=9></td></tr><tr><td width=70 align=center valign=top><table border=0 cellspacing=0 cellpadding=0 width=30 height=32 background=\"<?feet_icon?>\"><tr><td width=32 align=center valign=top><img src=\"<?feet_pic?>\" width=\"32\" height=\"32\"></td></tr></table></td></tr></table></td><td width=220 align=center valign=top><table border=0 cellspacing=1 cellpadding=1 width=220 height=50><tr><td width=220 align=center valign=top><font name=\"hs9\" color=\"LEVEL\"><?feet_name?></font></td></tr><tr><td width=220 align=center valign=top><?feet_button?></td></tr></table></td></tr></table></td><td height=62><table border=0 cellspacing=1 cellpadding=1 height=55 width=220 background=\"l2ui_ct1.EditBox_DF_bg\"><tr><td width=50 align=left valign=top><table border=0 cellspacing=1 cellpadding=1 width=50 height=50 background=\"l2ui_ct1.Windows_DF_TooltipBG\"><tr><td width=70 height=9></td></tr><tr><td width=70 align=center valign=top><table border=0 cellspacing=0 cellpadding=0 width=30 height=32 background=\"<?rring_icon?>\"><tr><td width=32 align=center valign=top><img src=\"<?rring_pic?>\" width=\"32\" height=\"32\"></td></tr></table></td></tr></table></td><td width=220 align=center valign=top><table border=0 cellspacing=1 cellpadding=1 width=220 height=50><tr><td width=220 align=center valign=top><font name=\"hs9\" color=\"LEVEL\"><?rring_name?></font></td></tr><tr><td width=220 align=center valign=top><?rring_button?></td></tr></table></td></tr></table></td></tr><tr><td height=62><table border=0 cellspacing=1 cellpadding=1 height=55 width=220 background=\"l2ui_ct1.EditBox_DF_bg\"><tr><td width=50 align=left valign=top><table border=0 cellspacing=1 cellpadding=1 width=50 height=50 background=\"l2ui_ct1.Windows_DF_TooltipBG\"><tr><td width=70 height=9></td></tr><tr><td width=70 align=center valign=top><table border=0 cellspacing=0 cellpadding=0 width=30 height=32 background=\"<?weapon_icon?>\"><tr><td width=32 align=center valign=top><img src=\"<?weapon_pic?>\" width=\"32\" height=\"32\"></td></tr></table></td></tr></table></td><td width=220 align=center valign=top><table border=0 cellspacing=1 cellpadding=1 width=220 height=50><tr><td width=220 align=center valign=top><font name=\"hs9\" color=\"LEVEL\"><?weapon_name?></font></td></tr><tr><td width=220 align=center valign=top><?weapon_button?></td></tr></table></td></tr></table></td><td height=62><table border=0 cellspacing=1 cellpadding=1 height=55 width=220 background=\"l2ui_ct1.EditBox_DF_bg\"><tr><td width=50 align=left valign=top><table border=0 cellspacing=1 cellpadding=1 width=50 height=50 background=\"l2ui_ct1.Windows_DF_TooltipBG\"><tr><td width=70 height=9></td></tr><tr><td width=70 align=center valign=top><table border=0 cellspacing=0 cellpadding=0 width=30 height=32 background=\"<?shield_icon?>\"><tr><td width=32 align=center valign=top><img src=\"<?shield_pic?>\" width=\"32\" height=\"32\"></td></tr></table></td></tr></table></td><td width=220 align=center valign=top><table border=0 cellspacing=1 cellpadding=1 width=220 height=50><tr><td width=220 align=center valign=top><font name=\"hs9\" color=\"LEVEL\"><?shield_name?></font></td></tr><tr><td width=220 align=center valign=top><?shield_button?></td></tr></table></td></tr></table></td></tr></table></center></td></tr></table></center>");

		return html.toString();
	}

	private boolean itemCheckGrade(Player player, ItemInstance item)
	{
		Grade grade = item.getCrystalType();

		switch(grade)
		{
			case NONE:
				return Config.BBS_ENCHANT_GRADE_ATTRIBUTE[0].equals("NG:PA") ? (player.hasBonus() ? true : false) : (Config.BBS_ENCHANT_GRADE_ATTRIBUTE[0].equals("NG:ON") ? true : Config.BBS_ENCHANT_GRADE_ATTRIBUTE[0].equals("NG:NO") ? false : true);
			case D:
				return Config.BBS_ENCHANT_GRADE_ATTRIBUTE[1].equals("D:PA") ? (player.hasBonus() ? true : false) : (Config.BBS_ENCHANT_GRADE_ATTRIBUTE[1].equals("D:ON") ? true : Config.BBS_ENCHANT_GRADE_ATTRIBUTE[1].equals("D:NO") ? false : true);
			case C:
				return Config.BBS_ENCHANT_GRADE_ATTRIBUTE[2].equals("C:PA") ? (player.hasBonus() ? true : false) : (Config.BBS_ENCHANT_GRADE_ATTRIBUTE[2].equals("C:ON") ? true : Config.BBS_ENCHANT_GRADE_ATTRIBUTE[2].equals("C:NO") ? false : true);
			case B:
				return Config.BBS_ENCHANT_GRADE_ATTRIBUTE[3].equals("B:PA") ? (player.hasBonus() ? true : false) : (Config.BBS_ENCHANT_GRADE_ATTRIBUTE[3].equals("B:ON") ? true : Config.BBS_ENCHANT_GRADE_ATTRIBUTE[3].equals("B:NO") ? false : true);
			case A:
				return Config.BBS_ENCHANT_GRADE_ATTRIBUTE[4].equals("A:PA") ? (player.hasBonus() ? true : false) : (Config.BBS_ENCHANT_GRADE_ATTRIBUTE[4].equals("A:ON") ? true : Config.BBS_ENCHANT_GRADE_ATTRIBUTE[4].equals("A:NO") ? false : true);
			case S:
				return Config.BBS_ENCHANT_GRADE_ATTRIBUTE[5].equals("S:PA") ? (player.hasBonus() ? true : false) : (Config.BBS_ENCHANT_GRADE_ATTRIBUTE[5].equals("S:ON") ? true : Config.BBS_ENCHANT_GRADE_ATTRIBUTE[5].equals("S:NO") ? false : true);
			case S80:
				return Config.BBS_ENCHANT_GRADE_ATTRIBUTE[6].equals("S80:PA") ? (player.hasBonus() ? true : false) : (Config.BBS_ENCHANT_GRADE_ATTRIBUTE[6].equals("S80:ON") ? true : Config.BBS_ENCHANT_GRADE_ATTRIBUTE[6].equals("S80:NO") ? false : true);
			case S84:
				return Config.BBS_ENCHANT_GRADE_ATTRIBUTE[7].equals("S84:PA") ? (player.hasBonus() ? true : false) : (Config.BBS_ENCHANT_GRADE_ATTRIBUTE[7].equals("S84:ON") ? true : Config.BBS_ENCHANT_GRADE_ATTRIBUTE[7].equals("S84:NO") ? false : true);
			default:
				return false;
		}
	}

	private boolean canEnchantArmorAttribute(int attr, ItemInstance item)
	{
		switch(attr)
		{
			case 0:
				if(item.getAttributeElementValue(Element.getReverseElement(Element.getElementById(0)), false) != 0)
					return false;
				break;
			case 1:
				if(item.getAttributeElementValue(Element.getReverseElement(Element.getElementById(1)), false) != 0)
					return false;
				break;
			case 2:
				if(item.getAttributeElementValue(Element.getReverseElement(Element.getElementById(2)), false) != 0)
					return false;
				break;
			case 3:
				if(item.getAttributeElementValue(Element.getReverseElement(Element.getElementById(3)), false) != 0)
					return false;
				break;
			case 4:
				if(item.getAttributeElementValue(Element.getReverseElement(Element.getElementById(4)), false) != 0)
					return false;
				break;
			case 5:
				if(item.getAttributeElementValue(Element.getReverseElement(Element.getElementById(5)), false) != 0)
					return false;
				break;
		}
		return true;
	}

	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5) {}
}