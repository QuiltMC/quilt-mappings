package quilt.internal.gametest.mixin;

import net.minecraft.resource.pack.PackManager;
import net.minecraft.server.Main;
import net.minecraft.server.MinecraftServer;
import net.minecraft.test.TestServer;
import net.minecraft.world.storage.WorldSaveStorage;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(Main.class)
class MainMixin {
    @ModifyExpressionValue(
        method = "main",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/dedicated/EulaReader;isEulaAgreedTo()Z")
    )
    private static boolean alwaysAgree(boolean isEulaAgreedTo) {
        return true;
    }

    @ModifyExpressionValue(
        method = "main",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/resource/pack/VanillaDataPackProvider;createPackManager" +
                "(Lnet/minecraft/world/storage/WorldSaveStorage$Session;)Lnet/minecraft/resource/pack/PackManager;"
        )
    )
    private static PackManager startTestServer(PackManager packManager, @Local WorldSaveStorage.Session session) {
        MinecraftServer.startServer((thread) -> TestServer.create(
            thread, session, packManager, Optional.empty(), false
        ));

        return packManager;
    }

    @Inject(
        method = "main", cancellable = true,
        at = @At(
            value = "INVOKE", shift = At.Shift.AFTER, by = 1,
            target = "Lnet/minecraft/resource/pack/VanillaDataPackProvider;createPackManager" +
                "(Lnet/minecraft/world/storage/WorldSaveStorage$Session;)Lnet/minecraft/resource/pack/PackManager;"
        )
    )
    private static void cancelNormalServer(String[] strings, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(
        method = "main", remap = false,
        slice = @Slice(from = @At(
            value = "FIELD", remap = false,
            target = "Lcom/mojang/logging/LogUtils;FATAL_MARKER:Lorg/slf4j/Marker;"
        )),
        at = @At(
            value = "INVOKE", remap = false, shift = At.Shift.AFTER,
            target = "Lorg/slf4j/Logger;error(Lorg/slf4j/Marker;Ljava/lang/String;Ljava/lang/Throwable;)V"
        )
    )
    private static void exit(CallbackInfo info) {
        System.exit(-1);
    }
}
