package BotTools.commands.slashcommands;

import java.util.ArrayList;

import BotTools.botactions.online.OnlineAction;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public abstract class SlashCommand implements SlashCommandData{
	protected ArrayList<SubCommand> subcommands = new ArrayList<SubCommand>();
	SlashCommandInteractionEvent event;
	protected SlashCommand(String name, String description) {
		super();
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
		addSubCommand(subcommand);
	}
	protected void addOption(String type, String name, String description, boolean required) {
		;;
	}
	protected void addOption(String type, String name, String description) {
		addOption(type, name, description, false);
	}
	public void setEvent(SlashCommandInteractionEvent event) {
		this.event = event;
	}
	protected void show(String url) {
		OnlineAction.sendImageInChannel(url, event.getMessageChannel());
	}
}
