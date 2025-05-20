package jts.gameserver;

import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastTable;
import jts.commons.configuration.ExProperties;
import jts.commons.net.nio.impl.SelectorConfig;
import jts.commons.versioning.Version;
import jts.gameserver.data.htm.HtmCache;
import jts.gameserver.loginservercon.ServerType;
import jts.gameserver.model.actor.instances.player.Bonus;
import jts.gameserver.model.base.Experience;
import jts.gameserver.model.base.PlayerAccess;
import jts.gameserver.skills.AbnormalEffect;
import jts.gameserver.utils.GArray;
import jts.gameserver.utils.HWID.HWIDComparator;
import jts.gameserver.utils.Location;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class Config
{
	private static final Logger _log = LoggerFactory.getLogger(Config.class);

	public static final int NCPUS = Runtime.getRuntime().availableProcessors();

	/** Configuration files */
	public static final String OTHER_CONFIG_FILE = "config/other.ini";
	public static final String RESIDENCE_CONFIG_FILE = "config/residence.ini";
	public static final String SPOIL_CONFIG_FILE = "config/spoil.ini";
	public static final String ALT_SETTINGS_FILE = "config/altsettings.ini";
	public static final String FORMULAS_CONFIGURATION_FILE = "config/formulas.ini";
	public static final String PVP_CONFIG_FILE = "config/pvp.ini";
	public static final String TELNET_CONFIGURATION_FILE = "config/telnet.ini";
	public static final String CONFIGURATION_FILE = "config/gameserver.ini";
	public static final String AI_CONFIG_FILE = "config/ai.ini";
	public static final String GATEKEEPER_CONFIG_FILE = "config/gatekeeper.ini";
	public static final String GEODATA_CONFIG_FILE = "config/geodata.ini";
	public static final String EXT_FILE = "config/ext.ini";
	public static final String TOP_FILE = "config/top.ini";
	public static final String OLYMPIAD_DATA_FILE = "config/olympiad.ini";
	public static final String ANUSEWORDS_CONFIG_FILE = "config/abusewords.txt";
	public static final String GM_PERSONAL_ACCESS_FILE = "config/GMAccess.xml";
	public static final String GM_ACCESS_FILES_DIR = "config/GMAccess.d/";
	public static final String COMMUNITY_BOARD_WAREHOUSE_CONFIG_FILE = "config/communityboard/cb_warehouse.ini";
	public static final String COMMUNITY_BOARD_BUFFER_CONFIG_FILE = "config/communityboard/cb_buffer.ini";
	public static final String COMMUNITY_BOARD_CLASS_MASTER_CONFIG_FILE = "config/communityboard/cb_classmaster.ini";
	public static final String COMMUNITY_BOARD_CHECK_CONDITION_CONFIG_FILE = "config/communityboard/cb_condition.ini";
	public static final String COMMUNITY_BOARD_COMMISSION_CONFIG_FILE = "config/communityboard/cb_commission.ini";
	public static final String COMMUNITY_BOARD_ENCHANT_CONFIG_FILE = "config/communityboard/cb_forge.ini";
	public static final String COMMUNITY_BOARD_GLOBAL_CONFIG_FILE = "config/communityboard/cb_global.ini";
	public static final String COMMUNITY_BOARD_SERVICES_CONFIG_FILE = "config/communityboard/cb_services.ini";
	public static final String COMMUNITY_BOARD_NEWS_CONFIG_FILE = "config/communityboard/cb_news.ini";
	public static final String COMMUNITY_BOARD_STATS_CONFIG_FILE = "config/communityboard/cb_stats.ini";
	public static final String COMMUNITY_BOARD_TELEPORT_CONFIG_FILE = "config/communityboard/cb_teleport.ini";
	public static final String COMMUNITY_BOARD_GAME_LOTTERY_CONFIG_FILE = "config/communityboard/cb_lottery.ini";
	public static final String COMMUNITY_BOARD_ACADEM_CONFIG_FILE = "config/communityboard/cb_academ.ini";
	public static final String NPCS_CONFIG_FILE = "config/Npcs.ini";
	public static final String RATE_CONFIG_FILE = "config/Rate.ini";
	public static final String HELLBOUND_CONFIG_FILE = "config/HellBound.ini";
	public static final String RATE_QUEST_CONFIG_FILE = "config/quests.ini";
	public static final String ITEMS_CONFIG_FILE = "config/items.ini";
    public static final String PHANTOM_FILE = "config/Phantoms.ini";
	public static final String SERVICE_BASH_CONFIG_FILE = "config/services/Bash.ini";
	public static final String SERVICE_ENTER_WORLD_CONFIG_FILE = "config/services/EnterWorld.ini";
	public static final String SERVICE_CHARACTER_CONFIG_FILE = "config/services/Character.ini";
	public static final String SERVICE_CHARACTER_CREATE_CONFIG_FILE = "config/services/CharacterCreate.ini";
	public static final String ENCHANT_CONFIG_FILE = "config/services/Enchant.ini";
	public static final String ITEM_MALL_CONFIG_FILE = "config/item-mall.ini";
	public static final String SERVICE_CLAN_CONFIG_FILE = "config/services/Clan.ini";
	public static final String SERVICE_OFFTRADE_CONFIG_FILE = "config/services/OffTrade.ini";
	public static final String SERVICE_OTHER_CONFIG_FILE = "config/services/Other.ini";
	public static final String SERVICE_SECURITY_CONFIG_FILE = "config/services/Security.ini";
	public static final String SERVICE_WEDDING_CONFIG_FILE = "config/services/Wedding.ini";
	public static final String SERVICE_BONUS_CONFIG_FILE = "config/services/BonusService.ini";
	private static final String EVENT_APRIL_FOOLS_FILE = "config/events/AprilFools.ini";
	private static final String EVENT_BOUNTY_HUNTERS_FILE = "config/events/BountyHunters.ini";
	private static final String EVENT_CAPTURE_THE_FLAG_FILE = "config/events/CaptureTheFlag.ini";
	private static final String EVENT_CHANGE_OF_HEART_FILE = "config/events/ChangeOfHeart.ini";
	private static final String EVENT_COFFER_OF_SHADOWS_FILE = "config/events/CofferOfShadows.ini";
	private static final String EVENT_FIGHT_CLUB_FILE = "config/events/FightClub.ini";
	private static final String EVENT_GLITTERING_MEDAL_FILE = "config/events/GlitteringMedal.ini";
	private static final String EVENT_TREASURES_OF_THE_HERALD_FILE = "config/events/TreasuresOfTheHerald.ini";
	private static final String DEFENSE_TOWNS_CONFIG_FILE= "config/events/DefenseTowns.ini";
	private static final String EVENT_HITMAN_FILE = "config/events/Hitman.ini";
	private static final String EVENT_L2_DAY_FILE = "config/events/L2Day.ini";
	private static final String EVENT_LAST_HERO_FILE = "config/events/LastHero.ini";
	private static final String EVENT_MARCH_8_FILE = "config/events/March8.ini";
	private static final String EVENT_MASTER_OF_ENCHANING_FILE = "config/events/MasterOfEnchaning.ini";
	private static final String EVENT_PC_BANG_FILE = "config/events/PcBang.ini";
	private static final String EVENT_SAVING_SNOWMAN_FILE = "config/events/SavingSnowman.ini";
	private static final String EVENT_DEATH_MATCH_FILE = "config/events/DeathMatch.ini";
	private static final String EVENT_THE_FALL_HARVEST_FILE = "config/events/TheFallHarvest.ini";
	private static final String EVENT_TRICK_OF_TRANSMUTATION_FILE = "config/events/TrickOfTransmutation.ini";
	private static final String EVENT_L2COIN = "config/events/L2Coin.ini";
	private static final String EVENT_SIMPLE = "config/events/Simple.ini";
	private static final String EVENT_UNDERGROUND_COLISEUM_FILE = "config/events/UndergroundColiseum.ini";
	private static final String EVENT_TVT_ARENA_FILE = "config/events/TVTArena.ini";
	private static final String EVENT_GVG_FILE = "config/events/GVG.ini";
	private static final String EVENT_TVT_FILE = "config/events/TVT.ini";
	private static final String CHAT_FILE_CONFIG = "config/chat.ini";
	private static final String INSTANCE_FILE_CONFIG = "config/Instances.ini";
	private static final String BOSS_FILE_CONFIG = "config/boss.ini";
	private static final String SKILL_FILE_CONFIG = "config/skill.ini";
	public static final String VERSION_FILE = "config/l2d-version.ini";
	public static final String LIC = "config/license.ini";
	public static final String BUFF_STORE_CONFIG_FILE = "config/services/OfflineBuffer.ini";

	public static int FIXINTERVALOFSAILRENSPAWN_HOUR;
	public static int RANDOMINTERVALOFSAILRENSPAWN;
	public static int FIXINTERVALOFBAIUM_HOUR;
	public static int RANDOMINTERVALOFBAIUM;
	public static int FIXINTERVALSLEEPBAIUM;
	public static int FIXINTERVALSLEEPVALACAS;
	public static int FIXINTERVALSLEEPANTHARAS;
	public static int FIXINTERVALOFBELETHSPAWN_HOUR;
	public static int FIXINTERVALOFBAYLORSPAWN_HOUR;
	public static int RANDOMINTERVALOFBAYLORSPAWN;
	public static boolean SKILL_CHANCE_ENABLE_ON;
	public static boolean SKILL_CHANCE_ENABLE;
	public static boolean ServicesUnBan;
	public static boolean ServicesUnBanAcc;
	public static int ServicesUnBanAccItem;
	public static int ServicesUnBanAccCount;
	public static boolean ServicesUnBanChar;
	public static int ServicesUnBanCharItem;
	public static int ServicesUnBanCharCount;
	public static boolean ServicesUnBanChat;
	public static int ServicesUnBanChatItem;
	public static int ServicesUnBanChatCount;
	public static boolean DONT_DESTROY_ARROWS;
	public static boolean DISABLE_GRADE_PENALTY;
	public static boolean ALT_DISPEL_MUSIC;
	/*Event GVG*/
    public static boolean EVENT_GvGDisableEffect;
    public static String OWNER_NAME;
    public static int LICENSE_KEY = -1;
	/* Alt Settings*/
	public static boolean ALT_ARENA_EXP;
	public static boolean ALT_GAME_DELEVEL;
	public static boolean ALT_SAVE_UNSAVEABLE;
	public static int ALT_SAVE_EFFECTS_REMAINING_TIME;
	public static boolean ALT_SHOW_REUSE_MSG;
	public static boolean ALT_DELETE_SA_BUFFS;
	public static boolean ALT_AUTO_LOOT;
	public static boolean ALT_AUTO_LOOT_HERBS;
	public static boolean ALT_AUTO_LOOT_INDIVIDUAL;
	public static boolean ALT_AUTO_LOOT_FROM_RAIDS;
	public static boolean ALT_AUTO_LOOT_PK;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_SHOP;
	public static boolean ALT_SAVING_SPS;
	public static boolean ALT_MANAHEAL_SPS_BONUS;
	public static double ALT_CRAFT_MASTERWORK_CHANCE;
	public static double ALT_CRAFT_DOUBLECRAFT_CHANCE;
	public static double ALT_RAID_RESPAWN_MULTIPLIER;
	public static boolean ALT_ALLOW_AUGMENT_ALL;
	public static boolean ALT_ALLOW_DROP_AUGMENTED;
	public static boolean ALT_GAME_UNREGISTER_RECIPE;
	public static boolean ALT_GAME_SHOW_DROPLIST;
	public static boolean ALT_ALLOW_NPC_SHIFTCLICK;
	public static boolean ALT_FULL_NPC_STATS_PAGE;
	public static boolean ALT_GAME_SUBCLASS_WITHOUT_QUESTS;
	public static int ALT_GAME_LEVEL_TO_GET_SUBCLASS;
	public static int ALT_GAME_SUB_ADD;
	public static int ALT_MAX_LEVEL;
	public static int ALT_MAX_SUB_LEVEL;
	public static boolean ALT_ALLOW_OTHERS_WITHDRAW_FROM_CLAN_WAREHOUSE;
	public static boolean ALT_ALLOW_CLAN_COMMAND_ONLY_FOR_CLAN_LEADER;
	public static boolean ALT_GAME_REQUIRE_CLAN_CASTLE;
	public static boolean ALT_GAME_REQUIRE_CASTLE_DAWN;
	public static boolean ALT_GAME_ALLOW_ADENA_DAWN;
	public static int ALT_ADD_RECIPES;
	public static int ALT_SS_ANNOUNCE_PERIOD;
	public static boolean ALT_PETITIONING_ALLOWED;
	public static int ALT_MAX_PETITIONS_PER_PLAYER;
	public static int ALT_MAX_PETITIONS_PENDING;
	public static boolean ALT_AUTO_LEARN_SKILLS;
	public static boolean ALT_AUTO_LEARN_FORGOTTEN_SKILLS;
	public static boolean ALT_SOCIAL_ACTION_REUSE;
	public static boolean ALT_DISABLE_SPELLBOOKS;
	public static boolean ALT_SIMPLE_SIGNS;
	public static boolean ALT_TELE_TO_CATACOMBS;
	public static boolean ALT_BS_CRYSTALLIZE;
	public static int ALT_MAMMON_UPGRADE;
	public static int ALT_MAMMON_EXCHANGE;
	public static boolean ALT_ALLOW_TATTOO;
	public static int ALT_BUFF_LIMIT;
	public static int ALT_SONG_LIMIT;
	public static boolean ALT_DEATH_PENALTY;
	public static boolean ALT_ALLOW_DEATH_PENALTY_C5;
	public static int ALT_DEATH_PENALTY_C5_CHANCE;
	public static boolean ALT_DEATH_PENALTY_C5_CHAOTIC_RECOVERY;
	public static int ALT_DEATH_PENALTY_C5_EXPERIENCE_PENALTY;
	public static int ALT_DEATH_PENALTY_C5_KARMA_PENALTY;
	public static double ALT_PK_DEATH_RATE;
	public static long ALT_NONOWNER_ITEM_PICKUP_DELAY;
	public static boolean ALT_NO_LASTHIT;
	public static boolean ALT_BUFF_SUMMON;
	public static boolean ALT_KAMALOKA_NIGHTMARES_PREMIUM_ONLY;
	public static boolean ALT_KAMALOKA_NIGHTMARE_REENTER;
	public static boolean ALT_KAMALOKA_ABYSS_REENTER;
	public static boolean ALT_KAMALOKA_LAB_REENTER;
	public static boolean ALT_PET_HEAL_BATTLE_ONLY;
	public static boolean ALT_ALLOW_SELL_COMMON;
	public static boolean ALT_ALLOW_SHADOW_WEAPONS;
	public static int[] ALT_DISABLED_MULTISELL;
	public static int[] ALT_SHOP_PRICE_LIMITS;
	public static int[] ALT_SHOP_UNALLOWED_ITEMS;
	public static int[] ALT_ALLOWED_PET_POTIONS;
	public static int ALT_FESTIVAL_MIN_PARTY_SIZE;
	public static double ALT_FESTIVAL_RATE_PRICE;
	public static int ALT_RIFT_MIN_PARTY_SIZE;
	public static int ALT_RIFT_SPAWN_DELAY;
	public static int ALT_RIFT_MAX_JUMPS;
	public static int ALT_RIFT_AUTO_JUMPS_TIME;
	public static int ALT_RIFT_AUTO_JUMPS_TIME_RAND;
	public static int ALT_RIFT_ENTER_COST_RECRUIT;
	public static int ALT_RIFT_ENTER_COST_SOLDIER;
	public static int ALT_RIFT_ENTER_COST_OFFICER;
	public static int ALT_RIFT_ENTER_COST_CAPTAIN;
	public static int ALT_RIFT_ENTER_COST_COMMANDER;
	public static int ALT_RIFT_ENTER_COST_HERO;
	public static boolean ALT_ALLOW_CLANSKILLS;
    public static boolean ALT_NOBLESSE_BLESSING;
	public static boolean ALT_ALLOW_LEARN_TRANS_SKILLS_WO_QUEST;
	public static boolean ALT_PARTY_LEADER_ONLY_CAN_INVITE;
	public static boolean ALT_ALLOW_NOBLE_TP_TO_ALL;
	public static double ALT_CLANHALL_BUFFTIME_MODIFIER;
	public static double ALT_SONGDANCETIME_MODIFIER;
	public static double ALT_MAXLOAD_MODIFIER;
	public static boolean ALT_IMPROVED_PETS_LIMITED_USE;
	public static double ALT_CHAMPION_CHANCE_RED;
	public static double ALT_CHAMPION_CHANCE_BLUE;
	public static boolean ALT_CHAMPION_CAN_BE_AGGRO;
	public static boolean ALT_CHAMPION_CAN_BE_SOCIAL;
	public static int ALT_CHAMPION_TOP_LEVEL;
	public static boolean ALT_VITALITY_ENABLED;
	public static double ALT_VITALITY_RATE;
	public static double ALT_VITALITY_CONSUME_RATE;
	public static int ALT_VITALITY_RAID_BONUS;
	public static boolean ALT_DEBUG_ENABLED;
	public static boolean ALT_DEBUG_PVP_ENABLED;
	public static boolean ALT_DEBUG_PVP_DUEL_ONLY;
	public static boolean ALT_DEBUG_PVE_ENABLED;
	public static int ALT_MAX_ALLY_SIZE;
	public static boolean ENABLE_KM_ALL_TO_ME;
	public static int ALT_PARTY_DISTRIBUTION_RANGE;
	public static double[] ALT_PARTY_BONUS;
	public static boolean ALT_ALL_PHYS_SKILLS_OVERHIT;
	public static boolean ALT_REMOVE_SKILLS_ON_DELEVEL;
	public static boolean ALT_USE_BOW_REUSE_MODIFIER;
	public static boolean ALT_ALLOW_CH_DOOR_OPEN_ON_CLICK;
	public static boolean ALT_CH_ALL_BUFFS;
	public static boolean ALT_CH_ALLOW_1H_BUFFS;
	public static boolean ALT_CH_SIMPLE_DIALOG;
	public static int ALT_AUGMENTATION_NG_SKILL_CHANCE;
	public static int ALT_AUGMENTATION_NG_GLOW_CHANCE;
	public static int ALT_AUGMENTATION_MID_SKILL_CHANCE;
	public static int ALT_AUGMENTATION_MID_GLOW_CHANCE;
	public static int ALT_AUGMENTATION_HIGH_SKILL_CHANCE;
	public static int ALT_AUGMENTATION_HIGH_GLOW_CHANCE;
	public static int ALT_AUGMENTATION_TOP_SKILL_CHANCE;
	public static int ALT_AUGMENTATION_TOP_GLOW_CHANCE;
	public static int ALT_AUGMENTATION_BASESTAT_CHANCE;
	public static int ALT_AUGMENTATION_ACC_SKILL_CHANCE;
	public static boolean ALT_OPEN_CLOAK_SLOT;
	public static boolean ALT_SHOW_SERVER_TIME;
	public static int ALT_FOLLOW_RANGE;
	public static boolean ALT_ITEM_AUCTION_ENABLED;
	public static boolean ALT_ITEM_AUCTION_CAN_REBID;
	public static boolean ALT_ITEM_AUCTION_START_ANNOUNCE;
	public static int ALT_ITEM_AUCTION_BID_ITEM_ID;
	public static long ALT_ITEM_AUCTION_MAX_BID;
	public static int ALT_ITEM_AUCTION_MAX_CANCEL_TIME_IN_MILLIS;
	public static boolean ALT_FISH_CHAMPIONSHIP_ENABLED;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_ITEM;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_1;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_2;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_3;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_4;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_5;
	public static boolean ALT_ENABLE_BLOCK_CHECKER_EVENT;
	public static int ALT_MIN_BLOCK_CHECKER_TEAM_MEMBERS;
	public static double ALT_RATE_COINS_REWARD_BLOCK_CHECKER;
	public static boolean ALT_HBCE_FAIR_PLAY;
	public static int ALT_PET_INVENTORY_LIMIT;
	public static int ALT_MIN_ADENA_TO_EAT;
	public static int ALT_TIME_IF_NOT_FEED;
	public static int ALT_INTERVAL_EATING;
	public static int VITAMIN_PETS_FOOD_ID;
	public static int ALT_LETHAL1_BY_HP;
	public static boolean ALT_DELETE_TRANSFORMATION_ON_DEATH;
	public static boolean ALT_GAME_CREATION;
	public static double ALT_GAME_CREATION_RARE_XPSP_RATE;
	public static double ALT_GAME_CREATION_XP_RATE;
	public static double ALT_GAME_CREATION_SP_RATE;
    public static int DRAGON_MIGRATION_PERIOD;
    public static int DRAGON_MIGRATION_CHANCE;
    public static int ANCIENT_HERB_SPAWN_RADIUS;
    public static int ANCIENT_HERB_SPAWN_CHANCE;
    public static int ANCIENT_HERB_SPAWN_COUNT;
    public static int ANCIENT_HERB_RESPAWN_TIME;
    public static int ANCIENT_HERB_DESPAWN_TIME;
    public static List<Location> HEIN_FIELDS_LOCATIONS = new ArrayList<>();
	public static boolean ENABLE_AUTO_HUNTING_REPORT;
    /* CharacterCreate */
	public static boolean CHARACTER_CREATE_CHAR_TITLE;
	public static String CHARACTER_CREATE_ADD_CHAR_TITLE;
	public static long CHARACTER_CREATE_START_ADENA;
	public static int CHARACTER_CREATE_START_LVL;
	public static long CHARACTER_CREATE_START_SP;
	public static boolean CHARACTER_CREATE_ALLOW_START_LOC;
	public static String[] CHARACTER_CREATE_START_LOC_HUMAN_FIGHTER;
	public static String[] CHARACTER_CREATE_START_LOC_HUMAN_MAGE;
	public static String[] CHARACTER_CREATE_START_LOC_ELF_FIGHTER;
	public static String[] CHARACTER_CREATE_START_LOC_ELF_MAGE;
	public static String[] CHARACTER_CREATE_START_LOC_DARKELF_FIGHTER;
	public static String[] CHARACTER_CREATE_START_LOC_DARKELF_MAGE;
	public static String[] CHARACTER_CREATE_START_LOC_ORC_FIGHTER;
	public static String[] CHARACTER_CREATE_START_LOC_ORC_MAGE;
	public static String[] CHARACTER_CREATE_START_LOC_DWARF;
	public static String[] CHARACTER_CREATE_START_LOC_KAMAEL;
	public static String DELAYED_SPAWN_OFF;
	public static int DELAYED_SPAWN_MIN_COUNT;
	public static int DELAYED_SPAWN_MAX_COUNT;
	public static long DELAYED_SPAWN_TIMEOUT;
	/* Formulas */
	public static boolean SKILLS_CHANCE_SHOW;
	public static double FORMULA_SKILLS_CHANCE_MOD;
	public static double FORMULA_SKILLS_CHANCE_MIN;
	public static double FORMULA_SKILLS_CHANCE_POW;
	public static double FORMULA_SKILLS_CHANCE_CAP;
	public static int FORMULA_SKILLS_CAST_TIME_MIN;
	public static double FORMULA_ABSORB_DAMAGE_MODIFIER;
	public static int FORMULA_LIM_PATK;
	public static int FORMULA_LIM_MATK;
	public static int FORMULA_LIM_PDEF;
	public static int FORMULA_LIM_MDEF;
	public static int FORMULA_LIM_MATK_SPD;
	public static int FORMULA_LIM_PATK_SPD;
	public static int FORMULA_LIM_CRIT_DAM;
	public static int FORMULA_LIM_CRIT;
	public static int FORMULA_LIM_MCRIT;
	public static int FORMULA_LIM_ACCURACY;
	public static int FORMULA_LIM_EVASION;
	public static int FORMULA_LIM_MOVE;
	public static int FORMULA_LIM_FAME;
	public static double FORMULA_POLE_DAMAGE_MODIFIER;
    public static boolean AUTOCP_SKILL;
    public static int AUTOCP_SKILL_TICK;
	public static int HTM_CACHE_MODE;

	public static double ALT_VITALITY_NEVIT_UP_POINT;
	public static double ALT_VITALITY_NEVIT_POINT;
	public static boolean ENABLE_FLAG_ATTACK_MOB;
	public static GArray<Integer> FLAG_MOB_LIST;
	public static int OTHER_ITEM_MALL_MAX_BUY_COUNT;
	public static boolean USE_OFFLIKE_ENCHANT;
	public static boolean USE_OFFLIKE_ENCHANT_MAGE_WEAPON;
	public static double USE_OFFLIKE_ENCHANT_MAGE_WEAPON_CHANCE;
	public static int[] OFFLIKE_ENCHANT_WEAPON;
	public static int[] OFFLIKE_ENCHANT_WEAPON_BLESSED;
	public static int[] OFFLIKE_ENCHANT_WEAPON_CRYSTAL;
	public static int[] OFFLIKE_ENCHANT_ARMOR;
	public static int[] OFFLIKE_ENCHANT_ARMOR_CRYSTAL;
	public static int[] OFFLIKE_ENCHANT_ARMOR_BLESSED;
	public static int[] OFFLIKE_ENCHANT_ARMOR_JEWELRY;
	public static int[] OFFLIKE_ENCHANT_ARMOR_JEWELRY_CRYSTAL;
	public static int[] OFFLIKE_ENCHANT_ARMOR_JEWELRY_BLESSED;

	public static int[] OFFLIKE_PREMIUM_ENCHANT_WEAPON;
	public static int[] OFFLIKE_PREMIUM_ENCHANT_WEAPON_BLESSED;
	public static int[] OFFLIKE_PREMIUM_ENCHANT_WEAPON_CRYSTAL;
	public static int[] OFFLIKE_PREMIUM_ENCHANT_ARMOR;
	public static int[] OFFLIKE_PREMIUM_ENCHANT_ARMOR_CRYSTAL;
	public static int[] OFFLIKE_PREMIUM_ENCHANT_ARMOR_BLESSED;
	public static int[] OFFLIKE_PREMIUM_ENCHANT_ARMOR_JEWELRY;
	public static int[] OFFLIKE_PREMIUM_ENCHANT_ARMOR_JEWELRY_CRYSTAL;
	public static int[] OFFLIKE_PREMIUM_ENCHANT_ARMOR_JEWELRY_BLESSED;

	public static boolean ALLOW_BBS_WAREHOUSE;
	public static boolean BBS_WAREHOUSE_ALLOW_PEACE_ZONE;
	public static boolean BBS_WAREHOUSE_ALLOW_PK;
	public static boolean ALLOW_BBS_MAMMON;
	public static boolean ALLOW_BBS_WIKI;
	public static boolean BBS_MAMMON_ALLOW_PEACE_ZONE;
	public static boolean BBS_BUFFER_ALLOWED_BUFFER;
	public static boolean BBS_BUFFER_ALLOWED_PK;
	public static boolean BBS_BUFFER_ALLOWED_CURSED_WEAPON;
	public static boolean CHECK_DEATH_TIME;
	public static int CHECK_DEATH_TIME_VAL;
	
	public static int BBS_BUFFER_PRICE_ID;
	public static int BBS_BUFFER_PRICE_ONE;
	public static int BBS_BUFFER_SAVE_PRICE_ID;
	public static int BBS_BUFFER_SAVE_PRICE_ONE;
	public static int BBS_BUFFER_ALT_TIME;
	public static int BBS_BUFFER_MIN_LVL;
	public static int BBS_BUFFER_MAX_LVL;
	public static int CBB_BUFFER_FREE_LEVEL;
	public static int[] BBS_BUFFER_ALLOWED_BUFF;
    public static boolean BBS_BUFFER_RECOVER_HP_MP_CP;
	public static boolean BBS_BUFFER_CLEAR_BUFF;
	public static String COMMUNITYBOARD_NAME;
	public static String COMMUNITYBOARD_COPY;
	public static String COMMUNITYBOARD_SERVER_ADMIN_NAME;
	public static String COMMUNITYBOARD_SERVER_SUPPORT_NAME;
	public static String COMMUNITYBOARD_SERVER_GM_NAME;
	public static String COMMUNITYBOARD_FORUM_ADMIN_NAME;
	public static int BBS_WASH_SINS_PRICE;
	public static int BBS_WASH_SINS_PRICE_ITEM_ID;
	public static int BBS_VIP_SECTION_PRICE;
	public static int BBS_VIP_SECTION_ITEM_ID;
	public static int BBS_CLEAR_PK_PRICE;
	public static int BBS_CLEAR_PK_PRICE_ITEM_ID;
	public static int BBS_CLEAR_PK_COUNT;
	public static int CBB_ONLINE_CHEAT_COUNT;
	public static int CBB_OFFTRADE_CHEAT_COUNT;
	public static int BBS_NEWS_UPDATE_TIME;

	public static boolean BBS_CLASS_MASTER_ALLOW;
	public static List<Integer> BBS_CLASS_MASTERS_ALLOW_LIST = new ArrayList<>();
	public static int[] BBS_CLASS_MASTER_PRICE_ITEM;
	public static int[] BBS_CLASS_MASTER_PRICE_COUNT;
	public static int BBS_CLASS_MASTER_SUB_PRICE_ITEM;
	public static boolean BBS_CLASS_MASTER_BUY_NOBLESSE;
	public static boolean BBS_CLASS_MASTER_ADD_SUB_CLASS;
	public static long BBS_CLASS_MASTER_SUB_ADD_PRICE_COUNT;
	public static boolean BBS_CLASS_MASTER_CHANGE_SUB_CLASS;
	public static long BBS_CLASS_MASTER_SUB_CHANGE_PRICE_COUNT;
	public static boolean BBS_CLASS_MASTER_CANCEL_SUB_CLASS;
	public static long BBS_CLASS_MASTER_SUB_CANCEL_PRICE_COUNT;

	public static boolean BBS_CHECK_IN_COMBAT;
	public static boolean BBS_CHECK_DEATH;
	public static boolean BBS_CHECK_MOVEMENT_DISABLE;
	public static boolean BBS_CHECK_ON_SIEGE_FIELD;
	public static boolean BBS_CHECK_ATTACKING_NOW;
	public static boolean BBS_CHECK_IN_OLYMPIAD_MODE;
	public static boolean BBS_CHECK_FLYING;
	public static boolean BBS_CHECK_IN_DUEL;
	public static boolean BBS_CHECK_IN_INSTANCE;
	public static boolean BBS_CHECK_IN_JAILED;
	public static boolean BBS_CHECK_OUT_OF_CONTROL;
	public static boolean BBS_CHECK_OUT_OF_TOWN_ONLY_FOR_PREMIUM;
	public static boolean BBS_CHECK_IN_EVENT;

	public static boolean BBS_COMMISSION_ALLOW;
	public static int[] BBS_COMMISSION_ARMOR_PRICE;
	public static int[] BBS_COMMISSION_WEAPON_PRICE;
	public static int[] BBS_COMMISSION_JEWERLY_PRICE;
	public static int[] BBS_COMMISSION_OTHER_PRICE;
	public static int[] BBS_COMMISSION_ALLOW_ITEMS;
	public static int BBS_COMMISSION_MAX_ENCHANT;
	public static int[] BBS_COMMISSION_NOT_ALLOW_ITEMS;
	public static boolean BBS_COMMISSION_ALLOW_PVP;
	public static boolean BBS_COMMISSION_HIDE_OLD_ITEMS;
	public static int BBS_COMMISSION_HIDE_OLD_AFTER;
	public static boolean BBS_COMMISSION_ALLOW_EQUIPPED;
	public static boolean BBS_COMMISSION_ALLOW_UNDERWEAR;
	public static boolean BBS_COMMISSION_ALLOW_CLOAK;
	public static boolean BBS_COMMISSION_ALLOW_BRACELET;
	public static boolean BBS_COMMISSION_ALLOW_AUGMENTED;
	public static int BBS_COMMISSION_COUNT_TO_PAGE;
	public static int[] BBS_COMMISSION_ITEMS;
	public static int BBS_COMMISSION_MAIL_TIME;

	public static int BBS_ENCHANT_ITEM;
	public static int[] BBS_ENCHANT_MAX;
	public static int[] BBS_WEAPON_ENCHANT_LVL;
	public static int[] BBS_ARMOR_ENCHANT_LVL;
	public static int[] BBS_JEWELS_ENCHANT_LVL;
	public static int[] BBS_ENCHANT_PRICE_WEAPON;
	public static int[] BBS_ENCHANT_PRICE_ARMOR;
	public static int[] BBS_ENCHANT_PRICE_JEWELS;

	public static int BBS_ENCHANT_WEAPON_ATTRIBUTE_MAX;
	public static int BBS_ENCHANT_ARMOR_ATTRIBUTE_MAX;
	public static int Item_Custom_Template_ID_PRICE_COUNT;
	public static int Item_Custom_Template_ID_PRICE;

	public static int[] BBS_ENCHANT_ATRIBUTE_LVL_WEAPON;
	public static int[] BBS_ENCHANT_ATRIBUTE_LVL_ARMOR;
	public static int[] BBS_ENCHANT_ATRIBUTE_PRICE_ARMOR;
	public static int[] BBS_ENCHANT_ATRIBUTE_PRICE_WEAPON;
	public static boolean BBS_ENCHANT_ATRIBUTE_PVP;

	public static boolean BBS_ENCHANT_HEAD_ATTRIBUTE;
	public static boolean BBS_ENCHANT_CHEST_ATTRIBUTE;
	public static boolean BBS_ENCHANT_LEGS_ATTRIBUTE;
	public static boolean BBS_ENCHANT_GLOVES_ATTRIBUTE;
	public static boolean BBS_ENCHANT_FEET_ATTRIBUTE;
	public static String[] BBS_ENCHANT_GRADE_ATTRIBUTE;

	public static boolean BBS_ENCHANT_WEAPON_ATTRIBUTE;
	public static boolean BBS_ENCHANT_SHIELD_ATTRIBUTE;

	public static int BBS_TELEPORT_ITEM_ID;
	public static int BBS_TELEPORT_SAVE_ITEM_ID;
	public static int BBS_TELEPORT_FREE_LEVEL;
	public static int BBS_TELEPORT_MAX_COUNT;
	public static int BBS_TELEPORT_SAVE_PRICE;
	public static int BBS_TELEPORT_PRICE;
	public static boolean BBS_TELEPORT_PRICE_PA;
	public static boolean BBS_TELEPORT_POINTS_PA;
	public static boolean BBS_TELEPORT_ALLOW_IN_INSTANCE;
	public static boolean BBS_TELEPORT_ALLOW_IN_UNDERWATHER;
	public static boolean BBS_TELEPORT_ALLOW_IN_COMBAT;
	public static boolean BBS_TELEPORT_ALLOW_ON_SIEGE;

	public static boolean SERVICES_CLAN_ACADEM_ENABLED;
	
	public static boolean BBS_GAME_LOTTERY_ALLOW;
	public static int[] BBS_GAME_LOTTERY_BET;
	public static double BBS_GAME_LOTTERY_WIN_CHANCE;
	public static double BBS_GAME_LOTTERY_JACKPOT_CHANCE;
	public static int BBS_GAME_LOTTERY_JACKTOP_STARTED_COUNT;
	public static int BBS_GAME_LOTTERY_LOOS_TO_JACKPOT;
	public static int BBS_GAME_LOTTERY_ITEM;
	public static int BBS_GAME_LOTTERY_REWARD_MULTIPLE;

	public static boolean ALLOW_IP_LOCK;
	public static boolean ALLOW_HWID_LOCK;
	public static int HWID_LOCK_MASK;
    public static HWIDComparator LOCK_ACCOUNT_HWID_COMPARATOR;
    public static boolean CAPTCHA_ENABLE;
    public static String CAPTCHA_TYPE;
    public static int CAPTCHA_TIME;
    public static String[] CAPTCHA_IMAGE_WORDS;
    public static int CAPTCHA_COUNT_ERROR;
    public static int CAPTCHA_NPC_CHANCE;
    public static int CAPTCHA_CHAT_CHANCE;
	public static boolean CAPTCHA_COMMAND_ENABLE;
	public static int REUSE_COMMAND_TIME;

    public static boolean CAPTCHA_SHOW_PLAYERS_WITH_PA;
    
	/** GameServer ports */
	public static int[] PORTS_GAME;
	public static String GAMESERVER_HOSTNAME;

	public static String DATABASE_DRIVER;
	public static int DATABASE_MAX_CONNECTIONS;
	public static int DATABASE_MAX_IDLE_TIMEOUT;
	public static int DATABASE_IDLE_TEST_PERIOD;
	public static String DATABASE_URL;
	public static String DATABASE_LOGIN;
	public static String DATABASE_PASSWORD;

	// Database additional options
	public static boolean AUTOSAVE;

	public static long USER_INFO_INTERVAL;
	public static boolean BROADCAST_STATS_INTERVAL;
	public static long BROADCAST_CHAR_INFO_INTERVAL;

	public static int EFFECT_TASK_MANAGER_COUNT;

	public static int MAXIMUM_ONLINE_USERS;

	public static boolean DONTLOADSPAWN;
	public static boolean DONTLOADQUEST;
	public static int MAX_REFLECTIONS_COUNT;

	public static int SHIFT_BY;
	public static int SHIFT_BY_Z;
	public static int MAP_MIN_Z;
	public static int MAP_MAX_Z;
	public static int LINEAR_TERRITORY_CELL_SIZE;

	/** ChatBan */
	public static int CHAT_MESSAGE_MAX_LEN;
	public static boolean ABUSEWORD_BANCHAT;
	public static int[] BAN_CHANNEL_LIST = new int[18];
	public static boolean ABUSEWORD_REPLACE;
	public static String ABUSEWORD_REPLACE_STRING;
	public static int ABUSEWORD_BANTIME;
	public static String RESTRICTED_CHAR_NAMES;
	public static GArray<String> LIST_RESTRICTED_CHAR_NAMES = new GArray<String>();
	public static boolean ENABLE_TRADE_BLOCKSPAM;
	public static GArray<String> TRADE_LIST =  new GArray<String>();
	public static GArray<String> TRADE_LIST_SYMBOLS =  new GArray<String>();
	public static Pattern[] ABUSEWORD_LIST = {};
	public static boolean BANCHAT_ANNOUNCE;
	public static boolean BANCHAT_ANNOUNCE_FOR_ALL_WORLD;
	public static boolean BANCHAT_ANNOUNCE_NICK;

	public static int[] CHATFILTER_CHANNELS = new int[18];
	public static int CHATFILTER_MIN_LEVEL = 0;
	public static int CHATFILTER_WORK_TYPE = 1;

	public static final int[] VITALITY_LEVELS = { 240, 2000, 13000, 17000, 20000 };

	public static int[] CASTLE_SELECT_HOURS;
    public static int CASTLE_SIEGE_PERIOD;
    public static int TW_SIEGE_DAY;
	public static boolean ALT_PCBANG_POINTS_ENABLED;
	public static double ALT_PCBANG_POINTS_BONUS_DOUBLE_CHANCE;
	public static int ALT_PCBANG_POINTS_BONUS;
	public static int ALT_PCBANG_POINTS_DELAY;
	public static int ALT_PCBANG_POINTS_MIN_LVL;
	public static long ALT_PCBANG_POINTS_BAN_TIME;
	public static int ALT_PCBANG_POINTS_MAX_CODE_ENTER_ATTEMPTS;
	public static String ALT_PCBANG_POINTS_COUPON_TEMPLATE;
	public static int PC_BANG_ENCHANT_MAX;
	public static int PC_BANG_SAFE_ENCHANT;
	public static int ALT_PCBANG_POINTS_ON_START;
	public static int ALT_MAX_PC_BANG_POINTS;
	public static int ALT_PC_BANG_WIVERN_PRICE;
	public static int ALT_PC_BANG_WIVERN_TIME;

	/** Thread pools size */
	public static int SCHEDULED_THREAD_POOL_SIZE;
	public static int EXECUTOR_THREAD_POOL_SIZE;

	public static boolean ENABLE_RUNNABLE_STATS;

	/** Network settings */
	public static SelectorConfig SELECTOR_CONFIG = new SelectorConfig();

	/** Character name template */
	public static String CNAME_TEMPLATE;

	public static int CNAME_MAXLEN = 32;

	/** Clan name template */
	public static String CLAN_NAME_TEMPLATE;

	/** Clan title template */
	public static String CLAN_TITLE_TEMPLATE;

	/** Ally name template */
	public static String ALLY_NAME_TEMPLATE;

	/** Global chat state */
	public static boolean GLOBAL_SHOUT;
	public static boolean GLOBAL_TRADE_CHAT;
	public static int CHAT_RANGE;
	public static int SHOUT_OFFSET;
	public static boolean PREMIUM_HEROCHAT;
	public static boolean PREMIUM_SHOUT_CHAT;

	/** For test servers - evrybody has admin rights */
	public static boolean EVERYBODY_HAS_ADMIN_RIGHTS;
	public static int MULTISELL_SIZE;

	public static boolean SERVICES_CHANGE_NICK_ENABLED;
	public static String SERVICES_CHANGE_NICK_TEMPLATE;
	public static int SERVICES_CHANGE_NICK_PRICE;
	public static int SERVICES_CHANGE_NICK_ITEM;
    public static boolean SERVICES_CHANGE_TITLE_COLOR_ENABLED;
    public static int SERVICES_CHANGE_TITLE_COLOR_PRICE;
    public static int SERVICES_CHANGE_TITLE_COLOR_ITEM;
    
    public static boolean SERVICES_OLYMPIAD_RESET_ENABLED;
    public static int SERVICES_OLYMPIAD_ITEM;
    public static int SERVICES_OLYMPIAD_ITEM_PRICE;
    
    public static String[] SERVICES_CHANGE_TITLE_COLOR_LIST;
	public static boolean SERVICES_CHANGE_CLAN_NAME_ENABLED;
	public static int SERVICES_CHANGE_CLAN_NAME_PRICE;
	public static int SERVICES_CHANGE_CLAN_NAME_ITEM;
    public static int[] SERVICES_CLANLVL_PRICE;
    public static boolean SERVICES_CLANLVL_ACTIVE;
	public static int FORTRESS_BLOOD_OATH_COUNT;
	public static int FORTRESS_BLOOD_OATH_FRQ;
	public static int CLAN_PRICE_CREATE_ROYAL_SUB;
	public static int CLAN_PRICE_CREATE_KNIGHT_SUB;
	public static boolean SERVICES_CHANGE_PASSWORD;
	public static int PASSWORD_PAY_ID;
	public static long PASSWORD_PAY_COUNT;
	public static String APASSWD_TEMPLATE;
	public static int ACADEMY_SUB_LIMIT;
	public static int ACADEMY_SUB_LIMIT_LEVEL11;
	public static int ROYAL_SUB_LIMIT_1;
	public static int ROYAL_SUB_LIMIT_2;
	public static int ROYAL_SUB_LIMIT_1_LEVEL11;
	public static int ROYAL_SUB_LIMIT_2_LEVEL11;
	public static int KNIGHT_SUB_LIMIT_1;
	public static int KNIGHT_SUB_LIMIT_2;
	public static int KNIGHT_SUB_LIMIT_3;
	public static int KNIGHT_SUB_LIMIT_4;
	public static int KNIGHT_SUB_LIMIT_1_LEVEL9;
	public static int KNIGHT_SUB_LIMIT_2_LEVEL9;
	public static int KNIGHT_SUB_LIMIT_3_LEVEL10;
	public static int KNIGHT_SUB_LIMIT_4_LEVEL10;
	public static int TalkGuardChance;
	public static int TalkNormalChance = 0;
	public static int TalkNormalPeriod = 0;
	public static int TalkAggroPeriod = 0;
	public static int SERVICES_CLAN_LEVEL_CREATE;
	public static int REQUIREMEN_COST_CLAN_LEVEL_UP_TO_1;
	public static int REQUIREMEN_COST_CLAN_LEVEL_UP_TO_2;
	public static int REQUIREMEN_COST_CLAN_LEVEL_UP_TO_3;
	public static int REQUIREMEN_COST_CLAN_LEVEL_UP_TO_4;
	public static int REQUIREMEN_COST_CLAN_LEVEL_UP_TO_5;
	public static int REQUIREMEN_COST_CLAN_LEVEL_UP_TO_6;
	public static int REQUIREMEN_COST_CLAN_LEVEL_UP_TO_7;
	public static int REQUIREMEN_COST_CLAN_LEVEL_UP_TO_8;
	public static int REQUIREMEN_COST_CLAN_LEVEL_UP_TO_9;
	public static int REQUIREMEN_COST_CLAN_LEVEL_UP_TO_10;
	public static int REQUIREMEN_COST_CLAN_LEVEL_UP_TO_11;
	public static int REQUIREMEN_CLAN_LEVEL_UP_TO_1;
	public static int REQUIREMEN_CLAN_LEVEL_UP_TO_2;
	public static int REQUIREMEN_CLAN_LEVEL_UP_TO_3;
	public static int REQUIREMEN_CLAN_LEVEL_UP_TO_4;
	public static int REQUIREMEN_CLAN_LEVEL_UP_TO_5;
    public static int REQUIREMEN_CLAN_LEVEL_UP_TO_9;
	public static int REQUIREMEN_CLAN_LEVEL_UP_TO_10;
    public static int MEMBER_CLAN_LEVEL_UP_TO_6;
	public static int MEMBER_CLAN_LEVEL_UP_TO_7;
	public static int MEMBER_CLAN_LEVEL_UP_TO_8;
	public static int MEMBER_CLAN_LEVEL_UP_TO_9;
	public static int MEMBER_CLAN_LEVEL_UP_TO_10;
	public static int MEMBER_CLAN_LEVEL_UP_TO_11;
	public static int CLAN_MAIN_LIMIT_LEVEL0;
	public static int CLAN_MAIN_LIMIT_LEVEL1;
	public static int CLAN_MAIN_LIMIT_LEVEL2;
	public static int CLAN_MAIN_LIMIT_LEVEL3;
	public static int CLAN_MAIN_LIMIT_LEVEL4;
	public static int CLAN_MAIN_LIMIT_LEVEL5;
	public static int CLAN_MAIN_LIMIT_LEVEL6;
	public static int CLAN_MAIN_LIMIT_LEVEL7;
	public static int CLAN_MAIN_LIMIT_LEVEL8;
	public static int CLAN_MAIN_LIMIT_LEVEL9;
	public static int CLAN_MAIN_LIMIT_LEVEL10;
	public static int CLAN_MAIN_LIMIT_LEVEL11;
	public static int EXPELLED_MEMBER_PENALTY;
	public static int LEAVED_ALLY_PENALTY;
	public static int DISSOLVED_ALLY_PENALTY;
	public static int MIN_EARNED_ACADEM_POINT;
	public static int MAX_EARNED_ACADEM_POINT;

	public static boolean SERVICES_CHANGE_PET_NAME_ENABLED;
	public static int SERVICES_CHANGE_PET_NAME_PRICE;
	public static int SERVICES_CHANGE_PET_NAME_ITEM;

	public static boolean SERVICES_EXCHANGE_BABY_PET_ENABLED;
	public static int SERVICES_EXCHANGE_BABY_PET_PRICE;
	public static int SERVICES_EXCHANGE_BABY_PET_ITEM;

	public static boolean SERVICES_CHANGE_SEX_ENABLED;
	public static int SERVICES_CHANGE_SEX_PRICE;
	public static int SERVICES_CHANGE_SEX_ITEM;

	public static boolean SERVICES_CHANGE_BASE_ENABLED;
	public static int SERVICES_CHANGE_BASE_PRICE;
	public static int SERVICES_CHANGE_BASE_ITEM;

	public static boolean SERVICES_SEPARATE_SUB_ENABLED;
	public static int SERVICES_SEPARATE_SUB_PRICE;
	public static int SERVICES_SEPARATE_SUB_ITEM;

	public static boolean SERVICES_CHANGE_NICK_COLOR_ENABLED;
	public static int SERVICES_CHANGE_NICK_COLOR_PRICE;
	public static int SERVICES_CHANGE_NICK_COLOR_ITEM;
	public static String[] SERVICES_CHANGE_NICK_COLOR_LIST;
	public static boolean SERVICES_ALLOW_CHANGE_NICK_COLOR_TARGET;
	public static String[] SERVICES_CHANGE_NICK_COLOR_TARGET;

	public static boolean SERVICES_BASH_ENABLED;
	public static boolean SERVICES_BASH_SKIP_DOWNLOAD;
	public static int SERVICES_BASH_RELOAD_TIME;

	public static int SERVICES_RATE_TYPE;
	public static int[] SERVICES_RATE_BONUS_PRICE;
	public static int[] SERVICES_RATE_BONUS_ITEM;
	public static double[] SERVICES_RATE_BONUS_VALUE;
	public static int[] SERVICES_RATE_BONUS_DAYS;
	public static int SERVICES_RATE_BONUS_PERDAY_ITEM;
	public static int SERVICES_RATE_BONUS_PERDAY_PRICE;
	public static double SERVICES_RATE_BONUS_PERDAY_VALUE;

	public static boolean SERVICES_NOBLESS_SELL_ENABLED;
	public static int SERVICES_NOBLESS_SELL_PRICE;
	public static int SERVICES_NOBLESS_SELL_ITEM;

	public static boolean SERVICES_HERO_SELL_ENABLED;
	public static int[] SERVICES_HERO_SELL_PRICE;
	public static int[] SERVICES_HERO_SELL_ITEM;
	public static int[] SERVICES_HERO_SELL_DAY;
	public static boolean SERVICES_HERO_SELL_CHAT;
	public static boolean SERVICES_HERO_SELL_SKILL;
	public static boolean SERVICES_HERO_SELL_ITEMS;

	public static boolean SERVICES_EXPAND_INVENTORY_ENABLED;
	public static int SERVICES_EXPAND_INVENTORY_PRICE;
	public static int SERVICES_EXPAND_INVENTORY_ITEM;
	public static int SERVICES_EXPAND_INVENTORY_MAX;

	public static boolean SERVICES_EXPAND_WAREHOUSE_ENABLED;
	public static int SERVICES_EXPAND_WAREHOUSE_PRICE;
	public static int SERVICES_EXPAND_WAREHOUSE_ITEM;

	public static boolean SERVICES_EXPAND_CWH_ENABLED;
	public static int SERVICES_EXPAND_CWH_PRICE;
	public static int SERVICES_EXPAND_CWH_ITEM;

	public static String SERVICES_SELLPETS;

	public static boolean SERVICES_ENABLE_NO_CARRIER;
	public static int SERVICES_NO_CARRIER_DEFAULT_TIME;
	public static int[] ALT_SHOW_LEVEL_UP_PAGES;
	public static int[] Item_Custom_Template;

	public static boolean SERVICES_ALLOW_CLASS_BONUS;
	public static int SERVICES_CLASS_BONUS_ITEM_DAY;
	public static int SERVICES_NO_CARRIER_MAX_TIME;
	public static int SERVICES_NO_CARRIER_MIN_TIME;

	public static boolean SERVICES_2_CLASS_CHANGE_REWARD_ENABLED;
	public static int[] SERVICES_2_CLASS_MAGE_CHANGE_REWARD;
	public static int[] SERVICES_2_CLASS_FIGHTER_CHANGE_REWARD;

	public static boolean SERVICES_3_CLASS_CHANGE_REWARD_ENABLED;
	public static int[] SERVICES_3_CLASS_MAGE_CHANGE_REWARD;
	public static int[] SERVICES_3_CLASS_FIGHTER_CHANGE_REWARD;
	public static String FORBIDDEN_CHARACTER_NAMES;
	public static boolean FORBIDDEN_CHARACTER_NAMES_DEBUG;
	public static List<String> LIST_FORBIDDEN_CHARACTER_NAMES = new ArrayList<>();

	public static boolean SERVICES_OFFLINE_TRADE_ALLOW;
	public static boolean SERVICES_OFFLINE_TRADE_ALLOW_OFFSHORE;
	public static int SERVICES_OFFLINE_TRADE_MIN_LEVEL;
	public static int SERVICES_OFFLINE_TRADE_NAME_COLOR;
	public static int SERVICES_OFFLINE_TRADE_PRICE;
	public static int SERVICES_OFFLINE_TRADE_PRICE_ITEM;
	public static long SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK;
	public static boolean SERVICES_OFFLINE_TRADE_RESTORE_AFTER_RESTART;
	public static boolean SERVICES_GIRAN_HARBOR_ENABLED;
	public static boolean SERVICES_PARNASSUS_ENABLED;
	public static boolean SERVICES_PARNASSUS_NOTAX;
	public static long SERVICES_PARNASSUS_PRICE;

	public static boolean SERVICES_ALLOW_LOTTERY;
	public static int SERVICES_LOTTERY_PRIZE;
	public static int SERVICES_ALT_LOTTERY_PRICE;
	public static int SERVICES_LOTTERY_TICKET_PRICE;
	public static double SERVICES_LOTTERY_5_NUMBER_RATE;
	public static double SERVICES_LOTTERY_4_NUMBER_RATE;
	public static double SERVICES_LOTTERY_3_NUMBER_RATE;
	public static int SERVICES_LOTTERY_2_AND_1_NUMBER_PRIZE;

	public static boolean SERVICES_ALLOW_ROULETTE;
	public static long SERVICES_ROULETTE_MIN_BET;
	public static long SERVICES_ROULETTE_MAX_BET;

	/** Olympiad Compitition Starting time */
	public static int OLYMPIAD_START_TIME;
	
	/** Olympiad Compition Min */
	public static int OLYMPIAD_MIN;
	public static boolean SHOW_OLYMPIAD_PROF;
	/** Olympaid Comptetition Period */
	public static long OLYMPIAD_CPERIOD;
	/** Olympaid Weekly Period */
	public static long OLYMPIAD_WPERIOD;
	/** Olympaid Validation Period */
	public static long OLYMPIAD_VPERIOD;
	public static boolean OLYMPIAD_USE_MONTHLY_PERIOD;
	public static int OLYMPIAD_WEEKLY_WEEKCOUNT;
	public static int OLYMPIAD_WEEKLY_PERIOD_ENDDAY;
	public static final FastTable<Integer> OLYMPIAD_PERIOD_END_DAYS = new FastTable<>();

    public static boolean SERVICEFAMEACTIVE;
    public static int[] SERVICEFAMEPRICE;
    public static boolean SERVICEFAMEFREEFORPA;
    public static boolean SERVICECRPACTIVE;
    public static int[] SERVICECRPPRICE;
    public static boolean SERVICECRPFREEFORPA;
    public static boolean SERVICERECOMACTIVE;
    public static int[] SERVICERECOMPRICE;
    public static boolean SERVICERECOMFREEFORPA;
	
    public static boolean SERVICES_LVL_ENABLED;
    public static int SERVICES_LVL_UP_MAX;
    public static int SERVICES_LVL_UP_PRICE;
    public static int SERVICES_LVL_UP_ITEM;
    public static int SERVICES_LVL_DOWN_MAX;
    public static int SERVICES_LVL_DOWN_PRICE;
    public static int SERVICES_LVL_DOWN_ITEM;
    
	public static boolean OLYMPIAD_ENABLE;
	public static boolean OLYMPIAD_ENABLE_SPECTATING;

	public static int OLYMPIAD_CLASS_GAME_MIN;
	public static int OLYMPIAD_NONCLASS_GAME_MIN;
	public static int OLYMPIAD_TEAM_GAME_MIN;
	public static int OLYMPIAD_BEGIN_TIME;
	public static int OLYMPIAD_GAME_MAX_LIMIT;
	public static int OLYMPIAD_GAME_CLASSES_COUNT_LIMIT;
	public static int OLYMPIAD_GAME_NOCLASSES_COUNT_LIMIT;
	public static int OLYMPIAD_GAME_TEAM_COUNT_LIMIT;

	public static int OLYMPIAD_BATTLE_REWARD_ITEM;
	public static int OLYMPIAD_CLASSED_RITEM_C;
	public static int OLYMPIAD_NONCLASSED_RITEM_C;
	public static int OLYMPIAD_TEAM_RITEM_C;
	public static int OLYMPIAD_COMP_RITEM;
	public static int OLYMPIAD_GP_PER_POINT;
	public static int OLYMPIAD_HERO_POINTS;
	public static int OLYMPIAD_RANK1_POINTS;
	public static int OLYMPIAD_RANK2_POINTS;
	public static int OLYMPIAD_RANK3_POINTS;
	public static int OLYMPIAD_RANK4_POINTS;
	public static int OLYMPIAD_RANK5_POINTS;
	public static int OLYMPIAD_STADIAS_COUNT;
	public static int OLYMPIAD_BATTLES_FOR_REWARD;
	public static int OLYMPIAD_POINTS_DEFAULT;
	public static int OLYMPIAD_POINTS_WEEKLY;
	public static boolean OLYMPIAD_OLDSTYLE_STAT;
	public static boolean CHECK_OLYMPIAD_IP;
	public static boolean CHECK_OLYMPIAD_HWID;
    public static boolean OLY_ENCH_LIMIT_ENABLE;
    public static int OLY_ENCHANT_LIMIT_WEAPON;
    public static int OLY_ENCHANT_LIMIT_ARMOR;
    public static int OLY_ENCHANT_LIMIT_JEWEL;

	/** Logging Chat Window */
	public static boolean LOG_CHAT;

	public static Map<Integer, PlayerAccess> gmlist = new HashMap<>();

	/** Rate control */
	public static double RATE_XP;
	public static double RATE_CHANCE_DROP_ITEMS;
	public static double RATE_CHANCE_GROUP_DROP_ITEMS;
	public static double RATE_CHANCE_SPOIL;
	public static double RATE_CHANCE_SPOIL_WEAPON_ARMOR_ACCESSORY;
	public static double RATE_CHANCE_DROP_WEAPON_ARMOR_ACCESSORY;
	public static double RATE_SP;
	public static double RATE_QUESTS_REWARD;
	public static double RATE_QUESTS_DROP;
	public static double RATE_CLAN_REP_SCORE;
	public static int RATE_CLAN_REP_SCORE_MAX_AFFECTED;
	public static double RATE_DROP_ADENA;
	public static double RATE_DROP_ITEMS;
	public static double RATE_DROP_COMMON_ITEMS;
	public static double RATE_DROP_RAIDBOSS;
	public static double RATE_DROP_SPOIL;
	public static int[] NO_RATE_ITEMS;
	public static boolean NO_RATE_EQUIPMENT;
	public static boolean NO_RATE_KEY_MATERIAL;
	public static boolean NO_RATE_RECIPES;

	public static int RATE_MOD_DROP_SPOIL;
	public static int RATE_MOD_DROP_ADENA;
	public static int RATE_MOD_DROP_RAIDBOSS;
	public static int RATE_MOD_DROP_SIEGE_GUARD;
	public static int RATE_MOD_DROP_ITEMS;

	public static double RATE_DROP_SIEGE_GUARD;
	public static double RATE_DROP_SIEGE_GUARD_FOR_PREMIUM;
	public static double RATE_SIEGE_FAME_FOR_PREMIUM;
	public static double RATE_MANOR;
	public static double RATE_FISH_DROP_COUNT;
	public static boolean RATE_PARTY_MIN;

	public static double RATE_HELLBOUND_CONFIDENCE;
	public static int OPEN_HELLBOUND_CONFIDENCE;

	public static int RATE_MOB_SPAWN;
	public static int RATE_MOB_SPAWN_MIN_LEVEL;
	public static int RATE_MOB_SPAWN_MAX_LEVEL;

	/** Player Drop Rate control */
	public static boolean KARMA_DROP_GM;
	public static boolean KARMA_NEEDED_TO_DROP;

	public static int KARMA_DROP_ITEM_LIMIT;

	public static int KARMA_RANDOM_DROP_LOCATION_LIMIT;

	public static double KARMA_DROPCHANCE_BASE;
	public static double KARMA_DROPCHANCE_MOD;
	public static double NORMAL_DROPCHANCE_BASE;
	public static int DROPCHANCE_EQUIPMENT;
	public static int DROPCHANCE_EQUIPPED_WEAPON;
	public static int DROPCHANCE_ITEM;

	public static int AUTODESTROY_ITEM_AFTER;
	public static int AUTODESTROY_PLAYER_ITEM_AFTER;

	public static int DELETE_DAYS;

	public static int PURGE_BYPASS_TASK_FREQUENCY;
	public static String SERVER_NAME;

	/** Datapack root directory */
	public static File DATAPACK_ROOT;

	public static boolean ALLOW_DISCARDITEM;
	public static boolean ALLOW_MAIL;
    public static int ALLOW_MAIL_LVL;
    public static int MAIL_TIME;
	public static boolean ALLOW_WAREHOUSE;
	public static boolean ALLOW_WATER;
	public static boolean ALLOW_CURSED_WEAPONS;
	public static boolean DROP_CURSED_WEAPONS_ON_KICK;

	/** Pets */
	public static int SWIMING_SPEED;

	/** protocol revision */
	public static int MIN_PROTOCOL_REVISION;
	public static int MAX_PROTOCOL_REVISION;

	/** random animation interval */
	public static int MIN_NPC_ANIMATION;
	public static int MAX_NPC_ANIMATION;

	public static String DEFAULT_LANG;

	public static String RESTART_AT_TIME;

	public static int GAME_SERVER_LOGIN_PORT;
	public static boolean GAME_SERVER_LOGIN_CRYPT;
	public static String GAME_SERVER_LOGIN_HOST;
	public static String INTERNAL_HOSTNAME;
	public static String EXTERNAL_HOSTNAME;

	public static boolean SERVER_SIDE_NPC_NAME;
	public static boolean SERVER_SIDE_NPC_TITLE;
	public static boolean SERVER_SIDE_NPC_TITLE_LVL_AGR;

	public static String CLASS_MASTERS_PRICE;
	public static int CLASS_MASTERS_PRICE_ITEM;
    public static boolean ALLOW_AWAY_STATUS;
    public static boolean AWAY_ONLY_FOR_PREMIUM;
    public static int AWAY_TIMER;
    public static int BACK_TIMER;
    public static int AWAY_TITLE_COLOR;
    public static boolean AWAY_PLAYER_TAKE_AGGRO;
    public static boolean AWAY_PEACE_ZONE;
    public static boolean ALLOW_RESET_OLY_POINTS_COMMANDS;
	public static int[] CLASS_MASTERS_PRICE_LIST = new int[4];
	public static List<Integer> ALLOW_CLASS_MASTERS_LIST = new ArrayList<>();
	public static boolean ALLOW_NEWS_INFORMER;
	public static int[] NEWS_INFORMER_RND_ITEM_LIST;
	public static int[] NEWS_INFORMER_RND_ITEM_COUNTS;
	public static int[] NEWS_INFORMER_RND_ITEM_CHANCES;
	public static boolean NEWS_INFORMER_RND_ITEM;
	public static boolean NEWS_INFORMER_ONE_ITEM;
	public static int NEWS_INFORMER_ONE_ITEM_ID;
	public static int NEWS_INFORMER_ONE_ITEM_COUNT;
	public static boolean ALLOW_NEWBIE_BONUS_MANAGER;

	public static boolean ITEM_BROKER_ITEM_SEARCH;

	/** Inventory slots limits */
	public static int INVENTORY_MAXIMUM_NO_DWARF;
	public static int INVENTORY_MAXIMUM_DWARF;
	public static int INVENTORY_MAXIMUM_GM;
	public static int QUEST_INVENTORY_MAXIMUM;

	/** Warehouse slots limits */
	public static int WAREHOUSE_SLOTS_NO_DWARF;
	public static int WAREHOUSE_SLOTS_DWARF;
	public static int WAREHOUSE_SLOTS_CLAN;

	public static int FREIGHT_SLOTS;

	/** Spoil Rates */
	public static double BASE_SPOIL_RATE;
	public static double MINIMUM_SPOIL_RATE;
	public static boolean ALT_SPOIL_FORMULA;

	/** Manor Config */
	public static double MANOR_SOWING_BASIC_SUCCESS;
	public static double MANOR_SOWING_ALT_BASIC_SUCCESS;
	public static double MANOR_HARVESTING_BASIC_SUCCESS;
	public static int MANOR_DIFF_PLAYER_TARGET;
	public static double MANOR_DIFF_PLAYER_TARGET_PENALTY;
	public static int MANOR_DIFF_SEED_TARGET;
	public static double MANOR_DIFF_SEED_TARGET_PENALTY;

	/** Karma System Variables */
	public static int KARMA_MIN_KARMA;
	public static int KARMA_SP_DIVIDER;
	public static int KARMA_LOST_BASE;

	public static int MIN_PK_TO_ITEMS_DROP;
	public static boolean DROP_ITEMS_ON_DIE;
	public static boolean DROP_ITEMS_AUGMENTED;

	public static List<Integer> KARMA_LIST_NONDROPPABLE_ITEMS = new ArrayList<>();

	public static int PVP_TIME;

	/* PK System */
	public static boolean PK_SYSTEM_ENABLE;
	public static int[] PK_SYSTEM_KILLER_LEVEL_FOR_REWARD;
	public static int[] PK_SYSTEM_TARGET_LEVEL_FOR_REWARD;
	public static boolean PK_SYSTEM_ALLOW_REWARD;
	public static int[] PK_SYSTEM_ITEM_INFO;
	public static boolean PK_SYSTEM_ADD_EXP_SP;
	public static int[] PK_SYSTEM_EXP_SP;
	public static boolean PK_SYSTEM_ENABLE_BLOCK_TIME;
	public static int PK_SYSTEM_BLOCK_TIME_AFTER_KILL;
	public static boolean PK_SYSTEM_ANNOUNCE;
	public static int PK_SYSTEM_ANNOUNCE_RADIUS;
	public static boolean PK_SYSTEM_IN_ZONE;
	public static String PK_SYSTEM_ZONE;

	/* PvP System */
	public static boolean PvP_SYSTEM_ENABLE;
	public static int[] PvP_SYSTEM_KILLER_LEVEL_FOR_REWARD;
	public static int[] PvP_SYSTEM_TARGET_LEVEL_FOR_REWARD;
	public static boolean PvP_SYSTEM_ALLOW_REWARD;
	public static int[] PvP_SYSTEM_ITEM_INFO;
	public static boolean PvP_SYSTEM_ADD_EXP_SP;
	public static int[] PvP_SYSTEM_EXP_SP;
	public static boolean PvP_SYSTEM_ENABLE_BLOCK_TIME;
	public static int PvP_SYSTEM_BLOCK_TIME_AFTER_KILL;
	public static boolean PvP_SYSTEM_ANNOUNCE;
	public static int PvP_SYSTEM_ANNOUNCE_RADIUS;
	public static boolean PvP_SYSTEM_IN_ZONE;
	public static String PvP_SYSTEM_ZONE;

	/** Chance that an item will succesfully be enchanted */
	public static int ENCHANT_CHANCE_WEAPON;
	public static int PREMIUM_ENCHANT_CHANCE_WEAPON;
	public static int ENCHANT_CHANCE_ARMOR;
	public static int PREMIUM_ENCHANT_CHANCE_ARMOR;
	public static int ENCHANT_CHANCE_ACCESSORY;
	public static int PREMIUM_ENCHANT_CHANCE_ACCESSORY;
	public static int ENCHANT_CHANCE_CRYSTAL_WEAPON;
	public static int PREMIUM_ENCHANT_CHANCE_CRYSTAL_WEAPON;
	public static int ENCHANT_CHANCE_CRYSTAL_ARMOR;
	public static int PREMIUM_ENCHANT_CHANCE_CRYSTAL_ARMOR;
	public static int ENCHANT_CHANCE_CRYSTAL_ACCESSORY;
	public static int PREMIUM_ENCHANT_CHANCE_CRYSTAL_ACCESSORY;
	public static int ENCHANT_MAX_WEAPON;
	public static int ENCHANT_MAX_SHIELD_ARMOR;
	public static int ENCHANT_MAX_ACCESSORY;
	public static int ENCHANT_CRYSTAL_FAILED;
	public static int ENCHANT_SCROLL_LEVEL_WEAPON;
	public static int ENCHANT_SCROLL_LEVEL_ARMOR;
	public static int ENCHANT_SCROLL_LEVEL_ACCESSORY;
	public static int ENCHANT_ATTRIBUTE_STONE_CHANCE;
	public static int ENCHANT_ATTRIBUTE_CRYSTAL_CHANCE;
	public static int ARMOR_OVERENCHANT_HPBONUS_LIMIT;
	public static boolean SHOW_ENCHANT_EFFECT_RESULT;
	public static boolean SHOW_ENCHANT_RESULT_UP_3;

	public static boolean REGEN_SIT_WAIT;

	public static double RATE_RAID_REGEN;
	public static double RATE_RAID_DEFENSE;
	public static double RATE_RAID_ATTACK;
	public static double RATE_EPIC_DEFENSE;
	public static double RATE_EPIC_ATTACK;
	public static int RAID_MAX_LEVEL_DIFF;
	public static boolean PARALIZE_ON_RAID_DIFF;
    public static int SHADAI_SPAWN_CHANCE;
    public static boolean ANNOUNCE_SHADAI_SPAWN;
	public static boolean ALLOW_TALK_WHILE_SITTING;
	public static boolean HELLBOUND_ENTER_NOQUEST;
	public static int HELLBOUND_LEVEL;
	/** Deep Blue Mobs' Drop Rules Enabled */
	public static boolean DEEPBLUE_DROP_RULES;
	public static int DEEPBLUE_DROP_MAXDIFF;
	public static int DEEPBLUE_DROP_RAID_MAXDIFF;
	public static boolean UNSTUCK_SKILL;

	/** telnet enabled */
	public static boolean IS_TELNET_ENABLED;
	public static String TELNET_DEFAULT_ENCODING;
	public static String TELNET_PASSWORD;
	public static String TELNET_HOSTNAME;
	public static int TELNET_PORT;

	/** Percent CP is restore on respawn */
	public static double RESPAWN_RESTORE_CP;
	/** Percent HP is restore on respawn */
	public static double RESPAWN_RESTORE_HP;
	/** Percent MP is restore on respawn */
	public static double RESPAWN_RESTORE_MP;

	/** Maximum number of available slots for pvt stores (sell/buy) - Dwarves */
	public static int MAX_PVTSTORE_SLOTS_DWARF;
	/** Maximum number of available slots for pvt stores (sell/buy) - Others */
	public static int MAX_PVTSTORE_SLOTS_OTHER;
	public static int MAX_PVTCRAFT_SLOTS;

	public static boolean SENDSTATUS_TRADE_JUST_OFFLINE;
	public static double SENDSTATUS_TRADE_MOD;

	public static int CH_BID_GRADE1_MINCLANLEVEL;
	public static int CH_BID_GRADE1_MINCLANMEMBERS;
	public static int CH_BID_GRADE1_MINCLANMEMBERSLEVEL;
	public static int CH_BID_GRADE2_MINCLANLEVEL;
	public static int CH_BID_GRADE2_MINCLANMEMBERS;
	public static int CH_BID_GRADE2_MINCLANMEMBERSLEVEL;
	public static int CH_BID_GRADE3_MINCLANLEVEL;
	public static int CH_BID_GRADE3_MINCLANMEMBERS;
	public static int CH_BID_GRADE3_MINCLANMEMBERSLEVEL;
	public static double RESIDENCE_LEASE_FUNC_MULTIPLIER;
	public static double RESIDENCE_LEASE_MULTIPLIER;

    public static boolean ACCEPT_ALTERNATE_ID;
	public static int REQUEST_ID;

	public static boolean ANNOUNCE_MAMMON_SPAWN;

	public static int GM_NAME_COLOUR;
	public static boolean GM_HERO_AURA;
	public static int NORMAL_NAME_COLOUR;
	public static int CLANLEADER_NAME_COLOUR;
	public static int PREMIUM_NAME_COLOUR;

	/** AI */
	public static int AI_TASK_MANAGER_COUNT;
	public static long AI_TASK_ATTACK_DELAY;
	public static long AI_TASK_ACTIVE_DELAY;
	public static boolean BLOCK_ACTIVE_TASKS;
	public static boolean ALWAYS_TELEPORT_HOME;
	public static boolean ADEPT_ENABLE;
	public static boolean RND_WALK;
	public static int RND_WALK_RATE;
	public static int RND_ANIMATION_RATE;

	/** GATEKEEPER */
	public static boolean MULTILANG_GATEKEEPER;
	public static double GATEKEEPER_MODIFIER;
	public static int GATEKEEPER_FREE;
	public static int CRUMA_GATEKEEPER_LVL;
	public static boolean ALLOW_EVENT_GATEKEEPER;

	public static int AGGRO_CHECK_INTERVAL;
	public static long NONAGGRO_TIME_ONTELEPORT;

	/** Maximum range mobs can randomly go from spawn point */
	public static int MAX_DRIFT_RANGE;

	/** Maximum range mobs can pursue agressor from spawn point */
	public static int MAX_PURSUE_RANGE;
	public static int MAX_PURSUE_UNDERGROUND_RANGE;
	public static int MAX_PURSUE_RANGE_RAID;
	public static boolean ALT_AI_KELTIRS;
	public static boolean ALT_AI_TAURIN;
	public static int AI_KANABION_RESPAWN_TIME;

	public static boolean HIDE_GM_STATUS;
	public static boolean SHOW_GM_LOGIN;
	public static boolean SAVE_GM_EFFECTS; //Silence, gmspeed, etc...

	public static int MOVE_PACKET_DELAY;
	public static int ATTACK_PACKET_DELAY;

	public static boolean DAMAGE_FROM_FALLING;

	public static String GEO_EDITOR_HOST;

	/** Community Board */
	public static boolean COMMUNITYBOARD_ENABLED;
	public static String BBS_DEFAULT;
	public static String BBS_FOLDER;

	/** Wedding Options */
	public static boolean ALLOW_WEDDING;
	public static int WEDDING_PRICE;
	public static boolean WEDDING_PUNISH_INFIDELITY;
	public static boolean WEDDING_TELEPORT;
	public static int WEDDING_TELEPORT_PRICE;
	public static int WEDDING_TELEPORT_INTERVAL;
	public static boolean WEDDING_SAMESEX;
	public static boolean WEDDING_FORMALWEAR;
	public static int WEDDING_DIVORCE_COSTS;

	public static boolean BONUS_SERVICE_ENABLE;
	public static int[] BONUS_SERVICE_CLAN_REWARD;
	public static int[] BONUS_SERVICE_PARY_REWARD;

	public static double ALT_NPC_PATK_MODIFIER;
	public static double ALT_NPC_MATK_MODIFIER;
	public static double ALT_NPC_MAXHP_MODIFIER;
	public static double ALT_NPC_MAXMP_MODIFIER;
	public static int MAX_VORTEX_BOSS_COUNT;
	public static int TIME_DESPAWN_VORTEX_BOSS;
	/** Enchant Config * */
	public static int SAFE_ENCHANT_COMMON;
	public static int SAFE_ENCHANT_FULL_BODY;
	/** Allow Instance config */
    public static boolean ALLOW_INSTANCES_LEVEL_MANUAL;
    public static boolean ALLOW_INSTANCES_PARTY_MANUAL;
    public static int INSTANCES_LEVEL_MIN;
    public static int INSTANCES_LEVEL_MAX;
    public static int INSTANCES_PARTY_MIN;
    public static int INSTANCES_PARTY_MAX;
    public static int TIAT_KILLS_FOR_SOD;
    public static int EKIMUS_KILLS_FOR_SOI;
    public static boolean BROTHER_KILLS_FOR_EKIMUS;
    public static int COHEMENES_KILLS_FOR_SOI;
    public static int HALL_OF_ERROSION_DEF_COUNT;
    public static int SOD_OPEN_TIME;
    public static int SOI_OPEN_TIME;
	public static boolean ENABLE_ANNOUNCE_BOSS;
	public static GArray<Integer> ANNOUNCE_BOSS_RESPAWN;
    public static int FIXINTERVALOFANTHARAS_HOUR;
    public static boolean KILL_BARAKIEL_SET_NOBLE;
    public static boolean ALLOW_ANNOUNCE_NOBLE_RB;
    public static int SPAWN_ANTHARAS_TIME;
    public static int FIXINTERVALOFVALAKAS;
    public static int SPAWN_VALAKAS_TIME;
	/** Allow Manor system */
	public static boolean ALLOW_MANOR;

	/** Manor Refresh Starting time */
	public static int MANOR_REFRESH_TIME;

	/** Manor Refresh Min */
	public static int MANOR_REFRESH_MIN;

	/** Manor Next Period Approve Starting time */
	public static int MANOR_APPROVE_TIME;

	/** Manor Next Period Approve Min */
	public static int MANOR_APPROVE_MIN;

	/** Manor Maintenance Time */
	public static int MANOR_MAINTENANCE_PERIOD;

	public static double EVENT_APIL_FOOLS_DROP_CHANCE;

	public static double EVENT_CHANGE_OF_HEART_CHANCE;

	public static double EVENT_CofferOfShadowsPriceRate;
	public static double EVENT_CofferOfShadowsRewardRate;

	public static boolean FIGHT_CLUB_ENABLED;
	public static int FIGHT_CLUB_MINIMUM_LEVEL_TO_PARRICIPATION;
	public static int FIGHT_CLUB_MAXIMUM_LEVEL_TO_PARRICIPATION;
	public static int FIGHT_CLUB_MAXIMUM_LEVEL_DIFFERENCE;
	public static String[] FIGHT_CLUB_ALLOWED_RATE_ITEMS;
	public static int FIGHT_CLUB_PLAYERS_PER_PAGE;
	public static int FIGHT_CLUB_ARENA_TELEPORT_DELAY;
	public static boolean FIGHT_CLUB_CANCEL_BUFF_BEFORE_FIGHT;
	public static boolean FIGHT_CLUB_UNSUMMON_PETS;
	public static boolean FIGHT_CLUB_UNSUMMON_SUMMONS;
	public static boolean FIGHT_CLUB_REMOVE_CLAN_SKILLS;
	public static boolean FIGHT_CLUB_REMOVE_HERO_SKILLS;
	public static int FIGHT_CLUB_TIME_TO_PREPARATION;
	public static int FIGHT_CLUB_FIGHT_TIME;
	public static boolean FIGHT_CLUB_ALLOW_DRAW;
	public static int FIGHT_CLUB_TIME_TELEPORT_BACK;
	public static boolean FIGHT_CLUB_ANNOUNCE_RATE;

	public static double EVENT_GLITTMEDAL_NORMAL_CHANCE;
	public static double EVENT_GLITTMEDAL_GLIT_CHANCE;
    public static String[] DefenseTownsStartTime;
    public static boolean TMEnabled;
    public static int TMStartHour;
    public static int TMStartMin;
    public static int TMEventInterval;
    public static int TMMobLife;
    public static int BossLifeTime;
    public static int TMTime1;
    public static int TMTime2;
    public static int TMTime3;
    public static int TMTime4;
    public static int TMTime5;
    public static int TMTime6;
    public static int TMWave1;
    public static int TMWave2;
    public static int TMWave3;
    public static int TMWave4;
    public static int TMWave5;
    public static int TMWave6;
    public static int TMWave1Count;
    public static int TMWave2Count;
    public static int TMWave3Count;
    public static int TMWave4Count;
    public static int TMWave5Count;
    public static int TMWave6Count;
    public static int TMBoss;
    public static int[] TMItem;
    public static int[] TMItemCol;
    public static int[] TMItemColBoss;
    public static int[] TMItemChance;
    public static int[] TMItemChanceBoss;
	public static boolean EVENT_TREASURES_OF_THE_HERALD_ENABLE;
	public static int EVENT_TREASURES_OF_THE_HERALD_ITEM_ID;
	public static int EVENT_TREASURES_OF_THE_HERALD_ITEM_COUNT;
	public static int EVENT_TREASURES_OF_THE_HERALD_TIME;
	public static int EVENT_TREASURES_OF_THE_HERALD_MIN_LEVEL;
	public static int EVENT_TREASURES_OF_THE_HERALD_MAX_LEVEL;
	public static int EVENT_TREASURES_OF_THE_HERALD_MINIMUM_PARTY_MEMBER;
	public static int EVENT_TREASURES_OF_THE_HERALD_MAX_GROUP;
	public static int EVENT_TREASURES_OF_THE_HERALD_SCORE_BOX;
	public static int EVENT_TREASURES_OF_THE_HERALD_SCORE_BOSS;
	public static int EVENT_TREASURES_OF_THE_HERALD_SCORE_KILL;
	public static int EVENT_TREASURES_OF_THE_HERALD_SCORE_DEATH;
	public static Version VERSION;
	public static String SERVER_VERSION;
	public static String SERVER_REVISION;
	public static String SERVER_BUILD_DATE;
	public static String SERVER_SITE;
	public static boolean ENT_SHOWENTERMESSON;
	public static String ENT_SHOWENTERMESS;
	public static boolean EVENT_HITMAN_ENABLED;
	public static int EVENT_HITMAN_COST_ITEM_ID;
	public static int EVENT_HITMAN_COST_ITEM_COUNT;
	public static int EVENT_HITMAN_TASKS_PER_PAGE;
	public static String[] EVENT_HITMAN_ALLOWED_ITEM_LIST;

	public static double EVENT_L2DAY_LETTER_CHANCE;

	public static boolean EVENT_LAST_HERO_GIVE_ITEM;
	public static int EVENT_LAST_HERO_ITEM_ID;
	public static double EVENT_LAST_HERO_ITEM_COUNT;
	public static boolean EVENT_LAST_HERO_RATE;
	public static boolean EVENT_LAST_HERO_GIVE_ITEM_FINAL;
	public static int EVENT_LAST_HERO_ITEM_ID_FINAL;
	public static double EVENT_LAST_HERO_ITEM_COUNT_FINAL;
	public static boolean EVENT_LAST_HERO_RATE_FINAL;
	public static int EVENT_LAST_HERO_TIME;
	public static int EVENT_LAST_HERO_RUNNING_TIME;
	public static String[] EVENT_LAST_HERO_START_TIME;
	public static boolean EVENT_LAST_HERO_CATEGORIES;
	public static boolean EVENT_LAST_HERO_ALLOW_SUMMONS;
	public static boolean EVENT_LAST_HERO_ALLOW_BUFFS;
	public static boolean EVENT_LAST_HERO_ALLOW_MULTI_REGISTER;
	public static String EVENT_LAST_HERO_CHECK_WINDOW_METHOD;
	public static String[] EVENT_LAST_HERO_FIGHTER_BUFFS;
	public static String[] EVENT_LAST_HERO_MAGE_BUFFS;
	public static boolean EVENT_LAST_HERO_BUFF_PLAYERS;
	public static boolean EVENT_LAST_HERO_AURA_ENABLE;
	public static boolean EVENT_LAST_HERO_ALLOW_HEROES;

	public static String[] EVENT_CTF_REWARDS;
	public static int EVENT_CTF_TIME;
	public static int EVENT_CTF_RUNNING_TIME;
	public static boolean EVENT_CTF_RATE;
	public static String[] EVENT_CTF_START_TIME;
	public static boolean EVENT_CTF_CATEGORIES;
	public static int EVENT_CTF_MAX_PLAYER_IN_TEAM;
	public static int EVENT_CTF_MIN_PLAYER_IN_TEAM;
	public static boolean EVENT_CTF_ALLOW_SUMMONS;
	public static boolean EVENT_CTF_ALLOW_BUFFS;
	public static boolean EVENT_CTF_ALLOW_MULTI_REGISTER;
	public static String EVENT_CTF_CHECK_WINDOW_METHOD;
	public static String[] EVENT_CTF_FIGHTER_BUFFS;
	public static String[] EVENT_CTF_MAGE_BUFFS;
	public static boolean EVENT_CTF_BUFF_PLAYERS;

	public static String[] EVENT_DEATH_MATCH_REWARDS;
	public static int EVENT_DEATH_MATCH_TIME;
	public static int EVENT_DEATH_MATCH_RUNNING_TIME;
	public static boolean EVENT_DEATH_MATCH_RATE;
	public static String[] EVENT_DEATH_MATCH_START_TIME;
	public static boolean EVENT_DEATH_MATCH_CATEGORIES;
	public static int EVENT_DEATH_MATCH_MAX_PLAYER_IN_TEAM;
	public static int EVENT_DEATH_MATCH_MIN_PLAYER_IN_TEAM;
	public static boolean EVENT_DEATH_MATCH_ALLOW_SUMMONS;
	public static boolean EVENT_DEATH_MATCH_ALLOW_BUFFS;
	public static boolean EVENT_DEATH_MATCH_ALLOW_MULTI_REGISTER;
	public static String EVENT_DEATH_MATCH_CHECK_WINDOW_METHOD;
	public static String[] EVENT_DEATH_MATCH_FIGHTER_BUFFS;
	public static String[] EVENT_DEATH_MATCH_MAGE_BUFFS;
	public static boolean EVENT_DEATH_MATCH_BUFF_PLAYERS;

	public static double EVENT_MARCH8_DROP_CHANCE;
	public static double EVENT_MARCH8_PRICE_RATE;

	public static int ENCHANT_CHANCE_MASTER_YOGI_STAFF;
	public static int ENCHANT_MAX_MASTER_YOGI_STAFF;
	public static int SAFE_ENCHANT_MASTER_YOGI_STAFF;

	public static long EVENT_SAVING_SNOWMAN_LOTERY_PRICE;
	public static int EVENT_SAVING_SNOWMAN_REWARDER_CHANCE;

	public static String[] EVENTS_DISALLOWED_SKILLS;

	public static double EVENT_TFH_POLLEN_CHANCE;

	public static double EVENT_TRICK_OF_TRANS_CHANCE;

	public static int EVENT_MOUSE_COIN_ID;
	public static double EVENT_MOUSE_COIN_CHANCE;
	public static int EVENT_MOUSE_COIN_MIN_COUNT;
	public static int EVENT_MOUSE_COIN_MAX_COUNT;
	//public static int EVENT_BASE_COIN_AFTER_RB;
	public static boolean EVENT_MOUSE_ALTERNATIVE;
	public static int EVENT_MOUSE_ALTERNATIVE_LVL_GAP;
	public static boolean EVENT_MOUSE_ALTERNATIVE_RATE;
	public static double EVENT_MOUSE_ALT_CHANCE_LVL_40_60;
	public static int EVENT_MOUSE_ALT_COUNT_LVL_40_60;
	public static double EVENT_MOUSE_ALT_CHANCE_LVL_61_75;
	public static int EVENT_MOUSE_ALT_COUNT_LVL_61_75;
	public static double EVENT_MOUSE_ALT_CHANCE_LVL_76_80;
	public static int EVENT_MOUSE_ALT_COUNT_LVL_76_80;
	public static double EVENT_MOUSE_ALT_CHANCE_LVL_81_85;
	public static int EVENT_MOUSE_ALT_COUNT_LVL_81_85;

	public static double SIMPLE_COIN_CHANCE;
	public static int[] SIMPLE_COIN;
	public static int SIMPLE_COIN_MIN_COUNT;
	public static int SIMPLE_COIN_MAX_COUNT;
	public static int SIMPLE_MONSTER_MIN_LEVEL;
	public static int SIMPLE_MONSTER_MAX_LEVEL;

	public static double SIMPLE_COIN_CHANCE_RB;
	public static int[] SIMPLE_COIN_RB;
	public static int SIMPLE_COIN_MIN_COUNT_RB;
	public static int SIMPLE_COIN_MAX_COUNT_RB;
	public static int SIMPLE_MONSTER_MIN_LEVEL_RB;
	public static int SIMPLE_MONSTER_MAX_LEVEL_RB;

	public static int SIMPLE_EVENT_MANAGER_ID;
	public static int SIMPLE_EVENT_MANAGER_MULTISELL;
	public static boolean SIMPLE_RATE_ITEM_BY_HP;
	public static double SERVICES_BONUS_XP;
	public static double SERVICES_BONUS_SP;
	public static double SERVICES_BONUS_ADENA;
	public static double SERVICES_BONUS_ITEMS;
	public static double SERVICES_BONUS_SPOIL;
	public static boolean EVENT_BOUNTY_HUNTERS_ENABLED;

	public static boolean SERVICES_NO_TRADE_ONLY_OFFLINE;
	public static double SERVICES_TRADE_TAX;
	public static double SERVICES_OFFSHORE_TRADE_TAX;
	public static boolean SERVICES_OFFSHORE_NO_CASTLE_TAX;
	public static boolean SERVICES_TRADE_TAX_ONLY_OFFLINE;
	public static boolean SERVICES_TRADE_ONLY_FAR;
	public static int SERVICES_TRADE_RADIUS;
	public static int SERVICES_TRADE_MIN_LEVEL;

	/** Geodata config */
	public static int GEO_X_FIRST, GEO_Y_FIRST, GEO_X_LAST, GEO_Y_LAST;
	public static String GEOFILES_PATTERN;
	public static boolean ALLOW_GEODATA;
	public static boolean ALLOW_FALL_FROM_WALLS;
	public static boolean ALLOW_KEYBOARD_MOVE;
	public static boolean COMPACT_GEO;
	public static int CLIENT_Z_SHIFT;
	public static int MAX_Z_DIFF;
	public static int MIN_LAYER_HEIGHT;

	/** Geodata (Pathfind) config */
	public static int PATHFIND_BOOST;
	public static boolean PATHFIND_DIAGONAL;
	public static boolean PATH_CLEAN;
	public static int PATHFIND_MAX_Z_DIFF;
	public static long PATHFIND_MAX_TIME;
	public static String PATHFIND_BUFFERS;

	public static boolean DEBUG;

	/* Item-Mall Configs */
	public static int GAME_POINT_ITEM_ID;
	public static boolean itemmallEnable;
	public static String LOGIN_DB;

	public static int WEAR_DELAY;

	public static boolean GOODS_INVENTORY_ENABLED = false;
	public static boolean EX_NEW_PETITION_SYSTEM;
	public static boolean EX_JAPAN_MINIGAME;
	public static boolean EX_LECTURE_MARK;
	public static boolean SECOND_AUTH_ENABLED;
	public static boolean SECOND_AUTH_BAN_ACC;
	public static boolean SECOND_AUTH_STRONG_PASS;
	public static int SECOND_AUTH_MAX_ATTEMPTS;
	public static long SECOND_AUTH_BAN_TIME;
	public static String SECOND_AUTH_REC_LINK;
	public static boolean SAVE_ADMIN_SPAWN;

	public static boolean L2_TOP_MANAGER_ENABLED;
	public static int L2_TOP_MANAGER_INTERVAL;
	public static String L2_TOP_WEB_ADDRESS;
	public static String L2_TOP_SMS_ADDRESS;
	public static String L2_TOP_SERVER_ADDRESS;
	public static int L2_TOP_SAVE_DAYS;
	public static int[] L2_TOP_REWARD;
	public static String L2_TOP_SERVER_PREFIX;
	public static int[] L2_TOP_REWARD_NO_CLAN;

	public static boolean MMO_TOP_MANAGER_ENABLED;
	public static int MMO_TOP_MANAGER_INTERVAL;
	public static String MMO_TOP_WEB_ADDRESS;
	public static String MMO_TOP_SERVER_ADDRESS;
	public static int MMO_TOP_SAVE_DAYS;
	public static int[] MMO_TOP_REWARD;
	public static int[] MMO_TOP_REWARD_NO_CLAN;

	public static boolean SMS_PAYMENT_MANAGER_ENABLED;
	public static String SMS_PAYMENT_WEB_ADDRESS;
	public static int SMS_PAYMENT_MANAGER_INTERVAL;
	public static int SMS_PAYMENT_SAVE_DAYS;
	public static String SMS_PAYMENT_SERVER_ADDRESS;
	public static int[] SMS_PAYMENT_REWARD;
	public static int[] SMS_PAYMENT_REWARD_NO_CLAN;
	public static boolean SMS_PAYMENT_TYPE;
	public static String SMS_PAYMENT_PREFIX;

	public static boolean LOGIN_SERVER_GM_ONLY;
	public static boolean LOGIN_SERVER_BRACKETS;
	public static boolean LOGIN_SERVER_IS_PVP;
	public static int LOGIN_SERVER_AGE_LIMIT;
	public static int LOGIN_SERVER_SERVER_TYPE;

	public static boolean ALT_ENABLE_UNDERGROUND_BATTLE_EVENT;
	public static int ALT_MIN_UNDERGROUND_BATTLE_TEAM_MEMBERS;
	public static boolean EVENT_UNDERGROUND_COLISEUM_ONLY_PATY;

	public static boolean EVENT_TVT_ARENA_ENABLED;
	public static int EVENT_TVT_ARENA_TECH_REASON;
	public static int EVENT_TVT_ARENA_NO_PLAYERS;
	public static int EVENT_TVT_ARENA_TEAM_DRAW;
	public static int EVENT_TVT_ARENA_TEAM_WIN;
	public static int EVENT_TVT_ARENA_TEAM_LOSS;
	public static boolean EVENT_TVT_ARENA_ALLOW_CLAN_SKILL;
	public static boolean EVENT_TVT_ARENA_ALLOW_HERO_SKILL;
	public static boolean EVENT_TVT_ARENA_ALLOW_BUFFS;
	public static int EVENT_TVT_ARENA_TEAM_COUNT;
	public static int EVENT_TVT_ARENA_TIME_TO_START;
	public static int EVENT_TVT_ARENA_TEAMLEADER_EXIT;
	public static int EVENT_TVT_ARENA_FIGHT_TIME;
	public static int EVENT_TVT_ARENA_TEAM_COUNT_MIN;
	public static String[] EVENT_TVT_ARENA_START_TIME;
	public static String[] EVENT_TVT_ARENA_STOP_TIME;

	/* Quests */
	public static double _001_ADENA_RATE;
	public static double _001_EXP_RATE;
	public static double _001_SP_RATE;
	public static double _001_ITEM_RATE;

	public static boolean ITEMS_INFINITE_BLESSED_SPIRIT_SHOT;
	public static boolean ITEMS_INFINITE_SPIRIT_SHOT;
	public static boolean ITEMS_INFINITE_SOUL_SHOT;
	public static int[] ITEMS_GET_PREMIUM;
	public static int[] ITEMS_GET_PREMIUM_DAYS;
	public static double[] ITEMS_GET_PREMIUM_VALUE;
	public static int[] ITEMS_GET_PREMIUM_RANDOM_DAYS;
	public static boolean SELL_ALL_ITEMS_FREE;

	/* EnterWorld*/
	public static boolean ENTER_WORLD_ANNOUNCEMENTS_HERO_LOGIN;
	public static boolean ENTER_WORLD_ANNOUNCEMENTS_LORD_LOGIN;
	public static boolean ENTER_WORLD_SHOW_HTML_WELCOME;
	public static boolean ENTER_WORLD_SHOW_HTML_PREMIUM_BUY;
	
	/* Custom Start Items */
	public static boolean STARTING_ITEMS;
	public static List<int[]> STARTING_ITEMS_ID_QTY;
	
	/* Zones Load */
    public static AbnormalEffect SERVICES_OFFLINE_ABNORMAL_EFFECT;
    public static boolean ALLOW_PHANTOM_PLAYERS;
    public static boolean ALLOW_PHANTOM_SETS;
    public static int PHANTOM_MIN_CLASS_ID;
    public static int PHANTOM_MAX_CLASS_ID;
    public static boolean ALLOW_PHANTOM_CHAT;
    public static int PHANTOM_CHAT_CHANSE;
    public static String PHANTOM_PLAYERS_AKK;
    public static String TVT_AREA_ZONE;
	public static String[] EVENTS_TvT_DISALLOWED_SKILLS;
	public static String[] EVENTS_CTF_DISALLOWED_SKILLS;
	public static String[] EVENTS_LH_DISALLOWED_SKILLS;
	public static String[] EVENTS_DM_DISALLOWED_SKILLS;
	public static String[] EVENTS_TVTAREA_DISALLOWED_SKILLS;
    public static int PHANTOM_PLAYERS_COUNT_FIRST;
    public static boolean PHANTOM_PLAYERS_SOULSHOT_ANIM;
    public static long PHANTOM_PLAYERS_DELAY_FIRST;
    public static long PHANTOM_PLAYERS_DESPAWN_FIRST;
    public static int PHANTOM_PLAYERS_DELAY_SPAWN_FIRST;
    public static int PHANTOM_PLAYERS_DELAY_DESPAWN_FIRST;
    public static int PHANTOM_PLAYERS_COUNT_NEXT;
    public static long PHANTOM_PLAYERS_DELAY_NEXT;
    public static long PHANTOM_PLAYERS_DESPAWN_NEXT;
    public static int PHANTOM_PLAYERS_DELAY_SPAWN_NEXT;
    public static int PHANTOM_PLAYERS_DELAY_DESPAWN_NEXT;
    public static int PHANTOM_PLAYERS_ENCHANT_MIN;
    public static int PHANTOM_PLAYERS_ENCHANT_MAX;
    public static long PHANTOM_PLAYERS_CP_REUSE_TIME;
    public static final FastList<Integer> PHANTOM_PLAYERS_NAME_CLOLORS = new FastList<Integer>();
    public static final FastList<Integer> PHANTOM_PLAYERS_TITLE_CLOLORS = new FastList<Integer>();
    public static int PHANTOM_MAX_PATK_BOW;
    public static int PHANTOM_MAX_MDEF_BOW;
    public static int PHANTOM_MAX_PSPD_BOW;
    public static int PHANTOM_MAX_PDEF_BOW;
    public static int PHANTOM_MAX_MATK_BOW;
    public static int PHANTOM_MAX_MSPD_BOW;
    public static int PHANTOM_MAX_HP_BOW;
    public static int PHANTOM_MAX_PATK_MAG;
    public static int PHANTOM_MAX_MDEF_MAG;
    public static int PHANTOM_MAX_PSPD_MAG;
    public static int PHANTOM_MAX_PDEF_MAG;
    public static int PHANTOM_MAX_MATK_MAG;
    public static int PHANTOM_MAX_MSPD_MAG;
    public static int PHANTOM_MAX_HP_MAG;
    public static int PHANTOM_MAX_PATK_HEAL;
    public static int PHANTOM_MAX_MDEF_HEAL;
    public static int PHANTOM_MAX_PSPD_HEAL;
    public static int PHANTOM_MAX_PDEF_HEAL;
    public static int PHANTOM_MAX_MATK_HEAL;
    public static int PHANTOM_MAX_MSPD_HEAL;
    public static int PHANTOM_MAX_HP_HEAL;
    public static boolean EVENT_TvTAllowMultiReg;
	public static String EVENT_TvTCheckWindowMethod;
	public static boolean EVENT_TvTCategories;
	public static String[] EVENT_TvTFighterBuffs;
	public static String[] EVENT_TvTMageBuffs;
	public static boolean EVENT_TvTBuffPlayers;
	public static boolean EVENT_TvTAllowSummons;
	public static boolean EVENT_TvTAllowBuffs;
	public static boolean EVENT_TvTrate;
	public static boolean EVENT_TvTAllowParty;
	public static int EVENT_TvTMaxPlayerInTeam;
	public static int EVENT_TvTMinPlayerInTeam;
	public static int EVENT_TvTTime;
	public static String[] EVENT_TvTRewards;
	public static int[] EVENT_TvTOpenCloseDoors;
	public static int EVENT_TVT_TIME_FOR_FIGHT;

	
	public static void loadTVTSettings()
	{
		ExProperties eventTVTSettings = load(EVENT_TVT_FILE);
		EVENT_TvTStartTime = eventTVTSettings.getProperty("TvT_StartTime", "20:00").trim().replaceAll(" ", "").split(",");
		EVENT_TvTRewards = eventTVTSettings.getProperty("TvT_Rewards", "").trim().replaceAll(" ", "").split(";");
		EVENT_TvTTime = eventTVTSettings.getProperty("TvT_time", 3);
		EVENT_TvTCategories = eventTVTSettings.getProperty("TvT_Categories", false);
		EVENT_TvTMaxPlayerInTeam = eventTVTSettings.getProperty("TvT_MaxPlayerInTeam", 20);
		EVENT_TvTMinPlayerInTeam = eventTVTSettings.getProperty("TvT_MinPlayerInTeam", 2);
		EVENT_TvTAllowSummons = eventTVTSettings.getProperty("TvT_AllowSummons", false);
		EVENT_TvTAllowBuffs = eventTVTSettings.getProperty("TvT_AllowBuffs", false);
		EVENT_TvTAllowMultiReg = eventTVTSettings.getProperty("TvT_AllowMultiReg", false);
		EVENT_TvTCheckWindowMethod = eventTVTSettings.getProperty("TvT_CheckWindowMethod", "IP");
		EVENT_TVT_TIME_FOR_FIGHT = eventTVTSettings.getProperty("TvTEventTime", 20);
		EVENT_TvTFighterBuffs = eventTVTSettings.getProperty("TvT_FighterBuffs", "").trim().replaceAll(" ", "").split(";");
		EVENT_TvTMageBuffs = eventTVTSettings.getProperty("TvT_MageBuffs", "").trim().replaceAll(" ", "").split(";");
		EVENT_TvTBuffPlayers = eventTVTSettings.getProperty("TvT_BuffPlayers", false);
		EVENT_TvTrate = eventTVTSettings.getProperty("TvT_rate", true);
		EVENT_TvTAllowParty = eventTVTSettings.getProperty("TvT_AllowParty", true);
		TVT_AREA_ZONE = eventTVTSettings.getProperty("TvTAreaZone", "[hunter_town_tvt]");
		EVENTS_TvT_DISALLOWED_SKILLS = eventTVTSettings.getProperty("TvT_DisallowedSkills", "").trim().replaceAll(" ", "").split(";");
	}
	
	
    
    public static void loadChatConfig()
	{

	ExProperties chatSettings = load(CHAT_FILE_CONFIG);
	ENT_SHOWENTERMESSON = chatSettings.getProperty("EntWelcomeMessageOn", false);
	ENT_SHOWENTERMESS = String.valueOf(chatSettings.getProperty("EntWelcomeMessage","")).trim();
	GLOBAL_SHOUT = chatSettings.getProperty("GlobalShout", false);
	GLOBAL_TRADE_CHAT = chatSettings.getProperty("GlobalTradeChat", false);
	CHAT_RANGE = chatSettings.getProperty("ChatRange", 1250);
	SHOUT_OFFSET = chatSettings.getProperty("ShoutOffset", 0);
	PREMIUM_HEROCHAT = chatSettings.getProperty("PremiumHeroChat", false);
	PREMIUM_SHOUT_CHAT = chatSettings.getProperty("PremiumShoutChat", false);
	CHAT_MESSAGE_MAX_LEN = chatSettings.getProperty("ChatMessageLimit", 1000);
	ABUSEWORD_BANCHAT = chatSettings.getProperty("ABUSEWORD_BANCHAT", false);
	ADEPT_ENABLE = chatSettings.getProperty("AdeptToTownSay", true);
	TalkGuardChance = chatSettings.getProperty("TalkGuardChance", 90);
	TalkNormalChance = chatSettings.getProperty("TalkNormalChance", 50);
	TalkNormalPeriod = chatSettings.getProperty("TalkNormalPeriod", 20);
	TalkAggroPeriod = chatSettings.getProperty("TalkAggroPeriod", 2);
	int counter = 0;
	for(int id : chatSettings.getProperty("ABUSEWORD_BAN_CHANNEL", new int[] { 0 }))
	{
		BAN_CHANNEL_LIST[counter] = id;
		counter++;
	}
	ABUSEWORD_REPLACE = chatSettings.getProperty("ABUSEWORD_REPLACE", false);
	ABUSEWORD_REPLACE_STRING = chatSettings.getProperty("ABUSEWORD_REPLACE_STRING", "[censored]");
	BANCHAT_ANNOUNCE = chatSettings.getProperty("BANCHAT_ANNOUNCE", true);
	BANCHAT_ANNOUNCE_FOR_ALL_WORLD = chatSettings.getProperty("BANCHAT_ANNOUNCE_FOR_ALL_WORLD", true);
	BANCHAT_ANNOUNCE_NICK = chatSettings.getProperty("BANCHAT_ANNOUNCE_NICK", true);
	ABUSEWORD_BANTIME = chatSettings.getProperty("ABUSEWORD_UNBAN_TIMER", 30);
	RESTRICTED_CHAR_NAMES = chatSettings.getProperty("ListOfRestrictedCharNames", "Admin");
	LIST_RESTRICTED_CHAR_NAMES = new GArray<String>();
	for (String name : RESTRICTED_CHAR_NAMES.split(","))
	{
	LIST_RESTRICTED_CHAR_NAMES.add(name);
	}
	ENABLE_TRADE_BLOCKSPAM = chatSettings.getProperty("EnableTradeBlockList", false);
	if(ENABLE_TRADE_BLOCKSPAM)
	{
		final String tradeBlockList = chatSettings.getProperty("TradeBlockList", "");
		TRADE_LIST = new GArray<String>();
		TRADE_LIST.addAll(Arrays.asList(tradeBlockList.split(",")));
		final String tradeSymbolList = chatSettings.getProperty("TradeSymbolList", "");
		TRADE_LIST_SYMBOLS = new GArray<String>();
		TRADE_LIST_SYMBOLS.addAll(Arrays.asList(tradeSymbolList.split(",")));
	}

	CHATFILTER_MIN_LEVEL = chatSettings.getProperty("ChatFilterMinLevel", 0);
	counter = 0;
	for(int id : chatSettings.getProperty("ChatFilterChannels", new int[] { 1, 8 }))
	{
		CHATFILTER_CHANNELS[counter] = id;
		counter++;
	}
	CHATFILTER_WORK_TYPE = chatSettings.getProperty("ChatFilterWorkType", 1);
	}
    public static void loadLicenseSettings()
    {
		ExProperties LicenseSettings = load(LIC);
      OWNER_NAME = LicenseSettings.getProperty("UserName", "test");
    }
    public static void loadServerConfig()
	{
		ExProperties serverSettings = load(CONFIGURATION_FILE);

        INTERNAL_HOSTNAME = serverSettings.getProperty("InternalHostname", "127.0.0.1");
        EXTERNAL_HOSTNAME = serverSettings.getProperty("ExternalHostname", "127.0.0.1");
        GAME_SERVER_LOGIN_HOST = serverSettings.getProperty("LoginHost", "127.0.0.1");
        GAMESERVER_HOSTNAME = serverSettings.getProperty("GameserverHostname", "127.0.0.1");
        DELAYED_SPAWN_OFF = serverSettings.getProperty("DelayedSpawnOfflike", "OFFLIKE");
        DELAYED_SPAWN_MIN_COUNT = serverSettings.getProperty("DelayedSpawnMinCount", 1);
        DELAYED_SPAWN_MAX_COUNT = serverSettings.getProperty("DelayedSpawnMaxCount", 2);
        DELAYED_SPAWN_TIMEOUT = serverSettings.getProperty("DelayedSpawnTimeout", 15);
        PORTS_GAME = serverSettings.getProperty("GameserverPort", new int[] { 7777 });
		GAME_SERVER_LOGIN_PORT = serverSettings.getProperty("LoginPort", 9013);
		GAME_SERVER_LOGIN_CRYPT = serverSettings.getProperty("LoginUseCrypt", true);
		LOGIN_DB = serverSettings.getProperty("LoginDB", "l2login");

		LOGIN_SERVER_AGE_LIMIT = serverSettings.getProperty("ServerAgeLimit", 0);
		LOGIN_SERVER_GM_ONLY = serverSettings.getProperty("ServerGMOnly", false);
		LOGIN_SERVER_BRACKETS = serverSettings.getProperty("ServerBrackets", false);
		LOGIN_SERVER_IS_PVP = serverSettings.getProperty("PvPServer", false);
		for(String a : serverSettings.getProperty("ServerType", ArrayUtils.EMPTY_STRING_ARRAY))
		{
			if(a.trim().isEmpty())
				continue;

			ServerType t = ServerType.valueOf(a.toUpperCase());
			LOGIN_SERVER_SERVER_TYPE |= t.getMask();
		}
		
		
		REQUEST_ID = serverSettings.getProperty("RequestServerID", 0);
		ACCEPT_ALTERNATE_ID = serverSettings.getProperty("AcceptAlternateID", true);
		

		EVERYBODY_HAS_ADMIN_RIGHTS = serverSettings.getProperty("EverybodyHasAdminRights", false);

		HIDE_GM_STATUS = serverSettings.getProperty("HideGMStatus", false);
		SHOW_GM_LOGIN = serverSettings.getProperty("ShowGMLogin", true);
		SAVE_GM_EFFECTS = serverSettings.getProperty("SaveGMEffects", false);

		CNAME_TEMPLATE = serverSettings.getProperty("CnameTemplate", "[A-Za-z0-9\u0410-\u042f\u0430-\u044f]{2,16}");
		CLAN_NAME_TEMPLATE = serverSettings.getProperty("ClanNameTemplate", "[A-Za-z0-9\u0410-\u042f\u0430-\u044f]{3,16}");
		CLAN_TITLE_TEMPLATE = serverSettings.getProperty("ClanTitleTemplate", "[A-Za-z0-9\u0410-\u042f\u0430-\u044f \\p{Punct}]{1,16}");
		ALLY_NAME_TEMPLATE = serverSettings.getProperty("AllyNameTemplate", "[A-Za-z0-9\u0410-\u042f\u0430-\u044f]{3,16}");

		LOG_CHAT = serverSettings.getProperty("LogChat", false);

		AUTODESTROY_ITEM_AFTER = serverSettings.getProperty("AutoDestroyDroppedItemAfter", 0);
		AUTODESTROY_PLAYER_ITEM_AFTER = serverSettings.getProperty("AutoDestroyPlayerDroppedItemAfter", 0);
		DELETE_DAYS = serverSettings.getProperty("DeleteCharAfterDays", 7);
		PURGE_BYPASS_TASK_FREQUENCY = serverSettings.getProperty("PurgeTaskFrequency", 60);

		SERVER_NAME = serverSettings.getProperty("ServerName", "Bartz");

		try
		{
			DATAPACK_ROOT = new File(serverSettings.getProperty("DatapackRoot", ".")).getCanonicalFile();
		}
		catch(IOException e)
		{
			_log.error("", e);
		}

		ALLOW_DISCARDITEM = serverSettings.getProperty("AllowDiscardItem", true);
		ALLOW_MAIL = serverSettings.getProperty("AllowMail", true);
        ALLOW_MAIL_LVL = serverSettings.getProperty("AllowMailLvL", 1);
        MAIL_TIME = serverSettings.getProperty("AllowMailTime", 10);
		ALLOW_WAREHOUSE = serverSettings.getProperty("AllowWarehouse", true);
		ALLOW_WATER = serverSettings.getProperty("AllowWater", true);

		MIN_PROTOCOL_REVISION = serverSettings.getProperty("MinProtocolRevision", 267);
		MAX_PROTOCOL_REVISION = serverSettings.getProperty("MaxProtocolRevision", 271);

		AUTOSAVE = serverSettings.getProperty("Autosave", true);

		MAXIMUM_ONLINE_USERS = serverSettings.getProperty("MaximumOnlineUsers", 3000);

		DATABASE_DRIVER = serverSettings.getProperty("Driver", "com.mysql.jdbc.Driver");
		DATABASE_URL = serverSettings.getProperty("URL", "jdbc:mysql://localhost/l2jdb");
		DATABASE_LOGIN = serverSettings.getProperty("Login", "root");
		DATABASE_PASSWORD = serverSettings.getProperty("Password", "");
		
		DATABASE_MAX_CONNECTIONS = serverSettings.getProperty("MaximumDbConnections", 10);
		DATABASE_MAX_IDLE_TIMEOUT = serverSettings.getProperty("MaxIdleConnectionTimeout", 600);
		DATABASE_IDLE_TEST_PERIOD = serverSettings.getProperty("IdleConnectionTestPeriod", 60);

		USER_INFO_INTERVAL = serverSettings.getProperty("UserInfoInterval", 100L);
		BROADCAST_STATS_INTERVAL = serverSettings.getProperty("BroadcastStatsInterval", true);
		BROADCAST_CHAR_INFO_INTERVAL = serverSettings.getProperty("BroadcastCharInfoInterval", 100L);

		EFFECT_TASK_MANAGER_COUNT = serverSettings.getProperty("EffectTaskManagers", 2);

		SCHEDULED_THREAD_POOL_SIZE = serverSettings.getProperty("ScheduledThreadPoolSize", NCPUS * 4);
		EXECUTOR_THREAD_POOL_SIZE = serverSettings.getProperty("ExecutorThreadPoolSize", NCPUS * 2);

		ENABLE_RUNNABLE_STATS = serverSettings.getProperty("EnableRunnableStats", false);

		SELECTOR_CONFIG.SLEEP_TIME = serverSettings.getProperty("SelectorSleepTime", 10L);
		SELECTOR_CONFIG.INTEREST_DELAY = serverSettings.getProperty("InterestDelay", 30L);
		SELECTOR_CONFIG.MAX_SEND_PER_PASS = serverSettings.getProperty("MaxSendPerPass", 32);
		SELECTOR_CONFIG.READ_BUFFER_SIZE = serverSettings.getProperty("ReadBufferSize", 65536);
		SELECTOR_CONFIG.WRITE_BUFFER_SIZE = serverSettings.getProperty("WriteBufferSize", 131072);
		SELECTOR_CONFIG.HELPER_BUFFER_COUNT = serverSettings.getProperty("BufferPoolSize", 64);

		DEFAULT_LANG = serverSettings.getProperty("DefaultLang", "ru");
		RESTART_AT_TIME = serverSettings.getProperty("AutoRestartAt", "0 5 * * *");

		MOVE_PACKET_DELAY = serverSettings.getProperty("MovePacketDelay", 100);
		ATTACK_PACKET_DELAY = serverSettings.getProperty("AttackPacketDelay", 500);

		DONTLOADSPAWN = serverSettings.getProperty("StartWithoutSpawn", false);
		DONTLOADQUEST = serverSettings.getProperty("StartWithoutQuest", false);

		MAX_REFLECTIONS_COUNT = serverSettings.getProperty("MaxReflectionsCount", 300);

		WEAR_DELAY = serverSettings.getProperty("WearDelay", 5);

		HTM_CACHE_MODE = serverSettings.getProperty("HtmCacheMode", HtmCache.LAZY);
	}
	public static void loadVersionSettings()
	{
		VERSION = new Version(GameServer.class);
		SERVER_VERSION = VERSION.getVersionNumber();
		SERVER_REVISION = VERSION.getRevisionNumber();
		SERVER_BUILD_DATE = VERSION.getBuildDate();
	}
	public static void loadTelnetConfig()
	{
		ExProperties telnetSettings = load(TELNET_CONFIGURATION_FILE);

		IS_TELNET_ENABLED = telnetSettings.getProperty("EnableTelnet", false);
		TELNET_DEFAULT_ENCODING = telnetSettings.getProperty("TelnetEncoding", "UTF-8");
		TELNET_PORT = telnetSettings.getProperty("Port", 7000);
		TELNET_HOSTNAME = telnetSettings.getProperty("BindAddress", "127.0.0.1");
		TELNET_PASSWORD = telnetSettings.getProperty("Password", "");
	}

	public static void loadResidenceConfig()
	{
		ExProperties residenceSettings = load(RESIDENCE_CONFIG_FILE);

		CH_BID_GRADE1_MINCLANLEVEL = residenceSettings.getProperty("ClanHallBid_Grade1_MinClanLevel", 2);
		CH_BID_GRADE1_MINCLANMEMBERS = residenceSettings.getProperty("ClanHallBid_Grade1_MinClanMembers", 1);
		CH_BID_GRADE1_MINCLANMEMBERSLEVEL = residenceSettings.getProperty("ClanHallBid_Grade1_MinClanMembersAvgLevel", 1);
		CH_BID_GRADE2_MINCLANLEVEL = residenceSettings.getProperty("ClanHallBid_Grade2_MinClanLevel", 2);
		CH_BID_GRADE2_MINCLANMEMBERS = residenceSettings.getProperty("ClanHallBid_Grade2_MinClanMembers", 1);
		CH_BID_GRADE2_MINCLANMEMBERSLEVEL = residenceSettings.getProperty("ClanHallBid_Grade2_MinClanMembersAvgLevel", 1);
		CH_BID_GRADE3_MINCLANLEVEL = residenceSettings.getProperty("ClanHallBid_Grade3_MinClanLevel", 2);
		CH_BID_GRADE3_MINCLANMEMBERS = residenceSettings.getProperty("ClanHallBid_Grade3_MinClanMembers", 1);
		CH_BID_GRADE3_MINCLANMEMBERSLEVEL = residenceSettings.getProperty("ClanHallBid_Grade3_MinClanMembersAvgLevel", 1);
		RESIDENCE_LEASE_FUNC_MULTIPLIER = residenceSettings.getProperty("ResidenceLeaseFuncMultiplier", 1.);
		RESIDENCE_LEASE_MULTIPLIER = residenceSettings.getProperty("ResidenceLeaseMultiplier", 1.);
		CASTLE_SELECT_HOURS = residenceSettings.getProperty("CastleSelectHours", new int[] { 16, 20 });
        CASTLE_SIEGE_PERIOD = residenceSettings.getProperty("CastleSiegePeriod", 14);
        TW_SIEGE_DAY = residenceSettings.getProperty("TWSiegePeriod", 14);
	}

	public static void loadOtherConfig()
	{
		ExProperties otherSettings = load(OTHER_CONFIG_FILE);

		DEEPBLUE_DROP_RULES = otherSettings.getProperty("UseDeepBlueDropRules", true);
		DEEPBLUE_DROP_MAXDIFF = otherSettings.getProperty("DeepBlueDropMaxDiff", 8);
		DEEPBLUE_DROP_RAID_MAXDIFF = otherSettings.getProperty("DeepBlueDropRaidMaxDiff", 2);

		SWIMING_SPEED = otherSettings.getProperty("SwimingSpeedTemplate", 50);

		/* Inventory slots limits */
		INVENTORY_MAXIMUM_NO_DWARF = otherSettings.getProperty("MaximumSlotsForNoDwarf", 80);
		INVENTORY_MAXIMUM_DWARF = otherSettings.getProperty("MaximumSlotsForDwarf", 100);
		INVENTORY_MAXIMUM_GM = otherSettings.getProperty("MaximumSlotsForGMPlayer", 250);
		QUEST_INVENTORY_MAXIMUM = otherSettings.getProperty("MaximumSlotsForQuests", 100);

		MULTISELL_SIZE = otherSettings.getProperty("MultisellPageSize", 10);

		/* Warehouse slots limits */
		WAREHOUSE_SLOTS_NO_DWARF = otherSettings.getProperty("BaseWarehouseSlotsForNoDwarf", 100);
		WAREHOUSE_SLOTS_DWARF = otherSettings.getProperty("BaseWarehouseSlotsForDwarf", 120);
		WAREHOUSE_SLOTS_CLAN = otherSettings.getProperty("MaximumWarehouseSlotsForClan", 200);
		FREIGHT_SLOTS = otherSettings.getProperty("MaximumFreightSlots", 10);

		REGEN_SIT_WAIT = otherSettings.getProperty("RegenSitWait", false);
		/* Amount of HP, MP, and CP is restored */
		RESPAWN_RESTORE_CP = otherSettings.getProperty("RespawnRestoreCP", 0.) / 100;
		RESPAWN_RESTORE_HP = otherSettings.getProperty("RespawnRestoreHP", 65.) / 100;
		RESPAWN_RESTORE_MP = otherSettings.getProperty("RespawnRestoreMP", 0.) / 100;

		/* Maximum number of available slots for pvt stores */
		MAX_PVTSTORE_SLOTS_DWARF = otherSettings.getProperty("MaxPvtStoreSlotsDwarf", 5);
		MAX_PVTSTORE_SLOTS_OTHER = otherSettings.getProperty("MaxPvtStoreSlotsOther", 4);
		MAX_PVTCRAFT_SLOTS = otherSettings.getProperty("MaxPvtManufactureSlots", 20);

		SENDSTATUS_TRADE_JUST_OFFLINE = otherSettings.getProperty("SendStatusTradeJustOffline", false);
		SENDSTATUS_TRADE_MOD = otherSettings.getProperty("SendStatusTradeMod", 1.);

		ANNOUNCE_MAMMON_SPAWN = otherSettings.getProperty("AnnounceMammonSpawn", true);

		GM_NAME_COLOUR = Integer.decode("0x" + otherSettings.getProperty("GMNameColour", "FFFFFF"));
		GM_HERO_AURA = otherSettings.getProperty("GMHeroAura", false);
		NORMAL_NAME_COLOUR = Integer.decode("0x" + otherSettings.getProperty("NormalNameColour", "FFFFFF"));
		CLANLEADER_NAME_COLOUR = Integer.decode("0x" + otherSettings.getProperty("ClanleaderNameColour", "FFFFFF"));
		PREMIUM_NAME_COLOUR = Integer.decode("0x" + otherSettings.getProperty("PremiumNameColour", "FFFFFF"));

		ALLOW_CURSED_WEAPONS = otherSettings.getProperty("AllowCursedWeapons", false);
		DROP_CURSED_WEAPONS_ON_KICK = otherSettings.getProperty("DropCursedWeaponsOnKick", false);

		ALT_VITALITY_NEVIT_UP_POINT = otherSettings.getProperty("AltVitalityNevitUpPoint", 100);
		ALT_VITALITY_NEVIT_POINT = otherSettings.getProperty("AltVitalityNevitPoint", 100);
		ENABLE_FLAG_ATTACK_MOB = otherSettings.getProperty("EnableFlagAttackMob", false);
		if(ENABLE_FLAG_ATTACK_MOB)
		{
			final String mobList = otherSettings.getProperty("FlagMobList", "");
			FLAG_MOB_LIST = new GArray<Integer>();
			for(final String id : mobList.trim().split(","))
			FLAG_MOB_LIST.add(Integer.parseInt(id.trim()));
}

	}
	public static void loaditemmalSettings()
	{
	  ExProperties itemmalSettings = load(ITEM_MALL_CONFIG_FILE);
	  GAME_POINT_ITEM_ID = itemmalSettings.getProperty("GamePointItemId", 0);
	  itemmallEnable = itemmalSettings.getProperty("ItemMallEnable", false);
	  OTHER_ITEM_MALL_MAX_BUY_COUNT = itemmalSettings.getProperty("ItemMallMaxBuyCount", 99);
	}

	public static void loadEnchantSettings()
	{
		ExProperties EnchantSettings = load(ENCHANT_CONFIG_FILE);

		USE_OFFLIKE_ENCHANT = EnchantSettings.getProperty("Offlike", false);
		USE_OFFLIKE_ENCHANT_MAGE_WEAPON = EnchantSettings.getProperty("OffMage", false);
		USE_OFFLIKE_ENCHANT_MAGE_WEAPON_CHANCE = EnchantSettings.getProperty("OffMChance", 0.6667);

		OFFLIKE_ENCHANT_WEAPON = EnchantSettings.getProperty("OffWeapon", new int[] { 0 });
		OFFLIKE_ENCHANT_WEAPON_BLESSED = EnchantSettings.getProperty("OffCWeapon", new int[] { 0 });
		OFFLIKE_ENCHANT_WEAPON_CRYSTAL = EnchantSettings.getProperty("OffBWeapon", new int[] { 0 });
		OFFLIKE_ENCHANT_ARMOR = EnchantSettings.getProperty("OffArmor", new int[] { 0 });
		OFFLIKE_ENCHANT_ARMOR_CRYSTAL = EnchantSettings.getProperty("OffCArmor", new int[] { 0 });
		OFFLIKE_ENCHANT_ARMOR_BLESSED = EnchantSettings.getProperty("OffBArmor", new int[] { 0 });
		OFFLIKE_ENCHANT_ARMOR_JEWELRY = EnchantSettings.getProperty("OffJewelry", new int[] { 0 });
		OFFLIKE_ENCHANT_ARMOR_JEWELRY_CRYSTAL = EnchantSettings.getProperty("OffCJewelry", new int[] { 0 });
		OFFLIKE_ENCHANT_ARMOR_JEWELRY_BLESSED = EnchantSettings.getProperty("OffBJewelry", new int[] { 0 });

		OFFLIKE_PREMIUM_ENCHANT_WEAPON = EnchantSettings.getProperty("OffPWeapon", new int[] { 0 });
		OFFLIKE_PREMIUM_ENCHANT_WEAPON_BLESSED = EnchantSettings.getProperty("OffPCWeapon", new int[] { 0 });
		OFFLIKE_PREMIUM_ENCHANT_WEAPON_CRYSTAL = EnchantSettings.getProperty("OffPBWeapon", new int[] { 0 });
		OFFLIKE_PREMIUM_ENCHANT_ARMOR = EnchantSettings.getProperty("OffPArmor", new int[] { 0 });
		OFFLIKE_PREMIUM_ENCHANT_ARMOR_CRYSTAL = EnchantSettings.getProperty("OffPCArmor", new int[] { 0 });
		OFFLIKE_PREMIUM_ENCHANT_ARMOR_BLESSED = EnchantSettings.getProperty("OffPBArmor", new int[] { 0 });
		OFFLIKE_PREMIUM_ENCHANT_ARMOR_JEWELRY = EnchantSettings.getProperty("OffPJewelry", new int[] { 0 });
		OFFLIKE_PREMIUM_ENCHANT_ARMOR_JEWELRY_CRYSTAL = EnchantSettings.getProperty("OffPCJewelry", new int[] { 0 });
		OFFLIKE_PREMIUM_ENCHANT_ARMOR_JEWELRY_BLESSED = EnchantSettings.getProperty("OffPBJewelry", new int[] { 0 });

		ENCHANT_CHANCE_WEAPON = EnchantSettings.getProperty("WChance", 66);
		PREMIUM_ENCHANT_CHANCE_WEAPON = EnchantSettings.getProperty("PWChance", 76);
		ENCHANT_CHANCE_CRYSTAL_WEAPON = EnchantSettings.getProperty("BWChance", 76);
		PREMIUM_ENCHANT_CHANCE_CRYSTAL_WEAPON = EnchantSettings.getProperty("BPWChance", 86);

		ENCHANT_CHANCE_ARMOR = EnchantSettings.getProperty("AChance", 66);
		PREMIUM_ENCHANT_CHANCE_ARMOR = EnchantSettings.getProperty("PAChance", 76);
		ENCHANT_CHANCE_CRYSTAL_ARMOR = EnchantSettings.getProperty("BAChance", 76);
		PREMIUM_ENCHANT_CHANCE_CRYSTAL_ARMOR = EnchantSettings.getProperty("BPAChance", 86);

		ENCHANT_CHANCE_ACCESSORY = EnchantSettings.getProperty("JChance", 66);
		PREMIUM_ENCHANT_CHANCE_ACCESSORY = EnchantSettings.getProperty("PJChance", 76);
		ENCHANT_CHANCE_CRYSTAL_ACCESSORY = EnchantSettings.getProperty("BJChance", 76);
		PREMIUM_ENCHANT_CHANCE_CRYSTAL_ACCESSORY = EnchantSettings.getProperty("BPJChance", 86);

		SAFE_ENCHANT_COMMON = EnchantSettings.getProperty("Safe", 3);
		SAFE_ENCHANT_FULL_BODY = EnchantSettings.getProperty("FBSafe", 4);

		ENCHANT_MAX_WEAPON = EnchantSettings.getProperty("MaxWeapon", 20);
		ENCHANT_MAX_SHIELD_ARMOR = EnchantSettings.getProperty("MaxArmor", 20);
		ENCHANT_MAX_ACCESSORY = EnchantSettings.getProperty("MaxAccessory", 20);

		ENCHANT_CRYSTAL_FAILED = EnchantSettings.getProperty("BFailed", 0);

		ENCHANT_SCROLL_LEVEL_WEAPON = EnchantSettings.getProperty("WLevel", 1);
		ENCHANT_SCROLL_LEVEL_ARMOR = EnchantSettings.getProperty("ALevel", 1);
		ENCHANT_SCROLL_LEVEL_ACCESSORY = EnchantSettings.getProperty("JLevel", 1);

		ARMOR_OVERENCHANT_HPBONUS_LIMIT = EnchantSettings.getProperty("HPBonus", 10) - 3;

		SHOW_ENCHANT_EFFECT_RESULT = EnchantSettings.getProperty("EffectResult", false);

		ENCHANT_ATTRIBUTE_STONE_CHANCE = EnchantSettings.getProperty("AttChance", 50);
		ENCHANT_ATTRIBUTE_CRYSTAL_CHANCE = EnchantSettings.getProperty("CAttChance", 30);

		SHOW_ENCHANT_RESULT_UP_3 = EnchantSettings.getProperty("ResultUP3", false);
	}

	public static void loadSpoilConfig()
	{
		ExProperties spoilSettings = load(SPOIL_CONFIG_FILE);

		BASE_SPOIL_RATE = spoilSettings.getProperty("BasePercentChanceOfSpoilSuccess", 78.);
		MINIMUM_SPOIL_RATE = spoilSettings.getProperty("MinimumPercentChanceOfSpoilSuccess", 1.);
		ALT_SPOIL_FORMULA = spoilSettings.getProperty("AltFormula", false);
		MANOR_SOWING_BASIC_SUCCESS = spoilSettings.getProperty("BasePercentChanceOfSowingSuccess", 100.);
		MANOR_SOWING_ALT_BASIC_SUCCESS = spoilSettings.getProperty("BasePercentChanceOfSowingAltSuccess", 10.);
		MANOR_HARVESTING_BASIC_SUCCESS = spoilSettings.getProperty("BasePercentChanceOfHarvestingSuccess", 90.);
		MANOR_DIFF_PLAYER_TARGET = spoilSettings.getProperty("MinDiffPlayerMob", 5);
		MANOR_DIFF_PLAYER_TARGET_PENALTY = spoilSettings.getProperty("DiffPlayerMobPenalty", 5.);
		MANOR_DIFF_SEED_TARGET = spoilSettings.getProperty("MinDiffSeedMob", 5);
		MANOR_DIFF_SEED_TARGET_PENALTY = spoilSettings.getProperty("DiffSeedMobPenalty", 5.);
		ALLOW_MANOR = spoilSettings.getProperty("AllowManor", true);
		MANOR_REFRESH_TIME = spoilSettings.getProperty("AltManorRefreshTime", 20);
		MANOR_REFRESH_MIN = spoilSettings.getProperty("AltManorRefreshMin", 0);
		MANOR_APPROVE_TIME = spoilSettings.getProperty("AltManorApproveTime", 6);
		MANOR_APPROVE_MIN = spoilSettings.getProperty("AltManorApproveMin", 0);
		MANOR_MAINTENANCE_PERIOD = spoilSettings.getProperty("AltManorMaintenancePeriod", 360000);
	}

	public static void loadFormulasConfig()
	{
		ExProperties formulasSettings = load(FORMULAS_CONFIGURATION_FILE);

		SKILLS_CHANCE_SHOW = formulasSettings.getProperty("ShowChance", false);
		FORMULA_SKILLS_CHANCE_MOD = formulasSettings.getProperty("SkillsChanceMod", 11.);
		FORMULA_SKILLS_CHANCE_POW = formulasSettings.getProperty("SkillsChancePow", 0.5);
		FORMULA_SKILLS_CHANCE_MIN = formulasSettings.getProperty("SkillsChanceMin", 5.);
		FORMULA_SKILLS_CHANCE_CAP = formulasSettings.getProperty("SkillsChanceCap", 95.);
		FORMULA_SKILLS_CAST_TIME_MIN = formulasSettings.getProperty("SkillsCastTimeMin", 333);
		FORMULA_ABSORB_DAMAGE_MODIFIER = formulasSettings.getProperty("AbsorbDamageModifier", 1.0);
		FORMULA_LIM_PATK = formulasSettings.getProperty("LimitPatk", 20000);
		FORMULA_LIM_MATK = formulasSettings.getProperty("LimitMAtk", 25000);
		FORMULA_LIM_PDEF = formulasSettings.getProperty("LimitPDef", 15000);
		FORMULA_LIM_MDEF = formulasSettings.getProperty("LimitMDef", 15000);
		FORMULA_LIM_PATK_SPD = formulasSettings.getProperty("LimitPatkSpd", 1500);
		FORMULA_LIM_MATK_SPD = formulasSettings.getProperty("LimitMatkSpd", 1999);
		FORMULA_LIM_CRIT_DAM = formulasSettings.getProperty("LimitCriticalDamage", 2000);
		FORMULA_LIM_CRIT = formulasSettings.getProperty("LimitCritical", 500);
		FORMULA_LIM_MCRIT = formulasSettings.getProperty("LimitMCritical", 20);
		FORMULA_LIM_ACCURACY = formulasSettings.getProperty("LimitAccuracy", 200);
		FORMULA_LIM_EVASION = formulasSettings.getProperty("LimitEvasion", 200);
		FORMULA_LIM_MOVE = formulasSettings.getProperty("LimitMove", 250);
		FORMULA_LIM_FAME = formulasSettings.getProperty("LimitFame", 50000);
		FORMULA_POLE_DAMAGE_MODIFIER = formulasSettings.getProperty("PoleDamageModifier", 1.0);
	}
    public static void loadPhantomsConfig() {
        ExProperties PhantomsSettings = load(PHANTOM_FILE);

        ALLOW_PHANTOM_PLAYERS = PhantomsSettings.getProperty("AllowPhantomPlayers", false);
        ALLOW_PHANTOM_SETS = PhantomsSettings.getProperty("AllowPhantomSets", false);
        PHANTOM_MIN_CLASS_ID = PhantomsSettings.getProperty("PhantomMinClassId", 0);
        PHANTOM_MAX_CLASS_ID = PhantomsSettings.getProperty("PhantomMaxClassId", 122);
        ALLOW_PHANTOM_CHAT = PhantomsSettings.getProperty("AllowPhantomPlayersChat", false);
        PHANTOM_CHAT_CHANSE = PhantomsSettings.getProperty("PhantomPlayersChatChance", 1);

        PHANTOM_PLAYERS_AKK = PhantomsSettings.getProperty("PhantomPlayerAccounts", "l2-dream.ru");
        PHANTOM_PLAYERS_SOULSHOT_ANIM = PhantomsSettings.getProperty("PhantomSoulshotAnimation", true);
        PHANTOM_PLAYERS_COUNT_FIRST = PhantomsSettings.getProperty("FirstCount", 50);
        PHANTOM_PLAYERS_DELAY_FIRST = PhantomsSettings.getProperty("FirstDelay", 5);
        PHANTOM_PLAYERS_DESPAWN_FIRST = TimeUnit.MINUTES.toMillis(PhantomsSettings.getProperty("FirstDespawn", 60));
        PHANTOM_PLAYERS_DELAY_SPAWN_FIRST = (int) TimeUnit.SECONDS.toMillis(PhantomsSettings.getProperty("FirstDelaySpawn", 1));
        PHANTOM_PLAYERS_DELAY_DESPAWN_FIRST = (int) TimeUnit.SECONDS.toMillis(PhantomsSettings.getProperty("FirstDelayDespawn", 20));
        PHANTOM_PLAYERS_COUNT_NEXT = PhantomsSettings.getProperty("NextCount", 50);
        PHANTOM_PLAYERS_CP_REUSE_TIME = PhantomsSettings.getProperty("CpReuseTime", 200);
        PHANTOM_PLAYERS_DELAY_NEXT = TimeUnit.MINUTES.toMillis(PhantomsSettings.getProperty("NextDelay", 15));
        PHANTOM_PLAYERS_DESPAWN_NEXT = TimeUnit.MINUTES.toMillis(PhantomsSettings.getProperty("NextDespawn", 90));
        PHANTOM_PLAYERS_DELAY_SPAWN_NEXT = (int) TimeUnit.SECONDS.toMillis(PhantomsSettings.getProperty("NextDelaySpawn", 20));
        PHANTOM_PLAYERS_DELAY_DESPAWN_NEXT = (int) TimeUnit.SECONDS.toMillis(PhantomsSettings.getProperty("NextDelayDespawn", 30));
        String[] ppp = PhantomsSettings.getProperty("FakeEnchant", "0,14").split(",");
        PHANTOM_PLAYERS_ENCHANT_MIN = Integer.parseInt(ppp[0]);
        PHANTOM_PLAYERS_ENCHANT_MAX = Integer.parseInt(ppp[1]);
        ppp = PhantomsSettings.getProperty("FakeNameColors", "FFFFFF,FFFFFF").split(",");
        for (String ncolor : ppp) {
            String nick = new TextBuilder(ncolor).reverse().toString();
            PHANTOM_PLAYERS_NAME_CLOLORS.add(Integer.decode("0x" + nick));
        }
        ppp = PhantomsSettings.getProperty("FakeTitleColors", "FFFF77,FFFF77").split(",");
        for (String tcolor : ppp) {
            String title = new TextBuilder(tcolor).reverse().toString();
            PHANTOM_PLAYERS_TITLE_CLOLORS.add(Integer.decode("0x" + title));
        }
    }
	public static void loadExtSettings()
	{
		ExProperties properties = load(EXT_FILE);

		SAVE_ADMIN_SPAWN = properties.getProperty("SaveAdminSpawn", false);
		
		EX_NEW_PETITION_SYSTEM = properties.getProperty("NewPetitionSystem", false);
		EX_JAPAN_MINIGAME = properties.getProperty("JapanMinigame", false);
		EX_LECTURE_MARK = properties.getProperty("LectureMark", false);

		SECOND_AUTH_ENABLED = properties.getProperty("SAEnabled", false);
		SECOND_AUTH_BAN_ACC = properties.getProperty("SABanAccEnabled", false);
		SECOND_AUTH_STRONG_PASS = properties.getProperty("SAStrongPass", false);
		SECOND_AUTH_MAX_ATTEMPTS = properties.getProperty("SAMaxAttemps", 5);
		SECOND_AUTH_BAN_TIME = properties.getProperty("SABanTime", 480L);
		SECOND_AUTH_REC_LINK = properties.getProperty("SARecoveryLink", "http://www.my-domain.com/charPassRec.php");
		ENABLE_AUTO_HUNTING_REPORT = properties.getProperty("AllowAutoHuntingReport", true);

	}

	public static void loadTopSettings()
	{
		ExProperties topSettings = load(TOP_FILE);

		L2_TOP_MANAGER_ENABLED = topSettings.getProperty("L2TopManagerEnabled", false);
		L2_TOP_MANAGER_INTERVAL = topSettings.getProperty("L2TopManagerInterval", 300000);
		L2_TOP_WEB_ADDRESS = topSettings.getProperty("L2TopWebAddress", "");
		L2_TOP_SMS_ADDRESS = topSettings.getProperty("L2TopSmsAddress", "");
		L2_TOP_SERVER_ADDRESS = topSettings.getProperty("L2TopServerAddress", "L2Dream.ru");
		L2_TOP_SAVE_DAYS = topSettings.getProperty("L2TopSaveDays", 30);
		L2_TOP_REWARD = topSettings.getProperty("L2TopReward", new int[0]);
		L2_TOP_SERVER_PREFIX = topSettings.getProperty("L2TopServerPrefix", "");
		L2_TOP_REWARD_NO_CLAN = topSettings.getProperty("L2TopRewardNoClan", new int[0]);

		MMO_TOP_MANAGER_ENABLED = topSettings.getProperty("MMOTopEnable", false);
		MMO_TOP_MANAGER_INTERVAL = topSettings.getProperty("MMOTopManagerInterval", 300000);
		MMO_TOP_WEB_ADDRESS = topSettings.getProperty("MMOTopUrl", "");
		MMO_TOP_SERVER_ADDRESS = topSettings.getProperty("MMOTopServerAddress", "L2Dream.ru");
		MMO_TOP_SAVE_DAYS = topSettings.getProperty("MMOTopSaveDays", 30);
		MMO_TOP_REWARD = topSettings.getProperty("MMOTopReward", new int[0]);
		MMO_TOP_REWARD_NO_CLAN = topSettings.getProperty("MMOTopRewardNoClan", new int[0]);
	}

	public static void loadAltSettings()
	{
		ExProperties altSettings = load(ALT_SETTINGS_FILE);

		ALT_ARENA_EXP = altSettings.getProperty("ArenaExp", true);
		ALT_GAME_DELEVEL = altSettings.getProperty("Delevel", true);
		ALT_AUTO_LOOT = altSettings.getProperty("AutoLoot", false);
		ALT_AUTO_LOOT_HERBS = altSettings.getProperty("AutoLootHerbs", false);
		ALT_AUTO_LOOT_INDIVIDUAL = altSettings.getProperty("AutoLootIndividual", false);
		ALT_AUTO_LOOT_FROM_RAIDS = altSettings.getProperty("AutoLootFromRaids", false);
		ALT_AUTO_LOOT_PK = altSettings.getProperty("AutoLootPK", false);
		SELL_ALL_ITEMS_FREE = altSettings.getProperty("SellAllItemsFree", false);
		ALT_GAME_KARMA_PLAYER_CAN_SHOP = altSettings.getProperty("AltKarmaPlayerCanShop", false);
		ALT_SAVE_EFFECTS_REMAINING_TIME = altSettings.getProperty("AltSaveEffectsRemainingTime", 5);
		ALT_DELETE_SA_BUFFS = altSettings.getProperty("AltDeleteSABuffs", false);
		ALT_SAVING_SPS = altSettings.getProperty("SavingSpS", false);
		ALT_MANAHEAL_SPS_BONUS = altSettings.getProperty("ManahealSpSBonus", false);
		ALT_CRAFT_MASTERWORK_CHANCE = altSettings.getProperty("CraftMasterworkChance", 3.);
		ALT_CRAFT_DOUBLECRAFT_CHANCE = altSettings.getProperty("CraftDoubleCraftChance", 3.);
		ALT_GAME_UNREGISTER_RECIPE = altSettings.getProperty("AltUnregisterRecipe", true);
		ALT_ALLOW_NPC_SHIFTCLICK = altSettings.getProperty("AllowShiftClick", true);
		ALT_GAME_SHOW_DROPLIST = altSettings.getProperty("AltShowDroplist", true);
		ALT_FULL_NPC_STATS_PAGE = altSettings.getProperty("AltFullStatsPage", false);
		ALT_GAME_SUBCLASS_WITHOUT_QUESTS = altSettings.getProperty("AltAllowSubClassWithoutQuest", false);
		ALT_GAME_LEVEL_TO_GET_SUBCLASS = altSettings.getProperty("AltLevelToGetSubclass", 75);
		ALT_GAME_SUB_ADD = altSettings.getProperty("AltSubAdd", 0);
		ALT_MAX_LEVEL = Math.min(altSettings.getProperty("AltMaxLevel", 85), Experience.LEVEL.length - 1);
		ALT_MAX_SUB_LEVEL = Math.min(altSettings.getProperty("AltMaxSubLevel", 80), Experience.LEVEL.length - 1);
		ALT_ADD_RECIPES = altSettings.getProperty("AltAddRecipes", 0);
		ALT_MAX_ALLY_SIZE = altSettings.getProperty("AltMaxAllySize", 3);
		ENABLE_KM_ALL_TO_ME = altSettings.getProperty("EnableKmAllToMe", false);
		ALT_GAME_REQUIRE_CLAN_CASTLE = altSettings.getProperty("AltRequireClanCastle", false);
		ALT_GAME_REQUIRE_CASTLE_DAWN = altSettings.getProperty("AltRequireCastleDawn", true);
		ALT_GAME_ALLOW_ADENA_DAWN = altSettings.getProperty("AltAllowAdenaDawn", true);
		ALT_ALLOW_OTHERS_WITHDRAW_FROM_CLAN_WAREHOUSE = altSettings.getProperty("AltAllowOthersWithdrawFromClanWarehouse", false);
		ALT_ALLOW_CLAN_COMMAND_ONLY_FOR_CLAN_LEADER = altSettings.getProperty("AltAllowClanCommandOnlyForClanLeader", true);
		ALT_SS_ANNOUNCE_PERIOD = altSettings.getProperty("SSAnnouncePeriod", 0);
		ALT_ALLOW_AUGMENT_ALL = altSettings.getProperty("AugmentAll", false);
		ALT_DEATH_PENALTY = altSettings.getProperty("EnableAltDeathPenalty", false);
		ALT_PK_DEATH_RATE = altSettings.getProperty("AltPKDeathRate", 0.);
		ALT_ALLOW_DEATH_PENALTY_C5 = altSettings.getProperty("EnableDeathPenaltyC5", true);
		ALT_DEATH_PENALTY_C5_CHANCE = altSettings.getProperty("DeathPenaltyC5Chance", 10);
		ALT_DEATH_PENALTY_C5_CHAOTIC_RECOVERY = altSettings.getProperty("ChaoticCanUseScrollOfRecovery", false);
		ALT_DEATH_PENALTY_C5_EXPERIENCE_PENALTY = altSettings.getProperty("DeathPenaltyC5RateExpPenalty", 1);
		ALT_DEATH_PENALTY_C5_KARMA_PENALTY = altSettings.getProperty("DeathPenaltyC5RateKarma", 1);
		ALT_SIMPLE_SIGNS = altSettings.getProperty("PushkinSignsOptions", false);
		ALT_TELE_TO_CATACOMBS = altSettings.getProperty("TeleToCatacombs", false);
		ALT_BS_CRYSTALLIZE = altSettings.getProperty("BSCrystallize", false);
		ALT_MAMMON_UPGRADE = altSettings.getProperty("MammonUpgrade", 6680500);
		ALT_MAMMON_EXCHANGE = altSettings.getProperty("MammonExchange", 10091400);
		ALT_ALLOW_SELL_COMMON = altSettings.getProperty("AllowSellCommon", true);
		ALT_ALLOW_SHADOW_WEAPONS = altSettings.getProperty("AllowShadowWeapons", true);
		ALT_ALLOW_TATTOO = altSettings.getProperty("AllowTattoo", false);
		ALT_DISABLED_MULTISELL = altSettings.getProperty("DisabledMultisells", new int[] {});
		ALT_SHOP_PRICE_LIMITS = altSettings.getProperty("ShopPriceLimits", new int[] {});
		ALT_SHOP_UNALLOWED_ITEMS = altSettings.getProperty("ShopUnallowedItems", new int[] {});
		ALT_ALLOWED_PET_POTIONS = altSettings.getProperty("AllowedPetPotions", new int[] { 735, 1060, 1061, 1062, 1374, 1375, 1539, 1540, 6035, 6036 });
		ALT_BUFF_LIMIT = altSettings.getProperty("BuffLimit", 20);
		ALT_SONG_LIMIT = altSettings.getProperty("SongLimit", 12);
		ALT_NONOWNER_ITEM_PICKUP_DELAY = altSettings.getProperty("NonOwnerItemPickupDelay", 15L) * 1000L;
		ALT_FESTIVAL_MIN_PARTY_SIZE = altSettings.getProperty("FestivalMinPartySize", 5);
		ALT_FESTIVAL_RATE_PRICE = altSettings.getProperty("FestivalRatePrice", 1.0);
		ALT_RIFT_MIN_PARTY_SIZE = altSettings.getProperty("RiftMinPartySize", 5);
		ALT_RIFT_SPAWN_DELAY = altSettings.getProperty("RiftSpawnDelay", 10000);
		ALT_RIFT_MAX_JUMPS = altSettings.getProperty("MaxRiftJumps", 4);
		ALT_RIFT_AUTO_JUMPS_TIME = altSettings.getProperty("AutoJumpsDelay", 8);
		ALT_RIFT_AUTO_JUMPS_TIME_RAND = altSettings.getProperty("AutoJumpsDelayRandom", 120000);
		ALT_RIFT_ENTER_COST_RECRUIT = altSettings.getProperty("RecruitFC", 18);
		ALT_RIFT_ENTER_COST_SOLDIER = altSettings.getProperty("SoldierFC", 21);
		ALT_RIFT_ENTER_COST_OFFICER = altSettings.getProperty("OfficerFC", 24);
		ALT_RIFT_ENTER_COST_CAPTAIN = altSettings.getProperty("CaptainFC", 27);
		ALT_RIFT_ENTER_COST_COMMANDER = altSettings.getProperty("CommanderFC", 30);
		ALT_RIFT_ENTER_COST_HERO = altSettings.getProperty("HeroFC", 33);
		ALT_PARTY_LEADER_ONLY_CAN_INVITE = altSettings.getProperty("PartyLeaderOnlyCanInvite", true);
		ALT_ALLOW_NOBLE_TP_TO_ALL = altSettings.getProperty("AllowNobleTPToAll", false);
		ALT_SOCIAL_ACTION_REUSE = altSettings.getProperty("AltSocialActionReuse", false);
		ALT_RAID_RESPAWN_MULTIPLIER = altSettings.getProperty("AltRaidRespawnMultiplier", 1.0);
		ALT_ALLOW_DROP_AUGMENTED = altSettings.getProperty("AlowDropAugmented", false);
		ALT_CLANHALL_BUFFTIME_MODIFIER = altSettings.getProperty("ClanHallBuffTimeModifier", 1.0);
		ALT_SONGDANCETIME_MODIFIER = altSettings.getProperty("SongDanceTimeModifier", 1.0);
		ALT_MAXLOAD_MODIFIER = altSettings.getProperty("MaxLoadModifier", 1.0);
		ALT_CHAMPION_CHANCE_RED = altSettings.getProperty("AltChampionChanceRed", 0.);
		ALT_CHAMPION_CHANCE_BLUE = altSettings.getProperty("AltChampionChanceBlue", 0.);
		ALT_CHAMPION_CAN_BE_AGGRO = altSettings.getProperty("AltChampionAggro", false);
		ALT_CHAMPION_CAN_BE_SOCIAL = altSettings.getProperty("AltChampionSocial", false);
		ALT_CHAMPION_TOP_LEVEL = altSettings.getProperty("AltChampionTopLevel", 75);
		ALT_NO_LASTHIT = altSettings.getProperty("NoLasthitOnRaid", false);
		ALT_BUFF_SUMMON = altSettings.getProperty("BuffSummon", false);
		ALT_IMPROVED_PETS_LIMITED_USE = altSettings.getProperty("ImprovedPetsLimitedUse", false);
		ALT_PET_HEAL_BATTLE_ONLY = altSettings.getProperty("PetsHealOnlyInBattle", true);
		ALT_PARTY_DISTRIBUTION_RANGE = altSettings.getProperty("AltPartyDistributionRange", 1500);
		ALT_PARTY_BONUS = altSettings.getProperty("AltPartyBonus", new double[] { 1.00, 1.10, 1.20, 1.30, 1.40, 1.50, 2.00, 2.10, 2.20 });
		ALT_USE_BOW_REUSE_MODIFIER = altSettings.getProperty("AltUseBowReuseModifier", true);
		ALT_ALLOW_CH_DOOR_OPEN_ON_CLICK = altSettings.getProperty("AllowChDoorOpenOnClick", true);
		ALT_CH_ALL_BUFFS = altSettings.getProperty("AltChAllBuffs", false);
		ALT_CH_ALLOW_1H_BUFFS = altSettings.getProperty("AltChAllowHourBuff", false);
		ALT_CH_SIMPLE_DIALOG = altSettings.getProperty("AltChSimpleDialog", false);
		ALT_VITALITY_ENABLED = altSettings.getProperty("AltVitalityEnabled", true);
		ALT_VITALITY_RATE = altSettings.getProperty("AltVitalityRate", 1.);
		ALT_VITALITY_RAID_BONUS = altSettings.getProperty("AltVitalityRaidBonus", 2000);
		ALT_VITALITY_CONSUME_RATE = altSettings.getProperty("AltVitalityConsumeRate", 1.);
		ALT_KAMALOKA_NIGHTMARES_PREMIUM_ONLY = altSettings.getProperty("KamalokaNightmaresPremiumOnly", false);
		ALT_KAMALOKA_NIGHTMARE_REENTER = altSettings.getProperty("SellReenterNightmaresTicket", true);
		ALT_KAMALOKA_ABYSS_REENTER = altSettings.getProperty("SellReenterAbyssTicket", true);
		ALT_KAMALOKA_LAB_REENTER = altSettings.getProperty("SellReenterLabyrinthTicket", true);
		ALT_PET_INVENTORY_LIMIT = altSettings.getProperty("AltPetInventoryLimit", 12);
		ALT_FOLLOW_RANGE = altSettings.getProperty("FollowRange", 100);
		ALT_OPEN_CLOAK_SLOT = altSettings.getProperty("OpenCloakSlot", false);
		ALT_SHOW_SERVER_TIME = altSettings.getProperty("ShowServerTime", false);
		ALT_ITEM_AUCTION_ENABLED = altSettings.getProperty("AltItemAuctionEnabled", true);
		ALT_ITEM_AUCTION_CAN_REBID = altSettings.getProperty("AltItemAuctionCanRebid", false);
		ALT_ITEM_AUCTION_START_ANNOUNCE = altSettings.getProperty("AltItemAuctionAnnounce", true);
		ALT_ITEM_AUCTION_BID_ITEM_ID = altSettings.getProperty("AltItemAuctionBidItemId", 57);
		ALT_ITEM_AUCTION_MAX_BID = altSettings.getProperty("AltItemAuctionMaxBid", 1000000L);
		ALT_ITEM_AUCTION_MAX_CANCEL_TIME_IN_MILLIS = altSettings.getProperty("AltItemAuctionMaxCancelTimeInMillis", 604800000);
		ALT_FISH_CHAMPIONSHIP_ENABLED = altSettings.getProperty("AltFishChampionshipEnabled", true);
		ALT_FISH_CHAMPIONSHIP_REWARD_ITEM = altSettings.getProperty("AltFishChampionshipRewardItemId", 57);
		ALT_FISH_CHAMPIONSHIP_REWARD_1 = altSettings.getProperty("AltFishChampionshipReward1", 800000);
		ALT_FISH_CHAMPIONSHIP_REWARD_2 = altSettings.getProperty("AltFishChampionshipReward2", 500000);
		ALT_FISH_CHAMPIONSHIP_REWARD_3 = altSettings.getProperty("AltFishChampionshipReward3", 300000);
		ALT_FISH_CHAMPIONSHIP_REWARD_4 = altSettings.getProperty("AltFishChampionshipReward4", 200000);
		ALT_FISH_CHAMPIONSHIP_REWARD_5 = altSettings.getProperty("AltFishChampionshipReward5", 100000);
		ALT_ENABLE_BLOCK_CHECKER_EVENT = altSettings.getProperty("EnableBlockCheckerEvent", true);
		ALT_MIN_BLOCK_CHECKER_TEAM_MEMBERS = Math.min(Math.max(altSettings.getProperty("BlockCheckerMinTeamMembers", 1), 1), 6);
		ALT_RATE_COINS_REWARD_BLOCK_CHECKER = altSettings.getProperty("BlockCheckerRateCoinReward", 1.);
		ALT_HBCE_FAIR_PLAY = altSettings.getProperty("HBCEFairPlay", false);
		ALT_PETITIONING_ALLOWED = altSettings.getProperty("PetitioningAllowed", true);
		ALT_MAX_PETITIONS_PER_PLAYER = altSettings.getProperty("MaxPetitionsPerPlayer", 5);
		ALT_MAX_PETITIONS_PENDING = altSettings.getProperty("MaxPetitionsPending", 25);
		ALT_DEBUG_ENABLED = altSettings.getProperty("AltDebugEnabled", false);
		ALT_DEBUG_PVP_ENABLED = altSettings.getProperty("AltDebugPvPEnabled", false);
		ALT_DEBUG_PVP_DUEL_ONLY = altSettings.getProperty("AltDebugPvPDuelOnly", true);
		ALT_DEBUG_PVE_ENABLED = altSettings.getProperty("AltDebugPvEEnabled", false);
		ALT_MIN_ADENA_TO_EAT = altSettings.getProperty("MinAdenaToLuckpyEat", 10000);
		ALT_TIME_IF_NOT_FEED = altSettings.getProperty("TimeIfNotFeedDissapear", 10);
		ALT_INTERVAL_EATING = altSettings.getProperty("IntervalBetweenEating", 15);
		VITAMIN_PETS_FOOD_ID = altSettings.getProperty("AltVitaminPetsFoodId",-1);
		ALT_LETHAL1_BY_HP = altSettings.getProperty("Lethel1ByHP", 50000);
		ALT_DELETE_TRANSFORMATION_ON_DEATH = altSettings.getProperty("DeleteTransOnDeath", false);

    ANCIENT_HERB_SPAWN_RADIUS = altSettings.getProperty("AncientHerbSpawnRadius", 600);
    ANCIENT_HERB_SPAWN_CHANCE = altSettings.getProperty("AncientHerbSpawnChance", 3);
    ANCIENT_HERB_SPAWN_COUNT = altSettings.getProperty("AncientHerbSpawnCount", 5);
    ANCIENT_HERB_RESPAWN_TIME = altSettings.getProperty("AncientHerbRespawnTime", 60) * 1000;
    ANCIENT_HERB_DESPAWN_TIME = altSettings.getProperty("AncientHerbDespawnTime", 60) * 1000;
    String[] locs = altSettings.getProperty("AncientHerbSpawnPoints", "").split(";");
    if (locs != null) {
      for (String string : locs) {
        if (string != null)
        {
          String[] cords = string.split(",");
          int x = Integer.parseInt(cords[0]);
          int y = Integer.parseInt(cords[1]);
          int z = Integer.parseInt(cords[2]);
          HEIN_FIELDS_LOCATIONS.add(new Location(x, y, z));
        }
      }
    }
	}
	
	public static void loadServicesBashSettings()
	{
		ExProperties servicesBashSettings = load(SERVICE_BASH_CONFIG_FILE);

		SERVICES_BASH_ENABLED = servicesBashSettings.getProperty("BashEnabled", false);
		SERVICES_BASH_SKIP_DOWNLOAD = servicesBashSettings.getProperty("BashSkipDownload", false);
		SERVICES_BASH_RELOAD_TIME = servicesBashSettings.getProperty("BashReloadTime", 24);
	}

	public static void loadServicesEnterWorldSettings()
	{
		ExProperties servicesEnterWorldSettings = load(SERVICE_ENTER_WORLD_CONFIG_FILE);

		ENTER_WORLD_ANNOUNCEMENTS_HERO_LOGIN = servicesEnterWorldSettings.getProperty("Hero", false);
		ENTER_WORLD_ANNOUNCEMENTS_LORD_LOGIN = servicesEnterWorldSettings.getProperty("Lord", false);
		ENTER_WORLD_SHOW_HTML_WELCOME = servicesEnterWorldSettings.getProperty("WelcomeHTML", false);
		ENTER_WORLD_SHOW_HTML_PREMIUM_BUY = servicesEnterWorldSettings.getProperty("PremiumHTML", false);
	}

	public static void loadServicesCharacterCreateSettings()
	{
		ExProperties servicesCharacterCreateSettings = load(SERVICE_CHARACTER_CREATE_CONFIG_FILE);
		CHARACTER_CREATE_CHAR_TITLE = servicesCharacterCreateSettings.getProperty("SetTitle", false);
		CHARACTER_CREATE_ADD_CHAR_TITLE = servicesCharacterCreateSettings.getProperty("Title", "");
		CHARACTER_CREATE_START_ADENA = servicesCharacterCreateSettings.getProperty("Adena", 0L);
		CHARACTER_CREATE_START_LVL = servicesCharacterCreateSettings.getProperty("Level", 0);
		CHARACTER_CREATE_START_SP = servicesCharacterCreateSettings.getProperty("Sp", 0L);
		CHARACTER_CREATE_ALLOW_START_LOC = servicesCharacterCreateSettings.getProperty("StartLocation", false);
		CHARACTER_CREATE_START_LOC_HUMAN_FIGHTER = servicesCharacterCreateSettings.getProperty("LocationHF", "").split(",");
		CHARACTER_CREATE_START_LOC_HUMAN_MAGE = servicesCharacterCreateSettings.getProperty("LocationHM", "").split(",");
		CHARACTER_CREATE_START_LOC_ELF_FIGHTER = servicesCharacterCreateSettings.getProperty("LocationEF", "").split(",");
		CHARACTER_CREATE_START_LOC_ELF_MAGE = servicesCharacterCreateSettings.getProperty("LocationEM", "").split(",");
		CHARACTER_CREATE_START_LOC_DARKELF_FIGHTER = servicesCharacterCreateSettings.getProperty("LocationDEF", "").split(",");
		CHARACTER_CREATE_START_LOC_DARKELF_MAGE = servicesCharacterCreateSettings.getProperty("LocationDEM", "").split(",");
		CHARACTER_CREATE_START_LOC_ORC_FIGHTER = servicesCharacterCreateSettings.getProperty("LocationOF", "").split(",");
		CHARACTER_CREATE_START_LOC_ORC_MAGE = servicesCharacterCreateSettings.getProperty("LocationOM", "").split(",");
		CHARACTER_CREATE_START_LOC_DWARF = servicesCharacterCreateSettings.getProperty("LocationD", "").split(",");
		CHARACTER_CREATE_START_LOC_KAMAEL = servicesCharacterCreateSettings.getProperty("LocationK", "").split(",");
		/* Cusom Start Items */
	    STARTING_ITEMS = servicesCharacterCreateSettings.getProperty("StartingItems", false);
	    STARTING_ITEMS_ID_QTY = new ArrayList<>();
	    String[] propertySplit = servicesCharacterCreateSettings.getProperty("StartingItemsIdQty", "20635,1;20638,1").split(";");
	    for (String reward : propertySplit)
	    {
	            String[] rewardSplit = reward.split(",");
	            if (rewardSplit.length != 2)
	            {}
	            else
	            {
	            	try
	            	{
	            		STARTING_ITEMS_ID_QTY.add(new int[]
	            				{
	            					Integer.parseInt(rewardSplit[0]),
	            					Integer.parseInt(rewardSplit[1])
	            				});
	                    }
	                    catch (NumberFormatException ignored)
	                    {
	
	                    }
	            	}
	    		}
		}

	public static void loadServicesCharacterSettings()
	{
		ExProperties servicesCharacterSettings = load(SERVICE_CHARACTER_CONFIG_FILE);

		for(int id : servicesCharacterSettings.getProperty("AllowClassMasters", ArrayUtils.EMPTY_INT_ARRAY))
			if(id != 0)
				ALLOW_CLASS_MASTERS_LIST.add(id);

		CLASS_MASTERS_PRICE = servicesCharacterSettings.getProperty("ClassMastersPrice", "0,0,0");
		if(CLASS_MASTERS_PRICE.length() >= 5)
		{
			int level = 1;
			for(String id : CLASS_MASTERS_PRICE.split(","))
			{
				CLASS_MASTERS_PRICE_LIST[level] = Integer.parseInt(id);
				level++;
			}
		}
		CLASS_MASTERS_PRICE_ITEM = servicesCharacterSettings.getProperty("ClassMastersPriceItem", 57);
        ALLOW_AWAY_STATUS = servicesCharacterSettings.getProperty("AllowAwayStatus", false); // FIXME:
        AWAY_ONLY_FOR_PREMIUM = servicesCharacterSettings.getProperty("AwayOnlyForPremium", true);
        AWAY_PLAYER_TAKE_AGGRO = servicesCharacterSettings.getProperty("AwayPlayerTakeAggro", false);
        AWAY_TITLE_COLOR = Integer.decode("0x" + servicesCharacterSettings.getProperty("AwayTitleColor", "0000FF"));
        AWAY_TIMER = servicesCharacterSettings.getProperty("AwayTimer", 30);
        BACK_TIMER = servicesCharacterSettings.getProperty("BackTimer", 30);
        AWAY_PEACE_ZONE = servicesCharacterSettings.getProperty("AwayOnlyInPeaceZone", false);
		SERVICES_CHANGE_NICK_ENABLED = servicesCharacterSettings.getProperty("NickChangeEnabled", false);
		SERVICES_CHANGE_NICK_TEMPLATE = servicesCharacterSettings.getProperty("NickChangeTemplate", "[A-Za-z0-9\u0410-\u042f\u0430-\u044f]{2,16}");
		SERVICES_CHANGE_NICK_PRICE = servicesCharacterSettings.getProperty("NickChangePrice", 100);
		SERVICES_CHANGE_NICK_ITEM = servicesCharacterSettings.getProperty("NickChangeItem", 4037);
        SERVICES_CHANGE_TITLE_COLOR_ENABLED = servicesCharacterSettings.getProperty("TitleColorChangeEnabled", false);
        SERVICES_CHANGE_TITLE_COLOR_PRICE = servicesCharacterSettings.getProperty("TitleColorChangePrice", 100);
        SERVICES_CHANGE_TITLE_COLOR_ITEM = servicesCharacterSettings.getProperty("TitleColorChangeItem", 4037);
        SERVICES_CHANGE_TITLE_COLOR_LIST = servicesCharacterSettings.getProperty("TitleColorChangeList", new String[]{"00FF00"});
		ServicesUnBanChat = servicesCharacterSettings.getProperty("ServicesUnBanChat", false);
		ServicesUnBanChatItem = servicesCharacterSettings.getProperty("ServicesUnBanChatItem", 4037);
		ServicesUnBanChatCount = servicesCharacterSettings.getProperty("ServicesUnBanChatCount", 1);
		ServicesUnBan = servicesCharacterSettings.getProperty("ServicesUnBan", false);
		ServicesUnBanAcc = servicesCharacterSettings.getProperty("ServicesUnBanAcc", false);
		ServicesUnBanAccItem = servicesCharacterSettings.getProperty("ServicesUnBanAccItem", 4037);
		ServicesUnBanAccCount = servicesCharacterSettings.getProperty("ServicesUnBanAccCount", 1);
		ServicesUnBanChar = servicesCharacterSettings.getProperty("ServicesUnBanChar", false);
		ServicesUnBanCharItem = servicesCharacterSettings.getProperty("ServicesUnBanCharItem", 4037);
		ServicesUnBanCharCount = servicesCharacterSettings.getProperty("ServicesUnBanCharCount", 1);
		
        SERVICES_OLYMPIAD_RESET_ENABLED = servicesCharacterSettings.getProperty("OlympiadRestorePoint", false);
        SERVICES_OLYMPIAD_ITEM_PRICE = servicesCharacterSettings.getProperty("OlympiadRestoreItemPrice", 100);
        SERVICES_OLYMPIAD_ITEM = servicesCharacterSettings.getProperty("OlympiadRestoreItem", 4037);
        
		SERVICES_CHANGE_PASSWORD = servicesCharacterSettings.getProperty("ChangePassword", false);
		PASSWORD_PAY_ID = servicesCharacterSettings.getProperty("ChangePasswordPayId", 0);
		PASSWORD_PAY_COUNT = servicesCharacterSettings.getProperty("ChangePassowrdPayCount", 0);
		APASSWD_TEMPLATE = servicesCharacterSettings.getProperty("ApasswdTemplate", "[A-Za-z0-9]{5,16}");
		SERVICES_CHANGE_PET_NAME_ENABLED = servicesCharacterSettings.getProperty("PetNameChangeEnabled", false);
		SERVICES_CHANGE_PET_NAME_PRICE = servicesCharacterSettings.getProperty("PetNameChangePrice", 100);
		SERVICES_CHANGE_PET_NAME_ITEM = servicesCharacterSettings.getProperty("PetNameChangeItem", 4037);

		SERVICES_EXCHANGE_BABY_PET_ENABLED = servicesCharacterSettings.getProperty("BabyPetExchangeEnabled", false);
		SERVICES_EXCHANGE_BABY_PET_PRICE = servicesCharacterSettings.getProperty("BabyPetExchangePrice", 100);
		SERVICES_EXCHANGE_BABY_PET_ITEM = servicesCharacterSettings.getProperty("BabyPetExchangeItem", 4037);

		SERVICES_CHANGE_SEX_ENABLED = servicesCharacterSettings.getProperty("SexChangeEnabled", false);
		SERVICES_CHANGE_SEX_PRICE = servicesCharacterSettings.getProperty("SexChangePrice", 100);
		SERVICES_CHANGE_SEX_ITEM = servicesCharacterSettings.getProperty("SexChangeItem", 4037);

		SERVICES_CHANGE_BASE_ENABLED = servicesCharacterSettings.getProperty("BaseChangeEnabled", false);
		SERVICES_CHANGE_BASE_PRICE = servicesCharacterSettings.getProperty("BaseChangePrice", 100);
		SERVICES_CHANGE_BASE_ITEM = servicesCharacterSettings.getProperty("BaseChangeItem", 4037);

		SERVICES_SEPARATE_SUB_ENABLED = servicesCharacterSettings.getProperty("SeparateSubEnabled", false);
		SERVICES_SEPARATE_SUB_PRICE = servicesCharacterSettings.getProperty("SeparateSubPrice", 100);
		SERVICES_SEPARATE_SUB_ITEM = servicesCharacterSettings.getProperty("SeparateSubItem", 4037);

		SERVICES_CHANGE_NICK_COLOR_ENABLED = servicesCharacterSettings.getProperty("NickColorChangeEnabled", false);
		SERVICES_CHANGE_NICK_COLOR_PRICE = servicesCharacterSettings.getProperty("NickColorChangePrice", 100);
		SERVICES_CHANGE_NICK_COLOR_ITEM = servicesCharacterSettings.getProperty("NickColorChangeItem", 4037);
		SERVICES_CHANGE_NICK_COLOR_LIST = servicesCharacterSettings.getProperty("NickColorChangeList", new String[] { "00FF00" });
		SERVICES_ALLOW_CHANGE_NICK_COLOR_TARGET = servicesCharacterSettings.getProperty("CTarget", false);
		SERVICES_CHANGE_NICK_COLOR_TARGET = servicesCharacterSettings.getProperty("CTList", "").split(",");

        SERVICEFAMEACTIVE = servicesCharacterSettings.getProperty("FameService", false);
        SERVICEFAMEPRICE = servicesCharacterSettings.getProperty("FameServicePrice", new int[]{10, 1, 57});
        SERVICEFAMEFREEFORPA = servicesCharacterSettings.getProperty("FameServiceFreeForPA", false);
        
        SERVICES_LVL_ENABLED = servicesCharacterSettings.getProperty("LevelChangeEnabled", false);
        SERVICES_LVL_UP_MAX = servicesCharacterSettings.getProperty("LevelUPChangeMax", 85);
        SERVICES_LVL_UP_PRICE = servicesCharacterSettings.getProperty("LevelUPChangePrice", 1000);
        SERVICES_LVL_UP_ITEM = servicesCharacterSettings.getProperty("LevelUPChangeItem", 4037);
        SERVICES_LVL_DOWN_MAX = servicesCharacterSettings.getProperty("LevelDownChangeMax", 1);
        SERVICES_LVL_DOWN_PRICE = servicesCharacterSettings.getProperty("LevelDownChangePrice", 1000);
        SERVICES_LVL_DOWN_ITEM = servicesCharacterSettings.getProperty("LevelDownChangeItem", 4037);
        
        SERVICERECOMACTIVE = servicesCharacterSettings.getProperty("RecService", false);
        SERVICERECOMPRICE = servicesCharacterSettings.getProperty("RecServicePrice", new int[]{10, 1, 57});
        SERVICERECOMFREEFORPA= servicesCharacterSettings.getProperty("RecServiceFreeForPA", true);
		
		SERVICES_NOBLESS_SELL_ENABLED = servicesCharacterSettings.getProperty("NoblessSellEnabled", false);
		SERVICES_NOBLESS_SELL_PRICE = servicesCharacterSettings.getProperty("NoblessSellPrice", 1000);
		SERVICES_NOBLESS_SELL_ITEM = servicesCharacterSettings.getProperty("NoblessSellItem", 4037);

		SERVICES_HERO_SELL_ENABLED = servicesCharacterSettings.getProperty("HeroSell", false);
		SERVICES_HERO_SELL_PRICE = servicesCharacterSettings.getProperty("HeroPrice", new int[] { 0 });
		SERVICES_HERO_SELL_ITEM = servicesCharacterSettings.getProperty("HeroItem", new int[] { 0 });
		SERVICES_HERO_SELL_DAY = servicesCharacterSettings.getProperty("HeroDay", new int[] { 0 });
		SERVICES_HERO_SELL_CHAT = servicesCharacterSettings.getProperty("HeroChat", false);
		SERVICES_HERO_SELL_SKILL = servicesCharacterSettings.getProperty("HeroSkills", false);
		SERVICES_HERO_SELL_ITEMS = servicesCharacterSettings.getProperty("HeroItems", false);

		SERVICES_EXPAND_INVENTORY_ENABLED = servicesCharacterSettings.getProperty("ExpandInventoryEnabled", false);
		SERVICES_EXPAND_INVENTORY_PRICE = servicesCharacterSettings.getProperty("ExpandInventoryPrice", 1000);
		SERVICES_EXPAND_INVENTORY_ITEM = servicesCharacterSettings.getProperty("ExpandInventoryItem", 4037);
		SERVICES_EXPAND_INVENTORY_MAX = servicesCharacterSettings.getProperty("ExpandInventoryMax", 250);

		SERVICES_EXPAND_WAREHOUSE_ENABLED = servicesCharacterSettings.getProperty("ExpandWarehouseEnabled", false);
		SERVICES_EXPAND_WAREHOUSE_PRICE = servicesCharacterSettings.getProperty("ExpandWarehousePrice", 1000);
		SERVICES_EXPAND_WAREHOUSE_ITEM = servicesCharacterSettings.getProperty("ExpandWarehouseItem", 4037);

		SERVICES_RATE_TYPE = servicesCharacterSettings.getProperty("RateBonusType", Bonus.NO_BONUS);
		SERVICES_RATE_BONUS_PRICE = servicesCharacterSettings.getProperty("RateBonusPrice", new int[] { 1500 });
		SERVICES_RATE_BONUS_ITEM = servicesCharacterSettings.getProperty("RateBonusItem", new int[] { 4037 });
		SERVICES_RATE_BONUS_VALUE = servicesCharacterSettings.getProperty("RateBonusValue", new double[] { 1.25 });
		SERVICES_RATE_BONUS_DAYS = servicesCharacterSettings.getProperty("RateBonusTime", new int[] { 30 });
		SERVICES_BONUS_XP = servicesCharacterSettings.getProperty("RateBonusXp", 1.);
		SERVICES_BONUS_SP = servicesCharacterSettings.getProperty("RateBonusSp", 1.);
		SERVICES_BONUS_ADENA = servicesCharacterSettings.getProperty("RateBonusAdena", 1.);
		SERVICES_BONUS_ITEMS = servicesCharacterSettings.getProperty("RateBonusItems", 1.);
		SERVICES_BONUS_SPOIL = servicesCharacterSettings.getProperty("RateBonusSpoil", 1.);
		SERVICES_RATE_BONUS_PERDAY_ITEM = servicesCharacterSettings.getProperty("RateBonusDailyItem", 57);
		SERVICES_RATE_BONUS_PERDAY_PRICE = servicesCharacterSettings.getProperty("RateBonusDailyPrice", 1);
		SERVICES_RATE_BONUS_PERDAY_VALUE = servicesCharacterSettings.getProperty("RateBonusDailyValue", 1.);

		SERVICES_SELLPETS = servicesCharacterSettings.getProperty("SellPets", "");

		SERVICES_ENABLE_NO_CARRIER = servicesCharacterSettings.getProperty("EnableNoCarrier", false);
		SERVICES_NO_CARRIER_MIN_TIME = servicesCharacterSettings.getProperty("NoCarrierMinTime", 0);
		SERVICES_NO_CARRIER_MAX_TIME = servicesCharacterSettings.getProperty("NoCarrierMaxTime", 90);
		SERVICES_NO_CARRIER_DEFAULT_TIME = servicesCharacterSettings.getProperty("NoCarrierDefaultTime", 60);

		ALT_SHOW_LEVEL_UP_PAGES = servicesCharacterSettings.getProperty("ShowPageOnLevelUp", new int[] { 10, 20, 30 });

		SERVICES_ALLOW_CLASS_BONUS = servicesCharacterSettings.getProperty("EnableItemBonus", false);
		SERVICES_CLASS_BONUS_ITEM_DAY = servicesCharacterSettings.getProperty("BonusIteamDay", 10);

		SERVICES_2_CLASS_CHANGE_REWARD_ENABLED = servicesCharacterSettings.getProperty("Enable2ClassChangeBonus", false);
		SERVICES_2_CLASS_MAGE_CHANGE_REWARD = servicesCharacterSettings.getProperty("Change2ClassMageBonus", new int[0]);
		SERVICES_2_CLASS_FIGHTER_CHANGE_REWARD = servicesCharacterSettings.getProperty("Change2ClassFighterBonus", new int[0]);

		SERVICES_3_CLASS_CHANGE_REWARD_ENABLED = servicesCharacterSettings.getProperty("Enable3ClassChangeBonus", false);
		SERVICES_3_CLASS_MAGE_CHANGE_REWARD = servicesCharacterSettings.getProperty("Change3ClassMageBonus", new int[0]);
		SERVICES_3_CLASS_FIGHTER_CHANGE_REWARD = servicesCharacterSettings.getProperty("Change3ClassFighterBonus", new int[0]);
		FORBIDDEN_CHARACTER_NAMES = servicesCharacterSettings.getProperty("ForbiddenCharacterNames", "Admin,admin,Gm,GM,gM");
		FORBIDDEN_CHARACTER_NAMES_DEBUG = servicesCharacterSettings.getProperty("ForbiddenCharacterNamesDebug", false);
		LIST_FORBIDDEN_CHARACTER_NAMES = new ArrayList<>();
		for(String name : FORBIDDEN_CHARACTER_NAMES.split(","))
			LIST_FORBIDDEN_CHARACTER_NAMES.add(name);
	}
	public static void loadServicesClanSettings()
	{
		ExProperties servicesClanSettings = load(SERVICE_CLAN_CONFIG_FILE);

		SERVICES_EXPAND_CWH_ENABLED = servicesClanSettings.getProperty("ExpandCWHEnabled", false);
		SERVICES_EXPAND_CWH_PRICE = servicesClanSettings.getProperty("ExpandCWHPrice", 1000);
		SERVICES_EXPAND_CWH_ITEM = servicesClanSettings.getProperty("ExpandCWHItem", 4037);
		SERVICES_CHANGE_CLAN_NAME_ENABLED = servicesClanSettings.getProperty("ClanNameChangeEnabled", false);
		SERVICES_CHANGE_CLAN_NAME_PRICE = servicesClanSettings.getProperty("ClanNameChangePrice", 100);
		SERVICES_CHANGE_CLAN_NAME_ITEM = servicesClanSettings.getProperty("ClanNameChangeItem", 4037);
		
		SERVICES_CLANLVL_ACTIVE = servicesClanSettings.getProperty("ServiceClanLvLSellActive", false);
        SERVICES_CLANLVL_PRICE= servicesClanSettings.getProperty("ServiceClanLvLSellValue", new int[]{57,12,1, 57,13,2, 57,14,3, 57,15,4, 57,16,5, 57,17,6, 57,18,7, 57,19,8, 57,20,9, 57,21,10, 57,22,11});
		        
        SERVICECRPACTIVE = servicesClanSettings.getProperty("CrpService", false);
        SERVICECRPPRICE =  servicesClanSettings.getProperty("CrpServicePrice", new int[]{10, 1, 57});
        SERVICECRPFREEFORPA= servicesClanSettings.getProperty("CrpServiceFreeForPA", true);
        
        FORTRESS_BLOOD_OATH_COUNT = servicesClanSettings.getProperty("FortressBloodOathCount", 1);
		FORTRESS_BLOOD_OATH_FRQ = servicesClanSettings.getProperty("FortressBloodOathFrequency", 360);
		CLAN_PRICE_CREATE_ROYAL_SUB = servicesClanSettings.getProperty("ClanPriceRoyalSub", 5000);
		CLAN_PRICE_CREATE_KNIGHT_SUB = servicesClanSettings.getProperty("ClanPriceKnightSub", 10000);
		ACADEMY_SUB_LIMIT = servicesClanSettings.getProperty("AcademyLimit", 20);
		ACADEMY_SUB_LIMIT_LEVEL11 = servicesClanSettings.getProperty("AcademyLimit11", 30);
		ROYAL_SUB_LIMIT_1 = servicesClanSettings.getProperty("RoyalLimit1", 20);
		ROYAL_SUB_LIMIT_2 = servicesClanSettings.getProperty("RoyalLimit2", 20);
		ROYAL_SUB_LIMIT_1_LEVEL11 = servicesClanSettings.getProperty("RoyalLimit1Level11", 30);
		ROYAL_SUB_LIMIT_2_LEVEL11 = servicesClanSettings.getProperty("RoyalLimit2Level11", 30);
		KNIGHT_SUB_LIMIT_1 = servicesClanSettings.getProperty("KnightLimit1", 10);
		KNIGHT_SUB_LIMIT_2 = servicesClanSettings.getProperty("KnightLimit2", 10);
		KNIGHT_SUB_LIMIT_3 = servicesClanSettings.getProperty("KnightLimit3", 10);
		KNIGHT_SUB_LIMIT_4 = servicesClanSettings.getProperty("KnightLimit4", 10);
		KNIGHT_SUB_LIMIT_1_LEVEL9 = servicesClanSettings.getProperty("KnightLimit1Level9", 25);
		KNIGHT_SUB_LIMIT_2_LEVEL9 = servicesClanSettings.getProperty("KnightLimit2Level9", 25);
		KNIGHT_SUB_LIMIT_3_LEVEL10 = servicesClanSettings.getProperty("KnightLimit3Level10", 25);
		KNIGHT_SUB_LIMIT_4_LEVEL10 = servicesClanSettings.getProperty("KnightLimit4Level10", 25);
		SERVICES_CLAN_LEVEL_CREATE = servicesClanSettings.getProperty("ClanCreateByLevel", 0);
		REQUIREMEN_COST_CLAN_LEVEL_UP_TO_1 = servicesClanSettings.getProperty("AdenaForLevelUpTo1", 650000);
		REQUIREMEN_COST_CLAN_LEVEL_UP_TO_2 = servicesClanSettings.getProperty("AdenaForLevelUpTo2", 2500000);
		REQUIREMEN_COST_CLAN_LEVEL_UP_TO_3 = servicesClanSettings.getProperty("BloodMarkForLevelUpTo3", 1);
		REQUIREMEN_COST_CLAN_LEVEL_UP_TO_4 = servicesClanSettings.getProperty("AllianceManifestoForLevelUpTo4", 1);
		REQUIREMEN_COST_CLAN_LEVEL_UP_TO_5 = servicesClanSettings.getProperty("SealOfAspirationForLevelUpTo5", 1);
		REQUIREMEN_COST_CLAN_LEVEL_UP_TO_6 = servicesClanSettings.getProperty("ReputationScoreForLevelUpTo6", 5000);
		REQUIREMEN_COST_CLAN_LEVEL_UP_TO_7 = servicesClanSettings.getProperty("ReputationScoreForLevelUpTo7", 10000);
		REQUIREMEN_COST_CLAN_LEVEL_UP_TO_8 = servicesClanSettings.getProperty("ReputationScoreForLevelUpTo8", 20000);
		REQUIREMEN_COST_CLAN_LEVEL_UP_TO_9 = servicesClanSettings.getProperty("ReputationScoreForLevelUpTo9", 40000);
		REQUIREMEN_COST_CLAN_LEVEL_UP_TO_10 = servicesClanSettings.getProperty("ReputationScoreForLevelUpTo10", 40000);
		REQUIREMEN_COST_CLAN_LEVEL_UP_TO_11 = servicesClanSettings.getProperty("ReputationScoreForLevelUpTo11", 75000);
		REQUIREMEN_CLAN_LEVEL_UP_TO_1 = servicesClanSettings.getProperty("SPForLevelUpTo1", 20000);
		REQUIREMEN_CLAN_LEVEL_UP_TO_2 = servicesClanSettings.getProperty("SPForLevelUpTo2", 100000);
		REQUIREMEN_CLAN_LEVEL_UP_TO_3 = servicesClanSettings.getProperty("SPForLevelUpTo3", 350000);
		REQUIREMEN_CLAN_LEVEL_UP_TO_4 = servicesClanSettings.getProperty("SPForLevelUpTo4", 1000000);
		REQUIREMEN_CLAN_LEVEL_UP_TO_5 = servicesClanSettings.getProperty("SPForLevelUpTo5", 2500000);
		REQUIREMEN_CLAN_LEVEL_UP_TO_9 = servicesClanSettings.getProperty("BloodOathsForLevelUpTo9", 150);
		REQUIREMEN_CLAN_LEVEL_UP_TO_10 = servicesClanSettings.getProperty("BloodPledgesForLevelUpTo10", 5);
		MEMBER_CLAN_LEVEL_UP_TO_6 = servicesClanSettings.getProperty("MemberClanLevelUpTo6", 30);
		MEMBER_CLAN_LEVEL_UP_TO_7 = servicesClanSettings.getProperty("MemberClanLevelUpTo7", 50);
		MEMBER_CLAN_LEVEL_UP_TO_8 = servicesClanSettings.getProperty("MemberClanLevelUpTo8", 80);
		MEMBER_CLAN_LEVEL_UP_TO_9 = servicesClanSettings.getProperty("MemberClanLevelUpTo9", 120);
		MEMBER_CLAN_LEVEL_UP_TO_10 = servicesClanSettings.getProperty("MemberClanLevelUpTo10", 140);
		MEMBER_CLAN_LEVEL_UP_TO_11 = servicesClanSettings.getProperty("MemberClanLevelUpTo11", 170);
		CLAN_MAIN_LIMIT_LEVEL0 = servicesClanSettings.getProperty("ClanLimitInLevel0", 10);
		CLAN_MAIN_LIMIT_LEVEL1 = servicesClanSettings.getProperty("ClanLimitInLevel1", 15);
		CLAN_MAIN_LIMIT_LEVEL2 = servicesClanSettings.getProperty("ClanLimitInLevel2", 20);
		CLAN_MAIN_LIMIT_LEVEL3 = servicesClanSettings.getProperty("ClanLimitInLevel3", 30);
		CLAN_MAIN_LIMIT_LEVEL4 = servicesClanSettings.getProperty("ClanLimitInLevel4", 30);
		CLAN_MAIN_LIMIT_LEVEL5 = servicesClanSettings.getProperty("ClanLimitInLevel5", 40);
		CLAN_MAIN_LIMIT_LEVEL6 = servicesClanSettings.getProperty("ClanLimitInLevel6", 40);
		CLAN_MAIN_LIMIT_LEVEL7 = servicesClanSettings.getProperty("ClanLimitInLevel7", 40);
		CLAN_MAIN_LIMIT_LEVEL8 = servicesClanSettings.getProperty("ClanLimitInLevel8", 40);
		CLAN_MAIN_LIMIT_LEVEL9 = servicesClanSettings.getProperty("ClanLimitInLevel9", 40);
		CLAN_MAIN_LIMIT_LEVEL10 = servicesClanSettings.getProperty("ClanLimitInLevel10", 40);
		CLAN_MAIN_LIMIT_LEVEL11 = servicesClanSettings.getProperty("ClanLimitInLevel11", 40);
		EXPELLED_MEMBER_PENALTY = servicesClanSettings.getProperty("ExpelledMemberPenalty", 24);
		LEAVED_ALLY_PENALTY = servicesClanSettings.getProperty("LeavedAllyPenalty", 24);
		DISSOLVED_ALLY_PENALTY = servicesClanSettings.getProperty("DissolvedAllyPenalty", 24);
		MIN_EARNED_ACADEM_POINT = servicesClanSettings.getProperty("MinEarnedAcademPoint", 190);
		MAX_EARNED_ACADEM_POINT = servicesClanSettings.getProperty("MaxEarnedAcademPoint", 650);
	}

	public static void loadServicesOffTradeSettings()
	{
		ExProperties servicesOffTradeSettings = load(SERVICE_OFFTRADE_CONFIG_FILE);

		SERVICES_OFFLINE_TRADE_ALLOW = servicesOffTradeSettings.getProperty("AllowOfflineTrade", false);
		SERVICES_OFFLINE_TRADE_ALLOW_OFFSHORE = servicesOffTradeSettings.getProperty("AllowOfflineTradeOnlyOffshore", true);
		SERVICES_OFFLINE_TRADE_MIN_LEVEL = servicesOffTradeSettings.getProperty("OfflineMinLevel", 0);
		SERVICES_OFFLINE_TRADE_NAME_COLOR = Integer.decode("0x" + servicesOffTradeSettings.getProperty("OfflineTradeNameColor", "B0FFFF"));
		SERVICES_OFFLINE_TRADE_PRICE = servicesOffTradeSettings.getProperty("OfflineTradePrice", 0);
		SERVICES_OFFLINE_TRADE_PRICE_ITEM = servicesOffTradeSettings.getProperty("OfflineTradePriceItem", 0);
		SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK = servicesOffTradeSettings.getProperty("OfflineTradeDaysToKick", 14) * 86400L;
		SERVICES_OFFLINE_TRADE_RESTORE_AFTER_RESTART = servicesOffTradeSettings.getProperty("OfflineRestoreAfterRestart", true);
		SERVICES_NO_TRADE_ONLY_OFFLINE = servicesOffTradeSettings.getProperty("NoTradeOnlyOffline", false);
		SERVICES_TRADE_TAX = servicesOffTradeSettings.getProperty("TradeTax", 0.0);
		SERVICES_OFFSHORE_TRADE_TAX = servicesOffTradeSettings.getProperty("OffshoreTradeTax", 0.0);
		SERVICES_TRADE_TAX_ONLY_OFFLINE = servicesOffTradeSettings.getProperty("TradeTaxOnlyOffline", false);
		SERVICES_TRADE_ONLY_FAR = servicesOffTradeSettings.getProperty("TradeOnlyFar", false);
		SERVICES_TRADE_RADIUS = servicesOffTradeSettings.getProperty("TradeRadius", 30);
		SERVICES_GIRAN_HARBOR_ENABLED = servicesOffTradeSettings.getProperty("GiranHarborZone", false);
		SERVICES_PARNASSUS_ENABLED = servicesOffTradeSettings.getProperty("ParnassusZone", false);
		SERVICES_PARNASSUS_NOTAX = servicesOffTradeSettings.getProperty("ParnassusNoTax", false);
		SERVICES_PARNASSUS_PRICE = servicesOffTradeSettings.getProperty("ParnassusPrice", 500000);
		SERVICES_OFFSHORE_NO_CASTLE_TAX = servicesOffTradeSettings.getProperty("NoCastleTaxInOffshore", false);
		SERVICES_TRADE_MIN_LEVEL = servicesOffTradeSettings.getProperty("MinLevelForTrade", 0);
        SERVICES_OFFLINE_ABNORMAL_EFFECT = AbnormalEffect.valueOf(servicesOffTradeSettings.getProperty("OfflineAbnormalEffect", "NULL"));
    }

	public static void loadServicesOtherSettings()
	{
		ExProperties servicesOtherSettings = load(SERVICE_OTHER_CONFIG_FILE);

		SERVICES_ALLOW_LOTTERY = servicesOtherSettings.getProperty("AllowLottery", false);
		SERVICES_LOTTERY_PRIZE = servicesOtherSettings.getProperty("LotteryPrize", 50000);
		SERVICES_ALT_LOTTERY_PRICE = servicesOtherSettings.getProperty("AltLotteryPrice", 2000);
		SERVICES_LOTTERY_TICKET_PRICE = servicesOtherSettings.getProperty("LotteryTicketPrice", 2000);
		SERVICES_LOTTERY_5_NUMBER_RATE = servicesOtherSettings.getProperty("Lottery5NumberRate", 0.6);
		SERVICES_LOTTERY_4_NUMBER_RATE = servicesOtherSettings.getProperty("Lottery4NumberRate", 0.4);
		SERVICES_LOTTERY_3_NUMBER_RATE = servicesOtherSettings.getProperty("Lottery3NumberRate", 0.2);
		SERVICES_LOTTERY_2_AND_1_NUMBER_PRIZE = servicesOtherSettings.getProperty("Lottery2and1NumberPrize", 200);

		SERVICES_ALLOW_ROULETTE = servicesOtherSettings.getProperty("AllowRoulette", false);
		SERVICES_ROULETTE_MIN_BET = servicesOtherSettings.getProperty("RouletteMinBet", 1L);
		SERVICES_ROULETTE_MAX_BET = servicesOtherSettings.getProperty("RouletteMaxBet", Long.MAX_VALUE);

		ITEM_BROKER_ITEM_SEARCH = servicesOtherSettings.getProperty("UseItemBrokerItemSearch", false);

		ALLOW_NEWS_INFORMER = servicesOtherSettings.getProperty("AllowNewsInformer", false);
		NEWS_INFORMER_RND_ITEM_LIST = servicesOtherSettings.getProperty("NewsInformerRndItemList", new int[] { 57, 4037 });
		NEWS_INFORMER_RND_ITEM_COUNTS = servicesOtherSettings.getProperty("NewsInformerRndItemCount", new int[] { 1, 2 });
		NEWS_INFORMER_RND_ITEM_CHANCES = servicesOtherSettings.getProperty("NewsInformerRndItemChanses", new int[] { 10, 20 });

		NEWS_INFORMER_RND_ITEM = servicesOtherSettings.getProperty("NewsInformerRndItem", false);
		NEWS_INFORMER_ONE_ITEM = servicesOtherSettings.getProperty("NewsInformerOneItem", false);
		NEWS_INFORMER_ONE_ITEM_ID = servicesOtherSettings.getProperty("NewsInformerOneItemId", 57);
		NEWS_INFORMER_ONE_ITEM_COUNT = servicesOtherSettings.getProperty("NewsInformerOneItemCount", 1);

		ALLOW_NEWBIE_BONUS_MANAGER = servicesOtherSettings.getProperty("AsgardGuildService", false);
	}

	public static void loadServicesSecuritySettings()
	{
		ExProperties servicesSecuritySettings = load(SERVICE_SECURITY_CONFIG_FILE);

		ALLOW_IP_LOCK = servicesSecuritySettings.getProperty("AllowLockIP", false);
		ALLOW_HWID_LOCK = servicesSecuritySettings.getProperty("AllowLockHwid", false);
		HWID_LOCK_MASK = servicesSecuritySettings.getProperty("HwidLockMask", 10);
        LOCK_ACCOUNT_HWID_COMPARATOR = new HWIDComparator();
        
        CAPTCHA_ENABLE = servicesSecuritySettings.getProperty("CaptchaEnable", false);
        CAPTCHA_TYPE = servicesSecuritySettings.getProperty("CaptchaType", "IMAGE");
        CAPTCHA_IMAGE_WORDS = servicesSecuritySettings.getProperty("CaptchaImageWords", new String[]{"lineage2", "higefive", "grind"});
        CAPTCHA_TIME = servicesSecuritySettings.getProperty("CaptchaTimeout", 40);
        CAPTCHA_SHOW_PLAYERS_WITH_PA = servicesSecuritySettings.getProperty("CaptchaShowPlayersWithPA", true);
        CAPTCHA_COUNT_ERROR = servicesSecuritySettings.getProperty("CaptchaCountError", 3);
        CAPTCHA_NPC_CHANCE = servicesSecuritySettings.getProperty("CaptchaNpcChance", 2);
        CAPTCHA_CHAT_CHANCE = servicesSecuritySettings.getProperty("CaptchaChatChance", 3);
		CAPTCHA_COMMAND_ENABLE = servicesSecuritySettings.getProperty("CaptchaCommandEnable", false);
		REUSE_COMMAND_TIME = servicesSecuritySettings.getProperty("ReuseCommandTime", 10);


        
	}

	public static void loadServicesWeddingSettings()
	{
		ExProperties servicesWeddingSettings = load(SERVICE_WEDDING_CONFIG_FILE);

		ALLOW_WEDDING = servicesWeddingSettings.getProperty("AllowWedding", false);
		WEDDING_PRICE = servicesWeddingSettings.getProperty("WeddingPrice", 500000);
		WEDDING_PUNISH_INFIDELITY = servicesWeddingSettings.getProperty("WeddingPunishInfidelity", true);
		WEDDING_TELEPORT = servicesWeddingSettings.getProperty("WeddingTeleport", true);
		WEDDING_TELEPORT_PRICE = servicesWeddingSettings.getProperty("WeddingTeleportPrice", 500000);
		WEDDING_TELEPORT_INTERVAL = servicesWeddingSettings.getProperty("WeddingTeleportInterval", 120);
		WEDDING_SAMESEX = servicesWeddingSettings.getProperty("WeddingAllowSameSex", true);
		WEDDING_FORMALWEAR = servicesWeddingSettings.getProperty("WeddingFormalWear", true);
		WEDDING_DIVORCE_COSTS = servicesWeddingSettings.getProperty("WeddingDivorceCosts", 20);
	}

	public static void loadServicesBonusSettings()
	{
		ExProperties servicesBonusSettings = load(SERVICE_BONUS_CONFIG_FILE);

		BONUS_SERVICE_ENABLE = servicesBonusSettings.getProperty("BonusEnabled", false);
		BONUS_SERVICE_CLAN_REWARD = servicesBonusSettings.getProperty("ClanBonusReward", new int[0]);
		BONUS_SERVICE_PARY_REWARD = servicesBonusSettings.getProperty("PartyBonusReward", new int[0]);
	}

	public static void loadPvPSettings()
	{
		ExProperties pvpSettings = load(PVP_CONFIG_FILE);

		/* KARMA SYSTEM */
		KARMA_MIN_KARMA = pvpSettings.getProperty("MinKarma", 240);
		KARMA_SP_DIVIDER = pvpSettings.getProperty("SPDivider", 7);
		KARMA_LOST_BASE = pvpSettings.getProperty("BaseKarmaLost", 0);

		KARMA_DROP_GM = pvpSettings.getProperty("CanGMDropEquipment", false);
		KARMA_NEEDED_TO_DROP = pvpSettings.getProperty("KarmaNeededToDrop", true);
		DROP_ITEMS_ON_DIE = pvpSettings.getProperty("DropOnDie", false);
		DROP_ITEMS_AUGMENTED = pvpSettings.getProperty("DropAugmented", false);

		KARMA_DROP_ITEM_LIMIT = pvpSettings.getProperty("MaxItemsDroppable", 10);
		MIN_PK_TO_ITEMS_DROP = pvpSettings.getProperty("MinPKToDropItems", 5);

		KARMA_RANDOM_DROP_LOCATION_LIMIT = pvpSettings.getProperty("MaxDropThrowDistance", 70);

		KARMA_DROPCHANCE_BASE = pvpSettings.getProperty("ChanceOfPKDropBase", 20.);
		KARMA_DROPCHANCE_MOD = pvpSettings.getProperty("ChanceOfPKsDropMod", 1.);
		NORMAL_DROPCHANCE_BASE = pvpSettings.getProperty("ChanceOfNormalDropBase", 1.);
		DROPCHANCE_EQUIPPED_WEAPON = pvpSettings.getProperty("ChanceOfDropWeapon", 3);
		DROPCHANCE_EQUIPMENT = pvpSettings.getProperty("ChanceOfDropEquippment", 17);
		DROPCHANCE_ITEM = pvpSettings.getProperty("ChanceOfDropOther", 80);

		KARMA_LIST_NONDROPPABLE_ITEMS = new ArrayList<>();
		for(int id : pvpSettings.getProperty("ListOfNonDroppableItems", new int[] { 57 }))
			KARMA_LIST_NONDROPPABLE_ITEMS.add(id);

		PVP_TIME = pvpSettings.getProperty("PvPTime", 40000);

		/* PK System */
		PK_SYSTEM_ENABLE = pvpSettings.getProperty("PK_SystemEnable", false);
		PK_SYSTEM_KILLER_LEVEL_FOR_REWARD = pvpSettings.getProperty("PK_LevelForReward", new int[] { 20, 85 });
		PK_SYSTEM_TARGET_LEVEL_FOR_REWARD = pvpSettings.getProperty("PK_TargetLevelForReward", new int[] { 20, 85 });
		PK_SYSTEM_ALLOW_REWARD = pvpSettings.getProperty("PK_AllowRewardItem", false);
		PK_SYSTEM_ITEM_INFO = pvpSettings.getProperty("PK_RewardItemID", new int[] { 0, 0, 0 });
		PK_SYSTEM_ADD_EXP_SP = pvpSettings.getProperty("PK_AllowExpSp", false);
		PK_SYSTEM_EXP_SP = pvpSettings.getProperty("PK_XpSpIncreaseRate", new int[] { 0, 0 });
		PK_SYSTEM_ENABLE_BLOCK_TIME = pvpSettings.getProperty("PK_EnableBlockTime", false);
		PK_SYSTEM_BLOCK_TIME_AFTER_KILL = pvpSettings.getProperty("PK_BlockTimeAfterKill", 900);
		PK_SYSTEM_ANNOUNCE = pvpSettings.getProperty("PK_AnnouncePlayerKiller", false);
		PK_SYSTEM_ANNOUNCE_RADIUS = pvpSettings.getProperty("PK_AnnounceRadius", 900);
		PK_SYSTEM_IN_ZONE = pvpSettings.getProperty("PK_SystemInZone", false);
		PK_SYSTEM_ZONE = pvpSettings.getProperty("PK_ZoneId", "[colosseum_battle]");

		/* PvP System */
		PvP_SYSTEM_ENABLE = pvpSettings.getProperty("PvP_SystemEnable", false);
		PvP_SYSTEM_KILLER_LEVEL_FOR_REWARD = pvpSettings.getProperty("PvP_LevelForReward", new int[] { 20, 85 });
		PvP_SYSTEM_TARGET_LEVEL_FOR_REWARD = pvpSettings.getProperty("PvP_TargetLevelForReward", new int[] { 20, 85 });
		PvP_SYSTEM_ALLOW_REWARD = pvpSettings.getProperty("PvP_AllowRewardItem", false);
		PvP_SYSTEM_ITEM_INFO = pvpSettings.getProperty("PvP_RewardItemID", new int[] { 0, 0, 0 });
		PvP_SYSTEM_ADD_EXP_SP = pvpSettings.getProperty("PvP_AllowExpSp", false);
		PvP_SYSTEM_EXP_SP = pvpSettings.getProperty("PvP_XpSpIncreaseRate", new int[] { 0, 0 });
		PvP_SYSTEM_ENABLE_BLOCK_TIME = pvpSettings.getProperty("PvP_EnableBlockTime", false);
		PvP_SYSTEM_BLOCK_TIME_AFTER_KILL = pvpSettings.getProperty("PvP_BlockTimeAfterKill", 900);
		PvP_SYSTEM_ANNOUNCE = pvpSettings.getProperty("PvP_AnnouncePlayerKiller", false);
		PvP_SYSTEM_ANNOUNCE_RADIUS = pvpSettings.getProperty("PvP_AnnounceRadius", 900);
		PvP_SYSTEM_IN_ZONE = pvpSettings.getProperty("PvP_SystemInZone", false);
		PvP_SYSTEM_ZONE = pvpSettings.getProperty("PvP_ZoneId", "[colosseum_battle]");
	}

	public static void loadAISettings()
	{
		ExProperties aiSettings = load(AI_CONFIG_FILE);
		AI_TASK_MANAGER_COUNT = aiSettings.getProperty("AiTaskManagers", 1);
		AI_TASK_ATTACK_DELAY = aiSettings.getProperty("AiTaskDelay", 1000);
		AI_TASK_ACTIVE_DELAY = aiSettings.getProperty("AiTaskActiveDelay", 1000);
		BLOCK_ACTIVE_TASKS = aiSettings.getProperty("BlockActiveTasks", false);
		ALWAYS_TELEPORT_HOME = aiSettings.getProperty("AlwaysTeleportHome", false);
        DRAGON_MIGRATION_PERIOD = aiSettings.getProperty("DragonValleyMigrationPeriod", 60);
        DRAGON_MIGRATION_CHANCE = aiSettings.getProperty("DragonValleyMigrationChance", 33);
		RND_WALK = aiSettings.getProperty("RndWalk", true);
		RND_WALK_RATE = aiSettings.getProperty("RndWalkRate", 1);
		RND_ANIMATION_RATE = aiSettings.getProperty("RndAnimationRate", 2);

		AGGRO_CHECK_INTERVAL = aiSettings.getProperty("AggroCheckInterval", 250);
		NONAGGRO_TIME_ONTELEPORT = aiSettings.getProperty("NonAggroTimeOnTeleport", 15000);
		MAX_DRIFT_RANGE = aiSettings.getProperty("MaxDriftRange", 100);
		MAX_PURSUE_RANGE = aiSettings.getProperty("MaxPursueRange", 4000);
		MAX_PURSUE_UNDERGROUND_RANGE = aiSettings.getProperty("MaxPursueUndergoundRange", 2000);
		MAX_PURSUE_RANGE_RAID = aiSettings.getProperty("MaxPursueRangeRaid", 5000);
		ALT_AI_KELTIRS = aiSettings.getProperty("AltAiKeltirs", false);
		ALT_AI_TAURIN = aiSettings.getProperty("AltAiTaurin", false);
		AI_KANABION_RESPAWN_TIME = aiSettings.getProperty("KanabionRespawnTime", 1000);
	}

	public static void loadGatekeeperSettings()
	{
		ExProperties gatekeeperSettings = load(GATEKEEPER_CONFIG_FILE);

		MULTILANG_GATEKEEPER = gatekeeperSettings.getProperty("MultiLangGatekeeper", false);
		ALLOW_EVENT_GATEKEEPER = gatekeeperSettings.getProperty("AllowEventGatekeeper", false);
		GATEKEEPER_MODIFIER = gatekeeperSettings.getProperty("GkCostMultiplier", 1.0);
		GATEKEEPER_FREE = gatekeeperSettings.getProperty("GkFree", 40);
		CRUMA_GATEKEEPER_LVL = gatekeeperSettings.getProperty("GkCruma", 65);
	}

	public static void loadGeodataSettings()
	{
		ExProperties geodataSettings = load(GEODATA_CONFIG_FILE);

		DAMAGE_FROM_FALLING = geodataSettings.getProperty("DamageFromFalling", true);
		GEO_EDITOR_HOST = geodataSettings.getProperty("GeoEditorHost", "*");

		SHIFT_BY = geodataSettings.getProperty("HShift", 12);
		SHIFT_BY_Z = geodataSettings.getProperty("VShift", 11);
		MAP_MIN_Z = geodataSettings.getProperty("MapMinZ", -32768);
		MAP_MAX_Z = geodataSettings.getProperty("MapMaxZ", 32767);

		LINEAR_TERRITORY_CELL_SIZE = geodataSettings.getProperty("LinearTerritoryCellSize", 32);

		GEO_X_FIRST = geodataSettings.getProperty("GeoFirstX", 11);
		GEO_Y_FIRST = geodataSettings.getProperty("GeoFirstY", 10);
		GEO_X_LAST = geodataSettings.getProperty("GeoLastX", 26);
		GEO_Y_LAST = geodataSettings.getProperty("GeoLastY", 26);

		GEOFILES_PATTERN = geodataSettings.getProperty("GeoFilesPattern", "(\\d{2}_\\d{2})\\.l2j");
		ALLOW_GEODATA = geodataSettings.getProperty("AllowGeodata", true);
		ALLOW_FALL_FROM_WALLS = geodataSettings.getProperty("AllowFallFromWalls", false);
		ALLOW_KEYBOARD_MOVE = geodataSettings.getProperty("AllowMoveWithKeyboard", true);
		COMPACT_GEO = geodataSettings.getProperty("CompactGeoData", false);
		CLIENT_Z_SHIFT = geodataSettings.getProperty("ClientZShift", 16);
		PATHFIND_BOOST = geodataSettings.getProperty("PathFindBoost", 2);
		PATHFIND_DIAGONAL = geodataSettings.getProperty("PathFindDiagonal", true);
		PATH_CLEAN = geodataSettings.getProperty("PathClean", true);
		PATHFIND_MAX_Z_DIFF = geodataSettings.getProperty("PathFindMaxZDiff", 32);
		MAX_Z_DIFF = geodataSettings.getProperty("MaxZDiff", 64);
		MIN_LAYER_HEIGHT = geodataSettings.getProperty("MinLayerHeight", 64);
		PATHFIND_MAX_TIME = geodataSettings.getProperty("PathFindMaxTime", 10000000);
		PATHFIND_BUFFERS = geodataSettings.getProperty("PathFindBuffers", "8x96;8x128;8x160;8x192;4x224;4x256;4x288;2x320;2x384;2x352;1x512");
	}

	public static void loadRateSettings()
	{
		ExProperties rateSettings = load(RATE_CONFIG_FILE);
		RATE_CHANCE_DROP_ITEMS = rateSettings.getProperty("RateChanceDropItems", 1.);
		RATE_CHANCE_GROUP_DROP_ITEMS = rateSettings.getProperty("RateChanceGroupDropItems", 1.);
		RATE_CHANCE_SPOIL = rateSettings.getProperty("RateChanceSpoil", 1.);
		RATE_CHANCE_SPOIL_WEAPON_ARMOR_ACCESSORY = rateSettings.getProperty("RateChanceSpoilWAA", 1.);
		RATE_CHANCE_DROP_WEAPON_ARMOR_ACCESSORY = rateSettings.getProperty("RateChanceDropWAA", 1.);
		RATE_XP = rateSettings.getProperty("RateXp", 1.);
		RATE_SP = rateSettings.getProperty("RateSp", 1.);
		RATE_DROP_ADENA = rateSettings.getProperty("RateDropAdena", 1.);
		RATE_DROP_ITEMS = rateSettings.getProperty("RateDropItems", 1.);
		RATE_DROP_SPOIL = rateSettings.getProperty("RateDropSpoil", 1.);
		RATE_QUESTS_REWARD = rateSettings.getProperty("RateQuestsReward", 1.);
		RATE_DROP_RAIDBOSS = rateSettings.getProperty("RateRaidBoss", 1.);
		RATE_QUESTS_DROP = rateSettings.getProperty("RateQuestsDrop", 1.);
		RATE_DROP_SIEGE_GUARD = rateSettings.getProperty("RateSiegeGuard", 1.);
		RATE_DROP_SIEGE_GUARD_FOR_PREMIUM = rateSettings.getProperty("PremiumRateSiegeGuard", 1.);
		RATE_SIEGE_FAME_FOR_PREMIUM = rateSettings.getProperty("PremiumSiegeFame", 1.);
		RATE_FISH_DROP_COUNT = rateSettings.getProperty("RateFishDropCount", 1.);
		RATE_PARTY_MIN = rateSettings.getProperty("RatePartyMin", false);
		RATE_CLAN_REP_SCORE = rateSettings.getProperty("RateClanRepScore", 1.);
		RATE_MANOR = rateSettings.getProperty("RateManor", 1.);
		NO_RATE_ITEMS = rateSettings.getProperty("NoRateItemIds", new int[] { 6660, 6662, 6661, 6659, 6656, 6658, 8191, 6657, 10170, 10314, 16025, 16026 });
		NO_RATE_EQUIPMENT = rateSettings.getProperty("NoRateEquipment", true);
		NO_RATE_KEY_MATERIAL = rateSettings.getProperty("NoRateKeyMaterial", true);
		RATE_DROP_COMMON_ITEMS = rateSettings.getProperty("RateDropCommonItems", 1.);
		RATE_CLAN_REP_SCORE_MAX_AFFECTED = rateSettings.getProperty("RateClanRepScoreMaxAffected", 2);
		RATE_MOB_SPAWN = rateSettings.getProperty("RateMobSpawn", 1);
		RATE_MOB_SPAWN_MIN_LEVEL = rateSettings.getProperty("RateMobMinLevel", 1);
		RATE_MOB_SPAWN_MAX_LEVEL = rateSettings.getProperty("RateMobMaxLevel", 100);
		NO_RATE_RECIPES = rateSettings.getProperty("NoRateRecipes", true);

		RATE_MOD_DROP_SPOIL = rateSettings.getProperty("RateModDropSpoil", 1);
		RATE_MOD_DROP_ADENA = rateSettings.getProperty("RateModDropAdena", 1);
		RATE_MOD_DROP_RAIDBOSS = rateSettings.getProperty("RateModDropRaidBoss", 1);
		RATE_MOD_DROP_SIEGE_GUARD = rateSettings.getProperty("RateModDropSiegeGuard", 1);
		RATE_MOD_DROP_ITEMS = rateSettings.getProperty("RateModDropItems", 1);
		ALT_GAME_CREATION = rateSettings.getProperty("AllowAltGreationRate", false);
		ALT_GAME_CREATION_RARE_XPSP_RATE = rateSettings.getProperty("AltGreationRateXpSp", 1.);
		ALT_GAME_CREATION_XP_RATE = rateSettings.getProperty("AltGreationRateXp", 1.);
		ALT_GAME_CREATION_SP_RATE = rateSettings.getProperty("AltGreationRateSp", 1.);
	}

	public static void loadHellBoundSettings()
	{
		ExProperties HellBoundSettings = load(HELLBOUND_CONFIG_FILE);
		HELLBOUND_ENTER_NOQUEST = HellBoundSettings.getProperty("HellboundNoQuestTeleport", false);
		HELLBOUND_LEVEL = HellBoundSettings.getProperty("HellboundLevel", 0);
		RATE_HELLBOUND_CONFIDENCE = HellBoundSettings.getProperty("RateHellboundConfidence", 1.);
		OPEN_HELLBOUND_CONFIDENCE = HellBoundSettings.getProperty("OpenHellboundConfidence", 1);
        SHADAI_SPAWN_CHANCE = HellBoundSettings.getProperty("ShadaiSpawnChance", 40);
        ANNOUNCE_SHADAI_SPAWN = HellBoundSettings.getProperty("AnnounceShadaiSpawn", false);
	}

	public static void loadNpcsSettings()
	{
		ExProperties npcsSettings = load(NPCS_CONFIG_FILE);
		ALLOW_TALK_WHILE_SITTING = npcsSettings.getProperty("AllowTalkWhileSitting", true);
		RATE_RAID_REGEN = npcsSettings.getProperty("RateRaidRegen", 1.);
		RATE_RAID_DEFENSE = npcsSettings.getProperty("RateRaidDefense", 1.);
		RATE_RAID_ATTACK = npcsSettings.getProperty("RateRaidAttack", 1.);
		RATE_EPIC_DEFENSE = npcsSettings.getProperty("RateEpicDefense", RATE_RAID_DEFENSE);
		RATE_EPIC_ATTACK = npcsSettings.getProperty("RateEpicAttack", RATE_RAID_ATTACK);
		RAID_MAX_LEVEL_DIFF = npcsSettings.getProperty("RaidMaxLevelDiff", 8);
		PARALIZE_ON_RAID_DIFF = npcsSettings.getProperty("ParalizeOnRaidLevelDiff", true);
		MIN_NPC_ANIMATION = npcsSettings.getProperty("MinNPCAnimation", 5);
		MAX_NPC_ANIMATION = npcsSettings.getProperty("MaxNPCAnimation", 90);
		SERVER_SIDE_NPC_NAME = npcsSettings.getProperty("ServerSideNpcName", false);
		SERVER_SIDE_NPC_TITLE = npcsSettings.getProperty("ServerSideNpcTitle", false);
		SERVER_SIDE_NPC_TITLE_LVL_AGR = npcsSettings.getProperty("ServerSideNpcTitleLvlAgr", true);
		ALT_NPC_PATK_MODIFIER = npcsSettings.getProperty("NpcPAtkModifier", 1.0);
		ALT_NPC_MATK_MODIFIER = npcsSettings.getProperty("NpcMAtkModifier", 1.0);
		ALT_NPC_MAXHP_MODIFIER = npcsSettings.getProperty("NpcMaxHpModifier", 1.00);
		ALT_NPC_MAXMP_MODIFIER = npcsSettings.getProperty("NpcMapMpModifier", 1.00);
		MAX_VORTEX_BOSS_COUNT = npcsSettings.getProperty("MaxVortexBossCount", 0);
		TIME_DESPAWN_VORTEX_BOSS = npcsSettings.getProperty("TimeDespawnVortexBoss", 15);	}

	public static void loadCommunityBoardWarehouseSettings()
	{
		ExProperties communityBoardWarehouseSettings = load(COMMUNITY_BOARD_WAREHOUSE_CONFIG_FILE);
		ALLOW_BBS_WAREHOUSE = communityBoardWarehouseSettings.getProperty("AllowBBSWarehouse", true);
		BBS_WAREHOUSE_ALLOW_PEACE_ZONE = communityBoardWarehouseSettings.getProperty("AllowBBSWarehousePeaceZone", true);
		BBS_WAREHOUSE_ALLOW_PK = communityBoardWarehouseSettings.getProperty("BBSWarehouseAllowPK", false);
	}

	public static void loadCommunityBoardBufferSettings()
	{
		ExProperties communityBoardBufferSettings = load(COMMUNITY_BOARD_BUFFER_CONFIG_FILE);

		BBS_BUFFER_ALLOWED_BUFFER = communityBoardBufferSettings.getProperty("Enable", false);
		BBS_BUFFER_ALLOWED_PK = communityBoardBufferSettings.getProperty("AllowedBuffsPK", false);
		BBS_BUFFER_ALLOWED_CURSED_WEAPON = communityBoardBufferSettings.getProperty("AllowedBuffsCursedWeapon", false);
		CHECK_DEATH_TIME = Boolean.parseBoolean(communityBoardBufferSettings.getProperty("CheckDeathTime", "true"));
		CHECK_DEATH_TIME_VAL = (Integer.parseInt(communityBoardBufferSettings.getProperty("CheckDeathTimeVal", "30")) * 1000);
		
		BBS_BUFFER_PRICE_ID = communityBoardBufferSettings.getProperty("Id", 57);
		BBS_BUFFER_PRICE_ONE = communityBoardBufferSettings.getProperty("Price", 1000);
		BBS_BUFFER_SAVE_PRICE_ID = communityBoardBufferSettings.getProperty("SaveId", 57);
		BBS_BUFFER_SAVE_PRICE_ONE = communityBoardBufferSettings.getProperty("SavePrice", 1000);
		BBS_BUFFER_ALT_TIME = communityBoardBufferSettings.getProperty("Time", 1);
		BBS_BUFFER_MIN_LVL = communityBoardBufferSettings.getProperty("MinLevel", 1);
		BBS_BUFFER_MAX_LVL = communityBoardBufferSettings.getProperty("MaxLevel", 85);
		CBB_BUFFER_FREE_LEVEL = communityBoardBufferSettings.getProperty("FreeLevel", 40);
		BBS_BUFFER_ALLOWED_BUFF = communityBoardBufferSettings.getProperty("AllowedBuffs", new int[] {});
		BBS_BUFFER_RECOVER_HP_MP_CP = communityBoardBufferSettings.getProperty("Recover", false);
		BBS_BUFFER_CLEAR_BUFF = communityBoardBufferSettings.getProperty("Clear", false);
	}

	public static void loadCommunityBoardClassMasterSettings()
	{
		ExProperties communityBoardClassMasterSettings = load(COMMUNITY_BOARD_CLASS_MASTER_CONFIG_FILE);
		BBS_CLASS_MASTER_ALLOW = communityBoardClassMasterSettings.getProperty("Allow", false);

		for(int id : communityBoardClassMasterSettings.getProperty("Class", ArrayUtils.EMPTY_INT_ARRAY))
			if(id != 0)
				BBS_CLASS_MASTERS_ALLOW_LIST.add(id);

		BBS_CLASS_MASTER_PRICE_ITEM = communityBoardClassMasterSettings.getProperty("Item", new int[] { 57, 57, 57 });
		BBS_CLASS_MASTER_PRICE_COUNT = communityBoardClassMasterSettings.getProperty("Price", new int[] { 1000, 1000, 1000 });

		BBS_CLASS_MASTER_SUB_PRICE_ITEM = communityBoardClassMasterSettings.getProperty("SubItem", 0);
		BBS_CLASS_MASTER_ADD_SUB_CLASS = communityBoardClassMasterSettings.getProperty("SubAdd", false);
		BBS_CLASS_MASTER_SUB_ADD_PRICE_COUNT = communityBoardClassMasterSettings.getProperty("SubAPrice", 0);
		BBS_CLASS_MASTER_CHANGE_SUB_CLASS = communityBoardClassMasterSettings.getProperty("SubChange", false);
		BBS_CLASS_MASTER_SUB_CHANGE_PRICE_COUNT = communityBoardClassMasterSettings.getProperty("SubCPrice", 0);
		BBS_CLASS_MASTER_CANCEL_SUB_CLASS = communityBoardClassMasterSettings.getProperty("SubCancel", false);
		BBS_CLASS_MASTER_SUB_CANCEL_PRICE_COUNT = communityBoardClassMasterSettings.getProperty("SubDPrice", 0);
		BBS_CLASS_MASTER_BUY_NOBLESSE = communityBoardClassMasterSettings.getProperty("Noble", false);
	}

	public static void loadCommunityBoardCheckConditionSettings()
	{
		ExProperties communityBoardCheckConditionSettings = load(COMMUNITY_BOARD_CHECK_CONDITION_CONFIG_FILE);

		BBS_CHECK_OUT_OF_TOWN_ONLY_FOR_PREMIUM = communityBoardCheckConditionSettings.getProperty("OutOfTownForPremium", false);
		BBS_CHECK_MOVEMENT_DISABLE = communityBoardCheckConditionSettings.getProperty("MovementDisabled", false);
		BBS_CHECK_IN_COMBAT = communityBoardCheckConditionSettings.getProperty("InCombat", false);
		BBS_CHECK_DEATH = communityBoardCheckConditionSettings.getProperty("Death", false);
		BBS_CHECK_ON_SIEGE_FIELD = communityBoardCheckConditionSettings.getProperty("OnSiegeField", false);
		BBS_CHECK_ATTACKING_NOW = communityBoardCheckConditionSettings.getProperty("AttackingNow", false);
		BBS_CHECK_IN_OLYMPIAD_MODE = communityBoardCheckConditionSettings.getProperty("InOlympiadMode", false);
		BBS_CHECK_FLYING = communityBoardCheckConditionSettings.getProperty("Flying", false);
		BBS_CHECK_IN_DUEL = communityBoardCheckConditionSettings.getProperty("InDuel", false);
		BBS_CHECK_IN_INSTANCE = communityBoardCheckConditionSettings.getProperty("InInstance", false);
		BBS_CHECK_IN_JAILED = communityBoardCheckConditionSettings.getProperty("InJailed", false);
		BBS_CHECK_OUT_OF_CONTROL = communityBoardCheckConditionSettings.getProperty("OutOfControl", false);
		BBS_CHECK_IN_EVENT = communityBoardCheckConditionSettings.getProperty("InEvent", false);
	}

	public static void loadCommunityBoardCommissionSettings()
	{
		ExProperties communityBoardCommissionSettings = load(COMMUNITY_BOARD_COMMISSION_CONFIG_FILE);

		BBS_COMMISSION_ALLOW = communityBoardCommissionSettings.getProperty("AllowCommision", false);
		BBS_COMMISSION_ARMOR_PRICE = communityBoardCommissionSettings.getProperty("ArmorPrice", new int[] { 57, 10000 });
		BBS_COMMISSION_WEAPON_PRICE = communityBoardCommissionSettings.getProperty("WeaponPrice", new int[] { 57, 10000 });
		BBS_COMMISSION_JEWERLY_PRICE = communityBoardCommissionSettings.getProperty("JewerlyPrice", new int[] { 57, 10000 });
		BBS_COMMISSION_OTHER_PRICE = communityBoardCommissionSettings.getProperty("OtherPrice", new int[] { 57, 10000 });
		BBS_COMMISSION_ALLOW_ITEMS = communityBoardCommissionSettings.getProperty("AllowItems", new int[] { 57, 4037 });
		BBS_COMMISSION_NOT_ALLOW_ITEMS = communityBoardCommissionSettings.getProperty("NotAllowItems", new int[] { 57, 4037 });
		BBS_COMMISSION_MAX_ENCHANT = communityBoardCommissionSettings.getProperty("MaxEnchant", 20);
		BBS_COMMISSION_HIDE_OLD_ITEMS = communityBoardCommissionSettings.getProperty("HideOld", false);
		BBS_COMMISSION_HIDE_OLD_AFTER = communityBoardCommissionSettings.getProperty("HideAfter", 7);
		BBS_COMMISSION_ALLOW_PVP = communityBoardCommissionSettings.getProperty("SellPvP", false);
		BBS_COMMISSION_ALLOW_EQUIPPED = communityBoardCommissionSettings.getProperty("SellEquipped", false);
		BBS_COMMISSION_ALLOW_UNDERWEAR = communityBoardCommissionSettings.getProperty("SellUnderwear", false);
		BBS_COMMISSION_ALLOW_CLOAK = communityBoardCommissionSettings.getProperty("SellCloak", false);
		BBS_COMMISSION_ALLOW_BRACELET = communityBoardCommissionSettings.getProperty("SellBraclet", false);
		BBS_COMMISSION_ALLOW_AUGMENTED = communityBoardCommissionSettings.getProperty("SellAugmented", false);
		BBS_COMMISSION_COUNT_TO_PAGE = communityBoardCommissionSettings.getProperty("SellInPage", 5);
		BBS_COMMISSION_ITEMS = communityBoardCommissionSettings.getProperty("Item", new int[] { 57, 4037 });
		BBS_COMMISSION_MAIL_TIME = communityBoardCommissionSettings.getProperty("MailTime", 30);
	}

	public static void loadCommunityBoardEnchantSettings()
	{
		ExProperties communityBoardEnchantSettings = load(COMMUNITY_BOARD_ENCHANT_CONFIG_FILE);

		BBS_ENCHANT_ITEM = communityBoardEnchantSettings.getProperty("Item", 4356);
		BBS_ENCHANT_MAX = communityBoardEnchantSettings.getProperty("MaxEnchant", new int[] { 25 });
		BBS_WEAPON_ENCHANT_LVL = communityBoardEnchantSettings.getProperty("WValue", new int[] { 5 });
		BBS_ARMOR_ENCHANT_LVL = communityBoardEnchantSettings.getProperty("AValue", new int[] { 5 });
		BBS_JEWELS_ENCHANT_LVL = communityBoardEnchantSettings.getProperty("JValue", new int[] { 5 });
		BBS_ENCHANT_PRICE_WEAPON = communityBoardEnchantSettings.getProperty("WPrice", new int[] { 5 });
		BBS_ENCHANT_PRICE_ARMOR = communityBoardEnchantSettings.getProperty("APrice", new int[] { 5 });
		BBS_ENCHANT_PRICE_JEWELS = communityBoardEnchantSettings.getProperty("JPrice", new int[] { 5 });
		BBS_ENCHANT_ATRIBUTE_LVL_WEAPON = communityBoardEnchantSettings.getProperty("AtributeWeaponValue", new int[] { 25 });
		BBS_ENCHANT_ATRIBUTE_PRICE_WEAPON = communityBoardEnchantSettings.getProperty("PriceForAtributeWeapon", new int[] { 25 });
		BBS_ENCHANT_ATRIBUTE_LVL_ARMOR = communityBoardEnchantSettings.getProperty("AtributeArmorValue", new int[] { 25 });
		BBS_ENCHANT_ATRIBUTE_PRICE_ARMOR = communityBoardEnchantSettings.getProperty("PriceForAtributeArmor", new int[] { 25 });
		BBS_ENCHANT_ATRIBUTE_PVP = communityBoardEnchantSettings.getProperty("AtributePvP", true);
		BBS_ENCHANT_WEAPON_ATTRIBUTE_MAX = communityBoardEnchantSettings.getProperty("MaxWAttribute", 25);
		BBS_ENCHANT_ARMOR_ATTRIBUTE_MAX = communityBoardEnchantSettings.getProperty("MaxAAttribute", 25);

		BBS_ENCHANT_HEAD_ATTRIBUTE = communityBoardEnchantSettings.getProperty("AtributeHead", true);
		BBS_ENCHANT_CHEST_ATTRIBUTE = communityBoardEnchantSettings.getProperty("AtributeChest", true);
		BBS_ENCHANT_LEGS_ATTRIBUTE = communityBoardEnchantSettings.getProperty("AtributeLegs", true);
		BBS_ENCHANT_GLOVES_ATTRIBUTE = communityBoardEnchantSettings.getProperty("AtributeGloves", true);
		BBS_ENCHANT_FEET_ATTRIBUTE = communityBoardEnchantSettings.getProperty("AtributeFeet", true);
		Item_Custom_Template = communityBoardEnchantSettings.getProperty("ItemCustomTemplate", new int[] { 10, 20, 30 });
		Item_Custom_Template_ID_PRICE = communityBoardEnchantSettings.getProperty("ItemCustomTemplateIdPrice", 25);
		Item_Custom_Template_ID_PRICE_COUNT = communityBoardEnchantSettings.getProperty("ItemCustomTemplatePriceCount", 25);


		BBS_ENCHANT_WEAPON_ATTRIBUTE = communityBoardEnchantSettings.getProperty("AtributeWeapon", true);
		BBS_ENCHANT_SHIELD_ATTRIBUTE = communityBoardEnchantSettings.getProperty("AtributeShield", false);
		BBS_ENCHANT_GRADE_ATTRIBUTE = communityBoardEnchantSettings.getProperty("AtributeGrade", "NG:NO;D:NO;C:NO;B:NO;A:ON;S:ON;S80:ON;S84:ON").trim().replaceAll(" ", "").split(";");
	}

	public static void loadCommunityBoardGlobalSettings()
	{
		ExProperties communityBoardGlobalSettings = load(COMMUNITY_BOARD_GLOBAL_CONFIG_FILE);

		COMMUNITYBOARD_ENABLED = communityBoardGlobalSettings.getProperty("AllowCommunityBoard", true);
		BBS_DEFAULT = communityBoardGlobalSettings.getProperty("Link", "_bbshome");
		BBS_FOLDER = communityBoardGlobalSettings.getProperty("FolderPath", "off");
		COMMUNITYBOARD_NAME = communityBoardGlobalSettings.getProperty("Name", "");
		COMMUNITYBOARD_COPY = communityBoardGlobalSettings.getProperty("Copyright", "");
		COMMUNITYBOARD_SERVER_ADMIN_NAME = communityBoardGlobalSettings.getProperty("ServerAdminNames", "L2Dream");
		COMMUNITYBOARD_SERVER_SUPPORT_NAME = communityBoardGlobalSettings.getProperty("ServerSupportNames", "L2Dream");
		COMMUNITYBOARD_SERVER_GM_NAME = communityBoardGlobalSettings.getProperty("ServerGMNames", "L2Dream");
		COMMUNITYBOARD_FORUM_ADMIN_NAME = communityBoardGlobalSettings.getProperty("ForumAdminNames", "L2Dream");
	}
	public static void loadInstanceSettings()
	{
		ExProperties InstanceSettings= load(INSTANCE_FILE_CONFIG);
        ALLOW_INSTANCES_LEVEL_MANUAL = InstanceSettings.getProperty("AllowInstancesLevelManual", false);
        ALLOW_INSTANCES_PARTY_MANUAL = InstanceSettings.getProperty("AllowInstancesPartyManual", false);
        INSTANCES_LEVEL_MIN = InstanceSettings.getProperty("InstancesLevelMin", 1);
        INSTANCES_LEVEL_MAX = InstanceSettings.getProperty("InstancesLevelMax", 85);
        INSTANCES_PARTY_MIN = InstanceSettings.getProperty("InstancesPartyMin", 2);
        INSTANCES_PARTY_MAX = InstanceSettings.getProperty("InstancesPartyMax", 100);
        TIAT_KILLS_FOR_SOD = InstanceSettings.getProperty("TiatKillsForSoD", 10);
        EKIMUS_KILLS_FOR_SOI = InstanceSettings.getProperty("EkimusKillsForSoI", 3);
        BROTHER_KILLS_FOR_EKIMUS = InstanceSettings.getProperty("BrothersKillsForEkimus", false);
        COHEMENES_KILLS_FOR_SOI = InstanceSettings.getProperty("CohemenesKillsForEkimus", 10);
        HALL_OF_ERROSION_DEF_COUNT = InstanceSettings.getProperty("HallOfErrosinDefCount", 10);
        SOD_OPEN_TIME = InstanceSettings.getProperty("SodOpenTime", 12);
        SOI_OPEN_TIME = InstanceSettings.getProperty("SoiOpenTime", 12);

	}
	public static void loadBossSettings()
	{
		ExProperties BossSettings= load(BOSS_FILE_CONFIG);
		ENABLE_ANNOUNCE_BOSS = BossSettings.getProperty("EnableAnnounceBoss", false);
		if(ENABLE_ANNOUNCE_BOSS)
		{
			final String bossList = BossSettings.getProperty("AnnounceBossList", "");
			ANNOUNCE_BOSS_RESPAWN = new GArray<Integer>();
			for(final String id : bossList.trim().split(","))
				ANNOUNCE_BOSS_RESPAWN.add(Integer.parseInt(id.trim()));
		}
        KILL_BARAKIEL_SET_NOBLE = BossSettings.getProperty("AllowBecomeNobleKillBarakiel", false);
        ALLOW_ANNOUNCE_NOBLE_RB = BossSettings.getProperty("AnnounceNobleRBSpawn", false);
		FIXINTERVALOFANTHARAS_HOUR = BossSettings.getProperty("RespawnTimeAntharas", 264);
		SPAWN_ANTHARAS_TIME = BossSettings.getProperty("SpawnTimeAntharas", 10);
		FIXINTERVALSLEEPANTHARAS = BossSettings.getProperty("SleepTimeAntharas", 15);
		FIXINTERVALOFVALAKAS = BossSettings.getProperty("RespawnTimeValakas", 264);
		SPAWN_VALAKAS_TIME = BossSettings.getProperty("SpawnTimeValakas", 20);
		FIXINTERVALSLEEPVALACAS = BossSettings.getProperty("SleepTimeValakas", 20);
		FIXINTERVALOFSAILRENSPAWN_HOUR = BossSettings.getProperty("RespawnTimeSailren", 24);
		RANDOMINTERVALOFSAILRENSPAWN = BossSettings.getProperty("RandomRespawnTimeSailren", 24);
		FIXINTERVALOFBAIUM_HOUR = BossSettings.getProperty("RespawnTimeBaium", 120);
		RANDOMINTERVALOFBAIUM = BossSettings.getProperty("RandomRespawnTimeBaium", 8);
		FIXINTERVALSLEEPBAIUM = BossSettings.getProperty("SleepTimeBaium", 30);
		FIXINTERVALOFBELETHSPAWN_HOUR = BossSettings.getProperty("RespawnTimeBeleth", 48);
		FIXINTERVALOFBAYLORSPAWN_HOUR = BossSettings.getProperty("RespawnTimeBaylor", 1440);
		RANDOMINTERVALOFBAYLORSPAWN =BossSettings.getProperty("RandomRespawnBaylor", 1440);
	}
	public static void loadSkillSettings()
	{
		ExProperties SkillSettings= load(SKILL_FILE_CONFIG);
		
		SKILL_CHANCE_ENABLE_ON = SkillSettings.getProperty("SkillChanceNewPlayer", true);
		SKILL_CHANCE_ENABLE = SkillSettings.getProperty("SkillChanceConfig", true);
		ALT_SHOW_REUSE_MSG = SkillSettings.getProperty("AltShowSkillReuseMessage", true);
		ALT_DISABLE_SPELLBOOKS = SkillSettings.getProperty("AltDisableSpellbooks", false);
		ALT_SAVE_UNSAVEABLE = SkillSettings.getProperty("AltSaveUnsaveable", false);
		ALT_AUTO_LEARN_SKILLS = SkillSettings.getProperty("AutoLearnSkills", false);
		ALT_AUTO_LEARN_FORGOTTEN_SKILLS = SkillSettings.getProperty("AutoLearnForgottenSkills", false);
		ALT_ALLOW_CLANSKILLS = SkillSettings.getProperty("AllowClanSkills", true);
		ALT_NOBLESSE_BLESSING = SkillSettings.getProperty("AltNoblesseBlessing", false);
		ALT_ALLOW_LEARN_TRANS_SKILLS_WO_QUEST = SkillSettings.getProperty("AllowLearnTransSkillsWOQuest", false);
		ALT_REMOVE_SKILLS_ON_DELEVEL = SkillSettings.getProperty("AltRemoveSkillsOnDelevel", true);
		ALT_ALL_PHYS_SKILLS_OVERHIT = SkillSettings.getProperty("AltAllPhysSkillsOverhit", true);
		ALT_AUGMENTATION_NG_SKILL_CHANCE = SkillSettings.getProperty("AugmentationNGSkillChance", 15);
		ALT_AUGMENTATION_NG_GLOW_CHANCE = SkillSettings.getProperty("AugmentationNGGlowChance", 0);
		ALT_AUGMENTATION_MID_SKILL_CHANCE = SkillSettings.getProperty("AugmentationMidSkillChance", 30);
		ALT_AUGMENTATION_MID_GLOW_CHANCE = SkillSettings.getProperty("AugmentationMidGlowChance", 40);
		ALT_AUGMENTATION_HIGH_SKILL_CHANCE = SkillSettings.getProperty("AugmentationHighSkillChance", 45);
		ALT_AUGMENTATION_HIGH_GLOW_CHANCE = SkillSettings.getProperty("AugmentationHighGlowChance", 70);
		ALT_AUGMENTATION_TOP_SKILL_CHANCE = SkillSettings.getProperty("AugmentationTopSkillChance", 60);
		ALT_AUGMENTATION_TOP_GLOW_CHANCE = SkillSettings.getProperty("AugmentationTopGlowChance", 100);
		ALT_AUGMENTATION_BASESTAT_CHANCE = SkillSettings.getProperty("AugmentationBaseStatChance", 1);
		ALT_AUGMENTATION_ACC_SKILL_CHANCE = SkillSettings.getProperty("AugmentationAccSkillChance", 10);
		UNSTUCK_SKILL = SkillSettings.getProperty("UnstuckSkill", true);
        AUTOCP_SKILL = SkillSettings.getProperty("AutoCPSkill", false);
        AUTOCP_SKILL_TICK = SkillSettings.getProperty("AutoCPSkillTick", 1000);
		ALT_DISPEL_MUSIC = SkillSettings.getProperty("AltDispelDanceSong", false);


	}
	public static void loadCommunityBoardServicesSettings()
	{
		ExProperties communityBoardServicesSettings = load(COMMUNITY_BOARD_SERVICES_CONFIG_FILE);
		ALLOW_BBS_MAMMON = communityBoardServicesSettings.getProperty("AllowBBSMammon", true);
		ALLOW_BBS_WIKI = communityBoardServicesSettings.getProperty("AllowBBSWiki", true);

		BBS_MAMMON_ALLOW_PEACE_ZONE = communityBoardServicesSettings.getProperty("AllowBBSMammonPeaceZone", true);
		BBS_WASH_SINS_PRICE = communityBoardServicesSettings.getProperty("WashSinsPrice", 10000);
		BBS_WASH_SINS_PRICE_ITEM_ID = communityBoardServicesSettings.getProperty("WashSinsPriceID", 57);
		BBS_CLEAR_PK_PRICE = communityBoardServicesSettings.getProperty("ClearPkPrice", 10000);
		BBS_CLEAR_PK_PRICE_ITEM_ID = communityBoardServicesSettings.getProperty("ClearPkPriceID", 57);
		BBS_CLEAR_PK_COUNT = communityBoardServicesSettings.getProperty("ClearPkCount", 1);
		BBS_VIP_SECTION_PRICE = communityBoardServicesSettings.getProperty("VipSectionPrice", 1000);
		BBS_VIP_SECTION_ITEM_ID = communityBoardServicesSettings.getProperty("VipSectionID", 4037);
	}
	public static void loadCommunityBoardStatsSettings()
	{
		ExProperties communityBoardStatsSettings = load(COMMUNITY_BOARD_STATS_CONFIG_FILE);
		CBB_ONLINE_CHEAT_COUNT = communityBoardStatsSettings.getProperty("OnlineCheatCount", 0);
		CBB_OFFTRADE_CHEAT_COUNT = communityBoardStatsSettings.getProperty("OfftradeCheatCount", 0);
	}

	public static void loadCommunityBoardNewsSettings()
	{
		ExProperties communityBoardNewsSettings = load(COMMUNITY_BOARD_NEWS_CONFIG_FILE);

		BBS_NEWS_UPDATE_TIME = communityBoardNewsSettings.getProperty("UpdateTime", 5 * 60);
	}

	public static void loadCommunityBoardTeleportSettings()
	{
		ExProperties communityBoardTeleportSettings = load(COMMUNITY_BOARD_TELEPORT_CONFIG_FILE);

		BBS_TELEPORT_PRICE = communityBoardTeleportSettings.getProperty("Price", 5000);
		BBS_TELEPORT_ITEM_ID = communityBoardTeleportSettings.getProperty("ItemId", 57);
		BBS_TELEPORT_SAVE_ITEM_ID = communityBoardTeleportSettings.getProperty("SaveItemId", 57);
		BBS_TELEPORT_SAVE_PRICE = communityBoardTeleportSettings.getProperty("SavePrice", 5000);
		BBS_TELEPORT_PRICE_PA = communityBoardTeleportSettings.getProperty("PriceForPremium", true);
		BBS_TELEPORT_POINTS_PA = communityBoardTeleportSettings.getProperty("SavePointForPremium", false);
		BBS_TELEPORT_FREE_LEVEL = communityBoardTeleportSettings.getProperty("FreeByLevel", 40);
		BBS_TELEPORT_MAX_COUNT = communityBoardTeleportSettings.getProperty("MaxPointsCount", 7);
		BBS_TELEPORT_ALLOW_IN_INSTANCE = communityBoardTeleportSettings.getProperty("InInstance", false);
		BBS_TELEPORT_ALLOW_IN_UNDERWATHER = communityBoardTeleportSettings.getProperty("UnderWater", false);
		BBS_TELEPORT_ALLOW_IN_COMBAT = communityBoardTeleportSettings.getProperty("InCombat", false);
		BBS_TELEPORT_ALLOW_IN_COMBAT = communityBoardTeleportSettings.getProperty("InSiege", false);
	}

	public static void loadCommunityBoardLotterySettings()
	{
		ExProperties communityBoardLotterySettings = load(COMMUNITY_BOARD_GAME_LOTTERY_CONFIG_FILE);

		BBS_GAME_LOTTERY_ALLOW = communityBoardLotterySettings.getProperty("Allow", false);
		BBS_GAME_LOTTERY_BET = communityBoardLotterySettings.getProperty("Bet", new int[] { 1000, 4000, 50000, 250000, 1000000, 5000000 });
		BBS_GAME_LOTTERY_WIN_CHANCE = communityBoardLotterySettings.getProperty("WChance", 35.5);
		BBS_GAME_LOTTERY_JACKPOT_CHANCE = communityBoardLotterySettings.getProperty("JChance", 0.01);
		BBS_GAME_LOTTERY_JACKTOP_STARTED_COUNT= communityBoardLotterySettings.getProperty("NJackpot", 10000);
		BBS_GAME_LOTTERY_LOOS_TO_JACKPOT = communityBoardLotterySettings.getProperty("ToJackpot", 10);
		BBS_GAME_LOTTERY_ITEM = communityBoardLotterySettings.getProperty("Item", 57);
		BBS_GAME_LOTTERY_REWARD_MULTIPLE = communityBoardLotterySettings.getProperty("Reward", 4);
	}
	
	public static void loadCommunityBoardAcademSettings()
	{
		ExProperties communityBoardAcademSettings = load(COMMUNITY_BOARD_ACADEM_CONFIG_FILE);
		
		SERVICES_CLAN_ACADEM_ENABLED = communityBoardAcademSettings.getProperty("EnableServiceAcadem", false);

	}
	public static void loadAprilFoolsSettings()
	{
		ExProperties eventAprilFoolsSettings = load(EVENT_APRIL_FOOLS_FILE);

		EVENT_APIL_FOOLS_DROP_CHANCE = eventAprilFoolsSettings.getProperty("AprilFollsDropChance", 50.);
	}

	public static void loadBountyHuntersSettings()
	{
		ExProperties eventBountyHuntersSettings = load(EVENT_BOUNTY_HUNTERS_FILE);

		EVENT_BOUNTY_HUNTERS_ENABLED = eventBountyHuntersSettings.getProperty("BountyHuntersEnabled", true);
	}

	public static void loadChangeOfHeartSettings()
	{
		ExProperties eventChangeOfHeartSettings = load(EVENT_CHANGE_OF_HEART_FILE);

		EVENT_CHANGE_OF_HEART_CHANCE = eventChangeOfHeartSettings.getProperty("EVENT_CHANGE_OF_HEART_CHANCE", 5.);
	}

	public static void loadCofferOfShadowsSettings()
	{
		ExProperties eventCofferOfShadowsSettings = load(EVENT_COFFER_OF_SHADOWS_FILE);

		EVENT_CofferOfShadowsPriceRate = eventCofferOfShadowsSettings.getProperty("CofferOfShadowsPriceRate", 1.);
		EVENT_CofferOfShadowsRewardRate = eventCofferOfShadowsSettings.getProperty("CofferOfShadowsRewardRate", 1.);
	}

	public static void loadGlitteringMedalSettings()
	{
		ExProperties eventGlitteringMedalSettings = load(EVENT_GLITTERING_MEDAL_FILE);

		EVENT_GLITTMEDAL_NORMAL_CHANCE = eventGlitteringMedalSettings.getProperty("MEDAL_CHANCE", 10.);
		EVENT_GLITTMEDAL_GLIT_CHANCE = eventGlitteringMedalSettings.getProperty("GLITTMEDAL_CHANCE", 0.1);
	}

	public static void loadHitmanSettings()
	{
		ExProperties eventHitmanSettings = load(EVENT_HITMAN_FILE);

		EVENT_HITMAN_ENABLED = eventHitmanSettings.getProperty("HitmanEnabled", false);
		EVENT_HITMAN_COST_ITEM_ID = eventHitmanSettings.getProperty("CostItemId", 57);
		EVENT_HITMAN_COST_ITEM_COUNT = eventHitmanSettings.getProperty("CostItemCount", 1000);
		EVENT_HITMAN_TASKS_PER_PAGE = eventHitmanSettings.getProperty("TasksPerPage", 7);
		EVENT_HITMAN_ALLOWED_ITEM_LIST = eventHitmanSettings.getProperty("AllowedItems", new String[] { "4037", "57" });
	}

	public static void loadL2DaySettings()
	{
		ExProperties eventL2DaySettings = load(EVENT_L2_DAY_FILE);

		EVENT_L2DAY_LETTER_CHANCE = eventL2DaySettings.getProperty("L2DAY_LETTER_CHANCE", 1.);
	}

	public static void loadFightClubSettings()
	{
		ExProperties eventFightClubSettings = load(EVENT_FIGHT_CLUB_FILE);

		FIGHT_CLUB_ENABLED = eventFightClubSettings.getProperty("Enabled", false);
		FIGHT_CLUB_MINIMUM_LEVEL_TO_PARRICIPATION = eventFightClubSettings.getProperty("MinimumLevel", 1);
		FIGHT_CLUB_MAXIMUM_LEVEL_TO_PARRICIPATION = eventFightClubSettings.getProperty("MaximumLevel", 85);
		FIGHT_CLUB_MAXIMUM_LEVEL_DIFFERENCE = eventFightClubSettings.getProperty("MaximumLevelDifference", 10);
		FIGHT_CLUB_ALLOWED_RATE_ITEMS = eventFightClubSettings.getProperty("AllowedItems", "").trim().replaceAll(" ", "").split(",");
		FIGHT_CLUB_PLAYERS_PER_PAGE = eventFightClubSettings.getProperty("RatesOnPage", 10);
		FIGHT_CLUB_ARENA_TELEPORT_DELAY = eventFightClubSettings.getProperty("ArenaTeleportDelay", 5);
		FIGHT_CLUB_CANCEL_BUFF_BEFORE_FIGHT = eventFightClubSettings.getProperty("CancelBuffs", true);
		FIGHT_CLUB_UNSUMMON_PETS = eventFightClubSettings.getProperty("UnsummonPets", true);
		FIGHT_CLUB_UNSUMMON_SUMMONS = eventFightClubSettings.getProperty("UnsummonSummons", true);
		FIGHT_CLUB_REMOVE_CLAN_SKILLS = eventFightClubSettings.getProperty("RemoveClanSkills", false);
		FIGHT_CLUB_REMOVE_HERO_SKILLS = eventFightClubSettings.getProperty("RemoveHeroSkills", false);
		FIGHT_CLUB_TIME_TO_PREPARATION = eventFightClubSettings.getProperty("TimeToPreparation", 10);
		FIGHT_CLUB_FIGHT_TIME = eventFightClubSettings.getProperty("TimeToDraw", 300);
		FIGHT_CLUB_ALLOW_DRAW = eventFightClubSettings.getProperty("AllowDraw", true);
		FIGHT_CLUB_TIME_TELEPORT_BACK = eventFightClubSettings.getProperty("TimeToBack", 10);
		FIGHT_CLUB_ANNOUNCE_RATE = eventFightClubSettings.getProperty("AnnounceRate", false);
	}

	public static void loadTreasuresOfTheHeraldSettings()
	{
		ExProperties eventTreasuresOfTheHeraldSettings = load(EVENT_TREASURES_OF_THE_HERALD_FILE);

		EVENT_TREASURES_OF_THE_HERALD_ENABLE = eventTreasuresOfTheHeraldSettings.getProperty("Enable", false);
		EVENT_TREASURES_OF_THE_HERALD_ITEM_ID = eventTreasuresOfTheHeraldSettings.getProperty("RewardId", 13067);
		EVENT_TREASURES_OF_THE_HERALD_ITEM_COUNT = eventTreasuresOfTheHeraldSettings.getProperty("RewardCount", 30);
		EVENT_TREASURES_OF_THE_HERALD_TIME = eventTreasuresOfTheHeraldSettings.getProperty("Time", 1200);
		EVENT_TREASURES_OF_THE_HERALD_MIN_LEVEL = eventTreasuresOfTheHeraldSettings.getProperty("MinLevel", 80);
		EVENT_TREASURES_OF_THE_HERALD_MAX_LEVEL = eventTreasuresOfTheHeraldSettings.getProperty("MaxLevel", 85);
		EVENT_TREASURES_OF_THE_HERALD_MINIMUM_PARTY_MEMBER = eventTreasuresOfTheHeraldSettings.getProperty("MinPartyMember", 6);
		EVENT_TREASURES_OF_THE_HERALD_MAX_GROUP = eventTreasuresOfTheHeraldSettings.getProperty("MaxGroup", 100);
		EVENT_TREASURES_OF_THE_HERALD_SCORE_BOX = eventTreasuresOfTheHeraldSettings.getProperty("ScoreBox", 20);
		EVENT_TREASURES_OF_THE_HERALD_SCORE_BOSS = eventTreasuresOfTheHeraldSettings.getProperty("ScoreBoss", 100);
		EVENT_TREASURES_OF_THE_HERALD_SCORE_KILL = eventTreasuresOfTheHeraldSettings.getProperty("ScoreKill", 5);
		EVENT_TREASURES_OF_THE_HERALD_SCORE_DEATH = eventTreasuresOfTheHeraldSettings.getProperty("ScoreDeath", 3);
	}
    public static void loadDefenseTownSettings() {
        ExProperties TmWaweSettings = load(DEFENSE_TOWNS_CONFIG_FILE);
        TMEnabled = TmWaweSettings.getProperty("DefenseTownsEnabled", false);
        TMStartHour = TmWaweSettings.getProperty("DefenseTownsStartHour", 19);
        TMStartMin = TmWaweSettings.getProperty("DefenseTownsStartMin", 0);

        TMEventInterval = TmWaweSettings.getProperty("DefenseTownsEventInterval", 0);

        TMMobLife = TmWaweSettings.getProperty("DefenseTownsMobLife", 10) * 60000;

        BossLifeTime = TmWaweSettings.getProperty("BossLifeTime", 25) * 60000;

        TMTime1 = TmWaweSettings.getProperty("DefenseTownsTime1", 2) * 60000;
        TMTime2 = TmWaweSettings.getProperty("DefenseTownsTime2", 5) * 60000;
        TMTime3 = TmWaweSettings.getProperty("DefenseTownsTime3", 5) * 60000;
        TMTime4 = TmWaweSettings.getProperty("DefenseTownsTime4", 5) * 60000;
        TMTime5 = TmWaweSettings.getProperty("DefenseTownsTime5", 5) * 60000;
        TMTime6 = TmWaweSettings.getProperty("DefenseTownsTime6", 5) * 60000;

        TMWave1 = TmWaweSettings.getProperty("DefenseTownsWave1", 18855);
        TMWave2 = TmWaweSettings.getProperty("DefenseTownsWave2", 18855);
        TMWave3 = TmWaweSettings.getProperty("DefenseTownsWave3", 25699);
        TMWave4 = TmWaweSettings.getProperty("DefenseTownsWave4", 18855);
        TMWave5 = TmWaweSettings.getProperty("DefenseTownsWave5", 18855);
        TMWave6 = TmWaweSettings.getProperty("DefenseTownsWave6", 25699);

        TMWave1Count = TmWaweSettings.getProperty("DefenseTownsWave1Count", 3);
        TMWave2Count = TmWaweSettings.getProperty("DefenseTownsWave2Count", 2);
        TMWave3Count = TmWaweSettings.getProperty("DefenseTownsWave3Count", 2);
        TMWave4Count = TmWaweSettings.getProperty("DefenseTownsWave4Count", 2);
        TMWave5Count = TmWaweSettings.getProperty("DefenseTownsWave5Count", 2);
        TMWave6Count = TmWaweSettings.getProperty("DefenseTownsWave6Count", 2);

        TMBoss = TmWaweSettings.getProperty("DefenseTownsBoss", 25700);

        TMItem = TmWaweSettings.getProperty("DefenseTownsItem", new int[]{4037, 57, 9552, 9553, 9554, 9555, 9556, 9557, 6577, 6578});
        TMItemCol = TmWaweSettings.getProperty("DefenseTownsItemCol", new int[]{1, 77700000, 1, 1, 1, 1, 1, 1, 1, 1});
        TMItemColBoss = TmWaweSettings.getProperty("DefenseTownsItemColBoss", new int[]{5, 77700000, 10, 10, 10, 10, 10, 10, 2, 2});
        TMItemChance = TmWaweSettings.getProperty("DefenseTownsItemChance", new int[]{20, 40, 10, 10, 10, 10, 10, 10, 20, 20});
        TMItemChanceBoss = TmWaweSettings.getProperty("DefenseTownsItemChanceBoss", new int[]{50, 40, 50, 50, 50, 50, 50, 50, 20, 20});
    }
	public static void loadLastHeroSettings()
	{
		ExProperties eventLastHeroSettings = load(EVENT_LAST_HERO_FILE);

		EVENT_LAST_HERO_GIVE_ITEM = eventLastHeroSettings.getProperty("GiveBonus", true);
		EVENT_LAST_HERO_ITEM_ID = eventLastHeroSettings.getProperty("BonusId", 57);
		EVENT_LAST_HERO_ITEM_COUNT = eventLastHeroSettings.getProperty("BonusCount", 5000.);
		EVENT_LAST_HERO_RATE = eventLastHeroSettings.getProperty("Rate", true);
		EVENT_LAST_HERO_GIVE_ITEM_FINAL = eventLastHeroSettings.getProperty("GiveBonusFinal", true);
		EVENT_LAST_HERO_ITEM_ID_FINAL = eventLastHeroSettings.getProperty("BonusFinalId", 57);
		EVENT_LAST_HERO_ITEM_COUNT_FINAL = eventLastHeroSettings.getProperty("BonusCountFinal", 10000.);
		EVENT_LAST_HERO_RATE_FINAL = eventLastHeroSettings.getProperty("RateFinal", true);
		EVENT_LAST_HERO_TIME = eventLastHeroSettings.getProperty("Time", 3);
		EVENT_LAST_HERO_START_TIME = eventLastHeroSettings.getProperty("StartTime", "20:00").trim().replaceAll(" ", "").split(",");
		EVENT_LAST_HERO_CATEGORIES = eventLastHeroSettings.getProperty("Categories", false);
		EVENT_LAST_HERO_ALLOW_SUMMONS = eventLastHeroSettings.getProperty("AllowSummons", false);
		EVENT_LAST_HERO_ALLOW_BUFFS = eventLastHeroSettings.getProperty("AllowBuffs", false);
		EVENT_LAST_HERO_ALLOW_MULTI_REGISTER = eventLastHeroSettings.getProperty("AllowMultiRegLH", false);
		EVENT_LAST_HERO_CHECK_WINDOW_METHOD = eventLastHeroSettings.getProperty("CheckWindowMethodLH", "IP");
		 
		EVENT_LAST_HERO_RUNNING_TIME = eventLastHeroSettings.getProperty("EventRunningTime", 20);
		EVENT_LAST_HERO_FIGHTER_BUFFS = eventLastHeroSettings.getProperty("FighterBuffs", "").trim().replaceAll(" ", "").split(";");
		EVENT_LAST_HERO_MAGE_BUFFS = eventLastHeroSettings.getProperty("MageBuffs", "").trim().replaceAll(" ", "").split(";");
		EVENT_LAST_HERO_BUFF_PLAYERS = eventLastHeroSettings.getProperty("BuffPlayers", false);
		EVENT_LAST_HERO_AURA_ENABLE = eventLastHeroSettings.getProperty("GiveHero", false);
		EVENT_LAST_HERO_ALLOW_HEROES = eventLastHeroSettings.getProperty("AllowHeroRegister", false);
		EVENTS_LH_DISALLOWED_SKILLS = eventLastHeroSettings.getProperty("LH_DisallowedSkills", "").trim().replaceAll(" ", "").split(";");
	}

	public static void loadCaptureTheFlagSettings()
	{
		ExProperties eventCaptureTheFlagSettings = load(EVENT_CAPTURE_THE_FLAG_FILE);

		EVENT_CTF_REWARDS = eventCaptureTheFlagSettings.getProperty("Rewards", "").trim().replaceAll(" ", "").split(";");
		EVENT_CTF_TIME = eventCaptureTheFlagSettings.getProperty("Time", 3);
		EVENT_CTF_RUNNING_TIME = eventCaptureTheFlagSettings.getProperty("RunningTime", 10);
		EVENT_CTF_RATE = eventCaptureTheFlagSettings.getProperty("Rate", true);
		EVENT_CTF_START_TIME = eventCaptureTheFlagSettings.getProperty("StartTime", "20:00").trim().replaceAll(" ", "").split(",");
		EVENT_CTF_CATEGORIES = eventCaptureTheFlagSettings.getProperty("Categories", false);
		EVENT_CTF_MAX_PLAYER_IN_TEAM = eventCaptureTheFlagSettings.getProperty("MaxPlayerInTeam", 20);
		EVENT_CTF_MIN_PLAYER_IN_TEAM = eventCaptureTheFlagSettings.getProperty("MinPlayerInTeam", 2);
		EVENT_CTF_ALLOW_SUMMONS = eventCaptureTheFlagSettings.getProperty("AllowSummons", false);
		EVENT_CTF_ALLOW_BUFFS = eventCaptureTheFlagSettings.getProperty("AllowBuffs", false);
		EVENT_CTF_ALLOW_MULTI_REGISTER = eventCaptureTheFlagSettings.getProperty("AllowMultiReg", false);
		EVENT_CTF_CHECK_WINDOW_METHOD = eventCaptureTheFlagSettings.getProperty("CheckWindowMethod", "IP");
		EVENT_CTF_FIGHTER_BUFFS = eventCaptureTheFlagSettings.getProperty("FighterBuffs", "").trim().replaceAll(" ", "").split(";");
		EVENT_CTF_MAGE_BUFFS = eventCaptureTheFlagSettings.getProperty("MageBuffs", "").trim().replaceAll(" ", "").split(";");
		EVENT_CTF_BUFF_PLAYERS = eventCaptureTheFlagSettings.getProperty("BuffPlayers", false);
		EVENTS_CTF_DISALLOWED_SKILLS = eventCaptureTheFlagSettings.getProperty("CTF_DisallowedSkills", "").trim().replaceAll(" ", "").split(";");
	}

	public static void loadDeathMatchSettings()
	{
		ExProperties eventDeathMatchSettings = load(EVENT_DEATH_MATCH_FILE);

		EVENT_DEATH_MATCH_REWARDS = eventDeathMatchSettings.getProperty("Rewards", "").trim().replaceAll(" ", "").split(";");
		EVENT_DEATH_MATCH_TIME = eventDeathMatchSettings.getProperty("Time", 3);
		EVENT_DEATH_MATCH_RUNNING_TIME = eventDeathMatchSettings.getProperty("RunningTime", 10);
		EVENT_DEATH_MATCH_RATE = eventDeathMatchSettings.getProperty("Rate", true);
		EVENT_DEATH_MATCH_START_TIME = eventDeathMatchSettings.getProperty("StartTime", "20:00").trim().replaceAll(" ", "").split(",");
		EVENT_DEATH_MATCH_CATEGORIES = eventDeathMatchSettings.getProperty("Categories", false);
		EVENT_DEATH_MATCH_MAX_PLAYER_IN_TEAM = eventDeathMatchSettings.getProperty("MaxPlayerInTeam", 20);
		EVENT_DEATH_MATCH_MIN_PLAYER_IN_TEAM = eventDeathMatchSettings.getProperty("MinPlayerInTeam", 2);
		EVENT_DEATH_MATCH_ALLOW_SUMMONS = eventDeathMatchSettings.getProperty("AllowSummons", false);
		EVENT_DEATH_MATCH_ALLOW_BUFFS = eventDeathMatchSettings.getProperty("AllowBuffs", false);
		EVENT_DEATH_MATCH_ALLOW_MULTI_REGISTER = eventDeathMatchSettings.getProperty("AllowMultiReg", false);
		EVENT_DEATH_MATCH_CHECK_WINDOW_METHOD = eventDeathMatchSettings.getProperty("CheckWindowMethod", "IP");
		EVENT_DEATH_MATCH_FIGHTER_BUFFS = eventDeathMatchSettings.getProperty("FighterBuffs", "").trim().replaceAll(" ", "").split(";");
		EVENT_DEATH_MATCH_MAGE_BUFFS = eventDeathMatchSettings.getProperty("MageBuffs", "").trim().replaceAll(" ", "").split(";");
		EVENT_DEATH_MATCH_BUFF_PLAYERS = eventDeathMatchSettings.getProperty("BuffPlayers", false);
		EVENTS_DM_DISALLOWED_SKILLS = eventDeathMatchSettings.getProperty("DM_DisallowedSkills", "").trim().replaceAll(" ", "").split(";");

	}

	public static void loadMarch8Settings()
	{
		ExProperties eventMarch8Settings = load(EVENT_MARCH_8_FILE);

		EVENT_MARCH8_DROP_CHANCE = eventMarch8Settings.getProperty("March8DropChance", 10.);
		EVENT_MARCH8_PRICE_RATE = eventMarch8Settings.getProperty("March8PriceRate", 1.);
	}

	public static void loadMasterOfEnchaningSettings()
	{
		ExProperties eventMasterOfEnchaningSettings = load(EVENT_MASTER_OF_ENCHANING_FILE);

		ENCHANT_CHANCE_MASTER_YOGI_STAFF = eventMasterOfEnchaningSettings.getProperty("MasterYogiEnchantChance", 66);
		ENCHANT_MAX_MASTER_YOGI_STAFF = eventMasterOfEnchaningSettings.getProperty("MasterYogiEnchantMaxWeapon", 28);
		SAFE_ENCHANT_MASTER_YOGI_STAFF = eventMasterOfEnchaningSettings.getProperty("MasterYogiSafeEnchant", 3);
	}

	public static void loadPcBangSettings()
	{
		ExProperties eventPcBangSettings = load(EVENT_PC_BANG_FILE);

		ALT_PCBANG_POINTS_ENABLED = eventPcBangSettings.getProperty("AltPcBangPointsEnabled", false);
		ALT_MAX_PC_BANG_POINTS = eventPcBangSettings.getProperty("AltPcBangPointsMaxCount", 20000);
		ALT_PCBANG_POINTS_ON_START = eventPcBangSettings.getProperty("AltPcBangPointsOnStart", 300);
		ALT_PCBANG_POINTS_BONUS = eventPcBangSettings.getProperty("AltPcBangPointsBonus", 100);
		ALT_PCBANG_POINTS_DELAY = eventPcBangSettings.getProperty("AltPcBangPointsDelay", 5);
		ALT_PCBANG_POINTS_MIN_LVL = eventPcBangSettings.getProperty("AltPcBangPointsMinLvl", 1);
		ALT_PCBANG_POINTS_BONUS_DOUBLE_CHANCE = eventPcBangSettings.getProperty("AltPcBangPointsDoubleChance", 10.);
		ALT_PCBANG_POINTS_MAX_CODE_ENTER_ATTEMPTS = eventPcBangSettings.getProperty("AltPcBangPointsMaxEnterAttempts", 5);
		ALT_PCBANG_POINTS_BAN_TIME = eventPcBangSettings.getProperty("AltPcBangPointsBanTime", 480L);
		ALT_PCBANG_POINTS_COUPON_TEMPLATE = eventPcBangSettings.getProperty("AltPcBangPointsCouponTemplate", "[A-Za-z0-9]{4,20}");
		PC_BANG_ENCHANT_MAX = eventPcBangSettings.getProperty("AltPcBangEnchantMaxLevel", 23);
		PC_BANG_SAFE_ENCHANT = eventPcBangSettings.getProperty("AltPcBangEnchantSafeLevel", 3);
		ALT_PC_BANG_WIVERN_PRICE = eventPcBangSettings.getProperty("AltPcBangWiwernRentPrice", 2500);
		ALT_PC_BANG_WIVERN_TIME = eventPcBangSettings.getProperty("AltPcBangWiwernRentTime", 5);
	}

	public static void loadSavingSnowmanSettings()
	{
		ExProperties eventSavingSnowmanSettings = load(EVENT_SAVING_SNOWMAN_FILE);

		EVENT_SAVING_SNOWMAN_LOTERY_PRICE = eventSavingSnowmanSettings.getProperty("SavingSnowmanLoteryPrice", 50000);
		EVENT_SAVING_SNOWMAN_REWARDER_CHANCE = eventSavingSnowmanSettings.getProperty("SavingSnowmanRewarderChance", 2);
	}

	public static void loadTheFallHarvestSettings()
	{
		ExProperties eventTheFallHarvestSettings = load(EVENT_THE_FALL_HARVEST_FILE);

		EVENT_TFH_POLLEN_CHANCE = eventTheFallHarvestSettings.getProperty("TFH_POLLEN_CHANCE", 5.);
	}

	public static void loadTrickOfTransmutationSettings()
	{
		ExProperties eventTrickOfTransmutationSettings = load(EVENT_TRICK_OF_TRANSMUTATION_FILE);

		EVENT_TRICK_OF_TRANS_CHANCE = eventTrickOfTransmutationSettings.getProperty("TRICK_OF_TRANS_CHANCE", 10.);
	}

	public static void loadL2CoinsSettings()
	{
		ExProperties eventL2CoinsSettings = load(EVENT_L2COIN);

		EVENT_MOUSE_COIN_ID = eventL2CoinsSettings.getProperty("Coin", 10639);
		EVENT_MOUSE_COIN_CHANCE = eventL2CoinsSettings.getProperty("CoinChance", 10.);
		EVENT_MOUSE_COIN_MIN_COUNT = eventL2CoinsSettings.getProperty("CoinMinCount", 1);
		EVENT_MOUSE_COIN_MAX_COUNT = eventL2CoinsSettings.getProperty("CoinMaxCount", 4);
		//EVENT_BASE_COIN_AFTER_RB = eventL2CoinsSettings.getProperty("L2CoinsBaseCoinAfterRB", 40);

		EVENT_MOUSE_ALTERNATIVE = eventL2CoinsSettings.getProperty("Alternative", false);

		EVENT_MOUSE_ALTERNATIVE_RATE = eventL2CoinsSettings.getProperty("AlternativeRate", false);

		EVENT_MOUSE_ALTERNATIVE_LVL_GAP = eventL2CoinsSettings.getProperty("AlternativeLvl", 5);

		EVENT_MOUSE_ALT_CHANCE_LVL_40_60 = eventL2CoinsSettings.getProperty("AlternativeChanceLvl_40_60", 25.);
		EVENT_MOUSE_ALT_COUNT_LVL_40_60 = eventL2CoinsSettings.getProperty("AlternativeCountLvl_40_60", 2);

		EVENT_MOUSE_ALT_CHANCE_LVL_61_75 = eventL2CoinsSettings.getProperty("AlternativeChanceLvl_61_75", 50.);
		EVENT_MOUSE_ALT_COUNT_LVL_61_75 = eventL2CoinsSettings.getProperty("AlternativeCountLvl_61_75", 4);

		EVENT_MOUSE_ALT_CHANCE_LVL_76_80 = eventL2CoinsSettings.getProperty("AlternativeChanceLvl_76_80", 75.);
		EVENT_MOUSE_ALT_COUNT_LVL_76_80 = eventL2CoinsSettings.getProperty("AlternativeCountLvl_76_80", 6);

		EVENT_MOUSE_ALT_CHANCE_LVL_81_85 = eventL2CoinsSettings.getProperty("AlternativeChanceLvl_81_85", 100.);
		EVENT_MOUSE_ALT_COUNT_LVL_81_85 = eventL2CoinsSettings.getProperty("AlternativeCountLvl_81_85", 2);

	}

	public static void loadSimpleEventSettings()
	{
		ExProperties eventSimpleSettings = load(EVENT_SIMPLE);

		SIMPLE_COIN_CHANCE = eventSimpleSettings.getProperty("CoinChance", 10.);
		SIMPLE_COIN = eventSimpleSettings.getProperty("Coin", new int[] {});
		SIMPLE_COIN_MIN_COUNT = eventSimpleSettings.getProperty("CoinMinCount", 1);
		SIMPLE_COIN_MAX_COUNT = eventSimpleSettings.getProperty("CoinMaxCount", 4);
		SIMPLE_MONSTER_MIN_LEVEL = eventSimpleSettings.getProperty("MonsterMinLevel", 76);
		SIMPLE_MONSTER_MAX_LEVEL = eventSimpleSettings.getProperty("MonsterMaxLevel", 85);

		SIMPLE_COIN_CHANCE_RB = eventSimpleSettings.getProperty("RaidBossCoinChance", 10.);
		SIMPLE_COIN_RB = eventSimpleSettings.getProperty("RaidBossCoin", new int[] {});
		SIMPLE_COIN_MIN_COUNT_RB = eventSimpleSettings.getProperty("RaidBossCoinMinCount", 1);
		SIMPLE_COIN_MAX_COUNT_RB = eventSimpleSettings.getProperty("RaidBossCoinMaxCount", 4);
		SIMPLE_MONSTER_MIN_LEVEL_RB = eventSimpleSettings.getProperty("RaidBossMinLevel", 76);
		SIMPLE_MONSTER_MAX_LEVEL_RB = eventSimpleSettings.getProperty("RaidBossMaxLevel", 85);

		SIMPLE_EVENT_MANAGER_ID = eventSimpleSettings.getProperty("NpcId", 32508);
		SIMPLE_EVENT_MANAGER_MULTISELL = eventSimpleSettings.getProperty("MultisellId", 3250801);
		SIMPLE_RATE_ITEM_BY_HP = eventSimpleSettings.getProperty("RateItemByHP", true);
	}

	public static void loadOlympiadSettings()
	{
		ExProperties olympSettings = load(OLYMPIAD_DATA_FILE);

		OLYMPIAD_ENABLE = olympSettings.getProperty("Enable", true);
        SHOW_OLYMPIAD_PROF = olympSettings.getProperty("ShowOlympiadProf", false);
		OLYMPIAD_ENABLE_SPECTATING = olympSettings.getProperty("Spectating", true);
		OLYMPIAD_START_TIME = olympSettings.getProperty("Hours", 18);
		OLYMPIAD_MIN = olympSettings.getProperty("Minutes", 0);
		OLYMPIAD_CPERIOD = olympSettings.getProperty("CPeriod", 21600000);
		OLYMPIAD_WPERIOD = olympSettings.getProperty("WPeriod", 604800000);
		OLYMPIAD_VPERIOD = olympSettings.getProperty("VPeriod", 43200000);

		OLYMPIAD_USE_MONTHLY_PERIOD = olympSettings.getProperty("MonthlyPeriod", true);
		OLYMPIAD_WEEKLY_WEEKCOUNT = olympSettings.getProperty("WeekCount", 1);
		OLYMPIAD_WEEKLY_PERIOD_ENDDAY = olympSettings.getProperty("EndDay", 2);
		OLYMPIAD_PERIOD_END_DAYS.clear();
		for(String id : olympSettings.getProperty("PeriodEndDays", "1").split(";"))
			OLYMPIAD_PERIOD_END_DAYS.add(Integer.parseInt(id));

		OLYMPIAD_CLASS_GAME_MIN = olympSettings.getProperty("Class", 5);
		OLYMPIAD_NONCLASS_GAME_MIN = olympSettings.getProperty("NonClass", 9);
		OLYMPIAD_TEAM_GAME_MIN = olympSettings.getProperty("Team", 4);
		OLYMPIAD_BEGIN_TIME = olympSettings.getProperty("OlympiadBeginTime", 120);
		OLYMPIAD_GAME_MAX_LIMIT = olympSettings.getProperty("GameLimit", 70);
		OLYMPIAD_GAME_CLASSES_COUNT_LIMIT = olympSettings.getProperty("GameClasses", 30);
		OLYMPIAD_GAME_NOCLASSES_COUNT_LIMIT = olympSettings.getProperty("GameNoClasses", 60);
		OLYMPIAD_GAME_TEAM_COUNT_LIMIT = olympSettings.getProperty("GameTeam", 10);

		OLYMPIAD_BATTLE_REWARD_ITEM = olympSettings.getProperty("BattleItem", 13722);
		OLYMPIAD_CLASSED_RITEM_C = olympSettings.getProperty("ClassedItemCount", 50);
		OLYMPIAD_NONCLASSED_RITEM_C = olympSettings.getProperty("NonClassedItemCount", 40);
		OLYMPIAD_TEAM_RITEM_C = olympSettings.getProperty("TeamItemCount", 50);
		OLYMPIAD_COMP_RITEM = olympSettings.getProperty("CompItem", 13722);
		OLYMPIAD_GP_PER_POINT = olympSettings.getProperty("GPPerPoint", 1000);
		OLYMPIAD_HERO_POINTS = olympSettings.getProperty("HeroPoints", 180);
		OLYMPIAD_RANK1_POINTS = olympSettings.getProperty("Rank1Points", 120);
		OLYMPIAD_RANK2_POINTS = olympSettings.getProperty("Rank2Points", 80);
		OLYMPIAD_RANK3_POINTS = olympSettings.getProperty("Rank3Points", 55);
		OLYMPIAD_RANK4_POINTS = olympSettings.getProperty("Rank4Points", 35);
		OLYMPIAD_RANK5_POINTS = olympSettings.getProperty("Rank5Points", 20);
		OLYMPIAD_STADIAS_COUNT = olympSettings.getProperty("StadiasCount", 160);
		OLYMPIAD_BATTLES_FOR_REWARD = olympSettings.getProperty("BattlesForReward", 15);
		OLYMPIAD_POINTS_DEFAULT = olympSettings.getProperty("PointsDefault", 50);
		OLYMPIAD_POINTS_WEEKLY = olympSettings.getProperty("PointsWeekly", 10);
		OLYMPIAD_OLDSTYLE_STAT = olympSettings.getProperty("OldStyleStat", false);
		CHECK_OLYMPIAD_HWID = olympSettings.getProperty("HWIDCheker", false);
		CHECK_OLYMPIAD_IP = olympSettings.getProperty("IPCheker", false);
        OLY_ENCH_LIMIT_ENABLE = olympSettings.getProperty("OlyEnchantLimit", false);
        OLY_ENCHANT_LIMIT_WEAPON = olympSettings.getProperty("OlyEnchantLimitWeapon", 0);
        OLY_ENCHANT_LIMIT_ARMOR = olympSettings.getProperty("OlyEnchantLimitArmor", 0);
        OLY_ENCHANT_LIMIT_JEWEL = olympSettings.getProperty("OlyEnchantLimitJewel", 0);
	}

	public static void loadUndergroundColiseumSettings()
	{
		ExProperties undergroundColiseumSettings = load(EVENT_UNDERGROUND_COLISEUM_FILE);

		ALT_ENABLE_UNDERGROUND_BATTLE_EVENT = undergroundColiseumSettings.getProperty("ArenaEnable", true);
		ALT_MIN_UNDERGROUND_BATTLE_TEAM_MEMBERS = undergroundColiseumSettings.getProperty("MinPlayersInTeam", 5);
		EVENT_UNDERGROUND_COLISEUM_ONLY_PATY = undergroundColiseumSettings.getProperty("EnterOnlyPaty", true);
	}

	public static void loadTvTArenaSettings()
	{
		ExProperties eventTvTArenaSettings = load(EVENT_TVT_ARENA_FILE);

		EVENT_TVT_ARENA_ENABLED = eventTvTArenaSettings.getProperty("Enabled", false);
		EVENT_TVT_ARENA_TECH_REASON = eventTvTArenaSettings.getProperty("TechReason", 0);
		EVENT_TVT_ARENA_NO_PLAYERS = eventTvTArenaSettings.getProperty("NoPlayers", 0);
		EVENT_TVT_ARENA_TEAM_DRAW = eventTvTArenaSettings.getProperty("Drow", 0);
		EVENT_TVT_ARENA_TEAM_WIN = eventTvTArenaSettings.getProperty("Win", 0);
		EVENT_TVT_ARENA_TEAM_LOSS = eventTvTArenaSettings.getProperty("Loss", 0);
		EVENT_TVT_ARENA_TEAMLEADER_EXIT = eventTvTArenaSettings.getProperty("TeamLeaderExit", 0);
		EVENT_TVT_ARENA_ALLOW_CLAN_SKILL = eventTvTArenaSettings.getProperty("AllowClanSkills", false);
		EVENT_TVT_ARENA_ALLOW_HERO_SKILL = eventTvTArenaSettings.getProperty("AllowHeroSkills", false);
		EVENT_TVT_ARENA_ALLOW_BUFFS = eventTvTArenaSettings.getProperty("AllowBuffs", false);
		EVENT_TVT_ARENA_TEAM_COUNT = eventTvTArenaSettings.getProperty("TeamCount", 0);
		EVENT_TVT_ARENA_TIME_TO_START = eventTvTArenaSettings.getProperty("TimeToStart", 0);
		EVENT_TVT_ARENA_FIGHT_TIME = eventTvTArenaSettings.getProperty("FightTime", 10);
		EVENT_TVT_ARENA_TEAM_COUNT_MIN = eventTvTArenaSettings.getProperty("MinTeamCount", 1);
		EVENT_TVT_ARENA_START_TIME = eventTvTArenaSettings.getProperty("EventStartTime", "20:12").trim().replaceAll(" ", "").split(",");
		EVENT_TVT_ARENA_STOP_TIME = eventTvTArenaSettings.getProperty("EventStopTime", "21:12").trim().replaceAll(" ", "").split(",");
		EVENTS_TVTAREA_DISALLOWED_SKILLS = eventTvTArenaSettings.getProperty("TvTArea_DisallowedSkills", "").trim().replaceAll(" ", "").split(";");
	}

	public static void loadGVGSettings()
	{
		ExProperties eventGVGSettings = load(EVENT_GVG_FILE);

        EVENT_GvGDisableEffect = eventGVGSettings.getProperty("GvGDisableEffect", false);

		
	}
	public static String[] EVENT_TvTStartTime;

	public static class EventInterval
	{
		public final int hour;
		public final int minute;
		public final int category;

		public EventInterval(final int h, final int m, final int category)
		{
			hour = h;
			minute = m;
			this.category = category;
		}
	}
	public static void loadQuestRateSettings()
	{
		ExProperties questRateSettings = load(RATE_QUEST_CONFIG_FILE);

		_001_ADENA_RATE = questRateSettings.getProperty("_001_AdenaRate", 1.0);
		_001_EXP_RATE = questRateSettings.getProperty("_001_ExpRate", 1.0);
		_001_SP_RATE = questRateSettings.getProperty("_001_SpRate", 1.0);
		_001_ITEM_RATE = questRateSettings.getProperty("_001_ItemRate", 1.0);
	}

	public static void loadItemsSettings()
	{
		ExProperties itemsSettings = load(ITEMS_CONFIG_FILE);

		ITEMS_INFINITE_SPIRIT_SHOT = itemsSettings.getProperty("ISpirit", true);
		DONT_DESTROY_ARROWS = itemsSettings.getProperty("DontDestroyArrows", false);
		ITEMS_INFINITE_BLESSED_SPIRIT_SHOT = itemsSettings.getProperty("IBlessSpirit", true);
		ITEMS_INFINITE_SOUL_SHOT = itemsSettings.getProperty("ISoul", true);
		ITEMS_GET_PREMIUM = itemsSettings.getProperty("PremiumUse", new int[] { 0 });
		ITEMS_GET_PREMIUM_DAYS = itemsSettings.getProperty("PremiumUseDays", new int[] { 0 });
		ITEMS_GET_PREMIUM_VALUE = itemsSettings.getProperty("PremiumUseValue", new double[] { 1. });
		ITEMS_GET_PREMIUM_RANDOM_DAYS = itemsSettings.getProperty("PremiumRandomDays", new int[] { 0, 7 });
		DISABLE_GRADE_PENALTY = itemsSettings.getProperty("DisableGradePenalty", false);

	}
	public static boolean BUFF_STORE_ENABLED;
	public static boolean BUFF_STORE_MP_ENABLED;
	public static double BUFF_STORE_MP_CONSUME_MULTIPLIER;
	public static boolean BUFF_STORE_ITEM_CONSUME_ENABLED;
	public static List<Integer> BUFF_STORE_ALLOWED_CLASS_LIST;
	public static List<Integer> BUFF_STORE_FORBIDDEN_SKILL_LIST;

	public static void loadBuffStoreConfig()
	{
		ExProperties buffStoreConfig = load(BUFF_STORE_CONFIG_FILE);
		BUFF_STORE_ENABLED = buffStoreConfig.getProperty("BuffStoreEnabled", false);
		BUFF_STORE_MP_ENABLED = buffStoreConfig.getProperty("BuffStoreMpEnabled", true);
		BUFF_STORE_MP_CONSUME_MULTIPLIER = buffStoreConfig.getProperty("BuffStoreMpConsumeMultiplier", 1.0f);
		BUFF_STORE_ITEM_CONSUME_ENABLED = buffStoreConfig.getProperty("BuffStoreItemConsumeEnabled", true);

		final String[] classes = buffStoreConfig.getProperty("BuffStoreAllowedClassList", "").split(",");
		BUFF_STORE_ALLOWED_CLASS_LIST = new ArrayList<>();
		if(classes.length > 0)
		{
			for(String classId : classes)
			{
				BUFF_STORE_ALLOWED_CLASS_LIST.add(Integer.parseInt(classId));
			}
		}

		final String[] skills = buffStoreConfig.getProperty("BuffStoreForbiddenSkillList", "").split(",");
		BUFF_STORE_FORBIDDEN_SKILL_LIST = new ArrayList<>();
		if(skills.length > 0)
		{
			for(String skillId : skills)
			{
				BUFF_STORE_FORBIDDEN_SKILL_LIST.add(Integer.parseInt(skillId));
			}
		}
	}
    public static void load()
	{
		loadServerConfig();
		loadLicenseSettings();
		loadChatConfig();
		loadTelnetConfig();
		loadVersionSettings();
		loadResidenceConfig();
		loadOtherConfig();
		loadEnchantSettings();
		loaditemmalSettings();
		loadSpoilConfig();
		loadFormulasConfig();
		loadBuffStoreConfig();
		loadAltSettings();
		loadServicesBashSettings();
		loadServicesEnterWorldSettings();
		loadServicesClanSettings();
		loadServicesSecuritySettings();
		loadServicesWeddingSettings();
		loadServicesBonusSettings();
		loadServicesCharacterCreateSettings();
		loadServicesCharacterSettings();
		loadServicesOffTradeSettings();
		loadServicesOtherSettings();
		loadPvPSettings();
		loadAISettings();
		loadGatekeeperSettings();
		loadGeodataSettings();
		loadAprilFoolsSettings();
		loadBountyHuntersSettings();
		loadCaptureTheFlagSettings();
		loadChangeOfHeartSettings();
		loadCofferOfShadowsSettings();
		loadFightClubSettings();
		loadGlitteringMedalSettings();
		loadTreasuresOfTheHeraldSettings();
		loadDefenseTownSettings();
		loadHitmanSettings();
		loadL2DaySettings();
		loadLastHeroSettings();
		loadMarch8Settings();
		loadMasterOfEnchaningSettings();
		loadPcBangSettings();
		loadSavingSnowmanSettings();
		loadDeathMatchSettings();
		loadTheFallHarvestSettings();
		loadTrickOfTransmutationSettings();
		loadL2CoinsSettings();
		loadSimpleEventSettings();
		loadOlympiadSettings();
		loadExtSettings();
		loadPhantomsConfig();
		loadTopSettings();
		loadRateSettings();
		loadInstanceSettings();
		loadBossSettings();
		loadSkillSettings();
		loadHellBoundSettings();
		loadNpcsSettings();
		loadCommunityBoardBufferSettings();
		loadCommunityBoardWarehouseSettings();
		loadCommunityBoardClassMasterSettings();
		loadCommunityBoardCheckConditionSettings();
		loadCommunityBoardCommissionSettings();
		loadCommunityBoardEnchantSettings();
		loadCommunityBoardGlobalSettings();
		loadCommunityBoardServicesSettings();
		loadCommunityBoardStatsSettings();
		loadCommunityBoardNewsSettings();
		loadCommunityBoardTeleportSettings();
		loadCommunityBoardLotterySettings();
		loadCommunityBoardAcademSettings();
		loadQuestRateSettings();
		loadUndergroundColiseumSettings();
		loadTvTArenaSettings();
		loadGVGSettings();
		loadTVTSettings();
		loadItemsSettings();
		abuseLoad();
		loadGMAccess();
	}

	private Config() {}

	public static void abuseLoad()
	{
		List<Pattern> tmp = new ArrayList<>();

		LineNumberReader lnr = null;
		try
		{
			String line;

			lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(ANUSEWORDS_CONFIG_FILE), "UTF-8"));

			while((line = lnr.readLine()) != null)
			{
				StringTokenizer st = new StringTokenizer(line, "\n\r");
				if(st.hasMoreTokens())
					tmp.add(Pattern.compile(".*" + st.nextToken() + ".*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
			}

			ABUSEWORD_LIST = tmp.toArray(new Pattern[tmp.size()]);
			tmp.clear();
			_log.info("Abuse: Loaded " + ABUSEWORD_LIST.length + " abuse words.");
		}
		catch(IOException e1)
		{
			_log.warn("Error reading abuse: " + e1);
		}
		finally
		{
			try
			{
				if(lnr != null)
					lnr.close();
			}
			catch(Exception ignored) {} // nothing
		}
	}

	public static void loadGMAccess()
	{
		gmlist.clear();
		loadGMAccess(new File(GM_PERSONAL_ACCESS_FILE));
		File dir = new File(GM_ACCESS_FILES_DIR);
		if(!dir.exists() || !dir.isDirectory())
		{
			_log.info("Dir " + dir.getAbsolutePath() + " not exists.");
			return;
		}
		for(File f : dir.listFiles())
			if(!f.isDirectory() && f.getName().endsWith(".xml"))
				loadGMAccess(f);
	}

	public static void loadGMAccess(File file)
	{
		try
		{
			Field fld;
			//File file = new File(filename);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			Document doc = factory.newDocumentBuilder().parse(file);

			for(Node z = doc.getFirstChild(); z != null; z = z.getNextSibling())
				for(Node n = z.getFirstChild(); n != null; n = n.getNextSibling())
				{
					if(!n.getNodeName().equalsIgnoreCase("char"))
						continue;

					PlayerAccess pa = new PlayerAccess();
					for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						Class<?> cls = pa.getClass();
						String node = d.getNodeName();

						if(node.equalsIgnoreCase("#text"))
							continue;
						try
						{
							fld = cls.getField(node);
						}
						catch(NoSuchFieldException e)
						{
							_log.info("Not found desclarate ACCESS name: " + node + " in XML Player access Object");
							continue;
						}

						if(fld.getType().getName().equalsIgnoreCase("boolean"))
							fld.setBoolean(pa, Boolean.parseBoolean(d.getAttributes().getNamedItem("set").getNodeValue()));
						else if(fld.getType().getName().equalsIgnoreCase("int"))
							fld.setInt(pa, Integer.valueOf(d.getAttributes().getNamedItem("set").getNodeValue()));
					}
					gmlist.put(pa.PlayerID, pa);
				}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public static String getField(String fieldName)
	{
		Field field = FieldUtils.getField(Config.class, fieldName);

		if(field == null)
			return null;

		try
		{
			return String.valueOf(field.get(null));
		}
		catch(IllegalArgumentException | IllegalAccessException ignored) {}

        return null;
	}

	public static boolean setField(String fieldName, String value)
	{
		Field field = FieldUtils.getField(Config.class, fieldName);

		if(field == null)
			return false;

		try
		{
			if(field.getType() == boolean.class)
				field.setBoolean(null, BooleanUtils.toBoolean(value));
			else if(field.getType() == int.class)
				field.setInt(null, NumberUtils.toInt(value));
			else if(field.getType() == long.class)
				field.setLong(null, NumberUtils.toLong(value));
			else if(field.getType() == double.class)
				field.setDouble(null, NumberUtils.toDouble(value));
			else if(field.getType() == String.class)
				field.set(null, value);
			else
				return false;
		}
		catch(IllegalArgumentException e)
		{
			return false;
		}
		catch(IllegalAccessException e)
		{
			return false;
		}

		return true;
	}

	public static ExProperties load(String filename)
	{
		_log.info("Loading File: " + filename);
		return load(new File(filename));
	}

	public static ExProperties load(File file)
	{
		ExProperties result = new ExProperties();

		try
		{
			result.load(file);
		}
		catch(IOException e)
		{
			
			_log.error("Error loading : " + file.getName() + "!");
		}

		return result;
	}

	public static boolean containsAbuseWord(String s)
	{
		for(Pattern pattern : ABUSEWORD_LIST)
			if(pattern.matcher(s).matches())
				return true;
		return false;
	}
}