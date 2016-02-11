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


public class Leucippus_ extends PlugInFrame {
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

	public Leucippus_() {
		super("Leucippus");
		showbondlengthmessage = true;
		showPointsMessage = true;
		imp = IJ.getImage();
		setup();
		pack();

		this.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e) {
				comm.exit();
				drawHandler.exit();
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


		c.gridwidth = 2;
		c.gridx = 0; c.gridy = 2;
		c.fill = GridBagConstraints.BOTH;
		add(tabPane,c);
	}

	private void createPointDialog(PointType pt) {
		Roi roi = imp.getRoi();
		if (roi == null || roi.getType() != 10){
			JOptionPane.showMessageDialog(null, "To add a point you need to use the imagej point select tool to select a point.");
			return;
		}

		if(pt == PointType.INCORRECT_ATOM && pointsetList.getLength() == 9){
			JOptionPane.showMessageDialog(this, "You can't select an incorrect point without any pointsets.");
			return;
		}

		JTextField name = new JTextField();
		final JComponent[] inputs = new JComponent[] {
				new JLabel("Name"),
				name
		};

		int n = JOptionPane.showConfirmDialog(this, inputs, "Add Point", JOptionPane.YES_NO_OPTION);
		if (n == JOptionPane.YES_OPTION) {
			if(pt == PointType.INCORRECT_ATOM){
				for(DrawableItem di :  pointsetList.getList()){
					if(di.isDrawn()){
						boolean breakit = false;
						DrawablePointSet dps = (DrawablePointSet)di;
						for(int[] p : dps.getPoints()){
							double dist = dps.distance(p, new int[]{(int)roi.getXBase(),
																											(int)roi.getYBase()});
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
				pointList.addItem(new DrawablePoint(name.getText(),
																						roi.getXBase(),
																						roi.getYBase(),
																						pt));
			}
		}
	}

	private void createLineDialog(LineType lt) {
		Roi roi = imp.getRoi();
		if (roi == null || !roi.isLine()){
			JOptionPane.showMessageDialog(this, "To add a line you need to use the imagej line selection tool to select a line.");
		}else{
			Line line = (Line)roi;
			lineList.addItem(new DrawableLine("Default Bondlength",
																				line.x1, line.y1,
																				line.x2, line.y2,
																				lt));
		}
	}

	private String createFileSaveDialog() {
		JFileChooser chooser = new JFileChooser();
		if(chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
			return chooser.getSelectedFile().getPath();
		return null;
	}
}
