package plugComm;

import org.json.*;
import plugGUI.*;
import plugComm.Comm2Helper;

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

public class Comm2 implements Runnable {
  private static Comm2 comm;
  private Boolean quit = false;
  private DrawableHandler drawHandler;

  protected Process pyProcess;
  protected BufferedReader inStream;
	protected BufferedWriter outStream;


  /**
   * Returns the current instance of Comm2, if no instance exists one is
   * instantiated.
   * @return    the instance of Comm2
   */
  public static Comm2 getInstance(){
    if (comm == null)
      comm = new Comm2();
    return comm;
  }

  /**
   * The class constructor, this constructor is private so we can use the
   * singleton pattern with this element.
   */
  private Comm2(){
    drawHandler = null;
  }

  /**
   * Sets the drawhandler for the instance of the communicator.
   * The drawhandler makes it easier to update pointsets.
   * @param drawhand  the instance of drawhandler that we'd like to use.
   */
  public void setDrawHandler(DrawableHandler drawhand){
    drawHandler = drawhand;
  }

  public void exit(){
    try {
			quit = true;
			JSONObject exitmessage = new JSONObject();
			exitmessage.put("type", "EXIT");
			outStream.write(exitmessage.toString() + "\n");
			outStream.flush();
		} catch (Exception e) {
			System.out.println(e);
      System.out.println("In exit");
		}
  }

  /**
   * Runs the thread for receiving messages from the python process which it
   * creates. Thread retreives messages and parses them into either point sets
   * or exits the thread if an exit message is received.
   * At the end of this function all member variables must be reset into their
   * state they were in at instantiation.
   */
  public void run() {
    if (pyProcess == null)
      Comm2Helper.setupProcess(this);

    while(!quit){
      try{
        String line = inStream.readLine();
				if (line == null) continue;

				JSONObject jmessage = new JSONObject(new JSONTokener(line));
        if(jmessage.get("type").equals("exit")) break;
				if(jmessage.get("type").equals("pointsets"))
					Comm2Helper.parsePointSets(drawHandler, jmessage);

      }catch(Exception e){
      }
    }

    try {
      quit = false; pyProcess = null;
      inStream.close(); inStream = null;
      outStream.close(); outStream = null;
		} catch(Exception e) {
		}
  }


  /**
   * Send the image in the drawHandler to the python process
   * This function is no longer used.
   */
  public void sendImage(){
    try {
			outStream.write(Comm2Helper.prepImageMessage(drawHandler).toString()+"\n");
			outStream.flush();
		} catch (Exception e) {
		}
  }

  public void automatedPointCalculation(BufferedImage image){
    try {
			outStream.write(Comm2Helper.prepAutoMessage(drawHandler).toString()+"\n");
			outStream.flush();
		} catch (Exception e) {
		}
  }

  /**
   * Sends an image along with any line associated data that can be used to
   * help atomic detection to the python process.
   * This version of the function isn't really used at the moment.
   * @param image     The images that is to undergo analysis
   * @param points    A {@link DrawableList} of points that can parsed and used
   *                  to guide the detection process. (Can be null, Not Used)
   * @param lines     A {@link DrawableLine} that defines the length between two
   *                  bonds (can be null)
   * @param sigma     The sigma that will be passed into gaussian smoothing.
   * @param blocksize The size of the gaussian kernel
   */
  public void calculatePoints(BufferedImage image,
								DrawableList points,
								DrawableLine line,
								double sigma,
								double blocksize) {
    JSONObject message = Comm2Helper.prepPointsMessage
      (image, points, line, sigma, blocksize);
    try {
      outStream.write(message.toString() + "\n");
      outStream.flush();
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  /**
   * Sends data to constrains a pointset based on the bondlength and any other
   * data that is could help guide the detection process.
   *
   * @param image     The images that is to undergo analysis
   * @param points    A {@link DrawableList} of points that can parsed and used
   *                  to guide the detection process. (Can be null, Not Used)
   * @param lines     A {@link DrawableLine} that defines the length between two
   *                  bonds
   * @param pointset  The pointset that is to be contrained.
   * @param sigma     The sigma that will be passed into gaussian smoothing.
   * @param blocksize The size of the gaussian kernel
   */
  public void constrainPoints(BufferedImage image,
								DrawableList points,
								DrawableLine line,
                DrawablePointSet pointset,
								double sigma,
								double blocksize) {
    JSONObject message = Comm2Helper.prepPointSetMessage
      (image, points, line, pointset, sigma, blocksize);
    try{
      outStream.write(message.toString() + "\n");
      outStream.flush();
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  /**
   * Sends and image along with any lines associated data that can be used to
   * help atomic detection to the python process.
   * This version of the function isn't really used at the moment.
   * @param image     The images that is to undergo analysis
   * @param line     A {@link DrawableLine} that defines the length between two
   *                  bonds
   */
  public void calculatePoints(BufferedImage image, DrawableLine line) {
    JSONObject message = Comm2Helper.prepPointsMessage(image, line);
    try {
      outStream.write(message.toString() + "\n");
      outStream.flush();
    } catch (Exception e) {
      System.out.println(e);
    }
  }
}
