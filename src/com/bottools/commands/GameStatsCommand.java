package com.bottools.commands;

import com.bottools.main.Botmain;

import net.dv8tion.jda.api.entities.Member;

public abstract class GameStatsCommand extends OnlineCommand {
	protected String gameName;
	protected OrganizationCommand show, id;
	protected GameStatsCommand(String gameName) {
		super(gameName);
		this.gameName = gameName;
		isSubCommandRequired(true);

		
		makeSlashCommand();

		new MethodCommand(this, "showstats", "Show the target player's stats", this::showStats);
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
	protected class SCSetID extends SubCommand{
		protected SCSetID() {
			super(GameStatsCommand.this, "setid");
			setHelpMessage("sets the game id of the mentioned user\n"
			+ "For example:\n"
			+ "`/" + GameStatsCommand.this.getName() + " setid *in-game id* @forthisperson` to set the ID for the mentioned user. Or\n"
			+ "`/" + GameStatsCommand.this.getName() + " setid *in-game id*` to set the ID for yourself");
			addOption("string", "id", "the steam/dotabuff id", true);
		}
		protected boolean execute(String[] args) { 
			Botmain.gdp.setGameID(getGuild(), getTargetMember(), gameName, args[0]);
			say("The " + gameName + " ID of " + getTargetMember().getEffectiveName() + " has been set to " + getUserID());
			return true;
		}
	}
	private final class SCGetID extends SubCommand{
		private SCGetID() {
			super(GameStatsCommand.this, "getid");
			addAlias("showid");
			setHelpMessage("Shows the recorded game ID for the mentioned user or for yourself if no user is mentioned");
		}
		protected boolean execute(String[] args) {
			String id = getUserID();
			if(id == null) 	sendNoIDMessage();
			else 			say("The in-game ID of " + getTargetMember().getUser().getName() + " is " + id);
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
		say("This person's " + gameName + " ID has not been set. Use:\n"
				+ "`/" + GameStatsCommand.this.getName() + " setid *in-game id* @forthisperson` to set someone's ID");
	}
	protected boolean execute(String[] args) { 
		assert false;
		return false;
	}
	protected abstract boolean showStats(String[] args);
	protected abstract boolean lastGame(String[] args);

	@Override
	protected boolean connect(String URL) {
		if(getUserID() == null) {
			sendNoIDMessage();
			return false;
		}
		return super.connect(URL);
	}
}
