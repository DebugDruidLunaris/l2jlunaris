package jts.gameserver.model.instances;

import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.HennaEquipList;
import jts.gameserver.network.serverpackets.HennaUnequipList;
import jts.gameserver.templates.npc.NpcTemplate;

@SuppressWarnings("serial")
public class SymbolMakerInstance extends NpcInstance
{
	public SymbolMakerInstance(int objectID, NpcTemplate template)
	{
		super(objectID, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.equals("Draw"))
			player.sendPacket(new HennaEquipList(player));
		else if(command.equals("RemoveList"))
			player.sendPacket(new HennaUnequipList(player));
		else
			super.onBypassFeedback(player, command);
	}

	@Override
	public String getHtmlPath(int npcId, int val, Player player)
	{
		String pom;
		if(val == 0)
			pom = "SymbolMaker";
		else
			pom = "SymbolMaker-" + val;

		return "symbolmaker/" + pom + ".htm";
	}
}