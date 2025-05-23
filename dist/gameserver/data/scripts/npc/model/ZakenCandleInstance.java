package npc.model;

import jts.gameserver.model.Player;
import jts.gameserver.model.entity.Reflection;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.templates.npc.NpcTemplate;

@SuppressWarnings("serial")
public final class ZakenCandleInstance extends NpcInstance
{
	private static final int OHS_Weapon = 15280; // spark
	private static final int THS_Weapon = 15281; // red
	private static final int BOW_Weapon = 15302; // blue
	private static final int Anchor = 32468;
	private boolean used = false;

	public ZakenCandleInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		setRHandId(OHS_Weapon);
		_hasRandomAnimation = false;
	}

	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		Reflection r = getReflection();
		if(r.isDefault() || used)
			return;

		for(NpcInstance npc : getAroundNpc(1000, 100))
			if(npc.getNpcId() == Anchor)
			{
				setRHandId(BOW_Weapon);
				broadcastCharInfo();
				used = true;
				return;
			}

		setRHandId(THS_Weapon);
		broadcastCharInfo();
		used = true;
	}

	@Override
	public void onBypassFeedback(Player player, String command) {}
}