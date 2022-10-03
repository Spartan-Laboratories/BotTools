package com.bottools.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.bottools.commands.slashcommands.Option;
import com.bottools.main.Botmain;
import com.bottools.main.Parser;
import com.bottools.main.Parser.CommandContainer;

import botactions.BotAction;
import botactions.online.OnlineAction;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;
import net.dv8tion.jda.internal.utils.Checks;
/**
 * The command superclass that all command subclasses inherit from. When creating a new command subclass it must 
 * <ul>
 * <li><b>extend</b> this class
 * <li> implement the {@link #execute(String[])} method
 * <li> have a constructor that calls <b>super()</b> and sets the command name using {@link #setCommandName(String)} as well as adds any possible
 * aliases using {@link #addAlias(String...)}
 * </ul>
 * <h6>Use:</h6><pre>CommandName <b>extends</b> Command{
 * 	<b>public</b> CommandName(){
 *		<b>super</b>();
 * 		setCommandName("commandname");
 * 		addAlias("alias"); 	// Optional
 * 	}
 * 	<b>public boolean</b> execute(){
 * 		Botmain.out("The command has been executed");
 * 		return true;
 * 	}
 * }
 * </pre>
 * <br>
 * 
 * @author spartak
 *
 */
public abstract class Command{
	private boolean hasName; 
	/**
	 * Should this command display messages describing the actions that it is taking.
	 * <br><b>true</b> to display debug messages
	 * <br><b>false</b> to not display error messages (default)
	 */
	protected boolean debug;
	protected Guild guild;
	private Member member;
	protected User user;
	private TextChannel channel;
	protected Message message;
	protected List<Member> tagged;
	protected JDA jda = Botmain.jda;
	protected int terminalArg;
	protected EmbedBuilder eb;
	protected String[] args;
	protected ReplyAction reply;
	private Member taggedMember;
	private String paramNames, aliases;
	private String commandName;
	private Map<String, SubCommand> subCommands = new HashMap<String, SubCommand>();
	private boolean subCommandRequired;
	private boolean isSlashCommand;
	private String noSubCommandDescription;
	private final String scRequiredErrMsg = "This command has to be followed by a sub-command.";
	private final String invalidSCErrMsg = " is not a valid sub-command.";
	/**
	 * The help message that is shown when using /help [command name]
	 */
	private String helpMessage;
	private boolean isReady;
	protected String usedCommandName;
	protected CommandData slashCommandData;
	/**
	 * The message event that contains the message that triggered this command
	 */
	protected GuildMessageReceivedEvent messageEvent;
	protected final ArrayList<String> aliasList = new ArrayList<String>();
	protected ReplyAction deferredReply;
	private SlashCommandEvent scEvent;
	private OrganizationCommand get, set;
	/**
	 * The only constructor in the class. It is required that this constructor is called from the constructors of subclasses.
	 * Otherwise various help message and error report bugs will occur.
	 */
	protected Command(String commandName) {
		paramNames = "";
		aliases = "";
		setCommandName(commandName);
		resetEmbedBuilder();
		setHelpMessage();
		generateSlashCommandData();
	}
	/**
	 * Takes in a {@link MessageReceivedEvent} and sets it as this command's event for the execution of the following commands. 
	 * Needs to be called prior to calling {@link #execute(String[])}
	 * Returns the {@link Command} that it is a part of to be able to conveniently call {@link #execute(String[])} right away
	 * @param event - the event of the message that triggered this command
	 * @return The {@link Command} whose event is being set
	 */
	public Command setEvent(GuildMessageReceivedEvent event) {
		messageEvent = event;
		guild = event.getGuild();
		member = event.getMember();
		user = event.getAuthor();
		channel = event.getChannel();
		message = event.getMessage();
		tagged = message.getMentionedMembers();
		taggedMember = tagged.size() == 0 ? member : tagged.get(0);
		return this;
	}
	/**
	 * Return all the aliases of this command in the form of a single String value.
	 * @return all the aliases of this command	 */
	public String getAliases() {
		return aliases;
	}
	/**
	 * Makes the bot send the automatically generated help message in the text channel that the command was received in.
	 */
	protected final void help() {
		say(getHelpMessage());
	}
	public void help(String[] subcommand){
		if(subcommand.length == 0)
			help();
		else
			getSubCommand(subcommand[0]).help(getSecondaryArgs(subcommand));			
	}
	/** Takes in a String value and sets the as
	 * the name of this command as well as adds it to the list of this command's aliases. Can only be called one time. Consecutive calls will be ignored.
	 * @param commandName - the name of the command
	 * @return <b>true</b> if this is the first time that this method has been called allowing this method to execute correctly
	 * <br><b>false</b> if the command name had already been set and this method was not executed.
	 */
	private boolean setCommandName(String commandName) {
		if(hasName)
			return false;
		hasName = true;
		aliasList.add(this.commandName = commandName.toLowerCase().replaceAll(" ", "").replaceAll("/", ""));
		return true;
	}
	/**
	 * Outputs the given message in the text channel of the event set by {@link #setEvent(GuildMessageReceivedEvent)}
	 * @param message - the message that is to be written in the text channel
	 */
	protected Message say(String message) {
		return BotAction.say(getChannel(), message);
	}
	protected void show(String url) {
		OnlineAction.sendImageInChannel(url, getChannel());
	}
	protected void show(File file) {
		BotAction.sendFileInChannel(file, getChannel());
	}
	
	/**
	 * Outputs the given message in the text channel of the event set by {@link #setEvent(GuildMessageReceivedEvent)}.
	 * <br>Works the same way as {@link #say(String)} but with a delay in seconds set by secondsDelay.
	 * @param secondsDelay - the delay in seconds after which the message is to be written.
	 * @param message - the message that is to be written in the text channel.
	 */
	protected void say(int secondsDelay, String message) {
		getChannel().sendMessage(message).completeAfter(secondsDelay, TimeUnit.SECONDS);
	}
	/**
	 * Return the primary name of this command as it was set when calling {@link #setCommandName(String)}.
	 * @return the name of this command.
	 */
	public String getName() {
		return commandName;
	}
	/**
	 * Returns all of the argument requirements of this command as they were set when calling {@link #setArgs(String...)}.
	 * Used for /help command messages 
	 * @return - all the argument requirements of this command
	 */
	public String getArgs() {
		return paramNames;
	}
	/**
	 * Adds an alias to the list of aliases that can trigger this command. Updates the help message to show the aliases.
	 * @param aliases - a list of aliases that identify this command.
	 */
	protected void addAlias(String... aliases) {
		for(String alias : aliases) {
			this.aliases = this.aliases.concat("'" + alias + "' ");
			aliasList.add(alias);
		}
		setHelpMessage();
	}
	/**
	 * Return all of the aliases of this command in the form of a String array.
	 * @return a String array that contains all the aliases of this command
	 */
	public String[] getAliasList(){
		return aliasList.toArray(new String[aliasList.size()]);	
	}
	/**
	 * Update the help message to reflect all the automatically gathered information about this command.
	 */
	private void setHelpMessage() {
		if(subCommands.size() == 0)
			helpMessage = String.format("Command name: %s\nParameters: %s\nAliases: %s\n", getName(), getArgs(), getAliases());
		else 
			helpMessage = "The command \"" + getName() + "\" " + (subCommandRequired ? "has to" : "can") + " be followed by a sub-command\n"
						+ (subCommandRequired ? "" : "When used without a subcommand " + noSubCommandDescription)
						+ "\nThe possible sub-commands are: \n" + subCommands.keySet()
						+ "\nTo show a help message for a specific sub-command use: /help " + getName() + " [sub-command name]";
		
	}
	protected boolean checkTreatAsSubCommand(String[] args) {
		if(args.length == 0)
			return false;
		if(isValidSubCommand(args[0]))
			return true;
		
		// If a valid subcommand was not found
		if(subCommandRequired) 	// but a subcommand is required
			if(args.length > 0) // and an argument was given 
				showSCErrorMessage(args[0] + invalidSCErrMsg); 	//Tell the user that the arg was invalid
			else				// or an argument was not given
				showSCErrorMessage(scRequiredErrMsg);			//Tell the user that an arg is required
		
		else if(terminalArg > 0) {
			this.args = new String[terminalArg];
			for(int i = 0; i < terminalArg - 1; i++)
				this.args[i] = args[i];
			this.args[terminalArg - 1] = concatArgs(args, terminalArg, args.length);
		}
		return false;
	}
	private String getSubcommandNames() {
		String subcommandNames = "";
		for(Object commandName: subCommands.keySet())
			subcommandNames += commandName.toString() + ", ";
		return subcommandNames;
	}
	private void showSCErrorMessage(String errorMessage) {
		say(errorMessage);
		say("The possible sub-commands are:\n" + getSubcommandNames());
	}
	public static void greet(User user){
		Botmain.jda.getTextChannelsByName("botspam", true).get(0).sendMessage("fuck you " + user.getAsMention()).queue();
	}
	public static String concatArgs(String[] args){
		return concatArgs(args, 1, args.length);
	}
	/**
	 * 
	 * @param args the String[] that is to be converted to a String
	 * @param startIndex the element to start with (first element is 1)
	 * @param endIndex the element to end with (last element is same as .lenth)
	 * @return A String that contains the selected element group with a space between elements
	 */
	public static String concatArgs(String[] args, int startIndex, int endIndex){
		String concatenation = "";
		for(int i = startIndex; i <= endIndex; i++)
			concatenation += args[i-1] + " ";
		concatenation = concatenation.substring(0, concatenation.length()-1);
		return concatenation;
	}
	
	public static String[] removeLeadingElement(String[] args) {
		String[] newArgs = new String[args.length - 1];
		for(int i = 1; i <= args.length; i++)
			newArgs[i-1] = args[i];
		return newArgs;
	}
	
	private final void resetEmbedBuilder() {
		eb = new EmbedBuilder();
	}
	protected final String[] getSecondaryArgs(String[] args) {
		if(args.length == 0)throw new IllegalArgumentException("Passed in arguments must have a size of >=1");
		if(args.length == 1)return new String[0];
		String[] secondaryArgs = new String[args.length - 1];
		for(int i = 1; i < args.length; i++)
			secondaryArgs[i-1] = args[i];
		return secondaryArgs;
	}
	protected final void addSubCommand(String commandName, SubCommand command) {
		subCommands.put(commandName, command);
		setHelpMessage();
	}
	protected final void removeSubCommand(String commandName) {
		subCommands.remove(commandName);
	}
	private boolean isValidSubCommand(String name) {
		return subCommands.containsKey(name);
	}
	protected SubCommand getSubCommand(String name) {
		if(!isValidSubCommand(name))
			say("No sub-command \"" + name+ "\" exists");
		return subCommands.get(name);
	}
	protected final void setHelpMessage(String helpMessage) {
		this.helpMessage = helpMessage;
	}
	public String getHelpMessage() {
		return helpMessage;
	}
	protected void isSubCommandRequired(boolean isRequired) {
		subCommandRequired = isRequired;
	}
	/**
	 * Sets the description of this command's action in the case that it is used without a sub-command.
	 * It is recommended that this method is used by all commands that do not require a sub-command,
	 * otherwise their description that is shown by /help [command name] will be blank.
	 * Note that by default sub-commands are not required, to change that call {@link #isSubCommandRequired(boolean)}
	 * @param description - a description of what this command does when used without a sub-command
	 */
	protected void setNoSubCommandDescription(String description) {
		noSubCommandDescription = description;
	} 
	
	protected boolean validateSubCommand(String[] args) {
		return isValidSubCommand(args[0]);
	}
	protected void debug(String debugMessage) {
		if(debug)
			Botmain.out(debugMessage); 
	}
	protected void quickEmbed(String title, String description) {
		eb.setAuthor(title);
		if(description.length() > 2048)
			description = description.substring(0,2048);
		eb.setDescription(description);
	}
	protected MessageEmbed getEmbed() {
		return eb.build(); 
	}
	protected void sendEmbed() {
		sendEmbed(channel);
		resetEmbedBuilder();
	}
	private void sendEmbed(TextChannel channel) {
		channel.sendMessage(getEmbed()).complete();
	}
	protected void sendEmbed(Collection<? extends TextChannel> channelList) {
		channelList.forEach(channel->{
			sendEmbed(channel);
		});
		resetEmbedBuilder();
		
	}
	public boolean handle(CommandContainer commandText) {
	//	System.out.println("Handling event with name: " + commandText.commandName + 
	//			"\nAnd args: \" " + Command.concatArgs(commandText.args) + "\"");
		usedCommandName = commandText.commandName;
		args = commandText.args;
		if(checkTreatAsSubCommand(args)) {
			commandText.commandName = args[0];
			commandText.args = getSecondaryArgs(args);
			return getSubCommand(args[0].toLowerCase()).handle(commandText);//setEvent(messageEvent).handle(commandText);
		}
		return execute(args);
	}
	public void say(Message message) {
		getChannel().sendMessage(message).complete();
	}
	protected void setReady() {
		isReady = true;
	}
	protected boolean isReady() {
		return isReady;
	}
	public boolean isSlashCommand() {
		return isSlashCommand;
	}
	protected CommandData makeSlashCommand() {
		isSlashCommand = true;
		slashCommandData.setDescription(getDescription());
		return slashCommandData;
	}
	public CommandData getCommandData() {	
		return slashCommandData;
			
	}
	public boolean handle(SlashCommandEvent event) {
		reply = event.deferReply();
		setEvent(event);
		return handle(Parser.parse(event));
	}
	
	private Command setEvent(SlashCommandEvent event) {
		scEvent = event;
		guild = event.getGuild();
		member = event.getMember();
		user = event.getUser();
		channel = event.getTextChannel();
		return this;
	}
	
	protected final String getDescription() {
		String description = getHelpMessage();
		if(description.length() > 100)
			description = description.substring(0, description.indexOf("\n"));
		return description;
	}
	
	protected Command addOption(String type, String name, String description, boolean required) {
		slashCommandData.addOptions(new Option(type, name, description, required));
		return this;
	}
	
	/**
	 * Executes this command using the arguments provided by args and the last event given by {@link #setEvent(GuildMessageReceivedEvent event)} .
	 * @param args - the arguments that followed this command.
	 * @return always returns <b> true</b>
	 * @throws Exception any error that may have thrown an exception while attempting to execute this command.
	 */
	protected abstract boolean execute(String[] args);
	protected void reply(String message) {
		reply.setContent(message).complete();
	}
	protected File saveImage(String url, String fileName) {
		return OnlineAction.saveImage(url, fileName);
	}
	
	protected Member getTargetMember() {
		return taggedMember != null ? taggedMember : member; 
	}
	
	protected void generateSlashCommandData() {
		slashCommandData = new CommandData(commandName, getDescription());
		
	}
	protected void addToSlashCommandData(SubcommandData subcommand) {
		slashCommandData.addSubcommands(subcommand);
	}
	protected void addToSlashCommandData(SubcommandGroupData subcommandGroup) {
		slashCommandData.addSubcommandGroups(subcommandGroup);
	}
	protected void addToSlashCommandData(OptionData optionData) {
		slashCommandData.addOptions(optionData);
	}
	protected OrganizationCommand addSubCommandGroup(String name) {
		return new OrganizationCommand(this, name);
	}
	protected OrganizationCommand getter() {
		return get != null ? get : (get = addSubCommandGroup("get"));
	}
	protected OrganizationCommand setter() {
		return set != null ? set : (set = addSubCommandGroup("set"));
	}
	protected TextChannel getChannel() {
		return channel;
	}
	protected TextChannel setChannel(TextChannel newChannel) {
		return channel = newChannel;
	}
	protected TextChannel resetChannel() {
		return channel = messageEvent != null ? messageEvent.getChannel() : scEvent.getTextChannel();
	}
	protected Member getMember() {
		return member;
	}
	protected Member setMember(Member newMember) {
		return member = newMember;
	}
	protected Member resetMember() {
		return member = messageEvent != null ? messageEvent.getMember() : scEvent.getMember();
	}
	protected Guild getGuild() {
		return guild;
	}
	protected Guild setGuild(Guild guild) {
		return this.guild = guild;
	}
	protected Guild resetGuild() {
		return this.guild = messageEvent != null ? messageEvent.getGuild() : scEvent.getGuild();
	}
}

