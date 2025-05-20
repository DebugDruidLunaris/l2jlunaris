package jts.gameserver.model.reference;

import jts.commons.lang.reference.AbstractHardReference;

public class L2Reference<T> extends AbstractHardReference<T>
{
	public L2Reference(T reference)
	{
		super(reference);
	}
}