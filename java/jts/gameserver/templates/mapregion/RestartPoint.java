package jts.gameserver.templates.mapregion;

import java.util.List;

import jts.gameserver.utils.Location;

public class RestartPoint
{
	private final String _name;
	private final int _bbs;
	private final int _msgId;
	private final List<Location> _restartPoints;
	private final List<Location> _PKrestartPoints;

	public RestartPoint(String name, int bbs, int msgId, List<Location> restartPoints, List<Location> PKrestartPoints)
	{
		_name = name;
		_bbs = bbs;
		_msgId = msgId;
		_restartPoints = restartPoints;
		_PKrestartPoints = PKrestartPoints;
	}
	public String getNameLoc()
	{
		if(getName().equalsIgnoreCase("[aden_town]"))
			return " Деревни Адена";
		if(getName().equalsIgnoreCase("[oren_castle_town]"))
			return " Деревни Орена";
		if(getName().equalsIgnoreCase("[giran_castle_town]"))
			return " Деревни Гирана";
		if(getName().equalsIgnoreCase("[giran_habor]"))
			return " Гавани Гирана";
		if(getName().equalsIgnoreCase("[heiness_town]"))
			return " Деревни Хейна";
		if(getName().equalsIgnoreCase("[dion_castle_town]"))
			return " Деревни Диона";
		if(getName().equalsIgnoreCase("[floran_town]"))
			return " Деревни Флоран";
		if(getName().equalsIgnoreCase("[gludio_castle_town]"))
			return " Деревни Глудио";
		if(getName().equalsIgnoreCase("[DMZ]"))
			return " Нейтральной Зоны";
		if(getName().equalsIgnoreCase("[gludin_town]"))
			return " Деревни Глудина";
		if(getName().equalsIgnoreCase("[darkelf_town]"))
			return " Деревни Темных Эльфов";
		if(getName().equalsIgnoreCase("[elf_town]"))
			return " Деревни Эльфов";
		if(getName().equalsIgnoreCase("[talking_island_town]"))
			return " Деревни Говорящего Острова";
		if(getName().equalsIgnoreCase("[godard_town]"))
			return " Деревни Годдарт";
		if(getName().equalsIgnoreCase("[town_of_schuttgart]"))
			return " Деревни Шутгарт";
		if(getName().equalsIgnoreCase("[rune_town]"))
			return "  Деревни Руна";
		if(getName().equalsIgnoreCase("[kamael_town]"))
			return " Деревни Камаэль";
		if(getName().equalsIgnoreCase("[dwarf_town]"))
			return " Деревни Гномов";
		if(getName().equalsIgnoreCase("[orc_town]"))
			return " Деревни Орков";
		if(getName().equalsIgnoreCase("[hunter_town]"))
			return " Деревни Охотников";
		return getName();
	//TODO переделать на сисмесаги , с клиента 910-926
	}
	public String getName()
	{
		return _name;
	}

	public int getBbs()
	{
		return _bbs;
	}

	public int getMsgId()
	{
		return _msgId;
	}

	public List<Location> getRestartPoints()
	{
		return _restartPoints;
	}

	public List<Location> getPKrestartPoints()
	{
		return _PKrestartPoints;
	}
}