package jts.gameserver.xmrpcserver.model;


public class ServerRates
{
	@SuppressWarnings("unused")
	private final double xp;
	@SuppressWarnings("unused")
	private final double sp;
	@SuppressWarnings("unused")
	private final double questDrop;
	@SuppressWarnings("unused")
	private final double questReward;
	@SuppressWarnings("unused")
	private final double drop;
	@SuppressWarnings("unused")
	private final double spoil;
	@SuppressWarnings("unused")
	private final double raid;

	public ServerRates(double xp, double sp, double questDrop, double questReward, double drop, double spoil, double raid)
	{
		this.xp = xp;
		this.sp = sp;
		this.questDrop = questDrop;
		this.questReward = questReward;
		this.drop = drop;
		this.spoil = spoil;
		this.raid = raid;
	}
}
