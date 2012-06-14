import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JProgressBar;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JMenu;

@SuppressWarnings("serial")
public class gif2bootGUI extends JFrame {

	private final String VERSION = "0.5.1";
	private JPanel createTabPanel;
	private JPanel contentPane;
	private JTextField filenameField;
	//private String PWD = new File("").getAbsolutePath();
	private static String PWD = ClassLoader.getSystemClassLoader().getResource(".").getPath();
	private String FS = System.getProperty("file.separator");

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		try {
			PWD = URLDecoder.decode(PWD, "UTF-8");
		} catch (UnsupportedEncodingException e1) { e1.printStackTrace(); }
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					gif2bootGUI frame = new gif2bootGUI();
					frame.setVisible(true);
				} catch (Exception e) {	e.printStackTrace(); }
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public gif2bootGUI() {
		
		setTitle("Gif2Boot v" + VERSION);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 600, 535);
		createTabPanel = new JPanel();
		//createTabPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		//setContentPane(createTabPane);
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.addTab("Create Boot Animation", null, createTabPanel, null);
		
		
		//MENU BAR
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
		mnFile.add(mntmExit);
		
		JMenu mnExtras = new JMenu("Extras");
		menuBar.add(mnExtras);
		JMenuItem mntmFlashBootAnimation = new JMenuItem("Flash Existing Boot Animation");
		mnExtras.add(mntmFlashBootAnimation);
		mntmFlashBootAnimation.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	new Thread( new Runnable() { public void run() {
            		flashBootAnimation(PWD+"bootanimation.zip");
            	}
            	}).start();
            }
        });
		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);
		JMenuItem mntmAbout = new JMenuItem("About");
		mntmAbout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(new JFrame(), "Gif2Boot v" + VERSION	+ " is a utility that " +
								"converts any animated GIF\ninto a bootanimation.zip for use on android phones.\n\nThis utility was created by BLuFeNiX.");
            }
        });
		mnHelp.add(mntmAbout);
				
		
		// DONATION LINK
		JLabel lblDonating = new JLabel("If you found this software useful, please consider");
		LinkLabel lblDonating_1 = new LinkLabel("donating.");
		lblDonating_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	String url = "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=ZWD6P3YW4EX9Q";
            	try {
            		java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
				} catch (IOException e1) { e1.printStackTrace(); }
            }
        });
		
		
		
		// LOAD PANEL
		JPanel loadPanel = new JPanel();
		loadPanel.setBorder(new TitledBorder("Load Animated GIF"));
		filenameField = new JTextField();
		filenameField.setColumns(10);
		final JButton btnBrowse = new JButton("Browse");
		btnBrowse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("BROWSE");
                JFileChooser fileChooser = new JFileChooser();
                //fileChooser.setCurrentDirectory(new File(filenameField.getText()));
                System.out.println(PWD);
                fileChooser.setCurrentDirectory(new File(PWD));
                int returnVal = fileChooser.showOpenDialog(new JFrame());
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                	File file = fileChooser.getSelectedFile();
                    filenameField.setText(file.getAbsolutePath());
                }
            }
        });
		
		
		// OPTIONS PANEL
		JPanel optionsPanel = new JPanel();
		optionsPanel.setBorder(new TitledBorder(null, "Options", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		JLabel lblDeviceResolution = new JLabel("Device Resolution:");
		String[] resolutions = { "240x320","240x400","240x432","320x480","480x640","480x800","480x854",
								 "600x1024","640x960","1024x600","1024x768","1280x768","1280x800","1536x1152",
								 "1920x1152","1920x1200","2048x1536","2560x1536","2560x1600" };
		final JComboBox comboBoxResolution = new JComboBox(resolutions);
		comboBoxResolution.setSelectedIndex(3);
		final JCheckBox chckbxZoomFrame = new JCheckBox("zoom frame");
		final JCheckBox chckbxCenterFrame = new JCheckBox("center frame");
		chckbxCenterFrame.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	chckbxZoomFrame.setSelected(false); // use check box as radio button
            }
        });
		chckbxZoomFrame.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	chckbxCenterFrame.setSelected(false); // use check box as radio button
            }
        });
		
		
		
		// PREVIEW PANEL
		JPanel previewPanel = new JPanel();
		previewPanel.setBorder(new TitledBorder(null, "Preview", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		JLabel lblComingSoon = new JLabel("Coming Soon...");
		previewPanel.add(lblComingSoon);
		
		
		// CREATE PANEL
		JPanel createPanel = new JPanel();
		createPanel.setBorder(new TitledBorder(null, "Create Boot Animation", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		final JLabel progressLabel = new JLabel("");
		final JProgressBar progressBar = new JProgressBar();
		final JButton btnCreate = new JButton("Create");
		btnCreate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                new Thread(
                        new Runnable() {
                            public void run() {
                            	
                            	String temp = (String) comboBoxResolution.getSelectedItem();
                                String[] dim = temp.split("x");
                                int x = Integer.parseInt(dim[0]);
                                int y = Integer.parseInt(dim[1]);
                                
                                String options = "";
                                if (chckbxCenterFrame.isSelected()) {
                                	options += "centerFrame,";
                                }
                                else if (chckbxZoomFrame.isSelected()) {
                                	options += "zoomFrame,";
                                }
                                
                                btnBrowse.setEnabled(false);
                                filenameField.setEnabled(false);
                                comboBoxResolution.setEnabled(false);
                                //chckbxCenterFrame.setEnabled(false);
                                btnCreate.setEnabled(false);
                                
                                
                                int status = backend.createBootZip(new File(filenameField.getText()), new Dimension(x, y), options, progressBar, progressLabel, PWD);
                                
                				switch (status) {
                				case 1:
                					JOptionPane.showMessageDialog(new JFrame(), "The specified GIF file was not found, please check the path and try again.");
                					break;
                				case 2:
                					JOptionPane.showMessageDialog(new JFrame(), "No image reader was found. Are you sure you selected an animated GIF?");
                					break;
                				case 3:
                					JOptionPane.showMessageDialog(new JFrame(), "I/O error. Do you have sufficient permissions for the filesystem?");
                					break;
                				default:
                					//JOptionPane.showMessageDialog(new JFrame(), "Done.");
                					int reply = JOptionPane.showConfirmDialog(null, "Done. Transfer boot animation to phone?", "Success", JOptionPane.YES_NO_OPTION);
                			        if (reply == JOptionPane.YES_OPTION) {
                			        	System.out.println("YES, user wants to flash.");
                			        	flashBootAnimation(PWD+FS+"bootanimation.zip");
                			        }
                				}
                				
                				btnBrowse.setEnabled(true);
                                filenameField.setEnabled(true);
                                comboBoxResolution.setEnabled(true);
                                //chckbxCenterFrame.setEnabled(true);
                                btnCreate.setEnabled(true);
                				
                            }

                        }).start();
				
            }
        });
		
		GroupLayout gl_optionsPanel = new GroupLayout(optionsPanel);
		gl_optionsPanel.setHorizontalGroup(
			gl_optionsPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_optionsPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_optionsPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(chckbxZoomFrame)
						.addComponent(chckbxCenterFrame)
						.addGroup(gl_optionsPanel.createSequentialGroup()
							.addComponent(lblDeviceResolution)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(comboBoxResolution, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap(18, Short.MAX_VALUE))
		);
		gl_optionsPanel.setVerticalGroup(
			gl_optionsPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_optionsPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_optionsPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblDeviceResolution)
						.addComponent(comboBoxResolution, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(chckbxCenterFrame)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(chckbxZoomFrame)
					.addContainerGap(125, Short.MAX_VALUE))
		);
		optionsPanel.setLayout(gl_optionsPanel);
			
		GroupLayout gl_createPanel = new GroupLayout(createPanel);
		gl_createPanel.setHorizontalGroup(
			gl_createPanel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_createPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(progressLabel)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(progressBar, GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(btnCreate)
					.addContainerGap())
		);
		gl_createPanel.setVerticalGroup(
			gl_createPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_createPanel.createSequentialGroup()
					.addGroup(gl_createPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(btnCreate, GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE)
						.addGroup(gl_createPanel.createSequentialGroup()
							.addContainerGap()
							.addGroup(gl_createPanel.createParallelGroup(Alignment.TRAILING)
								.addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(progressLabel))))
					.addContainerGap())
		);
		createPanel.setLayout(gl_createPanel);
		
		GroupLayout gl_loadPanel = new GroupLayout(loadPanel);
		gl_loadPanel.setHorizontalGroup(
			gl_loadPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_loadPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(filenameField, GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnBrowse, GroupLayout.PREFERRED_SIZE, 87, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
		);
		gl_loadPanel.setVerticalGroup(
			gl_loadPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_loadPanel.createSequentialGroup()
					.addGroup(gl_loadPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(filenameField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnBrowse))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		loadPanel.setLayout(gl_loadPanel);
		GroupLayout gl_createTabPanel = new GroupLayout(createTabPanel);
		gl_createTabPanel.setHorizontalGroup(
			gl_createTabPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_createTabPanel.createSequentialGroup()
					.addGap(12)
					.addGroup(gl_createTabPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(loadPanel, GroupLayout.DEFAULT_SIZE, 564, Short.MAX_VALUE)
						.addGroup(gl_createTabPanel.createSequentialGroup()
							.addComponent(optionsPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addGap(6)
							.addComponent(previewPanel, GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE))
						.addComponent(createPanel, GroupLayout.DEFAULT_SIZE, 564, Short.MAX_VALUE))
					.addGap(7))
		);
		gl_createTabPanel.setVerticalGroup(
			gl_createTabPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_createTabPanel.createSequentialGroup()
					.addGap(12)
					.addComponent(loadPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(6)
					.addGroup(gl_createTabPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(optionsPanel, GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE)
						.addComponent(previewPanel, GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE))
					.addGap(6)
					.addComponent(createPanel, GroupLayout.PREFERRED_SIZE, 65, GroupLayout.PREFERRED_SIZE)
					.addGap(22))
		);
		createTabPanel.setLayout(gl_createTabPanel);
		
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap(148, Short.MAX_VALUE)
					.addComponent(lblDonating)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblDonating_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
				.addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 588, Short.MAX_VALUE)
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 451, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblDonating_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblDonating)))
		);
		contentPane.setLayout(gl_contentPane);
	}
	
	protected void flashBootAnimation(String path) {
		int result = backend.flashBootAnimation(path);

		if (result == ADBInterface.DEVICE_NOT_FOUND) {
    		JOptionPane.showMessageDialog(new JFrame(), "No Android device detected. Please check the following:\n" +
    				"* Are device drivers installed (Windows only)\n" +
    				"* Is USB Debuging enabled on the device?\n" +
    				"* Is the device connected?");
    	}
		else if (result == ADBInterface.OUTPUT_EMPTY) {
			JOptionPane.showMessageDialog(new JFrame(), "Operation Cancelled. Boot animation not flashed. Insufficient permissions?");
		}
		else if (result == ADBInterface.OS_NOT_SUPPORTED) {
			JOptionPane.showMessageDialog(new JFrame(), System.getProperty("os.name") + " is not currently supported for automated boot animation transfer.");
		}
		else if (result == ADBInterface.FAILED) {
			JOptionPane.showMessageDialog(new JFrame(), "Failed to start ADB daemon. Please close any other instances of ADB and try again.");
		}
		else if (result == ADBInterface.NO_SUCH_FILE) {
			JOptionPane.showMessageDialog(new JFrame(), "bootanimation.zip not found. Did you create one?");		
		}
		else if (result == ADBInterface.DEVICE_OFFLINE) {
			JOptionPane.showMessageDialog(new JFrame(), "Device appears to be connected, but is offline. Please check the following:\n" +
					"* Is USB Debuging enabled on the device?\n" +
					"* Are you connecting to the device in a non-standard way? (emulation, cheap USB hub, etc.)\n" +
    				"* Reboot the device.");			
		}
    	else {
    		JOptionPane.showMessageDialog(new JFrame(), "Boot animation flash appears to be successful. Reeboot and enjoy :)");
    	}								
	}
}
