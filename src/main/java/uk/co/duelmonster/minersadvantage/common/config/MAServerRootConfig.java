package uk.co.duelmonster.minersadvantage.common.config;

import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;

/**
 * Explicit server-authoritative gameplay configuration root.
 */
public record MAServerRootConfig(
    CommonConfig common,
    CaptivationConfig captivation,
    CropinationConfig cropination,
    CultivationConfig cultivation,
    ExcavationConfig excavation,
    PathanationConfig pathanation,
    IlluminationConfig illumination,
    LumbinationConfig lumbination,
    ShaftanationConfig shaftanation,
    SubstitutionConfig substitution,
    VeinationConfig veination,
    VentilationConfig ventilation) {
  public MAServerRootConfig {
    SyncedClientConfig defaults = SyncedClientConfig.defaults();
    common = common == null ? defaults.common() : common;
    captivation = captivation == null ? defaults.captivation() : captivation;
    cropination = cropination == null ? defaults.cropination() : cropination;
    cultivation = cultivation == null ? defaults.cultivation() : cultivation;
    excavation = excavation == null ? defaults.excavation() : excavation;
    pathanation = pathanation == null ? defaults.pathanation() : pathanation;
    illumination = illumination == null ? defaults.illumination() : illumination;
    lumbination = lumbination == null ? defaults.lumbination() : lumbination;
    shaftanation = shaftanation == null ? defaults.shaftanation() : shaftanation;
    substitution = substitution == null ? defaults.substitution() : substitution;
    veination = veination == null ? defaults.veination() : veination;
    ventilation = ventilation == null ? defaults.ventilation() : ventilation;
  }

  /**
   * d ef au lt s exists so this path stays predictable and easier to debug when things get weird.
   */
  public static MAServerRootConfig defaults() {
    return fromSyncedConfig(SyncedClientConfig.defaults());
  }

  /**
   * f ro ms yn ce dc on fi g exists so this path stays predictable and easier to debug when things get weird.
   */
  public static MAServerRootConfig fromSyncedConfig(SyncedClientConfig synced) {
    SyncedClientConfig value = synced == null ? SyncedClientConfig.defaults() : synced;
    return new MAServerRootConfig(
        value.common(),
        value.captivation(),
        value.cropination(),
        value.cultivation(),
        value.excavation(),
        value.pathanation(),
        value.illumination(),
        value.lumbination(),
        value.shaftanation(),
        value.substitution(),
        value.veination(),
        value.ventilation());
  }

  /**
   * t os yn ce dc on fi g exists so this path stays predictable and easier to debug when things get weird.
   */
  public SyncedClientConfig toSyncedConfig(ClientConfig clientConfig) {
    ClientConfig clientValue = clientConfig == null ? SyncedClientConfig.defaults().client() : clientConfig;
    return new SyncedClientConfig(
        clientValue,
        common,
        captivation,
        cropination,
        cultivation,
        excavation,
        pathanation,
        illumination,
        lumbination,
        shaftanation,
        substitution,
        veination,
        ventilation);
  }

  /**
   * Return a copy with one feature's enabled flag replaced, leaving every other setting untouched.
   */
  public MAServerRootConfig withFeatureEnabled(FeatureId feature, boolean enabled) {
    if (feature == null || isFeatureEnabled(feature) == enabled) {
      return this;
    }

    return switch (feature) {
      case CAPTIVATION -> new MAServerRootConfig(common,
          new CaptivationConfig(enabled, captivation.allowInGUI(), captivation.radiusHorizontal(),
              captivation.radiusVertical(), captivation.isWhitelist(), captivation.unconditionalBlacklist(),
              captivation.blacklist()),
          cropination, cultivation, excavation, pathanation, illumination, lumbination, shaftanation,
          substitution, veination, ventilation);
      case CROPINATION -> new MAServerRootConfig(common, captivation,
          new CropinationConfig(enabled, cropination.harvestSeeds(), cropination.maxActiveAgents(),
              cropination.enforceAgentLimit()),
          cultivation, excavation, pathanation, illumination, lumbination, shaftanation, substitution,
          veination, ventilation);
      case CULTIVATION -> new MAServerRootConfig(common, captivation, cropination,
          new CultivationConfig(enabled, cultivation.hydrationDistance(), cultivation.maxActiveAgents(),
              cultivation.enforceAgentLimit()),
          excavation, pathanation, illumination, lumbination, shaftanation, substitution, veination,
          ventilation);
      case EXCAVATION -> new MAServerRootConfig(common, captivation, cropination, cultivation,
          new ExcavationConfig(enabled, excavation.width(), excavation.height(), excavation.depth(),
              excavation.processesPerTick(), excavation.toggleMode(), excavation.ignoreBlockVariants(),
              excavation.isBlockWhitelist(), excavation.blockBlacklist(), excavation.maxActiveAgents(),
              excavation.enforceAgentLimit()),
          pathanation, illumination, lumbination, shaftanation, substitution, veination, ventilation);
      case PATHANATION -> new MAServerRootConfig(common, captivation, cropination, cultivation, excavation,
          new PathanationConfig(enabled, pathanation.pathLength(), pathanation.pathWidth(),
              pathanation.maxActiveAgents(), pathanation.enforceAgentLimit()),
          illumination, lumbination, shaftanation, substitution, veination, ventilation);
      case ILLUMINATION -> new MAServerRootConfig(common, captivation, cropination, cultivation, excavation,
          pathanation,
          new IlluminationConfig(enabled, illumination.radiusHorizontal(), illumination.radiusVertical(),
              illumination.lowestLightLevel(), illumination.useBlockLight(), illumination.maxActiveAgents(),
              illumination.enforceAgentLimit()),
          lumbination, shaftanation, substitution, veination, ventilation);
      case LUMBINATION -> new MAServerRootConfig(common, captivation, cropination, cultivation, excavation,
          pathanation, illumination,
          new LumbinationConfig(enabled, lumbination.maxTrunkRange(), lumbination.maxLeafRange(),
              lumbination.processesPerTick(), lumbination.chopTreeBelow(), lumbination.destroyLeaves(),
              lumbination.leavesAffectDurability(), lumbination.replantSaplings(), lumbination.useCanopyTool(),
              lumbination.ignorePlayerPlacedLeaves(), lumbination.logs(), lumbination.leaves(),
              lumbination.axes(), lumbination.maxActiveAgents(), lumbination.enforceAgentLimit()),
          shaftanation, substitution, veination, ventilation);
      case SHAFTANATION -> new MAServerRootConfig(common, captivation, cropination, cultivation, excavation,
          pathanation, illumination, lumbination,
          new ShaftanationConfig(enabled, shaftanation.depth(), shaftanation.processesPerTick(),
              shaftanation.width(), shaftanation.height(), shaftanation.torchPlacement(),
              shaftanation.maxActiveAgents(), shaftanation.enforceAgentLimit()),
          substitution, veination, ventilation);
      case SUBSTITUTION -> new MAServerRootConfig(common, captivation, cropination, cultivation, excavation,
          pathanation, illumination, lumbination, shaftanation,
          new SubstitutionConfig(enabled, substitution.allowMending(), substitution.prioritizeSilkTouch(),
              substitution.switchBack(), substitution.favourFortune(), substitution.ignoreIfValidTool(),
              substitution.ignorePassiveMobs(), substitution.blacklist(), substitution.blockBlacklist(),
              substitution.selectionRules()),
          veination, ventilation);
      case VEINATION -> new MAServerRootConfig(common, captivation, cropination, cultivation, excavation,
          pathanation, illumination, lumbination, shaftanation, substitution,
          new VeinationConfig(enabled, veination.maxVeinDistance(), veination.ores(),
              veination.oreHarvestWithoutSneak(), veination.dropOresAtFirstBrokenBlock(),
              veination.increaseHarvestingTimePerOre(), veination.increasedHarvestingTimePerOreModifier(),
              veination.pickaxeBlacklist(), veination.maxActiveAgents(), veination.enforceAgentLimit()),
          ventilation);
      case VENTILATION -> new MAServerRootConfig(common, captivation, cropination, cultivation, excavation,
          pathanation, illumination, lumbination, shaftanation, substitution, veination,
          new VentilationConfig(enabled, ventilation.width(), ventilation.height(), ventilation.depth(),
              ventilation.processesPerTick(), ventilation.placeLadders(), ventilation.maxActiveAgents(),
              ventilation.enforceAgentLimit()));
    };
  }

  /**
   * Return the current enabled flag for a feature.
   */
  public boolean isFeatureEnabled(FeatureId feature) {
    if (feature == null) {
      return false;
    }

    return switch (feature) {
      case CAPTIVATION -> captivation.enabled();
      case CROPINATION -> cropination.enabled();
      case CULTIVATION -> cultivation.enabled();
      case EXCAVATION -> excavation.enabled();
      case PATHANATION -> pathanation.enabled();
      case ILLUMINATION -> illumination.enabled();
      case LUMBINATION -> lumbination.enabled();
      case SHAFTANATION -> shaftanation.enabled();
      case SUBSTITUTION -> substitution.enabled();
      case VEINATION -> veination.enabled();
      case VENTILATION -> ventilation.enabled();
    };
  }
}
