package BotTools.commands.slashcommands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public abstract class SubCommand extends SubcommandData{

	protected SubCommand(String name, String description) {
		super(name, description);
	}
	protected abstract void addOptions();
	protected abstract String getReply(SlashCommandInteractionEvent event);
	protected abstract void execute(SlashCommandInteractionEvent event);
	protected void addOption(String type, String name, String description, boolean required) {
		addOptions(new Option(type, name, description, required));
	}
	protected void addOption(String type, String name, String description) {
		addOption(type, name, description, false);
	}
}
