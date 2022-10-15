package BotTools.plugins;

import BotTools.main.Botmain;

@FunctionalInterface
public interface Plugin {
	public void add(Botmain toBot);
}
