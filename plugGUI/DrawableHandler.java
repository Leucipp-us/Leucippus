package plugGUI;

import ij.gui.Roi;
import ij.gui.Line;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.ImagePlus;
import ij.process.ImageProcessor;

import java.util.ArrayList;
import java.util.Collections;
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

	/**
	 * Constructor for the DrawableHandler.
	 * @param imp 		ImagePlus that contains the image. This will also be used
	 *								drawing.
	 * @param pList		DrawableList for points. This is used to monitor changes in
	 *								the list and change accordingly.
	 * @param lList		DrawableList for lines. This is used to monitor changes in
	 *								the list and change accordingly.
	 * @param psList	DrawableList for pointsets. This is used to monitor changes
	 *								in the list and change accordingly.
	 */
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


	/**
	 * Redraws the ROIs highlighting points, lines or pointsets on the ImagePlus
	 * when one of the DrawableLists change.
	 * @param e 		the event that changed the table.
	 */
	public void tableChanged(TableModelEvent e){
		redraw();
	}

	/**
	 * Tells the DrawableHandler to prepare for exit.
	 * Removes itself as a listener from all things it's been added to.
	 */
	public void exit(){
		imageP.setOverlay(null);
		imageP.getCanvas().removeMouseListener(this);
		imageP.getCanvas().removeMouseMotionListener(this);
	}


	private void getXYSelections() {
		ArrayList<DrawableItem> tl = pointsetList.getList();
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
	}

	/**
	 * Determines if there are highlighted selections
	 * @param e			the mouse press event
	 */
	public void mousePressed(MouseEvent e) {
		selections = new ArrayList<int[]>();
		moved_selections = new ArrayList<int[]>();
		Roi roi = imageP.getRoi();

		if(roi != null) {
			if(!isAfterRoiMoved) getSelectedPoints(roi);

			getXYSelections();

			if(roi.getType() == 0) {
				roix = (int)roi.getBounds().getX();
				roiy = (int)roi.getBounds().getY();
				roih = (int)roi.getBounds().getHeight();
				roiw = (int)roi.getBounds().getWidth();
				// redraw();
			} else if(roi.getType() == 10) {
				isAfterRoiMoved = false;
				roix = e.getX();
				roiy = e.getY();
			}
		}
	}

	/**
	 * Returns the current selections on the ImagePlus
	 * @return 		the current set of selections as a list of 2d points.
	 */
	public ArrayList<int[]> getSelections(){
		return selections;
	}

	/**
	 * If the conditions are correct this functions moves selected points in a
	 * pointset.
	 * @param e			the mouse press event
	 */
	public void mouseDragged(MouseEvent e) {
		Roi roi = imageP.getRoi();

		if(roi != null) {
			if(roi.getType() == 0) {

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

	/**
	 * Resets status after dragging points.
	 * @param e			the mouse press event
	 */
	public void mouseReleased(MouseEvent e) {
		Roi roi = imageP.getRoi();
		if(roi != null) {
			redraw();

			if (isRoiMoving) isAfterRoiMoved = true;
			isRoiMoving = false;
			isRoiResizing = false;
			getSelectedPoints(roi);
			getXYSelections();
		}
	}

	public void addPointset(DrawablePointSet d){
		pointsetList.addItem(d);
	}


	/**
	 * Hides all rois on the ImagePlus
	 */
	public void hideAll(){
		for(DrawableItem i : pointList.getList())
			i.isDrawn(false);

		for(DrawableItem i : lineList.getList())
			i.isDrawn(false);

		for(DrawableItem i : pointsetList.getList())
			i.isDrawn(false);
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

	private void redraw() {
		//important to get ROI before the imageP is set
		Roi roi = imageP.getRoi();
		Overlay overlay = new Overlay();

		ArrayList<DrawableItem> tl = pointList.getList();

		for (DrawableItem i : tl) {
			DrawablePoint p = (DrawablePoint) i;
			if (p.isDrawn()) {
				Roi r = new OvalRoi(p.getx()-4, p.gety()-4, 9, 9);
				r.setFillColor(p.getColor());
				overlay.add(r);
			}
		}

		tl = lineList.getList();
		for (DrawableItem i : tl) {
			DrawableLine l = (DrawableLine) i;
			if(l.isDrawn()) {
				Line r = new Line(
							(int)l.getStartX(),
							(int)l.getStartY(),
							(int)l.getEndX(),
							(int)l.getEndY());
				r.setFillColor(l.getColor());
				r.setWidth(5);
				overlay.add(r);
			}
		}

		tl = pointsetList.getList();
		if (roi != null && !isRoiResizing) {
			for (int j = 0; j < indeces.size(); j++) {
				Integer i = indeces.get(j);
				Integer p = psindeces.get(j);

				DrawablePointSet dps = (DrawablePointSet) tl.get(p);
				int[] arr = dps.getPoints().get(i);

				boolean draw = true;
				if(!draw) {
					Roi r = new OvalRoi(arr[0]-4, arr[1]-4, 9, 9);
					r.setFillColor(getComplement(dps.getColor()));
					overlay.add(r);
				}
			}
		}

		for (DrawableItem item : tl) {
			DrawablePointSet ps = (DrawablePointSet) item;
			if(ps.isDrawn()) {
				//Draws Graph Edges
				for(int[] edge : ps.getEdges()){

					if(ps.isPointHidden(edge[0]) || ps.isPointHidden(edge[1])) continue;

					int[] p1 = ps.getPoints().get(edge[0]);
					int[] p2 = ps.getPoints().get(edge[1]);

					Roi r = new Line(p1[0], p1[1], p2[0], p2[1]);
					r.setFillColor(ps.getColor());
					overlay.add(r);
				}

				//Draws cycles
				for(int[] cycle : ps.getCycles()){
					int[] xPoints = new int[cycle.length];
					int[] yPoints = new int[cycle.length];

					boolean draw = true;
					for(int i = 0; i < cycle.length; i++){
						if(ps.isPointHidden(cycle[i])){
							draw=false;
							break;
						}
						if (!draw) continue;
						xPoints[i] = ps.getPoints().get(cycle[i])[0];
						yPoints[i] = ps.getPoints().get(cycle[i])[1];
					}

					Roi r = new PolygonRoi(xPoints, yPoints, cycle.length, Roi.POLYGON);
					Color c = DrawablePointSet.ringColor(cycle.length);
					r.setFillColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 32));
					overlay.add(r);
				}

				//Draws Points
				for(int i = 0; i < ps.getPoints().size(); i++) {
					if(ps.isPointHidden(i)) continue;
					int[] arr  = ps.getPoints().get(i);

					boolean draw = true;

					if (!draw) continue;
					if(roi != null && isRoiResizing){
						if( (roi.getType() == 0 && roi.contains(arr[0],arr[1])) ||
							(roi.getType() == 10 && checkInCircle((int) roi.getXBase(),
																	(int) roi.getYBase(),
																	arr[0],
																	arr[1],
																	7)) ) {
							Roi r = new OvalRoi(arr[0]-4, arr[1]-4, 9, 9);
							r.setFillColor(getComplement(ps.getColor()));
							overlay.add(r);
						}
					}
					Roi r = new OvalRoi(arr[0]-2, arr[1]-2, 5, 5);
					r.setFillColor(ps.getColor());
					overlay.add(r);
				}
			}
		}
		if (roi != null)
			imageP.setRoi(roi);
		imageP.setOverlay(overlay);
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

	public int deleteSelected() {
		Roi roi = imageP.getRoi();

		int count = 0;
		for(DrawableItem i : pointsetList.getList()){
			if (i.isDrawn()) count++;
			if (count == 1) break;
		}

		if (count == 0) return 1;

		if(roi!=null){
			getSelectedPoints(roi);
			for (int j = 0; j < indeces.size(); j++) {
				Integer i = indeces.get(j);
				Integer p = psindeces.get(j);

				((DrawablePointSet) pointsetList.getList().get(p)).remove(i);
			}
		}
		redraw();
		return 0;
	}

	/**
	 * Merges all the selected points currently selected.
	 * @return 				returns zero (I don't know why)
	 */
	public int mergeSelected() {
		Roi roi = imageP.getRoi();

		int count = 0;
		for(DrawableItem i : pointsetList.getList()){
			if (i.isDrawn()) count++;
			if (count == 2) break;
		}

		if (count != 1) return 1;

		if(roi!=null){
			getSelectedPoints(roi);
			moved_selections = new ArrayList<int[]>();
			for (int j = 0; j < indeces.size(); j++) {
				Integer i = indeces.get(j);
				Integer p = psindeces.get(j);

				DrawablePointSet dps = (DrawablePointSet) pointsetList.getList().get(p);
				int[] arr = dps.getPoints().get(i);
				moved_selections.add(arr);
			}


			double xavg = 0, yavg = 0;

			for(int[] p : moved_selections){
				xavg += p[0];
				yavg += p[1];
			}

			xavg /= moved_selections.size();
			yavg /= moved_selections.size();

			int[] np = {(int)xavg, (int)yavg};

			Collections.sort(indeces, Collections.reverseOrder());
			DrawablePointSet dps = (DrawablePointSet)pointsetList.getList().get(0);
			for (int i : indeces){
				dps.getPoints().remove(i);
			}
		}
		getSelectedPoints(roi);
		redraw();
		return 0;
	}

	//needed because java (java's stupid)
	public void mouseExited(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {}
}
