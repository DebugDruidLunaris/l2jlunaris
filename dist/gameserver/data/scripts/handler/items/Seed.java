package handler.items;

import jts.gameserver.cache.Msg;
import jts.gameserver.instancemanager.MapRegionManager;
import jts.gameserver.model.Manor;
import jts.gameserver.model.Playable;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.model.instances.ChestInstance;
import jts.gameserver.model.instances.MinionInstance;
import jts.gameserver.model.instances.MonsterInstance;
import jts.gameserver.model.instances.RaidBossInstance;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.tables.SkillTable;
import jts.gameserver.templates.mapregion.DomainArea;

public class Seed extends ScriptItemHandler
{
	private static int[] _itemIds = {};

	public Seed()
	{
		_itemIds = new int[Manor.getInstance().getAllSeeds().size()];
		int id = 0;
		for(Integer s : Manor.getInstance().getAllSeeds().keySet())
			_itemIds[id++] = s.shortValue();
	}

	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		if(playable == null || !playable.isPlayer())
			return false;
		Player player = (Player) playable;

		// Цель не выбрана
		if(playable.getTarget() == null)
		{
			player.sendActionFailed();
			return false;
		}

		// Цель не моб, РБ или миньон
		if(!player.getTarget().isMonster() || player.getTarget() instanceof RaidBossInstance || player.getTarget() instanceof MinionInstance && ((MinionInstance) player.getTarget()).getLeader() instanceof RaidBossInstance || player.getTarget() instanceof ChestInstance || ((MonsterInstance) playable.getTarget()).getChampion() > 0 && !item.isAltSeed())
		{
			player.sendPacket(SystemMsg.THE_TARGET_IS_UNAVAILABLE_FOR_SEEDING);
			return false;
		}

		MonsterInstance target = (MonsterInstance) playable.getTarget();

		if(target == null)
		{
			player.sendPacket(Msg.INVALID_TARGET);
			return false;
		}

		// Моб мертв
		if(target.isDead())
		{
			player.sendPacket(Msg.INVALID_TARGET);
			return false;
		}

		// Уже посеяно
		if(target.isSeeded())
		{
			player.sendPacket(SystemMsg.THE_SEED_HAS_BEEN_SOWN);
			return false;
		}

		int seedId = item.getItemId();
		if(seedId == 0 || player.getInventory().getItemByItemId(item.getItemId()) == null)
		{
			player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
			return false;
		}

		DomainArea domain = MapRegionManager.getInstance().getRegionData(DomainArea.class, player);
		int castleId = domain == null ? 0 : domain.getId();
		// Несовпадение зоны
		if(Manor.getInstance().getCastleIdForSeed(seedId) != castleId)
		{
			player.sendPacket(SystemMsg.THIS_SEED_MAY_NOT_BE_SOWN_HERE);
			return false;
		}

		// use Sowing skill, id 2097
		Skill skill = SkillTable.getInstance().getInfo(2097, 1);
		if(skill == null)
		{
			player.sendActionFailed();
			return false;
		}

		if(skill.checkCondition(player, target, false, false, true))
		{
			player.setUseSeed(seedId);
			player.getAI().Cast(skill, target);
		}
		return true;
	}

	@Override
	public final int[] getItemIds()
	{
		return _itemIds;
	}
}