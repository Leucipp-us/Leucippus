package plugGUI;

import java.lang.Math;
import java.util.ArrayList;

public class DrawablePointSet extends DrawableItem {
	private ArrayList<int[]> points;
	private ArrayList<ArrayList<int[]>> features;
	private ArrayList<ArrayList<int[]>> admap;

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

	public ArrayList<int[]> getPoints() {
		return points;
	}

	public ArrayList<ArrayList<int[]>> getFeatures() {
		return features;
	}

	public ArrayList<ArrayList<int[]>> getAdmap() {
		return admap;
	}

	public double distance(int[] p1, int[] p2) {
		int dp1 = (p2[1] - p1[1])*(p2[1] - p1[1]);
		int dp0 = (p2[0] - p1[0])*(p2[0] - p1[0]);
		return Math.sqrt(dp1 + dp0);
	}
}