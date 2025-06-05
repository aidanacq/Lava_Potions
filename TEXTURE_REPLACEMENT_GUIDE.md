# Texture Replacement Guide - Hybrid Lava Potion System

This guide explains which placeholder files you need to replace with actual PNG textures to complete the hybrid lava potion texture system.

## 📁 **Placeholder Files to Replace**

### 1. **`src/main/resources/assets/lava_potions/textures/item/lava_contents.png`**

**Type**: Main Lava Texture (Full Color, Animated)
**Usage**: Lava Bottle and Awkward Lava Potion (no tinting applied)
**Specifications**:
- **Size**: 16x16 pixels
- **Format**: PNG with transparency
- **Colors**: Full lava colors (bright oranges, reds, yellows)
- **Animation**: Should be animated (multiple frames vertically)
- **Shape**: Must fit the liquid area inside vanilla potion bottles
- **Effect**: Shows pure lava texture without any color modification

**Visual Requirements**:
- Flowing/bubbling lava effect
- Bright, molten appearance
- Should look like actual lava from Minecraft
- Transparent areas where no liquid should appear
- Multiple animation frames for flowing effect

### 2. **`src/main/resources/assets/lava_potions/textures/item/tinted_lava_contents.png`**

**Type**: Tintable Lava Texture (Grayscale/White, Animated)
**Usage**: Effect potions (gets color tinted to show effect influence)
**Specifications**:
- **Size**: 16x16 pixels
- **Format**: PNG with transparency
- **Colors**: Grayscale or white (for proper color tinting)
- **Animation**: Should be animated (multiple frames vertically)
- **Shape**: Must fit the liquid area inside vanilla potion bottles
- **Effect**: Gets tinted with effect colors blended with lava orange

**Visual Requirements**:
- Same shape and flow pattern as `lava_contents.png`
- Grayscale/white version so color tinting works properly
- Should maintain lava texture details but in neutral colors
- Multiple animation frames matching the main lava texture

## 🎨 **Animation Setup**

Both textures use the same animation configuration:

```json
{
  "animation": {
    "frametime": 8,
    "interpolate": true
  }
}
```

**Animation Requirements**:
- **Frame Time**: 8 ticks per frame (smooth animation)
- **Interpolation**: Enabled for smoother transitions
- **Frame Layout**: Vertical strip (frames stacked vertically)
- **Recommended Frames**: 4-8 frames for good flow effect

## 🔧 **How the System Works**

### **Base Potions** (Lava Bottle, Awkward Lava):
- Use `lava_contents.png` (full color)
- Color handler returns `0xFFFFFF` (white = no tint)
- Shows pure lava texture as-is

### **Effect Potions** (Future effects):
- Use `tinted_lava_contents.png` (grayscale)
- Color handler returns blended color (60% lava orange + 40% effect color)
- Shows lava texture tinted to indicate the effect

### **Bottle Types**:
- **Regular**: Uses `minecraft:item/potion_overlay`
- **Splash**: Uses `minecraft:item/splash_potion_overlay`
- **Lingering**: Uses `minecraft:item/lingering_potion_overlay`

## 🎯 **Texture Creation Tips**

### **For `lava_contents.png`**:
1. Start with vanilla lava texture as reference
2. Adapt it to fit potion bottle shape
3. Use bright, saturated colors:
   - Primary: `#FF6600` (bright orange)
   - Secondary: `#FF3300` (red-orange)
   - Highlights: `#FFAA00` (yellow-orange)
4. Add bubbling/flowing animation
5. Ensure transparency where no liquid should be

### **For `tinted_lava_contents.png`**:
1. Take the `lava_contents.png` and convert to grayscale
2. Alternatively, use white/light gray for maximum tinting effect
3. Maintain the same shape and animation frames
4. Test with different tint colors to ensure good visibility

## 🧪 **Testing Your Textures**

After replacing the placeholder files:

1. **Test Base Potions**:
   - Lava Bottle should show pure lava texture
   - Awkward Lava should show pure lava texture
   - No color tinting should be visible

2. **Test Effect Potions** (when you add them):
   - Should show lava texture with effect color influence
   - Should maintain lava theme while showing effect color

3. **Test All Variants**:
   - Regular potions (normal bottle shape)
   - Splash potions (splash bottle shape)
   - Lingering potions (lingering bottle shape)

## 📋 **File Structure After Replacement**

```
src/main/resources/assets/lava_potions/textures/item/
├── lava_contents.png              (Your full-color lava texture)
├── lava_contents.png.mcmeta       (Animation config - already created)
├── tinted_lava_contents.png       (Your grayscale lava texture)
└── tinted_lava_contents.png.mcmeta (Animation config - already created)
```

## 🚀 **Future Expansion**

When you add new effect potions:

1. They will automatically use the `tinted_lava_contents.png` texture
2. The color handler will blend their effect color with lava orange
3. No additional texture files needed - the tinting system handles everything
4. Just add the effect color logic to `LavaPotionColorHandler.java`

This hybrid system gives you the best of both worlds: pure lava appearance for base potions and tinted lava for effects that still maintains the lava theme! 