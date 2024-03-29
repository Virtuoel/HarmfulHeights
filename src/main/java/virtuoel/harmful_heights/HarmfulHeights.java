package virtuoel.harmful_heights;

import java.util.List;

import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.service.MixinService;

import draylar.magna.Magna;
import draylar.magna.api.BlockBreaker;
import draylar.magna.api.BlockFinder;
import draylar.magna.api.BlockProcessor;
import draylar.magna.api.BreakValidator;
import draylar.magna.api.MagnaTool;
import draylar.magna.api.event.ToolRadiusCallback;
import draylar.magna.impl.MagnaPlayerInteractionManagerExtension;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import virtuoel.harmful_heights.api.HarmfulHeightsConfig;
import virtuoel.harmful_heights.init.ScaleTypeRegistrar;
import virtuoel.pehkui.api.ScaleData;

public class HarmfulHeights implements ModInitializer
{
	public static final String MOD_ID = "harmful_heights";
	
	public static final ILogger LOGGER = MixinService.getService().getLogger(MOD_ID);
	
	public HarmfulHeights()
	{
		HarmfulHeightsConfig.BUILDER.config.get();
	}
	
	@Override
	public void onInitialize()
	{
		ScaleTypeRegistrar.INSTANCE.getClass();
		
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) ->
		{
			if (entity instanceof PlayerEntity player)
			{
				final ScaleData data = ScaleTypeRegistrar.HARM.getScaleData(entity);
				
				if (data.getBaseScale() != 1.0F)
				{
					player.calculateDimensions();
				}
			}
		});
		
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) ->
		{
			if (entity instanceof PlayerEntity player && player.hurtTime == 0 && HarmfulHeightsConfig.COMMON.damageGrowsPlayer.get().booleanValue())
			{
				final ScaleData data = ScaleTypeRegistrar.HARM.getScaleData(entity);
				final float scale = data.getBaseScale();
				final float maxScale = HarmfulHeightsConfig.COMMON.maxScale.get().floatValue();
				final float increment = HarmfulHeightsConfig.COMMON.growthIncrement.get().floatValue();
				
				boolean changed = false;
				if (scale < maxScale)
				{
					data.setTargetScale(Math.min(maxScale, scale + increment));
					changed = true;
				}
				
				if (changed || HarmfulHeightsConfig.COMMON.maxScaleDamageBreaksSurroundings.get().booleanValue())
				{
					if (HarmfulHeightsConfig.COMMON.growthBreaksSurroundings.get().booleanValue())
					{
						final Box box = player.getDimensions(EntityPose.STANDING).getBoxAt(player.getPos());
						final BlockPos start = new BlockPos(MathHelper.floor(box.minX + 1.0E-7), MathHelper.floor(box.minY + 1.0E-7), MathHelper.floor(box.minZ + 1.0E-7));
						final BlockPos end = new BlockPos(MathHelper.floor(box.maxX - 1.0E-7), MathHelper.floor(box.maxY - 1.0E-7), MathHelper.floor(box.maxZ - 1.0E-7));
						final World world = player.getEntityWorld();
						
						if (world.isRegionLoaded(start, end))
						{
							BlockPos.stream(start, end).forEach(pos ->
							{
								final BlockState blockState = world.getBlockState(pos);
								
								if (blockState.getHardness(world, pos) != -1.0F &&
									VoxelShapes.matchesAnywhere(
										blockState.getCollisionShape(world, pos).offset(
											(double) pos.getX(),
											(double) pos.getY(),
											(double) pos.getZ()
										),
										VoxelShapes.cuboid(box),
										BooleanBiFunction.AND
									))
								{
									world.breakBlock(pos, true, player);
								}
							});
						}
					}
				}
			}
			
			return true;
		});
	}
	
	public static void handleScaledBreaking(ServerWorld world, ServerPlayerEntity player, BlockPos pos, CallbackInfoReturnable<Boolean> info)
	{
		if (((MagnaPlayerInteractionManagerExtension) player.interactionManager).magna_isMining())
		{
			info.setReturnValue(true);
			return;
		}
		
		if (player.isSneaking())
		{
			return;
		}
		
		final ScaleData data = ScaleTypeRegistrar.BREAKING.getScaleData(player);
		final float scale = data.getScale();
		
		if (scale > 1.0F)
		{
			final ItemStack stack = player.getMainHandStack();
			
			final int breakRadius, depth;
			final BlockFinder finder;
			final BlockProcessor processor;
			final BreakValidator validator;
			boolean hasToolRadius = false;
			
			if (stack.getItem() instanceof MagnaTool tool && (hasToolRadius = !tool.ignoreRadiusBreak(stack, player)))
			{
				breakRadius = tool.getRadius(stack);
				depth = tool.getDepth(stack);
				finder = tool.getBlockFinder();
				processor = tool.getProcessor(world, player, pos, stack);
				validator = (view, breakPos) -> tool.isBlockValidForBreaking(view, breakPos, stack);
			}
			else
			{
				breakRadius = 0;
				depth = 0;
				finder = BlockFinder.DEFAULT;
				processor = (tool, input) -> input;
				validator = (view, breakPos) ->
				{
					final BlockState s = view.getBlockState(breakPos);
					
					if (s.getHardness(view, breakPos) == -1.0)
					{
						return false;
					}
					
					if (stack.isSuitableFor(s))
					{
						return true;
					}
					
					if (!s.isToolRequired())
					{
						return true;
					}
					
					return stack.getMiningSpeedMultiplier(s) > 1.0F;
				};
			}
			
			final BlockState state = world.getBlockState(pos);
			
			if (state.getHardness(world, pos) > 0 && player.canHarvest(state))
			{
				final int radius = !hasToolRadius ? 0 : ToolRadiusCallback.EVENT.invoker().getRadius(stack, breakRadius);
				
				final int diameter = 1 + (2 * radius);
				final float scaledDiameter = diameter * scale;
				final int scaledRadius = Math.round((scaledDiameter - 1.0F) / 2.0F);
				
				final int scaledDepth = Math.round((depth + 1) * scale) - 1;
				
				if (scaledRadius != 0 || scaledDepth != 0)
				{
					final BlockFinder sortedFinder = new BlockFinder()
					{
						@Override
						public List<BlockPos> findPositions(World world, PlayerEntity playerEntity, int radius, int depth)
						{
							return finder.findPositions(world, playerEntity, radius, depth)
								.stream()
								.sorted((l, r) -> Double.compare(l.getSquaredDistance(pos), r.getSquaredDistance(pos)))
								.toList();
						}
					};
					
					final boolean damageToolPerBlock = hasToolRadius || HarmfulHeightsConfig.COMMON.damageToolForEveryBlockBroken.get().booleanValue();
					
					final boolean lastValue = Magna.CONFIG.autoPickup;
					Magna.CONFIG.autoPickup = true;
					BlockBreaker.breakInRadius(world, player, scaledRadius, scaledDepth, sortedFinder, validator, processor, damageToolPerBlock);
					Magna.CONFIG.autoPickup = lastValue;
					
					if (!damageToolPerBlock)
					{
						stack.postMine(world, state, pos, player);
						player.incrementStat(Stats.MINED.getOrCreateStat(state.getBlock()));
						player.addExhaustion(0.005F);
					}
					
					info.setReturnValue(true);
				}
			}
		}
	}
	
	public static Identifier id(String path)
	{
		return new Identifier(MOD_ID, path);
	}
	
	public static Identifier id(String path, String... paths)
	{
		return id(paths.length == 0 ? path : path + "/" + String.join("/", paths));
	}
}
