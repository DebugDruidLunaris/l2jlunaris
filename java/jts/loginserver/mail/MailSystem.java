package jts.loginserver.mail;

import jts.loginserver.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MailSystem
{
	private static final Logger _log = LoggerFactory.getLogger(MailSystem.class);
	private final Map<String, MailContent> _mailData = new HashMap<String, MailContent>();

	public static MailSystem getInstance()
	{
		return SingletonHolder._instance;
	}

	public MailSystem()
	{
		loadMails();
	}

	public void sendMail(String account, String messageId, String... args)
	{
		BaseMail mail = new BaseMail(account, messageId, args);
		mail.run();
	}

	private void loadMails()
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		File file = new File(Config.DATAPACK_ROOT, "config/mail/MailList.xml");
		Document doc;
		if(file.exists())
		{
			try
			{
				doc = factory.newDocumentBuilder().parse(file);
			}
			catch(Exception e)
			{
				_log.warn("Could not parse MailList.xml file: " + e.getMessage(), e);
				return;
			}

			Node n = doc.getFirstChild();
			File mailFile;
			for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
			{
				if(d.getNodeName().equals("mail"))
				{
					String mailId = d.getAttributes().getNamedItem("id").getNodeValue();
					String subject = d.getAttributes().getNamedItem("subject").getNodeValue();
					String maFile = d.getAttributes().getNamedItem("file").getNodeValue();

					mailFile = new File(Config.DATAPACK_ROOT, "config/mail/" + maFile);
					try
					{
						FileInputStream fis = new FileInputStream(mailFile);
						@SuppressWarnings("resource")
						BufferedInputStream bis = new BufferedInputStream(fis);
						int bytes = bis.available();
						byte[] raw = new byte[bytes];

						bis.read(raw);
						String html = new String(raw, "UTF-8");
						html = html.replaceAll("\r\n", "\n");
						html = html.replace("%servermail%", Config.EMAIL_SERVERINFO_ADDRESS);
						html = html.replace("%servername%", Config.EMAIL_SERVERINFO_NAME);

						_mailData.put(mailId, new MailContent(subject, html));
					}
					catch(IOException e)
					{
						_log.warn("IOException while reading " + maFile);
					}
				}
			}
			_log.info("eMail System Loaded");
		}
		else
		{
			_log.warn("Cannot load eMail System - Missing file MailList.xml");
		}
	}

	public class MailContent
	{
		private final String _subject;
		private final String _text;

		/**
		 * @param subject
		 * @param text
		 */
		public MailContent(String subject, String text)
		{
			_subject = subject;
			_text = text;
		}

		public String getSubject()
		{
			return _subject;
		}

		public String getText()
		{
			return _text;
		}
	}

	public MailContent getMailContent(String mailId)
	{
		return _mailData.get(mailId);
	}

	private static class SingletonHolder
	{
		protected static final MailSystem _instance = new MailSystem();
	}
}
