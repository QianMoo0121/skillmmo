package dev.nickrobson.minecraft.skillmmo.mixin;

import dev.nickrobson.minecraft.skillmmo.config.SkillMmoConfig;
import dev.nickrobson.minecraft.skillmmo.util.SkillMmoBooleanGameRuleSetter;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(GameRules.class)
public abstract class MixinGameRules {
    @Shadow
    @Final
    private Map<GameRules.Key<?>, GameRules.Rule<?>> rules;

    @Inject(method = "<init>()V", at = @At("RETURN"))
    private void onRegisterGameRule(CallbackInfo ci) {
        // 安全检查：确保配置已经被注册
        try {
            // 首先检查配置持有者是否存在
            if (AutoConfig.getConfigHolder(SkillMmoConfig.class) != null) {
                // 然后再获取配置实例并检查相关选项
                SkillMmoConfig config = SkillMmoConfig.getConfig();
                if (config.enableDoLimitedCraftingGameruleInAllNewWorlds) {
                    GameRules.Rule<?> rule = this.rules.get(GameRules.DO_LIMITED_CRAFTING);
                    if (rule instanceof SkillMmoBooleanGameRuleSetter setter) {
                        setter.skillMmo$setValue(true);
                    }
                }
            }
        } catch (RuntimeException e) { // 修改此处，捕获 RuntimeException
            // 配置尚未注册，跳过此操作
            // 这在模组初始化期间是正常的
            System.err.println("SkillMMO Mod: SkillMmoConfig not yet registered when GameRules initialized. Skipping gamerule modification. Error: " + e.getMessage());
        }
    }

    @Mixin(GameRules.BooleanRule.class)
    public static abstract class MixinBooleanRule implements SkillMmoBooleanGameRuleSetter {
        @Shadow
        private boolean value;

        @Override
        public void skillMmo$setValue(boolean enabled) {
            this.value = enabled;
        }
    }
}
