package com.asecave.main;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

public class MainCpu extends ApplicationAdapter {

	private SpriteBatch batch;
	private OrthographicCamera cam;
	private FrameBuffer fbo;

	private SpaceCpu space;

	private final int scale = 5;

	@Override
	public void create() {

		batch = new SpriteBatch();

		cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		if (fbo != null)
			fbo.dispose();
		fbo = new FrameBuffer(Format.RGB888, Gdx.graphics.getWidth() / scale, Gdx.graphics.getHeight() / scale, false);

		space = new SpaceCpu(Gdx.graphics.getWidth() / scale, Gdx.graphics.getHeight() / scale);
		
		cam.position.set(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f, 0f);
	}

	@Override
	public void render() {

		cam.update();

		space.update();
		batch.setProjectionMatrix(cam.combined);
		batch.begin();
		batch.draw(space.getTexture(), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.end();

		if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
			Gdx.app.exit();
		}
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
