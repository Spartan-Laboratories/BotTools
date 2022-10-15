package BotTools.commands;

import java.util.HashMap;

import BotTools.main.SingleChannelListener;
import net.dv8tion.jda.api.entities.Guild;

public abstract class ChannelReservingCommand extends Command {
	private HashMap<Guild, SingleChannelListener> reserved = new HashMap<Guild, SingleChannelListener>();
	
	protected ChannelReservingCommand(String commandName) {
		super(commandName);
		new SCReserve();
		new SCUnreserve();
	}
	@Override
	public abstract boolean execute(String[] args);
	protected void preExecute() {}
	protected class SCReserve extends SubCommand{
		protected SCReserve() {
			super(ChannelReservingCommand.this, "reserve");
			setHelpMessage("Reserves this text channel to be used exclusively by this command.\n"
					+ "After reserving, subcommands can be called directly.\n"
					+ "Example: `/play` instead of`/music play`");
		}
		@Override
		public boolean execute(String[] args) {
			if(reserved.containsKey(guild))
				say("Unable to reserve this channel, a reservation for this command already exists in this server");
			else {
				reserved.put(guild, new SingleChannelListener(getChannel(), ChannelReservingCommand.this)); 
				say("This channel is now reserved for the purposes of the command: " + getName() + "\nYou can now use its sub-commands directly");
			}
			return true;
		}
	}
	protected class SCUnreserve extends SubCommand{
		protected SCUnreserve() {
			super(ChannelReservingCommand.this, "unreserve");
			setHelpMessage("Removes the reservation for this channel");
		}
		@Override
		public boolean execute(String[] args) {
			reserved.get(guild).destroy();
			reserved.remove(guild);
			say("channel is no longer reserved");
			return true;
		}
	}
}
