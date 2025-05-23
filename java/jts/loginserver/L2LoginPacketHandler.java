package jts.loginserver;

import java.nio.ByteBuffer;

import jts.commons.net.nio.impl.IPacketHandler;
import jts.commons.net.nio.impl.ReceivablePacket;
import jts.loginserver.L2LoginClient.LoginClientState;
import jts.loginserver.clientpackets.AuthGameGuard;
import jts.loginserver.clientpackets.RequestAuthLogin;
import jts.loginserver.clientpackets.RequestServerList;
import jts.loginserver.clientpackets.RequestServerLogin;

public final class L2LoginPacketHandler implements IPacketHandler<L2LoginClient>
{
	@SuppressWarnings("incomplete-switch")
	@Override
	public ReceivablePacket<L2LoginClient> handlePacket(ByteBuffer buf, L2LoginClient client)
	{
		int opcode = buf.get() & 0xFF;

		ReceivablePacket<L2LoginClient> packet = null;
		LoginClientState state = client.getState();

		switch(state)
		{
			case CONNECTED:
				if(opcode == 0x07)
					packet = new AuthGameGuard();
				break;
			case AUTHED_GG:
				if(opcode == 0x00)
					packet = new RequestAuthLogin();
				break;
			case AUTHED:
				if(opcode == 0x05)
					packet = new RequestServerList(false);
				else if(opcode == 0x02)
					packet = new RequestServerLogin();
				break;
			case FAKE_LOGIN:
				if(opcode == 0x05)
					packet = new RequestServerList(true);
				else if(opcode == 0x02)
					packet = new RequestServerLogin();
				break;
		}
		return packet;
	}
}