package com.starclient.classs;

public class Actions {
    public final Keybinds keybind;
    public final Runnable callback;

    public Actions(Keybinds key, Runnable callback) {
        this.keybind = key;
        this.callback = callback;
    }
}
