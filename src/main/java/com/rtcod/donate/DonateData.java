package com.rtcod.donate;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DonateData extends SavedData {
    private final Map<UUID, String> ranks = new HashMap<>();
    private final Map<UUID, Map<String, Integer>> caseKeys = new HashMap<>();
    private final Map<UUID, Map<String, Long>> kitCooldowns = new HashMap<>();

    public static DonateData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                DonateData::load, DonateData::new, "rtcod_donate_data"
        );
    }

    public static DonateData load(CompoundTag tag) {
        DonateData data = new DonateData();
        CompoundTag ranksTag = tag.getCompound("ranks");
        for (String key : ranksTag.getAllKeys()) {
            data.ranks.put(UUID.fromString(key), ranksTag.getString(key));
        }

        CompoundTag keysTag = tag.getCompound("caseKeys");
        for (String uuidStr : keysTag.getAllKeys()) {
            CompoundTag pTag = keysTag.getCompound(uuidStr);
            Map<String, Integer> map = new HashMap<>();
            for (String cType : pTag.getAllKeys()) {
                map.put(cType, pTag.getInt(cType));
            }
            data.caseKeys.put(UUID.fromString(uuidStr), map);
        }

        CompoundTag cdTag = tag.getCompound("kitCooldowns");
        for (String uuidStr : cdTag.getAllKeys()) {
            CompoundTag pTag = cdTag.getCompound(uuidStr);
            Map<String, Long> map = new HashMap<>();
            for (String kitId : pTag.getAllKeys()) {
                map.put(kitId, pTag.getLong(kitId));
            }
            data.kitCooldowns.put(UUID.fromString(uuidStr), map);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag ranksTag = new CompoundTag();
        ranks.forEach((k, v) -> ranksTag.putString(k.toString(), v));
        tag.put("ranks", ranksTag);

        CompoundTag keysTag = new CompoundTag();
        caseKeys.forEach((uuid, map) -> {
            CompoundTag pTag = new CompoundTag();
            map.forEach(pTag::putInt);
            keysTag.put(uuid.toString(), pTag);
        });
        tag.put("caseKeys", keysTag);

        CompoundTag cdTag = new CompoundTag();
        kitCooldowns.forEach((uuid, map) -> {
            CompoundTag pTag = new CompoundTag();
            map.forEach(pTag::putLong);
            cdTag.put(uuid.toString(), pTag);
        });
        tag.put("kitCooldowns", cdTag);
        return tag;
    }

    public String getRank(UUID uuid) {
        return ranks.getOrDefault(uuid, "default");
    }

    public void setRank(UUID uuid, String rank) {
        ranks.put(uuid, rank.toLowerCase());
        setDirty();
    }

    public boolean hasRankPermission(UUID uuid, String requiredRank) {
        return rankWeight(getRank(uuid)) >= rankWeight(requiredRank);
    }

    private int rankWeight(String rank) {
        return switch (rank.toLowerCase()) {
            case "vip" -> 1;
            case "premium" -> 2;
            case "lord" -> 3;
            default -> 0;
        };
    }

    public int getKeys(UUID uuid, String caseType) {
        return caseKeys.getOrDefault(uuid, Map.of()).getOrDefault(caseType.toLowerCase(), 0);
    }

    public void addKeys(UUID uuid, String caseType, int amount) {
        Map<String, Integer> map = caseKeys.computeIfAbsent(uuid, k -> new HashMap<>());
        String key = caseType.toLowerCase();
        map.put(key, Math.max(0, map.getOrDefault(key, 0) + amount));
        setDirty();
    }

    public boolean consumeKey(UUID uuid, String caseType) {
        int cur = getKeys(uuid, caseType);
        if (cur <= 0) return false;
        addKeys(uuid, caseType, -1);
        return true;
    }

    public long getRemainingCooldownMs(UUID uuid, String kitId, long cooldownDurationMs) {
        long lastUsed = kitCooldowns.getOrDefault(uuid, Map.of()).getOrDefault(kitId.toLowerCase(), 0L);
        long now = System.currentTimeMillis();
        long diff = (lastUsed + cooldownDurationMs) - now;
        return Math.max(0L, diff);
    }

    public void markKitUsed(UUID uuid, String kitId) {
        kitCooldowns.computeIfAbsent(uuid, k -> new HashMap<>()).put(kitId.toLowerCase(), System.currentTimeMillis());
        setDirty();
    }
}