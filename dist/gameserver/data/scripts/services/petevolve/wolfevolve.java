package services.petevolve;

import jts.commons.dao.JdbcEntityState;
import jts.gameserver.model.Player;
import jts.gameserver.model.Summon;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.scripts.Functions;
import jts.gameserver.tables.PetDataTable;
import jts.gameserver.tables.PetDataTable.L2Pet;

public class wolfevolve extends Functions
{
	private static final int WOLF = PetDataTable.PET_WOLF_ID; //Чтоб было =), проверка на Wolf
	private static final int WOLF_COLLAR = L2Pet.WOLF.getControlItemId(); // Ошейник Wolf
	private static final int GREAT_WOLF_NECKLACE = L2Pet.GREAT_WOLF.getControlItemId(); // Ожерелье Great Wolf

	public void evolve()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;
		Summon pl_pet = player.getPet();
		if(player.getInventory().getItemByItemId(WOLF_COLLAR) == null)
		{
			show("scripts/services/petevolve/no_item.htm", player, npc);
			return;
		}
		if(pl_pet == null || pl_pet.isDead())
		{
			show("scripts/services/petevolve/evolve_no.htm", player, npc);
			return;
		}
		if(pl_pet.getNpcId() != WOLF)
		{
			show("scripts/services/petevolve/no_wolf.htm", player, npc);
			return;
		}
		if(pl_pet.getLevel() < 55)
		{
			show("scripts/services/petevolve/no_level.htm", player, npc);
			return;
		}

		int controlItemId = player.getPet().getControlItemObjId();
		player.getPet().unSummon();

		ItemInstance control = player.getInventory().getItemByObjectId(controlItemId);
		control.setItemId(GREAT_WOLF_NECKLACE);
		control.setEnchantLevel(L2Pet.GREAT_WOLF.getMinLevel());
		control.setJdbcState(JdbcEntityState.UPDATED);
		control.update();
		player.sendItemList(false);

		show("scripts/services/petevolve/yes_wolf.htm", player, npc);
	}
}