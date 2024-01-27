package dev.heliosclient.hud.hudelements;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.hud.HudElement;
import dev.heliosclient.hud.HudElementData;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.MathUtils;
import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.util.math.Vec3d;

public class CompactData extends HudElement {
    public static HudElementData DATA = new HudElementData<>("CompactData","Displays data in compact manner", CompactData::new);
    public CompactData() {
        super(DATA);
        this.width = 75;
        this.height = 50;
    }

    @Override
    public void renderElement(DrawContext drawContext, TextRenderer textRenderer) {
        int coordX, coordY, coordZ;
        if (mc.player == null) {
            coordX = 0;
            coordY = 0;
            coordZ = 0;
        } else {
            coordX = (int) MathUtils.round(mc.player.getX(), 0);
            coordY = (int) MathUtils.round(mc.player.getY(), 0);
            coordZ = (int) MathUtils.round(mc.player.getZ(), 0);
        }

        String fps = "FPS: " + ColorUtils.gray + mc.getCurrentFps();
        String speed = "Speed: " + ColorUtils.gray + MathUtils.round(moveSpeed(), 1);
        String ping = "Ping: " + ColorUtils.gray + getPing();
        String biome = "Biome: " + ColorUtils.gray + "None";

        super.renderElement(drawContext, textRenderer);

        this.width = Math.round(
                Math.max(
                        Math.min(82, Math.max(Renderer2D.getStringWidth(fps + speed),Renderer2D.getStringWidth(ping + biome))),
                Math.max(Renderer2D.getStringWidth(fps + speed) + 5, Renderer2D.getStringWidth(ping + biome) + 5)));


        this.height = Math.round(Renderer2D.getStringHeight() * 5 + 11);

        Renderer2D.drawString(drawContext.getMatrices(), "X: " + ColorUtils.gray + coordX, this.x + 1, this.y + 1, HeliosClient.uiColor);
        Renderer2D.drawString(drawContext.getMatrices(), "Y: " + ColorUtils.gray + coordY, this.x + 1, this.y + Renderer2D.getStringHeight() + 3, HeliosClient.uiColor);
        Renderer2D.drawString(drawContext.getMatrices(), "Z: " + ColorUtils.gray + coordZ, this.x + 1, this.y + Renderer2D.getStringHeight() * 2 + 5, HeliosClient.uiColor);

        Renderer2D.drawString(drawContext.getMatrices(), fps, this.x + 1, this.y + Renderer2D.getStringHeight() * 3 + 7, HeliosClient.uiColor);
        Renderer2D.drawString(drawContext.getMatrices(), speed, this.x + 1 + Renderer2D.getStringWidth(fps) + 3, this.y + Renderer2D.getStringHeight() * 3 + 7, HeliosClient.uiColor);
        Renderer2D.drawString(drawContext.getMatrices(), ping, this.x + 1, this.y + Renderer2D.getStringHeight() * 4 + 9, HeliosClient.uiColor);
        Renderer2D.drawString(drawContext.getMatrices(), biome, this.x + 1 + Renderer2D.getStringWidth(ping) + 3, this.y + Renderer2D.getStringHeight() * 4 + 9, HeliosClient.uiColor);
    }

    private double moveSpeed() {
        if (mc.player == null) {
            return 0;
        }
        Vec3d move = new Vec3d(mc.player.getX() - mc.player.prevX, 0, mc.player.getZ() - mc.player.prevZ).multiply(20);

        return Math.abs(MathUtils.length2D(move));
    }
    public static int getPing() {
        if (mc.player == null) {
            return 0;
        }
        PlayerListEntry entry = mc.player.networkHandler.getPlayerListEntry(mc.player.getUuid());
        if (entry != null) {
            return entry.getLatency();
        }
        return 0;
    }
}
