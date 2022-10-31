package BotTools.commands;

import BotTools.main.Botmain;

public class HelpCommand extends Command{
	public HelpCommand(){
		super("help");
		addAlias("halp", "halppls");
		makeInteractible()
			.setDescription("See what this bot can do");
	}
	@Override
	public boolean execute(String[] args){
		reply("Hello, I am " + Botmain.jda.getSelfUser().getName());
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
