package plugComm;

import plugGUI.*;
import org.json.*;
import plugComm.Comm2;

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

final class Comm2Helper {
  public static ArrayList<int[]> extractIntList(JSONObject jo, String key){
		if(!jo.isNull(key)){
			ArrayList<int[]> intlist = new ArrayList<int[]>();
			JSONArray ja = jo.getJSONArray(key);

			for(int j = 0; j < ja.length(); j++){

				JSONArray arr = ja.getJSONArray(j);
				int[] in = new int[arr.length()];

				for (int k = 0; k < arr.length(); k++)
					in[k] = (int) arr.getDouble(k);
				intlist.add(in);
			}
			return intlist;
		}
		return null;
	}

  public static ArrayList<ArrayList<int[]>> extractPointLists(JSONObject jo, String key){
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

  public static void setupProcess(Comm2 comm) {
    ProcessBuilder pb = new ProcessBuilder(System.getenv("HOME")+"/.conda/envs/python2/bin/python2", "/home/david/Documents/git/ImageJPlugin/pycode");
    pb.redirectError(Redirect.INHERIT);
    try {
      comm.pyProcess = pb.start();
      comm.inStream = new BufferedReader(
        new InputStreamReader(
          comm.pyProcess.getInputStream()));
      comm.outStream = new BufferedWriter(
        new OutputStreamWriter(comm.pyProcess.getOutputStream()));
        System.out.println(comm.outStream);
    } catch (Exception e) {
      System.out.println(e.toString());
    }
  }

  public static JSONObject prepImageMessage(DrawableHandler drawHandler) {
    if(drawHandler == null)
      return null;

    BufferedImage image = drawHandler.getGrayScaleOriginal();
    byte[] idata = ((DataBufferByte) image.getData().getDataBuffer()).getData();

    JSONObject message = new JSONObject();
    message.put("type", "IMAGE");

    JSONObject jsonImage = new JSONObject();
    jsonImage.put("data", idata);
    jsonImage.put("height", image.getHeight());
    jsonImage.put("width", image.getWidth());
    message.put("image", jsonImage);
    return message;
  }

  public static void parsePointSets(DrawableHandler drawHandler, JSONObject jmessage) {
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

  public static BufferedImage parseImage(JSONArray jaImg) {
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

  public static JSONObject prepPointsMessage(BufferedImage image,
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
    return message;
  }

  public static JSONObject prepPointsMessage(BufferedImage image,
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
    return message;
  }
}
