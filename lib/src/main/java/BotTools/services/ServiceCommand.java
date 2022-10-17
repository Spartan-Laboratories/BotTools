package BotTools.services;

import java.util.function.Consumer;
import BotTools.main.Botmain;

public class ServiceCommand extends CheckValueService{
	
	public static void createService(String serviceName, Consumer<String> onChange, int interval) {
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
