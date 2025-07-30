package com.hbm.inventory.recipes;

import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.items.ItemEnums;
import com.hbm.items.ModItems;
import com.hbm.util.Tuple.Pair;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static com.hbm.inventory.OreDictManager.*;

//TODO: clean this shit up
//Alcater: on it

// TODO: Update this
public class PressRecipes {

	public enum StampType {
		NONE,
		FLAT,
		PLATE,
		WIRE,
		CIRCUIT,
		C357,
		C44,
		C9,
		C50,
		PRINTING1,
		PRINTING2,
		PRINTING3,
		PRINTING4,
		PRINTING5,
		PRINTING6,
		PRINTING7,
		PRINTING8
	}

	public static LinkedHashMap<Pair<StampType, ? extends AStack>, ItemStack> pressRecipes = new LinkedHashMap<>();

	public static void addRecipe(StampType stamp, AStack input, ItemStack output){
		if(!input.getStackList().isEmpty())
			pressRecipes.put(new Pair<>(stamp, input), output);
	}

	public static void registerOverrides() {
		addRecipe(StampType.FLAT, new OreDictStack(COAL.dust()), new ItemStack(Items.COAL));
		addRecipe(StampType.FLAT, new OreDictStack("dustQuartz"), new ItemStack(Items.QUARTZ));
		addRecipe(StampType.FLAT, new OreDictStack(NETHERQUARTZ.dust()), new ItemStack(Items.QUARTZ));
		addRecipe(StampType.FLAT, new OreDictStack(LAPIS.dust()), new ItemStack(Items.DYE, 1, 4));
		addRecipe(StampType.FLAT, new OreDictStack(DIAMOND.dust()), new ItemStack(Items.DIAMOND));
		addRecipe(StampType.FLAT, new OreDictStack(EMERALD.dust()), new ItemStack(Items.EMERALD));
		addRecipe(StampType.FLAT, new ComparableStack(ModItems.pellet_coal), new ItemStack(Items.DIAMOND));
		addRecipe(StampType.FLAT, new ComparableStack(ModItems.biomass), new ItemStack(ModItems.biomass_compressed));
		addRecipe(StampType.FLAT, new ComparableStack(ModItems.powder_lignite), new ItemStack(ModItems.briquette_lignite));
		addRecipe(StampType.FLAT, new ComparableStack(ModItems.meteorite_sword_reforged), new ItemStack(ModItems.meteorite_sword_hardened));
		addRecipe(StampType.FLAT, new OreDictStack("fuelCoke"), new ItemStack(ModItems.ingot_graphite));
		addRecipe(StampType.FLAT, new OreDictStack("sugarcane"), new ItemStack(Items.PAPER, 2));
		addRecipe(StampType.FLAT, new ComparableStack(Blocks.LOG, 1, 3), new ItemStack(ModItems.ball_resin, 1));

		addRecipe(StampType.PLATE, new OreDictStack(IRON.ingot()), new ItemStack(ModItems.plate_iron));
		addRecipe(StampType.PLATE, new OreDictStack(GOLD.ingot()), new ItemStack(ModItems.plate_gold));
		addRecipe(StampType.PLATE, new OreDictStack(TI.ingot()), new ItemStack(ModItems.plate_titanium));
		addRecipe(StampType.PLATE, new OreDictStack(AL.ingot()), new ItemStack(ModItems.plate_aluminium));
		addRecipe(StampType.PLATE, new OreDictStack(STEEL.ingot()), new ItemStack(ModItems.plate_steel));
		addRecipe(StampType.PLATE, new OreDictStack(PB.ingot()), new ItemStack(ModItems.plate_lead));
		addRecipe(StampType.PLATE, new OreDictStack(CU.ingot()), new ItemStack(ModItems.plate_copper));
		addRecipe(StampType.PLATE, new OreDictStack("ingotAdvanced"), new ItemStack(ModItems.plate_advanced_alloy));
		addRecipe(StampType.PLATE, new OreDictStack(ALLOY.ingot()), new ItemStack(ModItems.plate_advanced_alloy));
		addRecipe(StampType.PLATE, new OreDictStack(SA326.ingot()), new ItemStack(ModItems.plate_schrabidium));
		addRecipe(StampType.PLATE, new OreDictStack(CMB.ingot()), new ItemStack(ModItems.plate_combine_steel));
		addRecipe(StampType.PLATE, new OreDictStack(BIGMT.ingot()), new ItemStack(ModItems.plate_saturnite));

		for(NTMMaterial mat : Mats.orderedList) {
			if(mat.autogen.contains(MaterialShapes.WIRE) && OreDictionary.doesOreNameExist(MaterialShapes.INGOT.make(mat))) {
				addRecipe(StampType.WIRE, new OreDictStack(MaterialShapes.INGOT.make(mat)), new ItemStack(ModItems.wire_fine, 8, mat.id));
			}
		}

//		addRecipe(StampType.CIRCUIT, new ComparableStack(ModItems.circuit_raw), new ItemStack(ModItems.circuit_aluminium));
//		addRecipe(StampType.CIRCUIT, new ComparableStack(ModItems.circuit_bismuth_raw), new ItemStack(ModItems.circuit_bismuth));
//		addRecipe(StampType.CIRCUIT, new ComparableStack(ModItems.circuit_arsenic_raw), new ItemStack(ModItems.circuit_arsenic));
//		addRecipe(StampType.CIRCUIT, new ComparableStack(ModItems.circuit_tantalium_raw), new ItemStack(ModItems.circuit_tantalium));
		addRecipe(StampType.CIRCUIT, new OreDictStack(SI.billet()),	DictFrame.fromOne(ModItems.circuit, ItemEnums.EnumCircuitType.SILICON));

		}

	@NotNull
    private static StampType getStampType(@NotNull Item stamp){
		if (stamps_flat.contains(stamp)) {
			return StampType.FLAT;
		}
		if (stamps_plate.contains(stamp)) {
			return StampType.PLATE;
		}
		if (stamps_wire.contains(stamp)) {
			return StampType.WIRE;
		}
		if (stamps_circuit.contains(stamp)) {
			return StampType.CIRCUIT;
		}
		if (stamps_357.contains(stamp)) {
			return StampType.C357;
		}
		if (stamps_44.contains(stamp)) {
			return StampType.C44;
		}
		if (stamps_9.contains(stamp)) {
			return StampType.C9;
		}
		if (stamps_50.contains(stamp)) {
			return StampType.C50;
		}
		return StampType.NONE;
	}

	public static List<ItemStack> toStack(List<Item> iList){
		List<ItemStack> i_stamps = new ArrayList<>();
		for(Item i : iList){
			i_stamps.add(new ItemStack(i));
		}
		return i_stamps;
	}

	public static List<ItemStack> getStampList(StampType pType){
		if (pType == StampType.FLAT) {
			return toStack(stamps_flat);
		}
		if (pType == StampType.PLATE) {
			return toStack(stamps_plate);
		}
		if (pType == StampType.WIRE) {
			return toStack(stamps_wire);
		}
		if (pType == StampType.CIRCUIT) {
			return toStack(stamps_circuit);
		}
		if (pType == StampType.C357) {
			return toStack(stamps_357);
		}
		if (pType == StampType.C44) {
			return toStack(stamps_44);
		}
		if (pType == StampType.C9) {
			return toStack(stamps_9);
		}
		if (pType == StampType.C50) {
			return toStack(stamps_50);
		}
		return new ArrayList<>();
	}
	
	@NotNull
	public static ItemStack getPressResult(@NotNull ItemStack input, @NotNull ItemStack stamp) {
		StampType pType = getStampType(stamp.getItem());
		if(pType == StampType.NONE) return ItemStack.EMPTY;

		return getPressOutput(pType, input);
	}

	@NotNull
    private static ItemStack getPressOutput(StampType pType, @NotNull ItemStack input){
		ItemStack outputItem = pressRecipes.get(new Pair<>(pType, new ComparableStack(input.getItem(), 1, input.getItemDamage())));
		if(outputItem != null)
			return outputItem;

		int[] ids = OreDictionary.getOreIDs(new ItemStack(input.getItem(), 1, input.getItemDamage()));
		for(int id : ids) {

			OreDictStack oreStack = new OreDictStack(OreDictionary.getOreName(id));
			outputItem = pressRecipes.get(new Pair<>(pType, oreStack));
			if(outputItem != null)
				return outputItem;
		}
		return ItemStack.EMPTY;
	}

	private static final List<Item> stamps_flat = new ArrayList<>() {
        {
            add(ModItems.stamp_stone_flat);
            add(ModItems.stamp_iron_flat);
            add(ModItems.stamp_steel_flat);
            add(ModItems.stamp_titanium_flat);
            add(ModItems.stamp_obsidian_flat);
            add(ModItems.stamp_desh_flat);
        }
    };

	public static List<Item> stamps_plate = new ArrayList<>() {
        {
            add(ModItems.stamp_stone_plate);
            add(ModItems.stamp_iron_plate);
            add(ModItems.stamp_steel_plate);
            add(ModItems.stamp_titanium_plate);
            add(ModItems.stamp_obsidian_plate);
            add(ModItems.stamp_desh_plate);
        }
    };

	public static List<Item> stamps_wire = new ArrayList<>() {
        {
            add(ModItems.stamp_stone_wire);
            add(ModItems.stamp_iron_wire);
            add(ModItems.stamp_steel_wire);
            add(ModItems.stamp_titanium_wire);
            add(ModItems.stamp_obsidian_wire);
            add(ModItems.stamp_desh_wire);
        }
    };

	public static List<Item> stamps_circuit = new ArrayList<>() {
        {
            add(ModItems.stamp_stone_circuit);
            add(ModItems.stamp_iron_circuit);
            add(ModItems.stamp_steel_circuit);
            add(ModItems.stamp_titanium_circuit);
            add(ModItems.stamp_obsidian_circuit);
            add(ModItems.stamp_desh_circuit);
        }
    };

	private static final List<Item> stamps_357 = new ArrayList<>() {
        {
            add(ModItems.stamp_357);
            add(ModItems.stamp_desh_357);
        }
    };

	private static final List<Item> stamps_44 = new ArrayList<>() {
        {
            add(ModItems.stamp_44);
            add(ModItems.stamp_desh_44);
        }
    };

	private static final List<Item> stamps_9 = new ArrayList<>() {
        {
            add(ModItems.stamp_9);
            add(ModItems.stamp_desh_9);
        }
    };

	private static final List<Item> stamps_50 = new ArrayList<>() {
        {
            add(ModItems.stamp_50);
            add(ModItems.stamp_desh_50);
        }
    };
}
