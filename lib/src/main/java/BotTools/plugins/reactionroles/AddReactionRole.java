package BotTools.plugins.reactionroles;

import java.util.ArrayList;
import java.util.List;

import BotTools.commands.Command;
import BotTools.main.Botmain;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;

public class AddReactionRole extends Command {
	public AddReactionRole() {
		super("addreactionrole");
		setHelpMessage("Use this command to tie a server emote to a role");
		makeSlashCommand();
		addOption("integer", "emote", "the reaction emote that you want tied to a role", true);
		addOption("role", "role", "the role that you want to be given on reaction", true);
	}
	
	@Override
	public boolean execute(String[] args) {
		Emoji emote = getEmote(args[0]);
		Role role = getRole(args[1]);
		if(emote == null || role == null)
			return false; 
		makeReactionRole(guild, emote, role);
		reply("Emote " + args[0] + " is now tied to the role: " + role.getAsMention());
		CreateMainWelcomeMessage.updateMessages(guild, emote);
		return true;
	}
	
	private Emoji getEmote(String emoteString) {
		List<RichCustomEmoji> emojis = guild.getEmojis();
		Emoji emote = guild.getEmojisByName(args[0].split(":")[1], true).get(0);
		return emote != null ? emote : invalidEmoteMessage();
	} 
	private Emoji invalidEmoteMessage() {
		say("Emote was not found. First option must be an emote that is found in this server.");
		return null;
	}
	private Role getRole(String roleString) {
		String roleID = args[1];
		Role role = guild.getRoleById(roleID);
		return role != null ? role : invalidRoleMessage();
	}
	private Role invalidRoleMessage() {
		say("Role was not found. Second option must be a role that is found in this server");
		return null;
	}
	
	private void makeReactionRole(Guild guild, Emoji emote, Role role) {
		Botmain.gdp.addReactionRole(guild, emote, role);
	}

	public static Role getCorrespondingRole(Guild guild, Emoji emote) {
		return guild.getRoleById(Botmain.gdp.getCorrespondingRoleID(guild, emote));
	}
	public static List<Emoji> getEmoteList(Guild guild){
		ArrayList<Emoji> emotes = new ArrayList<Emoji>();
		Botmain.gdp.getGameEmoteIDs(guild).forEach(emoteID ->
		emotes.add(guild.getEmojisByName(emoteID, true).get(0)));
		return emotes;
	}
}
