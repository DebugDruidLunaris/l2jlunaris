package jts.gameserver.network.clientpackets;

import jts.gameserver.cache.Msg;
import jts.gameserver.model.Player;
import jts.gameserver.model.instances.PetInstance;
import jts.gameserver.utils.Log_New;

public class RequestChangePetName extends L2GameClientPacket
{
	private String _name;

	@Override
	protected void readImpl()
	{
		_name = readS();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		PetInstance pet = activeChar.getPet() != null && activeChar.getPet().isPet() ? (PetInstance) activeChar.getPet() : null;
		if(pet == null)
			return;

		if(pet.isDefaultName())
		{
			if(_name.length() < 1 || _name.length() > 8)
			{
				sendPacket(Msg.YOUR_PETS_NAME_CAN_BE_UP_TO_8_CHARACTERS);
				return;
			}
			pet.setName(_name);
			pet.broadcastCharInfo();
			pet.updateControlItem();
			Log_New.LogEvent(activeChar.getName(), "ChangeName", "NickChangeForPet", new String[] { "changed pet name to: " + this._name + "" });
		}
	}
}