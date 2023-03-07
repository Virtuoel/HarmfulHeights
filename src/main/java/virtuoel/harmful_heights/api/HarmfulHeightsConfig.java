package virtuoel.harmful_heights.api;

import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus;

import virtuoel.harmful_heights.HarmfulHeights;
import virtuoel.kanos_config.api.JsonConfigBuilder;

public class HarmfulHeightsConfig
{
	@ApiStatus.Internal
	public static final JsonConfigBuilder BUILDER = new JsonConfigBuilder(
		HarmfulHeights.MOD_ID,
		"config.json"
	);
	
	public static final Client CLIENT = new Client(BUILDER);
	public static final Common COMMON = new Common(BUILDER);
	public static final Server SERVER = new Server(BUILDER);
	
	public static final class Client
	{
		private Client(final JsonConfigBuilder builder)
		{
			
		}
	}
	
	public static final class Common
	{
		public final Supplier<Boolean> damageGrowsPlayer;
		public final Supplier<Double> startingScale;
		public final Supplier<Double> growthIncrement;
		public final Supplier<Double> maxScale;
		public final Supplier<Boolean> growthBreaksSurroundings;
		public final Supplier<Boolean> maxScaleDamageBreaksSurroundings;
		public final Supplier<Boolean> damageToolForEveryBlockBroken;
		
		private Common(final JsonConfigBuilder builder)
		{
			this.damageGrowsPlayer = builder.booleanConfig("damageGrowsPlayer", true);
			this.startingScale = builder.doubleConfig("startingScale", 0.1D);
			this.growthIncrement = builder.doubleConfig("growthIncrement", 0.1D);
			this.maxScale = builder.doubleConfig("maxScale", 8.0D);
			this.growthBreaksSurroundings = builder.booleanConfig("growthBreaksSurroundings", true);
			this.maxScaleDamageBreaksSurroundings = builder.booleanConfig("maxScaleDamageBreaksSurroundings", true);
			this.damageToolForEveryBlockBroken = builder.booleanConfig("damageToolForEveryBlockBroken", false);
		}
	}
	
	public static final class Server
	{
		private Server(final JsonConfigBuilder builder)
		{
			
		}
	}
	
	private HarmfulHeightsConfig()
	{
		
	}
}
