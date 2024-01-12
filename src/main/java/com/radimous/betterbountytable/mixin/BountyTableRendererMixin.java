package com.radimous.betterbountytable.mixin;

import iskallia.vault.block.render.BountyTableRenderer;
import iskallia.vault.init.ModItems;
import iskallia.vault.util.InventoryUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collections;
import java.util.List;


@Mixin(value = BountyTableRenderer.class, remap = false)
public class BountyTableRendererMixin {
    @Unique
    private static long betterBountyTable$lastTick = 0;
    @Unique
    private static boolean betterBountyTable$hasLostBounty = false;

    // I need to supress the original search
    // because ModifyVariable modifies var after calling the original expensive method
    @Redirect(method = "render(Liskallia/vault/block/entity/BountyTableTileEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V", at = @At(value = "INVOKE", target = "Liskallia/vault/util/InventoryUtil;findAllItems(Lnet/minecraft/world/entity/player/Player;)Ljava/util/List;"))
    private List<InventoryUtil.ItemAccess> removeOriginal(Player inventoryFn) {
        return Collections.emptyList();
    }

    @ModifyVariable(method = "render(Liskallia/vault/block/entity/BountyTableTileEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V", at = @At("STORE"), ordinal = 2)
    private boolean hasLostBounty(boolean value) {
        Player player = Minecraft.getInstance().player;
        if (player == null)
            return false;
        int currentTick = player.tickCount;
        // search every tick, not every frame
        if (betterBountyTable$lastTick != currentTick) {
            betterBountyTable$hasLostBounty = InventoryUtil.findAllItems(player).stream().anyMatch(itemAccess -> itemAccess.getStack().is(ModItems.LOST_BOUNTY));
            betterBountyTable$lastTick = currentTick;
        }
        return betterBountyTable$hasLostBounty;
    }
}