/*
    Weave (Web-based Analysis and Visualization Environment)
    Copyright (C) 2008-2011 University of Massachusetts Lowell

    This file is a part of Weave.

    Weave is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License, Version 3,
    as published by the Free Software Foundation.

    Weave is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Weave.  If not, see <http://www.gnu.org/licenses/>.
*/

package weave.configs;

import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import weave.managers.ConfigManager;
import weave.managers.IconManager;
import weave.utils.BugReportUtils;
import weave.utils.TraceUtils;

public class MySQL extends Config
{
	public static MySQL _instance 		= null;
	public static MySQL getConfig()
	{
		if( _instance == null )
			_instance = new MySQL();
		return _instance;
	}
	
	public MySQL()
	{
		super("MySQL", "http://dev.mysql.com/downloads/mysql/");
	}

	@Override public void initConfig()
	{
		super.initConfig(_PORT);
		
		try {
			
			setDescription(getConfigName() + " is a widely used open-source relational database management system.");
			setWarning("<center><b>" + getConfigName() + " requires the use of its external application found " + 
						"<a href='" + getURL() + "'>here.</a></b></center>");
			setImage(ImageIO.read(IconManager.IMAGE_MYSQL));

		} catch (IOException e) {
			TraceUtils.trace(TraceUtils.STDERR, e);
			BugReportUtils.showBugReportDialog(e);
		} catch (NumberFormatException e) {
			TraceUtils.trace(TraceUtils.STDERR, e);
			BugReportUtils.showBugReportDialog(e);
		}
	}
	@Override public boolean loadConfig() 
	{
		boolean result = ConfigManager.getConfigManager().setDatabase(_instance); 
		if( result )
			super.loadConfig();
		else
			JOptionPane.showMessageDialog(null, 
					"There was an error loading the " + getConfigName() + " plugin.\n" + 
					"Another plugin might already be loaded.", 
					"Error", JOptionPane.ERROR_MESSAGE);
		return result;
	}

	@Override public boolean unloadConfig()
	{
		boolean result = ConfigManager.getConfigManager().setDatabase(null);
		super.unloadConfig();
		return result;
	}
}
