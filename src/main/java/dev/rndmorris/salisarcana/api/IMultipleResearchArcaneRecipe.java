package dev.rndmorris.salisarcana.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.world.World;
import thaumcraft.api.crafting.IArcaneRecipe;

import java.util.List;

public interface IMultipleResearchArcaneRecipe extends IArcaneRecipe {
    List<String> salisArcana$getResearches(IInventory inv, World world, EntityPlayer player);
}
