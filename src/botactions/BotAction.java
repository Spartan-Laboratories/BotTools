package botactions;

import java.io.File;

import net.dv8tion.jda.api.entities.*;

public class BotAction{
	public static Message say(TextChannel channel, String message){
		return channel.sendMessage(message).complete();
	}
	public static Message sendFileInChannel(File file, TextChannel channel) {
		return channel.sendFile(file).complete();
	}
}