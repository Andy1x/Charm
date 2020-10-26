package svenhjol.charm.handler;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DyeColor;
import net.minecraft.util.ResourceLocation;
import svenhjol.charm.Charm;
import svenhjol.charm.mixin.accessor.BufferBuilderStorageAccessor;
import svenhjol.charm.mixin.accessor.MinecraftClientAccessor;
import svenhjol.charm.mixin.accessor.RenderPhaseAccessor;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

public class ColoredGlintHandler {
    public static final String GLINT_TAG = "charm_glint";

    public static Map<DyeColor, ResourceLocation> TEXTURES = new HashMap<>();
    public static Map<DyeColor, RenderLayer> GLINT = new HashMap<>();
    public static Map<DyeColor, RenderLayer> DIRECT_GLINT = new HashMap<>();
    public static Map<DyeColor, RenderLayer> ENTITY_GLINT = new HashMap<>();
    public static Map<DyeColor, RenderLayer> DIRECT_ENTITY_GLINT = new HashMap<>();

    private static boolean hasInit = false;
    private static final boolean changeDefaultGlintColor = false;

    public static void init() {
        if (hasInit)
            return;

        for (DyeColor dyeColor : DyeColor.values()) {
            TEXTURES.put(dyeColor, new ResourceLocation(Charm.MOD_ID, "textures/misc/" + dyeColor.getName() + "_glint.png"));

            GLINT.put(dyeColor, createGlint(dyeColor, TEXTURES.get(dyeColor)));
            DIRECT_GLINT.put(dyeColor, createDirectGlint(dyeColor, TEXTURES.get(dyeColor)));
            ENTITY_GLINT.put(dyeColor, createEntityGlint(dyeColor, TEXTURES.get(dyeColor)));
            DIRECT_ENTITY_GLINT.put(dyeColor, createDirectEntityGlint(dyeColor, TEXTURES.get(dyeColor)));
        }

        hasInit = true;
    }

    public static DyeColor getDefaultClintColor() {
        return DyeColor.PURPLE;
    }

    public static RenderLayer createGlint(DyeColor dyeColor, ResourceLocation texture) {
        RenderLayer renderLayer = RenderLayer.of("glint_" + dyeColor.getName(), VertexFormats.POSITION_TEXTURE, 7, 256, RenderLayer.MultiPhaseParameters.builder()
            .texture(new RenderPhase.Texture(texture, true, false))
            .writeMaskState(RenderPhaseAccessor.getColorMask())
            .cull(RenderPhaseAccessor.getDisableCulling())
            .depthTest(RenderPhaseAccessor.getEqualDepthTest())
            .transparency(RenderPhaseAccessor.getGlintTransparency())
            .texturing(RenderPhaseAccessor.getGlintTexturing())
            .build(false));

        getEntityBuilders().put(renderLayer, new BufferBuilder(renderLayer.getExpectedBufferSize()));
        return renderLayer;
    }

    public static RenderLayer createDirectGlint(DyeColor dyeColor, ResourceLocation texture) {
        RenderLayer renderLayer = RenderLayer.of("glint_direct_" + dyeColor.getName(), VertexFormats.POSITION_TEXTURE, 7, 256, RenderLayer.MultiPhaseParameters.builder()
            .texture(new RenderPhase.Texture(texture, true, false))
            .writeMaskState(RenderPhaseAccessor.getColorMask())
            .cull(RenderPhaseAccessor.getDisableCulling())
            .depthTest(RenderPhaseAccessor.getEqualDepthTest())
            .transparency(RenderPhaseAccessor.getGlintTransparency())
            .texturing(RenderPhaseAccessor.getGlintTexturing())
            .build(false));

        getEntityBuilders().put(renderLayer, new BufferBuilder(renderLayer.getExpectedBufferSize()));
        return renderLayer;
    }

    public static RenderLayer createEntityGlint(DyeColor dyeColor, ResourceLocation texture) {
        RenderLayer renderLayer = RenderLayer.of("entity_glint_" + dyeColor.getName(), VertexFormats.POSITION_TEXTURE, 7, 256, RenderLayer.MultiPhaseParameters.builder()
            .texture(new RenderPhase.Texture(texture, true, false))
            .writeMaskState(RenderPhaseAccessor.getColorMask())
            .cull(RenderPhaseAccessor.getDisableCulling())
            .depthTest(RenderPhaseAccessor.getEqualDepthTest())
            .transparency(RenderPhaseAccessor.getGlintTransparency())
            .texturing(RenderPhaseAccessor.getEntityGlintTexturing())
            .target(RenderPhaseAccessor.getItemTarget())
            .build(false));

        getEntityBuilders().put(renderLayer, new BufferBuilder(renderLayer.getExpectedBufferSize()));
        return renderLayer;
    }

    public static RenderLayer createDirectEntityGlint(DyeColor dyeColor, ResourceLocation texture) {
        RenderLayer renderLayer = RenderLayer.of("entity_glint_direct_" + dyeColor.getName(), VertexFormats.POSITION_TEXTURE, 7, 256, RenderLayer.MultiPhaseParameters.builder()
            .texture(new RenderPhase.Texture(texture, true, false))
            .writeMaskState(RenderPhaseAccessor.getColorMask())
            .cull(RenderPhaseAccessor.getDisableCulling())
            .depthTest(RenderPhaseAccessor.getEqualDepthTest())
            .transparency(RenderPhaseAccessor.getGlintTransparency())
            .texturing(RenderPhaseAccessor.getEntityGlintTexturing())
            .target(RenderPhaseAccessor.getItemTarget())
            .build(false));

        getEntityBuilders().put(renderLayer, new BufferBuilder(renderLayer.getExpectedBufferSize()));
        return renderLayer;
    }

    public static VertexConsumer getDirectItemGlintConsumer(VertexConsumerProvider provider, RenderLayer layer, boolean solid, boolean glint, @Nullable ItemStack stack) {
        RenderLayer renderDirectGlint = RenderLayer.getDirectGlint();
        RenderLayer renderDirectEntityGlint = RenderLayer.getDirectEntityGlint();

        if (changeDefaultGlintColor) {
            renderDirectGlint = DIRECT_GLINT.get(getDefaultClintColor());
            renderDirectEntityGlint = DIRECT_ENTITY_GLINT.get(getDefaultClintColor());
        }

        if (stack != null && stack.hasTag()) {
            CompoundNBT tag = stack.getTag();
            if (tag != null) {
                if (tag.contains(GLINT_TAG)) {
                    DyeColor dyeColor = DyeColor.byName(tag.getString(GLINT_TAG), DyeColor.PURPLE);
                    renderDirectGlint = DIRECT_GLINT.get(dyeColor);
                    renderDirectEntityGlint = DIRECT_ENTITY_GLINT.get(dyeColor);
                }
            }
        }

        return glint ? VertexConsumers.dual(provider.getBuffer(solid ? renderDirectGlint : renderDirectEntityGlint), provider.getBuffer(layer)) : provider.getBuffer(layer);
    }

    public static VertexConsumer getItemGlintConsumer(VertexConsumerProvider vertexConsumers, RenderLayer layer, boolean solid, boolean glint, @Nullable ItemStack stack) {
        if (glint) {
            RenderLayer renderGlint = RenderLayer.getGlint();
            RenderLayer renderEntityGlint = RenderLayer.getEntityGlint();

            if (changeDefaultGlintColor) {
                renderGlint = GLINT.get(getDefaultClintColor());
                renderEntityGlint = ENTITY_GLINT.get(getDefaultClintColor());
            }

            if (stack != null && stack.hasTag()) {
                CompoundNBT tag = stack.getTag();
                if (tag != null) {
                    if (tag.contains(GLINT_TAG)) {
                        DyeColor dyeColor = DyeColor.byName(tag.getString(GLINT_TAG), DyeColor.PURPLE);
                        renderGlint = GLINT.get(dyeColor);
                        renderEntityGlint = ENTITY_GLINT.get(dyeColor);
                    }
                }
            }

            return MinecraftClient.isFabulousGraphicsOrBetter() && layer == TexturedRenderLayers.getItemEntityTranslucentCull() ? VertexConsumers.dual(vertexConsumers.getBuffer(RenderLayer.method_30676()), vertexConsumers.getBuffer(layer)) : VertexConsumers.dual(vertexConsumers.getBuffer(solid ? renderGlint : renderEntityGlint), vertexConsumers.getBuffer(layer));
        } else {
            return vertexConsumers.getBuffer(layer);
        }
    }

    private static SortedMap<RenderLayer, BufferBuilder> getEntityBuilders() {
        BufferBuilderStorage bufferBuilders = ((MinecraftClientAccessor) MinecraftClient.getInstance()).getBufferBuilders();
        return ((BufferBuilderStorageAccessor)bufferBuilders).getEntityBuilders();
    }
}
