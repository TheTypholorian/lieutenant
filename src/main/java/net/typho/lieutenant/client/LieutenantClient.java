package net.typho.lieutenant.client;

import com.mojang.brigadier.suggestion.Suggestion;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.typho.lieutenant.CloneType;
import net.typho.lieutenant.Lieutenant;
import net.typho.lieutenant.SelectionItem;
import net.typho.lieutenant.SetFeatureS2CPacket;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class LieutenantClient implements ClientModInitializer {
    public static final KeyBinding SELECT_SELF = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.lieutenant.select_self",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_ALT,
            "category.lieutenant"
    ));
    public static final Map<Identifier, ItemStack> ICONS = new LinkedHashMap<>();
    public static final Map<Identifier, Item> NON_ITEM_ICONS = new LinkedHashMap<>();

    static {
        NON_ITEM_ICONS.put(Identifier.of("acacia"), Items.ACACIA_SAPLING);
        NON_ITEM_ICONS.put(Identifier.of("amethyst_geode"), Items.AMETHYST_CLUSTER);
        NON_ITEM_ICONS.put(Identifier.of("azalea_tree"), Items.FLOWERING_AZALEA);
        NON_ITEM_ICONS.put(Identifier.of("bamboo_no_podzol"), Items.BAMBOO);
        NON_ITEM_ICONS.put(Identifier.of("bamboo_some_podzol"), Items.BAMBOO);
        NON_ITEM_ICONS.put(Identifier.of("bamboo_vegetation"), Items.BAMBOO);
        NON_ITEM_ICONS.put(Identifier.of("basalt_blobs"), Items.BASALT);
        NON_ITEM_ICONS.put(Identifier.of("basalt_pillar"), Items.BASALT);
        NON_ITEM_ICONS.put(Identifier.of("birch"), Items.BIRCH_SAPLING);
        NON_ITEM_ICONS.put(Identifier.of("birch_bees_0002"), Items.BIRCH_SAPLING);
        NON_ITEM_ICONS.put(Identifier.of("birch_bees_002"), Items.BIRCH_SAPLING);
        NON_ITEM_ICONS.put(Identifier.of("birch_bees_005"), Items.BIRCH_SAPLING);
        NON_ITEM_ICONS.put(Identifier.of("birch_tall"), Items.BIRCH_SAPLING);
        NON_ITEM_ICONS.put(Identifier.of("blackstone_blobs"), Items.BLACKSTONE);
        NON_ITEM_ICONS.put(Identifier.of("bonus_chest"), Items.CHEST);
        NON_ITEM_ICONS.put(Identifier.of("cave_vine"), Items.VINE);
        NON_ITEM_ICONS.put(Identifier.of("cave_vine_in_moss"), Items.GLOW_BERRIES);
        NON_ITEM_ICONS.put(Identifier.of("cherry"), Items.CHERRY_SAPLING);
        NON_ITEM_ICONS.put(Identifier.of("cherry_bees_005"), Items.CHERRY_SAPLING);
        NON_ITEM_ICONS.put(Identifier.of("clay_pool_with_dripleaves"), Items.CLAY);
        NON_ITEM_ICONS.put(Identifier.of("clay_with_dripleaves"), Items.CLAY);
        NON_ITEM_ICONS.put(Identifier.of("crimson_forest_vegetation"), Items.CRIMSON_ROOTS);
        NON_ITEM_ICONS.put(Identifier.of("crimson_forest_vegetation_bonemeal"), Items.CRIMSON_ROOTS);
        NON_ITEM_ICONS.put(Identifier.of("crimson_fungus_planted"), Items.CRIMSON_FUNGUS);
        NON_ITEM_ICONS.put(Identifier.of("dark_forest_vegetation"), Items.DARK_OAK_LOG);
        NON_ITEM_ICONS.put(Identifier.of("dark_oak"), Items.DARK_OAK_SAPLING);
        NON_ITEM_ICONS.put(Identifier.of("delta"), Items.MAGMA_BLOCK);
        NON_ITEM_ICONS.put(Identifier.of("desert_well"), Items.SANDSTONE);
        NON_ITEM_ICONS.put(Identifier.of("disk_clay"), Items.CLAY);
        NON_ITEM_ICONS.put(Identifier.of("disk_grass"), Items.GRASS_BLOCK);
        NON_ITEM_ICONS.put(Identifier.of("disk_gravel"), Items.GRAVEL);
        NON_ITEM_ICONS.put(Identifier.of("disk_sand"), Items.SAND);
        NON_ITEM_ICONS.put(Identifier.of("dripleaf"), Items.BIG_DRIPLEAF);
        NON_ITEM_ICONS.put(Identifier.of("dripstone_cluster"), Items.POINTED_DRIPSTONE);
        NON_ITEM_ICONS.put(Identifier.of("end_gateway_delayed"), Items.BEDROCK);
        NON_ITEM_ICONS.put(Identifier.of("end_gateway_return"), Items.BEDROCK);
        NON_ITEM_ICONS.put(Identifier.of("end_island"), Items.END_STONE);
        NON_ITEM_ICONS.put(Identifier.of("end_platform"), Items.OBSIDIAN);
        NON_ITEM_ICONS.put(Identifier.of("end_spike"), Items.OBSIDIAN);
        NON_ITEM_ICONS.put(Identifier.of("fancy_oak"), Items.OAK_SAPLING);
        NON_ITEM_ICONS.put(Identifier.of("fancy_oak_bees"), Items.OAK_SAPLING);
        NON_ITEM_ICONS.put(Identifier.of("fancy_oak_bees_0002"), Items.OAK_SAPLING);
        NON_ITEM_ICONS.put(Identifier.of("fancy_oak_bees_002"), Items.OAK_SAPLING);
        NON_ITEM_ICONS.put(Identifier.of("fancy_oak_bees_005"), Items.OAK_SAPLING);
        NON_ITEM_ICONS.put(Identifier.of("flower_cherry"), Items.PINK_PETALS);
        NON_ITEM_ICONS.put(Identifier.of("flower_default"), Items.POPPY);
        NON_ITEM_ICONS.put(Identifier.of("flower_flower_forest"), Items.ORANGE_TULIP);
        NON_ITEM_ICONS.put(Identifier.of("flower_meadow"), Items.AZURE_BLUET);
        NON_ITEM_ICONS.put(Identifier.of("flower_plain"), Items.DANDELION);
        NON_ITEM_ICONS.put(Identifier.of("flower_swamp"), Items.BLUE_ORCHID);
        NON_ITEM_ICONS.put(Identifier.of("forest_flowers"), Items.LILY_OF_THE_VALLEY);
        NON_ITEM_ICONS.put(Identifier.of("forest_rock"), Items.MOSSY_COBBLESTONE);
        NON_ITEM_ICONS.put(Identifier.of("fossil_coal"), Items.BONE_BLOCK);
        NON_ITEM_ICONS.put(Identifier.of("fossil_diamonds"), Items.BONE_BLOCK);
        NON_ITEM_ICONS.put(Identifier.of("freeze_top_layer"), Items.SNOW);
        NON_ITEM_ICONS.put(Identifier.of("glowstone_extra"), Items.GLOWSTONE);
        NON_ITEM_ICONS.put(Identifier.of("huge_brown_mushroom"), Items.BROWN_MUSHROOM_BLOCK);
        NON_ITEM_ICONS.put(Identifier.of("huge_red_mushroom"), Items.RED_MUSHROOM_BLOCK);
        NON_ITEM_ICONS.put(Identifier.of("ice_patch"), Items.ICE);
        NON_ITEM_ICONS.put(Identifier.of("ice_spike"), Items.ICE);
        NON_ITEM_ICONS.put(Identifier.of("iceberg_blue"), Items.BLUE_ICE);
        NON_ITEM_ICONS.put(Identifier.of("iceberg_packed"), Items.PACKED_ICE);
        NON_ITEM_ICONS.put(Identifier.of("jungle_bush"), Items.JUNGLE_LEAVES);
        NON_ITEM_ICONS.put(Identifier.of("jungle_tree"), Items.JUNGLE_SAPLING);
        NON_ITEM_ICONS.put(Identifier.of("jungle_tree_no_vine"), Items.JUNGLE_SAPLING);
        NON_ITEM_ICONS.put(Identifier.of("lake_lava"), Items.LAVA_BUCKET);
        NON_ITEM_ICONS.put(Identifier.of("large_basalt_columns"), Items.BASALT);
        NON_ITEM_ICONS.put(Identifier.of("large_dripstone"), Items.POINTED_DRIPSTONE);
        NON_ITEM_ICONS.put(Identifier.of("lush_caves_clay"), Items.CLAY);
        NON_ITEM_ICONS.put(Identifier.of("mangrove"), Items.MANGROVE_PROPAGULE);
        NON_ITEM_ICONS.put(Identifier.of("mangrove_vegetation"), Items.MANGROVE_PROPAGULE);
        NON_ITEM_ICONS.put(Identifier.of("meadow_trees"), Items.OAK_SAPLING);
        NON_ITEM_ICONS.put(Identifier.of("mega_jungle_tree"), Items.JUNGLE_SAPLING);
        NON_ITEM_ICONS.put(Identifier.of("mega_pine"), Items.SPRUCE_SAPLING);
        NON_ITEM_ICONS.put(Identifier.of("mega_spruce"), Items.SPRUCE_SAPLING);
        NON_ITEM_ICONS.put(Identifier.of("monster_room"), Items.SPAWNER);
        NON_ITEM_ICONS.put(Identifier.of("moss_patch"), Items.MOSS_BLOCK);
        NON_ITEM_ICONS.put(Identifier.of("moss_patch_bonemeal"), Items.MOSS_BLOCK);
        NON_ITEM_ICONS.put(Identifier.of("moss_patch_ceiling"), Items.MOSS_BLOCK);
        NON_ITEM_ICONS.put(Identifier.of("moss_vegetation"), Items.MOSS_CARPET);
        NON_ITEM_ICONS.put(Identifier.of("mushroom_island_vegetation"), Items.MYCELIUM);
        NON_ITEM_ICONS.put(Identifier.of("nether_sprouts_bonemeal"), Items.NETHER_SPROUTS);
        NON_ITEM_ICONS.put(Identifier.of("oak"), Items.OAK_SAPLING);
        NON_ITEM_ICONS.put(Identifier.of("oak_bees_0002"), Items.OAK_SAPLING);
        NON_ITEM_ICONS.put(Identifier.of("oak_bees_002"), Items.OAK_SAPLING);
        NON_ITEM_ICONS.put(Identifier.of("oak_bees_005"), Items.OAK_SAPLING);
        NON_ITEM_ICONS.put(Identifier.of("ore_ancient_debris_large"), Items.ANCIENT_DEBRIS);
        NON_ITEM_ICONS.put(Identifier.of("ore_ancient_debris_small"), Items.ANCIENT_DEBRIS);
        NON_ITEM_ICONS.put(Identifier.of("ore_andesite"), Items.ANDESITE);
        NON_ITEM_ICONS.put(Identifier.of("ore_blackstone"), Items.BLACKSTONE);
        NON_ITEM_ICONS.put(Identifier.of("ore_clay"), Items.CLAY);
        NON_ITEM_ICONS.put(Identifier.of("ore_coal"), Items.COAL_ORE);
        NON_ITEM_ICONS.put(Identifier.of("ore_coal_buried"), Items.COAL_ORE);
        NON_ITEM_ICONS.put(Identifier.of("ore_copper_large"), Items.COPPER_ORE);
        NON_ITEM_ICONS.put(Identifier.of("ore_copper_small"), Items.COPPER_ORE);
        NON_ITEM_ICONS.put(Identifier.of("ore_diamond_buried"), Items.DIAMOND_ORE);
        NON_ITEM_ICONS.put(Identifier.of("ore_diamond_large"), Items.DIAMOND_ORE);
        NON_ITEM_ICONS.put(Identifier.of("ore_diamond_medium"), Items.DIAMOND_ORE);
        NON_ITEM_ICONS.put(Identifier.of("ore_diamond_small"), Items.DIAMOND_ORE);
        NON_ITEM_ICONS.put(Identifier.of("ore_diorite"), Items.DIORITE);
        NON_ITEM_ICONS.put(Identifier.of("ore_dirt"), Items.DIRT);
        NON_ITEM_ICONS.put(Identifier.of("ore_emerald"), Items.EMERALD_ORE);
        NON_ITEM_ICONS.put(Identifier.of("ore_gold"), Items.GOLD_ORE);
        NON_ITEM_ICONS.put(Identifier.of("ore_gold_buried"), Items.GOLD_ORE);
        NON_ITEM_ICONS.put(Identifier.of("ore_granite"), Items.GRANITE);
        NON_ITEM_ICONS.put(Identifier.of("ore_gravel"), Items.GRAVEL);
        NON_ITEM_ICONS.put(Identifier.of("ore_gravel_nether"), Items.GRAVEL);
        NON_ITEM_ICONS.put(Identifier.of("ore_infested"), Items.INFESTED_STONE);
        NON_ITEM_ICONS.put(Identifier.of("ore_iron"), Items.IRON_ORE);
        NON_ITEM_ICONS.put(Identifier.of("ore_iron_small"), Items.IRON_ORE);
        NON_ITEM_ICONS.put(Identifier.of("ore_lapis"), Items.LAPIS_ORE);
        NON_ITEM_ICONS.put(Identifier.of("ore_lapis_buried"), Items.LAPIS_ORE);
        NON_ITEM_ICONS.put(Identifier.of("ore_magma"), Items.MAGMA_BLOCK);
        NON_ITEM_ICONS.put(Identifier.of("ore_nether_gold"), Items.NETHER_GOLD_ORE);
        NON_ITEM_ICONS.put(Identifier.of("ore_quartz"), Items.NETHER_QUARTZ_ORE);
        NON_ITEM_ICONS.put(Identifier.of("ore_redstone"), Items.REDSTONE_ORE);
        NON_ITEM_ICONS.put(Identifier.of("ore_soul_sand"), Items.SOUL_SAND);
        NON_ITEM_ICONS.put(Identifier.of("ore_tuff"), Items.TUFF);
        NON_ITEM_ICONS.put(Identifier.of("patch_berry_bush"), Items.SWEET_BERRIES);
        NON_ITEM_ICONS.put(Identifier.of("patch_brown_mushroom"), Items.BROWN_MUSHROOM);
        NON_ITEM_ICONS.put(Identifier.of("patch_cactus"), Items.CACTUS);
        NON_ITEM_ICONS.put(Identifier.of("patch_crimson_roots"), Items.CRIMSON_ROOTS);
        NON_ITEM_ICONS.put(Identifier.of("patch_dead_bush"), Items.DEAD_BUSH);
        NON_ITEM_ICONS.put(Identifier.of("patch_fire"), Items.FLINT_AND_STEEL);
        NON_ITEM_ICONS.put(Identifier.of("patch_grass"), Items.SHORT_GRASS);
        NON_ITEM_ICONS.put(Identifier.of("patch_grass_jungle"), Items.SHORT_GRASS);
        NON_ITEM_ICONS.put(Identifier.of("patch_large_fern"), Items.LARGE_FERN);
        NON_ITEM_ICONS.put(Identifier.of("patch_melon"), Items.MELON);
        NON_ITEM_ICONS.put(Identifier.of("patch_pumpkin"), Items.PUMPKIN);
        NON_ITEM_ICONS.put(Identifier.of("patch_red_mushroom"), Items.RED_MUSHROOM);
        NON_ITEM_ICONS.put(Identifier.of("patch_soul_fire"), Items.FLINT_AND_STEEL);
        NON_ITEM_ICONS.put(Identifier.of("patch_sugar_cane"), Items.SUGAR_CANE);
        NON_ITEM_ICONS.put(Identifier.of("patch_sunflower"), Items.SUNFLOWER);
        NON_ITEM_ICONS.put(Identifier.of("patch_taiga_grass"), Items.SHORT_GRASS);
        NON_ITEM_ICONS.put(Identifier.of("patch_tall_grass"), Items.TALL_GRASS);
        NON_ITEM_ICONS.put(Identifier.of("patch_waterlily"), Items.LILY_PAD);
        NON_ITEM_ICONS.put(Identifier.of("pile_hay"), Items.HAY_BLOCK);
        NON_ITEM_ICONS.put(Identifier.of("pile_ice"), Items.ICE);
        NON_ITEM_ICONS.put(Identifier.of("pile_melon"), Items.MELON);
        NON_ITEM_ICONS.put(Identifier.of("pile_pumpkin"), Items.PUMPKIN);
        NON_ITEM_ICONS.put(Identifier.of("pile_snow"), Items.SNOW_BLOCK);
        NON_ITEM_ICONS.put(Identifier.of("pine"), Items.SPRUCE_SAPLING);
        NON_ITEM_ICONS.put(Identifier.of("rooted_azalea_tree"), Items.FLOWERING_AZALEA);
        NON_ITEM_ICONS.put(Identifier.of("sculk_patch_ancient_city"), Items.SCULK);
        NON_ITEM_ICONS.put(Identifier.of("sculk_patch_deep_dark"), Items.SCULK);
        NON_ITEM_ICONS.put(Identifier.of("seagrass_mid"), Items.SEAGRASS);
        NON_ITEM_ICONS.put(Identifier.of("seagrass_short"), Items.SEAGRASS);
        NON_ITEM_ICONS.put(Identifier.of("seagrass_simple"), Items.SEAGRASS);
        NON_ITEM_ICONS.put(Identifier.of("seagrass_slightly_less_short"), Items.SEAGRASS);
        NON_ITEM_ICONS.put(Identifier.of("seagrass_tall"), Items.SEAGRASS);
        NON_ITEM_ICONS.put(Identifier.of("single_piece_of_grass"), Items.SHORT_GRASS);
        NON_ITEM_ICONS.put(Identifier.of("small_basalt_columns"), Items.BASALT);
        NON_ITEM_ICONS.put(Identifier.of("spring_lava_frozen"), Items.LAVA_BUCKET);
        NON_ITEM_ICONS.put(Identifier.of("spring_lava_nether"), Items.LAVA_BUCKET);
        NON_ITEM_ICONS.put(Identifier.of("spring_lava_overworld"), Items.LAVA_BUCKET);
        NON_ITEM_ICONS.put(Identifier.of("spring_nether_closed"), Items.LAVA_BUCKET);
        NON_ITEM_ICONS.put(Identifier.of("spring_nether_open"), Items.LAVA_BUCKET);
        NON_ITEM_ICONS.put(Identifier.of("spring_water"), Items.WATER_BUCKET);
        NON_ITEM_ICONS.put(Identifier.of("spruce"), Items.SPRUCE_SAPLING);
        NON_ITEM_ICONS.put(Identifier.of("super_birch_bees"), Items.BIRCH_SAPLING);
        NON_ITEM_ICONS.put(Identifier.of("super_birch_bees_0002"), Items.BIRCH_SAPLING);
        NON_ITEM_ICONS.put(Identifier.of("swamp_oak"), Items.OAK_SAPLING);
        NON_ITEM_ICONS.put(Identifier.of("tall_mangrove"), Items.MANGROVE_PROPAGULE);
        NON_ITEM_ICONS.put(Identifier.of("trees_birch_and_oak"), Items.OAK_SAPLING);
        NON_ITEM_ICONS.put(Identifier.of("trees_flower_forest"), Items.OAK_SAPLING);
        NON_ITEM_ICONS.put(Identifier.of("trees_grove"), Items.OAK_SAPLING);
        NON_ITEM_ICONS.put(Identifier.of("trees_jungle"), Items.JUNGLE_SAPLING);
        NON_ITEM_ICONS.put(Identifier.of("trees_old_growth_pine_taiga"), Items.SPRUCE_SAPLING);
        NON_ITEM_ICONS.put(Identifier.of("trees_old_growth_spruce_taiga"), Items.SPRUCE_SAPLING);
        NON_ITEM_ICONS.put(Identifier.of("trees_plains"), Items.OAK_SAPLING);
        NON_ITEM_ICONS.put(Identifier.of("trees_savanna"), Items.ACACIA_SAPLING);
        NON_ITEM_ICONS.put(Identifier.of("trees_sparse_jungle"), Items.JUNGLE_SAPLING);
        NON_ITEM_ICONS.put(Identifier.of("trees_taiga"), Items.SPRUCE_SAPLING);
        NON_ITEM_ICONS.put(Identifier.of("trees_water"), Items.OAK_SAPLING);
        NON_ITEM_ICONS.put(Identifier.of("trees_windswept_hills"), Items.ACACIA_SAPLING);
        NON_ITEM_ICONS.put(Identifier.of("twisting_vines_bonemeal"), Items.TWISTING_VINES);
        NON_ITEM_ICONS.put(Identifier.of("underwater_magma"), Items.MAGMA_BLOCK);
        NON_ITEM_ICONS.put(Identifier.of("vines"), Items.VINE);
        NON_ITEM_ICONS.put(Identifier.of("void_start_platform"), Items.STONE);
        NON_ITEM_ICONS.put(Identifier.of("warm_ocean_vegetation"), Items.SEAGRASS);
        NON_ITEM_ICONS.put(Identifier.of("warped_forest_vegetation"), Items.NETHER_SPROUTS);
        NON_ITEM_ICONS.put(Identifier.of("warped_forest_vegetation_bonemeal"), Items.NETHER_SPROUTS);
        NON_ITEM_ICONS.put(Identifier.of("warped_fungus_planted"), Items.WARPED_FUNGUS);
    }

    public static boolean hasSuggestionIcon(Suggestion suggestion) {
        Identifier id = Identifier.tryParse(suggestion.getText());
        return id != null && Registries.ITEM.containsId(id);
    }

    public static ItemStack getSuggestionIcon(Identifier id) {
        return ICONS.computeIfAbsent(id, k -> {
            Item item = NON_ITEM_ICONS.get(k);

            if (item != null) {
                return new ItemStack(item);
            }

            item = Registries.ITEM.getOrEmpty(k).orElse(null);

            if (item != null) {
                return new ItemStack(item);
            }

            return null;
        });
    }

    public static void renderSuggestionIcon(DrawContext context, int x, int y, Suggestion suggestion) {
        Identifier id = Identifier.tryParse(suggestion.getText());

        if (id != null) {
            ItemStack stack = getSuggestionIcon(id);

            if (stack != null) {
                float scale = 2 / 3f;
                MatrixStack matrices = context.getMatrices();
                matrices.push();
                matrices.scale(scale, scale, 1);
                matrices.translate(x / scale + (28 * (1 - scale)), y / scale + (28 * (1 - scale)), 0);
                context.drawItem(stack, 0, 0);
                matrices.pop();
            }
        }
    }

    public static void renderScrollBar(DrawContext context, int x, int y, int w, int h, int index, int size) {
        int i = (int) ((float) index / size * (h - 8));
        context.fill(x, y + i, w + x, y + i + 8, Color.GRAY.getRGB());
    }

    public static Text keyTooltipText(KeyBinding key) {
        return Text.translatable("tooltip.lieutenant.key", Text.translatable(key.getBoundKeyTranslationKey()).getString()).formatted(Formatting.GOLD);
    }

    public static Text blockTooltipText(RegistryKey<Block> target) {
        return blockTooltipText(Registries.BLOCK.getOrEmpty(target).orElse(null));
    }

    public static Text blockTooltipText(Block block) {
        if (block == null) {
            return Text.translatable("tooltip.lieutenant.null_block");
        }

        return Text.translatable(block.getTranslationKey()).setStyle(Style.EMPTY.withColor(block.getDefaultMapColor() == MapColor.CLEAR ? -1 : block.getDefaultMapColor().color));
    }

    public static Text featureTooltipText(RegistryKey<ConfiguredFeature<?, ?>> target) {
        if (target == null) {
            return Text.translatable("tooltip.lieutenant.null_feature");
        }

        return Text.literal(target.getValue().toString());
    }

    public static Text fillTooltipText() {
        return Text.translatable(
                "tooltip.lieutenant.fill"
        );
    }

    public static Text cloneTooltipText() {
        return Text.translatable(
                "tooltip.lieutenant.clone",
                keyTooltipText(MinecraftClient.getInstance().options.useKey)
        );
    }

    public static Text circleTooltipText() {
        return Text.translatable(
                "tooltip.lieutenant.circle"
        );
    }

    public static Text featureTooltipText() {
        return Text.translatable(
                "tooltip.lieutenant.feature"
        );
    }

    public static Text radiusTooltipText() {
        return Text.translatable(
                "tooltip.lieutenant.radius",
                keyTooltipText(LieutenantClient.SELECT_SELF)
        );
    }

    public static Text selectTooltipText() {
        return Text.translatable(
                "tooltip.lieutenant.select",
                keyTooltipText(MinecraftClient.getInstance().options.useKey),
                keyTooltipText(MinecraftClient.getInstance().options.sneakKey)
        );
    }

    public static Text selectReplaceTooltipText() {
        return Text.translatable(
                "tooltip.lieutenant.select_replace",
                keyTooltipText(MinecraftClient.getInstance().options.pickItemKey)
        );
    }

    public static Text permissionTooltipText(int level) {
        return Text.translatable(
                "tooltip.lieutenant.permission",
                level
        );
    }

    @Override
    public void onInitializeClient() {
        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register((context, hit) -> {
            PlayerEntity player = MinecraftClient.getInstance().player;

            if (player != null) {
                ItemStack held = player.getMainHandStack();

                if (held.getItem() instanceof SelectionItem selection && player.raycast(32, 1f, false) instanceof BlockHitResult blockHit) {
                    MatrixStack matrices = Objects.requireNonNull(context.matrixStack());
                    Vec3d cam = context.camera().getPos();

                    matrices.push();
                    matrices.translate(-cam.x, -cam.y, -cam.z);

                    selection.renderSelection(matrices, Objects.requireNonNull(context.consumers()).getBuffer(RenderLayer.getLines()), player, held, blockHit);

                    matrices.pop();

                    return false;
                }
            }

            return true;
        });
        ClientPlayNetworking.registerGlobalReceiver(SetFeatureS2CPacket.ID, (packet, context) -> Lieutenant.FEATURE_ITEM.feature = packet.feature());
        Lieutenant.CIRCLE_ITEM.radius = 3;
        Lieutenant.CLONE_ITEM.type = CloneType.INSERT;
    }
}
