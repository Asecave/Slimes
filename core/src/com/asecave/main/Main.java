package com.asecave.main;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

public class Main extends ApplicationAdapter {

	private SpriteBatch batch;
	private SpriteBatch batch2;
	private Pixmap pix;
	private OrthographicCamera cam;
	private FrameBuffer fbo;
	
	private Space space;

	private final int scale = 5;

	@Override
	public void create() {

		batch = new SpriteBatch();

		cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		pix = new Pixmap(Gdx.graphics.getWidth() / scale, Gdx.graphics.getHeight() / scale, Format.RGB888);

		if (fbo != null)
			fbo.dispose();
		fbo = new FrameBuffer(Format.RGB888, pix.getWidth(), pix.getHeight(), false);

		batch2 = new SpriteBatch();
		space = new Space(pix.getWidth() / 2, pix.getHeight() / 2);
	}

	@Override
	public void render() {
		
		cam.update();

		fbo.begin();

		Texture t = new Texture(pix);
		space.update(pix);
		batch.begin();
		batch.draw(t, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.end();
		t.dispose();
		fbo.end();

		batch2.setProjectionMatrix(cam.combined);
		batch2.begin();
		t = fbo.getColorBufferTexture();
		t.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		batch2.draw(t, -Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, Gdx.graphics.getWidth(),
				-Gdx.graphics.getHeight());
		batch2.end();
	}

	@Override
	public void dispose() {
	}

	@Override
	public void resize(int width, int height) {
		cam.viewportWidth = width;
		cam.viewportHeight = height;
	}
}
