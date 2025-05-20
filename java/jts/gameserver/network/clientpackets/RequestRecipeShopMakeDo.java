package jts.gameserver.network.clientpackets;

import jts.commons.util.Rnd;
import jts.gameserver.Config;
import jts.gameserver.cache.Msg;
import jts.gameserver.data.xml.holder.RecipeHolder;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.Recipe;
import jts.gameserver.model.RecipeComponent;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.model.items.ManufactureItem;
import jts.gameserver.network.serverpackets.RecipeShopItemInfo;
import jts.gameserver.network.serverpackets.StatusUpdate;
import jts.gameserver.network.serverpackets.SystemMessage;
import jts.gameserver.network.serverpackets.SystemMessage2;
import jts.gameserver.network.serverpackets.components.CustomMessage;
import jts.gameserver.utils.ItemFunctions;
import jts.gameserver.utils.TradeHelper;

public class RequestRecipeShopMakeDo extends L2GameClientPacket
{
	private int _manufacturerId;
	private int _recipeId;
	private long _price;

	@Override
	protected void readImpl()
	{
		_manufacturerId = readD();
		_recipeId = readD();
		_price = readQ();
	}

	@Override
	protected void runImpl()
	{
		Player buyer = getClient().getActiveChar();
		if(buyer == null)
			return;

		if(buyer.isActionsDisabled())
		{
			buyer.sendActionFailed();
			return;
		}

		if(buyer.isInStoreMode())
		{
			buyer.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}

		if(buyer.isInTrade())
		{
			buyer.sendActionFailed();
			return;
		}

		if(buyer.isFishing())
		{
			buyer.sendPacket(Msg.YOU_CANNOT_DO_ANYTHING_ELSE_WHILE_FISHING);
			return;
		}

		if(!buyer.getPlayerAccess().UseTrade)
		{
			buyer.sendPacket(Msg.THIS_ACCOUNT_CANOT_USE_PRIVATE_STORES);
			return;
		}

		Player manufacturer = (Player) buyer.getVisibleObject(_manufacturerId);
		if(manufacturer == null || manufacturer.getPrivateStoreType() != Player.STORE_PRIVATE_MANUFACTURE || !manufacturer.isInRangeZ(buyer, Creature.INTERACTION_DISTANCE))
		{
			buyer.sendActionFailed();
			return;
		}

		Recipe recipeList = null;
		for(ManufactureItem mi : manufacturer.getCreateList())
			if(mi.getRecipeId() == _recipeId)
				if(_price == mi.getCost())
				{
					recipeList = RecipeHolder.getInstance().getRecipeByRecipeId(_recipeId);
					break;
				}

		if(recipeList == null)
		{
			buyer.sendActionFailed();
			return;
		}

		int success = 0;

		if(recipeList.getRecipes().length == 0)
		{
			manufacturer.sendMessage(new CustomMessage("jts.gameserver.RecipeController.NoRecipe", manufacturer).addString(recipeList.getRecipeName()));
			buyer.sendMessage(new CustomMessage("jts.gameserver.RecipeController.NoRecipe", manufacturer).addString(recipeList.getRecipeName()));
			return;
		}

		if(!manufacturer.findRecipe(_recipeId))
		{
			buyer.sendActionFailed();
			return;
		}

		if(manufacturer.getCurrentMp() < recipeList.getMpCost())
		{
			manufacturer.sendPacket(Msg.NOT_ENOUGH_MP);
			buyer.sendPacket(Msg.NOT_ENOUGH_MP, new RecipeShopItemInfo(buyer, manufacturer, _recipeId, _price, success));
			return;
		}

		buyer.getInventory().writeLock();
		try
		{
			if(buyer.getAdena() < _price)
			{
				buyer.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA, new RecipeShopItemInfo(buyer, manufacturer, _recipeId, _price, success));
				return;
			}

			RecipeComponent[] recipes = recipeList.getRecipes();

			for(RecipeComponent recipe : recipes)
			{
				if(recipe.getQuantity() == 0)
					continue;

				ItemInstance item = buyer.getInventory().getItemByItemId(recipe.getItemId());

				if(item == null || recipe.getQuantity() > item.getCount())
				{
					buyer.sendPacket(Msg.NOT_ENOUGH_MATERIALS, new RecipeShopItemInfo(buyer, manufacturer, _recipeId, _price, success));
					return;
				}
			}

			if(!buyer.reduceAdena(_price, false))
			{
				buyer.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA, new RecipeShopItemInfo(buyer, manufacturer, _recipeId, _price, success));
				return;
			}

			for(RecipeComponent recipe : recipes)
				if(recipe.getQuantity() != 0)
				{
					buyer.getInventory().destroyItemByItemId(recipe.getItemId(), recipe.getQuantity());
					//TODO audit
					buyer.sendPacket(SystemMessage2.removeItems(recipe.getItemId(), recipe.getQuantity()));
				}

			long tax = TradeHelper.getTax(manufacturer, _price);
			if(tax > 0)
			{
				_price -= tax;
				manufacturer.sendMessage(new CustomMessage("trade.HavePaidTax", manufacturer).addNumber(tax));
			}

			manufacturer.addAdena(_price);
		}
		finally
		{
			buyer.getInventory().writeUnlock();
		}

		manufacturer.sendMessage(new CustomMessage("jts.gameserver.RecipeController.GotOrder", manufacturer).addString(recipeList.getRecipeName()));

		manufacturer.reduceCurrentMp(recipeList.getMpCost(), null);
		manufacturer.sendStatusUpdate(false, false, StatusUpdate.CUR_MP);

		int tryCount = 1, successCount = 0;
		if(Rnd.chance(Config.ALT_CRAFT_DOUBLECRAFT_CHANCE))
			tryCount++;

		for(int i = 0; i < tryCount; i++)
			if(Rnd.chance(recipeList.getSuccessRate()))
			{
				int itemId = recipeList.getFoundation() != 0 ? Rnd.chance(Config.ALT_CRAFT_MASTERWORK_CHANCE) ? recipeList.getFoundation() : recipeList.getItemId() : recipeList.getItemId();
				long count = recipeList.getCount();
				ItemFunctions.addItem(buyer, itemId, count, true);
				if (Config.ALT_GAME_CREATION)
				{
					ItemInstance item = buyer.getInventory().getItemByItemId(itemId);
					long _exp = -1;
					long _sp = -1;
					int recipeLevel = recipeList.getLevel();
					if (_exp < 0)
					{
						_exp = item.getTemplate().getReferencePrice() * count;
						_exp /= recipeLevel;
					}
					if (_sp < 0)
						_sp = _exp / 10;
					if (itemId == recipeList.getFoundation())
					{
						_exp *= (long)Config.ALT_GAME_CREATION_RARE_XPSP_RATE;
						_sp *= (long)Config.ALT_GAME_CREATION_RARE_XPSP_RATE;
					}
					if (_exp < 0)
						_exp = 0;
					if (_sp < 0)
						_sp = 0;

					int _skillLevel = manufacturer.getSkillLevel(172);
					for (int j = _skillLevel; j > recipeLevel; j--)
					{
						_exp /= 4;
						_sp /= 4;
					}
					manufacturer.addExpAndSp(_exp * (long)Config.ALT_GAME_CREATION_XP_RATE, _sp * (long)Config.ALT_GAME_CREATION_SP_RATE);
				}
				success = 1;
				successCount++;
			}

		SystemMessage sm;
		if(successCount == 0)
		{
			sm = new SystemMessage(SystemMessage.S1_HAS_FAILED_TO_CREATE_S2_AT_THE_PRICE_OF_S3_ADENA);
			sm.addString(manufacturer.getName());
			sm.addItemName(recipeList.getItemId());
			sm.addNumber(_price);
			buyer.sendPacket(sm);

			sm = new SystemMessage(SystemMessage.THE_ATTEMPT_TO_CREATE_S2_FOR_S1_AT_THE_PRICE_OF_S3_ADENA_HAS_FAILED);
			sm.addString(buyer.getName());
			sm.addItemName(recipeList.getItemId());
			sm.addNumber(_price);
			manufacturer.sendPacket(sm);

		}
		else if(recipeList.getCount() > 1 || successCount > 1)
		{
			sm = new SystemMessage(SystemMessage.S1_CREATED_S2_S3_AT_THE_PRICE_OF_S4_ADENA);
			sm.addString(manufacturer.getName());
			sm.addItemName(recipeList.getItemId());
			sm.addNumber(recipeList.getCount() * successCount);
			sm.addNumber(_price);
			buyer.sendPacket(sm);

			sm = new SystemMessage(SystemMessage.S2_S3_HAVE_BEEN_SOLD_TO_S1_FOR_S4_ADENA);
			sm.addString(buyer.getName());
			sm.addItemName(recipeList.getItemId());
			sm.addNumber(recipeList.getCount() * successCount);
			sm.addNumber(_price);
			manufacturer.sendPacket(sm);

		}
		else
		{
			sm = new SystemMessage(SystemMessage.S1_CREATED_S2_AFTER_RECEIVING_S3_ADENA);
			sm.addString(manufacturer.getName());
			sm.addItemName(recipeList.getItemId());
			sm.addNumber(_price);
			buyer.sendPacket(sm);

			sm = new SystemMessage(SystemMessage.S2_IS_SOLD_TO_S1_AT_THE_PRICE_OF_S3_ADENA);
			sm.addString(buyer.getName());
			sm.addItemName(recipeList.getItemId());
			sm.addNumber(_price);
			manufacturer.sendPacket(sm);
		}

		buyer.sendChanges();
		buyer.sendPacket(new RecipeShopItemInfo(buyer, manufacturer, _recipeId, _price, success));
	}
}