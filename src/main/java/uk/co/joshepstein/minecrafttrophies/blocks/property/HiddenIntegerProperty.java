package uk.co.joshepstein.minecrafttrophies.blocks.property;

import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class HiddenIntegerProperty extends IntegerProperty {
	protected HiddenIntegerProperty(String name, int min, int max) {
		super(name, min, max);
	}

	public static HiddenIntegerProperty create(String name, int min, int max) {
		return new HiddenIntegerProperty(name, min, max);
	}
}
