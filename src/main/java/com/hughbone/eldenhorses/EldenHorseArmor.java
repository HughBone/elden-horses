package com.hughbone.eldenhorses;

import net.minecraft.item.HorseArmorItem;
import net.minecraft.util.Identifier;

public class EldenHorseArmor extends HorseArmorItem {

    public EldenHorseArmor(int bonus, String name, Settings settings) {
        super(bonus, name, settings);
    }

    @Override
    public Identifier getEntityTexture() {
        return new Identifier("elden_horses","textures/entity/horse_armor_netherite.png");
    }
}
