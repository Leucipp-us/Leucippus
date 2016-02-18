package plugIO;

import plugGUI.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.awt.Color;
import java.util.ArrayList;

public class AnnotationReader {

	/**
	 * Reads in data in an XML file and stores it in DrawableLists
	 * @param filename		the filename of the XML file containing the stored data
	 * @param points			a DrawableList of points for the data to be stored in
	 * @param lines				a DrawableList of lines for the data to be stored in
	 * @param pointsets		a DrawableList of pointsets for the data to be stored in
	 */
	public static void read(String filename,
								DrawableList points,
								DrawableList lines,
								DrawableList pointsets) {

        Document dom;
        // Make an  instance of the DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // use the factory to take an instance of the document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // parse using the builder to get the DOM mapping of the
            // XML file
            dom = db.parse(filename);
            Element doc = dom.getDocumentElement();
            ArrayList<DrawableItem> items = new ArrayList<DrawableItem>();

            NodeList nlines = doc.getElementsByTagName("lines").item(0).getChildNodes();
            for(int i = 0; i < nlines.getLength(); i++) {
            	Node n = nlines.item(i);
            	if (n.getNodeName().equals("line")) {
            		DrawableLine l = readLine(n.getChildNodes());
            		items.add(l);
            	}
            }
            lines.setList(items);

            items = new ArrayList<DrawableItem>();
            NodeList npoints = doc.getElementsByTagName("points").item(0).getChildNodes();
            for(int i = 0; i < npoints.getLength(); i++) {
            	Node n = npoints.item(i);
            	if (n.getNodeName().equals("point")) {
            		DrawablePoint p = readPoint(n.getChildNodes());
            		items.add(p);
            	}
            }
            points.setList(items);


            items = new ArrayList<DrawableItem>();
            NodeList npointsets = doc.getElementsByTagName("pointsets").item(0).getChildNodes();
            for(int i = 0; i < npointsets.getLength(); i++) {
            	Node n = npointsets.item(i);
            	if (n.getNodeName().equals("pointset")) {
            		DrawablePointSet ps = readPointSet(n.getChildNodes());
            		items.add(ps);
            	}
            }
            pointsets.setList(items);



        } catch (ParserConfigurationException pce) {
            System.out.println(pce.getMessage());
        } catch (SAXException se) {
            System.out.println(se.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
    }

    private static DrawableLine readLine(NodeList atts) {
    	String name="", a1="", a2="";
		LineType type = null;
		double sx=0,sy=0,ex=0,ey=0;
		Color color = null;

    	for(int k = 0; k < atts.getLength(); k++) {
			Node item = atts.item(k);

			String t;
			String comp = item.getNodeName();
			if (comp.equals("color")) {
				color = readColor(item);
			} else if (comp.equals("name")) {
				if(item.hasChildNodes()){
					name = item.getFirstChild().getNodeValue();
				}else{
					name = "";
				}
			} else if (comp.equals("type")) {
				type = LineType.valueOf(item.
											getFirstChild().
											getNodeValue());
			} else if (comp.equals("atom1")) {
				a1 = item.getFirstChild().getNodeValue();
			} else if (comp.equals("atom2")) {
				a2 = item.getFirstChild().getNodeValue();
			} else if (comp.equals("startx")) {
				t = item.getFirstChild().getNodeValue();
				sx = Double.parseDouble(t);
			} else if (comp.equals("starty")) {
				t = item.getFirstChild().getNodeValue();
				sy = Double.parseDouble(t);
			} else if (comp.equals("endx")) {
				t = item.getFirstChild().getNodeValue();
				ex = Double.parseDouble(t);
			} else if (comp.equals("endy")) {
				t = item.getFirstChild().getNodeValue();
				ey = Double.parseDouble(t);
			}
		}
		DrawableLine l = new DrawableLine(name, sx, sy, ex, ey,
										  type, a1, a2);
		l.setColor(color);
		return l;
    }

    private static DrawablePoint readPoint(NodeList atts) {
    	double x=0, y=0;
    	String name = "";
    	Color color = null;
    	PointType type = null;

    	for(int k = 0; k < atts.getLength(); k++) {
			Node item = atts.item(k);

			String t;

			String comp = item.getNodeName();
			if (comp.equals("color")) {
				color = readColor(item);
			} else if (comp.equals("name")) {
				if(item.hasChildNodes()){
					name = item.getFirstChild().getNodeValue();
				}else{
					name = "";
				}
			} else if (comp.equals("type")) {
				type = PointType.valueOf(item.
											getFirstChild().
											getNodeValue());
			} else if (comp.equals("x")) {
				t = item.getFirstChild().getNodeValue();
				x = Double.parseDouble(t);
			} else if (comp.equals("y")) {
				t = item.getFirstChild().getNodeValue();
				y = Double.parseDouble(t);
			}
		}
		DrawablePoint p = new DrawablePoint(name, x, y, type);
		p.setColor(color);
    	return p;
    }

    private static DrawablePointSet readPointSet(NodeList atts) {
    	String name = "";
    	Color color = null;
    	ArrayList<int[]> pts = null;
    	ArrayList<ArrayList<int[]>> feats=null, admap=null;

    	for(int k = 0; k < atts.getLength(); k++) {
			Node item = atts.item(k);

			String t;
			String comp = item.getNodeName();
			if (comp.equals("color")) {
				color = readColor(item);
			} else if (comp.equals("name")) {
				if(item.hasChildNodes()){
					name = item.getFirstChild().getNodeValue();
				}else{
					name = "";
				}
			} else if (comp.equals("points")) {
				pts = loadPoints(item.getChildNodes());
			} else if (comp.equals("feats")) {
				feats = loadPointsList(item.getChildNodes(), "feat");
			} else if (comp.equals("admap")) {
				admap = loadPointsList(item.getChildNodes(), "neighbours");
			}
		}
		DrawablePointSet dps = new DrawablePointSet(name, pts, feats, admap);
		dps.setColor(color);
		return dps;
    }

    private static ArrayList<ArrayList<int[]>> loadPointsList(NodeList npl, String keyname){
    	ArrayList<ArrayList<int[]>> pl = new ArrayList<ArrayList<int[]>>();
    	for(int i = 0; i < npl.getLength(); i++) {
    		Node item = npl.item(i);
    		String t;
    		if(item.getNodeName().equals(keyname)) {
				ArrayList<int[]> pts = loadPoints(item.getChildNodes());
				pl.add(pts);
    		}
    	}
    	return pl;
    }

    private static ArrayList<int[]> loadPoints(NodeList npts){
    	ArrayList<int[]> pts = new ArrayList<int[]>();
    	for( int i = 0; i < npts.getLength(); i++){
    		Node item = npts.item(i);
    		String t;
    		if (item.getNodeName().equals("point")) {
				int[] pt = loadPoint(item.getChildNodes());
				pts.add(pt);
    		}
    	}
    	return pts;
    }

    private static int[] loadPoint(NodeList pt){
    	int[] arr = new int[2];

    	for(int k = 0; k < pt.getLength(); k++) {
			Node item = pt.item(k);

			String t;
			String comp = item.getNodeName();
			if (comp.equals("x")) {
				t = item.getFirstChild().getNodeValue();
    			arr[0] = Integer.parseInt(t);
    		} else if (comp.equals("y")) {
    			t = item.getFirstChild().getNodeValue();
    			arr[1] = Integer.parseInt(t);
    		}
		}
		return arr;
    }

    private static Color readColor(Node cn) {
    	int r=0, g=0, b=0;
    	String t;
    	NodeList comps = cn.getChildNodes();
    	for(int i = 0; i < comps.getLength(); i++) {
    		Node c = comps.item(i);

    		String comp = c.getNodeName();
			if (comp.equals("red")) {
				t = c.getFirstChild().getNodeValue();
    			r = Integer.parseInt(t);
			} else if (comp.equals("green")) {
				t = c.getFirstChild().getNodeValue();
    			g = Integer.parseInt(t);
			} else if (comp.equals("blue")) {
				t = c.getFirstChild().getNodeValue();
				b = Integer.parseInt(t);
			}
    	}
    	return new Color(r,g,b);
    }
}
