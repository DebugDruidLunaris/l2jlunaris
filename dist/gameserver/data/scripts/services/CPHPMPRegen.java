package services;

import jts.gameserver.cache.Msg;
import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.SystemMessage;
import jts.gameserver.scripts.Functions;

public class CPHPMPRegen extends Functions
{
	private static final int ADENA = 57;
	private static final long PRICE = 10;
	
	public void DoCPRegen()
	{
		Player player = getSelf();
		long cp = (long) Math.floor(player.getMaxCp() - player.getCurrentCp());
		long fullCost = cp * PRICE;
		if(fullCost <= 0)
		{
			player.sendPacket(Msg.NOTHING_HAPPENED);
			return;
		}
		if(getItemCount(player, ADENA) >= fullCost)
		{
			removeItem(player, ADENA, fullCost);
			player.sendPacket(new SystemMessage(SystemMessage.S1_HPS_HAVE_BEEN_RESTORED).addNumber(cp));
			player.setCurrentCp(player.getMaxCp());
		}
		else
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
	}

	
	public void DoHPRegen()
	{
		Player player = getSelf();
		long hp = (long) Math.floor(player.getMaxHp() - player.getCurrentHp());
		long fullCost = hp * PRICE;
		if(fullCost <= 0)
		{
			player.sendPacket(Msg.NOTHING_HAPPENED);
			return;
		}
		if(getItemCount(player, ADENA) >= fullCost)
		{
			removeItem(player, ADENA, fullCost);
			player.sendPacket(new SystemMessage(SystemMessage.S1_HPS_HAVE_BEEN_RESTORED).addNumber(hp));
			player.setCurrentHp(player.getMaxHp(), false);
		}
		else
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
	}

	public void DoMPRegen()
	{
		Player player = getSelf();
		long mp = (long) Math.floor(player.getMaxMp() - player.getCurrentMp());
		long fullCost = mp * PRICE;
		if(fullCost <= 0)
		{
			player.sendPacket(Msg.NOTHING_HAPPENED);
			return;
		}
		if(getItemCount(player, ADENA) >= fullCost)
		{
			removeItem(player, ADENA, fullCost);
			player.sendPacket(new SystemMessage(SystemMessage.S1_MPS_HAVE_BEEN_RESTORED).addNumber(mp));
			player.setCurrentMp(player.getMaxMp());
		}
		else
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
	}
}