package com.hughbone.eldenhorses.interfaces;

import net.minecraft.entity.passive.HorseEntity;

public interface ServerPlayerExt {
    void storeHorse(HorseEntity horse);
    void summonHorse(boolean mountPlayer);
}
