package handler.items;

import jts.commons.util.Rnd;
import jts.gameserver.cache.Msg;
import jts.gameserver.instancemanager.QuestManager;
import jts.gameserver.instancemanager.ReflectionManager;
import jts.gameserver.model.GameObject;
import jts.gameserver.model.Player;
import jts.gameserver.model.Zone.ZoneType;
import jts.gameserver.model.instances.DoorInstance;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.network.serverpackets.SystemMessage;
import jts.gameserver.network.serverpackets.components.CustomMessage;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.scripts.Functions;
import jts.gameserver.tables.SkillTable;
import jts.gameserver.utils.Location;

import org.apache.commons.lang3.ArrayUtils;

import quests._464_Oath;
import bosses.AntharasManager;
import bosses.ValakasManager;

public class Special extends SimpleItemHandler
{
	private static final int[] ITEM_IDS = new int[] 
	{
		8060,
		8556,
		13853,
		13808,
		13809,
		20630,
		14835,
		15537,
		10632,
		21899,
		21900,
		21901,
		21902,
		21903,
		21904,
		17268,
		21106,
		21107 
	};

	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}

	@Override
	protected boolean useItemImpl(Player player, ItemInstance item, boolean ctrl)
	{
		int itemId = item.getItemId();

		switch(itemId)
		{
		//Key of Enigma
			case 8060:
				return use8060(player, ctrl);
				//Dewdrop of Destruction
			case 8556:
				return use8556(player, ctrl);
				//DestroyedDarknessFragmentPowder -> DestroyedLightFragmentPowder
			case 13853:
				return use13853(player, ctrl);
				//Holy Water for SSQ 2nd quest
			case 13808:
				return use13808(player, ctrl);
				//Court Mag Staff for SSQ 2nd quest
			case 13809:
				return use13809(player, ctrl);
			case 14835:
				return use14835(player, ctrl);
				//Strongbox of Promise
			case 15537:
				return use15537(player, ctrl);
				//Soul Magic Box
			case 20630:
				return use20630(player, ctrl);
				//refined wondrous cube			
			case 21107:
				return use21107(player, ctrl);
				//Wondrous Cubic 1 time use
			case 21106:
				return use21106(player, ctrl);
				//Wondrous Cubic	
			case 10632:
				return use10632(player, ctrl);
			case 21899:
				return use21899(player, ctrl);
			case 21900:
				return use21900(player, ctrl);
			case 21901:
				return use21901(player, ctrl);
			case 21902:
				return use21902(player, ctrl);
			case 21903:
				return use21903(player, ctrl);
			case 21904:
				return use21904(player, ctrl);
				//Antharas Blood Crystal
			case 17268:
				return use17268(player, ctrl);
			default:
				return false;
		}
	}

	//Key of Enigma
	private boolean use8060(Player player, boolean ctrl)
	{
		if(Functions.removeItem(player, 8058, 1) == 1)
		{
			Functions.addItem(player, 8059, 1);
			return true;
		}
		return false;
	}

	//Dewdrop of Destruction
	private boolean use8556(Player player, boolean ctrl)
	{
		int[] npcs = { 29048, 29049 };

		GameObject t = player.getTarget();
		if(t == null || !t.isNpc() || !ArrayUtils.contains(npcs, ((NpcInstance) t).getNpcId()))
		{
			player.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(8556));
			return false;
		}
		if(player.getDistance(t) > 200)
		{
			player.sendPacket(new SystemMessage(SystemMessage.YOUR_TARGET_IS_OUT_OF_RANGE));
			return false;
		}

		useItem(player, 8556, 1);
		((NpcInstance) t).doDie(player);
		return true;
	}

	//DestroyedDarknessFragmentPowder -> DestroyedLightFragmentPowde
	private boolean use13853(Player player, boolean ctrl)
	{
		if(!player.isInZone(ZoneType.mother_tree))
		{
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
			return false;
		}
		useItem(player, 13853, 1);
		Functions.addItem(player, 13854, 1);
		return true;
	}

	//Holy Water for SSQ 2nd quest
	private boolean use13808(Player player, boolean ctrl)
	{
		int[] allowedDoors = { 17240101, 17240105, 17240109 };

		GameObject target = player.getTarget();
		if(player.getDistance(target) > 150)
			return false;
		if(target != null && target.isDoor())
		{
			int _door = ((DoorInstance) target).getDoorId();
			if(ArrayUtils.contains(allowedDoors, _door))
				player.getReflection().openDoor(_door);
			else
			{
				player.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
				return false;
			}
		}
		else
		{
			player.sendPacket(Msg.INVALID_TARGET);
			return false;
		}
		return true;
	}

	//Court Mag Staff for SSQ 2nd quest
	private boolean use13809(Player player, boolean ctrl)
	{
		int[] allowedDoors = { 17240103, 17240107 };

		GameObject target = player.getTarget();
		if(target != null && target.isDoor())
		{
			int _door = ((DoorInstance) target).getDoorId();
			if(ArrayUtils.contains(allowedDoors, _door))
			{
				useItem(player, 13809, 1);
				player.getReflection().openDoor(_door);
				return false;
			}
			else
			{
				player.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
				return false;
			}
		}
		else
		{
			player.sendPacket(Msg.INVALID_TARGET);
			return false;
		}
	}

	private boolean use14835(Player player, boolean ctrl)
	{
		//TODO [G1ta0] добавить кучу других проверок на возможность телепорта
		if(player.isActionsDisabled() || player.isInOlympiadMode() || player.isInZone(ZoneType.no_escape))
		{
			player.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(14835));
			return false;
		}

		useItem(player, 14835, 1);
		//Stakato nest entrance
		player.teleToLocation(89464, -44712, -2167, ReflectionManager.DEFAULT);
		return true;
	}

	//Strongbox of Promise
	private boolean use15537(Player player, boolean ctrl)
	{
		QuestState qs = player.getQuestState(_464_Oath.class);
		if(player.getLevel() >= 82 && qs == null)
		{
			useItem(player, 15537, 1);
			Functions.addItem(player, 15538, 1);
			Quest q = QuestManager.getQuest(464);
			QuestState st = player.getQuestState(q.getClass());
			st = q.newQuestState(player, Quest.CREATED);
			st.setState(Quest.STARTED);
			st.setCond(1);
		}
		else
		{
			player.sendMessage(new CustomMessage("Quest._464_Oath.QuestCannotBeTaken", player));
			return false;
		}
		return true;
	}

	//Wondrous Cubic 1 time use
	private boolean use21106(Player player, boolean ctrl)
	{
		Functions.removeItem(player, 21106, 1);

		int chance = Rnd.get(100);

		if(chance >= 0 && chance < 2)
			Functions.addItem(player, 21593, 1);

		else if(chance >= 2 && chance < 20)
			Functions.addItem(player, 10633, 1);

		else if(chance >= 20 && chance < 37)
			Functions.addItem(player, 10642, 1);

		else if(chance >= 37 && chance < 43)
			Functions.addItem(player, 21096, 1);

		else if(chance >= 43 && chance < 49)
			Functions.addItem(player, 21101, 1);

		else if(chance >= 49 && chance < 55)
			Functions.addItem(player, 10634, 1);

		else if(chance >= 55 && chance < 60)
			Functions.addItem(player, 10643, 1);

		else if(chance >= 60 && chance < 65)
			Functions.addItem(player, 21097, 1);

		else if(chance >= 65 && chance < 70)
			Functions.addItem(player, 21102, 1);

		else if(chance >= 70 && chance < 74)
			Functions.addItem(player, 10635, 1);

		else if(chance >= 74 && chance < 78)
			Functions.addItem(player, 10644, 1);

		else if(chance >= 78 && chance < 82)
			Functions.addItem(player, 21098, 1);

		else if(chance >= 82 && chance < 86)
			Functions.addItem(player, 21103, 1);

		else if(chance >= 86 && chance < 89)
			Functions.addItem(player, 10636, 1);

		else if(chance >= 89 && chance < 92)
			Functions.addItem(player, 10645, 1);

		else if(chance >= 92 && chance < 94)
			Functions.addItem(player, 21099, 1);

		else if(chance >= 94 && chance < 96)
			Functions.addItem(player, 21104, 1);

		else if(chance >= 96 && chance < 97)
			Functions.addItem(player, 10637, 1);

		else if(chance >= 97 && chance < 98)
			Functions.addItem(player, 10646, 1);

		else if(chance >= 98 && chance < 99)
			Functions.addItem(player, 21100, 1);

		else if(chance >= 99)
			Functions.addItem(player, 21105, 1);

		return true;
	}

	private boolean use20630(Player player, boolean ctrl)
	{
		Functions.addItem(player, 20602, 1);
		Functions.addItem(player, 20603, 1);
		return true;
	}

	private boolean use21107(Player player, boolean ctrl)
	{
		Functions.removeItem(player, 21107, 1);

		int chance = Rnd.get(100);

		if(chance >= 0 && chance < 6)
			Functions.addItem(player, 12845, 1);

		else if(chance >= 6 && chance < 12)
			Functions.addItem(player, 12846, 1);

		else if(chance >= 12 && chance < 18)
			Functions.addItem(player, 12847, 1);

		else if(chance >= 18 && chance < 24)
			Functions.addItem(player, 12848, 1);

		else if(chance >= 24 && chance < 29)
			Functions.addItem(player, 12849, 1);

		else if(chance >= 29 && chance < 34)
			Functions.addItem(player, 12850, 1);

		else if(chance >= 34 && chance < 39)
			Functions.addItem(player, 12851, 1);

		else if(chance >= 39 && chance < 44)
			Functions.addItem(player, 14166, 1);

		else if(chance >= 44 && chance < 48)
			Functions.addItem(player, 8738, 1);

		else if(chance >= 48 && chance < 52)
			Functions.addItem(player, 8739, 1);

		else if(chance >= 52 && chance < 56)
			Functions.addItem(player, 8740, 1);

		else if(chance >= 56 && chance < 60)
			Functions.addItem(player, 8741, 1);

		else if(chance >= 60 && chance < 64)
			Functions.addItem(player, 8742, 1);

		else if(chance >= 64 && chance < 68)
			Functions.addItem(player, 9574, 1);

		else if(chance >= 68 && chance < 72)
			Functions.addItem(player, 10484, 1);

		else if(chance >= 72 && chance < 76)
			Functions.addItem(player, 14167, 1);

		else if(chance >= 76 && chance < 78)
			Functions.addItem(player, 8748, 1);

		else if(chance >= 78 && chance < 80)
			Functions.addItem(player, 8749, 1);

		else if(chance >= 80 && chance < 82)
			Functions.addItem(player, 8750, 1);

		else if(chance >= 82 && chance < 84)
			Functions.addItem(player, 8751, 1);

		else if(chance >= 84 && chance < 86)
			Functions.addItem(player, 8752, 1);

		else if(chance >= 86 && chance < 88)
			Functions.addItem(player, 9575, 1);

		else if(chance >= 88 && chance < 90)
			Functions.addItem(player, 10485, 1);

		else if(chance >= 90 && chance < 92)
			Functions.addItem(player, 14168, 1);

		else if(chance >= 92 && chance < 93)
			Functions.addItem(player, 8958, 1);

		else if(chance >= 93 && chance < 94)
			Functions.addItem(player, 8959, 1);

		else if(chance >= 94 && chance < 95)
			Functions.addItem(player, 8960, 1);

		else if(chance >= 95 && chance < 96)
			Functions.addItem(player, 8961, 1);

		else if(chance >= 96 && chance < 97)
			Functions.addItem(player, 8962, 1);

		else if(chance >= 97 && chance < 98)
			Functions.addItem(player, 9576, 1);

		else if(chance >= 98 && chance < 99)
			Functions.addItem(player, 10486, 1);

		else if(chance >= 99)
			Functions.addItem(player, 14169, 1);

		return true;
	}

	//Wondrous Cubic
	private boolean use10632(Player player, boolean ctrl)
	{
		int chance = Rnd.get(100);

		if(chance >= 0 && chance < 2)
			Functions.addItem(player, 21593, 1);

		else if(chance >= 2 && chance < 20)
			Functions.addItem(player, 10633, 1);

		else if(chance >= 20 && chance < 37)
			Functions.addItem(player, 10642, 1);

		else if(chance >= 37 && chance < 43)
			Functions.addItem(player, 21096, 1);

		else if(chance >= 43 && chance < 49)
			Functions.addItem(player, 21101, 1);

		else if(chance >= 49 && chance < 55)
			Functions.addItem(player, 10634, 1);

		else if(chance >= 55 && chance < 60)
			Functions.addItem(player, 10643, 1);

		else if(chance >= 60 && chance < 65)
			Functions.addItem(player, 21097, 1);

		else if(chance >= 65 && chance < 70)
			Functions.addItem(player, 21102, 1);

		else if(chance >= 70 && chance < 74)
			Functions.addItem(player, 10635, 1);

		else if(chance >= 74 && chance < 78)
			Functions.addItem(player, 10644, 1);

		else if(chance >= 78 && chance < 82)
			Functions.addItem(player, 21098, 1);

		else if(chance >= 82 && chance < 86)
			Functions.addItem(player, 21103, 1);

		else if(chance >= 86 && chance < 89)
			Functions.addItem(player, 10636, 1);

		else if(chance >= 89 && chance < 92)
			Functions.addItem(player, 10645, 1);

		else if(chance >= 92 && chance < 94)
			Functions.addItem(player, 21099, 1);

		else if(chance >= 94 && chance < 96)
			Functions.addItem(player, 21104, 1);

		else if(chance >= 96 && chance < 97)
			Functions.addItem(player, 10637, 1);

		else if(chance >= 97 && chance < 98)
			Functions.addItem(player, 10646, 1);

		else if(chance >= 98 && chance < 99)
			Functions.addItem(player, 21100, 1);

		else if(chance >= 99)
			Functions.addItem(player, 21105, 1);

		return true;
	}

	//Totem
	private boolean use21899(Player player, boolean ctrl)
	{
		if(!player.isInZone(AntharasManager.getZone()) && !player.isInZone(ValakasManager.getZone()))
		{
			player.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(21899));
			return false;
		}
		Functions.spawn(Location.findPointToStay(player.getLoc(), 50, 100, player.getGeoIndex()), 143);
		return true;
	}

	//Totem
	private boolean use21900(Player player, boolean ctrl)
	{
		if(!player.isInZone(AntharasManager.getZone()) && !player.isInZone(ValakasManager.getZone()))
		{
			player.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(21900));
			return false;
		}
		Functions.spawn(Location.findPointToStay(player.getLoc(), 50, 100, player.getGeoIndex()), 144);
		return true;
	}

	//Totem
	private boolean use21901(Player player, boolean ctrl)
	{
		if(!player.isInZone(AntharasManager.getZone()) && !player.isInZone(ValakasManager.getZone()))
		{
			player.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(21901));
			return false;
		}
		Functions.spawn(Location.findPointToStay(player.getLoc(), 50, 100, player.getGeoIndex()), 145);
		return true;
	}

	//Totem
	private boolean use21902(Player player, boolean ctrl)
	{
		if(!player.isInZone(AntharasManager.getZone()) && !player.isInZone(ValakasManager.getZone()))
		{
			player.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(21902));
			return false;
		}
		Functions.spawn(Location.findPointToStay(player.getLoc(), 50, 100, player.getGeoIndex()), 146);
		return true;
	}

	// Refined Red Dragon Blood
	private boolean use21903(Player player, boolean ctrl)
	{
		if(!player.isInZone(AntharasManager.getZone()) && !player.isInZone(ValakasManager.getZone()))
		{
			player.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(21903));
			return false;
		}
		player.doCast(SkillTable.getInstance().getInfo(22298, 1), player, false);
		Functions.removeItem(player, 21903, 1);
		return true;
	}

	// Refined Blue Dragon Blood
	private boolean use21904(Player player, boolean ctrl)
	{
		if(!player.isInZone(AntharasManager.getZone()) && !player.isInZone(ValakasManager.getZone()))
		{
			player.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(21904));
			return false;
		}
		player.doCast(SkillTable.getInstance().getInfo(22299, 1), player, false);
		Functions.removeItem(player, 21904, 1);
		return true;
	}

	// Antharas Blood Crystal
	private boolean use17268(Player player, boolean ctrl)
	{
		if(!player.isInZone(AntharasManager.getZone()))
		{
			player.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(17268));
			return false;
		}
		player.doCast(SkillTable.getInstance().getInfo(9179, 1), player, false);
		Functions.removeItem(player, 17268, 1);
		return true;
	}

	private static long useItem(Player player, int itemId, long count)
	{
		player.sendPacket(new SystemMessage(SystemMessage.YOU_USE_S1).addItemName(itemId));
		return Functions.removeItem(player, itemId, count);
	}
}