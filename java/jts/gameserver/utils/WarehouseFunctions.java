package jts.gameserver.utils;

import jts.gameserver.Config;
import jts.gameserver.cache.Msg;
import jts.gameserver.model.Player;
import jts.gameserver.model.items.Warehouse;
import jts.gameserver.model.items.Warehouse.WarehouseType;
import jts.gameserver.model.pledge.Clan;
import jts.gameserver.network.serverpackets.WareHouseDepositList;
import jts.gameserver.network.serverpackets.WareHouseWithdrawList;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.templates.item.ItemTemplate.ItemClass;

public final class WarehouseFunctions
{
	private WarehouseFunctions() {}

	public static void showFreightWindow(Player player)
	{
		if(!WarehouseFunctions.canShowWarehouseWithdrawList(player, WarehouseType.FREIGHT))
		{
			player.sendActionFailed();
			return;
		}

		player.setUsingWarehouseType(WarehouseType.FREIGHT);
		player.sendPacket(new WareHouseWithdrawList(player, WarehouseType.FREIGHT, ItemClass.ALL));
	}

	public static void showRetrieveWindow(Player player, int val)
	{
		if(!WarehouseFunctions.canShowWarehouseWithdrawList(player, WarehouseType.PRIVATE))
		{
			player.sendActionFailed();
			return;
		}

		player.setUsingWarehouseType(WarehouseType.PRIVATE);
		player.sendPacket(new WareHouseWithdrawList(player, WarehouseType.PRIVATE, ItemClass.values()[val]));
	}

	public static void showDepositWindow(Player player)
	{
		if(!WarehouseFunctions.canShowWarehouseDepositList(player, WarehouseType.PRIVATE))
		{
			player.sendActionFailed();
			return;
		}

		player.setUsingWarehouseType(WarehouseType.PRIVATE);
		player.sendPacket(new WareHouseDepositList(player, WarehouseType.PRIVATE));
	}

	public static void showDepositWindowClan(Player player)
	{
		if(!WarehouseFunctions.canShowWarehouseDepositList(player, WarehouseType.CLAN))
		{
			player.sendActionFailed();
			return;
		}

		if(!(player.isClanLeader() || (Config.ALT_ALLOW_OTHERS_WITHDRAW_FROM_CLAN_WAREHOUSE || player.getVarB("canWhWithdraw")) && (player.getClanPrivileges() & Clan.CP_CL_WAREHOUSE_SEARCH) == Clan.CP_CL_WAREHOUSE_SEARCH))
			player.sendPacket(Msg.ITEMS_LEFT_AT_THE_CLAN_HALL_WAREHOUSE_CAN_ONLY_BE_RETRIEVED_BY_THE_CLAN_LEADER_DO_YOU_WANT_TO_CONTINUE);

		player.setUsingWarehouseType(WarehouseType.CLAN);
		player.sendPacket(new WareHouseDepositList(player, WarehouseType.CLAN));
	}

	public static void showWithdrawWindowClan(Player player, int val)
	{
		if(!WarehouseFunctions.canShowWarehouseWithdrawList(player, WarehouseType.CLAN))
		{
			player.sendActionFailed();
			return;
		}

		player.setUsingWarehouseType(WarehouseType.CLAN);
		player.sendPacket(new WareHouseWithdrawList(player, WarehouseType.CLAN, ItemClass.values()[val]));
	}

	public static boolean canShowWarehouseWithdrawList(Player player, WarehouseType type)
	{
		if(!player.getPlayerAccess().UseWarehouse)
			return false;

		Warehouse warehouse = null;
		switch(type)
		{
			case PRIVATE:
				warehouse = player.getWarehouse();
				break;
			case FREIGHT:
				warehouse = player.getFreight();
				break;
			case CLAN:
			case CASTLE:

				if(player.getClan() == null || player.getClan().getLevel() == 0)
				{
					player.sendPacket(Msg.ONLY_CLANS_OF_CLAN_LEVEL_1_OR_HIGHER_CAN_USE_A_CLAN_WAREHOUSE);
					return false;
				}

				boolean canWithdrawCWH = false;
				if(player.getClan() != null)
					if((player.getClanPrivileges() & Clan.CP_CL_WAREHOUSE_SEARCH) == Clan.CP_CL_WAREHOUSE_SEARCH)
						canWithdrawCWH = true;
				if(!canWithdrawCWH)
				{
					player.sendPacket(Msg.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_THE_CLAN_WAREHOUSE);
					return false;
				}
				warehouse = player.getClan().getWarehouse();
				break;
			default:
				return false;
		}

		if(warehouse.getSize() == 0)
		{
			player.sendPacket(type == WarehouseType.FREIGHT ? SystemMsg.NO_PACKAGES_HAVE_ARRIVED : Msg.YOU_HAVE_NOT_DEPOSITED_ANY_ITEMS_IN_YOUR_WAREHOUSE);
			return false;
		}

		return true;
	}

	public static boolean canShowWarehouseDepositList(Player player, WarehouseType type)
	{
		if(!player.getPlayerAccess().UseWarehouse)
			return false;

		switch(type)
		{
			case PRIVATE:
				return true;
			case CLAN:
			case CASTLE:

				if(player.getClan() == null || player.getClan().getLevel() == 0)
				{
					player.sendPacket(Msg.ONLY_CLANS_OF_CLAN_LEVEL_1_OR_HIGHER_CAN_USE_A_CLAN_WAREHOUSE);
					return false;
				}

				boolean canWithdrawCWH = false;
				if(player.getClan() != null)
					if((player.getClanPrivileges() & Clan.CP_CL_WAREHOUSE_SEARCH) == Clan.CP_CL_WAREHOUSE_SEARCH)
						canWithdrawCWH = true;
				if(!canWithdrawCWH)
				{
					player.sendPacket(Msg.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_THE_CLAN_WAREHOUSE);
					return false;
				}
				return true;
			default:
				return false;
		}
	}
}