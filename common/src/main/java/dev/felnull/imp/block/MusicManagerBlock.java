package dev.felnull.imp.block;


import dev.felnull.imp.IamMusicPlayer;
import dev.felnull.imp.blockentity.IMPBlockEntities;
import dev.felnull.imp.blockentity.MusicManagerBlockEntity;
import dev.felnull.otyacraftengine.shape.bundle.DirectionVoxelShapesBundle;
import dev.felnull.otyacraftengine.util.OEVoxelShapeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class MusicManagerBlock extends IMPBaseEntityBlock {
    private static final DirectionVoxelShapesBundle SHAPE = OEVoxelShapeUtils.makeAllDirection(OEVoxelShapeUtils.getShapeFromResource(new ResourceLocation(IamMusicPlayer.MODID, "music_manager"), MusicManagerBlock.class));
    //private HorizontalKineticBlock MusicManagerKineticBlock;
    protected MusicManagerBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        //MusicManagerKineticBlock = new HorizontalKineticBlock(this.properties) {
        //    @Override
        //    public Direction.Axis getRotationAxis(BlockState state) {
        //        return state.getValue(HORIZONTAL_FACING).getAxis();
        //    }

        //    @Override
        //    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        //        return face.getOpposite() == state.getValue(HORIZONTAL_FACING);
        //    }
        //};
        return new MusicManagerBlockEntity(blockPos, blockState);
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE.getShape(blockState.getValue(FACING));
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, IMPBlockEntities.MUSIC_MANAGER.get(), MusicManagerBlockEntity::tick);
    }
}
