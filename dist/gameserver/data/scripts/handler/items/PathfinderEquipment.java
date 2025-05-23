package handler.items;

import gnu.trove.map.hash.TIntObjectHashMap;
import jts.gameserver.cache.Msg;
import jts.gameserver.model.Player;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.network.serverpackets.SystemMessage;
import jts.gameserver.scripts.Functions;

public class PathfinderEquipment extends SimpleItemHandler
{
	private static TIntObjectHashMap<int[][]> rewards = new TIntObjectHashMap<int[][]>();
	static
	{
		rewards.put(12824, new int[][] { { 1539, 4 }, { 1463, 50 }, { 2510, 50 } });
		rewards.put(10836, new int[][] { { 1539, 4 }, { 1463, 100 }, { 2510, 100 }, { 956, 1 }, { 8623, 3 } });
		rewards.put(10837, new int[][] { { 1539, 2 }, { 1463, 150 }, { 2510, 150 }, { 956, 1 }, { 8623, 3 }, { 8629, 2 } });
		rewards.put(10838, new int[][] { { 1539, 5 }, { 1463, 210 }, { 2510, 210 }, { 956, 1 }, { 8623, 3 }, { 8629, 3 } });
		rewards.put(10844, new int[][] { { 1539, 10 }, { 1463, 500 }, { 2510, 500 }, { 956, 2 }, { 955, 1 }, { 8623, 15 }, { 8629, 15 } });
		rewards.put(12825, new int[][] { { 1539, 4 }, { 1463, 100 }, { 2510, 100 } });
		rewards.put(10841, new int[][] { { 1539, 7 }, { 1463, 250 }, { 2510, 250 }, { 956, 1 }, { 8623, 5 }, { 8629, 4 } });
		rewards.put(12827, new int[][] { { 1539, 10 }, { 1463, 500 }, { 2510, 500 }, { 956, 1 }, { 955, 1 }, { 8623, 10 }, { 8629, 10 } });
		rewards.put(10840, new int[][] { { 1539, 5 }, { 1463, 140 }, { 2510, 100 } });
		rewards.put(10842, new int[][] { { 1539, 7 }, { 1463, 400 }, { 2510, 400 }, { 956, 1 }, { 8623, 6 }, { 8629, 6 } });
		rewards.put(10843, new int[][] { { 1539, 9 }, { 1463, 400 }, { 2510, 400 }, { 956, 1 }, { 8623, 9 }, { 8629, 8 } });
		rewards.put(12826, new int[][] { { 1539, 6 }, { 1463, 200 }, { 2510, 200 } });
		rewards.put(10846, new int[][] { { 1539, 5 }, { 1464, 120 }, { 2511, 120 }, { 952, 1 }, { 8624, 5 }, { 8630, 4 } });
		rewards.put(12829, new int[][] { { 1539, 7 }, { 1464, 300 }, { 2511, 300 }, { 952, 1 }, { 951, 1 }, { 8624, 7 }, { 8630, 7 } });
		rewards.put(10845, new int[][] { { 1539, 7 }, { 1464, 150 }, { 2511, 150 } });
		rewards.put(10847, new int[][] { { 1539, 6 }, { 1464, 200 }, { 2511, 200 }, { 8624, 7 }, { 8630, 6 } });
		rewards.put(10848, new int[][] { { 1539, 10 }, { 1464, 250 }, { 2511, 250 }, { 952, 1 }, { 8624, 8 }, { 8630, 7 } });
		rewards.put(10849, new int[][] { { 1539, 10 }, { 1464, 350 }, { 2511, 350 }, { 952, 2 }, { 951, 1 }, { 8624, 10 }, { 8630, 10 } });
		rewards.put(12828, new int[][] { { 1539, 11 }, { 1464, 150 }, { 2511, 150 } });
		rewards.put(10851, new int[][] { { 1539, 10 }, { 1465, 200 }, { 2512, 200 }, { 8625, 7 }, { 8631, 7 } });
		rewards.put(12831, new int[][] { { 1539, 15 }, { 1464, 300 }, { 2511, 300 }, { 952, 2 }, { 951, 1 }, { 8624, 15 }, { 8630, 14 } });
		rewards.put(10850, new int[][] { { 1539, 5 }, { 1465, 100 }, { 2512, 100 } });
		rewards.put(10852, new int[][] { { 1539, 4 }, { 1465, 200 }, { 2512, 200 }, { 948, 1 }, { 8625, 5 }, { 8631, 5 } });
		rewards.put(10853, new int[][] { { 1539, 10 }, { 1465, 250 }, { 2512, 250 }, { 948, 1 }, { 8625, 6 }, { 8631, 6 } });
		rewards.put(10854, new int[][] { { 1539, 15 }, { 1465, 380 }, { 2512, 380 }, { 948, 2 }, { 8625, 12 }, { 8631, 12 } });
		rewards.put(12830, new int[][] { { 1539, 8 }, { 1465, 120 }, { 2512, 120 }, { 8625, 7 }, { 8631, 7 } });
		rewards.put(10856, new int[][] { { 1539, 10 }, { 1466, 250 }, { 2513, 250 }, { 8626, 9 }, { 8632, 9 } });
		rewards.put(12833, new int[][] { { 1539, 4 }, { 1465, 100 }, { 2512, 100 }, { 947, 1 }, { 8625, 3 }, { 8631, 3 } });
		rewards.put(10855, new int[][] { { 1539, 5 }, { 1466, 120 }, { 2513, 120 } });
		rewards.put(10857, new int[][] { { 1539, 10 }, { 1466, 300 }, { 2513, 300 }, { 8626, 10 }, { 8632, 10 } });
		rewards.put(10858, new int[][] { { 1539, 3 }, { 1466, 140 }, { 2513, 140 }, { 947, 1 }, { 8626, 5 }, { 8632, 5 } });
		rewards.put(10859, new int[][] { { 1539, 6 }, { 1466, 250 }, { 2513, 250 }, { 947, 2 }, { 8626, 6 }, { 8632, 6 } });
		rewards.put(12832, new int[][] { { 1539, 5 }, { 1466, 150 }, { 2513, 150 } });
		rewards.put(10861, new int[][] { { 1539, 10 }, { 1467, 350 }, { 2514, 350 }, { 8627, 12 }, { 8633, 11 } });
		rewards.put(12834, new int[][] { { 1539, 10 }, { 1467, 350 }, { 2514, 350 }, { 730, 2 }, { 8627, 10 }, { 8633, 10 } });
		rewards.put(10860, new int[][] { { 1539, 9 }, { 1467, 130 }, { 2514, 130 } });
		rewards.put(10862, new int[][] { { 1539, 12 }, { 1467, 360 }, { 2514, 360 }, { 8627, 12 }, { 8633, 12 } });
		rewards.put(10863, new int[][] { { 1539, 6 }, { 1467, 140 }, { 2514, 140 }, { 960, 1 }, { 8627, 4 }, { 8633, 3 } });
		rewards.put(10864, new int[][] { { 1539, 15 }, { 1467, 360 }, { 2514, 360 }, { 960, 1 }, { 8627, 11 }, { 8633, 11 } });
		rewards.put(10865, new int[][] { { 1539, 15 }, { 1467, 360 }, { 2514, 360 }, { 960, 1 }, { 8627, 11 }, { 8633, 11 } });
	}

	@Override
	protected boolean useItemImpl(Player player, ItemInstance item, boolean ctrl)
	{
		int itemId = item.getItemId();

		if(!canBeExtracted(itemId, player))
			return false;

		int[][] _rewards = rewards.get(itemId);
		if(_rewards == null || _rewards.length <= 0)
			return false;

		if(Functions.removeItem(player, itemId, 1) != 1)
			return false;

		for(int[] reward : _rewards)
			if(reward.length == 2)
				if(reward[0] > 0 && reward[1] > 0)
					Functions.addItem(player, reward[0], reward[1]);

		return true;
	}

	public static boolean canBeExtracted(int itemId, Player player)
	{
		if(player == null)
			return false;
		if(player.getWeightPenalty() >= 3 || player.getInventory().getSize() > player.getInventoryLimit() - 10)
		{
			player.sendPacket(Msg.YOUR_INVENTORY_IS_FULL, new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(itemId));
			return false;
		}
		return true;
	}

	@Override
	public int[] getItemIds()
	{
		return rewards.keys();
	}
}