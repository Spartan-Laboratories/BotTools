package BotTools.services;

import java.util.function.Function;

import BotTools.main.Botmain;
import net.dv8tion.jda.api.entities.Guild;

public class UserGameService extends UserBasedService {
	String gameName;
	
	public static void createService(String gameName, Function<String, Void> onChange, int interval) {
		createService(new UserGameService(gameName), onChange, interval);
	}
	
	UserGameService(String gameName) {
		super("game services/" + gameName + " user games");
		this.gameName = gameName;
	}
	@Override
	protected void checkGuild(Guild guild){
		
	}
	@Override
	protected String getUserId() {
		return Botmain.gdp.getID(getGuild(), getMember(), gameName);
	}
}
