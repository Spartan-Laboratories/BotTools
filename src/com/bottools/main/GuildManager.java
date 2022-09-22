/**
 * 
 */
package com.bottools.main;

import java.awt.Color;
import java.awt.Image;
import java.io.File;
import java.util.HashMap;
import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.internal.utils.tuple.ImmutablePair;


public class GuildManager {
	private Guild currentGuild;
	private Role defaultRole;
	private HashMap<Integer, ImmutablePair<String, Double>> rankRequirements;
	public GuildManager(){
		
	}
	/**
	 * Load a new guild. Must be called prior to the use of other Guild Manager methods.
	 * @param guild
	 */
	public void loadGuild(Guild guild){
		currentGuild = guild;
		System.out.println("Loading guild: " + currentGuild.getName());
		try{
			defaultRole = Botmain.gdp.getDefaultRole(guild);		
			System.out.println("The guild default role is set to: " + defaultRole.getName());
		}catch(Exception e){
			System.out.println("Could not load the default role for the guild: " + guild.getName());
		}
		
		rankRequirements = Botmain.gdp.getRankReqs(guild);
	}
	/**
	 * Makes the Guild Manager add points to the passed in user as a reward for writing a message. {@link #loadGuild(Guild)} must be
	 * called prior to the user of this method, otherwise the guild used will be either the previously used guild or null.
	 * 
	 * @param user - the user that has written a mesage
	 * @param message - the message that was written. Must be the raw message (including any possible command triggers)
	 * 
	 */
	public void managePoints(User user, String message, boolean hasImageAttachment){
		double previousPoints = Botmain.gdp.getPromotionPoints(currentGuild, user.getName());
		double numPoints = Parser.startsWithTrigger(message) ? 0.5 : (hasImageAttachment ? 5 : 1.8);
		Botmain.gdp.addPoints(user, numPoints);
		double currentPoints = Botmain.gdp.getPromotionPoints(currentGuild, user.getName());
		// TODO Rankup system
		//checkRankUp(currentGuild.getMember(user), previousPoints, currentPoints);
	}
	
	/**
	 * 
	 * 
	 * @param member
	 */
	public void sendWelcomeMessage(Member member) {
		Guild guild = member.getGuild();
		System.out.println("The guild name is: " + guild.getName());
		String channel = Botmain.gdp.getWelcomeMessageChannel(guild);
		System.out.println("The welcome message channel is: " + channel);
		if(channel.equals("none"))return;
		String message = member.getAsMention() + Botmain.gdp.getWelcomeMessage(guild);
		System.out.println("The welcome message is: " + message);
		guild.getTextChannelsByName(channel, true).get(0).sendMessage(message).complete();
		System.out.println("Message is sent");
	}
	boolean spamDetection(GuildMessageReceivedEvent event){
		TextChannel channel = event.getChannel();
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
	private void checkRankUp(Member member, double previousPoints, double currentPoints){
		double breachedPoints = 0;
		for(Integer i: rankRequirements.keySet()){
			double requirement = rankRequirements.get(i).right;
			if(currentPoints >= requirement && requirement > breachedPoints 
			&&!member.getRoles().contains(currentGuild.getRolesByName(rankRequirements.get(i).left, false).get(0))){
				currentGuild.addRoleToMember(member, currentGuild.getRolesByName(rankRequirements.get(i).left, false).get(0)).complete();
				currentGuild.removeRoleFromMember(member, currentGuild.getRolesByName(rankRequirements.get(i-1).left, false).get(0)).complete();
				currentGuild.getTextChannelsByName("bot-commands", false).get(0).sendMessage("Congratulations you have been promoted").complete();
				breachedPoints = requirement;
			}
		}
	}
	
	public void sendDogeMessage(TextChannel channel){
		EmbedBuilder builder = new EmbedBuilder();
		builder = builder.setTitle("Doge").setDescription("this is a doge").setThumbnail("http://clipartbarn.com/wp-content/uploads/2016/10/Thumbs-up-thumb-clip-art-at-vector.jpg")
		.setColor(new Color(0xff0000)).setImage("https://lh6.ggpht.com/Gg2BA4RXi96iE6Zi_hJdloQAZxO6lC6Drpdr7ouKAdCbEcE_Px-1o4r8bg8ku_xzyF4y=h900");
		channel.sendMessage(builder.build()).complete();
	}
}
