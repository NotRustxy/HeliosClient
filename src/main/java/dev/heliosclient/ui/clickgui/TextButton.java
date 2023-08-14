package dev.heliosclient.ui.clickgui;

import dev.heliosclient.ui.clickgui.ClickGUIScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.awt.*;

public class TextButton 
{
    String text;
    int x, y, color, width;
    private int hoverAnimationTimer = 0;

    public TextButton(String text, int color)
    {
        this.text = text;
        this.color = color;
    }

    public void render(DrawContext drawContext, TextRenderer textRenderer, int x, int y, int mouseX, int mouseY)
    {
        this.x = x;
        this.y = y;
        width = textRenderer.getWidth(text);
        if(hovered(mouseX, mouseY)) {
            hoverAnimationTimer = Math.min(hoverAnimationTimer+2, 40);
        } else {
            hoverAnimationTimer = Math.max(hoverAnimationTimer-2, 0);
        }
        drawContext.fill(x-1, y-2, x + width + 1, y + 10, new Color(255, 255, 255, hoverAnimationTimer).getRGB());
        drawContext.drawText(textRenderer, Text.literal(text), x, y, color, true);
    }

    public boolean hovered(int mouseX, int mouseY) 
    {
		return mouseX >= x - 1 && mouseX <= x + width + 1 && mouseY >= y - 2 && mouseY <= y + 10;
	}

    public void mouseClicked(int mouseX, int mouseY) 
    {
		if (hovered(mouseX, mouseY)) 
        {
            MinecraftClient.getInstance().setScreen(ClickGUIScreen.INSTANCE);
		}
	}
}
