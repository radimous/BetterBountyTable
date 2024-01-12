package com.radimous.betterbountytable.mixin;

import iskallia.vault.bounty.Bounty;
import iskallia.vault.client.gui.screen.bounty.element.BountyElement;
import iskallia.vault.client.gui.screen.bounty.element.BountyTableContainerElement;
import iskallia.vault.container.BountyContainer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Mixin(value = BountyTableContainerElement.class, remap = false)
public abstract class BountyTableContainerElementMixin {
    @Shadow
    @Final
    private BountyContainer container;
    @Shadow
    BountyElement bountyElement;
    @Unique
    private final List<UUID> betterBountyTable$bountyListBeforeReroll = new ArrayList<>();

    @Inject(method = "refreshBountySelection", at = @At("TAIL"))
    private void selectNewBounty(CallbackInfo ci) {
        for (var bounty : this.container.getAvailable()) {
            if (!betterBountyTable$bountyListBeforeReroll.contains(bounty.getId())) {
                this.bountyElement.setBounty(bounty.getId(), BountyElement.Status.AVAILABLE);
                break;
            }
        }
    }

    @Inject(method = "handleReroll", at = @At("HEAD"))
    private void getOldBountyIds(CallbackInfo ci) {
        betterBountyTable$bountyListBeforeReroll.clear();
        this.container.getAvailable().forEach(bounty -> betterBountyTable$bountyListBeforeReroll.add(bounty.getId()));
    }

    @Inject(method = "lambda$createRerollButtonTooltip$1", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0), cancellable = true)
    private void hideTooltipWhenShift(CallbackInfoReturnable<List<Component>> cir) {
        if (Screen.hasShiftDown()) {
            cir.setReturnValue(Collections.emptyList());
        }
    }

    @Inject(method = "lambda$createRerollButtonTooltip$1", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 1), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void shorterTooltip(CallbackInfoReturnable<List<Component>> cir, Bounty bounty, List<Component> tooltips, ItemStack pearl, int amount, int cost) {
        if (amount >= cost) {
            cir.setReturnValue(tooltips);
        }
    }
}