package handler.items;

import jts.gameserver.data.xml.holder.EventHolder;
import jts.gameserver.model.Playable;
import jts.gameserver.model.Player;
import jts.gameserver.model.entity.events.EventType;
import jts.gameserver.model.entity.events.impl.DominionSiegeEvent;
import jts.gameserver.model.entity.events.impl.DominionSiegeRunnerEvent;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.network.serverpackets.SystemMessage2;
import jts.gameserver.network.serverpackets.components.SystemMsg;

public class DisguiseScroll extends ScriptItemHandler
{
	private int[] ITEM_IDS = 
	{ 
		13677, // Gludio Disguise Scroll
		13678, // Dion Disguise Scroll
		13679, // Giran Disguise Scroll
		13680, // Oren Disguise Scroll
		13681, // Aden Disguise Scroll
		13682, // Innadril Disguise Scroll
		13683, // Goddard Disguise Scroll
		13684, // Rune Disguise Scroll
		13685 // Schuttgart Disguise Scroll
	};
	private int[] DOMINION_IDS = { 81, 82, 83, 84, 85, 86, 87, 88, 89 };

	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		if(!playable.isPlayer())
			return false;

		Player player = (Player) playable;

		DominionSiegeRunnerEvent runnerEvent = EventHolder.getInstance().getEvent(EventType.MAIN_EVENT, 1);
		if(!runnerEvent.isBattlefieldChatActive())
		{
			player.sendPacket(SystemMsg.THE_TERRITORY_WAR_EXCLUSIVE_DISGUISE_AND_TRANSFORMATION_CAN_BE_USED_20_MINUTES_BEFORE_THE_START_OF_THE_TERRITORY_WAR_TO_10_MINUTES_AFTER_ITS_END);
			return false;
		}
		int index = org.apache.commons.lang3.ArrayUtils.indexOf(ITEM_IDS, item.getItemId());
		DominionSiegeEvent siegeEvent = player.getEvent(DominionSiegeEvent.class);
		if(siegeEvent == null)
		{
			player.sendPacket(new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addName(item));
			return false;
		}
		if(siegeEvent.getId() != DOMINION_IDS[index])
		{
			player.sendPacket(SystemMsg.THE_DISGUISE_SCROLL_CANNOT_BE_USED_BECAUSE_IT_IS_MEANT_FOR_USE_IN_A_DIFFERENT_TERRITORY);
			return false;
		}
		if(player.isCursedWeaponEquipped())
		{
			player.sendPacket(SystemMsg.A_DISGUISE_CANNOT_BE_USED_WHEN_YOU_ARE_IN_A_CHAOTIC_STATE);
			return false;
		}
		if(player.getPrivateStoreType() != Player.STORE_PRIVATE_NONE)
		{
			player.sendPacket(SystemMsg.THE_DISGUISE_SCROLL_CANNOT_BE_USED_WHILE_YOU_ARE_ENGAGED_IN_A_PRIVATE_STORE_OR_MANUFACTURE_WORKSHOP);
			return false;
		}
		if(siegeEvent.getResidence().getOwner() == player.getClan())
		{
			player.sendPacket(SystemMsg.A_TERRITORY_OWNING_CLAN_MEMBER_CANNOT_USE_A_DISGUISE_SCROLL);
			return false;
		}
		if(player.consumeItem(item.getItemId(), 1))
		{
			siegeEvent.addObject(DominionSiegeEvent.DISGUISE_PLAYERS, player.getObjectId());
			player.broadcastCharInfo();
		}
		return true;
	}

	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}