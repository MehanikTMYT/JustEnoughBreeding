package com.christofmeg.justenoughbreeding.emi;

import com.christofmeg.justenoughbreeding.CommonConstants;
import com.christofmeg.justenoughbreeding.JustEnoughBreeding;
import com.christofmeg.justenoughbreeding.client.ClientRecipeCache;
import com.christofmeg.justenoughbreeding.recipe.*;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;

import java.util.Comparator;
import java.util.function.Function;

/**
 * Fleet port (NeoForge 26.1.2, EMI-only pack): EMI plugin for Just Enough Breeding. Upstream ships JEI/REI plugins on
 * 26.1.2 (EMI only on Fabric 1.21.1); this mirrors the JEI categories on top of the same client recipe cache.
 */
@EmiEntrypoint
public class EMIPlugin implements EmiPlugin {
    public static final EmiRecipeCategory ALLAY_DUPLICATION = category("allay_duplication", Items.AMETHYST_SHARD);
    public static final EmiRecipeCategory BREEDING = category("breeding", Items.WHEAT);
    public static final EmiRecipeCategory TAMING = category("taming", Items.BONE);
    public static final EmiRecipeCategory TEMPER = category("temper", Items.GOLDEN_APPLE);
    public static final EmiRecipeCategory TRANSFORMATION = category("transformation", Items.GOLDEN_CARROT);
    public static final EmiRecipeCategory TRUSTING = category("trusting", Items.SWEET_BERRIES);

    private static EmiRecipeCategory category(String name, ItemLike icon) {
        return new EmiRecipeCategory(Identifier.fromNamespaceAndPath(CommonConstants.MOD_ID, name), EmiStack.of(icon)) {
            @Override
            public net.minecraft.network.chat.Component getName() {
                return net.minecraft.network.chat.Component.translatable("translation.justenoughbreeding." + name);
            }
        };
    }

    @Override
    public void register(EmiRegistry registry) {
        for (EmiRecipeCategory c : new EmiRecipeCategory[]{ALLAY_DUPLICATION, BREEDING, TAMING, TEMPER, TRANSFORMATION, TRUSTING}) {
            registry.addCategory(c);
            registry.addWorkstation(c, c.icon instanceof EmiStack s ? s : EmiStack.EMPTY);
        }
        add(registry, JustEnoughBreeding.ALLAY_DUPLICATION_PROVIDER_TYPE.get(), AllayDuplicationRecipe.class,
            AllayDuplicationRecipe::mod, AllayDuplicationRecipe::entityType,
            r -> JebEmiRecipe.simple(ALLAY_DUPLICATION, "allay_duplication", r, r.entityType(), r.inputs(), r.spawnEggs(), null, null));
        add(registry, JustEnoughBreeding.BREEDING_PROVIDER_TYPE.get(), BreedingRecipe.class,
            BreedingRecipe::mod, BreedingRecipe::entityType,
            r -> JebEmiRecipe.simple(BREEDING, "breeding", r, r.entityType(), r.inputs(), r.spawnEggs(), r.extraInputs(), r.outputs()));
        add(registry, JustEnoughBreeding.TAMING_PROVIDER_TYPE.get(), TamingRecipe.class,
            TamingRecipe::mod, TamingRecipe::entityType,
            r -> JebEmiRecipe.simple(TAMING, "taming", r, r.entityType(), r.inputs(), r.spawnEggs(), r.extraInputs(), null));
        add(registry, JustEnoughBreeding.TEMPER_PROVIDER_TYPE.get(), TemperRecipe.class,
            TemperRecipe::mod, TemperRecipe::entityType,
            r -> JebEmiRecipe.simple(TEMPER, "temper", r, r.entityType(), r.inputs(), r.spawnEggs(), r.extraInputs(), null));
        add(registry, JustEnoughBreeding.TRUSTING_PROVIDER_TYPE.get(), TrustingRecipe.class,
            TrustingRecipe::mod, TrustingRecipe::entityType,
            r -> JebEmiRecipe.simple(TRUSTING, "trusting", r, r.entityType(), r.inputs(), r.spawnEggs(), r.extraInputs(), null));
        add(registry, JustEnoughBreeding.TRANSFORMATION_PROVIDER_TYPE.get(), TransformationRecipe.class,
            TransformationRecipe::mod, TransformationRecipe::outputEntityType,
            r -> JebEmiRecipe.transformation(TRANSFORMATION, r));
    }

    private static <T extends Recipe<?>> void add(EmiRegistry registry, RecipeType<?> type, Class<T> cls,
                                                  Function<T, String> mod, Function<T, EntityType<?>> entity,
                                                  Function<T, JebEmiRecipe> factory) {
        ClientRecipeCache.getRecipes().stream()
            .map(RecipeHolder::value)
            .filter(r -> r.getType() == type)
            .filter(cls::isInstance).map(cls::cast)
            .filter(r -> entity.apply(r) != null && JustEnoughBreeding.isModLoaded(mod.apply(r)))
            .sorted(Comparator.comparing(r -> entity.apply(r).toShortString()))
            .map(factory)
            .forEach(registry::addRecipe);
    }
}
