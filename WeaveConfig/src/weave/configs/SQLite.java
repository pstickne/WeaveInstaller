package weave.configs;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import weave.managers.ConfigManager;
import weave.managers.IconManager;
import weave.utils.BugReportUtils;
import weave.utils.ObjectUtils;
import weave.utils.TraceUtils;

public class SQLite extends Config
{
	public static final String NAME = "SQLite";
	private static SQLite _instance = null;
	
	public static SQLite getConfig()
	{
		if( _instance == null )
			_instance = new SQLite();
		return _instance;
	}
	
	
	public SQLite()
	{
		super(NAME);
	}
	
	@Override public void initConfig() 
	{
		super.initConfig();
		Map<String, Object> savedCFG = ConfigManager.getConfigManager().getSavedConfigSettings(getConfigName());
		
		try {
			setDescription(	getConfigName() + " is a software library that implements a self-contained, " +
							"serverless, zero-configuration, transactional SQL database engine.");
			setWarning(	"<center><b>" + getConfigName() + " will run inside the tool and does not require an external application.<br>" +
						"This is the appropriate choice for new users.</b></center>");
			setImage(ImageIO.read(IconManager.IMAGE_SQLITE));

			if( (Boolean)ObjectUtils.ternary(savedCFG, "get", false,
					new Class<?>[] { Object.class }, 
					new Object[] { "ACTIVE" }) )
				loadConfig();
			
		} catch (IOException e) {
			TraceUtils.trace(TraceUtils.STDERR, e);
			BugReportUtils.showBugReportDialog(e);
		} catch (NoSuchMethodException e) {
			TraceUtils.trace(TraceUtils.STDERR, e);
			BugReportUtils.showBugReportDialog(e);
		} catch (SecurityException e) {
			TraceUtils.trace(TraceUtils.STDERR, e);
			BugReportUtils.showBugReportDialog(e);
		} catch (IllegalAccessException e) {
			TraceUtils.trace(TraceUtils.STDERR, e);
			BugReportUtils.showBugReportDialog(e);
		} catch (IllegalArgumentException e) {
			TraceUtils.trace(TraceUtils.STDERR, e);
			BugReportUtils.showBugReportDialog(e);
		} catch (InvocationTargetException e) {
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
					"There was an error loading the " + CONFIG_NAME + " plugin.\n" + 
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