package dev.heliosclient.ui.clickgui.script;

import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.module.modules.render.GUI;
import dev.heliosclient.scripting.LuaFile;
import dev.heliosclient.scripting.LuaScriptManager;
import dev.heliosclient.ui.clickgui.Tooltip;
import dev.heliosclient.ui.clickgui.gui.PolygonMeshPatternRenderer;
import dev.heliosclient.ui.clickgui.gui.tables.Table;
import dev.heliosclient.ui.clickgui.gui.tables.TableEntry;
import dev.heliosclient.ui.clickgui.navbar.NavBar;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.KeyboardUtils;
import dev.heliosclient.util.SoundUtils;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.List;

public class ScriptManagerScreen extends Screen {
    public static ScriptManagerScreen INSTANCE = new ScriptManagerScreen();
    public static int managerWidth;
    public static int managerHeight;
    public static int startX;
    protected static MinecraftClient mc = MinecraftClient.getInstance();
    static boolean isListening = false, showLocalScripts = true;
    static int scaledWidth;
    static int scaledHeight;
    static float hue = 1f;
    public NavBar navBar = new NavBar();
    String heading = "Script Manager";
    Color darkerGray = Color.DARK_GRAY.darker().darker();
    //private int numRows = 4; // Number of rows (adjust as needed)
  //  private int numColumns = 4; // Number of columns (adjust as needed)
    private int scrollOffset = 0, maxScroll;
    static int lightBlack = ColorUtils.changeAlphaGetInt(0xFF000000, 165);
    //private int entryWidth, entryHeight;

    private Table scriptTable = new Table();


    protected ScriptManagerScreen() {
        super(Text.of("ScriptSelector"));
    }

    public static void displayCloudEntries(DrawContext context) {
        FontRenderers.Large_fxfontRenderer.drawString(context.getMatrices(), "Work in progress", 175 + (scaledWidth - 150 - 175) / 2.0f - FontRenderers.Large_fxfontRenderer.getStringWidth("Work in progress") / 2.0f, 55, Color.YELLOW.getRGB());
    }

    public static void drawButton(DrawContext drawContext, int x, int y, String text, String icon, int mouseX, int mouseY) {
        if (isMouseOver(mouseX, mouseY, x, y, 70, 13)) {
            y = y - 1;
        }
        x = x + 1;

        hue = (System.currentTimeMillis() % 10000) / 10000f;

        float width = managerWidth * 0.18f - 15;
        float height = 20;
        String trimmedText = FontRenderers.Mid_fxfontRenderer.trimToWidth(text, width);
        float iconWidth = FontRenderers.Large_iconRenderer.getStringWidth(icon);
        float iconHeight = FontRenderers.Large_iconRenderer.getStringHeight(icon);

        float halfTextHeight = height / 2.0f - FontRenderers.Mid_fxfontRenderer.getStringHeight(trimmedText) / 2.0f;
        float middleX = x + (width / 2.0f) - FontRenderers.Mid_fxfontRenderer.getStringWidth(trimmedText) / 2.0f;
        Color[] colors = ColorUtils.getNightSkyColors(hue);

        Renderer2D.drawRoundedGradientRectangleWithShadow(drawContext.getMatrices(), x, y, width, height, colors[0], colors[1], colors[1], colors[0], 4, 4, colors[0]);

        //Renderer2D.scaleAndPosition(drawContext.getMatrices(),x,y,width,20, Math.abs(1/ (float) mc.getWindow().getScaleFactor()));

        FontRenderers.Mid_fxfontRenderer.drawString(drawContext.getMatrices(), trimmedText, middleX, y + halfTextHeight, Color.WHITE.getRGB());

        //Only render the icon if there is space for it
        if (width > FontRenderers.Mid_fxfontRenderer.getStringWidth(trimmedText) + iconWidth) {
            FontRenderers.Large_iconRenderer.drawString(drawContext.getMatrices(), icon, x + iconWidth, y + height / 2.0f - iconHeight / 2.0f, Color.WHITE.getRGB());
        }
        //Renderer2D.stopScaling(drawContext.getMatrices());
    }

    public static boolean isMouseOver(double mouseX, double mouseY, double x, double y, double width, double height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public static void drawOnOffButton(DrawContext context, float x, float y, boolean state) {
        Renderer2D.drawRoundedRectangleWithShadowBadWay(context.getMatrices().peek().getPositionMatrix(), x, y, 15, 8, 4, state ? Color.GREEN.getRGB() : Color.RED.getRGB(), 100, 1, 1);

        float filledX = state ? x + 12 : x + 3;
        Renderer2D.drawFilledCircle(context.getMatrices().peek().getPositionMatrix(), filledX, y + 4, 4, Color.WHITE.getRGB());
    }

    @Override
    public void onDisplayed() {
        super.onDisplayed();
        addTableEntries();
    }

    @Override
    protected void init() {
        super.init();
        addTableEntries();
        calculateTable();
        scrollOffset = Math.min(scrollOffset, maxScroll);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (mc.world == null) {
            super.renderBackgroundTexture(context);
        }

        if(GUI.coolVisuals()) {
            PolygonMeshPatternRenderer.INSTANCE.render(context.getMatrices(), mouseX, mouseY);
        }

        scaledWidth = mc.getWindow().getScaledWidth();
        scaledHeight = mc.getWindow().getScaledHeight();
        managerWidth = scaledWidth - 200;
        managerHeight = scaledHeight - 80;
        startX = 100;

        calculateTable();

        //Draw background and heading bg
        Renderer2D.drawRoundedRectangle(context.getMatrices().peek().getPositionMatrix(), startX, 32,true,true,true,true, scaledWidth - 200, scaledHeight - 80, 3, ColorUtils.changeAlpha(Color.BLACK, 125).getRGB());
        Renderer2D.drawRoundedRectangleWithShadow(context.getMatrices(), startX, 32, scaledWidth - 200, 18, 3, 4, Color.BLACK.getRGB(), true, true, false, false);

        //Reload all scripts button
        Renderer2D.drawRoundedRectangleWithShadow(context.getMatrices(), scaledWidth - 125, 33, 15, 15, 5, 3, new Color(0xE2080009, true).brighter().getRGB());
        Renderer2D.drawOutlineRoundedBox(context.getMatrices().peek().getPositionMatrix(), scaledWidth - 125, 33, 15, 15, 5, 0.7f, Color.WHITE.getRGB());
        FontRenderers.Mid_iconRenderer.drawString(context.getMatrices(), "\uEA1D", scaledWidth - 121.7f, 36.5f, Color.WHITE.getRGB());


        //Draw heading
        FontRenderers.Mid_fxfontRenderer.drawString(context.getMatrices(), heading, startX + (scaledWidth - 200) / 2.0f - FontRenderers.Mid_fxfontRenderer.getStringWidth(heading) / 2.0f, 32 + FontRenderers.Mid_fxfontRenderer.getStringHeight(heading) / 2.0f, Color.WHITE.getRGB());

        //Draw rounded background behind for the buttons.
        float buttonSectionWidth = managerWidth * 0.18f;
        int lastButtonY = 134;
        Renderer2D.drawRoundedRectangle(context.getMatrices().peek().getPositionMatrix(), startX + 5, 60, buttonSectionWidth, lastButtonY - 30, 3, lightBlack);
        // Renderer2D.drawRectangleWithShadow(context.getMatrices(), 175, 50, 1f, scaledHeight - 98, Color.BLACK.brighter().brighter().getRGB(), 4);

        //Draw Side buttons
        drawButton(context, startX + 12, 66, "Local Scripts", "\uF15D", mouseX, mouseY);
        drawButton(context, startX + 12, 100, "Cloud Scripts", "\uEA37", mouseX, mouseY);
        drawButton(context, startX + 12, 134, "Force close all",  ColorUtils.gold + "\uF1AF", mouseX, mouseY);


        Renderer2D.drawOutlineRoundedBox(context.getMatrices().peek().getPositionMatrix(), startX - 1, 31, scaledWidth - 200 + 2, scaledHeight - 78, 3, 0.8f, darkerGray.getRGB());


        if (showLocalScripts) {
            drawLocalScriptEntries(context, mouseX, mouseY);
        } else {
            displayCloudEntries(context);
        }

        if (hoveredOverRefreshAll(mouseX, mouseY)) {
            Tooltip.tooltip.changeText("Refreshes/Reloads all scripts");
        }

        navBar.render(context, textRenderer, mouseX, mouseY);
        Tooltip.tooltip.render(context, mc.textRenderer, mouseX, mouseY);
    }

    public void drawLocalScriptEntries(DrawContext context, int mouseX, int mouseY) {
        // Enable scissor for scrolling
        float startEntryX = startX + (managerWidth * 0.20f) - 2;

        Renderer2D.enableScissor((int) (startEntryX), 50, managerWidth + 50, mc.getWindow().getScaledHeight() - 98);

        // Render script entries
        for(List<TableEntry> row: scriptTable.table){
            for (TableEntry entry : row) {
                if (entry instanceof ScriptEntry scriptEntry) {
                    drawScript(context, (float) scriptEntry.getX(),  (float) scriptEntry.getY(), mouseX, mouseY, scriptEntry.getLuaFile());
                }
            }
        }

        Renderer2D.disableScissor();

        // Draw scrollbar
        int scrollbarX = startX + managerWidth + 5;
        int scrollbarY = 50;
        float scrollbarHeight = maxScroll > 0 ? (int) (Math.pow(70 + 10, 2) * 2 / maxScroll) : 0;
        float scrollbarPosition = maxScroll > 0 ? scrollbarY + (scaledHeight - 100 - scrollbarHeight) * scrollOffset / maxScroll : scrollbarY; // Position of the scrollbar depends on the current scroll offset
        Renderer2D.drawRectangleWithShadow(context.getMatrices(), scrollbarX, scrollbarPosition, 1.5f, scrollbarHeight, Color.BLACK.brighter().brighter().getRGB(), 2);
    }

    public void drawScript(DrawContext drawContext, float x, float y, int mouseX, int mouseY, LuaFile file) {
        //Icon
        Renderer2D.drawRoundedRectangleWithShadow(drawContext.getMatrices(), x, y, 60, 70, 4, 4, ColorUtils.changeAlpha(ColorManager.INSTANCE.ClickGuiPrimary().darker().darker().darker(), 169).getRGB());
        Renderer2D.drawOutlineGradientRoundedBox(drawContext.getMatrices().peek().getPositionMatrix(), x, y, 60, 70, 4, 0.7f, ColorManager.INSTANCE.getPrimaryGradientStart(), ColorManager.INSTANCE.getPrimaryGradientEnd(), ColorManager.INSTANCE.getPrimaryGradientEnd(), ColorManager.INSTANCE.getPrimaryGradientStart());
        FontRenderers.Ultra_Large_iconRenderer.drawString(drawContext.getMatrices(), "\uF0F6", x + 19, y + 12, Color.WHITE.getRGB());

        Renderer2D.drawRoundedGradientRectangle(drawContext.getMatrices().peek().getPositionMatrix(), ColorManager.INSTANCE.getPrimaryGradientStart(), ColorManager.INSTANCE.getPrimaryGradientEnd(), ColorManager.INSTANCE.getPrimaryGradientEnd(), ColorManager.INSTANCE.getPrimaryGradientStart(), x, y + 58, 60, 12, 3, false, false, true, true);

        //Name
        FontRenderers.Mid_fxfontRenderer.drawString(drawContext.getMatrices(), file.getScriptName(), x + 30 - FontRenderers.Mid_fxfontRenderer.getStringWidth(file.getScriptName()) / 2.0f, y + 43f, Color.WHITE.getRGB());

        //Bind
        String bindKey = KeyboardUtils.translateShort(file.bindKey).toUpperCase();
        if (file.isListeningForBind) {
            bindKey = "Set";
        }
        Renderer2D.drawRoundedRectangleWithShadowBadWay(drawContext.getMatrices().peek().getPositionMatrix(), x + 4, y + 60, FontRenderers.Small_fxfontRenderer.getStringWidth(bindKey) + 3, 8, 2, Color.BLACK.getRGB(), 100, 1, 1);
        FontRenderers.Small_fxfontRenderer.drawString(drawContext.getMatrices(), bindKey, x + 4.9f, y + 60.5f, Color.WHITE.getRGB());

        //File state (loaded/unloaded)
        drawOnOffButton(drawContext, x + 26, y + 60, file.isLoaded());
        //Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(),xModified + 10, y + 60,FontRenderers.Small_fxfontRenderer.getStringWidth(file.isLoaded? "DISABLE":"ENABLE") + 3,7,2,Color.BLACK.getRGB());
        //FontRenderers.Small_fxfontRenderer.drawString(drawContext.getMatrices(),file.isLoaded? "DISABLE":"ENABLE",xModified + 10,y + 60.5f, file.isLoaded? Color.RED.getRGB() : Color.GREEN.getRGB());

        //Refresh / Reload
        Renderer2D.drawRoundedRectangleWithShadowBadWay(drawContext.getMatrices().peek().getPositionMatrix(), x + 45f, y + 59.5f, FontRenderers.Small_iconRenderer.getStringWidth("\uEA75") + 2, 8, 2, Color.BLACK.getRGB(), 100, 1, 1);
        FontRenderers.Small_iconRenderer.drawString(drawContext.getMatrices(), "\uEA1D", x + 46f, y + 60.5f, Color.WHITE.getRGB());

        if (isMouseOver(mouseX, mouseY, x, y, 60, 70)) {
            //Edit
            Renderer2D.drawRoundedRectangleWithShadowBadWay(drawContext.getMatrices().peek().getPositionMatrix(), x + 46f, y + 3f, FontRenderers.Small_iconRenderer.getStringWidth("\uEAF3") + 2, 8, 2, Color.WHITE.getRGB(), 100, 1, 1);
            FontRenderers.Small_iconRenderer.drawString(drawContext.getMatrices(), "\uEAF3", x + 47f, y + 3.5f, Color.BLACK.getRGB());
        }
    }

    private void addTableEntries(){
        scaledWidth = mc.getWindow().getScaledWidth();
        scaledHeight = mc.getWindow().getScaledHeight();
        managerWidth = scaledWidth - 200;
        managerHeight = scaledHeight - 80;

        scriptTable = new Table();
        for (LuaFile luaFile: LuaScriptManager.luaFiles) {
            scriptTable.addEntry(new ScriptEntry(luaFile),managerWidth * 0.88 - 15);
        }
    }

    public void calculateTable() {
        float startEntryX = startX + (managerWidth * 0.20f) + 10;
        maxScroll = (int) Math.ceil(scriptTable.adjustTableLayout(startEntryX, 65,managerWidth * 0.8 - 15,false));
    }

    public boolean hoveredOverRefreshAll(double mouseX, double mouseY) {
        return isMouseOver(mouseX, mouseY, scaledWidth - 125, 33, 15, 15);
    }


    public boolean hoveredOverLocalScripts(double mouseX, double mouseY) {
        return isMouseOver(mouseX, mouseY, startX + 12, 66, managerWidth * 0.18f - 15, 20);
    }

    public boolean hoveredOverCloudScripts(double mouseX, double mouseY) {
        return isMouseOver(mouseX, mouseY, startX + 12, 100, managerWidth * 0.18f - 15, 20);
    }
    public boolean hoveredOverForceCloseAll(double mouseX, double mouseY) {
        return isMouseOver(mouseX, mouseY, startX + 12, 134, managerWidth * 0.18f - 15, 20);
    }

    public boolean hoveredOverFileState(double mouseX, double mouseY, double entryX, double entryY) {
        return isMouseOver(mouseX, mouseY, entryX + 26, entryY + 60, 16, 8);
    }

    public boolean hoveredOverRefreshFile(double mouseX, double mouseY, double entryX, double entryY) {
        return isMouseOver(mouseX, mouseY, entryX + 45f, entryY + 59.5f, 4 + FontRenderers.Small_iconRenderer.getStringWidth("\uEA75"), 8);
    }

    public boolean hoveredOverEditFile(double mouseX, double mouseY, double entryX, double entryY) {
        return isMouseOver(mouseX, mouseY, entryX + 46f, entryY + 3f, 4 + FontRenderers.Small_iconRenderer.getStringWidth("\uEAF3"), 8);
    }

    public boolean hoveredOverBind(double mouseX, double mouseY, LuaFile file, double entryX, double entryY) {
        String bindKeyName = KeyboardUtils.translateShort(file.bindKey).toUpperCase();
        return isMouseOver(mouseX, mouseY, entryX + 5f, entryY + 60f, FontRenderers.Small_fxfontRenderer.getStringWidth(bindKeyName) + 3, 7.4f);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollOffset = MathHelper.clamp(scrollOffset - (int) verticalAmount * 10, 0, maxScroll); // Clamp the scroll offset between 0 and maxScroll
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        navBar.mouseClicked((int) mouseX, (int) mouseY, button);
        // Check mouse clicked on any of the entries
        for(List<TableEntry> row: scriptTable.table){
            for (TableEntry entry : row) {
                if (entry instanceof ScriptEntry scriptEntry) {
                    LuaFile file = scriptEntry.getLuaFile();
                    if (hoveredOverFileState(mouseX, mouseY, scriptEntry.getX(), scriptEntry.getY())) {
                        SoundUtils.playInstanceSound(SoundUtils.CLICK_SOUNDEVENT);
                        if (file.isLoaded()) {
                            LuaScriptManager.INSTANCE.closeScript(file);
                        } else {
                            LuaScriptManager.INSTANCE.loadScript(file);
                        }
                    }
                    if (hoveredOverRefreshFile(mouseX, mouseY, scriptEntry.getX(), scriptEntry.getY())) {
                        LuaScriptManager.reloadScript(file);
                    }

                    if (hoveredOverBind(mouseX, mouseY, file, scriptEntry.getX(), scriptEntry.getY())) {
                        file.setListening(true);
                        isListening = true;
                    }
                    if (hoveredOverEditFile(mouseX, mouseY, scriptEntry.getX(), scriptEntry.getY())) {
                        //Todo
                    }
                }
            }
        }
        if (hoveredOverLocalScripts(mouseX, mouseY)) {
            showLocalScripts = true;
            SoundUtils.playInstanceSound(SoundUtils.CLICK_SOUNDEVENT);
        }
        if (hoveredOverCloudScripts(mouseX, mouseY)) {
            showLocalScripts = false;
            SoundUtils.playInstanceSound(SoundUtils.CLICK_SOUNDEVENT);
        }
        if (hoveredOverForceCloseAll(mouseX, mouseY)) {
            SoundUtils.playInstanceSound(SoundUtils.TING_SOUNDEVENT,100,2f);
            for(LuaFile luaFile: LuaScriptManager.luaFiles){
                LuaScriptManager.INSTANCE.closeScript(luaFile);
            }
        }
        if (hoveredOverRefreshAll(mouseX, mouseY)) {
            LuaScriptManager.getScripts();
            addTableEntries();
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!isListening && keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.close();
        }

        // I don't want to add check for luaFiles is null or empty so using old loop and not enhanced.
        for (int i = 0; i < LuaScriptManager.luaFiles.size(); i++) {
            LuaFile file = LuaScriptManager.luaFiles.get(i);
            if (file.isListeningForBind && keyCode != GLFW.GLFW_KEY_ESCAPE) {
                file.setBindKey(keyCode);
                file.setListening(false);
                isListening = false;
            }
            if (file.isListeningForBind && keyCode == GLFW.GLFW_KEY_ESCAPE) {
                file.setBindKey(-1);
                file.setListening(false);
                isListening = false;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public class ScriptEntry implements TableEntry {
        private final LuaFile luaFile;
        private double x;
        private double y;

        public ScriptEntry(LuaFile luaFile) {
            this.luaFile = luaFile;
        }

        public LuaFile getLuaFile() {
            return luaFile;
        }

        @Override
        public double getWidth() {
            return 60 + 10;
        }

        @Override
        public double getHeight() {
            return 70 + 10;
        }

        @Override
        public void setPosition(double x, double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void setWidth(double width) {}

        public double getX() {
            return x;
        }

        public double getY() {
            return y - ScriptManagerScreen.this.scrollOffset;
        }
    }
}