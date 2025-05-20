package services.petevolve;

import jts.commons.dao.JdbcEntityState;
import jts.gameserver.Config;
import jts.gameserver.model.Player;
import jts.gameserver.model.Summon;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.scripts.Functions;
import jts.gameserver.tables.PetDataTable;
import jts.gameserver.tables.PetDataTable.L2Pet;

public class ibbuffalo extends Functions
{
	private static final int BABY_BUFFALO = PetDataTable.BABY_BUFFALO_ID;
	private static final int BABY_BUFFALO_PANPIPE = L2Pet.BABY_BUFFALO.getControlItemId();
	private static final int IN_BABY_BUFFALO_NECKLACE = L2Pet.IMPROVED_BABY_BUFFALO.getControlItemId();

	public void evolve()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;
		Summon pl_pet = player.getPet();
		if(player.getInventory().getItemByItemId(BABY_BUFFALO_PANPIPE) == null)
		{
			show("scripts/services/petevolve/no_item.htm", player, npc);
			return;
		}
		if(pl_pet == null || pl_pet.isDead())
		{
			show("scripts/services/petevolve/evolve_no.htm", player, npc);
			return;
		}
		if(pl_pet.getNpcId() != BABY_BUFFALO)
		{
			show("scripts/services/petevolve/no_pet.htm", player, npc);
			return;
		}
		if(Config.ALT_IMPROVED_PETS_LIMITED_USE && player.isMageClass())
		{
			show("scripts/services/petevolve/no_class_w.htm", player, npc);
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
		control.setItemId(IN_BABY_BUFFALO_NECKLACE);
		control.setEnchantLevel(L2Pet.IMPROVED_BABY_BUFFALO.getMinLevel());
		control.setJdbcState(JdbcEntityState.UPDATED);
		control.update();
		player.sendItemList(false);

		show("scripts/services/petevolve/yes_pet.htm", player, npc);
	}
}