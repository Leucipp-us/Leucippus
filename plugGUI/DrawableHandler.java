package plugGUI;

import ij.gui.Roi;
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

public class DrawableHandler implements TableModelListener,
										MouseListener,
										MouseMotionListener {
	private ImagePlus imageP;
	private BufferedImage originalImage;
	private BufferedImage currentImage;
	private DrawableList pointList;
	private DrawableList lineList;
	private DrawableList pointsetList;
	private int x;
	private int y;
	private boolean dontRedraw = false;
	private ArrayList<int[]> selections;
	private ArrayList<int[]> moved_selections;
	private ArrayList<Integer> psindeces;
	private ArrayList<Integer> indeces;
	private ImageProcessor imp;
	private int roix;
	private int roiy;
	private int roih;
	private int roiw;
	private boolean isRoiMoving = false;
	private boolean isRoiResizing = false;
	private boolean isAfterRoiMoved = false;

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

	public void mousePressed(MouseEvent e) {
		ArrayList<DrawableItem> tl = pointsetList.getList();	
		selections = new ArrayList<int[]>();
		moved_selections = new ArrayList<int[]>();
		Roi roi = imageP.getRoi();

		if(roi != null) {
			System.out.println(roi.getState());
			if(!isAfterRoiMoved) getSelectedPoints(roi);

			for (int j = 0; j < indeces.size(); j++) {
				Integer i = indeces.get(j);
				Integer p = psindeces.get(j);

				DrawablePointSet dps = (DrawablePointSet) tl.get(p);
				int[] arr = dps.getPoints().get(i);
				int[] dest = new int[2];
				System.arraycopy(arr, 0, dest, 0, arr.length );
				selections.add(dest);
				moved_selections.add(arr);
			}

			if(roi.getType() == 0) {
				roix = (int)roi.getBounds().getX();
				roiy = (int)roi.getBounds().getY();
				roih = (int)roi.getBounds().getHeight();
				roiw = (int)roi.getBounds().getWidth();
				redraw();
			} else if(roi.getType() == 10) {
				isAfterRoiMoved = false;
				roix = e.getX();
				roiy = e.getY();
			}

		}
	}

	public void mouseDragged(MouseEvent e) {
		Roi roi = imageP.getRoi();

		if(roi != null) {
			if(roi.getType() == 0) {
				// System.out.println(""+roi.getBounds().getX()+", "+
				// 					roi.getBounds().getY());
				 if (roih != (int)roi.getBounds().getHeight() ||
							roiw != (int)roi.getBounds().getWidth()){
				 	//resizing
				 	isRoiResizing = true;
				 	isAfterRoiMoved = false;
				} else if (roix != (int)roi.getBounds().getX() ||
							roiy != (int)roi.getBounds().getY()) {
					//moving
					isRoiMoving = true;
					for(int i = 0; i < moved_selections.size(); i++){
						int[] op = selections.get(i);
						int[] p = moved_selections.get(i);
						p[0] = (int)roi.getBounds().getX() - roix + op[0];
						p[1] = (int)roi.getBounds().getY() - roiy + op[1];
					}
				}

			} else if (roi.getType() == 10) {
				if (roi.getState() == 4) {
					//moving
					double mag = imageP.getCanvas().getMagnification();
					isRoiMoving = true;
					for(int i = 0; i < moved_selections.size(); i++){
						int[] op = selections.get(i);
						int[] p = moved_selections.get(i);
						p[0] = (int)((e.getX() - roix)/mag) + op[0];
						p[1] = (int)((e.getY() - roiy)/mag) + op[1];
					}
				}
			}
			redraw();
		}
	}

	public void mouseReleased(MouseEvent e) {
		Roi roi = imageP.getRoi();
		if(roi != null) {
			redraw();

			if (isRoiMoving) isAfterRoiMoved = true;
			isRoiMoving = false;
			isRoiResizing = false;
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

		Roi roi = imageP.getRoi();

		ArrayList<DrawableItem> tl = pointList.getList();

		imp.setLineWidth(9);
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
		if (roi != null) {
			for (int j = 0; j < indeces.size(); j++) {
				Integer i = indeces.get(j);
				Integer p = psindeces.get(j);

				DrawablePointSet dps = (DrawablePointSet) tl.get(p);
				int[] arr = dps.getPoints().get(i);
				imp.setLineWidth(9);
				imp.setColor(getComplement(dps.getColor()));
				imp.drawDot(arr[0], arr[1]);
			}
		}

		for (DrawableItem item : tl) {
			DrawablePointSet ps = (DrawablePointSet) item;
			if(ps.isDrawn()) {
				for(int i = 0; i < ps.getPoints().size(); i++) {
					int[] arr  = ps.getPoints().get(i);

					if(ps.getFeatures() != null && ps.getAdmap() != null){

						if(checkInCircle(arr[0],arr[1], 7)){
							imp.setLineWidth(1);
							imp.setColor(getComplement(ps.getColor()));
							drawLineSet(arr, ps.getFeatures().get(i));
						}
						imp.setColor(getHalfComplement(ps.getColor()));
						imp.setLineWidth(1);
						drawLineSet(arr, ps.getAdmap().get(i));
					}

					if(roi != null && isRoiResizing){
						if( (roi.getType() == 0 && roi.contains(arr[0],arr[1])) ||
							(roi.getType() == 10 && checkInCircle((int) roi.getXBase(),
																	(int) roi.getYBase(),
																	arr[0],
																	arr[1],
																	7)) ) {
							imp.setLineWidth(9);
							imp.setColor(getComplement(ps.getColor()));
							imp.drawDot(arr[0], arr[1]);
						}
					}

					imp.setLineWidth(5);
					imp.setColor(ps.getColor());
					imp.drawDot(arr[0], arr[1]);
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

	private boolean checkInCircle(int x, int y, int cx, int cy, int r) {
		int xcx = x - cx;
		int ycy = y - cy;
		int inner = xcx*xcx + ycy*ycy;
		double d = Math.sqrt(inner);
		return Math.abs(d) < r;
	}

	private void getSelectedPoints(Roi roi){
		ArrayList<DrawableItem> tl = pointsetList.getList();

		psindeces = new ArrayList<Integer>();
		indeces = new ArrayList<Integer>();
		for (int k = 0; k < tl.size(); k++) {
			DrawableItem item = tl.get(k);
			if(item.isDrawn()){
				DrawablePointSet ps = (DrawablePointSet) item;
				for (int i = 0; i < ps.getPoints().size(); i++){
					int[] p = ps.getPoints().get(i);

					if (roi.getType() == 10 && 
						checkInCircle((int) roi.getXBase(),
										(int) roi.getYBase(),
										p[0],
										p[1],
										7)) {
						indeces.add(i);
						psindeces.add(k);
					}

					else if (roi.getType() == 0 && roi.contains(p[0],p[1]))
					{
						indeces.add(i);
						psindeces.add(k);
					}
				}
			}
		}
	}

	//needed because java (java's stupid)
	public void mouseExited(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {}	
	public void mouseEntered(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}
}