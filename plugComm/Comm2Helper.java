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
  /**
   * Extracts a list of integer arrays from a JSONArray within a JSONObject
   * @param   jo    The JSONObject that contains a JSONArray that is to be
   *                that is to be converted
   * @param   key   The key of the JSONArray
   * @return        An ArrayList of integers that contains the extracted data.
   */
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

  /**
   * Extracts a list of points from nested JSONArrays within a JSONObject
   * @param   jo    The JSONObject that contains a JSONArray that is to be
   *                that is to be converted
   * @param   key   The key of the JSONArray
   * @return        An ArrayList of arraylists of integers that contains the
   *                extracted data.
   */
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

  /**
   * Sets up the python process that does all mathematical calulations for the
   * program.
   * @param comm    The existing instance of comm that will communicate with
   *                python.
   */
  public static void setupProcess(Comm2 comm) {
    //This code should probably be altered in such a way that it reads an
    //environment variable and uses that to determine the correct python location
    ProcessBuilder pb = new ProcessBuilder("leupython", System.getenv("HOME")+"/.imagej/plugins/Leuzippy.zip");
    pb.redirectError(Redirect.INHERIT);
    try {
      comm.pyProcess = pb.start();
      comm.inStream = new BufferedReader(
        new InputStreamReader(
          comm.pyProcess.getInputStream()));
      comm.outStream = new BufferedWriter(
        new OutputStreamWriter(comm.pyProcess.getOutputStream()));
    } catch (Exception e) {
      System.out.println(e.toString());
    }
  }

  /**
   * Wraps the image in the DrawableHandler in JSON to send to the python array.
   * @param   drawHandler   the DrawableHandler containing the image.
   * @return                the image wraped in a JSON Object
   */
  public static JSONObject prepImageMessage(DrawableHandler drawHandler) {
    if(drawHandler == null) return null;

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

  /**
   * Parses a pointset received from the python process and adds it to the
   * DrawableHandler.
   * @param drawHandler   The DrawableHandler that is in charge of display
   * @param jmessage      The message received from the python process
   */
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

  /**
   * Parses an Image stored in JSON into a BufferedImage.
   * @param jaImg   the image stored in JSON
   * @return        the converted BufferedImage
   */
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

  /**
   * Prepares an Image and a bondlength to be sent to the python process.
   * @param image   the image to be sent
   * @param line    the bondlength to be sent
   * @return        the json object containing the image and line data
   */
  public static JSONObject prepPointsMessage(BufferedImage image,
                DrawableLine line) {
    JSONObject message = new JSONObject();
    message.put("type", "INITIAL_POINTS");

    byte[] idata = ((DataBufferByte) image.getData().getDataBuffer()).getData();

    JSONObject jsonImage = new JSONObject();
    jsonImage.put("data", idata);
    jsonImage.put("height", image.getHeight());
    jsonImage.put("width", image.getWidth());
    message.put("image", jsonImage);

    message.put("sigma", false);
    message.put("blocksize", false);

    JSONArray lns = null;
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

  /**
   * Prepares an image and any other associated data to be sent to the python
   * process by wrapping it in JSON.
   * @param image     the image to be analysed
   * @param points    specifies any points that are either incorrect or missing
   * @param line      the line that specifies the bondlength in the image.
   * @param sigma     the sigma used in gaussian smoothing
   * @param blocksize the kernel size used in gaussian smoothing
   */
  public static JSONObject prepPointsMessage(BufferedImage image,
                DrawableList points,
                DrawableLine line,
                double sigma,
                double blocksize) {
    JSONObject message = new JSONObject();
    message.put("type", "INITIAL_POINTS");

    byte[] idata = ((DataBufferByte) image.getData().getDataBuffer()).getData();
    JSONObject jsonImage = new JSONObject();
    jsonImage.put("data", idata);
    jsonImage.put("height", image.getHeight());
    jsonImage.put("width", image.getWidth());
    message.put("image", jsonImage);

    message.put("sigma", false);
    message.put("blocksize", false);

    JSONArray pts = null;
    //need to handle missing/incorrect points
    message.put("points", pts);

    JSONArray lns = null;
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
    }
    message.put("lines", lns);
    return message;
  }

  /**
   * Prepares an image and any other associated data to be sent to the python
   * process by wrapping it in JSON.
   * @param image     the image to be analysed
   * @param points    specifies any points that are either incorrect or missing
   * @param line      the line that specifies the bondlength in the image.
   * @param pointset  the pointset that is currently been analysed
   * @param sigma     the sigma used in gaussian smoothing
   * @param blocksize the kernel size used in gaussian smoothing
   */
  public static JSONObject prepPointSetMessage(BufferedImage image,
                DrawableList points,
                DrawableLine line,
                DrawablePointSet pointset,
                double sigma,
                double blocksize) {
    JSONObject message = new JSONObject();
    message.put("type", "CONSTRAIN_POINTS");

    byte[] idata = ((DataBufferByte) image.getData().getDataBuffer()).getData();
    JSONObject jsonImage = new JSONObject();
    jsonImage.put("data", idata);
    jsonImage.put("height", image.getHeight());
    jsonImage.put("width", image.getWidth());
    message.put("image", jsonImage);
    message.put("sigma", sigma);
    message.put("blocksize", blocksize);

    JSONArray pts = null;
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
    }
    message.put("points", pts);

    JSONArray lns = null;
    if (line != null){
      lns = new JSONArray();
      JSONObject ln = new JSONObject();
      ln.put("type", line.getType().toString());
      ln.put("atom1", line.getAtom1());
      ln.put("atom2", line.getAtom2());
      ln.put("data", new double[]{
        line.getStartX(),
        line.getStartY(),
        line.getEndX(),
        line.getEndY()
      });

      lns.put(ln);
    }
    message.put("lines", lns);

    JSONArray ptset = null;
    if (pointset != null){
      ptset = new JSONArray();
      for(int[] arr : ((DrawablePointSet)pointset).getPoints()){
        JSONArray t = new JSONArray();
        t.put(arr[0]);
        t.put(arr[1]);
        ptset.put(t);
      }
    }
    message.put("pointset", ptset);
    return message;
  }
}
