import ij.*;
import ij.gui.*;
import ij.process.*;
import ij.plugin.frame.*;

import plugGUI.*;
import plugComm.Communicator;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.image.BufferedImage;
import java.awt.MenuBar;
import java.awt.Menu;

import java.io.File;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.JCheckBox;
import javax.swing.JTabbedPane;



public class Plugin_Frame extends PlugInFrame {
	private ImagePlus imp;
	private DrawableList pointList;
	private DrawableList lineList;
	private DrawableList pointsetList;
	private JTabbedPane tabPane;
	private DrawableHandler drawHandler;
	private Communicator comm;
	private MenuBar menuBar;

	public Plugin_Frame() {
		super("iAnalysis");
		setup();
		imp = IJ.getImage();
		pack();


		drawHandler = new DrawableHandler(imp, 
										  pointList, 
										  lineList,
										  pointsetList);
		this.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e) {
				comm.exit();
				drawHandler.showOriginalImage();
			}
		});

		comm = new Communicator(drawHandler);
		(new Thread(comm)).start();
		show();

	}

	private void setup() {
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;

		JButton selectPointButton = new JButton("Select Point");
		c.gridx = 0;
		c.gridy = 0;
		add(selectPointButton, c);

		JButton selectLineButton = new JButton("Select Line");
		c.gridx = 1;
		c.gridy = 0;
		add(selectLineButton, c);

        JButton recalculatePointsButton = new JButton("Calculate Detections");
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        add(recalculatePointsButton, c);

		JButton removeItemButton = new JButton("Remove Item");
		c.gridx = 0;
		c.gridy = 3;
		add(removeItemButton, c);

		JButton saveAnnotationsFile = new JButton("Save Annotations");
		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 1;
		add(saveAnnotationsFile, c);

		JButton saveImage = new JButton("Save Current Image");
		c.gridx = 1;
		c.gridy = 4;
		add(saveImage, c);

		selectPointButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createPointDialog();
            }
        });

        selectLineButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createLineDialog();
                
            }
        });

		removeItemButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

            	JScrollPane scrollPane = (JScrollPane)tabPane.getSelectedComponent();
            	JViewport viewport = scrollPane.getViewport(); 
            	DrawableList tempList = (DrawableList) viewport.getView();
            	int row = tempList.getSelectedRow();
            	if (row != -1) {
            		tempList.removeItem(row);
            	}
            }
        });

        saveImage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
				String filepath = createFileSaveDialog();
				if (filepath != null) {
					try {
						BufferedImage img = drawHandler.getCurrentImage();
						File outputfile = new File(filepath);
						ImageIO.write(img, "png", outputfile);
					} catch (Exception ex) {
						System.out.println(ex.toString());
					}
                }
            }
        });

        saveAnnotationsFile.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e){
        		String filepath = createFileSaveDialog();
        		if(filepath != null) {
        			BufferedWriter writer = null;
        			try {
        				writer = new BufferedWriter(
        					new OutputStreamWriter(
        						new FileOutputStream(filepath),
        						"utf-8"));

        				writer.write("Points:\n");
        				for(DrawableItem i : pointList.getList()) {
        					writer.write(((DrawablePoint) i).toString());
        					writer.write("\n");
        				}

        				writer.write("Lines:\n");
        				for (DrawableItem i : lineList.getList()) {
        					writer.write(((DrawableLine) i).toString());
        					writer.write("\n");
        				}

        				writer.flush();
        			} catch (Exception ex) {
        				System.out.println(ex.toString());
        			} finally {
					   try {writer.close();} catch (Exception ex) {/*ignore*/}
					}
        		}
        	}
        });

		recalculatePointsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	comm.calculatePoints(drawHandler.getGrayScaleOriginal(),
            							pointList,
            							lineList);
            }
        });

		setupLists(c);
	}

	private void setupLists(GridBagConstraints c){
		tabPane = new JTabbedPane();
		pointList = new DrawableList();
		lineList = new DrawableList();
		pointsetList = new DrawableList();

		tabPane.add("Points", new JScrollPane(pointList));
		tabPane.add("Lines", new JScrollPane(lineList));
		tabPane.add("PointSets", new JScrollPane(pointsetList));

		c.weightx = 1;
		c.weighty = 1;
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.BOTH;
		add(tabPane,c);

	}

	private void createPointDialog() {
		Roi roi = imp.getRoi();
		if (roi == null || roi.getType() != 10)
			return;//A popup should come up instead of the return

		JTextField name = new JTextField();
		JComboBox<PointType> pointType = new JComboBox<PointType>(PointType.values());
		final JComponent[] inputs = new JComponent[] {
				new JLabel("Name"),
				name,
				new JLabel("Type"),
				pointType
		};
		int n = JOptionPane.showConfirmDialog(null, inputs, "Add Point", JOptionPane.YES_NO_OPTION);
		if (n == JOptionPane.YES_OPTION) {
			PointType pt = (PointType)pointType.getSelectedItem();
			DrawableItem dPoint = new DrawablePoint(
												name.getText(),
												roi.getXBase(),
												roi.getYBase(),
												pt);
			

			pointList.addItem(dPoint);
		} 
	}

	private void createLineDialog() {
		Roi roi = imp.getRoi();
		
		if (roi == null || !roi.isLine())
			return;

		Line line = (Line)roi;

		JTextField name = new JTextField();
		JTextField atom1 = new JTextField();
		JTextField atom2 = new JTextField();
		JComboBox<LineType> lineType = new JComboBox<LineType>(LineType.values());
		final JComponent[] inputs = new JComponent[] {
			new JLabel("Name"),
			name,
			new JLabel("Type"),
			lineType,
			new JLabel("(For BondLengths)"),
			new JLabel("Atom 1"),
			atom1,
			new JLabel("Atom 2"),
			atom2
		};

		int n = JOptionPane.showConfirmDialog(null, inputs, "Add Line", JOptionPane.YES_NO_OPTION);
		if (n == JOptionPane.YES_OPTION) {
			LineType lt = (LineType) lineType.getSelectedItem();
			DrawableItem dLine = null;

			if (atom1.getText().length() == 0 || atom2.getText().length() == 0) {
				dLine = new DrawableLine(
										name.getText(),
										line.x1,
										line.y1,
										line.x2,
										line.y2,
										lt);
			} else {
				dLine = new DrawableLine(
										name.getText(),
										line.x1,
										line.y1,
										line.x2,
										line.y2,
										lt,
										atom1.getText(),
										atom2.getText());
			}
			lineList.addItem(dLine);
		}
	}

	private String createFileSaveDialog() {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
		"PNG Images", "png");
		chooser.setFileFilter(filter);
		int returnVal = chooser.showSaveDialog(this);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile().getPath();
		}
		return null;
	}


	private void createPointSetSaveDialot() {

	}
}
