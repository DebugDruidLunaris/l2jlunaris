package npc.model;

import jts.gameserver.model.Player;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.scripts.Functions;
import jts.gameserver.templates.npc.NpcTemplate;

@SuppressWarnings("serial")
public class LekonInstance extends NpcInstance
{
	private static final int ENERGY_STAR_STONE = 13277;
	private static final int AIRSHIP_SUMMON_LICENSE = 13559;

	public LekonInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.equals("get_license"))
		{
			if(player.getClan() == null || !player.isClanLeader() || player.getClan().getLevel() < 5)
			{
				showChatWindow(player, 2);
				return;
			}

			if(player.getClan().isHaveAirshipLicense() || Functions.getItemCount(player, AIRSHIP_SUMMON_LICENSE) > 0)
			{
				showChatWindow(player, 4);
				return;
			}

			if(Functions.removeItem(player, ENERGY_STAR_STONE, 10) != 10)
			{
				showChatWindow(player, 3);
				return;
			}

			Functions.addItem(player, AIRSHIP_SUMMON_LICENSE, 1);
		}
		else
			super.onBypassFeedback(player, command);
	}
}