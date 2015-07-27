package plugGUI;

import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.Plot;

import java.awt.FlowLayout;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.image.BufferedImage;

import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.ImageIcon;

public class HistogramWindow extends JFrame {
	private MenuBar menuBar;
	private ImagePlus imp;
	private ImageCanvas ic;
	private Plot plt;

	private int[] ri;
	private int[] ci;
	private int[] rc;
	private int[] cc;

	private BufferedImage riHist;

	public HistogramWindow () {
		super("Histogram of Point");
		setupMenus();
	}

	public void callback(ArrayList<BufferedImage> hists){
		riHist = hists.get(0);

		displayRI();
		pack();
		show();
	}

	private void displayRI() {
		getContentPane().setLayout(new FlowLayout());
		getContentPane().add(new JLabel(new ImageIcon(riHist)));
		
		repaint();
	}

	private void setupMenus(){
		menuBar = new MenuBar();
		Menu t;
		MenuItem mi;
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
	}
}