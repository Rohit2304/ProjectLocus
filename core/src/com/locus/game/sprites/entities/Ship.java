package com.locus.game.sprites.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.locus.game.levels.Level;
import com.locus.game.sprites.bullets.Bullet;

/**
 * Created by Divya Mamgai on 9/11/2016.
 * Ship
 */
public class Ship extends Entity {

    public enum Type {

        Human,
        Alien;

        public String toString() {
            return String.valueOf(ordinal());
        }

    }

    //    static final float VELOCITY_IMPULSE = 8f;
    static final float THRUST_SPEED = 24f;
    static final Vector2 THRUST_VELOCITY = new Vector2(0, THRUST_SPEED);
    private static final float MAX_SPEED = 64f;
    private static final float MAX_SPEED2 = MAX_SPEED * MAX_SPEED;
    static final float ANGULAR_IMPULSE = 16f;

    private static final float LINEAR_DAMPING = 1f;
    private static final float ANGULAR_DAMPING = 4f;

    private Vector2 bulletPosition;
    private short bulletsFired;

    public Ship(Level level, Ship.Type type, float x, float y) {

        this.level = level;

        definition = level.entityLoader.get(Entity.Type.Ship, type.ordinal());

        setTexture(definition.texture);
        setRegion(0, 0, definition.texture.getWidth(), definition.texture.getHeight());
        setSize(definition.width, definition.height);

        body = level.world.createBody(definition.bodyDef);

        body.setTransform(x, y, 0);
        body.setLinearDamping(LINEAR_DAMPING);
        body.setAngularDamping(ANGULAR_DAMPING);
        body.setUserData(this);
        definition.attachFixture(body);

        setOrigin(definition.bodyOrigin.x, definition.bodyOrigin.y);

        update();

        bulletPosition = new Vector2(0, 0);
        bulletsFired = 0;
        health = definition.health;

    }

    void fireBullet(Bullet.Type type) {
        if (bulletsFired <= 2) {
            Vector2 bodyPosition = body.getPosition();
            float angleRad = body.getAngle();
            for (Vector2 weaponPosition : definition.weaponPositionMap.get(type)) {
                level.bulletList.add(new Bullet(level, type, this,
                        bulletPosition.set(weaponPosition).rotateRad(angleRad).add(bodyPosition),
                        angleRad));
            }
        } else if (bulletsFired >= 6) {
            bulletsFired = -1;
        }
        bulletsFired++;
    }

    @Override
    public void drawHealth(SpriteBatch spriteBatch) {
        Vector2 bodyPosition = body.getPosition();
        spriteBatch.draw(level.healthBackgroundTexture,
                bodyPosition.x - 3f, bodyPosition.y + 3f,
                6f, 0.5f);
        spriteBatch.draw(level.healthForegroundTexture,
                bodyPosition.x - 3f, bodyPosition.y + 3f,
                6f * (health / 200f), 0.5f);
    }

    @Override
    public void update() {
        Vector2 linearVelocity = body.getLinearVelocity();
        float speed2 = linearVelocity.len2();
        if (speed2 > MAX_SPEED2) {
            body.setLinearVelocity(linearVelocity.scl(MAX_SPEED2 / speed2));
        }
        Vector2 spritePosition = body.getPosition().sub(definition.bodyOrigin);
        setPosition(spritePosition.x, spritePosition.y);
        setRotation(body.getAngle() * MathUtils.radiansToDegrees);
    }

    @Override
    public boolean inFrustum(Frustum frustum) {
        Vector2 bodyPosition = body.getPosition();
        return frustum.boundsInFrustum(bodyPosition.x, bodyPosition.y, 0,
                definition.halfWidth, definition.halfHeight, 0);
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

}
