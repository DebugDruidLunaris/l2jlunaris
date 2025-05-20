package ai.SkyshadowMeadow;

import jts.commons.threading.RunnableImpl;
import jts.commons.util.Rnd;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.ai.Fighter;
import jts.gameserver.model.Creature;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.scripts.Functions;
import jts.gameserver.network.serverpackets.ChangeWaitType;
import jts.gameserver.network.serverpackets.SocialAction;
import jts.gameserver.network.serverpackets.components.NpcString;
import jts.gameserver.tables.SkillTable;
import jts.gameserver.utils.Location;

public class SelMahumSquadLeader extends Fighter
{
	private boolean _firstTime1 = true;
	private boolean _firstTime2 = true;
	private boolean _firstTime3 = true;
	private boolean _firstTime4 = true;
	private boolean _firstTime5 = true;
	private boolean statsIsChanged = false;
	public static final NpcString[] _text = { NpcString.COME_AND_EAT, NpcString.LOOKS_DELICIOUS, NpcString.LETS_GO_EAT };

	public SelMahumSquadLeader(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(actor == null)
			return true;

		if(_def_think)
		{
			doTask();
			return true;
		}

		if( !statsIsChanged)
		{
			switch(actor.getNpcState())
			{
				case 1:
				{
					actor.doCast(SkillTable.getInstance().getInfo(6332, 1), actor, true);
					statsIsChanged = true;
					break;
				}
				case 2:
				{
					actor.doCast(SkillTable.getInstance().getInfo(6331, 1), actor, true);
					statsIsChanged = true;
					break;
				}
			}
		}

		if( !_firstTime2)
		{
			actor.broadcastPacket(new SocialAction(getActor().getObjectId(), 2));
			actor.broadcastPacket(new ChangeWaitType(getActor(), 0));
			actor.setNpcState((byte) 1);
			_firstTime2 = true;
		}

		if( !_firstTime4)
		{
			actor.broadcastPacket(new SocialAction(getActor().getObjectId(), 2));
			actor.broadcastPacket(new ChangeWaitType(getActor(), 0));
			actor.setNpcState((byte) 2);
			_firstTime4 = true;
		}

		for(NpcInstance npc : getActor().getAroundNpc(600, 600))
		{
			Location loc = Location.findPointToStay(npc, 100, 200);
			if(npc != null && npc.getNpcId() == 18933)
			{
				if(_firstTime1)
				{
					_firstTime1 = false;
					actor.setRunning();
					addTaskMove(loc, true);
					if(_firstTime5)
					{
						_firstTime5 = false;
						Functions.npcSay(actor, _text[Rnd.get(_text.length)]);
					}
					if(_firstTime2)
					{
						_firstTime2 = false;
						ThreadPoolManager.getInstance().schedule(new Go(), Rnd.get(20, 30) * 1000);
					}
				}
			}
		}

		for(NpcInstance npc : getActor().getAroundNpc(600, 600))
		{
			Location loc = Location.findPointToStay(npc, 100, 200);
			if(npc != null && npc.getNpcId() == 18927 && npc.getNpcState() == 1)
			{
				if(Rnd.chance(30))
				{
					if(_firstTime3)
					{
						_firstTime3 = false;
						actor.setRunning();
						addTaskMove(loc, true);
						if(_firstTime4)
						{
							_firstTime4 = false;
							ThreadPoolManager.getInstance().schedule(new Go(), Rnd.get(20, 30) * 1000);
						}
					}
				}
				else if(Rnd.chance(20))
				{
					actor.setNpcState((byte) 2);
					ThreadPoolManager.getInstance().schedule(new Stop(), Rnd.get(20, 30) * 1000);
				}
			}
		}

		return true;
	}

	private class Go extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			NpcInstance actor = getActor();
			Location loc = Location.findPointToStay(actor, 100, 200);

			actor.setNpcState((byte) 3);
			actor.setRunning();
			addTaskMove(loc, true);
			_firstTime1 = true;
			_firstTime3 = true;
		}
	}

	private class Stop extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			NpcInstance actor = getActor();
			actor.setNpcState((byte) 3);
		}
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		_firstTime1 = true;
		_firstTime2 = true;
		_firstTime3 = true;
		_firstTime4 = true;
		_firstTime5 = true;
		super.onEvtDead(killer);
	}
}