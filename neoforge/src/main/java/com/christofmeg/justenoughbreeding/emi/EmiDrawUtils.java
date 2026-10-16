package com.christofmeg.justenoughbreeding.emi;

import com.christofmeg.justenoughbreeding.JustEnoughBreeding;
import com.christofmeg.justenoughbreeding.client.ClientUtils;
import com.christofmeg.justenoughbreeding.config.MobOffset;
import com.christofmeg.justenoughbreeding.config.MobOffsetManager;
import com.christofmeg.justenoughbreeding.recipe.BreedingRecipe;
import com.christofmeg.justenoughbreeding.recipe.TransformationRecipe;
import com.christofmeg.justenoughbreeding.utils.CommonClientUtils;
import com.christofmeg.justenoughbreeding.utils.Rect;
import com.christofmeg.justenoughbreeding.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.crafting.Recipe;

/** Mob name + rendered mob, same as JEIUtils#drawMobNameAndEntity but without JEI types (EMI-only runtime). */
final class EmiDrawUtils {
    private EmiDrawUtils() {}

    static void drawMobNameAndEntity(EntityType<?> entityType, GuiGraphicsExtractor graphics, double mouseX, Recipe<?> recipe,
                                     int availableWidth, int extraX, boolean input, int categoryWidth) {
        if (entityType == null) return;
        Font font = Minecraft.getInstance().font;
        String name = Component.translatable(entityType.getDescriptionId()).getString();
        if (recipe instanceof BreedingRecipe breeding) {
            if (Boolean.TRUE.equals(breeding.tamed())) {
                name += " (" + Component.translatable("translation.justenoughbreeding.tamed").getString() + ")";
            } else if (Boolean.TRUE.equals(breeding.trusting())) {
                name += " (" + Component.translatable("translation.justenoughbreeding.trusting").getString() + ")";
            }
        } else if (recipe instanceof TransformationRecipe transformation && input && Boolean.TRUE.equals(transformation.tamed())) {
            name += " (" + Component.translatable("translation.justenoughbreeding.tamed").getString() + ")";
        }
        int width = font.width(name);
        if (width > availableWidth) {
            float perChar = (float) width / Math.max(1, name.length());
            name = name.substring(0, Math.min(name.length(), Math.max(0, (int) (availableWidth / perChar))));
        }
        if (!name.isEmpty()) {
            graphics.text(font, Component.literal(name), Math.max(0, extraX + (categoryWidth - 59 - 10) / 2 - 20), 0,
                DyeColor.BLACK.getTextColor(), false);
        }
        LivingEntity current;
        if (recipe instanceof TransformationRecipe transformation) {
            current = input
                ? ClientUtils.doRendering(JustEnoughBreeding.getEntityFromLoaderRegistries(Identifier.tryParse(transformation.inputEntity())), transformation.inputEntityNbt(), true)
                : ClientUtils.doRendering(transformation.outputEntityType(), transformation.outputEntityNbt(), false);
        } else {
            current = ClientUtils.doRendering(entityType);
        }
        LivingEntity living = Utils.getLivingEntity(current, input, recipe);
        if (living != null) {
            Rect rect = new Rect((categoryWidth - 59 - 10) / 2 + extraX, 10, 59, 59);
            MobOffset offset = MobOffsetManager.get(JustEnoughBreeding.getKeyLoaderRegistries(current.getType()));
            CommonClientUtils.renderEntity(current, rect, graphics, (int) mouseX, offset);
        }
    }
}
