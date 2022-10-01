package com.bottools.commands.slashcommands;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Option extends OptionData{
	Option(String type, String name, String description){
		this(type, name, description, false);
	}
	public Option(String type, String name, String description, boolean required){
		super(getOptionType(type), name, description, required);
	}
	private static OptionType getOptionType(String type) {
		switch(type) {
		case "user":
			return OptionType.USER;
		case "channel":
			return OptionType.CHANNEL;
		case "role":
			return OptionType.ROLE;
		case "subcommand":
			return OptionType.SUB_COMMAND;
		}
		return OptionType.STRING;
	}
}
