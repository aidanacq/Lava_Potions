# AlexsMobs Conflict Resolution

## Problem
When both Lava Potions and AlexsMobs are installed, Create's spout filling recipe (250mB lava + glass bottle) creates AlexsMobs' lava bottle instead of our lava bottle.

## Solution Overview
Implemented a multi-layered approach to ensure our lava bottle takes priority:

### 1. High-Priority Mixins
- **CreatePotionFluidHandlerFillMixin**: Priority 1500, intercepts `fillBottle` and `getRequiredAmountForFilledBottle`
- **CreateGenericItemFillingMixin**: Priority 1500, intercepts `canFillGlassBottleInternally`
- **CreatePotionFluidHandlerMixin**: Priority 1500, intercepts `getFluidFromPotionItem`

### 2. Explicit Recipe Override
- **lava_bottle_priority.json**: Conditional recipe that only loads when AlexsMobs is present
- Explicitly defines 250mB lava + glass bottle = our lava bottle

### 3. Event Handler Priority
- **CreateCompat.onRightClickBlock**: Changed from NORMAL to HIGH priority
- Handles basin right-click interactions before other mods

### 4. Conflict Detection
- **RecipeConflictResolver**: Detects when AlexsMobs is present and logs conflict resolution status

## Files Modified

### Mixins
- `CreatePotionFluidHandlerFillMixin.java` - Added lava fluid handling with priority
- `CreateGenericItemFillingMixin.java` - Added lava fluid support with priority
- `CreatePotionFluidHandlerMixin.java` - Enhanced with priority handling
- `lava_potions.mixins.json` - Updated mixin registration

### Recipes
- `data/create/recipes/spout_filling/lava_bottle_priority.json` - NEW: Conditional override recipe

### Utilities
- `CreateCompat.java` - Enhanced with AlexsMobs detection and HIGH priority events
- `RecipeConflictResolver.java` - NEW: Conflict detection and logging
- `Lava_Potions.java` - Registered new conflict resolver

## How It Works

1. **Mixin Priority**: Our mixins run with higher priority (1500) than AlexsMobs
2. **Direct Interception**: We intercept lava fluid handling before other mods can process it
3. **Recipe Override**: Explicit recipe ensures our result when both mods are present
4. **Event Priority**: Our event handlers run before AlexsMobs' handlers
5. **Comprehensive Coverage**: Multiple layers ensure compatibility across all Create interaction methods

## Testing

To verify the fix:
1. Install both Lava Potions and AlexsMobs
2. Use a Create spout to fill a glass bottle with 250mB of lava
3. Check logs for priority messages
4. Verify you receive our lava bottle instead of AlexsMobs'

## Log Messages to Look For

- "Mixin intercepted lava fluid filling - creating our lava bottle (priority over other mods)"
- "Mixin allowing lava fluid to fill glass bottles (priority over other mods)"
- "AlexsMobs detected - using priority mixins to override lava bottle conflicts"

## Compatibility Notes

- Solution is non-destructive - doesn't break AlexsMobs functionality
- Only affects the specific 250mB lava + glass bottle recipe
- Uses conditional loading to minimize impact when AlexsMobs isn't present
- Maintains compatibility with other Create interactions

## Technical Notes

- Removed direct SpoutBlockEntity mixin due to method name incompatibility
- Relies on PotionFluidHandler mixins which are more stable across Create versions
- Uses proper mixin priority system for reliable conflict resolution 