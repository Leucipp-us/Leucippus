package plugGUI;

import java.lang.StringBuilder;

public class DrawablePoint extends DrawableItem {
	private double x;
	private double y;
	private PointType pointType;

	public DrawablePoint(String n) {
		super(n);
	}

	public DrawablePoint (String n, double _x, double _y, PointType _pointType) {
		super(n);
		x = _x;
		y = _y;
		pointType = _pointType;
	}

	public double getx() {
		return x;
	}

	public double gety() {
		return y;
	}

	public PointType getType() {
		return pointType;
	}

	public String toString() {
		StringBuilder out = new StringBuilder(super.toString());
		out.append(", ");
		out.append(x);
		out.append(", ");
		out.append(y);
		out.append(", ");
		out.append(pointType);
		return out.toString();
	}
}