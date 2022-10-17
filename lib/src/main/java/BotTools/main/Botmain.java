package BotTools.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import BotTools.botactions.BotAction;
import BotTools.commands.Command;
import BotTools.commands.HelpCommand;
import BotTools.dataprocessing.GuildDataParser;
import BotTools.dataprocessing.XMLReader;
import BotTools.main.Parser.CommandContainer;
import BotTools.plugins.Plugin;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public abstract class Botmain implements Runnable{
	Logger log = LoggerFactory.getLogger(Botmain.class);
	public static JDA jda;
	protected static HashMap<String, Command> commands = new HashMap<String, Command>();
	public static ArrayList<CommandData> slashCommands = new ArrayList<CommandData>();
	protected static BotListener listener;
	public static GuildDataParser gdp;
	public static GuildManager guildManager = new GuildManager();
	public static ArrayList<String> commandNames = new ArrayList<String>();
	public static String[] keys = new String[3];
	private static Console console;
	//private static Console guiConsole = new GuiConsole();
	protected static XMLReader reader = new XMLReader();
	private static boolean running, debug;
	private static long startTime;
	private static PrintWriter logWriter;
	protected void init() {
		/* In case I want to see logger status messages
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
	    StatusPrinter.print(lc); 
	    */
		log.info("Begun bot initialization");
		running = true;
		debug = false;
		startTime = System.currentTimeMillis() + 18 * 3600000;
		// My systems
		defineLogWriter();
		gdp = createGuildDataParser();
		console = new Console();
		console.start();
		// Bot setup
		initializeBotExistence();	//Initial bot setup
		createCommand(new HelpCommand());
		listCommands();				//Old school commands setup
		listSlashCommands();		//Fancy new "slash" commands setup
		createAllSlashCommands();
		// My Systems again
		gdp.updateServerDatabase();	//Must be after bot and console initialization
		reader.setDocument("BotData.xml");
		//new Thread(this).start();
	}
	private void initializeBotExistence() {
		System.out.println("Starting bot initilization");
		try(BufferedReader keyReader = new BufferedReader(new FileReader(new File("key.txt")))){
			for(int i = 0; i < keys.length; i++)
				keys[i] = keyReader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("An unknown error has occurred while parsing the bot's key file.");
		}
		System.out.println("Successfully read bot key.\nStarting bot building.");
		try { 
			// TODO figure out how to build JDA again
			jda = JDABuilder.createDefault(keys[0], EnumSet.allOf(GatewayIntent.class))
			.addEventListeners(listener = createListener())
			.enableCache(CacheFlag.VOICE_STATE)
			.enableCache(CacheFlag.EMOJI) 
			.enableCache(CacheFlag.ONLINE_STATUS)
			.enableCache(CacheFlag.FORUM_TAGS)
			.setMemberCachePolicy(MemberCachePolicy.ALL)
			.build();
			
			//jda.getPresence().setGame(Game.of(Game.GameType.DEFAULT, "/help for info"));
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch(Exception e) {
			System.out.println("An unknown error has occurred with the bot builder.");
			e.printStackTrace();
		}
		System.out.println("Completed bot initialization");
	}
	protected abstract BotListener createListener();
	protected abstract void listCommands();
	protected abstract void listSlashCommands();
	private static void createAllSlashCommands() {
		jda.updateCommands().addCommands(slashCommands).complete();
	}
	
	static void handleCommand(CommandContainer commandText, MessageReceivedEvent event) {
		String commandName = commandText.commandName;
		Command command = getCommands().get(commandName);
		MessageChannel channel = event.getChannel();
		boolean isValid = getCommands().containsKey(commandName);
		if(isValid) {
			try {
				command.setEvent(event).handle(commandText);
			}catch(InsufficientPermissionException ipe) {
				ipe.printStackTrace();
				BotAction.say(channel, "Insufficient permissions to perform this command");
			}catch(Exception e) {
				e.printStackTrace();
				BotAction.say(channel, "An error occured while trying to execute this command");
			}
		}
		else {
			BotAction.say(channel,"This command does not exist");
		}
	}
	public static ArrayList<String> getCommandNameList(){
		return commandNames;
	}
	public static void stop() {
		jda.shutdown();
		console.stop();
		logWriter.close();
		running = false;
	}
	public static XMLReader getReader() {
		return reader;
	}
	public static void out(String text) {
		console.out(text);
		//jda.getGuildsByName("me", true).get(0).getMembersByName("spartak?", true).get(0).getUser().openPrivateChannel().complete().sendMessage(text).complete();
		log(text);
	}
	public static void debug(String debugMessage) {
		if(debug)
			out(debugMessage);
	}
	private static void log(String text) {
		logWriter.println(text);
		logWriter.flush();
	}
	public static void isInDebugMode(boolean isInDebug) {
		debug = isInDebug;
		log("Debug mode has been turned " + (debug ? "on" : "off"));
	}
	public static long getUptime() {
		return running ? (System.currentTimeMillis() - startTime) : -18 * 3600000;
	}
	private static void defineLogWriter() {
		try {
			logWriter = new PrintWriter(new FileWriter("log.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		final long second = 1000;
		final long minute = 60 * second;
		final long hour = 60 * minute;
		final long day = 24 * hour;
		do{ try {
				System.out.println("Starting central process");
				Node dateNode = reader.getChild(reader.getRoot(), "lastdailyupdate");
				String lastDate = reader.getValue(dateNode);
				System.out.println("The last daily update was on " + lastDate);
				OffsetDateTime now = Instant.now().atOffset(ZoneOffset.ofHours(-8));
				int currentDay = now.getDayOfMonth();
				int currentMonth = now.getMonthValue();
				String currentDate = currentMonth + "/" + currentDay;
				System.out.println("The current date is: " + currentDate);
				if(!lastDate.equals(currentDate))
					applyDailyUpdate(currentDate);
				else
					System.out.println("no update is needed");
				Thread.sleep(day);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}while(true);
	}
	protected abstract void applyDailyUpdate(String currentDate);
	public static void handleCommand(SlashCommandInteractionEvent event) {
		getCommands().get(event.getName()).handle(event);
	}
	public static void createCommand(Command command) {
		for(String alias : command.getAliasList()){
			Botmain.getCommands().put(alias, command);
			if(!commandNames.contains(command.getName()))
				commandNames.add(command.getName());
		}
		if(command.isSlashCommand())
			slashCommands.add(command.getCommandData());
	}
	
	public static Command getCommand(String name) {
		return getCommands().get(name);
	}
	public static HashMap<String, Command> getCommands() {
		return commands;
	}
	protected GuildDataParser createGuildDataParser() {
		return new GuildDataParser();
	}
	protected final void addPlugin(Plugin plugin) {
		plugin.get().forEach(Botmain::createCommand);
	}
}
