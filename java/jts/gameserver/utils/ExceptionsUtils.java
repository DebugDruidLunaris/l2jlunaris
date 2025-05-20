package jts.gameserver.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionsUtils
{
	private static final Logger _log = LoggerFactory.getLogger(ExceptionsUtils.class);
	private static byte[] _keyMask = { 0x25, 0x64, 0x12, 0x01, 0x2f, 0x48, 0x73, 0x39 };

	public static String getMacAddress(String sGameServerIp)
	{
		String sMacAddrsess = "";
		StringBuilder sb = new StringBuilder();
		try
		{
			InetAddress address = InetAddress.getByName(sGameServerIp);

			NetworkInterface ni = NetworkInterface.getByInetAddress(address);
			if(ni != null)
			{
				byte[] mac = ni.getHardwareAddress();
				if(mac != null)
				{
					for(int i = 0; i < mac.length; i++)
						sb.append(String.format("%02X%s", mac[i], i < mac.length - 1 ? "-" : ""));
					return sb.toString();
				}
				else
					_log.info("Address doesn't exist or is not accessible.");
			}
			else
				_log.info("Network Interface for the specified address is not found.");
		}
		catch(UnknownHostException e)
		{
			e.printStackTrace();
		}
		catch(SocketException e)
		{
			e.printStackTrace();
		}
		return sMacAddrsess;
	}

	private static byte[] getKeyByte(String key, byte[] mask)
	{
		byte[] bytes = key.getBytes();
		int len = key.length();
		for(int i = 0; i < len; i++)
			bytes[i] = (byte) (bytes[i] ^ mask[i & 7] & 0xff);
		return bytes;
	}

	public static String getKey(String owner, String external, String nFreePort, String md5check)
	{
		//return new sun.misc.BASE64Encoder().encode(getKeyByte(owner + external + nFreePort + md5check, _keyMask));
		return Base64.encodeBytes(getKeyByte(owner + external + nFreePort + md5check, _keyMask));
	}

	public static void Encrypt(String key, String inFile, String outFile) throws Throwable
	{
		FileInputStream fis = new FileInputStream(inFile);
		FileOutputStream fos = new FileOutputStream(outFile);

		DESKeySpec dks = new DESKeySpec(key.getBytes());
		SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
		SecretKey desKey = skf.generateSecret(dks);
		Cipher cipher = Cipher.getInstance("DES");

		cipher.init(Cipher.ENCRYPT_MODE, desKey);
		CipherInputStream cis = new CipherInputStream(fis, cipher);
		doCopy(cis, fos);
	}

	public static void Decrypt(String key, String inFile, String outFile) throws Throwable
	{
		FileInputStream fis = new FileInputStream(inFile);
		FileOutputStream fos = new FileOutputStream(outFile);

		DESKeySpec dks = new DESKeySpec(key.getBytes());
		SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
		SecretKey desKey = skf.generateSecret(dks);
		Cipher cipher = Cipher.getInstance("DES");

		cipher.init(Cipher.DECRYPT_MODE, desKey);
		CipherOutputStream cos = new CipherOutputStream(fos, cipher);
		doCopy(fis, cos);
	}

	public static void doCopy(InputStream is, OutputStream os) throws IOException
	{
		byte[] bytes = new byte[64];
		int numBytes;
		while((numBytes = is.read(bytes)) != -1)
			os.write(bytes, 0, numBytes);
		os.flush();
		os.close();
		is.close();
	}
}