package com.rtcod.donate;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DonateMenus {
    private static final Map<UUID, SpinSession> ACTIVE_SPINS = new ConcurrentHashMap<>();

    public static void openMainHub(ServerPlayer player) {
        SimpleContainer container = new SimpleContainer(27);
        fillBorder(container, Items.BLACK_STAINED_GLASS_PANE);

        DonateData data = DonateData.get(player.server);
        String rank = data.getRank(player.getUUID()).toUpperCase();

        container.setItem(11, ItemResolver.makeIcon(Items.NETHERITE_CHESTPLATE, "§6§l⚔ НАБОРЫ (КИТЫ)", List.of(
                "§7Нажмите, чтобы открыть меню наборов.",
                "§7Ваш текущий статус: §e" + rank,
                "",
                "§a▶ Нажмите ЛКМ для перехода"
        )));

        container.setItem(13, ItemResolver.makeIcon(Items.NETHER_STAR, "§d§l✦ ПРОФИЛЬ ДОНАТА", List.of(
                "§7Игрок: §f" + player.getGameProfile().getName(),
                "§7Привилегия: §b" + rank,
                "§7Ключи Обычного кейса: §a" + data.getKeys(player.getUUID(), "common"),
                "§7Ключи Мифического кейса: §6" + data.getKeys(player.getUUID(), "mythic"),
                "§7Ключи Пустотного кейса: §5" + data.getKeys(player.getUUID(), "void")
        )));

        container.setItem(15, ItemResolver.makeIcon(Items.ENDER_CHEST, "§5§l🎲 МИСТИЧЕСКИЕ КЕЙСЫ", List.of(
                "§7Испытайте удачу и выбейте легендарное",
                "§7снаряжение из измерения Умирающего Королевства!",
                "",
                "§d▶ Нажмите ЛКМ для перехода"
        )));

        player.openMenu(new SimpleMenuProvider((id, inv, p) -> new LockedChestMenu(MenuType.GENERIC_9x3, id, inv, container, 3) {
            @Override
            protected void onSlotClick(int slot, ServerPlayer sp) {
                if (slot == 11) openKitsMenu(sp);
                else if (slot == 15) openCasesMenu(sp);
            }
        }, Component.literal("§8✦ Донат-Меню RtCoD ✦")));
    }

    public static void openKitsMenu(ServerPlayer player) {
        SimpleContainer container = new SimpleContainer(27);
        fillBorder(container, Items.GRAY_STAINED_GLASS_PANE);

        setupKitIcon(container, 10, player, "start", "default", 3600_000L, Items.IRON_SWORD, "§a§lКит «Стартовый»", "Каждые 60 мин");
        setupKitIcon(container, 12, player, "vip", "vip", 12 * 3600_000L, Items.GOLDEN_SWORD, "§6§lКит «VIP Страж»", "Каждые 12 часов (Пепельный сет)");
        setupKitIcon(container, 14, player, "premium", "premium", 24 * 3600_000L, Items.DIAMOND_SWORD, "§c§lКит «PREMIUM Охотник»", "Раз в 24 часа (Сет Кровавой Луны)");
        setupKitIcon(container, 16, player, "lord", "lord", 48 * 3600_000L, Items.NETHERITE_SWORD, "§5§lКит «LORD Полководец»", "Раз в 48 часов (Обсидиановый сет + Ключ)");

        container.setItem(22, ItemResolver.makeIcon(Items.ARROW, "§c◀ Назад в главное меню", List.of()));

        player.openMenu(new SimpleMenuProvider((id, inv, p) -> new LockedChestMenu(MenuType.GENERIC_9x3, id, inv, container, 3) {
            @Override
            protected void onSlotClick(int slot, ServerPlayer sp) {
                if (slot == 10) claimKit(sp, "start", "default", 3600_000L);
                else if (slot == 12) claimKit(sp, "vip", "vip", 12 * 3600_000L);
                else if (slot == 14) claimKit(sp, "premium", "premium", 24 * 3600_000L);
                else if (slot == 16) claimKit(sp, "lord", "lord", 48 * 3600_000L);
                else if (slot == 22) openMainHub(sp);
            }
        }, Component.literal("§8⚔ Выбор Набора (Кита)")));
    }

    private static void setupKitIcon(SimpleContainer c, int slot, ServerPlayer p, String kitId, String reqRank, long cdMs, net.minecraft.world.item.Item icon, String title, String desc) {
        DonateData data = DonateData.get(p.server);
        boolean hasPerm = data.hasRankPermission(p.getUUID(), reqRank);
        long remMs = data.getRemainingCooldownMs(p.getUUID(), kitId, cdMs);
        String status = !hasPerm ? "§c✖ Требуется статус " + reqRank.toUpperCase()
                : (remMs > 0 ? "§e⏳ Перезарядка: " + (remMs / 60000 + 1) + " мин." : "§a✔ Доступно для получения!");

        c.setItem(slot, ItemResolver.makeIcon(icon, title, List.of(
                "§7" + desc,
                "§7Включает броню, оружие и ресурсы.",
                "",
                status
        )));
    }

    public static void claimKit(ServerPlayer player, String kitId, String reqRank, long cdMs) {
        DonateData data = DonateData.get(player.server);
        if (!data.hasRankPermission(player.getUUID(), reqRank)) {
            player.sendSystemMessage(Component.literal("§c[RtCoD] У вас нет привилегии " + reqRank.toUpperCase() + " для этого набора!"));
            player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1f, 1f);
            return;
        }
        long rem = data.getRemainingCooldownMs(player.getUUID(), kitId, cdMs);
        if (rem > 0) {
            player.sendSystemMessage(Component.literal("§e[RtCoD] Набор перезаряжается! Осталось: " + (rem / 60000 + 1) + " мин."));
            player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1f, 1f);
            return;
        }
        List<ItemStack> items = ItemResolver.getKitItems(kitId);
        if (items.isEmpty()) return;

        data.markKitUsed(player.getUUID(), kitId);
        for (ItemStack st : items) {
            ItemResolver.giveStack(player, st);
        }
        player.closeContainer();
        player.playNotifySound(SoundEvents.ARMOR_EQUIP_NETHERITE, SoundSource.PLAYERS, 1f, 1f);
        player.sendSystemMessage(Component.literal("§a[RtCoD] Вы успешно получили набор «" + kitId.toUpperCase() + "»!"));
    }

    public static void openCasesMenu(ServerPlayer player) {
        SimpleContainer container = new SimpleContainer(27);
        fillBorder(container, Items.PURPLE_STAINED_GLASS_PANE);
        DonateData data = DonateData.get(player.server);

        container.setItem(11, ItemResolver.makeIcon(Items.CHEST, "§a§l📦 Обычный Кейс Стража", List.of(
                "§7Содержит Пепельное снаряжение, алмазы,",
                "§7незерит, тотемы и золотые яблоки.",
                "",
                "§fВаши ключи: §a" + data.getKeys(player.getUUID(), "common") + " шт.",
                "§e▶ Нажмите, чтобы крутить!"
        )));

        container.setItem(13, ItemResolver.makeIcon(Items.ENDER_CHEST, "§6§l🔥 Мифический Кейс Королевства", List.of(
                "§7Содержит редкое оружие и броню Боссов",
                "§7(Кровавая Луна, Ледяной Лич, Король Утопших) и Ключ Мира!",
                "",
                "§fВаши ключи: §6" + data.getKeys(player.getUUID(), "mythic") + " шт.",
                "§e▶ Нажмите, чтобы крутить!"
        )));

        container.setItem(15, ItemResolver.makeIcon(Items.CRYING_OBSIDIAN, "§5§l✦ КЕЙС ПУСТОТНОГО ВЛАСТЕЛИНА ✦", List.of(
                "§7Высший тир! Оружие и броня Финального Босса",
                "§7с зачарованиями Острота VI и Защита V!",
                "",
                "§fВаши ключи: §d" + data.getKeys(player.getUUID(), "void") + " шт.",
                "§e▶ Нажмите, чтобы крутить!"
        )));

        container.setItem(22, ItemResolver.makeIcon(Items.ARROW, "§c◀ Назад в главное меню", List.of()));

        player.openMenu(new SimpleMenuProvider((id, inv, p) -> new LockedChestMenu(MenuType.GENERIC_9x3, id, inv, container, 3) {
            @Override
            protected void onSlotClick(int slot, ServerPlayer sp) {
                if (slot == 11) startCaseSpin(sp, "common", "Обычный Кейс");
                else if (slot == 13) startCaseSpin(sp, "mythic", "Мифический Кейс");
                else if (slot == 15) startCaseSpin(sp, "void", "Кейс Пустоты");
                else if (slot == 22) openMainHub(sp);
            }
        }, Component.literal("§8🎲 Мистические Кейсы RtCoD")));
    }

    public static void startCaseSpin(ServerPlayer player, String caseType, String title) {
        DonateData data = DonateData.get(player.server);
        if (!data.consumeKey(player.getUUID(), caseType)) {
            player.sendSystemMessage(Component.literal("§c[RtCoD] У вас нет ключей от кейса «" + title + "»!"));
            player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1f, 1f);
            return;
        }

        SimpleContainer spinContainer = new SimpleContainer(27);
        fillBorder(spinContainer, Items.BLACK_STAINED_GLASS_PANE);
        // Указатели выигрышного слота сверху и снизу (слоты 4 и 22)
        spinContainer.setItem(4, ItemResolver.makeIcon(Items.HOPPER, "§e§l▼ ВАШ ВЫИГРЫШ ▼", List.of()));
        spinContainer.setItem(22, ItemResolver.makeIcon(Items.GOLD_BLOCK, "§e§l▲ ВАШ ВЫИГРЫШ ▲", List.of()));

        SpinSession session = new SpinSession(player, caseType, title, spinContainer);
        ACTIVE_SPINS.put(player.getUUID(), session);

        player.openMenu(new SimpleMenuProvider((id, inv, p) -> new LockedChestMenu(MenuType.GENERIC_9x3, id, inv, spinContainer, 3) {
            @Override
            protected void onSlotClick(int slot, ServerPlayer sp) {}
        }, Component.literal("§8🎰 Прокрутка: " + title)));
    }

    public static void tickSpins() {
        Iterator<Map.Entry<UUID, SpinSession>> it = ACTIVE_SPINS.entrySet().iterator();
        while (it.hasNext()) {
            SpinSession s = it.next().getValue();
            if (s.tick()) {
                it.remove();
            }
        }
    }

    private static void fillBorder(SimpleContainer c, net.minecraft.world.item.Item pane) {
        ItemStack glass = ItemResolver.makeIcon(pane, " ", List.of());
        for (int i = 0; i < c.getContainerSize(); i++) {
            c.setItem(i, glass);
        }
    }

    // Сессия анимации рулетки
    private static class SpinSession {
        private final ServerPlayer player;
        private final String caseType;
        private final String title;
        private final SimpleContainer container;
        private final ItemStack[] tape = new ItemStack[9];
        private int ticks = 0;

        public SpinSession(ServerPlayer player, String caseType, String title, SimpleContainer container) {
            this.player = player;
            this.caseType = caseType;
            this.title = title;
            this.container = container;
            for (int i = 0; i < 9; i++) {
                tape[i] = ItemResolver.rollPrize(caseType);
            }
            renderTape();
        }

        public boolean tick() {
            if (!player.isAlive() || player.hasDisconnected()) {
                // Если игрок вышел во время прокрутки, сразу выдаём приз
                ItemResolver.giveStack(player, tape[4]);
                return true;
            }
            ticks++;
            // Замедление барабана: сначала каждые 2 тика, потом каждые 4, потом каждые 8
            boolean shouldShift = (ticks <= 40 && ticks % 2 == 0)
                    || (ticks > 40 && ticks <= 75 && ticks % 4 == 0)
                    || (ticks > 75 && ticks <= 105 && ticks % 7 == 0);

            if (shouldShift) {
                for (int i = 0; i < 8; i++) {
                    tape[i] = tape[i + 1];
                }
                tape[8] = ItemResolver.rollPrize(caseType);
                renderTape();
                player.playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.7f, 1.3f);
            }

            if (ticks >= 115) {
                ItemStack prize = tape[4].copy();
                ItemResolver.giveStack(player, prize);
                player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0f, 1.0f);
                player.closeContainer();
                player.server.getPlayerList().broadcastSystemMessage(
                        Component.literal("§6[Кейсы RtCoD] §e" + player.getGameProfile().getName()
                                + " §fоткрыл §d«" + title + "» §fи выбил: ")
                                .append(prize.getDisplayName().copy().withStyle(ChatFormatting.AQUA))
                                .append(Component.literal(" §7(x" + prize.getCount() + ")")),
                        false
                );
                return true;
            }
            return false;
        }

        private void renderTape() {
            for (int i = 0; i < 9; i++) {
                container.setItem(9 + i, tape[i]);
            }
        }
    }

    // Защищённый контейнер, из которого нельзя украсть иконки мышкой или Shift-кликом
    private abstract static class LockedChestMenu extends ChestMenu {
        public LockedChestMenu(MenuType<?> type, int id, Inventory playerInv, SimpleContainer container, int rows) {
            super(type, id, playerInv, container, rows);
        }

        protected abstract void onSlotClick(int slotIndex, ServerPlayer player);

        @Override
        public void clicked(int slotId, int button, ClickType clickType, Player player) {
            if (slotId >= 0 && slotId < 27 && player instanceof ServerPlayer sp) {
                onSlotClick(slotId, sp);
            }
            // Любое перемещение предметов внутри GUI блокируется
            this.broadcastChanges();
        }

        @Override
        public ItemStack quickMoveStack(Player player, int index) {
            return ItemStack.EMPTY;
        }
    }
}