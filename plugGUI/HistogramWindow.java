package plugGUI;

import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.Plot;

import java.awt.GridLayout;
import java.awt.Menu;
import java.awt.MenuBar;
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
	}

	public void callback(ArrayList<int[]> hist) {
		ri = hist.get(0);

		double[] x = new double[ri.length];
		double[] y = new double[ri.length];

		for (int i = 0; i < ri.length; i++) {
			x[i] = i;
			y[i] = (double) ri[i];
		}

		plt.setLineWidth(7);
		plt.setLimits(0,ri.length,0,getYMax(ri)+2);
		plt.addPoints(x, y, Plot.CIRCLE);
		plt.draw();

		drawHistogram();
		show();
	}

	private void displayRI() {
		removeAll();

		ArrayList<int[]> ris = parseHists(ri);
		for (int[] hist : ris) {
			ImagePlus imp = new ImagePlus();
			Plot plt = new Plot("Region Hist", "Bins", "Intensity");
			plt.setImagePlus(imp);

			double[] x = new double[hist.length];
			double[] y = new double[hist.length];

			for (int i = 0; i < hist.length; i++) {
				x[i] = i;
				y[i] = (double) hist[i];
			}
			plt.setLineWidth(7);
			plt.setLimits(0,ri.length,0,getYMax(ri)+2);
			plt.addPoints(x, y, Plot.CIRCLE);
			plt.draw();
			imp.draw();
			ImageCanvas imc = new ImageCanvas(imp);
			add(imc);
			imc.repaint();
		}
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