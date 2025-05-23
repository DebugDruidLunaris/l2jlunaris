package npc.model.residences;

import java.util.List;
import java.util.Map;

import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.base.Experience;
import jts.gameserver.model.entity.events.impl.SiegeEvent;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.pledge.Clan;
import jts.gameserver.model.reward.RewardItem;
import jts.gameserver.model.reward.RewardList;
import jts.gameserver.model.reward.RewardType;
import jts.gameserver.stats.Stats;
import jts.gameserver.templates.npc.NpcTemplate;

@SuppressWarnings("serial")
public class SiegeGuardInstance extends NpcInstance
{
	public SiegeGuardInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		setHasChatWindow(false);
	}

	@Override
	public boolean isSiegeGuard()
	{
		return true;
	}

	@Override
	public int getAggroRange()
	{
		return 1200;
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		Player player = attacker.getPlayer();
		if(player == null)
			return false;
		SiegeEvent<?, ?> siegeEvent = getEvent(SiegeEvent.class);
		SiegeEvent<?, ?> siegeEvent2 = attacker.getEvent(SiegeEvent.class);
		Clan clan = player.getClan();
		if(siegeEvent == null)
			return false;
		if(clan != null && siegeEvent == siegeEvent2 && siegeEvent.getSiegeClan(SiegeEvent.DEFENDERS, clan) != null)
			return false;
		return true;
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}

	@Override
	public boolean isInvul()
	{
		return false;
	}

	@Override
	protected void onDeath(Creature killer)
	{
		SiegeEvent<?, ?> siegeEvent = getEvent(SiegeEvent.class);
		if(killer != null)
		{
			Player player = killer.getPlayer();
			if(siegeEvent != null && player != null)
			{
				Clan clan = player.getClan();
				SiegeEvent<?, ?> siegeEvent2 = killer.getEvent(SiegeEvent.class);
				if(clan != null && siegeEvent == siegeEvent2 && siegeEvent.getSiegeClan(SiegeEvent.DEFENDERS, clan) == null)
				{
					Creature topdam = getAggroList().getTopDamager();
					if(topdam == null)
						topdam = killer;

					for(Map.Entry<RewardType, RewardList> entry : getTemplate().getRewards().entrySet())
						rollRewards(entry, killer, topdam);
				}
			}
		}
		super.onDeath(killer);
	}

	public void rollRewards(Map.Entry<RewardType, RewardList> entry, final Creature lastAttacker, Creature topDamager)
	{
		RewardList list = entry.getValue();

		final Player activePlayer = topDamager.getPlayer();

		if(activePlayer == null)
			return;

		final int diff = calculateLevelDiffForDrop(topDamager.getLevel());
		double mod = calcStat(Stats.REWARD_MULTIPLIER, 1., topDamager, null);
		mod *= Experience.penaltyModifier(diff, 9);

		List<RewardItem> rewardItems = list.roll(activePlayer, mod, false, true);

		for(RewardItem drop : rewardItems)
			dropItem(activePlayer, drop.itemId, drop.count);
	}

	@Override
	public boolean isFearImmune()
	{
		return true;
	}

	@Override
	public boolean isParalyzeImmune()
	{
		return true;
	}

	@Override
	public Clan getClan()
	{
		return null;
	}
}