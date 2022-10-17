package BotTools.plugins;

import java.util.Arrays;

import BotTools.plugins.moderation.AddReactionRole;
import BotTools.plugins.moderation.CreateMainWelcomeMessage;

public class Plugins {
	public static Plugin MODERATOR = ()->{
		return Arrays.asList(new AddReactionRole(), new CreateMainWelcomeMessage());
	};
}
