package jts.gameserver.model.actor.listener;

import jts.commons.listener.Listener;
import jts.commons.listener.ListenerList;
import jts.gameserver.ai.CtrlEvent;
import jts.gameserver.ai.CtrlIntention;
import jts.gameserver.listener.actor.OnAttackHitListener;
import jts.gameserver.listener.actor.OnAttackListener;
import jts.gameserver.listener.actor.OnCurrentHpDamageListener;
import jts.gameserver.listener.actor.OnDeathListener;
import jts.gameserver.listener.actor.OnKillListener;
import jts.gameserver.listener.actor.OnMagicHitListener;
import jts.gameserver.listener.actor.OnMagicUseListener;
import jts.gameserver.listener.actor.ai.OnAiEventListener;
import jts.gameserver.listener.actor.ai.OnAiIntentionListener;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Skill;

public class CharListenerList extends ListenerList<Creature>
{
	final static ListenerList<Creature> global = new ListenerList<Creature>();

	protected final Creature actor;

	public CharListenerList(Creature actor)
	{
		this.actor = actor;
	}

	public Creature getActor()
	{
		return actor;
	}

	public final static boolean addGlobal(Listener<Creature> listener)
	{
		return global.add(listener);
	}

	public final static boolean removeGlobal(Listener<Creature> listener)
	{
		return global.remove(listener);
	}

	public void onAiIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnAiIntentionListener.class.isInstance(listener))
					((OnAiIntentionListener) listener).onAiIntention(getActor(), intention, arg0, arg1);
	}

	public void onAiEvent(CtrlEvent evt, Object[] args)
	{
		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnAiEventListener.class.isInstance(listener))
					((OnAiEventListener) listener).onAiEvent(getActor(), evt, args);
	}

	public void onAttack(Creature target)
	{
		if(!global.getListeners().isEmpty())
			for(Listener<Creature> listener : global.getListeners())
				if(OnAttackListener.class.isInstance(listener))
					((OnAttackListener) listener).onAttack(getActor(), target);

		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnAttackListener.class.isInstance(listener))
					((OnAttackListener) listener).onAttack(getActor(), target);
	}

	public void onAttackHit(Creature attacker)
	{
		if(!global.getListeners().isEmpty())
			for(Listener<Creature> listener : global.getListeners())
				if(OnAttackHitListener.class.isInstance(listener))
					((OnAttackHitListener) listener).onAttackHit(getActor(), attacker);

		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnAttackHitListener.class.isInstance(listener))
					((OnAttackHitListener) listener).onAttackHit(getActor(), attacker);
	}

	public void onMagicUse(Skill skill, Creature target, boolean alt)
	{
		if(!global.getListeners().isEmpty())
			for(Listener<Creature> listener : global.getListeners())
				if(OnMagicUseListener.class.isInstance(listener))
					((OnMagicUseListener) listener).onMagicUse(getActor(), skill, target, alt);

		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnMagicUseListener.class.isInstance(listener))
					((OnMagicUseListener) listener).onMagicUse(getActor(), skill, target, alt);
	}

	public void onMagicHit(Skill skill, Creature caster)
	{
		if(!global.getListeners().isEmpty())
			for(Listener<Creature> listener : global.getListeners())
				if(OnMagicHitListener.class.isInstance(listener))
					((OnMagicHitListener) listener).onMagicHit(getActor(), skill, caster);

		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnMagicHitListener.class.isInstance(listener))
					((OnMagicHitListener) listener).onMagicHit(getActor(), skill, caster);
	}

	public void onDeath(Creature killer)
	{
		if(!global.getListeners().isEmpty())
			for(Listener<Creature> listener : global.getListeners())
				if(OnDeathListener.class.isInstance(listener))
					((OnDeathListener) listener).onDeath(getActor(), killer);

		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnDeathListener.class.isInstance(listener))
					((OnDeathListener) listener).onDeath(getActor(), killer);
	}

	public void onKill(Creature victim)
	{
		if(!global.getListeners().isEmpty())
			for(Listener<Creature> listener : global.getListeners())
				if(OnKillListener.class.isInstance(listener) && !((OnKillListener) listener).ignorePetOrSummon())
					((OnKillListener) listener).onKill(getActor(), victim);

		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnKillListener.class.isInstance(listener) && !((OnKillListener) listener).ignorePetOrSummon())
					((OnKillListener) listener).onKill(getActor(), victim);
	}

	public void onKillIgnorePetOrSummon(Creature victim)
	{
		if(!global.getListeners().isEmpty())
			for(Listener<Creature> listener : global.getListeners())
				if(OnKillListener.class.isInstance(listener) && ((OnKillListener) listener).ignorePetOrSummon())
					((OnKillListener) listener).onKill(getActor(), victim);

		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnKillListener.class.isInstance(listener) && ((OnKillListener) listener).ignorePetOrSummon())
					((OnKillListener) listener).onKill(getActor(), victim);
	}

	public void onCurrentHpDamage(double damage, Creature attacker, Skill skill)
	{
		if(!global.getListeners().isEmpty())
			for(Listener<Creature> listener : global.getListeners())
				if(OnCurrentHpDamageListener.class.isInstance(listener))
					((OnCurrentHpDamageListener) listener).onCurrentHpDamage(getActor(), damage, attacker, skill);

		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnCurrentHpDamageListener.class.isInstance(listener))
					((OnCurrentHpDamageListener) listener).onCurrentHpDamage(getActor(), damage, attacker, skill);
	}
}