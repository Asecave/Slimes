package com.asecave.main;

import java.util.Random;

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
import com.badlogic.gdx.utils.ScreenUtils;

public class Main extends ApplicationAdapter {

	private FrameBuffer[] screenStateFbo;
	private FrameBuffer[] screenOutputFbo;
	private FrameBuffer agentStateFbo;
	private FrameBuffer agentOutputFbo;
	private final int agentCount = 100;

	private SpriteBatch batch;
	private ShaderProgram screenShader;
	private ShaderProgram renderShader;
	private ShaderProgram agentShader;
	private OrthographicCamera cam;
	private Texture blank;

	private float scale = 10f;

	@Override
	public void create() {

		batch = new SpriteBatch();

		int width = (int) (Gdx.graphics.getWidth() / scale);
		int height = (int) (Gdx.graphics.getHeight() / scale);

		screenStateFbo = new FrameBuffer[3];
		screenOutputFbo = new FrameBuffer[3];
		for (int i = 0; i < screenStateFbo.length; i++) {
			screenStateFbo[i] = new FrameBuffer(Format.RGB888, width, height, false);
			screenOutputFbo[i] = new FrameBuffer(Format.RGB888, width, height, false);
		}

		agentStateFbo = new FrameBuffer(Format.RGB888, agentCount, 2, false);
		agentOutputFbo = new FrameBuffer(Format.RGB888, agentCount, 2, false);

		Pixmap pix = new Pixmap(agentStateFbo.getWidth(), agentStateFbo.getHeight(), Format.RGB888);
		for (int x = 0; x < agentStateFbo.getWidth(); x++) {
			pix.drawPixel(x, 0, ((width / 2) << 8) + 0xff);
			pix.drawPixel(x, 1, ((height / 2) << 8) + 0xff);
		}
		
		Texture tex = new Texture(pix);
		
		agentStateFbo.begin();
		batch.begin();
		TextureRegion tr = new TextureRegion(tex);
		tr.flip(false, true);
		batch.draw(tr, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.end();
		agentStateFbo.end();
		
		pix.dispose();
		tex.dispose();

		blank = new Texture(1, 1, Format.RGB888);

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
		agentShader = new ShaderProgram(Gdx.files.internal("shaders/passthrough.vert"),
				Gdx.files.internal("shaders/agent.frag"));
		if (!agentShader.isCompiled()) {
			System.out.println("agent:");
			System.out.println(renderShader.getLog());
		}

		cam = new OrthographicCamera();
		cam.position.add(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, 0f);
	}

	@Override
	public void render() {

		cam.update();
		
		agentShader.bind();
		agentShader.setUniformf("dt", Gdx.graphics.getDeltaTime());
		agentShader.setUniformf("texDimensions",
				new Vector2(agentStateFbo.getWidth(), agentStateFbo.getHeight()));
		
		step(agentStateFbo, agentOutputFbo, agentShader);
		
		screenShader.bind();
		screenShader.setUniformf("dt", Gdx.graphics.getDeltaTime());
		screenShader.setUniformf("frameDimensions",
				new Vector2(screenStateFbo[0].getWidth(), screenStateFbo[0].getHeight()));
		screenShader.setUniformf("agentDimensions",
				new Vector2(agentStateFbo.getWidth(), agentStateFbo.getHeight()));
		screenShader.setUniformi("agentCount", agentCount);
		agentStateFbo.getColorBufferTexture().bind(1);
		screenShader.setUniformi(1, 1);
		Gdx.graphics.getGL20().glActiveTexture(GL20.GL_TEXTURE0);

		for (int i = 0; i < screenStateFbo.length; i++) {
			step(screenStateFbo[i], screenOutputFbo[i], screenShader);
		}

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
		batch.draw(t, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.end();
		output.end();

		batch.setShader(null);
		state.begin();
		batch.begin();
		t = new TextureRegion(output.getColorBufferTexture());
		t.flip(false, true);
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
			renderShader.setUniformi(i + 1, i + 1);
		}
		Gdx.graphics.getGL20().glActiveTexture(GL20.GL_TEXTURE0);
		batch.setShader(renderShader);
		batch.begin();
		batch.draw(blank, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.end();
	}

	private void dumpPixel(int x, int y) {
		Pixmap pixmap = Pixmap.createFromFrameBuffer(0, 0, x + 1, y  + 1);
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
