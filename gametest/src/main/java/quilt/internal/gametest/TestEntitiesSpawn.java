package quilt.internal.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

public class TestEntitiesSpawn {
    @GameTest
    public void spawnEachEntity(TestContext context) {
        Registries.ENTITY_TYPE.stream()
            .filter(type -> type != EntityType.PLAYER)
            .forEach(type -> context.spawnEntity(type, BlockPos.ORIGIN).kill(context.getWorld()));

        context.createMockPlayer(GameMode.CREATIVE).kill(context.getWorld());

        context.complete();
    }
}
