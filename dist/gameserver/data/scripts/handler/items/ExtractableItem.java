package handler.items;

import gnu.trove.set.hash.TIntHashSet;

import java.util.List;

import jts.commons.util.Rnd;
import jts.gameserver.data.xml.holder.ItemHolder;
import jts.gameserver.model.Player;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.templates.item.ItemTemplate;
import jts.gameserver.utils.ItemFunctions;

public class ExtractableItem extends SimpleItemHandler
{
	private int[] _itemIds;

	public ExtractableItem()
	{
		TIntHashSet set = new TIntHashSet();
		for(ItemTemplate template : ItemHolder.getInstance().getAllTemplates())
		{
			if(template == null)
				continue;
			if(template.isExtractable())
				set.add(template.getItemId());
		}
		_itemIds = set.toArray();
	}

	@Override
	protected boolean useItemImpl(Player player, ItemInstance item, boolean ctrl)
	{
		if(!canBeExtracted(player, item))
			return false;

		if(!tryUseItem(player, item, 1, false))
			return false;

		List<ItemTemplate.CapsuledItem> capsuled_items = item.getTemplate().getCapsuledItems();
		for(ItemTemplate.CapsuledItem ci : capsuled_items)
		{
			if(Rnd.chance(ci.getChance()))
				ItemFunctions.addItem(player, ci.getItemId(), Rnd.get(ci.getMinCount(), ci.getMaxCount()), true);
		}

		return true;
	}

	@Override
	public int[] getItemIds()
	{
		return _itemIds;
	}
}