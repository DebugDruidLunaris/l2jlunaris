package jts.gameserver.network.clientpackets;

import java.nio.BufferUnderflowException;
import java.util.List;

import jts.commons.net.nio.impl.ReceivablePacket;
import jts.gameserver.network.GameClient;
import jts.gameserver.network.serverpackets.L2GameServerPacket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class L2GameClientPacket extends ReceivablePacket<GameClient>
{
	private static final Logger _log = LoggerFactory.getLogger(L2GameClientPacket.class);

	@Override
	public final boolean read()
	{
		try
		{
			readImpl();
			return true;
		}
		catch(BufferUnderflowException e)
		{
			_client.onPacketReadFail();
			_log.error("Client: " + _client + " - Failed reading: " + getType(), e);
		}
		catch(Exception e)
		{
			_log.error("Client: " + _client + " - Failed reading: " + getType(), e);
		}

		return false;
	}

	protected abstract void readImpl() throws Exception;

	@Override
	public final void run()
	{
		GameClient client = getClient();
		try
		{
			runImpl();
		}
		catch(Exception e)
		{
			_log.error("Client: " + client + " - Failed running: " + getType(), e);
		}
	}

	protected abstract void runImpl() throws Exception;

	protected String readS(int len)
	{
		String ret = readS();
		return ret.length() > len ? ret.substring(0, len) : ret;
	}

	protected void sendPacket(L2GameServerPacket packet)
	{
		getClient().sendPacket(packet);
	}

	protected void sendPacket(L2GameServerPacket... packets)
	{
		getClient().sendPacket(packets);
	}

	protected void sendPackets(List<L2GameServerPacket> packets)
	{
		getClient().sendPackets(packets);
	}

	public String getType()
	{
		return "[C] " + getClass().getSimpleName();
	}
}