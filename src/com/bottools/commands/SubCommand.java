package com.bottools.commands;

import java.util.ArrayList;

import com.bottools.commands.slashcommands.Option;
import com.bottools.main.Parser.CommandContainer;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

public abstract class SubCommand extends Command {
	int nestLevel;
	private Command parent;
	private SubcommandData subcommandData;
	private SubcommandGroupData subcommandGroupData;
	private ArrayList<Option> options = new ArrayList<Option>();
	protected SubCommand(Command parent, String name) {
		super(name);
		this.parent = parent;
		informParent();
		parent.addSubCommand(name, this);
		String helpMessage = name + " is a sub-command of the " + parent.getName() + " command\n";
		setHelpMessage(helpMessage);
		generateSlashSubcommandData();
	}
	
	protected final void informParent() {
		for(String alias: getAliasList())
			parent.addSubCommand(alias, this);
	}
	
	protected Command getParent() {
		return parent;
	}
	
	protected Command getParent(int steps) {
		return --steps>0?getParent():getParent(steps);
	}
	
	protected Command getParentCommand() {
		return SubCommand.class.isAssignableFrom(parent.getClass()) ? ((SubCommand)parent).getParentCommand() : parent;
	}
	
	@Override
	protected void addAlias(String... aliases) {
		super.addAlias(aliases);
		informParent();
	}
	
	@Override
	public boolean handle(CommandContainer data) {
		messageEvent = parent.messageEvent;
		return super.handle(data);
	}
	
	protected int getNestLevel(Command command) {
		return !SubCommand.class.isAssignableFrom(command.getClass()) ? 1 : getNestLevel(((SubCommand)command).getParent()) + 1;
	}
	
	@Override
	protected final void generateSlashCommandData() {}
	
	void addOrganization(OrganizationCommand org, String suffix) {
		generateSlashSubcommandData(org, suffix);
		subcommandData.addOptions(options);
	}
	
	private void generateSlashSubcommandData() {
		generateSlashSubcommandData(parent, getName());
	}
	
	private void generateSlashSubcommandData(Command command, String name) {
		nestLevel = getNestLevel(command);
		if(nestLevel == 1)
			command.addToSlashCommandData(subcommandGroupData = new SubcommandGroupData(name, getDescription()));
		else if(nestLevel == 2)
			command.addToSlashCommandData(subcommandData = new SubcommandData(name, getDescription()));
		else if(nestLevel > 2)
			command.addToSlashCommandData(getOptionData(name));
		else assert false;
		
	}
	
	@Override
	protected void addToSlashCommandData(SubcommandData subcommandData) {
		subcommandGroupData.addSubcommands(subcommandData);
	}
	
	@Override
	protected void addToSlashCommandData(OptionData optionData) {
		subcommandData.addOptions(optionData);
	}
	
	private OptionData getOptionData(String name) {
		return(new OptionData(OptionType.STRING, name, getDescription()));
	}
	
	
	
	@Override
	protected Member getTargetMember() {
		return getParentCommand().getTargetMember();
	}
	@Override
	protected TextChannel getChannel() {
		return getParentCommand().getChannel();
	}
	@Override 
	protected Guild getGuild() {
		return getParentCommand().getGuild();
	}
	@Override
	protected Command addOption(String type, String name, String description, boolean required) {
		Option o = new Option(type, name, description, required);
		int nestLevel = getNestLevel(getParent());
		switch(nestLevel) {
		case 1:
			options.add(o);
			break;
		case 2:
			subcommandData.addOptions(o);
			break;
		default:
			getParent(nestLevel - 2).addOption(type, name, description, required);
		}
		return this;
	}
	@Override
	protected void reply(String message) {
		getParentCommand().reply(message);
	}
}

