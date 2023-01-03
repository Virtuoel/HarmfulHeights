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
	public static final ScaleModifier HARM_MODIFIER = ScaleRegistries.register(
		ScaleRegistries.SCALE_MODIFIERS,
		HarmfulHeights.id("harm_multiplier"),
		new TypedScaleModifier(() -> ScaleTypeRegistrar.HARM_TYPE)
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
	
	public static final ScaleType HARM_TYPE = registerHarmType();
	
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
				.addDependentModifier(HARM_MODIFIER)
				.build()
		);
		
		ScaleTypes.BASE.getDefaultBaseValueModifiers().add(HARM_MODIFIER);
		
		return type;
	}
	
	public static final ScaleTypeRegistrar INSTANCE = new ScaleTypeRegistrar();
	
	private ScaleTypeRegistrar()
	{
		
	}
}
