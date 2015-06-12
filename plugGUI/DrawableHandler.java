package plugGUI;

import ij.ImagePlus;
import ij.process.ImageProcessor;

import java.util.ArrayList;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.ColorModel;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.lang.Math;
import java.lang.Integer;
import java.util.Arrays;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

public class DrawableHandler implements TableModelListener, MouseListener, MouseMotionListener {
	private ImagePlus imageP;
	private BufferedImage originalImage;
	private BufferedImage currentImage;
	private DrawableList pointList;
	private DrawableList lineList;
	private DrawableList pointsetList;
	private int x;
	private int y;
	private boolean dontRedraw = false;
	private ImageProcessor imp;

	public DrawableHandler(ImagePlus imp,
						   DrawableList pList, 
						   DrawableList lList, 
						   DrawableList psList) {
		pointList = pList;
		lineList = lList;
		pointsetList = psList;
		imageP = imp;
		originalImage = deepCopy(imageP.getBufferedImage());

		pList.addListener(this);
		lList.addListener(this);
		psList.addListener(this);
		imp.getCanvas().addMouseListener(this);
		imp.getCanvas().addMouseMotionListener(this);
	}

	public void tableChanged(TableModelEvent e){
		redraw();
	}

	public void mouseDragged(MouseEvent e) {
		dontRedraw = true;
	}

	public void mouseReleased(MouseEvent e) {
		x = e.getX();
		y = e.getY();

		if(!dontRedraw){
			ArrayList<DrawableItem> tl = pointsetList.getList();
			for (DrawableItem item : tl) {
				DrawablePointSet ps = (DrawablePointSet) item;
				if(ps.isDrawn()) {
					if(ps.getFeatures() != null){
						for (int[] arr : ps.getPoints()){
							if(checkInCircle(arr[0],arr[1], 7)){
								redraw();
								break;
							}
						}
						redraw();
					}
				}
			}
		} else {
			dontRedraw = false;
		}
	}

	public void addPointset(DrawablePointSet d){
		pointsetList.addItem(d);
	}

	public BufferedImage getCurrentImage() {
		return imageP.getBufferedImage();
	}

	public BufferedImage getOriginalImage() {
		return originalImage;
	}

	public BufferedImage getGrayScaleOriginal() {
		return grayScale(originalImage);
	}

	public void showOriginalImage() {
		imageP.setImage(originalImage);
	}

	public void redraw() {
		imageP.setImage(originalImage);
		imp = imageP.getProcessor().convertToColorProcessor();
		imageP.setProcessor(imp);

		ArrayList<DrawableItem> tl = pointList.getList();

		imp.setLineWidth(7);
		for (DrawableItem i : tl) {
			DrawablePoint p = (DrawablePoint) i;
			if (p.isDrawn()) {
				imp.setColor(p.getColor());
				imp.drawDot((int)p.getx(), (int)p.gety());
			}
		}

		imp.setLineWidth(5);
		tl = lineList.getList();
		for (DrawableItem i : tl) {
			DrawableLine l = (DrawableLine) i;
			if(l.isDrawn()) {
				imp.setColor(l.getColor());
				imp.drawLine(
							(int)l.getStartX(),
							(int)l.getStartY(),
							(int)l.getEndX(),
							(int)l.getEndY());
			}
		}

		tl = pointsetList.getList();
		for (DrawableItem item : tl) {
			DrawablePointSet ps = (DrawablePointSet) item;
			if(ps.isDrawn()) {

				if(ps.getFeatures() != null && ps.getAdmap() != null){
					for(int i = 0; i < ps.getPoints().size(); i++) {
						int[] arr  = ps.getPoints().get(i);

						if(checkInCircle(arr[0],arr[1], 7)){
							imp.setLineWidth(1);
							imp.setColor(getComplement(ps.getColor()));
							drawLineSet(arr, ps.getFeatures().get(i));
						}

						imp.setColor(getHalfComplement(ps.getColor()));
						imp.setLineWidth(1);
						drawLineSet(arr, ps.getAdmap().get(i));

						imp.setLineWidth(5);
						imp.setColor(ps.getColor());
						imp.drawDot(arr[0], arr[1]);
					}
				} else {
					imp.setColor(ps.getColor());
					for(int[] arr : ps.getPoints()) {
						imp.drawDot(arr[0], arr[1]);
					}
				}
			}
		}
		imageP.repaintWindow();
	}

	private void drawLineSet(int[] cen, ArrayList<int[]> endpoints) {
		for (int[] pts : endpoints) {
			imp.drawLine(cen[0],
						 cen[1],
						 pts[0],
						 pts[1]);
		}
	}

	private static Color getComplement(Color c) {
		float[] hsv = Color.RGBtoHSB(
			c.getRed(),
			c.getGreen(),
			c.getBlue(),
			null);

		hsv[0] += 0.5;
		if (hsv[0] > 1.0) hsv[0] -= 1.0;
		return Color.getHSBColor(hsv[0],hsv[1],hsv[2]);
	}

	private static Color getHalfComplement(Color c) {
		float[] hsv = Color.RGBtoHSB(
			c.getRed(),
			c.getGreen(),
			c.getBlue(),
			null);

		hsv[0] += 0.25;
		if (hsv[0] > 1.0) hsv[0] -= 1.0;
		return Color.getHSBColor(hsv[0],hsv[1],hsv[2]);
	}

	private static BufferedImage grayScale(BufferedImage bi) {
		BufferedImage image = new BufferedImage(bi.getWidth(), bi.getHeight(),  
	    BufferedImage.TYPE_BYTE_GRAY);  
		Graphics g = image.getGraphics();  
		g.drawImage(bi, 0, 0, null);  
		g.dispose();
		return image;
	}

	private static BufferedImage deepCopy(BufferedImage bi) {
		ColorModel cm = bi.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = bi.copyData(null);
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}

	private boolean checkInCircle(int cx, int cy, int r) {
		int xcx = x - cx;
		int ycy = y - cy;
		int inner = xcx*xcx + ycy*ycy;
		double d = Math.sqrt(inner);
		return Math.abs(d) < r;
	}

	//needed because java (java's stupid)
	public void mouseExited(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {}	
	public void mouseEntered(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}
}