package com.bottools.services;

import java.io.IOException;
import java.util.function.Function;

import com.bottools.commands.OnlineCommand;

import botactions.online.OnlineAction;

public class ServiceCommand extends OnlineCommand implements Runnable{
	private final ServicePacketReader packet;
	private int interval;
	private Function<String, Void> onChange;
	
	public static void createService(String serviceName, Function<String, Void> onChange, int interval) {
		new ServiceCommand(serviceName, onChange).setInterval(interval).start();
	}
	
	public ServiceCommand(String serviceName, Function<String, Void> onChange) {
		super(serviceName);
		packet = new ServicePacketReader(serviceName);
		this.onChange = onChange;
	}
	public ServiceCommand setInterval(int interval) {
		this.interval = interval;
		return this;
	}
	public void run() {
		sleep(3);
		while(true) {
			connect();
			skipLines();
			disconnect();
			getValue();
			//if(valueChanged())
			onChange();
			sleep();
		}
	}
	@Override
	public final boolean connect() {
		return super.connect(packet.getURL());
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
	private boolean valueChanged() {
		return !data.equals(packet.oldValue());
	}
	private void onChange() {
		packet.writeValue(data);
		onChange.apply(data);
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
	public void start() {
		new Thread(this).start();
	}
	@Override
	protected boolean execute(String[] args) {
		// TODO Auto-generated method stub
		return false;
	}
}
