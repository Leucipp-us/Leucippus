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

public class AnnotationWriter {
	public AnnotationWriter(){

	}
	public static void write(String filename, 
								DrawableList points,
								DrawableList lines) {
		Document dom;
	    Element e = null;

	    // instance of a DocumentBuilderFactory
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    try {
	        // use factory to get an instance of document builder
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        // create instance of DOM
	        dom = db.newDocument();

	        // create the root element
	        Element rootEle = dom.createElement("Annotations");

	        // create data elements and place them under root
	        e = dom.createElement("lines");
	        
	        for(DrawableItem i : lines.getList()) {
	        	DrawableLine l = (DrawableLine)i;
	        	e.appendChild(writeLine(dom, l));
	        }

	        rootEle.appendChild(e);

	        e = dom.createElement("points");
	        for(DrawableItem i : points.getList()) {
	        	DrawablePoint p = (DrawablePoint)i;
	        	e.appendChild(writePoint(dom, p));
	        }
	        rootEle.appendChild(e);

	        dom.appendChild(rootEle);

	        try {
	            Transformer tr = TransformerFactory.newInstance().newTransformer();
	            tr.setOutputProperty(OutputKeys.INDENT, "yes");
	            tr.setOutputProperty(OutputKeys.METHOD, "xml");
	            tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	            tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

	            // send DOM to file
	            tr.transform(new DOMSource(dom), 
	                                 new StreamResult(new FileOutputStream(filename)));

	        } catch (TransformerException te) {
	            System.out.println(te.getMessage());
	        } catch (IOException ioe) {
	            System.out.println(ioe.getMessage());
	        }
	    } catch (ParserConfigurationException pce) {
	        System.out.println("UsersXML: Error trying to instantiate DocumentBuilder " + pce);
	    }
	}

	private static Element writePoint(Document dom, DrawablePoint pt) {
		Element e, root = dom.createElement("point");

		e = dom.createElement("name");
		e.appendChild(dom.createTextNode(pt.getName()));
		root.appendChild(e);

		root.appendChild(writeColor(dom, pt.getColor()));

		e = dom.createElement("type");
		e.appendChild(dom.createTextNode(""+pt.getType()));
		root.appendChild(e);

		e = dom.createElement("x");
		e.appendChild(dom.createTextNode(""+pt.getx()));
		root.appendChild(e);

		e = dom.createElement("y");
		e.appendChild(dom.createTextNode(""+pt.gety()));
		root.appendChild(e);
		return root;
	}



	private static Element writeLine(Document dom, DrawableLine ln) {
		Element root = dom.createElement("line");
		Element e;

		e = dom.createElement("name");
		e.appendChild(dom.createTextNode("" + ln.getName()));
		root.appendChild(e);

		root.appendChild(writeColor(dom, ln.getColor()));

		e = dom.createElement("startx");
		e.appendChild(dom.createTextNode("" + ln.getStartX()));
		root.appendChild(e);

		e = dom.createElement("starty");
		e.appendChild(dom.createTextNode("" + ln.getStartY()));
		root.appendChild(e);

		e = dom.createElement("endx");
		e.appendChild(dom.createTextNode("" + ln.getEndX()));
		root.appendChild(e);

		e = dom.createElement("endy");
		e.appendChild(dom.createTextNode("" + ln.getEndY()));
		root.appendChild(e);

		e = dom.createElement("atom1");
		e.appendChild(dom.createTextNode("" + ln.getAtom1()));
		root.appendChild(e);

		e = dom.createElement("atom2");
		e.appendChild(dom.createTextNode("" + ln.getAtom2()));
		root.appendChild(e);

		e = dom.createElement("type");
		e.appendChild(dom.createTextNode(ln.getType().toString()));
		root.appendChild(e);

		return root;
	}

	private static Element writeColor(Document dom, Color c) {
		Element e, root = dom.createElement("color");

		e = dom.createElement("red");
		e.appendChild(dom.createTextNode("" + c.getRed()));
		root.appendChild(e);

		e = dom.createElement("green");
		e.appendChild(dom.createTextNode("" + c.getGreen()));
		root.appendChild(e);

		e = dom.createElement("blue");
		e.appendChild(dom.createTextNode("" + c.getBlue()));
		root.appendChild(e);
		return root;
	}
}