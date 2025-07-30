package com.hbm.main;

//FIXME This may have gotten mangled in a merge

import com.google.common.collect.ImmutableList;
import com.hbm.blocks.BlockEnums;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockCrate;
import com.hbm.blocks.generic.BlockResourceStone;
import com.hbm.capability.HbmCapability;
import com.hbm.capability.HbmLivingCapability;
import com.hbm.capability.NTMBatteryCapabilityHandler;
import com.hbm.command.CommandHbm;
import com.hbm.command.CommandPacketInfo;
import com.hbm.command.CommandRadiation;
import com.hbm.config.*;
import com.hbm.creativetabs.*;
import com.hbm.dim.CommandSpaceTP;
import com.hbm.dim.SolarSystem;
import com.hbm.entity.effect.*;
import com.hbm.entity.grenade.*;
import com.hbm.entity.item.EntityFireworks;
import com.hbm.entity.item.EntityMovingItem;
import com.hbm.entity.item.EntityMovingPackage;
import com.hbm.entity.logic.*;
import com.hbm.entity.missile.*;
import com.hbm.entity.mob.*;
import com.hbm.entity.mob.botprime.EntityBOTPrimeBody;
import com.hbm.entity.mob.botprime.EntityBOTPrimeHead;
import com.hbm.entity.particle.*;
import com.hbm.entity.projectile.*;
import com.hbm.entity.siege.SiegeTier;
import com.hbm.explosion.ExplosionNukeGeneric;
import com.hbm.forgefluid.FFPipeNetwork;
import com.hbm.forgefluid.ModForgeFluids;
import com.hbm.handler.*;
import com.hbm.handler.imc.IMCHandler;
import com.hbm.handler.neutron.NeutronHandler;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.hazard.HazardRegistry;
import com.hbm.interfaces.Spaghetti;
import com.hbm.inventory.BedrockOreRegistry;
import com.hbm.inventory.FluidContainerRegistry;
import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.control_panel.ControlEvent;
import com.hbm.inventory.control_panel.ControlRegistry;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.*;
import com.hbm.items.ModItems;
import com.hbm.items.special.ItemDepletedFuel;
import com.hbm.items.weapon.GrenadeDispenserRegistry;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.HbmWorld;
import com.hbm.lib.Library;
import com.hbm.lib.RefStrings;
import com.hbm.packet.PacketDispatcher;
import com.hbm.potion.HbmDetox;
import com.hbm.potion.HbmPotion;
import com.hbm.saveddata.satellites.Satellite;
import com.hbm.tileentity.TileEntityRegistrar;
import com.hbm.tileentity.bomb.*;
import com.hbm.tileentity.machine.rbmk.*;
import com.hbm.util.CrashHelper;
import com.hbm.world.ModBiomes;
import com.hbm.world.PlanetGen;
import com.hbm.world.feature.SchistStratum;
import com.hbm.world.generator.CellularDungeonFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.LoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

@Mod(modid = RefStrings.MODID, version = RefStrings.VERSION, name = RefStrings.NAME)
@Spaghetti("Total cluserfuck")
public class MainRegistry {

    @SidedProxy(clientSide = RefStrings.CLIENTSIDE, serverSide = RefStrings.SERVERSIDE)
    public static ServerProxy proxy;
    @Mod.Instance(RefStrings.MODID)
    public static MainRegistry instance;
    public static Logger logger;
    public static List<FFPipeNetwork> allPipeNetworks = new ArrayList<>();
    // Creative Tabs
    // ingots, nuggets, wires, machine parts
    public static CreativeTabs partsTab = new PartsTab(CreativeTabs.getNextID(), "tabParts");
    // items that belong in machines, fuels, etc
    public static CreativeTabs controlTab = new ControlTab(CreativeTabs.getNextID(), "tabControl");
    // templates, siren tracks
    public static CreativeTabs templateTab = new TemplateTab(CreativeTabs.getNextID(), "tabTemplate");
    // ore and mineral blocks
    public static CreativeTabs resourceTab = new ResourceTab(CreativeTabs.getNextID(), "tabResource");
    // construction blocks
    public static CreativeTabs blockTab = new BlockTab(CreativeTabs.getNextID(), "tabBlocks");
    // machines, structure parts
    public static CreativeTabs machineTab = new MachineTab(CreativeTabs.getNextID(), "tabMachine");
    // bombs
    public static CreativeTabs nukeTab = new NukeTab(CreativeTabs.getNextID(), "tabNuke");
    // missiles, satellites
    public static CreativeTabs missileTab = new MissileTab(CreativeTabs.getNextID(), "tabMissile");
    // turrets, weapons, ammo
    public static CreativeTabs weaponTab = new WeaponTab(CreativeTabs.getNextID(), "tabWeapon");
    // drinks, kits, tools
    public static CreativeTabs consumableTab = new ConsumableTab(CreativeTabs.getNextID(), "tabConsumable");
    public static int generalOverride = 0;
    public static int polaroidID = 1;
    public static int x;
    public static int y;
    public static int z;
    public static long time;
    public static File configDir;
    public static File configHbmDir;
    // Armor Materials
    // Drillgon200: I have no idea what the two strings and the number at the
    // end are.
    public static ArmorMaterial enumArmorMaterialT45 = EnumHelper.addArmorMaterial(RefStrings.MODID + ":T45", RefStrings.MODID + ":T45", 150, new int[]{3, 6, 8, 3}, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
    public static ArmorMaterial aMatBJ = EnumHelper.addArmorMaterial(RefStrings.MODID + ":BLACKJACK", RefStrings.MODID + ":HBM_BLACKJACK", 150, new int[]{3, 6, 8, 3}, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
    public static ArmorMaterial aMatAJR = EnumHelper.addArmorMaterial(RefStrings.MODID + ":T45AJR", RefStrings.MODID + ":T45AJR", 150, new int[]{3, 6, 8, 3}, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
    public static ArmorMaterial aMatSteamsuit = EnumHelper.addArmorMaterial(RefStrings.MODID + ":Steamsuit", RefStrings.MODID + ":Steamsuit", 150, new int[]{3, 6, 8, 3}, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
    public static ArmorMaterial aMatRPA = EnumHelper.addArmorMaterial(RefStrings.MODID + ":RPA", RefStrings.MODID + ":RPA", 150, new int[]{3, 6, 8, 3}, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
    public static ArmorMaterial aMatHEV = EnumHelper.addArmorMaterial(RefStrings.MODID + ":HEV", RefStrings.MODID + ":HEV", 150, new int[]{3, 6, 8, 3}, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
    public static ArmorMaterial enumArmorMaterialHazmat = EnumHelper.addArmorMaterial(RefStrings.MODID + ":HAZMAT", RefStrings.MODID + ":HAZMAT", 60, new int[]{1, 4, 5, 2}, 5, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
    public static ArmorMaterial enumArmorMaterialHazmat2 = EnumHelper.addArmorMaterial(RefStrings.MODID + ":HAZMAT2", RefStrings.MODID + ":HAZMAT2", 60, new int[]{1, 4, 5, 2}, 5, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
    public static ArmorMaterial enumArmorMaterialHazmat3 = EnumHelper.addArmorMaterial(RefStrings.MODID + ":HAZMAT3", RefStrings.MODID + ":HAZMAT3", 60, new int[]{1, 4, 5, 2}, 5, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
    public static ArmorMaterial enumArmorMaterialPaa = EnumHelper.addArmorMaterial(RefStrings.MODID + ":PAA", RefStrings.MODID + ":PAA", 75, new int[]{3, 6, 8, 3}, 25, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
    public static ArmorMaterial enumArmorMaterialSchrabidium = EnumHelper.addArmorMaterial(RefStrings.MODID + ":SCHRABIDIUM", RefStrings.MODID + ":SCHRABIDIUM", 100, new int[]{3, 6, 8, 3}, 50, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
    public static ArmorMaterial enumArmorMaterialEuphemium = EnumHelper.addArmorMaterial(RefStrings.MODID + ":EUPHEMIUM", RefStrings.MODID + ":EUPHEMIUM", 15000000, new int[]{3, 6, 8, 3}, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
    public static ArmorMaterial enumArmorMaterialSteel = EnumHelper.addArmorMaterial(RefStrings.MODID + ":STEEL", RefStrings.MODID + ":STEEL", 20, new int[]{2, 5, 6, 2}, 5, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
    public static ArmorMaterial enumArmorMaterialAlloy = EnumHelper.addArmorMaterial(RefStrings.MODID + ":ALLOY", RefStrings.MODID + ":ALLOY", 40, new int[]{3, 6, 8, 3}, 12, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
    public static ArmorMaterial enumArmorMaterialAusIII = EnumHelper.addArmorMaterial(RefStrings.MODID + ":AUSIII", RefStrings.MODID + ":AUSIII", 375, new int[]{2, 5, 6, 2}, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
    public static ArmorMaterial enumArmorMaterialTitanium = EnumHelper.addArmorMaterial(RefStrings.MODID + ":TITANIUM", RefStrings.MODID + ":TITANIUM", 25, new int[]{3, 6, 8, 3}, 9, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
    public static ArmorMaterial enumArmorMaterialCmb = EnumHelper.addArmorMaterial(RefStrings.MODID + ":CMB", RefStrings.MODID + ":CMB", 60, new int[]{3, 6, 8, 3}, 50, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
    public static ArmorMaterial enumArmorMaterialSecurity = EnumHelper.addArmorMaterial(RefStrings.MODID + ":SECURITY", RefStrings.MODID + ":SECURITY", 100, new int[]{3, 6, 8, 3}, 15, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
    public static ArmorMaterial enumArmorMaterialAsbestos = EnumHelper.addArmorMaterial(RefStrings.MODID + ":ASBESTOS", RefStrings.MODID + ":ASBESTOS", 20, new int[]{1, 3, 4, 1}, 5, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
    public static ArmorMaterial aMatCobalt = EnumHelper.addArmorMaterial(RefStrings.MODID + ":COBALT", RefStrings.MODID + ":COBALT", 70, new int[]{3, 6, 8, 3}, 25, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
    public static ArmorMaterial aMatStarmetal = EnumHelper.addArmorMaterial(RefStrings.MODID + ":STARMETAL", RefStrings.MODID + ":STARMETAL", 150, new int[]{3, 6, 8, 3}, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
    public static ArmorMaterial aMatLiquidator = EnumHelper.addArmorMaterial(RefStrings.MODID + ":LIQUIDATOR", RefStrings.MODID + ":LIQUIDATOR", 750, new int[]{3, 6, 8, 3}, 10, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
    public static ArmorMaterial aMatFau = EnumHelper.addArmorMaterial(RefStrings.MODID + ":DIGAMMA", RefStrings.MODID + ":DIGAMMA", 150, new int[]{3, 8, 6, 3}, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
    public static ArmorMaterial aMatDNS = EnumHelper.addArmorMaterial(RefStrings.MODID + ":DNT_NANO", RefStrings.MODID + ":DNT_NANO", 150, new int[]{3, 8, 6, 3}, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
    // Tool Materials
    public static ToolMaterial enumToolMaterialSchrabidium = EnumHelper.addToolMaterial(RefStrings.MODID + ":SCHRABIDIUM", 4, 10000, 50.0F, 100.0F, 200);
    public static ToolMaterial enumToolMaterialHammer = EnumHelper.addToolMaterial(RefStrings.MODID + ":SCHRABIDIUMHAMMER", 3, 0, 50.0F, 999999996F, 200);
    public static ToolMaterial enumToolMaterialChainsaw = EnumHelper.addToolMaterial(RefStrings.MODID + ":CHAINSAW", 3, 1500, 50.0F, 22.0F, 0);
    public static ToolMaterial enumToolMaterialSteel = EnumHelper.addToolMaterial(RefStrings.MODID + ":STEEL", 2, 500, 7.5F, 2.0F, 10);
    public static ToolMaterial enumToolMaterialTitanium = EnumHelper.addToolMaterial(RefStrings.MODID + ":TITANIUM", 2, 750, 9.0F, 2.5F, 15);
    public static ToolMaterial enumToolMaterialAlloy = EnumHelper.addToolMaterial(RefStrings.MODID + ":ALLOY", 3, 2000, 15.0F, 5.0F, 5);
    public static ToolMaterial enumToolMaterialCmb = EnumHelper.addToolMaterial(RefStrings.MODID + ":CMB", 4, 8500, 40.0F, 55F, 100);
    public static ToolMaterial enumToolMaterialElec = EnumHelper.addToolMaterial(RefStrings.MODID + ":ELEC", 2, 0, 30.0F, 12.0F, 2);
    public static ToolMaterial enumToolMaterialDesh = EnumHelper.addToolMaterial(RefStrings.MODID + ":DESH", 2, 0, 7.5F, 2.0F, 10);
    public static ToolMaterial enumToolMaterialCobalt = EnumHelper.addToolMaterial(RefStrings.MODID + ":COBALT", 4, 750, 9.0F, 2.5F, 15);
    public static ToolMaterial enumToolMaterialSaw = EnumHelper.addToolMaterial(RefStrings.MODID + ":SAW", 2, 750, 2.0F, 3.5F, 25);
    public static ToolMaterial enumToolMaterialBat = EnumHelper.addToolMaterial(RefStrings.MODID + ":BAT", 0, 500, 1.5F, 3F, 25);
    public static ToolMaterial enumToolMaterialBatNail = EnumHelper.addToolMaterial(RefStrings.MODID + ":BATNAIL", 0, 450, 1.0F, 4F, 25);
    public static ToolMaterial enumToolMaterialGolfClub = EnumHelper.addToolMaterial(RefStrings.MODID + ":GOLFCLUB", 1, 1000, 2.0F, 5F, 25);
    public static ToolMaterial enumToolMaterialPipeRusty = EnumHelper.addToolMaterial(RefStrings.MODID + ":PIPERUSTY", 1, 350, 1.5F, 4.5F, 25);
    public static ToolMaterial enumToolMaterialPipeLead = EnumHelper.addToolMaterial(RefStrings.MODID + ":PIPELEAD", 1, 250, 1.5F, 5.5F, 25);
    public static ToolMaterial enumToolMaterialBottleOpener = EnumHelper.addToolMaterial(RefStrings.MODID + ":OPENER", 1, 250, 1.5F, 0.5F, 200);
    public static ToolMaterial enumToolMaterialSledge = EnumHelper.addToolMaterial(RefStrings.MODID + ":SHIMMERSLEDGE", 1, 0, 25.0F, 26F, 200);
    public static ToolMaterial enumToolMaterialMultitool = EnumHelper.addToolMaterial(RefStrings.MODID + ":MULTITOOL", 3, 5000, 25F, 5.5F, 25);
    public static ToolMaterial matMeteorite = EnumHelper.addToolMaterial("HBM_METEORITE", 4, 0, 50F, 0.0F, 200);
    public static ToolMaterial matCrucible = EnumHelper.addToolMaterial("CRUCIBLE", 3, 10000, 50.0F, 100.0F, 200);
    public static ToolMaterial matHS = EnumHelper.addToolMaterial("CRUCIBLE", 3, 10000, 50.0F, 100.0F, 200);
    public static ToolMaterial matHF = EnumHelper.addToolMaterial("CRUCIBLE", 3, 10000, 50.0F, 100.0F, 200);

    static {
        HBMSoundHandler.init();
        FluidRegistry.enableUniversalBucket();
    }

    Random rand = new Random();

    public static void reloadConfig() {
        Configuration config = new Configuration(new File(proxy.getDataDir().getPath() + "/config/hbm/hbm.cfg"));
        config.load();

        GeneralConfig.loadFromConfig(config);
        MachineConfig.loadFromConfig(config);
        BombConfig.loadFromConfig(config);
        RadiationConfig.loadFromConfig(config);
        PotionConfig.loadFromConfig(config);
        ToolConfig.loadFromConfig(config);
        WeaponConfig.loadFromConfig(config);
        MobConfig.loadFromConfig(config);
        SpaceConfig.loadFromConfig(config);
        reloadCompatConfig();
        BedrockOreJsonConfig.init();
        config.save();
    }

    public static void reloadCompatConfig() {
        Configuration config = new Configuration(new File(proxy.getDataDir().getPath() + "/config/hbm/hbm_dimensions.cfg"));
        config.load();
        CompatibilityConfig.loadFromConfig(config);
        config.save();
    }

    @EventHandler //Apparently this is "legacy", well I am not making my own protocol
    public static void initIMC(FMLInterModComms.IMCEvent event) {

        ImmutableList<FMLInterModComms.IMCMessage> inbox = event.getMessages();

        for (FMLInterModComms.IMCMessage message : inbox) {
            IMCHandler handler = IMCHandler.getHandler(message.key);

            if (handler != null) {
                MainRegistry.logger.info("Received IMC of type >" + message.key + "< from " + message.getSender() + "!");
                handler.process(message);
            } else {
                MainRegistry.logger.error("Could not process unknown IMC type \"" + message.key + "\"");
            }
        }
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        CrashHelper.init();

        if (logger == null)
            logger = event.getModLog();

        if (generalOverride > 0 && generalOverride < 19) {
            polaroidID = generalOverride;
        } else {
            polaroidID = rand.nextInt(18) + 1;
            while (polaroidID == 4 || polaroidID == 9)
                polaroidID = rand.nextInt(18) + 1;
        }

        configDir = event.getModConfigurationDirectory();
        configHbmDir = new File(configDir.getAbsolutePath() + File.separatorChar + "hbmConfig");

        if (!configHbmDir.exists()) configHbmDir.mkdir();

        if (SharedMonsterAttributes.MAX_HEALTH.clampValue(Integer.MAX_VALUE) <= 2000) {
            ((RangedAttribute) SharedMonsterAttributes.MAX_HEALTH).maximumValue = Integer.MAX_VALUE;
        }
        proxy.checkGLCaps();
        reloadConfig();

        OreDictManager.registerGroups();
        OreDictManager oreMan = new OreDictManager();

        MinecraftForge.EVENT_BUS.register(oreMan); //OreRegisterEvent

        MinecraftForge.EVENT_BUS.register(new ModEventHandler());
        MinecraftForge.TERRAIN_GEN_BUS.register(new ModEventHandler());
        MinecraftForge.ORE_GEN_BUS.register(new ModEventHandler());
        MinecraftForge.EVENT_BUS.register(new ModEventHandlerImpact());
        MinecraftForge.TERRAIN_GEN_BUS.register(new ModEventHandlerImpact());
        MinecraftForge.EVENT_BUS.register(new PollutionHandler());
        MinecraftForge.EVENT_BUS.register(new NeutronHandler());

        if (event.getSide() == Side.CLIENT) {
            HbmKeybinds keyHandler = new HbmKeybinds();
            MinecraftForge.EVENT_BUS.register(keyHandler);
        }

        PacketDispatcher.registerPackets();

        HbmPotion.init();

        CapabilityManager.INSTANCE.register(HbmLivingCapability.IEntityHbmProps.class, new HbmLivingCapability.EntityHbmPropsStorage(), HbmLivingCapability.EntityHbmProps.FACTORY);
        CapabilityManager.INSTANCE.register(HbmCapability.IHBMData.class, new HbmCapability.HBMDataStorage(), HbmCapability.HBMData.FACTORY);
        Fluids.init();
        ModForgeFluids.init();
        ModItems.preInit();
        ModBlocks.preInit();
        BulletConfigSyncingUtil.loadConfigsForSync();
        CellularDungeonFactory.init();
        Satellite.register();
        HTTPHandler.loadStats();
        MultiblockBBHandler.init();
        ControlEvent.init();
        SiegeTier.registerTiers();
        HazardRegistry.registerItems();
        PotionRecipes.registerPotionRecipes();

        proxy.registerRenderInfo();
        HbmWorld.mainRegistry();
        proxy.preInit(event);
        Library.initSuperusers();

        enumArmorMaterialSchrabidium.setRepairItem(new ItemStack(ModItems.ingot_schrabidium));
        enumArmorMaterialHazmat.setRepairItem(new ItemStack(ModItems.hazmat_cloth));
        enumArmorMaterialHazmat2.setRepairItem(new ItemStack(ModItems.hazmat_cloth_red));
        enumArmorMaterialHazmat3.setRepairItem(new ItemStack(ModItems.hazmat_cloth_grey));
        enumArmorMaterialT45.setRepairItem(new ItemStack(ModItems.plate_titanium));
        aMatBJ.setRepairItem(new ItemStack(ModItems.plate_armor_lunar));
        aMatAJR.setRepairItem(new ItemStack(ModItems.plate_armor_ajr));
        aMatHEV.setRepairItem(new ItemStack(ModItems.plate_armor_hev));
        enumArmorMaterialTitanium.setRepairItem(new ItemStack(ModItems.ingot_titanium));
        enumArmorMaterialSteel.setRepairItem(new ItemStack(ModItems.ingot_steel));
        enumArmorMaterialAlloy.setRepairItem(new ItemStack(ModItems.ingot_advanced_alloy));
        enumArmorMaterialPaa.setRepairItem(new ItemStack(ModItems.plate_paa));
        enumArmorMaterialCmb.setRepairItem(new ItemStack(ModItems.ingot_combine_steel));
        enumArmorMaterialAusIII.setRepairItem(new ItemStack(ModItems.ingot_australium));
        enumArmorMaterialSecurity.setRepairItem(new ItemStack(ModItems.plate_kevlar));
        enumToolMaterialSchrabidium.setRepairItem(new ItemStack(ModItems.ingot_schrabidium));
        enumToolMaterialHammer.setRepairItem(new ItemStack(Item.getItemFromBlock(ModBlocks.block_schrabidium)));
        enumToolMaterialChainsaw.setRepairItem(new ItemStack(ModItems.ingot_steel));
        enumToolMaterialTitanium.setRepairItem(new ItemStack(ModItems.ingot_titanium));
        enumToolMaterialSteel.setRepairItem(new ItemStack(ModItems.ingot_steel));
        enumToolMaterialAlloy.setRepairItem(new ItemStack(ModItems.ingot_advanced_alloy));
        enumToolMaterialCmb.setRepairItem(new ItemStack(ModItems.ingot_combine_steel));
        enumToolMaterialBottleOpener.setRepairItem(new ItemStack(ModItems.plate_steel));
        enumToolMaterialDesh.setRepairItem(new ItemStack(ModItems.ingot_desh));
        enumArmorMaterialAsbestos.setRepairItem(new ItemStack(ModItems.asbestos_cloth));
        matMeteorite.setRepairItem(new ItemStack(ModItems.plate_paa));
        aMatLiquidator.setRepairItem(new ItemStack(ModItems.plate_lead));
        aMatFau.setRepairItem(new ItemStack(ModItems.plate_armor_fau));
        aMatDNS.setRepairItem(new ItemStack(ModItems.plate_armor_dnt));

        TileEntityRegistrar.init();

        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());

        int i = 0;
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_nuke_mk5"), EntityNukeExplosionMK5.class, "entity_nuke_mk5", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_d_smoke_fx"), EntityDSmokeFX.class, "entity_d_smoke_fx", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_fallout_rain"), EntityFalloutRain.class, "entity_fallout_rain", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_fallout_flare"), EntityFalloutUnderGround.class, "entity_fallout_flare", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_effect_torex"), EntityNukeTorex.class, "entity_effect_torex", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_smoke_fx"), EntitySmokeFX.class, "entity_smoke_fx", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_b_smoke_fx"), EntityBSmokeFX.class, "entity_b_smoke_fx", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_shrapnel"), EntityShrapnel.class, "enity_shrapnel", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_s_smoke_fx"), EntitySSmokeFX.class, "entity_s_smoke_fx", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_rubble"), EntityRubble.class, "entity_rubble", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_burning_foeq"), EntityBurningFOEQ.class, "entity_burning_foeq", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_nuke_mk3"), EntityNukeExplosionMK3.class, "entity_nuke_mk3", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_fleija_rainbow"), EntityCloudFleijaRainbow.class, "entity_fleija_rainbow", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_explosive_beam"), EntityExplosiveBeam.class, "entity_explosive_beam", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_tainted_creeper"), EntityTaintedCreeper.class, "entity_tainted_creeper", i++, MainRegistry.instance, 80, 3, true, 0x009CCA, 0x00F761);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_nuclear_creeper"), EntityNuclearCreeper.class, "entity_nuclear_creeper", i++, MainRegistry.instance, 80, 3, true, 0x3D3D3D, 0xCECECE);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_glowing_one"), EntityGlowingOne.class, "entity_glowing_one", i++, MainRegistry.instance, 1000, 1, true, 0x357C2E, 0x4CFF00);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_ntm_radiation_blaze"), EntityRADBeast.class, "entity_ntm_radiation_blaze", i++, MainRegistry.instance, 1000, 1, true, 0x303030, 0x27F000);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_cloud_fleija"), EntityCloudFleija.class, "entity_cloud_fleija", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_bullet"), EntityBullet.class, "entity_bullet", i++, MainRegistry.instance, 250, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_gasflame_fx"), EntityGasFlameFX.class, "entity_gasflame_fx", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_rocket"), EntityRocket.class, "entity_rocket", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_fire"), EntityFire.class, "entity_fire", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_aa_shell"), EntityAAShell.class, "entity_aa_shell", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_artillery_shell"), EntityArtilleryShell.class, "entity_artillery_shell", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_artillery_rocket"), EntityArtilleryRocket.class, "entity_artillery_rocket", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_bomber"), EntityBomber.class, "entity_bomber", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_agent_orange"), EntityOrangeFX.class, "entity_agent_orange", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_pink_cloud_fx"), EntityPinkCloudFX.class, "entity_pink_cloud_fx", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_cloud_fx"), EntityCloudFX.class, "entity_cloud_fx", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_chlorine_fx"), EntityChlorineFX.class, "entity_chlorine_fx", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_stinger"), EntityRocketHoming.class, "entity_stinger", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_t_smoke_fx"), EntityTSmokeFX.class, "entity_t_smoke_fx", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_boxcar"), EntityBoxcar.class, "entity_boxcar", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_zeta"), EntityBombletZeta.class, "entity_zeta", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_emp"), EntityEMP.class, "entity_emp", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_theta"), EntityBombletTheta.class, "entity_theta", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_selena"), EntityBombletSelena.class, "entity_selena", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_black_hole"), EntityBlackHole.class, "entity_black_hole", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_emp_blast"), EntityEMPBlast.class, "entity_emp_blast", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_bullet_mk2"), EntityBulletBase.class, "entity_bullet_mk2", i++, MainRegistry.instance, 250, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_duchessgambit"), EntityDuchessGambit.class, "entity_duchessgambit", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_spark_beam"), EntitySparkBeam.class, "entity_spark_beam", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_mod_beam"), EntityModBeam.class, "entity_mod_beam", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_vortex"), EntityVortex.class, "entity_vortex", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_raging_vortex"), EntityRagingVortex.class, "entity_raging_vortex", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_mini_nuke"), EntityMiniNuke.class, "entity_mini_nuke", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_mini_mirv"), EntityMiniMIRV.class, "entity_mini_mirv", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_balefire"), EntityBalefire.class, "entity_balefire", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_rainbow"), EntityRainbow.class, "entity_rainbow", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_plasma_beam"), EntityPlasmaBeam.class, "entity_plasma_beam", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_ln2"), EntityLN2.class, "entity_ln2", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_combine_ball"), EntityCombineBall.class, "entity_combine_ball", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_discharge"), EntityDischarge.class, "entity_discharge", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_schrab"), EntitySchrab.class, "entity_schrab", i++, MainRegistry.instance, 1000, 1, true);

        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_grenade_generic"), EntityGrenadeGeneric.class, "entity_grenade_generic", i++, MainRegistry.instance, 250, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_grenade_strong"), EntityGrenadeStrong.class, "entity_grenade_strong", i++, MainRegistry.instance, 250, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_grenade_frag"), EntityGrenadeFrag.class, "entity_grenade_frag", i++, MainRegistry.instance, 250, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_grenade_fire"), EntityGrenadeFire.class, "entity_grenade_fire", i++, MainRegistry.instance, 250, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_grenade_cluster"), EntityGrenadeCluster.class, "entity_grenade_cluster", i++, MainRegistry.instance, 250, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_grenade_flare"), EntityGrenadeFlare.class, "entity_grenade_flare", i++, MainRegistry.instance, 250, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_grenade_electric"), EntityGrenadeElectric.class, "entity_grenade_electric", i++, MainRegistry.instance, 250, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_grenade_poison"), EntityGrenadePoison.class, "entity_grenade_poison", i++, MainRegistry.instance, 250, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_grenade_gas"), EntityGrenadeGas.class, "entity_grenade_gas", i++, MainRegistry.instance, 250, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_grenade_schrabidium"), EntityGrenadeSchrabidium.class, "entity_grenade_schrabidium", i++, MainRegistry.instance, 250, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_grenade_pulse"), EntityGrenadePulse.class, "entity_grenade_pulse", i++, MainRegistry.instance, 250, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_grenade_plasma"), EntityGrenadePlasma.class, "entity_grenade_plasma", i++, MainRegistry.instance, 250, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_grenade_tau"), EntityGrenadeTau.class, "entity_grenade_tau", i++, MainRegistry.instance, 250, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_grenade_cloud"), EntityGrenadeCloud.class, "entity_grenade_cloud", i++, MainRegistry.instance, 250, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_grenade_pc"), EntityGrenadePC.class, "entity_grenade_pc", i++, MainRegistry.instance, 250, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_grenade_smart"), EntityGrenadeSmart.class, "entity_grenade_smart", i++, MainRegistry.instance, 250, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_grenade_mirv"), EntityGrenadeMIRV.class, "entity_grenade_mirv", i++, MainRegistry.instance, 250, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_grenade_breach"), EntityGrenadeBreach.class, "entity_grenade_breach", i++, MainRegistry.instance, 250, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_grenade_burst"), EntityGrenadeBurst.class, "entity_grenade_burst", i++, MainRegistry.instance, 250, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_grenade_lemon"), EntityGrenadeLemon.class, "entity_grenade_lemon", i++, MainRegistry.instance, 250, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_grenade_mk2"), EntityGrenadeMk2.class, "entity_grenade_mk2", i++, MainRegistry.instance, 250, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_grenade_aschrab"), EntityGrenadeASchrab.class, "entity_grenade_aschrab", i++, MainRegistry.instance, 250, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_grenade_zomg"), EntityGrenadeZOMG.class, "entity_grenade_zomg", i++, MainRegistry.instance, 250, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_grenade_solinium"), EntityGrenadeSolinium.class, "entity_grenade_solinium", i++, MainRegistry.instance, 250, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_grenade_shrapnel"), EntityGrenadeShrapnel.class, "entity_grenade_shrapnel", i++, MainRegistry.instance, 250, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_grenade_black_hole"), EntityGrenadeBlackHole.class, "entity_grenade_black_hole", i++, MainRegistry.instance, 250, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_grenade_gascan"), EntityGrenadeGascan.class, "entity_grenade_gascan", i++, MainRegistry.instance, 250, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_grenade_nuke"), EntityGrenadeNuke.class, "entity_grenade_nuke", i++, MainRegistry.instance, 250, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_grenade_nuclear"), EntityGrenadeNuclear.class, "entity_grenade_nuclear", i++, MainRegistry.instance, 250, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_grenade_if_generic"), EntityGrenadeIFGeneric.class, "entity_grenade_if_generic", i++, MainRegistry.instance, 250, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_grenade_if_he"), EntityGrenadeIFHE.class, "entity_grenade_if_he", i++, MainRegistry.instance, 250, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_grenade_if_bouncy"), EntityGrenadeIFBouncy.class, "entity_grenade_if_bouncy", i++, MainRegistry.instance, 250, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_grenade_if_sticky"), EntityGrenadeIFSticky.class, "entity_grenade_if_sticky", i++, MainRegistry.instance, 250, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_grenade_if_impact"), EntityGrenadeIFImpact.class, "entity_grenade_if_impact", i++, MainRegistry.instance, 250, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_grenade_if_incendiary"), EntityGrenadeIFIncendiary.class, "entity_grenade_if_incendiary", i++, MainRegistry.instance, 250, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_grenade_if_toxic"), EntityGrenadeIFToxic.class, "entity_grenade_if_toxic", i++, MainRegistry.instance, 250, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_grenade_if_concussion"), EntityGrenadeIFConcussion.class, "entity_grenade_if_concussion", i++, MainRegistry.instance, 250, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_grenade_if_brimstone"), EntityGrenadeIFBrimstone.class, "entity_grenade_if_brimstone", i++, MainRegistry.instance, 250, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_grenade_if_mystery"), EntityGrenadeIFMystery.class, "entity_grenade_if_mystery", i++, MainRegistry.instance, 250, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_grenade_if_spark"), EntityGrenadeIFSpark.class, "entity_grenade_if_spark", i++, MainRegistry.instance, 250, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_grenade_if_hopwire"), EntityGrenadeIFHopwire.class, "entity_grenade_if_hopwire", i++, MainRegistry.instance, 250, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_grenade_if_null"), EntityGrenadeIFNull.class, "entity_grenade_if_null", i++, MainRegistry.instance, 250, 1, true);

        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_missile_generic"), EntityMissileTier1.EntityMissileGeneric.class, "entity_missile_generic", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_missile_incendiary"), EntityMissileTier1.EntityMissileIncendiary.class, "entity_missile_incendiary", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_missile_cluster"), EntityMissileTier1.EntityMissileCluster.class, "entity_missile_cluster", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_missile_bunker_buster"), EntityMissileTier1.EntityMissileBunkerBuster.class, "entity_missile_bunker_buster", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_missile_strong"), EntityMissileTier2.EntityMissileStrong.class, "entity_missile_strong", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_missile_incendiary_strong"), EntityMissileTier2.EntityMissileIncendiaryStrong.class, "entity_missile_incendiary_strong", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_missile_cluster_strong"), EntityMissileTier2.EntityMissileClusterStrong.class, "entity_missile_cluster_strong", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_missile_buster_strong"), EntityMissileTier2.EntityMissileBusterStrong.class, "entity_missile_buster_strong", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_missile_emp_strong"), EntityMissileTier2.EntityMissileEMPStrong.class, "entity_missile_emp_strong", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_missile_burst"), EntityMissileTier3.EntityMissileBurst.class, "entity_missile_burst", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_missile_inferno"), EntityMissileTier3.EntityMissileInferno.class, "entity_missile_inferno", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_missile_rain"), EntityMissileTier3.EntityMissileRain.class, "entity_missile_rain", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_missile_drill"), EntityMissileTier3.EntityMissileDrill.class, "entity_missile_drill", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_missile_n2"), EntityMissileTier4.EntityMissileN2.class, "entity_missile_n2", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_missile_nuclear"), EntityMissileTier4.EntityMissileNuclear.class, "entity_missile_nuclear", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_missile_mirv"), EntityMissileTier4.EntityMissileMirv.class, "entity_missile_mirv", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_missile_endo"), EntityMissileTier3.EntityMissileEndo.class, "entity_missile_endo", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_missile_exo"), EntityMissileTier3.EntityMissileExo.class, "entity_missile_exo", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_missile_doomsday"), EntityMissileTier4.EntityMissileDoomsday.class, "entity_missile_doomsday", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_missile_taint"), EntityMissileTier0.EntityMissileTaint.class, "entity_missile_taint", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_missile_micro"), EntityMissileTier0.EntityMissileMicro.class, "entity_missile_micro", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_missile_bhole"), EntityMissileTier0.EntityMissileBHole.class, "entity_missile_bhole", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_missile_schrab"), EntityMissileTier0.EntityMissileSchrabidium.class, "entity_missile_schrab", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_missile_emp"), EntityMissileTier0.EntityMissileEMP.class, "entity_missile_emp", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_missile_ab"), EntityMissileAntiBallistic.class, "entity_missile_ab", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_missile_stealth"), EntityMissileStealth.class, "entity_missile_stealth", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_carrier"), EntityCarrier.class, "entity_carrier", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_booster"), EntityBooster.class, "entity_booster", i++, MainRegistry.instance, 1000, 1, true);

        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_railgun_pellet"), EntityRailgunBlast.class, "entity_railgun_pellet", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_gas_fx"), EntityGasFX.class, "entity_gas_fx", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_oil_spill"), EntityOilSpill.class, "entity_oil_spill", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_oil_spill_fx"), EntityOilSpillFX.class, "entity_oil_spill_fx", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_clound_solinium"), EntityCloudSolinium.class, "entity_clound_solinium", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_nuke_explosion_advanced"), EntityNukeExplosionPlus.class, "entity_nuke_explosion_advanced", i++, MainRegistry.instance, 250, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_falling_bomb"), EntityFallingNuke.class, "entity_falling_bomb", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_custom_missile"), EntityMissileCustom.class, "entity_custom_missile", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_laser_blast"), EntityDeathBlast.class, "entity_laser_blast", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_miner_rocket"), EntityMinerRocket.class, "entity_miner_rocket", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_meteor"), EntityMeteor.class, "entity_meteor", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_bobmazon"), EntityBobmazon.class, "entity_bobmazon", i++, MainRegistry.instance, 1000, 1, true);
        //Drillgon200: The hunter chopper is messed up and janky and I don't know what do about it. I'd probably have to recode the whole thing, and I don't have time for that.
        //Alcater: I feel that, sigh...
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_hunter_chopper"), EntityHunterChopper.class, "entity_hunter_chopper", i++, MainRegistry.instance, 1000, 1, true, 0x000020, 0x2D2D72);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_chopper_mine"), EntityChopperMine.class, "entity_chopper_mine", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_water_splash"), EntityWaterSplash.class, "entity_water_splash", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_miner_beam"), EntityMinerBeam.class, "entity_miner_beam", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_laser_beam"), EntityLaserBeam.class, "entity_laser_beam", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_mirvlet"), EntityMIRV.class, "entity_mirvlet", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_building"), EntityBuilding.class, "entity_building", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_cyber_crab"), EntityCyberCrab.class, "entity_cyber_crab", i++, MainRegistry.instance, 250, 1, true, 0xAAAAAA, 0x444444);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_taint_crab"), EntityTaintCrab.class, "entity_taint_crab", i++, MainRegistry.instance, 250, 1, true, 0x252324, 0x0082FF);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_tesla_crab"), EntityTeslaCrab.class, "entity_tesla_crab", i++, MainRegistry.instance, 250, 1, true, 0x252324, 0xCF1718);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_tom_the_moonstone"), EntityTom.class, "entity_tom_the_moonstone", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_tom_bust"), EntityTomBlast.class, "entity_tom_bust", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_soyuz_capsule"), EntitySoyuzCapsule.class, "entity_soyuz_capsule", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_soyuz"), EntitySoyuz.class, "entity_soyuz", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_laser"), EntityLaser.class, "entity_laser", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_c_item"), EntityMovingItem.class, "entity_c_item", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_c_package"), EntityMovingPackage.class, "entity_c_package", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_moonstone_blast"), EntityCloudTom.class, "entity_moonstone_blast", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_mask_man"), EntityMaskMan.class, "entity_mask_man", i++, MainRegistry.instance, 1000, 1, true, 0x78786F, 0x3E3E32);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_balls_o_tron"), EntityBOTPrimeHead.class, "entity_balls_o_tron", i++, MainRegistry.instance, 1000, 1, true, 0x434343, 0xA0A0A0);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_balls_o_tron_seg"), EntityBOTPrimeBody.class, "entity_balls_o_tron_seg", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_fucc_a_ducc"), EntityDuck.class, "entity_fucc_a_ducc", i++, MainRegistry.instance, 1000, 1, true, 0xd0d0d0, 0xEED900);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_vortex_beam"), EntityBeamVortex.class, "entity_vortex_beam", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_elder_one"), EntityQuackos.class, "entity_elder_one", i++, MainRegistry.instance, 1000, 1, true, 0xFFFFFF, 0xFFBF00);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_ntm_fbi"), EntityFBI.class, "entity_ntm_fbi", i++, MainRegistry.instance, 1000, 1, true, 0xE79255, 0x1F3849);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_firework_ball"), EntityFireworks.class, "entity_firework_ball", i++, MainRegistry.instance, 1000, 1, true);

        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_rbmk_debris"), EntityRBMKDebris.class, "entity_rbmk_debris", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_sawblade"), EntitySawblade.class, "entity_sawblade", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_spear"), EntitySpear.class, "entity_spear", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_missile_volcano"), EntityMissileTier4.EntityMissileVolcano.class, "entity_missile_volcano", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_ntm_ufo"), EntityUFO.class, "entity_ntm_ufo", i++, MainRegistry.instance, 1000, 1, true, 0x00FFFF, 0x606060);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_digamma_quasar"), EntityQuasar.class, "entity_digamma_quasar", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_mist"), EntityMist.class, "entity_mist", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_cog"), EntityCog.class, "entity_cog", i++, MainRegistry.instance, 1000, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(RefStrings.MODID, "entity_zirnox_debris"), EntityZirnoxDebris.class, "entity_zirnox_debris", i++, MainRegistry.instance, 1000, 1, true);

        ForgeChunkManager.setForcedChunkLoadingCallback(this, new LoadingCallback() {

            @Override
            public void ticketsLoaded(List<Ticket> tickets, World world) {
                for (Ticket ticket : tickets) {

                    if (ticket.getEntity() instanceof IChunkLoader) {
                        ((IChunkLoader) ticket.getEntity()).init(ticket);
                    }
                }
            }
        });

        GrenadeDispenserRegistry.registerDispenserBehaviors();
        TileEntityLaunchPadBase.registerLaunchables();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        ModItems.init();
        proxy.init(event);
        ModBlocks.init();
        HazmatRegistry.registerHazmats();
        ControlRegistry.init();
        OreDictManager.registerOres();
        Fluids.initForgeFluidCompat();
        PacketThreading.init();
        IMCHandler.init();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        ModItems.postInit();
        ModBlocks.postInit();
        ModBiomes.init();
        SolarSystem.init();
        PlanetGen.init();
        BlockCrate.setDrops();
        BedrockOreRegistry.registerBedrockOres();
        CraftingManager.addBedrockOreSmelting();
        ShredderRecipes.registerShredder();
        ShredderRecipes.registerOverrides();
        CentrifugeRecipes.register();
        PressRecipes.registerOverrides();
        ExplosionNukeGeneric.loadSoliniumFromFile();
        CyclotronRecipes.register();
        HadronRecipes.register();
        MagicRecipes.register();
        SILEXRecipes.register();
        GasCentrifugeRecipes.register();
        NTMToolHandler.register();
        SerializableRecipe.registerAllHandlers();
        SerializableRecipe.initialize();
        AnvilRecipes.register();
        WasteDrumRecipes.registerRecipes();
        ItemDepletedFuel.registerPoolRecepies();
        RefineryRecipes.registerRefinery();
        FluidContainerRegistry.register();
        TileEntityNukeCustom.registerBombItems();
        ArmorUtil.register();
        RBMKFuelRecipes.registerRecipes();
        DFCRecipes.register();
        SAFERecipes.registerRecipes();
        StorageDrumRecipes.registerRecipes();
        NuclearTransmutationRecipes.registerRecipes();
        EngineRecipes.registerEngineRecipes();
        FluidCombustionRecipes.registerFluidCombustionRecipes();
        HbmDetox.init();
        NTMBatteryCapabilityHandler.initialize();

        //Drillgon200: expand the max entity radius for the hunter chopper
        if (World.MAX_ENTITY_RADIUS < 5)
            World.MAX_ENTITY_RADIUS = 5;
        MinecraftForge.EVENT_BUS.register(new SchistStratum(ModBlocks.stone_gneiss.getDefaultState(), 0.01D, 5, 8, 30)); //DecorateBiomeEvent.Pre
        MinecraftForge.EVENT_BUS.register(new SchistStratum(ModBlocks.stone_resource.getDefaultState().withProperty(BlockResourceStone.META, BlockEnums.EnumStoneType.HEMATITE.ordinal()), 0.02D, 5.5, 5, 45)); //DecorateBiomeEvent.Pre

//        NTMCraftTweaker.applyPostInitActions();
        if (event.getSide() == Side.CLIENT) {
            BedrockOreRegistry.registerOreColors();
            ModForgeFluids.registerFluidColors();
        }
        proxy.postInit(event);
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent evt) {
        RBMKDials.createDials(evt.getServer().getEntityWorld());
        evt.registerServerCommand(new CommandRadiation());
        evt.registerServerCommand(new CommandHbm());
        evt.registerServerCommand(new CommandSpaceTP());
        evt.registerServerCommand(new CommandPacketInfo());
        AdvancementManager.init(evt.getServer());
        //MUST be initialized AFTER achievements!!
        BobmazonOfferFactory.reset();
        BobmazonOfferFactory.init();
    }
}
