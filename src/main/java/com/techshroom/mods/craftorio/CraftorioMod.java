/*
 * This file is part of Craftorio, licensed under the MIT License (MIT).
 *
 * Copyright (c) TechShroom Studios <https://techshoom.com>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.techshroom.mods.craftorio;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.annotations.VisibleForTesting;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Constants.MOD_ID, version = Constants.VERSION,
        name = Constants.NAME, acceptedMinecraftVersions = "@MC_VERSION@")
public final class CraftorioMod {

    @VisibleForTesting
    public static final String SERVER_SIDE_PROXY =
            "com.techshroom.mods.craftorio.CraftorioProxy";
    @VisibleForTesting
    public static final String CLIENT_SIDE_PROXY =
            SERVER_SIDE_PROXY + "$Client";
    @Instance
    private static CraftorioMod instance;
    private static final Logger logger = LogManager.getLogger(Constants.MOD_ID);
    @SidedProxy(modId = Constants.MOD_ID, clientSide = CLIENT_SIDE_PROXY,
            serverSide = SERVER_SIDE_PROXY)
    private static CraftorioProxy proxy;

    public static CraftorioMod getInstance() {
        return instance;
    }

    public static Logger getLogger() {
        return logger;
    }

    public static CraftorioProxy getProxy() {
        return proxy;
    }

    @EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        getProxy().onPreInit();
    }

    @EventHandler
    public void onInit(FMLInitializationEvent event) {
        getProxy().onInit();
    }

}
