package dev.rndmorris.tfixins.common.commands.arguments.handlers;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

import dev.rndmorris.tfixins.common.commands.CommandErrors;
import thaumcraft.api.research.ResearchCategories;

public class ResearchKeyHandler implements IArgumentHandler {

    public static final IArgumentHandler INSTANCE = new ResearchKeyHandler();

    @Override
    public Object parse(ICommandSender sender, String current, Iterator<String> args) {
        if (!args.hasNext()) {
            CommandErrors.invalidSyntax();
        }
        final var keyName = args.next();

        final var results = allResearchKeys().filter(keyName::equals)
            .collect(Collectors.toList());
        if (results.isEmpty()) {
            throw new CommandException("tfixins:error.unknown_research", keyName);
        }

        return results.get(0);
    }

    @Override
    public List<String> getAutocompleteOptions(ICommandSender sender, String current, Iterator<String> args) {
        if (args.hasNext()) {
            args.next();
        }
        if (args.hasNext()) {
            return null;
        }
        return allResearchKeys().collect(Collectors.toList());
    }

    private Stream<String> allResearchKeys() {
        return ResearchCategories.researchCategories.values()
            .stream()
            .flatMap(
                c -> c.research.keySet()
                    .stream());
    }
}
