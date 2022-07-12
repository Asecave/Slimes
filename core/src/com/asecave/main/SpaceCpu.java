package com.asecave.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class SpaceCpu {

	private Agent[] agents;
	private float agentSpeed = 40f;
	private float evaporateSpeed = 0.25f;
	
	private float[][][] screen;
	
	private Texture tex;

	public SpaceCpu(int width, int height) {

		screen = new float[width][height][3];
		
		agents = new Agent[1000];
		for (int i = 0; i < agents.length; i++) {
			agents[i] = new Agent(width / 2, height / 2);
		}
	}

	public void update() {

		for (int i = 0; i < agents.length; i++) {
			Agent agent = agents[i];
			Vector2 direction = new Vector2(MathUtils.cos(agent.angle), MathUtils.sin(agent.angle));
			Vector2 newPos = new Vector2(agent.pos).add(direction.scl(Gdx.graphics.getDeltaTime() * agentSpeed));

			if (newPos.x < 0 || newPos.x >= screen.length || newPos.y < 0 || newPos.y >= screen[0].length) {

				int random = hash(hash((int) (agent.pos.y * screen.length + agent.pos.x
						+ hash(i + (int) (Gdx.graphics.getDeltaTime() * 100000)))));
				float randomAngle = nhash(random) * 2f * 3.1415f;

				newPos.x = Math.min(screen.length - 1, Math.max(0, newPos.x));
				newPos.y = Math.min(screen[0].length - 1, Math.max(0, newPos.y));
				agents[i].angle = randomAngle;
			}
			
			agent.pos = newPos;
			
			drawPixel((int) agent.pos.x, (int) agent.pos.y, 1f, 1f, 1f);
		}
		
		for (int x = 0; x < screen.length; x++) {
			for (int y = 0; y < screen[0].length; y++) {
				float r = screen[x][y][0];
				float g = screen[x][y][1];
				float b = screen[x][y][2];
				float d = evaporateSpeed * Gdx.graphics.getDeltaTime();
				drawPixel(x, y, r - d, g - d, b - d);
			}
		}
	}

	private float nhash(int state) {
		return hash(state) / 4294967295f + 0.5f;
	}

	private int hash(int state) {
		state ^= 600152772;
		state *= 506952122;
		state ^= state >> 16;
		state *= 506952122;
		state ^= state >> 16;
		state *= 506952122;
		return state;
	}

	private class Agent {
		Vector2 pos;
		float angle;

		public Agent(int x, int y) {
			pos = new Vector2(x, y);
			angle = MathUtils.random(MathUtils.PI2);
		}
	}

	public Texture getTexture() {
		Pixmap p = new Pixmap(screen.length, screen[0].length, Format.RGB888);
		for (int x = 0; x < screen.length; x++) {
			for (int y = 0; y < screen[0].length; y++) {
				p.setColor(screen[x][y][0], screen[x][y][1], screen[x][y][2], 1f);
				p.drawPixel(x, y);
			}
		}
		if (tex != null) {
			tex.dispose();
		}
		tex = new Texture(p);
		p.dispose();
		return tex;
	}
	
	private void drawPixel(int x, int y, float r, float g, float b) {
		screen[x][y][0] = Math.max(Math.min(r, 1), 0);
		screen[x][y][1] = Math.max(Math.min(g, 1), 0);
		screen[x][y][2] = Math.max(Math.min(b, 1), 0);
	}
}
