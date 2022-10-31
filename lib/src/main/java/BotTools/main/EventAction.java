package BotTools.main;

import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateNameEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateOnlineStatusEvent;

@FunctionalInterface
public interface EventAction<EventType extends Event> {
	public void perform(EventType e);
	public interface GuildJoinAction extends EventAction<GuildJoinEvent>{}
	public interface UserUpdateOnlineStatusAction extends EventAction<UserUpdateOnlineStatusEvent>{}
	public interface MessageReceivedAction extends EventAction<MessageReceivedEvent>{}
	public interface MessageReactionAddAction extends EventAction<MessageReactionAddEvent>{}
	public interface MessageDeleteAction extends EventAction<MessageDeleteEvent>{}
	public interface GuildUpdateNameAction extends EventAction<GuildUpdateNameEvent>{}
	public interface GuildMemberJoinAction extends EventAction<GuildMemberJoinEvent>{}
	public interface SlashCommandInteractionAction extends EventAction<SlashCommandInteractionEvent>{}
}
