package jts.gameserver.listener.actor.player.impl;

import jts.commons.lang.reference.HardReference;
import jts.gameserver.listener.actor.player.OnAnswerListener;
import jts.gameserver.model.Player;
import jts.gameserver.model.instances.PetInstance;

public class ReviveAnswerListener implements OnAnswerListener
{
	private HardReference<Player> _playerRef;
	private double _power;
	private boolean _forPet;

	public ReviveAnswerListener(Player player, double power, boolean forPet)
	{
		_playerRef = player.getRef();
		_forPet = forPet;
		_power = power;
	}

	@Override
	public void sayYes()
	{
		Player player = _playerRef.get();
		if(player == null)
			return;
		if(!player.isDead() && !_forPet || _forPet && player.getPet() != null && !player.getPet().isDead())
			return;

		if(!_forPet)
			player.doRevive(_power);
		else if(player.getPet() != null)
			((PetInstance) player.getPet()).doRevive(_power);
	}

	@Override
	public void sayNo()	{}

	public double getPower()
	{
		return _power;
	}

	public boolean isForPet()
	{
		return _forPet;
	}
}