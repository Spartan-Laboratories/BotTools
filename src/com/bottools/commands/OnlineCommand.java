package com.bottools.commands;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONObject;

import dataprocessing.SourceKeyParser;
import botactions.online.OnlineAction;

public abstract class OnlineCommand extends Command{
	private SourceKeyParser keyParser = new SourceKeyParser();
	protected String primaryAddress;
	protected String data;
	/**
	 * Use this if you plan on having a primary address that you want to be 
	 * opened with the {@link OnlineCommand#open()} function.
	 * @param primaryAddress the primary URL that will be accessed by this command
	 */
	protected OnlineCommand(String name, String primaryAddress) {
		super(name);
		this.primaryAddress = primaryAddress;
	}
	protected OnlineCommand(String name) {
		this(name,"");
	}
	
	protected String getData(){
		try {
			data = OnlineAction.getNextLine();
			return data;
		}catch(IOException e) {
			System.out.println("The online command: " + getName() + " has failed to aquire data");
			e.printStackTrace();
		}
		return null;
	}
	protected void cutToAfter(String searchTerm) {
		data = cutToAfter(data, searchTerm);
	}
	protected String cutToAfter(String data, String searchTerm) {
		return data.substring(data.indexOf(searchTerm) + searchTerm.length());
	}
	
	protected void skipLines(int amount) {
		for(int i = 0; i < amount; i++)
			getData();
	}
	protected void skipLinesTo(String lineStartSearchTerm) throws IOException{
		data = OnlineAction.skipLinesTo(lineStartSearchTerm);
	}
	protected final void skipToContains(String partOfData) throws IOException{
		do {
			getData();
		}while(!data.contains(partOfData));
	}
	
	protected final String getValueFromKey(String key) {
		SourceKeyParser.KeySet keySet = keyParser.getKeys(key);
		
		for(String searchTerm: keySet.initial)
			cutToAfter(searchTerm);
		
		if(!validate(keySet.validation))
			for(String alternate: keySet.alternative) {
				cutToAfter(alternate);
				if(validate(keySet.validation))
					break;
			}
				
		return data.substring(0, data.indexOf(keySet.terminal));
	}
	
	private boolean validate(ArrayList<String> correctEntries) {
		if(correctEntries.size() == 0)
			return true;
		for(String validationString: correctEntries)switch(validationString) {
		case "digit":
			if(Character.isDigit(data.charAt(0)) || data.charAt(0) == '-')
				return true;
			break;
		case "alphabetic":
			if(Character.isAlphabetic(data.charAt(0)))
				return true;
			break;
		default:
			if(data.startsWith(validationString))
				return true;
		}
		return false;
	}
	/**
	 * Opens a new connection with the primary address of this command.
	 * @return whether the connection was successfully established.
	 */
	protected boolean connect() {
		return connect(primaryAddress);
	}
	protected void disconnect() {
		OnlineAction.closeConnection();
	}
	protected boolean connect(String address) {
		return OnlineAction.connect(address);
	}
	protected JSONObject get(String URL) {
		return OnlineAction.get(URL);
	}
	protected JSONObject get() {
		return get(primaryAddress);
	}
}
