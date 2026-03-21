package com.starclient.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
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
    private static final int PANEL_BORDER_COLOR = new Color(96, 58, 155, 255).getRGB();
    private static final int PANEL_INNER_BORDER_COLOR = new Color(52, 38, 74, 220).getRGB();
    private static final int HEADER_COLOR = new Color(16, 16, 22, 245).getRGB();
    private static final int GROUP_COLOR = new Color(14, 14, 20, 228).getRGB();
    private static final int TITLE_COLOR = new Color(240, 238, 255, 255).getRGB();
    private static final int SUBTITLE_COLOR = new Color(178, 151, 230, 255).getRGB();
    private static final int CONTROL_BG_COLOR = new Color(20, 20, 28, 230).getRGB();
    private static final int CONTROL_BG_HOVER_COLOR = new Color(28, 28, 38, 238).getRGB();
    private static final int CONTROL_BORDER_COLOR = new Color(66, 52, 92, 255).getRGB();
    private static final int CONTROL_ACCENT_COLOR = new Color(128, 88, 210, 255).getRGB();
    private static final int CONTROL_TEXT_COLOR = new Color(232, 228, 246, 255).getRGB();
    private static final int CONTROL_VALUE_COLOR = new Color(164, 142, 214, 255).getRGB();

    private static final int PANEL_WIDTH = 560;
    private static final int PANEL_HEIGHT = 360;
    private static final int HEADER_HEIGHT = 30;

    private static final int CONTROL_HEIGHT = 20;
    private static final int CONTROL_SPACING = 4;

    @Nullable
    private final Screen previousScreen;
    private final ShootingStarsRenderer shootingStarsRenderer = new ShootingStarsRenderer();
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

        int panelX = getPanelX();
        int panelY = getPanelY();

        addHeaderWidgets(panelX, panelY);
        addTabButtons(panelX, panelY);
        addDynamicSectionWidgets(panelX, panelY);
    }

    private void addHeaderWidgets(int panelX, int panelY) {
        Button closeButton = Button.builder(Component.literal("x"), button -> onClose())
                .bounds(panelX + PANEL_WIDTH - 24, panelY + 6, 16, 16)
                .build();
        this.addRenderableWidget(closeButton);

        EditBox box = new EditBox(this.font, panelX + PANEL_WIDTH - 170, panelY + 7, 130, 16,
                Component.literal("search"));
        box.setValue(Objects.requireNonNull(searchText));
        box.setSuggestion("search");
        box.setBordered(true);
        box.setResponder(value -> {
            searchText = value;
            rebuildMenuWidgets();
        });
        this.addRenderableWidget(box);
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
            this.addRenderableWidget(tabButton);
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
                    }));
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
                    null));
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

    @Override
    public void render(@NonNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, BACKGROUND_COLOR);

        applyBackgroundEffects();
        shootingStarsRenderer.render(context, this.width, this.height);

        int panelX = getPanelX();
        int panelY = getPanelY();

        context.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, PANEL_COLOR);
        context.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + HEADER_HEIGHT, HEADER_COLOR);
        context.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + 1, PANEL_BORDER_COLOR);
        context.fill(panelX, panelY + PANEL_HEIGHT - 1, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT,
                PANEL_BORDER_COLOR);
        context.fill(panelX, panelY, panelX + 1, panelY + PANEL_HEIGHT, PANEL_BORDER_COLOR);
        context.fill(panelX + PANEL_WIDTH - 1, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT,
                PANEL_BORDER_COLOR);
        context.fill(panelX + 1, panelY + HEADER_HEIGHT, panelX + PANEL_WIDTH - 1, panelY + HEADER_HEIGHT + 1,
                PANEL_INNER_BORDER_COLOR);

        context.drawString(this.font, Component.literal("✦"), panelX + 10, panelY + 10, PANEL_BORDER_COLOR, false);
        context.drawString(this.font, Component.literal("starclient"), panelX + 24, panelY + 10, TITLE_COLOR, false);

        for (SectionRenderBox box : sectionRenderBoxes) {
            drawGroupBox(context, box.x(), box.y(), box.width(), box.height(), box.title());
        }

        super.render(context, mouseX, mouseY, delta);
        drawCustomControlWidgets(context, mouseX, mouseY);
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
            context.fill(x, y, x + width, y + 1, CONTROL_BORDER_COLOR);
            context.fill(x, y + height - 1, x + width, y + height, CONTROL_BORDER_COLOR);
            context.fill(x, y, x + 1, y + height, CONTROL_BORDER_COLOR);
            context.fill(x + width - 1, y, x + width, y + height, CONTROL_BORDER_COLOR);

            switch (box.type()) {
                case TOGGLE -> drawToggleControl(context, box);
                case ACTION -> drawActionControl(context, box);
                case SLIDER -> drawSliderControl(context, box);
            }
        }
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
        context.fill(toggleX, toggleY, toggleX + toggleSize, toggleY + 1, CONTROL_BORDER_COLOR);
        context.fill(toggleX, toggleY + toggleSize - 1, toggleX + toggleSize, toggleY + toggleSize,
                CONTROL_BORDER_COLOR);
        context.fill(toggleX, toggleY, toggleX + 1, toggleY + toggleSize, CONTROL_BORDER_COLOR);
        context.fill(toggleX + toggleSize - 1, toggleY, toggleX + toggleSize, toggleY + toggleSize,
                CONTROL_BORDER_COLOR);

        boolean enabled = box.toggleGetter() != null && box.toggleGetter().get();
        if (enabled) {
            context.fill(toggleX + 2, toggleY + 2, toggleX + toggleSize - 2, toggleY + toggleSize - 2,
                    CONTROL_ACCENT_COLOR);
        }
    }

    private void drawActionControl(GuiGraphics context, ControlRenderBox box) {
        int x = box.x();
        int y = box.y();
        int width = box.width();

        context.drawString(this.font, Component.literal(Objects.requireNonNull(box.label())), x + 6, y + 6,
                CONTROL_TEXT_COLOR, false);
        context.drawString(this.font, Component.literal("+"), x + width - 10, y + 6, CONTROL_VALUE_COLOR, false);
    }

    private void drawSliderControl(GuiGraphics context, ControlRenderBox box) {
        int x = box.x();
        int y = box.y();
        int width = box.width();

        context.drawString(this.font, Component.literal(Objects.requireNonNull(box.label())), x + 6, y + 3,
                CONTROL_TEXT_COLOR, false);

        String valueText = box.sliderTextGetter() == null ? "" : box.sliderTextGetter().get();
        int valueTextWidth = this.font.width(valueText);
        context.drawString(this.font, Component.literal(valueText), x + width - valueTextWidth - 6, y + 3,
                CONTROL_VALUE_COLOR, false);

        int trackX = x + 6;
        int trackY = y + 14;
        int trackWidth = width - 12;
        context.fill(trackX, trackY, trackX + trackWidth, trackY + 2, CONTROL_BORDER_COLOR);

        double progress = box.sliderProgressGetter() == null ? 0.0 : box.sliderProgressGetter().getAsDouble();
        int fillWidth = (int) Math.round(trackWidth * Math.max(0.0, Math.min(1.0, progress)));
        context.fill(trackX, trackY, trackX + fillWidth, trackY + 2, CONTROL_ACCENT_COLOR);
    }

    private void drawGroupBox(GuiGraphics context, int x, int y, int width, int height, String title) {
        context.fill(x, y, x + width, y + height, GROUP_COLOR);
        context.fill(x, y, x + width, y + 1, PANEL_INNER_BORDER_COLOR);
        context.fill(x, y + height - 1, x + width, y + height, PANEL_INNER_BORDER_COLOR);
        context.fill(x, y, x + 1, y + height, PANEL_INNER_BORDER_COLOR);
        context.fill(x + width - 1, y, x + width, y + height, PANEL_INNER_BORDER_COLOR);
        context.drawString(this.font, Component.literal(Objects.requireNonNull(title)), x + 8, y + 7, SUBTITLE_COLOR,
                false);
    }

    public record MenuTab(@NonNull String label, List<@NonNull MenuSection> sections) {
    }

    public record MenuSection(@NonNull String title, int column, List<@NonNull MenuControl> controls) {
    }

    public sealed interface MenuControl permits ToggleOption, SliderOption, ActionOption {
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

    private record SectionRenderBox(int x, int y, int width, int height, String title) {
    }

    private enum ControlVisualType {
        TOGGLE,
        ACTION,
        SLIDER
    }

    private record ControlRenderBox(
            ControlVisualType type,
            int x,
            int y,
            int width,
            int height,
            AbstractWidget widget,
            String label,
            @Nullable Supplier<Boolean> toggleGetter,
            @Nullable Supplier<String> sliderTextGetter,
            @Nullable DoubleSupplier sliderProgressGetter) {
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
}
