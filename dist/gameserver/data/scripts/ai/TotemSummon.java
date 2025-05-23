package ai;

import jts.commons.threading.RunnableImpl;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.ai.DefaultAI;
import jts.gameserver.model.Creature;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.tables.SkillTable;

public class TotemSummon extends DefaultAI
{
	private static final int TotemofBody = 143;
	private static final int TotemofSpirit = 144;
	private static final int TotemofBravery = 145;
	private static final int TotemofFortitude = 146;

	private static final int TotemofBodyBuff = 23308;
	private static final int TotemofSpiritBuff = 23309;
	private static final int TotemofBraveryBuff = 23310;
	private static final int TotemofFortitudeBuff = 23311;
	private long _timer = 0;

	public TotemSummon(NpcInstance actor)
	{
		super(actor);
		actor.setHasChatWindow(false);
		actor.startImmobilized();
	}

	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();
		ThreadPoolManager.getInstance().schedule(new RunnableImpl(){
			@Override
			public void runImpl() throws Exception
			{
				if(getActor() != null)
					getActor().deleteMe();
			}
		}, 30 * 60 * 1000L);
	}

	@Override
	protected boolean thinkActive()
	{
		if(_timer < System.currentTimeMillis())
		{
			_timer = System.currentTimeMillis() + 15000L;
			for(Creature c : getActor().getAroundCharacters(450, 200))
				if(c.isPlayable() && !c.isDead())
					c.altOnMagicUseTimer(c, SkillTable.getInstance().getInfo(getBuffId(getActor().getNpcId()), 1));
		}

		return true;
	}

	private int getBuffId(int npcId)
	{
		int buffId = 0;
		switch(npcId)
		{
			case TotemofBody:
				buffId = TotemofBodyBuff;
				break;
			case TotemofSpirit:
				buffId = TotemofSpiritBuff;
				break;
			case TotemofBravery:
				buffId = TotemofBraveryBuff;
				break;
			case TotemofFortitude:
				buffId = TotemofFortitudeBuff;
				break;
			default:
				break;
		}
		return buffId;
	}
}