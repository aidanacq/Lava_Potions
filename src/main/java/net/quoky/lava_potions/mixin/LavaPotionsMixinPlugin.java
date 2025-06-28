package net.quoky.lava_potions.mixin;

import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.minecraftforge.fml.loading.LoadingModList;

/**
 * Mixin plugin to conditionally load Create compatibility mixins
 * Only loads Create-related mixins when Create mod is present
 */
public class LavaPotionsMixinPlugin implements IMixinConfigPlugin {

    private static final Logger LOGGER = LogManager.getLogger("LavaPotionsMixinPlugin");
    private static final String CREATE_MOD_ID = "create";
    
    // List of Create-specific mixin class names that should only load when Create is present
    private static final String[] CREATE_MIXIN_CLASSES = {
        "CreatePotionFluidHandlerMixin",
        "CreatePotionFluidMixin", 
        "CreatePotionFluidTypeMixin"
    };
    
    @Override
    public void onLoad(String mixinPackage) {
        LOGGER.info("Loading Lava Potions mixin plugin for package: {}", mixinPackage);
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        try {
            // Extract just the class name from the full mixin class name
            String simpleClassName = mixinClassName.substring(mixinClassName.lastIndexOf('.') + 1);
            
            // Check if this is a Create-specific mixin
            for (String createMixinClass : CREATE_MIXIN_CLASSES) {
                if (simpleClassName.equals(createMixinClass)) {
                    boolean createLoaded = LoadingModList.get().getModFileById(CREATE_MOD_ID) != null;
                    
                    if (createLoaded) {
                        LOGGER.debug("Applying Create mixin: {} (Create mod detected)", simpleClassName);
                    } else {
                        LOGGER.info("Skipping Create mixin: {} (Create mod not found)", simpleClassName);
                    }
                    
                    return createLoaded;
                }
            }
            
            // Apply all other mixins normally
            LOGGER.debug("Applying non-Create mixin: {}", simpleClassName);
            return true;
            
        } catch (Exception e) {
            LOGGER.error("Error checking if mixin should be applied: {} - {}", mixinClassName, e.getMessage());
            // In case of error, default to not applying Create mixins to avoid crashes
            String simpleClassName = mixinClassName.substring(mixinClassName.lastIndexOf('.') + 1);
            for (String createMixinClass : CREATE_MIXIN_CLASSES) {
                if (simpleClassName.equals(createMixinClass)) {
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
        // No special target handling needed
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // No pre-processing needed
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // No post-processing needed
    }
}