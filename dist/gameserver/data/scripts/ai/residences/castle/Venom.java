package ai.residences.castle;

import jts.gameserver.ai.Fighter;
import jts.gameserver.data.xml.holder.ResidenceHolder;
import jts.gameserver.model.Creature;
import jts.gameserver.model.entity.residence.Castle;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.network.serverpackets.components.NpcString;
import jts.gameserver.scripts.Functions;
import jts.gameserver.utils.NpcUtils;


//TODO нужен полный тест , от окончания реги на осаду он должен появлятся в подвале и тд
public class Venom extends Fighter
{
    boolean _teleported;
	public Venom(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	public void onEvtSpawn()
	{
		super.onEvtSpawn();
        _teleported = false;
		Functions.npcShout(getActor(), NpcString.WHO_DARES_TO_COVET_THE_THRONE_OF_OUR_CASTLE__LEAVE_IMMEDIATELY_OR_YOU_WILL_PAY_THE_PRICE_OF_YOUR_AUDACITY_WITH_YOUR_VERY_OWN_BLOOD);
	}
    @Override
    public boolean thinkActive()
    {
        NpcInstance actor = getActor();

        if ((actor == null) || actor.isDead()) {
            return true;
        }
        Castle castle = ResidenceHolder.getInstance().getResidence(Castle.class, 8);
        // Телепортируется в тронный зал если не был убит до осады.
        if (!_teleported && castle.getSiegeEvent().isInProgress()) {
            actor.teleToLocation(11493, -49153, -536);
            _teleported = true;
            return false;
        }
        return true;
    }
	@Override
	public void onEvtDead(Creature killer)
	{
		super.onEvtDead(killer);
        _teleported = false;
		Functions.npcShout(getActor(), NpcString.ITS_NOT_OVER_YET__IT_WONT_BE__OVER__LIKE_THIS__NEVER);
		NpcUtils.spawnSingle(29055, 12589, -49044, -3008, 120000);
	}
}