package com.christofmeg.justenoughbreeding.emi;

import com.christofmeg.justenoughbreeding.CommonConstants;
import com.christofmeg.justenoughbreeding.JustEnoughBreeding;
import com.christofmeg.justenoughbreeding.recipe.TransformationRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/** One JEB recipe as an EMI recipe: the mob (rendered, name on top), its food / item inputs, spawn egg and output. */
public final class JebEmiRecipe implements EmiRecipe {
    private static final int WIDTH = 166;
    private static final int HEIGHT = 91;
    private static final AtomicInteger IDS = new AtomicInteger();

    private final EmiRecipeCategory category;
    private final Identifier id;
    private final Recipe<?> recipe;
    private final @Nullable EntityType<?> entity;
    private final @Nullable EntityType<?> secondEntity; // transformation output
    private final EmiIngredient inputs;
    private final EmiIngredient extra;
    private final EmiIngredient spawnEgg;
    private final EmiIngredient outputs;
    private final EmiIngredient secondSpawnEgg;

    private JebEmiRecipe(EmiRecipeCategory category, String kind, Recipe<?> recipe, @Nullable EntityType<?> entity,
                         @Nullable EntityType<?> secondEntity, Ingredient inputs, @Nullable Ingredient extra,
                         @Nullable Ingredient spawnEgg, @Nullable Ingredient outputs, @Nullable Ingredient secondSpawnEgg) {
        this.category = category;
        String key = entity == null ? "unknown" : EntityType.getKey(entity).toString().replace(':', '/');
        this.id = Identifier.fromNamespaceAndPath(CommonConstants.MOD_ID, "/" + kind + "/" + key + "/" + IDS.incrementAndGet());
        this.recipe = recipe;
        this.entity = entity;
        this.secondEntity = secondEntity;
        this.inputs = EmiIngredient.of(inputs);
        this.extra = extra == null || extra.isEmpty() ? EmiStack.EMPTY : EmiIngredient.of(extra);
        this.spawnEgg = eggFor(entity, spawnEgg);
        this.outputs = outputs == null || outputs.isEmpty() ? EmiStack.EMPTY : EmiIngredient.of(outputs);
        this.secondSpawnEgg = secondEntity == null ? EmiStack.EMPTY : eggFor(secondEntity, secondSpawnEgg);
    }

    private static EmiIngredient eggFor(@Nullable EntityType<?> entity, @Nullable Ingredient explicit) {
        if (explicit != null && !explicit.isEmpty()) return EmiIngredient.of(explicit);
        if (entity == null) return EmiStack.EMPTY;
        return JustEnoughBreeding.getSpawnEggItem(entity).<EmiIngredient>map(h -> EmiStack.of(h.value())).orElse(EmiStack.EMPTY);
    }

    static JebEmiRecipe simple(EmiRecipeCategory category, String kind, Recipe<?> recipe, @Nullable EntityType<?> entity,
                               Ingredient inputs, @Nullable Ingredient spawnEgg, @Nullable Ingredient extra,
                               @Nullable Ingredient outputs) {
        return new JebEmiRecipe(category, kind, recipe, entity, null, inputs, extra, spawnEgg, outputs, null);
    }

    static JebEmiRecipe transformation(EmiRecipeCategory category, TransformationRecipe r) {
        EntityType<?> input = JustEnoughBreeding.getEntityFromLoaderRegistries(Identifier.tryParse(r.inputEntity()));
        return new JebEmiRecipe(category, "transformation", r, input, r.outputEntityType(), r.inputs(), r.extraInputs(),
            r.inputSpawnEggs(), r.outputs(), r.outputSpawnEggs());
    }

    @Override public EmiRecipeCategory getCategory() { return category; }
    @Override public @Nullable Identifier getId() { return id; }
    @Override public int getDisplayWidth() { return WIDTH; }
    @Override public int getDisplayHeight() { return HEIGHT; }
    @Override public boolean supportsRecipeTree() { return false; }

    @Override
    public List<EmiIngredient> getInputs() {
        List<EmiIngredient> list = new ArrayList<>();
        list.add(inputs);
        if (!extra.isEmpty()) list.add(extra);
        return list;
    }

    @Override
    public List<EmiIngredient> getCatalysts() {
        return spawnEgg.isEmpty() ? List.of() : List.of(spawnEgg);
    }

    @Override
    public List<EmiStack> getOutputs() {
        List<EmiStack> list = new ArrayList<>(outputs.getEmiStacks());
        list.addAll(secondSpawnEgg.getEmiStacks());
        return list;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        if (secondEntity == null) {
            widgets.addDrawable(0, 0, WIDTH, HEIGHT, (graphics, mouseX, mouseY, delta) ->
                EmiDrawUtils.drawMobNameAndEntity(entity, graphics, mouseX, recipe, 148, 0, true, WIDTH));
            widgets.addSlot(spawnEgg, 148, 0).catalyst(true);
            if (outputs.isEmpty()) {
                widgets.addSlot(inputs, 74, 30).large(false);
                if (!extra.isEmpty()) widgets.addSlot(extra, 74, 52);
            } else {
                widgets.addSlot(inputs, 74, extra.isEmpty() ? 36 : 46);
                if (!extra.isEmpty()) widgets.addSlot(extra, 74, 24);
                widgets.addTexture(EmiTexture.EMPTY_ARROW, 100, 37);
                widgets.addSlot(outputs, 130, 32).large(true).recipeContext(this);
            }
        } else {
            // transformation: input mob | items | output mob
            widgets.addDrawable(0, 0, WIDTH, HEIGHT, (graphics, mouseX, mouseY, delta) -> {
                EmiDrawUtils.drawMobNameAndEntity(entity, graphics, mouseX, recipe, 70, -45, true, WIDTH);
                EmiDrawUtils.drawMobNameAndEntity(secondEntity, graphics, mouseX, recipe, 70, 50, false, WIDTH);
            });
            widgets.addSlot(spawnEgg, 0, 72).catalyst(true);
            widgets.addSlot(inputs, 74, 40);
            if (!extra.isEmpty()) widgets.addSlot(extra, 74, 20);
            widgets.addTexture(EmiTexture.EMPTY_ARROW, 71, 62);
            widgets.addSlot(secondSpawnEgg, 148, 72).recipeContext(this);
            if (!outputs.isEmpty()) widgets.addSlot(outputs, 74, 2).recipeContext(this);
        }
    }
}
