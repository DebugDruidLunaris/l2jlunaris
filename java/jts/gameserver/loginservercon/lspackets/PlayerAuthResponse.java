package jts.gameserver.loginservercon.lspackets;

import jts.gameserver.Config;
import jts.gameserver.cache.Msg;
import jts.gameserver.dao.AccountBonusDAO;
import jts.gameserver.loginservercon.LoginServerCommunication;
import jts.gameserver.loginservercon.ReceivablePacket;
import jts.gameserver.loginservercon.SessionKey;
import jts.gameserver.loginservercon.gspackets.PlayerInGame;
import jts.gameserver.model.Player;
import jts.gameserver.model.actor.instances.player.Bonus;
import jts.gameserver.network.GameClient;
import jts.gameserver.network.serverpackets.CharacterSelectionInfo;
import jts.gameserver.network.serverpackets.LoginFail;
import jts.gameserver.network.serverpackets.ServerClose;

public class PlayerAuthResponse extends ReceivablePacket
{
	private String account;
	private boolean authed;
	private int playOkId1;
	private int playOkId2;
	private int loginOkId1;
	private int loginOkId2;
	private double bonus;
	private int bonusExpire;
	private String hwid;

	@Override
	public void readImpl()
	{
		account = readS();
		authed = readC() == 1;
		if(authed)
		{
			playOkId1 = readD();
			playOkId2 = readD();
			loginOkId1 = readD();
			loginOkId2 = readD();
			bonus = readF();
			bonusExpire = readD();
		}
		hwid = readS();
	}

	@Override
	protected void runImpl()
	{
		SessionKey skey = new SessionKey(loginOkId1, loginOkId2, playOkId1, playOkId2);
		GameClient client = LoginServerCommunication.getInstance().removeWaitingClient(account);
		if(client == null)
			return;

		if(authed && client.getSessionKey().equals(skey))
		{
			client.setAuthed(true);
			client.setState(GameClient.GameClientState.AUTHED);
			switch(Config.SERVICES_RATE_TYPE)
			{
				case Bonus.NO_BONUS:
					bonus = 1.;
					bonusExpire = 0;
					break;
				case Bonus.BONUS_GLOBAL_ON_GAMESERVER:
					double[] bonuses = AccountBonusDAO.getInstance().select(account);
					bonus = bonuses[0];
					bonusExpire = (int) bonuses[1];
					break;
			}
			client.setBonus(bonus);
			client.setBonusExpire(bonusExpire);

			GameClient oldClient = LoginServerCommunication.getInstance().addAuthedClient(client);
			if(oldClient != null)
			{
				oldClient.setAuthed(false);
				Player activeChar = oldClient.getActiveChar();
				if(activeChar != null)
				{
					//FIXME [G1ta0] сообщение чаще всего не показывается, т.к. при закрытии соединения очередь на отправку очищается
					activeChar.sendPacket(Msg.ANOTHER_PERSON_HAS_LOGGED_IN_WITH_THE_SAME_ACCOUNT);
					activeChar.logout();
				}
				else
					oldClient.close(ServerClose.STATIC);
			}

			sendPacket(new PlayerInGame(client.getLogin()));

			CharacterSelectionInfo csi = new CharacterSelectionInfo(client.getLogin(), client.getSessionKey().playOkID1);
			client.sendPacket(csi);
			client.setCharSelection(csi.getCharInfo());
			client.checkHwid(hwid);
		}
		else
			client.close(new LoginFail(LoginFail.ACCESS_FAILED_TRY_LATER));
	}
}