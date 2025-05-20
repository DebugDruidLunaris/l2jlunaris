package jts.gameserver.scripts;

public interface ScriptFile
{
	/**
	 * Вызывается при загрузке классов скриптов
	 */
	public void onLoad();

	/**
	 * Вызывается при перезагрузке
	 * После перезагрузки onLoad() вызывается автоматически
	 */
	public void onReload();

	/**
	 * Вызывается при выключении сервера
	 */
	public void onShutdown();
}