package jts.gameserver.network.clientpackets;

import jts.commons.util.Rnd;
import jts.gameserver.Config;
import jts.gameserver.cache.Msg;
import jts.gameserver.data.xml.holder.ItemHolder;
import jts.gameserver.data.xml.holder.RecipeHolder;
import jts.gameserver.model.Player;
import jts.gameserver.model.Recipe;
import jts.gameserver.model.RecipeComponent;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.network.serverpackets.ActionFail;
import jts.gameserver.network.serverpackets.RecipeItemMakeInfo;
import jts.gameserver.network.serverpackets.SystemMessage;
import jts.gameserver.network.serverpackets.SystemMessage2;
import jts.gameserver.templates.item.EtcItemTemplate.EtcItemType;
import jts.gameserver.utils.ItemFunctions;

public class RequestRecipeItemMakeSelf extends L2GameClientPacket
{
	private int _recipeId;

	/**
	 * packet type id 0xB8
	 * format:		cd
	 */
	@Override
	protected void readImpl()
	{
		_recipeId = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInStoreMode())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isProcessingRequest())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isFishing())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
			return;
		}

		Recipe recipeList = RecipeHolder.getInstance().getRecipeByRecipeId(_recipeId);

		if(recipeList == null || recipeList.getRecipes().length == 0)
		{
			activeChar.sendPacket(Msg.THE_RECIPE_IS_INCORRECT);
			return;
		}

		if(activeChar.getCurrentMp() < recipeList.getMpCost())
		{
			activeChar.sendPacket(Msg.NOT_ENOUGH_MP, new RecipeItemMakeInfo(activeChar, recipeList, 0));
			return;
		}

		if(!activeChar.findRecipe(_recipeId))
		{
			activeChar.sendPacket(Msg.PLEASE_REGISTER_A_RECIPE, ActionFail.STATIC);
			return;
		}

		activeChar.getInventory().writeLock();
		try
		{
			RecipeComponent[] recipes = recipeList.getRecipes();

			for(RecipeComponent recipe : recipes)
			{
				if(recipe.getQuantity() == 0)
					continue;

				if(Config.ALT_GAME_UNREGISTER_RECIPE && ItemHolder.getInstance().getTemplate(recipe.getItemId()).getItemType() == EtcItemType.RECIPE)
				{
					Recipe rp = RecipeHolder.getInstance().getRecipeByRecipeItem(recipe.getItemId());
					if(activeChar.hasRecipe(rp))
						continue;
					activeChar.sendPacket(Msg.NOT_ENOUGH_MATERIALS, new RecipeItemMakeInfo(activeChar, recipeList, 0));
					return;
				}

				ItemInstance item = activeChar.getInventory().getItemByItemId(recipe.getItemId());
				if(item == null || item.getCount() < recipe.getQuantity())
				{
					activeChar.sendPacket(Msg.NOT_ENOUGH_MATERIALS, new RecipeItemMakeInfo(activeChar, recipeList, 0));
					return;
				}
			}

			for(RecipeComponent recipe : recipes)
				if(recipe.getQuantity() != 0)
					if(Config.ALT_GAME_UNREGISTER_RECIPE && ItemHolder.getInstance().getTemplate(recipe.getItemId()).getItemType() == EtcItemType.RECIPE)
						activeChar.unregisterRecipe(RecipeHolder.getInstance().getRecipeByRecipeItem(recipe.getItemId()).getId());
					else
					{
						if(!activeChar.getInventory().destroyItemByItemId(recipe.getItemId(), recipe.getQuantity()))
							continue;//TODO audit
						activeChar.sendPacket(SystemMessage2.removeItems(recipe.getItemId(), recipe.getQuantity()));
					}
		}
		finally
		{
			activeChar.getInventory().writeUnlock();
		}

		activeChar.resetWaitSitTime();
		activeChar.reduceCurrentMp(recipeList.getMpCost(), null);

		int tryCount = 1, success = 0;
		if(Rnd.chance(Config.ALT_CRAFT_DOUBLECRAFT_CHANCE))
			tryCount++;

		for(int i = 0; i < tryCount; i++)
			if(Rnd.chance(recipeList.getSuccessRate()))
			{
				int itemId = recipeList.getFoundation() != 0 ? Rnd.chance(Config.ALT_CRAFT_MASTERWORK_CHANCE) ? recipeList.getFoundation() : recipeList.getItemId() : recipeList.getItemId();
				long count = recipeList.getCount();
				//TODO [G1ta0] добавить проверку на перевес
				ItemFunctions.addItem(activeChar, itemId, count, true);
				if (Config.ALT_GAME_CREATION)
				{
					ItemInstance item = activeChar.getInventory().getItemByItemId(itemId);
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

					int _skillLevel = activeChar.getSkillLevel(172);
					for (int j = _skillLevel; j > recipeLevel; j--)
					{
						_exp /= 4;
						_sp /= 4;
					}
					activeChar.addExpAndSp(_exp * (long)Config.ALT_GAME_CREATION_XP_RATE, _sp * (long)Config.ALT_GAME_CREATION_SP_RATE);
				}
				success = 1;
			}

		if(success == 0)
			activeChar.sendPacket(new SystemMessage(SystemMessage.S1_MANUFACTURING_FAILURE).addItemName(recipeList.getItemId()));
		activeChar.sendPacket(new RecipeItemMakeInfo(activeChar, recipeList, success));
	}
}