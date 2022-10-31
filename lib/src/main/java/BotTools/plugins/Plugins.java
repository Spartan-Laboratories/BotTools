package BotTools.plugins;

import java.util.List;

import BotTools.main.Botmain;
import BotTools.plugins.reactionroles.AddReactionRole;
import BotTools.plugins.reactionroles.CreateMainWelcomeMessage;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public class Plugins {
	public static Plugin REACTIONROLES = ()->{
		Botmain.createCommand(new AddReactionRole());
		Botmain.createCommand(new CreateMainWelcomeMessage());
		Botmain.responder.addOnMessageReactionAddAction(Plugins::giveReactionRole);
		Botmain.responder.addOnMessageDeleteAction(Plugins::removeDeletedWelcomeMessage);
	};
	public static void giveReactionRole(MessageReactionAddEvent event) {
		String roleid = Botmain.gdp.getCorrespondingRoleID(event.getGuild(), event.getEmoji());
		Role role = Botmain.jda.getRoleById(roleid);
		event.getGuild().addRoleToMember(event.getMember().getUser(), role);
	}
	public static void removeDeletedWelcomeMessage(MessageDeleteEvent event) {
		Botmain.gdp.removeWelcomeMessage(event.getGuild(), event.getMessageId());
	}
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
