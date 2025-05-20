package handler.voicecommands;

import jts.gameserver.handler.voicecommands.IVoicedCommandHandler;
import jts.gameserver.handler.voicecommands.VoicedCommandHandler;
import jts.gameserver.model.Player;
import jts.gameserver.scripts.Functions;
import jts.gameserver.scripts.ScriptFile;

public class AutoLootPremium extends Functions implements IVoicedCommandHandler, ScriptFile
{
	private String[] _commandList = new String[] { "paloot" };

	private static String _itemName = "Adena";
	private static int _premiumLootItemId = 57;
	private static int _premiumLootDayCount = 1000;

	@Override
	public void onLoad()
	{
		VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
	}

	@Override
	public void onReload() {}

	@Override
	public void onShutdown() {}

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String args)
	{
		if(command.equals("loot") && args != null && activeChar.getVar("AutoLoot") == null)
		{
			String[] param = args.split(" ");
			if(param.length == 2)
			{
				if(param[0].equalsIgnoreCase("add"))
				{
					String _dayS = param[1];
					if(!checkInteger(_dayS))
					{
						activeChar.sendMessage("" + activeChar.getName() + ", Escrever apenas números!");
						return false;
					}
					int _dayD = Integer.valueOf(_dayS);

					if(activeChar.getInventory().getCountOf(_premiumLootItemId) < _dayD * _premiumLootDayCount)
					{
						activeChar.sendMessage("" + activeChar.getName() + ", Itens não são suficientes para a aquisição de auto-loot");
						return false;
					}

					removeItem(activeChar, _premiumLootItemId, _dayD * _premiumLootDayCount);
					activeChar.setAutoLoot(true);
					activeChar.unsetVar("AutoLoot");
					activeChar.setVar("AutoLoot", "true", System.currentTimeMillis() + (86400000 * _dayD));
					activeChar.sendMessage("" + activeChar.getName() + ", Você comprou com sucesso prêmio sobre auto-Loot " + _dayD + " dias");
					return true;
				}
			}
			String append = "<center>Modo Prêmio auto-loot:<br>";
			append += "O custo para um dia auto-loot " + _premiumLootDayCount + " " + _itemName + "<br>";
			append += "Especifique quantos dias você deseja <br> comprar auto-loot:<br>";
			append += "<edit var=\"days\" width=150> <br>";
			append += "<button value=\"comprar\" action=\"bypass -h user_loot add $days \" width=150 height=15></center><br>";
			show(append, activeChar, null);
			activeChar.setAutoLoot(false);
			activeChar.unsetVar("AutoLoot");
		}
		else if(activeChar.getVar("AutoLoot") != null)
		{
			activeChar.sendMessage("" + activeChar.getName() + ", Você já usa o premium auto-loot!");
			return false;
		}
		return false;
	}

	public boolean checkInteger(String number)
	{
		try
		{
			int x = Integer.parseInt(number);
			number = Integer.toString(x);
			return true;
		}
		catch(NumberFormatException e)
		{
			e.printStackTrace();
		}
		return false;
	}
}