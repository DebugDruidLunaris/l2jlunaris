package jts.loginserver;

import java.nio.channels.SocketChannel;

import jts.commons.net.nio.impl.IAcceptFilter;
import jts.commons.net.nio.impl.IClientFactory;
import jts.commons.net.nio.impl.IMMOExecutor;
import jts.commons.net.nio.impl.MMOConnection;
import jts.commons.threading.RunnableImpl;
import jts.loginserver.serverpackets.Init;

public class SelectorHelper implements IMMOExecutor<L2LoginClient>, IClientFactory<L2LoginClient>, IAcceptFilter
{
	@Override
	public void execute(Runnable r)
	{
		ThreadPoolManager.getInstance().execute(r);
	}

	@Override
	public L2LoginClient create(MMOConnection<L2LoginClient> con)
	{
		final L2LoginClient client = new L2LoginClient(con);
		client.sendPacket(new Init(client));
		ThreadPoolManager.getInstance().schedule(new RunnableImpl(){
			@Override
			public void runImpl()
			{
				client.closeNow(false);
			}
		}, Config.LOGIN_TIMEOUT);
		return client;
	}

	@Override
	public boolean accept(SocketChannel sc)
	{
		return !IpBanManager.getInstance().isIpBanned(sc.socket().getInetAddress().getHostAddress());
	}
}