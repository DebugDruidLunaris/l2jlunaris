package jts.gameserver.network.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.regex.Matcher;

import jts.gameserver.Config;
import jts.gameserver.dao.CharacterDAO;
import jts.gameserver.data.xml.holder.SkillAcquireHolder;
import jts.gameserver.instancemanager.QuestManager;
import jts.gameserver.model.Player;
import jts.gameserver.model.SkillLearn;
import jts.gameserver.model.actor.instances.player.ShortCut;
import jts.gameserver.model.base.AcquireType;
import jts.gameserver.model.base.ClassId;
import jts.gameserver.model.base.Experience;
import jts.gameserver.model.base.Race;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.network.GameClient;
import jts.gameserver.network.serverpackets.CharacterCreateFail;
import jts.gameserver.network.serverpackets.CharacterCreateSuccess;
import jts.gameserver.network.serverpackets.CharacterSelectionInfo;
import jts.gameserver.tables.SkillTable;
import jts.gameserver.templates.PlayerTemplate;
import jts.gameserver.templates.item.CreateItem;
import jts.gameserver.templates.item.ItemTemplate;
import jts.gameserver.utils.ItemFunctions;
import jts.gameserver.utils.Location;
import jts.gameserver.utils.Log_New;
import jts.gameserver.utils.Util;

public class CharacterCreate extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(Config.class);
	private String _name;
	private int _sex;
	private int _classId;
	private int _hairStyle;
	private int _hairColor;
	private int _face;

	@Override
	protected void readImpl()
	{
		_name = readS();
		readD(); // race
		_sex = readD();
		_classId = readD();
		readD(); // int
		readD(); // str
		readD(); // con
		readD(); // men
		readD(); // dex
		readD(); // wit
		_hairStyle = readD();
		_hairColor = readD();
		_face = readD();
	}

	@Override
	//оставляем проверки сервиса смены  лиц и прически пока что нет 
	// когда будет полноконтентным для их количества добавим
	protected void runImpl()
	{
		for(ClassId cid : ClassId.VALUES)
			if(cid.getId() == _classId && cid.getLevel() != 1)
				return;
		if(_face < 0 || _face > 2)
		{
			_log.info("Tried to modify CharacterCreate in face that slot.");
			return;
		}
		if(CharacterDAO.getInstance().accountCharNumber(getClient().getLogin()) >= 8)
		{
			sendPacket(CharacterCreateFail.REASON_TOO_MANY_CHARACTERS);
			return;
		}
		if(!Util.isMatchingRegexp(_name, Config.CNAME_TEMPLATE))
		{
			sendPacket(CharacterCreateFail.REASON_16_ENG_CHARS);
			return;
		}
		else if(CharacterDAO.getInstance().getObjectIdByName(_name) > 0)
		{
			sendPacket(CharacterCreateFail.REASON_NAME_ALREADY_EXISTS);
			return;
		}
		else if(Config.LIST_FORBIDDEN_CHARACTER_NAMES.contains(_name))
		{
			if(Config.FORBIDDEN_CHARACTER_NAMES_DEBUG)
				_log.info("DEBUG: " + getType() + ": charname '" + _name + "' is forbidden.");
			sendPacket(CharacterCreateFail.REASON_NAME_ALREADY_EXISTS);
			return;
		}
		if (Config.LIST_RESTRICTED_CHAR_NAMES.contains(_name))
		{
		sendPacket(CharacterCreateFail.REASON_NAME_ALREADY_EXISTS);
		return;
		}
		// запрещаем использовать знак $ в имени игрока
		_name = Matcher.quoteReplacement(_name);
		
		Player newChar = Player.create(_classId, _sex, getClient().getLogin(), _name, _hairStyle, _hairColor, _face);
		if(newChar == null)
			return;

		sendPacket(CharacterCreateSuccess.STATIC);

		initNewChar(getClient(), newChar);
	}

	private void initNewChar(GameClient client, Player newChar)
	{
		PlayerTemplate template = newChar.getTemplate();

		Player.restoreCharSubClasses(newChar);

			if(Config.CHARACTER_CREATE_START_LVL > 0)
				newChar.addExpAndSp(Experience.LEVEL[Config.CHARACTER_CREATE_START_LVL] - newChar.getExp(), 0);

			if(Config.CHARACTER_CREATE_START_SP > 0)
				newChar.addExpAndSp(0, Config.CHARACTER_CREATE_START_SP);

			if(Config.CHARACTER_CREATE_START_ADENA > 0)
				newChar.addAdena(Config.CHARACTER_CREATE_START_ADENA);

	    if (Config.STARTING_ITEMS)
	    {
	            for (int[] reward : Config.STARTING_ITEMS_ID_QTY)
	            {
	                    ItemInstance startItem = ItemFunctions.createItem(reward[0]);
	                    
	                    if (startItem.isStackable())
	                    {
	                            startItem.setCount(reward[1]);
	                            newChar.getInventory().addItem(startItem);
	                    }
	                    else
	                    {
	                            for (int i = 0; i < reward[1]; ++i)
	                            {
	                                    startItem = ItemFunctions.createItem(reward[0]);
	                                    newChar.getInventory().addItem(startItem);
	                            }
	                    }
	            }
	    }

		if(Config.CHARACTER_CREATE_ALLOW_START_LOC)
		{
			String[] spawn_array = new String[0];
			boolean isMage = newChar.isMageClass();
			Race race = newChar.getRace();
			try
			{
				switch(race)
				{
					case human:
						spawn_array = isMage ? Config.CHARACTER_CREATE_START_LOC_HUMAN_MAGE : Config.CHARACTER_CREATE_START_LOC_HUMAN_FIGHTER;
						break;
					case elf:
						spawn_array = isMage ? Config.CHARACTER_CREATE_START_LOC_ELF_MAGE : Config.CHARACTER_CREATE_START_LOC_ELF_FIGHTER;
						break;
					case darkelf:
						spawn_array = isMage ? Config.CHARACTER_CREATE_START_LOC_DARKELF_MAGE : Config.CHARACTER_CREATE_START_LOC_DARKELF_FIGHTER;
						break;
					case orc:
						spawn_array = isMage ? Config.CHARACTER_CREATE_START_LOC_ORC_MAGE : Config.CHARACTER_CREATE_START_LOC_ORC_FIGHTER;
						break;
					case dwarf:
						spawn_array = Config.CHARACTER_CREATE_START_LOC_DWARF;
						break;
					case kamael:
						spawn_array = Config.CHARACTER_CREATE_START_LOC_KAMAEL;
						break;
					default:
						break;
				}
			}
			catch(Exception e)
			{
				_log.warn("Spawn error at account: " + newChar.getAccountName() + " player: " + newChar.getName(), e);
				_log.error("", e);
			}
			newChar.setLoc(new Location(Integer.parseInt(spawn_array[0]), Integer.parseInt(spawn_array[1]), Integer.parseInt(spawn_array[2])));
		}
		else
		{
			newChar.setLoc(template.spawnLoc);
		}

		if(Config.CHARACTER_CREATE_CHAR_TITLE)
		{
			newChar.setTitle(Config.CHARACTER_CREATE_ADD_CHAR_TITLE);
		}
		else
		{
			newChar.setTitle("");
		}

		for(CreateItem i : template.getItems())
		{
			ItemInstance item = ItemFunctions.createItem(i.getItemId());
			newChar.getInventory().addItem(item);

			if(item.getItemId() == 5588) // tutorial book
				newChar.registerShortCut(new ShortCut(11, 0, ShortCut.TYPE_ITEM, item.getObjectId(), -1, 1));

			if(item.isEquipable() && (newChar.getActiveWeaponItem() == null || item.getTemplate().getType2() != ItemTemplate.TYPE2_WEAPON))
				newChar.getInventory().equipItem(item);
		}

		// Adventurer's Scroll of Escape
		ItemInstance item = ItemFunctions.createItem(10650);
		item.setCount(5);
		newChar.getInventory().addItem(item);

		// Scroll of Escape: Kamael Village
		item = ItemFunctions.createItem(9716);
		item.setCount(10);
		newChar.getInventory().addItem(item);

		for(SkillLearn skill : SkillAcquireHolder.getInstance().getAvailableSkills(newChar, AcquireType.NORMAL))
			newChar.addSkill(SkillTable.getInstance().getInfo(skill.getId(), skill.getLevel()), true);

		if(newChar.getSkillLevel(1001) > 0) // Soul Cry
			newChar.registerShortCut(new ShortCut(1, 0, ShortCut.TYPE_SKILL, 1001, 1, 1));
		if(newChar.getSkillLevel(1177) > 0) // Wind Strike
			newChar.registerShortCut(new ShortCut(1, 0, ShortCut.TYPE_SKILL, 1177, 1, 1));
		if(newChar.getSkillLevel(1216) > 0) // Self Heal
			newChar.registerShortCut(new ShortCut(2, 0, ShortCut.TYPE_SKILL, 1216, 1, 1));

		// add attack, take, sit shortcut
		newChar.registerShortCut(new ShortCut(0, 0, ShortCut.TYPE_ACTION, 2, -1, 1));
		newChar.registerShortCut(new ShortCut(3, 0, ShortCut.TYPE_ACTION, 5, -1, 1));
		newChar.registerShortCut(new ShortCut(10, 0, ShortCut.TYPE_ACTION, 0, -1, 1));
		// Ð¿Ð¾Ð½Ñ�Ð» ÐºÐ°Ðº Ð½Ð° Ð¿Ð°Ð½ÐµÐ»ÑŒÐºÐµ Ð¾Ñ‚Ð¾Ð±Ñ€Ð°Ð·Ð¸Ñ‚ÑŒ. Ð½Ñ† Ñ�Ð¾Ñ„Ñ‚ 10-11 Ð¿Ð°Ð½ÐµÐ»Ð¸ Ñ�Ð´ÐµÐ»Ð°Ð»Ð¸(by VISTALL)
		// fly transform
		newChar.registerShortCut(new ShortCut(0, ShortCut.PAGE_FLY_TRANSFORM, ShortCut.TYPE_SKILL, 911, 1, 1));
		newChar.registerShortCut(new ShortCut(3, ShortCut.PAGE_FLY_TRANSFORM, ShortCut.TYPE_SKILL, 884, 1, 1));
		newChar.registerShortCut(new ShortCut(4, ShortCut.PAGE_FLY_TRANSFORM, ShortCut.TYPE_SKILL, 885, 1, 1));
		// air ship
		newChar.registerShortCut(new ShortCut(0, ShortCut.PAGE_AIRSHIP, ShortCut.TYPE_ACTION, 70, 0, 1));

		startTutorialQuest(newChar);
		
		newChar.setCurrentHpMp(newChar.getMaxHp(), newChar.getMaxMp());
		newChar.setCurrentCp(0); // retail
		newChar.setOnlineStatus(false);

		newChar.store(false);
		newChar.getInventory().store();
		newChar.deleteMe();
		new File("./log/chars/" + newChar.getName() + "/").mkdir();
		Log_New.LogEvent(newChar.getName(), newChar.getIP(), "charCreated", new String[] { "created" });
		client.setCharSelection(CharacterSelectionInfo.loadCharacterSelectInfo(client.getLogin()));
	}

	public static void startTutorialQuest(Player player)
	{
		Quest q = QuestManager.getQuest(255);
		if(q != null)
			q.newQuestState(player, Quest.CREATED);
	}
}