package com.asecave.main;

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

	private SpriteBatch batch;
	private FrameBuffer fbo1;
	private FrameBuffer fbo2;
	private Texture tex;
	private ShaderProgram shader;
	private OrthographicCamera cam;

	private long startTime = 0l;
	private float scale = 10f;
	private int minFrameTime = 0;

	@Override
	public void create() {

		batch = new SpriteBatch();

		Pixmap pix = new Pixmap((int) (Gdx.graphics.getWidth() / scale), (int) (Gdx.graphics.getHeight() / scale),
				Format.RGB888);

		pix.setColor(Color.WHITE);
		for (int x = 0; x < pix.getWidth(); x++) {
			for (int y = 0; y < pix.getHeight(); y++) {
				if (Math.random() < 0.3f) {
					pix.drawPixel(x, y);
				}
			}
		}

		tex = new Texture(pix);

		fbo1 = new FrameBuffer(Format.RGB888, tex.getWidth(), tex.getHeight(), false);
		fbo2 = new FrameBuffer(Format.RGB888, tex.getWidth(), tex.getHeight(), false);

		fbo1.begin();
		batch.begin();
		batch.draw(tex, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.end();
		fbo1.end();

		ShaderProgram.pedantic = false;
		shader = new ShaderProgram(Gdx.files.internal("shaders/slime.vert"), Gdx.files.internal("shaders/slime.frag"));
		if (!shader.isCompiled()) {
			System.out.println(shader.getLog());
		}

		cam = new OrthographicCamera();
		cam.position.add(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, 0f);
	}

	long last = System.currentTimeMillis();

	@Override
	public void render() {

		if (startTime == 0l) {
			startTime = System.nanoTime();
		}

		cam.update();

		if (System.currentTimeMillis() - last > minFrameTime) {
			shader.bind();
			shader.setUniformf("totalTime", (System.nanoTime() - startTime) / 1E9f);
			shader.setUniformf("dt", Gdx.graphics.getDeltaTime());
			shader.setUniformf("frameDimensions", new Vector2(fbo1.getWidth(), fbo1.getHeight()));
			batch.setShader(shader);
			last = System.currentTimeMillis();

		}
		fbo2.begin();
		batch.begin();
		TextureRegion t = new TextureRegion(fbo1.getColorBufferTexture());
		t.flip(false, true);
		batch.draw(t, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.end();
		fbo2.end();

		batch.setShader(null);
		fbo1.begin();
		batch.begin();
		t = new TextureRegion(fbo2.getColorBufferTexture());
		t.flip(false, true);
		batch.draw(t, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.end();
		fbo1.end();

		batch.setProjectionMatrix(cam.combined);
		batch.begin();
		t = new TextureRegion(fbo2.getColorBufferTexture());
		t.flip(false, true);
		if (scale > 1f) {
			t.getTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		}
		batch.draw(t, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.end();

		if (Gdx.input.isKeyPressed(Keys.ESCAPE)) {
			Gdx.app.exit();
		}
	}

	@Override
	public void dispose() {
		batch.dispose();
		fbo1.dispose();
		fbo2.dispose();
		tex.dispose();
	}

	@Override
	public void resize(int width, int height) {
		cam.viewportWidth = width;
		cam.viewportHeight = height;
	}
}
