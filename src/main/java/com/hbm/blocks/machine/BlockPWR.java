package com.hbm.blocks.machine;

import java.util.Random;

import com.hbm.inventory.fluid.FluidType;
//import com.hbm.render.block.ct.CT;
//import com.hbm.render.block.ct.CTStitchReceiver;
//import com.hbm.render.block.ct.IBlockCT;
import com.hbm.tileentity.machine.TileEntityPWRController;

import api.hbm.fluid.IFluidConnector;
//import cpw.mods.fml.relauncher.Side;
//import cpw.mods.fml.relauncher.SideOnly;
import com.typesafe.config.ConfigException;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
//import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
//import net.minecraft.util.IIcon;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;

//MrNorwood: Oh my fucking god fristie,this dogshit should be thrown the fuck out
public class BlockPWR extends BlockContainerBakeable {

    public static final PropertyBool IO_ENABLED = PropertyBool.create("io");


    public BlockPWR(Material mat, String name) {
        super(mat);
        this.setTranslationKey(name);
        this.setRegistryName(name);

        //ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, IO_ENABLED);
    }


    public int getMetaFromState(IBlockState state) {
        return state.getValue(IO_ENABLED) ? 1 : 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(IO_ENABLED, meta != 0);
    }


    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }


    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.AIR;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityBlockPWR();
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {

        TileEntity tile = worldIn.getTileEntity(pos);

        if (tile instanceof TileEntityBlockPWR) {
            TileEntityBlockPWR pwr = (TileEntityBlockPWR) tile;
            worldIn.removeTileEntity(pos);
            if (pwr.block != null) {
                worldIn.setBlockState(pos, state);
                TileEntity controller = worldIn.getTileEntity(pwr.getPos());

                if (controller instanceof TileEntityPWRController) {
                    ((TileEntityPWRController) controller).assembled = false;
                }
            }
        } else {
            worldIn.removeTileEntity(pos);
        }
        super.breakBlock(worldIn, pos, state);
    }

    //MrNorwood: tbh we can get rid of ISidedInventory and IInventory, that shit is slow as fuck compared to capabilities
    public static class TileEntityBlockPWR extends TileEntity implements IFluidConnector, ISidedInventory, ITickable, IInventory {

        public IBlockState block;
        public int coreX;
        public int coreY;
        public int coreZ;

        @Override
        public void update() {

            if (!world.isRemote) {

                if (world.getTotalWorldTime() % 20 == 0 && block != null) {

                    TileEntityPWRController controller = getCore();

                    if (controller != null) {
                        if (!controller.assembled) {
                            this.getBlockType().breakBlock(world, pos, block);
                        }
                    } else if (world.isChunkGeneratedAt(coreX >> 4, coreZ >> 4)) {
                        this.getBlockType().breakBlock(world, pos, block);
                    }
                }
            }
        }

        @Override
        public void readFromNBT(NBTTagCompound nbt) {
            super.readFromNBT(nbt);

            block = Block.getBlockById(nbt.getInteger("block")).getDefaultState();
            if (block != Blocks.AIR) {
                coreX = nbt.getInteger("cX");
                coreY = nbt.getInteger("cY");
                coreZ = nbt.getInteger("cZ");
            } else {
                block = null;
            }
        }

        //negroid tech
        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
            super.writeToNBT(nbt);

            if (block != null) {
                nbt.setInteger("block", Block.getIdFromBlock(block.getBlock()));
                nbt.setInteger("cX", coreX);
                nbt.setInteger("cY", coreY);
                nbt.setInteger("cZ", coreZ);
            }
            return nbt;
        }

        @Override
        public void markDirty() {
            if (this.world != null) {
                BlockPos pos = getPos(); // Get the BlockPos of the TileEntity
                this.world.notifyBlockUpdate(pos, this.world.getBlockState(pos), this.world.getBlockState(pos), 3);
            }
        }

        @Override
        public boolean isUsableByPlayer(EntityPlayer player) {
            return false;
        }

        @Override
        public void openInventory(EntityPlayer player) {

        }

        @Override
        public void closeInventory(EntityPlayer player) {

        }

        public TileEntityPWRController cachedCore;

        protected TileEntityPWRController getCore() {

            if (cachedCore != null && !cachedCore.isInvalid()) return cachedCore;

            if (world.isBlockLoaded(new BlockPos(coreX, coreY, coreZ))) {  // Check if the block is loaded
                TileEntity tile = world.getTileEntity(new BlockPos(coreX, coreY, coreZ));  // Use BlockPos for tile entity retrieval
                if (tile instanceof TileEntityPWRController controller) {
                    cachedCore = controller;
                    return controller;
                }
            }

            return null;
        }

        @Override
        public long transferFluid(FluidType type, int pressure, long fluid) {

            if (this.getBlockMetadata() != 1) return fluid;
            if (block == null) return fluid;
            TileEntityPWRController controller = this.getCore();
            if (controller != null) return controller.transferFluid(type, pressure, fluid);

            return fluid;
        }

        @Override
        public long getDemand(FluidType type, int pressure) {

            if (this.getBlockMetadata() != 1) return 0;
            if (block == null) return 0;
            TileEntityPWRController controller = this.getCore();
            if (controller != null) return controller.getDemand(type, pressure);

            return 0;
        }

        @Override
        public boolean canConnect(FluidType type) {

        }

        @Override
        public int getSizeInventory() {

            if (this.getBlockMetadata() != 1) return 0;
            if (block == null) return 0;
            TileEntityPWRController controller = this.getCore();
            if (controller != null) return controller.inventory.getSlots();

            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {

            if (this.getBlockMetadata() != 1) return null;
            if (block == null) return null;
            TileEntityPWRController controller = this.getCore();
            if (controller != null) return controller.inventory.getStackInSlot(slot);

            return null;
        }

        @Override
        public ItemStack decrStackSize(int slot, int amount) {

            if (this.getBlockMetadata() != 1) return null;
            if (block == null) return null;
            TileEntityPWRController controller = this.getCore();
            if (controller != null) return controller.inventory.extractItem(slot, amount, true);

            return null;
        }

        @Override
        public ItemStack removeStackFromSlot(int index) {
            return null;
        }

        @Override
        public ItemStack getStackInSlotOnClosing(int slot) {

            if (this.getBlockMetadata() != 1) return null;
            if (block == null) return null;
            TileEntityPWRController controller = this.getCore();
            if (controller != null) return controller.inventory.getStackInSlot(slot);

            return null;
        }

        @Override
        public void setInventorySlotContents(int slot, ItemStack stack) {

            if (this.getBlockMetadata() != 1) return;
            if (block == null) return;
            TileEntityPWRController controller = this.getCore();
            if (controller != null) controller.setInventorySlotContents(slot, stack);
        }

        @Override
        public int getInventoryStackLimit() {

            if (this.getBlockMetadata() != 1) return 0;
            if (block == null) return 0;
            TileEntityPWRController controller = this.getCore();
            if (controller != null) return controller.getInventoryStackLimit();

            return 0;
        }

        @Override
        public boolean isUseableByPlayer(EntityPlayer player) {
            return false;
        }

        @Override
        public void openInventory() {
        }

        @Override
        public void closeInventory() {
        }

        @Override
        public String getInventoryName() {
            return "";
        }

        @Override
        public boolean hasCustomInventoryName() {
            return false;
        }

        @Override
        public boolean isItemValidForSlot(int slot, ItemStack stack) {

            if (this.getBlockMetadata() != 1) return false;
            if (block == null) return false;
            TileEntityPWRController controller = this.getCore();
            if (controller != null) return controller.isItemValidForSlot(slot, stack);

            return false;
        }

        @Override
        public int getField(int id) {
            return 0;
        }

        @Override
        public void setField(int id, int value) {

        }

        @Override
        public int getFieldCount() {
            return 0;
        }

        @Override
        public void clear() {

        }

        @Override
        public int[] getAccessibleSlotsFromSide(int side) {

            if (this.getBlockMetadata() != 1) return new int[0];
            if (block == null) return new int[0];
            TileEntityPWRController controller = this.getCore();
            if (controller != null) return controller.getAccessibleSlotsFromSide(side);

            return new int[0];
        }

        @Override
        public boolean canInsertItem(int slot, ItemStack stack, int side) {

            if (this.getBlockMetadata() != 1) return false;
            if (block == null) return false;
            TileEntityPWRController controller = this.getCore();
            if (controller != null) return controller.canInsertItem(slot, stack, side);

            return false;
        }

        @Override
        public boolean canExtractItem(int slot, ItemStack stack, int side) {

            if (this.getBlockMetadata() != 1) return false;
            if (block == null) return false;
            TileEntityPWRController controller = this.getCore();
            if (controller != null) return controller.canExtractItem(slot, stack, side);

            return false;
        }

        public boolean isLoaded = true;

        @Override
        public boolean isLoaded() {
            return isLoaded;
        }

        @Override
        public void onChunkUnload() {
            super.onChunkUnload();
            this.isLoaded = false;
        }

        @Override
        public int[] getSlotsForFace(EnumFacing side) {
            // horror
            if (side == EnumFacing.DOWN) {
                return new int[]{0, 1};  // Return slots 0 and 1 for the DOWN side
            } else if (side == EnumFacing.UP) {
                return new int[]{2};  // Return slot 2 for the UP side
            } else {
                return new int[]{};  // No accessible slots for other sides
            }
        }

        @Override
        public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
            return false;
        }

        @Override
        public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
            return false;
        }

        @Override
        public String getName() {
            return "";
        }

        @Override
        public boolean hasCustomName() {
            return false;
        }
    }
}