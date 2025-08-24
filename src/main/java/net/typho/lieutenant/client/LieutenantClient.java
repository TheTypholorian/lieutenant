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
