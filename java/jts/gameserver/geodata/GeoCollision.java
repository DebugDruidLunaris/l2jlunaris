package jts.gameserver.geodata;

import jts.commons.geometry.Shape;

public interface GeoCollision
{
	public Shape getShape();
	public byte[][] getGeoAround();
	public void setGeoAround(byte[][] geo);
	public boolean isConcrete();
}