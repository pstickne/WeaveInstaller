/*
    Weave (Web-based Analysis and Visualization Environment)
    Copyright (C) 2008-2014 University of Massachusetts Lowell

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

package weave.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;

import weave.Settings;
import weave.async.AsyncCallback;
import weave.async.AsyncObserver;
import weave.async.AsyncTask;
import weave.configs.IConfig;
import weave.configs.Jetty;
import weave.configs.SQLite;
import weave.inc.SetupPanel;
import weave.managers.ConfigManager;
import weave.utils.BugReportUtils;
import weave.utils.DownloadUtils;
import weave.utils.FileUtils;
import weave.utils.ImageUtils;
import weave.utils.LaunchUtils;
import weave.utils.TraceUtils;
import weave.utils.TransferUtils;
import weave.utils.ZipUtils;

@SuppressWarnings("serial")
public class ConfigSetupPanel extends SetupPanel
{
	public JComboBox<String>	servletCombo,		databaseCombo;
	public JLabel				servletImage, 		databaseImage;
	public JLabel				reviewServletImage, reviewDatabaseImage;
	public JEditorPane			servletDesc, 		databaseDesc,		reviewDesc;
	public JEditorPane			servletWarning,		databaseWarning;

	public JLabel				servletWebappsLabel,	servletPortLabel,			databasePortLabel;
	public JLabel				reviewServletTitleLabel, reviewDatabaseTitleLabel;
	public JLabel				reviewServletPortLabel,	reviewServletWebappsLabel, 	reviewDatabasePortLabel;
	public JProgressBar			servletProgressBar;
	public JTextField			servletBrowserPath,		servletPortInput,			databasePortInput;
	public JTextField			reviewServletPortInput, reviewServletWebappsInput, 	reviewDatabasePortInput;
	public JFileChooser			servletFileChooser;
	public JButton				servletBrowserButton;

	/////////////////////////////////////////////////////////////////////////////////////
	
	public ConfigSetupPanel()
	{
		maxPanels = 3;

		setLayout(null);
		setBounds(0, 0, 350, 325);
		
		JPanel panel = null;
		for (int i = 0; i < maxPanels; i++) {
			switch (i) {
				case 0: panel = createServletPanel(); 	break;
				case 1: panel = createDatabasePanel();	break;
				case 2: panel = createReviewPanel();	break;
			}
			panels.add(panel);
			add(panel);
		}
		hidePanels();
	}
	
	/////////////////////////////////////////////////////////////////////////////////////
	
	public JPanel createServletPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setBounds(0, 0, 350, 325);
		panel.setBackground(new Color(0xFFFFFF));
		
		int activeServletIndex = 0;
		
		// Title
		JLabel title = new JLabel("Select Servlet");
		title.setFont(new Font(Settings.FONT, Font.BOLD, 15));
		title.setBounds(20, 20, 140, 30);
		panel.add(title);
		
		// Servlet Combobox
		List<IConfig> servlets = ConfigManager.getConfigManager().getServletConfigs();
		servletCombo = new JComboBox<String>();
		servletCombo.setBounds(160, 22, 170, 22);
		servletCombo.setVisible(true);
		servletCombo.setEnabled(true);
		servletCombo.setFont(new Font(Settings.FONT, Font.PLAIN, 13));
		
		for( int i = 0; i < servlets.size(); i++ ) {
			IConfig cfg = servlets.get(i);
			servletCombo.addItem(cfg.getConfigName());
			if( cfg.isConfigLoaded() )
				activeServletIndex = i;
		}
		
		servletCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateServletInfo(
						ConfigManager
						.getConfigManager()
						.getConfigByName(servletCombo.getSelectedItem()));
			}
		});
		panel.add(servletCombo);

		// Image
		servletImage = new JLabel((ImageIcon)null, JLabel.CENTER);
		servletImage.setBounds(20, 60, 80, 80);
		servletImage.setVerticalAlignment(JLabel.TOP);
		panel.add(servletImage);

		
		// Description
		servletDesc = new JEditorPane();
		servletDesc.setBounds(110, 60, 210, 85);
		servletDesc.setEditable(false);
		servletDesc.setContentType("text/html");
		servletDesc.setFont(new Font(Settings.FONT, Font.PLAIN, 10));
		String styleDesc =  "body { font-family: " + servletDesc.getFont().getFamily() + "; " + 
						"font-size: " + servletDesc.getFont().getSize() + "px; }";
		((HTMLDocument)servletDesc.getDocument()).getStyleSheet().addRule(styleDesc);
		servletDesc.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if( e.getEventType() == HyperlinkEvent.EventType.ACTIVATED )
				{
					try {
						LaunchUtils.browse(e.getURL().toURI());
					} catch (IOException ex) {
						TraceUtils.trace(TraceUtils.STDERR, ex);
						BugReportUtils.showBugReportDialog(ex);
					} catch (InterruptedException ex) {
						TraceUtils.trace(TraceUtils.STDERR, ex);
						BugReportUtils.showBugReportDialog(ex);
					} catch (URISyntaxException ex) {
						TraceUtils.trace(TraceUtils.STDERR, ex);
						BugReportUtils.showBugReportDialog(ex);
					}
				}
			}
		});
		panel.add(servletDesc);

		// Progress bar
		servletProgressBar = new JProgressBar();
		servletProgressBar.setBounds(20, 150, 310, 20);
		servletProgressBar.setMinimum(0);
		servletProgressBar.setMaximum(100);
		servletProgressBar.setValue(0);
		servletProgressBar.setStringPainted(true);
		servletProgressBar.setString("");
		servletProgressBar.setIndeterminate(true);
		servletProgressBar.setVisible(false);
		panel.add(servletProgressBar);
		
		// Warning
		servletWarning = new JEditorPane();
		servletWarning.setBounds(20, 180, 310, 60);
		servletWarning.setEditable(false);
		servletWarning.setContentType("text/html");
		servletWarning.setFont(new Font(Settings.FONT, Font.PLAIN, 10));
		String styleWarning = 	"body { font-family: " + servletWarning.getFont().getFamily() + "; " + 
								"font-size: " + servletWarning.getFont().getSize() + "px; }";
		((HTMLDocument)servletWarning.getDocument()).getStyleSheet().addRule(styleWarning);
		servletWarning.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if( e.getEventType() == HyperlinkEvent.EventType.ACTIVATED )
				{
					try {
						LaunchUtils.browse(e.getURL().toURI());
					} catch (IOException ex) {
						TraceUtils.trace(TraceUtils.STDERR, ex);
						BugReportUtils.showBugReportDialog(ex);
					} catch (InterruptedException ex) {
						TraceUtils.trace(TraceUtils.STDERR, ex);
						BugReportUtils.showBugReportDialog(ex);
					} catch (URISyntaxException ex) {
						TraceUtils.trace(TraceUtils.STDERR, ex);
						BugReportUtils.showBugReportDialog(ex);
					}
				}
			}
		});
		panel.add(servletWarning);

		
		// Servlet directory label
		servletWebappsLabel = new JLabel("Webapps:");
		servletWebappsLabel.setBounds(20, 280, 70, 25);
		servletWebappsLabel.setFont(new Font(Settings.FONT, Font.BOLD, 14));
		panel.add(servletWebappsLabel);
		
		
		// Servlet directory chooser
		servletFileChooser = new JFileChooser();
		servletFileChooser.setMultiSelectionEnabled(false);
		servletFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		servletFileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
		
		servletBrowserPath = new JTextField();
		servletBrowserPath.setBounds(100, 280, 150, 25);
		servletBrowserPath.setEditable(false);
		servletBrowserPath.setFont(new Font(Settings.FONT, Font.PLAIN, 13));
		servletBrowserPath.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(0, 0, 0)));
		servletBrowserPath.setVisible(true);
		panel.add(servletBrowserPath);
		
		servletBrowserButton = new JButton("Browse");
		servletBrowserButton.setBounds(260, 280, 70, 25);
		servletBrowserButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int retVal = servletFileChooser.showOpenDialog(ConfigSetupPanel.this);
				if( retVal == JFileChooser.APPROVE_OPTION )
				{
					IConfig config = ConfigManager
										.getConfigManager()
										.getConfigByName(servletCombo.getSelectedItem());
					File selectedFolder = servletFileChooser.getSelectedFile();
					if( (new File(selectedFolder, "ROOT")).exists() )
					{
						servletBrowserPath.setText(selectedFolder.getAbsolutePath());
						config.setWebappsDirectory(selectedFolder);
					}
					else
					{
						JOptionPane.showMessageDialog(null, 
								"This does not appear to be a valid webapps directory.", 
								"Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		servletBrowserButton.setVisible(true);
		panel.add(servletBrowserButton);
		
		
		// Servlet Port
		servletPortLabel = new JLabel("Port:");
		servletPortLabel.setBounds(20, 250, 70, 25);
		servletPortLabel.setFont(new Font(Settings.FONT, Font.BOLD, 14));
		servletPortLabel.setVisible(true);
		panel.add(servletPortLabel);

		
		// Servlet Port Input
		servletPortInput = new JTextField();
		servletPortInput.setBounds(100, 250, 150, 25);
		servletPortInput.setEditable(true);
		servletPortInput.setFont(new Font(Settings.FONT, Font.PLAIN, 14));
		servletPortInput.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(0, 0, 0)));
		servletPortInput.setVisible(true);
		panel.add(servletPortInput);
		
		
		// Apply default values
		servletCombo.setSelectedIndex(activeServletIndex);
		
		return panel;
	}

	/////////////////////////////////////////////////////////////////////////////////////
	
	public JPanel createDatabasePanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setBounds(0, 0, 350, 325);
		panel.setBackground(new Color(0xFFFFFF));
		
		int activeDatabaseIndex = 0;
		
		// Title
		JLabel title = new JLabel("Select Database");
		title.setFont(new Font(Settings.FONT, Font.BOLD, 15));
		title.setBounds(20, 20, 140, 30);
		panel.add(title);
		
		// Database Combobox
		List<IConfig> databases = ConfigManager.getConfigManager().getDatabaseConfigs();
		databaseCombo = new JComboBox<String>();
		databaseCombo.setBounds(160, 22, 170, 22);
		databaseCombo.setVisible(true);
		databaseCombo.setEnabled(true);
		databaseCombo.setFont(new Font(Settings.FONT, Font.PLAIN, 13));
		
		for( int i = 0; i < databases.size(); i++ ) {
			IConfig cfg = databases.get(i);
			databaseCombo.addItem(cfg.getConfigName());
			if( cfg.isConfigLoaded() )
				activeDatabaseIndex = i;
		}
		
		databaseCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateDatabaseInfo(
						ConfigManager
							.getConfigManager()
							.getConfigByName(databaseCombo.getSelectedItem()));
			}
		});
		panel.add(databaseCombo);

		// Image
		databaseImage = new JLabel((ImageIcon)null, JLabel.CENTER);
		databaseImage.setBounds(20, 60, 80, 80);
		databaseImage.setVerticalAlignment(JLabel.TOP);
		panel.add(databaseImage);

		
		// Description
		databaseDesc = new JEditorPane();
		databaseDesc.setBounds(110, 60, 210, 100);
		databaseDesc.setEditable(false);
		databaseDesc.setContentType("text/html");
		databaseDesc.setFont(new Font(Settings.FONT, Font.PLAIN, 10));
		String styleDesc =  "body { font-family: " + databaseDesc.getFont().getFamily() + "; " + 
							"font-size: " + databaseDesc.getFont().getSize() + "px; }";
		((HTMLDocument)databaseDesc.getDocument()).getStyleSheet().addRule(styleDesc);
		databaseDesc.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if( e.getEventType() == HyperlinkEvent.EventType.ACTIVATED )
				{
					try {
						LaunchUtils.browse(e.getURL().toURI());
					} catch (IOException ex) {
						TraceUtils.trace(TraceUtils.STDERR, ex);
						BugReportUtils.showBugReportDialog(ex);
					} catch (InterruptedException ex) {
						TraceUtils.trace(TraceUtils.STDERR, ex);
						BugReportUtils.showBugReportDialog(ex);
					} catch (URISyntaxException ex) {
						TraceUtils.trace(TraceUtils.STDERR, ex);
						BugReportUtils.showBugReportDialog(ex);
					}
				}
			}
		});
		panel.add(databaseDesc);
		
		
		// Warning
		databaseWarning = new JEditorPane();
		databaseWarning.setBounds(20, 180, 310, 60);
		databaseWarning.setEditable(false);
		databaseWarning.setContentType("text/html");
		databaseWarning.setFont(new Font(Settings.FONT, Font.PLAIN, 10));
		String styleWarning =	"body { font-family: " + databaseWarning.getFont().getFamily() + "; " + 
								"font-size: " + databaseWarning.getFont().getSize() + "px; }";
		((HTMLDocument)databaseWarning.getDocument()).getStyleSheet().addRule(styleWarning);
		databaseWarning.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if( e.getEventType() == HyperlinkEvent.EventType.ACTIVATED )
				{
					try {
						LaunchUtils.browse(e.getURL().toURI());
					} catch (IOException ex) {
						TraceUtils.trace(TraceUtils.STDERR, ex);
						BugReportUtils.showBugReportDialog(ex);
					} catch (InterruptedException ex) {
						TraceUtils.trace(TraceUtils.STDERR, ex);
						BugReportUtils.showBugReportDialog(ex);
					} catch (URISyntaxException ex) {
						TraceUtils.trace(TraceUtils.STDERR, ex);
						BugReportUtils.showBugReportDialog(ex);
					}
				}
			}
		});
		panel.add(databaseWarning);

		
		// Database port label
		databasePortLabel = new JLabel("Port:");
		databasePortLabel.setBounds(20, 250, 70, 25);
		databasePortLabel.setFont(new Font(Settings.FONT, Font.BOLD, 14));
		databasePortLabel.setVisible(true);
		panel.add(databasePortLabel);
		
		// Database port input
		databasePortInput = new JTextField();
		databasePortInput.setBounds(100, 250, 150, 25);
		databasePortInput.setFont(new Font(Settings.FONT, Font.PLAIN, 13));
		databasePortInput.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(0, 0, 0)));
		databasePortInput.setMargin(new Insets(2, 2, 2, 2));
		databasePortInput.setVisible(true);
		panel.add(databasePortInput);
		
		// Apply default values
		databaseCombo.setSelectedIndex(activeDatabaseIndex);
		
		return panel;
	}

	/////////////////////////////////////////////////////////////////////////////////////

	public JPanel createReviewPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setBounds(0, 0, 350, 325);
		panel.setBackground(new Color(0xFFFFFF));
		
		
		JLabel title = new JLabel("Review Configuration");
		title.setFont(new Font(Settings.FONT, Font.BOLD, 16));
		title.setBounds(20, 10, 310, 30);
		panel.add(title);
		
		reviewServletTitleLabel = new JLabel();
		reviewServletTitleLabel.setBounds(20, 50, 80, 25);
		reviewServletTitleLabel.setFont(new Font(Settings.FONT, Font.BOLD, 14));
		reviewServletTitleLabel.setVisible(true);
		panel.add(reviewServletTitleLabel);
		
		reviewServletImage = new JLabel((ImageIcon)null, JLabel.CENTER);
		reviewServletImage.setBounds(250, 50, 60, 60);
		reviewServletImage.setVerticalAlignment(JLabel.TOP);
		reviewServletImage.setVisible(true);
		panel.add(reviewServletImage);
		
		reviewServletPortLabel = new JLabel("Port: ");
		reviewServletPortLabel.setBounds(20, 90, 70, 25);
		reviewServletPortLabel.setFont(new Font(Settings.FONT, Font.PLAIN, 13));
		reviewServletPortLabel.setVisible(true);
		panel.add(reviewServletPortLabel);
		
		reviewServletPortInput = new JTextField();
		reviewServletPortInput.setBounds(90, 90, 100, 25);
		reviewServletPortInput.setVisible(true);
		reviewServletPortInput.setEditable(false);
		panel.add(reviewServletPortInput);

		reviewServletWebappsLabel = new JLabel("Webapps: ");
		reviewServletWebappsLabel.setBounds(20, 120, 70, 25);
		reviewServletWebappsLabel.setFont(new Font(Settings.FONT, Font.PLAIN, 13));
		reviewServletWebappsLabel.setVisible(true);
		panel.add(reviewServletWebappsLabel);
		
		reviewServletWebappsInput = new JTextField();
		reviewServletWebappsInput.setBounds(90, 120, 240, 25);
		reviewServletWebappsInput.setVisible(true);
		reviewServletWebappsInput.setEditable(false);
		panel.add(reviewServletWebappsInput);
		
		reviewDatabaseTitleLabel = new JLabel();
		reviewDatabaseTitleLabel.setBounds(20, 170, 80, 25);
		reviewDatabaseTitleLabel.setFont(new Font(Settings.FONT, Font.BOLD, 14));
		reviewDatabaseTitleLabel.setVisible(true);
		panel.add(reviewDatabaseTitleLabel);
		
		reviewDatabaseImage = new JLabel((ImageIcon)null, JLabel.CENTER);
		reviewDatabaseImage.setBounds(250, 170, 60, 60);
		reviewDatabaseImage.setVerticalAlignment(JLabel.TOP);
		reviewDatabaseImage.setVisible(true);
		panel.add(reviewDatabaseImage);
		
		reviewDatabasePortLabel = new JLabel("Port: ");
		reviewDatabasePortLabel.setBounds(20, 200, 70, 25);
		reviewDatabasePortLabel.setFont(new Font(Settings.FONT, Font.PLAIN, 13));
		reviewDatabasePortLabel.setVisible(true);
		panel.add(reviewDatabasePortLabel);
		
		reviewDatabasePortInput = new JTextField();
		reviewDatabasePortInput.setBounds(90, 200, 100, 25);
		reviewDatabasePortInput.setVisible(true);
		reviewDatabasePortInput.setEditable(false);
		panel.add(reviewDatabasePortInput);
		
		reviewDesc = new JEditorPane();
		reviewDesc.setBounds(20, 250, 310, 60);
		reviewDesc.setEditable(false);
		reviewDesc.setContentType("text/html");
		reviewDesc.setFont(new Font(Settings.FONT, Font.PLAIN, 10));
		String reviewWarning =	"body { font-family: " + reviewDesc.getFont().getFamily() + "; " + 
								"font-size: " + reviewDesc.getFont().getSize() + "px; }";
		((HTMLDocument)reviewDesc.getDocument()).getStyleSheet().addRule(reviewWarning);
		reviewDesc.setText("<center><b>Please make sure this configuration is correct.<br>When you are ready, click finish to complete the setup proccess.</b></center>");
		panel.add(reviewDesc);
		
		updateReviewPanel();
		
		return panel;
	}

	/////////////////////////////////////////////////////////////////////////////////////
	
	public boolean validatePanelInput(int panelIndex)
	{
		switch( panelIndex )
		{
			case 0:
				try {
					String str = servletPortInput.getText();
					int port = Integer.parseInt(str);
					if( port < 1 || port > 65535 ) {
						JOptionPane.showMessageDialog(null, "Port value out of range.", "Error", JOptionPane.WARNING_MESSAGE);
						return false;
					}
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(null, "Port must be a number.", "Error", JOptionPane.ERROR_MESSAGE);
					return false;
				}
				if( servletBrowserPath.isVisible() )
				{
					String str = servletBrowserPath.getText();
					if( str == null || str.length() == 0 ) {
						JOptionPane.showMessageDialog(null, "Webapps path must be specified below.", "Error", JOptionPane.WARNING_MESSAGE);
						return false;
					}
						
					File f = new File(str);
					if( !f.exists() ) {
						JOptionPane.showMessageDialog(null, "Webapps directory does not exist.\n\nPlease make sure the path exists and try again.", "Error", JOptionPane.ERROR_MESSAGE);
						return false;
					}
				}
				break;
				
			case 1:
				if( databasePortInput.isVisible() )
				{
					try {
						String str = databasePortInput.getText();
						int port = Integer.parseInt(str);
						if( port < 1 || port > 65535 ) {
							JOptionPane.showMessageDialog(null, "Port value out of range.", "Error", JOptionPane.WARNING_MESSAGE);
							return false;
						}
					} catch (NumberFormatException e) {
						JOptionPane.showMessageDialog(null, "Port must be a number.", "Error", JOptionPane.ERROR_MESSAGE);
						return false;
					}
				}
				break;
				
			case 2:
				break;
				
			default: break;
		}
		return true;
	}

	/////////////////////////////////////////////////////////////////////////////////////
	
	public boolean savePanelInput()
	{
		IConfig servletConfig = ConfigManager.getConfigManager().getConfigByName(servletCombo.getSelectedItem());
		IConfig databaseConfig = ConfigManager.getConfigManager().getConfigByName(databaseCombo.getSelectedItem());
		
		ConfigManager.getConfigManager().unloadAllConfigs();
		servletConfig.loadConfig();
		databaseConfig.loadConfig();
		
		return ConfigManager.getConfigManager().save();
	}
	public boolean savePanelInput(int panelIndex)
	{
		final IConfig config;
		boolean result = true;
		
		switch (panelIndex) {
			case 0:
				config = ConfigManager.getConfigManager().getConfigByName(servletCombo.getSelectedItem());
				config.setPort(servletPortInput.getText());
				if( servletBrowserPath.isVisible() )
					config.setWebappsDirectory(servletBrowserPath.getText());
				
				// Special Jetty case
				if( config.getConfigName().equals(Jetty.NAME) )
				{
					if( !config.getWebappsDirectory().exists() )
					{
						result = false;
						if( Settings.isOfflineMode() )
						{
							JOptionPane.showMessageDialog(null, 
									config.getConfigName() + " is not currently installed.\n\n" + 
									"Please make sure the provided " + config.getConfigName() + ".zip exists on your desktop\n" + 
									"so it can be properly installed. Press OK to continue.", "Install Plugin", JOptionPane.INFORMATION_MESSAGE);
							final File source = new File(Settings.DESKTOP_DIRECTORY, config.getConfigName() + ".zip");
							final File destination = new File(Settings.DEPLOYED_PLUGINS_DIRECTORY, config.getConfigName());
							
							if( !source.exists() )
							{
								JOptionPane.showMessageDialog(null, "Could not find " + config.getConfigName() + ".zip on the desktop.", "Missing File", JOptionPane.ERROR_MESSAGE);
								return false;
							}
							else
							{
								// Only way to get here is if it is the first time installing
								// or the the previous install failed
								// Therefore, we should delete the directory anyway
								FileUtils.recursiveDelete(destination);
								destination.mkdirs();
								
								extract(source, config);
							}
						}
						else
						{
							int choice = JOptionPane.showConfirmDialog(null, 
									config.getConfigName() + " is not currently installed.\n\n" +
									"Would you like to download it now?", 
									"Download Plugin", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

							if( choice == JOptionPane.YES_OPTION )
							{
								try {
									download(config);
								} catch (MalformedURLException e) {
									TraceUtils.trace(TraceUtils.STDERR, e);
									BugReportUtils.showBugReportDialog(e);
								}
							}
						}
					}
				}
				break;
	
			case 1:
				config = ConfigManager.getConfigManager().getConfigByName(databaseCombo.getSelectedItem());
				if( databasePortInput.isVisible() )
					config.setPort(databasePortInput.getText());
				break;
	
			case 2:
				break;
				
			default:
				break;
		}
		return result;
	}

	/////////////////////////////////////////////////////////////////////////////////////
	
	private void download(final IConfig config) throws MalformedURLException
	{
		final URL url = new URL(config.getURL());
		final File destination = new File(Settings.DOWNLOADS_DIRECTORY, config.getConfigName() + ".zip");
		
		final AsyncObserver observer = new AsyncObserver() {
			@Override
			public void onUpdate() {
				servletProgressBar.setValue(info.percent);
			}
		};
		AsyncCallback callback = new AsyncCallback() {
			@Override
			public void run(Object o) {
				int returnCode = (Integer) o;
				
				switch( returnCode )
				{
					case TransferUtils.COMPLETE:
						servletProgressBar.setValue(100);
						extract(destination, config);
						break;
					case TransferUtils.CANCELLED:
						break;
					case TransferUtils.FAILED:
						break;
				}
			}
		};
		AsyncTask task = new AsyncTask() {
			@Override
			public Object doInBackground() {
				Object o = TransferUtils.FAILED;
				try {
					observer.init(url);
					o = DownloadUtils.download(url, destination, observer, TransferUtils.MB);
				} catch (IOException e) {
					TraceUtils.trace(TraceUtils.STDERR, e);
					BugReportUtils.showBugReportDialog(e);
				} catch (InterruptedException e) {
					TraceUtils.trace(TraceUtils.STDERR, e);
					BugReportUtils.showBugReportDialog(e);
				}
				return o;
			}
		};

		try {
			servletProgressBar.setString("Downloading...");
			servletProgressBar.setVisible(true);
			Thread.sleep(1000);
			servletProgressBar.setIndeterminate(false);
			servletProgressBar.setValue(0);
		} catch (InterruptedException e) {
			TraceUtils.trace(TraceUtils.STDERR, e);
			BugReportUtils.showBugReportDialog(e);
		}
		task.addCallback(callback);
		task.execute();
	}
	
	private void extract(final File source, final IConfig config)
	{
		final File destination = new File(Settings.DEPLOYED_PLUGINS_DIRECTORY, config.getConfigName());
		
		final AsyncObserver observer = new AsyncObserver() {
			@Override
			public void onUpdate() {
				servletProgressBar.setValue(info.percent);
			}
		};
		AsyncCallback callback = new AsyncCallback() {
			@Override
			public void run(Object o) {
				servletProgressBar.setValue(100);
				if( destination.exists() )
					JOptionPane.showMessageDialog(null, 
							config.getConfigName() + " has been installed successfully.\n\n" +
							"Click Next to continue.",
							"Install Sucessful", JOptionPane.INFORMATION_MESSAGE);
				else
					JOptionPane.showMessageDialog(null, 
							config.getConfigName() + " install failed.\n\n" +
							"Reason: Bad Zip File",
							"Install Failed", JOptionPane.ERROR_MESSAGE);
				
				servletProgressBar.setVisible(false);
				servletProgressBar.setIndeterminate(true);
			}
		};
		AsyncTask task = new AsyncTask() {
			@Override
			public Object doInBackground() {
				Object o = TransferUtils.FAILED;
				try {
					observer.init(new ZipFile(source));
					o = ZipUtils.extract(source, destination, TransferUtils.MULTIPLE_FILES | TransferUtils.OVERWRITE, observer);
				} catch (ZipException e) {
					TraceUtils.trace(TraceUtils.STDERR, e);
					BugReportUtils.showBugReportDialog(e);
				} catch (IOException e) {
					TraceUtils.trace(TraceUtils.STDERR, e);
					BugReportUtils.showBugReportDialog(e);
				} catch (InterruptedException e) {
					TraceUtils.trace(TraceUtils.STDERR, e);
					BugReportUtils.showBugReportDialog(e);
				}
				return o;
			}
		};

		try {
			servletProgressBar.setString("Installing...");
			servletProgressBar.setVisible(true);
			servletProgressBar.setIndeterminate(true);
			Thread.sleep(1000);
			servletProgressBar.setIndeterminate(false);
			servletProgressBar.setValue(0);
		} catch (InterruptedException e) {
			TraceUtils.trace(TraceUtils.STDERR, e);
			BugReportUtils.showBugReportDialog(e);
		}
		
		task.addCallback(callback);
		task.execute();
	}
	
	/////////////////////////////////////////////////////////////////////////////////////
	
	public void updateReviewPanel()
	{
		IConfig servletConfig = ConfigManager.getConfigManager().getConfigByName(servletCombo.getSelectedItem());
		IConfig databaseConfig = ConfigManager.getConfigManager().getConfigByName(databaseCombo.getSelectedItem());
		
		try {
			// SERVLET
			if( reviewServletTitleLabel != null )
				reviewServletTitleLabel.setText(servletConfig.getConfigName());
			if( reviewServletImage != null )
				reviewServletImage.setIcon(
						new ImageIcon(ImageUtils.scale(
										servletConfig.getImage(),
										reviewServletImage.getWidth(),
										ImageUtils.SCALE_WIDTH)));
			if( reviewServletPortInput != null )
				reviewServletPortInput.setText("" + servletConfig.getPort());
			if( reviewServletWebappsInput != null ) {
				reviewServletWebappsLabel.setVisible(servletBrowserPath.isVisible());
				reviewServletWebappsInput.setVisible(servletBrowserPath.isVisible());
				reviewServletWebappsInput.setText(servletConfig.getWebappsDirectory().getCanonicalPath());
			}
		
			// DATABASE
			if( reviewDatabaseTitleLabel != null )
				reviewDatabaseTitleLabel.setText(databaseConfig.getConfigName());
			if( reviewDatabaseImage != null )
				reviewDatabaseImage.setIcon(
						new ImageIcon(ImageUtils.scale(
										databaseConfig.getImage(),
										reviewDatabaseImage.getWidth(),
										ImageUtils.SCALE_WIDTH)));
			if( reviewDatabasePortInput != null ) {
				reviewDatabasePortLabel.setVisible(databasePortInput.isVisible());
				reviewDatabasePortInput.setVisible(databasePortInput.isVisible());
				reviewDatabasePortInput.setText("" + databaseConfig.getPort());
			}
		} catch (IOException e) {
			TraceUtils.trace(TraceUtils.STDERR, e);
			BugReportUtils.showBugReportDialog(e);
		}
	}
	
	private void updateServletInfo(IConfig servlet)
	{
		if( servlet == null )
			return;
		
		if( servletImage != null )
			servletImage.setIcon(
					new ImageIcon(ImageUtils.scale(
									servlet.getImage(), 
									servletImage.getWidth(), 
									ImageUtils.SCALE_WIDTH)));
		if( servletDesc != null )
			servletDesc.setText(servlet.getDescription());
		if( servletWarning != null )
			servletWarning.setText(servlet.getWarning());
		if( servletBrowserPath != null )
			servletBrowserPath.setText(
					( servlet.getWebappsDirectory() != null ) ?
							servlet.getWebappsDirectory().getAbsolutePath() :
							"" );
		if( servletPortInput != null )
			servletPortInput.setText(Integer.toString(servlet.getPort()));
		
		boolean visible = !servlet.getConfigName().equals(Jetty.NAME);
		servletWebappsLabel.setVisible(visible);
		servletBrowserPath.setVisible(visible);
		servletBrowserButton.setVisible(visible);
	}
	private void updateDatabaseInfo(IConfig database)
	{
		if( database == null )
			return;
		
		if( databaseImage != null )
			databaseImage.setIcon(
					new ImageIcon(ImageUtils.scale(
									database.getImage(), 
									databaseImage.getWidth(), 
									ImageUtils.SCALE_WIDTH)));
		if( databaseDesc != null )
			databaseDesc.setText(database.getDescription());
		if( databaseWarning != null )
			databaseWarning.setText(database.getWarning());
		if( databasePortInput != null )
			databasePortInput.setText(Integer.toString(database.getPort()));
		
		boolean visible = !database.getConfigName().equals(SQLite.NAME);
		databasePortLabel.setVisible(visible);
		databasePortInput.setVisible(visible);
	}

	/////////////////////////////////////////////////////////////////////////////////////
	
	class ConfigInternals
	{
		IConfig config;
	}
}