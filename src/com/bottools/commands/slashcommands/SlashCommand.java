package com.bottools.commands.slashcommands;

import java.util.ArrayList;

import botactions.online.OnlineAction;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public abstract class SlashCommand extends CommandData{
	protected ArrayList<SubCommand> subcommands = new ArrayList<SubCommand>();
	SlashCommandEvent event;
	protected SlashCommand(String name, String description) {
		super(name, description);
		addSubCommands();
		addOptions();
	}
	public String getReply() {
		for(SubCommand c: subcommands)
			if(event.getSubcommandName().equals(c.getName()))
				return c.getReply(event);
		return "default reply";
	}
	public abstract void execute();
	protected abstract void addSubCommands();
	protected abstract void addOptions();
	protected void addSubCommands(SubCommand... subcommands) {
		for(SubCommand c: subcommands)
			addSubCommand(c);
	}
	protected void addSubCommand(SubCommand subcommand) {
		subcommands.add(subcommand);
		addSubcommands(subcommand);
	}
	protected void addOption(String type, String name, String description, boolean required) {
		addOptions(new Option(type, name, description, required));
	}
	protected void addOption(String type, String name, String description) {
		addOption(type, name, description, false);
	}
	public void setEvent(SlashCommandEvent event) {
		this.event = event;
	}
	protected void show(String url) {
		OnlineAction.sendImageInChannel(url, event.getTextChannel());
	}
}
