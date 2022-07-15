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
	private FrameBuffer[] agentStateFbo;
	private FrameBuffer[] agentOutputFbo;
	private FrameBuffer singleChannelFbo;
	private FrameBuffer renderFbo;

	private SpriteBatch batch;
	private ShaderProgram screenShader;
	private ShaderProgram mergeChannelShader;
	private ShaderProgram singleChannelShader;
	private OrthographicCamera cam;
	private Texture blank;

	Texture deb;
	
	private float scale = 5f;
	
	static Color encode(int v) {
		float r = ((v & 0xff000000) >>> 24) / 255f;
		float g = ((v & 0x00ff0000) >>> 16) / 255f;
		float b = ((v & 0x0000ff00) >>> 8) / 255f;
		float a = ((v & 0x000000ff)) / 255f;
		return new Color(r, g, b, a);
	}

	@Override
	public void create() {

		batch = new SpriteBatch();

		Pixmap pix = new Pixmap((int) (Gdx.graphics.getWidth() / scale), (int) (Gdx.graphics.getHeight() / scale),
				Format.RGB888);

		Random r = new Random(1);
		pix.setColor(0xffffffff);
		for (int x = 0; x < pix.getWidth(); x++) {
			for (int y = 0; y < pix.getHeight(); y++) {
				if (r.nextFloat() < 0.2f) {
					pix.drawPixel(x, y);
				}
			}
		}
		Texture tex = new Texture(pix);

		screenStateFbo = new FrameBuffer[3];
		screenOutputFbo = new FrameBuffer[3];
		for (int i = 0; i < screenStateFbo.length; i++) {
			screenStateFbo[i] = new FrameBuffer(Format.RGB888, tex.getWidth(), tex.getHeight(), false);
			screenOutputFbo[i] = new FrameBuffer(Format.RGB888, tex.getWidth(), tex.getHeight(), false);
			
			screenStateFbo[i].begin();
			batch.begin();
			batch.draw(tex, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			batch.end();
			screenStateFbo[i].end();
		}

		singleChannelFbo = new FrameBuffer(Format.RGB888, tex.getWidth(), tex.getHeight(), false);
		renderFbo = new FrameBuffer(Format.RGB888, tex.getWidth(), tex.getHeight(), false);

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
		mergeChannelShader = new ShaderProgram(Gdx.files.internal("shaders/passthrough.vert"),
				Gdx.files.internal("shaders/mergechannel.frag"));
		if (!mergeChannelShader.isCompiled()) {
			System.out.println("mergeChannel:");
			System.out.println(mergeChannelShader.getLog());
		}
		singleChannelShader = new ShaderProgram(Gdx.files.internal("shaders/passthrough.vert"),
				Gdx.files.internal("shaders/singlechannel.frag"));
		if (!singleChannelShader.isCompiled()) {
			System.out.println("singleChannel:");
			System.out.println(singleChannelShader.getLog());
		}

		cam = new OrthographicCamera();
		cam.position.add(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, 0f);
	}

	@Override
	public void render() {
		
		cam.update();

		screenShader.bind();
		screenShader.setUniformf("frameDimensions",
				new Vector2(screenStateFbo[0].getWidth(), screenStateFbo[0].getHeight()));

		step(screenStateFbo, screenOutputFbo, screenShader);

//		for (int i = 0; i < screenOutputFbo.length; i++) {
//			if (scale > 1f) {
//				screenOutputFbo[i].getColorBufferTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
//			} else {
//				screenOutputFbo[i].getColorBufferTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
//			}
//		}

		singleChannelShader.bind();
		singleChannelShader.setUniformi("channel", 2);
		singleChannelFbo.begin();
		batch.setShader(singleChannelShader);
		batch.begin();
		TextureRegion t = new TextureRegion(screenOutputFbo[1].getColorBufferTexture());
		t.flip(false, true);
		batch.draw(t, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.end();
		singleChannelFbo.end();
		
		
		
		mergeChannelShader.bind();
		singleChannelFbo.getColorBufferTexture().bind(1);
		mergeChannelShader.setUniformf(1, 1);
		mergeChannelShader.setUniformi("clear", 1);
		batch.setShader(mergeChannelShader);
		renderFbo.begin();
		batch.begin();
		t = new TextureRegion(singleChannelFbo.getColorBufferTexture());
		t.flip(false, true);
		batch.draw(t, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.end();
		renderFbo.end();
		
		batch.setShader(null);
		batch.setProjectionMatrix(cam.combined);
		batch.begin();
		Gdx.graphics.getGL20().glActiveTexture(GL20.GL_TEXTURE0);
		batch.draw(renderFbo.getColorBufferTexture(), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.end();

		if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
			Gdx.app.exit();
		}
	}

	private void step(FrameBuffer[] state, FrameBuffer[] output, ShaderProgram shader) {

		for (int i = 0; i < state.length; i++) {
			batch.setShader(shader);
			output[i].begin();
			batch.begin();
			TextureRegion t = new TextureRegion(state[i].getColorBufferTexture());
			t.flip(false, true);
			batch.draw(t, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			batch.end();
			output[i].end();

			batch.setShader(null);
			state[i].begin();
			batch.begin();
			t = new TextureRegion(output[i].getColorBufferTexture());
			t.flip(false, true);
			batch.draw(t, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			batch.end();
			state[i].end();
		}
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
