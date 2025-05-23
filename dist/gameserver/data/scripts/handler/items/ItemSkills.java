package handler.items;

import gnu.trove.set.hash.TIntHashSet;
import jts.gameserver.data.xml.holder.ItemHolder;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Playable;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.templates.item.ItemTemplate;

public class ItemSkills extends ScriptItemHandler
{
	private int[] _itemIds;

	public ItemSkills()
	{
		TIntHashSet set = new TIntHashSet();
		for(ItemTemplate template : ItemHolder.getInstance().getAllTemplates())
		{
			if(template == null)
				continue;

			for(Skill skill : template.getAttachedSkills())
				if(skill.isHandler())
					set.add(template.getItemId());
		}
		_itemIds = set.toArray();
	}

	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		Player player;
		if(playable.isPlayer())
			player = (Player) playable;
		else if(playable.isPet())
			player = playable.getPlayer();
		else
			return false;

		Skill[] skills = item.getTemplate().getAttachedSkills();

		for(int i = 0; i < skills.length; i++)
		{
			Skill skill = skills[i];
			Creature aimingTarget = skill.getAimingTarget(player, player.getTarget());
			if(skill.checkCondition(player, aimingTarget, ctrl, false, true))
				player.getAI().Cast(skill, aimingTarget, ctrl, false);
			else if(i == 0) //FIXME [VISTALL] всегда первый скил идет вместо конда?
				return false;
		}
		return true;
	}

	@Override
	public int[] getItemIds()
	{
		return _itemIds;
	}
}