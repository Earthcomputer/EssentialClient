package essentialclient.gui.keybinds;

import essentialclient.feature.clientmacro.ClientMacro;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public enum ClientKeybinds {
    DEBUG_MENU (new KeyBinding("Toggle Debug Menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F3, "Essential Client")),
    CLIENT_MACRO (new KeyBinding("Client Macro", GLFW.GLFW_KEY_UNKNOWN, "Essential Client"));

    private final KeyBinding key;

    ClientKeybinds(KeyBinding keyBinding) {
        this.key = keyBinding;
    }

    public int getKeyCode() {
        return Math.abs(KeyBindingHelper.getBoundKeyOf(this.key).getCode());
    }

    public KeyBinding getKeyBinding() {
        return this.key;
    }

    public static void loadKeybinds() {
        for (ClientKeybinds clientKeybinds : ClientKeybinds.values())
            KeyBindingHelper.registerKeyBinding(clientKeybinds.key);
        ClientMacro.registerKeyPress();
    }
}
