package uk.co.duelmonster.minersadvantage.client.GUI;

import java.util.ArrayList;
import java.util.List;

import de.erdbeerbaerlp.guilib.components.Button;
import de.erdbeerbaerlp.guilib.components.CheckBox;
import de.erdbeerbaerlp.guilib.components.EnumSlider;
import de.erdbeerbaerlp.guilib.components.Label;
import de.erdbeerbaerlp.guilib.components.Slider;
import de.erdbeerbaerlp.guilib.components.StringList;
import de.erdbeerbaerlp.guilib.components.TextFieldExt;
import de.erdbeerbaerlp.guilib.gui.ExtendedScreen;
import net.minecraft.client.gui.screen.Screen;
import uk.co.duelmonster.minersadvantage.common.Constants;
import uk.co.duelmonster.minersadvantage.common.Functions;
import uk.co.duelmonster.minersadvantage.common.TorchPlacement;
import uk.co.duelmonster.minersadvantage.config.MAConfig;
import uk.co.duelmonster.minersadvantage.config.defaults.MAConfig_Defaults;

public class MAConfigGUI extends ExtendedScreen {
	
	public MAConfigGUI(Screen parentGui) {
		super(parentGui);
	}
	
	private final String PressEnter = "... press Enter to update ...";
	
	private final int startY      = 40;
	private final int categoryX   = 50;
	private final int rowHeight   = 20;
	private final int rowSpace    = 10;
	private final int rowStartY   = rowHeight + rowSpace;
	private final int col_1       = 275;
	private final int col_2       = 500;
	private final int sliderWidth = 265;
	
	private int centerX() {
		return width / 2;
	}
	
	private Button btnFullReset;
	private Button btnClose;
	private Button btnCommon;
	private Button btnCaptivation;
	private Button btnCropination;
	private Button btnExcavation;
	private Button btnPathanation;
	private Button btnIllumination;
	private Button btnLumbination;
	private Button btnShaftanation;
	private Button btnSubstitution;
	private Button btnVeination;
	private Button btnResetCommon;
	private Button btnResetCaptivation;
	private Button btnResetCropination;
	private Button btnResetExcavation;
	private Button btnResetPathanation;
	private Button btnResetIllumination;
	private Button btnResetLumbination;
	private Button btnResetShaftanation;
	private Button btnResetSubstitution;
	private Button btnResetVeination;
	
	private Label lblTitleCategory;
	private Label lblTitleCommon;
	private Label lblSubTitleClient;
	private Label lblTitleCaptivation;
	private Label lblTitleCropination;
	private Label lblTitleExcavation;
	private Label lblTitlePathanation;
	private Label lblTitleIllumination;
	private Label lblTitleLumbination;
	private Label lblTitleShaftanation;
	private Label lblTitleSubstitution;
	private Label lblTitleVeination;
	
	private CheckBox chkCommonEnableTickDelay;
	private CheckBox chkCommonGatherDrops;
	private CheckBox chkCommonBreakAtToolSpeeds;
	private CheckBox chkCommonMineViens;
	private CheckBox chkCommonTPSGuard;
	private CheckBox chkCommonAutoIllum;
	private CheckBox chkClientDisableParticles;
	private CheckBox chkCaptivationEnabled;
	private CheckBox chkCaptivationAllowInGUI;
	private CheckBox chkCaptivationIsWhitelist;
	private CheckBox chkCaptivationUnconditionalBlacklist;
	private CheckBox chkCropinationEnabled;
	private CheckBox chkCropinationHarvestSeeds;
	private CheckBox chkExcavationEnabled;
	private CheckBox chkExcavationToggleMode;
	private CheckBox chkExcavationIgnoreBlockVariants;
	private CheckBox chkExcavationIsBlockWhitelist;
	private CheckBox chkPathanationEnabled;
	private CheckBox chkIlluminationEnabled;
	private CheckBox chkIlluminationUseBlockLight;
	private CheckBox chkLumbinationEnabled;
	private CheckBox chkLumbinationChopTreeBelow;
	private CheckBox chkLumbinationDestroyLeaves;
	private CheckBox chkLumbinationLeavesAffectDurability;
	private CheckBox chkLumbinationReplantSaplings;
	private CheckBox chkLumbinationUseShearsOnLeaves;
	private CheckBox chkShaftanationEnabled;
	private CheckBox chkSubstitutionEnabled;
	private CheckBox chkSubstitutionSwitchBack;
	private CheckBox chkSubstitutionFavourSilkTouch;
	private CheckBox chkSubstitutionFavourFortune;
	private CheckBox chkSubstitutionIgnoreIfValidTool;
	private CheckBox chkSubstitutionIgnorePassiveMobs;
	private CheckBox chkVeinationEnabled;
	
	private Slider sldCommonTickDelay;
	private Slider sldCommonBlocksPerTick;
	private Slider sldCommonBlockLimit;
	private Slider sldCommonBlockRadius;
	private Slider sldCaptivationRadiusHorizontal;
	private Slider sldCaptivationRadiusVertical;
	private Slider sldPathanationWidth;
	private Slider sldPathanationLength;
	private Slider sldIlluminationLightLevel;
	private Slider sldLumbinationLeafRange;
	private Slider sldLumbinationTrunkRange;
	private Slider sldShaftanationShaftLength;
	private Slider sldShaftanationShaftHeight;
	private Slider sldShaftanationShaftWidth;
	
	private EnumSlider esldShaftanationTorchPlacement;
	
	private TextFieldExt txtCommonTickDelay;
	private TextFieldExt txtCommonBlocksPerTick;
	private TextFieldExt txtCommonBlockLimit;
	private TextFieldExt txtCommonBlockRadius;
	private TextFieldExt txtCaptivationRadiusHorizontal;
	private TextFieldExt txtCaptivationRadiusVertical;
	private TextFieldExt txtPathanationWidth;
	private TextFieldExt txtPathanationLength;
	private TextFieldExt txtIlluminationLightLevel;
	private TextFieldExt txtLumbinationLeafRange;
	private TextFieldExt txtLumbinationTrunkRange;
	private TextFieldExt txtShaftanationShaftLength;
	private TextFieldExt txtShaftanationShaftHeight;
	private TextFieldExt txtShaftanationShaftWidth;
	
	private StringList slCaptivationBlacklist;
	private StringList slExcavationBlacklist;
	private StringList slLumbinationAxes;
	private StringList slLumbinationLeaves;
	private StringList slLumbinationLogs;
	private StringList slSubstitutionBlacklist;
	private StringList slVeinationOres;
	
	@Override
	public void buildGui() {
		
		btnClose = new Button(0, 0, 200, Functions.localize("minersadvantage.config.gui.close"));
		btnClose.setClickListener(this::close);
		
		btnFullReset = new Button(0, 0, 100, Functions.localize("minersadvantage.config.gui.fullreset"));
		btnFullReset.setClickListener(() -> {
			resetDefaults(btnFullReset);
		});
		btnFullReset.setTooltips(SplitTooltips(Functions.localize("minersadvantage.config.gui.fullreset.comment")));
		
		lblTitleCategory = new Label(Functions.localize("minersadvantage.config.gui.category"), 0, 0);
		lblTitleCategory.setCentered();
		
		btnCommon       = createCategoryButton(0, Functions.localize("minersadvantage.config.gui.common"));
		btnCaptivation  = createCategoryButton(1, "Captivation");
		btnCropination  = createCategoryButton(2, "Cropination");
		btnExcavation   = createCategoryButton(3, "Excavation");
		btnPathanation  = createCategoryButton(4, "Pathanation");
		btnIllumination = createCategoryButton(5, "Illumination");
		btnLumbination  = createCategoryButton(6, "Lumbination");
		btnShaftanation = createCategoryButton(7, "Shaftanation");
		btnSubstitution = createCategoryButton(8, "Substitution");
		btnVeination    = createCategoryButton(9, "Veination");
		
		btnResetCommon       = createCategoryResetButton();
		btnResetCaptivation  = createCategoryResetButton();
		btnResetCropination  = createCategoryResetButton();
		btnResetExcavation   = createCategoryResetButton();
		btnResetPathanation  = createCategoryResetButton();
		btnResetIllumination = createCategoryResetButton();
		btnResetLumbination  = createCategoryResetButton();
		btnResetShaftanation = createCategoryResetButton();
		btnResetSubstitution = createCategoryResetButton();
		btnResetVeination    = createCategoryResetButton();
		
		generateCommonControls();
		generateClientControls();
		generateCaptivationControls();
		generateCropinationControls();
		generateExcavationControls();
		generatePathanationControls();
		generateIlluminationControls();
		generateLumbinationControls();
		generateShaftanationControls();
		generateSubstitutionControls();
		generateVeinationControls();
		
		// Add always visible components
		this.addAllComponents(
				btnClose,
				btnFullReset,
				lblTitleCategory,
				btnCommon,
				btnCaptivation,
				btnCropination,
				btnExcavation,
				btnPathanation,
				btnIllumination,
				btnLumbination,
				btnShaftanation,
				btnSubstitution,
				btnVeination,
				btnResetCommon,
				btnResetCaptivation,
				btnResetCropination,
				btnResetExcavation,
				btnResetPathanation,
				btnResetIllumination,
				btnResetLumbination,
				btnResetShaftanation,
				btnResetSubstitution,
				btnResetVeination);
		
		toggleCategoryButton(btnCommon);
	}
	
	@Override
	public void updateGui() {
		
		lblTitleCategory.setPosition(categoryX + 100, 25);
		
		btnCommon.setPosition(categoryX, startY);
		btnCaptivation.setPosition(categoryX, startY + (rowStartY * 1));
		btnCropination.setPosition(categoryX, startY + (rowStartY * 2));
		btnExcavation.setPosition(categoryX, startY + (rowStartY * 3));
		btnPathanation.setPosition(categoryX, startY + (rowStartY * 4));
		btnIllumination.setPosition(categoryX, startY + (rowStartY * 5));
		btnLumbination.setPosition(categoryX, startY + (rowStartY * 6));
		btnShaftanation.setPosition(categoryX, startY + (rowStartY * 7));
		btnSubstitution.setPosition(categoryX, startY + (rowStartY * 8));
		btnVeination.setPosition(categoryX, startY + (rowStartY * 9));
		
		btnResetCommon.setPosition(categoryX - 30, startY);
		btnResetCaptivation.setPosition(categoryX - 30, startY + (rowStartY * 1));
		btnResetCropination.setPosition(categoryX - 30, startY + (rowStartY * 2));
		btnResetExcavation.setPosition(categoryX - 30, startY + (rowStartY * 3));
		btnResetPathanation.setPosition(categoryX - 30, startY + (rowStartY * 4));
		btnResetIllumination.setPosition(categoryX - 30, startY + (rowStartY * 5));
		btnResetLumbination.setPosition(categoryX - 30, startY + (rowStartY * 6));
		btnResetShaftanation.setPosition(categoryX - 30, startY + (rowStartY * 7));
		btnResetSubstitution.setPosition(categoryX - 30, startY + (rowStartY * 8));
		btnResetVeination.setPosition(categoryX - 30, startY + (rowStartY * 9));
		
		btnClose.setPosition(centerX() - btnClose.getWidth() / 2, height - 40);
		
		btnFullReset.setPosition(width - btnFullReset.getWidth() - 20, height - 40);
		
		positionCommonControls();
		positionClientControls();
		positionCaptivationControls();
		positionCropinationControls();
		positionExcavationControls();
		positionPathanationControls();
		positionIlluminationControls();
		positionLumbinationControls();
		positionShaftanationControls();
		positionSubstitutionControls();
		positionVeinationControls();
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
	
	@Override
	public boolean doesEscCloseGui() {
		return false;
	}
	
	private Button createCategoryButton(int destinationPage, String displayString) {
		
		Button categoryButton = new Button(0, 0, 200, displayString);
		categoryButton.setClickListener(() -> {
			setPage(destinationPage);
			toggleCategoryButton(categoryButton);
		});
		
		return categoryButton;
	}
	
	private Button createCategoryResetButton() {
		
		Button categoryResetButton = new Button(0, 0, 20, "X");
		categoryResetButton.setClickListener(() -> {
			resetDefaults(categoryResetButton);
			MAConfig.clientSpec.save();
		});
		categoryResetButton.setTooltips(SplitTooltips(Functions.localize("minersadvantage.config.gui.reset.comment")));
		
		return categoryResetButton;
	}
	
	private void toggleCategoryButton(Button btn) {
		btnCommon.setEnabled(btnCommon != btn);
		btnCaptivation.setEnabled(btnCaptivation != btn);
		btnCropination.setEnabled(btnCropination != btn);
		btnExcavation.setEnabled(btnExcavation != btn);
		btnPathanation.setEnabled(btnPathanation != btn);
		btnIllumination.setEnabled(btnIllumination != btn);
		btnLumbination.setEnabled(btnLumbination != btn);
		btnShaftanation.setEnabled(btnShaftanation != btn);
		btnSubstitution.setEnabled(btnSubstitution != btn);
		btnVeination.setEnabled(btnVeination != btn);
	}
	
	private String[] SplitTooltips(String input) {
		return SplitTooltips(input, false);
	}
	
	private String[] SplitTooltips(String input, Boolean appendPressEnter) {
		List<String> lines     = new ArrayList<String>();
		String[]     words     = input.split("\\s");     // splits the string based on whitespace
		String       line      = "";
		int          wordCount = 0;
		
		for (String word : words) {
			wordCount++;
			line += word;
			
			if (wordCount == 8) {
				wordCount = 0;
				lines.add(line);
				line = "";
			} else {
				line += " ";
			}
		}
		
		if (line != "") {
			lines.add(line);
		}
		
		if (appendPressEnter) {
			lines.add(PressEnter);
		}
		
		return lines.toArray(new String[0]);
	}
	
	private void generateCommonControls() {
		
		lblTitleCommon = new Label("Common", 0, 0);
		lblTitleCommon.setCentered();
		
		chkCommonEnableTickDelay   = new CheckBox(0, 0, Functions.localize("minersadvantage.common.enable_tick_delay"), false);
		chkCommonTPSGuard          = new CheckBox(0, 0, Functions.localize("minersadvantage.common.tps_guard"), false);
		chkCommonGatherDrops       = new CheckBox(0, 0, Functions.localize("minersadvantage.common.gather_drops"), false);
		chkCommonMineViens         = new CheckBox(0, 0, Functions.localize("minersadvantage.common.mine_veins"), false);
		chkCommonAutoIllum         = new CheckBox(0, 0, Functions.localize("minersadvantage.common.auto_illum"), false);
		chkCommonBreakAtToolSpeeds = new CheckBox(0, 0, Functions.localize("minersadvantage.common.break_at_tool_speeds"), false);
		
		sldCommonTickDelay     = new Slider(0, 0, sliderWidth, 20, Functions.localize("minersadvantage.common.tick_delay") + ": ", "", 1, 20, 1, false, true);
		sldCommonBlocksPerTick = new Slider(0, 0, sliderWidth, 20, Functions.localize("minersadvantage.common.blocks_per_tick") + ": ", "", 1, 16, 1, false, true);
		sldCommonBlockLimit    = new Slider(0, 0, sliderWidth, 20, Functions.localize("minersadvantage.common.limit") + ": ", "", Constants.MIN_BLOCKLIMIT, Constants.MAX_BLOCKLIMIT, Constants.MIN_BLOCKLIMIT, false, true);
		sldCommonBlockRadius   = new Slider(0, 0, sliderWidth, 20, Functions.localize("minersadvantage.common.radius") + ": ", "", Constants.MIN_BLOCKRADIUS, Constants.MAX_BLOCKRADIUS, Constants.MIN_BLOCKRADIUS, false, true);
		
		txtCommonTickDelay     = new TextFieldExt(0, 0, 50);
		txtCommonBlocksPerTick = new TextFieldExt(0, 0, 50);
		txtCommonBlockLimit    = new TextFieldExt(0, 0, 50);
		txtCommonBlockRadius   = new TextFieldExt(0, 0, 50);
		
		chkCommonEnableTickDelay.setTooltips(SplitTooltips(Functions.localize("minersadvantage.common.enable_tick_delay.comment")));
		chkCommonTPSGuard.setTooltips(SplitTooltips(Functions.localize("minersadvantage.common.tps_guard.comment")));
		chkCommonGatherDrops.setTooltips(SplitTooltips(Functions.localize("minersadvantage.common.gather_drops.comment")));
		chkCommonMineViens.setTooltips(SplitTooltips(Functions.localize("minersadvantage.common.mine_veins.comment")));
		chkCommonAutoIllum.setTooltips(SplitTooltips(Functions.localize("minersadvantage.common.auto_illum.comment")));
		chkCommonBreakAtToolSpeeds.setTooltips(SplitTooltips(Functions.localize("minersadvantage.common.break_at_tool_speeds.comment")));
		
		txtCommonTickDelay.setTooltips(SplitTooltips(Functions.localize("minersadvantage.common.tick_delay.comment"), true));
		txtCommonBlocksPerTick.setTooltips(SplitTooltips(Functions.localize("minersadvantage.common.blocks_per_tick.comment"), true));
		txtCommonBlockLimit.setTooltips(SplitTooltips(Functions.localize("minersadvantage.common.limit.comment"), true));
		txtCommonBlockRadius.setTooltips(SplitTooltips(Functions.localize("minersadvantage.common.radius.comment"), true));
		
		chkCommonBreakAtToolSpeeds.setEnabled(false);
		
		chkCommonEnableTickDelay.setChangeListener(() -> {
			MAConfig.CLIENT.common.setEnableTickDelay(chkCommonEnableTickDelay.isChecked());
			MAConfig.clientSpec.save();
		});
		chkCommonTPSGuard.setChangeListener(() -> {
			MAConfig.CLIENT.common.set_tpsGuard(chkCommonTPSGuard.isChecked());
			MAConfig.clientSpec.save();
		});
		chkCommonGatherDrops.setChangeListener(() -> {
			MAConfig.CLIENT.common.setGatherDrops(chkCommonGatherDrops.isChecked());
			MAConfig.clientSpec.save();
		});
		chkCommonMineViens.setChangeListener(() -> {
			MAConfig.CLIENT.common.setMineVeins(chkCommonMineViens.isChecked());
			MAConfig.clientSpec.save();
		});
		chkCommonAutoIllum.setChangeListener(() -> {
			MAConfig.CLIENT.common.setAutoIlluminate(chkCommonAutoIllum.isChecked());
			MAConfig.clientSpec.save();
		});
		chkCommonBreakAtToolSpeeds.setChangeListener(() -> {
			MAConfig.CLIENT.common.setBreakAtToolSpeeds(chkCommonBreakAtToolSpeeds.isChecked());
			MAConfig.clientSpec.save();
		});
		
		sldCommonTickDelay.setAction(() -> {
			MAConfig.CLIENT.common.setTickDelay(sldCommonTickDelay.getValueInt());
			txtCommonTickDelay.setText(String.valueOf(sldCommonTickDelay.getValueInt()));
			MAConfig.clientSpec.save();
		});
		sldCommonBlocksPerTick.setAction(() -> {
			MAConfig.CLIENT.common.setBlocksPerTick(sldCommonBlocksPerTick.getValueInt());
			txtCommonBlocksPerTick.setText(String.valueOf(sldCommonBlocksPerTick.getValueInt()));
			MAConfig.clientSpec.save();
		});
		sldCommonBlockLimit.setAction(() -> {
			MAConfig.CLIENT.common.setBlockLimit(sldCommonBlockLimit.getValueInt());
			txtCommonBlockLimit.setText(String.valueOf(sldCommonBlockLimit.getValueInt()));
			MAConfig.clientSpec.save();
		});
		sldCommonBlockRadius.setAction(() -> {
			MAConfig.CLIENT.common.setBlockRadius(sldCommonBlockRadius.getValueInt());
			txtCommonBlockRadius.setText(String.valueOf(sldCommonBlockRadius.getValueInt()));
			MAConfig.clientSpec.save();
		});
		
		txtCommonTickDelay.setReturnAction(() -> {
			sldCommonTickDelay.setValue(Double.parseDouble(txtCommonTickDelay.getText()));
		});
		txtCommonBlocksPerTick.setReturnAction(() -> {
			sldCommonBlocksPerTick.setValue(Double.parseDouble(txtCommonBlocksPerTick.getText()));
		});
		txtCommonBlockLimit.setReturnAction(() -> {
			sldCommonBlockLimit.setValue(Double.parseDouble(txtCommonBlockLimit.getText()));
		});
		txtCommonBlockRadius.setReturnAction(() -> {
			sldCommonBlockRadius.setValue(Double.parseDouble(txtCommonBlockRadius.getText()));
		});
		
		this.addComponent(lblTitleCommon, 0);
		
		this.addComponent(chkCommonEnableTickDelay, 0);
		this.addComponent(chkCommonTPSGuard, 0);
		this.addComponent(chkCommonGatherDrops, 0);
		this.addComponent(chkCommonMineViens, 0);
		this.addComponent(chkCommonAutoIllum, 0);
		this.addComponent(chkCommonBreakAtToolSpeeds, 0);
		
		this.addComponent(sldCommonTickDelay, 0);
		this.addComponent(sldCommonBlocksPerTick, 0);
		this.addComponent(sldCommonBlockLimit, 0);
		this.addComponent(sldCommonBlockRadius, 0);
		
		this.addComponent(txtCommonTickDelay, 0);
		this.addComponent(txtCommonBlocksPerTick, 0);
		this.addComponent(txtCommonBlockLimit, 0);
		this.addComponent(txtCommonBlockRadius, 0);
		
		applyCommonValues(false);
	}
	
	private void positionCommonControls() {
		
		lblTitleCommon.setPosition(centerX(), 25);
		
		chkCommonEnableTickDelay.setPosition(col_1, startY + 5);
		chkCommonTPSGuard.setPosition(col_2, startY + 5);
		
		sldCommonTickDelay.setPosition(col_1, startY + rowStartY);
		txtCommonTickDelay.setPosition(col_1 + sliderWidth + 10, startY + rowStartY);
		
		sldCommonBlocksPerTick.setPosition(col_1, startY + (rowStartY * 2));
		txtCommonBlocksPerTick.setPosition(col_1 + sliderWidth + 10, startY + (rowStartY * 2));
		
		sldCommonBlockLimit.setPosition(col_1, startY + (rowStartY * 3));
		txtCommonBlockLimit.setPosition(col_1 + sliderWidth + 10, startY + (rowStartY * 3));
		
		sldCommonBlockRadius.setPosition(col_1, startY + (rowStartY * 4));
		txtCommonBlockRadius.setPosition(col_1 + sliderWidth + 10, startY + (rowStartY * 4));
		
		chkCommonGatherDrops.setPosition(col_1, startY + (rowStartY * 5) + 5);
		chkCommonMineViens.setPosition(col_1, startY + (rowStartY * 6) + 5);
		chkCommonAutoIllum.setPosition(col_1, startY + (rowStartY * 7) + 5);
		
		chkCommonBreakAtToolSpeeds.setPosition(col_1, startY + (rowStartY * 8) + 5);
	}
	
	private void applyCommonValues(boolean applyDefaults) {
		
		chkCommonEnableTickDelay.setIsChecked(!applyDefaults ? MAConfig.CLIENT.common.enableTickDelay() : MAConfig_Defaults.Common.enableTickDelay);
		chkCommonTPSGuard.setIsChecked(!applyDefaults ? MAConfig.CLIENT.common.tpsGuard() : MAConfig_Defaults.Common.tpsGuard);
		chkCommonGatherDrops.setIsChecked(!applyDefaults ? MAConfig.CLIENT.common.gatherDrops() : MAConfig_Defaults.Common.gatherDrops);
		chkCommonMineViens.setIsChecked(!applyDefaults ? MAConfig.CLIENT.common.mineVeins() : MAConfig_Defaults.Common.mineVeins);
		chkCommonAutoIllum.setIsChecked(!applyDefaults ? MAConfig.CLIENT.common.autoIlluminate() : MAConfig_Defaults.Common.autoIlluminate);
		chkCommonBreakAtToolSpeeds.setIsChecked(!applyDefaults ? MAConfig.CLIENT.common.breakAtToolSpeeds() : MAConfig_Defaults.Common.breakAtToolSpeeds);
		
		sldCommonTickDelay.setValue(!applyDefaults ? MAConfig.CLIENT.common.tickDelay() : MAConfig_Defaults.Common.tickDelay);
		sldCommonBlocksPerTick.setValue(!applyDefaults ? MAConfig.CLIENT.common.blocksPerTick() : MAConfig_Defaults.Common.blocksPerTick);
		sldCommonBlockLimit.setValue(!applyDefaults ? MAConfig.CLIENT.common.blockLimit() : MAConfig_Defaults.Common.blockLimit);
		sldCommonBlockRadius.setValue(!applyDefaults ? MAConfig.CLIENT.common.blockRadius() : MAConfig_Defaults.Common.blockRadius);
		
		txtCommonTickDelay.setText(String.valueOf(!applyDefaults ? MAConfig.CLIENT.common.tickDelay() : MAConfig_Defaults.Common.tickDelay));
		txtCommonBlocksPerTick.setText(String.valueOf(!applyDefaults ? MAConfig.CLIENT.common.blocksPerTick() : MAConfig_Defaults.Common.blocksPerTick));
		txtCommonBlockLimit.setText(String.valueOf(!applyDefaults ? MAConfig.CLIENT.common.blockLimit() : MAConfig_Defaults.Common.blockLimit));
		txtCommonBlockRadius.setText(String.valueOf(!applyDefaults ? MAConfig.CLIENT.common.blockRadius() : MAConfig_Defaults.Common.blockRadius));
		
	}
	
	private void generateClientControls() {
		
		lblSubTitleClient = new Label("Client Only:", 0, 0);
		
		chkClientDisableParticles = new CheckBox(0, 0, Functions.localize("minersadvantage.client.disable_particle_effects"), false);
		
		chkClientDisableParticles.setTooltips(SplitTooltips(Functions.localize("minersadvantage.client.disable_particle_effects.comment")));
		
		chkClientDisableParticles.setChangeListener(() -> {
			MAConfig.CLIENT.setDisableParticleEffects(chkClientDisableParticles.isChecked());
			MAConfig.clientSpec.save();
		});
		
		this.addComponent(lblSubTitleClient, 0);
		this.addComponent(chkClientDisableParticles, 0);
		
		applyClientValues(false);
	}
	
	private void positionClientControls() {
		
		lblSubTitleClient.setPosition(col_1, startY + (rowStartY * 9) + 10);
		
		chkClientDisableParticles.setPosition(col_1, startY + (rowStartY * 10) + 5);
		
	}
	
	private void applyClientValues(boolean applyDefaults) {
		
		chkClientDisableParticles.setIsChecked(!applyDefaults ? MAConfig.CLIENT.disableParticleEffects() : MAConfig_Defaults.Client.disableParticleEffects);
		
	}
	
	private void generateCaptivationControls() {
		
		lblTitleCaptivation = new Label("Captivation", 0, 0);
		lblTitleCaptivation.setCentered();
		
		chkCaptivationEnabled                = new CheckBox(0, 0, Functions.localize("minersadvantage.captivation.enabled"), false);
		chkCaptivationAllowInGUI             = new CheckBox(0, 0, Functions.localize("minersadvantage.captivation.allow_in_gui"), false);
		chkCaptivationIsWhitelist            = new CheckBox(0, 0, Functions.localize("minersadvantage.captivation.is_whitelist"), false);
		chkCaptivationUnconditionalBlacklist = new CheckBox(0, 0, Functions.localize("minersadvantage.captivation.unconditional_blacklist"), false);
		
		sldCaptivationRadiusHorizontal = new Slider(0, 0, sliderWidth, 20, Functions.localize("minersadvantage.captivation.h_radius") + ": ", "", 0, 128, 0, false, true);
		sldCaptivationRadiusVertical   = new Slider(0, 0, sliderWidth, 20, Functions.localize("minersadvantage.captivation.v_radius") + ": ", "", 0, 128, 0, false, true);
		
		txtCaptivationRadiusHorizontal = new TextFieldExt(0, 0, 50);
		txtCaptivationRadiusVertical   = new TextFieldExt(0, 0, 50);
		
		slCaptivationBlacklist = new StringList(0, 0, 50, 50, Functions.localize("minersadvantage.captivation.blacklist"), null);
		
		chkCaptivationEnabled.setTooltips(SplitTooltips(Functions.localize("minersadvantage.captivation.enabled.comment")));
		chkCaptivationAllowInGUI.setTooltips(SplitTooltips(Functions.localize("minersadvantage.captivation.allow_in_gui.comment")));
		chkCaptivationIsWhitelist.setTooltips(SplitTooltips(Functions.localize("minersadvantage.captivation.is_whitelist.comment")));
		chkCaptivationUnconditionalBlacklist.setTooltips(SplitTooltips(Functions.localize("minersadvantage.captivation.unconditional_blacklist.comment")));
		
		txtCaptivationRadiusHorizontal.setTooltips(SplitTooltips(Functions.localize("minersadvantage.captivation.h_radius.comment"), true));
		txtCaptivationRadiusVertical.setTooltips(SplitTooltips(Functions.localize("minersadvantage.captivation.v_radius.comment"), true));
		
		chkCaptivationEnabled.setChangeListener(() -> {
			MAConfig.CLIENT.captivation.setEnabled(chkCaptivationEnabled.isChecked());
			MAConfig.clientSpec.save();
		});
		chkCaptivationAllowInGUI.setChangeListener(() -> {
			MAConfig.CLIENT.captivation.setAllowInGUI(chkCaptivationAllowInGUI.isChecked());
			MAConfig.clientSpec.save();
		});
		chkCaptivationIsWhitelist.setChangeListener(() -> {
			MAConfig.CLIENT.captivation.setIsWhitelist(chkCaptivationIsWhitelist.isChecked());
			MAConfig.clientSpec.save();
		});
		chkCaptivationUnconditionalBlacklist.setChangeListener(() -> {
			MAConfig.CLIENT.captivation.setUnconditionalBlacklist(chkCaptivationUnconditionalBlacklist.isChecked());
			MAConfig.clientSpec.save();
		});
		
		sldCaptivationRadiusHorizontal.setAction(() -> {
			MAConfig.CLIENT.captivation.setRadiusHorizontal(sldCaptivationRadiusHorizontal.getValueInt());
			txtCaptivationRadiusHorizontal.setText(String.valueOf(sldCaptivationRadiusHorizontal.getValueInt()));
			MAConfig.clientSpec.save();
		});
		sldCaptivationRadiusVertical.setAction(() -> {
			MAConfig.CLIENT.captivation.setRadiusVertical(sldCaptivationRadiusVertical.getValueInt());
			txtCaptivationRadiusVertical.setText(String.valueOf(sldCaptivationRadiusVertical.getValueInt()));
			MAConfig.clientSpec.save();
		});
		
		txtCaptivationRadiusHorizontal.setReturnAction(() -> {
			sldCaptivationRadiusHorizontal.setValue(Double.parseDouble(txtCaptivationRadiusHorizontal.getText()));
		});
		txtCaptivationRadiusVertical.setReturnAction(() -> {
			sldCaptivationRadiusVertical.setValue(Double.parseDouble(txtCaptivationRadiusVertical.getText()));
		});
		
		slCaptivationBlacklist.setUpdatedCallback(() -> {
			MAConfig.CLIENT.captivation.setBlacklist(slCaptivationBlacklist.getListValues());
			MAConfig.clientSpec.save();
		});
		
		this.addComponent(lblTitleCaptivation, 1);
		this.addComponent(chkCaptivationEnabled, 1);
		this.addComponent(chkCaptivationAllowInGUI, 1);
		this.addComponent(chkCaptivationIsWhitelist, 1);
		this.addComponent(chkCaptivationUnconditionalBlacklist, 1);
		
		this.addComponent(sldCaptivationRadiusHorizontal, 1);
		this.addComponent(sldCaptivationRadiusVertical, 1);
		
		this.addComponent(txtCaptivationRadiusHorizontal, 1);
		this.addComponent(txtCaptivationRadiusVertical, 1);
		
		this.addComponent(slCaptivationBlacklist, 1);
		
		applyCaptivationValues(false);
	}
	
	private void positionCaptivationControls() {
		
		lblTitleCaptivation.setPosition(centerX(), 25);
		
		chkCaptivationEnabled.setPosition(col_1, startY + 5);
		chkCaptivationAllowInGUI.setPosition(col_1, startY + rowStartY + 5);
		chkCaptivationIsWhitelist.setPosition(col_1, startY + (rowStartY * 2) + 5);
		chkCaptivationUnconditionalBlacklist.setPosition(col_1, startY + (rowStartY * 3) + 5);
		
		sldCaptivationRadiusHorizontal.setPosition(col_1, startY + (rowStartY * 4));
		txtCaptivationRadiusHorizontal.setPosition(col_1 + sliderWidth + 10, startY + (rowStartY * 4));
		
		sldCaptivationRadiusVertical.setPosition(col_1, startY + (rowStartY * 5));
		txtCaptivationRadiusVertical.setPosition(col_1 + sliderWidth + 10, startY + (rowStartY * 5));
		
		slCaptivationBlacklist.adjustLayout(col_1, startY + (rowStartY * 6), width - col_1 - 20, height - (startY + (rowStartY * 6)) - 60);
		
	}
	
	private void applyCaptivationValues(boolean applyDefaults) {
		
		chkCaptivationEnabled.setIsChecked(!applyDefaults ? MAConfig.CLIENT.captivation.enabled() : MAConfig_Defaults.Captivation.enabled);
		chkCaptivationAllowInGUI.setIsChecked(!applyDefaults ? MAConfig.CLIENT.captivation.allowInGUI() : MAConfig_Defaults.Captivation.allowInGUI);
		chkCaptivationIsWhitelist.setIsChecked(!applyDefaults ? MAConfig.CLIENT.captivation.isWhitelist() : MAConfig_Defaults.Captivation.isWhitelist);
		chkCaptivationUnconditionalBlacklist.setIsChecked(!applyDefaults ? MAConfig.CLIENT.captivation.unconditionalBlacklist() : MAConfig_Defaults.Captivation.unconditionalBlacklist);
		
		sldCaptivationRadiusHorizontal.setValue(!applyDefaults ? MAConfig.CLIENT.captivation.radiusHorizontal() : MAConfig_Defaults.Captivation.radiusHorizontal);
		sldCaptivationRadiusVertical.setValue(!applyDefaults ? MAConfig.CLIENT.captivation.radiusVertical() : MAConfig_Defaults.Captivation.radiusVertical);
		
		txtCaptivationRadiusHorizontal.setText(String.valueOf(!applyDefaults ? MAConfig.CLIENT.captivation.radiusHorizontal() : MAConfig_Defaults.Captivation.radiusHorizontal));
		txtCaptivationRadiusVertical.setText(String.valueOf(!applyDefaults ? MAConfig.CLIENT.captivation.radiusVertical() : MAConfig_Defaults.Captivation.radiusVertical));
		
		slCaptivationBlacklist.setListValues(new ArrayList<String>(!applyDefaults ? MAConfig.CLIENT.captivation.blacklist() : MAConfig_Defaults.Captivation.blacklist));
		
	}
	
	private void generateCropinationControls() {
		lblTitleCropination = new Label("Cropination", 0, 0);
		lblTitleCropination.setCentered();
		
		chkCropinationEnabled      = new CheckBox(0, 0, Functions.localize("minersadvantage.cropination.enabled"), false);
		chkCropinationHarvestSeeds = new CheckBox(0, 0, Functions.localize("minersadvantage.cropination.harvest_seeds"), false);
		
		chkCropinationEnabled.setTooltips(SplitTooltips(Functions.localize("minersadvantage.cropination.enabled.comment")));
		chkCropinationHarvestSeeds.setTooltips(SplitTooltips(Functions.localize("minersadvantage.cropination.harvest_seeds.comment")));
		
		chkCropinationEnabled.setChangeListener(() -> {
			MAConfig.CLIENT.cropination.setEnabled(chkCropinationEnabled.isChecked());
			MAConfig.clientSpec.save();
		});
		chkCropinationHarvestSeeds.setChangeListener(() -> {
			MAConfig.CLIENT.cropination.setHarvestSeeds(chkCropinationHarvestSeeds.isChecked());
			MAConfig.clientSpec.save();
		});
		
		this.addComponent(lblTitleCropination, 2);
		this.addComponent(chkCropinationEnabled, 2);
		this.addComponent(chkCropinationHarvestSeeds, 2);
		
		applyCropinationValues(false);
	}
	
	private void positionCropinationControls() {
		lblTitleCropination.setPosition(centerX(), 25);
		
		chkCropinationEnabled.setPosition(col_1, startY + 5);
		chkCropinationHarvestSeeds.setPosition(col_1, startY + rowStartY + 5);
		
	}
	
	private void applyCropinationValues(boolean applyDefaults) {
		
		chkCropinationEnabled.setIsChecked(!applyDefaults ? MAConfig.CLIENT.cropination.enabled() : MAConfig_Defaults.Cropination.enabled);
		chkCropinationHarvestSeeds.setIsChecked(!applyDefaults ? MAConfig.CLIENT.cropination.harvestSeeds() : MAConfig_Defaults.Cropination.harvestSeeds);
		
	}
	
	private void generateExcavationControls() {
		lblTitleExcavation = new Label("Excavation", 0, 0);
		lblTitleExcavation.setCentered();
		
		chkExcavationEnabled             = new CheckBox(0, 0, Functions.localize("minersadvantage.excavation.enabled"), false);
		chkExcavationToggleMode          = new CheckBox(0, 0, Functions.localize("minersadvantage.excavation.toggle_mode"), false);
		chkExcavationIgnoreBlockVariants = new CheckBox(0, 0, Functions.localize("minersadvantage.excavation.ignore_variants"), false);
		chkExcavationIsBlockWhitelist    = new CheckBox(0, 0, Functions.localize("minersadvantage.excavation.is_block_whitelist"), false);
		
		chkExcavationEnabled.setTooltips(SplitTooltips(Functions.localize("minersadvantage.excavation.enabled.comment")));
		chkExcavationToggleMode.setTooltips(SplitTooltips(Functions.localize("minersadvantage.excavation.toggle_mode.comment")));
		chkExcavationIgnoreBlockVariants.setTooltips(SplitTooltips(Functions.localize("minersadvantage.excavation.ignore_variants.comment")));
		chkExcavationIsBlockWhitelist.setTooltips(SplitTooltips(Functions.localize("minersadvantage.excavation.is_block_whitelist.comment")));
		
		slExcavationBlacklist = new StringList(0, 0, 50, 50, Functions.localize("minersadvantage.excavation.block_blacklist"), null);
		
		chkExcavationEnabled.setChangeListener(() -> {
			MAConfig.CLIENT.excavation.setEnabled(chkExcavationEnabled.isChecked());
			MAConfig.clientSpec.save();
		});
		chkExcavationToggleMode.setChangeListener(() -> {
			MAConfig.CLIENT.excavation.setToggleMode(chkExcavationToggleMode.isChecked());
			MAConfig.clientSpec.save();
		});
		chkExcavationIgnoreBlockVariants.setChangeListener(() -> {
			MAConfig.CLIENT.excavation.setIgnoreBlockVariants(chkExcavationIgnoreBlockVariants.isChecked());
			MAConfig.clientSpec.save();
		});
		chkExcavationIsBlockWhitelist.setChangeListener(() -> {
			MAConfig.CLIENT.excavation.setIsBlockWhitelist(chkExcavationIsBlockWhitelist.isChecked());
			MAConfig.clientSpec.save();
		});
		
		slExcavationBlacklist.setUpdatedCallback(() -> {
			MAConfig.CLIENT.excavation.setBlockBlacklist(slExcavationBlacklist.getListValues());
			MAConfig.clientSpec.save();
		});
		
		this.addComponent(lblTitleExcavation, 3);
		this.addComponent(chkExcavationEnabled, 3);
		this.addComponent(chkExcavationToggleMode, 3);
		this.addComponent(chkExcavationIgnoreBlockVariants, 3);
		this.addComponent(chkExcavationIsBlockWhitelist, 3);
		this.addComponent(slExcavationBlacklist, 3);
		
		applyExcavationValues(false);
	}
	
	private void positionExcavationControls() {
		
		lblTitleExcavation.setPosition(centerX(), 25);
		
		chkExcavationEnabled.setPosition(col_1, startY + 5);
		chkExcavationToggleMode.setPosition(col_1, startY + rowStartY + 5);
		chkExcavationIgnoreBlockVariants.setPosition(col_1, startY + (rowStartY * 2) + 5);
		chkExcavationIsBlockWhitelist.setPosition(col_1, startY + (rowStartY * 3) + 5);
		
		slExcavationBlacklist.adjustLayout(col_1, startY + (rowStartY * 4), width - col_1 - 20, height - (startY + (rowStartY * 4)) - 60);
		
	}
	
	private void applyExcavationValues(boolean applyDefaults) {
		
		chkExcavationEnabled.setIsChecked(!applyDefaults ? MAConfig.CLIENT.excavation.enabled() : MAConfig_Defaults.Excavation.enabled);
		chkExcavationToggleMode.setIsChecked(!applyDefaults ? MAConfig.CLIENT.excavation.toggleMode() : MAConfig_Defaults.Excavation.toggleMode);
		chkExcavationIgnoreBlockVariants.setIsChecked(!applyDefaults ? MAConfig.CLIENT.excavation.ignoreBlockVariants() : MAConfig_Defaults.Excavation.ignoreBlockVariants);
		chkExcavationIsBlockWhitelist.setIsChecked(!applyDefaults ? MAConfig.CLIENT.excavation.isBlockWhitelist() : MAConfig_Defaults.Excavation.isBlockWhitelist);
		
		slExcavationBlacklist.setListValues(new ArrayList<String>(!applyDefaults ? MAConfig.CLIENT.excavation.blockBlacklist() : MAConfig_Defaults.Excavation.blockBlacklist));
		
	}
	
	private void generatePathanationControls() {
		lblTitlePathanation = new Label("Pathanation", 0, 0);
		lblTitlePathanation.setCentered();
		
		chkPathanationEnabled = new CheckBox(0, 0, Functions.localize("minersadvantage.pathanation.enabled"), false);
		
		sldPathanationWidth  = new Slider(0, 0, sliderWidth, 20, Functions.localize("minersadvantage.pathanation.path_width") + ": ", "", 1, 16, 1, false, true);
		sldPathanationLength = new Slider(0, 0, sliderWidth, 20, Functions.localize("minersadvantage.pathanation.path_length") + ": ", "", 1, 64, 1, false, true);
		
		txtPathanationWidth  = new TextFieldExt(0, 0, 50);
		txtPathanationLength = new TextFieldExt(0, 0, 50);
		
		txtPathanationWidth.setText(String.valueOf(MAConfig.CLIENT.pathanation.pathWidth()));
		txtPathanationLength.setText(String.valueOf(MAConfig.CLIENT.pathanation.pathLength()));
		
		chkPathanationEnabled.setTooltips(SplitTooltips(Functions.localize("minersadvantage.pathanation.enabled.comment")));
		txtPathanationWidth.setTooltips(SplitTooltips(Functions.localize("minersadvantage.pathanation.path_width.comment")));
		txtPathanationLength.setTooltips(SplitTooltips(Functions.localize("minersadvantage.pathanation.path_length.comment")));
		
		chkPathanationEnabled.setChangeListener(() -> {
			MAConfig.CLIENT.pathanation.setEnabled(chkPathanationEnabled.isChecked());
			MAConfig.clientSpec.save();
		});
		
		sldPathanationWidth.setAction(() -> {
			MAConfig.CLIENT.pathanation.setPathWidth(sldPathanationWidth.getValueInt());
			txtPathanationWidth.setText(String.valueOf(sldPathanationWidth.getValueInt()));
			MAConfig.clientSpec.save();
		});
		sldPathanationLength.setAction(() -> {
			MAConfig.CLIENT.pathanation.setPathLength(sldPathanationLength.getValueInt());
			txtPathanationLength.setText(String.valueOf(sldPathanationLength.getValueInt()));
			MAConfig.clientSpec.save();
		});
		
		txtPathanationWidth.setReturnAction(() -> {
			sldPathanationWidth.setValue(Double.parseDouble(txtPathanationWidth.getText()));
		});
		txtPathanationLength.setReturnAction(() -> {
			sldPathanationLength.setValue(Double.parseDouble(txtPathanationLength.getText()));
		});
		
		this.addComponent(lblTitlePathanation, 4);
		this.addComponent(chkPathanationEnabled, 4);
		this.addComponent(sldPathanationWidth, 4);
		this.addComponent(sldPathanationLength, 4);
		this.addComponent(txtPathanationWidth, 4);
		this.addComponent(txtPathanationLength, 4);
		
		applyPathanationValues(false);
	}
	
	private void positionPathanationControls() {
		lblTitlePathanation.setPosition(centerX(), 25);
		
		chkPathanationEnabled.setPosition(col_1, startY + 5);
		
		sldPathanationWidth.setPosition(col_1, startY + rowStartY);
		txtPathanationWidth.setPosition(col_1 + sliderWidth + 10, startY + rowStartY);
		
		sldPathanationLength.setPosition(col_1, startY + (rowStartY * 2));
		txtPathanationLength.setPosition(col_1 + sliderWidth + 10, startY + (rowStartY * 2));
		
	}
	
	private void applyPathanationValues(boolean applyDefaults) {
		
		chkPathanationEnabled.setIsChecked(!applyDefaults ? MAConfig.CLIENT.pathanation.enabled() : MAConfig_Defaults.Pathanation.enabled);
		
		sldPathanationWidth.setValue(!applyDefaults ? MAConfig.CLIENT.pathanation.pathWidth() : MAConfig_Defaults.Pathanation.pathWidth);
		sldPathanationLength.setValue(!applyDefaults ? MAConfig.CLIENT.pathanation.pathLength() : MAConfig_Defaults.Pathanation.pathLength);
		
		txtPathanationWidth.setText(String.valueOf(!applyDefaults ? MAConfig.CLIENT.pathanation.pathWidth() : MAConfig_Defaults.Pathanation.pathWidth));
		txtPathanationLength.setText(String.valueOf(!applyDefaults ? MAConfig.CLIENT.pathanation.pathLength() : MAConfig_Defaults.Pathanation.pathLength));
		
	}
	
	private void generateIlluminationControls() {
		lblTitleIllumination = new Label("Illumination", 0, 0);
		lblTitleIllumination.setCentered();
		
		chkIlluminationEnabled       = new CheckBox(0, 0, Functions.localize("minersadvantage.illumination.enabled"), false);
		chkIlluminationUseBlockLight = new CheckBox(0, 0, Functions.localize("minersadvantage.illumination.use_block_light"), false);
		
		sldIlluminationLightLevel = new Slider(0, 0, sliderWidth, 20, Functions.localize("minersadvantage.illumination.light_level") + ": ", "", 0, 16, 0, false, true);
		
		txtIlluminationLightLevel = new TextFieldExt(0, 0, 50);
		
		chkIlluminationEnabled.setTooltips(SplitTooltips(Functions.localize("minersadvantage.illumination.enabled.comment")));
		chkIlluminationUseBlockLight.setTooltips(SplitTooltips(Functions.localize("minersadvantage.illumination.use_block_light.comment")));
		txtIlluminationLightLevel.setTooltips(SplitTooltips(Functions.localize("minersadvantage.illumination.light_level.comment")));
		
		chkIlluminationEnabled.setChangeListener(() -> {
			MAConfig.CLIENT.illumination.setEnabled(chkIlluminationEnabled.isChecked());
			MAConfig.clientSpec.save();
		});
		chkIlluminationUseBlockLight.setChangeListener(() -> {
			MAConfig.CLIENT.illumination.setUseBlockLight(chkIlluminationUseBlockLight.isChecked());
			MAConfig.clientSpec.save();
		});
		
		sldIlluminationLightLevel.setAction(() -> {
			MAConfig.CLIENT.illumination.setLowestLightLevel(sldIlluminationLightLevel.getValueInt());
			txtIlluminationLightLevel.setText(String.valueOf(sldIlluminationLightLevel.getValueInt()));
			MAConfig.clientSpec.save();
		});
		
		txtIlluminationLightLevel.setReturnAction(() -> {
			sldIlluminationLightLevel.setValue(Double.parseDouble(txtIlluminationLightLevel.getText()));
		});
		
		this.addComponent(lblTitleIllumination, 5);
		this.addComponent(chkIlluminationEnabled, 5);
		this.addComponent(chkIlluminationUseBlockLight, 5);
		this.addComponent(sldIlluminationLightLevel, 5);
		this.addComponent(txtIlluminationLightLevel, 5);
		
		applyIlluminationValues(false);
	}
	
	private void positionIlluminationControls() {
		lblTitleIllumination.setPosition(centerX(), 25);
		
		chkIlluminationEnabled.setPosition(col_1, startY + 5);
		chkIlluminationUseBlockLight.setPosition(col_1, startY + rowStartY + 5);
		
		sldIlluminationLightLevel.setPosition(col_1, startY + (rowStartY * 2));
		txtIlluminationLightLevel.setPosition(col_1 + sliderWidth + 10, startY + (rowStartY * 2));
		
	}
	
	private void applyIlluminationValues(boolean applyDefaults) {
		
		chkIlluminationEnabled.setIsChecked(!applyDefaults ? MAConfig.CLIENT.illumination.enabled() : MAConfig_Defaults.Illumination.enabled);
		chkIlluminationUseBlockLight.setIsChecked(!applyDefaults ? MAConfig.CLIENT.illumination.useBlockLight() : MAConfig_Defaults.Illumination.useBlockLight);
		
		sldIlluminationLightLevel.setValue(!applyDefaults ? MAConfig.CLIENT.illumination.lowestLightLevel() : MAConfig_Defaults.Illumination.lowestLightLevel);
		
		txtIlluminationLightLevel.setText(String.valueOf(!applyDefaults ? MAConfig.CLIENT.illumination.lowestLightLevel() : MAConfig_Defaults.Illumination.lowestLightLevel));
		
	}
	
	private void generateLumbinationControls() {
		lblTitleLumbination = new Label("Lumbination", 0, 0);
		lblTitleLumbination.setCentered();
		
		chkLumbinationEnabled                = new CheckBox(0, 0, Functions.localize("minersadvantage.lumbination.enabled"), false);
		chkLumbinationChopTreeBelow          = new CheckBox(0, 0, Functions.localize("minersadvantage.lumbination.chop_below"), false);
		chkLumbinationDestroyLeaves          = new CheckBox(0, 0, Functions.localize("minersadvantage.lumbination.destroy_leaves"), false);
		chkLumbinationLeavesAffectDurability = new CheckBox(0, 0, Functions.localize("minersadvantage.lumbination.leaves_affect_durability"), false);
		chkLumbinationReplantSaplings        = new CheckBox(0, 0, Functions.localize("minersadvantage.lumbination.replant_saplings"), false);
		chkLumbinationUseShearsOnLeaves      = new CheckBox(0, 0, Functions.localize("minersadvantage.lumbination.use_shears"), false);
		
		sldLumbinationLeafRange  = new Slider(0, 0, sliderWidth, 20, Functions.localize("minersadvantage.lumbination.leaf_range") + ": ", "", 0, 16, 0, false, true);
		sldLumbinationTrunkRange = new Slider(0, 0, sliderWidth, 20, Functions.localize("minersadvantage.lumbination.trunk_range") + ": ", "", 8, 128, 8, false, true);
		
		txtLumbinationLeafRange  = new TextFieldExt(0, 0, 50);
		txtLumbinationTrunkRange = new TextFieldExt(0, 0, 50);
		
		slLumbinationAxes   = new StringList(0, 0, 50, 50, Functions.localize("minersadvantage.lumbination.axes"), null);
		slLumbinationLeaves = new StringList(0, 0, 50, 50, Functions.localize("minersadvantage.lumbination.leaves"), null);
		slLumbinationLogs   = new StringList(0, 0, 50, 50, Functions.localize("minersadvantage.lumbination.logs"), null);
		
		txtLumbinationLeafRange.setText(String.valueOf(MAConfig.CLIENT.lumbination.leafRange()));
		txtLumbinationTrunkRange.setText(String.valueOf(MAConfig.CLIENT.lumbination.trunkRange()));
		
		chkLumbinationEnabled.setTooltips(SplitTooltips(Functions.localize("minersadvantage.lumbination.enabled.comment")));
		chkLumbinationChopTreeBelow.setTooltips(SplitTooltips(Functions.localize("minersadvantage.lumbination.chop_below.comment")));
		chkLumbinationDestroyLeaves.setTooltips(SplitTooltips(Functions.localize("minersadvantage.lumbination.destroy_leaves.comment")));
		chkLumbinationLeavesAffectDurability.setTooltips(SplitTooltips(Functions.localize("minersadvantage.lumbination.leaves_affect_durability.comment")));
		chkLumbinationReplantSaplings.setTooltips(SplitTooltips(Functions.localize("minersadvantage.lumbination.replant_saplings.comment")));
		chkLumbinationUseShearsOnLeaves.setTooltips(SplitTooltips(Functions.localize("minersadvantage.lumbination.use_shears.comment")));
		txtLumbinationLeafRange.setTooltips(SplitTooltips(Functions.localize("minersadvantage.lumbination.leaf_range.comment")));
		txtLumbinationTrunkRange.setTooltips(SplitTooltips(Functions.localize("minersadvantage.lumbination.trunk_range.comment")));
		
		chkLumbinationEnabled.setChangeListener(() -> {
			MAConfig.CLIENT.lumbination.setEnabled(chkLumbinationEnabled.isChecked());
			MAConfig.clientSpec.save();
		});
		chkLumbinationChopTreeBelow.setChangeListener(() -> {
			MAConfig.CLIENT.lumbination.setChopTreeBelow(chkLumbinationChopTreeBelow.isChecked());
			MAConfig.clientSpec.save();
		});
		chkLumbinationDestroyLeaves.setChangeListener(() -> {
			MAConfig.CLIENT.lumbination.setDestroyLeaves(chkLumbinationDestroyLeaves.isChecked());
			MAConfig.clientSpec.save();
		});
		chkLumbinationLeavesAffectDurability.setChangeListener(() -> {
			MAConfig.CLIENT.lumbination.setLeavesAffectDurability(chkLumbinationLeavesAffectDurability.isChecked());
			MAConfig.clientSpec.save();
		});
		chkLumbinationReplantSaplings.setChangeListener(() -> {
			MAConfig.CLIENT.lumbination.setReplantSaplings(chkLumbinationReplantSaplings.isChecked());
			MAConfig.clientSpec.save();
		});
		chkLumbinationUseShearsOnLeaves.setChangeListener(() -> {
			MAConfig.CLIENT.lumbination.setUseShearsOnLeaves(chkLumbinationUseShearsOnLeaves.isChecked());
			MAConfig.clientSpec.save();
		});
		
		sldLumbinationLeafRange.setAction(() -> {
			MAConfig.CLIENT.lumbination.setLeafRange(sldLumbinationLeafRange.getValueInt());
			txtLumbinationLeafRange.setText(String.valueOf(sldLumbinationLeafRange.getValueInt()));
			MAConfig.clientSpec.save();
		});
		sldLumbinationTrunkRange.setAction(() -> {
			MAConfig.CLIENT.lumbination.setTrunkRange(sldLumbinationTrunkRange.getValueInt());
			txtLumbinationTrunkRange.setText(String.valueOf(sldLumbinationTrunkRange.getValueInt()));
			MAConfig.clientSpec.save();
		});
		
		txtLumbinationLeafRange.setReturnAction(() -> {
			sldLumbinationLeafRange.setValue(Double.parseDouble(txtLumbinationLeafRange.getText()));
		});
		txtLumbinationTrunkRange.setReturnAction(() -> {
			sldLumbinationTrunkRange.setValue(Double.parseDouble(txtLumbinationTrunkRange.getText()));
		});
		
		slLumbinationAxes.setUpdatedCallback(() -> {
			MAConfig.CLIENT.lumbination.setAxes(slLumbinationAxes.getListValues());
			MAConfig.clientSpec.save();
		});
		
		slLumbinationLeaves.setUpdatedCallback(() -> {
			MAConfig.CLIENT.lumbination.setLeaves(slLumbinationLeaves.getListValues());
			MAConfig.clientSpec.save();
		});
		
		slLumbinationLogs.setUpdatedCallback(() -> {
			MAConfig.CLIENT.lumbination.setLogs(slLumbinationLogs.getListValues());
			MAConfig.clientSpec.save();
		});
		
		this.addComponent(lblTitleLumbination, 6);
		this.addComponent(chkLumbinationEnabled, 6);
		this.addComponent(chkLumbinationChopTreeBelow, 6);
		this.addComponent(chkLumbinationDestroyLeaves, 6);
		this.addComponent(chkLumbinationLeavesAffectDurability, 6);
		this.addComponent(chkLumbinationReplantSaplings, 6);
		this.addComponent(chkLumbinationUseShearsOnLeaves, 6);
		this.addComponent(sldLumbinationLeafRange, 6);
		this.addComponent(sldLumbinationTrunkRange, 6);
		this.addComponent(txtLumbinationLeafRange, 6);
		this.addComponent(txtLumbinationTrunkRange, 6);
		this.addComponent(slLumbinationAxes, 6);
		this.addComponent(slLumbinationLeaves, 6);
		this.addComponent(slLumbinationLogs, 6);
		
		applyLumbinationValues(false);
	}
	
	private void positionLumbinationControls() {
		lblTitleLumbination.setPosition(centerX(), 25);
		
		chkLumbinationEnabled.setPosition(col_1, startY + 5);
		chkLumbinationChopTreeBelow.setPosition(col_1, startY + rowStartY + 5);
		chkLumbinationDestroyLeaves.setPosition(col_1, startY + (rowStartY * 2) + 5);
		chkLumbinationLeavesAffectDurability.setPosition(col_1, startY + (rowStartY * 3) + 5);
		chkLumbinationReplantSaplings.setPosition(col_1, startY + (rowStartY * 4) + 5);
		chkLumbinationUseShearsOnLeaves.setPosition(col_1, startY + (rowStartY * 5) + 5);
		
		sldLumbinationLeafRange.setPosition(col_1, startY + (rowStartY * 6));
		txtLumbinationLeafRange.setPosition(col_1 + sliderWidth + 10, startY + (rowStartY * 6));
		
		sldLumbinationTrunkRange.setPosition(col_1, startY + (rowStartY * 7));
		txtLumbinationTrunkRange.setPosition(col_1 + sliderWidth + 10, startY + (rowStartY * 7));
		
		int listWidth = ((width - col_1 - 20) / 2) - 5;
		int col2      = (col_1 + listWidth + 10);
		slLumbinationAxes.adjustLayout(col2, startY, listWidth, rowStartY * 8);
		
		slLumbinationLeaves.adjustLayout(col_1, startY + (rowStartY * 8), listWidth, rowStartY * 7);
		slLumbinationLogs.adjustLayout(col2, startY + (rowStartY * 8), listWidth, rowStartY * 7);
		
	}
	
	private void applyLumbinationValues(boolean applyDefaults) {
		
		chkLumbinationEnabled.setIsChecked(!applyDefaults ? MAConfig.CLIENT.lumbination.enabled() : MAConfig_Defaults.Lumbination.enabled);
		chkLumbinationChopTreeBelow.setIsChecked(!applyDefaults ? MAConfig.CLIENT.lumbination.chopTreeBelow() : MAConfig_Defaults.Lumbination.chopTreeBelow);
		chkLumbinationDestroyLeaves.setIsChecked(!applyDefaults ? MAConfig.CLIENT.lumbination.destroyLeaves() : MAConfig_Defaults.Lumbination.destroyLeaves);
		chkLumbinationLeavesAffectDurability.setIsChecked(!applyDefaults ? MAConfig.CLIENT.lumbination.leavesAffectDurability() : MAConfig_Defaults.Lumbination.leavesAffectDurability);
		chkLumbinationReplantSaplings.setIsChecked(!applyDefaults ? MAConfig.CLIENT.lumbination.replantSaplings() : MAConfig_Defaults.Lumbination.replantSaplings);
		chkLumbinationUseShearsOnLeaves.setIsChecked(!applyDefaults ? MAConfig.CLIENT.lumbination.useShearsOnLeaves() : MAConfig_Defaults.Lumbination.useShearsOnLeaves);
		
		sldLumbinationLeafRange.setValue(!applyDefaults ? MAConfig.CLIENT.lumbination.leafRange() : MAConfig_Defaults.Lumbination.leafRange);
		sldLumbinationTrunkRange.setValue(!applyDefaults ? MAConfig.CLIENT.lumbination.trunkRange() : MAConfig_Defaults.Lumbination.trunkRange);
		
		txtLumbinationLeafRange.setText(String.valueOf(!applyDefaults ? MAConfig.CLIENT.lumbination.leafRange() : MAConfig_Defaults.Lumbination.leafRange));
		txtLumbinationTrunkRange.setText(String.valueOf(!applyDefaults ? MAConfig.CLIENT.lumbination.trunkRange() : MAConfig_Defaults.Lumbination.trunkRange));
		
		slLumbinationAxes.setListValues(new ArrayList<String>(!applyDefaults ? MAConfig.CLIENT.lumbination.axes() : MAConfig_Defaults.Lumbination.axes));
		slLumbinationLeaves.setListValues(new ArrayList<String>(!applyDefaults ? MAConfig.CLIENT.lumbination.leaves() : MAConfig_Defaults.Lumbination.leaves));
		slLumbinationLogs.setListValues(new ArrayList<String>(!applyDefaults ? MAConfig.CLIENT.lumbination.logs() : MAConfig_Defaults.Lumbination.logs));
		
	}
	
	private void generateShaftanationControls() {
		lblTitleShaftanation = new Label("Shaftanation", 0, 0);
		lblTitleShaftanation.setCentered();
		
		chkShaftanationEnabled = new CheckBox(0, 0, Functions.localize("minersadvantage.shaftanation.enabled"), false);
		
		sldShaftanationShaftLength = new Slider(0, 0, sliderWidth, 20, Functions.localize("minersadvantage.shaftanation.shaft_l") + ": ", "", 4, 128, 4, false, true);
		sldShaftanationShaftHeight = new Slider(0, 0, sliderWidth, 20, Functions.localize("minersadvantage.shaftanation.shaft_h") + ": ", "", 2, 16, 2, false, true);
		sldShaftanationShaftWidth  = new Slider(0, 0, sliderWidth, 20, Functions.localize("minersadvantage.shaftanation.shaft_w") + ": ", "", 1, 16, 1, false, true);
		
		esldShaftanationTorchPlacement = new <TorchPlacement>EnumSlider(0, 0, sliderWidth, 20, Functions.localize("minersadvantage.shaftanation.torch_placement") + ": ", "", TorchPlacement.class, TorchPlacement.FLOOR, true, () -> {
			MAConfig.CLIENT.shaftanation.setTorchPlacement((TorchPlacement) esldShaftanationTorchPlacement.getEnum());
			MAConfig.clientSpec.save();
		});
		
		txtShaftanationShaftLength = new TextFieldExt(0, 0, 50);
		txtShaftanationShaftHeight = new TextFieldExt(0, 0, 50);
		txtShaftanationShaftWidth  = new TextFieldExt(0, 0, 50);
		
		chkShaftanationEnabled.setTooltips(SplitTooltips(Functions.localize("minersadvantage.shaftanation.enabled.comment")));
		esldShaftanationTorchPlacement.setTooltips(SplitTooltips(Functions.localize("minersadvantage.shaftanation.torch_placement.comment")));
		txtShaftanationShaftLength.setTooltips(SplitTooltips(Functions.localize("minersadvantage.shaftanation.shaft_l.comment")));
		txtShaftanationShaftHeight.setTooltips(SplitTooltips(Functions.localize("minersadvantage.shaftanation.shaft_h.comment")));
		txtShaftanationShaftWidth.setTooltips(SplitTooltips(Functions.localize("minersadvantage.shaftanation.shaft_w.comment")));
		
		chkShaftanationEnabled.setChangeListener(() -> {
			MAConfig.CLIENT.shaftanation.setEnabled(chkShaftanationEnabled.isChecked());
			MAConfig.clientSpec.save();
		});
		
		sldShaftanationShaftLength.setAction(() -> {
			MAConfig.CLIENT.shaftanation.setShaftLength(sldShaftanationShaftLength.getValueInt());
			txtShaftanationShaftLength.setText(String.valueOf(sldShaftanationShaftLength.getValueInt()));
			MAConfig.clientSpec.save();
		});
		sldShaftanationShaftHeight.setAction(() -> {
			MAConfig.CLIENT.shaftanation.setShaftHeight(sldShaftanationShaftHeight.getValueInt());
			txtShaftanationShaftHeight.setText(String.valueOf(sldShaftanationShaftHeight.getValueInt()));
			MAConfig.clientSpec.save();
		});
		sldShaftanationShaftWidth.setAction(() -> {
			MAConfig.CLIENT.shaftanation.setShaftWidth(sldShaftanationShaftWidth.getValueInt());
			txtShaftanationShaftWidth.setText(String.valueOf(sldShaftanationShaftWidth.getValueInt()));
			MAConfig.clientSpec.save();
		});
		
		txtShaftanationShaftLength.setReturnAction(() -> {
			sldShaftanationShaftLength.setValue(Double.parseDouble(txtShaftanationShaftLength.getText()));
		});
		txtShaftanationShaftHeight.setReturnAction(() -> {
			sldShaftanationShaftHeight.setValue(Double.parseDouble(txtShaftanationShaftHeight.getText()));
		});
		txtShaftanationShaftWidth.setReturnAction(() -> {
			sldShaftanationShaftWidth.setValue(Double.parseDouble(txtShaftanationShaftWidth.getText()));
		});
		
		this.addComponent(lblTitleShaftanation, 7);
		this.addComponent(chkShaftanationEnabled, 7);
		this.addComponent(sldShaftanationShaftLength, 7);
		this.addComponent(sldShaftanationShaftHeight, 7);
		this.addComponent(sldShaftanationShaftWidth, 7);
		this.addComponent(esldShaftanationTorchPlacement, 7);
		this.addComponent(txtShaftanationShaftLength, 7);
		this.addComponent(txtShaftanationShaftHeight, 7);
		this.addComponent(txtShaftanationShaftWidth, 7);
		
		applyShaftanationValues(false);
	}
	
	private void positionShaftanationControls() {
		lblTitleShaftanation.setPosition(centerX(), 25);
		
		chkShaftanationEnabled.setPosition(col_1, startY + 5);
		
		sldShaftanationShaftLength.setPosition(col_1, startY + rowStartY);
		txtShaftanationShaftLength.setPosition(col_1 + sliderWidth + 10, startY + rowStartY);
		
		sldShaftanationShaftHeight.setPosition(col_1, startY + (rowStartY * 2));
		txtShaftanationShaftHeight.setPosition(col_1 + sliderWidth + 10, startY + (rowStartY * 2));
		
		sldShaftanationShaftWidth.setPosition(col_1, startY + (rowStartY * 3));
		txtShaftanationShaftWidth.setPosition(col_1 + sliderWidth + 10, startY + (rowStartY * 3));
		
		esldShaftanationTorchPlacement.setPosition(col_1, startY + (rowStartY * 4));
	}
	
	private void applyShaftanationValues(boolean applyDefaults) {
		
		chkShaftanationEnabled.setIsChecked(!applyDefaults ? MAConfig.CLIENT.shaftanation.enabled() : MAConfig_Defaults.Shaftanation.enabled);
		
		esldShaftanationTorchPlacement.setEnum(!applyDefaults ? MAConfig.CLIENT.shaftanation.torchPlacement() : MAConfig_Defaults.Shaftanation.torchPlacement);
		
		sldShaftanationShaftLength.setValue(!applyDefaults ? MAConfig.CLIENT.shaftanation.shaftLength() : MAConfig_Defaults.Shaftanation.shaftLength);
		sldShaftanationShaftHeight.setValue(!applyDefaults ? MAConfig.CLIENT.shaftanation.shaftHeight() : MAConfig_Defaults.Shaftanation.shaftHeight);
		sldShaftanationShaftWidth.setValue(!applyDefaults ? MAConfig.CLIENT.shaftanation.shaftWidth() : MAConfig_Defaults.Shaftanation.shaftWidth);
		
		txtShaftanationShaftLength.setText(String.valueOf(!applyDefaults ? MAConfig.CLIENT.shaftanation.shaftLength() : MAConfig_Defaults.Shaftanation.shaftLength));
		txtShaftanationShaftHeight.setText(String.valueOf(!applyDefaults ? MAConfig.CLIENT.shaftanation.shaftHeight() : MAConfig_Defaults.Shaftanation.shaftHeight));
		txtShaftanationShaftWidth.setText(String.valueOf(!applyDefaults ? MAConfig.CLIENT.shaftanation.shaftWidth() : MAConfig_Defaults.Shaftanation.shaftWidth));
		
	}
	
	private void generateSubstitutionControls() {
		lblTitleSubstitution = new Label("Substitution", 0, 0);
		lblTitleSubstitution.setCentered();
		
		chkSubstitutionEnabled           = new CheckBox(0, 0, Functions.localize("minersadvantage.substitution.enabled"), false);
		chkSubstitutionSwitchBack        = new CheckBox(0, 0, Functions.localize("minersadvantage.substitution.switchback"), false);
		chkSubstitutionFavourSilkTouch   = new CheckBox(0, 0, Functions.localize("minersadvantage.substitution.favour_silk"), false);
		chkSubstitutionFavourFortune     = new CheckBox(0, 0, Functions.localize("minersadvantage.substitution.favour_fortune"), false);
		chkSubstitutionIgnoreIfValidTool = new CheckBox(0, 0, Functions.localize("minersadvantage.substitution.ignore_valid"), false);
		chkSubstitutionIgnorePassiveMobs = new CheckBox(0, 0, Functions.localize("minersadvantage.substitution.ignore_passive"), false);
		
		slSubstitutionBlacklist = new StringList(0, 0, 50, 50, Functions.localize("minersadvantage.substitution.blacklist"), null);
		
		chkSubstitutionEnabled.setTooltips(SplitTooltips(Functions.localize("minersadvantage.substitution.enabled.comment")));
		chkSubstitutionSwitchBack.setTooltips(SplitTooltips(Functions.localize("minersadvantage.substitution.switchback.comment")));
		chkSubstitutionFavourSilkTouch.setTooltips(SplitTooltips(Functions.localize("minersadvantage.substitution.favour_silk.comment")));
		chkSubstitutionFavourFortune.setTooltips(SplitTooltips(Functions.localize("minersadvantage.substitution.favour_fortune.comment")));
		chkSubstitutionIgnoreIfValidTool.setTooltips(SplitTooltips(Functions.localize("minersadvantage.substitution.ignore_valid.comment")));
		chkSubstitutionIgnorePassiveMobs.setTooltips(SplitTooltips(Functions.localize("minersadvantage.substitution.ignore_passive.comment")));
		
		chkSubstitutionEnabled.setChangeListener(() -> {
			MAConfig.CLIENT.substitution.setEnabled(chkSubstitutionEnabled.isChecked());
			MAConfig.clientSpec.save();
		});
		chkSubstitutionSwitchBack.setChangeListener(() -> {
			MAConfig.CLIENT.substitution.setSwitchBack(chkSubstitutionSwitchBack.isChecked());
			MAConfig.clientSpec.save();
		});
		chkSubstitutionFavourSilkTouch.setChangeListener(() -> {
			MAConfig.CLIENT.substitution.setFavourSilkTouch(chkSubstitutionFavourSilkTouch.isChecked());
			MAConfig.clientSpec.save();
		});
		chkSubstitutionFavourFortune.setChangeListener(() -> {
			MAConfig.CLIENT.substitution.setFavourFortune(chkSubstitutionFavourFortune.isChecked());
			MAConfig.clientSpec.save();
		});
		chkSubstitutionIgnoreIfValidTool.setChangeListener(() -> {
			MAConfig.CLIENT.substitution.setIgnoreIfValidTool(chkSubstitutionIgnoreIfValidTool.isChecked());
			MAConfig.clientSpec.save();
		});
		chkSubstitutionIgnorePassiveMobs.setChangeListener(() -> {
			MAConfig.CLIENT.substitution.setIgnorePassiveMobs(chkSubstitutionIgnorePassiveMobs.isChecked());
			MAConfig.clientSpec.save();
		});
		
		slSubstitutionBlacklist.setUpdatedCallback(() -> {
			MAConfig.CLIENT.substitution.setToolBlacklist(slSubstitutionBlacklist.getListValues());
			MAConfig.clientSpec.save();
		});
		
		this.addComponent(lblTitleSubstitution, 8);
		this.addComponent(chkSubstitutionEnabled, 8);
		this.addComponent(chkSubstitutionSwitchBack, 8);
		this.addComponent(chkSubstitutionFavourSilkTouch, 8);
		this.addComponent(chkSubstitutionFavourFortune, 8);
		this.addComponent(chkSubstitutionIgnoreIfValidTool, 8);
		this.addComponent(chkSubstitutionIgnorePassiveMobs, 8);
		this.addComponent(slSubstitutionBlacklist, 8);
		
		applySubstitutionValues(false);
	}
	
	private void positionSubstitutionControls() {
		
		lblTitleSubstitution.setPosition(centerX(), 25);
		
		chkSubstitutionEnabled.setPosition(col_1, startY + 5);
		chkSubstitutionSwitchBack.setPosition(col_1, startY + rowStartY + 5);
		chkSubstitutionFavourSilkTouch.setPosition(col_1, startY + (rowStartY * 2) + 5);
		chkSubstitutionFavourFortune.setPosition(col_1, startY + (rowStartY * 3) + 5);
		chkSubstitutionIgnoreIfValidTool.setPosition(col_1, startY + (rowStartY * 4) + 5);
		chkSubstitutionIgnorePassiveMobs.setPosition(col_1, startY + (rowStartY * 5) + 5);
		
		slSubstitutionBlacklist.adjustLayout(col_1, startY + (rowStartY * 6), width - col_1 - 20, height - (startY + (rowStartY * 6)) - 60);
		
	}
	
	private void applySubstitutionValues(boolean applyDefaults) {
		
		chkSubstitutionEnabled.setIsChecked(!applyDefaults ? MAConfig.CLIENT.substitution.enabled() : MAConfig_Defaults.Substitution.enabled);
		chkSubstitutionSwitchBack.setIsChecked(!applyDefaults ? MAConfig.CLIENT.substitution.switchBack() : MAConfig_Defaults.Substitution.switchBack);
		chkSubstitutionFavourSilkTouch.setIsChecked(!applyDefaults ? MAConfig.CLIENT.substitution.favourSilkTouch() : MAConfig_Defaults.Substitution.favourSilkTouch);
		chkSubstitutionFavourFortune.setIsChecked(!applyDefaults ? MAConfig.CLIENT.substitution.favourFortune() : MAConfig_Defaults.Substitution.favourFortune);
		chkSubstitutionIgnoreIfValidTool.setIsChecked(!applyDefaults ? MAConfig.CLIENT.substitution.ignoreIfValidTool() : MAConfig_Defaults.Substitution.ignoreIfValidTool);
		chkSubstitutionIgnorePassiveMobs.setIsChecked(!applyDefaults ? MAConfig.CLIENT.substitution.ignorePassiveMobs() : MAConfig_Defaults.Substitution.ignorePassiveMobs);
		
		slSubstitutionBlacklist.setListValues(new ArrayList<String>(!applyDefaults ? MAConfig.CLIENT.substitution.blacklist() : MAConfig_Defaults.Substitution.blacklist));
		
	}
	
	private void generateVeinationControls() {
		lblTitleVeination = new Label("Veination", 0, 0);
		lblTitleVeination.setCentered();
		
		chkVeinationEnabled = new CheckBox(0, 0, Functions.localize("minersadvantage.veination.enabled"), false);
		
		slVeinationOres = new StringList(0, 0, 50, 50, Functions.localize("minersadvantage.veination.ores"), null);
		
		chkVeinationEnabled.setTooltips(SplitTooltips(Functions.localize("minersadvantage.veination.enabled.comment")));
		
		chkVeinationEnabled.setChangeListener(() -> {
			MAConfig.CLIENT.veination.setEnabled(chkVeinationEnabled.isChecked());
			MAConfig.clientSpec.save();
		});
		
		slVeinationOres.setUpdatedCallback(() -> {
			MAConfig.CLIENT.veination.setOres(slVeinationOres.getListValues());
			MAConfig.clientSpec.save();
		});
		
		this.addComponent(lblTitleVeination, 9);
		this.addComponent(chkVeinationEnabled, 9);
		this.addComponent(slVeinationOres, 9);
		
		applyVeinationValues(false);
	}
	
	private void positionVeinationControls() {
		
		lblTitleVeination.setPosition(centerX(), 25);
		
		chkVeinationEnabled.setPosition(col_1, startY + 5);
		
		slVeinationOres.adjustLayout(col_1, startY + rowStartY, width - col_1 - 20, height - (startY + rowStartY) - 60);
		
	}
	
	private void applyVeinationValues(boolean applyDefaults) {
		
		chkVeinationEnabled.setIsChecked(!applyDefaults ? MAConfig.CLIENT.veination.enabled() : MAConfig_Defaults.Veination.enabled);
		
		slVeinationOres.setListValues(new ArrayList<String>(!applyDefaults ? MAConfig.CLIENT.veination.ores() : MAConfig_Defaults.Veination.ores));
		
	}
	
	private void resetDefaults(Button btn) {
		
		if (btn == btnResetCommon || btn == btnFullReset) {
			applyCommonValues(true);
			applyClientValues(true);
		}
		if (btn == btnResetCaptivation || btn == btnFullReset) {
			applyCaptivationValues(true);
		}
		if (btn == btnResetCropination || btn == btnFullReset) {
			applyCropinationValues(true);
		}
		if (btn == btnResetExcavation || btn == btnFullReset) {
			applyExcavationValues(true);
		}
		if (btn == btnResetIllumination || btn == btnFullReset) {
			applyIlluminationValues(true);
		}
		if (btn == btnResetLumbination || btn == btnFullReset) {
			applyLumbinationValues(true);
		}
		if (btn == btnResetPathanation || btn == btnFullReset) {
			applyPathanationValues(true);
		}
		if (btn == btnResetShaftanation || btn == btnFullReset) {
			applyShaftanationValues(true);
		}
		if (btn == btnResetSubstitution || btn == btnFullReset) {
			applySubstitutionValues(true);
		}
		if (btn == btnResetVeination || btn == btnFullReset) {
			applyVeinationValues(true);
		}
		
	}
}
