package uk.co.joshepstein.minecrafttrophies.blocks;

import joshuaepstein.advancementtrophies.Main;
import joshuaepstein.advancementtrophies.blocks.entity.TrophyBlockEntity;
import joshuaepstein.advancementtrophies.blocks.property.HiddenIntegerProperty;
import joshuaepstein.advancementtrophies.init.ModBlockEntities;
import joshuaepstein.advancementtrophies.init.ModBlocks;
import joshuaepstein.advancementtrophies.init.ModConfigs;
import joshuaepstein.advancementtrophies.util.TrophyHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.text.DateFormat;
import java.util.*;

public class TrophyBlock extends HorizontalDirectionalBlock implements EntityBlock {

	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final HiddenIntegerProperty TROPHY_TYPE = HiddenIntegerProperty.create("trophy_type", 1, 5);

	public TrophyBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(TROPHY_TYPE, 3));
	}

	@Override
	public PushReaction getPistonPushReaction(BlockState pState) {
		return PushReaction.DESTROY;
	}

	@Override
	public List<ItemStack> getDrops(BlockState pState, LootContext.Builder pBuilder) {
		return Collections.emptyList();
	}

	@Override
	public void fillItemCategory(CreativeModeTab pTab, NonNullList<ItemStack> pItems) {
		// Fill for the 5 different types of trophies
		for (int i = 1; i <= 5; i++) {
			ItemStack stack = new ItemStack(this.asItem());
			CompoundTag tag = new CompoundTag();
			CompoundTag bet = new CompoundTag();
			tag.putInt(Main.sId("trophy_type"), i - 1);
			TrophyData data = new TrophyData();
			data.setType(TrophyData.Type.fromOrdinal(i - 1).getRegistryName());
			data.setGivenPlayerName("Creative Menu");
			data.setAdvName("Creative Menu");
			data.setItemResourceLocation(new ResourceLocation("minecraft", "command_block"));
			data.setDate(new Date());
			data.setUUID(UUID.randomUUID());
			bet.put("TrophyData", data.serializeNBT());
			tag.put("BlockEntityTag", bet);
			stack.setTag(tag);
			pItems.add(stack);
		}
	}

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
		TrophyBlockEntity te = (TrophyBlockEntity) level.getBlockEntity(pos);
		ItemStack stack = new ItemStack(this.asItem());
		CompoundTag tag = new CompoundTag();
		tag.putInt(Main.sId("trophy_type"), state.getValue(TROPHY_TYPE));
		CompoundTag blockentity = new CompoundTag();
		blockentity.put("TrophyData", te.getData().serializeNBT());
		tag.put("BlockEntityTag", blockentity);
		stack.setTag(tag);
		stack.setHoverName(new TextComponent(te.getData().getAdvName() + " Trophy").withStyle(Style.EMPTY.withColor(TrophyData.Type.fromRegistryName(te.getData().type).getColor()).withItalic(false)));
		return stack;
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
		super.setPlacedBy(level, pos, state, entity, stack);
		CompoundTag tag = stack.getOrCreateTag();
		BlockEntity entity1 = level.getBlockEntity(pos);
		if (entity1 instanceof TrophyBlockEntity blockentity) {
			if (tag.contains("BlockEntityTag")) {
				TrophyData data = new TrophyData(tag.getCompound("BlockEntityTag"));
				blockentity.load(tag.getCompound("BlockEntityTag"));
			}
			blockentity.setChanged();
		}
	}

	@Override
	public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
		return pLevel.getBlockState(pPos.above()).isAir() && !pLevel.getBlockState(pPos.east()).getMaterial().isLiquid() && !pLevel.getBlockState(pPos.west()).getMaterial().isLiquid() && !pLevel.getBlockState(pPos.north()).getMaterial().isLiquid() && !pLevel.getBlockState(pPos.south()).getMaterial().isLiquid();
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_49915_) {
		p_49915_.add(FACING).add(TROPHY_TYPE);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
		return ModBlockEntities.TROPHY.get().create(p_153215_, p_153216_);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
		switch (state.getValue(FACING)) {
			case NORTH -> {
				VoxelShape shape = Shapes.empty();
				shape = Shapes.join(shape, Shapes.box(0.3125, 0, 0.3125, 0.6875, 0.125, 0.6875), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.4375, 0.125, 0.4375, 0.5625, 0.3125, 0.5625), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.375, 0.3125, 0.375, 0.625, 0.375, 0.625), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.0625, 0.5, 0.4375, 0.25, 0.625, 0.5625), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.0625, 0.625, 0.4375, 0.1875, 0.75, 0.5625), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.0625, 0.75, 0.4375, 0.25, 0.875, 0.5625), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.8125, 0.625, 0.4375, 0.9375, 0.75, 0.5625), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.75, 0.5, 0.4375, 0.9375, 0.625, 0.5625), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.75, 0.75, 0.4375, 0.9375, 0.875, 0.5625), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.25, 0.375, 0.25, 0.75, 0.875, 0.375), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.25, 0.375, 0.625, 0.75, 0.875, 0.75), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.25, 0.375, 0.375, 0.375, 0.875, 0.625), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.625, 0.375, 0.375, 0.75, 0.875, 0.625), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.375, 0.375, 0.375, 0.625, 0.625, 0.625), BooleanOp.OR);
				return shape;
			}
			case SOUTH -> {
				VoxelShape shape = Shapes.empty();
				shape = Shapes.join(shape, Shapes.box(0.3125, 0, 0.3125, 0.6875, 0.125, 0.6875), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.4375, 0.125, 0.4375, 0.5625, 0.3125, 0.5625), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.375, 0.3125, 0.375, 0.625, 0.375, 0.625), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.75, 0.5, 0.4375, 0.9375, 0.625, 0.5625), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.8125, 0.625, 0.4375, 0.9375, 0.75, 0.5625), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.75, 0.75, 0.4375, 0.9375, 0.875, 0.5625), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.0625, 0.625, 0.4375, 0.1875, 0.75, 0.5625), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.0625, 0.5, 0.4375, 0.25, 0.625, 0.5625), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.0625, 0.75, 0.4375, 0.25, 0.875, 0.5625), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.25, 0.375, 0.625, 0.75, 0.875, 0.75), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.25, 0.375, 0.25, 0.75, 0.875, 0.375), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.625, 0.375, 0.375, 0.75, 0.875, 0.625), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.25, 0.375, 0.375, 0.375, 0.875, 0.625), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.375, 0.375, 0.375, 0.625, 0.625, 0.625), BooleanOp.OR);

				return shape;
			}
			case WEST -> {
				VoxelShape shape = Shapes.empty();
				shape = Shapes.join(shape, Shapes.box(0.3125, 0, 0.3125, 0.6875, 0.125, 0.6875), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.4375, 0.125, 0.4375, 0.5625, 0.3125, 0.5625), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.375, 0.3125, 0.375, 0.625, 0.375, 0.625), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.4375, 0.5, 0.75, 0.5625, 0.625, 0.9375), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.4375, 0.625, 0.8125, 0.5625, 0.75, 0.9375), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.4375, 0.75, 0.75, 0.5625, 0.875, 0.9375), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.4375, 0.625, 0.0625, 0.5625, 0.75, 0.1875), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.4375, 0.5, 0.0625, 0.5625, 0.625, 0.25), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.4375, 0.75, 0.0625, 0.5625, 0.875, 0.25), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.25, 0.375, 0.25, 0.375, 0.875, 0.75), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.625, 0.375, 0.25, 0.75, 0.875, 0.75), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.375, 0.375, 0.625, 0.625, 0.875, 0.75), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.375, 0.375, 0.25, 0.625, 0.875, 0.375), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.375, 0.375, 0.375, 0.625, 0.625, 0.625), BooleanOp.OR);

				return shape;
			}
			case EAST -> {
				VoxelShape shape = Shapes.empty();
				shape = Shapes.join(shape, Shapes.box(0.3125, 0, 0.3125, 0.6875, 0.125, 0.6875), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.4375, 0.125, 0.4375, 0.5625, 0.3125, 0.5625), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.375, 0.3125, 0.375, 0.625, 0.375, 0.625), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.4375, 0.5, 0.0625, 0.5625, 0.625, 0.25), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.4375, 0.625, 0.0625, 0.5625, 0.75, 0.1875), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.4375, 0.75, 0.0625, 0.5625, 0.875, 0.25), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.4375, 0.625, 0.8125, 0.5625, 0.75, 0.9375), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.4375, 0.5, 0.75, 0.5625, 0.625, 0.9375), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.4375, 0.75, 0.75, 0.5625, 0.875, 0.9375), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.625, 0.375, 0.25, 0.75, 0.875, 0.75), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.25, 0.375, 0.25, 0.375, 0.875, 0.75), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.375, 0.375, 0.25, 0.625, 0.875, 0.375), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.375, 0.375, 0.625, 0.625, 0.875, 0.75), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0.375, 0.375, 0.375, 0.625, 0.625, 0.625), BooleanOp.OR);

				return shape;
			}
			default -> {
				return Shapes.block();
			}
		}
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		ItemStack stack = blockPlaceContext.getItemInHand();
		CompoundTag tag = stack.getOrCreateTag();
		TrophyData data = new TrophyData(tag);
		return this.defaultBlockState().setValue(FACING, blockPlaceContext.getHorizontalDirection().getOpposite()).setValue(TROPHY_TYPE, TrophyData.Type.fromRegistryName(data.getType()).ordinal() + 1);
//        return this.defaultBlockState().setValue(FACING, blockPlaceContext.getHorizontalDirection().getOpposite()).setValue(TROPHY_TYPE, 2);
	}

	@Override
	public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos blockPos, Player player, boolean willHarvest, FluidState fluid) {
		BlockEntity blockEntity = level.getBlockEntity(blockPos);
		if (blockEntity instanceof TrophyBlockEntity trophyBlockEntity) {
			ItemStack stack = new ItemStack(this);
			CompoundTag tag = new CompoundTag();
			CompoundTag bet = new CompoundTag();
			tag.put("TrophyData", trophyBlockEntity.getData().serializeNBT());
			tag.putInt(Main.sId("trophy_type"), TrophyData.Type.fromRegistryName(trophyBlockEntity.getData().getType()).ordinal() + 1);
			bet.putInt(Main.sId("trophy_type"), TrophyData.Type.fromRegistryName(trophyBlockEntity.getData().getType()).ordinal() + 1);
			bet.put("BlockEntityTag", tag);
			stack.setTag(bet);
			stack.setHoverName(new TextComponent(trophyBlockEntity.getData().getAdvName() + " Trophy").withStyle(Style.EMPTY.withColor(TrophyData.Type.fromRegistryName(trophyBlockEntity.getData().type).getColor()).withItalic(false)));
//            level.addFreshEntity(new ItemEntity(level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), stack));
			popResource(level, blockPos, stack);
		}
		return super.onDestroyedByPlayer(state, level, blockPos, player, willHarvest, fluid);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, Random rand) {
		super.animateTick(state, level, pos, rand);
		if (!ModConfigs.TROPHY_CONFIG.getParticles().getEnabled()) return;
		BlockEntity blockEntity = level.getBlockEntity(pos);
		ChatFormatting color = ChatFormatting.WHITE;
		TrophyData.Type type = null;
		if (blockEntity instanceof TrophyBlockEntity trophyBlockEntity) {
			TrophyData data = trophyBlockEntity.getData();
			type = TrophyData.Type.fromRegistryName(data.getType());
			if (type == TrophyData.Type.NETHERITE) {
				color = ChatFormatting.RESET;
			} else {
				color = type.getColor();
			}
		}
		int[] RGB = new int[3];
		if (color == ChatFormatting.RESET) {
			RGB[0] = type.getDecimalColor() >> 16 & 0xFF;
			RGB[1] = type.getDecimalColor() >> 8 & 0xFF;
			RGB[2] = type.getDecimalColor() & 0xFF;
		} else {
			RGB[0] = color.getColor() >> 16 & 0xFF;
			RGB[1] = color.getColor() >> 8 & 0xFF;
			RGB[2] = color.getColor() & 0xFF;
		}
		if (Minecraft.getInstance().options.particles.equals(ParticleStatus.ALL)) {
			if (rand.nextInt(3) == 0) {
				ParticleEngine mgr = Minecraft.getInstance().particleEngine;
				double x = pos.getX() + rand.nextDouble();
				double y = pos.getY() + rand.nextDouble();
				double z = pos.getZ() + rand.nextDouble();
				while (state.getShape(level, pos).bounds().contains(x, y, z)) {
					x = pos.getX() + rand.nextDouble();
					y = pos.getY() + rand.nextDouble();
					z = pos.getZ() + rand.nextDouble();
				}
				SimpleAnimatedParticle p = (SimpleAnimatedParticle) mgr.createParticle(ParticleTypes.FIREWORK, x, y, z, 0.0, 0.0, 0.0);
				if (p != null) {
					p.gravity = 0F;
					p.setLifetime(ModConfigs.TROPHY_CONFIG.getParticles().getLifetime());
					p.setBoundingBox(new AABB(x, y, z, x + 0.1, y + 0.1, z + 0.1));
					if (randomBoolean(rand, 30) && ModConfigs.TROPHY_CONFIG.getParticles().getColourFromTrophyColor()) {
						int rgb = RGB[0] + rand.nextInt(1);
						rgb = (rgb << 8) + (RGB[1] + rand.nextInt(1));
						rgb = (rgb << 8) + (RGB[2] + rand.nextInt(1));
						p.setColor(rgb);
					} else {
						p.setColor(0xFFFFFF);
					}
				}
			}
		} else if (Minecraft.getInstance().options.particles.equals(ParticleStatus.DECREASED)) {
			if (rand.nextInt(6) == 2) {
				ParticleEngine mgr = Minecraft.getInstance().particleEngine;
				double x = pos.getX() + rand.nextDouble();
				double y = pos.getY() + rand.nextDouble();
				double z = pos.getZ() + rand.nextDouble();
				while (state.getShape(level, pos).bounds().contains(x, y, z)) {
					x = pos.getX() + rand.nextDouble();
					y = pos.getY() + rand.nextDouble();
					z = pos.getZ() + rand.nextDouble();
				}
				SimpleAnimatedParticle p = (SimpleAnimatedParticle) mgr.createParticle(ParticleTypes.FIREWORK, x, y, z, 0.0, 0.0, 0.0);
				if (p != null) {
					p.gravity = 0F;
					p.setLifetime(ModConfigs.TROPHY_CONFIG.getParticles().getLifetime());
					p.setBoundingBox(new AABB(x, y, z, x + 0.1, y + 0.1, z + 0.1));
					if (randomBoolean(rand, 30) && ModConfigs.TROPHY_CONFIG.getParticles().getColourFromTrophyColor()) {
						int rgb = RGB[0] + rand.nextInt(1);
						rgb = (rgb << 8) + (RGB[1] + rand.nextInt(1));
						rgb = (rgb << 8) + (RGB[2] + rand.nextInt(1));
						p.setColor(rgb);
					} else {
						p.setColor(0xFFFFFF);
					}
				}
			}
		}
	}

	public static double randomDouble(Random rand, double min, double max) {
		return rand.nextDouble() * (max - min) + min;
	}

	public static boolean randomBoolean(Random rand, int x) {
		return rand.nextInt(100) < x;
	}


	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter getter, List<Component> tooltip, TooltipFlag flag) {
//        Get BlockEntityTag data
		if (stack.getOrCreateTag().contains("BlockEntityTag") && stack.getOrCreateTag().getCompound("BlockEntityTag").contains("TrophyData")) {
			CompoundTag tag = stack.getOrCreateTag().getCompound("BlockEntityTag");
			TrophyData data = new TrophyData(tag);
			data.addInformation(tooltip, flag, stack);
		} else {
			if (Screen.hasShiftDown()) {
				tooltip.add(new TextComponent(ChatFormatting.DARK_RED + "No data found!"));
			} else {
				tooltip.add(new TextComponent(ChatFormatting.DARK_GRAY + "Hold " + ChatFormatting.WHITE + "<SHIFT>" + ChatFormatting.DARK_GRAY + " for more information"));
			}
		}
		super.appendHoverText(stack, getter, tooltip, flag);
	}

	public static class TrophyData implements INBTSerializable<CompoundTag> {
		public static final TrophyData EMPTY = new EmptyTrophyData();

		private CompoundTag delegate = new CompoundTag();

		protected UUID uuid;
		protected String advName;
		protected ResourceLocation itemResourceLocation;
		protected String givenPlayerName;
		protected Date date;
		protected ResourceLocation type;

		public TrophyData(CompoundTag delegate) {
			this.delegate = delegate;
			if (this.delegate.contains("BlockEntityTag")) {
				deserializeNBT(this.delegate.getCompound("BlockEntityTag").getCompound("TrophyData"));
			} else {
				deserializeNBT(this.delegate.getCompound("TrophyData"));
			}
		}

		public TrophyData(ItemStack delegate) {
			if (delegate != null) {
				this.delegate = delegate.getOrCreateTagElement("BlockEntityTag");
				deserializeNBT(this.delegate.getCompound("TrophyData"));
			}
		}

		public CompoundTag getDelegate() {
			return this.delegate;
		}

		public void updateDelegate() {
			if (this.delegate != null) {
				CompoundTag tag = new CompoundTag();
				tag.put("TrophyData", serializeNBT());
				this.delegate.put("BlockEntityTag", tag);
			}
		}

		public UUID getUUID() {
			return uuid;
		}

		public TrophyData setUUID(UUID uuid) {
			this.uuid = uuid;
			return this;
		}

		public String getAdvName() {
			return advName;
		}

		public TrophyData setAdvName(String advName) {
			this.advName = advName;
			return this;
		}

		public ResourceLocation getItemResourceLocation() {
			return itemResourceLocation;
		}

		public TrophyData setItemResourceLocation(ResourceLocation itemResourceLocation) {
			this.itemResourceLocation = itemResourceLocation;
			return this;
		}

		public Item getItem() {
			return ForgeRegistries.ITEMS.getValue(itemResourceLocation);
		}

		public String getGivenPlayerName() {
			return givenPlayerName;
		}

		public TrophyData setGivenPlayerName(String givenPlayerName) {
			this.givenPlayerName = givenPlayerName;
			return this;
		}

		public Date getDate() {
			return date;
		}

		public TrophyData setDate(Date date) {
			this.date = date;
			return this;
		}

		public ResourceLocation getType() {
			return type;
		}

		public Type getTypeType() {
			return Type.fromRegistryName(type);
		}

		public TrophyData setType(ResourceLocation type) {
			this.type = type;
			return this;
		}

		@OnlyIn(Dist.CLIENT)
		public void addInformation(List<Component> tooltip, TooltipFlag flag, ItemStack stack) {
			if (stack.getOrCreateTag().contains("BlockEntityTag") && stack.getOrCreateTag().getCompound("BlockEntityTag").contains("TrophyData")) {
				if (Screen.hasShiftDown()) {
					// Create date format for the locale area of the player
					DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());

					if (advName != null) tooltip.add(new TextComponent("Advancement: ").append(new TextComponent(advName).withStyle(Style.EMPTY.withColor(11583738))));
					if (date != null) tooltip.add(new TextComponent("Date Obtained: ").append(new TextComponent(dateFormat.format(date)).withStyle(Style.EMPTY.withColor(11583738))));
					if (givenPlayerName != null) tooltip.add(new TextComponent("Given to: ").append(new TextComponent(givenPlayerName).withStyle(Style.EMPTY.withColor(11583738))));
					if (itemResourceLocation != null) tooltip.add(new TextComponent("Item: ").append(new TranslatableComponent(ForgeRegistries.ITEMS.getValue(itemResourceLocation).getDescriptionId()).withStyle(Style.EMPTY.withColor(11583738))));
					if (type != null) tooltip.add(new TextComponent("Type: ").append(new TextComponent(Type.fromRegistryName(type).name.substring(0, 1).toUpperCase() + Type.fromRegistryName(type).name.substring(1)).withStyle(Type.fromRegistryName(type).color)));
				} else {
					tooltip.add(new TextComponent(ChatFormatting.DARK_GRAY + "Hold " + ChatFormatting.WHITE + "<SHIFT>" + ChatFormatting.DARK_GRAY + " for more information"));
				}
			} else {
				tooltip.add(new TextComponent(ChatFormatting.DARK_RED + "No data! Please recycle this trophy!"));
			}
		}

		@Override
		public CompoundTag serializeNBT() {
			CompoundTag nbt = new CompoundTag();
			if (uuid != null) {
				nbt.putUUID("uuid", uuid);
			}
			if (advName != null) {
				nbt.putString("advName", advName);
			}
			if (itemResourceLocation != null) {
				nbt.putString("itemResourceLocation", itemResourceLocation.toString());
			}
			if (givenPlayerName != null) {
				nbt.putString("givenPlayerName", givenPlayerName);
			}
			if (date != null) {
				nbt.putLong("date", date.getTime());
			}
			if (type != null) {
				nbt.putString("type", type.toString());
			}
			return nbt;
		}

		@Override
		public void deserializeNBT(CompoundTag nbt) {
			if (nbt.contains("BlockEntityTag")) {
				nbt = nbt.getCompound("BlockEntityTag");
			}
			if (nbt.contains("TrophyData")) {
				nbt = nbt.getCompound("TrophyData");
			}
			if (nbt.contains("uuid")) {
				uuid = nbt.getUUID("uuid");
			}
			if (nbt.contains("advName")) {
				advName = nbt.getString("advName");
			}
			if (nbt.contains("itemResourceLocation")) {
				itemResourceLocation = new ResourceLocation(nbt.getString("itemResourceLocation"));
			}
			if (nbt.contains("givenPlayerName")) {
				givenPlayerName = nbt.getString("givenPlayerName");
			}
			if (nbt.contains("date")) {
				date = new Date(nbt.getLong("date"));
			}
			if (nbt.contains("type")) {
				type = new ResourceLocation(nbt.getString("type"));
			}
		}

		public static TrophyData deserialize(CompoundTag nbt) {
			TrophyData data = new TrophyData(nbt);
			data.deserializeNBT(nbt);
			return data;
		}

		public TrophyData copy() {
			CompoundTag nbt = new CompoundTag();
			nbt.put("TrophyData", serializeNBT());
			return new TrophyData(nbt);
		}

		public TrophyData() {
		}

		public enum Type {
			IRON("iron", Main.id("textures/block/trophy/iron.png"), ChatFormatting.WHITE),
			GOLD("gold", Main.id("textures/block/trophy/gold.png"), ChatFormatting.GOLD),
			DIAMOND("diamond", Main.id("textures/block/trophy/diamond.png"), ChatFormatting.AQUA),
			EMERALD("emerald", Main.id("textures/block/trophy/emerald.png"), ChatFormatting.GREEN),
			NETHERITE("netherite", Main.id("textures/block/trophy/netherite.png"), 3025196),
			UNKNOWN("unknown", Main.id("textures/block/trophy/unknown.png"), ChatFormatting.DARK_GRAY);

			private final String name;
			private final ResourceLocation texture;
			private final ChatFormatting color;
			private final int decimalColor;

			Type(String name, ResourceLocation texture, ChatFormatting color) {
				this.name = name;
				this.texture = texture;
				this.color = color;
				this.decimalColor = color.getColor() == null ? 0 : color.getColor();
			}

			Type(String name, ResourceLocation texture, int color) {
				this.name = name;
				this.texture = texture;
				this.decimalColor = color;
				if (color == 3025196) {
					this.color = ChatFormatting.DARK_PURPLE;
				} else {
					this.color = ChatFormatting.WHITE;
				}
			}

			public String getName() {
				return name;
			}

			public ResourceLocation getTexture() {
				return texture;
			}

			public ChatFormatting getColor() {
				return color;
			}

			public int getDecimalColor() {
				return decimalColor;
			}

			public static Type fromName(String name) {
				for (Type type : values()) {
					if (type.getName().equals(name)) {
						return type;
					}
				}
				return UNKNOWN;
			}

			public static Type fromOrdinal(int ordinal) {
				for (Type type : values()) {
					if (type.ordinal() == ordinal) {
						return type;
					}
				}
				return UNKNOWN;
			}

			public ItemStack getItemStack() {
				return TrophyHelper.setType(this, new ItemStack(ModBlocks.TROPHY.get()));
			}

			public ResourceLocation getRegistryName() {
				return Main.id(name);
			}

			public static Type fromRegistryName(ResourceLocation name) {
				for (Type type : values()) {
					if (type.getRegistryName().equals(name)) {
						return type;
					}
				}
				return UNKNOWN;
			}
		}

		private static class EmptyTrophyData extends TrophyData {
			public EmptyTrophyData() {
				super();
				uuid = UUID.randomUUID();
				advName = "Blank";
				itemResourceLocation = new ResourceLocation("minecraft:air");
				givenPlayerName = "Dev";
				date = new Date();
				type = Type.UNKNOWN.getRegistryName();
			}
		}
	}
}
