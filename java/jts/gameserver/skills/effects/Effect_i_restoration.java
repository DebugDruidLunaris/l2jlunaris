package jts.gameserver.skills.effects;

import jts.gameserver.model.Effect;
import jts.gameserver.model.Playable;
import jts.gameserver.stats.Env;
import jts.gameserver.utils.ItemFunctions;

/**
 * @author : Prototype
 * @date : 17.10.15  21:15
 */
public class Effect_i_restoration extends Effect 
{
	private int itemId;
	private long count;
	private int capsule_itemId;
	private long capsule_count;
	public Effect_i_restoration(Env env, EffectTemplate template) 
	{
		super(env, template);	
		String[] capsule = getTemplate().getParam().getString("action_capsule").split(";");
		capsule_itemId = Integer.parseInt(capsule[0]);
		capsule_count = Long.parseLong(capsule[1]);
		String[] item = getTemplate().getParam().getString("extract").split(";");
		itemId = Integer.parseInt(item[0]);
        count = Long.parseLong(item[1]);
	}		
	@Override
	public void onStart() 
	{
		super.onStart();
		Playable playable = (Playable) getEffected();
		if(playable != null && capsule_itemId != -1 && capsule_count != -1)
		{
			ItemFunctions.removeItem(playable, capsule_itemId, capsule_count, true);
		}
		if(playable!=null && itemId > 0)
		{
			ItemFunctions.addItem(playable, itemId, count, true);
		}
	}
    @Override
    protected boolean onActionTime() 
    {
        return false;
    } 	    
}