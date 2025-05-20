package jts.gameserver.handler.voicecommands;

import java.util.HashMap;
import java.util.Map;

import jts.commons.data.xml.AbstractHolder;
import jts.gameserver.Config;
import jts.gameserver.handler.voicecommands.impl.Autocp;
import jts.gameserver.handler.voicecommands.impl.Away;
import jts.gameserver.handler.voicecommands.impl.BuffStoreVoiced;
import jts.gameserver.handler.voicecommands.impl.Cfg;
import jts.gameserver.handler.voicecommands.impl.Clicker;
import jts.gameserver.handler.voicecommands.impl.Debug;
import jts.gameserver.handler.voicecommands.impl.Hellbound;
import jts.gameserver.handler.voicecommands.impl.Help;
import jts.gameserver.handler.voicecommands.impl.Offline;
import jts.gameserver.handler.voicecommands.impl.PartyInfo;
import jts.gameserver.handler.voicecommands.impl.Password;
import jts.gameserver.handler.voicecommands.impl.Relocate;
import jts.gameserver.handler.voicecommands.impl.Repair;
import jts.gameserver.handler.voicecommands.impl.ReportBot;
import jts.gameserver.handler.voicecommands.impl.Security;
import jts.gameserver.handler.voicecommands.impl.ServerInfo;
import jts.gameserver.handler.voicecommands.impl.Wedding;
import jts.gameserver.handler.voicecommands.impl.WhoAmI;

public class VoicedCommandHandler extends AbstractHolder
{
	private static final VoicedCommandHandler _instance = new VoicedCommandHandler();

	public static VoicedCommandHandler getInstance()
	{
		return _instance;
	}

	private Map<String, IVoicedCommandHandler> _datatable = new HashMap<String, IVoicedCommandHandler>();

	private VoicedCommandHandler()
	{
		registerVoicedCommandHandler(new Help());
		registerVoicedCommandHandler(new Hellbound());
		registerVoicedCommandHandler(new Cfg());
		registerVoicedCommandHandler(new Offline());
		registerVoicedCommandHandler(new Repair());
		registerVoicedCommandHandler(new ServerInfo());
		registerVoicedCommandHandler(new Wedding());
		registerVoicedCommandHandler(new WhoAmI());
		registerVoicedCommandHandler(new Debug());
		registerVoicedCommandHandler(new Security());
		registerVoicedCommandHandler(new Clicker());
		registerVoicedCommandHandler(new Password());
		registerVoicedCommandHandler(new ReportBot());
		registerVoicedCommandHandler(new PartyInfo());
		registerVoicedCommandHandler(new BuffStoreVoiced());
		registerVoicedCommandHandler(new Away());
		if(Config.AUTOCP_SKILL)
		registerVoicedCommandHandler(new Autocp());
		registerVoicedCommandHandler(new Relocate());
	}

	public void registerVoicedCommandHandler(IVoicedCommandHandler handler)
	{
		String[] ids = handler.getVoicedCommandList();
		for(String element : ids)
			_datatable.put(element, handler);
	}

	public IVoicedCommandHandler getVoicedCommandHandler(String voicedCommand)
	{
		String command = voicedCommand;
		if(voicedCommand.indexOf(" ") != -1)
			command = voicedCommand.substring(0, voicedCommand.indexOf(" "));

		return _datatable.get(command);
	}

	@Override
	public int size()
	{
		return _datatable.size();
	}

	@Override
	public void clear()
	{
		_datatable.clear();
	}
}