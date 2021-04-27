package com.talhanation.smallships.upgrades;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.datafixers.util.Pair;
import java.util.List;

import com.talhanation.smallships.entities.UpgradeAbleEntity;
import com.talhanation.smallships.init.ModEntityTypes;
import com.talhanation.smallships.util.MathUtil;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.BannerTileEntityRenderer;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BannerItem;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.tileentity.BannerTileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

public class BannerModel {
    private static final BannerTileEntity BANNER_TE = new BannerTileEntity();

    public static void renderBanner(BannerUpgrade bannerUpgrade, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, ItemStack banner, int packedLight, ModelRenderer modelRenderer) {
        UpgradeAbleEntity planeEntity = bannerUpgrade.getPlaneEntity();
        if (!banner.isEmpty()) {
            matrixStackIn.push();
            EntityType<?> entityType = planeEntity.getType();
            if (entityType == ModEntityTypes.COG_ENTITY.get()) {
                matrixStackIn.rotate(Vector3f.YP.rotationDegrees(90.0F));
                matrixStackIn.translate(-3.0D, -0.5D, 0.0D);
            } else {
                matrixStackIn.rotate(Vector3f.XP.rotationDegrees(90.0F));
                matrixStackIn.rotate(Vector3f.YP.rotationDegrees(90.0F));
                matrixStackIn.translate(0.7D, 2.35D, 0.05D);
                if (entityType == ModEntityTypes.BRIGG_ENTITY.get())
                    matrixStackIn.translate(0.0D, 1.1D, 0.0D);
            }
            matrixStackIn.scale(0.6F, 0.6F, 0.6F);
            BannerItem item = (BannerItem)banner.getItem();
            BANNER_TE.loadFromItemStack(banner, item.getColor());
            float f2 = partialTicks + planeEntity.ticksExisted;
            float r = 0.05F * MathHelper.cos(f2 / 5.0F) * 180.0F;
            r += bannerUpgrade.prevRotation - MathUtil.lerpAngle(partialTicks, planeEntity.prevRotationYaw, planeEntity.rotationYaw);
            r = (float) (r + MathUtil.lerpAngle(partialTicks, MathUtil.wrapSubtractDegrees(bannerUpgrade.rotation, bannerUpgrade.prevRotation), 0.0f));
            matrixStackIn.rotate(Vector3f.XP.rotationDegrees(r));
            List<Pair<BannerPattern, DyeColor>> list = BANNER_TE.getPatternList();
            BannerTileEntityRenderer.func_230180_a_(matrixStackIn, bufferIn, packedLight, OverlayTexture.NO_OVERLAY, modelRenderer, ModelBakery.LOCATION_BANNER_BASE, true, list);
            matrixStackIn.pop();
        }
    }
}
