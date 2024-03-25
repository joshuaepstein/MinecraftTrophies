package uk.co.joshepstein.minecrafttrophies.blocks.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import joshuaepstein.advancementtrophies.blocks.entity.TrophyBlockEntity;
import joshuaepstein.advancementtrophies.init.ModConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Objects;

public class TrophyRenderer implements BlockEntityRenderer<TrophyBlockEntity> {
	private final ItemRenderer itemRenderer;

	public TrophyRenderer(BlockEntityRendererProvider.Context context) {
		Minecraft minecraft = Minecraft.getInstance();
		this.itemRenderer = minecraft.getItemRenderer();
	}

	@Override
	public void render(TrophyBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack,
					   MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
		Property<net.minecraft.core.Direction> facing = HorizontalDirectionalBlock.FACING;
		net.minecraft.core.Direction direction = pBlockEntity.getBlockState().getValue(facing);
		ItemStack itemStack = new ItemStack(pBlockEntity.getData().getItem());

		pPoseStack.pushPose();
		pPoseStack.translate(0.5f, 1.1f, 0.5f);
		pPoseStack.scale(0.35f, 0.35f, 0.35f);
		pPoseStack.mulPose(Vector3f.YN.rotationDegrees(0));
		// Rotate the item based on the direction the trophy is facing
		switch (direction) {
			case NORTH -> pPoseStack.mulPose(Vector3f.YN.rotationDegrees(0));
			case EAST -> pPoseStack.mulPose(Vector3f.YN.rotationDegrees(90));
			case SOUTH -> pPoseStack.mulPose(Vector3f.YN.rotationDegrees(180));
			case WEST -> pPoseStack.mulPose(Vector3f.YN.rotationDegrees(270));
		}

		this.itemRenderer.renderStatic(itemStack, ItemTransforms.TransformType.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBufferSource, 0);
		pPoseStack.popPose();


		if (Minecraft.getInstance().hitResult != null && Minecraft.getInstance().hitResult.getType() == HitResult.Type.BLOCK && ModConfigs.TROPHY_CONFIG.getShowName()) {
			BlockHitResult hitresult = (BlockHitResult) Minecraft.getInstance().hitResult;
			if (hitresult.getBlockPos().equals(pBlockEntity.getBlockPos())) {
				Font fontRenderer = Minecraft.getInstance().font;
				Quaternion cameraRotation = Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation();
				Component displayNameIn = new TextComponent(pBlockEntity.getData() == null || Objects.equals(pBlockEntity.getData().getAdvName(), "") ? "Blank Name" : pBlockEntity.getData().getAdvName());
				pPoseStack.pushPose();
				pPoseStack.translate(0.5F, 1.6F, 0.5F);
				pPoseStack.mulPose(cameraRotation);
				pPoseStack.scale(-0.013F, -0.013F, -0.013F);
				Matrix4f matrix4f = pPoseStack.last().pose();
				float backgroundOpacity = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
				int alpha = (int) (backgroundOpacity * 255.0F) << 24;
				float textOffset = -fontRenderer.width(displayNameIn) / 2;
				fontRenderer.drawInBatch(displayNameIn, textOffset, 0F, 553648127, false, matrix4f, pBufferSource, true, alpha, pPackedLight);
				fontRenderer.drawInBatch(displayNameIn, textOffset, 0F, -1, false, matrix4f, pBufferSource, false, 0, pPackedLight);
				Component description = new TextComponent(pBlockEntity.getData() == null || Objects.equals(pBlockEntity.getData().getGivenPlayerName(), "") ? "" : "Given To: " + pBlockEntity.getData().getGivenPlayerName());
				pPoseStack.translate(0F, 10F, 0F);
				textOffset = -fontRenderer.width(description) / 2;
				fontRenderer.drawInBatch(description, textOffset, 0F, 553648127, false, matrix4f, pBufferSource, true, alpha, pPackedLight);
				fontRenderer.drawInBatch(description, textOffset, 0F, -1, false, matrix4f, pBufferSource, false, 0, pPackedLight);
				pPoseStack.popPose();
			}
		}
	}
}