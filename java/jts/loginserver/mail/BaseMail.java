package jts.loginserver.mail;

import jts.commons.dbutils.DbUtils;
import jts.loginserver.Config;
import jts.loginserver.database.L2DatabaseFactory;
import jts.loginserver.mail.MailSystem.MailContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

public class BaseMail implements Runnable
{
	private static final Logger _log = LoggerFactory.getLogger(BaseMail.class);

	private final Properties _mailProp = new Properties();
	private final SmtpAuthenticator _authenticator;
	private MimeMessage _messageMime = null;

	private class SmtpAuthenticator extends Authenticator
	{
		private final PasswordAuthentication _auth;

		public SmtpAuthenticator()
		{
			_auth = new PasswordAuthentication(Config.EMAIL_SYS_USERNAME, Config.EMAIL_SYS_PASSWORD);
		}

		@Override
		public PasswordAuthentication getPasswordAuthentication()
		{
			return _auth;
		}
	}

	public BaseMail(String account, String mailId, String... args)
	{
		_mailProp.put("mail.smtp.host", Config.EMAIL_SYS_HOST);
		_mailProp.put("mail.smtp.auth", Config.EMAIL_SYS_SMTP_AUTH);
		_mailProp.put("mail.smtp.port", Config.EMAIL_SYS_PORT);
		_mailProp.put("mail.smtp.socketFactory.port", Config.EMAIL_SYS_PORT);
		_mailProp.put("mail.smtp.socketFactory.class", Config.EMAIL_SYS_FACTORY);
		_mailProp.put("mail.smtp.socketFactory.fallback", Config.EMAIL_SYS_FACTORY_CALLBACK);

		_authenticator = Config.EMAIL_SYS_SMTP_AUTH ? new SmtpAuthenticator() : null;

		String mailAddr = getUserMail(account);

		if(mailAddr == null)
			return;

		MailContent content = MailSystem.getInstance().getMailContent(mailId);
		if(content == null)
			return;

		String message = compileHtml(account, content.getText(), args);

		Session mailSession = Session.getDefaultInstance(_mailProp, _authenticator);

		try
		{
			_messageMime = new MimeMessage(mailSession);
			_messageMime.setSubject(content.getSubject());
			try
			{
				_messageMime.setFrom(new InternetAddress(Config.EMAIL_SYS_ADDRESS, Config.EMAIL_SERVERINFO_NAME));
			}
			catch(UnsupportedEncodingException e)
			{
				_log.warn("Sender Address not Valid!");
			}
			_messageMime.setContent(message, "text/html");
			_messageMime.setRecipient(Message.RecipientType.TO, new InternetAddress(mailAddr));
		}
		catch(MessagingException e)
		{
			_log.warn(getClass().getSimpleName() + ": " + e.getMessage());
		}
	}

	private String compileHtml(String account, String html, String[] args)
	{
		if(args != null)
			for(int i = 0; i < args.length; i++)
				html = html.replace("%var" + i + "%", args[i]);

		html = html.replace("%accountname%", account);
		return html;
	}

	private String getUserMail(String username)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(Config.EMAIL_SYS_SELECTQUERY);
			statement.setString(1, username);
			ResultSet rset = statement.executeQuery();
			if(rset.next())
			{
				String mail = rset.getString(Config.EMAIL_SYS_DBFIELD);
				return mail;
			}
			rset.close();
			statement.close();
		}
		catch(Exception e)
		{
			_log.warn("Cannot select user mail: Exception");
		}
		finally
		{
			DbUtils.closeQuietly(con);
		}
		return null;
	}

	@Override
	public void run()
	{
		try
		{
			if(_messageMime != null)
				Transport.send(_messageMime);
		}
		catch(MessagingException e)
		{
			_log.warn("Error encounterd while sending email");
		}
	}
}
