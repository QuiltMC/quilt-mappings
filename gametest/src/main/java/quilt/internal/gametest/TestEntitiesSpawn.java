package quilt.internal.gametest;

import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.test.AbstractTestFunction;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TestEntitiesSpawn extends AbstractTestFunction {
    public static final String PATH = "entities_spawn";

    public static void spawnEachEntity(TestContext context) {
        Registries.ENTITY_TYPE.stream()
            .filter(type -> type != EntityType.PLAYER)
            .forEach(type -> context.spawnEntity(type, BlockPos.ORIGIN).kill(context.getWorld()));

        context.createMockPlayer(GameMode.CREATIVE).kill(context.getWorld());

        context.complete();
    }

    @Override
    public void register(BiConsumer<RegistryKey<Consumer<TestContext>>, Consumer<TestContext>> registrar) {
        final RegistryKey<Consumer<TestContext>> key =
            RegistryKey.of(RegistryKeys.TEST_FUNCTION, Identifier.of("quilt", PATH));

        registrar.accept(key, TestEntitiesSpawn::spawnEachEntity);
    }
}
