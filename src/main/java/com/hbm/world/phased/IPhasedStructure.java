package com.hbm.world.phased;

import com.hbm.world.phased.AbstractPhasedStructure.BlockInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@ParametersAreNonnullByDefault
public interface IPhasedStructure {

    /**
     * Generates the part of the structure that lies within a specific chunk.
     *
     * @param world           The world
     * @param rand            A random object
     * @param structureOrigin The absolute origin (corner) of the entire structure in the world.
     * @param chunkPos        The position of the chunk to generate blocks in.
     */
    void generateForChunk(World world, Random rand, BlockPos structureOrigin, ChunkPos chunkPos, List<BlockInfo> blockInfos);

    @Contract("_ -> new")
    default List<@NotNull BlockPos> getValidationPoints(@NotNull BlockPos origin){
        return Collections.emptyList();
    }

    default boolean checkSpawningConditions(@NotNull World world, @NotNull BlockPos pos) {
        return true;
    }
}
