package npc.model;

import jts.gameserver.Config;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.instancemanager.games.HandysBlockCheckerManager;
import jts.gameserver.instancemanager.games.HandysBlockCheckerManager.ArenaParticipantsHolder;
import jts.gameserver.model.Player;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.network.serverpackets.ExCubeGameChangeTimeToStart;
import jts.gameserver.network.serverpackets.ExCubeGameRequestReady;
import jts.gameserver.network.serverpackets.ExCubeGameTeamList;
import jts.gameserver.network.serverpackets.SystemMessage;
import jts.gameserver.templates.npc.NpcTemplate;

@SuppressWarnings("serial")
public class HandysBlockCheckerInstance extends NpcInstance
{
	public HandysBlockCheckerInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		if(!Config.ALT_ENABLE_BLOCK_CHECKER_EVENT)
			return;
		HandysBlockCheckerManager.getInstance().startUpParticipantsQueue();
	}

	// Arena Managers
	private static final int A_MANAGER_1 = 32521;
	private static final int A_MANAGER_2 = 32522;
	private static final int A_MANAGER_3 = 32523;
	private static final int A_MANAGER_4 = 32524;

	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		if(player == null || !Config.ALT_ENABLE_BLOCK_CHECKER_EVENT)
			return;
		int npcId = getNpcId();

		int arena = -1;
		switch(npcId)
		{
			case A_MANAGER_1:
				arena = 0;
				break;
			case A_MANAGER_2:
				arena = 1;
				break;
			case A_MANAGER_3:
				arena = 2;
				break;
			case A_MANAGER_4:
				arena = 3;
				break;
		}

		if(arena != -1)
		{
			if(eventIsFull(arena))
			{
				player.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_REGISTER_BECAUSE_CAPACITY_HAS_BEEN_EXCEEDED));
				return;
			}
			if(HandysBlockCheckerManager.getInstance().arenaIsBeingUsed(arena))
			{
				player.sendPacket(new SystemMessage(SystemMessage.THE_MATCH_IS_BEING_PREPARED_PLEASE_TRY_AGAIN_LATER));
				return;
			}
			if(HandysBlockCheckerManager.getInstance().addPlayerToArena(player, arena))
			{
				ArenaParticipantsHolder holder = HandysBlockCheckerManager.getInstance().getHolder(arena);

				final ExCubeGameTeamList tl = new ExCubeGameTeamList(holder.getRedPlayers(), holder.getBluePlayers(), arena);

				player.sendPacket(tl);

				int countBlue = holder.getBlueTeamSize();
				int countRed = holder.getRedTeamSize();
				int minMembers = Config.ALT_MIN_BLOCK_CHECKER_TEAM_MEMBERS;

				if(countBlue >= minMembers && countRed >= minMembers)
				{
					holder.updateEvent();
					holder.broadCastPacketToTeam(new ExCubeGameRequestReady());
					holder.broadCastPacketToTeam(new ExCubeGameChangeTimeToStart(10));
					ThreadPoolManager.getInstance().schedule(holder.getEvent().new StartEvent(), 10100L);
				}
			}
		}
	}

	private boolean eventIsFull(int arena)
	{
		if(HandysBlockCheckerManager.getInstance().getHolder(arena).getAllPlayers().size() == 12)
			return true;
		return false;
	}
}