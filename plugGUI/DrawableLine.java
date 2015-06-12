package plugGUI;

import java.lang.StringBuilder;

public class DrawableLine extends DrawableItem {
	private double startx;
	private double starty;
	private double endx;
	private double endy;
	private LineType lineType;
	private String atom1;
	private String atom2;

	public DrawableLine(String n) {
		super(n);
	}

	public DrawableLine (String n,
						 double _startx,
						 double _starty,
						 double _endx,
						 double _endy,
						 LineType _lineType) {
		super(n);
		startx = _startx;
		starty = _starty;
		endx = _endx;
		endy = _endy;
		lineType = _lineType;
		atom1 = null;
		atom2 = null;
	}


	public DrawableLine (String n,
						 double _startx,
						 double _starty,
						 double _endx,
						 double _endy,
						 LineType _lineType,
						 String _atom1,
						 String _atom2) {
		super(n);
		startx = _startx;
		starty = _starty;
		endx = _endx;
		endy = _endy;
		lineType = _lineType;
		atom1 = _atom1;
		atom2 = _atom2;
	}

	public LineType getType() {
		return lineType;
	}

	public double getStartX() {
		return startx;
	}

	public double getStartY() {
		return starty;
	}

	public double getEndX() {
		return endx;
	}

	public double getEndY() {
		return endy;
	}

	public String getAtom1() {
		return atom1;
	}

	public String getAtom2() {
		return atom2;
	}

	@Override
	public String toString() {
		StringBuilder out = new StringBuilder(super.toString());
		out.append(", ");
		out.append(startx);
		out.append(", ");
		out.append(starty);
		out.append(", ");
		out.append(endx);
		out.append(", ");
		out.append(endy);
		out.append(", ");
		out.append(atom1);
		out.append(", ");
		out.append(atom2);
		out.append(", ");
		out.append(lineType);
		return out.toString();
	}
}