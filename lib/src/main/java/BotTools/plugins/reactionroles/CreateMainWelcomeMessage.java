package BotTools.plugins.reactionroles;

import java.util.ArrayList;
import java.util.List;

import BotTools.commands.Command;
import BotTools.main.Botmain;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.internal.utils.tuple.Pair;

public class CreateMainWelcomeMessage extends Command {
	public CreateMainWelcomeMessage() {
		super("createmainwelcomemessage");
		makeInteractible();
	}
	
	@Override
	public boolean execute(String[] args) {
		ReactionRoleActions.createWelcomeMessage(getGuild());
		return true;
	}
	
	public boolean executeInChannel(TextChannel channel) {
		setChannel(channel); 
		setGuild(channel.getGuild());
		return execute(new String[0]);
	}
	
	
	static void updateMessages(Guild guild, Emoji emote) {
		List<Message> welcomeMessages = getGuildWelcomeMessages(guild);
		if(welcomeMessages != null)
			welcomeMessages.forEach(message -> message.addReaction(emote).complete());
	}
	
	
	public static List<Message> getGuildWelcomeMessages(Guild guild){
		ArrayList<Message> messages = new ArrayList<Message>();
		
		for(Pair<String, String> id: Botmain.gdp.getWelcomeMessageIDs(guild))try {
			Message message = guild.getTextChannelById(id.getLeft()).retrieveMessageById(id.getRight()).complete();
			messages.add(message);
		}catch(ErrorResponseException e) {
			//log.warn("There are deleted messages still marked as welcome messages in guild data");
		}
		return messages;
	}
}
