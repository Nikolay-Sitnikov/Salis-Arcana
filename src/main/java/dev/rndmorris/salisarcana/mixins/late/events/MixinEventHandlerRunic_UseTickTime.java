package dev.rndmorris.salisarcana.mixins.late.events;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraftforge.event.entity.living.LivingEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import thaumcraft.common.lib.events.EventHandlerRunic;

@Mixin(value = EventHandlerRunic.class)
public class MixinEventHandlerRunic_UseTickTime {
    @Redirect(method = "livingTick", at = @At(value = "INVOKE", target = "Ljava/lang/System;currentTimeMillis()J"))
    public long overrideSystemTime(@Local(argsOnly = true) LivingEvent.LivingUpdateEvent event) {
        return event.entity.worldObj.getTotalWorldTime();
    }
}
