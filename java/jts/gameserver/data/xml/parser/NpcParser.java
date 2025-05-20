package jts.gameserver.data.xml.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jts.commons.data.xml.AbstractDirParser;
import jts.gameserver.Config;
import jts.gameserver.data.xml.holder.NpcHolder;
import jts.gameserver.model.Skill;
import jts.gameserver.model.TeleportLocation;
import jts.gameserver.model.base.ClassId;
import jts.gameserver.model.base.Element;
import jts.gameserver.model.reward.RewardData;
import jts.gameserver.model.reward.RewardGroup;
import jts.gameserver.model.reward.RewardList;
import jts.gameserver.model.reward.RewardType;
import jts.gameserver.tables.SkillTable;
import jts.gameserver.templates.StatsSet;
import jts.gameserver.templates.npc.AbsorbInfo;
import jts.gameserver.templates.npc.Faction;
import jts.gameserver.templates.npc.MinionData;
import jts.gameserver.templates.npc.NpcTemplate;
import jts.gameserver.utils.Location;

public final class NpcParser extends AbstractDirParser<NpcHolder>
{
	private static final NpcParser _instance = new NpcParser();

	public static NpcParser getInstance()
	{
		return _instance;
	}

	private NpcParser()
	{
		super(NpcHolder.getInstance());
	}

	@Override
	public File getXMLDir()
	{
		return new File(Config.DATAPACK_ROOT, "data/xml/npc/");
	}

	@Override
	public boolean isIgnored(File f)
	{
		return false;
	}

	@Override
	public String getDTDFileName()
	{
		return "npc.dtd";
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void readData(org.dom4j.Element rootElement) throws Exception
	{
		for(Iterator npcIterator = rootElement.elementIterator(); npcIterator.hasNext();)
		{
			org.dom4j.Element npcElement = (org.dom4j.Element) npcIterator.next();
			int npcId = Integer.parseInt(npcElement.attributeValue("id"));
			int templateId = npcElement.attributeValue("template_id") == null ? 0 : Integer.parseInt(npcElement.attributeValue("id"));
			String name = npcElement.attributeValue("name");
			String title = npcElement.attributeValue("title");

			StatsSet set = new StatsSet();
			set.set("npcId", npcId);
			set.set("displayId", templateId);
			set.set("name", name);
			set.set("title", title);
			set.set("baseCpReg", 0);
			set.set("baseCpMax", 0);

			for(Iterator firstIterator = npcElement.elementIterator(); firstIterator.hasNext();)
			{
				org.dom4j.Element firstElement = (org.dom4j.Element) firstIterator.next();
				if(firstElement.getName().equalsIgnoreCase("set"))
					set.set(firstElement.attributeValue("name"), firstElement.attributeValue("value"));
				else if(firstElement.getName().equalsIgnoreCase("equip"))
					for(Iterator eIterator = firstElement.elementIterator(); eIterator.hasNext();)
					{
						org.dom4j.Element eElement = (org.dom4j.Element) eIterator.next();
						set.set(eElement.getName(), eElement.attributeValue("item_id"));
					}
				else if(firstElement.getName().equalsIgnoreCase("ai_params"))
				{
					StatsSet ai = new StatsSet();
					for(Iterator eIterator = firstElement.elementIterator(); eIterator.hasNext();)
					{
						org.dom4j.Element eElement = (org.dom4j.Element) eIterator.next();
						ai.set(eElement.attributeValue("name"), eElement.attributeValue("value"));
					}
					set.set("aiParams", ai);
				}
				else if(firstElement.getName().equalsIgnoreCase("attributes"))
				{
					int[] attributeAttack = new int[6];
					int[] attributeDefence = new int[6];
					for(Iterator eIterator = firstElement.elementIterator(); eIterator.hasNext();)
					{
						org.dom4j.Element eElement = (org.dom4j.Element) eIterator.next();
						Element element;
						if(eElement.getName().equalsIgnoreCase("defence"))
						{
							element = Element.getElementByName(eElement.attributeValue("attribute"));
							attributeDefence[element.getId()] = Integer.parseInt(eElement.attributeValue("value"));
						}
						else if(eElement.getName().equalsIgnoreCase("attack"))
						{
							element = Element.getElementByName(eElement.attributeValue("attribute"));
							attributeAttack[element.getId()] = Integer.parseInt(eElement.attributeValue("value"));
						}
					}

					set.set("baseAttributeAttack", attributeAttack);
					set.set("baseAttributeDefence", attributeDefence);
				}
			}

			NpcTemplate template = new NpcTemplate(set);

			for(Iterator secondIterator = npcElement.elementIterator(); secondIterator.hasNext();)
			{
				org.dom4j.Element secondElement = (org.dom4j.Element) secondIterator.next();
				String nodeName = secondElement.getName();
				if(nodeName.equalsIgnoreCase("faction"))
				{
					String factionId = secondElement.attributeValue("name");
					Faction faction = new Faction(factionId);
					int factionRange = Integer.parseInt(secondElement.attributeValue("range"));
					faction.setRange(factionRange);
					for(Iterator nextIterator = secondElement.elementIterator(); nextIterator.hasNext();)
					{
						final org.dom4j.Element nextElement = (org.dom4j.Element) nextIterator.next();
						int ignoreId = Integer.parseInt(nextElement.attributeValue("npc_id"));
						faction.addIgnoreNpcId(ignoreId);
					}
					template.setFaction(faction);
				}
				else if(nodeName.equalsIgnoreCase("rewardlist"))
				{
					RewardType type = RewardType.valueOf(secondElement.attributeValue("type"));
					boolean autoLoot = secondElement.attributeValue("auto_loot") != null && Boolean.parseBoolean(secondElement.attributeValue("auto_loot"));
					RewardList list = new RewardList(type, autoLoot);

					for(Iterator nextIterator = secondElement.elementIterator(); nextIterator.hasNext();)
					{
						final org.dom4j.Element nextElement = (org.dom4j.Element) nextIterator.next();
						final String nextName = nextElement.getName();
						if(nextName.equalsIgnoreCase("group"))
						{
							double enterChance = nextElement.attributeValue("chance") == null ? RewardList.MAX_CHANCE : Double.parseDouble(nextElement.attributeValue("chance")) * 10000;
							RewardGroup group = (type == RewardType.SWEEP || type == RewardType.NOT_RATED_NOT_GROUPED) ? null : new RewardGroup(enterChance * Config.RATE_CHANCE_GROUP_DROP_ITEMS);
							for(Iterator<org.dom4j.Element> rewardIterator = nextElement.elementIterator(); rewardIterator.hasNext();)
							{
								org.dom4j.Element rewardElement = rewardIterator.next();
								RewardData data = parseReward(rewardElement,1);
								if(type == RewardType.SWEEP || type == RewardType.NOT_RATED_NOT_GROUPED)
									warn("Can't load rewardlist from group: " + npcId + "; type: " + type);
								else
									group.addData(data);
							}

							if(group != null)
								list.add(group);
						}
						else if(nextName.equalsIgnoreCase("reward"))
						{
							if(type != RewardType.SWEEP && type != RewardType.NOT_RATED_NOT_GROUPED)
							{
								warn("Reward can't be without group(and not grouped): " + npcId + "; type: " + type);
								continue;
							}

							RewardData data;
							if (type == RewardType.SWEEP)
								data = parseReward(nextElement, 2);
							else
								data = parseReward(nextElement, 3);
							RewardGroup g = new RewardGroup(RewardList.MAX_CHANCE);
							g.addData(data);
							list.add(g);
						}
					}

					if(type == RewardType.RATED_GROUPED || type == RewardType.NOT_RATED_GROUPED)
						if(!list.validate())
							warn("Problems with rewardlist for npc: " + npcId + "; type: " + type);

					template.putRewardList(type, list);
				}
				else if(nodeName.equalsIgnoreCase("skills"))
					for(Iterator nextIterator = secondElement.elementIterator(); nextIterator.hasNext();)
					{
						org.dom4j.Element nextElement = (org.dom4j.Element) nextIterator.next();
						int id = Integer.parseInt(nextElement.attributeValue("id"));
						int level = Integer.parseInt(nextElement.attributeValue("level"));

						// Для определения расы используется скилл 4416
						if(id == 4416)
							template.setRace(level);

						Skill skill = SkillTable.getInstance().getInfo(id, level);

						//TODO
						//if(skill == null || skill.getSkillType() == L2Skill.SkillType.NOTDONE)
						//	unimpl.add(Integer.valueOf(skillId));
						if(skill == null)
							continue;

						template.addSkill(skill);
					}
				else if(nodeName.equalsIgnoreCase("minions"))
					for(Iterator nextIterator = secondElement.elementIterator(); nextIterator.hasNext();)
					{
						org.dom4j.Element nextElement = (org.dom4j.Element) nextIterator.next();
						int id = Integer.parseInt(nextElement.attributeValue("npc_id"));
						int count = Integer.parseInt(nextElement.attributeValue("count"));

						template.addMinion(new MinionData(id, count));
					}
				else if(nodeName.equalsIgnoreCase("teach_classes"))
					for(Iterator nextIterator = secondElement.elementIterator(); nextIterator.hasNext();)
					{
						org.dom4j.Element nextElement = (org.dom4j.Element) nextIterator.next();

						int id = Integer.parseInt(nextElement.attributeValue("id"));

						template.addTeachInfo(ClassId.VALUES[id]);
					}
				else if(nodeName.equalsIgnoreCase("absorblist"))
					for(Iterator nextIterator = secondElement.elementIterator(); nextIterator.hasNext();)
					{
						org.dom4j.Element nextElement = (org.dom4j.Element) nextIterator.next();

						int chance = Integer.parseInt(nextElement.attributeValue("chance"));
						int cursedChance = nextElement.attributeValue("cursed_chance") == null ? 0 : Integer.parseInt(nextElement.attributeValue("cursed_chance"));
						int minLevel = Integer.parseInt(nextElement.attributeValue("min_level"));
						int maxLevel = Integer.parseInt(nextElement.attributeValue("max_level"));
						boolean skill = nextElement.attributeValue("skill") != null && Boolean.parseBoolean(nextElement.attributeValue("skill"));
						AbsorbInfo.AbsorbType absorbType = AbsorbInfo.AbsorbType.valueOf(nextElement.attributeValue("type"));

						template.addAbsorbInfo(new AbsorbInfo(skill, absorbType, chance, cursedChance, minLevel, maxLevel));
					}
				else if(nodeName.equalsIgnoreCase("teleportlist"))
					for(Iterator sublistIterator = secondElement.elementIterator(); sublistIterator.hasNext();)
					{
						org.dom4j.Element subListElement = (org.dom4j.Element) sublistIterator.next();
						int id = Integer.parseInt(subListElement.attributeValue("id"));
						List<TeleportLocation> list = new ArrayList<TeleportLocation>();
						for(Iterator targetIterator = subListElement.elementIterator(); targetIterator.hasNext();)
						{
							org.dom4j.Element targetElement = (org.dom4j.Element) targetIterator.next();
							int itemId = Integer.parseInt(targetElement.attributeValue("item_id", "57"));
							long price = Integer.parseInt(targetElement.attributeValue("price"));
							int npcStringId = Integer.parseInt(targetElement.attributeValue("name"));
							String RuName = targetElement.attributeValue("RuName");
							String EnName = targetElement.attributeValue("EnName");
							int castleId = Integer.parseInt(targetElement.attributeValue("castle_id", "0"));
							TeleportLocation loc = new TeleportLocation(itemId, price, npcStringId, RuName, EnName, castleId);
							loc.set(Location.parseLoc(targetElement.attributeValue("loc")));
							list.add(loc);
						}
						template.addTeleportList(id, list.toArray(new TeleportLocation[list.size()]));
					}
			}

			getHolder().addTemplate(template);
		}
	}

	private RewardData parseReward(org.dom4j.Element rewardElement, int id)
	{
		int itemId = Integer.parseInt(rewardElement.attributeValue("item_id"));
		int min = Integer.parseInt(rewardElement.attributeValue("min"));
		int max = Integer.parseInt(rewardElement.attributeValue("max"));
		// переводим в системный вид
		double chance = Double.parseDouble(rewardElement.attributeValue("chance")) * 10000;
		double chance_dop = chance * Config.RATE_CHANCE_DROP_ITEMS;
		double chance_sp = chance * Config.RATE_CHANCE_SPOIL;
		double chance_weapon = chance * Config.RATE_CHANCE_DROP_WEAPON_ARMOR_ACCESSORY;
		double chance_weapon_sp = chance * Config.RATE_CHANCE_SPOIL_WEAPON_ARMOR_ACCESSORY;
		if (chance_dop > 1000000)
			chance_dop = 1000000;
		if (chance_sp > 1000000)
			chance_sp = 1000000;
		if (chance_weapon > 1000000)
			chance_weapon = 1000000;
		if (chance_weapon_sp > 1000000)
			chance_weapon_sp = 1000000;

		RewardData data = new RewardData(itemId);
		if (id == 1)
		{
			if (data.getItem().isCommonItem())
				data.setChance(chance * Config.RATE_DROP_COMMON_ITEMS);
			else if (data.getItem().isWeapon() || data.getItem().isArmor() || data.getItem().isAccessory())
				data.setChance(chance_weapon);
			else
				data.setChance(chance_dop);
		}
		else if (id == 2)
		{
			if (data.getItem().isWeapon() || data.getItem().isArmor() || data.getItem().isAccessory())
				data.setChance(chance_weapon_sp);
			else
				data.setChance(chance_sp);
		}
		else if (id == 3)
			data.setChance(chance);

		data.setMinDrop(min);
		data.setMaxDrop(max);

		return data;
	}
}
