package uk.co.joshepstein.minecrafttrophies.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import uk.co.joshepstein.minecrafttrophies.blocks.TrophyBlock;
import uk.co.joshepstein.minecrafttrophies.init.ModBlockEntities;

public class TrophyBlockEntity extends BlockEntity {
	TrophyBlock.TrophyData data = TrophyBlock.TrophyData.EMPTY;

	public TrophyBlockEntity(BlockEntityType<?> p_155228_, BlockPos p_155229_, BlockState p_155230_, TrophyBlock.TrophyData data) {
		super(p_155228_, p_155229_, p_155230_);
		this.data = data;
	}

	public TrophyBlockEntity(BlockPos blockPos, BlockState blockState) {
		this(ModBlockEntities.TROPHY.get(), blockPos, blockState, TrophyBlock.TrophyData.EMPTY);
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		CompoundTag dataTag = new CompoundTag();
		dataTag.put("TrophyData", data.serializeNBT());
		tag.put("BlockEntityTag", dataTag);
		super.saveAdditional(tag);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		this.data = new TrophyBlock.TrophyData(tag);
		this.setChanged();
	}

	@Override
	public CompoundTag getUpdateTag() {
		return this.saveWithoutMetadata();
	}

	public static @NotNull TrophyBlockEntity create(BlockPos pos, BlockState state) {
		return new TrophyBlockEntity(pos, state);
	}

	public TrophyBlock.TrophyData getData() {
		return data;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		super.deserializeNBT(nbt);
		this.data = new TrophyBlock.TrophyData(nbt);
	}

	@Override
	public CompoundTag serializeNBT() {
		CompoundTag bet = new CompoundTag();
		CompoundTag tag = new CompoundTag();

		tag.put("TrophyData", data.serializeNBT());
		bet.put("BlockEntityTag", tag);
		return bet;
	}

}
