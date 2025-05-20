package ai.SkyshadowMeadow;

import java.util.List;

import jts.commons.threading.RunnableImpl;
import jts.commons.util.Rnd;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.ai.Fighter;
import jts.gameserver.model.Player;
import jts.gameserver.model.World;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.scripts.Functions;
import jts.gameserver.network.serverpackets.components.NpcString;
import jts.gameserver.tables.SkillTable;

public class SelMahumShef extends Fighter
{
	private long _wait_timeout = System.currentTimeMillis() + 30000;
	private boolean _firstTime = true;
	public static final NpcString[] _text = {NpcString.I_BROUGHT_THE_FOOD, NpcString.COME_AND_EAT};

	public SelMahumShef(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(actor != null)
		{
			if(_wait_timeout < System.currentTimeMillis())
			{
				List<Player> players = World.getAroundPlayers(actor, 100, 100);
				for(Player p : players)
					actor.doCast(SkillTable.getInstance().getInfo(6330, 1), p, true);
				_wait_timeout = (System.currentTimeMillis() + 30000);
			}

			for(NpcInstance npc : actor.getAroundNpc(150, 150))
			{
				if(npc.isMonster() && npc.getNpcId() == 18927)
				{
					if(_firstTime)
					{
						// Включаем паузу что бы не зафлудить чат.
						_firstTime = false;
						Functions.npcSay(actor, _text[Rnd.get(_text.length)]);
						ThreadPoolManager.getInstance().schedule(new NewText(), 20000); // Время паузы
					}
				}
			}

			return super.thinkActive();
		}
		return true;
	}

	private class NewText extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			NpcInstance actor = getActor();
			if(actor == null)
				return;

			// Выключаем паузу
			_firstTime = true;
		}
	}
}