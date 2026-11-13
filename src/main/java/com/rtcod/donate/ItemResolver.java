package com.rtcod.donate;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ItemResolver {
    private static final Random RANDOM = new Random();

    public record LootEntry(String itemId, int count, int weight, String customName, Map<Enchantment, Integer> enchants) {}

    public static ItemStack resolve(String id, int count, String customName, Map<Enchantment, Integer> enchants) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
        if (item == null || item == Items.AIR) {
            item = Items.NETHERITE_SCRAP; // Резерв, если мод не установлен
        }
        ItemStack stack = new ItemStack(item, count);
        if (customName != null && !customName.isEmpty()) {
            stack.setHoverName(Component.literal(customName));
        }
        if (enchants != null) {
            enchants.forEach(stack::enchant);
        }
        return stack;
    }

    public static ItemStack makeIcon(Item item, String title, List<String> loreLines) {
        ItemStack stack = new ItemStack(item);
        stack.setHoverName(Component.literal(title));
        CompoundTag display = stack.getOrCreateTagElement("display");
        ListTag lore = new ListTag();
        for (String line : loreLines) {
            lore.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(line))));
        }
        display.put("Lore", lore);
        return stack;
    }

    public static void giveStack(ServerPlayer player, ItemStack stack) {
        ItemStack copy = stack.copy();
        if (!player.getInventory().add(copy)) {
            player.drop(copy, false);
        }
    }

    // ==================== КИТЫ (НАБОРЫ) ====================
    public static List<ItemStack> getKitItems(String kitId) {
        List<ItemStack> list = new ArrayList<>();
        switch (kitId.toLowerCase()) {
            case "start" -> {
                list.add(resolve("minecraft:iron_sword", 1, "§7Меч Странника", Map.of(Enchantments.SHARPNESS, 2, Enchantments.UNBREAKING, 2)));
                list.add(resolve("minecraft:iron_pickaxe", 1, null, Map.of(Enchantments.BLOCK_EFFICIENCY, 2)));
                list.add(resolve("minecraft:iron_chestplate", 1, null, Map.of(Enchantments.ALL_DAMAGE_PROTECTION, 1)));
                list.add(resolve("minecraft:iron_leggings", 1, null, Map.of(Enchantments.ALL_DAMAGE_PROTECTION, 1)));
                list.add(resolve("minecraft:cooked_beef", 32, null, null));
                list.add(resolve("minecraft:golden_apple", 4, null, null));
            }
            case "vip" -> {
                list.add(resolve("darkrealm:ashen_sword", 1, "§6✦ Пепельный Клинок [VIP]", Map.of(Enchantments.SHARPNESS, 4, Enchantments.UNBREAKING, 3)));
                list.add(resolve("darkrealm:ashen_helmet", 1, null, Map.of(Enchantments.ALL_DAMAGE_PROTECTION, 3)));
                list.add(resolve("darkrealm:ashen_chestplate", 1, null, Map.of(Enchantments.ALL_DAMAGE_PROTECTION, 3)));
                list.add(resolve("darkrealm:ashen_leggings", 1, null, Map.of(Enchantments.ALL_DAMAGE_PROTECTION, 3)));
                list.add(resolve("darkrealm:ashen_boots", 1, null, Map.of(Enchantments.ALL_DAMAGE_PROTECTION, 3)));
                list.add(resolve("darkrealm:ashen_essence", 8, null, null));
                list.add(resolve("minecraft:golden_apple", 12, null, null));
                list.add(resolve("minecraft:experience_bottle", 32, null, null));
            }
            case "premium" -> {
                list.add(resolve("darkrealm:bloodmoon_scythe", 1, "§c✦ Коса Кровавой Луны [PREMIUM]", Map.of(Enchantments.SHARPNESS, 5, Enchantments.UNBREAKING, 3, Enchantments.MOB_LOOTING, 3)));
                list.add(resolve("darkrealm:bloodmoon_helmet", 1, null, Map.of(Enchantments.ALL_DAMAGE_PROTECTION, 4, Enchantments.UNBREAKING, 3)));
                list.add(resolve("darkrealm:bloodmoon_chestplate", 1, null, Map.of(Enchantments.ALL_DAMAGE_PROTECTION, 4, Enchantments.UNBREAKING, 3)));
                list.add(resolve("darkrealm:bloodmoon_leggings", 1, null, Map.of(Enchantments.ALL_DAMAGE_PROTECTION, 4, Enchantments.UNBREAKING, 3)));
                list.add(resolve("darkrealm:bloodmoon_boots", 1, null, Map.of(Enchantments.ALL_DAMAGE_PROTECTION, 4, Enchantments.UNBREAKING, 3)));
                list.add(resolve("darkrealm:bloodmoon_essence", 16, null, null));
                list.add(resolve("minecraft:enchanted_golden_apple", 2, null, null));
                list.add(resolve("minecraft:totem_of_undying", 1, null, null));
            }
            case "lord" -> {
                list.add(resolve("darkrealm:obsidian_hammer", 1, "§5✦ Молот Владыки [LORD]", Map.of(Enchantments.SHARPNESS, 5, Enchantments.UNBREAKING, 4, Enchantments.FIRE_ASPECT, 2)));
                list.add(resolve("darkrealm:obsidian_scythe", 1, "§5✦ Жнец Цитадели [LORD]", Map.of(Enchantments.SHARPNESS, 5, Enchantments.MOB_LOOTING, 3)));
                list.add(resolve("darkrealm:obsidian_helmet", 1, null, Map.of(Enchantments.ALL_DAMAGE_PROTECTION, 4, Enchantments.UNBREAKING, 3, Enchantments.MENDING, 1)));
                list.add(resolve("darkrealm:obsidian_chestplate", 1, null, Map.of(Enchantments.ALL_DAMAGE_PROTECTION, 4, Enchantments.UNBREAKING, 3, Enchantments.MENDING, 1)));
                list.add(resolve("darkrealm:obsidian_leggings", 1, null, Map.of(Enchantments.ALL_DAMAGE_PROTECTION, 4, Enchantments.UNBREAKING, 3, Enchantments.MENDING, 1)));
                list.add(resolve("darkrealm:obsidian_boots", 1, null, Map.of(Enchantments.ALL_DAMAGE_PROTECTION, 4, Enchantments.UNBREAKING, 3, Enchantments.MENDING, 1)));
                list.add(resolve("darkrealm:realm_key", 1, null, null));
                list.add(resolve("darkrealm:obsidian_essence", 24, null, null));
                list.add(resolve("minecraft:enchanted_golden_apple", 6, null, null));
                list.add(resolve("minecraft:totem_of_undying", 2, null, null));
            }
        }
        return list;
    }

    // ==================== ПУЛЫ КЕЙСОВ ====================
    public static List<LootEntry> getCasePool(String caseType) {
        return switch (caseType.toLowerCase()) {
            case "mythic" -> List.of(
                    new LootEntry("darkrealm:frost_scythe", 1, 18, "§b❄ Ледяная Коса Лича", Map.of(Enchantments.SHARPNESS, 5, Enchantments.UNBREAKING, 3)),
                    new LootEntry("darkrealm:drowned_greataxe", 1, 18, "§2⚓ Секира Короля Утопших", Map.of(Enchantments.SHARPNESS, 5, Enchantments.UNBREAKING, 3)),
                    new LootEntry("darkrealm:bloodmoon_dagger", 1, 20, "§c🩸 Кинжал Охотницы", Map.of(Enchantments.SHARPNESS, 5, Enchantments.MOB_LOOTING, 3)),
                    new LootEntry("darkrealm:frost_chestplate", 1, 15, null, Map.of(Enchantments.ALL_DAMAGE_PROTECTION, 4, Enchantments.UNBREAKING, 3)),
                    new LootEntry("darkrealm:drowned_chestplate", 1, 15, null, Map.of(Enchantments.ALL_DAMAGE_PROTECTION, 4, Enchantments.UNBREAKING, 3)),
                    new LootEntry("darkrealm:bloodmoon_essence", 16, 25, null, null),
                    new LootEntry("darkrealm:frost_essence", 16, 25, null, null),
                    new LootEntry("minecraft:netherite_ingot", 6, 20, null, null),
                    new LootEntry("minecraft:enchanted_golden_apple", 3, 15, null, null),
                    new LootEntry("darkrealm:realm_key", 1, 8, "§d🔑 Ключ Умирающего Королевства", null)
            );
            case "void" -> List.of(
                    new LootEntry("darkrealm:void_scythe", 1, 8, "§5§l✦ ПУСТОТНАЯ КОСА ВЛАСТЕЛИНА ✦", Map.of(Enchantments.SHARPNESS, 6, Enchantments.UNBREAKING, 4, Enchantments.MENDING, 1)),
                    new LootEntry("darkrealm:void_hammer", 1, 8, "§5§l✦ МОЛОТ ПУСТОТЫ ✦", Map.of(Enchantments.SHARPNESS, 6, Enchantments.UNBREAKING, 4, Enchantments.MENDING, 1)),
                    new LootEntry("darkrealm:void_greataxe", 1, 9, "§5§l✦ СЕКИРА БЕЗДНЫ ✦", Map.of(Enchantments.SHARPNESS, 6, Enchantments.UNBREAKING, 4)),
                    new LootEntry("darkrealm:void_chestplate", 1, 10, "§5§l✦ Кираса Властелина Пустоты", Map.of(Enchantments.ALL_DAMAGE_PROTECTION, 5, Enchantments.UNBREAKING, 4, Enchantments.MENDING, 1)),
                    new LootEntry("darkrealm:void_helmet", 1, 12, "§5§l✦ Корона Пустоты", Map.of(Enchantments.ALL_DAMAGE_PROTECTION, 5, Enchantments.UNBREAKING, 4)),
                    new LootEntry("darkrealm:void_leggings", 1, 12, "§5§l✦ Поножи Пустоты", Map.of(Enchantments.ALL_DAMAGE_PROTECTION, 5, Enchantments.UNBREAKING, 4)),
                    new LootEntry("darkrealm:void_boots", 1, 12, "§5§l✦ Поступи Пустоты", Map.of(Enchantments.ALL_DAMAGE_PROTECTION, 5, Enchantments.UNBREAKING, 4)),
                    new LootEntry("darkrealm:obsidian_hammer", 1, 18, "§6🔥 Обсидиановый Молот Полководца", Map.of(Enchantments.SHARPNESS, 5, Enchantments.UNBREAKING, 3)),
                    new LootEntry("darkrealm:void_essence", 20, 20, null, null),
                    new LootEntry("darkrealm:realm_key", 1, 15, null, null)
            );
            default -> List.of( // common
                    new LootEntry("darkrealm:ashen_sword", 1, 20, null, Map.of(Enchantments.SHARPNESS, 3)),
                    new LootEntry("darkrealm:ashen_dagger", 1, 20, null, Map.of(Enchantments.SHARPNESS, 3)),
                    new LootEntry("darkrealm:ashen_essence", 10, 30, null, null),
                    new LootEntry("minecraft:diamond", 16, 35, null, null),
                    new LootEntry("minecraft:netherite_ingot", 2, 18, null, null),
                    new LootEntry("minecraft:golden_apple", 8, 30, null, null),
                    new LootEntry("minecraft:totem_of_undying", 1, 15, null, null),
                    new LootEntry("minecraft:experience_bottle", 32, 35, null, null)
            );
        };
    }

    public static ItemStack rollPrize(String caseType) {
        List<LootEntry> pool = getCasePool(caseType);
        int totalWeight = pool.stream().mapToInt(LootEntry::weight).sum();
        int roll = RANDOM.nextInt(totalWeight);
        int current = 0;
        for (LootEntry entry : pool) {
            current += entry.weight();
            if (roll < current) {
                return resolve(entry.itemId(), entry.count(), entry.customName(), entry.enchants());
            }
        }
        LootEntry fallback = pool.get(0);
        return resolve(fallback.itemId(), fallback.count(), fallback.customName(), fallback.enchants());
    }
}