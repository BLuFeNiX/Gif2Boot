import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JProgressBar;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JMenu;

public class gif2bootGUI extends JFrame {

	private final String VERSION = "0.3";
	private JPanel contentPane;
	private JTextField filenameField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
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
		setBounds(100, 100, 600, 500);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		
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
		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);
		JMenuItem mntmAbout = new JMenuItem("About");
		mntmAbout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(new JFrame(), "Gif2Boot v" + VERSION	+ " is a utility that " +
								"converts any animated GIF\ninto a bootanimation.zip for use on android phones.");
            }
        });
		mnHelp.add(mntmAbout);
		JMenuItem mntmFlashBootAnimation = new JMenuItem("Flash Boot Animation");
		mntmFlashBootAnimation.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	new Thread( new Runnable() { public void run() {
            		backend.flashBootAnimation();
            	}
            	}).start();
            }
        });
		mnHelp.add(mntmFlashBootAnimation);
				
		
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
                fileChooser.setCurrentDirectory(new File("."));
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
		final JCheckBox chckbxCenterFrame = new JCheckBox("center frame");
		
		
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
                                boolean isCenterFrame = chckbxCenterFrame.isSelected();
                                
                                btnBrowse.setEnabled(false);
                                filenameField.setEnabled(false);
                                comboBoxResolution.setEnabled(false);
                                chckbxCenterFrame.setEnabled(false);
                                btnCreate.setEnabled(false);
                                
                                
                                int status = backend.createBootZip(new File(filenameField.getText()), new Dimension(x, y), isCenterFrame, progressBar, progressLabel);
                                
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
                			        	backend.flashBootAnimation();
                			        }
                				}
                				
                				btnBrowse.setEnabled(true);
                                filenameField.setEnabled(true);
                                comboBoxResolution.setEnabled(true);
                                chckbxCenterFrame.setEnabled(true);
                                btnCreate.setEnabled(true);
                				
                            }
                        }).start();
				
            }
        });
		
		

		
		
		
		
		
		
		
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
						.addComponent(createPanel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 564, Short.MAX_VALUE)
						.addGroup(Alignment.LEADING, gl_contentPane.createSequentialGroup()
							.addComponent(optionsPanel, GroupLayout.PREFERRED_SIZE, 282, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(previewPanel, GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE))
						.addComponent(loadPanel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 564, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addComponent(loadPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addComponent(optionsPanel, GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE)
						.addComponent(previewPanel, GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(createPanel, GroupLayout.PREFERRED_SIZE, 65, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
		);
		
		GroupLayout gl_optionsPanel = new GroupLayout(optionsPanel);
		gl_optionsPanel.setHorizontalGroup(
			gl_optionsPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_optionsPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_optionsPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_optionsPanel.createSequentialGroup()
							.addComponent(lblDeviceResolution)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(comboBoxResolution, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addComponent(chckbxCenterFrame))
					.addContainerGap(300, Short.MAX_VALUE))
		);
		gl_optionsPanel.setVerticalGroup(
			gl_optionsPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_optionsPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_optionsPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblDeviceResolution)
						.addComponent(comboBoxResolution, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(chckbxCenterFrame)
					.addContainerGap(220, Short.MAX_VALUE))
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
		contentPane.setLayout(gl_contentPane);
	}
}
