package plugGUI;

import java.awt.Color;
import java.lang.Math;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;

public class DrawablePointSet extends DrawableItem {
	private ArrayList<int[]> points;
	private ArrayList<ArrayList<int[]>> features;
	private ArrayList<ArrayList<int[]>> admap;
	private ArrayList<int[]> hois;
	private ArrayList<int[]> cycles;
	private ArrayList<int[]> graphedges;
	private ArrayList<Boolean> drawMask;

	public static Color ringColor(int cycleLength){
		switch(cycleLength){
			case 3:
				return Color.PINK;
			case 4:
				return Color.ORANGE;
			case 5:
				return Color.GREEN;
			case 6:
				return Color.CYAN;
			case 7:
				return Color.BLUE;
			case 8:
				return Color.MAGENTA;
		}
		return Color.RED;
	}

	public DrawablePointSet(String n, ArrayList<int[]> pts) {
		super(n);
		this.points = pts;
		this.features = null;
	}

	public DrawablePointSet(String n,
							ArrayList<int[]> pts,
							ArrayList<ArrayList<int[]>> feats) {
		super(n);
		this.points = pts;
		this.features = feats;
		setupDrawMask();
	}

	public DrawablePointSet(String n,
							ArrayList<int[]> pts,
							ArrayList<ArrayList<int[]>> feats,
							ArrayList<ArrayList<int[]>> admap) {
		super(n);
		this.points = pts;
		this.features = feats;
		this.admap = admap;
		setupDrawMask();
	}

	public DrawablePointSet(String n,
							ArrayList<int[]> pts,
							ArrayList<ArrayList<int[]>> feats,
							ArrayList<ArrayList<int[]>> admap,
							ArrayList<int[]> hois,
							ArrayList<int[]> graphedges,
							ArrayList<int[]> cycles) {
		super(n);
		this.points = pts;
		this.features = feats;
		this.admap = admap;
		this.hois = hois;
		this.graphedges = graphedges;
		this.cycles = cycles;
		setupDrawMask();
	}

	private void setupDrawMask(){
		this.drawMask = new ArrayList<Boolean>(Arrays.asList(new Boolean[this.points.size()]));
		Collections.fill(this.drawMask, Boolean.TRUE);
	}

	public ArrayList<int[]> getPoints() {
		return points;
	}

	public ArrayList<ArrayList<int[]>> getFeatures() {
		return features;
	}

	public ArrayList<ArrayList<int[]>> getAdmap() {
		return admap;
	}

	public ArrayList<int[]> getEdges(){
		return graphedges;
	}

	public ArrayList<int[]> getCycles(){
		return cycles;
	}

	public boolean isPointHidden(int i){
		return (drawMask.get(i) != Boolean.TRUE);
	}

	public double distance(int[] p1, int[] p2) {
		int dp1 = (p2[1] - p1[1])*(p2[1] - p1[1]);
		int dp0 = (p2[0] - p1[0])*(p2[0] - p1[0]);
		return Math.sqrt(dp1 + dp0);
	}

	public void remove(int i){
		drawMask.set(i , false);
	}
}
