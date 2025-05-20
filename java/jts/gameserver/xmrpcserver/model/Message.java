package jts.gameserver.xmrpcserver.model;



public class Message
{
	@SuppressWarnings("unused")
	private final MessageType type;
	@SuppressWarnings("unused")
	private final String message;
	@SuppressWarnings("unused")
	private final String data;

	public Message(MessageType type)
	{
		this.type = type;
		message = "";
		data = "";
	}

	public Message(MessageType type, String message)
	{
		this.type = type;
		this.message = message;
		data = "";
	}

	public Message(MessageType type, String message, String data)
	{
		this.type = type;
		this.message = message;
		this.data = data;
	}

	public enum MessageType
	{
		ERROR,
		WARNING,
		NOTICE,
		OK,
		FAILED,
	}
}