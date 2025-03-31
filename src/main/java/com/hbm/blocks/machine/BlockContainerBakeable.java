package com.hbm.blocks.machine;

import com.google.common.collect.ImmutableMap;
import com.hbm.blocks.ModBlocks;
import com.hbm.items.IDynamicModels;
import com.hbm.render.block.BlockBakeFrame;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;

import java.util.Objects;

public abstract class BlockContainerBakeable extends BlockContainer implements IDynamicModels {
    BlockBakeFrame blockFrame;

    public BlockContainerBakeable(Material material, SoundType soundType, String regName, BlockBakeFrame blockFrame) {
        super(material);
        this.setTranslationKey(regName);
        this.setRegistryName(regName);
        this.setSoundType(soundType);
        this.setHarvestLevel("pickaxe", 0);
        this.blockFrame = blockFrame;
        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    public void bakeModel(ModelBakeEvent event) {

        try {
            IModel baseModel = ModelLoaderRegistry.getModel(new ResourceLocation(blockFrame.getBaseModel()));
            ImmutableMap.Builder<String, String> textureMap = ImmutableMap.builder();

            blockFrame.putTextures(textureMap);
            IModel retexturedModel = baseModel.retexture(textureMap.build());
            IBakedModel bakedModel = retexturedModel.bake(
                    ModelRotation.X0_Y0, DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter()
            );

            ModelResourceLocation modelLocation = new ModelResourceLocation(Objects.requireNonNull(getRegistryName()), "inventory");
            event.getModelRegistry().putObject(modelLocation, bakedModel);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public void registerModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(Objects.requireNonNull(this.getRegistryName()), "inventory"));
    }

    @Override
    public void registerSprite(TextureMap map) {
        blockFrame.registerBlockTextures(map);
    }


}
