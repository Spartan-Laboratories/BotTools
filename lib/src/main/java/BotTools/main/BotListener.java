package BotTools.main;
import java.io.File;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateNameEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateOnlineStatusEvent;

/**
 * The BotListener class detects user actions that happen within
 * the servers that this bot is a part of. Whenever any user within
 * one of those servers performs any action this class fires off a 
 * series of actions of its own. With no modification this class will
 * only fire off the default bot actions associated with the user action.
 * However, additional actions may be added in a bot's implementation of 
 * this API.
 * 
 * 
 * @author spartak
 *
 */
public final class BotListener extends ListenerAdapter {
	private static final Logger log = LoggerFactory.getLogger(BotListener.class);
	/**
	 * This is a list of MessageChannels that this listener is going to ignore.
	 * By default it is empty but can be changed by plugins or bot implemented commands 
	 * that reserve channels for specific commands
	 */
	private ArrayList<MessageChannel> ignoredChannels = new ArrayList<MessageChannel>();
	Responder responder = new Responder();
	
	/**
	 * The Event Types that this listener listens 
	 * and fires off event actions for.
	 * @author spartak
	 *
	 */
	
	BotListener() {
		addTrigger("/");
		
		responder.addOnGuildJoinAction(				this::defaultOnGuildJoinAction);
		responder.addOnGuildUpdateNameAction(			this::defaultOnGuildUpdateNameAction);
		responder.addOnGuildMemberJoinAction(			this::defaultOnGuildMemberJoinAction);
		responder.addOnMessageReceivedAction(			this::defaultOnMessageReceivedAction);
		responder.addOnSlashCommandInteractionAction(	Botmain::handleCommand);
	}
	@Override
	public void onGuildJoin(GuildJoinEvent event){
		responder.actOn(event);
	}
	private void defaultOnGuildJoinAction(GuildJoinEvent event) {
		Guild joinedServer = event.getGuild();
		String serverName = joinedServer.getName();
		try {
			Botmain.gdp.createGuildDatabase(joinedServer);
		}catch(Exception e) {
			Botmain.out("Joined the server: " + serverName);
			Botmain.out("But with the following errors:\n");
			e.printStackTrace();
			return;
		}
		log.info("Successfully joined the server: {}", serverName);
	}
	
	@Override
	public void onGuildUpdateName(GuildUpdateNameEvent event) {
		responder.actOn(event);
	}
	private void defaultOnGuildUpdateNameAction(GuildUpdateNameEvent event) {
		new File("guildData/" + event.getOldName()).renameTo(
		new File("guildData/" + event.getGuild().getName()));
	}
	
	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event){
		responder.actOn(event);
	}
	private void defaultOnGuildMemberJoinAction(GuildMemberJoinEvent event) {
		System.out.println("The user " + event.getUser().getName() + " has joined the guild " + event.getGuild().getName());
		
		// Updates the database of every guild, therefore not requiring a Guild argument
		Botmain.gdp.updateServerDatabase();			
		System.out.println("Guild member database has been updated");
		
		// Makes the bot send a welcome message to the user in the specified text channel
		System.out.println("Sending welcome message");
		Botmain.guildManager.sendWelcomeMessage(event.getMember());
		
		// Gives the new guild member the specified default role
		System.out.println("adding the default role to the member");
		event.getGuild().addRoleToMember(event.getMember(), Botmain.gdp.getDefaultRole(event.getGuild())).complete();
	}
	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		responder.actOn(event);
	}
	
	/**
	 * What happens when a User types in any message
	 * 
	 */
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		responder.actOn(event);
	}
	private void defaultOnMessageReceivedAction(MessageReceivedEvent event) {
		String message = event.getMessage().getContentRaw();
		boolean isCommand = Parser.startsWithTrigger(message);
		if(!isCommand)
			return;
		if(ignoredChannels.contains(event.getChannel()))
			return;
		Botmain.handleCommand(Parser.parse(message), event);
	}
	@Override
	public void onMessageDelete(MessageDeleteEvent event) {
		responder.actOn(event);
	}
	
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event){
		responder.actOn(event);
	}
	@Override
	public void onUserUpdateOnlineStatus(UserUpdateOnlineStatusEvent event) {
		responder.actOn(event);
	}
	public void ignoreChannel(MessageChannel channel) {
		ignoredChannels.add(channel);
	}
	public void unignoreChannel(MessageChannel channel) {
		ignoredChannels.remove(channel);
	}
	public void addTrigger(String trigger) {
		Parser.addTrigger(trigger);
	}

	
}
