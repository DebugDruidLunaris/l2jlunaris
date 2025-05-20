package ai.custom;

import java.util.List;

import jts.commons.util.Rnd;
import jts.gameserver.ai.Mystic;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.entity.Reflection;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.network.serverpackets.MagicSkillUse;
import jts.gameserver.scripts.Functions;

public class SSQAnakim extends Mystic
{
	private static final String PLAYER_NAME = "%playerName%";

	private static final String[] chat = 
	{
		"Для вечности Эйнхазад !!!",
		"Уважаемое Шилен потомство! Вы не способны противостоять нам!",
		"Я покажу вам реальную власть Эйнхазад!",
		"Уважаемый военный силы Света! Перейди и уничтож потомство Шилен !!!" 
	};

	private static final String[] pms =
	{
		"Ослабление моей власти .. Спешите и включите уплотнительного устройства !!!",
		"Все 4 уплотнительные устройства должен быть включен !!!",
		"Нападение Лилит становится сильнее! Идите вперед и поверните его назад!",
		PLAYER_NAME + ", Удерживайтеть. Мы почти закончили!" 
	};

	private long _lastChatTime = 0;
	private long _lastPMTime = 0;
	private long _lastSkillTime = 0;

	public SSQAnakim(NpcInstance actor)
	{
		super(actor);
		actor.setHasChatWindow(false);
	}

	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();
	}

	@Override
	protected boolean thinkActive()
	{
		if(_lastChatTime < System.currentTimeMillis())
		{
			Functions.npcSay(getActor(), chat[Rnd.get(chat.length)]);
			_lastChatTime = System.currentTimeMillis() + 12 * 1000;
		}
		if(_lastPMTime < System.currentTimeMillis())
		{
			Player player = getPlayer();
			if(player != null)
			{
				String text = pms[Rnd.get(pms.length)];
				if(text.contains(PLAYER_NAME))
					text = text.replace(PLAYER_NAME, player.getName());
				Functions.npcSayToPlayer(getActor(), player, text);
			}
			_lastPMTime = System.currentTimeMillis() + 20 * 1000;
		}
		if(_lastSkillTime < System.currentTimeMillis())
		{
			if(getLilith() != null)
				getActor().broadcastPacket(new MagicSkillUse(getActor(), getLilith(), 6191, 1, 5000, 10));
			_lastSkillTime = System.currentTimeMillis() + 6500;
		}
		return true;
	}

	private NpcInstance getLilith()
	{
		List<NpcInstance> around = getActor().getAroundNpc(1000, 300);
		if(around != null && !around.isEmpty())
			for(NpcInstance npc : around)
				if(npc.getNpcId() == 32715)
					return npc;
		return null;
	}

	private Player getPlayer()
	{
		Reflection reflection = getActor().getReflection();
		if(reflection == null)
			return null;
		List<Player> pl = reflection.getPlayers();
		if(pl.isEmpty())
			return null;
		return pl.get(0);
	}

	@Override
	protected boolean randomWalk()
	{
		return false;
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage) {}

	@Override
	protected void onEvtAggression(Creature attacker, int aggro) {}
}