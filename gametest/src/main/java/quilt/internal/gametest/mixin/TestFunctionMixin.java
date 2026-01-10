package quilt.internal.gametest.mixin;

import quilt.internal.gametest.TestEntitiesSpawn;

import net.minecraft.registry.Registry;
import net.minecraft.test.AbstractTestFunction;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestFunction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

@Mixin(TestFunction.class)
class TestFunctionMixin {
    @Inject(method = "bootstrap", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/test/TestFunction;registerFunctions(Lnet/minecraft/registry/Registry;)V"
    ))
    private static void boostrapFunctions(
        Registry<Consumer<TestContext>> registry, CallbackInfoReturnable<Consumer<TestContext>> cir
    ) {
        AbstractTestFunction.register(new TestEntitiesSpawn());
    }
}
