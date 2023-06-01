package com.asecave.main;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;

public class Main extends ApplicationAdapter {

	private FrameBuffer[] screenStateFbo;
	private FrameBuffer[] screenOutputFbo;
	private FrameBuffer renderBuffer;
	private final int agentCount = 50000;

	private SpriteBatch batch;
	private ShaderProgram screenShader;
	private ShaderProgram renderShader;
	private OrthographicCamera cam;
	private Texture blank;

	private float scale = 1f;

	private int width;
	private int height;

	private Agent[] agents;

	@Override
	public void create() {

		batch = new SpriteBatch();

		width = (int) (Gdx.graphics.getWidth() / scale);
		height = (int) (Gdx.graphics.getHeight() / scale);

		Agent.width = width;
		Agent.height = height;

		screenStateFbo = new FrameBuffer[3];
		screenOutputFbo = new FrameBuffer[3];
		for (int i = 0; i < screenStateFbo.length; i++) {
			screenStateFbo[i] = new FrameBuffer(Format.RGB888, width, height, false);
			screenOutputFbo[i] = new FrameBuffer(Format.RGB888, width, height, false);
		}

		blank = new Texture(1, 1, Format.RGB888);
		
		renderBuffer = new FrameBuffer(Format.RGB888, width, height, false);

		ShaderProgram.pedantic = false;
		screenShader = new ShaderProgram(Gdx.files.internal("shaders/passthrough.vert"),
				Gdx.files.internal("shaders/screen.frag"));
		if (!screenShader.isCompiled()) {
			System.out.println("Screen:");
			System.out.println(screenShader.getLog());
		}
		renderShader = new ShaderProgram(Gdx.files.internal("shaders/passthrough.vert"),
				Gdx.files.internal("shaders/render.frag"));
		if (!renderShader.isCompiled()) {
			System.out.println("render:");
			System.out.println(renderShader.getLog());
		}

		agents = new Agent[agentCount];
		for (int i = 0; i < agents.length; i++) {
			agents[i] = new Agent();
		}

		cam = new OrthographicCamera();
		cam.position.add(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, 0f);
	}

	@Override
	public void render() {

		cam.update();

		screenShader.bind();
		screenShader.setUniformf("dt", Gdx.graphics.getDeltaTime());
		screenShader.setUniformf("frameDimensions",
				new Vector2(screenStateFbo[0].getWidth(), screenStateFbo[0].getHeight()));
		screenShader.setUniformi("agentCount", agentCount);

		Pixmap pix = new Pixmap(width, height, Format.RGB888);
		pix.setColor(Color.WHITE);
		for (int i = 0; i < agents.length; i++) {
			agents[i].update();
			pix.drawPixel((int) agents[i].posX, (int) agents[i].posY);
		}
		Texture tex = new Texture(pix);
		tex.bind(1);
		screenShader.setUniformi("agents", 1);
		Gdx.graphics.getGL20().glActiveTexture(GL20.GL_TEXTURE0);

		for (int i = 0; i < screenStateFbo.length; i++) {
			step(screenStateFbo[i], screenOutputFbo[i], screenShader);
		}

		pix.dispose();
		tex.dispose();

		renderCombined(screenOutputFbo);

		if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
			Gdx.app.exit();
		}
	}

	private void step(FrameBuffer state, FrameBuffer output, ShaderProgram shader) {
		batch.setShader(shader);
		output.begin();
		batch.begin();
		TextureRegion t = new TextureRegion(state.getColorBufferTexture());
		t.flip(false, true);
		t.getTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		batch.draw(t, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.end();
		output.end();

		batch.setShader(null);
		state.begin();
		batch.begin();
		t = new TextureRegion(output.getColorBufferTexture());
		t.flip(false, true);
		t.getTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		batch.draw(t, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.end();
		state.end();
	}

	private void renderCombined(FrameBuffer[] output) {
		renderShader.bind();
		for (int i = 0; i < output.length; i++) {
			if (scale > 1f) {
				screenOutputFbo[i].getColorBufferTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
			} else {
				screenOutputFbo[i].getColorBufferTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
			}
			screenOutputFbo[i].getColorBufferTexture().bind(i + 1);
			if (i == 0)
				renderShader.setUniformi("channelR", i + 1);
			if (i == 1)
				renderShader.setUniformi("channelG", i + 1);
			if (i == 2)
				renderShader.setUniformi("channelB", i + 1);
		}
		Gdx.graphics.getGL20().glActiveTexture(GL20.GL_TEXTURE0);
		batch.setShader(renderShader);
		renderBuffer.begin();
		batch.begin();
		batch.draw(blank, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.end();

		if (Agent.screen != null)
			Agent.screen.dispose();
		Agent.screen = Pixmap.createFromFrameBuffer(0, 0, width, height);
		
		
		renderBuffer.end();
		
		batch.setShader(null);
		batch.begin();
		batch.draw(renderBuffer.getColorBufferTexture(), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.end();
	}

	private void dumpPixel(int x, int y) {
		Pixmap pixmap = Pixmap.createFromFrameBuffer(0, 0, x + 1, y + 1);
		System.out.println(new Color(pixmap.getPixel(x, y)));
	}

	@Override
	public void dispose() {
		batch.dispose();
		for (int i = 0; i < screenStateFbo.length; i++) {
			screenStateFbo[i].dispose();
			screenOutputFbo[i].dispose();
		}
	}

	@Override
	public void resize(int width, int height) {
		cam.viewportWidth = width;
		cam.viewportHeight = height;
	}
}
