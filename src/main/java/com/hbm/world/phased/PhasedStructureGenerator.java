package com.hbm.world.phased;

import com.hbm.config.GeneralConfig;
import com.hbm.world.phased.AbstractPhasedStructure.BlockInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Solution to cascading worldgen.
 * For some reason the worldgen can still cascade with the phased generator in <strong>extremely rare cases</strong>, idk why and I can't fix it.<br>
 * So instead there is a {@link GeneralConfig#enableTickBasedWorldGenerator} flag that can <strong>eliminate</strong> the cascading.<br>
 * This would be incompatible to most, if not all chunk pregenerators.<br>
 * Note that even with {@link GeneralConfig#enableTickBasedWorldGenerator} disabled, the mitigation still works.
 *
 * @author mlbv
 */
public class PhasedStructureGenerator implements IWorldGenerator {
    public static final PhasedStructureGenerator INSTANCE = new PhasedStructureGenerator();
    private final Map<Integer, Set<PendingValidationStructure>> pendingValidations = new ConcurrentHashMap<>();
    private final Map<Integer, Queue<ReadyToGenerateStructure>> generationQueues = new ConcurrentHashMap<>();

    private PhasedStructureGenerator() {
    }

    public void scheduleStructureForValidation(World world, BlockPos origin, IPhasedStructure structure, Map<ChunkPos, List<BlockInfo>> layout) {
        if (layout.isEmpty() && structure.getValidationPoints(BlockPos.ORIGIN).isEmpty()) return;
        int dimId = world.provider.getDimension();
        this.pendingValidations.computeIfAbsent(dimId, k -> ConcurrentHashMap.newKeySet())
                .add(new PendingValidationStructure(origin, structure, layout, world.getSeed()));
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        int dimId = world.provider.getDimension();
        Set<PendingValidationStructure> structuresInDim = this.pendingValidations.get(dimId);
        if (structuresInDim == null || structuresInDim.isEmpty()) {
            return;
        }

        ChunkPos currentChunkPos = new ChunkPos(chunkX, chunkZ);
        Iterator<PendingValidationStructure> iterator = structuresInDim.iterator();

        while (iterator.hasNext()) {
            PendingValidationStructure pending = iterator.next();
            if (pending.chunksAwaitingGeneration.contains(currentChunkPos)) {
                pending.chunksAwaitingGeneration.remove(currentChunkPos);
                if (pending.chunksAwaitingGeneration.isEmpty()) {
                    ReadyToGenerateStructure validated = validate(world, pending);

                    if (validated != null) {
                        if (GeneralConfig.enableTickBasedWorldGenerator) {
                            generationQueues.computeIfAbsent(dimId, k -> new ConcurrentLinkedQueue<>()).add(validated);
                        } else {
                            generateValidated(world, validated);
                        }
                    }
                    iterator.remove();
                }
            }
        }
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world.isRemote || !GeneralConfig.enableTickBasedWorldGenerator) {
            return;
        }

        Queue<ReadyToGenerateStructure> queue = generationQueues.get(event.world.provider.getDimension());
        if (queue != null && !queue.isEmpty()) {
            ReadyToGenerateStructure validated = queue.poll();
            if (validated != null) {
                generateValidated(event.world, validated);
            }
        }
    }

    @Nullable
    private ReadyToGenerateStructure validate(World world, PendingValidationStructure pending) {
        IPhasedStructure phasedStructure = pending.structure;
        BlockPos originAtY0 = pending.origin;
        List<BlockPos> validationPoints = phasedStructure.getValidationPoints(originAtY0);

        if (validationPoints.isEmpty()) {
            return new ReadyToGenerateStructure(pending, originAtY0);
        } else {
            if (GeneralConfig.enableDebugWorldGen) {
                IChunkProvider chunkProvider = world.getChunkProvider();
                for (BlockPos validationPoint : validationPoints) {
                    int chunkX = validationPoint.getX() >> 4;
                    int chunkZ = validationPoint.getZ() >> 4;
                    if (!chunkProvider.isChunkGeneratedAt(chunkX, chunkZ)) {
                        throw new IllegalStateException(String.format(
                                "Structure %s attempted to validate in an ungenerated chunk at [%d, %d] (validation point: %s). " +
                                        "This is a bug!",
                                phasedStructure.getClass().getName(), chunkX, chunkZ, validationPoint
                        ));
                    }
                }
            }
            int newY = validationPoints.stream()
                    .mapToInt(p -> world.getHeight(p.getX(), p.getZ()))
                    .min()
                    .orElse(-1);

            if (newY <= 0 || newY >= world.getHeight()) return null;
            BlockPos realOrigin = new BlockPos(originAtY0.getX(), newY, originAtY0.getZ());
            if (phasedStructure.checkSpawningConditions(world, realOrigin)) {
                return new ReadyToGenerateStructure(pending, realOrigin);
            }
        }
        return null;
    }

    private void generateValidated(World world, ReadyToGenerateStructure validated) {
        IPhasedStructure phasedStructure = validated.pending.structure;
        Random structureRand = validated.pending.createRandom();
        Map<ChunkPos, List<BlockInfo>> layout = validated.pending.layout;
        int originChunkX = validated.finalOrigin.getX() >> 4;
        int originChunkZ = validated.finalOrigin.getZ() >> 4;
        for (Map.Entry<ChunkPos, List<BlockInfo>> entry : layout.entrySet()) {
            ChunkPos relativeChunkPos = entry.getKey();
            List<BlockInfo> blocksForThisChunk = entry.getValue();
            ChunkPos absoluteChunkPos = new ChunkPos(originChunkX + relativeChunkPos.x, originChunkZ + relativeChunkPos.z);
            phasedStructure.generateForChunk(world, structureRand, validated.finalOrigin, absoluteChunkPos, blocksForThisChunk);
        }
    }

    public void forceGenerateStructure(World world, Random rand, BlockPos origin, IPhasedStructure structure, Map<ChunkPos, List<BlockInfo>> layout) {
        int originChunkX = origin.getX() >> 4;
        int originChunkZ = origin.getZ() >> 4;
        for (Map.Entry<ChunkPos, List<BlockInfo>> entry : layout.entrySet()) {
            ChunkPos absoluteChunkPos = new ChunkPos(originChunkX + entry.getKey().x, originChunkZ + entry.getKey().z);
            structure.generateForChunk(world, rand, origin, absoluteChunkPos, entry.getValue());
        }
    }

    private static class ReadyToGenerateStructure {
        final PendingValidationStructure pending;
        final BlockPos finalOrigin;
        ReadyToGenerateStructure(PendingValidationStructure pending, BlockPos finalOrigin) {
            this.pending = pending;
            this.finalOrigin = finalOrigin;
        }
    }

    private static class PendingValidationStructure {
        final BlockPos origin;
        final IPhasedStructure structure;
        final Set<ChunkPos> requiredChunks;
        final Set<ChunkPos> chunksAwaitingGeneration;
        final Map<ChunkPos, List<BlockInfo>> layout;
        final long worldSeed;

        PendingValidationStructure(BlockPos origin, IPhasedStructure structure, Map<ChunkPos, List<BlockInfo>> layout, long worldSeed) {
            this.origin = origin;
            this.structure = structure;
            this.layout = layout;
            this.worldSeed = worldSeed;
            Set<ChunkPos> allRequiredRelativeChunks = new HashSet<>(layout.keySet());
            List<BlockPos> validationPoints = structure.getValidationPoints(BlockPos.ORIGIN);
            for (BlockPos validationPoint : validationPoints) {
                allRequiredRelativeChunks.add(new ChunkPos(validationPoint.getX() >> 4, validationPoint.getZ() >> 4));
            }
            int originChunkX = origin.getX() >> 4;
            int originChunkZ = origin.getZ() >> 4;
            this.requiredChunks = new HashSet<>();
            for(ChunkPos relativePos : allRequiredRelativeChunks) {
                this.requiredChunks.add(new ChunkPos(originChunkX + relativePos.x, originChunkZ + relativePos.z));
            }
            this.chunksAwaitingGeneration = new HashSet<>(this.requiredChunks);
        }

        Random createRandom() {
            Random rand = new Random(this.worldSeed);
            long x = rand.nextLong() ^ this.origin.getX();
            long z = rand.nextLong() ^ this.origin.getZ();
            rand.setSeed(x * this.origin.getX() + z * this.origin.getZ() ^ this.worldSeed);
            return rand;
        }
    }
}
