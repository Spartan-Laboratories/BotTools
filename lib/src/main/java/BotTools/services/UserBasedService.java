package BotTools.services;

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import BotTools.main.Botmain;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

public class UserBasedService extends CheckValueService{
	private static final Logger log = LoggerFactory.getLogger(UserBasedService.class);
	String serviceName;
	
	public static void createService(String serviceName, Function<String, Void> onChange, int interval) {
		createService(new UserBasedService(serviceName), onChange, interval);
	}
	
	UserBasedService(String serviceName) {
		super(serviceName);
		this.serviceName = serviceName;
	}

	@Override
	protected void loop() {		
		jda.getGuilds().forEach(this::checkGuild);
	}
	protected void checkGuild(Guild guild) {
		setGuild(guild);
		log.debug("Checking guild: {}", guild.getName());
		guild.getMembers().forEach(member ->{
			if(member.getOnlineStatus() == OnlineStatus.ONLINE)
				checkMember(member);
		});
	}
	protected void checkMember(Member member) {
		setMember(member);
		log.debug("Checking member: {}", member.getUser().getName());
		String memberUrl = substituteUrl();
		if(memberUrl == null)
			return;
		connect(memberUrl);
		navigate();
		checkValues();
	}
	private String substituteUrl() {
		String userId = getUserId();
		if(userId == null || userId.equals("") || userId.equals("none"))
			return null;
		log.debug("Found id: {}, for user: {}", userId, getMember().getUser().getName());
		return packet.getURL().replace("userid", userId);
	}
	protected String getUserId() {
		return Botmain.gdp.getUserValue(getGuild(), getMember(), "keyid");
	}
	
}
