package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.Random;

public class BlockAmmoCrate extends Block {

	public BlockAmmoCrate(Material materialIn, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);

		ModBlocks.ALL_BLOCKS.add(this);
	}

	Random rand = new Random();

	@Override
	public void getDrops(NonNullList<ItemStack> ret, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		ret.add(new ItemStack(ModItems.cap_nuka, 12 + rand.nextInt(21)));
        ret.add(new ItemStack(ModItems.syringe_metal_stimpak, 1 + rand.nextInt(3)));

        if(rand.nextInt(10) == 0) ret.add(new ItemStack(ModItems.syringe_metal_super, 2));
	}

}
