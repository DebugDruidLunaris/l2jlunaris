package jts.gameserver.model;

import static jts.gameserver.network.serverpackets.ExSetCompassZoneCode.ZONE_ALTERED_FLAG;
import static jts.gameserver.network.serverpackets.ExSetCompassZoneCode.ZONE_PEACE_FLAG;
import static jts.gameserver.network.serverpackets.ExSetCompassZoneCode.ZONE_PVP_FLAG;
import static jts.gameserver.network.serverpackets.ExSetCompassZoneCode.ZONE_SIEGE_FLAG;
import static jts.gameserver.network.serverpackets.ExSetCompassZoneCode.ZONE_SSQ_FLAG;
import gnu.trove.map.hash.THashMap;

import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import jts.commons.collections.LazyArrayList;
import jts.commons.dao.JdbcEntityState;
import jts.commons.dbutils.DbUtils;
import jts.commons.lang.reference.HardReference;
import jts.commons.lang.reference.HardReferences;
import jts.commons.threading.RunnableImpl;
import jts.commons.util.Rnd;
import jts.gameserver.Config;
import jts.gameserver.GameTimeController;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.ai.CtrlEvent;
import jts.gameserver.ai.CtrlIntention;
import jts.gameserver.ai.PlayableAI.nextAction;
import jts.gameserver.ai.PlayerAI;
import jts.gameserver.cache.Msg;
import jts.gameserver.captcha.CaptchaValidator;
import jts.gameserver.common.DifferentMethods;
import jts.gameserver.common.Scheme;
import jts.gameserver.dao.AccountBonusDAO;
import jts.gameserver.dao.AccountReportDAO;
import jts.gameserver.dao.CharacterDAO;
import jts.gameserver.dao.CharacterGroupReuseDAO;
import jts.gameserver.dao.CharacterPostFriendDAO;
import jts.gameserver.dao.EffectsDAO;
import jts.gameserver.data.xml.holder.CharTemplateHolder;
import jts.gameserver.data.xml.holder.EventHolder;
import jts.gameserver.data.xml.holder.HennaHolder;
import jts.gameserver.data.xml.holder.InstantZoneHolder;
import jts.gameserver.data.xml.holder.ItemHolder;
import jts.gameserver.data.xml.holder.MultiSellHolder.MultiSellListContainer;
import jts.gameserver.data.xml.holder.NpcHolder;
import jts.gameserver.data.xml.holder.RecipeHolder;
import jts.gameserver.data.xml.holder.ResidenceHolder;
import jts.gameserver.data.xml.holder.SkillAcquireHolder;
import jts.gameserver.database.DatabaseFactory;
import jts.gameserver.database.mysql;
import jts.gameserver.geodata.GeoEngine;
import jts.gameserver.handler.bbs.CommunityBoardManager;
import jts.gameserver.handler.bbs.ICommunityBoardHandler;
import jts.gameserver.handler.items.IItemHandler;
import jts.gameserver.idfactory.IdFactory;
import jts.gameserver.instancemanager.AutoHuntingManager;
import jts.gameserver.instancemanager.BypassManager;
import jts.gameserver.instancemanager.BypassManager.BypassType;
import jts.gameserver.instancemanager.BypassManager.DecodedBypass;
import jts.gameserver.instancemanager.CursedWeaponsManager;
import jts.gameserver.instancemanager.DimensionalRiftManager;
import jts.gameserver.instancemanager.MatchingRoomManager;
import jts.gameserver.instancemanager.OfflineBufferManager;
import jts.gameserver.instancemanager.QuestManager;
import jts.gameserver.instancemanager.ReflectionManager;
import jts.gameserver.instancemanager.games.HandysBlockCheckerManager;
import jts.gameserver.instancemanager.games.HandysBlockCheckerManager.ArenaParticipantsHolder;
import jts.gameserver.listener.actor.player.OnAnswerListener;
import jts.gameserver.listener.actor.player.impl.ReviveAnswerListener;
import jts.gameserver.listener.actor.player.impl.ScriptAnswerListener;
import jts.gameserver.listener.actor.player.impl.SummonAnswerListener;
import jts.gameserver.loginservercon.LoginServerCommunication;
import jts.gameserver.loginservercon.gspackets.BonusRequest;
import jts.gameserver.model.GameObjectTasks.EndSitDownTask;
import jts.gameserver.model.GameObjectTasks.EndStandUpTask;
import jts.gameserver.model.GameObjectTasks.HourlyTask;
import jts.gameserver.model.GameObjectTasks.KickTask;
import jts.gameserver.model.GameObjectTasks.PvPFlagTask;
import jts.gameserver.model.GameObjectTasks.RecomBonusTask;
import jts.gameserver.model.GameObjectTasks.UnJailTask;
import jts.gameserver.model.GameObjectTasks.WaterTask;
import jts.gameserver.model.Request.L2RequestType;
import jts.gameserver.model.Zone.ZoneType;
import jts.gameserver.model.actor.instances.player.Bonus;
import jts.gameserver.model.actor.instances.player.BookMarkList;
import jts.gameserver.model.actor.instances.player.FriendList;
import jts.gameserver.model.actor.instances.player.Macro;
import jts.gameserver.model.actor.instances.player.MacroList;
import jts.gameserver.model.actor.instances.player.NevitSystem;
import jts.gameserver.model.actor.instances.player.RecomBonus;
import jts.gameserver.model.actor.instances.player.ShortCut;
import jts.gameserver.model.actor.instances.player.ShortCutList;
import jts.gameserver.model.actor.listener.PlayerListenerList;
import jts.gameserver.model.actor.recorder.PlayerStatsChangeRecorder;
import jts.gameserver.model.base.AcquireType;
import jts.gameserver.model.base.ClassId;
import jts.gameserver.model.base.Element;
import jts.gameserver.model.base.Experience;
import jts.gameserver.model.base.InvisibleType;
import jts.gameserver.model.base.PlayerAccess;
import jts.gameserver.model.base.Race;
import jts.gameserver.model.base.RestartType;
import jts.gameserver.model.base.TeamType;
import jts.gameserver.model.entity.DimensionalRift;
import jts.gameserver.model.entity.Hero;
import jts.gameserver.model.entity.Reflection;
import jts.gameserver.model.entity.SevenSignsFestival.DarknessFestival;
import jts.gameserver.model.entity.boat.Boat;
import jts.gameserver.model.entity.boat.ClanAirShip;
import jts.gameserver.model.entity.events.GlobalEvent;
import jts.gameserver.model.entity.events.impl.DominionSiegeEvent;
import jts.gameserver.model.entity.events.impl.DuelEvent;
import jts.gameserver.model.entity.events.impl.SiegeEvent;
import jts.gameserver.model.entity.olympiad.CompType;
import jts.gameserver.model.entity.olympiad.Olympiad;
import jts.gameserver.model.entity.olympiad.OlympiadGame;
import jts.gameserver.model.entity.residence.Castle;
import jts.gameserver.model.entity.residence.ClanHall;
import jts.gameserver.model.entity.residence.Fortress;
import jts.gameserver.model.entity.residence.Residence;
import jts.gameserver.model.instances.DecoyInstance;
import jts.gameserver.model.instances.FestivalMonsterInstance;
import jts.gameserver.model.instances.GuardInstance;
import jts.gameserver.model.instances.MonsterInstance;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.instances.PetBabyInstance;
import jts.gameserver.model.instances.PetInstance;
import jts.gameserver.model.instances.ReflectionBossInstance;
import jts.gameserver.model.instances.StaticObjectInstance;
import jts.gameserver.model.instances.TamedBeastInstance;
import jts.gameserver.model.instances.TrapInstance;
import jts.gameserver.model.items.Inventory;
import jts.gameserver.model.items.ItemContainer;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.model.items.LockType;
import jts.gameserver.model.items.ManufactureItem;
import jts.gameserver.model.items.PcFreight;
import jts.gameserver.model.items.PcInventory;
import jts.gameserver.model.items.PcRefund;
import jts.gameserver.model.items.PcWarehouse;
import jts.gameserver.model.items.TradeItem;
import jts.gameserver.model.items.Warehouse;
import jts.gameserver.model.items.Warehouse.WarehouseType;
import jts.gameserver.model.items.attachment.FlagItemAttachment;
import jts.gameserver.model.items.attachment.PickableAttachment;
import jts.gameserver.model.matching.MatchingRoom;
import jts.gameserver.model.petition.PetitionMainGroup;
import jts.gameserver.model.pledge.Alliance;
import jts.gameserver.model.pledge.Clan;
import jts.gameserver.model.pledge.Privilege;
import jts.gameserver.model.pledge.RankPrivs;
import jts.gameserver.model.pledge.SubUnit;
import jts.gameserver.model.pledge.UnitMember;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestEventType;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.network.GameClient;
import jts.gameserver.network.serverpackets.AbnormalStatusUpdate;
import jts.gameserver.network.serverpackets.ActionFail;
import jts.gameserver.network.serverpackets.AutoAttackStart;
import jts.gameserver.network.serverpackets.CameraMode;
import jts.gameserver.network.serverpackets.ChairSit;
import jts.gameserver.network.serverpackets.ChangeWaitType;
import jts.gameserver.network.serverpackets.CharInfo;
import jts.gameserver.network.serverpackets.ConfirmDlg;
import jts.gameserver.network.serverpackets.EtcStatusUpdate;
import jts.gameserver.network.serverpackets.ExAutoSoulShot;
import jts.gameserver.network.serverpackets.ExBR_AgathionEnergyInfo;
import jts.gameserver.network.serverpackets.ExBR_ExtraUserInfo;
import jts.gameserver.network.serverpackets.ExBasicActionList;
import jts.gameserver.network.serverpackets.ExDominionWarStart;
import jts.gameserver.network.serverpackets.ExDuelUpdateUserInfo;
import jts.gameserver.network.serverpackets.ExOlympiadMatchEnd;
import jts.gameserver.network.serverpackets.ExOlympiadMode;
import jts.gameserver.network.serverpackets.ExOlympiadSpelledInfo;
import jts.gameserver.network.serverpackets.ExPCCafePointInfo;
import jts.gameserver.network.serverpackets.ExQuestItemList;
import jts.gameserver.network.serverpackets.ExSetCompassZoneCode;
import jts.gameserver.network.serverpackets.ExShowScreenMessage;
import jts.gameserver.network.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import jts.gameserver.network.serverpackets.ExStartScenePlayer;
import jts.gameserver.network.serverpackets.ExStorageMaxCount;
import jts.gameserver.network.serverpackets.ExVitalityPointInfo;
import jts.gameserver.network.serverpackets.ExVoteSystemInfo;
import jts.gameserver.network.serverpackets.GetItem;
import jts.gameserver.network.serverpackets.HennaInfo;
import jts.gameserver.network.serverpackets.InventoryUpdate;
import jts.gameserver.network.serverpackets.ItemList;
import jts.gameserver.network.serverpackets.L2GameServerPacket;
import jts.gameserver.network.serverpackets.LeaveWorld;
import jts.gameserver.network.serverpackets.MagicSkillLaunched;
import jts.gameserver.network.serverpackets.MagicSkillUse;
import jts.gameserver.network.serverpackets.MyTargetSelected;
import jts.gameserver.network.serverpackets.NpcHtmlMessage;
import jts.gameserver.network.serverpackets.NpcInfoPoly;
import jts.gameserver.network.serverpackets.ObserverEnd;
import jts.gameserver.network.serverpackets.ObserverStart;
import jts.gameserver.network.serverpackets.PartySmallWindowUpdate;
import jts.gameserver.network.serverpackets.PartySpelled;
import jts.gameserver.network.serverpackets.PlaySound;
import jts.gameserver.network.serverpackets.PledgeShowMemberListDelete;
import jts.gameserver.network.serverpackets.PledgeShowMemberListDeleteAll;
import jts.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import jts.gameserver.network.serverpackets.PrivateStoreListBuy;
import jts.gameserver.network.serverpackets.PrivateStoreListSell;
import jts.gameserver.network.serverpackets.PrivateStoreMsgBuy;
import jts.gameserver.network.serverpackets.PrivateStoreMsgSell;
import jts.gameserver.network.serverpackets.QuestList;
import jts.gameserver.network.serverpackets.RadarControl;
import jts.gameserver.network.serverpackets.RecipeShopMsg;
import jts.gameserver.network.serverpackets.RecipeShopSellList;
import jts.gameserver.network.serverpackets.RelationChanged;
import jts.gameserver.network.serverpackets.Ride;
import jts.gameserver.network.serverpackets.SendTradeDone;
import jts.gameserver.network.serverpackets.ServerClose;
import jts.gameserver.network.serverpackets.SetupGauge;
import jts.gameserver.network.serverpackets.ShortBuffStatusUpdate;
import jts.gameserver.network.serverpackets.ShortCutInit;
import jts.gameserver.network.serverpackets.ShortCutRegister;
import jts.gameserver.network.serverpackets.SkillCoolTime;
import jts.gameserver.network.serverpackets.SkillList;
import jts.gameserver.network.serverpackets.Snoop;
import jts.gameserver.network.serverpackets.SocialAction;
import jts.gameserver.network.serverpackets.SpawnEmitter;
import jts.gameserver.network.serverpackets.SpecialCamera;
import jts.gameserver.network.serverpackets.StatusUpdate;
import jts.gameserver.network.serverpackets.SystemMessage;
import jts.gameserver.network.serverpackets.SystemMessage2;
import jts.gameserver.network.serverpackets.TargetSelected;
import jts.gameserver.network.serverpackets.TargetUnselected;
import jts.gameserver.network.serverpackets.TeleportToLocation;
import jts.gameserver.network.serverpackets.UserInfo;
import jts.gameserver.network.serverpackets.ValidateLocation;
import jts.gameserver.network.serverpackets.components.CustomMessage;
import jts.gameserver.network.serverpackets.components.IStaticPacket;
import jts.gameserver.network.serverpackets.components.SceneMovie;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.scripts.Events;
import jts.gameserver.skills.AddedSkill;
import jts.gameserver.skills.EffectType;
import jts.gameserver.skills.TimeStamp;
import jts.gameserver.skills.effects.EffectCubic;
import jts.gameserver.skills.effects.EffectTemplate;
import jts.gameserver.skills.skillclasses.Charge;
import jts.gameserver.skills.skillclasses.Transformation;
import jts.gameserver.stats.Formulas;
import jts.gameserver.stats.Stats;
import jts.gameserver.stats.funcs.FuncTemplate;
import jts.gameserver.tables.ClanTable;
import jts.gameserver.tables.PetDataTable;
import jts.gameserver.tables.SkillTable;
import jts.gameserver.tables.SkillTreeTable;
import jts.gameserver.taskmanager.AutoSaveManager;
import jts.gameserver.taskmanager.LazyPrecisionTaskManager;
import jts.gameserver.templates.FishTemplate;
import jts.gameserver.templates.Henna;
import jts.gameserver.templates.InstantZone;
import jts.gameserver.templates.PlayerTemplate;
import jts.gameserver.templates.item.ArmorTemplate;
import jts.gameserver.templates.item.ArmorTemplate.ArmorType;
import jts.gameserver.templates.item.ItemTemplate;
import jts.gameserver.templates.item.WeaponTemplate;
import jts.gameserver.templates.item.WeaponTemplate.WeaponType;
import jts.gameserver.templates.npc.NpcTemplate;
import jts.gameserver.utils.AntiFlood;
import jts.gameserver.utils.AutoHuntingPunish;
import jts.gameserver.utils.EffectsComparator;
import jts.gameserver.utils.FixEnchantOlympiad;
import jts.gameserver.utils.GArray;
import jts.gameserver.utils.GameStats;
import jts.gameserver.utils.HWID.HardwareID;
import jts.gameserver.utils.ItemFunctions;
import jts.gameserver.utils.Language;
import jts.gameserver.utils.Location;
import jts.gameserver.utils.Log;
import jts.gameserver.utils.Log_New;
import jts.gameserver.utils.SiegeUtils;
import jts.gameserver.utils.SqlBatch;
import jts.gameserver.utils.Strings;
import jts.gameserver.utils.TeleportUtils;
import jts.loginserver.database.L2DatabaseFactory;
import jts.gameserver.database.OfflineBuffersTable;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.napile.primitive.Containers;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import GameGuard.GameGuard;
import GameGuard.network.GuardManager;

@SuppressWarnings("serial")
public final class Player extends Playable implements PlayerGroup
{
	public static final int DEFAULT_TITLE_COLOR = 0xFFFF77;
	public static final int MAX_POST_FRIEND_SIZE = 100;
	public static final int MAX_FRIEND_SIZE = 128;
	public long _deathtime;
	private static final Logger _log = LoggerFactory.getLogger(Player.class);

	public static final String NO_TRADERS_VAR = "notraders";
	public static final String NO_ANIMATION_OF_CAST_VAR = "notShowBuffAnim";
	public static final String MY_BIRTHDAY_RECEIVE_YEAR = "MyBirthdayReceiveYear";
	private static final String NOT_CONNECTED = "<not connected>";

	public Map<Integer, SubClass> _classlist = new HashMap<Integer, SubClass>(4);
	private AutoHuntingPunish _AutoHuntingPunish = null;
	public AccountReportDAO _account = null;
	public final static int OBSERVER_NONE = 0;
	public final static int OBSERVER_STARTING = 1;
	public final static int OBSERVER_STARTED = 3;
	public final static int OBSERVER_LEAVING = 2;

	public static final int STORE_PRIVATE_NONE = 0;
	public static final int STORE_PRIVATE_SELL = 1;
	public static final int STORE_PRIVATE_BUY = 3;
	public static final int STORE_PRIVATE_MANUFACTURE = 5;
	public static final int STORE_OBSERVING_GAMES = 7;
	public static final int STORE_PRIVATE_SELL_PACKAGE = 8;
	public static final int STORE_PRIVATE_BUFF = 20;

	public static final int RANK_VAGABOND = 0;
	public static final int RANK_VASSAL = 1;
	public static final int RANK_HEIR = 2;
	public static final int RANK_KNIGHT = 3;
	public static final int RANK_WISEMAN = 4;
	public static final int RANK_BARON = 5;
	public static final int RANK_VISCOUNT = 6;
	public static final int RANK_COUNT = 7;
	public static final int RANK_MARQUIS = 8;
	public static final int RANK_DUKE = 9;
	public static final int RANK_GRAND_DUKE = 10;
	public static final int RANK_DISTINGUISHED_KING = 11;
	public static final int RANK_EMPEROR = 12; // unused

	public static final int LANG_ENG = 0;
	public static final int LANG_RUS = 1;
	public static final int LANG_UNK = -1;

	public static final int[] EXPERTISE_LEVELS = { 0, 20, 40, 52, 61, 76, 80, 84, Integer.MAX_VALUE };
	private final static int[] _classIdprefetch = {0,10,18,25,31,38,44,49,53,123};

	private GameClient _connection;
	private String _login;

	private static String _changePasswordResult = "<font color=\"33CC33\">Ваш пароль от аккаунта был успешно изменен!</font>";

	private int _karma, _pkKills, _pvpKills;
	private int _face, _hairStyle, _hairColor;
	private int _recomHave, _recomLeftToday, _fame;
	private int _recomLeft = 20;
	private int _recomBonusTime = 3600;
	private boolean _isHourglassEffected, _isRecomTimerActive;
	private boolean _isUndying = false;
	private int _deleteTimer;
	private boolean _hasFlagCTF = false;

	private long _createTime, _onlineTime, _onlineBeginTime, _leaveClanTime, _deleteClanTime, _NoChannel, _NoChannelBegin;
	private long _uptime;
	private String _captcha = "";
	private int _captchaCount = 0;

	private boolean _CaptchaChatBlocked;
	private long _lastCaptchaReguest = 0;
	private int _tryCaptchaCount;
	
	/**
	 * Time on login in game
	 */
	private long _lastAccess;

	/**
	 * The Color of players name / title (white is 0xFFFFFF)
	 */
	private int _nameColor, _titlecolor;

	private int _vitalityLevel = -1;
	private double _vitality = Config.VITALITY_LEVELS[4];
	private boolean _overloaded;

	boolean sittingTaskLaunched;

	/**
	 * Time counter when L2Player is sitting
	 */
	private int _waitTimeWhenSit;

	private boolean _autoLoot = Config.ALT_AUTO_LOOT, AutoLootHerbs = Config.ALT_AUTO_LOOT_HERBS;

	private final PcInventory _inventory = new PcInventory(this);
	private final Warehouse _warehouse = new PcWarehouse(this);
	private final ItemContainer _refund = new PcRefund(this);
	private final PcFreight _freight = new PcFreight(this);

	public final BookMarkList bookmarks = new BookMarkList(this, 0);

	public final AntiFlood antiFlood = new AntiFlood();

	/**
	 * The table containing all L2RecipeList of the L2Player
	 */
	private final Map<Integer, Recipe> _recipebook = new TreeMap<Integer, Recipe>();
	private final Map<Integer, Recipe> _commonrecipebook = new TreeMap<Integer, Recipe>();

	/**
	 * Premium Items
	 */
	private Map<Integer, PremiumItem> _premiumItems = new TreeMap<Integer, PremiumItem>();

	/**
	 * The table containing all Quests began by the L2Player
	 */
	private final Map<String, QuestState> _quests = new HashMap<String, QuestState>();

	/**
	 * The list containing all shortCuts of this L2Player
	 */
	private final ShortCutList _shortCuts = new ShortCutList(this);

	/**
	 * The list containing all macroses of this L2Player
	 */
	private final MacroList _macroses = new MacroList(this);

	/**
	 * The Private Store type of the L2Player (STORE_PRIVATE_NONE=0,
	 * STORE_PRIVATE_SELL=1, sellmanage=2, STORE_PRIVATE_BUY=3, buymanage=4,
	 * STORE_PRIVATE_MANUFACTURE=5)
	 */
	private int _privatestore;
	/**
	 * Ð”Ð°Ð½Ð½Ñ‹Ðµ Ð´Ð»Ñ� Ð¼Ð°Ð³Ð°Ð·Ð¸Ð½Ð° Ñ€ÐµÑ†ÐµÐ¿Ñ‚Ð¾Ð²
	 */
	private String _manufactureName;
	private List<ManufactureItem> _createList = Collections.emptyList();
	/**
	 * Ð”Ð°Ð½Ð½Ñ‹Ðµ Ð´Ð»Ñ� Ð¼Ð°Ð³Ð°Ð·Ð¸Ð½Ð° Ð¿Ñ€Ð¾Ð´Ð°Ð¶Ð¸
	 */
	private String _sellStoreName;
	private List<TradeItem> _sellList = Collections.emptyList();
	private List<TradeItem> _packageSellList = Collections.emptyList();
	/**
	 * Ð”Ð°Ð½Ð½Ñ‹Ðµ Ð´Ð»Ñ� Ð¼Ð°Ð³Ð°Ð·Ð¸Ð½Ð° Ð¿Ð¾ÐºÑƒÐ¿ÐºÐ¸
	 */
	private String _buyStoreName;
	private List<TradeItem> _buyList = Collections.emptyList();
	/**
	 * Ð”Ð°Ð½Ð½Ñ‹Ðµ Ð´Ð»Ñ� Ð¾Ð±Ð¼ÐµÐ½Ð°
	 */
	private List<TradeItem> _tradeList = Collections.emptyList();

	/**
	 * hennas
	 */
	private final Henna[] _henna = new Henna[3];
	private int _hennaSTR, _hennaINT, _hennaDEX, _hennaMEN, _hennaWIT, _hennaCON;

	private Party _party;
	private Location _lastPartyPosition;

	private Clan _clan;
	private int _pledgeClass = 0, _pledgeType = Clan.SUBUNIT_NONE, _powerGrade = 0, _lvlJoinedAcademy = 0, _apprentice = 0;

	/**
	 * GM Stuff
	 */
	private int _accessLevel;
	private PlayerAccess _playerAccess = new PlayerAccess();

	private boolean _messageRefusal = false, _tradeRefusal = false, _blockAll = false;

	/**
	 * The L2Summon of the L2Player
	 */
	private Summon _summon = null;
	private boolean _riding;
	private DecoyInstance _decoy = null;

	private Map<Integer, EffectCubic> _cubics = null;
	private int _agathionId = 0;

	private Request _request;

	private ItemInstance _arrowItem;

	/**
	 * The fists L2Weapon of the L2Player (used when no weapon is equipped)
	 */
	private WeaponTemplate _fistsWeaponItem;

	private Map<Integer, String> _chars = new HashMap<Integer, String>(8);

	/**
	 * The current higher Expertise of the L2Player (None=0, D=1, C=2, B=3, A=4,
	 * S=5, S80=6, S84=7)
	 */
	public int expertiseIndex = 0;

	private ItemInstance _enchantScroll = null;

	private WarehouseType _usingWHType;

	private boolean _isOnline = false;

	private AtomicBoolean _isLogout = new AtomicBoolean();

	/**
	 * The L2NpcInstance corresponding to the last Folk which one the player
	 * talked.
	 */
	private HardReference<NpcInstance> _lastNpc = HardReferences.emptyRef();
	/**
	 * Ñ‚ÑƒÑ‚ Ñ…Ñ€Ð°Ð½Ð¸Ð¼ Ð¼ÑƒÐ»ÑŒÑ‚Ð¸Ñ�ÐµÐ»Ð» Ñ� ÐºÐ¾Ñ‚Ð¾Ñ€Ñ‹Ð¼ Ñ€Ð°Ð±Ð¾Ñ‚Ð°ÐµÐ¼
	 */
	private MultiSellListContainer _multisell = null;

	private Set<Integer> _activeSoulShots = new CopyOnWriteArraySet<Integer>();

	private WorldRegion _observerRegion;
	private AtomicInteger _observerMode = new AtomicInteger(0);

	public int _telemode = 0;

	private int _handysBlockCheckerEventArena = -1;

	public boolean entering = true;

	/**
	 * Ð­Ñ‚Ð° Ñ‚Ð¾Ñ‡ÐºÐ° Ð¿Ñ€Ð¾Ð²ÐµÑ€Ñ�ÐµÑ‚Ñ�Ñ� Ð¿Ñ€Ð¸ Ð½ÐµÑˆÑ‚Ð°Ñ‚Ð½Ð¾Ð¼ Ð²Ñ‹Ñ…Ð¾Ð´Ðµ Ñ‡Ð°Ñ€Ð°, Ð¸ ÐµÑ�Ð»Ð¸ Ð½Ðµ Ñ€Ð°Ð²Ð½Ð° null Ñ‡Ð°Ñ€
	 * Ð²Ð¾Ð·Ð²Ñ€Ð°Ñ‰Ð°ÐµÑ‚Ñ�Ñ� Ð² Ð½ÐµÐµ Ð˜Ñ�Ð¿Ð¾Ð»ÑŒÐ·ÑƒÐµÑ‚Ñ�Ñ� Ð½Ð°Ð¿Ñ€Ð¸Ð¼ÐµÑ€ Ð´Ð»Ñ� Ð²Ð¾Ð·Ð²Ñ€Ð°Ñ‰ÐµÐ½Ð¸Ñ� Ð¿Ñ€Ð¸ Ð¿Ð°Ð´ÐµÐ½Ð¸Ð¸ Ñ�
	 * Ð²Ð¸Ð²ÐµÑ€Ð½Ñ‹ ÐŸÐ¾Ð»Ðµ heading Ð¸Ñ�Ð¿Ð¾Ð»ÑŒÐ·ÑƒÐµÑ‚Ñ�Ñ� Ð´Ð»Ñ� Ñ…Ñ€Ð°Ð½ÐµÐ½Ð¸Ñ� Ð´ÐµÐ½ÐµÐ³ Ð²Ð¾Ð·Ð²Ñ€Ð°Ñ‰Ð°ÐµÐ¼Ñ‹Ñ… Ð¿Ñ€Ð¸
	 * Ñ�Ð±Ð¾Ðµ
	 */
	public Location _stablePoint = null;

	/**
	 * new loto ticket *
	 */
	public int _loto[] = new int[5];
	/**
	 * new race ticket *
	 */
	public int _race[] = new int[2];

	private final Map<Integer, String> _blockList = new ConcurrentSkipListMap<Integer, String>(); // characters
	// blocked
	// with
	// '/block
	// <charname>'
	// cmd
	private final FriendList _friendList = new FriendList(this);

	private boolean _hero = false;

	/**
	 * True if the L2Player is in a boat
	 */
	private Boat _boat;
	private Location _inBoatPosition;

	protected int _baseClass = -1;
	protected SubClass _activeClass = null;

	private Bonus _bonus = new Bonus();
	private Future<?> _bonusExpiration;

	private boolean _isSitting;
	private StaticObjectInstance _sittingObject;

	private boolean _noble = false;

	private boolean _inOlympiadMode;
	private OlympiadGame _olympiadGame;
	private OlympiadGame _olympiadObserveGame;

	private int _olympiadSide = -1;

	/**
	 * ally with ketra or varka related wars
	 */
	private int _varka = 0;
	private int _ketra = 0;
	private int _ram = 0;

	private byte[] _keyBindings = ArrayUtils.EMPTY_BYTE_ARRAY;

	private int _cursedWeaponEquippedId = 0;

	private final Fishing _fishing = new Fishing(this);
	private boolean _isFishing;

	private Future<?> _taskWater;
	private Future<?> _autoSaveTask;
	private Future<?> _kickTask;

	private Future<?> _vitalityTask;
	private Future<?> _pcCafePointsTask;
	public Future<?> _unjailTask;

	private final Lock _storeLock = new ReentrantLock();

	private int _zoneMask;

	private boolean _offline = false;
	private boolean _awaying = false;
	private int _transformationId;
	private int _transformationTemplate;
	private String _transformationName;
	private long _lastNpcInteractionTime = 0;
	private int _pcBangPoints;

	Map<Integer, Skill> _transformationSkills = new HashMap<Integer, Skill>();

	private int _expandInventory = 0;
	private int _expandWarehouse = 0;
	private int _battlefieldChatId;
	private int _lectureMark;
	private InvisibleType _invisibleType = InvisibleType.NONE;

	private List<String> bypasses = null, bypasses_bbs = null;
	private IntObjectMap<String> _postFriends = Containers.emptyIntObjectMap();

	private List<String> _blockedActions = new ArrayList<String>();

	private boolean _notShowBuffAnim = false;
	private boolean _notShowTraders = false;
	private boolean _debug = false;

	private long _dropDisabled;
	private long _lastItemAuctionInfoRequest;
	private long _charCaptchaTime = System.currentTimeMillis();

	private IntObjectMap<TimeStamp> _sharedGroupReuses = new CHashIntObjectMap<TimeStamp>();
	private Pair<Integer, OnAnswerListener> _askDialog = null;

	// High Five: Navit's Bonus System
	private NevitSystem _nevitSystem = new NevitSystem(this);

	private MatchingRoom _matchingRoom;
	private PetitionMainGroup _petitionGroup;
	private final Map<Integer, Long> _instancesReuses = new ConcurrentHashMap<Integer, Long>();
	private THashMap<String, Scheme> schemes = new THashMap<String, Scheme>();

	public boolean _isOrdered;

	/**
	 * ÐšÐ¾Ð½Ñ�Ñ‚Ñ€ÑƒÐºÑ‚Ð¾Ñ€ Ð´Ð»Ñ� L2Player. Ð�Ð°Ð¿Ñ€Ñ�Ð¼ÑƒÑŽ Ð½Ðµ Ð²Ñ‹Ð·Ñ‹Ð²Ð°ÐµÑ‚Ñ�Ñ�, Ð´Ð»Ñ� Ñ�Ð¾Ð·Ð´Ð°Ð½Ð¸Ñ� Ð¸Ð³Ñ€Ð¾ÐºÐ°
	 * Ð¸Ñ�Ð¿Ð¾Ð»ÑŒÐ·ÑƒÐµÑ‚Ñ�Ñ� PlayerManager.create
	 */
	public Player(final int objectId, final PlayerTemplate template, final String accountName)
	{
		super(objectId, template);

		_login = accountName;
		_nameColor = 0xFFFFFF;
		_titlecolor = 0xFFFF77;
		_baseClass = getClassId().getId();
	}

	/**
	 * Constructor<?> of L2Player (use L2Character constructor).<BR>
	 * <BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Call the L2Character constructor to create an empty _skills slot and
	 * copy basic Calculator set to this L2Player</li>
	 * <li>Create a L2Radar object</li>
	 * <li>Retrieve from the database all items of this L2Player and add them to
	 * _inventory</li>
	 * <p/>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SET the
	 * account name of the L2Player</B></FONT><BR>
	 * <BR>
	 * 
	 * @param objectId
	 *            Identifier of the object to initialized
	 * @param template
	 *            The L2PlayerTemplate to apply to the L2Player
	 */
	private Player(final int objectId, final PlayerTemplate template)
	{
		this(objectId, template, null);

		_ai = new PlayerAI(this);

		if(!Config.EVERYBODY_HAS_ADMIN_RIGHTS)
		{
			setPlayerAccess(Config.gmlist.get(objectId));
		}
		else
		{
			setPlayerAccess(Config.gmlist.get(0));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public HardReference<Player> getRef()
	{
		return (HardReference<Player>) super.getRef();
	}

	public String getAccountName()
	{
		if(_connection == null)
			return _login;
		return _connection.getLogin();
	}

	public String getIP()
	{
		if(_connection == null)
			return NOT_CONNECTED;
		return _connection.getIpAddr();
	}
	public void storeHWID(final String HWID)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE characters SET LastHWID=? WHERE obj_id=?");
			statement.setString(1, HWID);
			statement.setInt(2, getObjectId());
			statement.execute();
		}
		catch(final Exception e)
		{
			_log.warn("could not store characters HWID: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	/**
	 * Ð’Ð¾Ð·Ð²Ñ€Ð°Ñ‰Ð°ÐµÑ‚ Ñ�Ð¿Ð¸Ñ�Ð¾Ðº Ð¿ÐµÑ€Ñ�Ð¾Ð½Ð°Ð¶ÐµÐ¹ Ð½Ð° Ð°ÐºÐºÐ°ÑƒÐ½Ñ‚Ðµ, Ð·Ð° Ð¸Ñ�ÐºÐ»ÑŽÑ‡ÐµÐ½Ð¸ÐµÐ¼ Ñ‚ÐµÐºÑƒÑ‰ÐµÐ³Ð¾
	 * 
	 * @return Ð¡Ð¿Ð¸Ñ�Ð¾Ðº Ð¿ÐµÑ€Ñ�Ð¾Ð½Ð°Ð¶ÐµÐ¹
	 */
	public Map<Integer, String> getAccountChars()
	{
		return _chars;
	}

	@Override
	public final PlayerTemplate getTemplate()
	{
		return (PlayerTemplate) _template;
	}

	@Override
	public PlayerTemplate getBaseTemplate()
	{
		return (PlayerTemplate) _baseTemplate;
	}

	public void changeSex()
	{
		boolean male = true;
		if(getSex() == 1)
			male = false;
		_template = CharTemplateHolder.getInstance().getTemplate(getClassId(), !male);
	}

	@Override
	public PlayerAI getAI()
	{
		return (PlayerAI) _ai;
	}

	@Override
	public void doCast(final Skill skill, final Creature target, boolean forceUse)
	{
		if(skill == null)
			return;

		super.doCast(skill, target, forceUse);

		// if(getUseSeed() != 0 && skill.getSkillType() == SkillType.SOWING)
		// sendPacket(new ExUseSharedGroupItem(getUseSeed(), getUseSeed(), 5000,
		// 5000));
	}

	@Override
	public void sendReuseMessage(Skill skill)
	{
		if(isCastingNow())
			return;
		TimeStamp sts = getSkillReuse(skill);
		if(sts == null || !sts.hasNotPassed())
			return;
		long timeleft = sts.getReuseCurrent();
		if(!Config.ALT_SHOW_REUSE_MSG && timeleft < 10000 || timeleft < 500)
			return;
		long hours = timeleft / 3600000;
		long minutes = (timeleft - hours * 3600000) / 60000;
		long seconds = (long) Math.ceil((timeleft - hours * 3600000 - minutes * 60000) / 1000.);
		if(hours > 0)
		{
			sendPacket(new SystemMessage(SystemMessage.THERE_ARE_S2_HOURS_S3_MINUTES_AND_S4_SECONDS_REMAINING_IN_S1S_REUSE_TIME).addSkillName(skill.getId(), skill.getDisplayLevel()).addNumber(hours).addNumber(minutes).addNumber(seconds));
		}
		else if(minutes > 0)
		{
			sendPacket(new SystemMessage(SystemMessage.THERE_ARE_S2_MINUTES_S3_SECONDS_REMAINING_IN_S1S_REUSE_TIME).addSkillName(skill.getId(), skill.getDisplayLevel()).addNumber(minutes).addNumber(seconds));
		}
		else
		{
			sendPacket(new SystemMessage(SystemMessage.THERE_ARE_S2_SECONDS_REMAINING_IN_S1S_REUSE_TIME).addSkillName(skill.getId(), skill.getDisplayLevel()).addNumber(seconds));
		}
	}

	@Override
	public final int getLevel()
	{
		return _activeClass == null ? 1 : _activeClass.getLevel();
	}

	public int getSex()
	{
		return getTemplate().isMale ? 0 : 1;
	}

	public int getFace()
	{
		return _face;
	}

	public void setFace(int face)
	{
		_face = face;
	}

	public int getHairColor()
	{
		return _hairColor;
	}

	public void setHairColor(int hairColor)
	{
		_hairColor = hairColor;
	}

	public int getHairStyle()
	{
		return _hairStyle;
	}

	public void setHairStyle(int hairStyle)
	{
		_hairStyle = hairStyle;
	}

	public void offline()
	{
		if(_connection != null)
		{
			_connection.setActiveChar(null);
			_connection.close(ServerClose.STATIC);
			setNetConnection(null);
		}

		setNameColor(Config.SERVICES_OFFLINE_TRADE_NAME_COLOR);
        startAbnormalEffect(Config.SERVICES_OFFLINE_ABNORMAL_EFFECT);
		setOfflineMode(true);

		setVar("offline", String.valueOf(System.currentTimeMillis() / 1000L), -1);

		if(Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK > 0)
		{
			startKickTask(Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK * 1000L);
		}

		Party party = getParty();
		if(party != null)
		{
			if(isFestivalParticipant())
			{
				party.broadcastMessageToPartyMembers(getName() + " has been removed from the upcoming festival.");
			}
			leaveParty();
		}

		if(getPet() != null)
		{
			getPet().unSummon();
		}

		CursedWeaponsManager.getInstance().doLogout(this);

		if(isInOlympiadMode())
		{
			Olympiad.logoutPlayer(this);
		}

		broadcastCharInfo();
		OfflineBuffersTable.getInstance().onLogout(this);
		stopWaterTask();
		stopBonusTask();
		stopHourlyTask();
		stopVitalityTask();
		stopPcBangPointsTask();
		stopAutoSaveTask();
		stopRecomBonusTask(true);
		stopQuestTimers();
		getNevitSystem().stopTasksOnLogout();

		try
		{
			getInventory().store();
		}
		catch(Throwable t)
		{
			_log.error("", t);
		}

		try
		{
			store(false);
		}
		catch(Throwable t)
		{
			_log.error("", t);
		}
	}

	/**
	 * Ð¡Ð¾ÐµÐ´Ð¸Ð½ÐµÐ½Ð¸Ðµ Ð·Ð°ÐºÑ€Ñ‹Ð²Ð°ÐµÑ‚Ñ�Ñ�, ÐºÐ»Ð¸ÐµÐ½Ñ‚ Ð·Ð°ÐºÑ€Ñ‹Ð²Ð°ÐµÑ‚Ñ�Ñ�, Ð¿ÐµÑ€Ñ�Ð¾Ð½Ð°Ð¶ Ñ�Ð¾Ñ…Ñ€Ð°Ð½Ñ�ÐµÑ‚Ñ�Ñ� Ð¸
	 * ÑƒÐ´Ð°Ð»Ñ�ÐµÑ‚Ñ�Ñ� Ð¸Ð· Ð¸Ð³Ñ€Ñ‹
	 */
	public void kick()
	{
		try
		{
			if(GameGuard.isProtectionOn())
			{
				GuardManager.scheduleSendPacketToClient(0, this);
			}
			if(_connection != null)
			{
				_connection.close(LeaveWorld.STATIC);
				setNetConnection(null);
			}
			prepareToLogout();
			deleteMe();
		}
		catch(Exception e)
		{}
	}	

	/**
	 * Ð¡Ð¾ÐµÐ´Ð¸Ð½ÐµÐ½Ð¸Ðµ Ð½Ðµ Ð·Ð°ÐºÑ€Ñ‹Ð²Ð°ÐµÑ‚Ñ�Ñ�, ÐºÐ»Ð¸ÐµÐ½Ñ‚ Ð½Ðµ Ð·Ð°ÐºÑ€Ñ‹Ð²Ð°ÐµÑ‚Ñ�Ñ�, Ð¿ÐµÑ€Ñ�Ð¾Ð½Ð°Ð¶ Ñ�Ð¾Ñ…Ñ€Ð°Ð½Ñ�ÐµÑ‚Ñ�Ñ� Ð¸
	 * ÑƒÐ´Ð°Ð»Ñ�ÐµÑ‚Ñ�Ñ� Ð¸Ð· Ð¸Ð³Ñ€Ñ‹
	 */
	public void restart()
	{
		try
		{
			if(GameGuard.isProtectionOn())
			{
				GuardManager.scheduleSendPacketToClient(0, this);
			}
		if(_connection != null)
		{
			_connection.setActiveChar(null);
			setNetConnection(null);
		}
		prepareToLogout();
		deleteMe();
	}
		catch(Exception e)
		{}
	}
	/**
	 * Ð¡Ð¾ÐµÐ´Ð¸Ð½ÐµÐ½Ð¸Ðµ Ð·Ð°ÐºÑ€Ñ‹Ð²Ð°ÐµÑ‚Ñ�Ñ�, ÐºÐ»Ð¸ÐµÐ½Ñ‚ Ð½Ðµ Ð·Ð°ÐºÑ€Ñ‹Ð²Ð°ÐµÑ‚Ñ�Ñ�, Ð¿ÐµÑ€Ñ�Ð¾Ð½Ð°Ð¶ Ñ�Ð¾Ñ…Ñ€Ð°Ð½Ñ�ÐµÑ‚Ñ�Ñ� Ð¸
	 * ÑƒÐ´Ð°Ð»Ñ�ÐµÑ‚Ñ�Ñ� Ð¸Ð· Ð¸Ð³Ñ€Ñ‹
	 */
	public void logout()
	{
		try
		{
			if(GameGuard.isProtectionOn())
			{
				GuardManager.scheduleSendPacketToClient(0, this);
			}
		if(_connection != null)
		{
			_connection.close(ServerClose.STATIC);
			setNetConnection(null);
		}
		prepareToLogout();
		deleteMe();
		}
		catch(Exception e)
		{}
	}

	private void prepareToLogout()
	{
		if(_isLogout.getAndSet(true))
			return;

		setNetConnection(null);
		setIsOnline(false);

		getListeners().onExit();

		if(isFlying() && !checkLandingState())
		{
			_stablePoint = TeleportUtils.getRestartLocation(this, RestartType.TO_VILLAGE);
		}

		if(isCastingNow())
		{
			abortCast(true, true);
		}

		Party party = getParty();
		if(party != null)
		{
			if(isFestivalParticipant())
			{
				party.broadcastMessageToPartyMembers(getName() + " has been removed from the upcoming festival."); // TODO
			}
			// [G1ta0]
			// custom
			// message
			leaveParty();
		}

		CursedWeaponsManager.getInstance().doLogout(this);

		if(isInObserverMode())
		{
			if(getOlympiadObserveGame() != null)
			{
				leaveObserverMode();
			}
			else
			{
				leaveOlympiadObserverMode(true);
			}
			_observerMode.set(OBSERVER_NONE);
		}

		if(isInOlympiadMode() || getOlympiadGame() != null)
		{
			Olympiad.logoutPlayer(this);
		}

		stopFishing();

		if(_stablePoint != null)
		{
			teleToLocation(_stablePoint);
		}

		Summon pet = getPet();
		if(pet != null)
		{
			pet.saveEffects();
			pet.unSummon();
			EffectsDAO.getInstance().insert(this);
		}

		_friendList.notifyFriends(false);

		if(isProcessingRequest())
		{
			getRequest().cancel();
		}

		stopAllTimers();

		if(isInBoat())
		{
			getBoat().removePlayer(this);
		}

		SubUnit unit = getSubUnit();
		UnitMember member = unit == null ? null : unit.getUnitMember(getObjectId());
		if(member != null)
		{
			int sponsor = member.getSponsor();
			int apprentice = getApprentice();
			PledgeShowMemberListUpdate memberUpdate = new PledgeShowMemberListUpdate(this);
			for(Player clanMember : _clan.getOnlineMembers(getObjectId()))
			{
				clanMember.sendPacket(memberUpdate);
				if(clanMember.getObjectId() == sponsor)
				{
					clanMember.sendPacket(new SystemMessage(SystemMessage.S1_YOUR_CLAN_ACADEMYS_APPRENTICE_HAS_LOGGED_OUT).addString(_name));
				}
				else if(clanMember.getObjectId() == apprentice)
				{
					clanMember.sendPacket(new SystemMessage(SystemMessage.S1_YOUR_CLAN_ACADEMYS_SPONSOR_HAS_LOGGED_OUT).addString(_name));
				}
			}
			member.setPlayerInstance(this, true);
		}

		FlagItemAttachment attachment = getActiveWeaponFlagAttachment();
		if(attachment != null)
		{
			attachment.onLogout(this);
		}

		if(CursedWeaponsManager.getInstance().getCursedWeapon(getCursedWeaponEquippedId()) != null)
		{
			CursedWeaponsManager.getInstance().getCursedWeapon(getCursedWeaponEquippedId()).setPlayer(null);
		}

		MatchingRoom room = getMatchingRoom();
		if(room != null)
			if(room.getLeader() == this)
			{
				room.disband();
			}
			else
			{
				room.removeMember(this, false);
			}
		setMatchingRoom(null);

		MatchingRoomManager.getInstance().removeFromWaitingList(this);

		destroyAllTraps();

		if(_decoy != null)
		{
			_decoy.unSummon();
			_decoy = null;
		}

		stopPvPFlag();

		Reflection ref = getReflection();

		if(ref != ReflectionManager.DEFAULT)
		{
			if(ref.getReturnLoc() != null)
			{
				_stablePoint = ref.getReturnLoc();
			}

			ref.removeObject(this);
		}

		try
		{
			getInventory().store();
			getRefund().clear();
		}
		catch(Throwable t)
		{
			_log.error("", t);
		}

		try
		{
			store(false);
		}
		catch(Throwable t)
		{
			_log.error("", t);
		}
		// Bot punishment
		if (Config.ENABLE_AUTO_HUNTING_REPORT)
		{
			// Save punish
			if (isBeingPunished())
			{
				try
				{
					AutoHuntingManager.getInstance().savePlayerPunish(this);
				}
				catch (Exception e)
				{
					_log.warn("deleteMe()", e);
				}
			}
			// Save report points left
			if (_account != null)
			{
				try
				{
					_account.updatePoints(this._login);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		
		try
		{
			getInventory().store();
			getRefund().clear();
		}
		catch (Throwable t)
		{
			_log.error("", t);
		}
		
		try
		{
			store(false);
		}
		catch (Throwable t)
		{
			_log.error("", t);
		}
	}
	public boolean isBeingPunished()
	{
		return _AutoHuntingPunish != null;
	}
	public AutoHuntingPunish getPlayerPunish()
	{
		return _AutoHuntingPunish;
	}
	public AutoHuntingPunish.Punish getBotPunishType()
	{
		return _AutoHuntingPunish.getBotPunishType();
	}
	
	public boolean hasEarnedExp()
	{
		if ((getExp() - _firstExp) != 0)
		{
			return true;
		}
		return false;
	}
	
	public long _firstExp;
	public synchronized void setPunishDueBotting(AutoHuntingPunish.Punish punishType, int minsOfPunish)
	{
		if (_AutoHuntingPunish == null)
		{
			_AutoHuntingPunish = new AutoHuntingPunish(punishType, minsOfPunish);
		}
	}
	
	public void setFirstExp(long value)
	{
		_firstExp = value;
	}
	public void endPunishment()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM bot_reported_punish WHERE charId = ?");
			statement.setInt(1, getObjectId());
			statement.execute();
			statement.close();
		}
		catch (SQLException sqle)
		{
			sqle.printStackTrace();
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (SQLException sqle)
			{
			}
		}
		_AutoHuntingPunish = null;
		this.sendMessage("Your punishment has expired. Do not bot again!");
	}
	
	/**
	 * @return a table containing all L2RecipeList of the L2Player.<BR>
	 * <BR>
	 */
	public Collection<Recipe> getDwarvenRecipeBook()
	{
		return _recipebook.values();
	}

	public Collection<Recipe> getCommonRecipeBook()
	{
		return _commonrecipebook.values();
	}

	public int recipesCount()
	{
		return _commonrecipebook.size() + _recipebook.size();
	}

	public boolean hasRecipe(final Recipe id)
	{
		return _recipebook.containsValue(id) || _commonrecipebook.containsValue(id);
	}

	public boolean findRecipe(final int id)
	{
		return _recipebook.containsKey(id) || _commonrecipebook.containsKey(id);
	}

	/**
	 * Add a new L2RecipList to the table _recipebook containing all
	 * L2RecipeList of the L2Player
	 */
	public void registerRecipe(final Recipe recipe, boolean saveDB)
	{
		if(recipe == null)
			return;
		if(recipe.isDwarvenRecipe())
		{
			_recipebook.put(recipe.getId(), recipe);
		}
		else
		{
			_commonrecipebook.put(recipe.getId(), recipe);
		}
		if(saveDB)
		{
			mysql.set("REPLACE INTO character_recipebook (char_id, id) VALUES(?,?)", getObjectId(), recipe.getId());
		}
	}

	/**
	 * Remove a L2RecipList from the table _recipebook containing all
	 * L2RecipeList of the L2Player
	 */
	public void unregisterRecipe(final int RecipeID)
	{
		if(_recipebook.containsKey(RecipeID))
		{
			mysql.set("DELETE FROM `character_recipebook` WHERE `char_id`=? AND `id`=? LIMIT 1", getObjectId(), RecipeID);
			_recipebook.remove(RecipeID);
		}
		else if(_commonrecipebook.containsKey(RecipeID))
		{
			mysql.set("DELETE FROM `character_recipebook` WHERE `char_id`=? AND `id`=? LIMIT 1", getObjectId(), RecipeID);
			_commonrecipebook.remove(RecipeID);
		}
		else
		{
			_log.warn("Attempted to remove unknown RecipeList" + RecipeID);
		}
	}

	// ------------------- Quest Engine ----------------------

	public QuestState getQuestState(String quest)
	{
		questRead.lock();
		try
		{
			return _quests.get(quest);
		}
		finally
		{
			questRead.unlock();
		}
	}

	public QuestState getQuestState(Class<?> quest)
	{
		return getQuestState(quest.getSimpleName());
	}

	public boolean isQuestCompleted(String quest)
	{
		QuestState q = getQuestState(quest);
		return q != null && q.isCompleted();
	}

	public boolean isQuestCompleted(Class<?> quest)
	{
		QuestState q = getQuestState(quest);
		return q != null && q.isCompleted();
	}

	public void setQuestState(QuestState qs)
	{
		questWrite.lock();
		try
		{
			_quests.put(qs.getQuest().getName(), qs);
		}
		finally
		{
			questWrite.unlock();
		}
	}

	public void removeQuestState(String quest)
	{
		questWrite.lock();
		try
		{
			_quests.remove(quest);
		}
		finally
		{
			questWrite.unlock();
		}
	}

	public Quest[] getAllActiveQuests()
	{
		List<Quest> quests = new ArrayList<Quest>(_quests.size());
		questRead.lock();
		try
		{
			for(final QuestState qs : _quests.values())
				if(qs.isStarted())
				{
					quests.add(qs.getQuest());
				}
		}
		finally
		{
			questRead.unlock();
		}
		return quests.toArray(new Quest[quests.size()]);
	}

	public QuestState[] getAllQuestsStates()
	{
		questRead.lock();
		try
		{
			return _quests.values().toArray(new QuestState[_quests.size()]);
		}
		finally
		{
			questRead.unlock();
		}
	}

	public List<QuestState> getQuestsForEvent(NpcInstance npc, QuestEventType event)
	{
		List<QuestState> states = new ArrayList<QuestState>();
		Quest[] quests = npc.getTemplate().getEventQuests(event);
		QuestState qs;
		if(quests != null)
		{
			for(Quest quest : quests)
			{
				qs = getQuestState(quest.getName());
				if(qs != null && !qs.isCompleted())
				{
					states.add(getQuestState(quest.getName()));
				}
			}
		}
		return states;
	}

	public void processQuestEvent(String quest, String event, NpcInstance npc)
	{
		if(event == null)
		{
			event = "";
		}
		QuestState qs = getQuestState(quest);
		if(qs == null)
		{
			Quest q = QuestManager.getQuest(quest);
			if(q == null)
			{
				_log.warn("Quest " + quest + " not found!");
				return;
			}
			qs = q.newQuestState(this, Quest.CREATED);
		}
		if(qs == null || qs.isCompleted())
			return;
		qs.getQuest().notifyEvent(event, qs, npc);
		sendPacket(new QuestList(this));
	}

	/**
	 * ÐŸÑ€Ð¾Ð²ÐµÑ€ÐºÐ° Ð½Ð° Ð¿ÐµÑ€ÐµÐ¿Ð¾Ð»Ð½ÐµÐ½Ð¸Ðµ Ð¸Ð½Ð²ÐµÐ½Ñ‚Ð°Ñ€Ñ� Ð¸ Ð¿ÐµÑ€ÐµÐ±Ð¾Ñ€ Ð² Ð²ÐµÑ�Ðµ Ð´Ð»Ñ� ÐºÐ²ÐµÑ�Ñ‚Ð¾Ð² Ð¸ Ñ�Ð²ÐµÐ½Ñ‚Ð¾Ð²
	 * 
	 * @return true ÐµÑ�Ð»Ð¸ Ð²Ðµ Ð¿Ñ€Ð¾Ð²ÐµÑ€ÐºÐ¸ Ð¿Ñ€Ð¾ÑˆÐ»Ð¸ ÑƒÑ�Ð¿ÐµÑˆÐ½Ð¾
	 */
	public boolean isQuestContinuationPossible(boolean msg)
	{
		if(getWeightPenalty() >= 3 || getInventoryLimit() * 0.9 < getInventory().getSize() || Config.QUEST_INVENTORY_MAXIMUM * 0.9 < getInventory().getQuestSize())
		{
			if(msg)
			{
				sendPacket(Msg.PROGRESS_IN_A_QUEST_IS_POSSIBLE_ONLY_WHEN_YOUR_INVENTORYS_WEIGHT_AND_VOLUME_ARE_LESS_THAN_80_PERCENT_OF_CAPACITY);
			}
			return false;
		}
		return true;
	}

	/**
	 * ÐžÑ�Ñ‚Ð°Ð½Ð°Ð²Ð»Ð¸Ð²Ð°ÐµÐ¼ Ð¸ Ð·Ð°Ð¿Ð¾Ð¼Ð¸Ð½Ð°ÐµÐ¼ Ð²Ñ�Ðµ ÐºÐ²ÐµÑ�Ñ‚Ð¾Ð²Ñ‹Ðµ Ñ‚Ð°Ð¹Ð¼ÐµÑ€Ñ‹
	 */
	public void stopQuestTimers()
	{
		for(QuestState qs : getAllQuestsStates())
			if(qs.isStarted())
			{
				qs.pauseQuestTimers();
			}
			else
			{
				qs.stopQuestTimers();
			}
	}

	/**
	 * Ð’Ð¾Ñ�Ñ�Ñ‚Ð°Ð½Ð°Ð²Ð»Ð¸Ð²Ð°ÐµÐ¼ Ð²Ñ�Ðµ ÐºÐ²ÐµÑ�Ñ‚Ð¾Ð²Ñ‹Ðµ Ñ‚Ð°Ð¹Ð¼ÐµÑ€Ñ‹
	 */
	public void resumeQuestTimers()
	{
		for(QuestState qs : getAllQuestsStates())
		{
			qs.resumeQuestTimers();
		}
	}

	// ----------------- End of Quest Engine -------------------

	public Collection<ShortCut> getAllShortCuts()
	{
		return _shortCuts.getAllShortCuts();
	}

	public ShortCut getShortCut(int slot, int page)
	{
		return _shortCuts.getShortCut(slot, page);
	}

	public void registerShortCut(ShortCut shortcut)
	{
		_shortCuts.registerShortCut(shortcut);
	}

	public void deleteShortCut(int slot, int page)
	{
		_shortCuts.deleteShortCut(slot, page);
	}

	public void registerMacro(Macro macro)
	{
		_macroses.registerMacro(macro);
	}

	public void deleteMacro(int id)
	{
		_macroses.deleteMacro(id);
	}

	public MacroList getMacroses()
	{
		return _macroses;
	}

	public boolean isCastleLord(int castleId)
	{
		return _clan != null && isClanLeader() && _clan.getCastle() == castleId;
	}

	/**
	 * ÐŸÑ€Ð¾Ð²ÐµÑ€Ñ�ÐµÑ‚ Ñ�Ð²Ð»Ñ�ÐµÑ‚Ñ�Ñ� Ð»Ð¸ Ñ�Ñ‚Ð¾Ñ‚ Ð¿ÐµÑ€Ñ�Ð¾Ð½Ð°Ð¶ Ð²Ð»Ð°Ð´ÐµÐ»ÑŒÑ†ÐµÐ¼ ÐºÑ€ÐµÐ¿Ð¾Ñ�Ñ‚Ð¸
	 * 
	 * @param fortressId
	 * @return true ÐµÑ�Ð»Ð¸ Ð²Ð»Ð°Ð´ÐµÐ»ÐµÑ†
	 */
	public boolean isFortressLord(int fortressId)
	{
		return _clan != null && isClanLeader() && _clan.getHasFortress() == fortressId;
	}

	public int getPkKills()
	{
		return _pkKills;
	}

	public void setPkKills(final int pkKills)
	{
		_pkKills = pkKills;
	}

	public long getCreateTime()
	{
		return _createTime;
	}

	public void setCreateTime(final long createTime)
	{
		_createTime = createTime;
	}

	public int getDeleteTimer()
	{
		return _deleteTimer;
	}

	public void setDeleteTimer(final int deleteTimer)
	{
		_deleteTimer = deleteTimer;
	}

	public int getCurrentLoad()
	{
		return getInventory().getTotalWeight();
	}

	public long getLastAccess()
	{
		return _lastAccess;
	}

	public void setLastAccess(long value)
	{
		_lastAccess = value;
	}

	public int getRecomHave()
	{
		return _recomHave;
	}

	public void setRecomHave(int value)
	{
		if(value > 255)
		{
			_recomHave = 255;
		}
		else if(value < 0)
		{
			_recomHave = 0;
		}
		else
		{
			_recomHave = value;
		}
	}

	public int getRecomBonusTime()
	{
		if(_recomBonusTask != null)
			return (int) Math.max(0, _recomBonusTask.getDelay(TimeUnit.SECONDS));
		return _recomBonusTime;
	}

	public void setRecomBonusTime(int val)
	{
		_recomBonusTime = val;
	}

	public int getRecomLeft()
	{
		return _recomLeft;
	}

	public void setRecomLeft(final int value)
	{
		_recomLeft = value;
	}

	public boolean isHourglassEffected()
	{
		return _isHourglassEffected;
	}

	public void setHourlassEffected(boolean val)
	{
		_isHourglassEffected = val;
	}

	public void startHourglassEffect()
	{
		setHourlassEffected(true);
		stopRecomBonusTask(true);
		sendVoteSystemInfo();
	}

	public void stopHourglassEffect()
	{
		setHourlassEffected(false);
		startRecomBonusTask();
		sendVoteSystemInfo();
	}

	public int addRecomLeft()
	{
		int recoms = 0;
		if(getRecomLeftToday() < 20)
		{
			recoms = 10;
		}
		else
		{
			recoms = 1;
		}
		setRecomLeft(getRecomLeft() + recoms);
		setRecomLeftToday(getRecomLeftToday() + recoms);
		sendUserInfo(true);
		return recoms;
	}

	public int getRecomLeftToday()
	{
		return _recomLeftToday;
	}

	public void setRecomLeftToday(final int value)
	{
		_recomLeftToday = value;
		setVar("recLeftToday", String.valueOf(_recomLeftToday), -1);
	}

	public void giveRecom(final Player target)
	{
		int targetRecom = target.getRecomHave();
		if(targetRecom < 255)
		{
			target.addRecomHave(1);
		}
		if(getRecomLeft() > 0)
		{
			setRecomLeft(getRecomLeft() - 1);
		}

		sendUserInfo(true);
	}

	public void addRecomHave(final int val)
	{
		setRecomHave(getRecomHave() + val);
		broadcastUserInfo(true);
		sendVoteSystemInfo();
	}

	public int getRecomBonus()
	{
		if(getRecomBonusTime() > 0 || isHourglassEffected())
			return RecomBonus.getRecoBonus(this);
		return 0;
	}

	public double getRecomBonusMul()
	{
		if(getRecomBonusTime() > 0 || isHourglassEffected())
			return RecomBonus.getRecoMultiplier(this);
		return 1;
	}

	public void sendVoteSystemInfo()
	{
		sendPacket(new ExVoteSystemInfo(this));
	}

	public boolean isRecomTimerActive()
	{
		return _isRecomTimerActive;
	}

	public void setRecomTimerActive(boolean val)
	{
		if(_isRecomTimerActive == val)
			return;

		_isRecomTimerActive = val;

		if(val)
		{
			startRecomBonusTask();
		}
		else
		{
			stopRecomBonusTask(true);
		}

		sendVoteSystemInfo();
	}

	private ScheduledFuture<?> _recomBonusTask;

	public void startRecomBonusTask()
	{
		if(_recomBonusTask == null && getRecomBonusTime() > 0 && isRecomTimerActive() && !isHourglassEffected())
		{
			_recomBonusTask = ThreadPoolManager.getInstance().schedule(new RecomBonusTask(this), getRecomBonusTime() * 1000);
		}
	}

	public void stopRecomBonusTask(boolean saveTime)
	{
		if(_recomBonusTask != null)
		{
			if(saveTime)
			{
				setRecomBonusTime((int) Math.max(0, _recomBonusTask.getDelay(TimeUnit.SECONDS)));
			}
			_recomBonusTask.cancel(false);
			_recomBonusTask = null;
		}
	}

	@Override
	public int getKarma()
	{
		return _karma;
	}

	public void setKarma(int karma)
	{
		if(karma < 0)
		{
			karma = 0;
		}

		if(_karma == karma)
			return;

		_karma = karma;

		sendChanges();

		if(getPet() != null)
		{
			getPet().broadcastCharInfo();
		}
	}

	@Override
	public int getMaxLoad()
	{
		// Weight Limit = (CON Modifier*69000)*Skills
		// Source http://jts.bravehost.com/weightlimit.html (May 2007)
		// Fitted exponential curve to the data
		int con = getCON();
		if(con < 1)
			return (int) (31000 * Config.ALT_MAXLOAD_MODIFIER);
		else if(con > 59)
			return (int) (176000 * Config.ALT_MAXLOAD_MODIFIER);
		else
			return (int) calcStat(Stats.MAX_LOAD, Math.pow(1.029993928, con) * 30495.627366 * Config.ALT_MAXLOAD_MODIFIER, this, null);
	}

	private Future<?> _updateEffectIconsTask;

	private class UpdateEffectIcons extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			updateEffectIconsImpl();
			_updateEffectIconsTask = null;
		}
	}

	@Override
	public void updateEffectIcons()
	{
		if(entering || isLogoutStarted())
			return;

		if(Config.USER_INFO_INTERVAL == 0)
		{
			if(_updateEffectIconsTask != null)
			{
				_updateEffectIconsTask.cancel(false);
				_updateEffectIconsTask = null;
			}
			updateEffectIconsImpl();
			return;
		}

		if(_updateEffectIconsTask != null)
			return;

		_updateEffectIconsTask = ThreadPoolManager.getInstance().schedule(new UpdateEffectIcons(), Config.USER_INFO_INTERVAL);
	}

	private void updateEffectIconsImpl()
	{
		Effect[] effects = getEffectList().getAllFirstEffects();
		Arrays.sort(effects, EffectsComparator.getInstance());

		PartySpelled ps = new PartySpelled(this, false);
		AbnormalStatusUpdate mi = new AbnormalStatusUpdate();

		for(Effect effect : effects)
			if(effect.isInUse())
			{
				if(effect.getStackType().equals(EffectTemplate.HP_RECOVER_CAST))
				{
					sendPacket(new ShortBuffStatusUpdate(effect));
				}
				else
				{
					effect.addIcon(mi);
				}
				if(_party != null)
				{
					effect.addPartySpelledIcon(ps);
				}
			}

		sendPacket(mi);
		if(_party != null)
		{
			_party.broadCast(ps);
		}

		if(isInOlympiadMode() && isOlympiadCompStart())
		{
			OlympiadGame olymp_game = _olympiadGame;
			if(olymp_game != null)
			{
				ExOlympiadSpelledInfo olympiadSpelledInfo = new ExOlympiadSpelledInfo();

				for(Effect effect : effects)
					if(effect != null && effect.isInUse())
					{
						effect.addOlympiadSpelledIcon(this, olympiadSpelledInfo);
					}

				if(olymp_game.getType() == CompType.CLASSED || olymp_game.getType() == CompType.NON_CLASSED)
				{
					for(Player member : olymp_game.getTeamMembers(this))
					{
						member.sendPacket(olympiadSpelledInfo);
					}
				}

				for(Player member : olymp_game.getSpectators())
				{
					member.sendPacket(olympiadSpelledInfo);
				}
			}
		}
	}

	public int getWeightPenalty()
	{
		return getSkillLevel(4270, 0);
	}

	public void refreshOverloaded()
	{
		if(isLogoutStarted() || getMaxLoad() <= 0)
			return;

		setOverloaded(getCurrentLoad() > getMaxLoad());
		double weightproc = 100. * (getCurrentLoad() - calcStat(Stats.MAX_NO_PENALTY_LOAD, 0, this, null)) / getMaxLoad();
		int newWeightPenalty = 0;

		if(weightproc < 50)
		{
			newWeightPenalty = 0;
		}
		else if(weightproc < 66.6)
		{
			newWeightPenalty = 1;
		}
		else if(weightproc < 80)
		{
			newWeightPenalty = 2;
		}
		else if(weightproc < 100)
		{
			newWeightPenalty = 3;
		}
		else
		{
			newWeightPenalty = 4;
		}

		int current = getWeightPenalty();
		if(current == newWeightPenalty)
			return;

		if(newWeightPenalty > 0)
		{
			super.addSkill(SkillTable.getInstance().getInfo(4270, newWeightPenalty));
		}
		else
		{
			super.removeSkill(getKnownSkill(4270));
		}

		sendPacket(new SkillList(this));
		sendEtcStatusUpdate();
		updateStats();
	}

	public int getArmorsExpertisePenalty()
	{
		return getSkillLevel(6213, 0);
	}

	public int getWeaponsExpertisePenalty()
	{
		return getSkillLevel(6209, 0);
	}

	public int getExpertisePenalty(ItemInstance item)
	{
		if(item.getTemplate().getType2() == ItemTemplate.TYPE2_WEAPON)
			return getWeaponsExpertisePenalty();
		else if(item.getTemplate().getType2() == ItemTemplate.TYPE2_SHIELD_ARMOR || item.getTemplate().getType2() == ItemTemplate.TYPE2_ACCESSORY)
			return getArmorsExpertisePenalty();
		return 0;
	}

	public void refreshExpertisePenalty()
	{
		if(isLogoutStarted())
			return;

		// Calculate the current higher Expertise of the L2Player
		int level = (int) calcStat(Stats.GRADE_EXPERTISE_LEVEL, getLevel(), null, null);
		int i = 0;
		for(i = 0; i < EXPERTISE_LEVELS.length; i++)
			if(level < EXPERTISE_LEVELS[i + 1])
			{
				break;
			}

		boolean skillUpdate = false; // Ð”Ð»Ñ� Ñ‚Ð¾Ð³Ð¾, Ñ‡Ñ‚Ð¾Ð±Ñ‹ Ð»Ð¸ÑˆÐ½Ð¸Ð¹ Ñ€Ð°Ð· Ð½Ðµ Ð¿Ð¾Ñ�Ñ‹Ð»Ð°Ñ‚ÑŒ
		// Ð¿Ð°ÐºÐµÑ‚Ñ‹
		// Add the Expertise skill corresponding to its Expertise level
		if(expertiseIndex != i)
		{
			expertiseIndex = i;
			if(expertiseIndex > 0)
			{
				addSkill(SkillTable.getInstance().getInfo(239, expertiseIndex), false);
				skillUpdate = true;
			}
		}
		if(Config.DISABLE_GRADE_PENALTY)
			return;

		int newWeaponPenalty = 0;
		int newArmorPenalty = 0;
		ItemInstance[] items = getInventory().getPaperdollItems();
		for(ItemInstance item : items)
			if(item != null)
			{
				int crystaltype = item.getTemplate().getCrystalType().ordinal();
				if(item.getTemplate().getType2() == ItemTemplate.TYPE2_WEAPON)
				{
					if(crystaltype > newWeaponPenalty)
					{
						newWeaponPenalty = crystaltype;
					}
				}
				else if(item.getTemplate().getType2() == ItemTemplate.TYPE2_SHIELD_ARMOR || item.getTemplate().getType2() == ItemTemplate.TYPE2_ACCESSORY)
					if(crystaltype > newArmorPenalty)
					{
						newArmorPenalty = crystaltype;
					}
			}

		newWeaponPenalty = newWeaponPenalty - expertiseIndex;
		if(newWeaponPenalty <= 0)
		{
			newWeaponPenalty = 0;
		}
		else if(newWeaponPenalty >= 4)
		{
			newWeaponPenalty = 4;
		}

		newArmorPenalty = newArmorPenalty - expertiseIndex;
		if(newArmorPenalty <= 0)
		{
			newArmorPenalty = 0;
		}
		else if(newArmorPenalty >= 4)
		{
			newArmorPenalty = 4;
		}

		int weaponExpertise = getWeaponsExpertisePenalty();
		int armorExpertise = getArmorsExpertisePenalty();

		if(weaponExpertise != newWeaponPenalty)
		{
			weaponExpertise = newWeaponPenalty;
			if(newWeaponPenalty > 0)
			{
				super.addSkill(SkillTable.getInstance().getInfo(6209, weaponExpertise));
			}
			else
			{
				super.removeSkill(getKnownSkill(6209));
			}
			skillUpdate = true;
		}
		if(armorExpertise != newArmorPenalty)
		{
			armorExpertise = newArmorPenalty;
			if(newArmorPenalty > 0)
			{
				super.addSkill(SkillTable.getInstance().getInfo(6213, armorExpertise));
			}
			else
			{
				super.removeSkill(getKnownSkill(6213));
			}
			skillUpdate = true;
		}

		if(skillUpdate)
		{
			getInventory().validateItemsSkills();

			sendPacket(new SkillList(this));
			sendEtcStatusUpdate();
			updateStats();
		}
	}

	public int getPvpKills()
	{
		return _pvpKills;
	}

	public void setPvpKills(int pvpKills)
	{
		_pvpKills = pvpKills;
	}

	public ClassId getClassId()
	{
		return getTemplate().classId;
	}

	public void addClanPointsOnProfession(final int id)
	{
		if(getLvlJoinedAcademy() != 0 && _clan != null && _clan.getLevel() >= 5 && ClassId.VALUES[id].getLevel() == 2)
		{
			_clan.incReputation(100, true, "Academy");
		}
		else if(getLvlJoinedAcademy() != 0 && _clan != null && _clan.getLevel() >= 5 && ClassId.VALUES[id].getLevel() == 3)
		{
			int earnedPoints = 0;
			if(getLvlJoinedAcademy() <= 16)
			{
				earnedPoints = Config.MAX_EARNED_ACADEM_POINT;
			}
			else if(getLvlJoinedAcademy() >= 39)
			{
				earnedPoints = Config.MIN_EARNED_ACADEM_POINT;
			}
			else
			{
				earnedPoints = Config.MAX_EARNED_ACADEM_POINT - (getLvlJoinedAcademy() - 16) * 20;
			}

			_clan.removeClanMember(getObjectId());

			SystemMessage sm = new SystemMessage(SystemMessage.CLAN_ACADEMY_MEMBER_S1_HAS_SUCCESSFULLY_COMPLETED_THE_2ND_CLASS_TRANSFER_AND_OBTAINED_S2_CLAN_REPUTATION_POINTS);
			sm.addString(getName());
			sm.addNumber(_clan.incReputation(earnedPoints, true, "Academy"));
			_clan.broadcastToOnlineMembers(sm);
			_clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListDelete(getName()), this);

			setClan(null);
			setTitle("");
			sendPacket(Msg.CONGRATULATIONS_YOU_WILL_NOW_GRADUATE_FROM_THE_CLAN_ACADEMY_AND_LEAVE_YOUR_CURRENT_CLAN_AS_A_GRADUATE_OF_THE_ACADEMY_YOU_CAN_IMMEDIATELY_JOIN_A_CLAN_AS_A_REGULAR_MEMBER_WITHOUT_BEING_SUBJECT_TO_ANY_PENALTIES);
			setLeaveClanTime(0);

			broadcastCharInfo();

			sendPacket(PledgeShowMemberListDeleteAll.STATIC);

			if(Config.SERVICES_CLAN_ACADEM_ENABLED && getVar("ClanListAcadem") != null)
			{
				int reward = Integer.parseInt(getVar("ClanListAcadem"));
				addAdena(reward, true);
				unsetVar("ClanListAcadem");
			}
			ItemFunctions.addItem(this, 8181, 1, true);
		}
	}

	/**
	 * Set the template of the L2Player.
	 * 
	 * @param id
	 *            The Identifier of the L2PlayerTemplate to set to the L2Player
	 */
	public synchronized void setClassId(final int id, boolean noban, boolean fromQuest)
	{
		if(!noban && !(ClassId.VALUES[id].equalsOrChildOf(ClassId.VALUES[getActiveClassId()]) || getPlayerAccess().CanChangeClass || Config.EVERYBODY_HAS_ADMIN_RIGHTS))
		{
			Thread.dumpStack();
			return;
		}

		// Ð•Ñ�Ð»Ð¸ Ð½Ð¾Ð²Ñ‹Ð¹ ID Ð½Ðµ Ð¿Ñ€Ð¸Ð½Ð°Ð´Ð»ÐµÐ¶Ð¸Ñ‚ Ð¸Ð¼ÐµÑŽÑ‰Ð¸Ð¼Ñ�Ñ� ÐºÐ»Ð°Ñ�Ñ�Ð°Ð¼ Ð·Ð½Ð°Ñ‡Ð¸Ñ‚ Ñ�Ñ‚Ð¾ Ð½Ð¾Ð²Ð°Ñ� Ð¿Ñ€Ð¾Ñ„Ð°
		if(!getSubClasses().containsKey(id))
		{
			final SubClass cclass = getActiveClass();
			getSubClasses().remove(getActiveClassId());
			changeClassInDb(cclass.getClassId(), id);
			if(cclass.isBase())
			{
				setBaseClass(id);
				addClanPointsOnProfession(id);
				ItemInstance coupons = null;
				if(ClassId.VALUES[id].getLevel() == 2)
				{
					if(fromQuest && Config.ALT_ALLOW_SHADOW_WEAPONS)
					{
						coupons = ItemFunctions.createItem(8869);
					}
					unsetVar("newbieweapon");
					unsetVar("p1q2");
					unsetVar("p1q3");
					unsetVar("p1q4");
					unsetVar("prof1");
					unsetVar("ng1");
					unsetVar("ng2");
					unsetVar("ng3");
					unsetVar("ng4");
				}
				else if(ClassId.VALUES[id].getLevel() == 3)
				{
					if(fromQuest && Config.ALT_ALLOW_SHADOW_WEAPONS)
					{
						coupons = ItemFunctions.createItem(8870);
					}
					unsetVar("newbiearmor");
					unsetVar("dd1"); // ÑƒÐ´Ð°Ð»Ñ�ÐµÐ¼ Ð¾Ñ‚Ð¼ÐµÑ‚ÐºÐ¸ Ð¾ Ð²Ñ‹Ð´Ð°Ñ‡Ðµ Ð´Ð¸Ð¼ÐµÐ½ÑˆÐµÐ½
					// Ð´Ð°Ð¹Ð¼Ð¾Ð½Ð´Ð¾Ð²
					unsetVar("dd2");
					unsetVar("dd3");
					unsetVar("prof2.1");
					unsetVar("prof2.2");
					unsetVar("prof2.3");
				}

				if(coupons != null)
				{
					coupons.setCount(15);
					sendPacket(SystemMessage2.obtainItems(coupons));
					getInventory().addItem(coupons);
				}
			}

			// Ð’Ñ‹Ð´Ð°Ñ‡Ð° Holy Pomander
			switch(ClassId.VALUES[id])
			{
				case cardinal:
					ItemFunctions.addItem(this, 15307, 1, true);
					break;
				case evaSaint:
					ItemFunctions.addItem(this, 15308, 1, true);
					break;
				case shillienSaint:
					ItemFunctions.addItem(this, 15309, 4, true);
					break;
				default:
					break;
			}

			cclass.setClassId(id);
			getSubClasses().put(id, cclass);
			rewardSkills(true);
			storeCharSubClasses();

			if(fromQuest)
			{
				// Ð¡Ð¾Ñ†Ð¸Ð°Ð»ÐºÐ° Ð¿Ñ€Ð¸ Ð¿Ð¾Ð»ÑƒÑ‡ÐµÐ½Ð¸Ð¸ Ð¿Ñ€Ð¾Ñ„Ñ‹
				broadcastPacket(new MagicSkillUse(this, this, 5103, 1, 1000, 0));
				sendPacket(new PlaySound("ItemSound.quest_fanfare_2"));
			}
			broadcastCharInfo();
		}

		PlayerTemplate t = CharTemplateHolder.getInstance().getTemplate(id, getSex() == 1);
		if(t == null)
		{
			_log.error("Missing template for classId: " + id);
			// do not throw error - only print error
			return;
		}

		// Set the template of the L2Player
		_template = t;
		
		// Update class icon in party and clan
		if(isInParty())
		{
			getParty().broadCast(new PartySmallWindowUpdate(this));
		}
		if(getClan() != null)
			getClan().broadcastToOnlineMembers(new PledgeShowMemberListUpdate(this));
		if(_matchingRoom != null)
		{
			_matchingRoom.broadcastPlayerUpdate(this);
		}
	}

	public long getExp()
	{
		return _activeClass == null ? 0 : _activeClass.getExp();
	}

	public long getMaxExp()
	{
		return _activeClass == null ? Experience.LEVEL[Experience.getMaxLevel() + 1] : _activeClass.getMaxExp();
	}

	public void setEnchantScroll(final ItemInstance scroll)
	{
		_enchantScroll = scroll;
	}

	public ItemInstance getEnchantScroll()
	{
		return _enchantScroll;
	}

	public void setFistsWeaponItem(final WeaponTemplate weaponItem)
	{
		_fistsWeaponItem = weaponItem;
	}

	public WeaponTemplate getFistsWeaponItem()
	{
		return _fistsWeaponItem;
	}

	public WeaponTemplate findFistsWeaponItem(final int classId)
	{
		// human fighter fists
		if(classId >= 0x00 && classId <= 0x09)
			return (WeaponTemplate) ItemHolder.getInstance().getTemplate(246);

		// human mage fists
		if(classId >= 0x0a && classId <= 0x11)
			return (WeaponTemplate) ItemHolder.getInstance().getTemplate(251);

		// elven fighter fists
		if(classId >= 0x12 && classId <= 0x18)
			return (WeaponTemplate) ItemHolder.getInstance().getTemplate(244);

		// elven mage fists
		if(classId >= 0x19 && classId <= 0x1e)
			return (WeaponTemplate) ItemHolder.getInstance().getTemplate(249);

		// dark elven fighter fists
		if(classId >= 0x1f && classId <= 0x25)
			return (WeaponTemplate) ItemHolder.getInstance().getTemplate(245);

		// dark elven mage fists
		if(classId >= 0x26 && classId <= 0x2b)
			return (WeaponTemplate) ItemHolder.getInstance().getTemplate(250);

		// orc fighter fists
		if(classId >= 0x2c && classId <= 0x30)
			return (WeaponTemplate) ItemHolder.getInstance().getTemplate(248);

		// orc mage fists
		if(classId >= 0x31 && classId <= 0x34)
			return (WeaponTemplate) ItemHolder.getInstance().getTemplate(252);

		// dwarven fists
		if(classId >= 0x35 && classId <= 0x39)
			return (WeaponTemplate) ItemHolder.getInstance().getTemplate(247);

		return null;
	}

	/*
	 	public void addVitExpAndSp(long addToExp, long addToSp, final L2NpcInstance target, final boolean applyBonus, final boolean appyToPet)
		{
			if(target == null)
				return;

			if(!target.isRaid() && !target.isBoss() && Config.ENABLE_VITALITY)
				switch (getVitalityLevel())
				{
					case 0:
						break;
					case 1:
						addToExp *= Config.RATE_VITALITY_LEVEL_1;
						addToSp *= Config.RATE_VITALITY_LEVEL_1;
						break;
					case 2:
						addToExp *= Config.RATE_VITALITY_LEVEL_2;
						addToSp *= Config.RATE_VITALITY_LEVEL_2;
						break;
					case 3:
						addToExp *= Config.RATE_VITALITY_LEVEL_3;
						addToSp *= Config.RATE_VITALITY_LEVEL_3;
						break;
					case 4:
						addToExp *= Config.RATE_VITALITY_LEVEL_4;
						addToSp *= Config.RATE_VITALITY_LEVEL_4;
						break;
				}

			addExpAndSp(addToExp, addToSp, applyBonus, appyToPet);
		}
	  */

	public void addExpAndCheckBonus(MonsterInstance mob, final double noRateExp, double noRateSp, double partyVitalityMod)
	{
		if(_activeClass == null)
			return;

		// Ð�Ð°Ñ‡Ð¸Ñ�Ð»ÐµÐ½Ð¸Ðµ Ð´ÑƒÑˆ ÐºÐ°Ð¼Ð°Ñ�Ð»Ñ�Ð¼
		double neededExp = calcStat(Stats.SOULS_CONSUME_EXP, 0, mob, null);
		if(neededExp > 0 && noRateExp > neededExp)
		{
			mob.broadcastPacket(new SpawnEmitter(mob, this));
			ThreadPoolManager.getInstance().schedule(new GameObjectTasks.SoulConsumeTask(this), 1000);
		}

		double vitalityBonus = 0.;
		int npcLevel = mob.getLevel();
		if(Config.ALT_VITALITY_ENABLED)
		{
			boolean blessActive = getNevitSystem().isBlessingActive();
			vitalityBonus = mob.isRaid() ? 0. : getVitalityLevel(blessActive) / 2.;
			vitalityBonus *= Config.ALT_VITALITY_RATE;
			if(noRateExp > 0)
			{
				if(!isVitalityStop())
				{
					if(!mob.isRaid())
					{
						// TODO: Ð Ð°Ð·Ð¾Ð±Ñ€Ð°Ñ‚Ñ�Ñ�, Ð½ÐµÐ»ÑŒÐ·Ñ� Ð¿Ñ€ÐµÐ´Ð¼ÐµÑ‚Ñ‹ Ð¸Ñ�Ð¿Ð¾Ð»ÑŒÐ·Ð¾Ð²Ð°Ñ‚ÑŒ, Ð¸Ð»Ð¸ Ð¿Ñ€ÐµÐ´Ð¼ÐµÑ‚Ñ‹ Ð½Ðµ Ð±ÑƒÐ´ÑƒÑ‚ Ð´Ð°Ð²Ð°Ñ‚ÑŒ Ñ�Ñ„Ñ„ÐµÐºÑ‚Ð°?
						// (Ð’Ñ�Ðµ Ð¿Ñ€ÐµÐ´Ð¼ÐµÑ‚Ñ‹ Ð´Ð»Ñ� Ð²Ð¾Ñ�Ð¿Ð¾Ð»Ð½ÐµÐ½Ð¸Ñ� Ð¸Ð»Ð¸ Ð¿Ð¾Ð´Ð´ÐµÑ€Ð¶Ð°Ð½Ð¸Ñ� Ñ�Ð½ÐµÑ€Ð³Ð¸Ð¸ Ð½Ðµ Ð´ÐµÐ¹Ñ�Ñ‚Ð²ÑƒÑŽÑ‚ Ð²Ð¾ Ð²Ñ€ÐµÐ¼Ñ� Ð´ÐµÐ¹Ñ�Ñ‚Ð²Ð¸Ñ� Ð�Ð¸Ñ�Ñ…Ð¾Ð¶Ð´ÐµÐ½Ð¸Ñ� Ð�ÐµÐ²Ð¸Ñ‚Ñ‚Ð°)
						if(!blessActive && !(getVarB("NoExp") && getExp() == Experience.LEVEL[getLevel() + 1] - 1))
						{
							double points = ((noRateExp / (npcLevel * npcLevel)) * 100) / 9;
							points *= Config.ALT_VITALITY_CONSUME_RATE;

							if(getEffectList().getEffectByType(EffectType.Vitality) != null)
								points *= -1;

							setVitality(getVitality() - points * partyVitalityMod);
						}
					}
					else
						setVitality(getVitality() + Config.ALT_VITALITY_RAID_BONUS);
				}
				else
					setVitality(getVitality());
			}
		}

		//ÐŸÑ€Ð¸ Ð¿ÐµÑ€Ð²Ð¾Ð¼ Ð²Ñ‹Ð·Ð¾Ð²Ðµ, Ð°ÐºÑ‚Ð¸Ð²Ð¸Ñ€ÑƒÐµÐ¼ Ñ‚Ð°Ð¹Ð¼ÐµÑ€Ñ‹ Ð±Ð¾Ð½ÑƒÑ�Ð¾Ð².
		if(!isInPeaceZone())
		{
			setRecomTimerActive(true);
			getNevitSystem().startAdventTask();
			if((getLevel() - npcLevel) <= 9)
			{
				int nevitPoints = (int) Math.round(((noRateExp / (npcLevel * npcLevel)) * 100) / 20); //TODO: Ð¤Ð¾Ñ€Ð¼ÑƒÐ»Ð° Ð¾Ñ‚ Ð±Ð°Ð»Ð´Ñ‹.
				getNevitSystem().addPoints(nevitPoints);
			}
		}

		long normalExp = (long) (noRateExp * ((Config.RATE_XP * getRateExp() + vitalityBonus) * getRecomBonusMul()));
		long normalSp = (long) (noRateSp * (Config.RATE_SP * getRateSp() + vitalityBonus));

		long expWithoutBonus = (long) (noRateExp * Config.RATE_XP * getRateExp());
		long spWithoutBonus = (long) (noRateSp * Config.RATE_SP * getRateSp());

		addExpAndSp(normalExp, normalSp, normalExp - expWithoutBonus, normalSp - spWithoutBonus, false, true);
	}

	private boolean _isVitalityStop = false;

	public void VitalityStop(boolean stop)
	{
		_isVitalityStop = stop;
	}

	private boolean isVitalityStop()
	{
		return _isVitalityStop;
	}

	@Override
	public void addExpAndSp(long exp, long sp)
	{
		addExpAndSp(exp, sp, 0, 0, false, false);
	}

	public void addExpAndSp(long addToExp, long addToSp, long bonusAddExp, long bonusAddSp, boolean applyRate, boolean applyToPet)
	{
		if(_activeClass == null)
			return;

		if(applyRate)
		{
			addToExp *= Config.RATE_XP * getRateExp();
			addToSp *= Config.RATE_SP * getRateSp();
		}

		Summon pet = getPet();
		if(addToExp > 0)
		{
			if(applyToPet)
				if(pet != null && !pet.isDead() && !PetDataTable.isVitaminPet(pet.getNpcId()))
					// Sin Eater Ð·Ð°Ð±Ð¸Ñ€Ð°ÐµÑ‚ Ð²Ñ�ÑŽ Ñ�ÐºÑ�Ð¿Ñƒ Ñƒ Ð¿ÐµÑ€Ñ�Ð¾Ð½Ð°Ð¶Ð°
					if(pet.getNpcId() == PetDataTable.SIN_EATER_ID)
					{
						pet.addExpAndSp(addToExp, 0);
						addToExp = 0;
					}
					else if(pet.isPet() && pet.getExpPenalty() > 0f)
						if(pet.getLevel() > getLevel() - 20 && pet.getLevel() < getLevel() + 5)
						{
							pet.addExpAndSp((long) (addToExp * pet.getExpPenalty()), 0);
							addToExp *= 1. - pet.getExpPenalty();
						}
						else
						{
							pet.addExpAndSp((long) (addToExp * pet.getExpPenalty() / 5.), 0);
							addToExp *= 1. - pet.getExpPenalty() / 5.;
						}
					else if(pet.isSummon())
					{
						addToExp *= 1. - pet.getExpPenalty();
					}

			// Remove Karma when the player kills L2MonsterInstance
			// TODO [G1ta0] Ð´Ð²Ð¸Ð½ÑƒÑ‚ÑŒ Ð² Ð¼ÐµÑ‚Ð¾Ð´ Ð½Ð°Ñ‡Ð¸Ñ�Ð»ÐµÐ½Ð¸Ñ� Ð½Ð°Ð³Ñ€Ð°Ð´ Ð¿Ñ€Ð¸ ÑƒÐ±Ð¹Ð¸Ñ�Ñ‚Ð²Ðµ Ð¼Ð¾Ð±Ð°
			if(!isCursedWeaponEquipped() && addToSp > 0 && _karma > 0)
			{
				_karma -= addToSp / (Config.KARMA_SP_DIVIDER * Config.RATE_SP);
			}

			if(_karma < 0)
			{
				_karma = 0;
			}

			long max_xp = getVarB("NoExp") ? Experience.LEVEL[getLevel() + 1] - 1 : getMaxExp();
			addToExp = Math.min(addToExp, max_xp - getExp());
		}

		int oldLvl = _activeClass.getLevel();

		_activeClass.addExp(addToExp);
		_activeClass.addSp(addToSp);

		if(addToExp > 0 && addToSp > 0 && (bonusAddExp > 0 || bonusAddSp > 0))
		{
			sendPacket(new SystemMessage2(SystemMsg.YOU_HAVE_ACQUIRED_S1_EXP_BONUS_S2_AND_S3_SP_BONUS_S4).addLong(addToExp).addLong(bonusAddExp).addInteger(addToSp).addInteger((int) bonusAddSp));
		}
		else if(addToSp > 0 && addToExp == 0)
		{
			sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_ACQUIRED_S1_SP).addNumber(addToSp));
		}
		else if(addToSp > 0 && addToExp > 0)
		{
			sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_EARNED_S1_EXPERIENCE_AND_S2_SP).addNumber(addToExp).addNumber(addToSp));
		}
		else if(addToSp == 0 && addToExp > 0)
		{
			sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_EARNED_S1_EXPERIENCE).addNumber(addToExp));
		}

		int level = _activeClass.getLevel();
		if(level != oldLvl)
		{
			int levels = level - oldLvl;
			if(levels > 0)
			{
				getNevitSystem().addPoints(1950);
			}
			levelSet(levels);
		}

		if(pet != null && pet.isPet() && PetDataTable.isVitaminPet(pet.getNpcId()))
		{
			PetInstance _pet = (PetInstance) pet;
			_pet.setLevel(getLevel());
			_pet.setExp(_pet.getExpForNextLevel());
			_pet.broadcastStatusUpdate();
		}

		if(getNevitSystem().isBlessingActive())
		{
			addVitality(Config.ALT_VITALITY_NEVIT_POINT);
		}

		updateStats();
	}

	/**
	 * Give Expertise skill of this level.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Get the Level of the L2Player</li> <li>Add the Expertise skill
	 * corresponding to its Expertise level</li> <li>Update the overloaded
	 * status of the L2Player</li><BR>
	 * <BR>
	 * <p/>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T give other
	 * free skills (SP needed = 0)</B></FONT><BR>
	 * <BR>
	 * 
	 * @param send
	 */
	private void rewardSkills(boolean send)
	{
		boolean update = false;
		if(Config.ALT_AUTO_LEARN_SKILLS)
		{
			int unLearnable = 0;
			Collection<SkillLearn> skills = SkillAcquireHolder.getInstance().getAvailableSkills(this, AcquireType.NORMAL);
			while(skills.size() > unLearnable)
			{
				unLearnable = 0;
				for(SkillLearn s : skills)
				{
					Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
					if(sk == null || !sk.getCanLearn(getClassId()) || !Config.ALT_AUTO_LEARN_FORGOTTEN_SKILLS && s.isClicked())
					{
						unLearnable++;
						continue;
					}
					addSkill(sk, true);
				}
				skills = SkillAcquireHolder.getInstance().getAvailableSkills(this, AcquireType.NORMAL);
			}
			update = true;
		}
		else
		{
			// Ð¡ÐºÐ¸Ð»Ð»Ñ‹ Ð´Ð°ÑŽÑ‰Ð¸ÐµÑ�Ñ� Ð±ÐµÑ�Ð¿Ð»Ð°Ñ‚Ð½Ð¾ Ð½Ðµ Ñ‚Ñ€ÐµÐ±ÑƒÑŽÑ‚ Ð¸Ð·ÑƒÑ‡ÐµÐ½Ð¸Ñ�
			for(SkillLearn skill : SkillAcquireHolder.getInstance().getAvailableSkills(this, AcquireType.NORMAL))
				if(skill.getCost() == 0 && skill.getItemId() == 0)
				{
					Skill sk = SkillTable.getInstance().getInfo(skill.getId(), skill.getLevel());
					addSkill(sk, true);
					if(getAllShortCuts().size() > 0 && sk.getLevel() > 1)
					{
						for(ShortCut sc : getAllShortCuts())
							if(sc.getId() == sk.getId() && sc.getType() == ShortCut.TYPE_SKILL)
							{
								ShortCut newsc = new ShortCut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), sk.getLevel(), 1);
								sendPacket(new ShortCutRegister(this, newsc));
								registerShortCut(newsc);
							}
					}
					update = true;
				}
		}

		if(send && update)
		{
			sendPacket(new SkillList(this));
		}

		updateStats();
	}

	public Race getRace()
	{
		return getBaseTemplate().race;
	}

	public int getIntSp()
	{
		return (int) getSp();
	}

	public long getSp()
	{
		return _activeClass == null ? 0 : _activeClass.getSp();
	}

	public void setSp(long sp)
	{
		if(_activeClass != null)
		{
			_activeClass.setSp(sp);
		}
	}

	public int getClanId()
	{
		return _clan == null ? 0 : _clan.getClanId();
	}

	public long getLeaveClanTime()
	{
		return _leaveClanTime;
	}

	public long getDeleteClanTime()
	{
		return _deleteClanTime;
	}

	public void setLeaveClanTime(final long time)
	{
		_leaveClanTime = time;
	}

	public void setDeleteClanTime(final long time)
	{
		_deleteClanTime = time;
	}

	public void setOnlineTime(final long time)
	{
		_onlineTime = time;
		_onlineBeginTime = System.currentTimeMillis();
	}

	public void setNoChannel(final long time)
	{
		_NoChannel = time;
		if(_NoChannel > 2145909600000L || _NoChannel < 0)
		{
			_NoChannel = -1;
		}

		if(_NoChannel > 0)
		{
			_NoChannelBegin = System.currentTimeMillis();
		}
		else
		{
			_NoChannelBegin = 0;
		}
	}

	public long getNoChannel()
	{
		return _NoChannel;
	}

	public long getNoChannelRemained()
	{
		if(_NoChannel == 0)
			return 0;
		else if(_NoChannel < 0)
			return -1;
		else
		{
			long remained = _NoChannel - System.currentTimeMillis() + _NoChannelBegin;
			if(remained < 0)
				return 0;

			return remained;
		}
	}

	public void setLeaveClanCurTime()
	{
		_leaveClanTime = System.currentTimeMillis();
	}

	public void setDeleteClanCurTime()
	{
		_deleteClanTime = System.currentTimeMillis();
	}

	public boolean canJoinClan()
	{
		if(_leaveClanTime == 0)
			return true;
		if(System.currentTimeMillis() - _leaveClanTime >= 24 * 60 * 60 * 1000L)
		{
			_leaveClanTime = 0;
			return true;
		}
		return false;
	}

	public boolean canCreateClan()
	{
		if(_deleteClanTime == 0)
			return true;
		if(System.currentTimeMillis() - _deleteClanTime >= 10 * 24 * 60 * 60 * 1000L)
		{
			_deleteClanTime = 0;
			return true;
		}
		return false;
	}

	public IStaticPacket canJoinParty(Player inviter)
	{
		Request request = getRequest();
		if(request != null && request.isInProgress() && request.getOtherPlayer(this) != inviter)
			return SystemMsg.WAITING_FOR_ANOTHER_REPLY.packet(inviter); // Ð·Ð°Ð½Ñ�Ñ‚
		if(isBlockAll() || getMessageRefusal()) // Ð²Ñ�ÐµÑ… Ð½Ð°Ñ„Ð¸Ð³
			return SystemMsg.THAT_PERSON_IS_IN_MESSAGE_REFUSAL_MODE.packet(inviter);
		if(isInParty()) // ÑƒÐ¶Ðµ
			return new SystemMessage2(SystemMsg.C1_IS_A_MEMBER_OF_ANOTHER_PARTY_AND_CANNOT_BE_INVITED).addName(this);
		if(inviter.getReflection() != getReflection()) // Ð² Ñ€Ð°Ð·Ð½Ñ‹Ñ… Ð¸Ð½Ñ�Ñ‚Ð°Ð½Ñ‚Ð°Ñ…
			if(inviter.getReflection() != ReflectionManager.DEFAULT && getReflection() != ReflectionManager.DEFAULT)
				return SystemMsg.INVALID_TARGET.packet(inviter);
		if(isCursedWeaponEquipped() || inviter.isCursedWeaponEquipped()) // Ð·Ð°Ñ€Ð¸Ñ‡
			return SystemMsg.INVALID_TARGET.packet(inviter);
		if(inviter.isInOlympiadMode() || isInOlympiadMode()) // Ð¾Ð»Ð¸Ð¼Ð¿Ð¸Ð°Ð´Ð°
			return SystemMsg.A_USER_CURRENTLY_PARTICIPATING_IN_THE_OLYMPIAD_CANNOT_SEND_PARTY_AND_FRIEND_INVITATIONS.packet(inviter);
		if(!inviter.getPlayerAccess().CanJoinParty || !getPlayerAccess().CanJoinParty) // Ð½Ð¸Ð·Ñ�
			return SystemMsg.INVALID_TARGET.packet(inviter);
		if(getTeam() != TeamType.NONE) // ÑƒÑ‡Ð°Ñ�Ñ‚Ð½Ð¸Ðº Ð¿Ð²Ð¿ Ñ�Ð²ÐµÐ½Ñ‚Ð° Ð¸Ð»Ð¸ Ð´ÑƒÑ�Ð»Ð¸
			return SystemMsg.INVALID_TARGET.packet(inviter);
		return null;
	}

	@Override
	public PcInventory getInventory()
	{
		return _inventory;
	}

	@Override
	public long getWearedMask()
	{
		return _inventory.getWearedMask();
	}

	public PcFreight getFreight()
	{
		return _freight;
	}

	public void removeItemFromShortCut(final int objectId)
	{
		_shortCuts.deleteShortCutByObjectId(objectId);
	}

	public void removeSkillFromShortCut(final int skillId)
	{
		_shortCuts.deleteShortCutBySkillId(skillId);
	}

	public boolean isSitting()
	{
		return _isSitting;
	}

	public void setSitting(boolean val)
	{
		_isSitting = val;
	}

	public boolean getSittingTask()
	{
		return sittingTaskLaunched;
	}

	@Override
	public void sitDown(StaticObjectInstance throne)
	{
		if(isSitting() || sittingTaskLaunched || isAlikeDead())
			return;

		if(isStunned() || isSleeping() || isParalyzed() || isAttackingNow() || isCastingNow() || isMoving)
		{
			getAI().setNextAction(nextAction.REST, null, null, false, false);
			return;
		}

		resetWaitSitTime();
		getAI().setIntention(CtrlIntention.AI_INTENTION_REST, null, null);

		if(throne == null)
		{
			broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_SITTING));
		}
		else
		{
			broadcastPacket(new ChairSit(this, throne));
		}

		_sittingObject = throne;
		setSitting(true);
		sittingTaskLaunched = true;
		ThreadPoolManager.getInstance().schedule(new EndSitDownTask(this), 2500);
	}

	@Override
	public void standUp()
	{
		if(!isSitting() || sittingTaskLaunched || isInStoreMode() || isAlikeDead())
			return;

		// FIXME [G1ta0] Ñ�Ñ„Ñ„ÐµÐºÑ‚ Ñ�Ð°Ð¼ Ð¾Ñ‚ÐºÐ»ÑŽÑ‡Ð°ÐµÑ‚Ñ�Ñ� Ð²Ð¾ Ð²Ñ€ÐµÐ¼Ñ� Ð´ÐµÐ¹Ñ�Ñ‚Ð²Ð¸Ñ�, ÐµÑ�Ð»Ð¸ Ð¿ÐµÑ€Ñ�Ð¾Ð½Ð°Ð¶
		// Ð½Ðµ Ñ�Ð¸Ð´Ð¸Ñ‚, Ð²Ð¾Ð·Ð¼Ð¾Ð¶Ð½Ð¾ Ñ�Ñ‚Ð¾Ð¸Ñ‚ ÑƒÐ±Ñ€Ð°Ñ‚ÑŒ
		getEffectList().stopAllSkillEffects(EffectType.Relax);

		getAI().clearNextAction();
		broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_STANDING));

		_sittingObject = null;
		sittingTaskLaunched = true;
		ThreadPoolManager.getInstance().schedule(new EndStandUpTask(this), 2500);
	}

	public void updateWaitSitTime()
	{
		if(_waitTimeWhenSit < 200)
		{
			_waitTimeWhenSit += 2;
		}
	}

	public int getWaitSitTime()
	{
		return _waitTimeWhenSit;
	}

	public void resetWaitSitTime()
	{
		_waitTimeWhenSit = 0;
	}

	public Warehouse getWarehouse()
	{
		return _warehouse;
	}

	public ItemContainer getRefund()
	{
		return _refund;
	}

	public long getAdena()
	{
		return getInventory().getAdena();
	}

	public boolean reduceAdena(long adena)
	{
		return reduceAdena(adena, false);
	}

	/**
	 * Ð—Ð°Ð±Ð¸Ñ€Ð°ÐµÑ‚ Ð°Ð´ÐµÐ½Ñƒ Ñƒ Ð¸Ð³Ñ€Ð¾ÐºÐ°.<BR>
	 * <BR>
	 * 
	 * @param adena
	 *            - Ñ�ÐºÐ¾Ð»ÑŒÐºÐ¾ Ð°Ð´ÐµÐ½Ñ‹ Ð·Ð°Ð±Ñ€Ð°Ñ‚ÑŒ
	 * @param notify
	 *            - Ð¾Ñ‚Ð¾Ð±Ñ€Ð°Ð¶Ð°Ñ‚ÑŒ Ñ�Ð¸Ñ�Ñ‚ÐµÐ¼Ð½Ð¾Ðµ Ñ�Ð¾Ð¾Ð±Ñ‰ÐµÐ½Ð¸Ðµ
	 * @return true ÐµÑ�Ð»Ð¸ Ñ�Ð½Ñ�Ð»Ð¸
	 */
	public boolean reduceAdena(long adena, boolean notify)
	{
		if(adena < 0)
			return false;
		if(adena == 0)
			return true;
		boolean result = getInventory().reduceAdena(adena);
		if(notify && result)
		{
			sendPacket(SystemMessage2.removeItems(ItemTemplate.ITEM_ID_ADENA, adena));
		}
		return result;
	}

	public ItemInstance addAdena(long adena)
	{
		return addAdena(adena, false);
	}

	/**
	 * Ð”Ð¾Ð±Ð°Ð²Ð»Ñ�ÐµÑ‚ Ð°Ð´ÐµÐ½Ñƒ Ð¸Ð³Ñ€Ð¾ÐºÑƒ.<BR>
	 * <BR>
	 * 
	 * @param adena
	 *            - Ñ�ÐºÐ¾Ð»ÑŒÐºÐ¾ Ð°Ð´ÐµÐ½Ñ‹ Ð´Ð°Ñ‚ÑŒ
	 * @param notify
	 *            - Ð¾Ñ‚Ð¾Ð±Ñ€Ð°Ð¶Ð°Ñ‚ÑŒ Ñ�Ð¸Ñ�Ñ‚ÐµÐ¼Ð½Ð¾Ðµ Ñ�Ð¾Ð¾Ð±Ñ‰ÐµÐ½Ð¸Ðµ
	 * @return L2ItemInstance - Ð½Ð¾Ð²Ð¾Ðµ ÐºÐ¾Ð»Ð¸Ñ‡ÐµÑ�Ñ‚Ð²Ð¾ Ð°Ð´ÐµÐ½Ñ‹
	 */
	public ItemInstance addAdena(long adena, boolean notify)
	{
		if(adena < 1)
			return null;
		ItemInstance item = getInventory().addAdena(adena);
		if(item != null && notify)
		{
			sendPacket(SystemMessage2.obtainItems(ItemTemplate.ITEM_ID_ADENA, adena, 0));
		}
		return item;
	}

	public GameClient getNetConnection()
	{
		return _connection;
	}

	public int getRevision()
	{
		return _connection == null ? 0 : _connection.getRevision();
	}

	public void setNetConnection(final GameClient connection)
	{
		_connection = connection;
	}

	public boolean isConnected()
	{
		return _connection != null && _connection.isConnected();
	}

	@Override
	public void onAction(final Player player, boolean shift)
	{
		if(isFrozen())
		{
			player.sendPacket(ActionFail.STATIC);
			return;
		}

		if(Events.onAction(player, this, shift))
		{
			player.sendPacket(ActionFail.STATIC);
			return;
		}
		// Check if the other player already target this L2Player
		if(player.getTarget() != this)
		{
			player.setTarget(this);
			if(player.getTarget() == this)
			{
				player.sendPacket(new MyTargetSelected(getObjectId(), 0)); // The
				// color
				// to
				// display
				// in
				// the
				// select
				// window
				// is
				// White
			}
			else
			{
				player.sendPacket(ActionFail.STATIC);
			}
		}
		else if(getPrivateStoreType() != Player.STORE_PRIVATE_NONE)
		{
			if(getDistance(player) > INTERACTION_DISTANCE && player.getAI().getIntention() != CtrlIntention.AI_INTENTION_INTERACT)
			{
				if(!shift)
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this, null);
				}
				else
				{
					player.sendPacket(ActionFail.STATIC);
				}
			}
			else
			{
				player.doInteract(this);
			}
		}
		else if(isAutoAttackable(player))
		{
			player.getAI().Attack(this, false, shift);
		}
		else if(player != this)
		{
			if(player.getAI().getIntention() != CtrlIntention.AI_INTENTION_FOLLOW)
			{
				if(!shift)
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this, Config.ALT_FOLLOW_RANGE);
				}
				else
				{
					player.sendPacket(ActionFail.STATIC);
				}
			}
			else
			{
				player.sendPacket(ActionFail.STATIC);
			}
		}
		else
		{
			player.sendPacket(ActionFail.STATIC);
		}
	}

	@Override
	public void broadcastStatusUpdate()
	{
		if(!needStatusUpdate()) // ÐŸÐ¾ Ð¸Ð´ÐµÐµ ÐµÑˆÐµ Ð´Ð¾Ð»Ð¶Ð½Ð¾ Ñ�Ñ€ÐµÐ·Ð°Ñ‚ÑŒ Ñ‚Ñ€Ð°Ñ„Ñ„Ð¸Ðº. Ð‘ÑƒÐ´ÑƒÑ‚
			// Ð³Ð»ÑŽÐºÐ¸ Ñ� Ð¾Ñ‚Ð¾Ð±Ñ€Ð°Ð¶ÐµÐ½Ð¸ÐµÐ¼ - ÑƒÐ±Ñ€Ð°Ñ‚ÑŒ Ñ�Ñ‚Ð¾
			// ÑƒÑ�Ð»Ð¾Ð²Ð¸Ðµ.
			return;

		StatusUpdate su = makeStatusUpdate(StatusUpdate.MAX_HP, StatusUpdate.MAX_MP, StatusUpdate.MAX_CP, StatusUpdate.CUR_HP, StatusUpdate.CUR_MP, StatusUpdate.CUR_CP);
		sendPacket(su);
		// Check if a party is in progress
		if(isInParty())
		{
			// Send the Server->Client packet PartySmallWindowUpdate with
			// current HP, MP and Level to all other L2Player of the Party
			getParty().broadcastToPartyMembers(this, new PartySmallWindowUpdate(this));
		}

		DuelEvent duelEvent = getEvent(DuelEvent.class);
		if(duelEvent != null)
		{
			duelEvent.sendPacket(new ExDuelUpdateUserInfo(this), getTeam().revert().name());
		}

		if(isInOlympiadMode() && isOlympiadCompStart())
			if(_olympiadGame != null)
			{
				_olympiadGame.broadcastInfo(this, null, false);
			}
	}

	private ScheduledFuture<?> _broadcastCharInfoTask;

	public class BroadcastCharInfoTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			broadcastCharInfoImpl();
			_broadcastCharInfoTask = null;
		}
	}

	@Override
	public void broadcastCharInfo()
	{
		broadcastUserInfo(false);
	}

	/**
	 * ÐžÑ‚Ð¿Ñ€Ð°Ð²Ð»Ñ�ÐµÑ‚ UserInfo Ð´Ð°Ð½Ð¾Ð¼Ñƒ Ð¸Ð³Ñ€Ð¾ÐºÑƒ Ð¸ CharInfo Ð²Ñ�ÐµÐ¼ Ð¾ÐºÑ€ÑƒÐ¶Ð°ÑŽÑ‰Ð¸Ð¼.<BR>
	 * <BR>
	 * <p/>
	 * <B><U> ÐšÐ¾Ð½Ñ†ÐµÐ¿Ñ‚</U> :</B><BR>
	 * <BR>
	 * Ð¡ÐµÑ€Ð²ÐµÑ€ ÑˆÐ»ÐµÑ‚ Ð¸Ð³Ñ€Ð¾ÐºÑƒ UserInfo. Ð¡ÐµÑ€Ð²ÐµÑ€ Ð²Ñ‹Ð·Ñ‹Ð²Ð°ÐµÑ‚ Ð¼ÐµÑ‚Ð¾Ð´
	 * {@link Creature#broadcastPacketToOthers(jts.gameserver.network.serverpackets.L2GameServerPacket...)}
	 * Ð´Ð»Ñ� Ñ€Ð°Ñ�Ñ�Ñ‹Ð»ÐºÐ¸ CharInfo<BR>
	 * <BR>
	 * <p/>
	 * <B><U> Ð”ÐµÐ¹Ñ�Ñ‚Ð²Ð¸Ñ�</U> :</B><BR>
	 * <BR>
	 * <li>ÐžÑ‚Ñ�Ñ‹Ð»ÐºÐ° Ð¸Ð³Ñ€Ð¾ÐºÑƒ UserInfo(Ð»Ð¸Ñ‡Ð½Ñ‹Ðµ Ð¸ Ð¾Ð±Ñ‰Ð¸Ðµ Ð´Ð°Ð½Ð½Ñ‹Ðµ)</li>
	 * <li>ÐžÑ‚Ñ�Ñ‹Ð»ÐºÐ° Ð´Ñ€ÑƒÐ³Ð¸Ð¼ Ð¸Ð³Ñ€Ð¾ÐºÐ°Ð¼ CharInfo(Public data only)</li><BR>
	 * <BR>
	 * <p/>
	 * <FONT COLOR=#FF0000><B> <U>Ð’Ð½Ð¸Ð¼Ð°Ð½Ð¸Ðµ</U> : Ð�Ð• ÐŸÐžÐ¡Ð«Ð›Ð�Ð™Ð¢Ð• UserInfo Ð´Ñ€ÑƒÐ³Ð¸Ð¼
	 * Ð¸Ð³Ñ€Ð¾ÐºÐ°Ð¼ Ð»Ð¸Ð±Ð¾ CharInfo Ð´Ð°Ð½Ð¾Ð¼Ñƒ Ð¸Ð³Ñ€Ð¾ÐºÑƒ.<BR>
	 * Ð�Ð• Ð’Ð«Ð—Ð«Ð’Ð�Ð•Ð™Ð¢Ð• Ð­Ð¢ÐžÐ¢ ÐœÐ•Ð¢ÐžÐ” ÐšÐ ÐžÐœÐ• ÐžÐ¡ÐžÐ‘Ð«Ð¥ ÐžÐ‘Ð¡Ð¢ÐžÐ¯Ð¢Ð•Ð›Ð¬Ð¡Ð¢Ð’(Ñ�Ð¼ÐµÐ½Ð° Ñ�Ð°Ð±ÐºÐ»Ð°Ñ�Ñ�Ð° Ðº
	 * Ð¿Ñ€Ð¸Ð¼ÐµÑ€Ñƒ)!!! Ð¢Ñ€Ð°Ñ„Ñ„Ð¸Ðº Ð´Ð¸ÐºÐ¾ ÐºÑƒÑˆÐ°ÐµÑ‚Ñ�Ñ� Ñƒ Ð¸Ð³Ñ€Ð¾ÐºÐ¾Ð² Ð¸ Ð½Ð°Ñ‡Ð¸Ð½Ð°ÑŽÑ‚Ñ�Ñ� Ð»Ð°Ð³Ð¸.<br>
	 * Ð˜Ñ�Ð¿Ð¾Ð»ÑŒÐ·ÑƒÐ¹Ñ‚Ðµ Ð¼ÐµÑ‚Ð¾Ð´ {@link Player#sendChanges()}</B></FONT><BR>
	 * <BR>
	 */
	public void broadcastUserInfo(boolean force)
	{
		sendUserInfo(force);

		if(!isVisible() || isInvisible())
			return;

		if(Config.BROADCAST_CHAR_INFO_INTERVAL == 0)
		{
			force = true;
		}

		if(force)
		{
			if(_broadcastCharInfoTask != null)
			{
				_broadcastCharInfoTask.cancel(false);
				_broadcastCharInfoTask = null;
			}
			broadcastCharInfoImpl();
			return;
		}

		if(_broadcastCharInfoTask != null)
			return;

		_broadcastCharInfoTask = ThreadPoolManager.getInstance().schedule(new BroadcastCharInfoTask(), Config.BROADCAST_CHAR_INFO_INTERVAL);
	}

	private int _polyNpcId;

	public void setPolyId(int polyid)
	{
		_polyNpcId = polyid;

		teleToLocation(getLoc());
		broadcastUserInfo(true);
	}

	public boolean isPolymorphed()
	{
		return _polyNpcId != 0;
	}

	public int getPolyId()
	{
		return _polyNpcId;
	}

	private void broadcastCharInfoImpl()
	{
		if(!isVisible() || isInvisible())
			return;

		L2GameServerPacket ci = isPolymorphed() ? new NpcInfoPoly(this) : new CharInfo(this);
		L2GameServerPacket exCi = new ExBR_ExtraUserInfo(this);
		L2GameServerPacket dominion = getEvent(DominionSiegeEvent.class) != null ? new ExDominionWarStart(this) : null;
		for(Player player : World.getAroundPlayers(this))
		{
			player.sendPacket(ci, exCi);
			player.sendPacket(RelationChanged.update(player, this, player));
			if(dominion != null)
			{
				player.sendPacket(dominion);
			}
		}
	}

	public void broadcastRelationChanged()
	{
		if(!isVisible() || isInvisible())
			return;

		for(Player player : World.getAroundPlayers(this))
		{
			player.sendPacket(RelationChanged.update(player, this, player));
		}
	}

	public void sendEtcStatusUpdate()
	{
		if(!isVisible())
			return;

		sendPacket(new EtcStatusUpdate(this));
	}

	private Future<?> _userInfoTask;

	private class UserInfoTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			sendUserInfoImpl();
			_userInfoTask = null;
		}
	}

	private void sendUserInfoImpl()
	{
		sendPacket(new UserInfo(this), new ExBR_ExtraUserInfo(this));
		DominionSiegeEvent siegeEvent = getEvent(DominionSiegeEvent.class);
		if(siegeEvent != null)
		{
			sendPacket(new ExDominionWarStart(this));
		}
	}

	public void sendUserInfo()
	{
		sendUserInfo(false);
	}

	public void sendUserInfo(boolean force)
	{
		if(!isVisible() || entering || isLogoutStarted())
			return;

		if(Config.USER_INFO_INTERVAL == 0 || force)
		{
			if(_userInfoTask != null)
			{
				_userInfoTask.cancel(false);
				_userInfoTask = null;
			}
			sendUserInfoImpl();
			return;
		}

		if(_userInfoTask != null)
			return;

		_userInfoTask = ThreadPoolManager.getInstance().schedule(new UserInfoTask(), Config.USER_INFO_INTERVAL);
	}

	@Override
	public StatusUpdate makeStatusUpdate(int... fields)
	{
		StatusUpdate su = new StatusUpdate(getObjectId());
		for(int field : fields)
		{
			switch(field)
			{
				case StatusUpdate.CUR_HP:
					su.addAttribute(field, (int) getCurrentHp());
					break;
				case StatusUpdate.MAX_HP:
					su.addAttribute(field, getMaxHp());
					break;
				case StatusUpdate.CUR_MP:
					su.addAttribute(field, (int) getCurrentMp());
					break;
				case StatusUpdate.MAX_MP:
					su.addAttribute(field, getMaxMp());
					break;
				case StatusUpdate.CUR_LOAD:
					su.addAttribute(field, getCurrentLoad());
					break;
				case StatusUpdate.MAX_LOAD:
					su.addAttribute(field, getMaxLoad());
					break;
				case StatusUpdate.PVP_FLAG:
					su.addAttribute(field, _pvpFlag);
					break;
				case StatusUpdate.KARMA:
					su.addAttribute(field, getKarma());
					break;
				case StatusUpdate.CUR_CP:
					su.addAttribute(field, (int) getCurrentCp());
					break;
				case StatusUpdate.MAX_CP:
					su.addAttribute(field, getMaxCp());
					break;
			}
		}
		return su;
	}

	public void sendStatusUpdate(boolean broadCast, boolean withPet, int... fields)
	{
		if(fields.length == 0 || entering && !broadCast)
			return;

		StatusUpdate su = makeStatusUpdate(fields);
		if(!su.hasAttributes())
			return;

		List<L2GameServerPacket> packets = new ArrayList<L2GameServerPacket>(withPet ? 2 : 1);
		if(withPet && getPet() != null)
		{
			packets.add(getPet().makeStatusUpdate(fields));
		}

		packets.add(su);

		if(!broadCast)
		{
			sendPacket(packets);
		}
		else if(entering)
		{
			broadcastPacketToOthers(packets);
		}
		else
		{
			broadcastPacket(packets);
		}
	}

	/**
	 * @return the Alliance Identifier of the L2Player.<BR>
	 * <BR>
	 */
	public int getAllyId()
	{
		return _clan == null ? 0 : _clan.getAllyId();
	}

	@Override
	public void sendPacket(IStaticPacket p)
	{
		if(!isConnected())
			return;

		if(isPacketIgnored(p.packet(this)))
			return;

		_connection.sendPacket(p.packet(this));
	}

	@Override
	public void sendPacket(IStaticPacket... packets)
	{
		if(!isConnected())
			return;

		for(IStaticPacket p : packets)
		{
			if(isPacketIgnored(p))
			{
				continue;
			}

			_connection.sendPacket(p.packet(this));
		}
	}

	private boolean isPacketIgnored(IStaticPacket p)
	{
		if(p == null)
			return true;
		if(_notShowBuffAnim && (p.getClass() == MagicSkillUse.class || p.getClass() == MagicSkillLaunched.class))
			return true;

		// if(_notShowTraders && (p.getClass() == PrivateStoreMsgBuy.class ||
		// p.getClass() == PrivateStoreMsgSell.class || p.getClass() ==
		// RecipeShopMsg.class))
		// return true;

		return false;
	}

	@Override
	public void sendPacket(List<? extends IStaticPacket> packets)
	{
		if(!isConnected())
			return;

		for(IStaticPacket p : packets)
		{
			_connection.sendPacket(p.packet(this));
		}
	}

	public void doInteract(GameObject target)
	{
		if(target == null || isActionsDisabled())
		{
			sendActionFailed();
			return;
		}
		if(target.isPlayer())
		{
			if(target.getDistance(this) <= INTERACTION_DISTANCE)
			{
				Player temp = (Player) target;

				if(temp.getPrivateStoreType() == STORE_PRIVATE_SELL || temp.getPrivateStoreType() == STORE_PRIVATE_SELL_PACKAGE)
				{
					sendPacket(new PrivateStoreListSell(this, temp));
					sendActionFailed();
				}
				else if(temp.getPrivateStoreType() == STORE_PRIVATE_BUY)
				{
					sendPacket(new PrivateStoreListBuy(this, temp));
					sendActionFailed();
				}
				else if(temp.getPrivateStoreType() == STORE_PRIVATE_MANUFACTURE)
				{
					sendPacket(new RecipeShopSellList(this, temp));
					sendActionFailed();
				}
				else if(temp.getPrivateStoreType() == STORE_PRIVATE_BUFF)
				{
					OfflineBufferManager.getInstance().processBypass(this, "BuffStore bufflist " + temp.getObjectId());
					sendActionFailed();
				}
				sendActionFailed();
			}
			else if(getAI().getIntention() != CtrlIntention.AI_INTENTION_INTERACT)
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this, null);
			}
		}
		else
		{
			target.onAction(this, false);
		}
	}

	public void doAutoLootOrDrop(ItemInstance item, NpcInstance fromNpc)
	{
		boolean forceAutoloot = fromNpc.isFlying() || getReflection().isAutolootForced();

		if((fromNpc.isRaid() || fromNpc instanceof ReflectionBossInstance) && !Config.ALT_AUTO_LOOT_FROM_RAIDS && !item.isHerb() && !forceAutoloot)
		{
			item.dropToTheGround(this, fromNpc);
			return;
		}

		// Herbs
		if(item.isHerb())
		{
			if(!AutoLootHerbs && !forceAutoloot)
			{
				item.dropToTheGround(this, fromNpc);
				return;
			}
			Skill[] skills = item.getTemplate().getAttachedSkills();
			if(skills.length > 0)
			{
				for(Skill skill : skills)
				{
					altUseSkill(skill, this);
					if(getPet() != null && getPet().isSummon() && !getPet().isDead())
					{
						getPet().altUseSkill(skill, getPet());
					}
				}
			}
			item.deleteMe();
			return;
		}

		if(!_autoLoot && !forceAutoloot)
		{
			item.dropToTheGround(this, fromNpc);
			return;
		}
		// Check if the L2Player is in a Party
		if(!isInParty())
		{
			if(!pickupItem(item, Log.Pickup))
			{
				item.dropToTheGround(this, fromNpc);
				return;
			}
		}
		else
		{
			getParty().distributeItem(this, item, fromNpc);
		}

		broadcastPickUpMsg(item);
	}

	@Override
	public void doPickupItem(final GameObject object)
	{
		// Check if the L2Object to pick up is a L2ItemInstance
		if(!object.isItem())
		{
			_log.warn("trying to pickup wrong target." + getTarget());
			return;
		}

		sendActionFailed();
		stopMove();

		ItemInstance item = (ItemInstance) object;

		synchronized (item)
		{
			if(!item.isVisible())
				return;

			// Check if me not owner of item and, if in party, not in owner
			// party and nonowner pickup delay still active
			if(!ItemFunctions.checkIfCanPickup(this, item))
			{
				SystemMessage sm;
				if(item.getItemId() == 57)
				{
					sm = new SystemMessage(SystemMessage.YOU_HAVE_FAILED_TO_PICK_UP_S1_ADENA);
					sm.addNumber(item.getCount());
				}
				else
				{
					sm = new SystemMessage(SystemMessage.YOU_HAVE_FAILED_TO_PICK_UP_S1);
					sm.addItemName(item.getItemId());
				}
				sendPacket(sm);
				return;
			}

			// Herbs
			if(item.isHerb())
			{
				Skill[] skills = item.getTemplate().getAttachedSkills();
				if(skills.length > 0)
				{
					for(Skill skill : skills)
					{
						altUseSkill(skill, this);
					}
				}

				broadcastPacket(new GetItem(item, getObjectId()));
				item.deleteMe();
				return;
			}

			FlagItemAttachment attachment = item.getAttachment() instanceof FlagItemAttachment ? (FlagItemAttachment) item.getAttachment() : null;

			if(!isInParty() || attachment != null)
			{
				if(pickupItem(item, Log.Pickup))
				{
					broadcastPacket(new GetItem(item, getObjectId()));
					broadcastPickUpMsg(item);
					item.pickupMe();
				}
			}
			else
			{
				getParty().distributeItem(this, item, null);
			}
		}
	}

	public boolean pickupItem(ItemInstance item, String log)
	{
		PickableAttachment attachment = item.getAttachment() instanceof PickableAttachment ? (PickableAttachment) item.getAttachment() : null;

		if(!ItemFunctions.canAddItem(this, item))
			return false;

		if(item.getItemId() == ItemTemplate.ITEM_ID_ADENA || item.getItemId() == 6353)// FIXME [G1ta0] Ñ…Ð°Ñ€Ð´ÐºÐ¾Ð´
		{
			Quest q = QuestManager.getQuest(255);
			if(q != null)
			{
				processQuestEvent(q.getName(), "CE" + item.getItemId(), null);
			}
		}

		Log.LogItem(this, log, item);
		Log_New.LogEvent(getName(), getIP(), "PickUpItem", new String[] { "player pickup item " + log + ": " + getName() + " item: " + item.getCount() + " of " + item.getItemId() + "" });
		sendPacket(SystemMessage2.obtainItems(item));
		getInventory().addItem(item);

		if(attachment != null)
		{
			attachment.pickUp(this);
		}

		sendChanges();
		return true;
	}

	public void setObjectTarget(GameObject target)
	{
		setTarget(target);
		if(target == null)
			return;

		if(target == getTarget())
			if(target.isNpc())
			{
				NpcInstance npc = (NpcInstance) target;
				sendPacket(new MyTargetSelected(npc.getObjectId(), getLevel() - npc.getLevel()));
				sendPacket(npc.makeStatusUpdate(StatusUpdate.CUR_HP, StatusUpdate.MAX_HP));
				sendPacket(new ValidateLocation(npc), ActionFail.STATIC);
			}
			else
			{
				sendPacket(new MyTargetSelected(target.getObjectId(), 0));
			}
	}

	@Override
	public void setTarget(GameObject newTarget)
	{
		// Check if the new target is visible
		if(newTarget != null && !newTarget.isVisible())
		{
			newTarget = null;
		}

		// Can't target and attack festival monsters if not participant
		if(newTarget instanceof FestivalMonsterInstance && !isFestivalParticipant())
		{
			newTarget = null;
		}

		Party party = getParty();

		// Can't target and attack rift invaders if not in the same room
		if(party != null && party.isInDimensionalRift())
		{
			int riftType = party.getDimensionalRift().getType();
			int riftRoom = party.getDimensionalRift().getCurrentRoom();
			if(newTarget != null && !DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(newTarget.getX(), newTarget.getY(), newTarget.getZ()))
			{
				newTarget = null;
			}
		}

		GameObject oldTarget = getTarget();

		if(oldTarget != null)
		{
			if(oldTarget.equals(newTarget))
				return;

			// Remove the L2Player from the _statusListener of the old target if
			// it was a L2Character
			if(oldTarget.isCreature())
			{
				((Creature) oldTarget).removeStatusListener(this);
			}

			broadcastPacket(new TargetUnselected(this));
		}

		if(newTarget != null)
		{
			// Add the L2Player to the _statusListener of the new target if it's
			// a L2Character
			if(newTarget.isCreature())
			{
				((Creature) newTarget).addStatusListener(this);
			}

			broadcastPacket(new TargetSelected(getObjectId(), newTarget.getObjectId(), getLoc()));
		}

		if(Config.SERVICES_ALLOW_CHANGE_NICK_COLOR_TARGET)
		{
			Player player = getPlayer();

			String[] colors = Config.SERVICES_CHANGE_NICK_COLOR_TARGET; // Ð¦Ð²ÐµÑ‚Ð°.
			int id = Rnd.get(Config.SERVICES_CHANGE_NICK_COLOR_TARGET.length);
			player.setNameColor(Integer.decode("0x" + colors[id]));
			player.broadcastUserInfo(true);
		}

		super.setTarget(newTarget);
	}

	/**
	 * @return the active weapon instance (always equipped in the right hand).<BR>
	 * <BR>
	 */
	@Override
	public ItemInstance getActiveWeaponInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
	}

	/**
	 * @return the active weapon item (always equipped in the right hand).<BR>
	 * <BR>
	 */
	@Override
	public WeaponTemplate getActiveWeaponItem()
	{
		final ItemInstance weapon = getActiveWeaponInstance();

		if(weapon == null)
			return getFistsWeaponItem();

		return (WeaponTemplate) weapon.getTemplate();
	}

	/**
	 * @return the secondary weapon instance (always equipped in the left hand).<BR>
	 * <BR>
	 */
	@Override
	public ItemInstance getSecondaryWeaponInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
	}

	/**
	 * @return the secondary weapon item (always equipped in the left hand) or
	 *         the fists weapon.<BR>
	 * <BR>
	 */
	@Override
	public WeaponTemplate getSecondaryWeaponItem()
	{
		final ItemInstance weapon = getSecondaryWeaponInstance();

		if(weapon == null)
			return getFistsWeaponItem();

		final ItemTemplate item = weapon.getTemplate();

		if(item instanceof WeaponTemplate)
			return (WeaponTemplate) item;

		return null;
	}

	public boolean isWearingArmor(final ArmorType armorType)
	{
		final ItemInstance chest = getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);

		if(chest == null)
			return armorType == ArmorType.NONE;

		if(chest.getItemType() != armorType)
			return false;

		if(chest.getBodyPart() == ItemTemplate.SLOT_FULL_ARMOR)
			return true;

		final ItemInstance legs = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS);

		return legs == null ? armorType == ArmorType.NONE : legs.getItemType() == armorType;
	}

	@Override
	public void reduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflect, boolean transferDamage, boolean isDot, boolean sendMessage)
	{
		if(attacker == null || isDead() || attacker.isDead() && !isDot)
			return;

		// 5182 = Blessing of protection, Ñ€Ð°Ð±Ð¾Ñ‚Ð°ÐµÑ‚ ÐµÑ�Ð»Ð¸ Ñ€Ð°Ð·Ð½Ð¸Ñ†Ð° ÑƒÑ€Ð¾Ð²Ð½ÐµÐ¹ Ð±Ð¾Ð»ÑŒÑˆÐµ
		// 10 Ð¸ Ð½Ðµ Ð² Ð·Ð¾Ð½Ðµ Ð¾Ñ�Ð°Ð´Ñ‹
		if(attacker.isPlayer() && Math.abs(attacker.getLevel() - getLevel()) > 10)
		{
			// ÐŸÐš Ð½Ðµ Ð¼Ð¾Ð¶ÐµÑ‚ Ð½Ð°Ð½ÐµÑ�Ñ‚Ð¸ ÑƒÑ€Ð¾Ð½ Ñ‡Ð°Ñ€Ñƒ Ñ� Ð±Ð»ÐµÑ�Ñ�Ð¸Ð½Ð³Ð¾Ð¼
			if(attacker.getKarma() > 0 && getEffectList().getEffectsBySkillId(5182) != null && !isInZone(ZoneType.SIEGE))
				return;
			// Ñ‡Ð°Ñ€ Ñ� Ð±Ð»ÐµÑ�Ñ�Ð¸Ð½Ð³Ð¾Ð¼ Ð½Ðµ Ð¼Ð¾Ð¶ÐµÑ‚ Ð½Ð°Ð½ÐµÑ�Ñ‚Ð¸ ÑƒÑ€Ð¾Ð½ ÐŸÐš
			if(getKarma() > 0 && attacker.getEffectList().getEffectsBySkillId(5182) != null && !attacker.isInZone(ZoneType.SIEGE))
				return;
		}

		// Reduce the current HP of the L2Player
		super.reduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, canReflect, transferDamage, isDot, sendMessage);
	}

	@Override
	protected void onReduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp)
	{
		if(standUp)
		{
			standUp();
			if(isFakeDeath())
			{
				breakFakeDeath();
			}
		}

		if(attacker.isPlayable())
			if(!directHp && getCurrentCp() > 0)
			{
				double cp = getCurrentCp();
				if(cp >= damage)
				{
					cp -= damage;
					damage = 0;
				}
				else
				{
					damage -= cp;
					cp = 0;
				}

				setCurrentCp(cp);
			}

		double hp = getCurrentHp();

		DuelEvent duelEvent = getEvent(DuelEvent.class);
		if(duelEvent != null)
			if(hp - damage <= 1) // ÐµÑ�Ð»Ð¸ Ñ…Ð¿ <= 1 - ÑƒÐ±Ð¸Ñ‚
			{
				setCurrentHp(1, false);
				duelEvent.onDie(this);
				return;
			}

		if(isInOlympiadMode())
		{
			OlympiadGame game = _olympiadGame;
			if(this != attacker && (skill == null || skill.isOffensive()))
			{
				// Ð´Ð°Ð¼Ð°Ð³
				// Ð¾Ñ‚
				// Ð¿Ñ€Ð¾Ñ�Ñ‚Ñ‹Ñ…
				// ÑƒÐ´Ð°Ñ€Ð¾Ð²
				// Ð¸
				// Ð°Ñ‚Ð°ÐºÑƒÑŽÑ‰Ð¸Ñ…
				// Ñ�ÐºÐ¸Ð»Ð»Ð¾Ð²
				game.addDamage(this, Math.min(hp, damage));
			}

			if(hp - damage <= 1) // ÐµÑ�Ð»Ð¸ Ñ…Ð¿ <= 1 - ÑƒÐ±Ð¸Ñ‚
				if(game.getType() != CompType.TEAM)
				{
					game.setWinner(getOlympiadSide() == 1 ? 2 : 1);
					game.endGame(20000, false);
					setCurrentHp(1, false);
					attacker.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
					attacker.sendActionFailed();
					return;
				}
				else if(game.doDie(this)) // Ð’Ñ�Ðµ ÑƒÐ¼ÐµÑ€Ð»Ð¸
				{
					game.setWinner(getOlympiadSide() == 1 ? 2 : 1);
					game.endGame(20000, false);
				}
		}

		super.onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp);
	}

	private void altDeathPenalty(final Creature killer)
	{
		// Reduce the Experience of the L2Player in function of the calculated
		// Death Penalty
		if(!Config.ALT_GAME_DELEVEL)
			return;
		if(isInZoneBattle())
			return;
		if(getNevitSystem().isBlessingActive())
			return;
		deathPenalty(killer);
	}

	public final boolean atWarWith(final Player player)
	{
		return _clan != null && player.getClan() != null && getPledgeType() != -1 && player.getPledgeType() != -1 && _clan.isAtWarWith(player.getClan().getClanId());
	}

	public boolean atMutualWarWith(Player player)
	{
		return _clan != null && player.getClan() != null && getPledgeType() != -1 && player.getPledgeType() != -1 && _clan.isAtWarWith(player.getClan().getClanId()) && player.getClan().isAtWarWith(_clan.getClanId());
	}

	public final void doPurePk(final Player killer)
	{
		// Check if the attacker has a PK counter greater than 0
		final int pkCountMulti = Math.max(killer.getPkKills() / 2, 1);

		// Calculate the level difference Multiplier between attacker and killed
		// L2Player
		// final int lvlDiffMulti = Math.max(killer.getLevel() / _level, 1);

		// Calculate the new Karma of the attacker : newKarma =
		// baseKarma*pkCountMulti*lvlDiffMulti
		// Add karma to attacker and increase its PK counter
		killer.increaseKarma(Config.KARMA_MIN_KARMA * pkCountMulti); // *
		// lvlDiffMulti);
		killer.setPkKills(killer.getPkKills() + 1);
		if(Config.PK_SYSTEM_ENABLE)
			doPkPvPSys(killer, getPlayer(), "Pk");
	}

	public final void doKillInPeace(final Player killer) // Check if the
	// L2Player killed
	// haven't Karma
	{
		if(_karma <= 0)
		{
			doPurePk(killer);
		}
		else
		{
			killer.setPvpKills(killer.getPvpKills() + 1);
		}
	}

	public void checkAddItemToDrop(List<ItemInstance> array, List<ItemInstance> items, int maxCount)
	{
		for(int i = 0; i < maxCount && !items.isEmpty(); i++)
		{
			array.add(items.remove(Rnd.get(items.size())));
		}
	}

	public FlagItemAttachment getActiveWeaponFlagAttachment()
	{
		ItemInstance item = getActiveWeaponInstance();
		if(item == null || !(item.getAttachment() instanceof FlagItemAttachment))
			return null;
		return (FlagItemAttachment) item.getAttachment();
	}

	protected void doPKPVPManage(Creature killer)
	{
		FlagItemAttachment attachment = getActiveWeaponFlagAttachment();
		if(attachment != null)
		{
			attachment.onDeath(this, killer);
		}

		if(killer == null || killer == _summon || killer == this)
			return;

		if(isInZoneBattle() || killer.isInZoneBattle())
			return;

		if(killer instanceof Summon && (killer = killer.getPlayer()) == null)
			return;

		// Processing Karma/PKCount/PvPCount for killer
		if(killer.isPlayer())
		{
			final Player pk = (Player) killer;
			final int repValue = getLevel() - pk.getLevel() >= 20 ? 2 : 1;
			boolean war = atMutualWarWith(pk);

			// TODO [VISTALL] fix it
			if(war /*
							* || _clan.getSiege() != null && _clan.getSiege() ==
							* pk.getClan().getSiege() && (_clan.isDefender() &&
							* pk.getClan().isAttacker() || _clan.isAttacker() &&
							* pk.getClan().isDefender())
							*/)
				if(pk.getClan().getReputationScore() > 0 && _clan.getLevel() >= 5 && _clan.getReputationScore() > 0 && pk.getClan().getLevel() >= 5)
				{
					_clan.broadcastToOtherOnlineMembers(new SystemMessage(SystemMessage.YOUR_CLAN_MEMBER_S1_WAS_KILLED_S2_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_CLAN_REPUTATION_SCORE_AND_ADDED_TO_YOUR_OPPONENT_CLAN_REPUTATION_SCORE).addString(getName()).addNumber(-_clan.incReputation(-repValue, true, "ClanWar")), this);
					pk.getClan().broadcastToOtherOnlineMembers(new SystemMessage(SystemMessage.FOR_KILLING_AN_OPPOSING_CLAN_MEMBER_S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_OPPONENTS_CLAN_REPUTATION_SCORE).addNumber(pk.getClan().incReputation(repValue, true, "ClanWar")), pk);
				}

			if(isOnSiegeField())
				return;

			if(_pvpFlag > 0 || war)
			{
				pk.setPvpKills(pk.getPvpKills() + 1);
				if(Config.PvP_SYSTEM_ENABLE)
					doPkPvPSys(pk, getPlayer(), "PvP");

			}
			else
			{
				doKillInPeace(pk);
			}

			pk.sendChanges();
		}

		int karma = _karma;
		decreaseKarma(Config.KARMA_LOST_BASE);

		// Ð² Ð½Ð¾Ñ€Ð¼Ð°Ð»ÑŒÐ½Ñ‹Ñ… ÑƒÑ�Ð»Ð¾Ð²Ð¸Ñ�Ñ… Ð²ÐµÑ‰Ð¸ Ñ‚ÐµÑ€Ñ�ÑŽÑ‚Ñ�Ñ� Ñ‚Ð¾Ð»ÑŒÐºÐ¾ Ð¿Ñ€Ð¸ Ñ�Ð¼ÐµÑ€Ñ‚Ð¸ Ð¾Ñ‚ Ð³Ð²Ð°Ñ€Ð´Ð° Ð¸Ð»Ð¸
		// Ð¸Ð³Ñ€Ð¾ÐºÐ°
		// ÐºÑ€Ð¾Ð¼Ðµ Ñ‚Ð¾Ð³Ð¾, Ð°Ð»ÑŒÑ‚ Ð½Ð° Ð¿Ð¾Ñ‚ÐµÑ€ÑŽ Ð²ÐµÑ‰ÐµÐ¹ Ð¿Ñ€Ð¸ Ñ�Ð¼ÐµÑ‚Ñ€Ð¸ Ð¿Ð¾Ð·Ð²Ð¾Ð»Ñ�ÐµÑ‚ Ñ‚ÐµÑ€Ñ�Ñ‚ÑŒ Ð²ÐµÑ‰Ð¸ Ð¿Ñ€Ð¸
		// Ñ�Ð¼Ñ‚ÐµÑ€Ð¸ Ð¾Ñ‚ Ð¼Ð¾Ð½Ñ�Ñ‚Ñ€Ð°
		boolean isPvP = killer.isPlayable() || killer instanceof GuardInstance;

		if(killer.isMonster() && !Config.DROP_ITEMS_ON_DIE // ÐµÑ�Ð»Ð¸ ÑƒÐ±Ð¸Ð» Ð¼Ð¾Ð½Ñ�Ñ‚Ñ€
				// Ð¸ Ð°Ð»ÑŒÑ‚ Ð²Ñ‹ÐºÐ»ÑŽÑ‡ÐµÐ½
				|| isPvP // ÐµÑ�Ð»Ð¸ ÑƒÐ±Ð¸Ð» Ð¸Ð³Ñ€Ð¾Ðº Ð¸Ð»Ð¸ Ð³Ð²Ð°Ñ€Ð´ Ð¸
				&& (_pkKills < Config.MIN_PK_TO_ITEMS_DROP // ÐºÐ¾Ð»Ð¸Ñ‡ÐµÑ�Ñ‚Ð²Ð¾ Ð¿Ðº
				// Ñ�Ð»Ð¸ÑˆÐºÐ¾Ð¼ Ð¼Ð°Ð»Ð¾
				|| karma == 0 && Config.KARMA_NEEDED_TO_DROP) // ÐºÐ°Ñ€Ð¼Ñ‹ Ð½ÐµÑ‚
				|| isFestivalParticipant() // Ð² Ñ„ÐµÑ�Ñ‚Ð¸Ð²Ð°Ð»Ðµ Ð²ÐµÑ‰Ð¸ Ð½Ðµ Ñ‚ÐµÑ€Ñ�ÑŽÑ‚Ñ�Ñ�
				|| !killer.isMonster() && !isPvP) // Ð² Ð¿Ñ€Ð¾Ñ‡Ð¸Ñ… Ñ�Ð»ÑƒÑ‡Ð°Ñ�Ñ… Ñ‚Ð¾Ð¶Ðµ
			return;

		// No drop from GM's
		if(!Config.KARMA_DROP_GM && isGM())
			return;

		final int max_drop_count = isPvP ? Config.KARMA_DROP_ITEM_LIMIT : 1;

		double dropRate; // Ð±Ð°Ð·Ð¾Ð²Ñ‹Ð¹ ÑˆÐ°Ð½Ñ� Ð² Ð¿Ñ€Ð¾Ñ†ÐµÐ½Ñ‚Ð°Ñ…
		if(isPvP)
		{
			dropRate = _pkKills * Config.KARMA_DROPCHANCE_MOD + Config.KARMA_DROPCHANCE_BASE;
		}
		else
		{
			dropRate = Config.NORMAL_DROPCHANCE_BASE;
		}

		int dropEquipCount = 0, dropWeaponCount = 0, dropItemCount = 0;

		for(int i = 0; i < Math.ceil(dropRate / 100) && i < max_drop_count; i++)
			if(Rnd.chance(dropRate))
			{
				int rand = Rnd.get(Config.DROPCHANCE_EQUIPPED_WEAPON + Config.DROPCHANCE_EQUIPMENT + Config.DROPCHANCE_ITEM) + 1;
				if(rand > Config.DROPCHANCE_EQUIPPED_WEAPON + Config.DROPCHANCE_EQUIPMENT)
				{
					dropItemCount++;
				}
				else if(rand > Config.DROPCHANCE_EQUIPPED_WEAPON)
				{
					dropEquipCount++;
				}
				else
				{
					dropWeaponCount++;
				}
			}

		List<ItemInstance> drop = new LazyArrayList<ItemInstance>(), // Ð¾Ð±Ñ‰Ð¸Ð¹
		// Ð¼Ð°Ñ�Ñ�Ð¸Ð²
		// Ñ�
		// Ñ€ÐµÐ·ÑƒÐ»ÑŒÑ‚Ð°Ñ‚Ð°Ð¼Ð¸
		// Ð²Ñ‹Ð±Ð¾Ñ€Ð°
		dropItem = new LazyArrayList<ItemInstance>(), dropEquip = new LazyArrayList<ItemInstance>(), dropWeapon = new LazyArrayList<ItemInstance>(); // Ð²Ñ€ÐµÐ¼ÐµÐ½Ð½Ñ‹Ðµ

		getInventory().writeLock();
		try
		{
			for(ItemInstance item : getInventory().getItems())
			{
				if(!item.canBeDropped(this, true) || Config.KARMA_LIST_NONDROPPABLE_ITEMS.contains(item.getItemId()))
				{
					continue;
				}

				if(item.getTemplate().getType2() == ItemTemplate.TYPE2_WEAPON)
				{
					dropWeapon.add(item);
				}
				else if(item.getTemplate().getType2() == ItemTemplate.TYPE2_SHIELD_ARMOR || item.getTemplate().getType2() == ItemTemplate.TYPE2_ACCESSORY)
				{
					dropEquip.add(item);
				}
				else if(item.getTemplate().getType2() == ItemTemplate.TYPE2_OTHER)
				{
					dropItem.add(item);
				}
			}

			checkAddItemToDrop(drop, dropWeapon, dropWeaponCount);
			checkAddItemToDrop(drop, dropEquip, dropEquipCount);
			checkAddItemToDrop(drop, dropItem, dropItemCount);

			// Dropping items, if present
			if(drop.isEmpty())
				return;

			for(ItemInstance item : drop)
			{
				if(item.isAugmented() && !Config.ALT_ALLOW_DROP_AUGMENTED)
				{
					item.setAugmentationId(0);
				}

				item = getInventory().removeItem(item);
				Log.LogItem(this, Log.PvPDrop, item);
				Log_New.LogEvent(getName(), getIP(), "PickUpItem", new String[] { "player pvp drop item: " + getName() + " dropped item: " + item.getCount() + " of " + item.getItemId() + "" });
				if(item.getEnchantLevel() > 0)
				{
					sendPacket(new SystemMessage(SystemMessage.DROPPED__S1_S2).addNumber(item.getEnchantLevel()).addItemName(item.getItemId()));
				}
				else
				{
					sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_DROPPED_S1).addItemName(item.getItemId()));
				}

				if(killer.isPlayable() && (Config.ALT_AUTO_LOOT && Config.ALT_AUTO_LOOT_PK || isInFlyingTransform()))
				{
					killer.getPlayer().getInventory().addItem(item);
					Log.LogItem(this, Log.Pickup, item);
					Log_New.LogEvent(killer.getPlayer().getName(), killer.getPlayer().getIP(), "PickUpItem", new String[] { "player pickup item from pk: " + killer.getPlayer().getName() + " item: " + item.getCount() + " of " + item.getItemId() + "" });
					killer.getPlayer().sendPacket(SystemMessage2.obtainItems(item));
				}
				else
				{
					item.dropToTheGround(this, Location.findAroundPosition(this, Config.KARMA_RANDOM_DROP_LOCATION_LIMIT));
				}
			}
		}
		finally
		{
			getInventory().writeUnlock();
		}
	}

	private void doPkPvPSys(Player one, Player two, String type)
	{
		if(one.getNetConnection().getIpAddr().equals(two.getNetConnection().getIpAddr()))
			return;

		if(one.getNetConnection().getHWID().equals(two.getNetConnection().getHWID()))
			return;

		if((one.getClan() != null || two.getClan() != null) && one.getClan() == two.getClan())
			return;

		if(type.equals("PvP"))
		{
			if(one.getLevel() < Config.PvP_SYSTEM_KILLER_LEVEL_FOR_REWARD[0] || two.getLevel() < Config.PvP_SYSTEM_TARGET_LEVEL_FOR_REWARD[0])
				return;

			if(one.getLevel() > Config.PvP_SYSTEM_KILLER_LEVEL_FOR_REWARD[1] || two.getLevel() > Config.PvP_SYSTEM_TARGET_LEVEL_FOR_REWARD[1])
				return;

			if(Config.PvP_SYSTEM_IN_ZONE && (!one.isInZone(Config.PvP_SYSTEM_ZONE) || !two.isInZone(Config.PvP_SYSTEM_ZONE)))
				return;

			boolean time = Config.PvP_SYSTEM_ENABLE_BLOCK_TIME ? !one.getVarB("PvPSystemTime") : true;

			if(!time)
				return;

			// Ð’Ñ‹Ð´Ð°Ñ‡Ð° Ð¾Ð¿Ñ‹Ñ‚Ð° Ð¸ Ñ�ÐºÑ�Ð¿Ñ‹
			if(Config.PvP_SYSTEM_ADD_EXP_SP)
				one.addExpAndSp(Config.PvP_SYSTEM_EXP_SP[0], Config.PvP_SYSTEM_EXP_SP[1]);

			// Ð’Ñ‹Ð´Ð°Ñ‡Ð° Ð¸Ñ‚ÐµÐ¼Ð°.
			if(Config.PvP_SYSTEM_ALLOW_REWARD && Rnd.chance(Config.PvP_SYSTEM_ITEM_INFO[2] * one.getRateItems()))
			{
				one.getInventory().addItem(Config.PvP_SYSTEM_ITEM_INFO[0], Config.PvP_SYSTEM_ITEM_INFO[1]);
				one.sendMessage(new CustomMessage("common.pk.pvp.kill.reward", one).addString(two.getName()).addItemName(Config.PvP_SYSTEM_ITEM_INFO[0]).addString(Config.PvP_SYSTEM_ITEM_INFO[1] + " " + DifferentMethods.declension(one, Config.PvP_SYSTEM_ITEM_INFO[1], "Piece")));

				if(Config.PvP_SYSTEM_ENABLE_BLOCK_TIME)
				{
					long MINUTES = DifferentMethods.addMinutes(Config.PvP_SYSTEM_BLOCK_TIME_AFTER_KILL);
					long TIME = System.currentTimeMillis() + MINUTES;
					try
					{
						one.getPlayer().setVar("PvPSystemTime", TIME, TIME);
					}
					catch(Exception e)
					{
						one.sendMessage(new CustomMessage("common.Error", one));
					}
				}
			}

			if(Config.PvP_SYSTEM_ANNOUNCE && one != null && !one.isGM() && getPvpFlag() != 0)
			{
				for(Creature player : one.getAroundCharacters(Config.PvP_SYSTEM_ANNOUNCE_RADIUS, 500))
					player.sendPacket(new ExShowScreenMessage(new CustomMessage("common.pvp.kill", player.getPlayer()).addString(one.getName()).addString(getName()).toString(), 3000, ScreenMessageAlign.TOP_CENTER, true));

				one.sendPacket(new ExShowScreenMessage(new CustomMessage("common.pvp.youkill", one).addString(two.getName()).toString(), 3000, ScreenMessageAlign.TOP_CENTER, true));
			}
		}

		if(type.equals("Pk"))
		{
			if(one.getLevel() < Config.PK_SYSTEM_KILLER_LEVEL_FOR_REWARD[0] || two.getLevel() < Config.PK_SYSTEM_TARGET_LEVEL_FOR_REWARD[0])
				return;

			if(one.getLevel() > Config.PK_SYSTEM_KILLER_LEVEL_FOR_REWARD[1] || two.getLevel() > Config.PK_SYSTEM_TARGET_LEVEL_FOR_REWARD[1])
				return;

			if(Config.PK_SYSTEM_IN_ZONE && (!one.isInZone(Config.PK_SYSTEM_ZONE) || !two.isInZone(Config.PK_SYSTEM_ZONE)))
				return;

			boolean time = Config.PK_SYSTEM_ENABLE_BLOCK_TIME ? !one.getVarB("PKSystemTime") : true;

			if(!time)
				return;

			// Ð’Ñ‹Ð´Ð°Ñ‡Ð° Ð¾Ð¿Ñ‹Ñ‚Ð° Ð¸ Ñ�ÐºÑ�Ð¿Ñ‹
			if(Config.PK_SYSTEM_ADD_EXP_SP)
				one.addExpAndSp(Config.PK_SYSTEM_EXP_SP[0], Config.PK_SYSTEM_EXP_SP[1]);

			// Ð’Ñ‹Ð´Ð°Ñ‡Ð° Ð¸Ñ‚ÐµÐ¼Ð°.
			if(Config.PK_SYSTEM_ALLOW_REWARD && Rnd.chance(Config.PK_SYSTEM_ITEM_INFO[2] * one.getRateItems()))
			{
				one.getInventory().addItem(Config.PK_SYSTEM_ITEM_INFO[0], Config.PK_SYSTEM_ITEM_INFO[1]);
				one.sendMessage(new CustomMessage("common.pk.pvp.kill.reward", one).addString(two.getName()).addItemName(Config.PK_SYSTEM_ITEM_INFO[0]).addString(Config.PK_SYSTEM_ITEM_INFO[1] + " " + DifferentMethods.declension(one, Config.PK_SYSTEM_ITEM_INFO[1], "Piece")));

				if(Config.PK_SYSTEM_ENABLE_BLOCK_TIME)
				{
					long MINUTES = DifferentMethods.addMinutes(Config.PK_SYSTEM_BLOCK_TIME_AFTER_KILL);
					long TIME = System.currentTimeMillis() + MINUTES;
					try
					{
						one.getPlayer().setVar("PKSystemTime", TIME, TIME);
					}
					catch(Exception e)
					{
						one.sendMessage(new CustomMessage("common.Error", one));
					}
				}
			}

			if(Config.PK_SYSTEM_ANNOUNCE && one != null && !one.isGM() && getPvpFlag() == 0)
			{
				for(Creature player : one.getAroundCharacters(Config.PK_SYSTEM_ANNOUNCE_RADIUS, 500))
					player.sendPacket(new ExShowScreenMessage(new CustomMessage("common.pk.kill", player.getPlayer()).addString(one.getName()).addString(getName()).toString(), 3000, ScreenMessageAlign.TOP_CENTER, true));

				one.sendPacket(new ExShowScreenMessage(new CustomMessage("common.pk.youkill", one).addString(two.getName()).toString(), 3000, ScreenMessageAlign.TOP_CENTER, true));
			}
		}
	}

	@Override
	protected void onDeath(Creature killer)
	{
		Player player = getPlayer();
		//Check for active charm of luck for death penalty
		if(!player.isPhantom())
		getDeathPenalty().checkCharmOfLuck();

		if(isInStoreMode())
		{
			setPrivateStoreType(Player.STORE_PRIVATE_NONE);
		}
		if(isProcessingRequest())
		{
			Request request = getRequest();
			if(isInTrade())
			{
				Player parthner = request.getOtherPlayer(this);
				sendPacket(SendTradeDone.FAIL);
				parthner.sendPacket(SendTradeDone.FAIL);
			}
			request.cancel();
		}

		setAgathion(0);

		boolean checkPvp = true;
		if(Config.ALLOW_CURSED_WEAPONS)
			if(isCursedWeaponEquipped())
			{
				CursedWeaponsManager.getInstance().dropPlayer(this);
				checkPvp = false;
			}
			else if(killer != null && killer.isPlayer() && killer.isCursedWeaponEquipped())
			{
				CursedWeaponsManager.getInstance().increaseKills(((Player) killer).getCursedWeaponEquippedId());
				checkPvp = false;
			}

		if(checkPvp)
		{
			doPKPVPManage(killer);

			altDeathPenalty(killer);
		}

		// And in the end of process notify death penalty that owner died :)
		if(!player.isPhantom())
		getDeathPenalty().notifyDead(killer);

		setIncreasedForce(0);

		if(isInParty() && getParty().isInReflection() && getParty().getReflection() instanceof DimensionalRift)
		{
			((DimensionalRift) getParty().getReflection()).memberDead(this);
		}

		stopWaterTask();

		if(!isSalvation() && isOnSiegeField() && isCharmOfCourage())
		{
			ask(new ConfirmDlg(SystemMsg.YOUR_CHARM_OF_COURAGE_IS_TRYING_TO_RESURRECT_YOU, 60000), new ReviveAnswerListener(this, 100, false));
			setCharmOfCourage(false);
		}

		if(getLevel() < 6)
		{
			Quest q = QuestManager.getQuest(255);
			if(q != null)
			{
				processQuestEvent(q.getName(), "CE30", null);
			}
		}

		super.onDeath(killer);
	}

	public void restoreExp()
	{
		restoreExp(100.);
	}

	public void restoreExp(double percent)
	{
		if(percent == 0)
			return;

		int lostexp = 0;

		String lostexps = getVar("lostexp");
		if(lostexps != null)
		{
			lostexp = Integer.parseInt(lostexps);
			unsetVar("lostexp");
		}

		if(lostexp != 0)
		{
			addExpAndSp((long) (lostexp * percent / 100), 0);
		}
	}

	public void deathPenalty(Creature killer)
	{
		if(killer == null)
			return;
		if(Config.ALLOW_PHANTOM_PLAYERS)
		{
			Player player = getPlayer();
			if(player.isPhantom())
				return;
		}
		final boolean atwar = killer.getPlayer() != null && atWarWith(killer.getPlayer());

		double deathPenaltyBonus = getDeathPenalty().getLevel() * Config.ALT_DEATH_PENALTY_C5_EXPERIENCE_PENALTY;
		if(deathPenaltyBonus < 2)
		{
			deathPenaltyBonus = 1;
		}
		else
		{
			deathPenaltyBonus = deathPenaltyBonus / 2;
		}

		// The death steal you some Exp: 10-40 lvl 8% loose
		double percentLost = 8.0;

		int level = getLevel();
		if(level >= 79)
		{
			percentLost = 1.0;
		}
		else if(level >= 78)
		{
			percentLost = 1.5;
		}
		else if(level >= 76)
		{
			percentLost = 2.0;
		}
		else if(level >= 40)
		{
			percentLost = 4.0;
		}

		if(Config.ALT_DEATH_PENALTY)
		{
			percentLost = percentLost * Config.RATE_XP + _pkKills * Config.ALT_PK_DEATH_RATE;
		}

		if(isFestivalParticipant() || atwar)
		{
			percentLost = percentLost / 4.0;
		}

		// Calculate the Experience loss
		int lostexp = (int) Math.round((Experience.LEVEL[level + 1] - Experience.LEVEL[level]) * percentLost / 100);
		lostexp *= deathPenaltyBonus;

		lostexp = (int) calcStat(Stats.EXP_LOST, lostexp, killer, null);

		// Ð�Ð° Ð·Ð°Ñ€ÐµÐ³Ð¸Ñ�Ñ‚Ñ€Ð¸Ñ€Ð¾Ð²Ð°Ð½Ð½Ð¾Ð¹ Ð¾Ñ�Ð°Ð´Ðµ Ð½ÐµÑ‚ Ð¿Ð¾Ñ‚ÐµÑ€Ð¸ Ð¾Ð¿Ñ‹Ñ‚Ð°, Ð½Ð° Ñ‡ÑƒÐ¶Ð¾Ð¹ Ð¾Ñ�Ð°Ð´Ðµ - ÐºÐ°Ðº
		// Ð¿Ñ€Ð¸ Ð¾Ð±Ñ‹Ñ‡Ð½Ð¾Ð¹ Ñ�Ð¼ÐµÑ€Ñ‚Ð¸ Ð¾Ñ‚ *Ð¼Ð¾Ð±Ð°*
		if(isOnSiegeField())
		{
			SiegeEvent<?, ?> siegeEvent = getEvent(SiegeEvent.class);
			if(siegeEvent != null)
			{
				lostexp = 0;
			}

			if(siegeEvent != null)
			{
				List<Effect> effect = getEffectList().getEffectsBySkillId(Skill.SKILL_BATTLEFIELD_DEATH_SYNDROME);
				if(effect != null)
				{
					int syndromeLvl = effect.get(0).getSkill().getLevel();
					if(syndromeLvl < 5)
					{
						getEffectList().stopEffect(Skill.SKILL_BATTLEFIELD_DEATH_SYNDROME);
						Skill skill = SkillTable.getInstance().getInfo(Skill.SKILL_BATTLEFIELD_DEATH_SYNDROME, syndromeLvl + 1);
						skill.getEffects(this, this, false, false);
					}
					else if(syndromeLvl == 5)
					{
						getEffectList().stopEffect(Skill.SKILL_BATTLEFIELD_DEATH_SYNDROME);
						Skill skill = SkillTable.getInstance().getInfo(Skill.SKILL_BATTLEFIELD_DEATH_SYNDROME, 5);
						skill.getEffects(this, this, false, false);
					}
				}
				else
				{
					Skill skill = SkillTable.getInstance().getInfo(Skill.SKILL_BATTLEFIELD_DEATH_SYNDROME, 1);
					if(skill != null)
					{
						skill.getEffects(this, this, false, false);
					}
				}
			}
		}

		long before = getExp();
		addExpAndSp(-lostexp, 0);
		long lost = before - getExp();

		if(lost > 0)
		{
			setVar("lostexp", String.valueOf(lost), -1);
		}
	}

	public void setRequest(Request transaction)
	{
		_request = transaction;
	}

	public Request getRequest()
	{
		return _request;
	}

	/**
	 * ÐŸÑ€Ð¾Ð²ÐµÑ€ÐºÐ°, Ð·Ð°Ð½Ñ�Ñ‚ Ð»Ð¸ Ð¸Ð³Ñ€Ð¾Ðº Ð´Ð»Ñ� Ð¾Ñ‚Ð²ÐµÑ‚Ð° Ð½Ð° Ð·Ð°Ñ€Ð¾Ñ�
	 * 
	 * @return true, ÐµÑ�Ð»Ð¸ Ð¸Ð³Ñ€Ð¾Ðº Ð½Ðµ Ð¼Ð¾Ð¶ÐµÑ‚ Ð¾Ñ‚Ð²ÐµÑ‚Ð¸Ñ‚ÑŒ Ð½Ð° Ð·Ð°Ð¿Ñ€Ð¾Ñ�
	 */
	public boolean isBusy()
	{
		return isProcessingRequest() || isOutOfControl() || isInOlympiadMode() || getTeam() != TeamType.NONE || isInStoreMode() || isInDuel() || getMessageRefusal() || isBlockAll() || isInvisible();
	}

	public boolean isProcessingRequest()
	{
		if(_request == null)
			return false;
		if(!_request.isInProgress())
			return false;
		return true;
	}

	public boolean isInTrade()
	{
		return isProcessingRequest() && getRequest().isTypeOf(L2RequestType.TRADE);
	}

	public List<L2GameServerPacket> addVisibleObject(GameObject object, Creature dropper)
	{
		if(isLogoutStarted() || object == null || object.getObjectId() == getObjectId() || !object.isVisible())
			return Collections.emptyList();

		return object.addPacketList(this, dropper);
	}

	@Override
	public List<L2GameServerPacket> addPacketList(Player forPlayer, Creature dropper)
	{
		if(isInvisible() && forPlayer.getObjectId() != getObjectId())
			return Collections.emptyList();

		if(getPrivateStoreType() != STORE_PRIVATE_NONE && !isInBuffStore()  && forPlayer.getVarB("notraders"))
			return Collections.emptyList();

		if(isInObserverMode() && getCurrentRegion() != getObserverRegion() && getObserverRegion() == forPlayer.getCurrentRegion())
			return Collections.emptyList();

		List<L2GameServerPacket> list = new ArrayList<L2GameServerPacket>();
		if(forPlayer.getObjectId() != getObjectId())
		{
			list.add(isPolymorphed() ? new NpcInfoPoly(this) : new CharInfo(this));
		}

		list.add(new ExBR_ExtraUserInfo(this));

		if(isSitting() && _sittingObject != null)
		{
			list.add(new ChairSit(this, _sittingObject));
		}

		if(getPrivateStoreType() != STORE_PRIVATE_NONE)
		{
			if(getPrivateStoreType() == STORE_PRIVATE_BUY)
			{
				list.add(new PrivateStoreMsgBuy(this));
			}
			else if(getPrivateStoreType() == STORE_PRIVATE_SELL || getPrivateStoreType() == STORE_PRIVATE_SELL_PACKAGE)
			{
				list.add(new PrivateStoreMsgSell(this));
			}
			else if(getPrivateStoreType() == STORE_PRIVATE_MANUFACTURE)
			{
				list.add(new RecipeShopMsg(this));
			}
			if(forPlayer.isInZonePeace())
				return list;
		}

		if(isCastingNow())
		{
			Creature castingTarget = getCastingTarget();
			Skill castingSkill = getCastingSkill();
			long animationEndTime = getAnimationEndTime();
			if(castingSkill != null && castingTarget != null && castingTarget.isCreature() && getAnimationEndTime() > 0)
			{
				list.add(new MagicSkillUse(this, castingTarget, castingSkill.getId(), castingSkill.getLevel(), (int) (animationEndTime - System.currentTimeMillis()), 0));
			}
		}

		if(isInCombat())
		{
			list.add(new AutoAttackStart(getObjectId()));
		}

		list.add(RelationChanged.update(forPlayer, this, forPlayer));
		DominionSiegeEvent dominionSiegeEvent = getEvent(DominionSiegeEvent.class);
		if(dominionSiegeEvent != null)
		{
			list.add(new ExDominionWarStart(this));
		}

		if(isInBoat())
		{
			list.add(getBoat().getOnPacket(this, getInBoatPosition()));
		}
		else if(isMoving || isFollow)
		{
			list.add(movePacket());
		}
		return list;
	}

	public List<L2GameServerPacket> removeVisibleObject(GameObject object, List<L2GameServerPacket> list)
	{
		if(isLogoutStarted() || object == null || object.getObjectId() == getObjectId()) // FIXME ||
			// isTeleporting()
			return null;

		List<L2GameServerPacket> result = list == null ? object.deletePacketList() : list;

		getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, object);
		return result;
	}

	private void levelSet(int levels)
	{
		if(levels > 0)
		{
			sendPacket(Msg.YOU_HAVE_INCREASED_YOUR_LEVEL);
			broadcastPacket(new SocialAction(getObjectId(), SocialAction.LEVEL_UP));

			setCurrentHpMp(getMaxHp(), getMaxMp());
			setCurrentCp(getMaxCp());

			Quest q = QuestManager.getQuest(255);
			if(q != null)
			{
				processQuestEvent(q.getName(), "CE40", null);
			}

			for(int i = 0; i < Config.ALT_SHOW_LEVEL_UP_PAGES.length; i++)
				if(getLevel() == Config.ALT_SHOW_LEVEL_UP_PAGES[i])
				{
					sendPacket(new NpcHtmlMessage(5).setFile("levelup/level-" + Config.ALT_SHOW_LEVEL_UP_PAGES[i] + ".htm").replace("%servername%", Config.SERVER_NAME));
				}
		}
		else if(levels < 0)
			if(Config.ALT_REMOVE_SKILLS_ON_DELEVEL)
			{
				checkSkills();
			}

		// Recalculate the party level
		if(isInParty())
		{
			getParty().recalculatePartyData();
		}

		if(_clan != null)
			_clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(this));

		if(_matchingRoom != null)
		{
			_matchingRoom.broadcastPlayerUpdate(this);
		}

		// Give Expertise skill of this level
		rewardSkills(true);
	}

	/**
	 * Ð£Ð´Ð°Ð»Ñ�ÐµÑ‚ Ð²Ñ�Ðµ Ñ�ÐºÐ¸Ð»Ð»Ñ‹, ÐºÐ¾Ñ‚Ð¾Ñ€Ñ‹Ðµ ÑƒÑ‡Ð°Ñ‚Ñ�Ñ� Ð½Ð° ÑƒÑ€Ð¾Ð²Ð½Ðµ Ð±Ð¾Ð»ÑŒÑˆÐµÐ¼, Ñ‡ÐµÐ¼ Ñ‚ÐµÐºÑƒÑ‰Ð¸Ð¹+maxDiff
	 */
	public void checkSkills()
	{
		for(Skill sk : getAllSkillsArray())
		{
			SkillTreeTable.checkSkill(this, sk);
		}
	}

	public void startTimers()
	{
		startAutoSaveTask();
		startPcBangPointsTask();
		startBonusTask();
		getInventory().startTimers();
		resumeQuestTimers();
	}

	public void stopAllTimers()
	{
		setAgathion(0);
		stopWaterTask();
		stopBonusTask();
		stopHourlyTask();
		stopKickTask();
		stopVitalityTask();
		stopPcBangPointsTask();
		stopAutoSaveTask();
		stopRecomBonusTask(true);
		getInventory().stopAllTimers();
		stopQuestTimers();
		getNevitSystem().stopTasksOnLogout();
	}

	@Override
	public Summon getPet()
	{
		return _summon;
	}

	public void setPet(Summon summon)
	{
		boolean isPet = false;
		if(_summon != null && _summon.isPet())
		{
			isPet = true;
		}
		unsetVar("pet");
		_summon = summon;
		autoShot();
		if(summon == null)
		{
			if(isPet)
			{
				if(isLogoutStarted())
					if(getPetControlItem() != null)
					{
						setVar("pet", String.valueOf(getPetControlItem().getObjectId()), -1);
					}
				setPetControlItem(null);
			}
			getEffectList().stopEffect(4140);
		}
	}

	public void scheduleDelete()
	{
		long time = 0L;

		if(Config.SERVICES_ENABLE_NO_CARRIER)
		{
			time = NumberUtils.toInt(getVar("noCarrier"), Config.SERVICES_NO_CARRIER_DEFAULT_TIME);
			setNonAggroTime(System.currentTimeMillis() + time * 1000L);
			setIsInvul(true);
		}

		scheduleDelete(time * 1000L);
	}

	/**
	 * Ð£Ð´Ð°Ð»Ð¸Ñ‚ Ð¿ÐµÑ€Ñ�Ð¾Ð½Ð°Ð¶Ð° Ð¸Ð· Ð¼Ð¸Ñ€Ð° Ñ‡ÐµÑ€ÐµÐ· ÑƒÐºÐ°Ð·Ð°Ð½Ð½Ð¾Ðµ Ð²Ñ€ÐµÐ¼Ñ�, ÐµÑ�Ð»Ð¸ Ð½Ð° Ð¼Ð¾Ð¼ÐµÐ½Ñ‚ Ð¸Ñ�Ñ‚ÐµÑ‡ÐµÐ½Ð¸Ñ�
	 * Ð²Ñ€ÐµÐ¼ÐµÐ½Ð¸ Ð¾Ð½ Ð½Ðµ Ð±ÑƒÐ´ÐµÑ‚ Ð¿Ñ€Ð¸Ñ�Ð¾ÐµÐ´Ð¸Ð½ÐµÐ½.
	 * 
	 * TODO: Ñ‡ÐµÑ€ÐµÐ· Ð¼Ð¸Ð½ÑƒÑ‚Ñƒ Ð´ÐµÐ»Ð°Ñ‚ÑŒ ÐµÐ³Ð¾ Ð½ÐµÑƒÑ�Ð·Ð²Ð¸Ð¼Ñ‹Ð¼. TODO: Ñ�Ð´ÐµÐ»Ð°Ñ‚ÑŒ Ð¿Ñ€Ð¸Ð²Ñ�Ð·ÐºÑƒ Ð²Ñ€ÐµÐ¼ÐµÐ½Ð¸
	 * Ðº ÐºÐ¾Ð½Ñ‚ÐµÐºÑ�Ñ‚Ñƒ, Ð´Ð»Ñ� Ð·Ð¾Ð½ Ñ� Ð»Ð¸Ð¼Ð¸Ñ‚Ð¾Ð¼ Ð²Ñ€ÐµÐ¼ÐµÐ½Ð¸ Ð¾Ñ�Ñ‚Ð°Ð²Ð»Ñ�Ñ‚ÑŒ Ð² Ð¸Ð³Ñ€Ðµ Ð½Ð° Ð²Ñ�Ðµ Ð²Ñ€ÐµÐ¼Ñ� Ð²
	 * Ð·Ð¾Ð½Ðµ.
	 * 
	 * @param time
	 *            Ð²Ñ€ÐµÐ¼Ñ� Ð² Ð¼Ð¸Ð»Ð»Ð¸Ñ�ÐµÐºÑƒÐ½Ð´Ð°Ñ…
	 */
	public void scheduleDelete(long time)
	{
		if(isLogoutStarted() || isInOfflineMode())
			return;

		broadcastCharInfo();

		ThreadPoolManager.getInstance().schedule(new RunnableImpl(){
			@Override
			public void runImpl() throws Exception
			{
				if(!isConnected())
				{
					prepareToLogout();
					deleteMe();
				}
			}
		}, time);
	}

	@Override
	protected void onDelete()
	{
		super.onDelete();

		// Ð£Ð±Ð¸Ñ€Ð°ÐµÐ¼ Ñ„Ñ�Ð¹Ðº Ð² Ñ‚Ð¾Ñ‡ÐºÐµ Ð½Ð°Ð±Ð»ÑŽÐ´ÐµÐ½Ð¸Ñ�
		WorldRegion observerRegion = getObserverRegion();
		if(observerRegion != null)
		{
			observerRegion.removeObject(this);
		}

		// Send friendlists to friends that this player has logged off
		_friendList.notifyFriends(false);

		bookmarks.clear();

		_inventory.clear();
		_warehouse.clear();
		_summon = null;
		_arrowItem = null;
		_fistsWeaponItem = null;
		_chars = null;
		_enchantScroll = null;
		_lastNpc = HardReferences.emptyRef();
		_observerRegion = null;
	}

	public void setTradeList(List<TradeItem> list)
	{
		_tradeList = list;
	}

	public List<TradeItem> getTradeList()
	{
		return _tradeList;
	}

	public String getSellStoreName()
	{
		return _sellStoreName;
	}

	public void setSellStoreName(String name)
	{
		_sellStoreName = Strings.stripToSingleLine(name);
	}

	public void setSellList(boolean packageSell, List<TradeItem> list)
	{
		if(packageSell)
		{
			_packageSellList = list;
		}
		else
		{
			_sellList = list;
		}
	}

	public List<TradeItem> getSellList()
	{
		return getSellList(_privatestore == STORE_PRIVATE_SELL_PACKAGE);
	}

	public List<TradeItem> getSellList(boolean packageSell)
	{
		return packageSell ? _packageSellList : _sellList;
	}

	public String getBuyStoreName()
	{
		return _buyStoreName;
	}

	public void setBuyStoreName(String name)
	{
		_buyStoreName = Strings.stripToSingleLine(name);
	}

	public void setBuyList(List<TradeItem> list)
	{
		_buyList = list;
	}

	public List<TradeItem> getBuyList()
	{
		return _buyList;
	}

	public void setManufactureName(String name)
	{
		_manufactureName = Strings.stripToSingleLine(name);
	}

	public String getManufactureName()
	{
		return _manufactureName;
	}

	public List<ManufactureItem> getCreateList()
	{
		return _createList;
	}

	public void setCreateList(List<ManufactureItem> list)
	{
		_createList = list;
	}

	public void setPrivateStoreType(final int type)
	{
		_privatestore = type;
		if(type != STORE_PRIVATE_NONE)
		{
			setVar("storemode", String.valueOf(type), -1);
		}
		else
		{
			
			unsetVar("storemode");
		}
	}

	public boolean isInStoreMode()
	{
		return _privatestore != STORE_PRIVATE_NONE;
	}
	public boolean isInBuffStore()
	{
		return (getPrivateStoreType() == STORE_PRIVATE_BUFF);
	}

	public int getPrivateStoreType()
	{
		return _privatestore;
	}

	/**
	 * Set the _clan object, _clanId, _clanLeader Flag and title of the
	 * L2Player.<BR>
	 * <BR>
	 * 
	 * @param clan
	 *            the clat to set
	 */
	public void setClan(Clan clan)
	{
		if(_clan != clan && _clan != null)
		{
			unsetVar("canWhWithdraw");
		}

		Clan oldClan = _clan;
		if(oldClan != null && clan == null)
		{
			for(Skill skill : oldClan.getAllSkills())
			{
				removeSkill(skill, false);
			}
		}

		_clan = clan;

		if(clan == null)
		{
			_pledgeType = Clan.SUBUNIT_NONE;
			_pledgeClass = 0;
			_powerGrade = 0;
			_apprentice = 0;
			getInventory().validateItems();
			return;
		}

		if(!clan.isAnyMember(getObjectId()))
		{
			setClan(null);
			if(!isNoble())
			{
				setTitle("");
			}
		}
	}

	@Override
	public Clan getClan()
	{
		return _clan;
	}

	public SubUnit getSubUnit()
	{
		return _clan == null ? null : _clan.getSubUnit(_pledgeType);
	}

	public ClanHall getClanHall()
	{
		int id = _clan != null ? _clan.getHasHideout() : 0;
		return ResidenceHolder.getInstance().getResidence(ClanHall.class, id);
	}

	public Castle getCastle()
	{
		int id = _clan != null ? _clan.getCastle() : 0;
		return ResidenceHolder.getInstance().getResidence(Castle.class, id);
	}

	public Fortress getFortress()
	{
		int id = _clan != null ? _clan.getHasFortress() : 0;
		return ResidenceHolder.getInstance().getResidence(Fortress.class, id);
	}

	public Alliance getAlliance()
	{
		return _clan == null ? null : _clan.getAlliance();
	}

	public boolean isClanLeader()
	{
		return _clan != null && getObjectId() == _clan.getLeaderId();
	}

	public boolean isAllyLeader()
	{
		return getAlliance() != null && getAlliance().getLeader().getLeaderId() == getObjectId();
	}

	@Override
	public void reduceArrowCount()
	{
		sendPacket(SystemMsg.YOU_CAREFULLY_NOCK_AN_ARROW);
		if(!getInventory().destroyItemByObjectId(getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LHAND), 1L))
		{
			getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, null);
			_arrowItem = null;
		}
	}
	/**
	 * Equip arrows needed in left hand and send a Server->Client packet
	 * ItemList to the L2Player then return True.
	 */
	protected boolean checkAndEquipArrows()
	{
		// Check if nothing is equipped in left hand
		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND) == null)
		{
			ItemInstance activeWeapon = getActiveWeaponInstance();
			if(activeWeapon != null)
				if(activeWeapon.getItemType() == WeaponType.BOW)
				{
					_arrowItem = getInventory().findArrowForBow(activeWeapon.getTemplate());
				}
				else if(activeWeapon.getItemType() == WeaponType.CROSSBOW)
				{
					_arrowItem = getInventory().findArrowForCrossbow(activeWeapon.getTemplate());
				}

			// Equip arrows needed in left hand
			if(_arrowItem != null)
			{
				getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, _arrowItem);
			}
		}
		else
		{
			// Get the L2ItemInstance of arrows equipped in left hand
			_arrowItem = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		}

		return _arrowItem != null;
	}

	public void setUptime(final long time)
	{
		_uptime = time;
	}

	public long getUptime()
	{
		return System.currentTimeMillis() - _uptime;
	}

	public boolean isInParty()
	{
		return _party != null;
	}

	public void setParty(final Party party)
	{
		_party = party;
	}

	public void joinParty(final Party party)
	{
		if(party != null)
		{
			party.addPartyMember(this);
		}
	}

	public void leaveParty()
	{
		if(isInParty())
		{
			_party.removePartyMember(this, false);
		}
	}

	public Party getParty()
	{
		return _party;
	}

	public void setLastPartyPosition(Location loc)
	{
		_lastPartyPosition = loc;
	}

	public Location getLastPartyPosition()
	{
		return _lastPartyPosition;
	}

	public boolean isGM()
	{
		return _playerAccess == null ? false : _playerAccess.IsGM;
	}

	/**
	 * Ð�Ð¸Ð³Ð´Ðµ Ð½Ðµ Ð¸Ñ�Ð¿Ð¾Ð»ÑŒÐ·ÑƒÐµÑ‚Ñ�Ñ�, Ð½Ð¾ Ð¼Ð¾Ð¶ÐµÑ‚ Ð¿Ñ€Ð¸Ð³Ð¾Ð´Ð¸Ñ‚ÑŒÑ�Ñ� Ð´Ð»Ñ� Ð‘Ð”
	 */
	public void setAccessLevel(final int level)
	{
		_accessLevel = level;
	}

	/**
	 * Ð�Ð¸Ð³Ð´Ðµ Ð½Ðµ Ð¸Ñ�Ð¿Ð¾Ð»ÑŒÐ·ÑƒÐµÑ‚Ñ�Ñ�, Ð½Ð¾ Ð¼Ð¾Ð¶ÐµÑ‚ Ð¿Ñ€Ð¸Ð³Ð¾Ð´Ð¸Ñ‚ÑŒÑ�Ñ� Ð´Ð»Ñ� Ð‘Ð”
	 */
	@Override
	public int getAccessLevel()
	{
		return _accessLevel;
	}

	public void setPlayerAccess(final PlayerAccess pa)
	{
		if(pa != null)
		{
			_playerAccess = pa;
		}
		else
		{
			_playerAccess = new PlayerAccess();
		}

		setAccessLevel(isGM() || _playerAccess.Menu ? 100 : 0);
	}

	public PlayerAccess getPlayerAccess()
	{
		return _playerAccess;
	}

	@Override
	public double getLevelMod()
	{
		return (89. + getLevel()) / 100.0;
	}

	/**
	 * Update Stats of the L2Player client side by sending Server->Client packet
	 * UserInfo/StatusUpdate to this L2Player and CharInfo/StatusUpdate to all
	 * players around (broadcast).<BR>
	 * <BR>
	 */
	@Override
	public void updateStats()
	{
		if(entering || isLogoutStarted())
			return;

		refreshOverloaded();
		refreshExpertisePenalty();
		super.updateStats();
	}

	@Override
	public void sendChanges()
	{
		if(entering || isLogoutStarted())
			return;
		super.sendChanges();
	}

	/**
	 * Send a Server->Client StatusUpdate packet with Karma to the L2Player and
	 * all L2Player to inform (broadcast).
	 */
	public void updateKarma(boolean flagChanged)
	{
		sendStatusUpdate(true, true, StatusUpdate.KARMA);
		if(flagChanged)
		{
			broadcastRelationChanged();
		}
	}

	public boolean isOrdered()
	{
		return _isOrdered;
	}

	public void setIsOrdered(boolean isOrdered)
	{
		if(isOrdered)
		{
			_log.info("TRUE");
		}
		else
		{
			_log.info("FALSE");
		}
		_isOrdered = isOrdered;
		broadcastUserInfo(true);
	}

	public boolean isOnline()
	{
		return _isOnline;
	}

	public void setIsOnline(boolean isOnline)
	{
		_isOnline = isOnline;
	}

	public void setOnlineStatus(boolean isOnline)
	{
		_isOnline = isOnline;
		updateOnlineStatus();
	}

	public void updateOnlineStatus()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE characters SET online=?, lastAccess=? WHERE obj_id=?");
			statement.setInt(1, isOnline() && !isInOfflineMode() ? 1 : 0);
			statement.setLong(2, System.currentTimeMillis() / 1000L);
			statement.setInt(3, getObjectId());
			statement.execute();
		}
		catch(final Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	/**
	 * Decrease Karma of the L2Player and Send it StatusUpdate packet with Karma
	 * and PvP Flag (broadcast).
	 */
	public void increaseKarma(final long add_karma)
	{
		boolean flagChanged = _karma == 0;
		long new_karma = _karma + add_karma;

		if(new_karma > Integer.MAX_VALUE)
		{
			new_karma = Integer.MAX_VALUE;
		}

		if(_karma == 0 && new_karma > 0)
		{
			if(_pvpFlag > 0)
			{
				_pvpFlag = 0;
				if(_PvPRegTask != null)
				{
					_PvPRegTask.cancel(true);
					_PvPRegTask = null;
				}
				sendStatusUpdate(true, true, StatusUpdate.PVP_FLAG);
			}

			_karma = (int) new_karma;
		}
		else
		{
			_karma = (int) new_karma;
		}

		updateKarma(flagChanged);
	}

	/**
	 * Decrease Karma of the L2Player and Send it StatusUpdate packet with Karma
	 * and PvP Flag (broadcast).
	 */
	public void decreaseKarma(final int i)
	{
		boolean flagChanged = _karma > 0;
		_karma -= i;
		if(_karma <= 0)
		{
			_karma = 0;
			updateKarma(flagChanged);
		}
		else
		{
			updateKarma(false);
		}
	}

	/**
	 * Create a new L2Player and add it in the characters table of the database.<BR>
	 * <BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Create a new L2Player with an account name</li>
	 * <li>Set the name, the Hair Style, the Hair Color and the Face type of the
	 * L2Player</li>
	 * <li>Add the player in the characters table of the database</li><BR>
	 * <BR>
	 * 
	 * @param accountName
	 *            The name of the L2Player
	 * @param name
	 *            The name of the L2Player
	 * @param hairStyle
	 *            The hair style Identifier of the L2Player
	 * @param hairColor
	 *            The hair color Identifier of the L2Player
	 * @param face
	 *            The face type Identifier of the L2Player
	 * @return The L2Player added to the database or null
	 */
	public static Player create(int classId, int sex, String accountName, final String name, final int hairStyle, final int hairColor, final int face)
	{
		PlayerTemplate template = CharTemplateHolder.getInstance().getTemplate(classId, sex != 0);

		// Create a new L2Player with an account name
		Player player = new Player(IdFactory.getInstance().getNextId(), template, accountName);

		player.setName(name);
		player.setTitle("");
		player.setHairStyle(hairStyle);
		player.setHairColor(hairColor);
		player.setFace(face);
		player.setCreateTime(System.currentTimeMillis());

		// Add the player in the characters table of the database
		if(!CharacterDAO.getInstance().insert(player))
			return null;

		return player;
	}

	/**
	 * Retrieve a L2Player from the characters table of the database and add it
	 * in _allObjects of the L2World
	 * 
	 * @return The L2Player loaded from the database
	 */
	public static Player restore(final int objectId)
	{
		Player player = null;
		Connection con = null;
		Statement statement = null;
		Statement statement2 = null;
		PreparedStatement statement3 = null;
		ResultSet rset = null;
		ResultSet rset2 = null;
		ResultSet rset3 = null;
		try
		{
			// Retrieve the L2Player from the characters table of the database
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			statement2 = con.createStatement();
			rset = statement.executeQuery("SELECT * FROM `characters` WHERE `obj_Id`=" + objectId + " LIMIT 1");
			rset2 = statement2.executeQuery("SELECT `class_id` FROM `character_subclasses` WHERE `char_obj_id`=" + objectId + " AND `isBase`=1 LIMIT 1");

			if(rset.next() && rset2.next())
			{
				final int classId = rset2.getInt("class_id");
				final boolean female = rset.getInt("sex") == 1;
				final PlayerTemplate template = CharTemplateHolder.getInstance().getTemplate(classId, female);

				player = new Player(objectId, template);
				player.setIsPhantom(false);
				player.loadVariables();
				player.loadInstanceReuses();
				player.loadPremiumItemList();
				player.bookmarks.setCapacity(rset.getInt("bookmarks"));
				player.bookmarks.restore();
				player._friendList.restore();
				player._postFriends = CharacterPostFriendDAO.getInstance().select(player);
				CharacterGroupReuseDAO.getInstance().select(player);

				player.setBaseClass(classId);
				player._login = rset.getString("account_name");
				player.setName(rset.getString("char_name"));

				player.setFace(rset.getInt("face"));
				player.setHairStyle(rset.getInt("hairStyle"));
				player.setHairColor(rset.getInt("hairColor"));
				player.setHeading(0);

				player.setKarma(rset.getInt("karma"));
				player.setPvpKills(rset.getInt("pvpkills"));
				player.setPkKills(rset.getInt("pkkills"));
				player.setLeaveClanTime(rset.getLong("leaveclan") * 1000L);
				if(player.getLeaveClanTime() > 0 && player.canJoinClan())
				{
					player.setLeaveClanTime(0);
				}
				player.setDeleteClanTime(rset.getLong("deleteclan") * 1000L);
				if(player.getDeleteClanTime() > 0 && player.canCreateClan())
				{
					player.setDeleteClanTime(0);
				}

				player.setNoChannel(rset.getLong("nochannel") * 1000L);
				if(player.getNoChannel() > 0 && player.getNoChannelRemained() < 0)
				{
					player.setNoChannel(0);
				}

				player.setOnlineTime(rset.getLong("onlinetime") * 1000L);

				final int clanId = rset.getInt("clanid");
				if(clanId > 0)
				{
					player.setClan(ClanTable.getInstance().getClan(clanId));
					player.setPledgeType(rset.getInt("pledge_type"));
					player.setPowerGrade(rset.getInt("pledge_rank"));
					player.setLvlJoinedAcademy(rset.getInt("lvl_joined_academy"));
					player.setApprentice(rset.getInt("apprentice"));
				}

				player.setCreateTime(rset.getLong("createtime") * 1000L);
				player.setDeleteTimer(rset.getInt("deletetime"));

				player.setTitle(rset.getString("title"));

				if(player.getVar("titlecolor") != null)
				{
					player.setTitleColor(Integer.decode("0x" + player.getVar("titlecolor")));
				}

				if(player.getVar("namecolor") == null)
					if(player.isGM())
					{
						player.setNameColor(Config.GM_NAME_COLOUR);
					}
					else if(player.getClan() != null && player.getClan().getLeaderId() == player.getObjectId())
					{
						player.setNameColor(Config.CLANLEADER_NAME_COLOUR);
					}
					else if(player.hasBonus() && player.getClan().getLeaderId() != player.getObjectId())
					{
						player.setNameColor(Config.PREMIUM_NAME_COLOUR);
					}
					else
					{
						player.setNameColor(Config.NORMAL_NAME_COLOUR);
					}
				else
				{
					player.setNameColor(Integer.decode("0x" + player.getVar("namecolor")));
				}

				if(Config.ALT_AUTO_LOOT_INDIVIDUAL)
				{
					player._autoLoot = player.getVarB("AutoLoot", Config.ALT_AUTO_LOOT);
					player.AutoLootHerbs = player.getVarB("AutoLootHerbs", Config.ALT_AUTO_LOOT_HERBS);
				}

				player.setFistsWeaponItem(player.findFistsWeaponItem(classId));
				player.setUptime(System.currentTimeMillis());
				player.setLastAccess(rset.getLong("lastAccess"));

				player.setRecomHave(rset.getInt("rec_have"));
				player.setRecomLeft(rset.getInt("rec_left"));
				player.setRecomBonusTime(rset.getInt("rec_bonus_time"));

				if(player.getVar("recLeftToday") != null)
				{
					player.setRecomLeftToday(Integer.parseInt(player.getVar("recLeftToday")));
				}
				else
				{
					player.setRecomLeftToday(0);
				}

				player.getNevitSystem().setPoints(rset.getInt("hunt_points"), rset.getInt("hunt_time"));

				player.setKeyBindings(rset.getBytes("key_bindings"));
				player.setPcBangPoints(rset.getInt("pcBangPoints"));

				player.setFame(rset.getInt("fame"), null);

				player.restoreRecipeBook();

				if(Config.OLYMPIAD_ENABLE)
				{
					player.setHero(Hero.getInstance().isHero(player.getObjectId()));
					player.setNoble(Olympiad.isNoble(player.getObjectId()));
				}

				player.updatePledgeClass();

				int reflection = 0;

				if(player.getVar("jailed") != null && System.currentTimeMillis() / 1000 < Integer.parseInt(player.getVar("jailed")) + 60)
				{
					player.setXYZ(-114648, -249384, -2984);
					player.sitDown(null);
					player.block();
					player._unjailTask = ThreadPoolManager.getInstance().schedule(new UnJailTask(player), Integer.parseInt(player.getVar("jailed")) * 1000L);
				}
				else
				{
					player.setXYZ(rset.getInt("x"), rset.getInt("y"), rset.getInt("z"));
					String ref = player.getVar("reflection");
					if(ref != null)
					{
						reflection = Integer.parseInt(ref);
						if(reflection > 0) // Ð½Ðµ Ð¿Ð¾Ñ€Ñ‚Ð°ÐµÐ¼ Ð½Ð°Ð·Ð°Ð´ Ð¸Ð· Ð“Ð¥, Ð¿Ð°Ñ€Ð½Ð°Ñ�Ð°,
						// Ð´Ð¶Ð°Ð¹Ð»Ð°
						{
							String back = player.getVar("backCoords");
							if(back != null)
							{
								player.setLoc(Location.parseLoc(back));
								player.unsetVar("backCoords");
							}
							reflection = 0;
						}
					}
				}

				player.setReflection(reflection);

				EventHolder.getInstance().findEvent(player);

				// TODO [G1ta0] Ð·Ð°Ð¿ÑƒÑ�ÐºÐ°Ñ‚ÑŒ Ð½Ð° Ð²Ñ…Ð¾Ð´Ðµ
				Quest.restoreQuestStates(player);

				player.getInventory().restore();

				restoreCharSubClasses(player);

				// 4 Ð¾Ñ‡ÐºÐ° Ð² Ð¼Ð¸Ð½ÑƒÑ‚Ñƒ Ð¾Ñ„Ñ„Ð»Ð°Ð¹Ð½Ð°
				player.setVitality(rset.getInt("vitality") + (int) ((System.currentTimeMillis() / 1000L - rset.getLong("lastAccess")) / 15.));
				try
				{
					String var = player.getVar("ExpandInventory");
					if(var != null)
					{
						player.setExpandInventory(Integer.parseInt(var));
					}
				}
				catch(Exception e)
				{
					_log.error("", e);
				}

				try
				{
					String var = player.getVar("ExpandWarehouse");
					if(var != null)
					{
						player.setExpandWarehouse(Integer.parseInt(var));
					}
				}
				catch(Exception e)
				{
					_log.error("", e);
				}

				try
				{
					String var = player.getVar(NO_ANIMATION_OF_CAST_VAR);
					if(var != null)
					{
						player.setNotShowBuffAnim(Boolean.parseBoolean(var));
					}
				}
				catch(Exception e)
				{
					_log.error("", e);
				}

				try
				{
					String var = player.getVar(NO_TRADERS_VAR);
					if(var != null)
					{
						player.setNotShowTraders(Boolean.parseBoolean(var));
					}
				}
				catch(Exception e)
				{
					_log.error("", e);
				}

				try
				{
					String var = player.getVar("pet");
					if(var != null)
					{
						player.setPetControlItem(Integer.parseInt(var));
					}
				}
				catch(Exception e)
				{
					_log.error("", e);
				}

				statement3 = con.prepareStatement("SELECT obj_Id, char_name FROM characters WHERE account_name=? AND obj_Id!=?");
				statement3.setString(1, player._login);
				statement3.setInt(2, objectId);
				rset3 = statement3.executeQuery();
				while(rset3.next())
				{
					final Integer charId = rset3.getInt("obj_Id");
					final String charName = rset3.getString("char_name");
					player._chars.put(charId, charName);
				}

				DbUtils.close(statement3, rset3);

				// if(!player.isGM())
				{
					LazyArrayList<Zone> zones = LazyArrayList.newInstance();

					World.getZones(zones, player.getLoc(), player.getReflection());

					if(!zones.isEmpty())
					{
						for(Zone zone : zones)
							if(zone.getType() == ZoneType.no_restart)
							{
								if(System.currentTimeMillis() / 1000L - player.getLastAccess() > zone.getRestartTime())
								{
									player.sendMessage(new CustomMessage("jts.gameserver.network.clientpackets.EnterWorld.TeleportedReasonNoRestart", player));
									player.setLoc(TeleportUtils.getRestartLocation(player, RestartType.TO_VILLAGE));
								}
							}
							else if(zone.getType() == ZoneType.SIEGE)
							{
								SiegeEvent<?, ?> siegeEvent = player.getEvent(SiegeEvent.class);
								if(siegeEvent != null)
								{
									player.setLoc(siegeEvent.getEnterLoc(player));
								}
								else
								{
									Residence r = ResidenceHolder.getInstance().getResidence(zone.getParams().getInteger("residence"));
									player.setLoc(r.getNotOwnerRestartPoint(player));
								}
							}
					}

					LazyArrayList.recycle(zones);

					if(DimensionalRiftManager.getInstance().checkIfInRiftZone(player.getLoc(), false))
					{
						player.setLoc(DimensionalRiftManager.getInstance().getRoom(0, 0).getTeleportCoords());
					}
				}

				player.restoreBlockList();
				player._macroses.restore();

				// FIXME [VISTALL] Ð½ÑƒÐ¶Ð½Ð¾ Ð»Ð¸?
				player.refreshExpertisePenalty();
				player.refreshOverloaded();

				player.getWarehouse().restore();
				player.getFreight().restore();

				player.restoreTradeList();
				if(player.getVar("storemode") != null)
				{
					player.setPrivateStoreType(Integer.parseInt(player.getVar("storemode")));
					player.setSitting(true);
				}
                try 
                {
                    String var = player.getVar("EnItemOlyRec");
                    if (Config.OLY_ENCH_LIMIT_ENABLE && var != null)
                    {
                        FixEnchantOlympiad.restoreEnchantItemsOly(player);
                    }
                } 
                catch (Exception e)
                {
                    _log.error("", e);
                }
				player.updateKetraVarka();
				player.updateRam();
				player.checkRecom();
			}
			
		}
		catch(final Exception e)
		{
			_log.error("Could not restore char data!", e);
		}
		finally
		{
			DbUtils.closeQuietly(statement2, rset2);
			DbUtils.closeQuietly(statement3, rset3);
			DbUtils.closeQuietly(con, statement, rset);
		}
		return player;
	}
	public static Player restorePhantom(final int objectId, int setLevel, int classi, boolean boll)
	{
		Player player = null;
		Connection con = null;
		Statement statement = null;
		ResultSet rset = null;
		try
		{
			// Retrieve the L2Player from the characters table of the database
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			rset = statement.executeQuery("SELECT * FROM `characters` WHERE `obj_Id`=" + objectId + " LIMIT 1");
			if(rset.next())
			{
				final int classId = _classIdprefetch[Rnd.get(_classIdprefetch.length)];
				final boolean female = rset.getInt("sex") == 1;
				final PlayerTemplate template = CharTemplateHolder.getInstance().getTemplate(classId, female);

				player = new Player(objectId, template);
				player.setIsPhantom(true);
			//	player.setLevel(setLevel);
				player.bookmarks.setCapacity(rset.getInt("bookmarks"));
				player.bookmarks.restore();
				CharacterGroupReuseDAO.getInstance().select(player);
				player.setName(rset.getString("char_name"));
				player.setTitle("");
				player.setHairStyle(rset.getInt("hairStyle"));
				player.setHairColor(rset.getInt("hairColor"));
				player.setFace(rset.getInt("face"));
				player.setCreateTime(System.currentTimeMillis());
				player.setBaseClass(classId);
				player._login = rset.getString("account_name");
				player.setReflection(ReflectionManager.DEFAULT);
			}
		}
		catch(final Exception e)
		{
			_log.error("Could not restore char data!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return player;
	}
	private void loadPremiumItemList()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT itemNum, itemId, itemCount, itemSender FROM character_premium_items WHERE charId=?");
			statement.setInt(1, getObjectId());
			rs = statement.executeQuery();
			while(rs.next())
			{
				int itemNum = rs.getInt("itemNum");
				int itemId = rs.getInt("itemId");
				long itemCount = rs.getLong("itemCount");
				String itemSender = rs.getString("itemSender");
				PremiumItem item = new PremiumItem(itemId, itemCount, itemSender);
				_premiumItems.put(itemNum, item);
			}
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rs);
		}
	}

	public void updatePremiumItem(int itemNum, long newcount)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE character_premium_items SET itemCount=? WHERE charId=? AND itemNum=?");
			statement.setLong(1, newcount);
			statement.setInt(2, getObjectId());
			statement.setInt(3, itemNum);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void deletePremiumItem(int itemNum)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM character_premium_items WHERE charId=? AND itemNum=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, itemNum);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public Map<Integer, PremiumItem> getPremiumItemList()
	{
		return _premiumItems;
	}

	/**
	 * Update L2Player stats in the characters table of the database.
	 */
	public void store(boolean fast)
	{
		if(!_storeLock.tryLock())
			return;
		Player player = getPlayer();
		if(player.isPhantom())
			return;

		try
		{
			Connection con = null;
			PreparedStatement statement = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement(//
				"UPDATE characters SET face=?,hairStyle=?,hairColor=?,x=?,y=?,z=?" + //
				",karma=?,pvpkills=?,pkkills=?,rec_have=?,rec_left=?,rec_bonus_time=?,hunt_points=?,hunt_time=?,clanid=?,deletetime=?," + //
				"title=?,accesslevel=?,online=?,leaveclan=?,deleteclan=?,nochannel=?," + //
				"onlinetime=?,pledge_type=?,pledge_rank=?,lvl_joined_academy=?,apprentice=?,key_bindings=?,pcBangPoints=?,char_name=?,vitality=?,fame=?,bookmarks=? WHERE obj_Id=? LIMIT 1");
				statement.setInt(1, getFace());
				statement.setInt(2, getHairStyle());
				statement.setInt(3, getHairColor());
				if(_stablePoint == null) // ÐµÑ�Ð»Ð¸ Ð¸Ð³Ñ€Ð¾Ðº Ð½Ð°Ñ…Ð¾Ð´Ð¸Ñ‚Ñ�Ñ� Ð² Ñ‚Ð¾Ñ‡ÐºÐµ Ð²
				// ÐºÐ¾Ñ‚Ð¾Ñ€Ð¾Ð¹ ÐµÐ³Ð¾ Ñ�Ð¾Ñ…Ñ€Ð°Ð½Ñ�Ñ‚ÑŒ Ð½Ðµ Ñ�Ñ‚Ð¾Ð¸Ñ‚
				// (Ð½Ð°Ð¿Ñ€Ð¸Ð¼ÐµÑ€ Ð½Ð° Ð²Ð¸Ð²ÐµÑ€Ð½Ðµ) Ñ‚Ð¾
				// Ñ�Ð¾Ñ…Ñ€Ð°Ð½Ñ�ÑŽÑ‚Ñ�Ñ� Ð¿Ð¾Ñ�Ð»ÐµÐ´Ð½Ð¸Ðµ ÐºÐ¾Ð¾Ñ€Ð´Ð¸Ð½Ð°Ñ‚Ñ‹
				{
					statement.setInt(4, getX());
					statement.setInt(5, getY());
					statement.setInt(6, getZ());
				}
				else
				{
					statement.setInt(4, _stablePoint.x);
					statement.setInt(5, _stablePoint.y);
					statement.setInt(6, _stablePoint.z);
				}
				statement.setInt(7, getKarma());
				statement.setInt(8, getPvpKills());
				statement.setInt(9, getPkKills());
				statement.setInt(10, getRecomHave());
				statement.setInt(11, getRecomLeft());
				statement.setInt(12, getRecomBonusTime());
				statement.setInt(13, getNevitSystem().getPoints());
				statement.setInt(14, getNevitSystem().getTime());
				statement.setInt(15, getClanId());
				statement.setInt(16, getDeleteTimer());
				statement.setString(17, _title);
				statement.setInt(18, _accessLevel);
				statement.setInt(19, isOnline() && !isInOfflineMode() ? 1 : 0);
				statement.setLong(20, getLeaveClanTime() / 1000L);
				statement.setLong(21, getDeleteClanTime() / 1000L);
				statement.setLong(22, _NoChannel > 0 ? getNoChannelRemained() / 1000 : _NoChannel);
				statement.setInt(23, (int) (_onlineBeginTime > 0 ? (_onlineTime + System.currentTimeMillis() - _onlineBeginTime) / 1000L : _onlineTime / 1000L));
				statement.setInt(24, getPledgeType());
				statement.setInt(25, getPowerGrade());
				statement.setInt(26, getLvlJoinedAcademy());
				statement.setInt(27, getApprentice());
				statement.setBytes(28, getKeyBindings());
				statement.setInt(29, getPcBangPoints());
				statement.setString(30, getName());
				statement.setInt(31, (int) getVitality());
				statement.setInt(32, getFame());
				statement.setInt(33, bookmarks.getCapacity());
				statement.setInt(34, getObjectId());


				statement.executeUpdate();
				GameStats.increaseUpdatePlayerBase();

				if(!fast)
				{
					EffectsDAO.getInstance().insert(this);
					CharacterGroupReuseDAO.getInstance().insert(this);
					storeDisableSkills();
					storeBlockList();
				}

				storeCharSubClasses();
				bookmarks.store();
			}
			catch(Exception e)
			{
				_log.error("Could not store char data: " + this + "!", e);
			}
			finally
			{
				DbUtils.closeQuietly(con, statement);
			}
		}
		finally
		{
			_storeLock.unlock();
		}
	}

	/**
	 * Add a skill to the L2Player _skills and its Func objects to the
	 * calculator set of the L2Player and save update in the character_skills
	 * table of the database.
	 * 
	 * @return The L2Skill replaced or null if just added a new L2Skill
	 */
	public Skill addSkill(final Skill newSkill, final boolean store)
	{
		if(newSkill == null)
			return null;

		// Add a skill to the L2Player _skills and its Func objects to the
		// calculator set of the L2Player
		Skill oldSkill = super.addSkill(newSkill);

		if(newSkill.equals(oldSkill))
			return oldSkill;

		// Add or update a L2Player skill in the character_skills table of the
		// database
		if(store)
		{
			storeSkill(newSkill, oldSkill);
		}

		return oldSkill;
	}

	public Skill removeSkill(Skill skill, boolean fromDB)
	{
		if(skill == null)
			return null;
		return removeSkill(skill.getId(), fromDB);
	}

	/**
	 * Remove a skill from the L2Character and its Func objects from calculator
	 * set of the L2Character and save update in the character_skills table of
	 * the database.
	 * 
	 * @return The L2Skill removed
	 */
	public Skill removeSkill(int id, boolean fromDB)
	{
		// Remove a skill from the L2Character and its Func objects from
		// calculator set of the L2Character
		Skill oldSkill = super.removeSkillById(id);

		if(!fromDB)
			return oldSkill;

		if(oldSkill != null)
		{
			Connection con = null;
			PreparedStatement statement = null;
			try
			{
				// Remove or update a L2Player skill from the character_skills
				// table of the database
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("DELETE FROM character_skills WHERE skill_id=? AND char_obj_id=? AND class_index=?");
				statement.setInt(1, oldSkill.getId());
				statement.setInt(2, getObjectId());
				statement.setInt(3, getActiveClassId());
				statement.execute();
			}
			catch(final Exception e)
			{
				_log.error("Could not delete skill!", e);
			}
			finally
			{
				DbUtils.closeQuietly(con, statement);
			}
		}

		return oldSkill;
	}

	/**
	 * Add or update a L2Player skill in the character_skills table of the
	 * database.
	 */
	private void storeSkill(final Skill newSkill, final Skill oldSkill)
	{
		if(newSkill == null) // Ð²Ð¾Ð¾Ð±Ñ‰Ðµ-Ñ‚Ð¾ Ð½ÐµÐ²Ð¾Ð·Ð¼Ð¾Ð¶Ð½Ð¾
		{
			_log.warn("could not store new skill. its NULL");
			return;
		}

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("REPLACE INTO character_skills (char_obj_id,skill_id,skill_level,class_index) values(?,?,?,?)");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newSkill.getId());
			statement.setInt(3, newSkill.getLevel());
			statement.setInt(4, getActiveClassId());
			statement.execute();
		}
		catch(final Exception e)
		{
			_log.error("Error could not store skills!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	/**
	 * Retrieve from the database all skills of this L2Player and add them to
	 * _skills.
	 */
	private void restoreSkills()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			// Retrieve all skills of this L2Player from the database
			// Send the SQL query : SELECT skill_id,skill_level FROM
			// character_skills WHERE char_obj_id=? to the database
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT skill_id,skill_level FROM character_skills WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, getActiveClassId());
			rset = statement.executeQuery();

			// Go though the recordset of this SQL query
			while(rset.next())
			{
				final int id = rset.getInt("skill_id");
				final int level = rset.getInt("skill_level");

				// Create a L2Skill object for each record
				final Skill skill = SkillTable.getInstance().getInfo(id, level);

				if(skill == null)
				{
					continue;
				}

				// Remove skill if not possible
				if(!isGM() && !SkillAcquireHolder.getInstance().isSkillPossible(this, skill))
				{
					// int ReturnSP =
					// SkillTreeTable.getInstance().getSkillCost(this, skill);
					// if(ReturnSP == Integer.MAX_VALUE || ReturnSP < 0)
					// ReturnSP = 0;
					removeSkill(skill, true);
					removeSkillFromShortCut(skill.getId());
					// if(ReturnSP > 0)
					// setSp(getSp() + ReturnSP);
					// TODO audit
					continue;
				}

				super.addSkill(skill);
			}

			// Restore noble skills
			if(isNoble())
			{
				updateNobleSkills();
			}

			// Restore Hero skills at main class only
			if(_hero && !isFakeHero() && getBaseClassId() == getActiveClassId())
			{
				Hero.addSkills(this);
			}

			if(isFakeHero() && Config.SERVICES_HERO_SELL_SKILL && getBaseClassId() == getActiveClassId())
			{
				Hero.addSkills(this);
			}

			// Restore clan skills
			if(_clan != null)
			{
				_clan.addSkillsQuietly(this);

				// Restore clan leader siege skills
				if(_clan.getLeaderId() == getObjectId() && _clan.getLevel() >= 5)
				{
					SiegeUtils.addSiegeSkills(this);
				}
			}

			// Give dwarven craft skill
			if(getActiveClassId() >= 53 && getActiveClassId() <= 57 || getActiveClassId() == 117 || getActiveClassId() == 118)
			{
				super.addSkill(SkillTable.getInstance().getInfo(1321, 1));
			}

			super.addSkill(SkillTable.getInstance().getInfo(1322, 1));

			if(Config.UNSTUCK_SKILL && getSkillLevel(1050) < 0)
			{
				super.addSkill(SkillTable.getInstance().getInfo(2099, 1));
			}
			if(Config.AUTOCP_SKILL)
			{
			super.addSkill(SkillTable.getInstance().getInfo(90004, 1));
			}
		}
		catch(final Exception e)
		{
			_log.warn("Could not restore skills for player objId: " + getObjectId());
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public void storeDisableSkills()
	{
		Connection con = null;
		Statement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			statement.executeUpdate("DELETE FROM character_skills_save WHERE char_obj_id = " + getObjectId() + " AND class_index=" + getActiveClassId() + " AND `end_time` < " + System.currentTimeMillis());

			if(_skillReuses.isEmpty())
				return;

			SqlBatch b = new SqlBatch("REPLACE INTO `character_skills_save` (`char_obj_id`,`skill_id`,`skill_level`,`class_index`,`end_time`,`reuse_delay_org`) VALUES");
			synchronized (_skillReuses)
			{
				StringBuilder sb;
				for(TimeStamp timeStamp : _skillReuses.values())
					if(timeStamp.hasNotPassed())
					{
						sb = new StringBuilder("(");
						sb.append(getObjectId()).append(",");
						sb.append(timeStamp.getId()).append(",");
						sb.append(timeStamp.getLevel()).append(",");
						sb.append(getActiveClassId()).append(",");
						sb.append(timeStamp.getEndTime()).append(",");
						sb.append(timeStamp.getReuseBasic()).append(")");
						b.write(sb.toString());
					}
			}
			if(!b.isEmpty())
			{
				statement.executeUpdate(b.close());
			}
		}
		catch(final Exception e)
		{
			_log.warn("Could not store disable skills data: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void restoreDisableSkills()
	{
		_skillReuses.clear();

		Connection con = null;
		Statement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			rset = statement.executeQuery("SELECT skill_id,skill_level,end_time,reuse_delay_org FROM character_skills_save WHERE char_obj_id=" + getObjectId() + " AND class_index=" + getActiveClassId());
			while(rset.next())
			{
				int skillId = rset.getInt("skill_id");
				int skillLevel = rset.getInt("skill_level");
				long endTime = rset.getLong("end_time");
				long rDelayOrg = rset.getLong("reuse_delay_org");
				long curTime = System.currentTimeMillis();

				Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);

				if(skill != null && endTime - curTime > 500)
				{
					_skillReuses.put(skill.hashCode(), new TimeStamp(skill, endTime, rDelayOrg));
				}
			}
			DbUtils.close(statement);

			statement = con.createStatement();
			statement.executeUpdate("DELETE FROM character_skills_save WHERE char_obj_id = " + getObjectId() + " AND class_index=" + getActiveClassId() + " AND `end_time` < " + System.currentTimeMillis());
		}
		catch(Exception e)
		{
			_log.error("Could not restore active skills data!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	/**
	 * Retrieve from the database all Henna of this L2Player, add them to _henna
	 * and calculate stats of the L2Player.<BR>
	 * <BR>
	 */
	private void restoreHenna()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("select slot, symbol_id from character_hennas where char_obj_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, getActiveClassId());
			rset = statement.executeQuery();

			for(int i = 0; i < 3; i++)
			{
				_henna[i] = null;
			}

			while(rset.next())
			{
				final int slot = rset.getInt("slot");
				if(slot < 1 || slot > 3)
				{
					continue;
				}

				final int symbol_id = rset.getInt("symbol_id");

				if(symbol_id != 0)
				{
					final Henna tpl = HennaHolder.getInstance().getHenna(symbol_id);
					if(tpl != null)
					{
						_henna[slot - 1] = tpl;
					}
				}
			}
		}
		catch(final Exception e)
		{
			_log.warn("could not restore henna: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		// Calculate Henna modifiers of this L2Player
		recalcHennaStats();

	}

	public int getHennaEmptySlots()
	{
		int totalSlots = 1 + getClassId().level();
		for(int i = 0; i < 3; i++)
			if(_henna[i] != null)
			{
				totalSlots--;
			}

		if(totalSlots <= 0)
			return 0;

		return totalSlots;

	}

	/**
	 * Remove a Henna of the L2Player, save update in the character_hennas table
	 * of the database and send Server->Client HennaInfo/UserInfo packet to this
	 * L2Player.<BR>
	 * <BR>
	 */
	public boolean removeHenna(int slot)
	{
		if(slot < 1 || slot > 3)
			return false;

		slot--;

		if(_henna[slot] == null)
			return false;

		final Henna henna = _henna[slot];
		final int dyeID = henna.getDyeId();

		_henna[slot] = null;

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM character_hennas where char_obj_id=? and slot=? and class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, slot + 1);
			statement.setInt(3, getActiveClassId());
			statement.execute();
		}
		catch(final Exception e)
		{
			_log.warn("could not remove char henna: " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}

		// Calculate Henna modifiers of this L2Player
		recalcHennaStats();

		// Send Server->Client HennaInfo packet to this L2Player
		sendPacket(new HennaInfo(this));
		// Send Server->Client UserInfo packet to this L2Player
		sendUserInfo(true);

		// Add the recovered dyes to the player's inventory and notify them.
		ItemFunctions.addItem(this, dyeID, henna.getDrawCount() / 2, true);

		return true;
	}

	/**
	 * Add a Henna to the L2Player, save update in the character_hennas table of
	 * the database and send Server->Client HennaInfo/UserInfo packet to this
	 * L2Player.<BR>
	 * <BR>
	 * 
	 * @param henna
	 *            L2Henna Ð Ò‘Ð Â»Ð¡Ð� Ð Ò‘Ð Ñ•Ð Â±Ð Â°Ð Ð†Ð Â»Ð ÂµÐ Ð…Ð Ñ‘Ð¡Ð�
	 */
	public boolean addHenna(Henna henna)
	{
		if(getHennaEmptySlots() == 0)
		{
			sendPacket(SystemMsg.NO_SLOT_EXISTS_TO_DRAW_THE_SYMBOL);
			return false;
		}

		// int slot = 0;
		for(int i = 0; i < 3; i++)
			if(_henna[i] == null)
			{
				_henna[i] = henna;

				// Calculate Henna modifiers of this L2Player
				recalcHennaStats();

				Connection con = null;
				PreparedStatement statement = null;
				try
				{
					con = DatabaseFactory.getInstance().getConnection();
					statement = con.prepareStatement("INSERT INTO `character_hennas` (char_obj_id, symbol_id, slot, class_index) VALUES (?,?,?,?)");
					statement.setInt(1, getObjectId());
					statement.setInt(2, henna.getSymbolId());
					statement.setInt(3, i + 1);
					statement.setInt(4, getActiveClassId());
					statement.execute();
				}
				catch(Exception e)
				{
					_log.warn("could not save char henna: " + e);
				}
				finally
				{
					DbUtils.closeQuietly(con, statement);
				}

				sendPacket(new HennaInfo(this));
				sendUserInfo(true);

				return true;
			}

		return false;
	}

	/**
	 * Calculate Henna modifiers of this L2Player.
	 */
	private void recalcHennaStats()
	{
		_hennaINT = 0;
		_hennaSTR = 0;
		_hennaCON = 0;
		_hennaMEN = 0;
		_hennaWIT = 0;
		_hennaDEX = 0;

		for(int i = 0; i < 3; i++)
		{
			Henna henna = _henna[i];
			if(henna == null)
			{
				continue;
			}
			if(!henna.isForThisClass(this))
			{
				continue;
			}

			_hennaINT += henna.getStatINT();
			_hennaSTR += henna.getStatSTR();
			_hennaMEN += henna.getStatMEN();
			_hennaCON += henna.getStatCON();
			_hennaWIT += henna.getStatWIT();
			_hennaDEX += henna.getStatDEX();
		}

		if(_hennaINT > 5)
		{
			_hennaINT = 5;
		}
		if(_hennaSTR > 5)
		{
			_hennaSTR = 5;
		}
		if(_hennaMEN > 5)
		{
			_hennaMEN = 5;
		}
		if(_hennaCON > 5)
		{
			_hennaCON = 5;
		}
		if(_hennaWIT > 5)
		{
			_hennaWIT = 5;
		}
		if(_hennaDEX > 5)
		{
			_hennaDEX = 5;
		}
	}

	/**
	 * @param slot
	 *            id Ñ�Ð»Ð¾Ñ‚Ð° Ñƒ Ð¿ÐµÑ€Ñ�Ð°
	 * @return the Henna of this L2Player corresponding to the selected slot.<BR>
	 * <BR>
	 */
	public Henna getHenna(final int slot)
	{
		if(slot < 1 || slot > 3)
			return null;
		return _henna[slot - 1];
	}

	public int getHennaStatINT()
	{
		return _hennaINT;
	}

	public int getHennaStatSTR()
	{
		return _hennaSTR;
	}

	public int getHennaStatCON()
	{
		return _hennaCON;
	}

	public int getHennaStatMEN()
	{
		return _hennaMEN;
	}

	public int getHennaStatWIT()
	{
		return _hennaWIT;
	}

	public int getHennaStatDEX()
	{
		return _hennaDEX;
	}

	@Override
	public boolean consumeItem(int itemConsumeId, long itemCount)
	{
		if(getInventory().destroyItemByItemId(itemConsumeId, itemCount))
		{
			sendPacket(SystemMessage2.removeItems(itemConsumeId, itemCount));
			return true;
		}
		return false;
	}

	@Override
	public boolean consumeItemMp(int itemId, int mp)
	{
		for(ItemInstance item : getInventory().getPaperdollItems())
			if(item != null && item.getItemId() == itemId)
			{
				final int newMp = item.getLifeTime() - mp;
				if(newMp >= 0)
				{
					item.setLifeTime(newMp);
					sendPacket(new InventoryUpdate().addModifiedItem(item));
					return true;
				}
				break;
			}
		return false;
	}

	/**
	 * @return True if the L2Player is a Mage.<BR>
	 * <BR>
	 */
	@Override
	public boolean isMageClass()
	{
		return _template.baseMAtk > 3;
	}

	public boolean isMounted()
	{
		return _mountNpcId > 0;
	}

	public final boolean isRiding()
	{
		return _riding;
	}

	public final void setRiding(boolean mode)
	{
		_riding = mode;
	}

	/**
	 * ÐŸÑ€Ð¾Ð²ÐµÑ€Ñ�ÐµÑ‚, Ð¼Ð¾Ð¶Ð½Ð¾ Ð»Ð¸ Ð¿Ñ€Ð¸Ð·ÐµÐ¼Ð»Ð¸Ñ‚ÑŒÑ�Ñ� Ð² Ñ�Ñ‚Ð¾Ð¹ Ð·Ð¾Ð½Ðµ.
	 * 
	 * @return Ð¼Ð¾Ð¶Ð½Ð¾ Ð»Ð¸ Ð¿Ñ€Ð¸Ð·ÐµÐ¼Ð»Ð¸Ñ‚Ñ�Ñ�
	 */
	public boolean checkLandingState()
	{
		if(isInZone(ZoneType.no_landing))
			return false;

		SiegeEvent<?, ?> siege = getEvent(SiegeEvent.class);
		if(siege != null)
		{
			Residence unit = siege.getResidence();
			if(unit != null && getClan() != null && isClanLeader() && (getClan().getCastle() == unit.getId() || getClan().getHasFortress() == unit.getId()))
				return true;
			return false;
		}

		return true;
	}

	public void setMount(int npcId, int obj_id, int level)
	{
		if(isCursedWeaponEquipped())
			return;

		switch(npcId)
		{
			case 0: // Dismount
				setFlying(false);
				setRiding(false);
				if(getTransformation() > 0)
				{
					setTransformation(0);
				}
				removeSkillById(Skill.SKILL_STRIDER_ASSAULT);
				removeSkillById(Skill.SKILL_WYVERN_BREATH);
				getEffectList().stopEffect(Skill.SKILL_HINDER_STRIDER);
				break;
			case PetDataTable.STRIDER_WIND_ID:
			case PetDataTable.STRIDER_STAR_ID:
			case PetDataTable.STRIDER_TWILIGHT_ID:
			case PetDataTable.RED_STRIDER_WIND_ID:
			case PetDataTable.RED_STRIDER_STAR_ID:
			case PetDataTable.RED_STRIDER_TWILIGHT_ID:
			case PetDataTable.GUARDIANS_STRIDER_ID:
				setRiding(true);
				if(isNoble())
				{
					addSkill(SkillTable.getInstance().getInfo(Skill.SKILL_STRIDER_ASSAULT, 1), false);
				}
				break;
			case PetDataTable.WYVERN_ID:
				setFlying(true);
				setLoc(getLoc().changeZ(32));
				addSkill(SkillTable.getInstance().getInfo(Skill.SKILL_WYVERN_BREATH, 1), false);
				break;
			case PetDataTable.WGREAT_WOLF_ID:
			case PetDataTable.FENRIR_WOLF_ID:
			case PetDataTable.WFENRIR_WOLF_ID:
				setRiding(true);
				break;
		}

		if(npcId > 0)
		{
			unEquipWeapon();
		}

		_mountNpcId = npcId;
		_mountObjId = obj_id;
		_mountLevel = level;

		broadcastUserInfo(true); // Ð½ÑƒÐ¶Ð½Ð¾ Ð¿Ð¾Ñ�Ð»Ð°Ñ‚ÑŒ Ð¿Ð°ÐºÐµÑ‚ Ð¿ÐµÑ€ÐµÐ´ Ride Ð´Ð»Ñ�
		// ÐºÐ¾Ñ€Ñ€ÐµÐºÑ‚Ð½Ð¾Ð³Ð¾ Ñ�Ð½Ñ�Ñ‚Ð¸Ñ� Ð¾Ñ€ÑƒÐ¶Ð¸Ñ� Ñ� Ð·Ð°Ñ‚Ð¾Ñ‡ÐºÐ¾Ð¹
		broadcastPacket(new Ride(this));
		broadcastUserInfo(true); // Ð½ÑƒÐ¶Ð½Ð¾ Ð¿Ð¾Ñ�Ð»Ð°Ñ‚ÑŒ Ð¿Ð°ÐºÐµÑ‚ Ð¿Ð¾Ñ�Ð»Ðµ Ride Ð´Ð»Ñ�
		// ÐºÐ¾Ñ€Ñ€ÐµÐºÑ‚Ð½Ð¾Ð³Ð¾ Ð¾Ñ‚Ð¾Ð±Ñ€Ð°Ð¶ÐµÐ½Ð¸Ñ� Ñ�ÐºÐ¾Ñ€Ð¾Ñ�Ñ‚Ð¸

		sendPacket(new SkillList(this));
	}

	public void unEquipWeapon()
	{
		ItemInstance wpn = getSecondaryWeaponInstance();
		if(wpn != null)
		{
			sendDisarmMessage(wpn);
			getInventory().unEquipItem(wpn);
		}

		wpn = getActiveWeaponInstance();
		if(wpn != null)
		{
			sendDisarmMessage(wpn);
			getInventory().unEquipItem(wpn);
		}

		abortAttack(true, true);
		abortCast(true, true);
	}

	/*
	 * @Override public double getMovementSpeedMultiplier() { int template_speed
	 * = _template.baseRunSpd; if(isMounted()) { L2PetData petData =
	 * PetDataTable.getInstance().getInfo(_mountNpcId, _mountLevel); if(petData
	 * != null) template_speed = petData.getSpeed(); } return getRunSpeed() * 1f
	 * / template_speed; }
	 */

	@Override
	public int getSpeed(int baseSpeed)
	{
		if(isMounted())
		{
			PetData petData = PetDataTable.getInstance().getInfo(_mountNpcId, _mountLevel);
			int speed = 187;
			if(petData != null)
			{
				speed = petData.getSpeed();
			}
			double mod = 1.;
			int level = getLevel();
			if(_mountLevel > level && level - _mountLevel > 10)
			{
				mod = 0.5; // Ð¨Ñ‚Ñ€Ð°Ñ„ Ð½Ð° Ñ€Ð°Ð·Ð½Ð¸Ñ†Ñƒ ÑƒÑ€Ð¾Ð²Ð½ÐµÐ¹ Ð¼ÐµÐ¶Ð´Ñƒ Ð¸Ð³Ñ€Ð¾ÐºÐ¾Ð¼ Ð¸ Ð¿ÐµÑ‚Ð¾Ð¼
			}
			baseSpeed = (int) (mod * speed);
		}
		return super.getSpeed(baseSpeed);
	}

	private int _mountNpcId;
	private int _mountObjId;
	private int _mountLevel;

	public int getMountNpcId()
	{
		return _mountNpcId;
	}

	public int getMountObjId()
	{
		return _mountObjId;
	}

	public int getMountLevel()
	{
		return _mountLevel;
	}

	public void sendDisarmMessage(ItemInstance wpn)
	{
		if(wpn.getEnchantLevel() > 0)
		{
			SystemMessage sm = new SystemMessage(SystemMessage.EQUIPMENT_OF__S1_S2_HAS_BEEN_REMOVED);
			sm.addNumber(wpn.getEnchantLevel());
			sm.addItemName(wpn.getItemId());
			sendPacket(sm);
		}
		else
		{
			SystemMessage sm = new SystemMessage(SystemMessage.S1__HAS_BEEN_DISARMED);
			sm.addItemName(wpn.getItemId());
			sendPacket(sm);
		}
	}

	/**
	 * Ð£Ñ�Ñ‚Ð°Ð½Ð°Ð²Ð»Ð¸Ð²Ð°ÐµÑ‚ Ñ‚Ð¸Ð¿ Ð¸Ñ�Ð¿Ð¾Ð»ÑŒÐ·ÑƒÐµÐ¼Ð¾Ð³Ð¾ Ñ�ÐºÐ»Ð°Ð´Ð°.
	 * 
	 * @param type
	 *            Ñ‚Ð¸Ð¿ Ñ�ÐºÐ»Ð°Ð´Ð°:<BR>
	 *            <ul>
	 *            <li>WarehouseType.PRIVATE
	 *            <li>WarehouseType.CLAN
	 *            <li>WarehouseType.CASTLE
	 *            </ul>
	 */
	public void setUsingWarehouseType(final WarehouseType type)
	{
		_usingWHType = type;
	}

	/**
	 * Ð â€™Ð Ñ•Ð Â·Ð Ð†Ð¡Ð‚Ð Â°Ð¡â€°Ð Â°Ð ÂµÐ¡â€š Ð¡â€šÐ Ñ‘Ð Ñ— Ð Ñ‘Ð¡ÐƒÐ Ñ—Ð Ñ•Ð Â»Ð¡ÐŠÐ Â·Ð¡Ñ“Ð ÂµÐ Ñ˜Ð Ñ•Ð Ñ–Ð Ñ• Ð¡ÐƒÐ Ñ”Ð Â»Ð Â°Ð Ò‘Ð Â°.
	 * 
	 * @return null Ð Ñ‘Ð Â»Ð Ñ‘ Ð¡â€šÐ Ñ‘Ð Ñ— Ð¡ÐƒÐ Ñ”Ð Â»Ð Â°Ð Ò‘Ð Â°:<br>
	 *         <ul>
	 *         <li>WarehouseType.PRIVATE
	 *         <li>WarehouseType.CLAN
	 *         <li>WarehouseType.CASTLE
	 *         </ul>
	 */
	public WarehouseType getUsingWarehouseType()
	{
		return _usingWHType;
	}

	public Collection<EffectCubic> getCubics()
	{
		return _cubics == null ? Collections.<EffectCubic> emptyList() : _cubics.values();
	}

	public void addCubic(EffectCubic cubic)
	{
		if(_cubics == null)
		{
			_cubics = new ConcurrentHashMap<Integer, EffectCubic>(3);
		}
		_cubics.put(cubic.getId(), cubic);
	}

	public void removeCubic(int id)
	{
		if(_cubics != null)
		{
			_cubics.remove(id);
		}
	}

	public EffectCubic getCubic(int id)
	{
		return _cubics == null ? null : _cubics.get(id);
	}

	@Override
	public String toString()
	{
		return getName() + "[" + getObjectId() + "]";
	}
	
	public final String toFullString()
	{
		final StringBuilder sb = new StringBuilder(160);
		sb.append("Player '").append(getName()).append("' [oid=").append(getObjectId()).append(", account='").append(getAccountName()).append(", ip=").append(_connection != null ? _connection.getIpAddr() : "0.0.0.0").append("']");
		return sb.toString();
	}
	/**
	 * @return the modifier corresponding to the Enchant Effect of the Active
	 *         Weapon (Min : 127).<BR>
	 * <BR>
	 */
	public int getEnchantEffect()
	{
		final ItemInstance wpn = getActiveWeaponInstance();

		if(wpn == null)
			return 0;

		return Math.min(127, wpn.getEnchantLevel());
	}

	/**
	 * Set the _lastFolkNpc of the L2Player corresponding to the last Folk witch
	 * one the player talked.<BR>
	 * <BR>
	 */
	public void setLastNpc(final NpcInstance npc)
	{
		if(npc == null)
		{
			_lastNpc = HardReferences.emptyRef();
		}
		else
		{
			_lastNpc = npc.getRef();
		}
	}

	/**
	 * @return the _lastFolkNpc of the L2Player corresponding to the last Folk
	 *         witch one the player talked.<BR>
	 * <BR>
	 */
	public NpcInstance getLastNpc()
	{
		return _lastNpc.get();
	}

	public void setMultisell(MultiSellListContainer multisell)
	{
		_multisell = multisell;
	}

	public MultiSellListContainer getMultisell()
	{
		return _multisell;
	}

	/**
	 * @return True if L2Player is a participant in the Festival of Darkness.<BR>
	 * <BR>
	 */
	public boolean isFestivalParticipant()
	{
		return getReflection() instanceof DarknessFestival;
	}

	@Override
	public boolean unChargeShots(boolean spirit)
	{
		ItemInstance weapon = getActiveWeaponInstance();
		if(weapon == null)
			return false;

		if(spirit)
		{
			weapon.setChargedSpiritshot(ItemInstance.CHARGED_NONE);
		}
		else
		{
			weapon.setChargedSoulshot(ItemInstance.CHARGED_NONE);
		}

		autoShot();
		return true;
	}

	public boolean unChargeFishShot()
	{
		ItemInstance weapon = getActiveWeaponInstance();
		if(weapon == null)
			return false;
		weapon.setChargedFishshot(false);
		autoShot();
		return true;
	}

	public void autoShot()
	{
		for(Integer shotId : _activeSoulShots)
		{
			ItemInstance item = getInventory().getItemByItemId(shotId);
			if(item == null)
			{
				removeAutoSoulShot(shotId);
				continue;
			}
			IItemHandler handler = item.getTemplate().getHandler();
			if(handler == null)
			{
				continue;
			}
			handler.useItem(this, item, false);
		}
	}

	public boolean getChargedFishShot()
	{
		ItemInstance weapon = getActiveWeaponInstance();
		return weapon != null && weapon.getChargedFishshot();
	}

	@Override
	public boolean getChargedSoulShot()
	{
		ItemInstance weapon = getActiveWeaponInstance();
		return weapon != null && weapon.getChargedSoulshot() == ItemInstance.CHARGED_SOULSHOT;
	}

	@Override
	public int getChargedSpiritShot()
	{
		ItemInstance weapon = getActiveWeaponInstance();
		if(weapon == null)
			return 0;
		return weapon.getChargedSpiritshot();
	}

	public void addAutoSoulShot(Integer itemId)
	{
		_activeSoulShots.add(itemId);
	}

	public void removeAutoSoulShot(Integer itemId)
	{
		_activeSoulShots.remove(itemId);
	}

	public Set<Integer> getAutoSoulShot()
	{
		return _activeSoulShots;
	}

	public void setInvisibleType(InvisibleType vis)
	{
		_invisibleType = vis;
	}

	@Override
	public InvisibleType getInvisibleType()
	{
		return _invisibleType;
	}

	public int getClanPrivileges()
	{
		if(_clan == null)
			return 0;
		if(isClanLeader())
			return Clan.CP_ALL;
		if(_powerGrade < 1 || _powerGrade > 9)
			return 0;
		RankPrivs privs = _clan.getRankPrivs(_powerGrade);
		if(privs != null)
			return privs.getPrivs();
		return 0;
	}

	public void teleToClosestTown()
	{
		teleToLocation(TeleportUtils.getRestartLocation(this, RestartType.TO_VILLAGE), ReflectionManager.DEFAULT);
	}

	public void teleToCastle()
	{
		teleToLocation(TeleportUtils.getRestartLocation(this, RestartType.TO_CASTLE), ReflectionManager.DEFAULT);
	}

	public void teleToFortress()
	{
		teleToLocation(TeleportUtils.getRestartLocation(this, RestartType.TO_FORTRESS), ReflectionManager.DEFAULT);
	}

	public void teleToClanhall()
	{
		teleToLocation(TeleportUtils.getRestartLocation(this, RestartType.TO_CLANHALL), ReflectionManager.DEFAULT);
	}

	@Override
	public void sendMessage(CustomMessage message)
	{
		sendMessage(message.toString());
	}

	@Override
	public void teleToLocation(int x, int y, int z, int refId)
	{
		if(isDeleted())
			return;

		super.teleToLocation(x, y, z, refId);
	}

	@Override
	public boolean onTeleported()
	{
		if(!super.onTeleported())
			return false;

		if(isFakeDeath())
		{
			breakFakeDeath();
		}

		if(isInBoat())
		{
			setLoc(getBoat().getLoc());
		}

		// 15 Ñ�ÐµÐºÑƒÐ½Ð´ Ð¿Ð¾Ñ�Ð»Ðµ Ñ‚ÐµÐ»ÐµÐ¿Ð¾Ñ€Ñ‚Ð° Ð½Ð° Ð¿ÐµÑ€Ñ�Ð¾Ð½Ð°Ð¶Ð° Ð½Ðµ Ð°Ð³Ñ€Ñ�Ñ‚Ñ�Ñ� Ð¼Ð¾Ð±Ñ‹
		setNonAggroTime(System.currentTimeMillis() + Config.NONAGGRO_TIME_ONTELEPORT);

		spawnMe();

		setLastClientPosition(getLoc());
		setLastServerPosition(getLoc());

		if(isPendingRevive())
		{
			doRevive();
		}

		DuelEvent duel = getEvent(DuelEvent.class);
		if(duel != null)
		{
			duel.abortDuel(this);
		}

		sendActionFailed();

		getAI().notifyEvent(CtrlEvent.EVT_TELEPORTED);

		if(isLockedTarget() && getTarget() != null)
		{
			sendPacket(new MyTargetSelected(getTarget().getObjectId(), 0));
		}

		sendUserInfo(true);
		if(getPet() != null)
		{
			getPet().teleportToOwner();
		}

		return true;
	}

	public boolean enterObserverMode(Location loc)
	{
		WorldRegion observerRegion = World.getRegion(loc);
		if(observerRegion == null)
			return false;
		if(!_observerMode.compareAndSet(OBSERVER_NONE, OBSERVER_STARTING))
			return false;

		setTarget(null);
		stopMove();
		sitDown(null);
		setFlying(true);

		// ÐžÑ‡Ð¸Ñ‰Ð°ÐµÐ¼ Ð²Ñ�Ðµ Ð²Ð¸Ð´Ð¸Ð¼Ñ‹Ðµ Ð¾Ð±ÑŒÐµÐºÑ‚Ñ‹
		World.removeObjectsFromPlayer(this);

		setObserverRegion(observerRegion);

		// ÐžÑ‚Ð¾Ð±Ñ€Ð°Ð¶Ð°ÐµÐ¼ Ð½Ð°Ð´Ð¿Ð¸Ñ�ÑŒ Ð½Ð°Ð´ Ð³Ð¾Ð»Ð¾Ð²Ð¾Ð¹
		broadcastCharInfo();

		// ÐŸÐµÑ€ÐµÑ…Ð¾Ð´Ð¸Ð¼ Ð² Ñ€ÐµÐ¶Ð¸Ð¼ Ð¾Ð±Ñ�ÐµÑ€Ð²Ð¸Ð½Ð³Ð°
		sendPacket(new ObserverStart(loc));

		return true;
	}

	public void appearObserverMode()
	{
		if(!_observerMode.compareAndSet(OBSERVER_STARTING, OBSERVER_STARTED))
			return;

		WorldRegion currentRegion = getCurrentRegion();
		WorldRegion observerRegion = getObserverRegion();

		// Ð”Ð¾Ð±Ð°Ð²Ð»Ñ�ÐµÐ¼ Ñ„Ñ�Ð¹Ðº Ð² Ñ‚Ð¾Ñ‡ÐºÑƒ Ð½Ð°Ð±Ð»ÑŽÐ´ÐµÐ½Ð¸Ñ�
		if(!observerRegion.equals(currentRegion))
		{
			observerRegion.addObject(this);
		}

		World.showObjectsToPlayer(this);

		OlympiadGame game = getOlympiadObserveGame();
		if(game != null)
		{
			game.addSpectator(this);
			game.broadcastInfo(null, this, true);
		}
	}

	public void leaveObserverMode()
	{
		if(!_observerMode.compareAndSet(OBSERVER_STARTED, OBSERVER_LEAVING))
			return;

		WorldRegion currentRegion = getCurrentRegion();
		WorldRegion observerRegion = getObserverRegion();

		// Ð£Ð±Ð¸Ñ€Ð°ÐµÐ¼ Ñ„Ñ�Ð¹Ðº Ð² Ñ‚Ð¾Ñ‡ÐºÐµ Ð½Ð°Ð±Ð»ÑŽÐ´ÐµÐ½Ð¸Ñ�
		if(!observerRegion.equals(currentRegion))
		{
			observerRegion.removeObject(this);
		}

		// ÐžÑ‡Ð¸Ñ‰Ð°ÐµÐ¼ Ð²Ñ�Ðµ Ð²Ð¸Ð´Ð¸Ð¼Ñ‹Ðµ Ð¾Ð±ÑŒÐµÐºÑ‚Ñ‹
		World.removeObjectsFromPlayer(this);

		setObserverRegion(null);

		setTarget(null);
		stopMove();

		// Ð’Ñ‹Ñ…Ð¾Ð´Ð¸Ð¼ Ð¸Ð· Ñ€ÐµÐ¶Ð¸Ð¼Ð° Ð¾Ð±Ñ�ÐµÑ€Ð²Ð¸Ð½Ð³Ð°
		sendPacket(new ObserverEnd(getLoc()));
	}

	public void returnFromObserverMode()
	{
		if(!_observerMode.compareAndSet(OBSERVER_LEAVING, OBSERVER_NONE))
			return;

		// Ð�ÑƒÐ¶Ð½Ð¾ Ð¿Ñ€Ð¸ Ñ‚ÐµÐ»ÐµÐ¿Ð¾Ñ€Ñ‚Ðµ Ñ� Ð±Ð¾Ð»ÐµÐµ Ð²Ñ‹Ñ�Ð¾ÐºÐ¾Ð¹ Ñ‚Ð¾Ñ‡ÐºÐ¸ Ð½Ð° Ð±Ð¾Ð»ÐµÐµ Ð½Ð¸Ð·ÐºÑƒÑŽ, Ð¸Ð½Ð°Ñ‡Ðµ
		// Ð½Ð°Ð½Ð¾Ñ�Ð¸Ñ‚Ñ�Ñ� Ð²Ñ€ÐµÐ´ Ð¾Ñ‚ "Ð¿Ð°Ð´ÐµÐ½Ð¸Ñ�"
		setLastClientPosition(null);
		setLastServerPosition(null);

		unblock();
		standUp();
		setFlying(false);

		broadcastCharInfo();

		World.showObjectsToPlayer(this);
	}

	public void enterOlympiadObserverMode(Location loc, OlympiadGame game, Reflection reflect)
	{
		WorldRegion observerRegion = World.getRegion(loc);
		if(observerRegion == null)
			return;

		OlympiadGame oldGame = getOlympiadObserveGame();
		if(!_observerMode.compareAndSet(oldGame != null ? OBSERVER_STARTED : OBSERVER_NONE, OBSERVER_STARTING))
			return;

		setTarget(null);
		stopMove();

		// ÐžÑ‡Ð¸Ñ‰Ð°ÐµÐ¼ Ð²Ñ�Ðµ Ð²Ð¸Ð´Ð¸Ð¼Ñ‹Ðµ Ð¾Ð±ÑŒÐµÐºÑ‚Ñ‹
		World.removeObjectsFromPlayer(this);
		WorldRegion oldObserverRegion = getObserverRegion();
		if(oldObserverRegion != null)
		{
			oldObserverRegion.removeObject(this);
		}
		setObserverRegion(observerRegion);

		if(oldGame != null)
		{
			oldGame.removeSpectator(this);
			sendPacket(ExOlympiadMatchEnd.STATIC);
		}
		else
		{
			block();

			// ÐžÑ‚Ð¾Ð±Ñ€Ð°Ð¶Ð°ÐµÐ¼ Ð½Ð°Ð´Ð¿Ð¸Ñ�ÑŒ Ð½Ð°Ð´ Ð³Ð¾Ð»Ð¾Ð²Ð¾Ð¹
			broadcastCharInfo();

			// ÐœÐµÐ½Ñ�ÐµÐ¼ Ð¸Ð½Ñ‚ÐµÑ€Ñ„ÐµÐ¹Ñ�
			sendPacket(new ExOlympiadMode(3));
		}

		setOlympiadObserveGame(game);

		// "Ð¢ÐµÐ»ÐµÐ¿Ð¾Ñ€Ñ‚Ð¸Ñ€ÑƒÐµÐ¼Ñ�Ñ�"
		setReflection(reflect);
		sendPacket(new TeleportToLocation(this, loc));
	}

	public void leaveOlympiadObserverMode(boolean removeFromGame)
	{
		OlympiadGame game = getOlympiadObserveGame();
		if(game == null)
			return;
		if(!_observerMode.compareAndSet(OBSERVER_STARTED, OBSERVER_LEAVING))
			return;

		if(removeFromGame)
		{
			game.removeSpectator(this);
		}
		setOlympiadObserveGame(null);

		WorldRegion currentRegion = getCurrentRegion();
		WorldRegion observerRegion = getObserverRegion();

		// Ð£Ð±Ð¸Ñ€Ð°ÐµÐ¼ Ñ„Ñ�Ð¹Ðº Ð² Ñ‚Ð¾Ñ‡ÐºÐµ Ð½Ð°Ð±Ð»ÑŽÐ´ÐµÐ½Ð¸Ñ�
		if(observerRegion != null && currentRegion != null && !observerRegion.equals(currentRegion))
		{
			observerRegion.removeObject(this);
		}

		// ÐžÑ‡Ð¸Ñ‰Ð°ÐµÐ¼ Ð²Ñ�Ðµ Ð²Ð¸Ð´Ð¸Ð¼Ñ‹Ðµ Ð¾Ð±ÑŒÐµÐºÑ‚Ñ‹
		World.removeObjectsFromPlayer(this);

		setObserverRegion(null);

		setTarget(null);
		stopMove();

		// ÐœÐµÐ½Ñ�ÐµÐ¼ Ð¸Ð½Ñ‚ÐµÑ€Ñ„ÐµÐ¹Ñ�
		sendPacket(new ExOlympiadMode(0));
		sendPacket(ExOlympiadMatchEnd.STATIC);

		setReflection(ReflectionManager.DEFAULT);
		// "Ð¢ÐµÐ»ÐµÐ¿Ð¾Ñ€Ñ‚Ð¸Ñ€ÑƒÐµÐ¼Ñ�Ñ�"
		sendPacket(new TeleportToLocation(this, getLoc()));
	}

	public void setOlympiadSide(final int i)
	{
		_olympiadSide = i;
	}

	public int getOlympiadSide()
	{
		return _olympiadSide;
	}

	@Override
	public boolean isInObserverMode()
	{
		return _observerMode.get() > 0;
	}

	public int getObserverMode()
	{
		return _observerMode.get();
	}

	public WorldRegion getObserverRegion()
	{
		return _observerRegion;
	}

	public void setObserverRegion(WorldRegion region)
	{
		_observerRegion = region;
	}

	public int getTeleMode()
	{
		return _telemode;
	}

	public void setTeleMode(final int mode)
	{
		_telemode = mode;
	}

	public void setLoto(final int i, final int val)
	{
		_loto[i] = val;
	}

	public int getLoto(final int i)
	{
		return _loto[i];
	}

	public void setRace(final int i, final int val)
	{
		_race[i] = val;
	}

	public int getRace(final int i)
	{
		return _race[i];
	}

	public boolean getMessageRefusal()
	{
		return _messageRefusal;
	}

	public void setMessageRefusal(final boolean mode)
	{
		_messageRefusal = mode;
	}

	public void setTradeRefusal(final boolean mode)
	{
		_tradeRefusal = mode;
	}

	public boolean getTradeRefusal()
	{
		return _tradeRefusal;
	}

	public void addToBlockList(final String charName)
	{
		if(charName == null || charName.equalsIgnoreCase(getName()) || isInBlockList(charName))
		{
			// ÑƒÐ¶Ðµ Ð² Ñ�Ð¿Ð¸Ñ�ÐºÐµ
			sendPacket(Msg.YOU_HAVE_FAILED_TO_REGISTER_THE_USER_TO_YOUR_IGNORE_LIST);
			return;
		}

		Player block_target = World.getPlayer(charName);

		if(block_target != null)
		{
			if(block_target.isGM())
			{
				sendPacket(Msg.YOU_MAY_NOT_IMPOSE_A_BLOCK_ON_A_GM);
				return;
			}
			_blockList.put(block_target.getObjectId(), block_target.getName());
			sendPacket(new SystemMessage(SystemMessage.S1_HAS_BEEN_ADDED_TO_YOUR_IGNORE_LIST).addString(block_target.getName()));
			block_target.sendPacket(new SystemMessage(SystemMessage.S1__HAS_PLACED_YOU_ON_HIS_HER_IGNORE_LIST).addString(getName()));
			return;
		}

		int charId = CharacterDAO.getInstance().getObjectIdByName(charName);

		if(charId == 0)
		{
			// Ñ‡Ð°Ñ€ Ð½Ðµ Ñ�ÑƒÑ‰ÐµÑ�Ñ‚Ð²ÑƒÐµÑ‚
			sendPacket(Msg.YOU_HAVE_FAILED_TO_REGISTER_THE_USER_TO_YOUR_IGNORE_LIST);
			return;
		}

		if(Config.gmlist.containsKey(charId) && Config.gmlist.get(charId).IsGM)
		{
			sendPacket(Msg.YOU_MAY_NOT_IMPOSE_A_BLOCK_ON_A_GM);
			return;
		}
		_blockList.put(charId, charName);
		sendPacket(new SystemMessage(SystemMessage.S1_HAS_BEEN_ADDED_TO_YOUR_IGNORE_LIST).addString(charName));
	}

	public void removeFromBlockList(final String charName)
	{
		int charId = 0;
		for(int blockId : _blockList.keySet())
			if(charName.equalsIgnoreCase(_blockList.get(blockId)))
			{
				charId = blockId;
				break;
			}
		if(charId == 0)
		{
			sendPacket(Msg.YOU_HAVE_FAILED_TO_DELETE_THE_CHARACTER_FROM_IGNORE_LIST);
			return;
		}
		sendPacket(new SystemMessage(SystemMessage.S1_HAS_BEEN_REMOVED_FROM_YOUR_IGNORE_LIST).addString(_blockList.remove(charId)));
		Player block_target = GameObjectsStorage.getPlayer(charId);
		if(block_target != null)
		{
			block_target.sendMessage(getName() + " has removed you from his/her Ignore List."); // Ð’
			// Ñ�Ð¸Ñ�Ñ‚ÐµÐ¼Ð½Ñ‹Ñ…(619
			// ==
			// 620)
			// Ð¼ÐµÑ�Ñ�Ð°Ð³Ð°Ñ…
			// Ð¾ÑˆÐ¸Ð±ÐºÐ°
			// ;)
		}
	}

	public boolean isInBlockList(final Player player)
	{
		return isInBlockList(player.getObjectId());
	}

	public boolean isInBlockList(final int charId)
	{
		return _blockList != null && _blockList.containsKey(charId);
	}

	public boolean isInBlockList(final String charName)
	{
		for(int blockId : _blockList.keySet())
			if(charName.equalsIgnoreCase(_blockList.get(blockId)))
				return true;
		return false;
	}

	private void restoreBlockList()
	{
		_blockList.clear();

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT target_Id, char_name FROM character_blocklist LEFT JOIN characters ON ( character_blocklist.target_Id = characters.obj_Id ) WHERE character_blocklist.obj_Id = ?");
			statement.setInt(1, getObjectId());
			rs = statement.executeQuery();
			while(rs.next())
			{
				int targetId = rs.getInt("target_Id");
				String name = rs.getString("char_name");
				if(name == null)
				{
					continue;
				}
				_blockList.put(targetId, name);
			}
		}
		catch(SQLException e)
		{
			_log.warn("Can't restore player blocklist " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rs);
		}
	}

	private void storeBlockList()
	{
		Connection con = null;
		Statement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			statement.executeUpdate("DELETE FROM character_blocklist WHERE obj_Id=" + getObjectId());

			if(_blockList.isEmpty())
				return;

			SqlBatch b = new SqlBatch("INSERT IGNORE INTO `character_blocklist` (`obj_Id`,`target_Id`) VALUES");

			synchronized (_blockList)
			{
				StringBuilder sb;
				for(Entry<Integer, String> e : _blockList.entrySet())
				{
					sb = new StringBuilder("(");
					sb.append(getObjectId()).append(",");
					sb.append(e.getKey()).append(")");
					b.write(sb.toString());
				}
			}
			if(!b.isEmpty())
			{
				statement.executeUpdate(b.close());
			}
		}
		catch(Exception e)
		{
			_log.warn("Can't store player blocklist " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public boolean isBlockAll()
	{
		return _blockAll;
	}

	public void setBlockAll(final boolean state)
	{
		_blockAll = state;
	}

	public Collection<String> getBlockList()
	{
		return _blockList.values();
	}

	public Map<Integer, String> getBlockListMap()
	{
		return _blockList;
	}

	public void setHero(final boolean hero)
	{
		_hero = hero;
	}

	@Override
	public boolean isHero()
	{
		return _hero;
	}

	public boolean isFakeHero()
	{
		if(Config.SERVICES_HERO_SELL_ENABLED && getVarB("hasFakeHero"))
			return true;

		return false;
	}

	public boolean FakeHeroEquip()
	{
		if(isFakeHero() && Config.SERVICES_HERO_SELL_ITEMS)
			return true;

		return false;
	}

	public boolean FakeHeroChat()
	{
		if(isFakeHero() && Config.SERVICES_HERO_SELL_CHAT)
			return true;

		return false;
	}

	public boolean FakeHeroSkill()
	{
		if(isFakeHero() && Config.SERVICES_HERO_SELL_SKILL)
			return true;

		return false;
	}

	public void setIsInOlympiadMode(final boolean b)
	{
		_inOlympiadMode = b;
	}

	@Override
	public boolean isInOlympiadMode()
	{
		return _inOlympiadMode;
	}

	public boolean isOlympiadGameStart()
	{
		return _olympiadGame != null && _olympiadGame.getState() == 1;
	}

	public boolean isOlympiadCompStart()
	{
		return _olympiadGame != null && _olympiadGame.getState() == 2;
	}

	public void updateNobleSkills()
	{
		if(isNoble())
		{
			if(isClanLeader() && getClan().getCastle() > 0)
			{
				super.addSkill(SkillTable.getInstance().getInfo(Skill.SKILL_WYVERN_AEGIS, 1));
			}
			super.addSkill(SkillTable.getInstance().getInfo(Skill.SKILL_NOBLESSE_BLESSING, 1));
			super.addSkill(SkillTable.getInstance().getInfo(Skill.SKILL_SUMMON_CP_POTION, 1));
			super.addSkill(SkillTable.getInstance().getInfo(Skill.SKILL_FORTUNE_OF_NOBLESSE, 1));
			super.addSkill(SkillTable.getInstance().getInfo(Skill.SKILL_HARMONY_OF_NOBLESSE, 1));
			super.addSkill(SkillTable.getInstance().getInfo(Skill.SKILL_SYMPHONY_OF_NOBLESSE, 1));
		}
		else
		{
			super.removeSkillById(Skill.SKILL_WYVERN_AEGIS);
			super.removeSkillById(Skill.SKILL_NOBLESSE_BLESSING);
			super.removeSkillById(Skill.SKILL_SUMMON_CP_POTION);
			super.removeSkillById(Skill.SKILL_FORTUNE_OF_NOBLESSE);
			super.removeSkillById(Skill.SKILL_HARMONY_OF_NOBLESSE);
			super.removeSkillById(Skill.SKILL_SYMPHONY_OF_NOBLESSE);
		}
	}

	public void setNoble(boolean noble)
	{
		if(noble)
		{
			broadcastPacket(new MagicSkillUse(this, this, 6673, 1, 1000, 0));
		}
		_noble = noble;
	}

	public boolean isNoble()
	{
		return _noble;
	}

	public int getSubLevel()
	{
		return isSubClassActive() ? getLevel() : 0;
	}

	/* varka silenos and ketra orc quests related functions */
	public void updateKetraVarka()
	{
		if(ItemFunctions.getItemCount(this, 7215) > 0)
		{
			_ketra = 5;
		}
		else if(ItemFunctions.getItemCount(this, 7214) > 0)
		{
			_ketra = 4;
		}
		else if(ItemFunctions.getItemCount(this, 7213) > 0)
		{
			_ketra = 3;
		}
		else if(ItemFunctions.getItemCount(this, 7212) > 0)
		{
			_ketra = 2;
		}
		else if(ItemFunctions.getItemCount(this, 7211) > 0)
		{
			_ketra = 1;
		}
		else if(ItemFunctions.getItemCount(this, 7225) > 0)
		{
			_varka = 5;
		}
		else if(ItemFunctions.getItemCount(this, 7224) > 0)
		{
			_varka = 4;
		}
		else if(ItemFunctions.getItemCount(this, 7223) > 0)
		{
			_varka = 3;
		}
		else if(ItemFunctions.getItemCount(this, 7222) > 0)
		{
			_varka = 2;
		}
		else if(ItemFunctions.getItemCount(this, 7221) > 0)
		{
			_varka = 1;
		}
		else
		{
			_varka = 0;
			_ketra = 0;
		}
	}

	public int getVarka()
	{
		return _varka;
	}

	public int getKetra()
	{
		return _ketra;
	}

	public void updateRam()
	{
		if(ItemFunctions.getItemCount(this, 7247) > 0)
		{
			_ram = 2;
		}
		else if(ItemFunctions.getItemCount(this, 7246) > 0)
		{
			_ram = 1;
		}
		else
		{
			_ram = 0;
		}
	}

	public int getRam()
	{
		return _ram;
	}

	public void setPledgeType(final int typeId)
	{
		_pledgeType = typeId;
	}

	public int getPledgeType()
	{
		return _pledgeType;
	}

	public void setLvlJoinedAcademy(int lvl)
	{
		_lvlJoinedAcademy = lvl;
	}

	public int getLvlJoinedAcademy()
	{
		return _lvlJoinedAcademy;
	}

	public int getPledgeClass()
	{
		return _pledgeClass;
	}

	public void updatePledgeClass()
	{
		int CLAN_LEVEL = _clan == null ? -1 : _clan.getLevel();
		boolean IN_ACADEMY = _clan != null && Clan.isAcademy(_pledgeType);
		boolean IS_GUARD = _clan != null && Clan.isRoyalGuard(_pledgeType);
		boolean IS_KNIGHT = _clan != null && Clan.isOrderOfKnights(_pledgeType);

		boolean IS_GUARD_CAPTAIN = false, IS_KNIGHT_COMMANDER = false, IS_LEADER = false;

		SubUnit unit = getSubUnit();
		if(unit != null)
		{
			UnitMember unitMember = unit.getUnitMember(getObjectId());
			if(unitMember == null)
			{
				_log.warn("Player: unitMember null, clan: " + _clan.getClanId() + "; pledgeType: " + unit.getType());
				return;
			}
			IS_GUARD_CAPTAIN = Clan.isRoyalGuard(unitMember.getLeaderOf());
			IS_KNIGHT_COMMANDER = Clan.isOrderOfKnights(unitMember.getLeaderOf());
			IS_LEADER = unitMember.getLeaderOf() == Clan.SUBUNIT_MAIN_CLAN;
		}

		switch(CLAN_LEVEL)
		{
			case -1:
				_pledgeClass = RANK_VAGABOND;
				break;
			case 0:
			case 1:
			case 2:
			case 3:
				if(IS_LEADER)
				{
					_pledgeClass = RANK_HEIR;
				}
				else
				{
					_pledgeClass = RANK_VASSAL;
				}
				break;
			case 4:
				if(IS_LEADER)
				{
					_pledgeClass = RANK_KNIGHT;
				}
				else
				{
					_pledgeClass = RANK_HEIR;
				}
				break;
			case 5:
				if(IS_LEADER)
				{
					_pledgeClass = RANK_WISEMAN;
				}
				else if(IN_ACADEMY)
				{
					_pledgeClass = RANK_VASSAL;
				}
				else
				{
					_pledgeClass = RANK_HEIR;
				}
				break;
			case 6:
				if(IS_LEADER)
				{
					_pledgeClass = RANK_BARON;
				}
				else if(IN_ACADEMY)
				{
					_pledgeClass = RANK_VASSAL;
				}
				else if(IS_GUARD_CAPTAIN)
				{
					_pledgeClass = RANK_WISEMAN;
				}
				else if(IS_GUARD)
				{
					_pledgeClass = RANK_HEIR;
				}
				else
				{
					_pledgeClass = RANK_KNIGHT;
				}
				break;
			case 7:
				if(IS_LEADER)
				{
					_pledgeClass = RANK_COUNT;
				}
				else if(IN_ACADEMY)
				{
					_pledgeClass = RANK_VASSAL;
				}
				else if(IS_GUARD_CAPTAIN)
				{
					_pledgeClass = RANK_VISCOUNT;
				}
				else if(IS_GUARD)
				{
					_pledgeClass = RANK_KNIGHT;
				}
				else if(IS_KNIGHT_COMMANDER)
				{
					_pledgeClass = RANK_BARON;
				}
				else if(IS_KNIGHT)
				{
					_pledgeClass = RANK_HEIR;
				}
				else
				{
					_pledgeClass = RANK_WISEMAN;
				}
				break;
			case 8:
				if(IS_LEADER)
				{
					_pledgeClass = RANK_MARQUIS;
				}
				else if(IN_ACADEMY)
				{
					_pledgeClass = RANK_VASSAL;
				}
				else if(IS_GUARD_CAPTAIN)
				{
					_pledgeClass = RANK_COUNT;
				}
				else if(IS_GUARD)
				{
					_pledgeClass = RANK_WISEMAN;
				}
				else if(IS_KNIGHT_COMMANDER)
				{
					_pledgeClass = RANK_VISCOUNT;
				}
				else if(IS_KNIGHT)
				{
					_pledgeClass = RANK_KNIGHT;
				}
				else
				{
					_pledgeClass = RANK_BARON;
				}
				break;
			case 9:
				if(IS_LEADER)
				{
					_pledgeClass = RANK_DUKE;
				}
				else if(IN_ACADEMY)
				{
					_pledgeClass = RANK_VASSAL;
				}
				else if(IS_GUARD_CAPTAIN)
				{
					_pledgeClass = RANK_MARQUIS;
				}
				else if(IS_GUARD)
				{
					_pledgeClass = RANK_BARON;
				}
				else if(IS_KNIGHT_COMMANDER)
				{
					_pledgeClass = RANK_COUNT;
				}
				else if(IS_KNIGHT)
				{
					_pledgeClass = RANK_WISEMAN;
				}
				else
				{
					_pledgeClass = RANK_VISCOUNT;
				}
				break;
			case 10:
				if(IS_LEADER)
				{
					_pledgeClass = RANK_GRAND_DUKE;
				}
				else if(IN_ACADEMY)
				{
					_pledgeClass = RANK_VASSAL;
				}
				else if(IS_GUARD)
				{
					_pledgeClass = RANK_VISCOUNT;
				}
				else if(IS_KNIGHT)
				{
					_pledgeClass = RANK_BARON;
				}
				else if(IS_GUARD_CAPTAIN)
				{
					_pledgeClass = RANK_DUKE;
				}
				else if(IS_KNIGHT_COMMANDER)
				{
					_pledgeClass = RANK_MARQUIS;
				}
				else
				{
					_pledgeClass = RANK_COUNT;
				}
				break;
			case 11:
				if(IS_LEADER)
				{
					_pledgeClass = RANK_DISTINGUISHED_KING;
				}
				else if(IN_ACADEMY)
				{
					_pledgeClass = RANK_VASSAL;
				}
				else if(IS_GUARD)
				{
					_pledgeClass = RANK_COUNT;
				}
				else if(IS_KNIGHT)
				{
					_pledgeClass = RANK_VISCOUNT;
				}
				else if(IS_GUARD_CAPTAIN)
				{
					_pledgeClass = RANK_GRAND_DUKE;
				}
				else if(IS_KNIGHT_COMMANDER)
				{
					_pledgeClass = RANK_DUKE;
				}
				else
				{
					_pledgeClass = RANK_MARQUIS;
				}
				break;
		}

		if(_hero && _pledgeClass < RANK_MARQUIS)
		{
			_pledgeClass = RANK_MARQUIS;
		}
		else if(_noble && _pledgeClass < RANK_BARON)
		{
			_pledgeClass = RANK_BARON;
		}
	}

	public void setPowerGrade(final int grade)
	{
		_powerGrade = grade;
	}

	public int getPowerGrade()
	{
		return _powerGrade;
	}

	public void setApprentice(final int apprentice)
	{
		_apprentice = apprentice;
	}

	public int getApprentice()
	{
		return _apprentice;
	}

	public int getSponsor()
	{
		return _clan == null ? 0 : _clan.getAnyMember(getObjectId()).getSponsor();
	}

	public int getNameColor()
	{
		if(isInObserverMode())
			return Color.black.getRGB();

		return _nameColor;
	}

	public void setNameColor(final int nameColor)
	{
		if(nameColor != Config.NORMAL_NAME_COLOUR && nameColor != Config.CLANLEADER_NAME_COLOUR && nameColor != Config.GM_NAME_COLOUR && nameColor != Config.SERVICES_OFFLINE_TRADE_NAME_COLOR)
		{
			setVar("namecolor", Integer.toHexString(nameColor), -1);
		}
		else if(nameColor == Config.NORMAL_NAME_COLOUR)
		{
			unsetVar("namecolor");
		}
		_nameColor = nameColor;
	}

	public void setNameColor(final int red, final int green, final int blue)
	{
		_nameColor = (red & 0xFF) + ((green & 0xFF) << 8) + ((blue & 0xFF) << 16);
		if(_nameColor != Config.NORMAL_NAME_COLOUR && _nameColor != Config.CLANLEADER_NAME_COLOUR && _nameColor != Config.GM_NAME_COLOUR && _nameColor != Config.SERVICES_OFFLINE_TRADE_NAME_COLOR)
		{
			setVar("namecolor", Integer.toHexString(_nameColor), -1);
		}
		else
		{
			unsetVar("namecolor");
		}
	}

	private final Map<String, String> user_variables = new ConcurrentHashMap<String, String>();

	public void setVar(String name, String value, long expirationTime)
	{
		user_variables.put(name, value);
		mysql.set("REPLACE INTO character_variables (obj_id, type, name, value, expire_time) VALUES (?,'user-var',?,?,?)", getObjectId(), name, value, expirationTime);
	}

	public void setVar(String name, int value, long expirationTime)
	{
		setVar(name, String.valueOf(value), expirationTime);
	}

	public void setVar(String name, long value, long expirationTime)
	{
		setVar(name, String.valueOf(value), expirationTime);
	}

	public void unsetVar(String name)
	{
		if(name == null)
			return;

		if(user_variables.remove(name) != null)
		{
			mysql.set("DELETE FROM `character_variables` WHERE `obj_id`=? AND `type`='user-var' AND `name`=? LIMIT 1", getObjectId(), name);
		}
	}

	public String getVar(String name)
	{
		return user_variables.get(name);
	}

	public boolean getVarB(String name, boolean defaultVal)
	{
		String var = user_variables.get(name);
		if(var == null)
			return defaultVal;
		return !(var.equals("0") || var.equalsIgnoreCase("false"));
	}

	public boolean getVarB(String name)
	{
		String var = user_variables.get(name);
		return !(var == null || var.equals("0") || var.equalsIgnoreCase("false"));
	}

	public long getVarLong(String name)
	{
		return getVarLong(name, 0L);
	}

	public long getVarLong(String name, long defaultVal)
	{
		long result = defaultVal;
		String var = getVar(name);
		if(var != null)
		{
			result = Long.parseLong(var);
		}
		return result;
	}

	public int getVarInt(String name)
	{
		return getVarInt(name, 0);
	}

	public int getVarInt(String name, int defaultVal)
	{
		int result = defaultVal;
		String var = getVar(name);
		if(var != null)
		{
			result = Integer.parseInt(var);
		}
		return result;
	}

	public Map<String, String> getVars()
	{
		return user_variables;
	}

	private void loadVariables()
	{
		Connection con = null;
		PreparedStatement offline = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			offline = con.prepareStatement("SELECT * FROM character_variables WHERE obj_id = ?");
			offline.setInt(1, getObjectId());
			rs = offline.executeQuery();
			while(rs.next())
			{
				String name = rs.getString("name");
				String value = Strings.stripSlashes(rs.getString("value"));
				user_variables.put(name, value);
				if(name.startsWith("Scheme_"))
				{
					Scheme scheme = new Scheme(name.substring(7));
					try
					{
						for(String str : value.split(";"))
						{
							String[] arrayOfString2 = str.split(",");
							int id = Integer.parseInt(arrayOfString2[0]);
							int lvl = Integer.parseInt(arrayOfString2[1]);
							scheme.addBuff(id, lvl);
						}
						setScheme(name, scheme);
					}
					catch(NullPointerException npe)
					{
						unsetVar(name);
						removeScheme(name);
						_log.info("Remove (" + name + ") Scheme in Community Buffer because is null");
					}
					catch(NumberFormatException nfe)
					{
						unsetVar(name);
						removeScheme(name);
						_log.info("Remove (" + name + ") Scheme in Community Buffer because is null");
					}
				}
			}

			// TODO Ð—Ð´ÐµÑ�ÑŒ Ð¾Ð±Ñ�Ð·Ñ�Ñ‚ÐµÐ»ÑŒÐ½Ð¾ Ð²Ñ‹Ñ�Ñ‚Ð°Ð²Ð»Ñ�Ñ‚ÑŒ Ð²Ñ�Ðµ Ñ�Ñ‚Ð°Ð½Ð´Ð°Ñ€Ñ‚Ð½Ñ‹Ðµ Ð¿Ð°Ñ€Ð°Ð¼ÐµÑ‚Ñ€Ñ‹,
			// Ð¸Ð½Ð°Ñ‡Ðµ Ð±ÑƒÐ´ÑƒÑ‚ NPE
			if(getVar("lang@") == null)
			{
				setVar("lang@", Config.DEFAULT_LANG, -1);
			}
			
			if(Config.SKILL_CHANCE_ENABLE_ON)
			{
				setVar("SkillsHideChance", 1, -1);
			}
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, offline, rs);
		}
	}

	public static String getVarFromPlayer(int objId, String var)
	{
		String value = null;
		Connection con = null;
		PreparedStatement offline = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			offline = con.prepareStatement("SELECT value FROM character_variables WHERE obj_id = ? AND name = ?");
			offline.setInt(1, objId);
			offline.setString(2, var);
			rs = offline.executeQuery();
			if(rs.next())
			{
				value = Strings.stripSlashes(rs.getString("value"));
			}
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, offline, rs);
		}
		return value;
	}


	public String getLang()
	{
		return getVar("lang@");
	}
	public String getSkillsHideChance()
	{
		return getVar("SkillsHideChance");
	}

	public int getLangId()
	{
		Player player = getPlayer();
		if (!player.isPhantom())
		{
		String lang = getLang();
		if(lang.equalsIgnoreCase("en") || lang.equalsIgnoreCase("e") || lang.equalsIgnoreCase("eng"))
			return LANG_ENG;
		if(lang.equalsIgnoreCase("ru") || lang.equalsIgnoreCase("r") || lang.equalsIgnoreCase("rus"))
			return LANG_RUS;
		return LANG_UNK;
		}
		else
			return LANG_RUS;
	}

	public Language getLanguage()
	{
		String lang = getLang();
		if(lang == null || lang.equalsIgnoreCase("en") || lang.equalsIgnoreCase("e") || lang.equalsIgnoreCase("eng"))
			return Language.ENGLISH;
		if(lang.equalsIgnoreCase("ru") || lang.equalsIgnoreCase("r") || lang.equalsIgnoreCase("rus"))
			return Language.RUSSIAN;
		return Language.ENGLISH;
	}

	public boolean isLangRus()
	{
		return getLangId() == LANG_RUS;
	}

	public int isAtWarWith(final Integer id)
	{
		return _clan == null || !_clan.isAtWarWith(id) ? 0 : 1;
	}

	public int isAtWar()
	{
		return _clan == null || _clan.isAtWarOrUnderAttack() <= 0 ? 0 : 1;
	}

	public void stopWaterTask()
	{
		if(_taskWater != null)
		{
			_taskWater.cancel(false);
			_taskWater = null;
			sendPacket(new SetupGauge(this, SetupGauge.CYAN, 0));
			sendChanges();
		}
	}

	public void startWaterTask()
	{
		if(isDead())
		{
			stopWaterTask();
		}
		else if(Config.ALLOW_WATER && _taskWater == null)
		{
			int timeinwater = (int) (calcStat(Stats.BREATH, 86, null, null) * 1000L);
			sendPacket(new SetupGauge(this, SetupGauge.CYAN, timeinwater));
			if(getTransformation() > 0 && getTransformationTemplate() > 0 && !isCursedWeaponEquipped())
			{
				setTransformation(0);
			}
			_taskWater = ThreadPoolManager.getInstance().scheduleAtFixedRate(new WaterTask(this), timeinwater, 1000L);
			sendChanges();
		}
	}

	public void doRevive(double percent)
	{
		restoreExp(percent);
		doRevive();
	}

	@Override
	public void doRevive()
	{
		super.doRevive();
		_deathtime = System.currentTimeMillis();
		setAgathionRes(false);
		unsetVar("lostexp");
		updateEffectIcons();
		autoShot();
	}

	public void reviveRequest(Player reviver, double percent, boolean pet)
	{
		ReviveAnswerListener reviveAsk = _askDialog != null && _askDialog.getValue() instanceof ReviveAnswerListener ? (ReviveAnswerListener) _askDialog.getValue() : null;
		if(reviveAsk != null)
		{
			if(reviveAsk.isForPet() == pet && reviveAsk.getPower() >= percent)
			{
				reviver.sendPacket(Msg.BETTER_RESURRECTION_HAS_BEEN_ALREADY_PROPOSED);
				return;
			}
			if(pet && !reviveAsk.isForPet())
			{
				reviver.sendPacket(Msg.SINCE_THE_MASTER_WAS_IN_THE_PROCESS_OF_BEING_RESURRECTED_THE_ATTEMPT_TO_RESURRECT_THE_PET_HAS_BEEN_CANCELLED);
				return;
			}
			if(pet && isDead())
			{
				reviver.sendPacket(Msg.WHILE_A_PET_IS_ATTEMPTING_TO_RESURRECT_IT_CANNOT_HELP_IN_RESURRECTING_ITS_MASTER);
				return;
			}
		}

		if(pet && getPet() != null && getPet().isDead() || !pet && isDead())
		{
			ConfirmDlg pkt = new ConfirmDlg(SystemMsg.C1_IS_MAKING_AN_ATTEMPT_TO_RESURRECT_YOU_IF_YOU_CHOOSE_THIS_PATH_S2_EXPERIENCE_WILL_BE_RETURNED_FOR_YOU, 0);
			pkt.addName(reviver).addString(Math.round(percent) + " percent");

			ask(pkt, new ReviveAnswerListener(this, percent, pet));
		}
	}
   
	public void summonCharacterRequest(final Creature summoner, final Location loc, final int summonConsumeCrystal)
	{
		ConfirmDlg cd = new ConfirmDlg(SystemMsg.C1_WISHES_TO_SUMMON_YOU_FROM_S2, 60000);
		cd.addName(summoner).addZoneName(loc);

		ask(cd, new SummonAnswerListener(this, loc, summonConsumeCrystal));
	}

	public void scriptRequest(String text, String scriptName, Object[] args)
	{
		ask(new ConfirmDlg(SystemMsg.S1, 30000).addString(text), new ScriptAnswerListener(this, scriptName, args));
	}

	public void updateNoChannel(final long time)
	{
		setNoChannel(time);

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();

			final String stmt = "UPDATE characters SET nochannel = ? WHERE obj_Id=?";
			statement = con.prepareStatement(stmt);
			statement.setLong(1, _NoChannel > 0 ? _NoChannel / 1000 : _NoChannel);
			statement.setInt(2, getObjectId());
			statement.executeUpdate();
		}
		catch(final Exception e)
		{
			_log.warn("Could not activate nochannel:" + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}

		sendPacket(new EtcStatusUpdate(this));
	}

	private void checkRecom()
	{
		Calendar temp = Calendar.getInstance();
		temp.set(Calendar.HOUR_OF_DAY, 6);
		temp.set(Calendar.MINUTE, 30);
		temp.set(Calendar.SECOND, 0);
		temp.set(Calendar.MILLISECOND, 0);
		long count = Math.round((System.currentTimeMillis() / 1000 - _lastAccess) / 86400);
		if(count == 0 && _lastAccess < temp.getTimeInMillis() / 1000 && System.currentTimeMillis() > temp.getTimeInMillis())
		{
			count++;
		}

		for(int i = 1; i < count; i++)
		{
			setRecomHave(getRecomHave() - 20);
		}

		if(count > 0)
		{
			restartRecom();
		}
	}

	public void restartRecom()
	{
		setRecomBonusTime(3600);
		setRecomLeftToday(0);
		setRecomLeft(20);
		setRecomHave(getRecomHave() - 20);
		stopRecomBonusTask(false);
		startRecomBonusTask();
		sendUserInfo(true);
		sendVoteSystemInfo();
	}

	@Override
	public boolean isInBoat()
	{
		return _boat != null;
	}

	public Boat getBoat()
	{
		return _boat;
	}

	public void setBoat(Boat boat)
	{
		_boat = boat;
	}

	public Location getInBoatPosition()
	{
		return _inBoatPosition;
	}

	public void setInBoatPosition(Location loc)
	{
		_inBoatPosition = loc;
	}

	public Map<Integer, SubClass> getSubClasses()
	{
		return _classlist;
	}

	public void setBaseClass(final int baseClass)
	{
		_baseClass = baseClass;
	}

	public int getBaseClassId()
	{
		return _baseClass;
	}

	public void setActiveClass(SubClass activeClass)
	{
		_activeClass = activeClass;
	}

	public SubClass getActiveClass()
	{
		return _activeClass;
	}

	public int getActiveClassId()
	{
		return getActiveClass().getClassId();
	}

	/**
	 * Changing index of class in DB, used for changing class when finished
	 * professional quests
	 * 
	 * @param oldclass
	 * @param newclass
	 */
	public synchronized void changeClassInDb(final int oldclass, final int newclass)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE character_subclasses SET class_id=? WHERE char_obj_id=? AND class_id=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("UPDATE character_hennas SET class_index=? WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE object_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("UPDATE character_shortcuts SET class_index=? WHERE object_id=? AND class_index=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("UPDATE character_skills SET class_index=? WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("DELETE FROM character_effects_save WHERE object_id=? AND id=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("UPDATE character_effects_save SET id=? WHERE object_id=? AND id=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newclass);
			statement.executeUpdate();
			DbUtils.close(statement);

			statement = con.prepareStatement("UPDATE character_skills_save SET class_index=? WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DbUtils.close(statement);
		}
		catch(final SQLException e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	/**
	 * Ð¡Ð¾Ñ…Ñ€Ð°Ð½Ñ�ÐµÑ‚ Ð¸Ð½Ñ„Ð¾Ñ€Ð¼Ð°Ñ†Ð¸ÑŽ Ð¾ ÐºÐ»Ð°Ñ�Ñ�Ð°Ñ… Ð² Ð‘Ð”
	 */
	public void storeCharSubClasses()
	{
		SubClass main = getActiveClass();
		if(main != null)
		{
			main.setCp(getCurrentCp());
			// main.setExp(getExp());
			// main.setLevel(getLevel());
			// main.setSp(getSp());
			main.setHp(getCurrentHp());
			main.setMp(getCurrentMp());
			main.setActive(true);
			getSubClasses().put(getActiveClassId(), main);
		}
		else
		{
			_log.warn("Could not store char sub data, main class " + getActiveClassId() + " not found for " + this);
		}

		Connection con = null;
		Statement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();

			StringBuilder sb;
			for(SubClass subClass : getSubClasses().values())
			{
				sb = new StringBuilder("UPDATE character_subclasses SET ");
				sb.append("exp=").append(subClass.getExp()).append(",");
				sb.append("sp=").append(subClass.getSp()).append(",");
				sb.append("curHp=").append(subClass.getHp()).append(",");
				sb.append("curMp=").append(subClass.getMp()).append(",");
				sb.append("curCp=").append(subClass.getCp()).append(",");
				sb.append("level=").append(subClass.getLevel()).append(",");
				sb.append("active=").append(subClass.isActive() ? 1 : 0).append(",");
				sb.append("isBase=").append(subClass.isBase() ? 1 : 0).append(",");
				sb.append("death_penalty=").append(subClass.getDeathPenalty(this).getLevelOnSaveDB()).append(",");
				sb.append("certification='").append(subClass.getCertification()).append("'");
				sb.append(" WHERE char_obj_id=").append(getObjectId()).append(" AND class_id=").append(subClass.getClassId()).append(" LIMIT 1");
				statement.executeUpdate(sb.toString());
			}

			sb = new StringBuilder("UPDATE character_subclasses SET ");
			sb.append("maxHp=").append(getMaxHp()).append(",");
			sb.append("maxMp=").append(getMaxMp()).append(",");
			sb.append("maxCp=").append(getMaxCp());
			sb.append(" WHERE char_obj_id=").append(getObjectId()).append(" AND active=1 LIMIT 1");
			statement.executeUpdate(sb.toString());
		}
		catch(final Exception e)
		{
			_log.warn("Could not store char sub data: " + e);
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	/**
	 * Restore list of character professions and set up active proof Used when
	 * character is loading
	 */
	public static void restoreCharSubClasses(final Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT class_id,exp,sp,curHp,curCp,curMp,active,isBase,death_penalty,certification FROM character_subclasses WHERE char_obj_id=?");
			statement.setInt(1, player.getObjectId());
			rset = statement.executeQuery();

			SubClass activeSubclass = null;
			while(rset.next())
			{
				final SubClass subClass = new SubClass();
				subClass.setBase(rset.getInt("isBase") != 0);
				subClass.setClassId(rset.getInt("class_id"));
				subClass.setExp(rset.getLong("exp"));
				subClass.setSp(rset.getInt("sp"));
				subClass.setHp(rset.getDouble("curHp"));
				subClass.setMp(rset.getDouble("curMp"));
				subClass.setCp(rset.getDouble("curCp"));
				subClass.setDeathPenalty(new DeathPenalty(player, rset.getInt("death_penalty")));
				subClass.setCertification(rset.getInt("certification"));

				boolean active = rset.getInt("active") != 0;
				if(active)
				{
					activeSubclass = subClass;
				}
				player.getSubClasses().put(subClass.getClassId(), subClass);
			}

			if(player.getSubClasses().size() == 0)
				throw new Exception("There are no one subclass for player: " + player);

			int BaseClassId = player.getBaseClassId();
			if(BaseClassId == -1)
				throw new Exception("There are no base subclass for player: " + player);

			if(activeSubclass != null)
			{
				player.setActiveSubClass(activeSubclass.getClassId(), false);
			}

			if(player.getActiveClass() == null)
			{
				// ÐµÑ�Ð»Ð¸ Ð¸Ð·-Ð·Ð° ÐºÐ°ÐºÐ¾Ð³Ð¾-Ð»Ð¸Ð±Ð¾ Ñ�Ð±Ð¾Ñ� Ð½Ð¸ Ð¾Ð´Ð¸Ð½ Ð¸Ð· Ñ�Ð°Ð±ÐºÐ»Ð°Ñ�Ð¾Ð² Ð½Ðµ Ð¾Ñ‚Ð¼ÐµÑ‡ÐµÐ½
				// ÐºÐ°Ðº Ð°ÐºÑ‚Ð¸Ð²Ð½Ñ‹Ð¹ Ð¿Ð¾Ð¼ÐµÑ‡Ð°ÐµÐ¼ Ð±Ð°Ð·Ð¾Ð²Ñ‹Ð¹ ÐºÐ°Ðº Ð°ÐºÑ‚Ð¸Ð²Ð½Ñ‹Ð¹
				final SubClass subClass = player.getSubClasses().get(BaseClassId);
				subClass.setActive(true);
				player.setActiveSubClass(subClass.getClassId(), false);
			}
		}
		catch(final Exception e)
		{
			_log.warn("Could not restore char sub-classes: " + e);
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	/**
	 * Ð”Ð¾Ð±Ð°Ð²Ð¸Ñ‚ÑŒ ÐºÐ»Ð°Ñ�Ñ�, Ð¸Ñ�Ð¿Ð¾Ð»ÑŒÐ·ÑƒÐµÑ‚Ñ�Ñ� Ñ‚Ð¾Ð»ÑŒÐºÐ¾ Ð´Ð»Ñ� Ñ�Ð°Ð±ÐºÐ»Ð°Ñ�Ñ�Ð¾Ð²
	 * 
	 * @param storeOld
	 * @param certification
	 */
	public boolean addSubClass(final int classId, boolean storeOld, int certification)
	{
		if(_classlist.size() >= 4)
			return false;

		final ClassId newId = ClassId.VALUES[classId];

		final SubClass newClass = new SubClass();
		newClass.setBase(false);
		if(newId.getRace() == null)
			return false;

		newClass.setClassId(classId);
		newClass.setCertification(certification);

		_classlist.put(classId, newClass);

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			// Store the basic info about this new sub-class.
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO character_subclasses (char_obj_id, class_id, exp, sp, curHp, curMp, curCp, maxHp, maxMp, maxCp, level, active, isBase, death_penalty, certification) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newClass.getClassId());
			statement.setLong(3, Experience.LEVEL[40]);
			statement.setInt(4, 0);
			statement.setDouble(5, getCurrentHp());
			statement.setDouble(6, getCurrentMp());
			statement.setDouble(7, getCurrentCp());
			statement.setDouble(8, getCurrentHp());
			statement.setDouble(9, getCurrentMp());
			statement.setDouble(10, getCurrentCp());
			statement.setInt(11, 40);
			statement.setInt(12, 0);
			statement.setInt(13, 0);
			statement.setInt(14, 0);
			statement.setInt(15, certification);
			statement.execute();
		}
		catch(final Exception e)
		{
			_log.warn("Could not add character sub-class: " + e, e);
			return false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}

		setActiveSubClass(classId, storeOld);

		boolean countUnlearnable = true;
		int unLearnable = 0;

		Collection<SkillLearn> skills = SkillAcquireHolder.getInstance().getAvailableSkills(this, AcquireType.NORMAL);
		while(skills.size() > unLearnable)
		{
			for(final SkillLearn s : skills)
			{
				final Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
				if(sk == null || !sk.getCanLearn(newId))
				{
					if(countUnlearnable)
					{
						unLearnable++;
					}
					continue;
				}
				addSkill(sk, true);
			}
			countUnlearnable = false;
			skills = SkillAcquireHolder.getInstance().getAvailableSkills(this, AcquireType.NORMAL);
		}

		sendPacket(new SkillList(this));
		setCurrentHpMp(getMaxHp(), getMaxMp(), true);
		setCurrentCp(getMaxCp());
		return true;
	}

	/**
	 * Ð£Ð´Ð°Ð»Ñ�ÐµÑ‚ Ð²Ñ�ÑŽ Ð¸Ð½Ñ„Ð¾Ñ€Ð¼Ð°Ñ†Ð¸ÑŽ Ð¾ ÐºÐ»Ð°Ñ�Ñ�Ðµ Ð¸ Ð´Ð¾Ð±Ð°Ð²Ð»Ñ�ÐµÑ‚ Ð½Ð¾Ð²ÑƒÑŽ, Ñ‚Ð¾Ð»ÑŒÐºÐ¾ Ð´Ð»Ñ� Ñ�Ð°Ð±ÐºÐ»Ð°Ñ�Ñ�Ð¾Ð²
	 */
	public boolean modifySubClass(final int oldClassId, final int newClassId)
	{
		final SubClass originalClass = _classlist.get(oldClassId);
		if(originalClass == null || originalClass.isBase())
			return false;

		final int certification = originalClass.getCertification();

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			// Remove all basic info stored about this sub-class.
			statement = con.prepareStatement("DELETE FROM character_subclasses WHERE char_obj_id=? AND class_id=? AND isBase = 0");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);

			// Remove all skill info stored for this sub-class.
			statement = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=? AND class_index=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);

			// Remove all saved skills info stored for this sub-class.
			statement = con.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id=? AND class_index=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);

			// Remove all saved effects stored for this sub-class.
			statement = con.prepareStatement("DELETE FROM character_effects_save WHERE object_id=? AND id=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);

			// Remove all henna info stored for this sub-class.
			statement = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=? AND class_index=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);

			// Remove all shortcuts info stored for this sub-class.
			statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE object_id=? AND class_index=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DbUtils.close(statement);
		}
		catch(final Exception e)
		{
			_log.warn("Could not delete char sub-class: " + e);
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		_classlist.remove(oldClassId);

		return newClassId <= 0 || addSubClass(newClassId, false, certification);
	}

	/**
	 * Ð ÐˆÐ¡ÐƒÐ¡â€šÐ Â°Ð Ð…Ð Â°Ð Ð†Ð Â»Ð Ñ‘Ð Ð†Ð Â°Ð ÂµÐ¡â€š Ð Â°Ð Ñ”Ð¡â€šÐ Ñ‘Ð Ð†Ð Ð…Ð¡â€¹Ð â„– Ð¡ÐƒÐ Â°Ð Â±Ð Ñ”Ð Â»Ð Â°Ð¡ÐƒÐ¡Ðƒ
	 * <p/>
	 * <li>Retrieve from the database all skills of this L2Player and add them
	 * to _skills</li>
	 * <li>Retrieve from the database all macroses of this L2Player and add them
	 * to _macroses</li>
	 * <li>Retrieve from the database all shortCuts of this L2Player and add
	 * them to _shortCuts</li><BR>
	 * <BR>
	 */
	public void setActiveSubClass(final int subId, final boolean store)
	{
		final SubClass sub = getSubClasses().get(subId);
		if(sub == null)
			return;

		if(getActiveClass() != null)
		{
			EffectsDAO.getInstance().insert(this);
			storeDisableSkills();

			if(QuestManager.getQuest(422) != null)
			{
				String qn = QuestManager.getQuest(422).getName();
				if(qn != null)
				{
					QuestState qs = getQuestState(qn);
					if(qs != null)
					{
						qs.exitQuest(true);
					}
				}
			}
		}

		if(store)
		{
			final SubClass oldsub = getActiveClass();
			oldsub.setCp(getCurrentCp());
			// oldsub.setExp(getExp());
			// oldsub.setLevel(getLevel());
			// oldsub.setSp(getSp());
			oldsub.setHp(getCurrentHp());
			oldsub.setMp(getCurrentMp());
			oldsub.setActive(false);
			getSubClasses().put(getActiveClassId(), oldsub);
		}

		sub.setActive(true);
		setActiveClass(sub);
		getSubClasses().put(getActiveClassId(), sub);

		setClassId(subId, false, false);

		removeAllSkills();

		getEffectList().stopAllEffects();

		if(getPet() != null && (getPet().isSummon() || Config.ALT_IMPROVED_PETS_LIMITED_USE && (getPet().getNpcId() == PetDataTable.IMPROVED_BABY_KOOKABURRA_ID && !isMageClass() || getPet().getNpcId() == PetDataTable.IMPROVED_BABY_BUFFALO_ID && isMageClass())))
		{
			getPet().unSummon();
		}

		setAgathion(0);

		restoreSkills();
		rewardSkills(false);
		checkSkills();
		sendPacket(new ExStorageMaxCount(this));

		refreshExpertisePenalty();

		sendPacket(new SkillList(this));

		getInventory().refreshEquip();
		getInventory().validateItems();

		for(int i = 0; i < 3; i++)
		{
			_henna[i] = null;
		}

		restoreHenna();
		sendPacket(new HennaInfo(this));

		EffectsDAO.getInstance().restoreEffects(this);
		restoreDisableSkills();

		setCurrentHpMp(sub.getHp(), sub.getMp());
		setCurrentCp(sub.getCp());

		_shortCuts.restore();
		sendPacket(new ShortCutInit(this));
		for(int shotId : getAutoSoulShot())
		{
			sendPacket(new ExAutoSoulShot(shotId, true));
		}
		sendPacket(new SkillCoolTime(this));

		broadcastPacket(new SocialAction(getObjectId(), SocialAction.LEVEL_UP));

		getDeathPenalty().restore(this);

		setIncreasedForce(0);

		startHourlyTask();

		broadcastCharInfo();
		updateEffectIcons();
		updateStats();
	}

	/**
	 * Ð§ÐµÑ€ÐµÐ· delay Ð¼Ð¸Ð»Ð»Ð¸Ñ�ÐµÐºÑƒÐ½Ð´ Ð²Ñ‹Ð±Ñ€Ð¾Ñ�Ð¸Ñ‚ Ð¸Ð³Ñ€Ð¾ÐºÐ° Ð¸Ð· Ð¸Ð³Ñ€Ñ‹
	 */
	public void startKickTask(long delayMillis)
	{
		stopKickTask();
		_kickTask = ThreadPoolManager.getInstance().schedule(new KickTask(this), delayMillis);
	}

	public void stopKickTask()
	{
		if(_kickTask != null)
		{
			_kickTask.cancel(false);
			_kickTask = null;
		}
	}

	public void startBonusTask()
	{
		if(Config.SERVICES_RATE_TYPE != Bonus.NO_BONUS)
		{
			int bonusExpire = getNetConnection().getBonusExpire();
			double bonus = getNetConnection().getBonus();
			if(bonusExpire > System.currentTimeMillis() / 1000L)
			{
				getBonus().setRateXp(Config.SERVICES_BONUS_XP * bonus);
				getBonus().setRateSp(Config.SERVICES_BONUS_SP * bonus);
				getBonus().setDropAdena(Config.SERVICES_BONUS_ADENA * bonus);
				getBonus().setDropItems(Config.SERVICES_BONUS_ITEMS * bonus);
				getBonus().setDropSpoil(Config.SERVICES_BONUS_SPOIL *bonus);

				getBonus().setBonusExpire(bonusExpire);

				if(_bonusExpiration == null)
				{
					_bonusExpiration = LazyPrecisionTaskManager.getInstance().startBonusExpirationTask(this);
				}
				broadcastUserInfo(true);
			}
			else if(bonus > 0 && Config.SERVICES_RATE_TYPE == Bonus.BONUS_GLOBAL_ON_GAMESERVER)
			{
				AccountBonusDAO.getInstance().delete(getAccountName());
			}
			else
			{
				LoginServerCommunication.getInstance().sendPacket(new BonusRequest(getAccountName(), 0, 0));
			}

			broadcastUserInfo(true);

			//Functions i = new Functions();
			//i.show(HtmCache.getInstance().getNotNull("scripts/services/RateBonusGet.htm", this), this);
		}
	}

	public void stopBonusTask()
	{
		if(_bonusExpiration != null)
		{
			_bonusExpiration.cancel(false);
			_bonusExpiration = null;
			LazyPrecisionTaskManager.getInstance().startBonusExpirationTask(this);
			broadcastUserInfo(true);
		}
	}

	@Override
	public int getInventoryLimit()
	{
		return (int) calcStat(Stats.INVENTORY_LIMIT, 0, null, null);
	}

	public int getWarehouseLimit()
	{
		return (int) calcStat(Stats.STORAGE_LIMIT, 0, null, null);
	}

	public int getTradeLimit()
	{
		return (int) calcStat(Stats.TRADE_LIMIT, 0, null, null);
	}

	public int getDwarvenRecipeLimit()
	{
		return (int) calcStat(Stats.DWARVEN_RECIPE_LIMIT, 50, null, null) + Config.ALT_ADD_RECIPES;
	}

	public int getCommonRecipeLimit()
	{
		return (int) calcStat(Stats.COMMON_RECIPE_LIMIT, 50, null, null) + Config.ALT_ADD_RECIPES;
	}

	/**
	 * Ð’Ð¾Ð·Ð²Ñ€Ð°Ñ‰Ð°ÐµÑ‚ Ñ‚Ð¸Ð¿ Ð°Ñ‚Ð°ÐºÑƒÑŽÑ‰ÐµÐ³Ð¾ Ñ�Ð»ÐµÐ¼ÐµÐ½Ñ‚Ð°
	 */
	public Element getAttackElement()
	{
		return Formulas.getAttackElement(this, null);
	}

	/**
	 * Ð’Ð¾Ð·Ð²Ñ€Ð°Ñ‰Ð°ÐµÑ‚ Ñ�Ð¸Ð»Ñƒ Ð°Ñ‚Ð°ÐºÐ¸ Ñ�Ð»ÐµÐ¼ÐµÐ½Ñ‚Ð°
	 * 
	 * @return Ð·Ð½Ð°Ñ‡ÐµÐ½Ð¸Ðµ Ð°Ñ‚Ð°ÐºÐ¸
	 */
	public int getAttack(Element element)
	{
		if(element == Element.NONE)
			return 0;
		return (int) calcStat(element.getAttack(), 0., null, null);
	}

	/**
	 * Ð’Ð¾Ð·Ð²Ñ€Ð°Ñ‰Ð°ÐµÑ‚ Ð·Ð°Ñ‰Ð¸Ñ‚Ñƒ Ð¾Ñ‚ Ñ�Ð»ÐµÐ¼ÐµÐ½Ñ‚Ð°
	 * 
	 * @return Ð·Ð½Ð°Ñ‡ÐµÐ½Ð¸Ðµ Ð·Ð°Ñ‰Ð¸Ñ‚Ñ‹
	 */
	public int getDefence(Element element)
	{
		if(element == Element.NONE)
			return 0;
		return (int) calcStat(element.getDefence(), 0., null, null);
	}

	public boolean getAndSetLastItemAuctionRequest()
	{
		if(_lastItemAuctionInfoRequest + 2000L < System.currentTimeMillis())
		{
			_lastItemAuctionInfoRequest = System.currentTimeMillis();
			return true;
		}
		else
		{
			_lastItemAuctionInfoRequest = System.currentTimeMillis();
			return false;
		}
	}

	@Override
	public int getNpcId()
	{
		return -2;
	}

	public GameObject getVisibleObject(int id)
	{
		if(getObjectId() == id)
			return this;

		GameObject target = null;

		if(getTargetId() == id)
		{
			target = getTarget();
		}

		if(target == null && _party != null)
		{
			for(Player p : _party.getPartyMembers())
				if(p != null && p.getObjectId() == id)
				{
					target = p;
					break;
				}
		}

		if(target == null)
		{
			target = World.getAroundObjectById(this, id);
		}

		return target == null || target.isInvisible() ? null : target;
	}

	@Override
	public int getPAtk(final Creature target)
	{
		double init = getActiveWeaponInstance() == null ? isMageClass() ? 3 : 4 : 0;
		return (int) calcStat(Stats.POWER_ATTACK, init, target, null);
	}

	@Override
	public int getPDef(final Creature target)
	{
		double init = 4.; // empty cloak and underwear slots

		final ItemInstance chest = getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
		if(chest == null)
		{
			init += isMageClass() ? ArmorTemplate.EMPTY_BODY_MYSTIC : ArmorTemplate.EMPTY_BODY_FIGHTER;
		}
		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS) == null && (chest == null || chest.getBodyPart() != ItemTemplate.SLOT_FULL_ARMOR))
		{
			init += isMageClass() ? ArmorTemplate.EMPTY_LEGS_MYSTIC : ArmorTemplate.EMPTY_LEGS_FIGHTER;
		}

		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_HEAD) == null)
		{
			init += ArmorTemplate.EMPTY_HELMET;
		}
		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_GLOVES) == null)
		{
			init += ArmorTemplate.EMPTY_GLOVES;
		}
		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_FEET) == null)
		{
			init += ArmorTemplate.EMPTY_BOOTS;
		}

		return (int) calcStat(Stats.POWER_DEFENCE, init, target, null);
	}

	@Override
	public int getMDef(final Creature target, final Skill skill)
	{
		double init = 0.;

		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEAR) == null)
		{
			init += ArmorTemplate.EMPTY_EARRING;
		}
		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_REAR) == null)
		{
			init += ArmorTemplate.EMPTY_EARRING;
		}
		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_NECK) == null)
		{
			init += ArmorTemplate.EMPTY_NECKLACE;
		}
		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_LFINGER) == null)
		{
			init += ArmorTemplate.EMPTY_RING;
		}
		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_RFINGER) == null)
		{
			init += ArmorTemplate.EMPTY_RING;
		}

		return (int) calcStat(Stats.MAGIC_DEFENCE, init, target, skill);
	}

	public boolean isSubClassActive()
	{
		return getBaseClassId() != getActiveClassId();
	}

	@Override
	public String getTitle()
	{
		return super.getTitle();
	}

	public int getTitleColor()
	{
		return _titlecolor;
	}

	public void setTitleColor(final int titlecolor)
	{
		if(titlecolor != DEFAULT_TITLE_COLOR)
		{
			setVar("titlecolor", Integer.toHexString(titlecolor), -1);
		}
		else
		{
			unsetVar("titlecolor");
		}
		_titlecolor = titlecolor;
	}

	@Override
	public boolean isCursedWeaponEquipped()
	{
		return _cursedWeaponEquippedId != 0;
	}

	public void setCursedWeaponEquippedId(int value)
	{
		_cursedWeaponEquippedId = value;
	}

	public int getCursedWeaponEquippedId()
	{
		return _cursedWeaponEquippedId;
	}

	@Override
	public boolean isImmobilized()
	{
		return super.isImmobilized() || isOverloaded() || isSitting() || isFishing();
	}

	@Override
	public boolean isBlocked()
	{
		return super.isBlocked() || isInMovie() || isInObserverMode() || isTeleporting() || isLogoutStarted();
	}

	@Override
	public boolean isInvul()
	{
		return super.isInvul() || isInMovie();
	}

	/**
	 * if True, the L2Player can't take more item
	 */
	public void setOverloaded(boolean overloaded)
	{
		_overloaded = overloaded;
	}

	public boolean isOverloaded()
	{
		return _overloaded;
	}

	public boolean isFishing()
	{
		return _isFishing;
	}

	public Fishing getFishing()
	{
		return _fishing;
	}

	public void setFishing(boolean value)
	{
		_isFishing = value;
	}

	public void startFishing(FishTemplate fish, int lureId)
	{
		_fishing.setFish(fish);
		_fishing.setLureId(lureId);
		_fishing.startFishing();
	}

	public void stopFishing()
	{
		_fishing.stopFishing();
	}

	public Location getFishLoc()
	{
		return _fishing.getFishLoc();
	}

	public Bonus getBonus()
	{
		return _bonus;
	}

	public boolean hasBonus()
	{
		return _bonus.getBonusExpire() > System.currentTimeMillis() / 1000L;
	}

	public void checkBonus()
	{
		//		int bonusExpire = getNetConnection().getBonusExpire();
		//		if(bonusExpire <= System.currentTimeMillis() / 1000L)
		//		{
		//			stopBonusTask();
		//		}

		if(!hasBonus())
			stopBonusTask();
	}

	@Override
	public double getRateAdena()
	{
		checkBonus();
		return _party == null ? _bonus.getDropAdena() : _party._rateAdena;
	}

	@Override
	public double getRateItems()
	{
		checkBonus();
		return _party == null ? _bonus.getDropItems() : _party._rateDrop;
	}

	@Override
	public double getRateExp()
	{
		checkBonus();
		return calcStat(Stats.EXP, _party == null ? _bonus.getRateXp() : _party._rateExp, null, null);
	}

	@Override
	public double getRateSp()
	{
		checkBonus();
		return calcStat(Stats.SP, _party == null ? _bonus.getRateSp() : _party._rateSp, null, null);
	}

	@Override
	public double getRateSpoil()
	{
		checkBonus();
		return _party == null ? _bonus.getDropSpoil() : _party._rateSpoil;
	}

	private boolean _maried = false;
	private int _partnerId = 0;
	private int _coupleId = 0;
	private boolean _maryrequest = false;
	private boolean _maryaccepted = false;

	public boolean isMaried()
	{
		return _maried;
	}

	public void setMaried(boolean state)
	{
		_maried = state;
	}

	public void setMaryRequest(boolean state)
	{
		_maryrequest = state;
	}

	public boolean isMaryRequest()
	{
		return _maryrequest;
	}

	public void setMaryAccepted(boolean state)
	{
		_maryaccepted = state;
	}

	public boolean isMaryAccepted()
	{
		return _maryaccepted;
	}

	public int getPartnerId()
	{
		return _partnerId;
	}

	public void setPartnerId(int partnerid)
	{
		_partnerId = partnerid;
	}

	public int getCoupleId()
	{
		return _coupleId;
	}

	public void setCoupleId(int coupleId)
	{
		_coupleId = coupleId;
	}

	public void setUndying(boolean val)
	{
		if(!isGM())
			return;
		_isUndying = val;
	}

	public boolean isUndying()
	{
		return _isUndying;
	}

	private GArray<Player> _snoopListener = new GArray<>();
	private GArray<Player> _snoopedPlayer = new GArray<>();

	public void broadcastSnoop(int type, String name, String _text)
	{
		if(_snoopListener.size() > 0)
		{
			Snoop sn = new Snoop(getObjectId(), getName(), type, name, _text);
			for(Player pci : _snoopListener)
				if(pci != null)
					pci.sendPacket(sn);
		}
	}
	public void addSnooper(Player pci)
	{
		if(!_snoopListener.contains(pci))
			_snoopListener.add(pci);
	}
	public void removeSnooper(Player pci)
	{
		_snoopListener.remove(pci);
	}
	public void removeSnooped(Player pci)
	{
		_snoopedPlayer.remove(pci);
	}
	public void addSnooped(Player pci)
	{
		if(!_snoopedPlayer.contains(pci))
			_snoopedPlayer.add(pci);
	}
	public void resetReuse()
	{
		_skillReuses.clear();
		_sharedGroupReuses.clear();
	}

	public DeathPenalty getDeathPenalty()
	{
		return _activeClass == null ? null : _activeClass.getDeathPenalty(this);
	}

	private boolean _charmOfCourage = false;

	public boolean isCharmOfCourage()
	{
		return _charmOfCourage;
	}

	public void setCharmOfCourage(boolean val)
	{
		_charmOfCourage = val;

		if(!val)
		{
			getEffectList().stopEffect(Skill.SKILL_CHARM_OF_COURAGE);
		}

		sendEtcStatusUpdate();
	}

	private int _increasedForce = 0;
	private int _consumedSouls = 0;

	@Override
	public int getIncreasedForce()
	{
		return _increasedForce;
	}

	@Override
	public int getConsumedSouls()
	{
		return _consumedSouls;
	}

	@Override
	public void setConsumedSouls(int i, NpcInstance monster)
	{
		if(i == _consumedSouls)
			return;

		int max = (int) calcStat(Stats.SOULS_LIMIT, 0, monster, null);

		if(i > max)
		{
			i = max;
		}

		if(i <= 0)
		{
			_consumedSouls = 0;
			sendEtcStatusUpdate();
			return;
		}

		if(_consumedSouls != i)
		{
			int diff = i - _consumedSouls;
			if(diff > 0)
			{
				SystemMessage sm = new SystemMessage(SystemMessage.YOUR_SOUL_HAS_INCREASED_BY_S1_SO_IT_IS_NOW_AT_S2);
				sm.addNumber(diff);
				sm.addNumber(i);
				sendPacket(sm);
			}
		}
		else if(max == i)
		{
			sendPacket(Msg.SOUL_CANNOT_BE_ABSORBED_ANY_MORE);
			return;
		}

		_consumedSouls = i;
		sendPacket(new EtcStatusUpdate(this));
	}

	@Override
	public void setIncreasedForce(int i)
	{
		i = Math.min(i, Charge.MAX_CHARGE);
		i = Math.max(i, 0);

		if(i != 0 && i > _increasedForce)
		{
			sendPacket(new SystemMessage(SystemMessage.YOUR_FORCE_HAS_INCREASED_TO_S1_LEVEL).addNumber(i));
		}

		_increasedForce = i;
		sendEtcStatusUpdate();
	}

	private long _lastFalling;

	public boolean isFalling()
	{
		return System.currentTimeMillis() - _lastFalling < 5000;
	}

	public void falling(int height)
	{
		if(!Config.DAMAGE_FROM_FALLING || isDead() || isFlying() || isInWater() || isInBoat())
			return;
		_lastFalling = System.currentTimeMillis();
		int damage = (int) calcStat(Stats.FALL, getMaxHp() / 2000 * height, null, null);
		if(damage > 0)
		{
			int curHp = (int) getCurrentHp();
			if(curHp - damage < 1)
			{
				setCurrentHp(1, false);
			}
			else
			{
				setCurrentHp(curHp - damage, false);
			}
			sendPacket(new SystemMessage(SystemMessage.YOU_RECEIVED_S1_DAMAGE_FROM_TAKING_A_HIGH_FALL).addNumber(damage));
		}
	}

	/**
	 * Ð¡Ð¸Ñ�Ñ‚ÐµÐ¼Ð½Ñ‹Ðµ Ñ�Ð¾Ð¾Ð±Ñ‰ÐµÐ½Ð¸Ñ� Ð¾ Ñ‚ÐµÐºÑƒÑ‰ÐµÐ¼ Ñ�Ð¾Ñ�Ñ‚Ð¾Ñ�Ð½Ð¸Ð¸ Ñ…Ð¿
	 */
	@Override
	public void checkHpMessages(double curHp, double newHp)
	{
		// Ñ�ÑŽÐ´Ð° Ð¿Ð°Ñ�Ð¸Ð²Ð½Ñ‹Ðµ Ñ�ÐºÐ¸Ð»Ð»Ñ‹
		int[] _hp = { 30, 30 };
		int[] skills = { 290, 291 };

		// Ñ�ÑŽÐ´Ð° Ð°ÐºÑ‚Ð¸Ð²Ð½Ñ‹Ðµ Ñ�Ñ„Ñ„ÐµÐºÑ‚Ñ‹
		int[] _effects_skills_id = { 139, 176, 292, 292, 420 };
		int[] _effects_hp = { 30, 30, 30, 60, 30 };

		double percent = getMaxHp() / 100;
		double _curHpPercent = curHp / percent;
		double _newHpPercent = newHp / percent;
		boolean needsUpdate = false;

		// check for passive skills
		for(int i = 0; i < skills.length; i++)
		{
			int level = getSkillLevel(skills[i]);
			if(level > 0)
				if(_curHpPercent > _hp[i] && _newHpPercent <= _hp[i])
				{
					sendPacket(new SystemMessage(SystemMessage.SINCE_HP_HAS_DECREASED_THE_EFFECT_OF_S1_CAN_BE_FELT).addSkillName(skills[i], level));
					needsUpdate = true;
				}
				else if(_curHpPercent <= _hp[i] && _newHpPercent > _hp[i])
				{
					sendPacket(new SystemMessage(SystemMessage.SINCE_HP_HAS_INCREASED_THE_EFFECT_OF_S1_WILL_DISAPPEAR).addSkillName(skills[i], level));
					needsUpdate = true;
				}
		}

		// check for active effects
		for(Integer i = 0; i < _effects_skills_id.length; i++)
			if(getEffectList().getEffectsBySkillId(_effects_skills_id[i]) != null)
				if(_curHpPercent > _effects_hp[i] && _newHpPercent <= _effects_hp[i])
				{
					sendPacket(new SystemMessage(SystemMessage.SINCE_HP_HAS_DECREASED_THE_EFFECT_OF_S1_CAN_BE_FELT).addSkillName(_effects_skills_id[i], 1));
					needsUpdate = true;
				}
				else if(_curHpPercent <= _effects_hp[i] && _newHpPercent > _effects_hp[i])
				{
					sendPacket(new SystemMessage(SystemMessage.SINCE_HP_HAS_INCREASED_THE_EFFECT_OF_S1_WILL_DISAPPEAR).addSkillName(_effects_skills_id[i], 1));
					needsUpdate = true;
				}

		if(needsUpdate)
		{
			sendChanges();
		}
	}

	/**
	 * Ð¡Ð¸Ñ�Ñ‚ÐµÐ¼Ð½Ñ‹Ðµ Ñ�Ð¾Ð¾Ð±Ñ‰ÐµÐ½Ð¸Ñ� Ð´Ð»Ñ� Ñ‚ÐµÐ¼Ð½Ñ‹Ñ… Ñ�Ð»ÑŒÑ„Ð¾Ð² Ð¾ Ð²ÐºÐ»/Ð²Ñ‹ÐºÐ» ShadowSence (skill id =
	 * 294)
	 */
	public void checkDayNightMessages()
	{
		int level = getSkillLevel(294);
		if(level > 0)
			if(GameTimeController.getInstance().isNowNight())
			{
				sendPacket(new SystemMessage(SystemMessage.IT_IS_NOW_MIDNIGHT_AND_THE_EFFECT_OF_S1_CAN_BE_FELT).addSkillName(294, level));
			}
			else
			{
				sendPacket(new SystemMessage(SystemMessage.IT_IS_DAWN_AND_THE_EFFECT_OF_S1_WILL_NOW_DISAPPEAR).addSkillName(294, level));
			}
		sendChanges();
	}

	public int getZoneMask()
	{
		return _zoneMask;
	}

	// TODO [G1ta0] Ð¿ÐµÑ€ÐµÑ€Ð°Ð±Ð¾Ñ‚Ð°Ñ‚ÑŒ Ð² Ð»Ð¸Ñ�ÐµÐ½ÐµÑ€?
	@Override
	protected void onUpdateZones(List<Zone> leaving, List<Zone> entering)
	{
		super.onUpdateZones(leaving, entering);

		if((leaving == null || leaving.isEmpty()) && (entering == null || entering.isEmpty()))
			return;

		boolean lastInCombatZone = (_zoneMask & ZONE_PVP_FLAG) == ZONE_PVP_FLAG;
		boolean lastInDangerArea = (_zoneMask & ZONE_ALTERED_FLAG) == ZONE_ALTERED_FLAG;
		boolean lastOnSiegeField = (_zoneMask & ZONE_SIEGE_FLAG) == ZONE_SIEGE_FLAG;
		boolean lastInPeaceZone = (_zoneMask & ZONE_PEACE_FLAG) == ZONE_PEACE_FLAG;
		// FIXME G1ta0 boolean lastInSSQZone = (_zoneMask & ZONE_SSQ_FLAG) ==
		// ZONE_SSQ_FLAG;

		boolean isInCombatZone = isInCombatZone();
		boolean isInDangerArea = isInDangerArea();
		boolean isOnSiegeField = isOnSiegeField();
		boolean isInPeaceZone = isInPeaceZone();
		boolean isInSSQZone = isInSSQZone();

		// Ð¾Ð±Ð½Ð¾Ð²Ð»Ñ�ÐµÐ¼ ÐºÐ¾Ð¼Ð¿Ð°Ñ�, Ñ‚Ð¾Ð»ÑŒÐºÐ¾ ÐµÑ�Ð»Ð¸ Ð¿ÐµÑ€Ñ�Ð¾Ð½Ð°Ð¶ Ð² Ð¼Ð¸Ñ€Ðµ
		int lastZoneMask = _zoneMask;
		_zoneMask = 0;

		if(isInCombatZone)
		{
			_zoneMask |= ZONE_PVP_FLAG;
		}
		if(isInDangerArea)
		{
			_zoneMask |= ZONE_ALTERED_FLAG;
		}
		if(isOnSiegeField)
		{
			_zoneMask |= ZONE_SIEGE_FLAG;
		}
		if(isInPeaceZone)
		{
			_zoneMask |= ZONE_PEACE_FLAG;
		}
		if(isInSSQZone)
		{
			_zoneMask |= ZONE_SSQ_FLAG;
		}

		if(lastZoneMask != _zoneMask)
		{
			sendPacket(new ExSetCompassZoneCode(this));
		}

		if(lastInCombatZone != isInCombatZone)
		{
			broadcastRelationChanged();
		}

		if(lastInDangerArea != isInDangerArea)
		{
			sendPacket(new EtcStatusUpdate(this));
		}

		if(lastOnSiegeField != isOnSiegeField)
		{
			broadcastRelationChanged();
			if(isOnSiegeField)
			{
				sendPacket(Msg.YOU_HAVE_ENTERED_A_COMBAT_ZONE);
			}
			else
			{
				sendPacket(Msg.YOU_HAVE_LEFT_A_COMBAT_ZONE);
				if(!isTeleporting() && getPvpFlag() == 0)
				{
					startPvPFlag(null);
				}
			}
		}

		if(lastInPeaceZone != isInPeaceZone)
			if(isInPeaceZone)
			{
				setRecomTimerActive(false);
				if(getNevitSystem().isActive())
				{
					getNevitSystem().stopAdventTask(true);
				}
				startVitalityTask();
			}
			else
			{
				stopVitalityTask();
			}

		if(isInDuel() && isInPeaceZone)
		{
			getEvent(DuelEvent.class).abortDuel(this);
		}

		if(isInWater())
		{
			startWaterTask();
		}
		else
		{
			stopWaterTask();
		}
	}

	public void startAutoSaveTask()
	{
		if(!Config.AUTOSAVE)
			return;
		if(_autoSaveTask == null)
		{
			_autoSaveTask = AutoSaveManager.getInstance().addAutoSaveTask(this);
		}
	}

	public void stopAutoSaveTask()
	{
		if(_autoSaveTask != null)
		{
			_autoSaveTask.cancel(false);
		}
		_autoSaveTask = null;
	}

	public void startVitalityTask()
	{
		if(!Config.ALT_VITALITY_ENABLED)
			return;
		if(_vitalityTask == null)
		{
			_vitalityTask = LazyPrecisionTaskManager.getInstance().addVitalityRegenTask(this);
		}
	}

	public void stopVitalityTask()
	{
		if(_vitalityTask != null)
		{
			_vitalityTask.cancel(false);
		}
		_vitalityTask = null;
	}

  public void startPcBangPointsTask()
  {
    if ((!Config.ALT_PCBANG_POINTS_ENABLED) || (Config.ALT_PCBANG_POINTS_DELAY <= 0)) {
      return;
    }
    if (this._pcCafePointsTask == null) {
      this._pcCafePointsTask = LazyPrecisionTaskManager.getInstance().addPCCafePointsTask(this);
    }
  }

	public void stopPcBangPointsTask()
	{
		if(_pcCafePointsTask != null)
		{
			_pcCafePointsTask.cancel(false);
		}
		_pcCafePointsTask = null;
	}

	public void startUnjailTask(Player player, int time)
	{
		if(_unjailTask != null)
		{
			_unjailTask.cancel(false);
		}
		_unjailTask = ThreadPoolManager.getInstance().schedule(new UnJailTask(player), time * 60000);
	}

	public void stopUnjailTask()
	{
		if(_unjailTask != null)
		{
			_unjailTask.cancel(false);
		}
		_unjailTask = null;
	}

	@Override
	public void sendMessage(String message)
	{
		sendPacket(new SystemMessage(message));
	}

	private Location _lastClientPosition;
	private Location _lastServerPosition;

	public void setLastClientPosition(Location position)
	{
		_lastClientPosition = position;
	}

	public Location getLastClientPosition()
	{
		return _lastClientPosition;
	}

	public void setLastServerPosition(Location position)
	{
		_lastServerPosition = position;
	}

	public Location getLastServerPosition()
	{
		return _lastServerPosition;
	}

	private int _useSeed = 0;

	public void setUseSeed(int id)
	{
		_useSeed = id;
	}

	public int getUseSeed()
	{
		return _useSeed;
	}

	public int getRelation(Player target)
	{
		int result = 0;

		if(getClan() != null)
		{
			result |= RelationChanged.RELATION_CLAN_MEMBER;
			if(getClan() == target.getClan())
			{
				result |= RelationChanged.RELATION_CLAN_MATE;
			}
			if(getClan().getAllyId() != 0)
			{
				result |= RelationChanged.RELATION_ALLY_MEMBER;
			}
		}

		if(isClanLeader())
		{
			result |= RelationChanged.RELATION_LEADER;
		}

		Party party = getParty();
		if(party != null && party == target.getParty())
		{
			result |= RelationChanged.RELATION_HAS_PARTY;

			switch(party.getPartyMembers().indexOf(this))
			{
				case 0:
					result |= RelationChanged.RELATION_PARTYLEADER; // 0x10
					break;
				case 1:
					result |= RelationChanged.RELATION_PARTY4; // 0x8
					break;
				case 2:
					result |= RelationChanged.RELATION_PARTY3 + RelationChanged.RELATION_PARTY2 + RelationChanged.RELATION_PARTY1; // 0x7
					break;
				case 3:
					result |= RelationChanged.RELATION_PARTY3 + RelationChanged.RELATION_PARTY2; // 0x6
					break;
				case 4:
					result |= RelationChanged.RELATION_PARTY3 + RelationChanged.RELATION_PARTY1; // 0x5
					break;
				case 5:
					result |= RelationChanged.RELATION_PARTY3; // 0x4
					break;
				case 6:
					result |= RelationChanged.RELATION_PARTY2 + RelationChanged.RELATION_PARTY1; // 0x3
					break;
				case 7:
					result |= RelationChanged.RELATION_PARTY2; // 0x2
					break;
				case 8:
					result |= RelationChanged.RELATION_PARTY1; // 0x1
					break;
			}
		}

		Clan clan1 = getClan();
		Clan clan2 = target.getClan();
		if(clan1 != null && clan2 != null)
		{
			if(target.getPledgeType() != Clan.SUBUNIT_ACADEMY && getPledgeType() != Clan.SUBUNIT_ACADEMY)
				if(clan2.isAtWarWith(clan1.getClanId()))
				{
					result |= RelationChanged.RELATION_1SIDED_WAR;
					if(clan1.isAtWarWith(clan2.getClanId()))
					{
						result |= RelationChanged.RELATION_MUTUAL_WAR;
					}
				}
			if(getBlockCheckerArena() != -1)
			{
				result |= RelationChanged.RELATION_INSIEGE;
				ArenaParticipantsHolder holder = HandysBlockCheckerManager.getInstance().getHolder(getBlockCheckerArena());
				if(holder.getPlayerTeam(this) == 0)
				{
					result |= RelationChanged.RELATION_ENEMY;
				}
				else
				{
					result |= RelationChanged.RELATION_ALLY;
				}
				result |= RelationChanged.RELATION_ATTACKER;
			}
		}

		for(GlobalEvent e : getEvents())
		{
			result = e.getRelation(this, target, result);
		}

		return result;
	}

	/**
	 * 0=White, 1=Purple, 2=PurpleBlink
	 */
	protected int _pvpFlag;

	private Future<?> _PvPRegTask;
	private long _lastPvpAttack;

	public long getlastPvpAttack()
	{
		return _lastPvpAttack;
	}

	@Override
	public void startPvPFlag(Creature target)
	{
		if(_karma > 0)
			return;

		long startTime = System.currentTimeMillis();
		if(target != null && target.getPvpFlag() != 0)
		{
			startTime -= Config.PVP_TIME / 2;
		}
		if(_pvpFlag != 0 && _lastPvpAttack > startTime)
			return;

		_lastPvpAttack = startTime;

		updatePvPFlag(1);

		if(_PvPRegTask == null)
		{
			_PvPRegTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new PvPFlagTask(this), 1000, 1000);
		}
	}

	public void stopPvPFlag()
	{
		if(_PvPRegTask != null)
		{
			_PvPRegTask.cancel(false);
			_PvPRegTask = null;
		}
		updatePvPFlag(0);
	}

	public void updatePvPFlag(int value)
	{
		if(_handysBlockCheckerEventArena != -1)
			return;
		if(_pvpFlag == value)
			return;

		setPvpFlag(value);

		sendStatusUpdate(true, true, StatusUpdate.PVP_FLAG);

		broadcastRelationChanged();
	}

	public void setPvpFlag(int pvpFlag)
	{
		_pvpFlag = pvpFlag;
	}

	@Override
	public int getPvpFlag()
	{
		return _pvpFlag;
	}

	public boolean isInDuel()
	{
		return getEvent(DuelEvent.class) != null;
	}

	private Map<Integer, TamedBeastInstance> _tamedBeasts = new ConcurrentHashMap<Integer, TamedBeastInstance>();

	public Map<Integer, TamedBeastInstance> getTrainedBeasts()
	{
		return _tamedBeasts;
	}

	public void addTrainedBeast(TamedBeastInstance tamedBeast)
	{
		_tamedBeasts.put(tamedBeast.getObjectId(), tamedBeast);
	}

	public void removeTrainedBeast(int npcId)
	{
		_tamedBeasts.remove(npcId);
	}

	private long _lastAttackPacket = 0;

	public long getLastAttackPacket()
	{
		return _lastAttackPacket;
	}

	public void setLastAttackPacket()
	{
		_lastAttackPacket = System.currentTimeMillis();
	}

	private long _lastMovePacket = 0;

	public long getLastMovePacket()
	{
		return _lastMovePacket;
	}

	public void setLastMovePacket()
	{
		_lastMovePacket = System.currentTimeMillis();
	}

	public byte[] getKeyBindings()
	{
		return _keyBindings;
	}

	public void setKeyBindings(byte[] keyBindings)
	{
		if(keyBindings == null)
		{
			keyBindings = ArrayUtils.EMPTY_BYTE_ARRAY;
		}
		_keyBindings = keyBindings;
	}

	/**
	 * Ð£Ñ�Ñ‚Ð°Ð½Ð°Ð²Ð»Ð¸Ð²Ð°ÐµÑ‚ Ñ€ÐµÐ¶Ð¸Ð¼ Ñ‚Ñ€Ð°Ð½Ñ�Ñ„Ð¾Ñ€Ð¼Ð°Ð¸Ð¸<BR>
	 * 
	 * @param transformationId
	 *            Ð¸Ð´ÐµÐ½Ñ‚Ð¸Ñ„Ð¸ÐºÐ°Ñ‚Ð¾Ñ€ Ñ‚Ñ€Ð°Ð½Ñ�Ñ„Ð¾Ñ€Ð¼Ð°Ñ†Ð¸Ð¸ Ð˜Ð·Ð²ÐµÑ�Ñ‚Ð½Ñ‹Ðµ Ñ€ÐµÐ¶Ð¸Ð¼Ñ‹:<BR>
	 *            <li>0 - Ñ�Ñ‚Ð°Ð½Ð´Ð°Ñ€Ñ‚Ð½Ñ‹Ð¹ Ð²Ð¸Ð´ Ñ‡Ð°Ñ€Ð° <li>1 - Onyx Beast <li>2 - Death
	 *            Blader <li>etc.
	 */
	public void setTransformation(int transformationId)
	{
		if(transformationId == _transformationId || _transformationId != 0 && transformationId != 0)
			return;

		// Ð”Ð»Ñ� ÐºÐ°Ð¶Ð´Ð¾Ð¹ Ñ‚Ñ€Ð°Ð½Ñ�Ñ„Ð¾Ñ€Ð¼Ð°Ñ†Ð¸Ð¸ Ñ�Ð²Ð¾Ð¹ Ð½Ð°Ð±Ð¾Ñ€ Ñ�ÐºÐ¸Ð»Ð¾Ð²
		if(transformationId == 0) // ÐžÐ±Ñ‹Ñ‡Ð½Ð°Ñ� Ñ„Ð¾Ñ€Ð¼Ð°
		{
			// ÐžÑ�Ñ‚Ð°Ð½Ð°Ð²Ð»Ð¸Ð²Ð°ÐµÐ¼ Ñ‚ÐµÐºÑƒÑ‰Ð¸Ð¹ Ñ�Ñ„Ñ„ÐµÐºÑ‚ Ñ‚Ñ€Ð°Ð½Ñ�Ñ„Ð¾Ñ€Ð¼Ð°Ñ†Ð¸Ð¸
			for(Effect effect : getEffectList().getAllEffects())
				if(effect != null && effect.getEffectType() == EffectType.Transformation)
				{
					if(effect.calc() == 0)
					{
						continue;
					}
					effect.exit();
					preparateToTransform(effect.getSkill());
					break;
				}

			// Ð£Ð´Ð°Ð»Ñ�ÐµÐ¼ Ñ�ÐºÐ¸Ð»Ñ‹ Ñ‚Ñ€Ð°Ð½Ñ�Ñ„Ð¾Ñ€Ð¼Ð°Ñ†Ð¸Ð¸
			if(!_transformationSkills.isEmpty())
			{
				for(Skill s : _transformationSkills.values())
					if(!s.isCommon() && !SkillAcquireHolder.getInstance().isSkillPossible(this, s) && !s.isHeroic())
					{
						super.removeSkill(s);
					}
				_transformationSkills.clear();
			}
		}
		else
		{
			if(!isCursedWeaponEquipped())
			{
				// Ð”Ð¾Ð±Ð°Ð²Ð»Ñ�ÐµÐ¼ Ñ�ÐºÐ¸Ð»Ñ‹ Ñ‚Ñ€Ð°Ð½Ñ�Ñ„Ð¾Ñ€Ð¼Ð°Ñ†Ð¸Ð¸
				for(Effect effect : getEffectList().getAllEffects())
					if(effect != null && effect.getEffectType() == EffectType.Transformation)
					{
						if(effect.getSkill() instanceof Transformation && ((Transformation) effect.getSkill()).isDisguise)
						{
							for(Skill s : getAllSkills())
								if(s != null && (s.isActive() || s.isToggle()))
								{
									_transformationSkills.put(s.getId(), s);
								}
						}
						else
						{
							for(AddedSkill s : effect.getSkill().getAddedSkills())
								if(s.level == 0) // Ñ‚Ñ€Ð°Ð½Ñ�Ñ„Ð¾Ñ€Ð¼Ð°Ñ†Ð¸Ñ� Ð¿Ð¾Ð·Ð²Ð¾Ð»Ñ�ÐµÑ‚
								// Ð¿Ð¾Ð»ÑŒÐ·Ð¾Ð²Ð°Ñ‚ÑŒÑ�Ñ� Ð¾Ð±Ñ‹Ñ‡Ð½Ñ‹Ð¼
								// Ñ�ÐºÐ¸Ð»Ð»Ð¾Ð¼
								{
									int s2 = getSkillLevel(s.id);
									if(s2 > 0)
									{
										_transformationSkills.put(s.id, SkillTable.getInstance().getInfo(s.id, s2));
									}
								}
								else if(s.level == -2) // XXX: Ð´Ð¸ÐºÐ¸Ð¹ Ð¸Ð·Ð¶Ð¾Ð¿
								// Ð´Ð»Ñ� Ñ�ÐºÐ¸Ð»Ð»Ð¾Ð²
								// Ð·Ð°Ð²Ð¸Ñ�Ñ�Ñ‰Ð¸Ñ… Ð¾Ñ‚
								// ÑƒÑ€Ð¾Ð²Ð½Ñ� Ð¸Ð³Ñ€Ð¾ÐºÐ°
								{
									int learnLevel = Math.max(effect.getSkill().getMagicLevel(), 40);
									int maxLevel = SkillTable.getInstance().getBaseLevel(s.id);
									int curSkillLevel = 1;
									if(maxLevel > 3)
									{
										curSkillLevel += getLevel() - learnLevel;
									}
									else
									{
										curSkillLevel += (getLevel() - learnLevel) / ((76 - learnLevel) / maxLevel); // Ð½Ðµ
									}
									// Ñ�Ð¿Ñ€Ð°ÑˆÐ¸Ð²Ð°Ð¹Ñ‚Ðµ
									// Ð¼ÐµÐ½Ñ�
									// Ñ‡Ñ‚Ð¾
									// Ñ�Ñ‚Ð¾
									// Ñ‚Ð°ÐºÐ¾Ðµ
									curSkillLevel = Math.min(Math.max(curSkillLevel, 1), maxLevel);
									_transformationSkills.put(s.id, SkillTable.getInstance().getInfo(s.id, curSkillLevel));
								}
								else
								{
									_transformationSkills.put(s.id, s.getSkill());
								}
						}
						preparateToTransform(effect.getSkill());
						break;
					}
			}
			else
			{
				preparateToTransform(null);
			}

			if(!isInOlympiadMode() && !isCursedWeaponEquipped() && _hero && getBaseClassId() == getActiveClassId())
			{
				// Ð”Ð¾Ð±Ð°Ð²Ð»Ñ�ÐµÐ¼ Ñ…Ð¸Ñ€Ð¾ Ñ�ÐºÐ¸Ð»Ð»Ñ‹ Ð¿Ñ€Ð¾ÐºÐ»Ñ�Ñ‚Ð¾Ð¼Ñƒ Ñ‚Ñ€Ð°Ð½Ñ�Ñ„Ð¾Ñ€Ð¼Ñƒ
				_transformationSkills.put(395, SkillTable.getInstance().getInfo(395, 1));
				_transformationSkills.put(396, SkillTable.getInstance().getInfo(396, 1));
				_transformationSkills.put(1374, SkillTable.getInstance().getInfo(1374, 1));
				_transformationSkills.put(1375, SkillTable.getInstance().getInfo(1375, 1));
				_transformationSkills.put(1376, SkillTable.getInstance().getInfo(1376, 1));
			}

			for(Skill s : _transformationSkills.values())
			{
				addSkill(s, false);
			}
		}

		_transformationId = transformationId;

		sendPacket(new ExBasicActionList(this));
		sendPacket(new SkillList(this));
		sendPacket(new ShortCutInit(this));
		for(int shotId : getAutoSoulShot())
		{
			sendPacket(new ExAutoSoulShot(shotId, true));
		}
		broadcastUserInfo(true);
	}

	private void preparateToTransform(Skill transSkill)
	{
		if(transSkill == null || !transSkill.isBaseTransformation())
		{
			// ÐžÑ�Ñ‚Ð°Ð½Ð°Ð²Ð»Ð¸Ð²Ð°ÐµÐ¼ Ñ‚ÑƒÐ³Ð» Ñ�ÐºÐ¸Ð»Ð»Ñ‹
			for(Effect effect : getEffectList().getAllEffects())
				if(effect != null && effect.getSkill().isToggle())
				{
					effect.exit();
				}
		}
	}

	public boolean isInFlyingTransform()
	{
		return _transformationId == 8 || _transformationId == 9 || _transformationId == 260;
	}

	public boolean isInMountTransform()
	{
		return _transformationId == 106 || _transformationId == 109 || _transformationId == 110 || _transformationId == 20001;
	}

	/**
	 * Ð’Ð¾Ð·Ð²Ñ€Ð°Ñ‰Ð°ÐµÑ‚ Ñ€ÐµÐ¶Ð¸Ð¼ Ñ‚Ñ€Ð°Ð½Ñ�Ñ„Ð¾Ñ€Ð¼Ð°Ñ†Ð¸Ð¸
	 * 
	 * @return ID Ñ€ÐµÐ¶Ð¸Ð¼Ð° Ñ‚Ñ€Ð°Ð½Ñ�Ñ„Ð¾Ñ€Ð¼Ð°Ñ†Ð¸Ð¸
	 */
	public int getTransformation()
	{
		return _transformationId;
	}

	/**
	 * Ð’Ð¾Ð·Ð²Ñ€Ð°Ñ‰Ð°ÐµÑ‚ Ð¸Ð¼Ñ� Ñ‚Ñ€Ð°Ð½Ñ�Ñ„Ð¾Ñ€Ð¼Ð°Ñ†Ð¸Ð¸
	 * 
	 * @return String
	 */
	public String getTransformationName()
	{
		return _transformationName;
	}

	/**
	 * Ð£Ñ�Ñ‚Ð°Ð½Ð°Ð²Ð»Ð¸Ð²Ð°ÐµÑ‚ Ð¸Ð¼Ñ� Ñ‚Ñ€Ð°Ð½Ñ�Ñ„Ð¾Ñ€Ð¼Ð°Ð¸Ð¸
	 * 
	 * @param name
	 *            Ð¸Ð¼Ñ� Ñ‚Ñ€Ð°Ð½Ñ�Ñ„Ð¾Ñ€Ð¼Ð°Ñ†Ð¸Ð¸
	 */
	public void setTransformationName(String name)
	{
		_transformationName = name;
	}

	/**
	 * Ð£Ñ�Ñ‚Ð°Ð½Ð°Ð²Ð»Ð¸Ð²Ð°ÐµÑ‚ ÑˆÐ°Ð±Ð»Ð¾Ð½ Ñ‚Ñ€Ð°Ð½Ñ�Ñ„Ð¾Ñ€Ð¼Ð°Ñ†Ð¸Ð¸, Ð¸Ñ�Ð¿Ð¾Ð»ÑŒÐ·ÑƒÐµÑ‚Ñ�Ñ� Ð´Ð»Ñ� Ð¾Ð¿Ñ€ÐµÐ´ÐµÐ»ÐµÐ½Ð¸Ñ� ÐºÐ¾Ð»Ð»Ð¸Ð·Ð¸Ð¹
	 * 
	 * @param template
	 *            ID ÑˆÐ°Ð±Ð»Ð¾Ð½Ð°
	 */
	public void setTransformationTemplate(int template)
	{
		_transformationTemplate = template;
	}

	/**
	 * Ð’Ð¾Ð·Ð²Ñ€Ð°Ñ‰Ð°ÐµÑ‚ ÑˆÐ°Ð±Ð»Ð¾Ð½ Ñ‚Ñ€Ð°Ð½Ñ�Ñ„Ð¾Ñ€Ð¼Ð°Ñ†Ð¸Ð¸, Ð¸Ñ�Ð¿Ð¾Ð»ÑŒÐ·ÑƒÐµÑ‚Ñ�Ñ� Ð´Ð»Ñ� Ð¾Ð¿Ñ€ÐµÐ´ÐµÐ»ÐµÐ½Ð¸Ñ� ÐºÐ¾Ð»Ð»Ð¸Ð·Ð¸Ð¹
	 * 
	 * @return NPC ID
	 */
	public int getTransformationTemplate()
	{
		return _transformationTemplate;
	}

	/**
	 * Ð’Ð¾Ð·Ð²Ñ€Ð°Ñ‰Ð°ÐµÑ‚ ÐºÐ¾Ð»Ð»ÐµÐºÑ†Ð¸ÑŽ Ñ�ÐºÐ¸Ð»Ð»Ð¾Ð², Ñ� ÑƒÑ‡ÐµÑ‚Ð¾Ð¼ Ñ‚ÐµÐºÑƒÑ‰ÐµÐ¹ Ñ‚Ñ€Ð°Ð½Ñ�Ñ„Ð¾Ñ€Ð¼Ð°Ñ†Ð¸Ð¸
	 */
	@Override
	public final Collection<Skill> getAllSkills()
	{
		// Ð¢Ñ€Ð°Ð½Ñ�Ñ„Ð¾Ñ€Ð¼Ð°Ñ†Ð¸Ñ� Ð½ÐµÐ°ÐºÑ‚Ð¸Ð²Ð½Ð°
		if(_transformationId == 0)
			return super.getAllSkills();

		// Ð¢Ñ€Ð°Ð½Ñ�Ñ„Ð¾Ñ€Ð¼Ð°Ñ†Ð¸Ñ� Ð°ÐºÑ‚Ð¸Ð²Ð½Ð°
		Map<Integer, Skill> tempSkills = new HashMap<Integer, Skill>();
		for(Skill s : super.getAllSkills())
			if(s != null && !s.isActive() && !s.isToggle())
			{
				tempSkills.put(s.getId(), s);
			}
		tempSkills.putAll(_transformationSkills); // Ð”Ð¾Ð±Ð°Ð²Ð»Ñ�ÐµÐ¼ Ðº Ð¿Ð°Ñ�Ñ�Ð¸Ð²ÐºÐ°Ð¼ Ñ�ÐºÐ¸Ð»Ñ‹
		// Ñ‚ÐµÐºÑƒÑ‰ÐµÐ¹ Ñ‚Ñ€Ð°Ð½Ñ�Ñ„Ð¾Ñ€Ð¼Ð°Ñ†Ð¸Ð¸
		return tempSkills.values();
	}

	public void setAgathion(int id)
	{
		if(_agathionId == id)
			return;

		_agathionId = id;
		broadcastCharInfo();
	}

	public int getAgathionId()
	{
		return _agathionId;
	}

	public int getPcBangPoints()
	{
		return _pcBangPoints;
	}

	public void setPcBangPoints(int val)
	{
		_pcBangPoints = val;
	}

	public void addPcBangPoints(int count, boolean doublePoints)
	{
		if(doublePoints)
		{
			count *= 2;
		}

		if(_pcBangPoints + count > Config.ALT_MAX_PC_BANG_POINTS)
		{
			count = Config.ALT_MAX_PC_BANG_POINTS - _pcBangPoints;
		}

		_pcBangPoints += count;

		sendPacket(new SystemMessage(doublePoints ? SystemMessage.DOUBLE_POINTS_YOU_AQUIRED_S1_PC_BANG_POINT : SystemMessage.YOU_ACQUIRED_S1_PC_BANG_POINT).addNumber(count));
		sendPacket(new ExPCCafePointInfo(this, count, 1, 2, 12));
	}

	public boolean reducePcBangPoints(int count)
	{
		if(_pcBangPoints < count)
			return false;

		_pcBangPoints -= count;
		sendPacket(new SystemMessage(SystemMessage.YOU_ARE_USING_S1_POINT).addNumber(count));
		sendPacket(new ExPCCafePointInfo(this, 0, 1, 2, 12));
		return true;
	}

	private Location _groundSkillLoc;

	public void setGroundSkillLoc(Location location)
	{
		_groundSkillLoc = location;
	}

	public Location getGroundSkillLoc()
	{
		return _groundSkillLoc;
	}

	/**
	 * ÐŸÐµÑ€Ñ�Ð¾Ð½Ð°Ð¶ Ð² Ð¿Ñ€Ð¾Ñ†ÐµÑ�Ñ�Ðµ Ð²Ñ‹Ñ…Ð¾Ð´Ð° Ð¸Ð· Ð¸Ð³Ñ€Ñ‹
	 * 
	 * @return Ð²Ð¾Ð·Ð²Ñ€Ð°Ñ‰Ð°ÐµÑ‚ true ÐµÑ�Ð»Ð¸ Ð¿Ñ€Ð¾Ñ†ÐµÑ�Ñ� Ð²Ñ‹Ñ…Ð¾Ð´Ð° ÑƒÐ¶Ðµ Ð½Ð°Ñ‡Ð°Ð»Ñ�Ñ�
	 */
	public boolean isLogoutStarted()
	{
		return _isLogout.get();
	}

	public void setOfflineMode(boolean val)
	{
		if(!val)
		{
			unsetVar("offline");
		}
		_offline = val;
	}

	public boolean isInOfflineMode()
	{
		return _offline;
	}
    public void setAwayingMode(boolean awaying) 
    {
        _awaying = awaying;
    }
    public boolean isInAwayingMode() 
    {
        return _awaying;
    }
	public void saveTradeList()
	{
		String val = "";

		if(_sellList == null || _sellList.isEmpty())
		{
			unsetVar("selllist");
		}
		else
		{
			for(TradeItem i : _sellList)
			{
				val += i.getObjectId() + ";" + i.getCount() + ";" + i.getOwnersPrice() + ":";
			}
			setVar("selllist", val, -1);
			val = "";
			if(_tradeList != null && getSellStoreName() != null)
			{
				setVar("sellstorename", getSellStoreName(), -1);
			}
		}

		if(_packageSellList == null || _packageSellList.isEmpty())
		{
			unsetVar("packageselllist");
		}
		else
		{
			for(TradeItem i : _packageSellList)
			{
				val += i.getObjectId() + ";" + i.getCount() + ";" + i.getOwnersPrice() + ":";
			}
			setVar("packageselllist", val, -1);
			val = "";
			if(_tradeList != null && getSellStoreName() != null)
			{
				setVar("sellstorename", getSellStoreName(), -1);
			}
		}

		if(_buyList == null || _buyList.isEmpty())
		{
			unsetVar("buylist");
		}
		else
		{
			for(TradeItem i : _buyList)
			{
				val += i.getItemId() + ";" + i.getCount() + ";" + i.getOwnersPrice() + ":";
			}
			setVar("buylist", val, -1);
			val = "";
			if(_tradeList != null && getBuyStoreName() != null)
			{
				setVar("buystorename", getBuyStoreName(), -1);
			}
		}

		if(_createList == null || _createList.isEmpty())
		{
			unsetVar("createlist");
		}
		else
		{
			for(ManufactureItem i : _createList)
			{
				val += i.getRecipeId() + ";" + i.getCost() + ":";
			}
			setVar("createlist", val, -1);
			if(getManufactureName() != null)
			{
				setVar("manufacturename", getManufactureName(), -1);
			}
		}
	}

	public void restoreTradeList()
	{
		String var;
		var = getVar("selllist");
		if(var != null)
		{
			_sellList = new CopyOnWriteArrayList<TradeItem>();
			String[] items = var.split(":");
			for(String item : items)
			{
				if(item.equals(""))
				{
					continue;
				}
				String[] values = item.split(";");
				if(values.length < 3)
				{
					continue;
				}

				int oId = Integer.parseInt(values[0]);
				long count = Long.parseLong(values[1]);
				long price = Long.parseLong(values[2]);

				ItemInstance itemToSell = getInventory().getItemByObjectId(oId);

				if(count < 1 || itemToSell == null)
				{
					continue;
				}

				if(count > itemToSell.getCount())
				{
					count = itemToSell.getCount();
				}

				TradeItem i = new TradeItem(itemToSell);
				i.setCount(count);
				i.setOwnersPrice(price);

				_sellList.add(i);
			}
			var = getVar("sellstorename");
			if(var != null)
			{
				setSellStoreName(var);
			}
		}
		var = getVar("packageselllist");
		if(var != null)
		{
			_packageSellList = new CopyOnWriteArrayList<TradeItem>();
			String[] items = var.split(":");
			for(String item : items)
			{
				if(item.equals(""))
				{
					continue;
				}
				String[] values = item.split(";");
				if(values.length < 3)
				{
					continue;
				}

				int oId = Integer.parseInt(values[0]);
				long count = Long.parseLong(values[1]);
				long price = Long.parseLong(values[2]);

				ItemInstance itemToSell = getInventory().getItemByObjectId(oId);

				if(count < 1 || itemToSell == null)
				{
					continue;
				}

				if(count > itemToSell.getCount())
				{
					count = itemToSell.getCount();
				}

				TradeItem i = new TradeItem(itemToSell);
				i.setCount(count);
				i.setOwnersPrice(price);

				_packageSellList.add(i);
			}
			var = getVar("sellstorename");
			if(var != null)
			{
				setSellStoreName(var);
			}
		}
		var = getVar("buylist");
		if(var != null)
		{
			_buyList = new CopyOnWriteArrayList<TradeItem>();
			String[] items = var.split(":");
			for(String item : items)
			{
				if(item.equals(""))
				{
					continue;
				}
				String[] values = item.split(";");
				if(values.length < 3)
				{
					continue;
				}
				TradeItem i = new TradeItem();
				i.setItemId(Integer.parseInt(values[0]));
				i.setCount(Long.parseLong(values[1]));
				i.setOwnersPrice(Long.parseLong(values[2]));
				_buyList.add(i);
			}
			var = getVar("buystorename");
			if(var != null)
			{
				setBuyStoreName(var);
			}
		}
		var = getVar("createlist");
		if(var != null)
		{
			_createList = new CopyOnWriteArrayList<ManufactureItem>();
			String[] items = var.split(":");
			for(String item : items)
			{
				if(item.equals(""))
				{
					continue;
				}
				String[] values = item.split(";");
				if(values.length < 2)
				{
					continue;
				}
				int recId = Integer.parseInt(values[0]);
				long price = Long.parseLong(values[1]);
				if(findRecipe(recId))
				{
					_createList.add(new ManufactureItem(recId, price));
				}
			}
			var = getVar("manufacturename");
			if(var != null)
			{
				setManufactureName(var);
			}
		}
	}

	public void restoreRecipeBook()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT id FROM character_recipebook WHERE char_id=?");
			statement.setInt(1, getObjectId());
			rset = statement.executeQuery();

			while(rset.next())
			{
				int id = rset.getInt("id");
				Recipe recipe = RecipeHolder.getInstance().getRecipeByRecipeId(id);
				registerRecipe(recipe, false);
			}
		}
		catch(Exception e)
		{
			_log.warn("count not recipe skills:" + e);
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public DecoyInstance getDecoy()
	{
		return _decoy;
	}

	public void setDecoy(DecoyInstance decoy)
	{
		_decoy = decoy;
	}

	public int getMountType()
	{
		switch(getMountNpcId())
		{
			case PetDataTable.STRIDER_WIND_ID:
			case PetDataTable.STRIDER_STAR_ID:
			case PetDataTable.STRIDER_TWILIGHT_ID:
			case PetDataTable.RED_STRIDER_WIND_ID:
			case PetDataTable.RED_STRIDER_STAR_ID:
			case PetDataTable.RED_STRIDER_TWILIGHT_ID:
			case PetDataTable.GUARDIANS_STRIDER_ID:
				return 1;
			case PetDataTable.WYVERN_ID:
				return 2;
			case PetDataTable.WGREAT_WOLF_ID:
			case PetDataTable.FENRIR_WOLF_ID:
			case PetDataTable.WFENRIR_WOLF_ID:
				return 3;
		}
		return 0;
	}

	@Override
	public double getColRadius()
	{
		if(getTransformation() != 0)
		{
			final int template = getTransformationTemplate();
			if(template != 0)
			{
				final NpcTemplate npcTemplate = NpcHolder.getInstance().getTemplate(template);
				if(npcTemplate != null)
					return npcTemplate.collisionRadius;
			}
		}
		else if(isMounted())
		{
			final int mountTemplate = getMountNpcId();
			if(mountTemplate != 0)
			{
				final NpcTemplate mountNpcTemplate = NpcHolder.getInstance().getTemplate(mountTemplate);
				if(mountNpcTemplate != null)
					return mountNpcTemplate.collisionRadius;
			}
		}
		return getBaseTemplate().collisionRadius;
	}

	@Override
	public double getColHeight()
	{
		if(getTransformation() != 0)
		{
			final int template = getTransformationTemplate();
			if(template != 0)
			{
				final NpcTemplate npcTemplate = NpcHolder.getInstance().getTemplate(template);
				if(npcTemplate != null)
					return npcTemplate.collisionHeight;
			}
		}
		else if(isMounted())
		{
			final int mountTemplate = getMountNpcId();
			if(mountTemplate != 0)
			{
				final NpcTemplate mountNpcTemplate = NpcHolder.getInstance().getTemplate(mountTemplate);
				if(mountNpcTemplate != null)
					return mountNpcTemplate.collisionHeight;
			}
		}
		return getBaseTemplate().collisionHeight;
	}

	@Override
	public void setReflection(Reflection reflection)
	{
		if(getReflection() == reflection)
			return;

		super.setReflection(reflection);

		if(_summon != null && !_summon.isDead())
		{
			_summon.setReflection(reflection);
		}

		if(reflection != ReflectionManager.DEFAULT)
		{
			String var = getVar("reflection");
			if(var == null || !var.equals(String.valueOf(reflection.getId())))
			{
				setVar("reflection", String.valueOf(reflection.getId()), -1);
			}
		}
		else
		{
			unsetVar("reflection");
		}

		if(getActiveClass() != null)
		{
			getInventory().validateItems();
			// Ð”Ð»Ñ� ÐºÐ²ÐµÑ�Ñ‚Ð° _129_PailakaDevilsLegacy
			if(getPet() != null && (getPet().getNpcId() == 14916 || getPet().getNpcId() == 14917))
			{
				getPet().unSummon();
			}
		}
	}

	public void setLastNpcInteractionTime()
	{
		_lastNpcInteractionTime = System.currentTimeMillis();
	}
	public boolean canMoveAfterInteraction()
	{
		return System.currentTimeMillis() - _lastNpcInteractionTime > 1500L;
	}
	public boolean isTerritoryFlagEquipped()
	{
		ItemInstance weapon = getActiveWeaponInstance();
		return weapon != null && weapon.getTemplate().isTerritoryFlag();
	}

	private int _buyListId;

	public void setBuyListId(int listId)
	{
		_buyListId = listId;
	}

	public int getBuyListId()
	{
		return _buyListId;
	}

	public int getFame()
	{
		return _fame;
	}

	public void setFame(int fame, String log)
	{
		fame = Math.min(Config.FORMULA_LIM_FAME, fame);
		if(log != null && !log.isEmpty())
		{
			Log.add(_name + "|" + (fame - _fame) + "|" + fame + "|" + log, "fame");
			Log_New.LogEvent(getName(), getIP(), "Fame", new String[] { "add fame: " + getName() + " count: " + fame + " for " + log + "" });
		}
		if(fame > _fame)
		{
			sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_ACQUIRED_S1_REPUTATION_SCORE).addNumber(fame - _fame));
		}
		_fame = fame;
		sendChanges();
	}

	public void setFame(int fame)
	{
		fame = Math.min(Config.FORMULA_LIM_FAME, fame);
		if(fame > _fame)
		{
			sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_ACQUIRED_S1_REPUTATION_SCORE).addNumber(fame - _fame));
		}
		_fame = fame;
		sendChanges();
	}

	public void decFame(int fame)
	{
		_fame -= fame;
		sendChanges();
	}

	public int getVitalityLevel(boolean blessActive)
	{
		return Config.ALT_VITALITY_ENABLED ? blessActive ? 4 : _vitalityLevel : 0;
	}

	public double getVitality()
	{
		return Config.ALT_VITALITY_ENABLED ? _vitality : 0;
	}

	public void addVitality(double val)
	{
		setVitality(getVitality() + val);
	}

	public void setVitality(double newVitality)
	{
		if(!Config.ALT_VITALITY_ENABLED)
			return;

		newVitality = Math.max(Math.min(newVitality, Config.VITALITY_LEVELS[4]), 0);

		if(newVitality >= _vitality || getLevel() >= 10)
		{
			if(newVitality != _vitality)
				if(newVitality == 0)
				{
					sendPacket(Msg.VITALITY_IS_FULLY_EXHAUSTED);
				}
				else if(newVitality == Config.VITALITY_LEVELS[4])
				{
					sendPacket(Msg.YOUR_VITALITY_IS_AT_MAXIMUM);
				}

			_vitality = newVitality;
		}

		int newLevel = 0;
		if(_vitality >= Config.VITALITY_LEVELS[3])
		{
			newLevel = 4;
		}
		else if(_vitality >= Config.VITALITY_LEVELS[2])
		{
			newLevel = 3;
		}
		else if(_vitality >= Config.VITALITY_LEVELS[1])
		{
			newLevel = 2;
		}
		else if(_vitality >= Config.VITALITY_LEVELS[0])
		{
			newLevel = 1;
		}

		if(_vitalityLevel > newLevel)
		{
			getNevitSystem().addPoints(1500); // TODO: ÐšÐ¾Ð»Ð¸Ñ‡ÐµÑ�Ñ‚Ð²Ð¾ Ð¾Ñ‚ Ð±Ð°Ð»Ð´Ñ‹.
		}

		if(_vitalityLevel != newLevel)
		{
			if(_vitalityLevel != -1)
			{
				sendPacket(newLevel < _vitalityLevel ? Msg.VITALITY_HAS_DECREASED : Msg.VITALITY_HAS_INCREASED);
			}
			_vitalityLevel = newLevel;
		}

		sendPacket(new ExVitalityPointInfo((int) _vitality));
	}

	private final int _incorrectValidateCount = 0;

	public int getIncorrectValidateCount()
	{
		return _incorrectValidateCount;
	}

	public int setIncorrectValidateCount(int count)
	{
		return _incorrectValidateCount;
	}

	public int getExpandInventory()
	{
		return _expandInventory;
	}

	public void setExpandInventory(int inventory)
	{
		_expandInventory = inventory;
	}

	public int getExpandWarehouse()
	{
		return _expandWarehouse;
	}

	public void setExpandWarehouse(int warehouse)
	{
		_expandWarehouse = warehouse;
	}

	public boolean isNotShowBuffAnim()
	{
		return _notShowBuffAnim;
	}

	public void setNotShowBuffAnim(boolean value)
	{
		_notShowBuffAnim = value;
	}

	public void enterMovieMode()
	{
		if(isInMovie()) // already in movie
			return;

		setTarget(null);
		stopMove();
		setIsInMovie(true);
		sendPacket(new CameraMode(1));
	}

	public void leaveMovieMode()
	{
		setIsInMovie(false);
		sendPacket(new CameraMode(0));
		broadcastCharInfo();
	}

	public void specialCamera(GameObject target, int dist, int yaw, int pitch, int time, int duration)
	{
		sendPacket(new SpecialCamera(target.getObjectId(), dist, yaw, pitch, time, duration));
	}

	public void specialCamera(GameObject target, int dist, int yaw, int pitch, int time, int duration, int turn, int rise, int widescreen, int unk)
	{
		sendPacket(new SpecialCamera(target.getObjectId(), dist, yaw, pitch, time, duration, turn, rise, widescreen, unk));
	}

	private int _movieId = 0;
	private boolean _isInMovie;

	public void setMovieId(int id)
	{
		_movieId = id;
	}

	public int getMovieId()
	{
		return _movieId;
	}

	public boolean isInMovie()
	{
		return _isInMovie;
	}

	public void setIsInMovie(boolean state)
	{
		_isInMovie = state;
	}

	public void showQuestMovie(SceneMovie movie)
	{
		if(isInMovie()) // already in movie
			return;

		sendActionFailed();
		setTarget(null);
		stopMove();
		setMovieId(movie.getId());
		setIsInMovie(true);
		sendPacket(movie.packet(this));
	}

	public void showQuestMovie(int movieId)
	{
		if(isInMovie()) // already in movie
			return;

		sendActionFailed();
		setTarget(null);
		stopMove();
		setMovieId(movieId);
		setIsInMovie(true);
		sendPacket(new ExStartScenePlayer(movieId));
	}

	public void setAutoLoot(boolean enable)
	{
		if(Config.ALT_AUTO_LOOT_INDIVIDUAL)
		{
			_autoLoot = enable;
			setVar("AutoLoot", String.valueOf(enable), -1);
		}
		else
			sendMessage(new CustomMessage("scripts.services.off", getPlayer()));
	}

	public void setAutoLootHerbs(boolean enable)
	{
		if(Config.ALT_AUTO_LOOT_INDIVIDUAL)
		{
			AutoLootHerbs = enable;
			setVar("AutoLootHerbs", String.valueOf(enable), -1);
		}
		else
			sendMessage(new CustomMessage("scripts.services.off", getPlayer()));
	}

	public boolean isAutoLootEnabled()
	{
		return _autoLoot;
	}

	public boolean isAutoLootHerbsEnabled()
	{
		return AutoLootHerbs;
	}

	public final void reName(String name, boolean saveToDB)
	{
		setName(name);
		if(saveToDB)
		{
			saveNameToDB();
		}
		broadcastCharInfo();
	}

	public final void reName(String name)
	{
		reName(name, false);
	}

	public final void saveNameToDB()
	{
		Connection con = null;
		PreparedStatement st = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			st = con.prepareStatement("UPDATE characters SET char_name = ? WHERE obj_Id = ?");
			st.setString(1, getName());
			st.setInt(2, getObjectId());
			st.executeUpdate();
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, st);
		}
	}

	@Override
	public Player getPlayer()
	{
		return this;
	}

	private List<String> getStoredBypasses(boolean bbs)
	{
		if(bbs)
		{
			if(bypasses_bbs == null)
			{
				bypasses_bbs = new LazyArrayList<String>();
			}
			return bypasses_bbs;
		}
		if(bypasses == null)
		{
			bypasses = new LazyArrayList<String>();
		}
		return bypasses;
	}

	public void cleanBypasses(boolean bbs)
	{
		List<String> bypassStorage = getStoredBypasses(bbs);
		synchronized (bypassStorage)
		{
			bypassStorage.clear();
		}
	}

	public String encodeBypasses(String htmlCode, boolean bbs)
	{
		List<String> bypassStorage = getStoredBypasses(bbs);
		synchronized (bypassStorage)
		{
			return BypassManager.encode(htmlCode, bypassStorage, bbs);
		}
	}

	public DecodedBypass decodeBypass(String bypass)
	{
		BypassType bpType = BypassManager.getBypassType(bypass);
		boolean bbs = bpType == BypassType.ENCODED_BBS || bpType == BypassType.SIMPLE_BBS;
		List<String> bypassStorage = getStoredBypasses(bbs);
		if(bpType == BypassType.ENCODED || bpType == BypassType.ENCODED_BBS)
			return BypassManager.decode(bypass, bypassStorage, bbs, this);
		if(bpType == BypassType.SIMPLE)
			return new DecodedBypass(bypass, false).trim();
		if(bpType == BypassType.SIMPLE_BBS && !bypass.startsWith("_bbsscripts"))
			return new DecodedBypass(bypass, true).trim();
		ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(bypass);
		if(handler != null)
			return new DecodedBypass(bypass, handler).trim();
		_log.warn("Direct access to bypass: " + bypass + " / Player: " + getName());
		return null;
	}

	public int getTalismanCount()
	{
		return (int) calcStat(Stats.TALISMANS_LIMIT, 0, null, null);
	}

	public boolean getOpenCloak()
	{
		if(Config.ALT_OPEN_CLOAK_SLOT)
			return true;
		return (int) calcStat(Stats.CLOAK_SLOT, 0, null, null) > 0;
	}

	public final void disableDrop(int time)
	{
		_dropDisabled = System.currentTimeMillis() + time;
	}

	public final boolean isDropDisabled()
	{
		return _dropDisabled > System.currentTimeMillis();
	}

	private ItemInstance _petControlItem = null;

	public void setPetControlItem(int itemObjId)
	{
		setPetControlItem(getInventory().getItemByObjectId(itemObjId));
	}

	public void setPetControlItem(ItemInstance item)
	{
		_petControlItem = item;
	}

	public ItemInstance getPetControlItem()
	{
		return _petControlItem;
	}

	private AtomicBoolean isActive = new AtomicBoolean();

	public boolean isActive()
	{
		return isActive.get();
	}

	public void setActive()
	{
		setNonAggroTime(0);

		if(isActive.getAndSet(true))
			return;

		onActive();
	}

	private void onActive()
	{
		setNonAggroTime(0);
		sendPacket(Msg.YOU_ARE_PROTECTED_AGGRESSIVE_MONSTERS);

		if(getPetControlItem() != null)
		{
			ThreadPoolManager.getInstance().execute(new RunnableImpl(){
				@Override
				public void runImpl()
				{
					if(getPetControlItem() != null)
					{
						summonPet();
					}
				}

			});
		}
	}

	public void summonPet()
	{
		if(getPet() != null)
			return;

		ItemInstance controlItem = getPetControlItem();
		if(controlItem == null)
			return;

		int npcId = PetDataTable.getSummonId(controlItem);
		if(npcId == 0)
			return;

		NpcTemplate petTemplate = NpcHolder.getInstance().getTemplate(npcId);
		if(petTemplate == null)
			return;

		PetInstance pet = PetInstance.restore(controlItem, petTemplate, this);
		if(pet == null)
			return;

		setPet(pet);
		pet.setTitle(getName());

		if(!pet.isRespawned())
		{
			pet.setCurrentHp(pet.getMaxHp(), false);
			pet.setCurrentMp(pet.getMaxMp());
			pet.setCurrentFed(pet.getMaxFed());
			pet.updateControlItem();
			pet.store();
		}

		pet.getInventory().restore();

		pet.setNonAggroTime(System.currentTimeMillis() + Config.NONAGGRO_TIME_ONTELEPORT);
		pet.setReflection(getReflection());
		pet.spawnMe(Location.findPointToStay(this, 50, 70));
		pet.setRunning();
		pet.setFollowMode(true);
		pet.getInventory().validateItems();

		if(pet instanceof PetBabyInstance)
		{
			((PetBabyInstance) pet).startBuffTask();
		}
	}

	private Map<Integer, Long> _traps;

	public Collection<TrapInstance> getTraps()
	{
		if(_traps == null)
			return null;
		Collection<TrapInstance> result = new ArrayList<TrapInstance>(getTrapsCount());
		TrapInstance trap;
		for(Integer trapId : _traps.keySet())
			if((trap = (TrapInstance) GameObjectsStorage.get(_traps.get(trapId))) != null)
			{
				result.add(trap);
			}
			else
			{
				_traps.remove(trapId);
			}
		return result;
	}

	public int getTrapsCount()
	{
		return _traps == null ? 0 : _traps.size();
	}

	public void addTrap(TrapInstance trap)
	{
		if(_traps == null)
		{
			_traps = new HashMap<Integer, Long>();
		}
		_traps.put(trap.getObjectId(), trap.getStoredId());
	}

	public void removeTrap(TrapInstance trap)
	{
		Map<Integer, Long> traps = _traps;
		if(traps == null || traps.isEmpty())
			return;
		traps.remove(trap.getObjectId());
	}

	public void destroyFirstTrap()
	{
		Map<Integer, Long> traps = _traps;
		if(traps == null || traps.isEmpty())
			return;
		TrapInstance trap;
		for(Integer trapId : traps.keySet())
		{
			if((trap = (TrapInstance) GameObjectsStorage.get(traps.get(trapId))) != null)
			{
				trap.deleteMe();
				return;
			}
			return;
		}
	}

	public void destroyAllTraps()
	{
		Map<Integer, Long> traps = _traps;
		if(traps == null || traps.isEmpty())
			return;
		List<TrapInstance> toRemove = new ArrayList<TrapInstance>();
		for(Integer trapId : traps.keySet())
		{
			toRemove.add((TrapInstance) GameObjectsStorage.get(traps.get(trapId)));
		}
		for(TrapInstance t : toRemove)
			if(t != null)
			{
				t.deleteMe();
			}
	}

	public void setBlockCheckerArena(byte arena)
	{
		_handysBlockCheckerEventArena = arena;
	}

	public int getBlockCheckerArena()
	{
		return _handysBlockCheckerEventArena;
	}

	@Override
	public PlayerListenerList getListeners()
	{
		if(listeners == null)
		{
			synchronized (this)
			{
				if(listeners == null)
				{
					listeners = new PlayerListenerList(this);
				}
			}
		}
		return (PlayerListenerList) listeners;
	}

	@Override
	public PlayerStatsChangeRecorder getStatsRecorder()
	{
		if(_statsRecorder == null)
		{
			synchronized (this)
			{
				if(_statsRecorder == null)
				{
					_statsRecorder = new PlayerStatsChangeRecorder(this);
				}
			}
		}
		return (PlayerStatsChangeRecorder) _statsRecorder;
	}

	private Future<?> _hourlyTask;
	private int _hoursInGame = 0;

	public int getHoursInGame()
	{
		_hoursInGame++;
		return _hoursInGame;
	}

	public void startHourlyTask()
	{
		_hourlyTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new HourlyTask(this), 3600000L, 3600000L);
	}

	public void stopHourlyTask()
	{
		if(_hourlyTask != null)
		{
			_hourlyTask.cancel(false);
			_hourlyTask = null;
		}
	}

	public long getPremiumPoints()
	{
		if(Config.GAME_POINT_ITEM_ID != -1)
			return ItemFunctions.getItemCount(this, Config.GAME_POINT_ITEM_ID);
		else
			return getNetConnection().getPointG();
	}

	public void reducePremiumPoints(final int val)
	{
		int reduce = (getNetConnection().getPointG() - (val));
		if(Config.GAME_POINT_ITEM_ID != -1)
			ItemFunctions.removeItem(this, Config.GAME_POINT_ITEM_ID, val, true);
		else
			getNetConnection().setPointG(reduce);
	}

	private boolean _agathionResAvailable = false;

	public boolean isAgathionResAvailable()
	{
		return _agathionResAvailable;
	}

	public void setAgathionRes(boolean val)
	{
		_agathionResAvailable = val;
	}

	public boolean isClanAirShipDriver()
	{
		return isInBoat() && getBoat().isClanAirShip() && ((ClanAirShip) getBoat()).getDriver() == this;
	}

	/**
	 * _userSession - Ð¸Ñ�Ð¿Ð¾Ð»ÑŒÑŽÐ·ÑƒÐµÑ‚Ñ�Ñ� Ð´Ð»Ñ� Ñ…Ñ€Ð°Ð½ÐµÐ½Ð¸Ñ� Ð²Ñ€ÐµÐ¼ÐµÐ½Ð½Ñ‹Ñ… Ð¿ÐµÑ€ÐµÐ¼ÐµÐ½Ð½Ñ‹Ñ….
	 */
	private Map<String, String> _userSession;

	public String getSessionVar(String key)
	{
		if(_userSession == null)
			return null;
		return _userSession.get(key);
	}

	public void setSessionVar(String key, String val)
	{
		if(_userSession == null)
		{
			_userSession = new ConcurrentHashMap<String, String>();
		}

		if(val == null || val.isEmpty())
		{
			_userSession.remove(key);
		}
		else
		{
			_userSession.put(key, val);
		}
	}

	public FriendList getFriendList()
	{
		return _friendList;
	}

	public boolean isNotShowTraders()
	{
		return _notShowTraders;
	}

	public void setNotShowTraders(boolean notShowTraders)
	{
		_notShowTraders = notShowTraders;
	}

	public boolean isDebug()
	{
		return _debug;
	}

	public void setDebug(boolean b)
	{
		_debug = b;
	}

	public void sendItemList(boolean show)
	{
		ItemInstance[] items = getInventory().getItems();
		LockType lockType = getInventory().getLockType();
		int[] lockItems = getInventory().getLockItems();

		int allSize = items.length;
		int questItemsSize = 0;
		int agathionItemsSize = 0;
		for(ItemInstance item : items)
		{
			if(item.getTemplate().isQuest())
			{
				questItemsSize++;
			}
			if(item.getTemplate().getAgathionEnergy() > 0)
			{
				agathionItemsSize++;
			}
		}

		sendPacket(new ItemList(allSize - questItemsSize, items, show, lockType, lockItems));
		if(questItemsSize > 0)
		{
			sendPacket(new ExQuestItemList(questItemsSize, items, lockType, lockItems));
		}
		if(agathionItemsSize > 0)
		{
			sendPacket(new ExBR_AgathionEnergyInfo(agathionItemsSize, items));
		}
	}

	public int getBeltInventoryIncrease()
	{
		ItemInstance item = getInventory().getPaperdollItem(Inventory.PAPERDOLL_BELT);
		if(item != null && item.getTemplate().getAttachedSkills() != null)
		{
			for(Skill skill : item.getTemplate().getAttachedSkills())
			{
				for(FuncTemplate func : skill.getAttachedFuncs())
					if(func._stat == Stats.INVENTORY_LIMIT)
						return (int) func._value;
			}
		}
		return 0;
	}

	@Override
	public boolean isPlayer()
	{
		return true;
	}

	public boolean checkCoupleAction(Player target)
	{
		if(target.getPrivateStoreType() != Player.STORE_PRIVATE_NONE)
		{
			sendPacket(new SystemMessage(SystemMessage.COUPLE_ACTION_CANNOT_C1_TARGET_IN_PRIVATE_STORE).addName(target));
			return false;
		}
		if(target.isFishing())
		{
			sendPacket(new SystemMessage(SystemMessage.COUPLE_ACTION_CANNOT_C1_TARGET_IS_FISHING).addName(target));
			return false;
		}
		if(target.isInCombat())
		{
			sendPacket(new SystemMessage(SystemMessage.COUPLE_ACTION_CANNOT_C1_TARGET_IS_IN_COMBAT).addName(target));
			return false;
		}
		if(target.isCursedWeaponEquipped())
		{
			sendPacket(new SystemMessage(SystemMessage.COUPLE_ACTION_CANNOT_C1_TARGET_IS_CURSED_WEAPON_EQUIPED).addName(target));
			return false;
		}
		if(target.isInOlympiadMode())
		{
			sendPacket(new SystemMessage(SystemMessage.COUPLE_ACTION_CANNOT_C1_TARGET_IS_IN_OLYMPIAD).addName(target));
			return false;
		}
		if(target.isOnSiegeField())
		{
			sendPacket(new SystemMessage(SystemMessage.COUPLE_ACTION_CANNOT_C1_TARGET_IS_IN_SIEGE).addName(target));
			return false;
		}
		if(target.isInBoat() || target.getMountNpcId() != 0)
		{
			sendPacket(new SystemMessage(SystemMessage.COUPLE_ACTION_CANNOT_C1_TARGET_IS_IN_VEHICLE_MOUNT_OTHER).addName(target));
			return false;
		}
		if(target.isTeleporting())
		{
			sendPacket(new SystemMessage(SystemMessage.COUPLE_ACTION_CANNOT_C1_TARGET_IS_TELEPORTING).addName(target));
			return false;
		}
		if(target.getTransformation() != 0)
		{
			sendPacket(new SystemMessage(SystemMessage.COUPLE_ACTION_CANNOT_C1_TARGET_IS_IN_TRANSFORM).addName(target));
			return false;
		}
		if(target.isDead())
		{
			sendPacket(new SystemMessage(SystemMessage.COUPLE_ACTION_CANNOT_C1_TARGET_IS_DEAD).addName(target));
			return false;
		}
		return true;
	}

	@Override
	public void startAttackStanceTask()
	{
		startAttackStanceTask0();
		Summon summon = getPet();
		if(summon != null)
		{
			summon.startAttackStanceTask0();
		}
	}

	@Override
	public void displayGiveDamageMessage(Creature target, int damage, boolean crit, boolean miss, boolean shld, boolean magic)
	{
		super.displayGiveDamageMessage(target, damage, crit, miss, shld, magic);
		if(!GeoEngine.canSeeTarget(this, target, false))//test
		{
			sendMessage(target.getName() + (isLangRus() ? " укрылся от Вашей атаки." : " steal away from your attack."));
			return;
		}
		if(crit)
			if(magic)
			{
				sendPacket(new SystemMessage(SystemMessage.MAGIC_CRITICAL_HIT).addName(this));
			}
			else
			{
				sendPacket(new SystemMessage(SystemMessage.C1_HAD_A_CRITICAL_HIT).addName(this));
			}

		if(miss)
		{
			sendPacket(new SystemMessage(SystemMessage.C1S_ATTACK_WENT_ASTRAY).addName(this));
		}
		else if(!target.isDamageBlocked())
		{
			sendPacket(new SystemMessage(SystemMessage.C1_HAS_GIVEN_C2_DAMAGE_OF_S3).addName(this).addName(target).addNumber(damage));
		}

		if(target.isPlayer())
			if(shld && damage > 1)
			{
				target.sendPacket(SystemMsg.YOUR_SHIELD_DEFENSE_HAS_SUCCEEDED);
			}
			else if(shld && damage == 1)
			{
				target.sendPacket(SystemMsg.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS);
			}
	}

	@Override
	public void displayReceiveDamageMessage(Creature attacker, int damage)
	{
		if(attacker != this)
		{
			sendPacket(new SystemMessage(SystemMessage.C1_HAS_RECEIVED_DAMAGE_OF_S3_FROM_C2).addName(this).addName(attacker).addNumber((long) damage));
		}
	}

	public IntObjectMap<String> getPostFriends()
	{
		return _postFriends;
	}

	public boolean isSharedGroupDisabled(int groupId)
	{
		TimeStamp sts = _sharedGroupReuses.get(groupId);
		if(sts == null)
			return false;
		if(sts.hasNotPassed())
			return true;
		_sharedGroupReuses.remove(groupId);
		return false;
	}

	public TimeStamp getSharedGroupReuse(int groupId)
	{
		return _sharedGroupReuses.get(groupId);
	}

	public void addSharedGroupReuse(int group, TimeStamp stamp)
	{
		_sharedGroupReuses.put(group, stamp);
	}

	public Collection<IntObjectMap.Entry<TimeStamp>> getSharedGroupReuses()
	{
		return _sharedGroupReuses.entrySet();
	}

	public void sendReuseMessage(ItemInstance item)
	{
		TimeStamp sts = getSharedGroupReuse(item.getTemplate().getReuseGroup());
		if(sts == null || !sts.hasNotPassed())
			return;

		long timeleft = sts.getReuseCurrent();
		long hours = timeleft / 3600000;
		long minutes = (timeleft - hours * 3600000) / 60000;
		long seconds = (long) Math.ceil((timeleft - hours * 3600000 - minutes * 60000) / 1000.);

		if(hours > 0)
		{
			sendPacket(new SystemMessage2(item.getTemplate().getReuseType().getMessages()[2]).addItemName(item.getTemplate().getItemId()).addInteger(hours).addInteger(minutes).addInteger(seconds));
		}
		else if(minutes > 0)
		{
			sendPacket(new SystemMessage2(item.getTemplate().getReuseType().getMessages()[1]).addItemName(item.getTemplate().getItemId()).addInteger(minutes).addInteger(seconds));
		}
		else
		{
			sendPacket(new SystemMessage2(item.getTemplate().getReuseType().getMessages()[0]).addItemName(item.getTemplate().getItemId()).addInteger(seconds));
		}
	}

	public NevitSystem getNevitSystem()
	{
		return _nevitSystem;
	}

	public void ask(ConfirmDlg dlg, OnAnswerListener listener)
	{
		if(_askDialog != null)
			return;
		int rnd = Rnd.nextInt();
		_askDialog = new ImmutablePair<Integer, OnAnswerListener>(rnd, listener);
		dlg.setRequestId(rnd);
		sendPacket(dlg);
	}

	public Pair<Integer, OnAnswerListener> getAskListener(boolean clear)
	{
		if(!clear)
			return _askDialog;
		else
		{
			Pair<Integer, OnAnswerListener> ask = _askDialog;
			_askDialog = null;
			return ask;
		}
	}

	@Override
	public boolean isDead()
	{
		return isInOlympiadMode() || isInDuel() ? getCurrentHp() <= 1. : super.isDead();
	}

	@Override
	public int getAgathionEnergy()
	{
		ItemInstance item = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LBRACELET);
		return item == null ? 0 : item.getAgathionEnergy();
	}

	@Override
	public void setAgathionEnergy(int val)
	{
		ItemInstance item = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LBRACELET);
		if(item == null)
			return;
		item.setAgathionEnergy(val);
		item.setJdbcState(JdbcEntityState.UPDATED);

		sendPacket(new ExBR_AgathionEnergyInfo(1, item));
	}

	public boolean hasPrivilege(Privilege privilege)
	{
		return _clan != null && (getClanPrivileges() & privilege.mask()) == privilege.mask();
	}

	public MatchingRoom getMatchingRoom()
	{
		return _matchingRoom;
	}

	public void setMatchingRoom(MatchingRoom matchingRoom)
	{
		_matchingRoom = matchingRoom;
	}

	public void dispelBuffs()
	{
		for(Effect e : getEffectList().getAllEffects())
			if(!e.getSkill().isOffensive() && !e.getSkill().isNewbie() && e.isCancelable() && !e.getSkill().isPreservedOnDeath())
			{
				sendPacket(new SystemMessage(SystemMessage.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(e.getSkill().getId(), e.getSkill().getLevel()));
				e.exit();
			}
		if(getPet() != null)
		{
			for(Effect e : getPet().getEffectList().getAllEffects())
				if(!e.getSkill().isOffensive() && !e.getSkill().isNewbie() && e.isCancelable() && !e.getSkill().isPreservedOnDeath())
				{
					e.exit();
				}
		}
	}

	public void setInstanceReuse(int id, long time)
	{
		final SystemMessage msg = new SystemMessage(SystemMessage.INSTANT_ZONE_FROM_HERE__S1_S_ENTRY_HAS_BEEN_RESTRICTED_YOU_CAN_CHECK_THE_NEXT_ENTRY_POSSIBLE).addString(getName());
		sendPacket(msg);
		_instancesReuses.put(id, time);
		mysql.set("REPLACE INTO character_instances (obj_id, id, reuse) VALUES (?,?,?)", getObjectId(), id, time);
	}

	public void removeInstanceReuse(int id)
	{
		if(_instancesReuses.remove(id) != null)
		{
			mysql.set("DELETE FROM `character_instances` WHERE `obj_id`=? AND `id`=? LIMIT 1", getObjectId(), id);
		}
	}

	public void removeAllInstanceReuses()
	{
		_instancesReuses.clear();
		mysql.set("DELETE FROM `character_instances` WHERE `obj_id`=?", getObjectId());
	}

	public void removeInstanceReusesByGroupId(int groupId)
	{
		for(int i : InstantZoneHolder.getInstance().getSharedReuseInstanceIdsByGroup(groupId))
			if(getInstanceReuse(i) != null)
			{
				removeInstanceReuse(i);
			}
	}

	public Long getInstanceReuse(int id)
	{
		return _instancesReuses.get(id);
	}

	public Map<Integer, Long> getInstanceReuses()
	{
		return _instancesReuses;
	}

	private void loadInstanceReuses()
	{
		Connection con = null;
		PreparedStatement offline = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			offline = con.prepareStatement("SELECT * FROM character_instances WHERE obj_id = ?");
			offline.setInt(1, getObjectId());
			rs = offline.executeQuery();
			while(rs.next())
			{
				int id = rs.getInt("id");
				long reuse = rs.getLong("reuse");
				_instancesReuses.put(id, reuse);
			}
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, offline, rs);
		}
	}

	public Reflection getActiveReflection()
	{
		for(Reflection r : ReflectionManager.getInstance().getAll())
			if(r != null && ArrayUtils.contains(r.getVisitors(), getObjectId()))
				return r;
		return null;
	}

	public boolean canEnterInstance(int instancedZoneId)
	{
		InstantZone iz = InstantZoneHolder.getInstance().getInstantZone(instancedZoneId);

		if(isDead())
			return false;

		if(ReflectionManager.getInstance().size() > Config.MAX_REFLECTIONS_COUNT)
		{
			sendPacket(SystemMsg.THE_MAXIMUM_NUMBER_OF_INSTANCE_ZONES_HAS_BEEN_EXCEEDED);
			return false;
		}

		if(iz == null)
		{
			sendPacket(SystemMsg.SYSTEM_ERROR);
			return false;
		}

		if(ReflectionManager.getInstance().getCountByIzId(instancedZoneId) >= iz.getMaxChannels())
		{
			sendPacket(SystemMsg.THE_MAXIMUM_NUMBER_OF_INSTANCE_ZONES_HAS_BEEN_EXCEEDED);
			return false;
		}

		return iz.getEntryType().canEnter(this, iz);
	}

	public boolean canReenterInstance(int instancedZoneId)
	{
		InstantZone iz = InstantZoneHolder.getInstance().getInstantZone(instancedZoneId);
		if(getActiveReflection() != null && getActiveReflection().getInstancedZoneId() != instancedZoneId)
		{
			sendPacket(SystemMsg.YOU_HAVE_ENTERED_ANOTHER_INSTANCE_ZONE_THEREFORE_YOU_CANNOT_ENTER_CORRESPONDING_DUNGEON);
			return false;
		}
		if(iz.isDispelBuffs())
		{
			dispelBuffs();
		}
		return iz.getEntryType().canReEnter(this, iz);
	}

	public int getBattlefieldChatId()
	{
		return _battlefieldChatId;
	}

	public void setBattlefieldChatId(int battlefieldChatId)
	{
		_battlefieldChatId = battlefieldChatId;
	}

	@Override
	public void broadCast(IStaticPacket... packet)
	{
		sendPacket(packet);
	}

	@Override
	public Iterator<Player> iterator()
	{
		return Collections.singleton(this).iterator();
	}

	public PlayerGroup getPlayerGroup()
	{
		if(getParty() != null)
		{
			if(getParty().getCommandChannel() != null)
				return getParty().getCommandChannel();
			else
				return getParty();
		}
		else
			return this;
	}

	public boolean isActionBlocked(String action)
	{
		return _blockedActions.contains(action);
	}

	public void blockActions(String... actions)
	{
		Collections.addAll(_blockedActions, actions);
	}

	public void unblockActions(String... actions)
	{
		for(String action : actions)
		{
			_blockedActions.remove(action);
		}
	}

	public OlympiadGame getOlympiadGame()
	{
		return _olympiadGame;
	}

	public void setOlympiadGame(OlympiadGame olympiadGame)
	{
		_olympiadGame = olympiadGame;
	}

	public OlympiadGame getOlympiadObserveGame()
	{
		return _olympiadObserveGame;
	}

	public void setOlympiadObserveGame(OlympiadGame olympiadObserveGame)
	{
		_olympiadObserveGame = olympiadObserveGame;
	}

	public void addRadar(int x, int y, int z)
	{
		sendPacket(new RadarControl(0, 1, x, y, z));
	}

	public void addRadarWithMap(int x, int y, int z)
	{
		sendPacket(new RadarControl(0, 2, x, y, z));
	}

	public PetitionMainGroup getPetitionGroup()
	{
		return _petitionGroup;
	}

	public void setPetitionGroup(PetitionMainGroup petitionGroup)
	{
		_petitionGroup = petitionGroup;
	}

	public int getLectureMark()
	{
		return _lectureMark;
	}

	public void setLectureMark(int lectureMark)
	{
		_lectureMark = lectureMark;
	}

	private boolean _inCtF = false;
	private boolean _InDeathMatch = false;
	private boolean _inFightClub = false;
	private boolean _inLastHero = false;
	private boolean _inTVTArena = false;
	private boolean _inTvT = false;

	public boolean isInPvPEvent()
	{
		return !_InDeathMatch || !_inTvT || !_inCtF || !_inLastHero || !_inFightClub || !_inTVTArena ? false : true;
	}
	public boolean isInTvT()
	{
		return _inTvT;
	}

	public boolean isInDeathMatch()
	{
		return _InDeathMatch;
	}

	public boolean isInCtF()
	{
		return _inCtF;
	}

	public boolean isInLastHero()
	{
		return _inLastHero;
	}

	public boolean isInFightClub()
	{
		return _inFightClub;
	}

	public boolean isInTVTArena()
	{
		return _inTVTArena;
	}

	public boolean hasCTFflag()
	{
		return _hasFlagCTF;
	}

	public void setIsInDeathMatch(boolean param)
	{
		_InDeathMatch = param;
	}

	public void setIsInCtF(boolean param)
	{
		_inCtF = param;
	}
	public void setIsInTvT(boolean param)
	{
		_inTvT = param;
	}
	public void setIsInLastHero(boolean param)
	{
		_inLastHero = param;
	}

	public void setIsInFightClub(boolean param)
	{
		_inFightClub = param;
	}

	public void setIsInTVTArena(boolean param)
	{
		_inTVTArena = param;
	}

	public void setCTFflag(boolean param)
	{
		_hasFlagCTF = param;
	}

	public void onHero(boolean hero)
	{
		if(!hero)
		{
			for(ItemInstance item : getInventory().getItems())
			{
				if(item.isHeroWeapon())
				{
					getInventory().destroyItemByItemId(item.getItemId(), 1L);
				}
			}
		}
	}

	public long getOnlineTime()
	{
		return _onlineTime;
	}
	public boolean hasHWID()
	{
		return _connection != null && _connection.hasHWID();
	}
	public HardwareID getHWID()
	{
		return _connection.HWIDS;
	}

	public HardwareID getAllowHWID()
	{
		return _connection.ALLOW_HWID;
	}
	public boolean isBanHWID()
	{
		return _connection.checkHWIDBanned();
	}
	public CustomMessage getOnlineTime(Player player)
	{
		int total = (int) player.getOnlineTime();
		int days = total / (1000 * 60 * 60 * 24) % 7;
		int hours = total / (1000 * 60 * 60) % 24;
		int minute = total / (1000 * 60) % 60;

		if(days >= 1)
			return new CustomMessage("jts.gameserver.model.Player.getOnlineTime.day", player).addNumber(days).addNumber(hours).addNumber(minute);
		else
			return new CustomMessage("jts.gameserver.model.Player.getOnlineTime.hour", player).addNumber(hours).addNumber(minute);
	}
	public String setPasswordResult(String result)
	{
		_changePasswordResult = result;
		return _changePasswordResult;
	}

	public String getPasswordResult()
	{
		return _changePasswordResult;
	}
	private boolean is_bbs_use = false;

	public void setIsBBSUse(boolean value)
	{
		is_bbs_use = value;
	}

	public boolean isBBSUse()
	{
		return is_bbs_use;
	}
	public void setScheme(String name, Scheme scheme)
	{
		schemes.put(name, scheme);
	}

	public void removeScheme(String name)
	{
		schemes.remove(name);
	}

	public Scheme getScheme(String name)
	{
		return schemes.get(name);
	}

	public THashMap<String, Scheme> getSchemes()
	{
		return schemes;
	}

	private boolean _isInDuel = false;

	public void setIsInDuel(boolean InDuel)
	{
		_isInDuel = InDuel;
	}
	public void CaptchaChatBlock()
	{
		_CaptchaChatBlocked = true;
	}
	
	public void unCaptchaChatBlock()
	{
		_CaptchaChatBlocked = false;
	}
	
	public boolean isCaptchaChatBlocked()
	{
		return _CaptchaChatBlocked;
	}
  
	public void setCaptchaReguest()
	{
		_lastCaptchaReguest = System.currentTimeMillis() + 1200000L;
	}

	public long getCaptchaReguest()
	{
		return _lastCaptchaReguest;
	}
	
	public void resetCaptchaAtemptRequest()
	{
		_tryCaptchaCount = 0;
	}
	
	public void addCaptchaAtemptRequest()
	{
		_tryCaptchaCount ++;
	}
	
	public int getCaptchaAtemptRequest()
	{
		return _tryCaptchaCount;
	}
	public String getCaptcha()
	{
		return _captcha;
	}
	private ScheduledFuture<?> _scheduledPlayerCaptchaValidatio;
	
	private class SchedulePlayerCaptchaValidation implements Runnable
	{
		private final long _playerStoredId;

		public SchedulePlayerCaptchaValidation(final Player player)
		{
			_playerStoredId = player.getStoredId();
		}

		@Override
		public void run()
		{
			final Player player = GameObjectsStorage.getAsPlayer(_playerStoredId);
			if(player != null && !player.getCaptcha().equals(""))
				CaptchaValidator.kickPlayer(player);
		}
	}
	
	public ScheduledFuture<?> getSchedulePlayerCaptchaValidation()
	{
		return _scheduledPlayerCaptchaValidatio;
	}
	
	public void resetSchedulePlayerCaptchaValidation()
	{
		_scheduledPlayerCaptchaValidatio.cancel(false);
	}
	
	public void setSchedulePlayerCaptchaValidation()
	{
		_scheduledPlayerCaptchaValidatio = ThreadPoolManager.getInstance().schedule(new SchedulePlayerCaptchaValidation(this), Config.CAPTCHA_TIME * 1000);
	}
	public void setCaptcha(final String captcha)
	{
		_captcha = captcha;
	}
    public long getCharCaptchaTime()
    {
        return _charCaptchaTime;
    }
	public void setCharCaptchaTime()
    {
        _charCaptchaTime = System.currentTimeMillis();
    }
    public void setCaptchaCountError(int captchaCount)
    {
        _captchaCount = captchaCount;
    }

    public int getCaptchaCountError()
    {
        return _captchaCount;
    }
	public boolean getIsInDuel()
	{
		return _isInDuel;
	}
	private boolean _IsPhantom = false;

	public void setIsPhantom(boolean is_ph)
	{
		_IsPhantom = is_ph;
	}

	public boolean isPhantom()
	{
		return _IsPhantom;
	}

	public Location _phantomLoc = null;
	
	public void setPhantomLoc(int x, int y, int z)
	{
		_phantomLoc = new Location(x, y, z);
	}

	public Location getPhantomLoc()
	{
		return _phantomLoc;
	}

	private int _fakeProtect = 0;

	public boolean rndWalk(Creature target, boolean fake)
	{
		rndWalk();
		if (_fakeProtect > 2)
			return false;
		_fakeProtect += 1;
		return true;
	}

	public void clearRndWalk()
	{
		
	}

	public void rndWalk()
	{
		int posX = getX();
		int posY = getY();
		int posZ = getZ();
		switch (Rnd.get(1, 8))
		{
			case 1:
				posX += 600;
				posY += 200;
				break;
			case 2:
				posX += 200;
				posY += 600;
				break;
			case 3:
				posX += 600;
				posY -= 200;
				break;
			case 4:
				posX += 200;
				posY -= 600;
				break;
			case 5:
				posX -= 600;
				posY += 200;
				break;
			case 6:
				posX -= 200;
				posY += 600;
				break;
			case 7:
				posX -= 200;
				posY -= 600;
				break;
			case 8:
				posX -= 600;
				posY -= 200;
		}
		setRunning();
		//getAI().addTaskMove(posX, posY, posZ, true);
		moveToLocation(posX, posY, posZ, 40, true);
		getAI().setNextAction(nextAction.INTERACT, null, null, false, false);
		//getAI().setNextAction(nextAction.MOVE, null, null, false, false);
	}
	private ScheduledFuture<?> _scheduledAutoCPUse;
	
	private class AutoCPUse implements Runnable
	{
		private final Player _player = getPlayer();
		private final int FirstItemID = 5592;
		private final int SecondItemID = 5591;
		private final double HoldCp = getMaxCp() * 0.01 * (getVarB("ACPhold") ? NumberUtils.toInt(getVar("ACPhold")) : 99);
		private boolean mayberun = false;
		
		public void run()
		{
			if(isInStoreMode() || isDead() || isStunned()|| (isInOfflineMode() || isParalyzed() || isSleeping() || isAlikeDead() || isHealBlocked()|| isInOlympiadMode() || isInPeaceZone()))
				return;
			
			if(getCurrentCp() < HoldCp)
				mayberun = true;
			
			if(getCurrentCp() >= getMaxCp())
				mayberun = false;
			
			if(!mayberun)
				return;
			
			if(ItemFunctions.getItemCount(_player, FirstItemID) > 0)
				doAutoCPregen(FirstItemID, 200);
			else if(ItemFunctions.getItemCount(_player, SecondItemID) > 0)
				doAutoCPregen(SecondItemID, 50);
			else
			{
				sendPacket(SystemMsg.THERE_ARE_NOT_ENOUGH_NECESSARY_ITEMS_TO_USE_THE_SKILL);
				getEffectList().stopEffects(EffectType.AutoCp);
			}
		}
	}
	private void doAutoCPregen(int ItemId, int countCPrestore)
	{
		ItemFunctions.removeItem(this, ItemId, 1, false);
		broadcastPacket(new MagicSkillUse(this, this, 2166, 1, 500, 500));
		
		if(getCurrentCp() + countCPrestore <= getMaxCp())
			setCurrentCp(getCurrentCp() + countCPrestore);
		else
			setCurrentCp(getMaxCp());
		
		sendPacket(new SystemMessage2(SystemMsg.S1_CP_HAS_BEEN_RESTORED).addInteger(countCPrestore));
	}
	
	public ScheduledFuture<?> stateAutoCPUse()
	{
		return _scheduledAutoCPUse;
	}
	
	public void endAutoCPUse()
	{
		if(_scheduledAutoCPUse != null)
			_scheduledAutoCPUse.cancel(false);
		_scheduledAutoCPUse = null;
	}

	public void runAutoCPUse()
	{
		if(_scheduledAutoCPUse == null)
			_scheduledAutoCPUse = ThreadPoolManager.getInstance().scheduleAtFixedRate(new AutoCPUse(), Config.AUTOCP_SKILL_TICK, Config.AUTOCP_SKILL_TICK);
	}

	public void offlineBuffStore()
	{
		if(_connection != null)
		{
			_connection.setActiveChar(null);
			_connection.close(ServerClose.STATIC);
			setNetConnection(null);
		}
		setVar("offline_buff", String.valueOf(System.currentTimeMillis() / 1000L), -1);

		setOfflineMode(true);

		Party party = getParty();
		if(party != null)
		{
			if(isFestivalParticipant())
			{
				party.broadcastMessageToPartyMembers(getName() + " has been removed from the upcoming festival.");
			}
			leaveParty();
		}

		if(getPet() != null)
		{
			getPet().unSummon();
		}

		CursedWeaponsManager.getInstance().doLogout(this);

		if(isInOlympiadMode() || (getOlympiadGame() != null))
		{
			Olympiad.logoutPlayer(this);
		}

		if(isInObserverMode())
		{
			if(getOlympiadObserveGame() == null)
			{
				leaveObserverMode();
			}
			else
			{
				leaveOlympiadObserverMode(true);
			}
			_observerMode.set(OBSERVER_NONE);
		}

		broadcastCharInfo();

		// Guardamos el offline buffer en la db al salir
		OfflineBuffersTable.getInstance().onLogout(this);

		// Stop all tasks
		stopWaterTask();
		stopBonusTask();
		stopHourlyTask();
		stopVitalityTask();
		stopPcBangPointsTask();
		stopAutoSaveTask();
		stopRecomBonusTask(true);
		stopQuestTimers();
		getNevitSystem().stopTasksOnLogout();

		try
		{
			getInventory().store();
		}
		catch(Throwable t)
		{
			_log.error("Error while storing Player Inventory", t);
		}

		try
		{
			store(false);
		}
		catch(Throwable t)
		{
			_log.error("Error while storing Player", t);
		}
	}
}