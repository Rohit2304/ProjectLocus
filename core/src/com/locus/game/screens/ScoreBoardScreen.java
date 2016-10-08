package com.locus.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.locus.game.ProjectLocus;
import com.locus.game.tools.Text;

import java.util.ArrayList;

/**
 * Created by Rohit Yadav on 05-Oct-16.
 * Scoreboard Screen
 */

public class ScoreBoardScreen implements Screen, InputProcessor, GestureDetector.GestureListener {

    private class PlayerResultData {

        private Sprite shipSprite;
        private Text playerScoreText, playerNumberText;

        PlayerResultData(Sprite shipSprite, Text playerReadyText, Text playerNumberText) {
            this.shipSprite = shipSprite;
            this.playerNumberText = playerNumberText;
            this.playerScoreText = playerReadyText;
        }

    }

    private float backgroundMovementAngleRad;

    private ProjectLocus projectLocus;
    private LobbyScreen lobbyScreen;
    private OrthographicCamera foregroundCamera, backgroundCamera;
    private TiledMapRenderer tiledMapRenderer;
    private InputMultiplexer inputMultiplexer;
    private ArrayList<PlayerResultData> playerResultDataList;

    private static final int ROW_PADDING = 50, COLUMN_PADDING = 50, SHIP_PADDING = 34,
            MARGIN_TOP = 80;

    private Text backToLobby;
    private Vector3 backToLobbyBBMinimum, backToLobbyBBMaximum;
    private BoundingBox backToLobbyBB;

    ScoreBoardScreen(ProjectLocus projectLocus, LobbyScreen lobbyScreen,
                     float backgroundMovementAngleRad) {
        this.projectLocus = projectLocus;
        this.lobbyScreen = lobbyScreen;

        this.backgroundMovementAngleRad = backgroundMovementAngleRad;

        foregroundCamera = new OrthographicCamera(ProjectLocus.screenCameraWidth,
                ProjectLocus.screenCameraHeight);

        backgroundCamera = new OrthographicCamera(ProjectLocus.worldCameraWidth,
                ProjectLocus.worldCameraHeight);

        tiledMapRenderer = new OrthogonalTiledMapRenderer(projectLocus.tiledMapList.get(0),
                ProjectLocus.TILED_MAP_SCALE);

        backToLobbyBBMinimum = new Vector3();
        backToLobbyBBMaximum = new Vector3();
        backToLobbyBB = new BoundingBox();
        backToLobby = new Text(projectLocus.font32, "Back To Lobby ->");
        playerResultDataList = new ArrayList<PlayerResultData>();

        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(new GestureDetector(this));
        inputMultiplexer.addProcessor(this);

    }

    private void positionUI() {

        int row, col;
        float colWidth = (ProjectLocus.screenCameraWidth - (5 * COLUMN_PADDING)) / 4,
                rowHeight = ((ProjectLocus.screenCameraHeight - 80) - (3 * ROW_PADDING)) / 2;

        PlayerResultData playerResultData;
        for (int i = 0; i < playerResultDataList.size(); i++) {

            row = i / 4;
            col = i % 4;

            playerResultData = playerResultDataList.get(i);

            playerResultData.shipSprite.setPosition(COLUMN_PADDING + (col * (colWidth + COLUMN_PADDING))
                            + ((colWidth - playerResultData.shipSprite.getWidth()) / 2),
                    ROW_PADDING + (((row + 1) % 2) * (rowHeight + ROW_PADDING)) +
                            ((rowHeight - playerResultData.shipSprite.getHeight()) / 2));

            playerResultData.playerNumberText.setPosition(playerResultData.shipSprite.getX() +
                            ((playerResultData.shipSprite.getWidth() / 2)
                                    - playerResultData.playerNumberText.getHalfWidth()),
                    playerResultData.shipSprite.getY() +
                            playerResultData.shipSprite.getHeight() + SHIP_PADDING);

            playerResultData.playerScoreText.setPosition(playerResultData.shipSprite.getX() +
                            ((playerResultData.shipSprite.getWidth() / 2)
                                    - playerResultData.playerScoreText.getHalfWidth()),
                    playerResultData.shipSprite.getY() - SHIP_PADDING / 2);

        }

        backToLobbyBBMinimum.set(ProjectLocus.screenCameraWidth - COLUMN_PADDING
                - backToLobby.getWidth() - 20, ProjectLocus.screenCameraHeight - MARGIN_TOP +
                ROW_PADDING - backToLobby.getHalfHeight() - 20, 0);
        backToLobbyBBMaximum.set(backToLobbyBBMinimum.x + backToLobby.getWidth() + 20,
                backToLobbyBBMinimum.y + backToLobby.getHeight() + 20, 0);
        backToLobbyBB.set(backToLobbyBBMinimum, backToLobbyBBMaximum);

        backToLobby.setPosition(
                ProjectLocus.screenCameraWidth - COLUMN_PADDING - backToLobby.getWidth(),
                ProjectLocus.screenCameraHeight - MARGIN_TOP + ROW_PADDING -
                        backToLobby.getHalfHeight());

    }

    private void drawScoreBoardScreen(SpriteBatch spriteBatch) {

        for (PlayerResultData playerResultData : playerResultDataList) {
            playerResultData.playerNumberText.draw(spriteBatch);
            playerResultData.shipSprite.draw(spriteBatch);
            playerResultData.playerScoreText.draw(spriteBatch);
        }
        backToLobby.draw(spriteBatch);

    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        backgroundMovementAngleRad += delta * ProjectLocus.SCREEN_CAMERA_MOVEMENT_SPEED;
        backgroundCamera.position.set(
                ProjectLocus.WORLD_HALF_WIDTH + ProjectLocus.SCREEN_CAMERA_MOVEMENT_RADIUS *
                        MathUtils.cos(backgroundMovementAngleRad),
                ProjectLocus.WORLD_HALF_HEIGHT + ProjectLocus.SCREEN_CAMERA_MOVEMENT_RADIUS *
                        MathUtils.sin(backgroundMovementAngleRad), 0);
        backgroundCamera.update();

        tiledMapRenderer.setView(backgroundCamera);
        tiledMapRenderer.render();

        projectLocus.spriteBatch.setProjectionMatrix(foregroundCamera.combined);
        projectLocus.spriteBatch.begin();
        drawScoreBoardScreen(projectLocus.spriteBatch);
        projectLocus.spriteBatch.end();
    }

    @Override
    public void resize(int width, int height) {
        ProjectLocus.resizeCamera(width, height);
        foregroundCamera.setToOrtho(false, ProjectLocus.screenCameraWidth,
                ProjectLocus.screenCameraHeight);
        backgroundCamera.setToOrtho(false, ProjectLocus.worldCameraWidth,
                ProjectLocus.worldCameraHeight);
        positionUI();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Input.Keys.ENTER:
            case Input.Keys.BACK:
                projectLocus.setScreen(lobbyScreen);
                break;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector3 touchPosition = new Vector3(screenX, screenY, 0);
        foregroundCamera.unproject(touchPosition);
        if (backToLobbyBB.contains(touchPosition)) {
            projectLocus.setScreen(lobbyScreen);
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }
}