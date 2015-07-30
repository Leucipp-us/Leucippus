package plugGUI;

import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.Plot;

import plugComm.*;
import java.awt.FlowLayout;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.image.BufferedImage;

import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.ImageIcon;

public class HistogramWindow extends JFrame {
	private DrawableHandler drawHandler;
	private Communicator comm;
	private MenuBar menuBar;
	private ImageCanvas ic;
	private ImagePlus imp;
	public int[] point;
	public int blocksx;
	public int blocksy;
	public int cellsx;
	public int cellsy;

	private int[] ri;
	private int[] ci;
	private int[] rc;
	private int[] cc;

	public int type;

	private BufferedImage riHist;

	public HistogramWindow () {
		super("Histogram of Point");
		setupMenus();
		blocksx = 3;
		blocksy = 3;
		cellsx = 3;
		cellsy = 3;
	}

	public HistogramWindow (Communicator comm, 
							DrawableHandler drawHandler, 
							int[] arr){
		super("Histogram of Point");
		setupMenus();
		type = 0;

		blocksx = 3;
		blocksy = 3;
		cellsx = 3;
		cellsy = 3;

		point = arr;
		this.comm = comm;
		this.drawHandler = drawHandler;
		this.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});
	}

	public void exit() {
		drawHandler.removeHist(this);
	}

	public int[] getPoint(){
		return point;
	}

	public void callback(ArrayList<BufferedImage> hists){
		riHist = hists.get(0);

		displayRI();
		pack();
		show();
	}

	private void displayRI() {
		getContentPane().removeAll();
		getContentPane().add(new JLabel(new ImageIcon(riHist)));
		
		repaint();
	}

	private void setupMenus(){
		menuBar = new MenuBar();
		Menu t;
		MenuItem mi;

		t = new Menu("Edit");
		mi = new MenuItem("Blocks and Cells");
		mi.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e){
        		createUpdateDialog();
        	}
        });
		t.add(mi);
		menuBar.add(t);

		t = new Menu("Histogram Shape");
		mi = new MenuItem("Rectangular");

		t.add(mi);
		mi = new MenuItem("Circular");

		t.add(mi);
		menuBar.add(t);

		t = new Menu("Image Type");
		mi = new MenuItem("Image Intensity");
		t.add(mi);

		mi = new MenuItem("Image Curvature");
		t.add(mi);

		menuBar.add(t);
		setMenuBar(menuBar);
		getContentPane().setLayout(new FlowLayout());
	}

	private void createUpdateDialog(){
		JTextField tblocksx = new JTextField(""+blocksx);
		JTextField tblocksy = new JTextField(""+blocksy);
		JTextField tcellsx = new JTextField(""+cellsx);
		JTextField tcellsy = new JTextField(""+cellsy);

		final JComponent[] inputs = new JComponent[] {
			new JLabel("Number of Blocks in X direction"),
			tblocksx,
			new JLabel("Number of Blocks in Y direction"),
			tblocksy,
			new JLabel("Number of Pixels per block in X"),
			tcellsx,
			new JLabel("Number of Pixels per block in Y"),
			tcellsy
		};

		int n = JOptionPane.showConfirmDialog(null, inputs, "Update Values", JOptionPane.YES_NO_OPTION);
		if (n == JOptionPane.YES_OPTION) {
			blocksx = Integer.parseInt(tblocksx.getText());
			blocksy = Integer.parseInt(tblocksy.getText());
			cellsx  = Integer.parseInt(tcellsx.getText());
			cellsy  = Integer.parseInt(tcellsy.getText());

			comm.updateHistogram(this);
		}
	}
}