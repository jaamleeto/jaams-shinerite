package net.jaams.shinerite.recipe;

import java.util.Optional;
import java.util.stream.Stream;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.item.armortrim.TrimPatterns;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.Level;

import net.minecraftforge.common.ForgeHooks;

import net.jaams.shinerite.dyeable.IDyeableItem;
import net.jaams.shinerite.init.ModRecipes;
import net.jaams.shinerite.util.ShineriteTrimSupport;

public class ShineriteTrimSmithingRecipe implements SmithingRecipe {
	public static final String TAG_TRIM_COLOR = "shinerite_trim_color";


	final ResourceLocation id;
	final Ingredient template;
	final Ingredient base;
	final Ingredient addition;

	public ShineriteTrimSmithingRecipe(ResourceLocation id, Ingredient template, Ingredient base, Ingredient addition) {
		this.id = id;
		this.template = template;
		this.base = base;
		this.addition = addition;
	}

	@Override
	public boolean matches(Container container, Level level) {
		return this.canApply(container.getItem(0), container.getItem(1), container.getItem(2));
	}



	private boolean canApply(ItemStack templateStack, ItemStack baseStack, ItemStack additionStack) {
		return this.template.test(templateStack) && this.base.test(baseStack) && this.addition.test(additionStack);
	}

	@Override
	public ItemStack assemble(Container container, RegistryAccess registryAccess) {
		ItemStack templateStack = container.getItem(0);
		ItemStack baseStack = container.getItem(1);
		ItemStack additionStack = container.getItem(2);
		if (!this.canApply(templateStack, baseStack, additionStack)) {
			return ItemStack.EMPTY;
		}
		Optional<Holder.Reference<TrimMaterial>> material = TrimMaterials.getFromIngredient(registryAccess, additionStack);
		Optional<Holder.Reference<TrimPattern>> pattern = TrimPatterns.getFromTemplate(registryAccess, templateStack);
		if (material.isEmpty() || pattern.isEmpty())
			return ItemStack.EMPTY;
		Optional<ArmorTrim> existing = ArmorTrim.getTrim(registryAccess, baseStack);
		if (existing.isPresent() && existing.get().hasPatternAndMaterial(pattern.get(), material.get()))
			return ItemStack.EMPTY;
		ItemStack result = baseStack.copy();
		result.setCount(1);
		ArmorTrim trim = new ArmorTrim(material.get(), pattern.get());
		if (!ArmorTrim.setTrim(registryAccess, result, trim))
			return ItemStack.EMPTY;
		boolean tintable = ShineriteTrimSupport.isTintable(baseStack);
		boolean storeColor = ShineriteTrimSupport.shouldStoreColor(additionStack);
		if (storeColor)
			result.getOrCreateTag().putInt(TAG_TRIM_COLOR, ((IDyeableItem) additionStack.getItem()).getColor(additionStack));
		return result;
	}

	@Override
	public ItemStack getResultItem(RegistryAccess registryAccess) {
		ItemStack stack = new ItemStack(Items.IRON_CHESTPLATE);
		registryAccess.registryOrThrow(Registries.TRIM_PATTERN).holders().findFirst().ifPresent(pattern -> {
			registryAccess.registryOrThrow(Registries.TRIM_MATERIAL).getHolder(TrimMaterials.REDSTONE).ifPresent(material -> {
				ArmorTrim.setTrim(registryAccess, stack, new ArmorTrim(material, pattern));
			});
		});
		return stack;
	}

	@Override
	public boolean isTemplateIngredient(ItemStack stack) {
		return this.template.test(stack);
	}

	@Override
	public boolean isBaseIngredient(ItemStack stack) {
		return this.base.test(stack);
	}

	@Override
	public boolean isAdditionIngredient(ItemStack stack) {
		return this.addition.test(stack);
	}

	@Override
	public boolean isIncomplete() {
		return Stream.of(this.template, this.base, this.addition).anyMatch(ForgeHooks::hasNoElements);
	}

	@Override
	public ResourceLocation getId() {
		return this.id;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModRecipes.SHINERITE_TRIM_SERIALIZER.get();
	}
}
