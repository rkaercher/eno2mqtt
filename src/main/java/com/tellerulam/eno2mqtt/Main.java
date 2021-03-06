package com.tellerulam.eno2mqtt;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;

public class Main
{
	static final Timer t=new Timer(true);

	private static String getVersion()
	{
		// First, try the manifest tag
		String version=Main.class.getPackage().getImplementationVersion();
		if(version==null)
		{
			// Read from build.gradle instead
			try
			{
				List<String> buildFile=Files.readAllLines(Paths.get("build.gradle"),StandardCharsets.UTF_8);
				Pattern p=Pattern.compile("version.*=.*'([^']+)");
				for(String l:buildFile)
				{
					Matcher m=p.matcher(l);
					if(m.find())
						return m.group(1);
				}
			}
			catch(IOException e)
			{
				/* Ignore, no version */
			}
		}
		return version;
	}

	public static void main(String[] args) throws SecurityException, IOException, NumberFormatException
	{
		/*
		 * Interpret all command line arguments as property definitions (without the knx2mqtt prefix)
		 */
		for(String s:args)
		{
			String sp[]=s.split("=",2);
			if(sp.length!=2)
			{
				System.out.println("Invalid argument (no =): "+s);
				System.exit(1);
			}
			System.setProperty("eno2mqtt."+sp[0],sp[1]);
		}
		SyslogHandler.readConfig();
		Logger L=Logger.getLogger(Main.class.getName());
		L.info("eno2mqtt V"+getVersion()+" (C) 2015 Oliver Wagner <owagner@tellerulam.com>");
		try
		{
			DeviceManager.readDeviceList();
			MQTTHandler.init();
			ESP3Connector.setupConnections();
		}
		catch(Exception e)
		{
			L.log(Level.SEVERE,"Startup error",e);
			System.exit(1);
		}
	}
}
