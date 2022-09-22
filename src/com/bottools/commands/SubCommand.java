package com.bottools.commands;

import com.bottools.commands.slashcommands.Option;
import com.bottools.main.Parser.CommandContainer;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

public abstract class SubCommand extends Command {
	private Command parent;
	protected SubCommand(Command parent, String name) {
		super();
		this.parent = parent;
		parent.addSubCommand(name, this);
		String helpMessage = name + " is a sub-command of the " + parent.getName() + " command\n";
		setHelpMessage(helpMessage);
	}
	@Override
	protected void addAlias(String...aliases) {
		for(String alias: aliases)
			parent.addSubCommand(alias, this);
	}
	
	protected Command getParent() {
		return parent;
	}
	
	@Override
	public boolean handle(CommandContainer data) {
		messageEvent = parent.messageEvent;
		return super.handle(data);
	}
	
	public static abstract class SlashSC extends SubCommand {
		SubcommandData slashSCData;
		protected SlashSC(Command parent, String name) {
			super(parent, name);
			slashSCData = new SubcommandData(name, getHelpMessage());
			boolean parentIsSCG = SlashSCGroup.class.isAssignableFrom(parent.getClass());
			Object o = !parentIsSCG ? parent.slashCommandData.addSubcommands(slashSCData) : ((SlashSCGroup)parent).slashCommandData.addSubcommands(slashSCData);
			
		}
		@Override
		public Command addOption(String type, String name, String description, boolean required) {
			slashSCData.addOptions(new Option(type, name, description, required));
			return this;
		}
	}
	
	static public abstract class SlashSCGroup extends DelegateCommand {
		protected SubcommandGroupData slashCommandData;
		protected SlashSCGroup(Command parent, String name) {
			super(parent, name);
			slashCommandData = new SubcommandGroupData(name, getHelpMessage());
			parent.slashCommandData.addSubcommandGroups(slashCommandData);
		}
		@Override
		public Command addOption(String type, String name, String description, boolean required) {
			assert false;
			return null;
		}
	}
	
}

