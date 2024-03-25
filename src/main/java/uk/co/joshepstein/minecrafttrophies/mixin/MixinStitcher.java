package uk.co.joshepstein.minecrafttrophies.mixin;

import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(value = Stitcher.class)
public class MixinStitcher {

	@Shadow private int storageX;

	@Shadow private int storageY;

	@Shadow @Final private int maxWidth;

	@Shadow @Final private int maxHeight;

	@Shadow @Final private List<Stitcher.Region> storage;

	@Redirect(method = {"addToStorage"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/Stitcher;expand(Lnet/minecraft/client/renderer/texture/Stitcher$Holder;)Z"))
	public boolean expand(Stitcher stitcher, Stitcher.Holder holder) {
		Stitcher.Region region;
		boolean expandHorizontal;
		int storageWidth = Mth.smallestEncompassingPowerOfTwo(this.storageX);
		int storageHeight = Mth.smallestEncompassingPowerOfTwo(this.storageY);
		int storageWidthExpanded = Mth.smallestEncompassingPowerOfTwo(this.storageX + holder.width());
		int storageHeightExpanded = Mth.smallestEncompassingPowerOfTwo(this.storageY + holder.height());
		boolean canExpandWidth = storageWidthExpanded <= this.maxWidth;
		boolean canExpandHeight = storageHeightExpanded <= this.maxHeight;
		if (!canExpandHeight && !canExpandWidth) {
			return false;
		}
		boolean needsWidthExpansion = canExpandWidth && storageWidth != storageWidthExpanded;
		boolean needsHeightExpansion = canExpandHeight && storageHeight != storageHeightExpanded;
		if (needsWidthExpansion ^ needsHeightExpansion) {
			expandHorizontal = !needsWidthExpansion && canExpandWidth;
		} else {
			expandHorizontal = canExpandWidth && storageWidth <= storageHeight;
		}
		if (expandHorizontal) {
			this.storageY = Math.max(this.storageY, holder.height());
			region = new Stitcher.Region(this.storageX, 0, holder.width(), this.storageY);
			this.storageX += holder.width();
		} else {
			this.storageX = Math.max(this.storageX, holder.width());
			region = new Stitcher.Region(0, this.storageY, this.storageX, holder.height());
			this.storageY += holder.height();
		}
		region.add(holder);
		this.storage.add(region);
		return true;
	}
}
