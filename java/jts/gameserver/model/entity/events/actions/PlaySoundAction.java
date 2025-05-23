package jts.gameserver.model.entity.events.actions;

import java.util.List;

import jts.gameserver.model.GameObject;
import jts.gameserver.model.Player;
import jts.gameserver.model.entity.events.EventAction;
import jts.gameserver.model.entity.events.GlobalEvent;
import jts.gameserver.network.serverpackets.PlaySound;

public class PlaySoundAction implements EventAction
{
	private int _range;
	private String _sound;
	private PlaySound.Type _type;

	public PlaySoundAction(int range, String s, PlaySound.Type type)
	{
		_range = range;
		_sound = s;
		_type = type;
	}

	@Override
	public void call(GlobalEvent event)
	{
		GameObject object = event.getCenterObject();
		PlaySound packet = null;
		if(object != null)
			packet = new PlaySound(_type, _sound, 1, object.getObjectId(), object.getLoc());
		else
			packet = new PlaySound(_type, _sound, 0, 0, 0, 0, 0);

		List<Player> players = event.broadcastPlayers(_range);
		for(Player player : players)
			if(player != null)
				player.sendPacket(packet);
	}
}