package net.minestom.server.instance;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.UnitModifier;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

final class GeneratorImpl {
    private static final Vec SECTION_SIZE = new Vec(16);

    record ChunkEntry(List<Section> sections, int x, int z) {
        public ChunkEntry(Chunk chunk) {
            this(chunk.getSections(), chunk.getChunkX(), chunk.getChunkZ());
        }
    }

    static GenerationUnit.Section section(Section section, int sectionX, int sectionY, int sectionZ) {
        record SectionImpl(int sectionX, int sectionY, int sectionZ,
                           Point size, Point absoluteStart, Point absoluteEnd, UnitModifier modifier)
                implements GenerationUnit.Section {
        }
        final var start = SECTION_SIZE.mul(sectionX, sectionY, sectionZ);
        final var end = start.add(16);
        final UnitModifier modifier = new ModifierImpl(SECTION_SIZE, start, end) {
            @Override
            public void setBlock(int x, int y, int z, @NotNull Block block) {
                final int localX = ChunkUtils.toSectionRelativeCoordinate(x);
                final int localY = ChunkUtils.toSectionRelativeCoordinate(y);
                final int localZ = ChunkUtils.toSectionRelativeCoordinate(z);
                section.blockPalette().set(localX, localY, localZ, block.stateId());
            }

            @Override
            public void setRelative(int x, int y, int z, @NotNull Block block) {
                section.blockPalette().set(x, y, z, block.stateId());
            }

            @Override
            public void setAllRelative(@NotNull Supplier supplier) {
                section.blockPalette().setAll((x, y, z) -> supplier.get(x, y, z).stateId());
            }

            @Override
            public void fill(@NotNull Block block) {
                section.blockPalette().fill(block.stateId());
            }
        };
        return new SectionImpl(sectionX, sectionY, sectionZ, SECTION_SIZE, start, end, modifier);
    }

    static GenerationUnit.Chunk chunk(int minSection, int maxSection, ChunkEntry chunk) {
        final int minY = minSection * 16;

        AtomicInteger sectionCounterY = new AtomicInteger(minSection);
        List<GenerationUnit.Section> sections = chunk.sections().stream()
                .map(section -> section(section, chunk.x(), sectionCounterY.getAndIncrement(), chunk.z()))
                .toList();
        record Impl(int chunkX, int chunkZ, int minY, List<Section> sections,
                    Point size, Point absoluteStart, Point absoluteEnd, UnitModifier modifier)
                implements GenerationUnit.Chunk {
        }

        final int chunkX = chunk.x();
        final int chunkZ = chunk.z();
        final var size = new Vec(16, (maxSection - minSection) * 16, 16);
        final var start = new Vec(chunkX * 16, minY, chunkZ * 16);
        final var end = new Vec(chunkX * 16 + 16, size.y() + minY, chunkZ * 16 + 16);
        final UnitModifier modifier = new ModifierImpl(size, start, end) {
            @Override
            public void setBlock(int x, int y, int z, @NotNull Block block) {
                if (ChunkUtils.getChunkCoordinate(x) != chunkX || ChunkUtils.getChunkCoordinate(z) != chunkZ) {
                    throw new IllegalArgumentException("x and z must be in the same chunk");
                }
                y -= minY;
                final int sectionY = ChunkUtils.getChunkCoordinate(y);
                final GenerationUnit.Section section = sections.get(sectionY);
                section.modifier().setBlock(x, y, z, block);
            }

            @Override
            public void setRelative(int x, int y, int z, @NotNull Block block) {
                if (x < 0 || x >= size.x() || y < 0 || y >= size.y() || z < 0 || z >= size.z()) {
                    throw new IllegalArgumentException("x, y and z must be in the chunk: " + x + ", " + y + ", " + z);
                }
                final GenerationUnit.Section section = sections.get(y / 16);
                section.modifier().setBlock(x, y % 16, z, block);
            }

            @Override
            public void setAll(@NotNull Supplier supplier) {
                for (GenerationUnit.Section section : sections) {
                    final var start = section.absoluteStart();
                    final int startX = start.blockX();
                    final int startY = start.blockY();
                    final int startZ = start.blockZ();
                    section.modifier().setAllRelative((x, y, z) ->
                            supplier.get(x + startX, y + startY, z + startZ));
                }
            }

            @Override
            public void fill(@NotNull Block block) {
                for (GenerationUnit.Section section : sections) {
                    section.modifier().fill(block);
                }
            }
        };
        return new Impl(chunkX, chunkZ, minY, sections, size, start, end, modifier);
    }

    static abstract class ModifierImpl implements UnitModifier {
        private final Point size, start, end;

        public ModifierImpl(Point size, Point start, Point end) {
            this.size = size;
            this.start = start;
            this.end = end;
        }

        @Override
        public void setAll(@NotNull Supplier supplier) {
            for (int x = start.blockX(); x < end.blockX(); x++) {
                for (int y = start.blockY(); y < end.blockY(); y++) {
                    for (int z = start.blockZ(); z < end.blockZ(); z++) {
                        setBlock(x, y, z, supplier.get(x, y, z));
                    }
                }
            }
        }

        @Override
        public void setAllRelative(@NotNull Supplier supplier) {
            for (int x = 0; x < size.blockX(); x++) {
                for (int y = 0; y < size.blockY(); y++) {
                    for (int z = 0; z < size.blockZ(); z++) {
                        setRelative(x, y, z, supplier.get(x, y, z));
                    }
                }
            }
        }

        @Override
        public void fill(@NotNull Block block) {
            fill(start, end, block);
        }

        @Override
        public void fill(@NotNull Point start, @NotNull Point end, @NotNull Block block) {
            final int endX = end.blockX();
            final int endY = end.blockY();
            final int endZ = end.blockZ();
            for (int x = start.blockX(); x < endX; x++) {
                for (int y = start.blockY(); y < endY; y++) {
                    for (int z = start.blockZ(); z < endZ; z++) {
                        setBlock(x, y, z, block);
                    }
                }
            }
        }
    }
}
