package actions;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import jts.gameserver.Config;
import jts.gameserver.data.htm.HtmCache;
import jts.gameserver.handler.admincommands.impl.AdminEditChar;
import jts.gameserver.model.AggroList.HateComparator;
import jts.gameserver.model.AggroList.HateInfo;
import jts.gameserver.model.Effect;
import jts.gameserver.model.GameObject;
import jts.gameserver.model.GameObjectsStorage;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.model.base.Element;
import jts.gameserver.model.entity.events.GlobalEvent;
import jts.gameserver.model.instances.DoorInstance;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.instances.PetInstance;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestEventType;
import jts.gameserver.network.serverpackets.components.NpcString;
import jts.gameserver.scripts.Functions;
import jts.gameserver.stats.Stats;
import jts.gameserver.utils.HtmlUtils;
import jts.gameserver.utils.PositionUtils;
import jts.gameserver.utils.Util;

import org.apache.commons.lang3.StringUtils;

public class OnActionShift extends Functions
{
	public boolean OnActionShift_NpcInstance(Player player, GameObject object)
	{
		if(player == null || object == null)
			return false;
		if(!Config.ALT_ALLOW_NPC_SHIFTCLICK && !player.isGM())
		{
			if(Config.ALT_GAME_SHOW_DROPLIST && object.isNpc())
			{
				NpcInstance npc = (NpcInstance) object;
				if(npc.isDead())
					return false;
				droplist(player, npc, 0, 1);
			}
			return false;
		}
		if(object.isNpc())
		{
			NpcInstance npc = (NpcInstance) object;

			// Для мертвых мобов не показываем табличку, иначе спойлеры плачут
			if(npc.isDead())
				return false;
			String dialog;

			if(Config.ALT_FULL_NPC_STATS_PAGE)
			{
				dialog = HtmCache.getInstance().getNotNull("scripts/actions/player.L2NpcInstance.onActionShift.full.htm", player);
				dialog = dialog.replaceFirst("%class%", String.valueOf(npc.getClass().getSimpleName().replaceFirst("L2", "").replaceFirst("Instance", "")));
				dialog = dialog.replaceFirst("%id%", String.valueOf(npc.getNpcId()));
				dialog = dialog.replaceFirst("%respawn%", String.valueOf(npc.getSpawn() != null ? Util.formatTime(npc.getSpawn().getRespawnDelay()) : "0"));
				dialog = dialog.replaceFirst("%walkSpeed%", String.valueOf(npc.getWalkSpeed()));
				dialog = dialog.replaceFirst("%evs%", String.valueOf(npc.getEvasionRate(null)));
				dialog = dialog.replaceFirst("%acc%", String.valueOf(npc.getAccuracy()));
				dialog = dialog.replaceFirst("%crt%", String.valueOf(npc.getCriticalHit(null, null)));
				dialog = dialog.replaceFirst("%aspd%", String.valueOf(npc.getPAtkSpd()));
				dialog = dialog.replaceFirst("%cspd%", String.valueOf(npc.getMAtkSpd()));
				dialog = dialog.replaceFirst("%currentMP%", String.valueOf(npc.getCurrentMp()));
				dialog = dialog.replaceFirst("%currentHP%", String.valueOf(npc.getCurrentHp()));
				dialog = dialog.replaceFirst("%loc%", "");
				dialog = dialog.replaceFirst("%dist%", String.valueOf((int) npc.getDistance3D(player)));
				dialog = dialog.replaceFirst("%killed%", String.valueOf(0));//TODO [G1ta0] убрать
				dialog = dialog.replaceFirst("%spReward%", String.valueOf(npc.getSpReward()));
				dialog = dialog.replaceFirst("%xyz%", npc.getLoc().x + " " + npc.getLoc().y + " " + npc.getLoc().z);
				dialog = dialog.replaceFirst("%ai_type%", npc.getAI().getClass().getSimpleName());
				dialog = dialog.replaceFirst("%direction%", PositionUtils.getDirectionTo(npc, player).toString().toLowerCase());

				StringBuilder b = new StringBuilder("");
				for(GlobalEvent e : npc.getEvents())
					b.append(e.toString()).append(";");
				dialog = dialog.replaceFirst("%event%", b.toString());
			}
			else
				dialog = HtmCache.getInstance().getNotNull("scripts/actions/player.L2NpcInstance.onActionShift.htm", player);

			dialog = dialog.replaceFirst("%name%", nameNpc(npc));
			dialog = dialog.replaceFirst("%id%", String.valueOf(npc.getNpcId()));
			dialog = dialog.replaceFirst("%level%", String.valueOf(npc.getLevel()));
			dialog = dialog.replaceFirst("%respawn%", String.valueOf(npc.getSpawn() != null ? Util.formatTime(npc.getSpawn().getRespawnDelay()) : "0"));
			dialog = dialog.replaceFirst("%factionId%", String.valueOf(npc.getFaction()));
			dialog = dialog.replaceFirst("%aggro%", String.valueOf(npc.getAggroRange()));
			dialog = dialog.replaceFirst("%maxHp%", String.valueOf(npc.getMaxHp()));
			dialog = dialog.replaceFirst("%maxMp%", String.valueOf(npc.getMaxMp()));
			dialog = dialog.replaceFirst("%pDef%", String.valueOf(npc.getPDef(null)));
			dialog = dialog.replaceFirst("%mDef%", String.valueOf(npc.getMDef(null, null)));
			dialog = dialog.replaceFirst("%pAtk%", String.valueOf(npc.getPAtk(null)));
			dialog = dialog.replaceFirst("%mAtk%", String.valueOf(npc.getMAtk(null, null)));
			dialog = dialog.replaceFirst("%expReward%", String.valueOf(npc.getExpReward()));
			dialog = dialog.replaceFirst("%spReward%", String.valueOf(npc.getSpReward()));
			dialog = dialog.replaceFirst("%runSpeed%", String.valueOf(npc.getRunSpeed()));

			// Дополнительная инфа для ГМов
			if(player.isGM())
				dialog = dialog.replaceFirst("%AI%", String.valueOf(npc.getAI()) + ",<br1>active: " + npc.getAI().isActive() + ",<br1>intention: " + npc.getAI().getIntention());
			else
				dialog = dialog.replaceFirst("%AI%", "");

			show(dialog, player, npc);
		}
		return true;
	}

	public static String getNpcRaceById(final int raceId)
	{
		switch (raceId)
		{
			case 0:
				return "Нежить";
			case 1:
				return "Магические Создания";
			case 2:
				return "Звери";
			case 3:
				return "Животные";
			case 4:
				return "Растения";
			case 5:
				return "Гуманоиды";
			case 6:
				return "Духи";
			case 7:
				return "Ангелы";
			case 8:
				return "Демоны";
			case 9:
				return "Драконы";
			case 10:
				return "Гиганты";
			case 11:
				return "Жуки";
			case 12:
				return "Феи";
			case 13:
				return "Люди";
			case 14:
				return "Эльфы";
			case 15:
				return "Темные Эльфы";
			case 16:
				return "Орки";
			case 17:
				return "Гномы";
			case 18:
				return "Остальные";
			case 19:
				return "Неживые Существа";
			case 20:
				return "Осадные орудия";
			case 21:
				return "Армия Защиты";
			case 22:
				return "Наемники";
			case 23:
				return "Неизвестное Создание";
			case 24:
				return "Камаэль";
			default:
				return "Not defined";
		}
	}

	public void droplist(String[] param)
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		int type = Integer.parseInt(param[0]);
		int show_page = 1;
		if(param.length > 1)
		{
			show_page = Integer.parseInt(param[1]);
		}

		if(player == null || npc == null)
			return;

		droplist(player, npc, type, show_page);
	}

	public void droplist(Player player, NpcInstance npc, int type, int show_page)
	{
		if(player == null || npc == null)
			return;

		if(Config.ALT_GAME_SHOW_DROPLIST)
			RewardListInfo.showMainInfo(player, npc, type, show_page);
	}

	public void quests()
	{
		final Player player = (Player) getSelf();
		final NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		final StringBuilder dialog = new StringBuilder("<html><center><font color=\"LEVEL\">").append(npc.getName());

		final Quest[] list = npc.getTemplate().getEventQuests(QuestEventType.MOB_KILLED_WITH_QUEST);
		if(list != null && list.length != 0)
		{
			dialog.append("<center><img src=\"L2UI.SquareWhite\" width=\"275\" height=\"1\"><table  bgcolor=\"333333\" width=\"275\"><tr><td width=\"275\"><center><font color=\"FFFF00\">").append(player.isLangRus() ? "Убить для Квеста:" : "On Kill:").append("</font></center></td></tr></table><img src=\"L2UI.SquareWhite\" width=\"275\" height=\"1\"></center><br>");
			for(final Quest q : list)
				dialog.append("<img src=\"L2UI_CH3.QuestBtn\" width=32 height=32></td><td width=\"238\"><br><font color=\"LEVEL\">").append(q.getDescr(player)).append("</font><br1>[<font color=\"b09979\">").append(player.isLangRus() ? "ID Квеста: " : "Quest ID: ").append("</font>").append(q.getQuestIntId()).append("]</td></tr>");
		}

		dialog.append("</body></html>");
		show(dialog.toString(), player);
	}
	public void skills()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		StringBuilder dialog = new StringBuilder("<html scroll=\"no\"><title>").append(npc.getName()).append("</title><body><table>");
		dialog.append("<tr><td valign=\"top\" align=\"center\"><br><br><center><img src=\"L2UI.SquareWhite\" width=\"260\" height=\"1\"></center><table bgcolor=\"333333\" width=\"260\"><tr><td width=\"260\"><center><font color=\"FFFF00\">").append(player.isLangRus() ? "Активные Умения" : "Active Skills").append("</font></center></td></tr></table><center><img src=\"L2UI.SquareWhite\" width=\"260\" height=\"1\"></center></td></tr><tr><td><table>");

		Collection<Skill> list = npc.getAllSkills();
		if(list != null && !list.isEmpty())
		{
			for(Skill s : list)
				if(s.isActive())
					dialog.append("<tr><td width=\"32\"><img src=\"").append(s.getIcon()).append("\" width=\"32\" height=\"32\"></td><td width=\"238\"><font color=\"LEVEL\">").append(s.getName()).append("</font><br1>[<font color=\"b09979\">").append(player.isLangRus() ? "Уровень: " : "Level: ").append("</font>").append(s.getLevel()).append("][<font color=\"b09979\">Id: </font>").append(s.getId()).append("]</td></tr>");
			dialog.append("</table></td></tr><tr><td valign=\"top\" align=\"center\"><br><br><center><img src=\"L2UI.SquareWhite\" width=\"260\" height=\"1\"></center><table bgcolor=\"333333\" width=\"260\"><tr><td width=\"260\"><center><font color=\"FFFF00\">").append(player.isLangRus() ? "Пассивные Умения" : "Passive Skills").append("</font></center></td></tr></table><center><img src=\"L2UI.SquareWhite\" width=\"260\" height=\"1\"></center></td></tr><tr><td><table>");
			for(Skill s : list)
				if(!s.isActive())
					dialog.append("<tr><td width=\"32\"><img src=\"").append(s.getIcon()).append("\" width=\"32\" height=\"32\"></td><td width=\"238\"><font color=\"LEVEL\">").append(s.getName()).append("</font><br1>[<font color=\"b09979\">").append(player.isLangRus() ? "Уровень: " : "Level: ").append("</font>").append(s.getLevel()).append("][<font color=\"b09979\">Id: </font>").append(s.getId()).append("]</td></tr>");
		}

		dialog.append("</table></td></tr></table></body></html>");
		show(dialog.toString(), player, npc);
	}

	public void effects()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		StringBuilder dialog = new StringBuilder("<html scroll=\"no\"><title>").append(npc.getName()).append("</title><body><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr><td><table><tr><td valign=\"top\" align=\"center\">");
		dialog.append("<center><img src=\"L2UI.SquareWhite\" width=\"260\" height=\"1\"></center><table bgcolor=\"333333\" width=\"260\"><tr><td width=\"260\"><center><font color=\"FFFF00\">").append(player.isLangRus() ? "Эффекты" : "Effect").append("</font></center></td></tr></table><center><img src=\"L2UI.SquareWhite\" width=\"260\" height=\"1\"></center></td></tr><tr><td align=center><table width=\"260\">");

		List<Effect> list = npc.getEffectList().getAllEffects();
		if(list != null && !list.isEmpty())
			for(Effect e : list)
				dialog.append("<tr><td width=\"32\"><img src=\"").append(e.getSkill().getIcon()).append("\" width=\"32\" height=\"32\"></td><td width=\"238\"><font color=\"LEVEL\">").append(e.getSkill().getName()).append("</font><br1>[<font color=\"b09979\">").append(player.isLangRus() ? "Уровень: " : "Level: ").append("</font>").append(e.getSkill().getLevel()).append("][<font color=\"b09979\">Id: </font>").append(e.getSkill().getId()).append("]</td></tr>");

		dialog.append("</table></td></tr></table></td></tr></table><br><center><button value=\"").append(player.isLangRus() ? "Обновить" : "Refresh").append("\" action=\"bypass -h scripts_actions.OnActionShift:effects\" width=\"160\" height=\"25\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" /></center></body></html>");
		show(dialog.toString(), player, npc);
	}

	public void stats()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		String dialog = HtmCache.getInstance().getNotNull("scripts/actions/player.L2NpcInstance.stats.htm", player);
		dialog = dialog.replaceFirst("%name%", nameNpc(npc));
		dialog = dialog.replaceFirst("%id%", String.valueOf(npc.getNpcId()));
		dialog = dialog.replaceFirst("%level%", String.valueOf(npc.getLevel()));
		dialog = dialog.replaceFirst("%factionId%", String.valueOf(npc.getFaction()));
		dialog = dialog.replaceFirst("%aggro%", String.valueOf(npc.getAggroRange()));
		dialog = dialog.replaceFirst("%race%", getNpcRaceById(npc.getTemplate().getRace()));
		dialog = dialog.replaceFirst("%maxHp%", String.valueOf(npc.getMaxHp()));
		dialog = dialog.replaceFirst("%maxMp%", String.valueOf(npc.getMaxMp()));
		dialog = dialog.replaceFirst("%pDef%", String.valueOf(npc.getPDef(null)));
		dialog = dialog.replaceFirst("%mDef%", String.valueOf(npc.getMDef(null, null)));
		dialog = dialog.replaceFirst("%pAtk%", String.valueOf(npc.getPAtk(null)));
		dialog = dialog.replaceFirst("%mAtk%", String.valueOf(npc.getMAtk(null, null)));
		dialog = dialog.replaceFirst("%accuracy%", String.valueOf(npc.getAccuracy()));
		dialog = dialog.replaceFirst("%evasionRate%", String.valueOf(npc.getEvasionRate(null)));
		dialog = dialog.replaceFirst("%criticalHit%", String.valueOf(npc.getCriticalHit(null, null)));
		dialog = dialog.replaceFirst("%runSpeed%", String.valueOf(npc.getRunSpeed()));
		dialog = dialog.replaceFirst("%walkSpeed%", String.valueOf(npc.getWalkSpeed()));
		dialog = dialog.replaceFirst("%pAtkSpd%", String.valueOf(npc.getPAtkSpd()));
		dialog = dialog.replaceFirst("%mAtkSpd%", String.valueOf(npc.getMAtkSpd()));
		show(dialog, player, npc);
	}

	public void resists()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		StringBuilder dialog = new StringBuilder("<html><body><center><font color=\"LEVEL\">");
		dialog.append(nameNpc(npc)).append("<br></font></center><table width=\"80%\">");

		boolean hasResist;

		hasResist = addResist(dialog, "Fire", npc.calcStat(Stats.DEFENCE_FIRE, 0, null, null));
		hasResist |= addResist(dialog, "Wind", npc.calcStat(Stats.DEFENCE_WIND, 0, null, null));
		hasResist |= addResist(dialog, "Water", npc.calcStat(Stats.DEFENCE_WATER, 0, null, null));
		hasResist |= addResist(dialog, "Earth", npc.calcStat(Stats.DEFENCE_EARTH, 0, null, null));
		hasResist |= addResist(dialog, "Light", npc.calcStat(Stats.DEFENCE_HOLY, 0, null, null));
		hasResist |= addResist(dialog, "Darkness", npc.calcStat(Stats.DEFENCE_UNHOLY, 0, null, null));
		hasResist |= addResist(dialog, "Bleed", npc.calcStat(Stats.BLEED_RESIST, 0, null, null));
		hasResist |= addResist(dialog, "Poison", npc.calcStat(Stats.POISON_RESIST, 0, null, null));
		hasResist |= addResist(dialog, "Stun", npc.calcStat(Stats.STUN_RESIST, 0, null, null));
		hasResist |= addResist(dialog, "Root", npc.calcStat(Stats.ROOT_RESIST, 0, null, null));
		hasResist |= addResist(dialog, "Sleep", npc.calcStat(Stats.SLEEP_RESIST, 0, null, null));
		hasResist |= addResist(dialog, "Paralyze", npc.calcStat(Stats.PARALYZE_RESIST, 0, null, null));
		hasResist |= addResist(dialog, "Mental", npc.calcStat(Stats.MENTAL_RESIST, 0, null, null));
		hasResist |= addResist(dialog, "Debuff", npc.calcStat(Stats.DEBUFF_RESIST, 0, null, null));
		hasResist |= addResist(dialog, "Cancel", npc.calcStat(Stats.CANCEL_RESIST, 0, null, null));
		hasResist |= addResist(dialog, "Sword", 100 - npc.calcStat(Stats.SWORD_WPN_VULNERABILITY, null, null));
		hasResist |= addResist(dialog, "Dual Sword", 100 - npc.calcStat(Stats.DUAL_WPN_VULNERABILITY, null, null));
		hasResist |= addResist(dialog, "Blunt", 100 - npc.calcStat(Stats.BLUNT_WPN_VULNERABILITY, null, null));
		hasResist |= addResist(dialog, "Dagger", 100 - npc.calcStat(Stats.DAGGER_WPN_VULNERABILITY, null, null));
		hasResist |= addResist(dialog, "Bow", 100 - npc.calcStat(Stats.BOW_WPN_VULNERABILITY, null, null));
		hasResist |= addResist(dialog, "Crossbow", 100 - npc.calcStat(Stats.CROSSBOW_WPN_VULNERABILITY, null, null));
		hasResist |= addResist(dialog, "Polearm", 100 - npc.calcStat(Stats.POLE_WPN_VULNERABILITY, null, null));
		hasResist |= addResist(dialog, "Fist", 100 - npc.calcStat(Stats.FIST_WPN_VULNERABILITY, null, null));

		if(!hasResist)
			dialog.append("</table>No resists</body></html>");
		else
			dialog.append("</table></body></html>");
		show(dialog.toString(), player, npc);
	}

	private boolean addResist(StringBuilder dialog, String name, double val)
	{
		if(val == 0)
			return false;

		dialog.append("<tr><td>").append(name).append("</td><td>");
		if(val == Double.POSITIVE_INFINITY)
			dialog.append("MAX");
		else if(val == Double.NEGATIVE_INFINITY)
			dialog.append("MIN");
		else
		{
			dialog.append(String.valueOf((int) val));
			dialog.append("</td></tr>");
			return true;
		}

		dialog.append("</td></tr>");
		return true;
	}

	public void aggro()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		StringBuilder dialog = new StringBuilder("<html><body><table width=\"270\" border=\"1\" cellspacing=\"5\" cellpadding=\"5\">");
		dialog.append("<tr><td width=\"110\">").append(player.isLangRus() ? "Персонаж" : "Attacker").append("</td>");
		dialog.append("<td width=\"80\">").append(player.isLangRus() ? "Урон" : "Damage").append("</td>");
		dialog.append("<td width=\"80\">").append(player.isLangRus() ? "Агрессия" : "Hate").append("</td></tr>");

		Set<HateInfo> set = new TreeSet<HateInfo>(HateComparator.getInstance());
		set.addAll(npc.getAggroList().getCharMap().values());
		for(HateInfo aggroInfo : set)
			dialog.append("<tr><td width=\"110\" align=center>" + aggroInfo.attacker.getName() + "</td><td width=\"80\">" + aggroInfo.damage + "</td><td width=\"80\">" + aggroInfo.hate + "</td></tr>");

		dialog.append("</table><br></center><center><button value=\"").append(player.isLangRus() ? "Обновить" : "Refresh").append("\" action=\"bypass -h scripts_actions.OnActionShift:aggro\" width=\"160\" height=\"25\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" /></center></body></html>");
		show(dialog.toString(), player, npc);
	}

	public boolean OnActionShift_DoorInstance(Player player, GameObject object)
	{
		if(player == null || object == null || !player.getPlayerAccess().Door || !object.isDoor())
			return false;

		String dialog;
		DoorInstance door = (DoorInstance) object;
		dialog = HtmCache.getInstance().getNotNull("scripts/actions/admin.L2DoorInstance.onActionShift.htm", player);
		dialog = dialog.replaceFirst("%CurrentHp%", String.valueOf((int) door.getCurrentHp()));
		dialog = dialog.replaceFirst("%MaxHp%", String.valueOf(door.getMaxHp()));
		dialog = dialog.replaceAll("%ObjectId%", String.valueOf(door.getObjectId()));
		dialog = dialog.replaceFirst("%doorId%", String.valueOf(door.getDoorId()));
		dialog = dialog.replaceFirst("%pdef%", String.valueOf(door.getPDef(null)));
		dialog = dialog.replaceFirst("%mdef%", String.valueOf(door.getMDef(null, null)));
		dialog = dialog.replaceFirst("%type%", door.getDoorType().name());
		dialog = dialog.replaceFirst("%upgradeHP%", String.valueOf(door.getUpgradeHp()));
		StringBuilder b = new StringBuilder("");
		for(GlobalEvent e : door.getEvents())
			b.append(e.toString()).append(";");
		dialog = dialog.replaceFirst("%event%", b.toString());

		show(dialog, player);
		player.sendActionFailed();
		return true;
	}

	public boolean OnActionShift_Player(Player player, GameObject object)
	{
		if(player == null || object == null || !player.getPlayerAccess().CanViewChar)
			return false;
		if(object.isPlayer())
			AdminEditChar.showCharacterList(player, (Player) object);
		return true;
	}

	public boolean OnActionShift_PetInstance(Player player, GameObject object)
	{
		if(player == null || object == null || !player.getPlayerAccess().CanViewChar)
			return false;
		if(object.isPet())
		{
			PetInstance pet = (PetInstance) object;

			String dialog;

			dialog = HtmCache.getInstance().getNotNull("scripts/actions/admin.L2PetInstance.onActionShift.htm", player);
			dialog = dialog.replaceFirst("%name%", HtmlUtils.htmlNpcName(pet.getNpcId()));
			dialog = dialog.replaceFirst("%title%", String.valueOf(StringUtils.isEmpty(pet.getTitle()) ? "Empty" : pet.getTitle()));
			dialog = dialog.replaceFirst("%level%", String.valueOf(pet.getLevel()));
			dialog = dialog.replaceFirst("%class%", String.valueOf(pet.getClass().getSimpleName().replaceFirst("L2", "").replaceFirst("Instance", "")));
			dialog = dialog.replaceFirst("%xyz%", pet.getLoc().x + " " + pet.getLoc().y + " " + pet.getLoc().z);
			dialog = dialog.replaceFirst("%heading%", String.valueOf(pet.getLoc().h));

			dialog = dialog.replaceFirst("%owner%", String.valueOf(pet.getPlayer().getName()));
			dialog = dialog.replaceFirst("%ownerId%", String.valueOf(pet.getPlayer().getObjectId()));
			dialog = dialog.replaceFirst("%npcId%", String.valueOf(pet.getNpcId()));
			dialog = dialog.replaceFirst("%controlItemId%", String.valueOf(pet.getControlItem().getItemId()));

			dialog = dialog.replaceFirst("%exp%", String.valueOf(pet.getExp()));
			dialog = dialog.replaceFirst("%sp%", String.valueOf(pet.getSp()));

			dialog = dialog.replaceFirst("%maxHp%", String.valueOf(pet.getMaxHp()));
			dialog = dialog.replaceFirst("%maxMp%", String.valueOf(pet.getMaxMp()));
			dialog = dialog.replaceFirst("%currHp%", String.valueOf((int) pet.getCurrentHp()));
			dialog = dialog.replaceFirst("%currMp%", String.valueOf((int) pet.getCurrentMp()));

			dialog = dialog.replaceFirst("%pDef%", String.valueOf(pet.getPDef(null)));
			dialog = dialog.replaceFirst("%mDef%", String.valueOf(pet.getMDef(null, null)));
			dialog = dialog.replaceFirst("%pAtk%", String.valueOf(pet.getPAtk(null)));
			dialog = dialog.replaceFirst("%mAtk%", String.valueOf(pet.getMAtk(null, null)));
			dialog = dialog.replaceFirst("%accuracy%", String.valueOf(pet.getAccuracy()));
			dialog = dialog.replaceFirst("%evasionRate%", String.valueOf(pet.getEvasionRate(null)));
			dialog = dialog.replaceFirst("%crt%", String.valueOf(pet.getCriticalHit(null, null)));
			dialog = dialog.replaceFirst("%runSpeed%", String.valueOf(pet.getRunSpeed()));
			dialog = dialog.replaceFirst("%walkSpeed%", String.valueOf(pet.getWalkSpeed()));
			dialog = dialog.replaceFirst("%pAtkSpd%", String.valueOf(pet.getPAtkSpd()));
			dialog = dialog.replaceFirst("%mAtkSpd%", String.valueOf(pet.getMAtkSpd()));
			dialog = dialog.replaceFirst("%dist%", String.valueOf((int) pet.getRealDistance(player)));

			dialog = dialog.replaceFirst("%STR%", String.valueOf(pet.getSTR()));
			dialog = dialog.replaceFirst("%DEX%", String.valueOf(pet.getDEX()));
			dialog = dialog.replaceFirst("%CON%", String.valueOf(pet.getCON()));
			dialog = dialog.replaceFirst("%INT%", String.valueOf(pet.getINT()));
			dialog = dialog.replaceFirst("%WIT%", String.valueOf(pet.getWIT()));
			dialog = dialog.replaceFirst("%MEN%", String.valueOf(pet.getMEN()));

			show(dialog, player);
		}
		return true;
	}

	public boolean OnActionShift_ItemInstance(Player player, GameObject object)
	{
		if(player == null || object == null || !player.getPlayerAccess().CanViewChar)
			return false;
		if(object.isItem())
		{
			String dialog;
			ItemInstance item = (ItemInstance) object;
			dialog = HtmCache.getInstance().getNotNull("scripts/actions/admin.L2ItemInstance.onActionShift.htm", player);
			dialog = dialog.replaceFirst("%name%", String.valueOf(item.getTemplate().getName()));
			dialog = dialog.replaceFirst("%objId%", String.valueOf(item.getObjectId()));
			dialog = dialog.replaceFirst("%itemId%", String.valueOf(item.getItemId()));
			dialog = dialog.replaceFirst("%grade%", String.valueOf(item.getCrystalType()));
			dialog = dialog.replaceFirst("%count%", String.valueOf(item.getCount()));

			Player owner = GameObjectsStorage.getPlayer(item.getOwnerId()); //FIXME [VISTALL] несовсем верно, может быть CCE при условии если овнер не игрок
			dialog = dialog.replaceFirst("%owner%", String.valueOf(owner == null ? "none" : owner.getName()));
			dialog = dialog.replaceFirst("%ownerId%", String.valueOf(item.getOwnerId()));

			for(Element e : Element.VALUES)
				dialog = dialog.replaceFirst("%" + e.name().toLowerCase() + "Val%", String.valueOf(item.getAttributeElementValue(e, true)));

			dialog = dialog.replaceFirst("%attrElement%", String.valueOf(item.getAttributeElement()));
			dialog = dialog.replaceFirst("%attrValue%", String.valueOf(item.getAttributeElementValue()));

			dialog = dialog.replaceFirst("%enchLevel%", String.valueOf(item.getEnchantLevel()));
			dialog = dialog.replaceFirst("%type%", String.valueOf(item.getItemType()));

			dialog = dialog.replaceFirst("%dropTime%", String.valueOf(item.getDropTimeOwner()));
			//dialog = dialog.replaceFirst("%dropOwner%", String.valueOf(item.getDropOwnerId()));
			//dialog = dialog.replaceFirst("%dropOwnerId%", String.valueOf(item.getDropOwnerId()));

			show(dialog, player);
			player.sendActionFailed();
		}
		return true;
	}

	private String nameNpc(NpcInstance npc)
	{
		if(npc.getNameNpcString() == NpcString.NONE)
			return HtmlUtils.htmlNpcName(npc.getNpcId());
		else
			return HtmlUtils.htmlNpcString(npc.getNameNpcString().getId(), npc.getName());
	}
}