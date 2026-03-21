package com.starclient.screen;

import com.starclient.StarClientOptions;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class DynamicOptionPanelScreen extends Screen {
    private static final int BACKGROUND_COLOR = new Color(25, 25, 25, 185).getRGB();
    private static final int PANEL_COLOR = new Color(10, 10, 12, 232).getRGB();
    private static final int HEADER_COLOR = new Color(16, 16, 22, 245).getRGB();
    private static final int GROUP_COLOR = new Color(14, 14, 20, 228).getRGB();
    private static final int TITLE_COLOR = new Color(240, 238, 255, 255).getRGB();
    private static final int CONTROL_BG_COLOR = new Color(20, 20, 28, 230).getRGB();
    private static final int CONTROL_BG_HOVER_COLOR = new Color(28, 28, 38, 238).getRGB();
    private static final int CONTROL_TEXT_COLOR = new Color(232, 228, 246, 255).getRGB();
    private static final int TAB_TEXT_COLOR = new Color(154, 148, 172, 255).getRGB();
    private static final int SEARCH_BG_COLOR = new Color(10, 10, 14, 230).getRGB();
    private static final int SEARCH_BG_FOCUS_COLOR = new Color(14, 14, 20, 238).getRGB();
    private static final int SEARCH_PLACEHOLDER_COLOR = new Color(112, 110, 126, 255).getRGB();

    private static final int PANEL_WIDTH = 560;
    private static final int PANEL_HEIGHT = 360;
    private static final int HEADER_HEIGHT = 30;

    private static final int CONTROL_HEIGHT = 20;
    private static final int CONTROL_SPACING = 4;

    @Nullable
    private final Screen previousScreen;
    protected final ShootingStarsRenderer shootingStarsRenderer = new ShootingStarsRenderer();
    private final List<@NonNull MenuTab> tabs;

    private int selectedTabIndex = 0;
    private String searchText = "";
    private int panelX = Integer.MIN_VALUE;
    private int panelY = Integer.MIN_VALUE;
    private boolean draggingPanel = false;
    private double dragOffsetX = 0.0;
    private double dragOffsetY = 0.0;

    private final List<SectionRenderBox> sectionRenderBoxes = new ArrayList<>();
    private final List<ControlRenderBox> controlRenderBoxes = new ArrayList<>();
    private final List<TabRenderBox> tabRenderBoxes = new ArrayList<>();

    @Nullable
    private EditBox searchWidget;
    @Nullable
    private SearchRenderBox searchRenderBox;
    @Nullable
    private Button closeButtonWidget;

    protected DynamicOptionPanelScreen(@Nullable Screen previousScreen, Component title,
            List<@NonNull MenuTab> tabs) {
        super(title);
        this.previousScreen = previousScreen;
        this.tabs = tabs;
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.previousScreen);
        }
    }

    @Override
    protected void init() {
        if (panelX == Integer.MIN_VALUE || panelY == Integer.MIN_VALUE) {
            panelX = (this.width - PANEL_WIDTH) / 2;
            panelY = (this.height - PANEL_HEIGHT) / 2;
        }
        clampPanelPosition();
        rebuildMenuWidgets();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        clampPanelPosition();
        rebuildMenuWidgets();
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
        boolean handled = super.mouseClicked(event, doubleClick);
        if (handled) {
            return true;
        }

        if (event.button() == 0 && isInDragHandle(event.x(), event.y())) {
            draggingPanel = true;
            dragOffsetX = event.x() - panelX;
            dragOffsetY = event.y() - panelY;
            setDragging(true);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseDragged(@NonNull MouseButtonEvent event, double dragX, double dragY) {
        if (draggingPanel && event.button() == 0) {
            panelX = (int) Math.round(event.x() - dragOffsetX);
            panelY = (int) Math.round(event.y() - dragOffsetY);
            clampPanelPosition();
            rebuildMenuWidgets();
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(@NonNull MouseButtonEvent event) {
        if (draggingPanel && event.button() == 0) {
            draggingPanel = false;
            setDragging(false);
            return true;
        }
        return super.mouseReleased(event);
    }

    protected abstract void applyBackgroundEffects();

    protected static @NonNull ToggleOption toggle(@NonNull String label, Supplier<Boolean> getter,
            Consumer<Boolean> setter) {
        return new ToggleOption(label, getter, setter);
    }

    protected static @NonNull SliderOption slider(@NonNull String label, double min, double max,
            DoubleSupplier getter,
            DoubleConsumer setter, Function<Double, @NonNull String> formatter) {
        return new SliderOption(label, min, max, getter, setter, formatter);
    }

    protected static @NonNull ActionOption action(@NonNull String label, Runnable action) {
        return new ActionOption(label, action);
    }

    protected static @NonNull ColorPickerOption colorPicker(@NonNull String label, DoubleSupplier getter,
            DoubleConsumer setter) {
        return new ColorPickerOption(label, getter, setter);
    }

    @SafeVarargs
    protected static <T> List<@NonNull T> listOf(@NonNull T... values) {
        List<@NonNull T> result = new ArrayList<>(values.length);
        for (T value : values) {
            result.add(Objects.requireNonNull(value));
        }
        return List.copyOf(result);
    }

    private void rebuildMenuWidgets() {
        this.clearWidgets();
        this.sectionRenderBoxes.clear();
        this.controlRenderBoxes.clear();
        this.tabRenderBoxes.clear();
        this.searchWidget = null;
        this.searchRenderBox = null;
        this.closeButtonWidget = null;

        int panelX = getPanelX();
        int panelY = getPanelY();

        addHeaderWidgets(panelX, panelY);
        addTabButtons(panelX, panelY);
        addDynamicSectionWidgets(panelX, panelY);
    }

    private void addHeaderWidgets(int panelX, int panelY) {
        Button closeButton = Button.builder(Component.empty(), button -> onClose())
                .bounds(panelX + PANEL_WIDTH - 28, panelY + 5, 20, 20)
                .build();
        closeButton.setAlpha(0.0f);
        this.addRenderableWidget(closeButton);
        this.closeButtonWidget = closeButton;

        int searchFrameX = panelX + PANEL_WIDTH - 170;
        int searchFrameY = panelY + 7;
        int searchFrameWidth = 130;
        int searchFrameHeight = 16;
        int searchIconLaneWidth = 14;

        EditBox box = new EditBox(this.font, searchFrameX + 4, searchFrameY + 4,
                searchFrameWidth - searchIconLaneWidth - 6, searchFrameHeight - 4,
                Component.literal("search"));
        box.setValue(Objects.requireNonNull(searchText));
        box.setSuggestion("");
        box.setBordered(false);
        box.setTextColor(CONTROL_TEXT_COLOR);
        box.setTextColorUneditable(CONTROL_TEXT_COLOR);
        box.setInvertHighlightedTextColor(false);
        box.setResponder(value -> {
            searchText = value;
            rebuildMenuWidgets();
        });
        this.addRenderableWidget(box);
        this.searchWidget = box;
        this.searchRenderBox = new SearchRenderBox(searchFrameX, searchFrameY, searchFrameWidth, searchFrameHeight,
                searchIconLaneWidth);
    }

    private void addTabButtons(int panelX, int panelY) {
        int tabY = panelY + 8;
        int tabWidth = 76;
        int tabHeight = 16;
        int tabSpacing = 4;
        int tabX = panelX + 116;

        for (int i = 0; i < tabs.size(); i++) {
            int tabIndex = i;
            MenuTab tab = getTabAt(i);
            Button tabButton = Button.builder(Component.literal(Objects.requireNonNull(tab.label())), button -> {
                selectedTabIndex = tabIndex;
                rebuildMenuWidgets();
            }).bounds(tabX, tabY, tabWidth, tabHeight).build();
            tabButton.active = tabIndex != selectedTabIndex;
            tabButton.setAlpha(0.0f);
            this.addRenderableWidget(tabButton);
            this.tabRenderBoxes
                    .add(new TabRenderBox(tabX, tabY, tabWidth, tabHeight, tabButton, tab.label(), tabIndex));
            tabX += tabWidth + tabSpacing;
        }
    }

    private void addDynamicSectionWidgets(int panelX, int panelY) {
        if (tabs.isEmpty()) {
            return;
        }

        MenuTab selectedTab = getTabAtClampedIndex(selectedTabIndex);
        List<@NonNull MenuSection> sections = getSections(selectedTab);

        int contentX = panelX + 14;
        int contentY = panelY + HEADER_HEIGHT + 12;
        int contentW = PANEL_WIDTH - 28;
        int contentH = PANEL_HEIGHT - HEADER_HEIGHT - 24;
        int columnW = (contentW - 8) / 2;

        int[] columnCursorY = new int[] { contentY, contentY };

        for (MenuSection section : sections) {
            List<@NonNull MenuControl> filtered = filterSectionControls(section, searchText);
            if (filtered.isEmpty()) {
                continue;
            }

            int column = Math.max(0, Math.min(1, section.column()));
            int x = contentX + (column * (columnW + 8));
            int y = columnCursorY[column];
            int controlCount = filtered.size();
            int innerTop = y + 22;
            int boxHeight = 30 + controlCount * CONTROL_HEIGHT + Math.max(0, controlCount - 1) * CONTROL_SPACING;

            if (y + boxHeight > contentY + contentH) {
                continue;
            }

            sectionRenderBoxes.add(new SectionRenderBox(x, y, columnW, boxHeight, section.title()));

            int controlY = innerTop;
            for (MenuControl control : filtered) {
                int controlX = x + 8;
                int controlW = columnW - 16;
                addControlWidget(control, controlX, controlY, controlW);
                controlY += CONTROL_HEIGHT + CONTROL_SPACING;
            }

            columnCursorY[column] += boxHeight + 8;
        }
    }

    private List<@NonNull MenuControl> filterSectionControls(MenuSection section, String query) {
        String trimmed = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (trimmed.isEmpty()) {
            return section.controls();
        }

        boolean sectionMatch = section.title().toLowerCase(Locale.ROOT).contains(trimmed);
        if (sectionMatch) {
            return section.controls();
        }

        List<@NonNull MenuControl> filtered = new ArrayList<>();
        for (MenuControl control : section.controls()) {
            if (control.label().toLowerCase(Locale.ROOT).contains(trimmed)) {
                filtered.add(control);
            }
        }
        return filtered;
    }

    private void addControlWidget(MenuControl control, int x, int y, int width) {
        if (control instanceof ToggleOption toggleOption) {
            Button toggleButton = Button.builder(Component.empty(), button -> {
                boolean nextValue = !toggleOption.getter().get();
                toggleOption.setter().accept(nextValue);
                button.setMessage(Objects.requireNonNull(buildToggleMessage(toggleOption.label(), nextValue)));
            }).bounds(x, y, width, CONTROL_HEIGHT).build();
            toggleButton.setMessage(
                    Objects.requireNonNull(buildToggleMessage(toggleOption.label(), toggleOption.getter().get())));
            toggleButton.setAlpha(0.0f);
            this.addRenderableWidget(toggleButton);
            this.controlRenderBoxes.add(new ControlRenderBox(
                    ControlVisualType.TOGGLE,
                    x,
                    y,
                    width,
                    CONTROL_HEIGHT,
                    toggleButton,
                    toggleOption.label(),
                    toggleOption.getter(),
                    null,
                    null,
                    null));
            return;
        }

        if (control instanceof SliderOption sliderOption) {
            DynamicSlider sliderWidget = new DynamicSlider(
                    x,
                    y,
                    width,
                    CONTROL_HEIGHT,
                    sliderOption.label(),
                    sliderOption.min(),
                    sliderOption.max(),
                    sliderOption.getter(),
                    sliderOption.setter(),
                    sliderOption.formatter());
            this.addRenderableWidget(sliderWidget);
            sliderWidget.setAlpha(0.0f);
            this.controlRenderBoxes.add(new ControlRenderBox(
                    ControlVisualType.SLIDER,
                    x,
                    y,
                    width,
                    CONTROL_HEIGHT,
                    sliderWidget,
                    sliderOption.label(),
                    null,
                    () -> sliderOption.formatter().apply(sliderOption.getter().getAsDouble()),
                    () -> {
                        double min = sliderOption.min();
                        double max = sliderOption.max();
                        double span = max - min;
                        if (span <= 0.0) {
                            return 0.0;
                        }
                        double current = sliderOption.getter().getAsDouble();
                        return Math.max(0.0, Math.min(1.0, (current - min) / span));
                    },
                    null));
            return;
        }

        if (control instanceof ActionOption actionOption) {
            Button actionButton = Button
                    .builder(Component.literal(Objects.requireNonNull(actionOption.label())), button -> {
                        actionOption.action().run();
                        rebuildMenuWidgets();
                    }).bounds(x, y, width, CONTROL_HEIGHT).build();
            actionButton.setAlpha(0.0f);
            this.addRenderableWidget(actionButton);
            this.controlRenderBoxes.add(new ControlRenderBox(
                    ControlVisualType.ACTION,
                    x,
                    y,
                    width,
                    CONTROL_HEIGHT,
                    actionButton,
                    actionOption.label(),
                    null,
                    null,
                    null,
                    null));
            return;
        }

        if (control instanceof ColorPickerOption colorOption) {
            HueSlider hueWidget = new HueSlider(
                    x,
                    y,
                    width,
                    CONTROL_HEIGHT,
                    colorOption.getter(),
                    colorOption.setter());
            this.addRenderableWidget(hueWidget);
            hueWidget.setAlpha(0.0f);
            this.controlRenderBoxes.add(new ControlRenderBox(
                    ControlVisualType.HUE_PICKER,
                    x,
                    y,
                    width,
                    CONTROL_HEIGHT,
                    hueWidget,
                    colorOption.label(),
                    null,
                    null,
                    null,
                    colorOption.getter()));
        }
    }

    private Component buildToggleMessage(String label, boolean value) {
        return Component.literal(label + "  [" + (value ? "on" : "off") + "]");
    }

    private MenuTab getTabAt(int index) {
        return Objects.requireNonNull(tabs.get(index));
    }

    private MenuTab getTabAtClampedIndex(int index) {
        int clamped = Math.max(0, Math.min(index, tabs.size() - 1));
        return getTabAt(clamped);
    }

    private List<@NonNull MenuSection> getSections(MenuTab tab) {
        return new ArrayList<>(Objects.requireNonNull(tab.sections()));
    }

    private int getPanelX() {
        return panelX;
    }

    private int getPanelY() {
        return panelY;
    }

    private boolean isInDragHandle(double mouseX, double mouseY) {
        return mouseX >= panelX
                && mouseX <= panelX + 108
                && mouseY >= panelY
                && mouseY <= panelY + HEADER_HEIGHT;
    }

    private void clampPanelPosition() {
        int maxX = Math.max(0, this.width - PANEL_WIDTH);
        int maxY = Math.max(0, this.height - PANEL_HEIGHT);
        panelX = Math.max(0, Math.min(maxX, panelX));
        panelY = Math.max(0, Math.min(maxY, panelY));
    }

    private int themeColor(float saturation, float brightness, int alpha) {
        float hue = (float) Math.max(0.0, Math.min(1.0, StarClientOptions.menuThemeHue));
        int rgb = Color.HSBtoRGB(hue, saturation, brightness) & 0x00FFFFFF;
        int clampedAlpha = Math.max(0, Math.min(255, alpha));
        return (clampedAlpha << 24) | rgb;
    }

    private int getPanelBorderColor() {
        return themeColor(0.63f, 0.62f, 255);
    }

    private int getPanelInnerBorderColor() {
        return themeColor(0.50f, 0.33f, 220);
    }

    private int getSubtitleColor() {
        return themeColor(0.35f, 0.90f, 255);
    }

    private int getControlBorderColor() {
        return themeColor(0.44f, 0.36f, 255);
    }

    private int getControlAccentColor() {
        return themeColor(0.58f, 0.82f, 255);
    }

    private int getControlValueColor() {
        return themeColor(0.34f, 0.84f, 255);
    }

    private int getTabActiveTextColor() {
        return themeColor(0.12f, 0.98f, 255);
    }

    private int getTabHoverTextColor() {
        return themeColor(0.20f, 0.84f, 255);
    }

    private int getCloseButtonRingColor() {
        return themeColor(0.60f, 0.76f, 255);
    }

    private int getCloseButtonHoverRingColor() {
        return themeColor(0.55f, 0.90f, 255);
    }

    @Override
    public void render(@NonNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, BACKGROUND_COLOR);

        applyBackgroundEffects();
        shootingStarsRenderer.render(context, this.width, this.height);

        int panelX = getPanelX();
        int panelY = getPanelY();

        int panelBorderColor = getPanelBorderColor();
        int panelInnerBorderColor = getPanelInnerBorderColor();

        context.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, PANEL_COLOR);
        context.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + HEADER_HEIGHT, HEADER_COLOR);
        context.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + 1, panelBorderColor);
        context.fill(panelX, panelY + PANEL_HEIGHT - 1, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT,
                panelBorderColor);
        context.fill(panelX, panelY, panelX + 1, panelY + PANEL_HEIGHT, panelBorderColor);
        context.fill(panelX + PANEL_WIDTH - 1, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT,
                panelBorderColor);
        context.fill(panelX + 1, panelY + HEADER_HEIGHT, panelX + PANEL_WIDTH - 1, panelY + HEADER_HEIGHT + 1,
                panelInnerBorderColor);

        context.drawString(this.font, Component.literal("✦"), panelX + 10, panelY + 10, panelBorderColor, false);
        context.drawString(this.font, Component.literal("starclient"), panelX + 24, panelY + 10, TITLE_COLOR, false);

        for (SectionRenderBox box : sectionRenderBoxes) {
            drawGroupBox(context, box.x(), box.y(), box.width(), box.height(), box.title());
        }

        drawCustomCloseButton(context);
        drawCustomSearchWidget(context);
        drawCustomTabWidgets(context);

        super.render(context, mouseX, mouseY, delta);
        drawCustomControlWidgets(context, mouseX, mouseY);
    }

    private void drawCustomCloseButton(GuiGraphics context) {
        Button closeButton = this.closeButtonWidget;
        Minecraft client = this.minecraft;
        if (closeButton == null || client == null) {
            return;
        }

        LocalPlayer player = client.player;
        if (player == null) {
            return;
        }

        int x = closeButton.getX();
        int y = closeButton.getY();
        int size = closeButton.getWidth();
        boolean hovered = closeButton.isHoveredOrFocused();

        int ringColor = hovered ? getCloseButtonHoverRingColor() : getCloseButtonRingColor();
        context.fill(x, y, x + size, y + size, GROUP_COLOR);
        context.fill(x, y, x + size, y + 1, ringColor);
        context.fill(x, y + size - 1, x + size, y + size, ringColor);
        context.fill(x, y, x + 1, y + size, ringColor);
        context.fill(x + size - 1, y, x + size, y + size, ringColor);

        int faceSize = size - 4;
        int faceX = x + 2;
        int faceY = y + 2;
        PlayerFaceRenderer.draw(context, player.getSkin(), faceX, faceY, faceSize);

        int cornerMask = HEADER_COLOR;
        context.fill(faceX, faceY, faceX + 1, faceY + 1, cornerMask);
        context.fill(faceX + faceSize - 1, faceY, faceX + faceSize, faceY + 1, cornerMask);
        context.fill(faceX, faceY + faceSize - 1, faceX + 1, faceY + faceSize, cornerMask);
        context.fill(faceX + faceSize - 1, faceY + faceSize - 1, faceX + faceSize, faceY + faceSize, cornerMask);
    }

    private void drawCustomSearchWidget(GuiGraphics context) {
        EditBox search = this.searchWidget;
        SearchRenderBox searchBox = this.searchRenderBox;
        if (search == null || searchBox == null) {
            return;
        }

        int x = searchBox.x();
        int y = searchBox.y();
        int width = searchBox.width();
        int height = searchBox.height();
        boolean focused = search.isFocused();

        int bg = focused ? SEARCH_BG_FOCUS_COLOR : SEARCH_BG_COLOR;
        int border = focused ? getControlAccentColor() : getControlBorderColor();

        context.fill(x, y, x + width, y + height, bg);
        context.fill(x, y, x + width, y + 1, border);
        context.fill(x, y + height - 1, x + width, y + height, border);
        context.fill(x, y, x + 1, y + height, border);
        context.fill(x + width - 1, y, x + width, y + height, border);

        if (search.getValue().isEmpty() && !focused) {
            context.drawString(this.font, Component.literal("search"), x + 5, y + 6, SEARCH_PLACEHOLDER_COLOR, false);
        }

        int iconLaneX = x + width - searchBox.iconLaneWidth();
        int iconCenterX = iconLaneX + (searchBox.iconLaneWidth() / 2);
        int iconCenterY = y + (height / 2);
        drawMagnifierIcon(context, iconCenterX, iconCenterY, getControlValueColor());
    }

    private void drawMagnifierIcon(GuiGraphics context, int centerX, int centerY, int color) {
        int radius = 3;
        int left = centerX - radius;
        int top = centerY - radius;
        int right = centerX + radius;
        int bottom = centerY + radius;

        context.fill(left + 1, top, right, top + 1, color);
        context.fill(left + 1, bottom, right, bottom + 1, color);
        context.fill(left, top + 1, left + 1, bottom, color);
        context.fill(right, top + 1, right + 1, bottom, color);
        context.fill(centerX + 2, centerY + 2, centerX + 5, centerY + 3, color);
        context.fill(centerX + 3, centerY + 3, centerX + 4, centerY + 5, color);
    }

    private void drawCustomTabWidgets(GuiGraphics context) {
        for (TabRenderBox tab : tabRenderBoxes) {
            boolean selected = tab.index() == selectedTabIndex;
            boolean hovered = tab.button().isHoveredOrFocused();
            int textColor = selected ? getTabActiveTextColor() : (hovered ? getTabHoverTextColor() : TAB_TEXT_COLOR);
            String tabLabel = tab.label();

            int textWidth = this.font.width(tabLabel);
            int textX = tab.x() + (tab.width() - textWidth) / 2;
            int textY = tab.y() + 4;
            context.drawString(this.font, Component.literal(tabLabel), textX, textY, textColor, false);

            if (selected) {
                int underlineY = tab.y() + tab.height() - 1;
                context.fill(tab.x() + 6, underlineY, tab.x() + tab.width() - 6, underlineY + 1,
                        getControlAccentColor());
            } else if (hovered) {
                int underlineY = tab.y() + tab.height() - 1;
                context.fill(tab.x() + 10, underlineY, tab.x() + tab.width() - 10, underlineY + 1,
                        getControlBorderColor());
            }
        }
    }

    private void drawCustomControlWidgets(GuiGraphics context, int mouseX, int mouseY) {
        for (ControlRenderBox box : controlRenderBoxes) {
            boolean hovered = box.widget().isHoveredOrFocused();
            int background = hovered ? CONTROL_BG_HOVER_COLOR : CONTROL_BG_COLOR;

            int x = box.x();
            int y = box.y();
            int width = box.width();
            int height = box.height();

            context.fill(x, y, x + width, y + height, background);
            int borderColor = getControlBorderColor();
            context.fill(x, y, x + width, y + 1, borderColor);
            context.fill(x, y + height - 1, x + width, y + height, borderColor);
            context.fill(x, y, x + 1, y + height, borderColor);
            context.fill(x + width - 1, y, x + width, y + height, borderColor);

            switch (box.type()) {
                case TOGGLE -> drawToggleControl(context, box);
                case ACTION -> drawActionControl(context, box);
                case SLIDER -> drawSliderControl(context, box);
                case HUE_PICKER -> drawHuePickerControl(context, box);
            }
        }
    }

    private void drawHuePickerControl(GuiGraphics context, ControlRenderBox box) {
        int x = box.x();
        int y = box.y();
        int width = box.width();

        context.drawString(this.font, Component.literal(Objects.requireNonNull(box.label())), x + 6, y + 3,
                CONTROL_TEXT_COLOR, false);

        int gradientX = x + 6;
        int gradientY = y + 14;
        int gradientWidth = width - 28;
        int gradientHeight = 2;

        if (gradientWidth > 0) {
            for (int i = 0; i < gradientWidth; i++) {
                float hue = i / (float) Math.max(1, gradientWidth - 1);
                int color = (0xFF << 24) | (Color.HSBtoRGB(hue, 0.65f, 0.95f) & 0x00FFFFFF);
                context.fill(gradientX + i, gradientY, gradientX + i + 1, gradientY + gradientHeight, color);
            }
        }

        context.fill(gradientX, gradientY - 1, gradientX + gradientWidth, gradientY, getControlBorderColor());
        context.fill(gradientX, gradientY + gradientHeight, gradientX + gradientWidth, gradientY + gradientHeight + 1,
                getControlBorderColor());

        DoubleSupplier hueGetter = box.hueGetter();
        double hueValue = hueGetter == null ? 0.0 : hueGetter.getAsDouble();
        double clampedHue = Math.max(0.0, Math.min(1.0, hueValue));
        int handleX = gradientX + (int) Math.round(clampedHue * Math.max(0, gradientWidth - 1));
        context.fill(handleX - 1, gradientY - 2, handleX + 1, gradientY + gradientHeight + 2, 0xFFFFFFFF);

        int swatchColor = (0xFF << 24) | (Color.HSBtoRGB((float) clampedHue, 0.65f, 0.95f) & 0x00FFFFFF);
        int swatchX = x + width - 16;
        int swatchY = y + 5;
        context.fill(swatchX, swatchY, swatchX + 10, swatchY + 10, swatchColor);
        context.fill(swatchX, swatchY, swatchX + 10, swatchY + 1, getControlBorderColor());
        context.fill(swatchX, swatchY + 9, swatchX + 10, swatchY + 10, getControlBorderColor());
        context.fill(swatchX, swatchY, swatchX + 1, swatchY + 10, getControlBorderColor());
        context.fill(swatchX + 9, swatchY, swatchX + 10, swatchY + 10, getControlBorderColor());
    }

    private void drawToggleControl(GuiGraphics context, ControlRenderBox box) {
        int x = box.x();
        int y = box.y();
        int width = box.width();
        int height = box.height();

        context.drawString(this.font, Component.literal(Objects.requireNonNull(box.label())), x + 6, y + 6,
                CONTROL_TEXT_COLOR, false);

        int toggleSize = 10;
        int toggleX = x + width - toggleSize - 6;
        int toggleY = y + (height - toggleSize) / 2;
        context.fill(toggleX, toggleY, toggleX + toggleSize, toggleY + toggleSize, GROUP_COLOR);
        int controlBorderColor = getControlBorderColor();
        context.fill(toggleX, toggleY, toggleX + toggleSize, toggleY + 1, controlBorderColor);
        context.fill(toggleX, toggleY + toggleSize - 1, toggleX + toggleSize, toggleY + toggleSize,
                controlBorderColor);
        context.fill(toggleX, toggleY, toggleX + 1, toggleY + toggleSize, controlBorderColor);
        context.fill(toggleX + toggleSize - 1, toggleY, toggleX + toggleSize, toggleY + toggleSize,
                controlBorderColor);

        Supplier<Boolean> toggleGetter = box.toggleGetter();
        boolean enabled = toggleGetter != null && toggleGetter.get();
        if (enabled) {
            context.fill(toggleX + 2, toggleY + 2, toggleX + toggleSize - 2, toggleY + toggleSize - 2,
                    getControlAccentColor());
        }
    }

    private void drawActionControl(GuiGraphics context, ControlRenderBox box) {
        int x = box.x();
        int y = box.y();
        int width = box.width();

        context.drawString(this.font, Component.literal(Objects.requireNonNull(box.label())), x + 6, y + 6,
                CONTROL_TEXT_COLOR, false);
        context.drawString(this.font, Component.literal("+"), x + width - 10, y + 6, getControlValueColor(), false);
    }

    private void drawSliderControl(GuiGraphics context, ControlRenderBox box) {
        int x = box.x();
        int y = box.y();
        int width = box.width();

        context.drawString(this.font, Component.literal(Objects.requireNonNull(box.label())), x + 6, y + 3,
                CONTROL_TEXT_COLOR, false);

        Supplier<String> sliderTextGetter = box.sliderTextGetter();
        String valueTextRaw = sliderTextGetter == null ? null : sliderTextGetter.get();
        String valueText = valueTextRaw == null ? "" : valueTextRaw;
        int valueTextWidth = this.font.width(valueText);
        context.drawString(this.font, Component.literal(valueText), x + width - valueTextWidth - 6, y + 3,
                getControlValueColor(), false);

        int trackX = x + 6;
        int trackY = y + 14;
        int trackWidth = width - 12;
        context.fill(trackX, trackY, trackX + trackWidth, trackY + 2, getControlBorderColor());

        DoubleSupplier sliderProgressGetter = box.sliderProgressGetter();
        double progress = sliderProgressGetter == null ? 0.0 : sliderProgressGetter.getAsDouble();
        int fillWidth = (int) Math.round(trackWidth * Math.max(0.0, Math.min(1.0, progress)));
        context.fill(trackX, trackY, trackX + fillWidth, trackY + 2, getControlAccentColor());
    }

    private void drawGroupBox(GuiGraphics context, int x, int y, int width, int height, String title) {
        context.fill(x, y, x + width, y + height, GROUP_COLOR);
        int panelInnerBorderColor = getPanelInnerBorderColor();
        context.fill(x, y, x + width, y + 1, panelInnerBorderColor);
        context.fill(x, y + height - 1, x + width, y + height, panelInnerBorderColor);
        context.fill(x, y, x + 1, y + height, panelInnerBorderColor);
        context.fill(x + width - 1, y, x + width, y + height, panelInnerBorderColor);
        context.drawString(this.font, Component.literal(Objects.requireNonNull(title)), x + 8, y + 7,
                getSubtitleColor(), false);
    }

    public record MenuTab(@NonNull String label, List<@NonNull MenuSection> sections) {
    }

    public record MenuSection(@NonNull String title, int column, List<@NonNull MenuControl> controls) {
    }

    public sealed interface MenuControl permits ToggleOption, SliderOption, ActionOption, ColorPickerOption {
        @NonNull
        String label();
    }

    public record ToggleOption(@NonNull String label, Supplier<Boolean> getter, Consumer<Boolean> setter)
            implements MenuControl {
    }

    public record SliderOption(@NonNull String label, double min, double max, DoubleSupplier getter,
            DoubleConsumer setter,
            Function<Double, @NonNull String> formatter) implements MenuControl {
    }

    public record ActionOption(@NonNull String label, Runnable action) implements MenuControl {
    }

    public record ColorPickerOption(@NonNull String label, DoubleSupplier getter, DoubleConsumer setter)
            implements MenuControl {
    }

    private record SectionRenderBox(int x, int y, int width, int height, String title) {
    }

    private enum ControlVisualType {
        TOGGLE,
        ACTION,
        SLIDER,
        HUE_PICKER
    }

    private record ControlRenderBox(
            ControlVisualType type,
            int x,
            int y,
            int width,
            int height,
            @NonNull AbstractWidget widget,
            @NonNull String label,
            @Nullable Supplier<Boolean> toggleGetter,
            @Nullable Supplier<String> sliderTextGetter,
            @Nullable DoubleSupplier sliderProgressGetter,
            @Nullable DoubleSupplier hueGetter) {
    }

    private record TabRenderBox(int x, int y, int width, int height, @NonNull Button button, @NonNull String label,
            int index) {
    }

    private record SearchRenderBox(int x, int y, int width, int height, int iconLaneWidth) {
    }

    private static final class DynamicSlider extends AbstractSliderButton {
        private final String label;
        private final double min;
        private final double max;
        private final DoubleConsumer setter;
        private final DoubleSupplier getter;
        private final Function<Double, @NonNull String> valueFormatter;

        private DynamicSlider(int x, int y, int width, int height, String label, double min, double max,
                DoubleSupplier getter, DoubleConsumer setter, Function<Double, @NonNull String> valueFormatter) {
            super(x, y, width, height, Component.empty(), 0.0);
            this.label = label;
            this.min = min;
            this.max = max;
            this.setter = setter;
            this.getter = getter;
            this.valueFormatter = valueFormatter;
            syncFromOption();
        }

        private void syncFromOption() {
            double current = clamp(getter.getAsDouble(), min, max);
            this.value = (current - min) / (max - min);
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            double current = min + (max - min) * this.value;
            this.setMessage(Component.literal(label + ": " + valueFormatter.apply(current)));
        }

        @Override
        protected void applyValue() {
            double current = min + (max - min) * this.value;
            setter.accept(clamp(current, min, max));
            syncFromOption();
        }

        private static double clamp(double value, double min, double max) {
            return Math.max(min, Math.min(max, value));
        }
    }

    private static final class HueSlider extends AbstractSliderButton {
        private static final int TRACK_LEFT_PADDING = 6;
        private static final int TRACK_RIGHT_PADDING = 22;
        private final DoubleConsumer setter;
        private final DoubleSupplier getter;

        private HueSlider(int x, int y, int width, int height, DoubleSupplier getter, DoubleConsumer setter) {
            super(x, y, width, height, Component.empty(), 0.0);
            this.setter = setter;
            this.getter = getter;
            syncFromOption();
        }

        private void syncFromOption() {
            this.value = clamp(getter.getAsDouble());
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.empty());
        }

        @Override
        protected void applyValue() {
            setter.accept(clamp(this.value));
            syncFromOption();
        }

        @Override
        public void onClick(@NonNull MouseButtonEvent mouseButtonEvent, boolean doubleClick) {
            this.setValueFromTrack(mouseButtonEvent.x());
        }

        @Override
        protected void onDrag(@NonNull MouseButtonEvent mouseButtonEvent, double dragX, double dragY) {
            this.setValueFromTrack(mouseButtonEvent.x());
        }

        private void setValueFromTrack(double mouseX) {
            int trackStart = this.getX() + TRACK_LEFT_PADDING;
            int trackEnd = this.getX() + this.getWidth() - TRACK_RIGHT_PADDING;
            int trackWidth = Math.max(2, trackEnd - trackStart);
            double normalized = (mouseX - trackStart) / (trackWidth - 1.0);
            this.setValue(clamp(normalized));
        }

        private static double clamp(double value) {
            return Math.max(0.0, Math.min(1.0, value));
        }
    }
}
