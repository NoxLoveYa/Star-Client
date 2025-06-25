package com.starclient.utils;

import com.starclient.classs.Actions;
import com.starclient.classs.Keybinds;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class KeybindsHandler {
    private static final List<Actions> registeredActions = new ArrayList<Actions>();

    public static void registerAction(Keybinds key, Runnable callback) {
        registeredActions.add(new Actions(key, callback));
    }

    public static void registerAction(int keyCode, Runnable callback) {
        registeredActions.add(new Actions(new Keybinds(keyCode, GLFW.GLFW_PRESS), callback));
    }

    public static void registerAction(int keyCode, int mode, Runnable callback) {
        registeredActions.add(new Actions(new Keybinds(keyCode, mode), callback));
    }

    public static void registerAction(int keyCode, String action, Runnable callback) {
        registeredActions.add(new Actions(new Keybinds(keyCode, GLFW.GLFW_PRESS, action), callback));
    }

    public static void registerAction(int keyCode, int mode, String action, Runnable callback) {
        registeredActions.add(new Actions(new Keybinds(keyCode, mode, action), callback));
    }

    public static void checkForAction(int keyCode, int mode) {
        for (Actions action : registeredActions) {
            if (action.keybind.key == keyCode && action.keybind.mode == mode) {
                action.callback.run();
            }
        }
    }
}
