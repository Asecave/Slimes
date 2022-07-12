package com.asecave.main;

import java.util.Random;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
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

	private FrameBuffer screenStateFbo;
	private FrameBuffer screenOutputFbo;
	private FrameBuffer agentStateFbo;
	private FrameBuffer agentOutputFbo;
	
	private SpriteBatch batch;
	private Texture tex;
	private ShaderProgram screenShader;
	private OrthographicCamera cam;

	private float scale = 4f;

	@Override
	public void create() {

		batch = new SpriteBatch();

		Pixmap pix = new Pixmap((int) (Gdx.graphics.getWidth() / scale), (int) (Gdx.graphics.getHeight() / scale),
				Format.RGB888);

		Random r = new Random();
		pix.setColor(Color.WHITE);
		for (int x = 0; x < pix.getWidth(); x++) {
			for (int y = 0; y < pix.getHeight(); y++) {
				if (r.nextFloat() < 0.2f) {
					pix.drawPixel(x, y);
				}
			}
		}

		tex = new Texture(pix);

		screenStateFbo = new FrameBuffer(Format.RGB888, tex.getWidth(), tex.getHeight(), false);
		screenOutputFbo = new FrameBuffer(Format.RGB888, tex.getWidth(), tex.getHeight(), false);

		agentStateFbo = new FrameBuffer(Format.RGB888, tex.getWidth(), tex.getHeight(), false);

		screenStateFbo.begin();
		batch.begin();
		batch.draw(tex, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.end();
		screenStateFbo.end();

		ShaderProgram.pedantic = false;
		screenShader = new ShaderProgram(Gdx.files.internal("shaders/passthrough.vert"),
				Gdx.files.internal("shaders/slimeScreen.frag"));
		if (!screenShader.isCompiled()) {
			System.out.println(screenShader.getLog());
		}

		cam = new OrthographicCamera();
		cam.position.add(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, 0f);
	}

	@Override
	public void render() {

		cam.update();

		screenShader.bind();
		screenShader.setUniformf("dt", Gdx.graphics.getDeltaTime());
		screenShader.setUniformf("frameDimensions", new Vector2(screenStateFbo.getWidth(), screenStateFbo.getHeight()));

		step(screenStateFbo, screenOutputFbo, screenShader);
		
		batch.setProjectionMatrix(cam.combined);
		batch.begin();
		TextureRegion t = new TextureRegion(screenOutputFbo.getColorBufferTexture());
		t.flip(false, true);
		if (scale > 1f) {
			t.getTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		} else {
			t.getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		}
		batch.draw(t, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.end();

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

	@Override
	public void dispose() {
		batch.dispose();
		screenStateFbo.dispose();
		screenOutputFbo.dispose();
		tex.dispose();
	}

	@Override
	public void resize(int width, int height) {
		cam.viewportWidth = width;
		cam.viewportHeight = height;
	}
}
