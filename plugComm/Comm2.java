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

  public static Comm2 getInstance(){
    if (comm == null)
      comm = new Comm2();
    return comm;
  }

  private Comm2(){
    drawHandler = null;
  }

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
		}
  }

  public void run() {
    Comm2Helper.setupProcess(this);
    sendImage();

    try {
			while(!quit) {
				String line = inStream.readLine();
				if (line == null) continue;

        JSONTokener jsonReader = new JSONTokener(line);
				JSONObject jmessage = new JSONObject(jsonReader);

				if(jmessage.get("type").equals("pointsets")){
					Comm2Helper.parsePointSets(drawHandler, jmessage);
				}
			}
			System.out.println("exiting");
			inStream.close();
			outStream.close();
		} catch(Exception e) {
			System.out.println(e);
		}
  }

  public void sendImage(){
    try {
			outStream.write(Comm2Helper.prepImageMessage(drawHandler).toString()+"\n");
			outStream.flush();
		} catch (Exception e) {
			System.out.println(e);
		}
  }

  public void calculatePoints(BufferedImage image,
								DrawableList points,
								DrawableList lines,
								double sigma,
								double blocksize) {
    JSONObject message = Comm2Helper.prepPointsMessage
      (image, points, lines, sigma, blocksize);
    try {
      outStream.write(message.toString() + "\n");
      outStream.flush();
    } catch (Exception e) {
      System.out.println(e);
    }
  }

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
