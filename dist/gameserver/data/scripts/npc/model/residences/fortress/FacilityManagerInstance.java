package npc.model.residences.fortress;

import jts.commons.dao.JdbcEntityState;
import jts.gameserver.model.Player;
import jts.gameserver.model.entity.residence.Fortress;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.pledge.Clan;
import jts.gameserver.templates.item.ItemTemplate;
import jts.gameserver.templates.npc.NpcTemplate;

@SuppressWarnings("serial")
public abstract class FacilityManagerInstance extends NpcInstance
{
	public FacilityManagerInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	protected boolean buyFacility(Player player, int type, int lvl, long price)
	{
		Fortress fortress = getFortress();

		if((player.getClanPrivileges() & Clan.CP_CS_MANAGE_SIEGE) != Clan.CP_CS_MANAGE_SIEGE)
		{
			showChatWindow(player, "residence2/fortress/fortress_not_authorized.htm");
			return false;
		}

		if(fortress.getContractState() != Fortress.CONTRACT_WITH_CASTLE)
		{
			showChatWindow(player, "residence2/fortress/fortress_supply_officer005.htm");
			return false;
		}

		if(fortress.getFacilityLevel(type) >= lvl)
		{
			showChatWindow(player, "residence2/fortress/fortress_already_upgraded.htm");
			return false;
		}

		if(player.consumeItem(ItemTemplate.ITEM_ID_ADENA, price))
		{
			fortress.setFacilityLevel(type, lvl);
			fortress.setJdbcState(JdbcEntityState.UPDATED);
			fortress.update();

			showChatWindow(player, "residence2/fortress/fortress_supply_officer006.htm");
			return true;
		}
		else
		{
			showChatWindow(player, "residence2/fortress/fortress_not_enough_money.htm");
			return false;
		}
	}
}