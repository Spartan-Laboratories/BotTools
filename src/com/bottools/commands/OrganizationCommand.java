package com.bottools.commands;

import java.util.HashMap;

import com.bottools.main.Parser.CommandContainer;

public class OrganizationCommand extends SubCommand {

	private HashMap<String, String> subCommands = new HashMap<String, String>();
	
	public OrganizationCommand(Command parent, String name) {
		super(parent, name);
		setHelpMessage("Can be followed by:");
	}

	@Override
	public boolean handle(CommandContainer commandName) {
		commandName.args[0] = subCommands.get(commandName.args[0]);
		return getParent().handle(commandName);
	}
	
	public OrganizationCommand addCommand(String alias, String command) {
		subCommands.put(alias, command);
		setHelpMessage(getHelpMessage() + " " + command);
		return this;
	}
	public boolean execute(String[] args) {
		return false;
	}
}
