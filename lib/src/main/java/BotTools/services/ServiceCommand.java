package BotTools.services;

import java.io.IOException;
import java.util.function.Function;

import BotTools.commands.OnlineCommand;
import BotTools.main.Botmain;

public class ServiceCommand extends CheckValueService{
	
	public static void createService(String serviceName, Function<String, Void> onChange, int interval) {
		createService(new ServiceCommand(serviceName), onChange, interval);
	}
	
	public ServiceCommand(String serviceName) {
		super(serviceName);
	}
	public void loop() {
		connect();
		navigate();
		checkValues();
	}
	@Override
	public final boolean connect() {
		Botmain.out("The service: "  + getName() + " is trying to connect");
		return super.connect(packet.getURL());
	}
		
}
