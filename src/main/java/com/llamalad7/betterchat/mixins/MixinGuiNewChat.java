package com.llamalad7.betterchat.mixins;

import com.llamalad7.betterchat.BetterChat;
import com.llamalad7.betterchat.ducks.Configurable;
import com.llamalad7.betterchat.utils.AnimationTools;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ChatHud.class)
public abstract class MixinGuiNewChat extends DrawableHelper implements Configurable {
    @Shadow
    private boolean hasUnreadNewMessages;

    @Shadow
    public abstract float getChatScale();

    private float percentComplete;
    private int newLines;
    private long prevMillis = System.currentTimeMillis();
    private boolean configuring;
    private float animationPercent;
    private int lineBeingDrawn;

    @Override
    public void setConfiguring(boolean configuring) {
        this.configuring = configuring;
    }

    private void updatePercentage(long diff) {
        if (percentComplete < 1) percentComplete += 0.004f * diff;
        percentComplete = AnimationTools.clamp(percentComplete, 0, 1);
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void modifyChatRendering(CallbackInfo ci) {
        if (configuring) {
            ci.cancel();
            return;
        }
        long current = System.currentTimeMillis();
        long diff = current - prevMillis;
        prevMillis = current;
        updatePercentage(diff);
        float t = percentComplete;
        animationPercent = AnimationTools.clamp(1 - (--t) * t * t * t, 0, 1);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;pushMatrix()V", ordinal = 0, shift = At.Shift.AFTER))
    private void translate(CallbackInfo ci) {
        float y = /*BetterChat.getSettings().yOffset*/0;
        if (/*BetterChat.getSettings().smooth*/true && !this.hasUnreadNewMessages) {
            y += (9 - 9 * animationPercent) * this.getChatScale();
        }
        GlStateManager.translate(/*BetterChat.getSettings().xOffset*/0, y, 0);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;fill(IIIII)V", ordinal = 0))
    private void transparentBackground(int left, int top, int right, int bottom, int color) {
//        if (!BetterChat.getSettings().clear) fill(left, top, right, bottom, color);
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;", ordinal = 0, remap = false), index = 0)
    private int getLineBeingDrawn(int line) {
        lineBeingDrawn = line;
        return line;
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;drawWithShadow(Ljava/lang/String;FFI)I"), index = 3)
    private int modifyTextOpacity(int original) {
        if (/*BetterChat.getSettings().smooth*/true && lineBeingDrawn <= newLines) {
            int opacity = (original >> 24) & 0xFF;
            opacity *= animationPercent;
            return (original & ~(0xFF << 24)) | (opacity << 24);
        } else {
            return original;
        }
    }

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;I)V", at = @At("HEAD"))
    private void resetPercentage(CallbackInfo ci) {
        percentComplete = 0;
    }

    @ModifyVariable(method = "addMessage(Lnet/minecraft/text/Text;IIZ)V", at = @At("STORE"), ordinal = 0)
    private List<ChatHudLine> setNewLines(List<ChatHudLine> original) {
        newLines = original.size() - 1;
        return original;
    }

//    @ModifyVariable(method = "getChatComponent", at = @At(value = "STORE", ordinal = 0), ordinal = 3)
//    private int modifyX(int original) {
//        return original - BetterChat.getSettings().xOffset;
//    }
//
//    @ModifyVariable(method = "getChatComponent", at = @At(value = "STORE", ordinal = 0), ordinal = 4)
//    private int modifyY(int original) {
//        return original + BetterChat.getSettings().yOffset;
//    }
}
