package com.bottools.main;

import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;


public class Parser {
	static ArrayList<String> triggers = new ArrayList<String>();
	public static CommandContainer parse(String raw) {
		String beheaded = raw;
		for(String trigger: triggers){
			if(raw.startsWith(trigger)){
				beheaded = raw.replaceFirst(trigger, "").toLowerCase();
				break;
			}
		}
		// Turn multiple spaces into single space
		while((beheaded = beheaded.replaceAll("  ", " ")).contains("  "));
		
		// The index of the first whitespace (marking the end of the command name and the start of possible arguments)
		int commandNameEndIndex = beheaded.indexOf(" ");
		String command;
		ArrayList<String> nonTagArgs = new ArrayList<String>();
		if(commandNameEndIndex == -1) {
			command = beheaded;
		}
		else {
			command = beheaded.substring(0, commandNameEndIndex);
			String argsString = beheaded.substring(commandNameEndIndex + 1);
			
			for(String arg: argsString.split(" ")) 
				if(!arg.startsWith("#") && !arg.startsWith("@"))
					nonTagArgs.add(arg);
		}
		
		String[] args = nonTagArgs.toArray(new String[nonTagArgs.size()]);
		return new CommandContainer(raw, beheaded, command, args);
	} 
	public static CommandContainer parse(SlashCommandEvent event) {
		String  commandName	= event.getName(),
				sscgName	= event.getSubcommandGroup(),
				sscName		= event.getSubcommandName();
		List<OptionMapping> options = event.getOptions();
		boolean hasSSCG 	= sscgName != null,
				hasSSC		= sscName  != null,
				hasOptions	= options.size() > 0;
				
		ArrayList<String> args = new ArrayList<String>();
		if(hasSSCG)		args.add(sscgName);
		if(hasSSC)		args.add(sscName);
		if(hasOptions)	options.forEach(option ->{
							args.add(option.getType().toString());
							args.add(option.getAsString());
						});
		
		return new CommandContainer("","",commandName, (String[])args.toArray(new String[args.size()]));
	}
	public static class CommandContainer{
		public String raw;
		public String beheaded;
		public String commandName;
		public String[] args;
		CommandContainer(String raw, String beheaded, String commandName, String[] args) {
			this.raw = raw;
			this.beheaded = beheaded;
			this.commandName = commandName;
			this.args = args == null ? new String[0] : args;
		}
	}
	static boolean startsWithTrigger(String message){
		for(String trigger: triggers)
			if(message.startsWith(trigger))
				return true;
		return false;
	}
	static void addTrigger(String newTrigger){
		triggers.add(newTrigger);
	}
	static void removeTrigger(String trigger){
		triggers.remove(trigger);
	}
	public static String removeMentions(Message message) {
		String command = message.getContentRaw();
		for(Member mentioned: message.getMentionedMembers())
			command = command.replaceAll(mentioned.getAsMention(), "");
		return command;
	}
}
