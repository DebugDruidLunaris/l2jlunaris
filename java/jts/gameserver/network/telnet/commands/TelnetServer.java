package jts.gameserver.network.telnet.commands;

import java.lang.management.ManagementFactory;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.Set;

import jts.gameserver.Shutdown;
import jts.gameserver.network.telnet.TelnetCommand;
import jts.gameserver.network.telnet.TelnetCommandHolder;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;

public class TelnetServer implements TelnetCommandHolder
{
	private Set<TelnetCommand> _commands = new LinkedHashSet<TelnetCommand>();

	public TelnetServer()
	{
		_commands.add(new TelnetCommand("uptime"){
			@Override
			public String getUsage()
			{
				return "uptime";
			}

			@Override
			public String handle(String[] args)
			{
				return DurationFormatUtils.formatDurationHMS(ManagementFactory.getRuntimeMXBean().getUptime()) + "\n";
			}
		});

		_commands.add(new TelnetCommand("restart"){
			@Override
			public String getUsage()
			{
				return "restart <seconds>|now>";
			}

			@Override
			public String handle(String[] args)
			{
				if(args.length == 0)
					return null;

				StringBuilder sb = new StringBuilder();

				if(NumberUtils.isNumber(args[0]))
				{
					int val = NumberUtils.toInt(args[0]);
					Shutdown.getInstance().schedule(val, Shutdown.RESTART);
					sb.append("Server will restart in ").append(Shutdown.getInstance().getSeconds()).append(" seconds!\n");
					sb.append("Type \"abort\" to abort restart!\n");
				}
				else if(args[0].equalsIgnoreCase("now"))
				{
					sb.append("Server will restart now!\n");
					Shutdown.getInstance().schedule(0, Shutdown.RESTART);
				}
				else
				{
					String[] hhmm = args[0].split(":");

					Calendar date = Calendar.getInstance();
					Calendar now = Calendar.getInstance();

					date.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hhmm[0]));
					date.set(Calendar.MINUTE, hhmm.length > 1 ? Integer.parseInt(hhmm[1]) : 0);
					date.set(Calendar.SECOND, 0);
					date.set(Calendar.MILLISECOND, 0);
					if(date.before(now))
						date.roll(Calendar.DAY_OF_MONTH, true);

					int seconds = (int) (date.getTimeInMillis() / 1000L - now.getTimeInMillis() / 1000L);

					Shutdown.getInstance().schedule(seconds, Shutdown.RESTART);
					sb.append("Server will restart in ").append(Shutdown.getInstance().getSeconds()).append(" seconds!\n");
					sb.append("Type \"abort\" to abort restart!\n");
				}

				return sb.toString();
			}
		});

		_commands.add(new TelnetCommand("shutdown"){
			@Override
			public String getUsage()
			{
				return "shutdown <seconds>|now|<hh:mm>";
			}

			@Override
			public String handle(String[] args)
			{
				if(args.length == 0)
					return null;

				StringBuilder sb = new StringBuilder();

				if(NumberUtils.isNumber(args[0]))
				{
					int val = NumberUtils.toInt(args[0]);
					Shutdown.getInstance().schedule(val, Shutdown.SHUTDOWN);
					sb.append("Server will shutdown in ").append(Shutdown.getInstance().getSeconds()).append(" seconds!\n");
					sb.append("Type \"abort\" to abort shutdown!\n");
				}
				else if(args[0].equalsIgnoreCase("now"))
				{
					sb.append("Server will shutdown now!\n");
					Shutdown.getInstance().schedule(0, Shutdown.SHUTDOWN);
				}
				else
				{
					String[] hhmm = args[0].split(":");

					Calendar date = Calendar.getInstance();
					Calendar now = Calendar.getInstance();

					date.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hhmm[0]));
					date.set(Calendar.MINUTE, hhmm.length > 1 ? Integer.parseInt(hhmm[1]) : 0);
					date.set(Calendar.SECOND, 0);
					date.set(Calendar.MILLISECOND, 0);
					if(date.before(now))
						date.roll(Calendar.DAY_OF_MONTH, true);

					int seconds = (int) (date.getTimeInMillis() / 1000L - now.getTimeInMillis() / 1000L);

					Shutdown.getInstance().schedule(seconds, Shutdown.SHUTDOWN);
					sb.append("Server will shutdown in ").append(Shutdown.getInstance().getSeconds()).append(" seconds!\n");
					sb.append("Type \"abort\" to abort shutdown!\n");
				}

				return sb.toString();
			}
		});

		_commands.add(new TelnetCommand("abort"){

			@Override
			public String getUsage()
			{
				return "abort";
			}

			@Override
			public String handle(String[] args)
			{
				Shutdown.getInstance().cancel();
				return "Aborted.\n";
			}

		});
	}

	@Override
	public Set<TelnetCommand> getCommands()
	{
		return _commands;
	}
}