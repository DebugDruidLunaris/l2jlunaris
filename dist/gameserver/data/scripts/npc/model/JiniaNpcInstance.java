package npc.model;

import quests._10286_ReunionWithSirra;
import instances.FreyaHard;
import instances.FreyaNormal;
import jts.gameserver.model.Player;
import jts.gameserver.model.entity.Reflection;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.templates.npc.NpcTemplate;
import jts.gameserver.utils.ItemFunctions;
import jts.gameserver.utils.ReflectionUtils;

@SuppressWarnings("serial")
public final class JiniaNpcInstance extends NpcInstance
{
	private static final int normalFreyaIzId = 139;
	private static final int extremeFreyaIzId = 144;

	public JiniaNpcInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.equalsIgnoreCase("request_normalfreya"))
		{
			Reflection r = player.getActiveReflection();
			if(r != null)
			{
				if(player.canReenterInstance(normalFreyaIzId))
					player.teleToLocation(r.getTeleportLoc(), r);
			}
			else if(player.canEnterInstance(normalFreyaIzId))
				ReflectionUtils.enterReflection(player, new FreyaNormal(), normalFreyaIzId);
		}
		else if(command.equalsIgnoreCase("request_extremefreya"))
		{
			Reflection r = player.getActiveReflection();
			if(r != null)
			{
				if(player.canReenterInstance(extremeFreyaIzId))
					player.teleToLocation(r.getTeleportLoc(), r);
			}
			else if(player.canEnterInstance(extremeFreyaIzId))
				ReflectionUtils.enterReflection(player, new FreyaHard(), extremeFreyaIzId);
		}
		else if(command.equalsIgnoreCase("request_stone"))
		{
			if(player.getInventory().getCountOf(15469) > 0 || player.getInventory().getCountOf(15470) > 0)
				showChatWindow(player, 4);
			else if(player.getQuestState(_10286_ReunionWithSirra.class) == null || !player.getQuestState(_10286_ReunionWithSirra.class).isCompleted())
				
			{
				ItemFunctions.addItem(player, 15470, 1, true);
				showChatWindow(player, 5);
			}
			else
			{
				ItemFunctions.addItem(player, 15469, 1, true);
				showChatWindow(player, 5);
			}
		}
		else
			super.onBypassFeedback(player, command);
	}
}