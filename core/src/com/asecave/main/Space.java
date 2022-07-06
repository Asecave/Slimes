package com.asecave.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class Space {

	private Agent[] agents;
	private float agentSpeed = 40f;
	private float evaporateSpeed = 0.001f;

	public Space(int centerX, int centerY) {
		agents = new Agent[1000];
		for (int i = 0; i < agents.length; i++) {
			agents[i] = new Agent(centerX, centerY);
		}
	}

	public void update(Pixmap pix) {

		for (int i = 0; i < agents.length; i++) {
			Agent agent = agents[i];
			Vector2 direction = new Vector2(MathUtils.cos(agent.angle), MathUtils.sin(agent.angle));
			Vector2 newPos = new Vector2(agent.pos).add(direction.scl(Gdx.graphics.getDeltaTime() * agentSpeed));

			if (newPos.x < 0 || newPos.x >= pix.getWidth() || newPos.y < 0 || newPos.y >= pix.getHeight()) {

				int random = hash(hash((int) (agent.pos.y * pix.getWidth() + agent.pos.x
						+ hash(i + (int) (Gdx.graphics.getDeltaTime() * 100000)))));
				float randomAngle = nhash(random) * 2f * 3.1415f;

				newPos.x = Math.min(pix.getWidth() - 1, Math.max(0, newPos.x));
				newPos.y = Math.min(pix.getHeight() - 1, Math.max(0, newPos.y));
				agents[i].angle = randomAngle;
			}
			
			agent.pos = newPos;
			
			pix.setColor(Color.WHITE);
			pix.drawPixel((int) agent.pos.x, (int) agent.pos.y);
		}
		
		for (int x = 0; x < pix.getWidth(); x++) {
			for (int y = 0; y < pix.getHeight(); y++) {
				Color c = new Color(pix.getPixel(x, y));
				float d = evaporateSpeed * Gdx.graphics.getDeltaTime();
				pix.setColor(c.sub(d, d, d, 0f));
				pix.drawPixel(x, y);
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
}
