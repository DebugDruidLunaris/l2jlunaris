package services;

import java.util.ArrayList;
import java.util.List;

import jts.commons.util.Rnd;
import jts.gameserver.cache.Msg;
import jts.gameserver.model.Player;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.scripts.Functions;

public class ObtainTalisman extends Functions
{
	public void Obtain()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		if(!NpcInstance.canBypassCheck(player, npc))
			return;

		if(!player.isQuestContinuationPossible(false))
		{
			player.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
			return;
		}

		if(getItemCount(player, 9912) < 10)
		{
			show("scripts/services/ObtainTalisman-no.htm", player, npc);
			return;
		}

		final List<Integer> talismans = new ArrayList<Integer>();

		//9914-9965
		for(int i = 9914; i <= 9965; i++)
			if(i != 9923)
				talismans.add(i);
		//10416-10424
		for(int i = 10416; i <= 10424; i++)
			talismans.add(i);
		//10518-10519
		for(int i = 10518; i <= 10519; i++)
			talismans.add(i);
		//10533-10543
		for(int i = 10533; i <= 10543; i++)
			talismans.add(i);

		removeItem(player, 9912, 10);
		addItem(player, talismans.get(Rnd.get(talismans.size())), 1);
		show("scripts/services/ObtainTalisman.htm", player, npc);
	}
}