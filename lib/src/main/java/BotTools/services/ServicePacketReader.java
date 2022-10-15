package BotTools.services;

import java.util.ArrayList;

import org.w3c.dom.Node;

import BotTools.dataprocessing.XMLReader;

public class ServicePacketReader extends XMLReader {
	public ServicePacketReader(String packetName) {
		setDocument(packetName);
	}
	public String getURL() {
		return value("url");
	}
	public ArrayList<String> getSkipLineData(){
		ArrayList<String> lines = new ArrayList<String>();
		Node skipLines = getChild(getRoot(), "skiplines");
		getChildren(skipLines).forEach(line ->{
			lines.add(getValue(line));
		});
		return lines;
	}
	public String keyName() {
		return value("keyname");
	}
	public String oldValue() {
		return value("oldvalue");
	}
	private String value(String name) {
		return getValue(getChild(getRoot(), name));
	}
	public void writeValue(String data) {
		setValue(getChild(getRoot(), "oldvalue"), data);
		
	}
	
	@Override
	public void setDocument(String pathName) {
		super.setDocument("services/" + pathName + ".xml");
	}
	
	public static void main(String[] args) {
		ServicePacketReader test = new ServicePacketReader("game services/Dota 2.xml");
		System.out.println(test.getURL());
		test.getSkipLineData().forEach(key->{
			System.out.println(key);
		});
	}
}
