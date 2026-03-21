package com.starclient.screen.gui;

import org.jspecify.annotations.NonNull;

public sealed interface MenuControl permits ToggleOption, SliderOption, ActionOption, ColorPickerOption,
        SeparatorOption {
    @NonNull
    String label();
}
