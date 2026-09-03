package dev.harekuto.motifx.forge;

import com.mojang.brigadier.Command;
import dev.harekuto.motifx.MotifX;
import dev.harekuto.motifx.runtime.MotifRuntime;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MotifX.MOD_ID)
public final class ForgeEvents {
    private ForgeEvents() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("motifx")
            .then(Commands.literal("status").executes(context -> {
                var source = context.getSource();
                source.sendSuccess(() -> Component.literal("MotifX " + MotifX.VERSION + " | features=" + MotifRuntime.INSTANCE.features().size() + " | loadedMods=" + ModList.get().getMods().size()), false);
                return Command.SINGLE_SUCCESS;
            }))
            .then(Commands.literal("capabilities").executes(context -> {
                context.getSource().sendSuccess(() -> Component.literal("MotifX capabilities: " + String.join(", ", MotifRuntime.INSTANCE.features())), false);
                return Command.SINGLE_SUCCESS;
            }))
            .then(Commands.literal("selftest").executes(context -> {
                MotifRuntime.SelfTestResult result = MotifRuntime.INSTANCE.selfTest();
                context.getSource().sendSuccess(() -> Component.literal((result.passed() ? "MotifX self-test PASS: " : "MotifX self-test FAIL: ") + result.detail()), false);
                return result.passed() ? Command.SINGLE_SUCCESS : 0;
            }))
        );
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        MotifX.LOGGER.info("MotifX dedicated/common runtime ready; {} features enabled", MotifRuntime.INSTANCE.features().size());
    }
}
