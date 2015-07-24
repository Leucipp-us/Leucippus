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
		// setupPlot();
	}

	// public void callback(ArrayList<int[]> hist) {
	// 	ri = hist.get(0);

	// 	double[] x = new double[ri.length];
	// 	double[] y = new double[ri.length];

	// 	for (int i = 0; i < ri.length; i++) {
	// 		x[i] = i;
	// 		y[i] = (double) ri[i];
	// 	}

	// 	plt.setLineWidth(7);
	// 	plt.setLimits(0,ri.length,0,getYMax(ri)+2);
	// 	plt.addPoints(x, y, Plot.CIRCLE);
	// 	plt.draw();

	// 	drawHistogram();
	// 	show();
	// }

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


	private ArrayList<int[]> parseHists(int[] hists) {
		int[] t;
		ArrayList<int[]> ret = new ArrayList<int[]>();
		for(int i = 0; i < 9; i++){
			t = new int[8];
			for(int j = 0; j < 8; j++) {
				t[j] = hists[i*8 + j];
			}
			ret.add(t);
		}
		return ret;
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
		//do stuff

		plt.draw();
		imp.draw();
	}

	private double getYMax(int[] arr){
		int max = 0;
		for(int i : arr) 
			if (max < i) 
				max = i;
		return (double)max;
	}
	
}