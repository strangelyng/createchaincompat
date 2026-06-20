package net.strangelyng.createchaincompat.mixin.create.chains;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorRenderer;
import com.simibubi.create.foundation.render.RenderTypes;
import dev.engine_room.flywheel.lib.transform.PoseTransformStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.strangelyng.createchaincompat.ChainConveyorInterface;
import net.strangelyng.createchaincompat.datamaps.ChainTexData;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.*;

import static net.strangelyng.createchaincompat.datamaps.ChainDataMap.CHAIN_TEX_DATA;

@Mixin(value = ChainConveyorRenderer.class, remap = false)
public abstract class ChainConveyorRendererMixin {

    // I suspect the caching is being done in the wrong order atm...
    @Unique
    private static final Map<Item, ResourceLocation> CHAIN_RS = new HashMap<>();

    @Shadow
    public static final ResourceLocation CHAIN_LOCATION = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/block/chain.png");

    @Shadow
    private static void renderPart(PoseStack pPoseStack, VertexConsumer pConsumer, float pMaxY, float pX0, float pZ0,
                                   float pX1, float pZ1, float pX2, float pZ2, float pX3, float pZ3, float pMinU, float pMaxU, float pMinV,
                                   float pMaxV, int light1, int light2, boolean far) {}

    @Shadow
    private static void renderQuad(Matrix4f pPose, PoseStack.Pose pNormal, VertexConsumer pConsumer, float pMinY, float pMaxY,
                                   float pMinX, float pMinZ, float pMaxX, float pMaxZ, float pMinU, float pMaxU, float pMinV, float pMaxV,
                                   int light1, int light2) {}

    // TODO: READ FROM CONFIG INSTEAD OF DATAMAP
    @Inject(method = "renderChains", at = @At(
            value = "INVOKE", target = "Lcom/simibubi/create/content/kinetics/chainConveyor/ChainConveyorRenderer;renderChain(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;FFIIZ)V"),
            locals = LocalCapture.CAPTURE_FAILSOFT)
    void chaincompat$beforeRenderChain(ChainConveyorBlockEntity be, PoseStack ms, MultiBufferSource buffer,
                                       int light, int overlay, CallbackInfo ci, float time, float animation,
                                       Iterator var8, BlockPos blockPos, ChainConveyorBlockEntity.ConnectionStats stats,
                                       Vec3 diff, double yaw, double pitch, Level level, BlockPos tilePos, Vec3 startOffset,
                                       PoseTransformStack chain, int light1, int light2, boolean far) {
        Map<BlockPos, ItemLike> chainMap = ((ChainConveyorInterface)be).chaincompat$getConnectionsChain();
        ItemLike chainItem = chainMap.get(blockPos);
        Item item = chainItem != null ? chainItem.asItem() : Items.CHAIN;

        Holder<Item> holder = item.builtInRegistryHolder();

        ChainTexData texData = holder.getData(CHAIN_TEX_DATA);


        if (texData != null) {
            if (texData.model().isPresent()) {
                ModelResourceLocation chainModel = ModelResourceLocation.standalone(ResourceLocation.parse(texData.model().get()));

                chaincompat$renderCustomModelChain(ms, buffer, animation, stats.chainLength(), light, chainModel);
            } else if (texData.texture().isPresent()) {
                ResourceLocation chainTexture = ResourceLocation.parse(texData.texture().get());
                CHAIN_RS.put(item, chainTexture);

                chaincompat$renderChain(ms, buffer, animation, stats.chainLength(), light1, light2, far, chainTexture);
            }
        } else {
            if (item instanceof BlockItem blockItem) {
                ResourceLocation chainTexture = CHAIN_RS.get(blockItem);

                if (chainTexture == null) {
                    Block block = blockItem.getBlock();

                    ResourceLocation rl = level.registryAccess().registryOrThrow(Registries.BLOCK).getKey(block);
                    if (rl == null) return;

                    BakedModel chainModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(block.defaultBlockState());
                    ResourceLocation chainLoc = chainModel.getParticleIcon(ModelData.EMPTY).contents().name();

                    chainTexture = ResourceLocation.tryBuild(chainLoc.getNamespace(), "textures/"+chainLoc.getPath()+".png");

                    CHAIN_RS.put(blockItem, chainTexture);
                }
                chaincompat$renderChain(ms, buffer, animation, stats.chainLength(), light1, light2, far, chainTexture);
            } else {
                chaincompat$renderChain(ms, buffer, animation, stats.chainLength(), light1, light2, far, CHAIN_LOCATION);
            }
        }
    }

    @Redirect(method = "renderChains", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/kinetics/chainConveyor/ChainConveyorRenderer;renderChain(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;FFIIZ)V"))
    void chaincompat$cancelRenderChain(PoseStack ms, MultiBufferSource buffer, float animation, float length, int light1, int light2, boolean far) {

    }

    @Unique
    private static void chaincompat$renderChain(PoseStack ms, MultiBufferSource buffer, float animation, float length, int light1,
                                                int light2, boolean far, ResourceLocation chainTexture) {
        float radius = far ? 1f / 16f : 1.5f / 16f;
        float minV = far ? 0 : animation;
        float maxV = far ? 1 / 16f : length + minV;
        float minU = far ? 3 / 16f : 0;
        float maxU = far ? 4 / 16f : 3 / 16f;

        ms.pushPose();
        ms.translate(0.5D, 0.0D, 0.5D);

        VertexConsumer vc = buffer.getBuffer(RenderTypes.chain(chainTexture));
        renderPart(ms, vc, length, 0.0F, radius, radius, 0.0F, -radius, 0.0F, 0.0F, -radius, minU, maxU, minV, maxV,
                light1, light2, far);

        ms.popPose();
    }

    @Unique
    private static void chaincompat$renderCustomModelChain(PoseStack ms, MultiBufferSource buffer, float animation, float length, int light, ModelResourceLocation chainModel) {

        // chain has lots of ugly clipping at angles blech
        for (float transform = -1; transform < length + 1 ; transform += 1.0f) {
            float currentPos = transform + animation;
            if (currentPos < -1 || currentPos > length) {
                continue;
            }

            ms.pushPose();
            ms.translate(0, currentPos, 0);

            // TODO: CACHING? STORE ModelResourceLocation and Baked model in map?
            ModelBlockRenderer blockRenderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();

            BakedModel bakedModel = Minecraft.getInstance().getModelManager().getModel(chainModel);

            chaincompat$renderPart(ms, buffer.getBuffer(RenderType.CUTOUT_MIPPED), bakedModel, light, animation);

            // Possible to get quads, vertices, etc. from Json/Baked Model?
            // Get shape using those, then scroll UVs, like the original chain render method?
//            blockRenderer.renderModel(
//                    ms.last(),
//                    buffer.getBuffer(RenderType.CUTOUT_MIPPED),
//                    null,
//                    bakedModel,
//                    0.0f,
//                    0.0f,
//                    0.0f,
//                    light,
//                    OverlayTexture.NO_OVERLAY,
//                    ModelData.EMPTY,
//                    RenderType.CUTOUT_MIPPED
//            );

            /*
             * BLOCK-BASED IMPLEMENTATION
             *
             * SuperByteBuffer linkBuffer = CachedBuffers.block(BuiltInRegistries.BLOCK.get(chainModel).defaultBlockState());
             *
             * linkBuffer
             *        .light(light)
             *        .renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));
             */

            ms.popPose();
        }
    }

    @Unique
    private static void chaincompat$renderPart(PoseStack pPoseStack, VertexConsumer pConsumer, BakedModel bakedModel, int light, float animation) {
        PoseStack.Pose pose = pPoseStack.last();

        RandomSource random = RandomSource.create();

        List<BakedQuad> bakedQuads = bakedModel.getQuads(null, null, random);

        // This code appears to work... now can UVs be shift from it?

        for (BakedQuad bakedQuad : bakedQuads) {
            pConsumer.putBulkData(pose, bakedQuad, 1.0f, 1.0f, 1.0f, 1.0f, light, OverlayTexture.NO_OVERLAY);
        }
    }
}