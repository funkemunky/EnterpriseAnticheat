package dev.brighten.ac.command;

import co.aikar.commands.*;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import dev.brighten.ac.Anticheat;
import dev.brighten.ac.check.Check;
import dev.brighten.ac.check.CheckData;
import dev.brighten.ac.data.APlayer;
import dev.brighten.ac.handler.BBRevealHandler;
import dev.brighten.ac.messages.Messages;
import dev.brighten.ac.packet.handler.HandlerAbstract;
import dev.brighten.ac.utils.*;
import dev.brighten.ac.utils.annotation.Init;
import dev.brighten.ac.utils.msg.ChatBuilder;
import dev.brighten.ac.utils.reflections.Reflections;
import dev.brighten.ac.utils.reflections.types.WrappedClass;
import dev.brighten.ac.utils.reflections.types.WrappedMethod;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import lombok.SneakyThrows;
import lombok.val;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.zip.CRC32;

@Init(priority = Priority.LOW)
@CommandAlias("kauri|anticheat|ac")
@CommandPermission("anticheat.command")
public class AnticheatCommand extends BaseCommand {

    public AnticheatCommand() {
        BukkitCommandCompletions cc = (BukkitCommandCompletions) Anticheat.INSTANCE.getCommandManager()
                .getCommandCompletions();

        cc.registerCompletion("checks", (c) -> Anticheat.INSTANCE.getCheckManager().getCheckClasses().keySet()
                .stream()  .sorted(Comparator.naturalOrder())
                .map(name -> name.replace(" ", "_")).collect(Collectors.toList()));

        cc.registerCompletion("checkIds", (c) -> Anticheat.INSTANCE.getCheckManager().getCheckClasses().values()
                .stream().map(s -> s.getCheckClass().getAnnotation(CheckData.class).checkId())
                .sorted(Comparator.naturalOrder()).collect(Collectors.toList()));

        BukkitCommandContexts contexts = (BukkitCommandContexts) Anticheat.INSTANCE.getCommandManager()
                .getCommandContexts();

        contexts.registerOptionalContext(Integer.class, c -> {
            String arg = c.popFirstArg();

            if(arg == null) return null;
            try {
                return Integer.parseInt(arg);
            } catch(NumberFormatException e) {
                throw new InvalidCommandArgument(String.format(Color.Red
                        + "Argument \"%s\" is not an integer", arg));
            }
        });

        contexts.registerOptionalContext(APlayer.class, c -> {
            if(c.hasFlag("other")) {
                String arg = c.popFirstArg();

                Player onlinePlayer = Bukkit.getPlayer(arg);

                if(onlinePlayer != null) {
                    return Anticheat.INSTANCE.getPlayerRegistry().getPlayer(onlinePlayer.getUniqueId())
                            .orElse(null);
                } else return null;
            } else {
                CommandSender sender = c.getSender();
                
                if(sender instanceof Player) {
                    return Anticheat.INSTANCE.getPlayerRegistry().getPlayer(((Player) sender).getUniqueId())
                            .orElse(null);
                }
                else if(!c.isOptional()) throw new InvalidCommandArgument(MessageKeys.NOT_ALLOWED_ON_CONSOLE,
                        false, new String[0]);
                else return null;
            }
        });
    }

    @HelpCommand
    @Syntax("")
    @Description("View the help page")
    public void onHelp(CommandSender sender, CommandHelp help) {
        sender.sendMessage(MiscUtils.line(Color.Dark_Gray));
        help.showHelp();
        sender.sendMessage(MiscUtils.line(Color.Dark_Gray));
    }

    @Subcommand("alerts")
    @CommandPermission("anticheat.command.alerts")
    @Description("Toggle anticheat alerts")
    public void onAlerts(Player pl) {
        APlayer player = Anticheat.INSTANCE.getPlayerRegistry().getPlayer(pl.getUniqueId()).orElse(null);

        if(player == null) {
            pl.spigot().sendMessage(Messages.NULL_APLAYER);
            return;
        }

        if(Check.alertsEnabled.contains(player.getBukkitPlayer().getUniqueId())) {
            Check.alertsEnabled.remove(player.getBukkitPlayer().getUniqueId());
            pl.spigot().sendMessage(Messages.ALERTS_OFF);
        } else {
            Check.alertsEnabled.add(player.getBukkitPlayer().getUniqueId());
            pl.spigot().sendMessage(Messages.ALERTS_ON);
        }
    }
    @Subcommand("wand")
    @CommandPermission("anticheat.command.wand")
    @Description("Receive a magic bounding box wand")
    public void onWand(Player player) {
        BBRevealHandler.INSTANCE.giveWand(player);
        player.spigot().sendMessage(new ComponentBuilder(
                "You've been given a very special wand. Handle it responsibly.").color(ChatColor.GREEN).create());
    }

    @Subcommand("title")
    @Private
    public void onTitle(CommandSender sender, OnlinePlayer target, String title) {
        PacketPlayOutTitle packetSubtitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE,
                CraftChatMessage.fromString(Color.translate(title))[0]);
        HandlerAbstract.getHandler().sendPacketSilently(target.getPlayer(), packetSubtitle);
        sender.sendMessage(Color.Green + "Sent title!");
    }

    @Subcommand("playerinfo|info|pi")
    @Description("Get player's information")
    @Syntax("[player]")
    @CommandCompletion("@players")
    @CommandPermission("anticheat.command.info")
    public void onCommand(CommandSender sender, @Single APlayer player) {
        Anticheat.INSTANCE.getScheduler().execute(() -> {

            if(player == null) {
                sender.sendMessage(TextComponent.toLegacyText(Messages.NULL_APLAYER));
                return;
            }

            sender.sendMessage(MiscUtils.line(Color.Dark_Gray));
            sender.sendMessage(Color.translate("&6&lPing&8: &f" + player.getLagInfo().getTransPing() * 50 + "ms"));
            sender.sendMessage(Color.translate("&6&lVersion&8: &f" + player.getPlayerVersion().name()));
            sender.sendMessage(Color.translate("&6&lSensitivity&8: &f" + player.getMovement().getSensXPercent() + "%"));
            sender.sendMessage(MiscUtils.line(Color.Dark_Gray));
        });
    }

    @Subcommand("debug")
    @CommandCompletion("@checks|none @players")
    @Description("Debug a player")
    @Syntax("[check] [player]")
    @CommandPermission("anticheat.command.debug")
    public void onDebug(Player sender, @Single String check, @Optional OnlinePlayer targetPlayer) {
        Player target = targetPlayer != null ? targetPlayer.player : sender;
        switch (check.toLowerCase()) {
            case "none": {
                synchronized (Check.debugInstances) {
                    Check.debugInstances.forEach((nameKey, list) -> {
                        val iterator = list.iterator();
                        while(iterator.hasNext()) {
                            val tuple = iterator.next();

                            if(tuple.two.equals(target.getUniqueId())) {
                                iterator.remove();
                                sender.spigot()
                                        .sendMessage(new ChatBuilder(
                                                "&cTurned off debug for check &f%s &con target &f%s", nameKey,
                                                target.getName()).build());
                            }
                        }
                    });
                }
                break;
            }
            case "sniff": {
                APlayer targetData = Anticheat.INSTANCE.getPlayerRegistry().getPlayer(target.getUniqueId()).orElse(null);

                if(targetData != null) {
                    if(targetData.sniffing) {
                        targetData.sniffing = false;
                        sender.sendMessage(Color.Red + "Stopped sniff. Pasting...");
                        try {
                            sender.sendMessage(Color.Gray + "Paste: " + Color.White + Pastebin.makePaste(
                                    String.join("\n", targetData.sniffedPackets.toArray(new String[0])),
                                    "Sniffed from " + target.getName(), Pastebin.Privacy.UNLISTED));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        targetData.sniffedPackets.clear();
                    } else {
                        targetData.sniffing = true;
                        sender.sendMessage(Color.Green + "Started packet sniff on " + target.getName() + "!");
                    }
                } else {
                    sender.spigot().sendMessage(Messages.NULL_APLAYER);
                }
                break;
            }
            default: {
                if(!Anticheat.INSTANCE.getCheckManager().isCheck(check)) {
                    sender.sendMessage(Color.Red + "Check \"" + check + "\" is not a valid check!");
                    return;
                }
                synchronized (Check.debugInstances) {
                    Check.debugInstances.compute(check.replace("_", " "), (key, list) -> {
                        if(list == null) list = new ArrayList<>();

                        list.add(new Tuple<>(target.getUniqueId(), sender.getUniqueId()));

                        return list;
                    });

                    sender.spigot()
                            .sendMessage(new ChatBuilder(
                                    "&aTurned on debug for check &f%s &aon target &f%s",
                                    check.replace("_", " "),
                                    target.getName()).build());
                }
                break;
            }
        }
    }
}
