package BotTools.main;

import BotTools.commands.Command;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SingleChannelListener extends ListenerAdapter {
	protected MessageChannel channel;
	protected Parser.CommandContainer commandInfo;
	protected Command command;
	private BotListener primaryListener = (BotListener) Botmain.jda.getRegisteredListeners().get(0);
	public SingleChannelListener(MessageChannel channel, Command command){
		this.channel = channel;
		this.command = command;
		primaryListener.ignoreChannel(channel);
		Botmain.jda.addEventListener(this);
	}
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if(!event.getChannel().getId().equals(channel.getId()))
			return;
		String message = event.getMessage().getContentRaw();
		if(Parser.startsWithTrigger(message)) {
			
			// Arguments vary depending on whether the main command name was used or not
			// If it was used then the standard way that argument are retrieved works
			Parser.CommandContainer commandInfo = Parser.parse(message);
			// Otherwise start reading them from the first word
			commandInfo.args = commandInfo.commandName.equals(command.getName()) ? commandInfo.args : commandInfo.beheaded.split(" ");
			
			command.setEvent(event).handle(commandInfo);
		}
	}
	public final void destroy() {
		primaryListener.unignoreChannel(channel);
		Botmain.jda.removeEventListener(this);
	}
}
