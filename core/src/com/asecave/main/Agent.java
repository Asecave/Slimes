package com.asecave.main;

import java.util.Random;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class Agent {

	public float posX;
	public float posY;
	public float angle;

	private static Random r = new Random(0);

	public static int width;
	public static int height;
	public static Pixmap screen;
	
	private final static float SENSOR_ANGLE_DEG = 20f;
	private final static float SENSOR_OFFSET = 10f;
	private final static int SENSOR_SIZE = 1;
	private final static float TURN_SPEED = 0.2f;

	public Agent() {
		float radius = (height / 2 - 10) * (float) Math.sqrt(r.nextFloat());
		float theta = r.nextFloat() * MathUtils.PI2;
		
		posX = width / 2 + radius * MathUtils.cos(theta);
		posY = height / 2 + radius * MathUtils.sin(theta);

		angle = (float) Math.atan2(height / 2 - posY, width / 2 - posX);
	}

	public void update() {

		posX += MathUtils.cos(angle);
		posY += MathUtils.sin(angle);

		if (screen == null)
			return;
		// Steer based on sensory data
		float sensorAngleRad = SENSOR_ANGLE_DEG * (3.1415f / 180f);
		float weightForward = sense(0);
		float weightLeft = sense(sensorAngleRad);
		float weightRight = sense(-sensorAngleRad);

		
		float randomSteerStrength = r.nextFloat();
		float turnSpeed = TURN_SPEED * 2f * 3.1415f;

		// Continue in same direction
		if (weightForward > weightLeft && weightForward > weightRight) {
			angle += 0;
		}
		else if (weightForward < weightLeft && weightForward < weightRight) {
			angle += (randomSteerStrength - 0.5) * 2 * turnSpeed;
		}
		// Turn right
		else if (weightRight > weightLeft) {
			angle -= randomSteerStrength * turnSpeed;
		}
		// Turn left
		else if (weightLeft > weightRight) {
			angle += randomSteerStrength * turnSpeed;
		}

		if (posX < 0) {
			angle = (r.nextFloat() - 0.5f) * MathUtils.PI;
		}
		if (posX > width) {
			angle = (r.nextFloat() - 0.5f) * MathUtils.PI + MathUtils.PI;
		}
		if (posY < 0) {
			angle = r.nextFloat() * MathUtils.PI;
		}
		if (posY > height) {
			angle = -r.nextFloat() * MathUtils.PI;
		}
	}

	float sense(float sensorAngleOffset) {
		float sensorAngle = angle + sensorAngleOffset;
		float sensorDirX = MathUtils.cos(sensorAngle);
		float sensorDirY = MathUtils.sin(sensorAngle);

		int sensorPosX = (int) (posX + sensorDirX * SENSOR_OFFSET);
		int sensorPosY = (int) (posY + sensorDirY * SENSOR_OFFSET);

		float sum = 0;

		float senseWeight = 1 * 2 - 1;

		for (int offsetX = -SENSOR_SIZE; offsetX <= SENSOR_SIZE; offsetX++) {
			for (int offsetY = -SENSOR_SIZE; offsetY <= SENSOR_SIZE; offsetY++) {
				int sampleX = Math.min(width - 1, Math.max(0, sensorPosX + offsetX));
				int sampleY = Math.min(height - 1, Math.max(0, sensorPosY + offsetY));
				Color c = new Color(screen.getPixel(sampleX, height - sampleY - 1));
				Vector3 p = new Vector3(c.r, c.g, c.b);
				sum += p.dot(senseWeight, senseWeight, senseWeight);
			}
		}

		return sum;
	}
}
