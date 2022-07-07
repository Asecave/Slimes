package com.asecave.main;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;

public class Main extends ApplicationAdapter {

	private SpriteBatch batch;
	private SpriteBatch batch2;
	private OrthographicCamera cam;
	private FrameBuffer fbo;
	private Texture tex;
	private ShaderProgram shader;

	private long startTime = 0l;
	private final int scale = 1;

	@Override
	public void create() {

		batch = new SpriteBatch();

		cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		Pixmap pix = new Pixmap(Gdx.graphics.getWidth() / scale, Gdx.graphics.getHeight() / scale, Format.RGB888);

		pix.setColor(Color.WHITE);
		for (int x = 0; x < pix.getWidth(); x++) {
			for (int y = 0; y < pix.getHeight(); y++) {
				if (Math.random() < 0.2f) {
					pix.drawPixel(x, y);
				}
			}
		}

		tex = new Texture(pix);

		if (fbo != null)
			fbo.dispose();
		fbo = new FrameBuffer(Format.RGB888, tex.getWidth(), tex.getHeight(), false);

		batch2 = new SpriteBatch();

		ShaderProgram.pedantic = false;
		shader = new ShaderProgram(Gdx.files.internal("shaders/slime.vert"), Gdx.files.internal("shaders/slime.frag"));
		if (!shader.isCompiled()) {
			System.out.println(shader.getLog());
		}

	}

	long last = System.currentTimeMillis();

	@Override
	public void render() {

		if (startTime == 0l) {
			startTime = System.nanoTime();
		}

		cam.update();

		
		fbo.begin();

		if (System.currentTimeMillis() - last > 200) {
			shader.bind();
			shader.setUniformf("totalTime", (System.nanoTime() - startTime) / 1E9f);
			shader.setUniformf("dt", Gdx.graphics.getDeltaTime());
			shader.setUniformf("frameDimensions", new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
			batch.setShader(shader);
			last = System.currentTimeMillis();
		} else {
			batch.setShader(null);
		}
		batch.begin();
		batch.draw(tex, 0, 0, tex.getWidth(), tex.getHeight());
		batch.end();
		fbo.end();
		

		batch2.setProjectionMatrix(cam.combined);
		batch2.begin();
		Texture t = fbo.getColorBufferTexture();
		t.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		batch2.draw(t, -Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, Gdx.graphics.getWidth(),
				-Gdx.graphics.getHeight());
		batch2.end();
		
		tex = t;
	}

	@Override
	public void dispose() {
		batch.dispose();
		batch2.dispose();
		fbo.dispose();
		tex.dispose();
	}

	@Override
	public void resize(int width, int height) {
		cam.viewportWidth = width;
		cam.viewportHeight = height;
	}
}
