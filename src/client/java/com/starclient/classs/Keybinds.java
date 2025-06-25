package com.starclient.classs;


public class Keybinds {
    public int key;
    public int mode;
    public String action;

    public Keybinds(int key, int mode) {
        this.key = key;
        this.mode = mode;
        this.action = "unset";
    }

    public Keybinds(int key, int mode, String action) {
        this.key = key;
        this.mode = mode;
        this.action = action;
    }
}
