package jts.loginserver.crypt;

import jonelo.jacksum.JacksumAPI;
import jonelo.jacksum.algorithm.AbstractChecksum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PasswordHash
{
	private final static Logger _log = LoggerFactory.getLogger(PasswordHash.class);

	private static String name;

	@SuppressWarnings("static-access")
	public PasswordHash(String name)
	{
		this.name = name;
	}

	/**
	 * Сравнивает пароль и ожидаемый хеш
	 * @param password
	 * @param expected
	 * @return совпадает или нет
	 */
	public boolean compare(String password, String expected)
	{
		try
		{
			return encrypt(password).equals(expected);
		}
		catch(Exception e)
		{
			_log.error(name + ": encryption error!", e);
			return false;
		}
	}

	/**
	 * Получает пароль и возвращает хеш
	 * @param password
	 * @return hash
	 */
	public static String encrypt(String password) throws Exception
	{
		AbstractChecksum checksum = JacksumAPI.getChecksumInstance(name);
		checksum.setEncoding("BASE64");
		checksum.update(password.getBytes());
		return checksum.format("#CHECKSUM");
	}
}