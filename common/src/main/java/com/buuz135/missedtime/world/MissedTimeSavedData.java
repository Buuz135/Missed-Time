package com.buuz135.missedtime.world;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

public final class MissedTimeSavedData extends SavedData {
    public static final String NAME = "missedtime";
    public static final Factory<MissedTimeSavedData> FACTORY = new Factory<>(MissedTimeSavedData::new, MissedTimeSavedData::load, DataFixTypes.LEVEL);
    private long lastPlayedEpochMillis;

    public MissedTimeSavedData() {
        lastPlayedEpochMillis = System.currentTimeMillis();
    }

    private static MissedTimeSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        MissedTimeSavedData data = new MissedTimeSavedData();
        data.lastPlayedEpochMillis = tag.getLong("LastPlayedEpochMillis");
        if (data.lastPlayedEpochMillis <= 0) {
            data.lastPlayedEpochMillis = System.currentTimeMillis();
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putLong("LastPlayedEpochMillis", lastPlayedEpochMillis);
        return tag;
    }

    public long lastPlayedEpochMillis() {
        return lastPlayedEpochMillis;
    }

    public void setLastPlayedEpochMillis(long value) {
        lastPlayedEpochMillis = value;
        setDirty();
    }
}
