package plugGUI;

import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.Plot;

import java.awt.MenuBar;
import java.awt.Menu;
import java.awt.MenuItem;

import java.util.ArrayList;

import javax.swing.JFrame;

public class HistogramWindow extends JFrame {
	private MenuBar menuBar;
	private ImagePlus imp;
	private ImageCanvas ic;
	private Plot plt;

	private int[] ri;
	private int[] ci;
	private int[] rc;
	private int[] cc;

	public HistogramWindow () {
		super("Histogram of Point");
		setupMenus();
		setupPlot();

		pack();
		show();
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

	private void callback(ArrayList<int[]> hist) {
		ri = hist.get(0);
	}

	private void setupPlot(){
		imp = new ImagePlus();
		plt = new Plot("Hist", "Bins", "Intensity");
		plt.setImagePlus(imp);


		plt.draw();
		imp.draw();
		ic = new ImageCanvas(imp);
		add(ic);
	}

	private void drawHistogram(){
		remove(ic);

		//do stuff

		plt.draw();
		imp.draw();
		add(ic);
	}
	
}