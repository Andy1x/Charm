package svenhjol.charm.module.kilns;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import svenhjol.charm.module.kilns.Kilns;

public class FiringRecipe extends AbstractCookingRecipe {
    public FiringRecipe(Identifier id, String group, Ingredient input, ItemStack output, float experience, int cookTime) {
        super(Kilns.RECIPE_TYPE, id, group, input, output, experience, cookTime);
    }

    public Ingredient getInput() {
        return this.input;
    }

    @Environment(EnvType.CLIENT)
    public ItemStack getRecipeKindIcon() {
        return new ItemStack(Kilns.KILN);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Kilns.RECIPE_SERIALIZER;
    }
}
