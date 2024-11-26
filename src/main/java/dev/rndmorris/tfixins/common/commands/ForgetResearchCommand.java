package dev.rndmorris.tfixins.common.commands;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

import dev.rndmorris.tfixins.common.commands.arguments.ArgumentProcessor;
import dev.rndmorris.tfixins.common.commands.arguments.annotations.NamedArg;
import dev.rndmorris.tfixins.common.commands.arguments.handlers.FlagHandler;
import dev.rndmorris.tfixins.common.commands.arguments.handlers.IArgumentHandler;
import dev.rndmorris.tfixins.common.commands.arguments.handlers.PlayerHandler;
import dev.rndmorris.tfixins.common.commands.arguments.handlers.ResearchKeyHandler;
import dev.rndmorris.tfixins.config.FixinsConfig;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.common.Thaumcraft;

public class ForgetResearchCommand extends FixinsCommandBase<ForgetResearchCommand.Arguments> {

    public ForgetResearchCommand() {
        super(FixinsConfig.commandsModule.forgetResearch);
    }

    @Override
    protected ArgumentProcessor<Arguments> initializeProcessor() {
        return new ArgumentProcessor<>(
            Arguments.class,
            Arguments::new,
            new IArgumentHandler[] { PlayerHandler.INSTANCE, ResearchKeyHandler.INSTANCE, FlagHandler.INSTANCE, });
    }

    @Override
    protected void process(ICommandSender sender, String[] args) {
        final var arguments = argumentProcessor.process(sender, args);

        if (arguments.targetPlayer == null) {
            arguments.targetPlayer = getCommandSenderAsPlayer(sender);
        }
        if (arguments.researchKeys.isEmpty() && !arguments.allResearch) {
            CommandErrors.invalidSyntax();
        }

        // required to be a reference to the original list
        final var playerResearch = Thaumcraft.proxy.getPlayerKnowledge().researchCompleted
            .get(arguments.targetPlayer.getCommandSenderName());
        if (playerResearch == null) {
            return;
        }
        final var toForget = new ArrayDeque<String>();
        if (arguments.allResearch) {
            toForget.addAll(playerResearch);
        } else {
            toForget.addAll(arguments.researchKeys);
        }

        final var researchMap = playerResearch.stream()
            .filter(Objects::nonNull)
            .map(ResearchCategories::getResearch)
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(r -> r.key, r -> r));
        final var visited = new TreeSet<>(toForget);

        while (!toForget.isEmpty()) {
            final var key = toForget.poll();
            final var data = researchMap.get(key);

            if (data == null) {
                continue;
            }

            if (!data.isAutoUnlock()) {
                final var removeIndex = playerResearch.indexOf(key);
                if (removeIndex >= 0) {
                    playerResearch.remove(removeIndex);
                }
            }

            if (arguments.scalpel) {
                continue;
            }

            final Predicate<ResearchItem> isChildResearch = (r) -> {
                if (r.parents != null) {
                    for (var parent : r.parents) {
                        if (key.equals(parent)) {
                            return true;
                        }
                    }
                }
                if (r.parentsHidden != null) {
                    for (var parent : r.parentsHidden) {
                        if (key.equals(parent)) {
                            return true;
                        }
                    }
                }
                if (r.siblings != null) {
                    for (var sibling : r.siblings) {
                        if (key.equals(sibling)) {
                            return true;
                        }
                    }
                }
                return false;
            };

            researchMap.values()
                .stream()
                .filter(r -> !visited.contains(r.key) && isChildResearch.test(r))
                .forEach(r -> {
                    toForget.add(r.key);
                    visited.add(r.key);
                });
        }

    }

    public static class Arguments {

        @NamedArg(name = "--all", handler = FlagHandler.class, excludes = {"--research-key", "--scalpel"}, descLangKey = "all")
        public boolean allResearch;

        @NamedArg(
            name = "--research-key",
            handler = ResearchKeyHandler.class,
            excludes = "--all",
            descLangKey = "research")
        public List<String> researchKeys = new ArrayList<>();

        @NamedArg(name = "--player", handler = PlayerHandler.class, descLangKey = "player")
        public EntityPlayerMP targetPlayer;

        @NamedArg(name = "--scalpel", handler = FlagHandler.class, excludes = {"--all"}, descLangKey = "scalpel")
        public boolean scalpel;
    }
}
