package services;

import jts.gameserver.Config;
import jts.gameserver.data.xml.holder.ItemHolder;
import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.MagicSkillUse;
import jts.gameserver.scripts.Functions;
import jts.gameserver.templates.item.ItemTemplate;

public class CostumeShop extends Functions
{
	private static final int PRICE_WORK = Config.Item_Custom_Template_ID_PRICE_COUNT;
	private static final int ITEM_WORK = Config.Item_Custom_Template_ID_PRICE;
	
	public void SetFormalWear(String[] param)
	{
		if(param.length != 1)
			throw new IllegalArgumentException();

		int FormalWearId = Integer.parseInt(param[0]);
		boolean _BypassParamInvalid = true;

		for(int costume : ItemTemplate.ITEM_ID_FORMAL_WEAR)
			if(FormalWearId == costume)
				_BypassParamInvalid = false;
		if(_BypassParamInvalid)
			return;
		
		Player player = getSelf();
		
		if(player.getVar("FormalWearId") != null)
		{
			player.sendMessage(player.isLangRus() ? "У Вас уже вставлен костюм." : "You already inserted costume.");
			return;
		}
		
		if(player.getInventory().destroyItemByItemId(ITEM_WORK, PRICE_WORK))
		{
			if(Functions.getItemCount(player, FormalWearId) >= 1)
			{
				player.getInventory().destroyItemByItemId(ITEM_WORK, PRICE_WORK);
				Functions.removeItem(player, FormalWearId, 1);
				player.setVar("FormalWearId", FormalWearId, -1);
				player.broadcastPacket(new MagicSkillUse(player, player, 2003, 1, 1, 0));
				if(!player.getInventory().isRefresh)
					player.getInventory().refreshEquip();
			}
			else
				player.sendMessage(player.isLangRus() ? "У вас нет Нужного Костюма." : "You do not have the necessary Costume.");
				return;
			}
			else
				player.sendMessage(player.isLangRus() ? "У вас не хватает : " + ItemHolder.getInstance().getTemplate(Config.Item_Custom_Template_ID_PRICE).getName()+" В количестве : "+ Config.Item_Custom_Template_ID_PRICE_COUNT +"." : "You are missing : " + ItemHolder.getInstance().getTemplate(Config.Item_Custom_Template_ID_PRICE).getName()+" The amount : "+ Config.Item_Custom_Template_ID_PRICE_COUNT +".");
				return;
	}
	
	public void RemoveFormalWear(String[] param)
	{
		if(param.length != 1)
			throw new IllegalArgumentException();

		int FormalWearId = Integer.parseInt(param[0]);
		boolean _BypassParamInvalid = true;

		for(int costume : ItemTemplate.ITEM_ID_FORMAL_WEAR)
			if(FormalWearId == costume)
				_BypassParamInvalid = false;
		if(_BypassParamInvalid)
			return;
		
		Player player = getSelf();
		
		
		if(player.getVar("FormalWearId") == null)
		{
			player.sendMessage(player.isLangRus() ? "У Вас не вставлен костюм." : "You have not inserted a costume.");
			return;
		}
		//зачем брать за снятие костюма вообще какую то цену , поэтому снимаем косюм за 1 адену
		if(player.getInventory().destroyItemByItemId(57, 1))
		{
			if(player.getVarInt("FormalWearId") == FormalWearId)
			{
				player.getInventory().destroyItemByItemId(57, 1);
				player.unsetVar("FormalWearId");
				Functions.addItem(player, FormalWearId, 1);
				player.broadcastPacket(new MagicSkillUse(player, player, 2003, 1, 1, 0));
				if(!player.getInventory().isRefresh)
					player.getInventory().refreshEquip();
			}
			else
			player.sendMessage(player.isLangRus() ? "У вас нет Нужного Костюма." : "You do not have the necessary Costume.");
			return;
		}
		else
			player.sendMessage(player.isLangRus() ? "У вас не хватает : " + ItemHolder.getInstance().getTemplate(57).getName()+" В количестве : "+ 1 +"." : "You are missing : " + ItemHolder.getInstance().getTemplate(5).getName()+" The amount : "+ 1 +".");
			return;
	}
}