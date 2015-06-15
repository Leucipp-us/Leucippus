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
	public AnnotationReader(){}
	public static void read(String filename, 
								DrawableList points,
								DrawableList lines) {

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
            System.out.println(doc.getElementsByTagName("lines").item(0).getNodeName());
            NodeList nlines = doc.getElementsByTagName("lines").item(0).getChildNodes();
            for(int i = 0; i < nlines.getLength(); i++) {
            	Node n = nlines.item(i);
            	if (n.getNodeName().equals("line")) {
            		DrawableLine l = readLine(n.getChildNodes());
            		items.add(l);
            	}
            }
            lines.setList(items);



        } catch (ParserConfigurationException pce) {
            System.out.println(pce.getMessage());
        } catch (SAXException se) {
            System.out.println(se.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
    }

    public static DrawableLine readLine(NodeList atts) {
    	String name="", a1="", a2="";
		LineType type = null;
		double sx=0,sy=0,ex=0,ey=0;
		Color color = null;

    	for(int k = 0; k < atts.getLength(); k++) {
			Node item = atts.item(k);

			String t;
			switch (item.getNodeName()) {
				case "color":
					color = readColor(item);
					break;

				case "name":
					if(item.hasChildNodes()){
						name = item.getFirstChild().getNodeValue();
					}else{
						name = "";
					}
					break;

				case "type":
					type = LineType.valueOf(item.
											getFirstChild().
											getNodeValue());
					break;

				case "atom1":
					a1 = item.getFirstChild().getNodeValue();
					break;

				case "atom2":
					a2 = item.getFirstChild().getNodeValue();
					break;

				case "startx":
					t = item.getFirstChild().getNodeValue();
					sx = Double.parseDouble(t);
					break;

				case "starty":
					t = item.getFirstChild().getNodeValue();
					sy = Double.parseDouble(t);
					break;

				case "endx":
					t = item.getFirstChild().getNodeValue();
					ex = Double.parseDouble(t);
					break;

				case "endy":
					t = item.getFirstChild().getNodeValue();
					ey = Double.parseDouble(t);
					break;
			}
		}
		DrawableLine l = new DrawableLine(name, sx, sy, ex, ey,
										  type, a1, a2);
		l.setColor(color);
		return l;
    }

    public static Color readColor(Node cn) {
    	int r=0, g=0, b=0;
    	String t;
    	NodeList comps = cn.getChildNodes();
    	for(int i = 0; i < comps.getLength(); i++) {
    		Node c = comps.item(i);

    		switch (c.getNodeName()) {
    			case "red":
    				t = c.getFirstChild().getNodeValue();
    				r = Integer.parseInt(t);
    				break;
    			case "green":
    				t = c.getFirstChild().getNodeValue();
    				g = Integer.parseInt(t);
    				break;
    			case"blue":
    				t = c.getFirstChild().getNodeValue();
    				b = Integer.parseInt(t);
    				break;

    		}
    	}
    	return new Color(r,g,b);
    } 
}
