package uk.co.joshepstein.minecrafttrophies.mixin.accessor;

import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Slot.class)
public interface AccessorSlot {
	@Accessor(value = "x")
	@Mutable
	@Final
	void setX(int x);

	@Accessor(value = "y")
	@Mutable
	@Final
	void setY(int y);
}
