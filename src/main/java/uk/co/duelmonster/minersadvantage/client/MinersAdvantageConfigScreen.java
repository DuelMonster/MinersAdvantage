package uk.co.duelmonster.minersadvantage.client;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.AbstractConfigScreen;
import me.shedaniel.clothconfig2.gui.entries.SelectionListEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import uk.co.duelmonster.minersadvantage.common.config.ClientConfig;
import uk.co.duelmonster.minersadvantage.common.config.MAClientRootConfig;
import uk.co.duelmonster.minersadvantage.common.config.MAServerRootConfig;
import uk.co.duelmonster.minersadvantage.common.config.CommonConfig;
import uk.co.duelmonster.minersadvantage.common.config.MAConfig_Base;
import uk.co.duelmonster.minersadvantage.common.log.LogUtils;
import uk.co.duelmonster.minersadvantage.common.network.PlayerStateSyncPacket;
import uk.co.duelmonster.minersadvantage.common.services.utility.TorchPlacement;

/**
 * MinersAdvantageConfigScreen keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class MinersAdvantageConfigScreen {
  private static MAClientRootConfig currentClientConfig = MAConfig_Base.getClientRootConfig();
  private static MAServerRootConfig currentServerConfig = MAConfig_Base.getServerRootConfig();
  private static Screen currentConfigScreen;

  /**
   * MinersAdvantageConfigScreen exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  private MinersAdvantageConfigScreen() {
  }

  /**
   * create exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public static Screen create(Screen parent) {
    MutableConfig mutable = new MutableConfig(currentClientConfig, currentServerConfig);
    boolean gameplayEditable = isGameplayEditable();
    ConfigBuilder builder = ConfigBuilder.create()
        .setParentScreen(parent)
        .setTitle(Component.literal("MinersAdvantage"));
    ConfigEntryBuilder entryBuilder = builder.entryBuilder();

    ConfigCategory generalCategory = builder.getOrCreateCategory(Component.literal("General"));
    ConfigCategory featuresCategory = builder.getOrCreateCategory(Component.literal("Features"));
    ConfigCategory clientCategory = builder.getOrCreateCategory(Component.literal("Client only"));

    clientCategory.addEntry(entryBuilder.startBooleanToggle(
        Component.literal("Disable Particle Effects"),
        mutable.disableParticleEffects)
        .setDefaultValue(currentClientConfig.client().disableParticleEffects())
        .setTooltip(Component.literal("Disable MinersAdvantage particle effects on this client only."))
        .setSaveConsumer(value -> mutable.disableParticleEffects = value)
        .build());

    clientCategory.addEntry(entryBuilder.startBooleanToggle(
        Component.literal("Debug Logging"),
        mutable.debugLogging)
        .setDefaultValue(currentClientConfig.client().debugLogging())
        .setTooltip(Component.literal("Enable verbose MinersAdvantage debug logs for troubleshooting."))
        .setSaveConsumer(value -> mutable.debugLogging = value)
        .build());

    clientCategory.addEntry(entryBuilder.startAlphaColorField(
        Component.literal("Preview Outline Foreground Color"),
        mutable.outlineForegroundColor)
        .setDefaultValue(currentClientConfig.client().outlineForegroundColor())
        .setTooltip(Component.literal("Color picker for the depth-tested foreground outline."))
        .setSaveConsumer(value -> mutable.outlineForegroundColor = value)
        .build());

    clientCategory.addEntry(entryBuilder.startAlphaColorField(
        Component.literal("Preview Outline See-Through Color"),
        mutable.outlineSeeThroughColor)
        .setDefaultValue(currentClientConfig.client().outlineSeeThroughColor())
        .setTooltip(Component.literal("Color picker for the no-depth translucent outline."))
        .setSaveConsumer(value -> mutable.outlineSeeThroughColor = value)
        .build());

    if (!gameplayEditable) {
      generalCategory.addEntry(authorityNoticeEntry(entryBuilder));
      featuresCategory.addEntry(authorityNoticeEntry(entryBuilder));
    }
    featuresCategory.addEntry(entryBuilder
        .startTextDescription(Component.literal("Use the feature buttons below to open dedicated feature screens."))
        .build());
    addFeatureOpenEntry(featuresCategory, entryBuilder, "Captivation", () -> mutable.captivationEnabled,
        parentScreen -> createCaptivationScreen(parentScreen, mutable, gameplayEditable),
        () -> gameplayEditable && mutable.isCaptivationDifferentFromDefaults(),
        () -> {
          mutable.resetCaptivationToDefaults();
          saveMutableConfig(mutable, gameplayEditable);
        });
    addFeatureOpenEntry(featuresCategory, entryBuilder, "Cropination", () -> mutable.cropinationEnabled,
        parentScreen -> createCropinationScreen(parentScreen, mutable, gameplayEditable),
        () -> gameplayEditable && mutable.isCropinationDifferentFromDefaults(),
        () -> {
          mutable.resetCropinationToDefaults();
          saveMutableConfig(mutable, gameplayEditable);
        });
    addFeatureOpenEntry(featuresCategory, entryBuilder, "Cultivation", () -> mutable.cultivationEnabled,
        parentScreen -> createCultivationScreen(parentScreen, mutable, gameplayEditable),
        () -> gameplayEditable && mutable.isCultivationDifferentFromDefaults(),
        () -> {
          mutable.resetCultivationToDefaults();
          saveMutableConfig(mutable, gameplayEditable);
        });
    addFeatureOpenEntry(featuresCategory, entryBuilder, "Excavation", () -> mutable.excavationEnabled,
        parentScreen -> createExcavationScreen(parentScreen, mutable, gameplayEditable),
        () -> gameplayEditable && mutable.isExcavationDifferentFromDefaults(),
        () -> {
          mutable.resetExcavationToDefaults();
          saveMutableConfig(mutable, gameplayEditable);
        });
    addFeatureOpenEntry(featuresCategory, entryBuilder, "Pathanation", () -> mutable.pathanationEnabled,
        parentScreen -> createPathanationScreen(parentScreen, mutable, gameplayEditable),
        () -> gameplayEditable && mutable.isPathanationDifferentFromDefaults(),
        () -> {
          mutable.resetPathanationToDefaults();
          saveMutableConfig(mutable, gameplayEditable);
        });
    addFeatureOpenEntry(featuresCategory, entryBuilder, "Illumination", () -> mutable.illuminationEnabled,
        parentScreen -> createIlluminationScreen(parentScreen, mutable, gameplayEditable),
        () -> gameplayEditable && mutable.isIlluminationDifferentFromDefaults(),
        () -> {
          mutable.resetIlluminationToDefaults();
          saveMutableConfig(mutable, gameplayEditable);
        });
    addFeatureOpenEntry(featuresCategory, entryBuilder, "Lumbination", () -> mutable.lumbinationEnabled,
        parentScreen -> createLumbinationScreen(parentScreen, mutable, gameplayEditable),
        () -> gameplayEditable && mutable.isLumbinationDifferentFromDefaults(),
        () -> {
          mutable.resetLumbinationToDefaults();
          saveMutableConfig(mutable, gameplayEditable);
        });
    addFeatureOpenEntry(featuresCategory, entryBuilder, "Shaftanation", () -> mutable.shaftanationEnabled,
        parentScreen -> createShaftanationScreen(parentScreen, mutable, gameplayEditable),
        () -> gameplayEditable && mutable.isShaftanationDifferentFromDefaults(),
        () -> {
          mutable.resetShaftanationToDefaults();
          saveMutableConfig(mutable, gameplayEditable);
        });
    addFeatureOpenEntry(featuresCategory, entryBuilder, "Substitution", () -> mutable.substitutionEnabled,
        parentScreen -> createSubstitutionScreen(parentScreen, mutable, gameplayEditable),
        () -> gameplayEditable && mutable.isSubstitutionDifferentFromDefaults(),
        () -> {
          mutable.resetSubstitutionToDefaults();
          saveMutableConfig(mutable, gameplayEditable);
        });
    addFeatureOpenEntry(featuresCategory, entryBuilder, "Veination", () -> mutable.veinationEnabled,
        parentScreen -> createVeinationScreen(parentScreen, mutable, gameplayEditable),
        () -> gameplayEditable && mutable.isVeinationDifferentFromDefaults(),
        () -> {
          mutable.resetVeinationToDefaults();
          saveMutableConfig(mutable, gameplayEditable);
        });
    addFeatureOpenEntry(featuresCategory, entryBuilder, "Ventilation", () -> mutable.ventilationEnabled,
        parentScreen -> createVentilationScreen(parentScreen, mutable, gameplayEditable),
        () -> gameplayEditable && mutable.isVentilationDifferentFromDefaults(),
        () -> {
          mutable.resetVentilationToDefaults();
          saveMutableConfig(mutable, gameplayEditable);
        });

    addGameplayBoolean(generalCategory, entryBuilder, "TPS Guard", "Reduces heavy processing when TPS drops.",
        mutable.tpsGuard, currentServerConfig.common().tpsGuard(), gameplayEditable, value -> mutable.tpsGuard = value);
    addGameplayBoolean(generalCategory, entryBuilder, "Gather Drops", "Allow automation to pull nearby drops.",
        mutable.gatherDrops, currentServerConfig.common().gatherDrops(), gameplayEditable,
        value -> mutable.gatherDrops = value);
    addGameplayBoolean(generalCategory, entryBuilder, "Auto Illuminate", "Permit automatic torch placement behavior.",
        mutable.autoIlluminate, currentServerConfig.common().autoIlluminate(), gameplayEditable,
        value -> mutable.autoIlluminate = value);
    addGameplayBoolean(generalCategory, entryBuilder, "Mine Veins", "Permit connected ore mining logic.",
        mutable.mineVeins, currentServerConfig.common().mineVeins(), gameplayEditable,
        value -> mutable.mineVeins = value);
    addGameplayInt(generalCategory, entryBuilder, "Blocks Per Tick", "Maximum blocks processed each server tick.",
        mutable.blocksPerTick, currentServerConfig.common().blocksPerTick(), 1, 8, gameplayEditable,
        value -> mutable.blocksPerTick = value);
    addGameplayBoolean(generalCategory, entryBuilder, "Enable Tick Delay",
        "Enable an additional delay between processing steps.", mutable.enableTickDelay,
        currentServerConfig.common().enableTickDelay(), gameplayEditable, value -> mutable.enableTickDelay = value);
    addGameplayInt(generalCategory, entryBuilder, "Tick Delay", "Delay in ticks used when tick delay is enabled.",
        mutable.tickDelay, currentServerConfig.common().tickDelay(), 0, 40, gameplayEditable,
        value -> mutable.tickDelay = value);
    addGameplayInt(generalCategory, entryBuilder, "Block Radius",
        "Configured radius used by radius-limited operations.", mutable.blockRadius,
        currentServerConfig.common().blockRadius(), 1, 16, gameplayEditable, value -> mutable.blockRadius = value);

    builder.setSavingRunnable(() -> saveMutableConfig(mutable, gameplayEditable));
    Screen screen = builder.build();
    currentConfigScreen = screen;
    return screen;
  }

  private static void addFeatureOpenEntry(
      ConfigCategory category,
      ConfigEntryBuilder entryBuilder,
      String featureName,
      BooleanSupplier enabledSupplier,
      java.util.function.Function<Screen, Screen> targetScreenFactory,
      BooleanSupplier resetEnabledSupplier,
      Runnable resetRunnable) {
    @SuppressWarnings("unchecked")
    SelectionListEntry<FeatureLaunchAction>[] entryRef = new SelectionListEntry[1];
    SelectionListEntry<FeatureLaunchAction> entry = entryBuilder.startSelector(
        buildFeatureStatusLabel(featureName, enabledSupplier.getAsBoolean()),
        FeatureLaunchAction.values(),
        FeatureLaunchAction.OPEN_A)
        .setNameProvider(ignored -> Component.literal("Open"))
        .setDefaultValue(FeatureLaunchAction.OPEN_A)
        .setSaveConsumer(ignored -> {
        })
        .setErrorSupplier(action -> Optional.empty())
        .setTooltip(Component.literal("Open " + featureName + " settings"))
        .build();

    entryRef[0] = entry;
    updateFeatureEntryLabelReflective(entry, featureName, enabledSupplier.getAsBoolean());
    configureFeatureOpenButton(entry, targetScreenFactory);
    configureFeatureResetButton(entry, featureName, resetEnabledSupplier, resetRunnable);
    category.addEntry(entry);
  }

  /**
   * FeatureLaunchAction exists to keep selector entries stable while using a direct open-button handler.
   */
  private enum FeatureLaunchAction {
    OPEN_A,
    OPEN_B
  }

  /**
   * Refresh feature row label text without rebuilding the whole entry list.
   */
  private static void updateFeatureEntryLabelReflective(SelectionListEntry<?> entry, String featureName,
      boolean enabled) {
    try {
      Field fieldNameField = AbstractConfigListEntry.class.getDeclaredField("fieldName");
      fieldNameField.setAccessible(true);
      fieldNameField.set(entry, buildFeatureStatusLabel(featureName, enabled));
    } catch (ReflectiveOperationException ignored) {
      // Cosmetic refresh only: if internals change, keep behavior and avoid crashing.
    }
  }

  /**
   * Build the feature title label with enabled/disabled status styling.
   */
  private static Component buildFeatureStatusLabel(String featureName, boolean enabled) {
    Component status = enabled
        ? Component.literal("Enabled").withStyle(ChatFormatting.GREEN)
        : Component.literal("Disabled").withStyle(ChatFormatting.RED);
    return Component.literal(featureName + " [").append(status).append(Component.literal("]"));
  }

  private static void configureFeatureResetButton(
      SelectionListEntry<FeatureLaunchAction> entry,
      String featureName,
      BooleanSupplier resetEnabledSupplier,
      Runnable resetRunnable) {
    Button resetButton = getSelectionResetButton(entry);
    if (resetButton == null) {
      return;
    }

    resetButton.active = resetEnabledSupplier.getAsBoolean();
    setButtonOnPressReflective(resetButton, ignored -> {
      if (!resetEnabledSupplier.getAsBoolean()) {
        return;
      }

      Minecraft minecraft = Minecraft.getInstance();
      Screen previousScreen = currentConfigScreen != null
          ? currentConfigScreen
          : ClientRuntimeCompat.getCurrentScreen(minecraft);
      final Screen returnScreen = previousScreen;
      ClientRuntimeCompat.setScreen(minecraft, new ConfirmScreen(
          confirmed -> {
            ClientRuntimeCompat.setScreen(minecraft, returnScreen);
            if (confirmed) {
              resetRunnable.run();
            }
          },
          Component.literal("Reset " + featureName + " settings?"),
          Component.literal("This will reset all " + featureName + " settings to defaults.")));
    });
  }

  private static void configureFeatureOpenButton(
      SelectionListEntry<FeatureLaunchAction> entry,
      java.util.function.Function<Screen, Screen> targetScreenFactory) {
    Button openButton = getSelectionPrimaryButton(entry);
    if (openButton == null) {
      return;
    }

    setButtonOnPressReflective(openButton, ignored -> {
      Screen activeScreen = currentConfigScreen != null
          ? currentConfigScreen
          : ClientRuntimeCompat.getCurrentScreen(Minecraft.getInstance());
      final Screen targetParentScreen = activeScreen;
      if (targetParentScreen != null) {
        ClientRuntimeCompat.setScreen(Minecraft.getInstance(), targetScreenFactory.apply(targetParentScreen));
      }
      resetSelectorEditedState(entry);
    });
  }

  /**
   * Resolve the reset button from a selector entry via reflective access.
   */
  private static Button getSelectionResetButton(SelectionListEntry<?> entry) {
    try {
      Field field = SelectionListEntry.class.getDeclaredField("resetButton");
      field.setAccessible(true);
      Object value = field.get(entry);
      if (value instanceof Button button) {
        return button;
      }
      return null;
    } catch (ReflectiveOperationException exception) {
      return null;
    }
  }

  private static Button getSelectionPrimaryButton(SelectionListEntry<?> entry) {
    try {
      for (String fieldName : new String[] { "buttonWidget", "button" }) {
        try {
          Field field = SelectionListEntry.class.getDeclaredField(fieldName);
          field.setAccessible(true);
          Object value = field.get(entry);
          if (value instanceof Button button) {
            return button;
          }
        } catch (NoSuchFieldException ignored) {
          // Try next known field name.
        }
      }
      return null;
    } catch (ReflectiveOperationException exception) {
      return null;
    }
  }

  /**
   * Replace button click handler across mapped/private field names.
   */
  private static void setButtonOnPressReflective(Button button, Button.OnPress onPress) {
    Class<?> current = button.getClass();
    while (current != null) {
      Field[] fields = current.getDeclaredFields();
      for (Field field : fields) {
        if (!Button.OnPress.class.isAssignableFrom(field.getType())) {
          continue;
        }

        try {
          field.setAccessible(true);
          field.set(button, onPress);
          return;
        } catch (ReflectiveOperationException exception) {
          throw new IllegalStateException("Unable to configure feature reset button", exception);
        }
      }
      current = current.getSuperclass();
    }
  }

  /**
   * Clear selector edited marker after launching a feature screen.
   */
  private static void resetSelectorEditedState(SelectionListEntry<?> entry) {
    try {
      Field indexField = SelectionListEntry.class.getDeclaredField("index");
      indexField.setAccessible(true);
      Object indexValue = indexField.get(entry);
      if (!(indexValue instanceof java.util.concurrent.atomic.AtomicInteger index)) {
        return;
      }

      Field originalField = SelectionListEntry.class.getDeclaredField("original");
      originalField.setAccessible(true);
      int original = originalField.getInt(entry);
      index.set(original);
    } catch (ReflectiveOperationException ignored) {
      // Visual cleanup only: if internals change, do not break button behavior.
    }
  }

  /**
   * Build standalone client-only config screen.
   */
  private static Screen createClientConfigScreen(Screen parent, MutableConfig mutable, boolean gameplayEditable) {
    ConfigBuilder builder = ConfigBuilder.create()
        .setParentScreen(parent)
        .setTitle(Component.literal("MinersAdvantage - Client"));
    ConfigEntryBuilder entryBuilder = builder.entryBuilder();
    ConfigCategory clientCategory = builder.getOrCreateCategory(Component.literal("Client"));

    clientCategory.addEntry(entryBuilder.startBooleanToggle(
        Component.literal("Disable Particle Effects"),
        mutable.disableParticleEffects)
        .setDefaultValue(currentClientConfig.client().disableParticleEffects())
        .setTooltip(Component.literal("Disable MinersAdvantage particle effects on this client only."))
        .setSaveConsumer(value -> mutable.disableParticleEffects = value)
        .build());

    clientCategory.addEntry(entryBuilder.startBooleanToggle(
        Component.literal("Debug Logging"),
        mutable.debugLogging)
        .setDefaultValue(currentClientConfig.client().debugLogging())
        .setTooltip(Component.literal("Enable verbose MinersAdvantage debug logs for troubleshooting."))
        .setSaveConsumer(value -> mutable.debugLogging = value)
        .build());

    clientCategory.addEntry(entryBuilder.startAlphaColorField(
        Component.literal("Preview Outline Foreground Color"),
        mutable.outlineForegroundColor)
        .setDefaultValue(currentClientConfig.client().outlineForegroundColor())
        .setTooltip(Component.literal("Color picker for the depth-tested foreground outline."))
        .setSaveConsumer(value -> mutable.outlineForegroundColor = value)
        .build());

    clientCategory.addEntry(entryBuilder.startAlphaColorField(
        Component.literal("Preview Outline See-Through Color"),
        mutable.outlineSeeThroughColor)
        .setDefaultValue(currentClientConfig.client().outlineSeeThroughColor())
        .setTooltip(Component.literal("Color picker for the no-depth translucent outline."))
        .setSaveConsumer(value -> mutable.outlineSeeThroughColor = value)
        .build());

    builder.setSavingRunnable(() -> saveMutableConfig(mutable, gameplayEditable));
    return builder.build();
  }

  /**
   * Build standalone general/gameplay config screen.
   */
  private static Screen createGeneralConfigScreen(Screen parent, MutableConfig mutable, boolean gameplayEditable) {
    ConfigBuilder builder = ConfigBuilder.create()
        .setParentScreen(parent)
        .setTitle(Component.literal("MinersAdvantage - General"));
    ConfigEntryBuilder entryBuilder = builder.entryBuilder();
    ConfigCategory generalCategory = builder.getOrCreateCategory(Component.literal("General"));

    if (!gameplayEditable) {
      generalCategory.addEntry(authorityNoticeEntry(entryBuilder));
    }

    addGameplayBoolean(generalCategory, entryBuilder, "TPS Guard", "Reduces heavy processing when TPS drops.",
        mutable.tpsGuard, currentServerConfig.common().tpsGuard(), gameplayEditable, value -> mutable.tpsGuard = value);
    addGameplayBoolean(generalCategory, entryBuilder, "Gather Drops", "Allow automation to pull nearby drops.",
        mutable.gatherDrops, currentServerConfig.common().gatherDrops(), gameplayEditable,
        value -> mutable.gatherDrops = value);
    addGameplayBoolean(generalCategory, entryBuilder, "Auto Illuminate", "Permit automatic torch placement behavior.",
        mutable.autoIlluminate, currentServerConfig.common().autoIlluminate(), gameplayEditable,
        value -> mutable.autoIlluminate = value);
    addGameplayBoolean(generalCategory, entryBuilder, "Mine Veins", "Permit connected ore mining logic.",
        mutable.mineVeins, currentServerConfig.common().mineVeins(), gameplayEditable,
        value -> mutable.mineVeins = value);
    addGameplayInt(generalCategory, entryBuilder, "Blocks Per Tick", "Maximum blocks processed each server tick.",
        mutable.blocksPerTick, currentServerConfig.common().blocksPerTick(), 1, 8, gameplayEditable,
        value -> mutable.blocksPerTick = value);
    addGameplayBoolean(generalCategory, entryBuilder, "Enable Tick Delay",
        "Enable an additional delay between processing steps.", mutable.enableTickDelay,
        currentServerConfig.common().enableTickDelay(), gameplayEditable, value -> mutable.enableTickDelay = value);
    addGameplayInt(generalCategory, entryBuilder, "Tick Delay", "Delay in ticks used when tick delay is enabled.",
        mutable.tickDelay, currentServerConfig.common().tickDelay(), 0, 40, gameplayEditable,
        value -> mutable.tickDelay = value);
    addGameplayInt(generalCategory, entryBuilder, "Block Radius",
        "Configured radius used by radius-limited operations.", mutable.blockRadius,
        currentServerConfig.common().blockRadius(), 1, 16, gameplayEditable, value -> mutable.blockRadius = value);

    builder.setSavingRunnable(() -> saveMutableConfig(mutable, gameplayEditable));
    return builder.build();
  }

  /**
   * Build a feature-specific sub-screen and wire shared footer/save behavior.
   */
  private static Screen createFeatureConfigScreen(Screen parent, MutableConfig mutable, boolean gameplayEditable,
      String featureName, Consumer<List<AbstractConfigListEntry<?>>> featureEntries) {
    ConfigBuilder builder = ConfigBuilder.create()
        .setParentScreen(parent)
        .setTitle(Component.literal("MinersAdvantage - " + featureName));
    ConfigEntryBuilder entryBuilder = builder.entryBuilder();
    ConfigCategory category = builder.getOrCreateCategory(Component.literal(featureName));

    List<AbstractConfigListEntry<?>> entries = new ArrayList<>();
    if (!gameplayEditable) {
      entries.add(authorityNoticeEntry(entryBuilder));
    }
    featureEntries.accept(entries);
    entries.forEach(category::addEntry);

    builder.setSavingRunnable(() -> saveMutableConfig(mutable, gameplayEditable));
    Screen screen = builder.build();
    currentConfigScreen = screen;
    return screen;
  }

  /**
   * Replace default footer with explicit Back/Save buttons.
   */
  private static void addFeatureFooterButtons(Screen screen, Screen parent, MutableConfig mutable,
      boolean gameplayEditable) {
    int width = getScreenDimension(screen, "width");
    int height = getScreenDimension(screen, "height");
    int buttonWidth = 118;
    int spacing = 8;
    int totalWidth = (buttonWidth * 2) + spacing;
    int left = (width - totalWidth) / 2;
    int y = height - 28;

    Button backButton = Button.builder(Component.literal("Back"),
        ignored -> ClientRuntimeCompat.setScreen(Minecraft.getInstance(), parent))
        .bounds(left, y, buttonWidth, 20)
        .build();

    Button saveButton = Button.builder(Component.literal("Save"), ignored -> {
      if (screen instanceof AbstractConfigScreen configScreen) {
        configScreen.saveAll(false);
      } else {
        saveMutableConfig(mutable, gameplayEditable);
      }
      ClientRuntimeCompat.setScreen(Minecraft.getInstance(), parent);
    })
        .bounds(left + buttonWidth + spacing, y, buttonWidth, 20)
        .build();

    boolean backAdded = addWidgetReflective(screen, backButton);
    boolean saveAdded = addWidgetReflective(screen, saveButton);
    if (backAdded && saveAdded) {
      hideDefaultFooterButtons(screen, y);
    }
  }

  /**
   * Hide overlapping default footer controls near custom footer row.
   */
  private static void hideDefaultFooterButtons(Screen screen, int customFooterY) {
    for (Object child : getScreenChildrenReflective(screen)) {
      if (!(child instanceof Button button)) {
        continue;
      }

      int buttonY = getWidgetY(button);
      if (Math.abs(buttonY - customFooterY) <= 3) {
        button.visible = false;
        button.active = false;
      }
    }
  }

  /**
   * Pull child widgets through reflection so this screen stays resilient across API shifts.
   */
  private static List<?> getScreenChildrenReflective(Screen screen) {
    Class<?> current = screen.getClass();
    while (current != null) {
      try {
        for (Method method : current.getDeclaredMethods()) {
          if (method.getParameterCount() != 0 || !List.class.isAssignableFrom(method.getReturnType())) {
            continue;
          }
          method.setAccessible(true);
          Object result = method.invoke(screen);
          if (result instanceof List<?> list) {
            return list;
          }
        }
        current = current.getSuperclass();
      } catch (ReflectiveOperationException exception) {
        current = current.getSuperclass();
      }
    }
    return List.of();
  }

  /**
   * Read widget Y coordinate using method-first then field fallback.
   */
  private static int getWidgetY(Button button) {
    try {
      for (Method method : button.getClass().getMethods()) {
        if (method.getParameterCount() != 0 || method.getReturnType() != int.class) {
          continue;
        }
        Object value = method.invoke(button);
        if (value instanceof Integer y && y > Integer.MIN_VALUE / 2) {
          return y;
        }
      }
    } catch (ReflectiveOperationException ignored) {
      // fall back to field lookup below
    }

    try {
      Class<?> current = button.getClass();
      while (current != null) {
        for (java.lang.reflect.Field field : current.getDeclaredFields()) {
          if (field.getType() != int.class) {
            continue;
          }
          field.setAccessible(true);
          int value = field.getInt(button);
          if (value > Integer.MIN_VALUE / 2) {
            return value;
          }
        }
        current = current.getSuperclass();
      }
    } catch (ReflectiveOperationException ignored) {
      // If we cannot determine position, return a non-matching value.
    }

    return Integer.MIN_VALUE;
  }

  /**
   * Resolve private screen dimension field (width/height) reflectively.
   */
  private static int getScreenDimension(Screen screen, String fieldName) {
    // Prefer named accessors when present.
    try {
      Method method = screen.getClass().getMethod(fieldName);
      Object value = method.invoke(screen);
      if (value instanceof Integer dimension && dimension > 0) {
        return dimension;
      }
    } catch (ReflectiveOperationException ignored) {
      // Fall through to additional strategies.
    }

    try {
      String accessor = "width".equals(fieldName) ? "getWidth" : "getHeight";
      Method method = screen.getClass().getMethod(accessor);
      Object value = method.invoke(screen);
      if (value instanceof Integer dimension && dimension > 0) {
        return dimension;
      }
    } catch (ReflectiveOperationException ignored) {
      // Fall through to field lookup.
    }

    try {
      Class<?> current = screen.getClass();
      while (current != null) {
        try {
          java.lang.reflect.Field field = current.getDeclaredField(fieldName);
          field.setAccessible(true);
          int dimension = field.getInt(screen);
          if (dimension > 0) {
            return dimension;
          }
          break;
        } catch (NoSuchFieldException ignored) {
          current = current.getSuperclass();
        }
      }
    } catch (ReflectiveOperationException ignored) {
      // Fall through to window fallback.
    }

    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.getWindow() != null) {
      return "width".equals(fieldName)
          ? minecraft.getWindow().getGuiScaledWidth()
          : minecraft.getWindow().getGuiScaledHeight();
    }

    // Last resort conservative defaults to keep UI code alive instead of crashing.
    return "width".equals(fieldName) ? 320 : 240;
  }

  /**
   * Add widget through whichever add-method exists for this runtime.
   */
  private static boolean addWidgetReflective(Screen screen, Button button) {
    Method addMethod = findCompatibleWidgetAddMethod(screen.getClass(), button.getClass());
    if (addMethod == null) {
      return false;
    }

    try {
      addMethod.setAccessible(true);
      addMethod.invoke(screen, button);
      return true;
    } catch (ReflectiveOperationException | RuntimeException exception) {
      return false;
    }
  }

  /**
   * Locate a compatible one-arg screen widget insertion method.
   */
  private static Method findCompatibleWidgetAddMethod(Class<?> screenClass, Class<?> widgetClass) {
    String[] candidateNames = {
        "addRenderableWidget",
        "addDrawableChild",
        "addSelectableChild",
        "addDrawable"
    };

    Class<?> current = screenClass;
    while (current != null) {
      Method[] methods = current.getDeclaredMethods();
      for (String candidateName : candidateNames) {
        for (Method method : methods) {
          if (!candidateName.equals(method.getName())) {
            continue;
          }
          Class<?>[] parameterTypes = method.getParameterTypes();
          if (parameterTypes.length == 1 && parameterTypes[0].isAssignableFrom(widgetClass)) {
            return method;
          }
        }
      }

      // Mapping-safe fallback: locate any one-arg instance method that can accept the widget.
      for (Method method : methods) {
        if (java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
          continue;
        }
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 1 || !parameterTypes[0].isAssignableFrom(widgetClass)) {
          continue;
        }

        Class<?> returnType = method.getReturnType();
        if (returnType == Void.TYPE || returnType == Object.class || returnType.isAssignableFrom(widgetClass)) {
          return method;
        }
      }

      current = current.getSuperclass();
    }
    return null;
  }

  /**
   * Persist mutable edits to config roots and push client sync packet.
   */
  private static void saveMutableConfig(MutableConfig mutable, boolean gameplayEditable) {
    currentClientConfig = mutable.toClientRootConfig(currentClientConfig);
    MAConfig_Base.setClientRootConfig(currentClientConfig);
    LogUtils.applyConfiguredLogging();
    if (gameplayEditable) {
      currentServerConfig = mutable.toServerRootConfig(currentServerConfig);
      MAConfig_Base.setServerRootConfig(currentServerConfig);
    } else {
      currentServerConfig = MAConfig_Base.getServerRootConfig();
    }
    sendClientSync(currentClientConfig);
  }

  /**
   * Build Captivation feature settings screen.
   */
  private static Screen createCaptivationScreen(Screen parent, MutableConfig mutable, boolean gameplayEditable) {
    return createFeatureConfigScreen(parent, mutable, gameplayEditable, "Captivation", entries -> {
      ConfigEntryBuilder entryBuilder = ConfigEntryBuilder.create();
      addSectionHeading(entries, entryBuilder, "General");
      addGameplayBoolean(entries, entryBuilder, "Enabled", "Enable or disable Captivation.", mutable.captivationEnabled,
          currentServerConfig.captivation().enabled(), gameplayEditable, value -> mutable.captivationEnabled = value);
      addGameplayBoolean(entries, entryBuilder, "Allow In GUI", "Allow Captivation item controls in the game UI.",
          mutable.captivationAllowInGui, currentServerConfig.captivation().allowInGUI(), gameplayEditable,
          value -> mutable.captivationAllowInGui = value);
      addSectionHeading(entries, entryBuilder, "Radius");
      addGameplayInt(entries, entryBuilder, "Horizontal Radius", "Horizontal search radius for item attraction.",
          mutable.captivationRadiusHorizontal, currentServerConfig.captivation().radiusHorizontal(), 1, 128,
          gameplayEditable, value -> mutable.captivationRadiusHorizontal = value);
      addGameplayInt(entries, entryBuilder, "Vertical Radius", "Vertical search radius for item attraction.",
          mutable.captivationRadiusVertical, currentServerConfig.captivation().radiusVertical(), 1, 128,
          gameplayEditable, value -> mutable.captivationRadiusVertical = value);
      addSectionHeading(entries, entryBuilder, "Filtering");
      addGameplayBoolean(entries, entryBuilder, "Whitelist Mode", "Treat list entries as allowed items when enabled.",
          mutable.captivationWhitelist, currentServerConfig.captivation().isWhitelist(), gameplayEditable,
          value -> mutable.captivationWhitelist = value);
      addGameplayBoolean(entries, entryBuilder, "Unconditional Blacklist",
          "Always block listed items regardless of context.", mutable.captivationUnconditionalBlacklist,
          currentServerConfig.captivation().unconditionalBlacklist(), gameplayEditable,
          value -> mutable.captivationUnconditionalBlacklist = value);
      addGameplayStringList(entries, entryBuilder, "Blacklist", "Item ids used by Captivation filtering.",
          mutable.captivationBlacklist, currentServerConfig.captivation().blacklist(), gameplayEditable,
          value -> mutable.captivationBlacklist = value);
    });
  }

  /**
   * Build Cropination feature settings screen.
   */
  private static Screen createCropinationScreen(Screen parent, MutableConfig mutable, boolean gameplayEditable) {
    return createFeatureConfigScreen(parent, mutable, gameplayEditable, "Cropination", entries -> {
      ConfigEntryBuilder entryBuilder = ConfigEntryBuilder.create();
      addSectionHeading(entries, entryBuilder, "General");
      addGameplayBoolean(entries, entryBuilder, "Enabled", "Enable or disable Cropination.", mutable.cropinationEnabled,
          currentServerConfig.cropination().enabled(), gameplayEditable, value -> mutable.cropinationEnabled = value);
      addGameplayBoolean(entries, entryBuilder, "Harvest Seeds", "Allow Cropination to collect seeds while harvesting.",
          mutable.cropinationHarvestSeeds, currentServerConfig.cropination().harvestSeeds(), gameplayEditable,
          value -> mutable.cropinationHarvestSeeds = value);
    });
  }

  /**
   * Build Cultivation feature settings screen.
   */
  private static Screen createCultivationScreen(Screen parent, MutableConfig mutable, boolean gameplayEditable) {
    return createFeatureConfigScreen(parent, mutable, gameplayEditable, "Cultivation", entries -> {
      ConfigEntryBuilder entryBuilder = ConfigEntryBuilder.create();
      addSectionHeading(entries, entryBuilder, "General");
      addGameplayBoolean(entries, entryBuilder, "Enabled", "Enable or disable Cultivation.", mutable.cultivationEnabled,
          currentServerConfig.cultivation().enabled(), gameplayEditable, value -> mutable.cultivationEnabled = value);
      addGameplayInt(entries, entryBuilder, "Hydration Distance", "Maximum distance for hydration checks.",
          mutable.cultivationHydrationDistance, currentServerConfig.cultivation().hydrationDistance(), 1, 16,
          gameplayEditable, value -> mutable.cultivationHydrationDistance = value);
    });
  }

  /**
   * Build Excavation feature settings screen.
   */
  private static Screen createExcavationScreen(Screen parent, MutableConfig mutable, boolean gameplayEditable) {
    return createFeatureConfigScreen(parent, mutable, gameplayEditable, "Excavation", entries -> {
      ConfigEntryBuilder entryBuilder = ConfigEntryBuilder.create();
      addSectionHeading(entries, entryBuilder, "General");
      addGameplayBoolean(entries, entryBuilder, "Enabled", "Enable or disable Excavation.", mutable.excavationEnabled,
          currentServerConfig.excavation().enabled(), gameplayEditable, value -> mutable.excavationEnabled = value);
      addGameplayBoolean(entries, entryBuilder, "Toggle Mode", "Use Excavation as a toggle instead of hold behavior.",
          mutable.excavationToggleMode, currentServerConfig.excavation().toggleMode(), gameplayEditable,
          value -> mutable.excavationToggleMode = value);
      addGameplayBoolean(entries, entryBuilder, "Ignore Block Variants",
          "Ignore state variants when matching excavation targets.", mutable.excavationIgnoreBlockVariants,
          currentServerConfig.excavation().ignoreBlockVariants(), gameplayEditable,
          value -> mutable.excavationIgnoreBlockVariants = value);
      addSectionHeading(entries, entryBuilder, "Dimensions");
      addGameplayInt(entries, entryBuilder, "Width", "Excavation width in blocks.", mutable.excavationRadiusHorizontal,
          currentServerConfig.excavation().width(), 1, 127, gameplayEditable,
          value -> mutable.excavationRadiusHorizontal = value);
      addGameplayInt(entries, entryBuilder, "Height", "Excavation height in blocks.", mutable.excavationRadiusVertical,
          currentServerConfig.excavation().height(), 1, 127, gameplayEditable,
          value -> mutable.excavationRadiusVertical = value);
      addGameplayInt(entries, entryBuilder, "Depth", "Excavation depth in blocks.", mutable.excavationDepth,
          currentServerConfig.excavation().depth(), 1, 64, gameplayEditable, value -> mutable.excavationDepth = value);
      addGameplayInt(entries, entryBuilder, "Processes Per Tick", "Maximum excavation work units processed per tick.",
          mutable.excavationProcessesPerTick, currentServerConfig.excavation().processesPerTick(), 1, 512,
          gameplayEditable, value -> mutable.excavationProcessesPerTick = value);
      addSectionHeading(entries, entryBuilder, "Filtering");
      addGameplayBoolean(entries, entryBuilder, "Block Whitelist Mode",
          "Treat the block list as a whitelist when enabled.", mutable.excavationBlockWhitelist,
          currentServerConfig.excavation().isBlockWhitelist(), gameplayEditable,
          value -> mutable.excavationBlockWhitelist = value);
      addGameplayStringList(entries, entryBuilder, "Block List", "Block ids used by Excavation filtering.",
          mutable.excavationBlockBlacklist, currentServerConfig.excavation().blockBlacklist(), gameplayEditable,
          value -> mutable.excavationBlockBlacklist = value);
    });
  }

  /**
   * Build Pathanation feature settings screen.
   */
  private static Screen createPathanationScreen(Screen parent, MutableConfig mutable, boolean gameplayEditable) {
    return createFeatureConfigScreen(parent, mutable, gameplayEditable, "Pathanation", entries -> {
      ConfigEntryBuilder entryBuilder = ConfigEntryBuilder.create();
      addSectionHeading(entries, entryBuilder, "General");
      addGameplayBoolean(entries, entryBuilder, "Enabled", "Enable or disable Pathanation.", mutable.pathanationEnabled,
          currentServerConfig.pathanation().enabled(), gameplayEditable, value -> mutable.pathanationEnabled = value);
      addGameplayInt(entries, entryBuilder, "Path Length", "Maximum path length when Pathanation runs.",
          mutable.pathanationTargetBlockRange, currentServerConfig.pathanation().targetBlockRange(), 1, 64,
          gameplayEditable, value -> mutable.pathanationTargetBlockRange = value);
      addGameplayInt(entries, entryBuilder, "Path Width", "Configured path width.", mutable.pathanationPathWidth,
          currentServerConfig.pathanation().pathWidth(), 1, 9, gameplayEditable,
          value -> mutable.pathanationPathWidth = value);
    });
  }

  /**
   * Build Illumination feature settings screen.
   */
  private static Screen createIlluminationScreen(Screen parent, MutableConfig mutable, boolean gameplayEditable) {
    return createFeatureConfigScreen(parent, mutable, gameplayEditable, "Illumination", entries -> {
      ConfigEntryBuilder entryBuilder = ConfigEntryBuilder.create();
      addSectionHeading(entries, entryBuilder, "General");
      addGameplayBoolean(entries, entryBuilder, "Enabled", "Enable or disable Illumination.",
          mutable.illuminationEnabled, currentServerConfig.illumination().enabled(), gameplayEditable,
          value -> mutable.illuminationEnabled = value);
      addGameplayBoolean(entries, entryBuilder, "Use Block Light", "Use block light values for illumination checks.",
          mutable.illuminationUseBlockLight, currentServerConfig.illumination().useBlockLight(), gameplayEditable,
          value -> mutable.illuminationUseBlockLight = value);
      addSectionHeading(entries, entryBuilder, "Range");
      addGameplayInt(entries, entryBuilder, "Horizontal Radius", "Horizontal radius for illumination scans.",
          mutable.illuminationRadiusHorizontal, currentServerConfig.illumination().radiusHorizontal(), 1, 64,
          gameplayEditable, value -> mutable.illuminationRadiusHorizontal = value);
      addGameplayInt(entries, entryBuilder, "Vertical Radius", "Vertical radius for illumination scans.",
          mutable.illuminationRadiusVertical, currentServerConfig.illumination().radiusVertical(), 1, 64,
          gameplayEditable, value -> mutable.illuminationRadiusVertical = value);
      addGameplayInt(entries, entryBuilder, "Lowest Light Level", "Threshold that triggers light placement.",
          mutable.illuminationLowestLightLevel, currentServerConfig.illumination().lowestLightLevel(), 0, 15,
          gameplayEditable, value -> mutable.illuminationLowestLightLevel = value);
    });
  }

  /**
   * Build Lumbination feature settings screen.
   */
  private static Screen createLumbinationScreen(Screen parent, MutableConfig mutable, boolean gameplayEditable) {
    return createFeatureConfigScreen(parent, mutable, gameplayEditable, "Lumbination", entries -> {
      ConfigEntryBuilder entryBuilder = ConfigEntryBuilder.create();
      addSectionHeading(entries, entryBuilder, "General");
      addGameplayBoolean(entries, entryBuilder, "Enabled", "Enable or disable Lumbination.", mutable.lumbinationEnabled,
          currentServerConfig.lumbination().enabled(), gameplayEditable, value -> mutable.lumbinationEnabled = value);
      addGameplayBoolean(entries, entryBuilder, "Chop Tree Below", "Allow chopping from below the trunk.",
          mutable.lumbinationChopTreeBelow, currentServerConfig.lumbination().chopTreeBelow(), gameplayEditable,
          value -> mutable.lumbinationChopTreeBelow = value);
      addGameplayBoolean(entries, entryBuilder, "Destroy Leaves", "Allow Lumbination to break matching leaves.",
          mutable.lumbinationDestroyLeaves, currentServerConfig.lumbination().destroyLeaves(), gameplayEditable,
          value -> mutable.lumbinationDestroyLeaves = value);
      addGameplayBoolean(entries, entryBuilder, "Leaves Affect Durability",
          "Apply durability loss for processed leaves.", mutable.lumbinationLeavesAffectDurability,
          currentServerConfig.lumbination().leavesAffectDurability(), gameplayEditable,
          value -> mutable.lumbinationLeavesAffectDurability = value);
      addGameplayBoolean(entries, entryBuilder, "Replant Saplings", "Replant saplings after tree harvesting.",
          mutable.lumbinationReplantSaplings, currentServerConfig.lumbination().replantSaplings(), gameplayEditable,
          value -> mutable.lumbinationReplantSaplings = value);
      addGameplayBoolean(entries, entryBuilder, "Canopy Tool", "Use canopy tool behavior when harvesting leaves.",
          mutable.lumbinationUseCanopyTool, currentServerConfig.lumbination().useCanopyTool(), gameplayEditable,
          value -> mutable.lumbinationUseCanopyTool = value);
      addGameplayBoolean(entries, entryBuilder, "Ignore Player Placed Leaves",
          "Skip leaves placed by players (persistent leaves) when clearing the canopy.",
          mutable.lumbinationIgnorePlayerPlacedLeaves, currentServerConfig.lumbination().ignorePlayerPlacedLeaves(),
          gameplayEditable, value -> mutable.lumbinationIgnorePlayerPlacedLeaves = value);
      addSectionHeading(entries, entryBuilder, "Range");
      addGameplayInt(entries, entryBuilder, "Max Trunk Range", "Maximum trunk traversal range.",
          mutable.lumbinationMaxTrunkRange, currentServerConfig.lumbination().maxTrunkRange(), 1, 128, gameplayEditable,
          value -> mutable.lumbinationMaxTrunkRange = value);
      addGameplayInt(entries, entryBuilder, "Max Leaf Range", "Maximum leaf traversal range.",
          mutable.lumbinationMaxLeafRange, currentServerConfig.lumbination().maxLeafRange(), 1, 32, gameplayEditable,
          value -> mutable.lumbinationMaxLeafRange = value);
      addGameplayInt(entries, entryBuilder, "Processes Per Tick", "Maximum lumbination work units processed per tick.",
          mutable.lumbinationProcessesPerTick, currentServerConfig.lumbination().processesPerTick(), 1, 512,
          gameplayEditable, value -> mutable.lumbinationProcessesPerTick = value);
      addSectionHeading(entries, entryBuilder, "Filtering");
      addGameplayStringList(entries, entryBuilder, "Logs", "Log block ids used by Lumbination filtering.",
          mutable.lumbinationLogs, currentServerConfig.lumbination().logs(), gameplayEditable,
          value -> mutable.lumbinationLogs = value);
      addGameplayStringList(entries, entryBuilder, "Leaves", "Leaf block ids used by Lumbination filtering.",
          mutable.lumbinationLeaves, currentServerConfig.lumbination().leaves(), gameplayEditable,
          value -> mutable.lumbinationLeaves = value);
      addGameplayStringList(entries, entryBuilder, "Axes", "Item ids considered valid axes for Lumbination.",
          mutable.lumbinationAxes, currentServerConfig.lumbination().axes(), gameplayEditable,
          value -> mutable.lumbinationAxes = value);
    });
  }

  /**
   * Build Shaftanation feature settings screen.
   */
  private static Screen createShaftanationScreen(Screen parent, MutableConfig mutable, boolean gameplayEditable) {
    return createFeatureConfigScreen(parent, mutable, gameplayEditable, "Shaftanation", entries -> {
      ConfigEntryBuilder entryBuilder = ConfigEntryBuilder.create();
      addSectionHeading(entries, entryBuilder, "General");
      addGameplayBoolean(entries, entryBuilder, "Enabled", "Enable or disable Shaftanation.",
          mutable.shaftanationEnabled, currentServerConfig.shaftanation().enabled(), gameplayEditable,
          value -> mutable.shaftanationEnabled = value);
      addGameplayInt(entries, entryBuilder, "Depth", "Maximum depth for generated shafts.",
          mutable.shaftanationMaxDepth, currentServerConfig.shaftanation().depth(), 1, 256, gameplayEditable,
          value -> mutable.shaftanationMaxDepth = value);
      addGameplayInt(entries, entryBuilder, "Processes Per Tick", "Maximum shaftanation work units processed per tick.",
          mutable.shaftanationProcessesPerTick, currentServerConfig.shaftanation().processesPerTick(), 1, 512,
          gameplayEditable, value -> mutable.shaftanationProcessesPerTick = value);
      addSectionHeading(entries, entryBuilder, "Shape");
      addGameplayInt(entries, entryBuilder, "Width", "Configured shaft width.", mutable.shaftanationShaftWidth,
          currentServerConfig.shaftanation().width(), 1, 7, gameplayEditable,
          value -> mutable.shaftanationShaftWidth = value);
      addGameplayInt(entries, entryBuilder, "Height", "Configured shaft height.", mutable.shaftanationShaftHeight,
          currentServerConfig.shaftanation().height(), 1, 5, gameplayEditable,
          value -> mutable.shaftanationShaftHeight = value);
      addGameplayEnum(entries, entryBuilder, "Torch Placement", "Torch placement strategy for shaft runs.",
          mutable.shaftanationTorchPlacement, currentServerConfig.shaftanation().torchPlacement(), TorchPlacement.class,
          gameplayEditable, value -> mutable.shaftanationTorchPlacement = value);
    });
  }

  /**
   * Build Substitution feature settings screen.
   */
  private static Screen createSubstitutionScreen(Screen parent, MutableConfig mutable, boolean gameplayEditable) {
    return createFeatureConfigScreen(parent, mutable, gameplayEditable, "Substitution", entries -> {
      ConfigEntryBuilder entryBuilder = ConfigEntryBuilder.create();
      addSectionHeading(entries, entryBuilder, "General");
      addGameplayBoolean(entries, entryBuilder, "Enabled", "Enable or disable Substitution.",
          mutable.substitutionEnabled, currentServerConfig.substitution().enabled(), gameplayEditable,
          value -> mutable.substitutionEnabled = value);
      addGameplayBoolean(entries, entryBuilder, "Allow Mending", "Allow mending tools during substitution.",
          mutable.substitutionAllowMending, currentServerConfig.substitution().allowMending(), gameplayEditable,
          value -> mutable.substitutionAllowMending = value);
      addGameplayBoolean(entries, entryBuilder, "Prioritize Silk Touch",
          "Prefer silk-touch tools when selecting substitutions.", mutable.substitutionPrioritizeSilkTouch,
          currentServerConfig.substitution().prioritizeSilkTouch(), gameplayEditable,
          value -> mutable.substitutionPrioritizeSilkTouch = value);
      addGameplayBoolean(entries, entryBuilder, "Switch Back", "Switch back to the previous tool when possible.",
          mutable.substitutionSwitchBack, currentServerConfig.substitution().switchBack(), gameplayEditable,
          value -> mutable.substitutionSwitchBack = value);
      addGameplayBoolean(entries, entryBuilder, "Favour Fortune", "Prefer fortune-enchanted tools where applicable.",
          mutable.substitutionFavourFortune, currentServerConfig.substitution().favourFortune(), gameplayEditable,
          value -> mutable.substitutionFavourFortune = value);
      addGameplayBoolean(entries, entryBuilder, "Ignore If Valid Tool",
          "Skip substitution when current tool is already valid.", mutable.substitutionIgnoreIfValidTool,
          currentServerConfig.substitution().ignoreIfValidTool(), gameplayEditable,
          value -> mutable.substitutionIgnoreIfValidTool = value);
      addGameplayBoolean(entries, entryBuilder, "Ignore Passive Mobs",
          "Avoid substitution behavior against passive mobs.", mutable.substitutionIgnorePassiveMobs,
          currentServerConfig.substitution().ignorePassiveMobs(), gameplayEditable,
          value -> mutable.substitutionIgnorePassiveMobs = value);
      addSectionHeading(entries, entryBuilder, "Filtering");
      addGameplayStringList(entries, entryBuilder, "Blacklist", "Item ids excluded from substitution.",
          mutable.substitutionBlacklist, currentServerConfig.substitution().blacklist(), gameplayEditable,
          value -> mutable.substitutionBlacklist = value);
      entries.add(entryBuilder
          .startTextDescription(Component.literal("Selection rules are persisted in the server config file."))
          .build());
    });
  }

  /**
   * Build Veination feature settings screen.
   */
  private static Screen createVeinationScreen(Screen parent, MutableConfig mutable, boolean gameplayEditable) {
    return createFeatureConfigScreen(parent, mutable, gameplayEditable, "Veination", entries -> {
      ConfigEntryBuilder entryBuilder = ConfigEntryBuilder.create();
      addSectionHeading(entries, entryBuilder, "General");
      addGameplayBoolean(entries, entryBuilder, "Enabled", "Enable or disable Veination.", mutable.veinationEnabled,
          currentServerConfig.veination().enabled(), gameplayEditable, value -> mutable.veinationEnabled = value);
      addGameplayInt(entries, entryBuilder, "Max Vein Distance", "Maximum scan distance for connected ores.",
          mutable.veinationMaxVeinDistance, currentServerConfig.veination().maxVeinDistance(), 1, 64, gameplayEditable,
          value -> mutable.veinationMaxVeinDistance = value);
      addGameplayBoolean(entries, entryBuilder, "Harvest Without Sneak", "Allow ore vein harvesting without sneaking.",
          mutable.veinationOreHarvestWithoutSneak, currentServerConfig.veination().oreHarvestWithoutSneak(),
          gameplayEditable, value -> mutable.veinationOreHarvestWithoutSneak = value);
      addGameplayBoolean(entries, entryBuilder, "Drop At First Broken Block",
          "Drop ore results at the first mined block.", mutable.veinationDropOresAtFirstBrokenBlock,
          currentServerConfig.veination().dropOresAtFirstBrokenBlock(), gameplayEditable,
          value -> mutable.veinationDropOresAtFirstBrokenBlock = value);
      addSectionHeading(entries, entryBuilder, "Timing");
      addGameplayBoolean(entries, entryBuilder, "Increase Harvest Time Per Ore",
          "Scale mining time based on vein size.", mutable.veinationIncreaseHarvestingTimePerOre,
          currentServerConfig.veination().increaseHarvestingTimePerOre(), gameplayEditable,
          value -> mutable.veinationIncreaseHarvestingTimePerOre = value);
      addGameplayDouble(entries, entryBuilder, "Harvest Time Modifier",
          "Multiplier applied when increasing harvest time.", mutable.veinationHarvestTimeModifier,
          currentServerConfig.veination().increasedHarvestingTimePerOreModifier(), 0.01D, 10.0D, gameplayEditable,
          value -> mutable.veinationHarvestTimeModifier = value);
      addSectionHeading(entries, entryBuilder, "Filtering");
      addGameplayStringList(entries, entryBuilder, "Ores", "Ore block ids eligible for vein mining.",
          mutable.veinationOres, currentServerConfig.veination().ores(), gameplayEditable,
          value -> mutable.veinationOres = value);
      addGameplayStringList(entries, entryBuilder, "Pickaxe Blacklist", "Pickaxe item ids excluded from veination.",
          mutable.veinationPickaxeBlacklist, currentServerConfig.veination().pickaxeBlacklist(), gameplayEditable,
          value -> mutable.veinationPickaxeBlacklist = value);
    });
  }

  /**
   * Build Ventilation feature settings screen.
   */
  private static Screen createVentilationScreen(Screen parent, MutableConfig mutable, boolean gameplayEditable) {
    return createFeatureConfigScreen(parent, mutable, gameplayEditable, "Ventilation", entries -> {
      ConfigEntryBuilder entryBuilder = ConfigEntryBuilder.create();
      addSectionHeading(entries, entryBuilder, "General");
      addGameplayBoolean(entries, entryBuilder, "Enabled", "Enable or disable Ventilation.", mutable.ventilationEnabled,
          currentServerConfig.ventilation().enabled(), gameplayEditable, value -> mutable.ventilationEnabled = value);
      addGameplayBoolean(entries, entryBuilder, "Place Ladders", "Allow ladder placement during ventilation runs.",
          mutable.ventilationPlaceLadders, currentServerConfig.ventilation().placeLadders(), gameplayEditable,
          value -> mutable.ventilationPlaceLadders = value);
      addSectionHeading(entries, entryBuilder, "Dimensions");
      addGameplayInt(entries, entryBuilder, "Width", "Ventilation width in blocks.",
          mutable.ventilationRadiusHorizontal, currentServerConfig.ventilation().width(), 1, 32, gameplayEditable,
          value -> mutable.ventilationRadiusHorizontal = value);
      addGameplayInt(entries, entryBuilder, "Height", "Ventilation height in blocks.",
          mutable.ventilationRadiusVertical, currentServerConfig.ventilation().height(), 1, 64, gameplayEditable,
          value -> mutable.ventilationRadiusVertical = value);
      addGameplayInt(entries, entryBuilder, "Depth", "Ventilation depth in blocks.", mutable.ventilationDepth,
          currentServerConfig.ventilation().depth(), 1, 64, gameplayEditable,
          value -> mutable.ventilationDepth = value);
      addGameplayInt(entries, entryBuilder, "Processes Per Tick", "Maximum ventilation work units processed per tick.",
          mutable.ventilationProcessesPerTick, currentServerConfig.ventilation().processesPerTick(), 1, 512,
          gameplayEditable, value -> mutable.ventilationProcessesPerTick = value);
    });
  }

  private static void addFeatureSubCategory(
      ConfigCategory category,
      ConfigEntryBuilder entryBuilder,
      String featureName,
      boolean enabled,
      boolean editable,
      Consumer<List<AbstractConfigListEntry<?>>> entryCollector) {
    List<AbstractConfigListEntry<?>> entries = new ArrayList<>();
    if (!editable) {
      entries.add(authorityNoticeEntry(entryBuilder));
    }
    entryCollector.accept(entries);

    String status = enabled ? "Enabled" : "Disabled";
    me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder subCategory = entryBuilder.startSubCategory(
        Component.literal(featureName + " [" + status + "]"));
    subCategory.addAll(entries);
    subCategory.setTooltip(Component.literal("Open " + featureName + " settings"));
    subCategory.setExpanded(false);
    category.addEntry(subCategory.build());
  }

  private static void addSectionHeading(
      List<AbstractConfigListEntry<?>> entries,
      ConfigEntryBuilder entryBuilder,
      String heading) {
    entries.add(textDescriptionEntry(entryBuilder, "[" + heading + "]"));
  }

  private static me.shedaniel.clothconfig2.api.AbstractConfigListEntry<?> textDescriptionEntry(
      ConfigEntryBuilder entryBuilder,
      String text) {
    if (hasChatFormattingColorAccessor()) {
      return entryBuilder.startTextDescription(Component.literal(text))
          .build();
    }

    return entryBuilder.startSubCategory(Component.literal(text))
        .setExpanded(false)
        .build();
  }

  private static void addReadOnlyEntry(
      List<AbstractConfigListEntry<?>> entries,
      ConfigEntryBuilder entryBuilder,
      String text) {
    entries.add(textDescriptionEntry(entryBuilder, text));
  }

  private static void addReadOnlyEntry(
      ConfigCategory category,
      ConfigEntryBuilder entryBuilder,
      String text) {
    category.addEntry(textDescriptionEntry(entryBuilder, text));
  }

  private static void addGameplayBoolean(
      List<AbstractConfigListEntry<?>> entries,
      ConfigEntryBuilder entryBuilder,
      String label,
      String description,
      boolean currentValue,
      boolean defaultValue,
      boolean editable,
      Consumer<Boolean> consumer) {
    if (!editable) {
      addReadOnlyEntry(entries, entryBuilder, label + ": " + (currentValue ? "Enabled" : "Disabled"));
      return;
    }

    entries.add(entryBuilder.startBooleanToggle(Component.literal(label), currentValue)
        .setDefaultValue(defaultValue)
        .setTooltip(authorityAwareDescription(description, editable))
        .setSaveConsumer(consumer)
        .build());
  }

  private static void addGameplayInt(
      List<AbstractConfigListEntry<?>> entries,
      ConfigEntryBuilder entryBuilder,
      String label,
      String description,
      int currentValue,
      int defaultValue,
      int min,
      int max,
      boolean editable,
      Consumer<Integer> consumer) {
    if (!editable) {
      addReadOnlyEntry(entries, entryBuilder, label + ": " + currentValue);
      return;
    }

    entries.add(entryBuilder.startIntField(Component.literal(label), currentValue)
        .setDefaultValue(defaultValue)
        .setMin(min)
        .setMax(max)
        .setTooltip(authorityAwareDescription(description, editable))
        .setSaveConsumer(consumer)
        .build());
  }

  private static void addGameplayDouble(
      List<AbstractConfigListEntry<?>> entries,
      ConfigEntryBuilder entryBuilder,
      String label,
      String description,
      double currentValue,
      double defaultValue,
      double min,
      double max,
      boolean editable,
      Consumer<Double> consumer) {
    if (!editable) {
      addReadOnlyEntry(entries, entryBuilder, label + ": " + currentValue);
      return;
    }

    entries.add(entryBuilder.startDoubleField(Component.literal(label), currentValue)
        .setDefaultValue(defaultValue)
        .setMin(min)
        .setMax(max)
        .setTooltip(authorityAwareDescription(description, editable))
        .setSaveConsumer(consumer)
        .build());
  }

  private static <T extends Enum<T>> void addGameplayEnum(
      List<AbstractConfigListEntry<?>> entries,
      ConfigEntryBuilder entryBuilder,
      String label,
      String description,
      T currentValue,
      T defaultValue,
      Class<T> enumClass,
      boolean editable,
      Consumer<T> consumer) {
    if (!editable) {
      addReadOnlyEntry(entries, entryBuilder, label + ": " + currentValue.name());
      return;
    }

    entries.add(entryBuilder.startEnumSelector(Component.literal(label), enumClass, currentValue)
        .setDefaultValue(defaultValue)
        .setTooltip(authorityAwareDescription(description, editable))
        .setSaveConsumer(consumer)
        .build());
  }

  private static void addGameplayStringList(
      List<AbstractConfigListEntry<?>> entries,
      ConfigEntryBuilder entryBuilder,
      String label,
      String description,
      List<String> currentValue,
      List<String> defaultValue,
      boolean editable,
      Consumer<List<String>> consumer) {
    if (!editable) {
      addReadOnlyEntry(entries, entryBuilder,
          label + ": " + (currentValue.isEmpty() ? "[]" : String.join(", ", currentValue)));
      return;
    }

    entries.add(entryBuilder.startStrList(Component.literal(label), currentValue)
        .setDefaultValue(defaultValue)
        .setTooltip(authorityAwareDescription(description, editable))
        .setSaveConsumer(consumer)
        .build());
  }

  private static void addGameplayBoolean(
      ConfigCategory category,
      ConfigEntryBuilder entryBuilder,
      String label,
      String description,
      boolean currentValue,
      boolean defaultValue,
      boolean editable,
      Consumer<Boolean> consumer) {
    if (!editable) {
      addReadOnlyEntry(category, entryBuilder, label + ": " + (currentValue ? "Enabled" : "Disabled"));
      return;
    }

    category.addEntry(entryBuilder.startBooleanToggle(Component.literal(label), currentValue)
        .setDefaultValue(defaultValue)
        .setTooltip(authorityAwareDescription("Enable or disable " + label + ".", editable))
        .setSaveConsumer(consumer)
        .build());
  }

  private static void addGameplayInt(
      ConfigCategory category,
      ConfigEntryBuilder entryBuilder,
      String label,
      String description,
      int currentValue,
      int defaultValue,
      int min,
      int max,
      boolean editable,
      Consumer<Integer> consumer) {
    if (!editable) {
      addReadOnlyEntry(category, entryBuilder, label + ": " + currentValue);
      return;
    }

    category.addEntry(entryBuilder.startIntField(Component.literal(label), currentValue)
        .setDefaultValue(defaultValue)
        .setMin(min)
        .setMax(max)
        .setTooltip(authorityAwareDescription(description, editable))
        .setSaveConsumer(consumer)
        .build());
  }

  private static me.shedaniel.clothconfig2.api.AbstractConfigListEntry<?> authorityNoticeEntry(
      ConfigEntryBuilder entryBuilder) {
    return textDescriptionEntry(entryBuilder,
        "Gameplay settings are controlled by the server while connected to remote multiplayer.");
  }

  /**
   * Append server-authority note when gameplay values are read-only.
   */
  private static Component authorityAwareDescription(String baseDescription, boolean gameplayEditable) {
    if (gameplayEditable) {
      return Component.literal(baseDescription);
    }
    return Component.literal(baseDescription + " Controlled by the server in multiplayer.");
  }

  /**
   * Determine whether gameplay config is locally editable in current session.
   */
  private static boolean isGameplayEditable() {
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.getConnection() == null) {
      return true;
    }
    return minecraft.hasSingleplayerServer();
  }

  private static boolean hasChatFormattingColorAccessor() {
    try {
      ChatFormatting.class.getMethod("getColor");
      return true;
    } catch (NoSuchMethodException ignored) {
      return false;
    }
  }

  /**
   * sendClientSync exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  private static void sendClientSync(MAClientRootConfig updatedConfig) {
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.getConnection() == null || minecraft.player == null) {
      return;
    }

    long playerId = minecraft.player == null ? 0L : minecraft.player.getUUID().getLeastSignificantBits();
    PlayerStateSyncPacket packet = new PlayerStateSyncPacket(
        playerId,
        updatedConfig,
        MAServerRootConfig.defaults());

    if (invokeStaticSingleArgMethod(
        "net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking",
        "send",
        packet)) {
      return;
    }

    invokeStaticSingleArgMethod(
        "net.neoforged.neoforge.client.network.ClientPacketDistributor",
        "sendToServer",
        packet);
  }

  /**
   * Invoke optional static networking helper method with one argument.
   */
  private static boolean invokeStaticSingleArgMethod(String className, String methodName, Object argument) {
    try {
      Class<?> owner = Class.forName(className);
      for (Method method : owner.getMethods()) {
        if (!methodName.equals(method.getName()) || method.getParameterCount() != 1) {
          continue;
        }

        Class<?> parameterType = method.getParameterTypes()[0];
        if (!parameterType.isAssignableFrom(argument.getClass())) {
          continue;
        }

        method.invoke(null, argument);
        return true;
      }
    } catch (ReflectiveOperationException | LinkageError ignored) {
      // Optional cross-loader client sync should never crash the config UI.
    }

    return false;
  }

  /**
   * MutableConfig keeps this part of MinersAdvantage running without turning server ticks into confetti.
   * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
   */
  private static final class MutableConfig {
    private boolean disableParticleEffects;
    private boolean debugLogging;
    private int outlineForegroundColor;
    private int outlineSeeThroughColor;

    private boolean tpsGuard;
    private boolean gatherDrops;
    private boolean autoIlluminate;
    private boolean mineVeins;
    private int blocksPerTick;
    private boolean enableTickDelay;
    private int tickDelay;
    private int blockRadius;

    private boolean captivationEnabled;
    private boolean captivationAllowInGui;
    private int captivationRadiusHorizontal;
    private int captivationRadiusVertical;
    private boolean captivationWhitelist;
    private boolean captivationUnconditionalBlacklist;
    private List<String> captivationBlacklist;

    private boolean cropinationEnabled;
    private boolean cropinationHarvestSeeds;

    private boolean cultivationEnabled;
    private int cultivationHydrationDistance;

    private boolean excavationEnabled;
    private int excavationRadiusHorizontal;
    private int excavationRadiusVertical;
    private int excavationDepth;
    private int excavationProcessesPerTick;
    private boolean excavationToggleMode;
    private boolean excavationIgnoreBlockVariants;
    private boolean excavationBlockWhitelist;
    private List<String> excavationBlockBlacklist;

    private boolean pathanationEnabled;
    private int pathanationTargetBlockRange;
    private int pathanationPathWidth;

    private boolean illuminationEnabled;
    private int illuminationRadiusHorizontal;
    private int illuminationRadiusVertical;
    private int illuminationLowestLightLevel;
    private boolean illuminationUseBlockLight;

    private boolean lumbinationEnabled;
    private int lumbinationMaxTrunkRange;
    private int lumbinationMaxLeafRange;
    private int lumbinationProcessesPerTick;
    private boolean lumbinationChopTreeBelow;
    private boolean lumbinationDestroyLeaves;
    private boolean lumbinationLeavesAffectDurability;
    private boolean lumbinationReplantSaplings;
    private boolean lumbinationUseCanopyTool;
    private boolean lumbinationIgnorePlayerPlacedLeaves;
    private List<String> lumbinationLogs;
    private List<String> lumbinationLeaves;
    private List<String> lumbinationAxes;

    private boolean shaftanationEnabled;
    private int shaftanationMaxDepth;
    private int shaftanationProcessesPerTick;
    private int shaftanationShaftWidth;
    private int shaftanationShaftHeight;
    private TorchPlacement shaftanationTorchPlacement;

    private boolean substitutionEnabled;
    private boolean substitutionAllowMending;
    private boolean substitutionPrioritizeSilkTouch;
    private boolean substitutionSwitchBack;
    private boolean substitutionFavourFortune;
    private boolean substitutionIgnoreIfValidTool;
    private boolean substitutionIgnorePassiveMobs;
    private List<String> substitutionBlacklist;

    private boolean veinationEnabled;
    private int veinationMaxVeinDistance;
    private List<String> veinationOres;
    private boolean veinationOreHarvestWithoutSneak;
    private boolean veinationDropOresAtFirstBrokenBlock;
    private boolean veinationIncreaseHarvestingTimePerOre;
    private double veinationHarvestTimeModifier;
    private List<String> veinationPickaxeBlacklist;

    private boolean ventilationEnabled;
    private int ventilationRadiusHorizontal;
    private int ventilationRadiusVertical;
    private int ventilationDepth;
    private int ventilationProcessesPerTick;
    private boolean ventilationPlaceLadders;

    /**
     * MutableConfig exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private MutableConfig(MAClientRootConfig clientConfig, MAServerRootConfig serverConfig) {
      this.disableParticleEffects = clientConfig.client().disableParticleEffects();
      this.debugLogging = clientConfig.client().debugLogging();
      this.outlineForegroundColor = clientConfig.client().outlineForegroundColor();
      this.outlineSeeThroughColor = clientConfig.client().outlineSeeThroughColor();

      MAServerRootConfig config = serverConfig;
      this.tpsGuard = config.common().tpsGuard();
      this.gatherDrops = config.common().gatherDrops();
      this.autoIlluminate = config.common().autoIlluminate();
      this.mineVeins = config.common().mineVeins();
      this.blocksPerTick = config.common().blocksPerTick();
      this.enableTickDelay = config.common().enableTickDelay();
      this.tickDelay = config.common().tickDelay();
      this.blockRadius = config.common().blockRadius();

      this.captivationEnabled = config.captivation().enabled();
      this.captivationAllowInGui = config.captivation().allowInGUI();
      this.captivationRadiusHorizontal = config.captivation().radiusHorizontal();
      this.captivationRadiusVertical = config.captivation().radiusVertical();
      this.captivationWhitelist = config.captivation().isWhitelist();
      this.captivationUnconditionalBlacklist = config.captivation().unconditionalBlacklist();
      this.captivationBlacklist = new ArrayList<>(config.captivation().blacklist());

      this.cropinationEnabled = config.cropination().enabled();
      this.cropinationHarvestSeeds = config.cropination().harvestSeeds();

      this.cultivationEnabled = config.cultivation().enabled();
      this.cultivationHydrationDistance = config.cultivation().hydrationDistance();

      this.excavationEnabled = config.excavation().enabled();
      this.excavationRadiusHorizontal = config.excavation().width();
      this.excavationRadiusVertical = config.excavation().height();
      this.excavationDepth = config.excavation().depth();
      this.excavationProcessesPerTick = config.excavation().processesPerTick();
      this.excavationToggleMode = config.excavation().toggleMode();
      this.excavationIgnoreBlockVariants = config.excavation().ignoreBlockVariants();
      this.excavationBlockWhitelist = config.excavation().isBlockWhitelist();
      this.excavationBlockBlacklist = new ArrayList<>(config.excavation().blockBlacklist());

      this.pathanationEnabled = config.pathanation().enabled();
      this.pathanationTargetBlockRange = config.pathanation().targetBlockRange();
      this.pathanationPathWidth = config.pathanation().pathWidth();

      this.illuminationEnabled = config.illumination().enabled();
      this.illuminationRadiusHorizontal = config.illumination().radiusHorizontal();
      this.illuminationRadiusVertical = config.illumination().radiusVertical();
      this.illuminationLowestLightLevel = config.illumination().lowestLightLevel();
      this.illuminationUseBlockLight = config.illumination().useBlockLight();

      this.lumbinationEnabled = config.lumbination().enabled();
      this.lumbinationMaxTrunkRange = config.lumbination().maxTrunkRange();
      this.lumbinationMaxLeafRange = config.lumbination().maxLeafRange();
      this.lumbinationProcessesPerTick = config.lumbination().processesPerTick();
      this.lumbinationChopTreeBelow = config.lumbination().chopTreeBelow();
      this.lumbinationDestroyLeaves = config.lumbination().destroyLeaves();
      this.lumbinationLeavesAffectDurability = config.lumbination().leavesAffectDurability();
      this.lumbinationReplantSaplings = config.lumbination().replantSaplings();
      this.lumbinationUseCanopyTool = config.lumbination().useCanopyTool();
      this.lumbinationIgnorePlayerPlacedLeaves = config.lumbination().ignorePlayerPlacedLeaves();
      this.lumbinationLogs = new ArrayList<>(config.lumbination().logs());
      this.lumbinationLeaves = new ArrayList<>(config.lumbination().leaves());
      this.lumbinationAxes = new ArrayList<>(config.lumbination().axes());

      this.shaftanationEnabled = config.shaftanation().enabled();
      this.shaftanationMaxDepth = config.shaftanation().depth();
      this.shaftanationProcessesPerTick = config.shaftanation().processesPerTick();
      this.shaftanationShaftWidth = config.shaftanation().width();
      this.shaftanationShaftHeight = config.shaftanation().height();
      this.shaftanationTorchPlacement = config.shaftanation().torchPlacement();

      this.substitutionEnabled = config.substitution().enabled();
      this.substitutionAllowMending = config.substitution().allowMending();
      this.substitutionPrioritizeSilkTouch = config.substitution().prioritizeSilkTouch();
      this.substitutionSwitchBack = config.substitution().switchBack();
      this.substitutionFavourFortune = config.substitution().favourFortune();
      this.substitutionIgnoreIfValidTool = config.substitution().ignoreIfValidTool();
      this.substitutionIgnorePassiveMobs = config.substitution().ignorePassiveMobs();
      this.substitutionBlacklist = new ArrayList<>(config.substitution().blacklist());

      this.veinationEnabled = config.veination().enabled();
      this.veinationMaxVeinDistance = config.veination().maxVeinDistance();
      this.veinationOres = new ArrayList<>(config.veination().ores());
      this.veinationOreHarvestWithoutSneak = config.veination().oreHarvestWithoutSneak();
      this.veinationDropOresAtFirstBrokenBlock = config.veination().dropOresAtFirstBrokenBlock();
      this.veinationIncreaseHarvestingTimePerOre = config.veination().increaseHarvestingTimePerOre();
      this.veinationHarvestTimeModifier = config.veination().increasedHarvestingTimePerOreModifier();
      this.veinationPickaxeBlacklist = new ArrayList<>(config.veination().pickaxeBlacklist());

      this.ventilationEnabled = config.ventilation().enabled();
      this.ventilationRadiusHorizontal = config.ventilation().width();
      this.ventilationRadiusVertical = config.ventilation().height();
      this.ventilationDepth = config.ventilation().depth();
      this.ventilationProcessesPerTick = config.ventilation().processesPerTick();
      this.ventilationPlaceLadders = config.ventilation().placeLadders();
    }

    /**
     * Check if Captivation values differ from defaults.
     */
    private boolean isCaptivationDifferentFromDefaults() {
      uk.co.duelmonster.minersadvantage.common.config.CaptivationConfig defaults = MAServerRootConfig.defaults()
          .captivation();
      return captivationEnabled != defaults.enabled()
          || captivationAllowInGui != defaults.allowInGUI()
          || captivationRadiusHorizontal != defaults.radiusHorizontal()
          || captivationRadiusVertical != defaults.radiusVertical()
          || captivationWhitelist != defaults.isWhitelist()
          || captivationUnconditionalBlacklist != defaults.unconditionalBlacklist()
          || !captivationBlacklist.equals(defaults.blacklist());
    }

    /**
     * Reset Captivation values to defaults.
     */
    private void resetCaptivationToDefaults() {
      uk.co.duelmonster.minersadvantage.common.config.CaptivationConfig defaults = MAServerRootConfig.defaults()
          .captivation();
      captivationEnabled = defaults.enabled();
      captivationAllowInGui = defaults.allowInGUI();
      captivationRadiusHorizontal = defaults.radiusHorizontal();
      captivationRadiusVertical = defaults.radiusVertical();
      captivationWhitelist = defaults.isWhitelist();
      captivationUnconditionalBlacklist = defaults.unconditionalBlacklist();
      captivationBlacklist = new ArrayList<>(defaults.blacklist());
    }

    /**
     * Check if Cropination values differ from defaults.
     */
    private boolean isCropinationDifferentFromDefaults() {
      uk.co.duelmonster.minersadvantage.common.config.CropinationConfig defaults = MAServerRootConfig.defaults()
          .cropination();
      return cropinationEnabled != defaults.enabled()
          || cropinationHarvestSeeds != defaults.harvestSeeds();
    }

    /**
     * Reset Cropination values to defaults.
     */
    private void resetCropinationToDefaults() {
      uk.co.duelmonster.minersadvantage.common.config.CropinationConfig defaults = MAServerRootConfig.defaults()
          .cropination();
      cropinationEnabled = defaults.enabled();
      cropinationHarvestSeeds = defaults.harvestSeeds();
    }

    /**
     * Check if Cultivation values differ from defaults.
     */
    private boolean isCultivationDifferentFromDefaults() {
      uk.co.duelmonster.minersadvantage.common.config.CultivationConfig defaults = MAServerRootConfig.defaults()
          .cultivation();
      return cultivationEnabled != defaults.enabled()
          || cultivationHydrationDistance != defaults.hydrationDistance();
    }

    /**
     * Reset Cultivation values to defaults.
     */
    private void resetCultivationToDefaults() {
      uk.co.duelmonster.minersadvantage.common.config.CultivationConfig defaults = MAServerRootConfig.defaults()
          .cultivation();
      cultivationEnabled = defaults.enabled();
      cultivationHydrationDistance = defaults.hydrationDistance();
    }

    /**
     * Check if Excavation values differ from defaults.
     */
    private boolean isExcavationDifferentFromDefaults() {
      uk.co.duelmonster.minersadvantage.common.config.ExcavationConfig defaults = MAServerRootConfig.defaults()
          .excavation();
      return excavationEnabled != defaults.enabled()
          || excavationRadiusHorizontal != defaults.width()
          || excavationRadiusVertical != defaults.height()
          || excavationDepth != defaults.depth()
          || excavationProcessesPerTick != defaults.processesPerTick()
          || excavationToggleMode != defaults.toggleMode()
          || excavationIgnoreBlockVariants != defaults.ignoreBlockVariants()
          || excavationBlockWhitelist != defaults.isBlockWhitelist()
          || !excavationBlockBlacklist.equals(defaults.blockBlacklist());
    }

    /**
     * Reset Excavation values to defaults.
     */
    private void resetExcavationToDefaults() {
      uk.co.duelmonster.minersadvantage.common.config.ExcavationConfig defaults = MAServerRootConfig.defaults()
          .excavation();
      excavationEnabled = defaults.enabled();
      excavationRadiusHorizontal = defaults.width();
      excavationRadiusVertical = defaults.height();
      excavationDepth = defaults.depth();
      excavationProcessesPerTick = defaults.processesPerTick();
      excavationToggleMode = defaults.toggleMode();
      excavationIgnoreBlockVariants = defaults.ignoreBlockVariants();
      excavationBlockWhitelist = defaults.isBlockWhitelist();
      excavationBlockBlacklist = new ArrayList<>(defaults.blockBlacklist());
    }

    /**
     * Check if Pathanation values differ from defaults.
     */
    private boolean isPathanationDifferentFromDefaults() {
      uk.co.duelmonster.minersadvantage.common.config.PathanationConfig defaults = MAServerRootConfig.defaults()
          .pathanation();
      return pathanationEnabled != defaults.enabled()
          || pathanationTargetBlockRange != defaults.targetBlockRange()
          || pathanationPathWidth != defaults.pathWidth();
    }

    /**
     * Reset Pathanation values to defaults.
     */
    private void resetPathanationToDefaults() {
      uk.co.duelmonster.minersadvantage.common.config.PathanationConfig defaults = MAServerRootConfig.defaults()
          .pathanation();
      pathanationEnabled = defaults.enabled();
      pathanationTargetBlockRange = defaults.targetBlockRange();
      pathanationPathWidth = defaults.pathWidth();
    }

    /**
     * Check if Illumination values differ from defaults.
     */
    private boolean isIlluminationDifferentFromDefaults() {
      uk.co.duelmonster.minersadvantage.common.config.IlluminationConfig defaults = MAServerRootConfig.defaults()
          .illumination();
      return illuminationEnabled != defaults.enabled()
          || illuminationRadiusHorizontal != defaults.radiusHorizontal()
          || illuminationRadiusVertical != defaults.radiusVertical()
          || illuminationLowestLightLevel != defaults.lowestLightLevel()
          || illuminationUseBlockLight != defaults.useBlockLight();
    }

    /**
     * Reset Illumination values to defaults.
     */
    private void resetIlluminationToDefaults() {
      uk.co.duelmonster.minersadvantage.common.config.IlluminationConfig defaults = MAServerRootConfig.defaults()
          .illumination();
      illuminationEnabled = defaults.enabled();
      illuminationRadiusHorizontal = defaults.radiusHorizontal();
      illuminationRadiusVertical = defaults.radiusVertical();
      illuminationLowestLightLevel = defaults.lowestLightLevel();
      illuminationUseBlockLight = defaults.useBlockLight();
    }

    /**
     * Check if Lumbination values differ from defaults.
     */
    private boolean isLumbinationDifferentFromDefaults() {
      uk.co.duelmonster.minersadvantage.common.config.LumbinationConfig defaults = MAServerRootConfig.defaults()
          .lumbination();
      return lumbinationEnabled != defaults.enabled()
          || lumbinationMaxTrunkRange != defaults.maxTrunkRange()
          || lumbinationMaxLeafRange != defaults.maxLeafRange()
          || lumbinationProcessesPerTick != defaults.processesPerTick()
          || lumbinationChopTreeBelow != defaults.chopTreeBelow()
          || lumbinationDestroyLeaves != defaults.destroyLeaves()
          || lumbinationLeavesAffectDurability != defaults.leavesAffectDurability()
          || lumbinationReplantSaplings != defaults.replantSaplings()
          || lumbinationUseCanopyTool != defaults.useCanopyTool()
          || lumbinationIgnorePlayerPlacedLeaves != defaults.ignorePlayerPlacedLeaves()
          || !lumbinationLogs.equals(defaults.logs())
          || !lumbinationLeaves.equals(defaults.leaves())
          || !lumbinationAxes.equals(defaults.axes());
    }

    /**
     * Reset Lumbination values to defaults.
     */
    private void resetLumbinationToDefaults() {
      uk.co.duelmonster.minersadvantage.common.config.LumbinationConfig defaults = MAServerRootConfig.defaults()
          .lumbination();
      lumbinationEnabled = defaults.enabled();
      lumbinationMaxTrunkRange = defaults.maxTrunkRange();
      lumbinationMaxLeafRange = defaults.maxLeafRange();
      lumbinationProcessesPerTick = defaults.processesPerTick();
      lumbinationChopTreeBelow = defaults.chopTreeBelow();
      lumbinationDestroyLeaves = defaults.destroyLeaves();
      lumbinationLeavesAffectDurability = defaults.leavesAffectDurability();
      lumbinationReplantSaplings = defaults.replantSaplings();
      lumbinationUseCanopyTool = defaults.useCanopyTool();
      lumbinationIgnorePlayerPlacedLeaves = defaults.ignorePlayerPlacedLeaves();
      lumbinationLogs = new ArrayList<>(defaults.logs());
      lumbinationLeaves = new ArrayList<>(defaults.leaves());
      lumbinationAxes = new ArrayList<>(defaults.axes());
    }

    /**
     * Check if Shaftanation values differ from defaults.
     */
    private boolean isShaftanationDifferentFromDefaults() {
      uk.co.duelmonster.minersadvantage.common.config.ShaftanationConfig defaults = MAServerRootConfig.defaults()
          .shaftanation();
      return shaftanationEnabled != defaults.enabled()
          || shaftanationMaxDepth != defaults.depth()
          || shaftanationProcessesPerTick != defaults.processesPerTick()
          || shaftanationShaftWidth != defaults.width()
          || shaftanationShaftHeight != defaults.height()
          || shaftanationTorchPlacement != defaults.torchPlacement();
    }

    /**
     * Reset Shaftanation values to defaults.
     */
    private void resetShaftanationToDefaults() {
      uk.co.duelmonster.minersadvantage.common.config.ShaftanationConfig defaults = MAServerRootConfig.defaults()
          .shaftanation();
      shaftanationEnabled = defaults.enabled();
      shaftanationMaxDepth = defaults.depth();
      shaftanationProcessesPerTick = defaults.processesPerTick();
      shaftanationShaftWidth = defaults.width();
      shaftanationShaftHeight = defaults.height();
      shaftanationTorchPlacement = defaults.torchPlacement();
    }

    /**
     * Check if Substitution values differ from defaults.
     */
    private boolean isSubstitutionDifferentFromDefaults() {
      uk.co.duelmonster.minersadvantage.common.config.SubstitutionConfig defaults = MAServerRootConfig.defaults()
          .substitution();
      return substitutionEnabled != defaults.enabled()
          || substitutionAllowMending != defaults.allowMending()
          || substitutionPrioritizeSilkTouch != defaults.prioritizeSilkTouch()
          || substitutionSwitchBack != defaults.switchBack()
          || substitutionFavourFortune != defaults.favourFortune()
          || substitutionIgnoreIfValidTool != defaults.ignoreIfValidTool()
          || substitutionIgnorePassiveMobs != defaults.ignorePassiveMobs()
          || !substitutionBlacklist.equals(defaults.blacklist());
    }

    /**
     * Reset Substitution values to defaults.
     */
    private void resetSubstitutionToDefaults() {
      uk.co.duelmonster.minersadvantage.common.config.SubstitutionConfig defaults = MAServerRootConfig.defaults()
          .substitution();
      substitutionEnabled = defaults.enabled();
      substitutionAllowMending = defaults.allowMending();
      substitutionPrioritizeSilkTouch = defaults.prioritizeSilkTouch();
      substitutionSwitchBack = defaults.switchBack();
      substitutionFavourFortune = defaults.favourFortune();
      substitutionIgnoreIfValidTool = defaults.ignoreIfValidTool();
      substitutionIgnorePassiveMobs = defaults.ignorePassiveMobs();
      substitutionBlacklist = new ArrayList<>(defaults.blacklist());
    }

    /**
     * Check if Veination values differ from defaults.
     */
    private boolean isVeinationDifferentFromDefaults() {
      uk.co.duelmonster.minersadvantage.common.config.VeinationConfig defaults = MAServerRootConfig.defaults()
          .veination();
      return veinationEnabled != defaults.enabled()
          || veinationMaxVeinDistance != defaults.maxVeinDistance()
          || !veinationOres.equals(defaults.ores())
          || veinationOreHarvestWithoutSneak != defaults.oreHarvestWithoutSneak()
          || veinationDropOresAtFirstBrokenBlock != defaults.dropOresAtFirstBrokenBlock()
          || veinationIncreaseHarvestingTimePerOre != defaults.increaseHarvestingTimePerOre()
          || Double.compare(veinationHarvestTimeModifier, defaults.increasedHarvestingTimePerOreModifier()) != 0
          || !veinationPickaxeBlacklist.equals(defaults.pickaxeBlacklist());
    }

    /**
     * Reset Veination values to defaults.
     */
    private void resetVeinationToDefaults() {
      uk.co.duelmonster.minersadvantage.common.config.VeinationConfig defaults = MAServerRootConfig.defaults()
          .veination();
      veinationEnabled = defaults.enabled();
      veinationMaxVeinDistance = defaults.maxVeinDistance();
      veinationOres = new ArrayList<>(defaults.ores());
      veinationOreHarvestWithoutSneak = defaults.oreHarvestWithoutSneak();
      veinationDropOresAtFirstBrokenBlock = defaults.dropOresAtFirstBrokenBlock();
      veinationIncreaseHarvestingTimePerOre = defaults.increaseHarvestingTimePerOre();
      veinationHarvestTimeModifier = defaults.increasedHarvestingTimePerOreModifier();
      veinationPickaxeBlacklist = new ArrayList<>(defaults.pickaxeBlacklist());
    }

    /**
     * Check if Ventilation values differ from defaults.
     */
    private boolean isVentilationDifferentFromDefaults() {
      uk.co.duelmonster.minersadvantage.common.config.VentilationConfig defaults = MAServerRootConfig.defaults()
          .ventilation();
      return ventilationEnabled != defaults.enabled()
          || ventilationRadiusHorizontal != defaults.width()
          || ventilationRadiusVertical != defaults.height()
          || ventilationDepth != defaults.depth()
          || ventilationProcessesPerTick != defaults.processesPerTick()
          || ventilationPlaceLadders != defaults.placeLadders();
    }

    /**
     * Reset Ventilation values to defaults.
     */
    private void resetVentilationToDefaults() {
      uk.co.duelmonster.minersadvantage.common.config.VentilationConfig defaults = MAServerRootConfig.defaults()
          .ventilation();
      ventilationEnabled = defaults.enabled();
      ventilationRadiusHorizontal = defaults.width();
      ventilationRadiusVertical = defaults.height();
      ventilationDepth = defaults.depth();
      ventilationProcessesPerTick = defaults.processesPerTick();
      ventilationPlaceLadders = defaults.placeLadders();
    }

    /**
     * Build client root config from mutable in-memory state.
     */
    private MAClientRootConfig toClientRootConfig(MAClientRootConfig baseline) {
      return new MAClientRootConfig(
          new ClientConfig(
              disableParticleEffects,
              debugLogging,
              outlineForegroundColor,
              outlineSeeThroughColor));
    }

    /**
     * toServerRootConfig exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private MAServerRootConfig toServerRootConfig(MAServerRootConfig baseline) {
      CommonConfig updatedCommon = new CommonConfig(
          tpsGuard,
          gatherDrops,
          autoIlluminate,
          mineVeins,
          blocksPerTick,
          enableTickDelay,
          tickDelay,
          blockRadius);

      return new MAServerRootConfig(
          updatedCommon,
          new uk.co.duelmonster.minersadvantage.common.config.CaptivationConfig(
              captivationEnabled,
              captivationAllowInGui,
              captivationRadiusHorizontal,
              captivationRadiusVertical,
              captivationWhitelist,
              captivationUnconditionalBlacklist,
              captivationBlacklist),
          new uk.co.duelmonster.minersadvantage.common.config.CropinationConfig(
              cropinationEnabled,
              cropinationHarvestSeeds),
          new uk.co.duelmonster.minersadvantage.common.config.CultivationConfig(
              cultivationEnabled,
              cultivationHydrationDistance),
          new uk.co.duelmonster.minersadvantage.common.config.ExcavationConfig(
              excavationEnabled,
              excavationRadiusHorizontal,
              excavationRadiusVertical,
              excavationDepth,
              excavationProcessesPerTick,
              excavationToggleMode,
              excavationIgnoreBlockVariants,
              excavationBlockWhitelist,
              excavationBlockBlacklist),
          new uk.co.duelmonster.minersadvantage.common.config.PathanationConfig(
              pathanationEnabled,
              pathanationTargetBlockRange,
              pathanationPathWidth),
          new uk.co.duelmonster.minersadvantage.common.config.IlluminationConfig(
              illuminationEnabled,
              illuminationRadiusHorizontal,
              illuminationRadiusVertical,
              illuminationLowestLightLevel,
              illuminationUseBlockLight),
          new uk.co.duelmonster.minersadvantage.common.config.LumbinationConfig(
              lumbinationEnabled,
              lumbinationMaxTrunkRange,
              lumbinationMaxLeafRange,
              lumbinationProcessesPerTick,
              lumbinationChopTreeBelow,
              lumbinationDestroyLeaves,
              lumbinationLeavesAffectDurability,
              lumbinationReplantSaplings,
              lumbinationUseCanopyTool,
              lumbinationIgnorePlayerPlacedLeaves,
              lumbinationLogs,
              lumbinationLeaves,
              lumbinationAxes),
          new uk.co.duelmonster.minersadvantage.common.config.ShaftanationConfig(
              shaftanationEnabled,
              shaftanationMaxDepth,
              shaftanationProcessesPerTick,
              shaftanationShaftWidth,
              shaftanationShaftHeight,
              shaftanationTorchPlacement),
          new uk.co.duelmonster.minersadvantage.common.config.SubstitutionConfig(
              substitutionEnabled,
              substitutionAllowMending,
              substitutionPrioritizeSilkTouch,
              substitutionSwitchBack,
              substitutionFavourFortune,
              substitutionIgnoreIfValidTool,
              substitutionIgnorePassiveMobs,
              substitutionBlacklist,
              baseline.substitution().selectionRules()),
          new uk.co.duelmonster.minersadvantage.common.config.VeinationConfig(
              veinationEnabled,
              veinationMaxVeinDistance,
              veinationOres,
              veinationOreHarvestWithoutSneak,
              veinationDropOresAtFirstBrokenBlock,
              veinationIncreaseHarvestingTimePerOre,
              veinationHarvestTimeModifier,
              veinationPickaxeBlacklist),
          new uk.co.duelmonster.minersadvantage.common.config.VentilationConfig(
              ventilationEnabled,
              ventilationRadiusHorizontal,
              ventilationRadiusVertical,
              ventilationDepth,
              ventilationProcessesPerTick,
              ventilationPlaceLadders));
    }
  }
}
