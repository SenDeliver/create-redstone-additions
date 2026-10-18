package dev.siliconcarbidecube.create_redstone_additions;

import dev.siliconcarbidecube.create_redstone_additions.test.TestArea;
import dev.siliconcarbidecube.create_redstone_additions.test.TestServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RepeaterBlock;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SmokeTest {
    private static final List<String> BLOCKS = List.of("conjunctor", "disjunctor", "diode", "crossroad", "inverter",
            "golden_resistor", "iron_resistor", "ceramic_resistor", "glass_resistor");
    private static final List<String> ITEMS = List.of("cyan_quartz", "polished_cyan_quartz", "verdigris", "quartz_semiconductor");

    @Test
    void modContentIsRegisteredAndCreateIsLoaded() {
        assertTrue(ModList.get().isLoaded("create"));
        assertTrue(ModList.get().isLoaded(CreateRedstoneAdditions.MOD_ID));
        for (String id : BLOCKS) {
            ResourceLocation key = ResourceLocation.fromNamespaceAndPath(CreateRedstoneAdditions.MOD_ID, id);
            assertTrue(BuiltInRegistries.BLOCK.containsKey(key), "block " + id);
            assertTrue(BuiltInRegistries.ITEM.containsKey(key), "block item " + id);
        }
        for (String id : ITEMS) {
            assertTrue(BuiltInRegistries.ITEM.containsKey(ResourceLocation.fromNamespaceAndPath(CreateRedstoneAdditions.MOD_ID, id)), "item " + id);
        }
        assertNotNull(TestServer.level());
    }

    @Test
    void leverPowersWireAndRepeaterNeedsTicks() {
        try (TestArea area = TestArea.create()) {
            BlockPos lever = new BlockPos(1, 1, 1);
            BlockPos wire = lever.east();
            BlockPos repeater = wire.east();
            BlockPos out = repeater.east();
            area.lever(lever, false);
            area.wire(wire);
            area.set(repeater, Blocks.REPEATER.defaultBlockState().setValue(RepeaterBlock.FACING, Direction.WEST));
            area.wire(out);
            assertEquals(0, area.wirePower(wire));

            area.setLever(lever, true);
            assertEquals(15, area.wirePower(wire));
            assertEquals(15, area.signalInto(wire, Direction.WEST));
            assertEquals(15, area.signalFrom(lever, Direction.EAST));
            assertEquals(0, area.wirePower(out));

            area.tick(1);
            assertEquals(0, area.wirePower(out));
            area.tick(2);
            assertEquals(15, area.wirePower(out));
            assertEquals(15, area.power(out));

            area.setLever(lever, false);
            assertEquals(0, area.wirePower(wire));
            area.tick(3);
            assertEquals(0, area.wirePower(out));
        }
    }

    @Test
    void playerPlacesAndBreaksBlock() {
        try (TestArea area = TestArea.create()) {
            BlockPos pos = new BlockPos(3, 1, 3);
            FakePlayer player = area.player(new ItemStack(Items.DIRT, 2));
            area.useItemOn(player, pos.below(), Direction.UP);
            assertTrue(area.get(pos).is(Blocks.DIRT));
            assertEquals(1, player.getMainHandItem().getCount());

            assertEquals(1, area.drops(pos).size());
            assertTrue(area.breakBlock(player, pos));
            assertTrue(area.get(pos).isAir());
            assertEquals(1, area.countItems(Items.DIRT, pos, 1));
        }
    }
}
