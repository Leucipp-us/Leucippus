package plugGUI;

import java.awt.Color;
import java.lang.StringBuilder;

public class DrawableItem {
	private Color color;
	private String name;
	private boolean draw;

	public DrawableItem (String n){
		name = n;
		color = Color.RED;
		draw = true;
	}

	public String getName() {
		return name;
	}

	public void setName(String _name) {
		name = _name;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color _color) {
		color = _color;
	}

	public boolean isDrawn() {
		return draw;
	}

	public void isDrawn(boolean _draw) {
		draw = _draw;
	}

	public String toString() {
		StringBuilder out = new StringBuilder();
		out.append(name);
		out.append(", ");
		out.append(draw);
		out.append(", ");
		if (color != null){
			float[] car = color.getRGBComponents(null);
			for(int i = 0; i < 4; i++){
				out.append(car[i]);
				if (i != 3) out.append(", ");
			}
		}else{
			out.append("null");
			out.append(", ");
		}
		return out.toString();
	}
}