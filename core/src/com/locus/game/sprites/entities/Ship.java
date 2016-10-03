package com.locus.game.sprites.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.locus.game.levels.Level;
import com.locus.game.sprites.bullets.Bullet;
import com.locus.game.tools.InputController;

/**
 * Created by Divya Mamgai on 9/11/2016.
 * Ship
 */
public class Ship extends Entity implements InputController.InputCallBack {

    public enum Type {

        Fighter,
        SuperSonic,
        Bomber;

        public String toString() {
            return String.valueOf(ordinal());
        }

    }

    public static class Property {
        public Ship.Type type;
        public Color color;
    }

    private static final float LINEAR_DAMPING = 1f;
    private static final float ANGULAR_DAMPING = 4f;

    private Vector2 thrustVelocity;
    private Vector2 bulletPosition;
    private short bulletsFired;

    public Ship(Level level, Ship.Property property, float x, float y) {

        this.level = level;

        definition = level.projectLocus.entityLoader.get(Entity.Type.Ship, property.type.ordinal());

        setRegion(definition.textureRegion);
        setSize(definition.width, definition.height);
        setColor(property.color);

        body = level.world.createBody(definition.bodyDef);

        body.setTransform(x, y, 0);
        body.setLinearDamping(LINEAR_DAMPING);
        body.setAngularDamping(ANGULAR_DAMPING);
        body.setUserData(this);
        definition.attachFixture(body);

        setOrigin(definition.bodyOrigin.x, definition.bodyOrigin.y);

        update();

        thrustVelocity = new Vector2(0, 0);
        bulletPosition = new Vector2(0, 0);
        bulletsFired = 0;
        health = definition.maxHealth;

    }

    private void fireBullet(Bullet.Type type) {
        if (bulletsFired <= 2) {
            Vector2 bodyPosition = body.getPosition();
            float angleRad = body.getAngle();
            for (Vector2 weaponPosition : definition.weaponPositionMap.get(type)) {
                // Create a new Bullet at the desired position and add it to the Level.
                new Bullet(level, type, this,
                        bulletPosition.set(weaponPosition).rotateRad(angleRad).add(bodyPosition),
                        angleRad).addToLevel();
            }
        } else if (bulletsFired >= 6) {
            bulletsFired = -1;
        }
        bulletsFired++;
    }

    @Override
    public void update() {
        Vector2 linearVelocity = body.getLinearVelocity();
        float speed2 = linearVelocity.len2();
        if (speed2 > definition.maxSpeed2) {
            body.setLinearVelocity(linearVelocity.scl(definition.maxSpeed2 / speed2));
        }
        Vector2 spritePosition = body.getPosition().sub(definition.bodyOrigin);
        setPosition(spritePosition.x, spritePosition.y);
        setRotation(body.getAngle() * MathUtils.radiansToDegrees);
    }

    @Override
    public void draw(SpriteBatch spriteBatch, Frustum frustum) {

        Vector2 bodyPosition = body.getPosition();

        if (frustum.boundsInFrustum(bodyPosition.x, bodyPosition.y, 0,
                definition.halfWidth, definition.halfHeight, 0)) {

            super.draw(spriteBatch);

            float percentageHealth = health / definition.maxHealth;

            spriteBatch.draw(level.barBackgroundTexture,
                    bodyPosition.x - definition.halfWidth, bodyPosition.y + 3f,
                    definition.width, 0.5f);
            spriteBatch.draw(level.barForegroundTexture,
                    bodyPosition.x - definition.halfWidth, bodyPosition.y + 3f,
                    definition.width * percentageHealth, 0.5f);

        }

    }

    @Override
    public void kill() {
        if (isAlive) {
            isAlive = false;
            level.destroyEntityStack.push(this);
        }
    }

    @Override
    public void destroy() {
        level.world.destroyBody(body);
        level.entityList.remove(this);
    }

    @Override
    public void applyRotation(boolean isClockwise) {
        body.applyAngularImpulse((isClockwise ? -1 : 1) * definition.rotationSpeed, true);
    }

    @Override
    public void applyThrust(boolean isForward) {
        Vector2 playerPosition = body.getPosition();
        float angleRad = body.getAngle();
        if (isForward) {
            body.applyLinearImpulse(thrustVelocity.set(0, definition.thrustSpeed)
                    .rotateRad(angleRad), playerPosition, true);
        } else {
            body.applyLinearImpulse(thrustVelocity.set(0, definition.thrustSpeed)
                    .rotateRad(angleRad).scl(-1f), playerPosition, true);
        }
    }

    @Override
    public void fire() {
        fireBullet(Bullet.Type.Normal);
    }

}
