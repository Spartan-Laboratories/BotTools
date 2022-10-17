package BotTools.plugins;

import java.util.List;
import java.util.function.Supplier;

import BotTools.commands.Command;

@FunctionalInterface
public interface Plugin extends Supplier<List<Command>>{
}
