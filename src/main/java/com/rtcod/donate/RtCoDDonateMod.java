package com.rtcod.donate;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkConstants;

@Mod(RtCoDDonateMod.MODID)
public class RtCoDDonateMod {
    public static final String MODID = "rtcod_donate";

    public RtCoDDonateMod() {
        // Позволяет клиентам заходить на сервер без установки этого мода
        ModLoadingContext.get().registerExtensionPoint(
                IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(
                        () -> NetworkConstants.IGNORESERVERONLY,
                        (remoteVersion, isFromServer) -> true
                )
        );
        MinecraftForge.EVENT_BUS.register(DonateEvents.class);
    }
}