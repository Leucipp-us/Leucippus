package plugComm;

import plugGUI.*;
import org.json.*;

import java.awt.Point;
import java.awt.image.Raster;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import java.lang.Runnable;
import java.lang.ProcessBuilder;
import java.lang.ProcessBuilder.Redirect;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.util.ArrayList;



public class Communicator implements Runnable {
	private Process pyProcess;
	private Boolean quit = false;
	private BufferedReader inStream;
	private BufferedWriter outStream;
	private DrawableHandler drawHandler;
	private HistogramWindow currentWaiting;

	public Communicator(){
		drawHandler = null;
	}

	public Communicator(DrawableHandler drawhand){
		drawHandler = drawhand;
	}

	public void run() {
		setupProcess();
		sendImage();
		JSONObject jmessage = null;
		JSONTokener jsonReader = null;

		try {
			while(!quit) {
				String line = inStream.readLine();
				if (line == null) continue;
				
				jsonReader = new JSONTokener(line);
				jmessage = new JSONObject(jsonReader);

				if(jmessage.get("type").equals("pointsets")){
					parsePointSets(jmessage);
				} else if (jmessage.get("type").equals("histogram")){
					parseHistogram(jmessage);
				}

			}
			System.out.println("exiting");
			inStream.close();
			outStream.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}

	public void exit() {
		try {
			quit = true;
			JSONObject exitmessage = new JSONObject();
			exitmessage.put("type", "EXIT");
			outStream.write(exitmessage.toString() + "\n");
			outStream.flush();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void calculatePoints(BufferedImage image,
								DrawableList points,
								DrawableList lines) {
		JSONObject message = new JSONObject();
		message.put("type", "GET_DETECTIONS");

		byte[] idata = ((DataBufferByte) image.getData().getDataBuffer()).getData();

		JSONObject jsonImage = new JSONObject();
		jsonImage.put("data", idata);
		jsonImage.put("height", image.getHeight());
		jsonImage.put("width", image.getWidth());
		message.put("image", jsonImage);

		message.put("sigma", false);
		message.put("blocksize", false);


		JSONArray pts;
		if (points!=null) {
			pts = new JSONArray();
			for (DrawableItem i : points.getList()) {
				DrawablePoint p = (DrawablePoint) i;
				JSONObject pt = new JSONObject();
				pt.put("type", p.getType().toString());
				pt.put("x",p.getx());
				pt.put("y",p.gety());
				pts.put(pt);
			}
		} else {
			pts = null;
		}
		message.put("points", pts);

		JSONArray lns;
		if (lines != null){
			lns = new JSONArray();
			for (DrawableItem i : lines.getList()) {
				DrawableLine l = (DrawableLine) i;
				JSONObject ln = new JSONObject();

				ln.put("type", l.getType().toString());
				ln.put("atom1", l.getAtom1());
				ln.put("atom2", l.getAtom2());

				double[] ll = new double[]{
					l.getStartX(),
					l.getStartY(),
					l.getEndX(),
					l.getEndY()
				};
				ln.put("data", ll);

				lns.put(ln);
			}
		} else {
			lns = null;
		}
		message.put("lines", lns);


		try {
			outStream.write(message.toString() + "\n");
			outStream.flush();
		} catch (Exception e) {
			System.out.println(e);
		}
		
		// System.out.println(jsonImage);

	}


	public void calculatePoints(BufferedImage image,
								DrawableList points,
								DrawableList lines,
								double sigma,
								double blocksize) {
		JSONObject message = new JSONObject();
		message.put("type", "GET_DETECTIONS");

		byte[] idata = ((DataBufferByte) image.getData().getDataBuffer()).getData();

		JSONObject jsonImage = new JSONObject();
		jsonImage.put("data", idata);
		jsonImage.put("height", image.getHeight());
		jsonImage.put("width", image.getWidth());
		message.put("image", jsonImage);

		message.put("sigma", sigma);
		message.put("blocksize", blocksize);

		JSONArray pts;
		if (points!=null) {
			pts = new JSONArray();
			for (DrawableItem i : points.getList()) {
				DrawablePoint p = (DrawablePoint) i;
				JSONObject pt = new JSONObject();
				pt.put("type", p.getType().toString());
				pt.put("x",p.getx());
				pt.put("y",p.gety());
				pts.put(pt);
			}
		} else {
			pts = null;
		}
		message.put("points", pts);

		JSONArray lns;
		if (lines != null){
			lns = new JSONArray();
			for (DrawableItem i : lines.getList()) {
				DrawableLine l = (DrawableLine) i;
				JSONObject ln = new JSONObject();

				ln.put("type", l.getType().toString());
				ln.put("atom1", l.getAtom1());
				ln.put("atom2", l.getAtom2());

				double[] ll = new double[]{
					l.getStartX(),
					l.getStartY(),
					l.getEndX(),
					l.getEndY()
				};
				ln.put("data", ll);

				lns.put(ln);
			}
		} else {
			lns = null;
		}
		message.put("lines", lns);


		try {
			outStream.write(message.toString() + "\n");
			outStream.flush();
		} catch (Exception e) {
			System.out.println(e);
		}
		
		// System.out.println(jsonImage);

	}

	public void calculatePoints(BufferedImage image,
								DrawableLine line) {
		JSONObject message = new JSONObject();
		message.put("type", "GET_DETECTIONS");

		byte[] idata = ((DataBufferByte) image.getData().getDataBuffer()).getData();

		JSONObject jsonImage = new JSONObject();
		jsonImage.put("data", idata);
		jsonImage.put("height", image.getHeight());
		jsonImage.put("width", image.getWidth());
		message.put("image", jsonImage);

		message.put("sigma", false);
		message.put("blocksize", false);

		JSONArray lns;
		if (line != null){
			lns = new JSONArray();
			JSONObject ln = new JSONObject();
			ln.put("type", line.getType().toString());
			ln.put("atom1", line.getAtom1());
			ln.put("atom2", line.getAtom2());

			double[] ll = new double[]{
				line.getStartX(),
				line.getStartY(),
				line.getEndX(),
				line.getEndY()
			};
			ln.put("data", ll);

			lns.put(ln);
		} else {
			lns = null;
		}
		message.put("lines", lns);
		try {
			outStream.write(message.toString() + "\n");
			outStream.flush();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void getHistogram(BufferedImage image,
								int[] point,
								HistogramWindow histwindow) {
		currentWaiting = histwindow;
		JSONObject message = new JSONObject();
		message.put("type", "GET_HISTOGRAM");

		byte[] idata = ((DataBufferByte) image.getData().getDataBuffer()).getData();

		JSONObject jsonImage = new JSONObject();
		jsonImage.put("data", idata);
		jsonImage.put("height", image.getHeight());
		jsonImage.put("width", image.getWidth());
		message.put("image", jsonImage);

		JSONObject jsonPoint = new JSONObject();
		jsonPoint.put("data", point);
		message.put("point", jsonPoint);


		JSONObject histData = new JSONObject();
		histData.put("blocktype", 'r');
		histData.put("imagetype", 'i');
		histData.put("blocksx", histwindow.blocksx);
		histData.put("blocksy", histwindow.blocksy);
		histData.put("cellsx", histwindow.cellsx);
		histData.put("cellsy", histwindow.cellsy);
		message.put("histdata", histData);

		try {
			outStream.write(message.toString() + "\n");
			outStream.flush();
		} catch (Exception e) {
			System.out.println(e);
		}

		drawHandler.addHist(histwindow);
	}

	public void updateHistogram(HistogramWindow histwindow){
		currentWaiting = histwindow;
		JSONObject message = new JSONObject();
		message.put("type", "UPDATE_HISTOGRAM");

		JSONObject jsonPoint = new JSONObject();
		jsonPoint.put("data", histwindow.point);
		message.put("point", jsonPoint);

		JSONObject histData = new JSONObject();
		histData.put("blocktype", 'r');
		histData.put("imagetype", 'i');
		histData.put("bx", histwindow.blocksx);
		histData.put("by", histwindow.blocksy);
		histData.put("cx", histwindow.cellsx);
		histData.put("cy", histwindow.cellsy);
		message.put("histdata", histData);

		try {
			outStream.write(message.toString() + "\n");
			outStream.flush();
		} catch (Exception e) {
			System.out.println(e);
		}

		drawHandler.addHist(histwindow);
	}

	public void parseHistogram(JSONObject jmessage) {
		ArrayList<BufferedImage> histplots = new ArrayList<BufferedImage>();
		JSONArray jahists = jmessage.getJSONArray("histograms");
		for(int i = 0; i < jahists.length(); i++) {
			JSONArray jaImg = jahists.getJSONArray(i);
			BufferedImage img = parseImage(jaImg);
			histplots.add(img);
		}

		currentWaiting.callback(histplots);
	}

	public BufferedImage parseImage(JSONArray jaImg) {
		int rows = jaImg.length();
		int cols = jaImg.getJSONArray(0).length();
		byte[] idata = new byte[rows*cols*3];
		for(int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				JSONArray pixel = jaImg.getJSONArray(i).getJSONArray(j);
				idata[i*cols*3 + j*3    ] = (byte)pixel.getInt(0);
				idata[i*cols*3 + j*3 + 1] = (byte)pixel.getInt(1);
				idata[i*cols*3 + j*3 + 2] = (byte)pixel.getInt(2);
			}
		}
		BufferedImage img = new BufferedImage(cols,
											 rows,
											 BufferedImage.TYPE_3BYTE_BGR);

		img.setData(
			Raster.createRaster(
				img.getSampleModel(),
				new DataBufferByte(idata, idata.length),
				new Point()));
		return img;
	}

	private void parsePointSets(JSONObject jmessage) {
		String name = "";
		DrawablePointSet dps;
		JSONArray jas = jmessage.getJSONArray("pointsets");

		for (int i = 0; i < jas.length(); i++) {
			JSONObject jo = jas.getJSONObject(i);

			if(!jo.isNull("name")) name = jo.getString("name");
			ArrayList<int[]> pointset = extractIntList(jo, "points");
			ArrayList<int[]> hoislist = extractIntList(jo, "hois");
			ArrayList<ArrayList<int[]>> featset = extractPointLists(jo, "features");
			ArrayList<ArrayList<int[]>> admap = extractPointLists(jo, "admap");

			dps = new DrawablePointSet(name,pointset, featset, admap, hoislist);
			drawHandler.hideAll();
			drawHandler.addPointset(dps);
		}
	}

	private ArrayList<int[]> extractIntList(JSONObject jo, String key){
		if(!jo.isNull(key)){
			ArrayList<int[]> intlist = new ArrayList<int[]>();
			JSONArray ja = jo.getJSONArray(key);

			for(int j = 0; j < ja.length(); j++){

				JSONArray arr = ja.getJSONArray(j);
				int[] in = new int[arr.length()];

				for (int k = 0; k < arr.length(); k++) {
					in[k] = (int) arr.getDouble(k);
				}
				intlist.add(in);
			}
			return intlist;
		}
		return null;
	}

	private ArrayList<ArrayList<int[]>> extractPointLists(JSONObject jo, String key){
		if(!jo.isNull(key)){
			ArrayList<ArrayList<int[]>> pointlist = new ArrayList<ArrayList<int[]>>();
			JSONArray pts = jo.getJSONArray(key);
			for (int i = 0; i < pts.length(); i++) {

				JSONArray arr = pts.getJSONArray(i);
				ArrayList<int[]> list = new ArrayList<int[]>();
				for(int k = 0; k < arr.length(); k++) {
					JSONArray karr = arr.getJSONArray(k);
					int[] in = new int[2];
					in[0] = (int)karr.getDouble(0);
					in[1] = (int)karr.getDouble(1);
					list.add(in);
				}
				pointlist.add(list);
			}
			return pointlist;
		}
		return null;
	}

	private void sendImage() {
		if(drawHandler == null)
			return;

		BufferedImage image = drawHandler.getGrayScaleOriginal();
		byte[] idata = ((DataBufferByte) image.getData().getDataBuffer()).getData();

		JSONObject message = new JSONObject();
		message.put("type", "IMAGE");

		JSONObject jsonImage = new JSONObject();
		jsonImage.put("data", idata);
		jsonImage.put("height", image.getHeight());
		jsonImage.put("width", image.getWidth());
		message.put("image", jsonImage);

		try {
			outStream.write(message.toString() + "\n");
			outStream.flush();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private void setupProcess() {
		ProcessBuilder pb = new ProcessBuilder("python", "/home/david/git/imagejplugin/pycode");
		pb.redirectError(Redirect.INHERIT);
		try {
			pyProcess = pb.start();
			inStream = new BufferedReader(
				new InputStreamReader(
					pyProcess.getInputStream()));
			outStream = new BufferedWriter(
				new OutputStreamWriter(pyProcess.getOutputStream()));
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}
}