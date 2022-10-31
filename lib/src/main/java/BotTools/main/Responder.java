package BotTools.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import BotTools.main.EventAction.GuildJoinAction;
import BotTools.main.EventAction.GuildMemberJoinAction;
import BotTools.main.EventAction.GuildUpdateNameAction;
import BotTools.main.EventAction.MessageDeleteAction;
import BotTools.main.EventAction.MessageReactionAddAction;
import BotTools.main.EventAction.MessageReceivedAction;
import BotTools.main.EventAction.SlashCommandInteractionAction;
import BotTools.main.EventAction.UserUpdateOnlineStatusAction;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateNameEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateOnlineStatusEvent;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.*;
import org.springframework.core.type.filter.AssignableTypeFilter;

public class Responder {
	private HashMap<Class, List<EventAction>> actionMap = new HashMap<Class, List<EventAction>>();
	
	Responder(){
		getSubclasses(Event.class).forEach(subclass -> actionMap.put(subclass, new ArrayList<EventAction>()));
		int x = 1;
	}
	
	void actOn(Event event) {
		actionMap.get(event.getClass()).forEach(eventAction -> eventAction.perform(event));
	}
	public void addOnGuildJoinAction(GuildJoinAction onEventAction) {
		actionMap.get(GuildJoinEvent.class).add(onEventAction);
	}
	public void addOnGuildUpdateNameAction(GuildUpdateNameAction onEventAction) {
		actionMap.get(GuildUpdateNameEvent.class).add(onEventAction);
	}
	public void addOnGuildMemberJoinAction(GuildMemberJoinAction onEventAction) {
		actionMap.get(GuildMemberJoinEvent.class).add(onEventAction);
	}
	public void addOnUserUpdateOnlineStatusAction(UserUpdateOnlineStatusAction eventAction) {
		actionMap.get(UserUpdateOnlineStatusEvent.class).add(eventAction);
	}
	public void addOnMessageReactionAddAction(MessageReactionAddAction onEventAction) {
		actionMap.get(MessageReactionAddEvent.class).add(onEventAction);
	}
	public void addOnMessageReceivedAction(MessageReceivedAction onEventAction) {
		actionMap.get(MessageReceivedEvent.class).add(onEventAction);
	}
	public void addOnMessageDeleteAction(MessageDeleteAction onEventAction) {
		actionMap.get(MessageDeleteEvent.class).add(onEventAction);
	}
	public void addOnSlashCommandInteractionAction(SlashCommandInteractionAction onEventAction) {
		actionMap.get(SlashCommandInteractionEvent.class).add(onEventAction);
	}
	
	private static List<Class> getSubclasses(Class superClass) {
		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
		provider.addIncludeFilter(new AssignableTypeFilter(superClass));
		Set<BeanDefinition> components = provider.findCandidateComponents("net.dv8tion");
		ArrayList<Class> subclasses = new ArrayList<Class>();
		
		for(BeanDefinition component: components) {
			Class c = null;
			try {
				c = Class.forName(component.getBeanClassName());
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(c == superClass)
				continue;
			subclasses.add(c);
			subclasses.addAll(getSubclasses(c));
		}
		return subclasses;
	}
}
