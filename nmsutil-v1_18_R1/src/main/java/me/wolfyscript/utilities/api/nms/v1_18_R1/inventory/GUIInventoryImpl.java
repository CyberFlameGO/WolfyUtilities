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

package me.wolfyscript.utilities.api.nms.v1_18_R1.inventory;

import me.wolfyscript.utilities.api.inventory.gui.GuiHandler;
import me.wolfyscript.utilities.api.inventory.gui.GuiWindow;
import me.wolfyscript.utilities.api.inventory.gui.cache.CustomCache;
import me.wolfyscript.utilities.api.nms.inventory.GUIInventory;
import net.minecraft.world.Container;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftInventory;

public class GUIInventoryImpl<C extends CustomCache> extends CraftInventory implements GUIInventory<C> {

    private final GuiWindow<C> window;
    private final GuiHandler<C> guiHandler;

    public GUIInventoryImpl(GuiHandler<C> guiHandler, GuiWindow<C> window, Container inventory) {
        super(inventory);
        this.guiHandler = guiHandler;
        this.window = window;
    }

    @Override
    public GuiWindow<C> getWindow() {
        return window;
    }

    @Override
    public GuiHandler<C> getGuiHandler() {
        return guiHandler;
    }
}
