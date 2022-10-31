package BotTools.plugins;

import java.util.List;

import BotTools.main.Botmain;
import BotTools.plugins.reactionroles.*;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Plugins {
	public static Plugin REACTIONROLES = ()->{
		Botmain.createCommand(new AddReactionRole());
		Botmain.createCommand(new CreateMainWelcomeMessage());
		Botmain.responder.addOnMessageReactionAddAction(ReactionRoleActions::giveReactionRole);
		Botmain.responder.addOnMessageDeleteAction(ReactionRoleActions::removeDeletedWelcomeMessage);
	};
	private static boolean isSpam(MessageReceivedEvent event) {
		MessageChannel channel = event.getChannel();
		MessageHistory history = channel.getHistory();
		final int spamThreshold = 10;
		history.retrievePast(spamThreshold).complete();
		List<Message> retrievedMessages = history.getRetrievedHistory();
		if(retrievedMessages.size() > spamThreshold)
			return false;
		for(int i = 1; i < spamThreshold; i++)
			if(!retrievedMessages.get(i).getContentRaw().equals(event.getMessage().getContentRaw()))
				return false;
		return true;
	}
}
