package BotTools.dataprocessing;

import java.util.ArrayList;

import org.w3c.dom.Node;

public class SourceKeyParser {
	public static void main(String[] args) {
		SourceKeyParser finder = new SourceKeyParser();
		KeySet keys = finder.getKeys("duration");
		System.out.println("Initial");
		for(String s: keys.initial)
			System.out.println(s);
		System.out.println("Alternative");
		for(String s: keys.alternative)
			System.out.println(s);
		System.out.printf("Terminal: %s\n", keys.terminal);
	}
	private XMLReader reader = new XMLReader();
	public SourceKeyParser(){
		reader.setDocument("WebParseKeys.xml");
	}
	public KeySet getKeys(String searchedName) {
		Node root = reader.getRoot(); 		// Keys
		Node key = reader.stepDown(root);	// Key
		Node nameNode = reader.stepDown(key);
		try {
			while(true) {
				if(!nameNode.getNodeName().equals("name")) {
					key = reader.stepOver(key);
					nameNode = reader.stepDown(key);
					continue;
				}
				if(compare(nameNode, searchedName)) {
					KeySet keySet = new KeySet();
					Node keyTerm = reader.stepOver(nameNode);
					while(true) {
						switch(keyTerm.getNodeName().toLowerCase()) {
						case "initial": 	keySet.addInitial(reader.getValue(keyTerm)); 		break;
						case "alternate":	keySet.addAlternative(reader.getValue(keyTerm));	break;
						case "validation":	keySet.addValidation(reader.getValue(keyTerm)); 	break;
						case "terminal":	keySet.setTerminal(reader.getValue(keyTerm)); 		break;
						}
						if(keyTerm.getNodeName().equals("terminal"))
							return keySet;
						keyTerm = reader.stepOver(keyTerm);
					}
				}
				else
					nameNode = reader.stepOver(nameNode);
			}
		}catch(Exception e){
			System.out.println("Unable to find web key: " + searchedName);
			e.printStackTrace();
			throw new IllegalArgumentException();
		}
	}
	private boolean compare(Node nameNode, String searchedName) {
		return reader.getValue(nameNode).equals(searchedName);
	}
	public class KeySet{
		public ArrayList<String> initial = new ArrayList<String>(),
								 validation = new ArrayList<String>(),
								 alternative = new ArrayList<String>();
		public String terminal;
		private KeySet() {}
		private void addInitial(String initial) {
			this.initial.add(initial);
		}
		private void addValidation(String validation) {
			this.validation.add(validation);
		}
		private void addAlternative(String alternative) {
			this.alternative.add(alternative);
		}
		private void setTerminal(String terminal) {
			this.terminal = terminal;
		}
	}
	
}
