package BotTools.dataprocessing;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import BotTools.main.Botmain;

public class XMLReader {
	private DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	private DocumentBuilder db;
	private Document doc;
	private String docName;
	private Transformer tr;
	private boolean debug;
	/**
	 * Creates the XML reader
	 */
	public XMLReader(){
		//debug = true;
		try {
			db = dbf.newDocumentBuilder();
            tr = TransformerFactory.newInstance().newTransformer();
            tr.setOutputProperty(OutputKeys.INDENT, "yes");
            tr.setOutputProperty(OutputKeys.METHOD, "xml");
            tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		} catch (ParserConfigurationException | TransformerConfigurationException | TransformerFactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * Sets the document that is to be read and written to.
	 * 
	 * @param pathName the name of the document including the file path from the root folder (project folder)
	 */
	public void setDocument(String pathName){
		docName = pathName;
		try {
			db = dbf.newDocumentBuilder();
			doc = db.parse(pathName);
			
		}catch(FileNotFoundException e) {
			Botmain.out("A document with this filepath:\n" + pathName + "\ncould not be found");
		}
		catch (SAXException | IOException | ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public Node getRoot(){
		return doc.getDocumentElement();
	}
	/**
	 * Gets the sub-node of this node
	 * @param node - the parent node
	 * @return the child node
	 */
	public Node stepDown(Node node){
		if(debug)System.out.println("Starting step down function with: " + node);
		Node child = node.getFirstChild();
		if(debug)System.out.println("First child: " + child);
		child = child.getNextSibling();
		if(debug)System.out.println("Returning: " + child);
		return node.getFirstChild().getNextSibling();
	}
	/**
	 * Gets the next element of the same hierarchy level
	 * @param node - the current node
	 * @return the next node
	 */
	public Node stepOver(Node node){
		if(debug)System.out.println("Starting stepOver with: " + node);
		for(int i = 0; i < 2 && node != null; i++){
			try {
				node = node.getNextSibling();
				if(debug)System.out.println(node);
			}catch(NullPointerException e) {
				System.out.println("A StepOver function is returning a null value.");
			}
		}
		if(debug)System.out.println("Finishing StepOver with: " + node);
		return node;
	}
	public Node stepOver(Node node, int times){
		if(times == 0)
			return node;
		return stepOver(stepOver(node), --times);
	}
	/**
	 * Get the node of the hierarchically higher level that contains this node
	 * @param node - the current node
	 * @return the parent node
	 */
	public Node stepOut(Node node){
		return node.getParentNode();
	}
	public String getValue(Node node){
		try {
			return node.getFirstChild().getNodeValue();
		}catch(NullPointerException e) {
			System.out.println(node.getNodeName() + ", " + node.getNodeValue());
			Node child = node.getFirstChild();
			System.out.println(child.getNodeName() + ", " + child.getNodeValue());
		}
		throw new NullPointerException();
	}
	/**
	 * The path string of the document that the reader is currently parsing
	 * @return - The name of the document that is currently in use (the one that was set by {@link #setDocument(String)}
	 */
	public String currentDocument(){
		return docName;
	}
	/**
	 * Writes a new element to the file
	 * @param root - the element that will contain the new element
	 * @param tagName - the name of the tag that is being created
	 * @param content - the text value inside the tag that is being created
	 * @return - The {@link Element} that was just created by this method
	 */
	public Element newChild(Node root, String tagName, String content){
		Element addition = doc.createElement(tagName.replaceAll(" ", ""));
		addition.appendChild(doc.createTextNode(content));
		root.appendChild(addition);
		Node oldRoot = root;
		root.getParentNode().replaceChild(root, oldRoot);
		write();
		return addition;
	}
	public Element newChild(Node root, String tagName){
		return newChild(root, tagName, "");
	}
	public void newChild(Node root, Node addition){
		root.appendChild(addition);
		write();
	}
	public void removeTag(Node root, Node child) {
		root.removeChild(child);
		write();
	}
	public void removeTag(Node root, String child) {
		removeTag(root, getChild(root, child));
	}
	public void removeTagByText(Node root, String textValue) {
		for(Node node: getChildren(root))
			if(getValue(node).equals(textValue))
				removeTag(root, node);
	}
	/*
	public Element create(String tagName){
		return doc.createElement(tagName);
	}
	*/
	public void replaceValue(String newValue, Node... nodes){
		nodes[0].appendChild(doc.createTextNode(newValue));
		for(int i = 1; i < nodes.length; i++){
			nodes[i].appendChild(nodes[i-1]);
		}
	}
	public Node setValue(Node node, String value){
		if(debug)System.out.println("setValue() start node value: " + node.getTextContent());
		node.setTextContent(value);
		if(debug)System.out.println("setValue() end node value: " + node.getTextContent());
		write();
		return node;
	}
	
	public List<Node> getChildren(Node node){
		ArrayList<Node> children = new ArrayList<Node>();
		try {
			node = node.getFirstChild();
			if(node == null) return children;
			node = node.getNextSibling();
			if(node == null) return children;
			do {
				children.add(node);
			}while((node = stepOver(node)) != null);
		}catch(NullPointerException e) {
		}
		return children;
	}
	
	public Node getChild(Node parent, String name) {
		Node child = null;
		name = name.replaceAll(" ", "");
		try {
			if(debug)System.out.println("The parent node is: " + parent.getNodeName());
			if((child = stepDown(parent)) == null)
				System.err.println("node: " + parent + " does not have children");
			if(debug)System.out.println("The first child is: " + child.getNodeName());
		}catch(NullPointerException e) {
			System.err.println("node: " + parent + " does not exist");
			return null;
		}
		try {
			while(!child.getNodeName().toLowerCase().equals(name.toLowerCase())) {
				if(debug)System.out.println("Found child name: " + child.getNodeName());
				child = child.getNextSibling();
			}
		}catch(NullPointerException e) {
			try {
				System.err.println("No child node: " + name + " exists in parent node: " + parent.getNodeName());
			}catch(NullPointerException n) {
				n.printStackTrace();
				System.err.println("Parent node is null");
			}
			return null;
		}
		return child;
	}
	
	private void write(){
		try {
			tr.transform(new DOMSource(doc),  new StreamResult(new FileOutputStream(currentDocument())));
		} catch (FileNotFoundException | TransformerException e) {
			e.printStackTrace();
		}
	}
	
	public boolean nodeHasChild(Node parentNode, String childName) {
		NodeList children = parentNode.getChildNodes();
		for(int i = 0; i < children.getLength(); i++) 
			if(children.item(i).getNodeName().equals(childName))
				return true;
		return false;
	}
}

