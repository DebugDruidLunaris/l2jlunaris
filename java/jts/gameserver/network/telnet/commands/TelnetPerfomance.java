package jts.gameserver.network.telnet.commands;

import com.sun.management.HotSpotDiagnosticMXBean;
import jts.commons.dao.JdbcEntityStats;
import jts.commons.lang.StatsUtils;
import jts.commons.net.nio.impl.SelectorThread;
import jts.commons.threading.RunnableStatsManager;
import jts.gameserver.Config;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.dao.ItemsDAO;
import jts.gameserver.dao.MailDAO;
import jts.gameserver.database.DatabaseFactory;
import jts.gameserver.geodata.PathFindBuffers;
import jts.gameserver.network.telnet.TelnetCommand;
import jts.gameserver.network.telnet.TelnetCommandHolder;
import jts.gameserver.taskmanager.AiTaskManager;
import jts.gameserver.taskmanager.EffectTaskManager;
import jts.gameserver.utils.GameStats;
import net.sf.ehcache.Cache;
import net.sf.ehcache.statistics.LiveCacheStatistics;
import org.apache.commons.io.FileUtils;

import javax.management.MBeanServer;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.LinkedHashSet;
import java.util.Set;

public class TelnetPerfomance implements TelnetCommandHolder
{
	private Set<TelnetCommand> _commands = new LinkedHashSet<TelnetCommand>();

	public TelnetPerfomance()
	{
		_commands.add(new TelnetCommand("pool", "p"){
			@Override
			public String getUsage()
			{
				return "pool [dump]";
			}

			@Override
			public String handle(String[] args)
			{
				StringBuilder sb = new StringBuilder();

				if(args.length == 0 || args[0].isEmpty())
					sb.append(ThreadPoolManager.getInstance().getStats());
				else if(args[0].equals("dump") || args[0].equals("d"))
					try
					{
						new File("stats").mkdir();
						FileUtils.writeStringToFile(new File("stats/RunnableStats-" + new SimpleDateFormat("MMddHHmmss").format(System.currentTimeMillis()) + ".txt"), RunnableStatsManager.getInstance().getStats().toString());
						sb.append("Runnable stats saved.\n");
					}
					catch(IOException e)
					{
						sb.append("Exception: " + e.getMessage() + "!\n");
					}
				else
					return null;

				return sb.toString();
			}

		});

		_commands.add(new TelnetCommand("mem", "m"){
			@Override
			public String getUsage()
			{
				return "mem";
			}

			@Override
			public String handle(String[] args)
			{
				StringBuilder sb = new StringBuilder();
				sb.append(StatsUtils.getMemUsage());

				return sb.toString();
			}
		});

		_commands.add(new TelnetCommand("heap"){

			@Override
			public String getUsage()
			{
				return "heap [dump] <live>";
			}

			@Override
			public String handle(String[] args)
			{
				StringBuilder sb = new StringBuilder();

				if(args.length == 0 || args[0].isEmpty())
					return null;
				else if(args[0].equals("dump") || args[0].equals("d"))
					try
					{
						boolean live = args.length == 2 && !args[1].isEmpty() && (args[1].equals("live") || args[1].equals("l"));
						new File("dumps").mkdir();
						String filename = "dumps/HeapDump" + (live ? "Live" : "") + "-" + new SimpleDateFormat("MMddHHmmss").format(System.currentTimeMillis()) + ".hprof";

						MBeanServer server = ManagementFactory.getPlatformMBeanServer();
						HotSpotDiagnosticMXBean bean = ManagementFactory.newPlatformMXBeanProxy(server, "com.sun.management:type=HotSpotDiagnostic", HotSpotDiagnosticMXBean.class);
						bean.dumpHeap(filename, live);

						sb.append("Heap dumped.\n");
					}
					catch(IOException e)
					{
						sb.append("Exception: " + e.getMessage() + "!\n");
					}
				else
					return null;

				return sb.toString();
			}

		});
		_commands.add(new TelnetCommand("threads", "t"){
			@Override
			public String getUsage()
			{
				return "threads [dump]";
			}

			@Override
			public String handle(String[] args)
			{
				StringBuilder sb = new StringBuilder();

				if(args.length == 0 || args[0].isEmpty())
					sb.append(StatsUtils.getThreadStats());
				else if(args[0].equals("dump") || args[0].equals("d"))
					try
					{
						new File("stats").mkdir();
						FileUtils.writeStringToFile(new File("stats/ThreadsDump-" + new SimpleDateFormat("MMddHHmmss").format(System.currentTimeMillis()) + ".txt"), StatsUtils.getThreadStats(true, true, true).toString());
						sb.append("Threads stats saved.\n");
					}
					catch(IOException e)
					{
						sb.append("Exception: " + e.getMessage() + "!\n");
					}
				else
					return null;

				return sb.toString();
			}
		});

		_commands.add(new TelnetCommand("gc"){
			@Override
			public String getUsage()
			{
				return "gc";
			}

			@Override
			public String handle(String[] args)
			{
				StringBuilder sb = new StringBuilder();
				sb.append(StatsUtils.getGCStats());

				return sb.toString();
			}
		});

		_commands.add(new TelnetCommand("net", "ns"){
			@Override
			public String getUsage()
			{
				return "net";
			}

			@Override
			public String handle(String[] args)
			{
				StringBuilder sb = new StringBuilder();

				sb.append(SelectorThread.getStats());

				return sb.toString();
			}

		});

		_commands.add(new TelnetCommand("pathfind", "pfs"){

			@Override
			public String getUsage()
			{
				return "pathfind";
			}

			@Override
			public String handle(String[] args)
			{
				StringBuilder sb = new StringBuilder();

				sb.append(PathFindBuffers.getStats());

				return sb.toString();
			}

		});

		_commands.add(new TelnetCommand("dbstats", "ds"){

			@Override
			public String getUsage()
			{
				return "dbstats";
			}

			@Override
			public String handle(String[] args)
			{
				StringBuilder sb = new StringBuilder();

				sb.append("Basic database usage\n");
				sb.append("=================================================\n");
				sb.append("Connections").append("\n");
				try
				{
					sb.append("     Busy: ........................ ").append(DatabaseFactory.getInstance().getBusyConnectionCount()).append("\n");
					sb.append("     Idle: ........................ ").append(DatabaseFactory.getInstance().getIdleConnectionCount()).append("\n");
				}
				catch(SQLException e)
				{
					return "Error: " + e.getMessage() + "\n";
				}

				sb.append("Players").append("\n");
				sb.append("     Update: ...................... ").append(GameStats.getUpdatePlayerBase()).append("\n");

				double cacheHitCount, cacheMissCount, cacheHitRatio;
				Cache cache;
				LiveCacheStatistics cacheStats;
				JdbcEntityStats entityStats;

				cache = ItemsDAO.getInstance().getCache();
				cacheStats = cache.getLiveCacheStatistics();
				entityStats = ItemsDAO.getInstance().getStats();

				cacheHitCount = cacheStats.getCacheHitCount();
				cacheMissCount = cacheStats.getCacheMissCount();
				cacheHitRatio = cacheHitCount / (cacheHitCount + cacheMissCount);

				sb.append("Items").append("\n");
				sb.append("     getLoadCount: ................ ").append(entityStats.getLoadCount()).append("\n");
				sb.append("     getInsertCount: .............. ").append(entityStats.getInsertCount()).append("\n");
				sb.append("     getUpdateCount: .............. ").append(entityStats.getUpdateCount()).append("\n");
				sb.append("     getDeleteCount: .............. ").append(entityStats.getDeleteCount()).append("\n");
				sb.append("Cache").append("\n");
				sb.append("     getPutCount: ................. ").append(cacheStats.getPutCount()).append("\n");
				sb.append("     getUpdateCount: .............. ").append(cacheStats.getUpdateCount()).append("\n");
				sb.append("     getRemovedCount: ............. ").append(cacheStats.getRemovedCount()).append("\n");
				sb.append("     getEvictedCount: ............. ").append(cacheStats.getEvictedCount()).append("\n");
				sb.append("     getExpiredCount: ............. ").append(cacheStats.getExpiredCount()).append("\n");
				sb.append("     getSize: ..................... ").append(cacheStats.getSize()).append("\n");
				sb.append("     cacheHitRatio: ............... ").append(String.format("%2.2f", cacheHitRatio)).append("\n");
				sb.append("=================================================\n");

				cache = MailDAO.getInstance().getCache();
				cacheStats = cache.getLiveCacheStatistics();
				entityStats = MailDAO.getInstance().getStats();

				cacheHitCount = cacheStats.getCacheHitCount();
				cacheMissCount = cacheStats.getCacheMissCount();
				cacheHitRatio = cacheHitCount / (cacheHitCount + cacheMissCount);

				sb.append("Mail").append("\n");
				sb.append("     getLoadCount: ................ ").append(entityStats.getLoadCount()).append("\n");
				sb.append("     getInsertCount: .............. ").append(entityStats.getInsertCount()).append("\n");
				sb.append("     getUpdateCount: .............. ").append(entityStats.getUpdateCount()).append("\n");
				sb.append("     getDeleteCount: .............. ").append(entityStats.getDeleteCount()).append("\n");
				sb.append("Cache").append("\n");
				sb.append("     getPutCount: ................. ").append(cacheStats.getPutCount()).append("\n");
				sb.append("     getUpdateCount: .............. ").append(cacheStats.getUpdateCount()).append("\n");
				sb.append("     getRemovedCount: ............. ").append(cacheStats.getRemovedCount()).append("\n");
				sb.append("     getEvictedCount: ............. ").append(cacheStats.getEvictedCount()).append("\n");
				sb.append("     getExpiredCount: ............. ").append(cacheStats.getExpiredCount()).append("\n");
				sb.append("     getSize: ..................... ").append(cacheStats.getSize()).append("\n");
				sb.append("     cacheHitRatio: ............... ").append(String.format("%2.2f", cacheHitRatio)).append("\n");
				sb.append("=================================================\n");

				return sb.toString();
			}

		});

		_commands.add(new TelnetCommand("aistats", "as"){

			@Override
			public String getUsage()
			{
				return "aistats";
			}

			@Override
			public String handle(String[] args)
			{
				StringBuilder sb = new StringBuilder();

				for(int i = 0; i < Config.AI_TASK_MANAGER_COUNT; i++)
				{
					sb.append("AiTaskManager #").append(i + 1).append("\n");
					sb.append("=================================================\n");
					sb.append(AiTaskManager.getInstance().getStats(i));
					sb.append("=================================================\n");
				}

				return sb.toString();
			}

		});
		_commands.add(new TelnetCommand("effectstats", "es"){

			@Override
			public String getUsage()
			{
				return "effectstats";
			}

			@Override
			public String handle(String[] args)
			{
				StringBuilder sb = new StringBuilder();

				for(int i = 0; i < Config.EFFECT_TASK_MANAGER_COUNT; i++)
				{
					sb.append("EffectTaskManager #").append(i + 1).append("\n");
					sb.append("=================================================\n");
					sb.append(EffectTaskManager.getInstance().getStats(i));
					sb.append("=================================================\n");
				}

				return sb.toString();
			}

		});
	}

	@Override
	public Set<TelnetCommand> getCommands()
	{
		return _commands;
	}
}