package services.petevolve;

import jts.commons.dao.JdbcEntityState;
import jts.gameserver.model.Player;
import jts.gameserver.model.Summon;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.scripts.Functions;
import jts.gameserver.tables.PetDataTable;
import jts.gameserver.tables.PetDataTable.L2Pet;

public class fenrir extends Functions
{
	private static final int GREAT_WOLF = PetDataTable.GREAT_WOLF_ID;
	private static final int GREAT_WOLF_NECKLACE = L2Pet.GREAT_WOLF.getControlItemId();
	private static final int FENRIR_NECKLACE = L2Pet.FENRIR_WOLF.getControlItemId();

	public void evolve()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;
		if(player.getInventory().getItemByItemId(GREAT_WOLF_NECKLACE) == null)
		{
			show("scripts/services/petevolve/no_item.htm", player, npc);
			return;
		}
		Summon pl_pet = player.getPet();
		if(pl_pet == null || pl_pet.isDead())
		{
			show("scripts/services/petevolve/evolve_no.htm", player, npc);
			return;
		}
		if(pl_pet.getNpcId() != GREAT_WOLF)
		{
			show("scripts/services/petevolve/no_wolf.htm", player, npc);
			return;
		}
		if(pl_pet.getLevel() < 70)
		{
			show("scripts/services/petevolve/no_level_gw.htm", player, npc);
			return;
		}

		int controlItemId = player.getPet().getControlItemObjId();
		player.getPet().unSummon();

		ItemInstance control = player.getInventory().getItemByObjectId(controlItemId);
		control.setItemId(FENRIR_NECKLACE);
		control.setEnchantLevel(L2Pet.FENRIR_WOLF.getMinLevel());
		control.setJdbcState(JdbcEntityState.UPDATED);
		control.update();
		player.sendItemList(false);

		show("scripts/services/petevolve/yes_wolf.htm", player, npc);
	}
}