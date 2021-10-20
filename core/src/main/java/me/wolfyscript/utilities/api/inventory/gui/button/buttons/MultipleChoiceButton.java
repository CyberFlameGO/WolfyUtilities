/*
 *       WolfyUtilities, APIs and Utilities for Minecraft Spigot plugins
 *                      Copyright (C) 2021  WolfyScript
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.wolfyscript.utilities.api.inventory.gui.button.buttons;

import me.wolfyscript.utilities.api.inventory.gui.GuiCluster;
import me.wolfyscript.utilities.api.inventory.gui.GuiHandler;
import me.wolfyscript.utilities.api.inventory.gui.GuiWindow;
import me.wolfyscript.utilities.api.inventory.gui.button.Button;
import me.wolfyscript.utilities.api.inventory.gui.button.ButtonState;
import me.wolfyscript.utilities.api.inventory.gui.button.ButtonType;
import me.wolfyscript.utilities.api.inventory.gui.cache.CustomCache;
import me.wolfyscript.utilities.api.nms.inventory.GUIInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * This Button goes through each of the States.
 * Each click the index increases by 1 and it goes to the next State.
 * After the index reached the size of the States it is reset to 0 and the first state is selected.
 *
 * @param <C> The type of the {@link CustomCache}
 */
public class MultipleChoiceButton<C extends CustomCache> extends Button<C> {

    private final List<ButtonState<C>> states;
    private final StateFunction<C> stateFunction;
    private final HashMap<GuiHandler<C>, Integer> settings;

    /**
     * @param id            The id of the Button
     * @param stateFunction The {@link StateFunction} to set the state of the Button depending on the player, cached data, etc.
     * @param states        The {@link ButtonState}s that this Button will cycle through.
     */
    @SafeVarargs
    public MultipleChoiceButton(String id, StateFunction<C> stateFunction, @NotNull ButtonState<C>... states) {
        super(id, ButtonType.CHOICES);
        this.states = Arrays.asList(states);
        settings = new HashMap<>();
        this.stateFunction = stateFunction == null ? (cache, guiHandler, player, inventory, slot) -> settings.getOrDefault(guiHandler, 0) : stateFunction;
    }

    /**
     * @param id     The id of the Button
     * @param states The {@link ButtonState}s that this Button will cycle through.
     */
    @SafeVarargs
    public MultipleChoiceButton(String id, @NotNull ButtonState<C>... states) {
        this(id, null, states);
    }

    @Override
    public void init(GuiWindow<C> guiWindow) {
        for (ButtonState<C> btnState : states) {
            btnState.init(guiWindow);
        }
    }

    @Override
    public void init(GuiCluster<C> guiCluster) {
        for (ButtonState<C> btnState : states) {
            btnState.init(guiCluster);
        }
    }

    @Override
    public boolean execute(GuiHandler<C> guiHandler, Player player, GUIInventory<C> inventory, int slot, InventoryInteractEvent event) throws IOException {
        int setting = settings.getOrDefault(guiHandler, 0);
        if (states != null && setting < states.size()) {
            ButtonState<C> btnState = states.get(setting);
            setting++;
            if (setting >= states.size()) {
                settings.put(guiHandler, 0);
            } else {
                settings.put(guiHandler, setting);
            }
            return btnState.getAction().execute(guiHandler.getCustomCache(), guiHandler, player, inventory, this, slot, event);
        }
        return true;
    }

    @Override
    public void postExecute(GuiHandler<C> guiHandler, Player player, GUIInventory<C> inventory, ItemStack itemStack, int slot, InventoryInteractEvent event) throws IOException {
        int setting = settings.computeIfAbsent(guiHandler, g -> 0);
        if (states != null && setting < states.size()) {
            ButtonState<C> btnState = states.get(setting);
            if (btnState.getPostAction() != null) {
                btnState.getPostAction().run(guiHandler.getCustomCache(), guiHandler, player, inventory, itemStack, slot, event);
            }
        }
    }

    @Override
    public void preRender(GuiHandler<C> guiHandler, Player player, GUIInventory<C> inventory, ItemStack itemStack, int slot, boolean help) {
        int setting = stateFunction.run(guiHandler.getCustomCache(), guiHandler, player, inventory, slot);
        if (states != null && states.size() > setting && states.get(setting).getPrepareRender() != null) {
            states.get(setting).getPrepareRender().prepare(guiHandler.getCustomCache(), guiHandler, player, inventory, itemStack, slot, help);
        }
    }

    @Override
    public void render(GuiHandler<C> guiHandler, Player player, GUIInventory<C> guiInventory, Inventory inventory, ItemStack itemStack, int slot, boolean help) {
        int setting = settings.computeIfAbsent(guiHandler, g -> 0);
        if (states != null && states.size() > setting) {
            applyItem(guiHandler, player, guiInventory, inventory, states.get(setting), slot, help);
        }
    }

    public void setState(GuiHandler<C> guiHandler, int state) {
        this.settings.put(guiHandler, state);
    }

    public interface StateFunction<C extends CustomCache> {

        /**
         * Used to set the state for the {@link MultipleChoiceButton} depending on data from the cache or player, etc.
         *
         * @param cache      The current cache of the GuiHandler
         * @param guiHandler The current GuiHandler.
         * @param player     The current Player.
         * @param inventory  The original/previous inventory. No changes to this inventory will be applied on render!
         * @param slot       The slot in which the button is rendered.
         * @return a int indicating the state of the button.
         */
        int run(C cache, GuiHandler<C> guiHandler, Player player, GUIInventory<C> inventory, int slot);

    }
}
