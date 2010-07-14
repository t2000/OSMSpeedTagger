package de.stefantriller.OSMSpeedTagger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLHelper {
	
	public static final Element createXmlElement(Document xml, Element parent,
			String name) {
		Element e = xml.createElement(name);
		try
		{
			parent.appendChild(e);
		}
		catch (Exception ex)
		{
			System.out.println(ex.toString());
		}
		return e;
	}
	
	public static final void setXmlAttributeValue(Document xml, Element elem,
			String name, String value)
	{
		Attr a = xml.createAttribute(name);
		a.setValue(value);
		elem.getAttributes().setNamedItem(a);
	}
	
	public final static Document newDocument(String rootName)
			throws ParserConfigurationException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document xml = db.newDocument();
		Node root = xml.createElement(rootName);
		Attr a = xml.createAttribute("version");
		a.setValue("0.6");
		root.getAttributes().setNamedItem(a);
		
		a = xml.createAttribute("encoding");
		a.setValue("UTF-8");
		root.getAttributes().setNamedItem(a);
		xml.appendChild(root);
		return xml;
	}
	
	public final static String getXml(Node node)
	{
		// http://groups.google.com/group/android-developers/browse_thread/thread/2cc84c1bc8a6b477/5edb01c0721081b0
		StringBuilder buffer = new StringBuilder();

		if (node == null) { return ""; }

		if (node instanceof Document)
		{
			buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			buffer.append(getXml(((Document) node).getDocumentElement()));
		}
		else if (node instanceof Element)
		{
			Element element = (Element) node;
			buffer.append("<");
			buffer.append(element.getNodeName());
			if (element.hasAttributes())
			{
				NamedNodeMap map = element.getAttributes();
				for (int i = 0; i < map.getLength(); i++)
				{
					Node attr = map.item(i);
					buffer.append(" ");
					buffer.append(attr.getNodeName());
					buffer.append("=\"");
					buffer.append(attr.getNodeValue());
					buffer.append("\"");
				}
			}
			buffer.append(">");
			NodeList children = element.getChildNodes();
			for (int i = 0; i < children.getLength(); i++)
			{
				buffer.append(getXml(children.item(i)));
			}
			buffer.append("</");
			buffer.append(element.getNodeName());
			buffer.append(">\n");
		}
		else if (node != null && node.getNodeValue() != null)
		{
			buffer.append(node.getNodeValue());
		}

		return buffer.toString();
	}

}
