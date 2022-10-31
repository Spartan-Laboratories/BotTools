package BotTools.plugins.reactionroles;

import BotTools.botactions.BotAction;
import BotTools.main.Botmain;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public class ReactionRoleActions {
	public static void giveReactionRole(MessageReactionAddEvent event) {
		String roleid = Botmain.gdp.getCorrespondingRoleID(event.getGuild(), event.getEmoji());
		Role role = Botmain.jda.getRoleById(roleid);
		event.getGuild().addRoleToMember(event.getMember().getUser(), role);
	}
	public static void removeDeletedWelcomeMessage(MessageDeleteEvent event) {
		Botmain.gdp.removeWelcomeMessage(event.getGuild(), event.getMessageId());
	}
	static void createWelcomeMessage(GuildMemberJoinEvent event) {
		createWelcomeMessage(event.getGuild());
	}
	static void createWelcomeMessage(Guild guild) {
		String channelId = Botmain.gdp.getWelcomeMessageChannel(guild);
		MessageChannel channel = guild.getTextChannelById(channelId);
		String welcomeMessage = Botmain.gdp.getWelcomeMessage(guild);
		Message message = BotAction.say(channel, welcomeMessage);
		setGuildWelcomeMessageID(message);
		addGameEmotes(message);
	}
	static Message setGuildWelcomeMessageID(Message message) {
		Botmain.gdp.setWelcomeMessageID(message.getGuild(), (TextChannel) message.getChannel(), message);
		return message;
	}
	
	static boolean addGameEmotes(Message welcomeMessage) {
		AddReactionRole.getEmoteList(welcomeMessage.getGuild()).forEach(emote -> {
			welcomeMessage.addReaction(emote).complete();
		});
		return true; 
	}
}
