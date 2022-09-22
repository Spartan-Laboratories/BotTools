package com.bottools.commands;

public class DelegateCommand extends SubCommand {

	public DelegateCommand(Command parent, String name) {
		super(parent, name);
		isSubCommandRequired(true);
	}

	@Override
	public final boolean execute(String[] args) {
		say("Must be followed by a subcommand");
		return false;
	}

}
