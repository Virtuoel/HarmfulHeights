package virtuoel.harmful_heights.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import virtuoel.harmful_heights.HarmfulHeights;

@Mixin(value = ServerPlayerInteractionManager.class, priority = 1002)
public class ServerPlayerInteractionManagerMixin
{
	@Shadow public ServerWorld world;
	@Shadow public ServerPlayerEntity player;
	
	@Inject(method = "tryBreakBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onBreak(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/player/PlayerEntity;)V"), cancellable = true)
	private void harmful_heights$tryBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> info)
	{
		HarmfulHeights.handleScaledBreaking(world, player, pos, info);
	}
}
