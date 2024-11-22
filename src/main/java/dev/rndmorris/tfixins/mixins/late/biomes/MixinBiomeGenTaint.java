package dev.rndmorris.tfixins.mixins.late.biomes;

import static dev.rndmorris.tfixins.Config.taintBiomeColors;
import static dev.rndmorris.tfixins.ThaumicFixins.LOG;

import net.minecraft.world.biome.BiomeGenBase;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import thaumcraft.common.lib.world.biomes.BiomeGenTaint;

@Mixin(BiomeGenTaint.class)
public abstract class MixinBiomeGenTaint extends BiomeGenBase {

    public MixinBiomeGenTaint(int p_i1971_1_) {
        super(p_i1971_1_);
    }

    @Inject(method = "getBiomeGrassColor", at = @At("HEAD"), cancellable = true, remap = false)
    public void onGetBiomeGrassColor(int x, int y, int z, CallbackInfoReturnable<Integer> cir) {
        LOG.info("Getting taint grass color");
        if (taintBiomeColors != null && taintBiomeColors.grassSet) {
            cir.setReturnValue(taintBiomeColors.grass);
            cir.cancel();
        }
    }

    @Inject(method = "getBiomeFoliageColor", at = @At("HEAD"), cancellable = true, remap = false)
    public void onGetBiomeFoliageColor(int x, int y, int z, CallbackInfoReturnable<Integer> cir) {
        if (taintBiomeColors != null && taintBiomeColors.foliageSet) {
            cir.setReturnValue(taintBiomeColors.foliage);
            cir.cancel();
        }
    }

    @Inject(method = "getSkyColorByTemp", at = @At("HEAD"), cancellable = true, remap = false)
    public void onGetSkyColorByTemp(float temp, CallbackInfoReturnable<Integer> cir) {
        if (taintBiomeColors != null && taintBiomeColors.skySet) {
            cir.setReturnValue(taintBiomeColors.sky);
            cir.cancel();
        }
    }

    @Inject(method = "getWaterColorMultiplier", at = @At("HEAD"), cancellable = true, remap = false)
    public void onGetWaterColorMultiplier(CallbackInfoReturnable<Integer> cir) {
        if (taintBiomeColors != null && taintBiomeColors.waterSet) {
            cir.setReturnValue(taintBiomeColors.water);
            cir.cancel();
        }
    }
}
