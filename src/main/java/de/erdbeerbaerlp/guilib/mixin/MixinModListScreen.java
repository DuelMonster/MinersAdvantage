package de.erdbeerbaerlp.guilib.mixin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.erdbeerbaerlp.guilib.IHasConfigGUI;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.client.gui.screen.ModListScreen;
import net.minecraftforge.fml.client.gui.widget.ModListWidget;

@OnlyIn(Dist.CLIENT)
@Mixin(ModListScreen.class)
public abstract class MixinModListScreen extends Screen {
	
	@Shadow(remap = false)
	private Button                 configButton;
	@Shadow(remap = false)
	private ModListWidget.ModEntry selected;
	
	protected MixinModListScreen(ITextComponent titleIn) {
		super(titleIn);
	}
	
	@Inject(method = "updateCache",
			at = @At(   value = "RETURN",
						remap = false),
			require = 1,
			remap = false)
	private void onUpdate(CallbackInfo ci) {
		if (selected != null) {
			if (ModList.get().getModObjectById(selected.getInfo().getModId()).isPresent()) {
				final Object mod = ModList.get().getModObjectById(selected.getInfo().getModId()).get();
				if (ArrayUtils.contains(mod.getClass().getInterfaces(), IHasConfigGUI.class)) {
					configButton.active = true;
				}
			}
		}
	}
	
	@Inject(method = "displayModConfig",
			at = @At("HEAD"),
			remap = false)
	private void displayConfig(CallbackInfo ci) {
		if (selected != null) {
			if (ModList.get().getModObjectById(selected.getInfo().getModId()).isPresent()) {
				final Object mod = ModList.get().getModObjectById(selected.getInfo().getModId()).get();
				if (ArrayUtils.contains(mod.getClass().getInterfaces(), IHasConfigGUI.class)) {
					try {
						final Method getCfg = mod.getClass().getDeclaredMethod("getConfigGUI", Screen.class);
						final Screen s      = (Screen) getCfg.invoke(mod, this);
						minecraft.displayGuiScreen(s);
					}
					catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
	}
	
}
