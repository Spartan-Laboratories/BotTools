package BotTools.services;

import java.io.IOException;
import java.util.function.Consumer;
import BotTools.commands.OnlineCommand;
import BotTools.main.Botmain;

public abstract class CheckValueService extends OnlineCommand implements Runnable{
	private int interval;
	private Consumer<String> onChange;
	protected ServicePacketReader packet;
	private String packetName;
	CheckValueService(String serviceName) {
		super(serviceName);
		packetName = serviceName;
		packet = new ServicePacketReader(packetName);
		System.out.println("Created service: " + serviceName);
	}
	private CheckValueService setInterval(int interval) {
		this.interval = interval;
		return this;
	}
	
	private void triggerResponce(String newValue) {
		onChange.accept(newValue);
	}

	private void sleep() {
		sleep(interval);
	}
	private void sleep(int seconds) {
		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public boolean execute(String[] args) {return false;}
	private void start() {
		new Thread(this).start();
	}
	public void run() {
		initialSleep();
		while(true) {
			Botmain.out("The service: " + getName() + " is starting a check");
			packet = new ServicePacketReader(packetName);
			loop();
			sleep();
		}
	}
	private void initialSleep() {
		sleep(3);
	}
	protected abstract void loop();
	
	protected static void createService(CheckValueService service, Consumer<String> onChange, int interval){
		service.setTriggerResponce(onChange).setInterval(interval).start();
	}
	private CheckValueService setTriggerResponce(Consumer<String> onChange) {
		this.onChange = onChange;
		return this;
	}
	private boolean valueChanged() {
		return !data.equals(packet.oldValue());
	}
	protected void checkValues() {
		if(valueChanged()) {
			Botmain.out(getName() + " found a new value: " + data);
			packet.writeValue(data);
			triggerResponce(data);
		}
		else Botmain.out(getName() + " detected no changes.");
	}
	protected void navigate() {
		skipLines();
		disconnect();
		getValue();
	}
	private void skipLines() {
		packet.getSkipLineData().forEach(key->{try {
			skipLinesTo(key);
		}catch(IOException e) {
			System.out.println("The service: " + getName() + " has failed to perform a check");
		}});
	}
	private void getValue() {
		data = getValueFromKey(packet.keyName());
	}
}
