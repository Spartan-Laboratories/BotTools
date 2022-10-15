package BotTools.main;
import java.io.File;
import java.util.ArrayList;

import BotTools.botactions.BotAction;
import BotTools.commands.slashcommands.SlashCommand;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateNameEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;


public class BotListener extends ListenerAdapter {
	
	protected String message;
	protected MessageReceivedEvent messageEvent;
	protected MessageChannel channel;
	
	private ArrayList<MessageChannel> ignoredChannels = new ArrayList<MessageChannel>();
	
	public BotListener() {
		System.out.println("listener created");
		addTrigger("/");
	}
	@Override
	public void onGuildJoin(GuildJoinEvent event){
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
		Botmain.out("Successfully joined the server: " + serverName);
	}
	
	@Override
	public void onGuildUpdateName(GuildUpdateNameEvent event) {
		new File("guildData/" + event.getOldName()).renameTo(
		new File("guildData/" + event.getGuild().getName()));
	}
	
	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
	}
	/**
	 * What happens when a User types in any message
	 * 
	 */
	@Override
	public synchronized void onMessageReceived(MessageReceivedEvent event) {
		messageEvent = event;
		message = event.getMessage().getContentRaw();
		channel = event.getChannel();
		boolean isCommand = Parser.startsWithTrigger(message);
		if(!isCommand) {
			processSystemInteractions();
			if(isSpam()) performSpamHandling();
			else;
		}
		// If the message does start with a trigger then parse the message as a command and execute it.
		else if(!ignoredChannels.contains(event.getChannel()))
			Botmain.handleCommand(Parser.parse(message), event);
	}
	
	private void processSystemInteractions() {
		/*
		if(dc != null && !dc.isBroken())
			dc.process(event);
		if(event.getAuthor().isBot())
			return;
		tc.receiveAnswer(event.getChannel(), event.getMember(), message);
		*/
	}
	
	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event){
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
	// TODO Figure out what changed about use online status updates
	/*
	@Override
	public void onUserOnlineStatusUpdate(UserOnlineStatusUpdateEvent event) {
		User user = event.getUser();
		OnlineStatus status = event.getCurrentOnlineStatus();
		((NotifyCommand)Botmain.commands.get("notify")).processStatusChange(user, status);
	}
	*/
	@Override
	public void onReady(ReadyEvent e){
		System.out.println("Logged in");
	}
	private void performSpamHandling(MessageReceivedEvent event){
		event.getChannel().sendMessage("you need to stop!").complete();
		long oldTime = System.currentTimeMillis();
		while(System.currentTimeMillis() < oldTime + 1000);
		event.getChannel().sendMessage("/prune 3").complete();
		Guild guild = event.getGuild();
		Member member = event.getMember();
		//guild.getController().setMute(member, true).complete();
	}
	/*
	void setDuplicateCommand(DuplicateCommand dc) {
		this.dc = dc;
	}
	void setTriviaCommand(TriviaCommand tc) {
		this.tc = tc;
	}*/
	public void ignoreChannel(MessageChannel channel) {
		ignoredChannels.add(channel);
	}
	public void unignoreChannel(MessageChannel channel) {
		ignoredChannels.remove(channel);
	}
	protected void addTrigger(String trigger) {
		Parser.addTrigger(trigger);
	}

	private boolean isSpam() {
		return Botmain.guildManager.spamDetection(messageEvent);
	}
	private void performSpamHandling(){
		BotAction.say(channel,"you need to stop!");
		long oldTime = System.currentTimeMillis();
		while(System.currentTimeMillis() < oldTime + 1000);
		//TODO Create a BotAcion for this: BotAction.say(channel,"/prune 3");
		Guild guild = messageEvent.getGuild();
		Member member = messageEvent.getMember();
		//TODO Create a BotAction for this: guild.getController().setMute(member, true).complete();
	}
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		for(CommandData c: Botmain.slashCommands)
			if(SlashCommand.class.isAssignableFrom(c.getClass()))
			if(event.getName().equals(c.getName())) {
				SlashCommand sc = (SlashCommand)c;
				sc.setEvent(event);
				
				String reply = sc.getReply();
				if(reply != null)
					event.reply(reply).complete();
				
				sc.execute();
				return;
			}
		Botmain.handleCommand(event);
	}
}
