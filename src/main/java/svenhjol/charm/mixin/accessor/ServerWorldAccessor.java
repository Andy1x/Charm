package svenhjol.charm.mixin.accessor;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.gen.Spawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import svenhjol.charm.annotation.CharmMixin;

import java.util.List;

@Mixin(ServerWorld.class)
@CharmMixin(required = true)
public interface ServerWorldAccessor {
    @Accessor
    List<Spawner> getSpawners();

    @Invoker
    void callWakeSleepingPlayers();

    @Invoker
    void callResetWeather();
}
