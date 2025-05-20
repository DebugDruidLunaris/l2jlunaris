package jts.gameserver.data.xml.holder;


import gnu.trove.map.hash.TIntObjectHashMap;
import jts.commons.data.xml.AbstractHolder;
import jts.gameserver.model.PetData;
import jts.gameserver.templates.StatsSet;

/**
 * @author	ALF
 * @date	02.07.2012
 */
public class PetDataHolder extends AbstractHolder
{
	private static PetDataHolder _instance = new PetDataHolder();
	private TIntObjectHashMap<PetData> _pets = new TIntObjectHashMap<PetData>();
	public static PetDataHolder getInstance()
	{
		return _instance;
	}	

	public void addPetData(StatsSet set)
	{
		PetData petData = new PetData();
		int id = set.getInteger("id");
		
		petData.setID(id);
		petData.setLevel(set.getInteger("level"));
		petData.setExp(set.getLong("exp"));
		petData.setHP(set.getInteger("hp"));
		petData.setMP(set.getInteger("mp"));
		petData.setPAtk(set.getInteger("patk"));
		petData.setPDef(set.getInteger("pdef"));
		petData.setMAtk(set.getInteger("matk"));
		petData.setMDef(set.getInteger("mdef"));
		petData.setAccuracy(37 + petData.getLevel());
		petData.setEvasion(33 + petData.getLevel());
		petData.setCritical(40);
		petData.setSpeed(137);
		petData.setAtkSpeed(278);
		petData.setCastSpeed(333);
		petData.setFeedMax(set.getInteger("max_meal"));
		petData.setFeedBattle(set.getInteger("consume_meal_in_battle"));
		petData.setFeedNormal(set.getInteger("consume_meal_in_normal"));
		petData.setMaxLoad(set.getInteger("load"));
		petData.setHpRegen(set.getInteger("hpreg"));
		petData.setMpRegen(set.getInteger("mpreg"));
		_pets.put(petData.getID() * 100 + petData.getLevel(), petData);
	}
	
	public PetData getInfo(int petNpcId, int level)
	{
		PetData result = null;
		while (result == null && level < 100)
		{
			result = _pets.get(petNpcId * 100 + level);
			level++;
		}

		return result;
	}
	
	@Override
	public int size()
	{		
		return _pets.size();
	}

	@Override
	public void clear()
	{
		_pets.clear();
	}

}
