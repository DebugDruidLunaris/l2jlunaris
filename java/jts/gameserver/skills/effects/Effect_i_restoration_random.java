package jts.gameserver.skills.effects;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jts.commons.util.Rnd;
import jts.gameserver.model.Effect;
import jts.gameserver.model.Playable;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.stats.Env;
import jts.gameserver.utils.ItemFunctions;	
/**
 * @author : Prototype
 * @date : 17.10.15  21:19
*/
public class Effect_i_restoration_random extends Effect
{
	private final List<List<Item>> items;
	private final double[] chances;
	private int capsule_itemId;
	private long capsule_count;		
	private static final Pattern effectPattern = Pattern.compile("\\{(\\S+)\\}");
	private static final Pattern groupPattern = Pattern.compile("\\{\\[([\\d:;]+?)\\]([\\d.e-]+)\\}");
	public Effect_i_restoration_random(Env env, EffectTemplate template)
	{
		super(env, template);
		String[] capsule = getTemplate().getParam().getString("action_capsule").split(";");
		capsule_itemId = Integer.parseInt(capsule[0]);
		capsule_count = Long.parseLong(capsule[1]);		
	    String[] groups = getTemplate().getParam().getString("extract").split(";");
		items = new ArrayList<List<Item>>(groups.length);
		chances = new double[groups.length];
		double prevChance = 0;
		final Matcher e = effectPattern.matcher(getTemplate().getParam().getString("extract"));
		int i = 0;
		if (e.find()) 
		{	
			final String groupsE = e.group(1);
			final Matcher groupM = groupPattern.matcher(groupsE);           	
			while (groupM.find()) 
			{
				final String its = groupM.group(1);
				final List<Item> list = new ArrayList<>(its.split(";").length);
				for (final String item : its.split(";"))
				{
					final String id = item.split(":")[0];
					final String count = item.split(":")[1];
					final Item it = new Item();
					it.itemId = Integer.parseInt(id);
					it.count = Long.parseLong(count);
					list.add(it);
            	}
				final double chance = Double.parseDouble(groupM.group(2));
				items.add(i,list);
				chances[i] = prevChance + chance;
				prevChance = chances[i];
				i++;           		
			}
		}       
	}		
	@Override
	public void onStart() 
	{
		super.onStart();
		Playable playable = (Playable) getEffected();
		double chance = (double) Rnd.get(0, 1000000) / 10000;	
		double prevChance = 0.0D;
		int i;
		for(i = 0; i < chances.length; i++)
		{
			if(chance > prevChance && chance < chances[i])
				break;
		}
		if(playable !=null && capsule_itemId != -1 && capsule_count != -1)
		{
			ItemFunctions.removeItem(playable, capsule_itemId, capsule_count, true);
		}
		if (i < chances.length) 
		{
			List<Item> itemList = items.get(i);
			for (Item item : itemList) 
			{
				ItemFunctions.addItem(playable, item.itemId, item.count, true);
			}
		} 
		else 
		{
			getEffected().sendPacket(SystemMsg.THERE_WAS_NOTHING_FOUND_INSIDE);
		}
	}	
	@Override
	protected boolean onActionTime() 
	{
		return false;
	}	
	private final class Item 
	{
		public int itemId;
		public long count;
	}			
}

