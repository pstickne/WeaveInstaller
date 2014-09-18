package weave.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RegEdit 
{
	public static final String HKEY_LOCAL_MACHINE	= "HKLM\\";
	public static final String HKEY_CURRENT_USER	= "HKCU\\";
	public static final String HKEY_CLASSES_ROOT	= "HKCR\\";
	
	public static final String REG_SZ 			= "REG_SZ";
	public static final String REG_EXPAND_SZ	= "REG_EXPAND_SZ";
	public static final String REG_NONE			= "REG_NONE";
	
	private static boolean doAction(String action, String root, String path, String type, String key, String data)
	{
		StringBuilder sb = new StringBuilder("");
		List<String> query = new ArrayList<String>();
		List<String> results = null;
		
		try {
			
			sb.append("REG " + action + " " + root + path + " ");
	
			if( key == null ) 				{ }
			else if( key.length() == 0 )	sb.append("/ve ");
			else							sb.append("/v " + key + " ");
			
			if( action.equals("ADD") )
			{
				sb.append("/t " + type + " " + "/d \"" + data + "\" ");
			}
			else if( action.equals("DELETE") )
			{
				
			}
			
			sb.append("/f");
			
			query.add("cmd");
			query.add("/C");
			query.add(sb.toString());
			
			results = ProcessUtils.runAndWait(query);
			
			for( int i = 0; i < results.size(); i++ )
				if( results.get(i).toLowerCase().contains("success") )
					return true;
			
		} catch (IOException e) {
			TraceUtils.trace(TraceUtils.STDERR, e);
			BugReportUtils.showBugReportDialog(e);
			return false;
		} catch (InterruptedException e) {
			TraceUtils.trace(TraceUtils.STDERR, e);
			BugReportUtils.showBugReportDialog(e);
			return false;
		}
		return false;
	}
	
	
	public static boolean writeString(String root, String path, String type, String key, String data)
	{
		return doAction("ADD", root, path, type, key, data);
	}
	
	
	public static boolean deleteKey(String root, String path, String key)
	{
		return doAction("DELETE", root, path, "", key, "");
	}
}