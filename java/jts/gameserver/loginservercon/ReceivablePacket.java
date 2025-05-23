package jts.gameserver.loginservercon;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ReceivablePacket extends jts.commons.net.nio.ReceivablePacket<LoginServerCommunication>
{
	private static final Logger _log = LoggerFactory.getLogger(ReceivablePacket.class);

	@Override
	public LoginServerCommunication getClient()
	{
		return LoginServerCommunication.getInstance();
	}

	@Override
	protected ByteBuffer getByteBuffer()
	{
		return getClient().getReadBuffer();
	}

	@Override
	public final boolean read()
	{
		try
		{
			readImpl();
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		return true;
	}

	@Override
	public final void run()
	{
		try
		{
			runImpl();
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
	}

	protected abstract void readImpl();

	protected abstract void runImpl();

	protected void sendPacket(SendablePacket sp)
	{
		getClient().sendPacket(sp);
	}
}
