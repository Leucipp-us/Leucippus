import ij.*;
import ij.gui.Roi;
import ij.gui.Line;
import ij.process.*;
import ij.plugin.frame.*;

import plugGUI.*;
import plugComm.Communicator;
import plugIO.AnnotationWriter;
import plugIO.AnnotationReader;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.image.BufferedImage;
import java.awt.MenuBar;
import java.awt.Menu;
import java.awt.MenuItem;

import java.io.File;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;

import java.util.ArrayList;

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
	private boolean showDetectionHelpers;
	private boolean showbondlengthmessage;
	private boolean showPointsMessage;
	private LatticeInfoPane latticeInfoPane;

	public Plugin_Frame() {
		super("Placeholder");
		showbondlengthmessage = true;
		showPointsMessage = true;
		imp = IJ.getImage();
		setup();
		pack();

		this.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e) {
				comm.exit();
				drawHandler.showOriginalImage();
			}
		});

		

		show();
	}

	public void run(String arg) {
		(new Thread(comm)).start();
	}

	private void setup() {
		setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;

		JButton removeItemButton = new JButton("Remove Item");
		c.gridx = 0;
		c.gridy = 3;
		add(removeItemButton, c);

		
		removeItemButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	if (tabPane.getSelectedIndex()==0)
            		return;

            	JScrollPane scrollPane = (JScrollPane)tabPane.getSelectedComponent();
            	JViewport viewport = scrollPane.getViewport(); 
            	DrawableList tempList = (DrawableList) viewport.getView();
            	int row = tempList.getSelectedRow();
            	if (row != -1) {
            		tempList.removeItem(row);
            	}
            }
        });

        Menu t;
        Menu tt;
        MenuItem mi;
        menuBar = new MenuBar();
        t = new Menu("File");

        mi = new MenuItem("Load Annotations");
        mi.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		String filename = createFileSaveDialog();
        		if (filename != null)
        			AnnotationReader.read(filename,
	        								pointList,
	        								lineList,
	        								pointsetList);
        	}
        });
        t.add(mi);

        mi = new MenuItem("Save Annotations");
        mi.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		String filename = createFileSaveDialog();
        		if (filename != null)
        			AnnotationWriter.write(filename,
	        								pointList,
	        								lineList,
	        								pointsetList);
        	}
        });
        t.add(mi);

        tt = new Menu("Save Detections");
        mi = new MenuItem("as pdb");
        tt.add(mi);
        mi = new MenuItem("as cif");
        tt.add(mi);
        mi = new MenuItem("as xyz");
        tt.add(mi);
        t.add(tt);


        mi = new MenuItem("Exit");
        mi.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		close();
        	}
        });
        t.add(mi);
        menuBar.add(t);

        t = new Menu("Edit");
        tt = new Menu("Points");
        mi = new MenuItem("Merge Points");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                drawHandler.mergeSelected();
            }
        });
        tt.add(mi);
        t.add(tt);
        menuBar.add(t);

        t = new Menu("Insert");
        tt = new Menu("Points");
        mi = new MenuItem("Missing Point");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createPointDialog(PointType.MISSING_ATOM);
            }
        });
        tt.add(mi);

        mi = new MenuItem("Incorrect Point");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createPointDialog(PointType.INCORRECT_ATOM);
            }
        });
        tt.add(mi);
        t.add(tt);

        tt = new Menu("Lines");
        mi = new MenuItem("BondLength");
        mi.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		createLineDialog(LineType.BONDLENGTH);
        	}
        });
        tt.add(mi);
        t.add(tt);
        menuBar.add(t);

        t = new Menu("Analysis");
        mi = new MenuItem("Histogram");
        mi.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e){
        		ArrayList<int[]> sels = drawHandler.getSelections();
        		if (sels.size() != 1){
        			//output to user that only one point can be selected
        			System.out.println(sels.size());
        			return;
        		}
        		//
        		comm.getHistogram(drawHandler.getGrayScaleOriginal(),
        							sels.get(0),
        							new HistogramWindow());
        	}
        });
        t.add(mi);

        mi = new MenuItem("Initial Detections");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	comm.calculatePoints(drawHandler.getGrayScaleOriginal(),
            							null,
            							null);
            	if(showbondlengthmessage) {
            		JOptionPane.showMessageDialog(null, "You can add a bondlength and select Constrain Detection Set to get more accurate results");
            		showbondlengthmessage = false;
            	}
            	
            }
        });
        t.add(mi);
        mi = new MenuItem("Constrain Detection Set");
        mi.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e) {
        		if (lineList.getLength() < 1) {
        			JOptionPane.showMessageDialog(null, "Please Specify a bondlength to use this tool.");
        		}else{
        			comm.calculatePoints(drawHandler.getGrayScaleOriginal(),
            							null,
            							lineList);
        			if(showPointsMessage){
        				JOptionPane.showMessageDialog(null, "For even more accurate results specify which atoms are either incorrect or missing.");
        				showPointsMessage = false;
        			}
        		}
        	}
        });
        t.add(mi);

        mi = new MenuItem("Calculate Using user data");
        t.add(mi);
        menuBar.add(t);
		setMenuBar(menuBar);
		setupLists(c);
	}

	private void setupLists(GridBagConstraints c){
		tabPane = new JTabbedPane();
		pointList = new DrawableList();
		lineList = new DrawableList();
		pointsetList = new DrawableList();
		drawHandler = new DrawableHandler(imp, 
										  pointList, 
										  lineList,
										  pointsetList);
		comm = new Communicator(drawHandler);
		latticeInfoPane = new LatticeInfoPane(comm,
							drawHandler,
							pointList,
							lineList,
							pointsetList);

		tabPane.add("Analysis", new JScrollPane(latticeInfoPane));
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

	private void createPointDialog(PointType pt) {
		Roi roi = imp.getRoi();
		if (roi == null || roi.getType() != 10){
			JOptionPane.showMessageDialog(null, "To add a point you need to use the imagej point select tool to select a point.");
			return;//A popup should come up instead of the return
		}

		JTextField name = new JTextField();
		final JComponent[] inputs = new JComponent[] {
				new JLabel("Name"),
				name
		};
		int n = JOptionPane.showConfirmDialog(null, inputs, "Add Point", JOptionPane.YES_NO_OPTION);
		if (n == JOptionPane.YES_OPTION) {
			if(pt == PointType.INCORRECT_ATOM){
				if (pointsetList.getLength() == 0) {
					JOptionPane.showMessageDialog(null, "You can't select an incorrect point without any pointsets.");
					return;//A popup should come up instead of the return
				}
				int [] sp = {(int)roi.getXBase(), (int)roi.getYBase()};
				for(DrawableItem di :  pointsetList.getList()){
					if(di.isDrawn()){
						boolean breakit = false;
						DrawablePointSet dps = (DrawablePointSet)di;
						for(int[] p : dps.getPoints()){
							double dist = dps.distance(p, sp);
							if (dist < 7.0){
								DrawableItem dPoint = new DrawablePoint(
												name.getText(),
												(double)p[0],
												(double)p[1],
												pt);
								pointList.addItem(dPoint);

								breakit = true;
								break;
							}
						}
						if (breakit) break;
					}
				}
			} else {
				DrawableItem dPoint = new DrawablePoint(
												name.getText(),
												roi.getXBase(),
												roi.getYBase(),
												pt);
			

				pointList.addItem(dPoint);
			}


			
		} 
	}

	private void createLineDialog(LineType lt) {
		Roi roi = imp.getRoi();
		
		if (roi == null || !roi.isLine()) {
			JOptionPane.showMessageDialog(null, "To add a line you need to use the imagej line selection tool to select a line.");
			return;
		}

		Line line = (Line)roi;

		JTextField name = new JTextField();
		JTextField atom1 = new JTextField();
		JTextField atom2 = new JTextField();
		final JComponent[] inputs = new JComponent[] {
			new JLabel("Name"),
			name,
			new JLabel("(For BondLengths)"),
			new JLabel("Atom 1"),
			atom1,
			new JLabel("Atom 2"),
			atom2
		};

		int n = JOptionPane.showConfirmDialog(null, inputs, "Add Line", JOptionPane.YES_NO_OPTION);
		if (n == JOptionPane.YES_OPTION) {
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
		int returnVal = chooser.showSaveDialog(this);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile().getPath();
		}
		return null;
	}
}
