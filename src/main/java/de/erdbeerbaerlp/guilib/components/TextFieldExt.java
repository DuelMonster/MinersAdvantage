package de.erdbeerbaerlp.guilib.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

public class TextFieldExt extends TextField {
	
	private int      waitInterval    = 1500;
	private Runnable idleCallback;
	private boolean  keyBeingPressed = false;
	
	/**
	 * Creates an new text field
	 *
	 * @param x X position
	 * @param y Y position
	 * @param width Text Field width
	 */
	public TextFieldExt(int x, int y, int width) {
		this(x, y, width, 20);
	}
	
	/**
	 * Creates an new text field
	 *
	 * @param x X position
	 * @param y Y position
	 * @param width Text Field width
	 * @param height Text Field height
	 */
	public TextFieldExt(int x, int y, int width, int height) {
		super(x, y, width, height);
	}
	
	public boolean isKeyBeingPressed() {
		return keyBeingPressed;
	}
	
	/**
	 * @param waitInterval the waitInterval to set
	 */
	public void setWaitInterval(int waitInterval) {
		this.waitInterval = waitInterval;
	}
	
	/**
	 * Sets a callback to fire after typing has stopped
	 *
	 * @param action Runnable to fire
	 */
	public void setIdleCallback(Runnable idleCallback) {
		this.idleCallback = idleCallback;
	}
	
	@Override
	public void setFocused(boolean isFocused) {
		super.setFocused(isFocused);
	}
	
	@Override
	public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		keyBeingPressed = false;
		
		// Setup and execute timer to fire stopped typing event
		TextFieldExt _this = this;
		Timer        timer = new Timer(waitInterval, new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent arg0) {
									if (!keyBeingPressed && _this.idleCallback != null) _this.idleCallback.run();
								}
							});
		timer.setRepeats(false); // Only execute once
		timer.start(); // Go go go!
		
		return super.keyReleased(keyCode, scanCode, modifiers);
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		keyBeingPressed = true;
		
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
}
