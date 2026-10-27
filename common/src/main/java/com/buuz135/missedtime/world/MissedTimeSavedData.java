package com.buuz135.missedtime.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class MissedTimeSavedData extends SavedData {
    public static final String NAME = "missedtime";
    public static final Codec<MissedTimeSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.optionalFieldOf("LastPlayedEpochMillis", 0L).forGetter(MissedTimeSavedData::lastPlayedEpochMillis)
    ).apply(instance, MissedTimeSavedData::new));
    public static final SavedDataType<MissedTimeSavedData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("missedtime", NAME), MissedTimeSavedData::new, CODEC, DataFixTypes.LEVEL);
    private long lastPlayedEpochMillis;

    public MissedTimeSavedData() {
        lastPlayedEpochMillis = System.currentTimeMillis();
    }

    private MissedTimeSavedData(long lastPlayedEpochMillis) {
        this.lastPlayedEpochMillis = lastPlayedEpochMillis > 0 ? lastPlayedEpochMillis : System.currentTimeMillis();
    }

    public long lastPlayedEpochMillis() {
        return lastPlayedEpochMillis;
    }

    public void setLastPlayedEpochMillis(long value) {
        lastPlayedEpochMillis = value;
        setDirty();
    }
}
