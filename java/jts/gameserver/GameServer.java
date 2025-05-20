package jts.gameserver;

import java.io.File;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;

import jts.commons.lang.StatsUtils;
import jts.commons.listener.Listener;
import jts.commons.listener.ListenerList;
import jts.commons.net.nio.impl.SelectorThread;
import jts.commons.versioning.Version;
import jts.gameserver.ai.Keltirs;
import jts.gameserver.ai.Taurin;
import jts.gameserver.cache.CrestCache;
import jts.gameserver.cache.ImagesCache;
import jts.gameserver.captcha.CaptchaValidator;
import jts.gameserver.dao.CharacterDAO;
import jts.gameserver.dao.ItemsDAO;
import jts.gameserver.data.BoatHolder;
import jts.gameserver.data.xml.Parsers;
import jts.gameserver.data.xml.holder.EventHolder;
import jts.gameserver.data.xml.holder.ResidenceHolder;
import jts.gameserver.data.xml.holder.StaticObjectHolder;
import jts.gameserver.database.DatabaseFactory;
import jts.gameserver.geodata.GeoEngine;
import jts.gameserver.geodata.geoeditorcon.GeoEditorConnector;
import jts.gameserver.instancemanager.AutoHuntingManager;
import jts.gameserver.handler.admincommands.AdminCommandHandler;
import jts.gameserver.handler.items.ItemHandler;
import jts.gameserver.handler.usercommands.UserCommandHandler;
import jts.gameserver.handler.voicecommands.VoicedCommandHandler;
import jts.gameserver.idfactory.IdFactory;
import jts.gameserver.instancemanager.AutoSpawnManager;
import jts.gameserver.instancemanager.BloodAltarManager;
import jts.gameserver.instancemanager.CastleManorManager;
import jts.gameserver.instancemanager.CoupleManager;
import jts.gameserver.instancemanager.CursedWeaponsManager;
import jts.gameserver.instancemanager.DimensionalRiftManager;
import jts.gameserver.instancemanager.HellboundManager;
import jts.gameserver.instancemanager.L2TopManager;
import jts.gameserver.instancemanager.MMOTopManager;
import jts.gameserver.instancemanager.PetitionManager;
import jts.gameserver.instancemanager.PlayerMessageStack;
import jts.gameserver.instancemanager.RaidBossSpawnManager;
import jts.gameserver.instancemanager.SoDManager;
import jts.gameserver.instancemanager.SoIManager;
import jts.gameserver.instancemanager.SpawnManager;
import jts.gameserver.instancemanager.games.FishingChampionShipManager;
import jts.gameserver.instancemanager.games.LotteryManager;
import jts.gameserver.instancemanager.games.MiniGameScoreManager;
import jts.gameserver.instancemanager.itemauction.ItemAuctionManager;
import jts.gameserver.instancemanager.naia.NaiaCoreManager;
import jts.gameserver.instancemanager.naia.NaiaTowerManager;
import jts.gameserver.listener.GameListener;
import jts.gameserver.listener.game.OnShutdownListener;
import jts.gameserver.listener.game.OnStartListener;
import jts.gameserver.loginservercon.LoginServerCommunication;
import jts.gameserver.model.GameObjectsStorage;
import jts.gameserver.model.World;
import jts.gameserver.model.entity.Hero;
import jts.gameserver.model.entity.MonsterRace;
import jts.gameserver.model.entity.SevenSigns;
import jts.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import jts.gameserver.model.entity.olympiad.Olympiad;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.network.GameClient;
import jts.gameserver.network.GamePacketHandler;
import jts.gameserver.network.telnet.TelnetServer;
import jts.gameserver.scripts.Scripts;
import jts.gameserver.tables.AugmentationData;
import jts.gameserver.tables.ClanTable;
import jts.gameserver.tables.EnchantHPBonusTable;
import jts.gameserver.tables.LevelUpTable;
import jts.gameserver.tables.PetDataTable;
import jts.gameserver.tables.PetSkillsTable;
import jts.gameserver.tables.SkillTreeTable;
import jts.gameserver.taskmanager.ItemsAutoDestroy;
import jts.gameserver.taskmanager.TaskManager;
import jts.gameserver.taskmanager.tasks.RestoreOfflineTraders;
import jts.gameserver.instancemanager.DragonValleyManager;
import jts.gameserver.dao.ClanListServiceDAO;
import jts.gameserver.model.PhantomPlayers;
import jts.gameserver.database.OfflineBuffersTable;
import net.sf.ehcache.CacheManager;
import jts.gameserver.utils.HWID;
import jts.gameserver.utils.Loader;
import jts.gameserver.utils.Util;
import GameGuard.GameGuard;
import GameGuard.GGConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameServer
{
	public static final int LOGIN_SERVER_PROTOCOL = 2;
	private static final Logger _log = LoggerFactory.getLogger(GameServer.class);
	private long _serverStartTimeMillis;

	public class GameServerListenerList extends ListenerList<GameServer>
	{
		public void onStart()
		{
			for(Listener<GameServer> listener : getListeners())
				if(OnStartListener.class.isInstance(listener))
					((OnStartListener) listener).onStart();
		}

		public void onShutdown()
		{
			for(Listener<GameServer> listener : getListeners())
				if(OnShutdownListener.class.isInstance(listener))
					((OnShutdownListener) listener).onShutdown();
		}
	}
	public long getServerStartTime() {
		return _serverStartTimeMillis;
	}

	private static final int[] _keltirsList = {
			20481,
			20529,
			20530,
			20531,
			20532,
			20533,
			20534,
			20535,
			20536,
			20537,
			20538,
			20539,
			20544,
			20545,
			22229,
			22230,
			22231,
			18003 };

	private static final int[] _taurinList = { 30086 };

	public static GameServer _instance;

	private final SelectorThread<GameClient> _selectorThreads[];
	public static Version version;
	private TelnetServer statusServer;
	private final GameServerListenerList _listeners;

	private int _serverStarted;

	public SelectorThread<GameClient>[] getSelectorThreads()
	{
		return _selectorThreads;
	}

	public int time()
	{
		return (int) (System.currentTimeMillis() / 1000);
	}

    @SuppressWarnings({ "unchecked", "unused" })
	public GameServer() throws Exception
	{
		_instance = this;
        long serverLoadStart = System.currentTimeMillis();
		_serverStarted = time();
		_listeners = new GameServerListenerList();
		new File("./log/").mkdir();
		new File("./log/chars").mkdir();
		version = new Version(GameServer.class);
		_log.info("|================ START GAME SERVER ==================|");
		_log.info("");
		_log.info("|=================INFORMATION===================|");
		_log.info("| Powered by Prototype-Project (C) 2015");
		_log.info("| Version: " + version.getVersionNumber());
		_log.info("| Build Date: " + version.getBuildDate());
		_log.info("| Website:http://prototype-project.ru ");
		_log.info("|=================INFORMATION===================|");
		_log.info("");
		_log.info("|==================SYSTEM INFORMATION===========|");
		_log.info("| " + Util.getOSInfo());
        _log.info("| " + Util.getCpuInfo()[0]);
        _log.info("| " + Util.getCpuInfo()[1]);
		_log.info("| " + Util.getJavaInfo());
		_log.info("|==================SYSTEM INFORMATION===========|");
		_log.info("");
		_log.info("|================== Configurations Loaded ===========|");
		_log.info("");
		Config.load();
		_log.info("|================== Configurations Loaded Finish ===========|");
		_log.info("");
	    Date date = new Date();
	    System.out.println("The current java time-stamp is:");
	    System.out.println(new Timestamp(date.getTime()));
		checkFreePorts();
		// Initialize database
		Class.forName(Config.DATABASE_DRIVER).newInstance();
		DatabaseFactory.getInstance().getConnection().close();

		IdFactory _idFactory = IdFactory.getInstance();
		if(!_idFactory.isInitialized())
		{
			_log.error("Could not read object IDs from DB. Please Check Your Data.");
			throw new Exception("Could not initialize the ID factory");
		}

		CacheManager.getInstance();

		ThreadPoolManager.getInstance();
		Scripts.getInstance();
		_log.info("|================== GeoData Loaded ===========|");
		_log.info("");
		GeoEngine.load();
		_log.info("|================== GeoData Loaded Finish ===========|");
		GeoEditorConnector.getInstance();
		_log.info("");
		ImagesCache.getInstance();
		GameTimeController.getInstance();
		World.init();
		Parsers.parseAll();
		ItemsDAO.getInstance();
		CrestCache.getInstance();
		CharacterDAO.getInstance();
		ClanTable.getInstance();
		SkillTreeTable.getInstance();
		AugmentationData.getInstance();
		EnchantHPBonusTable.getInstance();
		LevelUpTable.getInstance();
		PetSkillsTable.getInstance();
		PetDataTable.getInstance();
		ItemAuctionManager.getInstance();
		BoatHolder.getInstance().spawnAll();
		StaticObjectHolder.getInstance().spawnAll();
		RaidBossSpawnManager.getInstance();
		Scripts.getInstance().init();
		DimensionalRiftManager.getInstance();
		Announcements.getInstance();
		LotteryManager.getInstance();
		PlayerMessageStack.getInstance();

		if(Config.AUTODESTROY_ITEM_AFTER > 0)
			ItemsAutoDestroy.getInstance();

		MonsterRace.getInstance();
		SevenSigns.getInstance();
		SevenSignsFestival.getInstance();
		SevenSigns.getInstance().updateFestivalScore();
		AutoSpawnManager.getInstance();
		SevenSigns.getInstance().spawnSevenSignsNPC();

		if(Config.OLYMPIAD_ENABLE)
		{
			Olympiad.load();
			Hero.getInstance();
		}

		PetitionManager.getInstance();
		CursedWeaponsManager.getInstance();

		if(!Config.ALLOW_WEDDING)
		{
			CoupleManager.getInstance();
			_log.info("CoupleManager initialized");
		}

		ItemHandler.getInstance();
		AdminCommandHandler.getInstance().log();
		UserCommandHandler.getInstance().log();
		VoicedCommandHandler.getInstance().log();

		TaskManager.getInstance();
		AutoHuntingManager.getInstance();
		_log.info("|================== Siege Loaded ===========|");
		_log.info("");
		ResidenceHolder.getInstance().callInit();
		EventHolder.getInstance().callInit();
		CastleManorManager.getInstance();
		Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());

		_log.info("IdFactory: Free ObjectID's remaining: " + IdFactory.getInstance().size());

		CoupleManager.getInstance();

		if(Config.ALT_FISH_CHAMPIONSHIP_ENABLED)
			FishingChampionShipManager.getInstance();

        DragonValleyManager.getInstance();
		HellboundManager.getInstance();
		NaiaTowerManager.getInstance();
		NaiaCoreManager.getInstance();
		SoDManager.getInstance();
		SoIManager.getInstance();
		BloodAltarManager.getInstance();
		MiniGameScoreManager.getInstance();
		L2TopManager.getInstance();
		MMOTopManager.getInstance();

		if(Config.ALT_AI_KELTIRS)
			for(int id : _keltirsList)
				for(NpcInstance i : GameObjectsStorage.getAllByNpcId(id, false))
					i.setAI(new Keltirs(i));

		if(Config.ALT_AI_TAURIN)
			for(int id : _taurinList)
				for(NpcInstance i : GameObjectsStorage.getAllByNpcId(id, false))
					i.setAI(new Taurin(i));

        if (Config.CAPTCHA_ENABLE)
        {
            CaptchaValidator.getInstance();
        }
		GamePacketHandler gph = new GamePacketHandler();
		if(Config.SERVICES_CLAN_ACADEM_ENABLED)
			ClanListServiceDAO.getInstance().clearList(1);


		InetAddress serverAddr = Config.GAMESERVER_HOSTNAME.equalsIgnoreCase("*") ? null : InetAddress.getByName(Config.GAMESERVER_HOSTNAME);

		_selectorThreads = new SelectorThread[Config.PORTS_GAME.length];
		for(int i = 0; i < Config.PORTS_GAME.length; i++)
		{
			_selectorThreads[i] = new SelectorThread<>(Config.SELECTOR_CONFIG, gph, gph, gph, null);
			_selectorThreads[i].openServerSocket(serverAddr, Config.PORTS_GAME[i]);
			_selectorThreads[i].start();
		}

		if(Config.SERVICES_OFFLINE_TRADE_RESTORE_AFTER_RESTART)
			ThreadPoolManager.getInstance().schedule(new RestoreOfflineTraders(), 30000L);
		if(Config.BUFF_STORE_ENABLED)
			OfflineBuffersTable.getInstance().restoreOfflineBuffers();

		getListeners().onStart();

		if(Config.IS_TELNET_ENABLED)
			statusServer = new TelnetServer();
		else
			_log.info("Telnet server is currently disabled.");

		_log.info("=================================================");
		String memUsage = new StringBuilder().append(StatsUtils.getMemUsage()).toString();
		for (String line : memUsage.split("\n"))
		{
			_log.info(line);
		}
		
		_log.info("=================================================");
		_log.info("Server Loaded in " + (System.currentTimeMillis() / 1000 - _serverStarted) + " seconds");
		Shutdown.getInstance().schedule(Config.RESTART_AT_TIME, Shutdown.RESTART);
		LoginServerCommunication.getInstance().start();
		_log.info("GameServer Started: " + Config.GAMESERVER_HOSTNAME);
		_log.info("StatusServer port:" + Arrays.toString(Config.PORTS_GAME) + " Started");
		_log.info("Maximum Numbers of Connected Players: " + Config.MAXIMUM_ONLINE_USERS);
		_log.info("|=================INFORMATION===================|");
		_log.info("| Powered by Prototype-Project (C) 2015");
		_log.info("| Version: " + version.getVersionNumber());
		_log.info("| Build Date: " + version.getBuildDate());
		_log.info("| Website:http://Prototype-Project.ru/ ");
		_log.info("|=================INFORMATION===================|");
        GameGuard.Init();
	//	Loader.getInstance();
        if (GameGuard.isProtectionOn()) {
            _log.info("GameGuard support enabled.");
            if (!GGConfig.PROTECT_GS_ENABLE_HWID_BANS)
            {
                HWID.reloadBannedHWIDs();
            }
        }
        if (Config.ALLOW_PHANTOM_PLAYERS)
        {
            PhantomPlayers.init();
        }
		SpawnManager.getInstance().spawnAll();
	}
    

	public GameServerListenerList getListeners()
	{
		return _listeners;
	}

	public static GameServer getInstance()
	{
		return _instance;
	}

	public <T extends GameListener> boolean addListener(T listener)
	{
		return _listeners.add(listener);
	}

	public <T extends GameListener> boolean removeListener(T listener)
	{
		return _listeners.remove(listener);
	}

	public static void checkFreePorts()
	{
		boolean binded = false;
		while(!binded)
			for(int PORT_GAME : Config.PORTS_GAME)
				try
				{
					ServerSocket ss;
					if(Config.GAMESERVER_HOSTNAME.equalsIgnoreCase("*"))
						ss = new ServerSocket(PORT_GAME);
					else
						ss = new ServerSocket(PORT_GAME, 50, InetAddress.getByName(Config.GAMESERVER_HOSTNAME));
					ss.close();
					binded = true;
				}
				catch(Exception e)
				{
					_log.warn("Port " + PORT_GAME + " is allready binded. Please free it and restart server.");
					binded = false;
					try
					{
						Thread.sleep(1000);
					}
					catch(InterruptedException e2)
					{}
				}
	}

	public static void main(String[] args) throws Exception
	{
		new GameServer();
	}

	public TelnetServer getStatusServer()
	{
		return statusServer;
	}
}