package BotTools.botactions;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import BotTools.main.Botmain;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
/**
 * The bottom layer wrapper responsible for the simplest bot interactions with discord.
 * Meant to be used only within the project. Although this class' methods are public,
 * typically specific commands should extends the Command
 * class and use its methods instead.
 * 
 * @see Command
 * 
 * @author Spartak
 */
public class BotAction{
	/** The logger that will track BotAction activity
	 * Most of the logged messages should be considered 'trace'
	 */
	private static final Logger log = LoggerFactory.getLogger(BotAction.class);
	/**
	 * Sends the given message in the given MessageChannel
	 * @param channel in which you want to send a message
	 * @param message that you want to send
	 * @return the message that was sent
	 */
	public static Message say(MessageChannel channel, String message){
		checkAndLog(channel, message);
		return channel.sendMessage(message).complete();
	}
	/**
	 * Sends the given file in the given MessageChannel
	 * @param channel in which you want to send a file
	 * @param file that you want to send
	 * @return the message that was sent
	 */
	public static Message sendFileInChannel(MessageChannel channel, File file) {
		channelCheck(channel);
		fileCheck(file);
		log.debug("Sending a file");
		log.trace("File: \'{}\', \tIn channel: []", file.getName(), channel);
		FileUpload fu = FileUpload.fromData(file, file.getName());
		return channel.sendFiles(fu).complete();
	}
	/**
	 * Creates a new message that contains data based on the 
	 * given messages and sends it to the given channel.
	 * Returns the created message
	 * @param channel in which you want to send a message
	 * @param message that you want to send
	 * @return the message that was created and sent
	 */
	public static Message say(MessageChannel channel, Message message) {
		checkAndLog(channel, message.getContentRaw());
		messageCheck(message);
		return channel.sendMessage(MessageCreateData.fromMessage(message)).complete();
	}
	/**
	 * Sends the given message in the given MessageChannel as a 
	 * Text-to-Speech message causing it to be read out loud to
	 * the online members of the guild.
	 * @param channel in which you want to send a message
	 * @param message that you want to be sent
	 * @return the message that was sent
	 */
	public static Message tts(MessageChannel channel, String message) {
		checkAndLog(channel, message);
		return channel.sendMessage(message).setTTS(true).complete();
	}
	private static void channelCheck(MessageChannel channel) {
		if(channel == null) {
			log.warn("An attempt was made to send a message to a null channel");
			throw new IllegalArgumentException("Cannot send message. Channel is null.");
		}
		if(!channel.canTalk()) {
			log.warn("An attempt was made to send a message to the channel: \"{}\", but {} does not have permission to speak in this channel",
					channel.getName(), Botmain.jda.getSelfUser().getName());
			throw new IllegalArgumentException("Cannot send message. Insufficient permissions.");
		}
	}
	private static void fileCheck(File file) {
		if(file == null) {
			log.warn("An attempt was made to send a file that is null");
			throw new IllegalArgumentException("Cannot send a null file");
		}
	}
	private static void messageCheck(Message message) {
		if(message == null) {
			log.warn("An attempt was made to send a message that is null.");
			throw new IllegalArgumentException("cannot send a null message");
		}
	}
	private static void messageLogging(MessageChannel channel, String message) {
		log.debug("Sending a message");
		log.trace("Message: \"{}\", \tIn channel: {}", message, channel);
	}
	private static void checkAndLog(MessageChannel channel, String message) {
		channelCheck(channel);
		messageLogging(channel, message);
	}
}