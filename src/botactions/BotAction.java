package botactions;

import java.io.File;
import com.bottools.commands.Command;

import net.dv8tion.jda.api.entities.*;
/**
 * The bottom layer wrapper responsible for the bot interacting with discord.
 * Meant to be used only within the project. Although this class' methods are public,
 * typically specific commands should extends the Command
 * class and use its methods instead.
 * 
 * @see Command
 * 
 * @author Spartak
 */
public class BotAction{
	/**
	 * Sends the given message in the given TextChannel
	 * @param channel in which you want to send a message
	 * @param message that you want to send
	 * @return Whether this action was successfully executed or not
	 */
	public static Message say(TextChannel channel, String message){
		return channel.sendMessage(message).complete();
	}
	/**
	 * Sends the given file in the given TextChannel
	 * @param file that you want to send
	 * @param channel in which you want to send a file
	 * @return Whether this action was successfully executed or not
	 */
	public static Message sendFileInChannel(File file, TextChannel channel) {
		return channel.sendFile(file).complete();
	}
}