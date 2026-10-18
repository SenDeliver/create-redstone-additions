package dev.siliconcarbidecube.create_redstone_additions;

import com.simibubi.create.AllItems;
import dev.siliconcarbidecube.create_redstone_additions.blocks.Diode;
import dev.siliconcarbidecube.create_redstone_additions.init.ModBlocks;
import dev.siliconcarbidecube.create_redstone_additions.test.TestArea;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class WrenchTest {
    @Test
    void sneakWrenchPicksUpIntoInventory() {
        try (TestArea area = TestArea.create()) {
            FakePlayer player = area.player(AllItems.WRENCH.asStack());
            player.setShiftKeyDown(true);
            BlockPos pos = new BlockPos(3, 1, 3);
            for (Supplier<? extends Block> block : DropsTest.BLOCKS) {
                area.set(pos, block.get().defaultBlockState());
                area.useItemOn(player, pos, Direction.UP);
                assertTrue(area.get(pos).isAir(), block.get() + " removed");
                assertEquals(1, player.getInventory().countItem(block.get().asItem()), block.get() + " in inventory");
            }
            assertTrue(area.items(pos, 2).isEmpty());
        }
    }

    @Test
    void sneakWrenchInCreativeRemovesWithoutItem() {
        try (TestArea area = TestArea.create()) {
            FakePlayer player = area.player(AllItems.WRENCH.asStack());
            player.setGameMode(GameType.CREATIVE);
            player.setShiftKeyDown(true);
            BlockPos pos = new BlockPos(3, 1, 3);
            area.set(pos, ModBlocks.DIODE.get().defaultBlockState());
            area.useItemOn(player, pos, Direction.UP);
            assertTrue(area.get(pos).isAir());
            assertEquals(0, player.getInventory().countItem(ModBlocks.DIODE.get().asItem()));
            assertTrue(area.items(pos, 2).isEmpty());
        }
    }

    @Test
    void wrenchRotatesEveryBlock() {
        try (TestArea area = TestArea.create()) {
            FakePlayer player = area.player(AllItems.WRENCH.asStack());
            BlockPos pos = new BlockPos(3, 1, 3);
            area.redstoneBlock(pos.north());
            for (Supplier<? extends Block> block : DropsTest.BLOCKS) {
                area.set(pos, block.get().defaultBlockState().setValue(DiodeBlock.FACING, Direction.NORTH));
                area.useItemOn(player, pos, Direction.UP);
                assertEquals(Direction.EAST, area.get(pos).getValue(DiodeBlock.FACING), block.get() + " rotated");
                area.tick(1);
                assertTrue(area.get(pos).is(block.get()));
            }
        }
    }

    @Test
    void wrenchRotatesDiodeAndUpdatesRedstone() {
        try (TestArea area = TestArea.create()) {
            BlockPos pos = new BlockPos(5, 1, 5);
            BlockPos oldLamp = pos.east(2);
            BlockPos newLamp = pos.south();
            area.set(pos, ModBlocks.DIODE.get().defaultBlockState().setValue(DiodeBlock.FACING, Direction.WEST));
            area.set(pos.east(), Blocks.STONE.defaultBlockState());
            area.set(oldLamp, Blocks.REDSTONE_LAMP.defaultBlockState());
            area.set(newLamp, Blocks.REDSTONE_LAMP.defaultBlockState());
            area.redstoneBlock(pos.west());
            assertEquals(15, area.get(pos).getValue(Diode.POWER));
            assertTrue(area.get(oldLamp).getValue(RedstoneLampBlock.LIT));

            area.useItemOn(area.player(AllItems.WRENCH.asStack()), pos, Direction.UP);
            assertEquals(Direction.NORTH, area.get(pos).getValue(DiodeBlock.FACING));
            assertEquals(0, area.get(pos).getValue(Diode.POWER));
            area.tick(5);
            assertFalse(area.get(newLamp).getValue(RedstoneLampBlock.LIT));
            assertFalse(area.get(oldLamp).getValue(RedstoneLampBlock.LIT));
        }
    }

    @Test
    void wrenchRotatesInverterAndUpdatesRedstone() {
        try (TestArea area = TestArea.create()) {
            BlockPos pos = new BlockPos(5, 1, 5);
            BlockPos newLamp = pos.south();
            area.redstoneBlock(pos.west());
            area.set(pos, ModBlocks.INVERTER.get().defaultBlockState()
                    .setValue(DiodeBlock.FACING, Direction.WEST).setValue(DiodeBlock.POWERED, false));
            area.set(newLamp, Blocks.REDSTONE_LAMP.defaultBlockState());
            area.tick(2);
            assertFalse(area.get(pos).getValue(DiodeBlock.POWERED));

            area.useItemOn(area.player(AllItems.WRENCH.asStack()), pos, Direction.UP);
            assertEquals(Direction.NORTH, area.get(pos).getValue(DiodeBlock.FACING));
            area.tick(2);
            assertTrue(area.get(pos).getValue(DiodeBlock.POWERED));
            assertTrue(area.get(newLamp).getValue(RedstoneLampBlock.LIT));
        }
    }
}
