package com.starclient.screen.gui;

import com.starclient.StarClientOptions;
import net.minecraft.client.gui.GuiGraphics;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
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
    private static final int SEARCH_FRAME_DEFAULT_WIDTH = 130;
    private static final int SEARCH_FRAME_MIN_WIDTH = 88;
    private static final int SEARCH_FRAME_RIGHT_MARGIN = 40;

    private static final int DEFAULT_PANEL_WIDTH = 560;
    private static final int DEFAULT_PANEL_HEIGHT = 360;
    private static final int MIN_PANEL_WIDTH = 420;
    private static final int MIN_PANEL_HEIGHT = 260;
    private static final int RESIZE_HANDLE_SIZE = 12;
    private static final int HEADER_HEIGHT = 30;

    private static final int CONTROL_HEIGHT = 20;
    private static final int CONTROL_SPACING = 4;

    private static int persistedPanelX = Integer.MIN_VALUE;
    private static int persistedPanelY = Integer.MIN_VALUE;
    private static int persistedPanelWidth = DEFAULT_PANEL_WIDTH;
    private static int persistedPanelHeight = DEFAULT_PANEL_HEIGHT;
    private static int persistedSelectedTabIndex = 0;
    private static String persistedSelectedSubTabLabel = "";
    private static final Map<String, List<String>> persistedSectionOrderByContext = new HashMap<>();

    @Nullable
    private final Screen previousScreen;
    protected final ShootingStarsRenderer shootingStarsRenderer = new ShootingStarsRenderer();
    private final List<@NonNull MenuTab> tabs;

    private int selectedTabIndex = 0;
    private String searchText = "";
    private int panelX = Integer.MIN_VALUE;
    private int panelY = Integer.MIN_VALUE;
    private int panelWidth = DEFAULT_PANEL_WIDTH;
    private int panelHeight = DEFAULT_PANEL_HEIGHT;
    private boolean draggingPanel = false;
    private boolean resizingPanel = false;
    private double dragOffsetX = 0.0;
    private double dragOffsetY = 0.0;
    private double resizeStartMouseX = 0.0;
    private double resizeStartMouseY = 0.0;
    private int resizeStartWidth = DEFAULT_PANEL_WIDTH;
    private int resizeStartHeight = DEFAULT_PANEL_HEIGHT;

    private final List<SectionRenderBox> sectionRenderBoxes = new ArrayList<>();
    private final List<ControlRenderBox> controlRenderBoxes = new ArrayList<>();
    private final List<TabRenderBox> tabRenderBoxes = new ArrayList<>();
    private final List<SubTabRenderBox> subTabRenderBoxes = new ArrayList<>();
    private final Map<String, List<String>> sectionOrderByContext = new HashMap<>();
    private static final Map<String, Boolean> persistedHuePickerRainbowByControlKey = new HashMap<>();
    private static final Map<String, Double> persistedHuePickerRainbowSpeedByControlKey = new HashMap<>();

    @Nullable
    private EditBox searchWidget;
    @Nullable
    private SearchRenderBox searchRenderBox;
    @Nullable
    private Button closeButtonWidget;

    private String selectedSubTabLabel = "";
    private boolean draggingSection = false;
    @Nullable
    private String draggingSectionKey;
    private double dragSectionOffsetX = 0.0;
    private double dragSectionOffsetY = 0.0;

    protected DynamicOptionPanelScreen(@Nullable Screen previousScreen, Component title,
            List<@NonNull MenuTab> tabs) {
        super(title);
        this.previousScreen = previousScreen;
        this.tabs = tabs;

        this.panelX = persistedPanelX;
        this.panelY = persistedPanelY;
        this.panelWidth = persistedPanelWidth;
        this.panelHeight = persistedPanelHeight;
        this.selectedTabIndex = tabs.isEmpty() ? 0 : Math.max(0, Math.min(persistedSelectedTabIndex, tabs.size() - 1));
        this.selectedSubTabLabel = persistedSelectedSubTabLabel;

        if (!persistedSectionOrderByContext.isEmpty()) {
            for (Map.Entry<String, List<String>> entry : persistedSectionOrderByContext.entrySet()) {
                this.sectionOrderByContext.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
        }
    }

    @Override
    public void onClose() {
        persistMenuState();
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.previousScreen);
        }
    }

    private void persistMenuState() {
        clampPanelSize();
        clampPanelPosition();

        persistedPanelX = this.panelX;
        persistedPanelY = this.panelY;
        persistedPanelWidth = this.panelWidth;
        persistedPanelHeight = this.panelHeight;
        persistedSelectedTabIndex = this.selectedTabIndex;
        persistedSelectedSubTabLabel = this.selectedSubTabLabel;

        persistedSectionOrderByContext.clear();
        for (Map.Entry<String, List<String>> entry : this.sectionOrderByContext.entrySet()) {
            persistedSectionOrderByContext.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
    }

    @Override
    protected void init() {
        clampPanelSize();
        if (panelX == Integer.MIN_VALUE || panelY == Integer.MIN_VALUE) {
            panelX = (this.width - panelWidth) / 2;
            panelY = (this.height - panelHeight) / 2;
        }
        clampPanelPosition();
        rebuildMenuWidgets();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        clampPanelSize();
        clampPanelPosition();
        rebuildMenuWidgets();
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
        boolean handled = super.mouseClicked(event, doubleClick);
        if (handled) {
            return true;
        }

        if (event.button() == 0) {
            SectionRenderBox section = findSectionAt(event.x(), event.y(), true);
            if (section != null) {
                draggingSection = true;
                draggingSectionKey = section.sectionKey();
                dragSectionOffsetX = event.x() - section.x();
                dragSectionOffsetY = event.y() - section.y();
                setDragging(true);
                return true;
            }
        }

        if (event.button() == 0 && isInResizeHandle(event.x(), event.y())) {
            resizingPanel = true;
            resizeStartMouseX = event.x();
            resizeStartMouseY = event.y();
            resizeStartWidth = panelWidth;
            resizeStartHeight = panelHeight;
            setDragging(true);
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
        if (draggingSection && event.button() == 0) {
            return true;
        }

        if (resizingPanel && event.button() == 0) {
            int widthDelta = (int) Math.round(event.x() - resizeStartMouseX);
            int heightDelta = (int) Math.round(event.y() - resizeStartMouseY);
            panelWidth = resizeStartWidth + widthDelta;
            panelHeight = resizeStartHeight + heightDelta;
            clampPanelSize();
            clampPanelPosition();
            rebuildMenuWidgets();
            return true;
        }

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
        if (draggingSection && event.button() == 0) {
            String sourceKey = draggingSectionKey;
            draggingSection = false;
            draggingSectionKey = null;
            dragSectionOffsetX = 0.0;
            dragSectionOffsetY = 0.0;
            setDragging(false);

            if (sourceKey != null) {
                SectionRenderBox source = findSectionByKey(sourceKey);
                SectionRenderBox target = findSectionAt(event.x(), event.y(), false);
                if (source != null && target != null && !Objects.equals(source.sectionKey(), target.sectionKey())) {
                    swapSectionOrder(source.sectionKey(), target.sectionKey());
                    rebuildMenuWidgets();
                }
            }
            return true;
        }

        if (resizingPanel && event.button() == 0) {
            resizingPanel = false;
            setDragging(false);
            return true;
        }

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

    protected static @NonNull ColorPickerOption colorPicker(@NonNull String label, DoubleSupplier getter,
            DoubleConsumer setter, BooleanSupplier rainbowGetter, Consumer<Boolean> rainbowSetter) {
        return new ColorPickerOption(label, getter, setter, rainbowGetter, rainbowSetter);
    }

    protected static @NonNull ColorPickerOption colorPicker(@NonNull String label, DoubleSupplier getter,
            DoubleConsumer setter, BooleanSupplier rainbowGetter, Consumer<Boolean> rainbowSetter,
            DoubleSupplier rainbowSpeedGetter, DoubleConsumer rainbowSpeedSetter) {
        return new ColorPickerOption(
                label,
                getter,
                setter,
                rainbowGetter,
                rainbowSetter,
                rainbowSpeedGetter,
                rainbowSpeedSetter);
    }

    protected static @NonNull SeparatorOption separator(@NonNull String label) {
        return new SeparatorOption(label);
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
        this.subTabRenderBoxes.clear();
        this.searchWidget = null;
        this.searchRenderBox = null;
        this.closeButtonWidget = null;

        int panelX = getPanelX();
        int panelY = getPanelY();

        addHeaderWidgets(panelX, panelY);
        addTabButtons(panelX, panelY);
        addSubTabButtons(panelX, panelY);
        addDynamicSectionWidgets(panelX, panelY);
    }

    private void addHeaderWidgets(int panelX, int panelY) {
        Button closeButton = Button.builder(Component.empty(), button -> onClose())
                .bounds(panelX + panelWidth - 28, panelY + 5, 20, 20)
                .build();
        closeButton.setAlpha(0.0f);
        this.addRenderableWidget(closeButton);
        this.closeButtonWidget = closeButton;

        int searchFrameWidth = getSearchFrameWidth();
        int searchFrameX = getSearchFrameX(panelX, searchFrameWidth);
        int searchFrameY = panelY + 7;
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
        int tabY = panelY + 7;
        int tabHeight = 16;
        int defaultTabWidth = 76;
        int minTabWidth = 40;
        int defaultTabSpacing = 4;
        int minTabSpacing = 1;
        int tabX = panelX + 116;

        int searchFrameWidth = getSearchFrameWidth();
        int searchFrameX = getSearchFrameX(panelX, searchFrameWidth);
        int tabRightLimit = searchFrameX - 8;
        int tabCount = tabs.size();
        if (tabCount <= 0) {
            return;
        }

        int availableWidth = Math.max(0, tabRightLimit - tabX);
        int tabSpacing = defaultTabSpacing;

        int tabWidth = defaultTabWidth;
        if (tabCount > 1) {
            int widthWithDefaultSpacing = (availableWidth - (tabCount - 1) * defaultTabSpacing) / tabCount;
            if (widthWithDefaultSpacing < minTabWidth) {
                tabSpacing = minTabSpacing;
            }
            tabWidth = (availableWidth - (tabCount - 1) * tabSpacing) / tabCount;
        } else {
            tabWidth = availableWidth;
        }

        tabWidth = Math.max(minTabWidth, Math.min(defaultTabWidth, tabWidth));

        for (int i = 0; i < tabs.size(); i++) {
            int tabIndex = i;
            MenuTab tab = getTabAt(i);
            int boundedTabX = Math.min(tabX, tabRightLimit - tabWidth);
            boundedTabX = Math.max(panelX + 116, boundedTabX);
            Button tabButton = Button.builder(Component.literal(Objects.requireNonNull(tab.label())), button -> {
                selectedTabIndex = tabIndex;
                selectedSubTabLabel = "";
                rebuildMenuWidgets();
            }).bounds(boundedTabX, tabY, tabWidth, tabHeight).build();
            tabButton.active = tabIndex != selectedTabIndex;
            tabButton.setAlpha(0.0f);
            this.addRenderableWidget(tabButton);
            this.tabRenderBoxes
                    .add(new TabRenderBox(boundedTabX, tabY, tabWidth, tabHeight, tabButton, tab.label(), tabIndex));
            tabX += tabWidth + tabSpacing;
        }
    }

    private int getSearchFrameWidth() {
        int widthLoss = Math.max(0, DEFAULT_PANEL_WIDTH - panelWidth);
        int scaled = SEARCH_FRAME_DEFAULT_WIDTH - (widthLoss / 2);
        return Math.max(SEARCH_FRAME_MIN_WIDTH, Math.min(SEARCH_FRAME_DEFAULT_WIDTH, scaled));
    }

    private int getSearchFrameX(int panelX, int searchFrameWidth) {
        return panelX + panelWidth - searchFrameWidth - SEARCH_FRAME_RIGHT_MARGIN;
    }

    private void addSubTabButtons(int panelX, int panelY) {
        if (tabs.isEmpty()) {
            return;
        }

        MenuTab selectedTab = getTabAtClampedIndex(selectedTabIndex);
        List<@NonNull String> subTabs = getSubTabs(selectedTab);
        if (subTabs.isEmpty()) {
            selectedSubTabLabel = "";
            return;
        }

        if (selectedSubTabLabel.isEmpty() || !subTabs.contains(selectedSubTabLabel)) {
            selectedSubTabLabel = subTabs.get(0);
        }

        int y = panelY + HEADER_HEIGHT + 4;
        int x = panelX + 16;
        int spacing = 6;

        for (String subTabLabel : subTabs) {
            int textWidth = this.font.width(subTabLabel);
            int width = Math.max(42, textWidth + 12);

            Button subTabButton = Button.builder(Component.literal(subTabLabel), button -> {
                selectedSubTabLabel = subTabLabel;
                rebuildMenuWidgets();
            }).bounds(x, y, width, 14).build();

            subTabButton.active = !Objects.equals(selectedSubTabLabel, subTabLabel);
            subTabButton.setAlpha(0.0f);
            this.addRenderableWidget(subTabButton);
            this.subTabRenderBoxes.add(new SubTabRenderBox(x, y, width, 14, subTabButton, subTabLabel));

            x += width + spacing;
        }
    }

    private void addDynamicSectionWidgets(int panelX, int panelY) {
        if (tabs.isEmpty()) {
            return;
        }

        MenuTab selectedTab = getTabAtClampedIndex(selectedTabIndex);
        List<@NonNull MenuSection> sections = orderSectionsForCurrentContext(getSectionsForSelectedSubTab(selectedTab));

        int contentX = panelX + 14;
        int contentY = panelY + HEADER_HEIGHT + 22;
        int contentW = panelWidth - 28;
        int contentH = panelHeight - HEADER_HEIGHT - 34;
        int columnW = (contentW - 8) / 2;

        int[] columnCursorY = new int[] { contentY, contentY };

        int slotIndex = 0;
        for (MenuSection section : sections) {
            List<@NonNull MenuControl> filtered = filterSectionControls(section, searchText);
            if (filtered.isEmpty()) {
                continue;
            }

            int column = slotIndex % 2;
            int x = contentX + (column * (columnW + 8));
            int y = columnCursorY[column];
            int controlCount = filtered.size();
            int innerTop = y + 22;
            int boxHeight = 30 + controlCount * CONTROL_HEIGHT + Math.max(0, controlCount - 1) * CONTROL_SPACING;

            if (y + boxHeight > contentY + contentH) {
                continue;
            }

            sectionRenderBoxes
                    .add(new SectionRenderBox(x, y, columnW, boxHeight, section.title(), column,
                            getSectionKey(section)));

            int controlY = innerTop;
            for (MenuControl control : filtered) {
                int controlX = x + 8;
                int controlW = columnW - 16;
                addControlWidget(control, controlX, controlY, controlW, getSectionKey(section));
                controlY += CONTROL_HEIGHT + CONTROL_SPACING;
            }

            columnCursorY[column] += boxHeight + 8;
            slotIndex++;
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

    private void addControlWidget(MenuControl control, int x, int y, int width, @NonNull String sectionKey) {
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
                    null,
                    sectionKey));
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
                    null,
                    sectionKey));
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
                    null,
                    sectionKey));
            return;
        }

        if (control instanceof ColorPickerOption colorOption) {
            String hueControlKey = sectionKey + "::" + colorOption.label();
            BooleanSupplier rainbowGetter = colorOption.rainbowGetter() != null
                    ? colorOption.rainbowGetter()
                    : () -> persistedHuePickerRainbowByControlKey.getOrDefault(hueControlKey, false);
            Consumer<Boolean> rainbowSetter = colorOption.rainbowSetter() != null
                    ? colorOption.rainbowSetter()
                    : enabled -> persistedHuePickerRainbowByControlKey.put(hueControlKey, enabled);
            DoubleSupplier rainbowSpeedGetter = colorOption.rainbowSpeedGetter() != null
                    ? colorOption.rainbowSpeedGetter()
                    : () -> persistedHuePickerRainbowSpeedByControlKey.getOrDefault(hueControlKey, 0.22);
            DoubleConsumer rainbowSpeedSetter = colorOption.rainbowSpeedSetter() != null
                    ? colorOption.rainbowSpeedSetter()
                    : speed -> persistedHuePickerRainbowSpeedByControlKey.put(hueControlKey, speed);
            HueSlider hueWidget = new HueSlider(
                    x,
                    y,
                    width,
                    CONTROL_HEIGHT,
                    colorOption.getter(),
                    colorOption.setter(),
                    rainbowGetter,
                    rainbowSetter,
                    rainbowSpeedGetter,
                    rainbowSpeedSetter);
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
                    colorOption.getter(),
                    sectionKey));
            return;
        }

        if (control instanceof SeparatorOption separatorOption) {
            Button separatorWidget = Button.builder(Component.empty(), button -> {
            }).bounds(x, y, width, CONTROL_HEIGHT).build();
            separatorWidget.active = false;
            separatorWidget.setAlpha(0.0f);
            this.addRenderableWidget(separatorWidget);
            this.controlRenderBoxes.add(new ControlRenderBox(
                    ControlVisualType.SEPARATOR,
                    x,
                    y,
                    width,
                    CONTROL_HEIGHT,
                    separatorWidget,
                    separatorOption.label(),
                    null,
                    null,
                    null,
                    null,
                    sectionKey));
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

    private List<@NonNull MenuSection> getSectionsForSelectedSubTab(MenuTab tab) {
        List<@NonNull MenuSection> allSections = new ArrayList<>(Objects.requireNonNull(tab.sections()));
        if (selectedSubTabLabel.isEmpty()) {
            return allSections;
        }

        List<@NonNull MenuSection> filtered = new ArrayList<>();
        for (MenuSection section : allSections) {
            if (Objects.equals(section.subTab(), selectedSubTabLabel)) {
                filtered.add(section);
            }
        }
        return filtered;
    }

    private List<@NonNull String> getSubTabs(MenuTab tab) {
        List<@NonNull String> labels = new ArrayList<>();
        for (MenuSection section : tab.sections()) {
            String subTab = section.subTab();
            if (!labels.contains(subTab)) {
                labels.add(subTab);
            }
        }
        return labels;
    }

    private List<@NonNull MenuSection> orderSectionsForCurrentContext(List<@NonNull MenuSection> sections) {
        if (sections.size() <= 1) {
            return sections;
        }

        String contextKey = getSectionOrderContextKey();
        List<String> savedOrder = sectionOrderByContext.get(contextKey);
        if (savedOrder == null || savedOrder.isEmpty()) {
            return sections;
        }

        Map<String, Integer> indexByKey = new HashMap<>();
        for (int i = 0; i < savedOrder.size(); i++) {
            indexByKey.put(savedOrder.get(i), i);
        }

        List<@NonNull MenuSection> ordered = new ArrayList<>(sections);
        ordered.sort((left, right) -> {
            int leftOrder = indexByKey.getOrDefault(getSectionKey(left), Integer.MAX_VALUE);
            int rightOrder = indexByKey.getOrDefault(getSectionKey(right), Integer.MAX_VALUE);
            return Integer.compare(leftOrder, rightOrder);
        });
        return ordered;
    }

    private void swapSectionOrder(String sourceKey, String targetKey) {
        String contextKey = getSectionOrderContextKey();
        List<String> order = sectionOrderByContext.computeIfAbsent(contextKey, key -> getDefaultSectionOrder());

        int sourceIndex = order.indexOf(sourceKey);
        int targetIndex = order.indexOf(targetKey);
        if (sourceIndex < 0 || targetIndex < 0 || sourceIndex == targetIndex) {
            return;
        }

        String source = order.get(sourceIndex);
        order.set(sourceIndex, order.get(targetIndex));
        order.set(targetIndex, source);
    }

    private List<String> getDefaultSectionOrder() {
        if (tabs.isEmpty()) {
            return new ArrayList<>();
        }

        MenuTab tab = getTabAtClampedIndex(selectedTabIndex);
        List<@NonNull MenuSection> sections = getSectionsForSelectedSubTab(tab);
        List<String> keys = new ArrayList<>(sections.size());
        for (MenuSection section : sections) {
            keys.add(getSectionKey(section));
        }
        return keys;
    }

    private @NonNull String getSectionOrderContextKey() {
        if (tabs.isEmpty()) {
            return "";
        }

        MenuTab tab = getTabAtClampedIndex(selectedTabIndex);
        String subTab = selectedSubTabLabel;
        if (subTab.isEmpty()) {
            subTab = "*";
        }
        return tab.label() + "|" + subTab;
    }

    private static @NonNull String getSectionKey(@NonNull MenuSection section) {
        return section.title() + "|" + section.subTab() + "|" + section.column();
    }

    private @Nullable SectionRenderBox findSectionAt(double mouseX, double mouseY, boolean headerOnly) {
        for (SectionRenderBox box : sectionRenderBoxes) {
            if (mouseX < box.x() || mouseX > box.x() + box.width() || mouseY < box.y()) {
                continue;
            }

            int bottom = headerOnly ? box.y() + 20 : box.y() + box.height();
            if (mouseY <= bottom) {
                return box;
            }
        }
        return null;
    }

    private @Nullable SectionRenderBox findSectionByKey(String sectionKey) {
        for (SectionRenderBox box : sectionRenderBoxes) {
            if (Objects.equals(box.sectionKey(), sectionKey)) {
                return box;
            }
        }
        return null;
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

    private boolean isInResizeHandle(double mouseX, double mouseY) {
        int handleX = panelX + panelWidth - RESIZE_HANDLE_SIZE;
        int handleY = panelY + panelHeight - RESIZE_HANDLE_SIZE;
        return mouseX >= handleX
                && mouseX <= panelX + panelWidth
                && mouseY >= handleY
                && mouseY <= panelY + panelHeight;
    }

    private void clampPanelSize() {
        int maxWidth = Math.max(MIN_PANEL_WIDTH, this.width);
        int maxHeight = Math.max(MIN_PANEL_HEIGHT, this.height);
        panelWidth = Math.max(MIN_PANEL_WIDTH, Math.min(maxWidth, panelWidth));
        panelHeight = Math.max(MIN_PANEL_HEIGHT, Math.min(maxHeight, panelHeight));
    }

    private void clampPanelPosition() {
        int maxX = Math.max(0, this.width - panelWidth);
        int maxY = Math.max(0, this.height - panelHeight);
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

        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, PANEL_COLOR);
        context.fill(panelX, panelY, panelX + panelWidth, panelY + HEADER_HEIGHT, HEADER_COLOR);
        context.fill(panelX, panelY, panelX + panelWidth, panelY + 1, panelBorderColor);
        context.fill(panelX, panelY + panelHeight - 1, panelX + panelWidth, panelY + panelHeight,
                panelBorderColor);
        context.fill(panelX, panelY, panelX + 1, panelY + panelHeight, panelBorderColor);
        context.fill(panelX + panelWidth - 1, panelY, panelX + panelWidth, panelY + panelHeight,
                panelBorderColor);
        context.fill(panelX + 1, panelY + HEADER_HEIGHT, panelX + panelWidth - 1, panelY + HEADER_HEIGHT + 1,
                panelInnerBorderColor);

        context.drawString(this.font, Component.literal("✦"), panelX + 10, panelY + 11, panelBorderColor, false);
        context.drawString(this.font, Component.literal("starclient"), panelX + 24, panelY + 11, TITLE_COLOR, false);

        for (SectionRenderBox box : sectionRenderBoxes) {
            if (draggingSection && Objects.equals(draggingSectionKey, box.sectionKey())) {
                continue;
            }
            drawGroupBox(context, box.x(), box.y(), box.width(), box.height(), box.title());
        }

        drawCustomCloseButton(context);
        drawCustomSearchWidget(context);
        drawCustomTabWidgets(context);
        drawCustomSubTabWidgets(context);
        drawResizeGrip(context, mouseX, mouseY);

        super.render(context, mouseX, mouseY, delta);
        drawCustomControlWidgets(context, mouseX, mouseY);
        drawFloatingDraggedSection(context, mouseX, mouseY);
    }

    private void drawFloatingDraggedSection(@NonNull GuiGraphics context, int mouseX, int mouseY) {
        if (!draggingSection || draggingSectionKey == null) {
            return;
        }

        SectionRenderBox source = findSectionByKey(draggingSectionKey);
        if (source == null) {
            return;
        }

        int floatX = (int) Math.round(mouseX - dragSectionOffsetX);
        int floatY = (int) Math.round(mouseY - dragSectionOffsetY);

        drawGroupBox(context, floatX, floatY, source.width(), source.height(), source.title());

        int dx = floatX - source.x();
        int dy = floatY - source.y();
        for (ControlRenderBox box : controlRenderBoxes) {
            if (!Objects.equals(box.sectionKey(), source.sectionKey())) {
                continue;
            }

            ControlRenderBox floatingBox = new ControlRenderBox(
                    box.type(),
                    box.x() + dx,
                    box.y() + dy,
                    box.width(),
                    box.height(),
                    box.widget(),
                    box.label(),
                    box.toggleGetter(),
                    box.sliderTextGetter(),
                    box.sliderProgressGetter(),
                    box.hueGetter(),
                    box.sectionKey());

            boolean hovered = floatingBox.widget().isHoveredOrFocused();
            int background = hovered ? CONTROL_BG_HOVER_COLOR : CONTROL_BG_COLOR;

            int x = floatingBox.x();
            int y = floatingBox.y();
            int width = floatingBox.width();
            int height = floatingBox.height();

            if (floatingBox.type() == ControlVisualType.SEPARATOR) {
                drawSeparatorControl(context, floatingBox);
                continue;
            }

            context.fill(x, y, x + width, y + height, background);
            int borderColor = getControlBorderColor();
            context.fill(x, y, x + width, y + 1, borderColor);
            context.fill(x, y + height - 1, x + width, y + height, borderColor);
            context.fill(x, y, x + 1, y + height, borderColor);
            context.fill(x + width - 1, y, x + width, y + height, borderColor);

            switch (floatingBox.type()) {
                case TOGGLE -> drawToggleControl(context, floatingBox);
                case ACTION -> drawActionControl(context, floatingBox);
                case SLIDER -> drawSliderControl(context, floatingBox);
                case HUE_PICKER -> drawHuePickerControl(context, floatingBox);
                case SEPARATOR -> drawSeparatorControl(context, floatingBox);
            }
        }
    }

    private void drawResizeGrip(@NonNull GuiGraphics context, int mouseX, int mouseY) {
        int panelX = getPanelX();
        int panelY = getPanelY();

        int handleX = panelX + panelWidth - RESIZE_HANDLE_SIZE;
        int handleY = panelY + panelHeight - RESIZE_HANDLE_SIZE;
        boolean hovered = mouseX >= handleX && mouseX <= panelX + panelWidth
                && mouseY >= handleY && mouseY <= panelY + panelHeight;

        int lineColor = hovered ? getControlAccentColor() : getControlBorderColor();
        DynamicOptionPanelRenderHelper.drawResizeGrip(
                context,
                panelX,
                panelY,
                panelWidth,
                panelHeight,
                RESIZE_HANDLE_SIZE,
                lineColor,
                PANEL_COLOR);
    }

    private void drawCustomCloseButton(@NonNull GuiGraphics context) {
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

    private void drawCustomSearchWidget(@NonNull GuiGraphics context) {
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

    private void drawMagnifierIcon(@NonNull GuiGraphics context, int centerX, int centerY, int color) {
        DynamicOptionPanelRenderHelper.drawMagnifierIcon(context, centerX, centerY, color);
    }

    private void drawCustomTabWidgets(@NonNull GuiGraphics context) {
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

    private void drawCustomSubTabWidgets(@NonNull GuiGraphics context) {
        for (SubTabRenderBox subTab : subTabRenderBoxes) {
            boolean selected = Objects.equals(selectedSubTabLabel, subTab.label());
            boolean hovered = subTab.button().isHoveredOrFocused();
            int textColor = selected ? getTabActiveTextColor() : (hovered ? getTabHoverTextColor() : TAB_TEXT_COLOR);

            int textWidth = this.font.width(subTab.label());
            int textX = subTab.x() + (subTab.width() - textWidth) / 2;
            int textY = subTab.y() + 3;
            context.drawString(this.font, Component.literal(subTab.label()), textX, textY, textColor, false);

            int underlineY = subTab.y() + subTab.height() - 1;
            if (selected) {
                context.fill(subTab.x() + 4, underlineY, subTab.x() + subTab.width() - 4, underlineY + 1,
                        getControlAccentColor());
            } else if (hovered) {
                context.fill(subTab.x() + 6, underlineY, subTab.x() + subTab.width() - 6, underlineY + 1,
                        getControlBorderColor());
            }
        }
    }

    private void drawCustomControlWidgets(@NonNull GuiGraphics context, int mouseX, int mouseY) {
        for (ControlRenderBox box : controlRenderBoxes) {
            if (draggingSection && Objects.equals(draggingSectionKey, box.sectionKey())) {
                continue;
            }

            boolean hovered = box.widget().isHoveredOrFocused();
            int background = hovered ? CONTROL_BG_HOVER_COLOR : CONTROL_BG_COLOR;

            int x = box.x();
            int y = box.y();
            int width = box.width();
            int height = box.height();

            if (box.type() == ControlVisualType.SEPARATOR) {
                drawSeparatorControl(context, box);
                continue;
            }

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
                case SEPARATOR -> drawSeparatorControl(context, box);
            }
        }
    }

    private void drawSeparatorControl(@NonNull GuiGraphics context, ControlRenderBox box) {
        DynamicOptionPanelRenderHelper.drawSeparatorControl(
                context,
                this.font,
                box.label(),
                box.x(),
                box.y(),
                box.width(),
                getSubtitleColor(),
                getPanelInnerBorderColor());
    }

    private void drawHuePickerControl(@NonNull GuiGraphics context, ControlRenderBox box) {
        int x = box.x();
        int y = box.y();
        int width = box.width();
        DoubleSupplier hueGetter = box.hueGetter();
        double hueValue = hueGetter == null ? 0.0 : hueGetter.getAsDouble();
        DynamicOptionPanelRenderHelper.drawHuePickerControl(
                context,
                this.font,
                box.label(),
                x,
                y,
                width,
                hueValue,
                CONTROL_TEXT_COLOR,
                getControlBorderColor());
    }

    private void drawToggleControl(@NonNull GuiGraphics context, ControlRenderBox box) {
        int x = box.x();
        int y = box.y();
        int width = box.width();
        int height = box.height();

        Supplier<Boolean> toggleGetter = box.toggleGetter();
        boolean enabled = toggleGetter != null && toggleGetter.get();
        DynamicOptionPanelRenderHelper.drawToggleControl(
                context,
                this.font,
                box.label(),
                x,
                y,
                width,
                height,
                enabled,
                CONTROL_TEXT_COLOR,
                GROUP_COLOR,
                getControlBorderColor(),
                getControlAccentColor());
    }

    private void drawActionControl(@NonNull GuiGraphics context, ControlRenderBox box) {
        int x = box.x();
        int y = box.y();
        int width = box.width();

        DynamicOptionPanelRenderHelper.drawActionControl(
                context,
                this.font,
                box.label(),
                x,
                y,
                width,
                CONTROL_TEXT_COLOR,
                getControlValueColor());
    }

    private void drawSliderControl(@NonNull GuiGraphics context, ControlRenderBox box) {
        int x = box.x();
        int y = box.y();
        int width = box.width();

        Supplier<String> sliderTextGetter = box.sliderTextGetter();
        String valueTextRaw = sliderTextGetter == null ? null : sliderTextGetter.get();
        String valueText = valueTextRaw == null ? "" : valueTextRaw;

        DoubleSupplier sliderProgressGetter = box.sliderProgressGetter();
        double progress = sliderProgressGetter == null ? 0.0 : sliderProgressGetter.getAsDouble();

        DynamicOptionPanelRenderHelper.drawSliderControl(
                context,
                this.font,
                box.label(),
                valueText,
                x,
                y,
                width,
                progress,
                CONTROL_TEXT_COLOR,
                getControlValueColor(),
                getControlBorderColor(),
                getControlAccentColor());
    }

    private void drawGroupBox(@NonNull GuiGraphics context, int x, int y, int width, int height,
            @NonNull String title) {
        DynamicOptionPanelRenderHelper.drawGroupBox(
                context,
                this.font,
                x,
                y,
                width,
                height,
                title,
                GROUP_COLOR,
                getPanelInnerBorderColor(),
                getSubtitleColor());
    }

    private record SectionRenderBox(int x, int y, int width, int height, @NonNull String title, int column,
            @NonNull String sectionKey) {
    }

    private enum ControlVisualType {
        TOGGLE,
        ACTION,
        SLIDER,
        HUE_PICKER,
        SEPARATOR
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
            @Nullable DoubleSupplier hueGetter,
            @NonNull String sectionKey) {
    }

    private record TabRenderBox(int x, int y, int width, int height, @NonNull Button button, @NonNull String label,
            int index) {
    }

    private record SubTabRenderBox(int x, int y, int width, int height, @NonNull Button button,
            @NonNull String label) {
    }

    private record SearchRenderBox(int x, int y, int width, int height, int iconLaneWidth) {
    }
}
