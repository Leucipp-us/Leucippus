package plugGUI;

import java.awt.Color;
import java.lang.Math;
import java.util.ArrayList;

public class DrawablePointSet extends DrawableItem {
	private ArrayList<int[]> points;
	private ArrayList<ArrayList<int[]>> features;
	private ArrayList<ArrayList<int[]>> admap;
	private ArrayList<int[]> hois;
	private ArrayList<int[]> cycles;
	private ArrayList<int[]> graphedges;

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
	}

	public DrawablePointSet(String n,
							ArrayList<int[]> pts,
							ArrayList<ArrayList<int[]>> feats,
							ArrayList<ArrayList<int[]>> admap) {
		super(n);
		this.points = pts;
		this.features = feats;
		this.admap = admap;
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

	public double distance(int[] p1, int[] p2) {
		int dp1 = (p2[1] - p1[1])*(p2[1] - p1[1]);
		int dp0 = (p2[0] - p1[0])*(p2[0] - p1[0]);
		return Math.sqrt(dp1 + dp0);
	}
}
