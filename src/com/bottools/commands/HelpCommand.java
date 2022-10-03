package com.bottools.commands;

import com.bottools.main.Botmain;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public class HelpCommand extends Command{
	public HelpCommand(){
		super("help");
		addAlias("halp", "halppls");
		makeSlashCommand()
			.setDescription("See what this bot can do");
	}
	@Override
	public boolean execute(String[] args){
		reply("Hello, I am Trump Bot");
		if(args.length == 0)
			executeGenericVersion();
		else
			executeCommandNameVersion(args);
		return true;
	}
	private void executeGenericVersion(){
		eb = eb//.setAuthor("Hello, I am TrumpBot")
				.setTitle("Here is what I can do:");
		createFields();
		sendEmbed();
	}
	private void executeCommandNameVersion(String[] commandAlias){
		if(!Botmain.getCommands().keySet().contains(commandAlias[0])){
			say("No such command exists");
			executeGenericVersion();
			return;
		}
		Botmain.getCommands().get(commandAlias[0]).setEvent(messageEvent).help(getSecondaryArgs(commandAlias)	);
	}
	private void createFields(){
		for(String name: Botmain.commandNames)
			eb = eb.addField("/" + name, Botmain.getCommands().get(name).getHelpMessage(), false);
	}
	
}
