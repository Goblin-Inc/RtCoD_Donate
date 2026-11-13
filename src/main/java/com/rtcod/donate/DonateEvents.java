package com.rtcod.donate;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DonateEvents {

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            DonateMenus.tickSpins();
        }
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        // /donate — Главное меню доната
        event.getDispatcher().register(Commands.literal("donate")
                .executes(ctx -> {
                    DonateMenus.openMainHub(ctx.getSource().getPlayerOrException());
                    return Command.SINGLE_SUCCESS;
                }));

        // /kits — Меню китов
        event.getDispatcher().register(Commands.literal("kits")
                .executes(ctx -> {
                    DonateMenus.openKitsMenu(ctx.getSource().getPlayerOrException());
                    return Command.SINGLE_SUCCESS;
                }));

        // /kit [start|vip|premium|lord] — Быстрое получение кита или открытие меню
        event.getDispatcher().register(Commands.literal("kit")
                .executes(ctx -> {
                    DonateMenus.openKitsMenu(ctx.getSource().getPlayerOrException());
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.argument("name", StringArgumentType.word())
                        .executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            String name = StringArgumentType.getString(ctx, "name").toLowerCase();
                            switch (name) {
                                case "start" -> DonateMenus.claimKit(p, "start", "default", 3600_000L);
                                case "vip" -> DonateMenus.claimKit(p, "vip", "vip", 12 * 3600_000L);
                                case "premium" -> DonateMenus.claimKit(p, "premium", "premium", 24 * 3600_000L);
                                case "lord" -> DonateMenus.claimKit(p, "lord", "lord", 48 * 3600_000L);
                                default -> p.sendSystemMessage(Component.literal("§cДоступные киты: start, vip, premium, lord"));
                            }
                            return Command.SINGLE_SUCCESS;
                        })));

        // /cases — Меню кейсов
        event.getDispatcher().register(Commands.literal("cases")
                .executes(ctx -> {
                    DonateMenus.openCasesMenu(ctx.getSource().getPlayerOrException());
                    return Command.SINGLE_SUCCESS;
                }));

        // /donadmin — Админ-команды (OP уровень 2 или консоль сервера / RCON сайта)
        event.getDispatcher().register(Commands.literal("donadmin")
                .requires(s -> s.hasPermission(2))
                .then(Commands.literal("setrank")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("rank", StringArgumentType.word())
                                        .executes(ctx -> {
                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                            String rank = StringArgumentType.getString(ctx, "rank").toLowerCase();
                                            DonateData.get(ctx.getSource().getServer()).setRank(target.getUUID(), rank);
                                            ctx.getSource().sendSuccess(() -> Component.literal("§aИгроку " + target.getGameProfile().getName() + " выдан статус: " + rank.toUpperCase()), true);
                                            target.sendSystemMessage(Component.literal("§6✦ Вам выдана привилегия: §e" + rank.toUpperCase() + "§6! Откройте §e/donate"));
                                            return Command.SINGLE_SUCCESS;
                                        }))))
                .then(Commands.literal("givekey")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("caseType", StringArgumentType.word())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 1000))
                                                .executes(ctx -> {
                                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                                    String cType = StringArgumentType.getString(ctx, "caseType").toLowerCase();
                                                    int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                                    DonateData.get(ctx.getSource().getServer()).addKeys(target.getUUID(), cType, amount);
                                                    ctx.getSource().sendSuccess(() -> Component.literal("§aВыдано " + amount + " ключей (" + cType + ") игроку " + target.getGameProfile().getName()), true);
                                                    target.sendSystemMessage(Component.literal("§d✦ Вы получили §e" + amount + " шт. §dключей от кейса «" + cType.toUpperCase() + "»! Введите §e/cases"));
                                                    return Command.SINGLE_SUCCESS;
                                                }))))));
    }
}