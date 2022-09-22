package com.bottools.main;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

import com.bottools.main.Botmain;
import com.bottools.main.Parser;
import com.bottools.main.Parser.CommandContainer;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.Role;

public class Console extends Thread{
	protected CommandContainer commandInfo;
	protected String[] args;
	protected String name;
	private Scanner scanner = new Scanner(System.in);
	private String channel;
	private Guild guild;
	Member user;
	private boolean running;
	public void run(){
		running = true;
		Botmain.out("The console is active");
		while(running)try{
			scan();
		}catch(Exception e){
			e.printStackTrace();
		}
		Botmain.out("Console thread has terminated");
	}
	
	public void end() {
		running = false;
		InputStream fakeIn = new ByteArrayInputStream("print closetest".getBytes());
		//System.setIn(fakeIn);
		scanner = new Scanner(fakeIn);
	}
	
	private void scan() throws IOException, ImproperConsoleCommandUseException, InterruptedException{
		Thread.sleep(100);
		if(scanner.hasNext()) {
			String command = scanner.nextLine();
			parse(command);
		}
	}
	private boolean parse(String command) throws IOException, ImproperConsoleCommandUseException{
		commandInfo = Parser.parse(command);
		args = commandInfo.args;
		name = commandInfo.commandName;
		switch(name){
		case "set":
			if(commandInfo.args == null || args.length == 0)
				Botmain.out("Invalid command arguments");
			else if(args[0].equals("channel"))
				if(args.length < 2 || args[1] == null || args[1].equals(""))
					Botmain.out("No channel type specified");
				else 
					if(commandInfo.args[1].equals("text"))
						if(commandInfo.args.length < 3 || commandInfo.args[2] == null || commandInfo.args[2].equals(""))
							Botmain.out("No text channel specified");
						else channel = commandInfo.args[2];
					else if(commandInfo.args[1].equals("voice"))
						Botmain.jda.getVoiceChannelsByName(commandInfo.args[2], true).get(0);
					else Botmain.out("Improper channel type");
			else if(commandInfo.args[0].equals("points") || commandInfo.args[0].equals("promotionpoints"))
				if(commandInfo.args.length < 2)
					Botmain.out("Invalid arguments");
				else {
					Botmain.guildManager.loadGuild(guild);
					Botmain.gdp.setPoints(user.getUser(), Double.parseDouble(commandInfo.args[1]));
				}
			return true;
		case "doge":
			Botmain.guildManager.sendDogeMessage(guild.getDefaultChannel());
			return true;
		case "showchat":
			if(args.length != 1)
				throw new ImproperConsoleCommandUseException("showchat",
				"Incorrect number of arguments. Required: 1. Provided: " + args.length);
			PrivateChannel channel = user.getUser().openPrivateChannel().complete();
			MessageHistory history = channel.getHistory();
			int size = Integer.parseInt(args[0]);
			
			List<Message> messages = history.retrievePast(size).complete();
			for(int i = messages.size(); i > 0; i--)
				Botmain.out(messages.get(i-1).getContentRaw());
			return true;
		case "update":
			Botmain.gdp.updateServerDatabase();
			return true;
		case "listservers":
			for(Guild g:Botmain.jda.getGuilds())
				Botmain.out(g.getName());
			return true;
		case "test":
			Botmain.jda.getPresence().setStatus(OnlineStatus.IDLE);
			//TODO Botmain.jda.getPresence().setGame(Game.playing("with himself"));;
			Botmain.jda.getPresence().setStatus(OnlineStatus.ONLINE);
			return true;
		case "addtag":
			if(args.length == 3)
				Botmain.gdp.addTag(args[0], args[1], args[2]);
			else if(args.length == 2)
				Botmain.gdp.addTag(args[0], args[1], "");
			return true;
		case "removetag":
			Botmain.gdp.removeTag(args[0], args[1]);
			return true;
		case "print":
			Botmain.out(args[0]);
			return true;
		case "checkpersoncontents":
			for(String s: Botmain.gdp.getContents(guild, args[0]))
				out(s);
			return true;
		case "sayemoji":
			String emoji = //guild.getEmotesByName(args[0], true).get(0).getAsMention();
			Botmain.jda.getEmotesByName("soccer", true).get(0).getAsMention();
			guild.getDefaultChannel().sendMessage(emoji).complete();
			return true;
		default: return commandNotFoundMessage();
		}
	}
	class ImproperConsoleCommandUseException extends Exception{
		String commandName, issue;
		ImproperConsoleCommandUseException(String commandName, String issue){
			this.commandName = commandName;
			this.issue = issue;
		}
		@Override
		public void printStackTrace() {
			Botmain.out("The command " + commandName + "was used incorrectly");
			System.err.println(issue);
			super.printStackTrace();
		}
	}
	public void out(String text) {
		System.out.println(text);
	}
	
	private boolean commandNotFoundMessage() {
		Botmain.out("The entered data was not a generic console command");
		return false;
	}
}


