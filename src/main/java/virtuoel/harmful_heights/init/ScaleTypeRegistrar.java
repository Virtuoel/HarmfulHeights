package virtuoel.harmful_heights.init;

import net.minecraft.entity.player.PlayerEntity;
import virtuoel.harmful_heights.HarmfulHeights;
import virtuoel.harmful_heights.api.HarmfulHeightsConfig;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleModifier;
import virtuoel.pehkui.api.ScaleRegistries;
import virtuoel.pehkui.api.ScaleType;
import virtuoel.pehkui.api.ScaleTypes;
import virtuoel.pehkui.api.TypedScaleModifier;

public class ScaleTypeRegistrar
{
	public static final ScaleModifier HARM_MULTIPLIER = ScaleRegistries.register(
		ScaleRegistries.SCALE_MODIFIERS,
		HarmfulHeights.id("harm_multiplier"),
		new TypedScaleModifier(() -> ScaleTypeRegistrar.HARM)
		{
			@Override
			public float modifyScale(ScaleData scaleData, float modifiedScale, float delta)
			{
				return scaleData.getEntity() instanceof PlayerEntity ? super.modifyScale(scaleData, modifiedScale, delta) : modifiedScale;
			}
			
			@Override
			public float modifyPrevScale(ScaleData scaleData, float modifiedScale)
			{
				return scaleData.getEntity() instanceof PlayerEntity ? super.modifyPrevScale(scaleData, modifiedScale) : modifiedScale;
			}
		}
	);
	
	public static final ScaleType BREAKING = ScaleRegistries.register(
		ScaleRegistries.SCALE_TYPES,
		HarmfulHeights.id("breaking"),
		ScaleType.Builder.create().build()
	);
	public static final ScaleType HARM = registerHarmType();
	
	private static ScaleType registerHarmType()
	{
		final ScaleType type = ScaleRegistries.register(
			ScaleRegistries.SCALE_TYPES,
			HarmfulHeights.id("harm"),
			ScaleType.Builder.create()
				.defaultTickDelay(0)
				.defaultBaseScale(HarmfulHeightsConfig.COMMON.startingScale.get().floatValue())
				.affectsDimensions()
				.defaultPersistence(true)
				.addDependentModifier(HARM_MULTIPLIER)
				.build()
		);
		
		ScaleTypes.BASE.getDefaultBaseValueModifiers().add(HARM_MULTIPLIER);
		ScaleTypeRegistrar.BREAKING.getDefaultBaseValueModifiers().add(HARM_MULTIPLIER);
		
		return type;
	}
	
	public static final ScaleTypeRegistrar INSTANCE = new ScaleTypeRegistrar();
	
	private ScaleTypeRegistrar()
	{
		
	}
}
