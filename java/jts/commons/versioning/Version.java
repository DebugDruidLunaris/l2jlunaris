package jts.commons.versioning;

import java.io.File;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Version
{
	private static final Logger _log = LoggerFactory.getLogger(Version.class);

	private String _revisionNumber = "exported";
	private String _versionNumber = "-1";
	private String _buildDate = "";
	private String _buildJdk = "";
	private String _coreDev = "";
	private String _dataDev = "";

	@SuppressWarnings("resource")
	public Version(Class<?> c)
	{
		File jarName = null;
		try
		{
			jarName = Locator.getClassSource(c);
			JarFile jarFile = new JarFile(jarName);

			Attributes attrs = jarFile.getManifest().getMainAttributes();

			setBuildJdk(attrs);

			setBuildDate(attrs);

			setRevisionNumber(attrs);

			setVersionNumber(attrs);

			setCoreDev(attrs);

			setDataDev(attrs);
		}
		catch(IOException e)
		{
			_log.error("Unable to get soft information\nFile name '" + (jarName == null ? "null" : jarName.getAbsolutePath()) + "' isn't a valid jar", e);
		}

	}

	private void setVersionNumber(Attributes attrs)
	{
		String versionNumber = attrs.getValue("Implementation-Version");
		if(versionNumber != null)
			_versionNumber = versionNumber;
		else
			_versionNumber = "-1";
	}

	private void setRevisionNumber(Attributes attrs)
	{
		String revisionNumber = attrs.getValue("Implementation-Build");
		if(revisionNumber != null)
			_revisionNumber = revisionNumber;
		else
			_revisionNumber = "-1";
	}

	private void setBuildJdk(Attributes attrs)
	{
		String buildJdk = attrs.getValue("Build-Jdk");
		if(buildJdk != null)
			_buildJdk = buildJdk;
		else
		{
			buildJdk = attrs.getValue("Created-By");
			if(buildJdk != null)
				_buildJdk = buildJdk;
			else
				_buildJdk = "-1";
		}
	}

	private void setBuildDate(Attributes attrs)
	{
		String buildDate = attrs.getValue("Build-Date");
		if(buildDate != null)
			_buildDate = buildDate;
		else
			_buildDate = "-1";
	}

	private void setCoreDev(Attributes attrs)
	{
		String coreDev = attrs.getValue("Core-Dev");
		if(coreDev != null)
			_coreDev = coreDev;
		else
			_coreDev = "";
	}

	private void setDataDev(Attributes attrs)
	{
		String dataDev = attrs.getValue("Data-Dev");
		if(dataDev != null)
			_dataDev = dataDev;
		else
			_dataDev = "";
	}

	public String getRevisionNumber()
	{
		return _revisionNumber;
	}

	public String getVersionNumber()
	{
		return _versionNumber;
	}

	public String getBuildDate()
	{
		return _buildDate;
	}

	public String getBuildJdk()
	{
		return _buildJdk;
	}

	public String getCoreDev()
	{
		return _coreDev;
	}

	public String getDataDev()
	{
		return _dataDev;
	}
}