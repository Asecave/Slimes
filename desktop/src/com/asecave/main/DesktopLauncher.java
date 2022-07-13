package com.asecave.main;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static void main(String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setForegroundFPS(100);
		config.useVsync(false);
		config.setTitle("Slimes");
		config.setWindowedMode(1680, 1050);
		config.setWindowedMode(Lwjgl3ApplicationConfiguration.getDisplayMode().width, Lwjgl3ApplicationConfiguration.getDisplayMode().height);
//		config.setFullscreenMode(
//				Lwjgl3ApplicationConfiguration.getDisplayMode(Lwjgl3ApplicationConfiguration.getPrimaryMonitor()));

//		Color c = new Color(0xffffffff);
//		for (int i = 0; i < 10000000; i++) {
//			int f = decode(c);
//			f -= 10;
//			c = encode(f);
//			System.out.println(c);
//		}

		new Lwjgl3Application(new Main(), config);
	}

//	static int decode(Color v) {
//		return ((int)(255 * v.r) << 24) | ((int)(255 * v.g) << 16) | ((int)(255 * v.b) << 8) | ((int)(255 * v.a));
//	}
//
//	static Color encode(int v) {
//		float r = ((v & 0xff000000) >>> 24) / 255f;
//		float g = ((v & 0x00ff0000) >>> 16) / 255f;
//		float b = ((v & 0x0000ff00) >>> 8) / 255f;
//		float a = ((v & 0x000000ff)) / 255f;
//		return new Color(r, g, b, a);
//	}
}
