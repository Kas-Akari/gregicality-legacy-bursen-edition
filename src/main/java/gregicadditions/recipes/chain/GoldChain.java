package gregicadditions.recipes.chain;

import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.unification.OreDictUnifier;

import static gregicadditions.GAMaterials.*;
import static gregicadditions.recipes.GARecipeMaps.CHEMICAL_DEHYDRATOR_RECIPES;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;

public class GoldChain {

    public static void init() {

        /* Gold Chain Attempt #5
         *
         * A rework of the already existing (and honestly quite accurate despite the flaws) gold processing chain,
         * trying to preserve it as much as possible while replicating real processes and chemistry.
         *
         * The chain features a total of 5 routes, each with increasing complexity, machines required and gold yield.
         * All elements are fully conserved and recycled with the exception of gold, which varies from one to another 
         * to incentive the pursue of longer routes.
         *
         * ROUTES:
         * - Route 1: Direct smelting of 1 Precious Metal Ingot to 1 gold nugget - Yield: 1 ngt per 1 PM ingot
         * - Route 2: Gold Alloy centrifugation to 1 gold nugget each - Yield: 4 ngt per 1 PM ingot
         * - Route 3: Gold Leach electrolysis to 2 gold nuggets each - Yield: 8 ngt per 1 PM ingot
         * - Route 4: Chloroauric Acid decomposition to 6 gold nuggets each - Yield: 12 ngt per 1 PM ingot
         * - Route 5: Chloroauric Acid electrolysis to 9 gold nuggets each - Yield: 18 ngt per 1 PM ingot
         *
         * In adittion to the gold, routes 2 to 5 also allow the recovery of all the copper invested. Routes 4 and 5 also
         * yield adittional metals as byproduct.
         *
         * For reaction purposes, the chemical formulas and units/mol involved are:
         * - Precious Metal Ingot: Au2? - 1 unit/mol
         * - Gold Alloy: Cu3Au2? - 4 units/mol 
         * - Gold Leach: Cu3Au2(OH)3? - 4 units/mol
         * - Chloroauric Acid: HAuCl4·3H2O - 1 unit/mol
         * - Copper Leach: Cu3? - 4 unit/mol
         */

        // ROUTES and REACTIONS ==============================================================================================
        // ROUTE 1
        // Step 1.1 - Smelting
        // 1 Precious Metal Ingot -> 1 Gold Nugget
        ModHandler.addSmeltingRecipe(OreDictUnifier.get(ingot, PreciousMetal), OreDictUnifier.get(nugget, Gold));

        // ROUTE 2
        // Step 2.1 - Gold alloy with Copper - Different variants for ingot and dust form for both
        // Au2? + 3Cu -> Cu3Au2?
        ALLOY_SMELTER_RECIPES.recipeBuilder().EUt(30).duration(100)
                .input(dust, PreciousMetal)
                .input(dust, Copper, 3)
                .output(ingot, GoldAlloy, 4)
                .buildAndRegister();

        ALLOY_SMELTER_RECIPES.recipeBuilder().EUt(30).duration(100)
                .input(ingot, PreciousMetal)
                .input(dust, Copper, 3)
                .output(ingot, GoldAlloy, 4)
                .buildAndRegister();

        ALLOY_SMELTER_RECIPES.recipeBuilder().EUt(30).duration(100)
                .input(dust, PreciousMetal)
                .input(ingot, Copper, 3)
                .output(ingot, GoldAlloy, 4)
                .buildAndRegister();

        ALLOY_SMELTER_RECIPES.recipeBuilder().EUt(30).duration(100)
                .input(ingot, PreciousMetal)
                .input(ingot, Copper, 3)
                .output(ingot, GoldAlloy, 4)
                .buildAndRegister();

        // Step 2.2 - Gold Alloy centrifugation
        // Cu3Au2? -> 3Cu + 2 Au (4 ngt)
        CENTRIFUGE_RECIPES.recipeBuilder()
                .input(dust, GoldAlloy, 4)
                .output(dust, Copper, 3)
                .output(dustTiny, Gold, 4)
                .duration(500)
                .EUt(30)
                .buildAndRegister();

        // ROUTE 3
        // Step 3.1 - Gold oxidation to gold hydroxide Au(OH)3
        // Cu3Au? + 6 HNO3 -> Cu3Au2(OH)3? + 6 NO2
        CHEMICAL_RECIPES.recipeBuilder().duration(80)
                .input(ingot, GoldAlloy, 4)
                .fluidInputs(NitricAcid.getFluid(6000))
                .outputs(GoldLeach.getItemStack(4))
                .fluidOutputs(NitrogenDioxide.getFluid(6000))
                .buildAndRegister();

        // Step 3.2 - Gold hydroxide neutralization
        // Cu3Au2(OH)3? + 8 HCl -> 2 HAuCl4·3H2O + Cu3?
        CHEMICAL_RECIPES.recipeBuilder().duration(80)
                .inputs(GoldLeach.getItemStack(4))
                .fluidInputs(HydrochloricAcid.getFluid(8000))
                .outputs(CopperLeach.getItemStack(4))
                .fluidOutputs(ChloroauricAcid.getFluid(2000))
                .buildAndRegister();

        // Step 3.3 - Gold Leach electrolysis
        // Cu3Au2(OH)3? + 3 H2 -> 6 H2O + 3 Cu + 2 Au (8 ngt)
        ELECTROLYZER_RECIPES.recipeBuilder()
                .inputs(GoldLeach.getItemStack(4))
                .fluidInputs(Hydrogen.getFluid(6000))
                .fluidOutputs(Water.getFluid(6000))
                .output(dust, Copper, 3)
                .output(dustTiny, Gold, 8)
                .duration(300)
                .EUt(30)
                .buildAndRegister();

        // ROUTE 4
        // Step 4.0 - Potassium metabisulfite synthesis (for the starting dust)
        // 6 K + 6 S + 15/2 O2 -> 3 K2S2O5
        MIXER_RECIPES.recipeBuilder().duration(100).EUt(30)
                .notConsumable(new IntCircuitIngredient(1))
                .input(dust, Potassium, 2)
                .input(dust, Sulfur, 2)
                .fluidInputs(Oxygen.getFluid(5000))
                .output(dust, PotassiumMetabisulfite, 9)
                .buildAndRegister();

        // Step 4.1 - Chloroauric acid decomposition with K2S2O5
        // 4 HAuCl4·3H20 -> 16 HCl + 3 O2 + 6 H2O + 4 Au (6 ngt)
        // This reaction is a bit tricky.  The chloroauric acid decomposition is usually done with SO2 or with an agent that
        // liberates it by hydrolysis (like K2S2O5). However, the metabisulfite is not a calatyst but a reducing agent; the
        // reactions involved on its regeneration would require the same number of steps as the final route, so to keep it cheaper
        // in order to justify the lower yield -as the gold purity is lower- the K2S2O5 usage and regeneration has been compressed
        // into a single reaction:
        // Reaction 1: Chloroauric decomposition: 4 HAuCl4·3H2O + 3 K2S2O5 -> 4 Au + 16 HCl + 6 KHSO4 + 3 H2O
        // Reaction 2: Potassion bisulfite electrolysis: 6 KHSO4 -> 6 K + 3 H2 + 6 S + 12 O2 -unreallistic-
        // Reaction 3: Water regeneration: 6 H + 3 O -> 3 H2O
        // Reaction 4: Potassion metabisulfite regeneration: 6 K + 6 S + 15/2 O2 -> 3 K2S2O5
        // Total reaction: 4 HAuCl4·3H2O -> 4 Au + 16 HCl + 3 O2 + 6 H2O
        CHEMICAL_RECIPES.recipeBuilder().duration(100)
                .fluidInputs(ChloroauricAcid.getFluid(2000))
                .notConsumable(dust, PotassiumMetabisulfite)
                .output(dustTiny, Gold, 6)
                .fluidOutputs(HydrochloricAcid.getFluid(8000))
                .fluidOutputs(Water.getFluid(3000))
                .fluidOutputs(Oxygen.getFluid(3000))
                .buildAndRegister();

        // Step 4.2 - Copper Leach decomposition
        // This step does not directly process Chloroauric Acid, and instead is processing
        // other byproducts from the chain, which are compacted from the older versions of the chain.
        // Cu3? -> 3Cu + Fe + Ni + Ag + Pb
        CHEMICAL_DEHYDRATOR_RECIPES.recipeBuilder().EUt(30).duration(80)
                .inputs(CopperLeach.getItemStack(4))
                .output(dust, Copper, 3)
                .chancedOutput(OreDictUnifier.get(dust, Lead), 1500, 500)
                .chancedOutput(OreDictUnifier.get(dust, Iron), 1200, 400)
                .chancedOutput(OreDictUnifier.get(dust, Nickel), 1000, 300)
                .chancedOutput(OreDictUnifier.get(dust, Silver), 800, 200)
                .buildAndRegister();

        // ROUTE 5
        // Step 5.1 - Chloroauric Acid electrolysis - Mimicking the Wohlwill process
        // 2 HAuCl4·3H2O -> 2Au + 2 HCl + 3 Cl2 + 6 H2O
        ELECTROLYZER_RECIPES.recipeBuilder()
                .fluidInputs(ChloroauricAcid.getFluid(1000))
                .output(dust, Gold, 2)
                .fluidOutputs(Water.getFluid(3000))
                .fluidOutputs(Chlorine.getFluid(3000))
                .fluidOutputs(HydrochloricAcid.getFluid(1000))
                .duration(100)
                .EUt(30)
                .buildAndRegister();
        
    }
}
