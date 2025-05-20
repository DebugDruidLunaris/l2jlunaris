package jts.gameserver.handler.chat;

import jts.gameserver.network.serverpackets.components.ChatType;

public interface IChatHandler
{
	void say();

	ChatType getType();
}