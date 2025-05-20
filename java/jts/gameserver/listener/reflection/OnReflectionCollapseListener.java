package jts.gameserver.listener.reflection;

import jts.commons.listener.Listener;
import jts.gameserver.model.entity.Reflection;

public interface OnReflectionCollapseListener extends Listener<Reflection>
{
	public void onReflectionCollapse(Reflection reflection);
}