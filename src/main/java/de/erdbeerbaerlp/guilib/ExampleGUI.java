package de.erdbeerbaerlp.guilib;

import java.util.Random;

import de.erdbeerbaerlp.guilib.components.Button;
import de.erdbeerbaerlp.guilib.components.CheckBox;
import de.erdbeerbaerlp.guilib.components.EnumSlider;
import de.erdbeerbaerlp.guilib.components.Image;
import de.erdbeerbaerlp.guilib.components.Label;
import de.erdbeerbaerlp.guilib.components.ProgressBar;
import de.erdbeerbaerlp.guilib.components.ScrollPanel;
import de.erdbeerbaerlp.guilib.components.Slider;
import de.erdbeerbaerlp.guilib.components.Spinner;
import de.erdbeerbaerlp.guilib.components.TextField;
import de.erdbeerbaerlp.guilib.components.ToggleButton;
import de.erdbeerbaerlp.guilib.gui.ExtendedScreen;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.BossInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ExampleGUI extends ExtendedScreen {
	private Button       exampleButton;
	private TextField    exampleTextField;
	private Label        exampleLabel1;
	private Button       exitButton;
	private CheckBox     exampleCheckbox;
	private Slider       exampleSlider1;
	private EnumSlider   exampleSlider2;
	private ToggleButton exampleToggleButton;
	private EnumSlider   drawTypeSlider;
	private Image        beeGif;
	private Image        apple;
	private Image        dynamicImage;
	private TextField    urlField;
	private Button       nextPageButton;
	private Button       prevPageButton;
	private Label        pageIndicator;
	private ScrollPanel  scrollPanel;
	private Button       scrollButton1, scrollButton2;
	private TextField    scrollTextField;
	private Slider       xSlider, ySlider, contentSlider;
	private Image        pikachu;
	private Slider       scrollSlider;
	
	private Spinner      spinner;
	private ProgressBar  pBar;
	private Slider       pBarSlider;
	private EnumSlider   pBarColorSlider;
	private Button       pBarLoader;
	private ToggleButton spinnerPlayPause;
	
	public ExampleGUI(Screen parent) {
		super(parent);
	}
	
	private Thread loader;
	
	@Override
	public void buildGui() {
		
		// Initialize variables
		exampleButton       = new Button(50, 50, "Button", Button.DefaultButtonIcons.SAVE);
		exampleTextField    = new TextField(50, 100, 150);
		exampleLabel1       = new Label("Example GUI", width / 2, 10);
		exitButton          = new Button(0, 0, Button.DefaultButtonIcons.DELETE);
		exampleCheckbox     = new CheckBox(50, 70, "Checkbox", false);
		exampleSlider1      = new Slider(50, 130, "Slider: ", 0, 100, 50, () -> System.out.println(exampleSlider1.getValue()));
		exampleSlider2      = new <ExampleEnum>EnumSlider(200, 130, "Enum Slider: ", ExampleEnum.class, ExampleEnum.EXAMPLE, () -> {
								System.out.println("Enum changed to \"" + ((ExampleEnum) exampleSlider2.getEnum()).getName() + "\"");
								System.out.println("Index: " + exampleSlider2.getValueInt());
								System.out.println("Other Value: " + ((ExampleEnum) exampleSlider2.getEnum()).getOtherValue());
							});
		exampleToggleButton = new ToggleButton(50, 170, "Toggle Button: ");
		drawTypeSlider      = new <ToggleButton.DrawType>EnumSlider(156, 170, "Draw type: ", ToggleButton.DrawType.class, ToggleButton.DrawType.COLORED_LINE, () -> this.exampleToggleButton.setDrawType((ToggleButton.DrawType) drawTypeSlider.getEnum()));
		beeGif              = new Image(250, 40, 64, 64);
		apple               = new Image(0, 0);
		
		dynamicImage   = new Image(0, 0, 300, 180);
		urlField       = new TextField(0, 0, 240);
		nextPageButton = new Button(0, 0, 40, ">");
		prevPageButton = new Button(0, 0, 40, "<");
		pageIndicator  = new Label(0, 0);
		
		scrollPanel     = new ScrollPanel(0, 0, 300, 200);
		scrollButton1   = new Button(30, 10, "Button 1");
		scrollButton2   = new Button(30, 330, "Button 2");
		scrollTextField = new TextField(2, 300, 100);
		scrollSlider    = new Slider(10, 212, "Another Slider: ", -20, 20, 0, () -> {
							System.out.println("Changed to " + scrollSlider.getValueInt());
						});
		
		xSlider       = new Slider(10, 20, "ScrollPanel X Pos", 0, 350, 0, () -> scrollPanel.setX(xSlider.getValueInt()));
		ySlider       = new Slider(10, 50, "ScrollPanel Y Pos", 0, 255, 0, () -> scrollPanel.setY(ySlider.getValueInt()));
		contentSlider = new Slider(10, 80, "ScrollPanel Content length", 0, 1200, 500, () -> scrollPanel.setContentHeight(contentSlider.getValueInt()));
		pikachu       = new Image(150, 40, 120, 70);
		
		spinner          = new Spinner(0, 0);
		pBar             = new ProgressBar(0, 0, 200);
		pBarSlider       = new Slider(0, 0, "ProgressBar value: ", 0, 100, 0, () -> {
								pBar.setValue(pBarSlider.getValueInt());
							});
		pBarColorSlider  = new EnumSlider(0, 0, "Progress Bar Color: ", BossInfo.Color.class, BossInfo.Color.BLUE, () -> {
								pBar.setColor((BossInfo.Color) pBarColorSlider.getEnum());
							});
		pBarLoader       = new Button(0, 0, "Load Something", Button.DefaultButtonIcons.PLAY);
		spinnerPlayPause = new ToggleButton(0, 0, Button.DefaultButtonIcons.PAUSE, Button.DefaultButtonIcons.PLAY);
		
		// Load images
		beeGif.loadImage("https://gamepedia.cursecdn.com/minecraft_gamepedia/thumb/5/58/Bee.gif/120px-Bee.gif");
		apple.loadImage(new ResourceLocation("minecraft", "textures/item/apple.png"));
		dynamicImage.loadImage("https://www.minecraft.net/etc.clientlibs/minecraft/clientlibs/main/resources/img/header/logo.png");
		pikachu.loadImage("https://i.pinimg.com/originals/9f/b1/25/9fb125f1fedc8cc62ab5b20699ebd87d.gif");
		
		// Register listeners
		exampleButton.setClickListener(() -> System.out.println("I have been clicked!"));
		exampleCheckbox.setChangeListener(() -> System.out.println(exampleCheckbox.isChecked() ? "I just got Checked" : "I just have been unchecked :/"));
		exitButton.setClickListener(this::close);
		exampleTextField.setReturnAction(() -> exampleButton.setText(exampleTextField.getText()));
		exampleToggleButton.setClickListener(() -> {
			System.out.println("New Value: " + exampleToggleButton.getValue());
			this.exampleButton.setEnabled(exampleToggleButton.getValue());
		});
		apple.setCallback(() -> {
			minecraft.getSoundHandler().play(SimpleSound.master(SoundEvents.ENTITY_PLAYER_BURP, 1));
			apple.setVisible(false);
			apple.disable();
		});
		urlField.setText(dynamicImage.getImageURL());
		urlField.setReturnAction(() -> dynamicImage.loadImage(urlField.getText()));
		nextPageButton.setClickListener(this::nextPage);
		prevPageButton.setClickListener(this::prevPage);
		pBarLoader.setClickListener(() -> {
			if (this.loader == null || !this.loader.isAlive()) {
				this.loader = new Thread(() -> {
					pBarSlider.disable();
					pBar.setValue(0);
					pBar.setMaxValue(140);
					pBarSlider.setValue(0);
					pBarSlider.setMaxValue(140);
					for (int i = 0; i < 140; i++) {
						pBar.increment(1);
						pBarSlider.setValue(pBar.getValue());
						try {
							Thread.sleep(new Random().nextInt(5) * 100);
						}
						catch (InterruptedException ignored) {}
					}
					try {
						Thread.sleep(3000);
					}
					catch (InterruptedException ignored) {}
					pBar.setValue(0);
					pBar.setMaxValue(100);
					pBarSlider.setValue(0);
					pBarSlider.setMaxValue(100);
					pBarSlider.enable();
				});
				this.loader.start();
			}
		});
		
		// Set tooltips
		exampleButton.setTooltips("Example Tooltip", "This is a Button");
		exampleCheckbox.setTooltips("Another Tooltip", "This is a Checkbox", "");
		exampleTextField.setTooltips("A simple Textbox", "This one does support colors too!", "Simply use a \u00A7", "Press return to run a callback");
		exitButton.setTooltips("Closes this GUI");
		exampleSlider1.setTooltips("A simple double/integer slider");
		exampleSlider2.setTooltips("This slider works using Enums", "It will change through all enum values");
		exampleToggleButton.setTooltips("This button can be toggled");
		drawTypeSlider.setTooltips("Change how the toggle button will be rendered");
		beeGif.setTooltips("This is an example image", "It was loaded from an URL");
		apple.setTooltips("Oh, look at this apple!", "", "Click to eat");
		scrollButton1.setTooltips("An button in an scroll panel");
		pikachu.setTooltips("Click me to open an URL");
		
		scrollButton1.setClickListener(() -> System.out.println("TEST1"));
		scrollButton2.setClickListener(() -> System.out.println("TEST2"));
		scrollTextField.setReturnAction(() -> System.out.println(scrollTextField.getText()));
		pikachu.setCallback(() -> {
			// Open an URL
			openURL("http://pikachu.com", (opened) -> {
				if (!opened) {
					pikachu.hide();
				}
			});
		});
		spinnerPlayPause.setClickListener(() -> {
			if (spinnerPlayPause.getValue())
				spinner.resume();
			else
				spinner.stop();
		});
		
		// Set some values
		exampleTextField.setAcceptsColors(true);
		exampleTextField.setText("Text Field");
		exampleTextField.setLabel("Text Field Label");
		scrollTextField.setLabel("Example Text field in scroll panel");
		scrollTextField.setText("TEEXT");
		exampleLabel1.setCentered();
		pageIndicator.setCentered();
		exampleToggleButton.setValue(true);
		urlField.setLabel("Image URL");
		urlField.setMaxStringLength(1024);
		pBar.setText("Progress Bar:");
		pBarSlider.setShowDecimal(false);
		spinnerPlayPause.setValue(true);
		spinnerPlayPause.setDrawType(ToggleButton.DrawType.STRING_OR_ICON);
		
		// Add components
		this.addAllComponents(exampleLabel1, exitButton, prevPageButton, nextPageButton, pageIndicator);
		this.addComponent(exampleButton, 0);
		this.addComponent(exampleCheckbox, 0);
		this.addComponent(exampleTextField, 0);
		this.addComponent(exampleSlider1, 0);
		this.addComponent(exampleSlider2, 0);
		this.addComponent(exampleToggleButton, 0);
		this.addComponent(drawTypeSlider, 0);
		this.addComponent(beeGif, 0);
		this.addComponent(apple, 0);
		this.addComponent(dynamicImage, 1);
		this.addComponent(urlField, 1);
		this.addComponent(scrollPanel, 2);
		this.addComponent(xSlider, 3);
		this.addComponent(ySlider, 3);
		this.addComponent(contentSlider, 3);
		this.addComponent(spinner, 4);
		this.addComponent(spinnerPlayPause, 4);
		this.addComponent(pBar, 4);
		this.addComponent(pBarSlider, 4);
		this.addComponent(pBarColorSlider, 4);
		this.addComponent(pBarLoader, 4);
		
		scrollPanel.addAllComponents(scrollButton1, scrollButton2, scrollTextField, pikachu, scrollSlider);
		scrollPanel.fitContent();
	}
	
	@Override
	public void updateGui() {
		// Update positions
		exampleLabel1.setX(width / 2); // always centered!
		
		nextPageButton.setPosition(width - nextPageButton.getWidth() - 6, height - nextPageButton.getHeight() - 15);
		prevPageButton.setPosition(6, nextPageButton.getY());
		pageIndicator.setPosition(width / 2, height - 13);
		pageIndicator.setText("Page " + getCurrentPage());
		
		exitButton.setX(width - exitButton.getWidth() - 6);
		exitButton.setY(6);
		
		apple.setPosition(exitButton.getX() - 40, exitButton.getY() + 30);
		
		dynamicImage.setPosition(width / 2 - 150, 50);
		urlField.setPosition(dynamicImage.getX(), dynamicImage.getY() - 30);
		urlField.setWidth(dynamicImage.getWidth());
		spinner.setPosition(width / 2 - 16, pBar.getY() - 50);
		spinnerPlayPause.setPosition(spinner.getX() + 32 + 20, spinner.getY() + 8);
		pBar.setPosition(spinner.getX() - pBar.getWidth() / 2, height / 2);
		pBarSlider.setPosition(pBar.getX() + 25, pBar.getY() + 40);
		pBarColorSlider.setPosition(pBarSlider.getX(), pBarSlider.getY() + pBarSlider.getHeight() + 4);
		pBarLoader.setPosition(pBarSlider.getX() - pBarLoader.getWidth() - 12, pBarSlider.getY() + pBarSlider.getHeight() + 4);
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
	
	@Override
	public boolean doesEscCloseGui() {
		return true;
	}
	
	/**
	 * An example enum
	 */
	public enum ExampleEnum {
		THIS("This", 1.0f), IS("is", 100.45f), AN("an", -90f), EXAMPLE("example", 1337f), SLIDER("slider", -2387f), WITH("with", 0.000000001f), ENUM("enum", 3.14159265f), VALUES("values.", 98234.5f), YAAAY("Yaay!", -0f);
		
		private String name;
		private float  otherValue;
		
		ExampleEnum(String name, float someOtherValue) {
			this.name       = name;
			this.otherValue = someOtherValue;
		}
		
		// This method will be called using reflection from the slider
		public String getName() {
			return name;
		}
		
		public float getOtherValue() {
			return otherValue;
		}
	}
	
}
