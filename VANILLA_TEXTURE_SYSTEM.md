# Vanilla-Style Texture System for Lava Potions

This document explains how the lava potions now implement the same texture system that vanilla Minecraft uses for potions.

## How Vanilla Potion Textures Work

Vanilla Minecraft potions use a sophisticated texture system:

1. **Base Textures**: 
   - `minecraft:item/potion_overlay` - The bottle outline (layer0)
   - `minecraft:item/potion` - The liquid contents (layer1)

2. **Color Tinting**: 
   - Layer0 (bottle) is never tinted - always appears as the original texture
   - Layer1 (liquid) gets tinted based on the potion's color using `ItemColor` handlers
   - The liquid texture is typically grayscale/white so tinting works properly

3. **Model Structure**:
   - Uses `item/generated` parent model
   - Each layer has an implicit tint index (layer0 = tintIndex 0, layer1 = tintIndex 1)
   - Color handlers check the tint index to determine which layer to color

## Implementation in Lava Potions

### 1. Custom Item Class (`LavaPotionItem`)

```java
public class LavaPotionItem extends Item {
    // Handles potion behavior like drinking, tooltips, NBT storage
    // Uses PotionUtils for storing potion type in NBT
    // Supports filling from lava sources
}
```

### 2. Color Handler (`LavaPotionColorHandler`)

```java
@SubscribeEvent
public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
    event.register((stack, tintIndex) -> {
        if (tintIndex == 1) { // Only tint the liquid layer
            return getLavaPotionColor(stack);
        }
        return 0xFFFFFF; // No tint for bottle layer
    }, ModItems.LAVA_POTION.get());
}
```

### 3. Item Model (`lava_potion.json`)

```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "minecraft:item/potion_overlay",
    "layer1": "minecraft:item/potion"
  }
}
```

### 4. Color Scheme

The lava potions use a fire/lava-themed color palette:

- **Lava Bottle**: `0xFF6600` (Bright orange like lava)
- **Awkward Lava**: `0xCC4400` (Dark orange-red)
- **Future Effects**: Will blend effect colors with lava orange to maintain theme

## Advantages of This System

1. **Consistency**: Matches vanilla Minecraft's potion system exactly
2. **Flexibility**: Easy to add new potion types with different colors
3. **Performance**: Uses existing vanilla textures and rendering pipeline
4. **Compatibility**: Works with resource packs that modify vanilla potion textures
5. **Extensibility**: Can easily add splash and lingering variants

## Adding New Potion Types

To add a new lava potion type:

1. **Register the Potion Type**:
```java
public static final RegistryObject<Potion> NEW_EFFECT = POTIONS.register("new_effect", 
    () -> new Potion(new MobEffectInstance(ModEffects.NEW_EFFECT.get(), 3600)));
```

2. **Add Color to Handler**:
```java
if (potion == ModPotionTypes.NEW_EFFECT.get()) {
    return 0xFFAA00; // Custom color
}
```

3. **Add Brewing Recipe**:
```java
BrewingRecipeRegistry.addRecipe(new CustomBrewingRecipe());
```

4. **Add Localization**:
```json
{
  "potion.lava_potions.new_effect": "New Effect Lava Potion"
}
```

## Texture Customization

If you want to customize the textures:

1. **Replace Vanilla References**: Update the model to use custom textures:
```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "lava_potions:item/lava_potion_overlay",
    "layer1": "lava_potions:item/lava_potion_contents"
  }
}
```

2. **Create Custom Textures**:
   - `lava_potion_overlay.png` - 16x16 bottle outline (transparent where liquid shows)
   - `lava_potion_contents.png` - 16x16 liquid texture (grayscale for proper tinting)

3. **Maintain Tinting**: Ensure the liquid texture is grayscale/white so color tinting works

## Brewing System Integration

The system integrates with both vanilla and custom brewing:

- **Glass Bottle + Lava Source** → Lava Bottle (via right-click interaction)
- **Lava Bottle + Nether Wart** → Awkward Lava Potion (via brewing stand)
- **Awkward Lava + Ingredients** → Effect Potions (future implementation)

## Creative Tab Integration

All potion variants are automatically added to the creative tab:
- Custom lava potions (with proper tinting)
- Vanilla potion items with lava types (for compatibility)
- Related items like lava cauldron

This system provides a solid foundation for expanding the lava potion system while maintaining full compatibility with vanilla Minecraft's potion mechanics. 