package dev.harekuto.motifx;

import com.mojang.logging.LogUtils;
import dev.harekuto.motifx.api.animation.Pose;
import dev.harekuto.motifx.api.graph.ParameterStore;
import dev.harekuto.motifx.api.graph.StateGraphPlayer;
import dev.harekuto.motifx.internal.DemoDefinitions;
import dev.harekuto.motifx.internal.MotifRuntimeMetrics;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(MotifX.MOD_ID)
public final class MotifX {
    public static final String MOD_ID = "motifx";
    public static final String VERSION = "0.1.0";
    private static final Logger LOGGER = LogUtils.getLogger();

    public MotifX() {
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
        LOGGER.info("MotifX {} initialized: Forge 1.20.1 runtime core active", VERSION);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("motifx")
                .then(Commands.literal("status").executes(context -> {
                    MotifRuntimeMetrics.Snapshot metrics = MotifRuntimeMetrics.snapshot();
                    context.getSource().sendSuccess(() -> Component.literal(
                            "MotifX " + VERSION + " | evaluations=" + metrics.evaluations()
                                    + " | avg=" + String.format(java.util.Locale.ROOT, "%.3f", metrics.averageEvaluationMicros()) + " us"), false);
                    return 1;
                }))
                .then(Commands.literal("demo").executes(context -> {
                    DemoDefinitions.DemoBundle demo = DemoDefinitions.create();
                    ParameterStore parameters = new ParameterStore(demo.layout());
                    StateGraphPlayer player = new StateGraphPlayer(demo.graph(), parameters, demo.skeleton());
                    Pose pose = new Pose(demo.skeleton());
                    parameters.set(demo.wave(), true);
                    long start = System.nanoTime();
                    for (int i = 0; i < 20; i++) player.update(0.05f);
                    player.sample(pose);
                    MotifRuntimeMetrics.recordEvaluation(System.nanoTime() - start);
                    MotifRuntimeMetrics.recordTransition();
                    var armRotation = pose.get(demo.armBone()).rotation();
                    context.getSource().sendSuccess(() -> Component.literal(
                            "MotifX demo state=" + player.currentStateName() + " armQuat=["
                                    + armRotation.x() + ", " + armRotation.y() + ", " + armRotation.z() + ", " + armRotation.w() + "]"), false);
                    return 1;
                })));
    }
}
