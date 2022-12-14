package BotTools.commands;

import java.util.function.Function;

public class MethodCommand extends SubCommand {
	private final Function<String[], Boolean> onExecute;
	public MethodCommand(Command parent, String name, String description, final Function<String[], Boolean> onExecute) {
		super(parent, name);
		setHelpMessage(description);
		this.onExecute = onExecute;
	}
	public MethodCommand(Command parent, String name, final Function<String[], Boolean> onExecute) {
		this(parent, name, "default description", onExecute);
	}
	public MethodCommand(Command parent, final Function<String[], Boolean> onExecute) {
		this(parent, onExecute.toString(), onExecute);
	}

	@Override
	final protected boolean execute(String[] args) {
		return onExecute.apply(args);
	}

}
