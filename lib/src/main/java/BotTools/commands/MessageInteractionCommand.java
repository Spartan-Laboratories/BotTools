package BotTools.commands;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public abstract class MessageInteractionCommand extends Command{


	protected MessageInteractionCommand(String commandName) {
		super(commandName);
		makeInteractible();
	}
	@Override
	public CommandData getCommandData() {
		return Commands.message(getName());
	}
}
