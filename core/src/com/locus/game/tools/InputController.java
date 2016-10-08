package com.locus.game.tools;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.locus.game.ProjectLocus;

import static com.badlogic.gdx.Gdx.input;

/**
 * Created by Rohit Yadav on 26-Sep-16.
 * Input Controller
 */
public class InputController implements InputProcessor, GestureDetector.GestureListener {

    public interface InputCallBack {

        void applyThrust(boolean isForward);

        void applyRotation(boolean isClockwise);

        void fire(boolean isPrimaryBulletEnabled, boolean isSecondaryBulletEnabled);

        void applyControls(boolean isThrustEnabled, boolean isThrustForward,
                           boolean isRotationEnabled, boolean isRotationClockwise,
                           boolean isFireEnabled, boolean isPrimaryBulletEnabled,
                           boolean isSecondaryBulletEnabled);

    }

    private float initialXCoordinateRotation, initialYCoordinateThrust, tapXCoordinate, tapYCoordinate;
    private OrthographicCamera camera;
    private InputCallBack inputCallBack;
    private static boolean isRotationEnabled, isThrustEnabled, isFireEnabled,
            isRotationClockwise, isThrustForward, isSetRotationPointer, isSetThrustPointer,
            isPrimaryBulletPointerEnabled, isSecondaryBulletPointerEnabled,
            isPrimaryBulletEnabled, isSecondaryBulletEnabled;
    private boolean isHost;
    private static int thrustPointerID, rotationPointerID, primaryBulletPointerID,
            secondaryBulletPointerID;

    public InputController(InputCallBack inputCallBack, boolean isHost) {
        this.inputCallBack = inputCallBack;
        this.isHost = isHost;
        camera = new OrthographicCamera(ProjectLocus.screenCameraWidth,
                ProjectLocus.screenCameraHeight);
        camera.position.set(ProjectLocus.screenCameraHalfWidth,
                ProjectLocus.screenCameraHalfHeight, 0);
        isRotationEnabled = isThrustEnabled = isFireEnabled = false;
        isRotationClockwise = isThrustForward = false;
        rotationPointerID = thrustPointerID = primaryBulletPointerID = secondaryBulletPointerID = -1;
        isSetRotationPointer = isSetThrustPointer = false;
        isPrimaryBulletEnabled = isSecondaryBulletEnabled = false;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.W || keycode == Input.Keys.UP) {
            isThrustEnabled = true;
            isThrustForward = true;
        }
        if (keycode == Input.Keys.S || keycode == Input.Keys.DOWN) {
            isThrustEnabled = true;
            isThrustForward = false;
        }
        if (keycode == Input.Keys.A || keycode == Input.Keys.LEFT) {
            isRotationEnabled = true;
            isRotationClockwise = false;
        }
        if (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT) {
            isRotationEnabled = true;
            isRotationClockwise = true;
        }
        if (keycode == Input.Keys.SPACE) {
            if (!isSecondaryBulletEnabled) {
                isPrimaryBulletEnabled = true;
                isSecondaryBulletEnabled = false;
            }
        }
        if (keycode == Input.Keys.CONTROL_LEFT || keycode == Input.Keys.CONTROL_RIGHT) {
            isSecondaryBulletEnabled = true;
            isPrimaryBulletEnabled = false;
        }
        isFireEnabled = isPrimaryBulletEnabled || isSecondaryBulletEnabled;
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.W || keycode == Input.Keys.UP ||
                keycode == Input.Keys.S || keycode == Input.Keys.DOWN) {
            isThrustEnabled = false;
        }
        if (keycode == Input.Keys.A || keycode == Input.Keys.LEFT ||
                keycode == Input.Keys.D || keycode == Input.Keys.RIGHT) {
            isRotationEnabled = false;
        }
        if (keycode == Input.Keys.SPACE) {
            isPrimaryBulletEnabled = false;
        }
        if (keycode == Input.Keys.CONTROL_LEFT || keycode == Input.Keys.CONTROL_RIGHT) {
            isSecondaryBulletEnabled = false;
            if (input.isKeyPressed(Input.Keys.SPACE)) {
                isPrimaryBulletEnabled = true;
            }
        }
        isFireEnabled = isPrimaryBulletEnabled || isSecondaryBulletEnabled;
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Gdx.app.log("TouchSample", "Touch Down");
        Vector3 touchPosition = new Vector3(screenX, screenY, 0);
        camera.unproject(touchPosition);
        if ((touchPosition.x > 0) && (!isSetThrustPointer)) {
            thrustPointerID = pointer;
            isSetThrustPointer = true;
            initialYCoordinateThrust = touchPosition.y;
        }
        if ((touchPosition.x < 0) && (!isSetRotationPointer)) {
            rotationPointerID = pointer;
            isSetRotationPointer = true;
            initialXCoordinateRotation = touchPosition.x;
        }
        if (primaryBulletPointerID == pointer) {
            if (isPrimaryBulletPointerEnabled) {
                isFireEnabled = isPrimaryBulletEnabled = (Math.abs(screenX - tapXCoordinate) < 20)
                        && (Math.abs(screenY - tapYCoordinate) < 20);
                isPrimaryBulletPointerEnabled = false;
            }
        }
        if (secondaryBulletPointerID == pointer) {
            if (isSecondaryBulletPointerEnabled) {
                isFireEnabled = isSecondaryBulletEnabled = (Math.abs(screenX - tapXCoordinate) < 20)
                        && (Math.abs(screenY - tapYCoordinate) < 20);
                isSecondaryBulletPointerEnabled = false;
            }
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        Gdx.app.log("TouchSample", "Touch Up");
        if ((rotationPointerID == pointer) && (isSetRotationPointer)) {
            isRotationEnabled = false;
            isSetRotationPointer = false;
        }
        if ((thrustPointerID == pointer) && (isSetThrustPointer)) {
            isThrustEnabled = false;
            isSetThrustPointer = false;
        }
        if (primaryBulletPointerID == pointer) {
            isPrimaryBulletEnabled = false;
//            isFireEnabled = isPrimaryBulletEnabled = isSecondaryBulletEnabled = false;
        }
        if (secondaryBulletPointerID == pointer) {
            isSecondaryBulletEnabled = false;
        }
        isFireEnabled = isPrimaryBulletEnabled || isSecondaryBulletEnabled;
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        Vector3 touchPosition = new Vector3(screenX, screenY, 0);
        camera.unproject(touchPosition);
        if ((pointer == thrustPointerID) && (isSetThrustPointer)) {
            if (touchPosition.x > 0) {
                if (Math.abs(touchPosition.y - initialYCoordinateThrust) > 16) {
                    isThrustForward = (initialYCoordinateThrust - touchPosition.y <= 0);
                    isThrustEnabled = true;
                } else {
                    isThrustEnabled = false;
                }
            } else {
                isThrustEnabled = false;
            }
        }
        if ((pointer == rotationPointerID) && (isSetRotationPointer)) {
            if (touchPosition.x < 0) {
                if (Math.abs(touchPosition.x - initialXCoordinateRotation) > 16) {
                    isRotationClockwise = (initialXCoordinateRotation - touchPosition.x <= 0);
                    isRotationEnabled = true;
                } else {
                    isRotationEnabled = false;
                }
            } else {
                isRotationEnabled = false;
            }
        }
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
        Vector3 touchPosition = new Vector3(x, y, 0);
        camera.unproject(touchPosition);
        if (count == 1) {
            Gdx.app.log("TouchSample", "Tap Detected");
            tapXCoordinate = x;
            tapYCoordinate = y;
            isFireEnabled = true;
            if (touchPosition.x > 0) {
                primaryBulletPointerID = thrustPointerID;
                isPrimaryBulletPointerEnabled = true;
            } else {
                secondaryBulletPointerID = rotationPointerID;
                isSecondaryBulletPointerEnabled = true;
            }
        } else {
            isPrimaryBulletPointerEnabled = isSecondaryBulletPointerEnabled = false;
            isFireEnabled = isPrimaryBulletEnabled = isSecondaryBulletEnabled = false;
        }
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

    public void update() {
        if (isHost) {
            if (isRotationEnabled) {
                inputCallBack.applyRotation(isRotationClockwise);
            }
            if (isThrustEnabled) {
                inputCallBack.applyThrust(isThrustForward);
            }
            if (isFireEnabled) {
                inputCallBack.fire(isPrimaryBulletEnabled, isSecondaryBulletEnabled);
            }
        } else if (isRotationEnabled || isThrustEnabled || isFireEnabled) {
            inputCallBack.applyControls(isThrustEnabled, isThrustForward,
                    isRotationEnabled, isRotationClockwise,
                    isFireEnabled, isPrimaryBulletEnabled, isSecondaryBulletEnabled);
        }
    }
}
