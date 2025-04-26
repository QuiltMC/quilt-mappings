package quilt.internal.gametest.mixin;

import quilt.internal.gametest.TestEntitiesSpawn;

import net.minecraft.registry.Registry;
import net.minecraft.test.TestContext;
import net.minecraft.unmapped.C_pxdgchfu;
import net.minecraft.unmapped.C_zmxlezwg;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

// C_zmxlezwg is TestFunction
@Mixin(C_zmxlezwg.class)
class TestFunctionMixin {
    // method_66917 is bootstrap
    @Inject(method = "method_66917", at = @At(
        value = "INVOKE",
        // C_zmxlezwg::method_67077 is AbstractTestFunction::bootstrap
        target = "Lnet/minecraft/unmapped/C_zmxlezwg;method_67077(Lnet/minecraft/registry/Registry;)V"
    ))
    private static void boostrapFunctions(
        Registry<Consumer<TestContext>> registry, CallbackInfoReturnable<Consumer<TestContext>> cir
    ) {
        // C_pxdgchfu::method_67076 is AbstractTestFunction::register
        C_pxdgchfu.method_67076(new TestEntitiesSpawn());
    }
}
