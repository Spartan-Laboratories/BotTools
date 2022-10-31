package BotTools.commands;

import BotTools.main.Botmain;
import BotTools.services.ServiceCommand;
import net.dv8tion.jda.api.entities.Member;

public abstract class GameStatsCommand extends OnlineCommand {
	protected String gameName;
	protected OrganizationCommand show, id;
	protected String tagSymbol;
	protected GameStatsCommand(String gameName) {
		super(gameName);
		this.gameName = gameName;
		tagSymbol = "#";
		isSubCommandRequired(true);

		makeInteractible();

		new MethodCommand(this, "showstats", "Show the target player's stats", this::showStats)
		.addOption("user", "name", "the server member whose game you want to see", false);;
		new MethodCommand(this, "lastgame", "Show the stats from the last game that was played", this::lastGame)
		.addOption("user", "name", "the server member whose game you want to see", false);
		new SCSetID();
		new SCGetID();
		
		setter().addCommand("id", "setid");
		getter().addCommand("id", "getid").addCommand("stats", "showstats");
		id	= new OrganizationCommand(this, "id").addCommand("set", "setid").addCommand("get", "getid").addCommand("show", "getid");
		show= new OrganizationCommand(this, "show").addCommand("id", "getid").addCommand("stats", "showstats");
		new OrganizationCommand(this, "last").addCommand("game", "lastgame");
	}
	protected abstract class GameStatsSubCommand extends SubCommand{

		protected GameStatsSubCommand(String name) {
			super(GameStatsCommand.this, name);
			init();
			addOptionals();
		}
		protected void init() {};
		private void addOptionals() {
			addOption("user", "person", "the person whose game id you want to set", false);
		}
	}
	protected class SCSetID extends GameStatsSubCommand{
		protected SCSetID() {
			super("setid");
			setHelpMessage("sets the game id of the mentioned user\n"
			+ "For example:\n"
			+ "`/" + GameStatsCommand.this.getName() + " setid *in-game id* @forthisperson` to set the ID for the mentioned user. Or\n"
			+ "`/" + GameStatsCommand.this.getName() + " setid *in-game id*` to set the ID for yourself");
		}
		@Override
		protected void init() {
			addOption("string", "id", "the steam/dotabuff id", true);
			addOption("string", "tagline", "the tag number associated with this id", false);
		}
		protected boolean execute(String[] args) { 
			Member targetMember = getTargetMember();
			String tagline = getOption("tagline").getAsString();
			if(Character.isDigit(tagline.charAt(0)))//if the first character of the tagline is numeric
				tagline = tagSymbol + tagline;
			String fullTag = args[0] + tagline;
			Botmain.gdp.setGameID(getGuild(), targetMember, gameName, fullTag);
			reply("The " + gameName + " ID of " + getTargetMember().getEffectiveName() + " has been set to " + getUserID());
			return true;
		}
	}
	private final class SCGetID extends GameStatsSubCommand{
		private SCGetID() {
			super("getid");
			addAlias("showid");
			setHelpMessage("Shows the recorded game ID for the mentioned user or for yourself if no user is mentioned");
		}
		protected boolean execute(String[] args) {
			String id = getUserID();
			if(id == null) 	sendNoIDMessage();
			else 			reply("The in-game ID of " + getTargetMember().getUser().getName() + " is " + id);
			return true;
		}
	}
	
	/**
	 * Returns the in-game username of the guild member that is the target of this command
	 * @return In-game username of the target member
	 */
	protected String getUserID() {
		return getUserID(getTargetMember());
	}
	protected String getUserID(Member member) {
		return Botmain.gdp.getID(getGuild(), member, gameName);
	}
	protected void sendNoIDMessage() {
		reply("This person's " + gameName + " ID has not been set. Use:\n"
				+ "`/" + GameStatsCommand.this.getName() + " setid *in-game id* @forthisperson` to set someone's ID");
	}
	protected boolean execute(String[] args) { 
		assert false;
		return false;
	}
	protected abstract boolean showStats(String[] args);
	protected abstract boolean lastGame(String[] args);
	protected abstract Void postPatchNotes(String value);
	private void startPatchNotesService() {
		ServiceCommand.createService("game services/" + gameName, this::postPatchNotes, 3600);
	}
}
