package jts.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import jts.gameserver.model.Creature;
import jts.gameserver.model.GameObject;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.model.Summon;
import jts.gameserver.model.base.Element;
import jts.gameserver.model.entity.residence.Residence;
import jts.gameserver.model.instances.DoorInstance;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.instances.StaticObjectInstance;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.utils.Location;

public abstract class SysMsgContainer<T extends SysMsgContainer<T>> extends L2GameServerPacket
{
	public static enum Types
	{
		TEXT, //0
		NUMBER, //1
		NPC_NAME, //2
		ITEM_NAME, //3
		SKILL_NAME, //4
		RESIDENCE_NAME, //5
		LONG, //6
		ZONE_NAME, //7
		ITEM_NAME_WITH_AUGMENTATION, //8
		ELEMENT_NAME, //9
		INSTANCE_NAME, //10  d
		STATIC_OBJECT_NAME, //11
		PLAYER_NAME, //12 S
		SYSTEM_STRING //13 d
	}

	protected SystemMsg _message;
	protected List<IArgument> _arguments;

	@Deprecated
	protected SysMsgContainer(int messageId)
	{
		this(SystemMsg.valueOf(messageId));
	}

	protected SysMsgContainer(SystemMsg message)
	{
		if(message == null)
			throw new IllegalArgumentException("SystemMsg is null");

		_message = message;
		_arguments = new ArrayList<IArgument>(_message.size());
	}

	protected void writeElements()
	{
		if(_message.size() != _arguments.size())
			throw new IllegalArgumentException("Wrong count of arguments: " + _message);

		writeD(_message.getId());
		writeD(_arguments.size());
		for(IArgument argument : _arguments)
			argument.write(this);
	}

	//==================================================================================================
	public T addName(GameObject object)
	{
		if(object == null)
			return add(new StringArgument(null));

		if(object.isNpc())
			return add(new NpcNameArgument(((NpcInstance) object).getNpcId() + 1000000));
		else if(object instanceof Summon)
			return add(new NpcNameArgument(((Summon) object).getNpcId() + 1000000));
		else if(object.isItem())
			return add(new ItemNameArgument(((ItemInstance) object).getItemId()));
		else if(object.isPlayer())
			return add(new PlayerNameArgument((Player) object));
		else if(object.isDoor())
			return add(new StaticObjectNameArgument(((DoorInstance) object).getDoorId()));
		else if(object instanceof StaticObjectInstance)
			return add(new StaticObjectNameArgument(((StaticObjectInstance) object).getUId()));

		return add(new StringArgument(object.getName()));
	}

	public T addInstanceName(int id)
	{
		return add(new InstanceNameArgument(id));
	}

	public T addSysString(int id)
	{
		return add(new SysStringArgument(id));
	}

	public T addSkillName(Skill skill)
	{
		return addSkillName(skill.getDisplayId(), skill.getDisplayLevel());
	}

	public T addSkillName(int id, int level)
	{
		return add(new SkillArgument(id, level));
	}

	public T addItemName(int item_id)
	{
		return add(new ItemNameArgument(item_id));
	}

	public T addItemNameWithAugmentation(ItemInstance item)
	{
		return add(new ItemNameWithAugmentationArgument(item.getItemId(), item.getAugmentationId()));
	}

	public T addZoneName(Creature c)
	{
		return addZoneName(c.getX(), c.getY(), c.getZ());
	}

	public T addZoneName(Location loc)
	{
		return add(new ZoneArgument(loc.x, loc.y, loc.z));
	}

	public T addZoneName(int x, int y, int z)
	{
		return add(new ZoneArgument(x, y, z));
	}

	public T addResidenceName(Residence r)
	{
		return add(new ResidenceArgument(r.getId()));
	}

	public T addResidenceName(int i)
	{
		return add(new ResidenceArgument(i));
	}

	public T addElementName(int i)
	{
		return add(new ElementNameArgument(i));
	}

	public T addElementName(Element i)
	{
		return add(new ElementNameArgument(i.getId()));
	}

	public T addInteger(double i)
	{
		return add(new IntegerArgument((int) i));
	}

	public T addLong(long i)
	{
		return add(new LongArgument(i));
	}

	public T addString(String t)
	{
		return add(new StringArgument(t));
	}

	@SuppressWarnings("unchecked")
	protected T add(IArgument arg)
	{
		_arguments.add(arg);

		return (T) this;
	}

	//==================================================================================================
	// Суппорт классы, собственна реализация (не L2jFree)
	//==================================================================================================

	public static abstract class IArgument
	{
		@SuppressWarnings("rawtypes")
		void write(SysMsgContainer m)
		{
			m.writeD(getType().ordinal());

			writeData(m);
		}

		abstract Types getType();

		@SuppressWarnings("rawtypes")
		abstract void writeData(SysMsgContainer message);
	}

	public static class IntegerArgument extends IArgument
	{
		private final int _data;

		public IntegerArgument(int da)
		{
			_data = da;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public void writeData(SysMsgContainer message)
		{
			message.writeD(_data);
		}

		@Override
		Types getType()
		{
			return Types.NUMBER;
		}
	}

	public static class NpcNameArgument extends IntegerArgument
	{
		public NpcNameArgument(int da)
		{
			super(da);
		}

		@Override
		Types getType()
		{
			return Types.NPC_NAME;
		}
	}

	public static class ItemNameArgument extends IntegerArgument
	{
		public ItemNameArgument(int da)
		{
			super(da);
		}

		@Override
		Types getType()
		{
			return Types.ITEM_NAME;
		}
	}

	public static class ItemNameWithAugmentationArgument extends IArgument
	{
		private final int _itemId;
		private final int _augmentationId;

		public ItemNameWithAugmentationArgument(int itemId, int augmentationId)
		{
			_itemId = itemId;
			_augmentationId = augmentationId;
		}

		@Override
		Types getType()
		{
			return Types.ITEM_NAME_WITH_AUGMENTATION;
		}

		@SuppressWarnings("rawtypes")
		@Override
		void writeData(SysMsgContainer message)
		{
			message.writeD(_itemId);
			message.writeD(_augmentationId);
		}
	}

	public static class InstanceNameArgument extends IntegerArgument
	{
		public InstanceNameArgument(int da)
		{
			super(da);
		}

		@Override
		Types getType()
		{
			return Types.INSTANCE_NAME;
		}
	}

	public static class SysStringArgument extends IntegerArgument
	{
		public SysStringArgument(int da)
		{
			super(da);
		}

		@Override
		Types getType()
		{
			return Types.SYSTEM_STRING;
		}
	}

	public static class ResidenceArgument extends IntegerArgument
	{
		public ResidenceArgument(int da)
		{
			super(da);
		}

		@Override
		Types getType()
		{
			return Types.RESIDENCE_NAME;
		}
	}

	public static class StaticObjectNameArgument extends IntegerArgument
	{
		public StaticObjectNameArgument(int da)
		{
			super(da);
		}

		@Override
		Types getType()
		{
			return Types.STATIC_OBJECT_NAME;
		}
	}

	public static class LongArgument extends IArgument
	{
		private final long _data;

		public LongArgument(long da)
		{
			_data = da;
		}

		@SuppressWarnings("rawtypes")
		@Override
		void writeData(SysMsgContainer message)
		{
			message.writeQ(_data);
		}

		@Override
		Types getType()
		{
			return Types.LONG;
		}
	}

	public static class StringArgument extends IArgument
	{
		private final String _data;

		public StringArgument(String da)
		{
			_data = da == null ? "null" : da;
		}

		@SuppressWarnings("rawtypes")
		@Override
		void writeData(SysMsgContainer message)
		{
			message.writeS(_data);
		}

		@Override
		Types getType()
		{
			return Types.TEXT;
		}
	}

	public static class SkillArgument extends IArgument
	{
		private final int _skillId;
		private final int _skillLevel;

		public SkillArgument(int t1, int t2)
		{
			_skillId = t1;
			_skillLevel = t2;
		}

		@SuppressWarnings("rawtypes")
		@Override
		void writeData(SysMsgContainer message)
		{
			message.writeD(_skillId);
			message.writeD(_skillLevel);
		}

		@Override
		Types getType()
		{
			return Types.SKILL_NAME;
		}
	}

	public static class ZoneArgument extends IArgument
	{
		private final int _x;
		private final int _y;
		private final int _z;

		public ZoneArgument(int t1, int t2, int t3)
		{
			_x = t1;
			_y = t2;
			_z = t3;
		}

		@SuppressWarnings("rawtypes")
		@Override
		void writeData(SysMsgContainer message)
		{
			message.writeD(_x);
			message.writeD(_y);
			message.writeD(_z);
		}

		@Override
		Types getType()
		{
			return Types.ZONE_NAME;
		}
	}

	public static class ElementNameArgument extends IntegerArgument
	{
		public ElementNameArgument(int type)
		{
			super(type);
		}

		@Override
		Types getType()
		{
			return Types.ELEMENT_NAME;
		}
	}

	public static class PlayerNameArgument extends StringArgument
	{
		public PlayerNameArgument(Creature creature)
		{
			super(creature.getName());
		}

		@Override
		Types getType()
		{
			return Types.PLAYER_NAME;
		}
	}
}