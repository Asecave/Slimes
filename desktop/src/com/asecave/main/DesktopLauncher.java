package com.asecave.main;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setForegroundFPS(0);
		config.useVsync(true);
		config.setTitle("Slimes");
		config.setWindowedMode(1680, 1050);
//		config.setWindowedMode(Lwjgl3ApplicationConfiguration.getDisplayMode().width, Lwjgl3ApplicationConfiguration.getDisplayMode().height);
		new Lwjgl3Application(new Main(), config);
	}
}
