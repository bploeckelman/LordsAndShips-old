package lando.systems.lordsandships.entities.enemies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import lando.systems.lordsandships.entities.Enemy;
import lando.systems.lordsandships.utils.Assets;

/**
 * Brian Ploeckelman created on 3/29/2015.
 */
public class Bat extends Enemy {

    final float max_vel_x = 50;
    final float max_vel_y = 50;

    float         moveTime;
    float         stateTime;
    Animation     anim;
    TextureRegion keyframe;

    // TODO: most of this shit is set in the ctor and doesn't pass along to Enemy or Entity anymore, refactor all the things!
    public Bat(Texture texture, float x, float y, float w, float h, float animRate) {
        super(texture, x, y, w, h, animRate);

        final String name = "bat";
        final int num_keyframes = 6;
        final TextureRegion[] keyframes = new TextureRegion[num_keyframes];
        for (int i = 0; i < num_keyframes; ++i) {
            keyframes[i] = Assets.creatures.findRegion(name, i+1);
        }

        anim = new Animation(animRate, keyframes);
        anim.setPlayMode(Animation.PlayMode.LOOP);
        keyframe = anim.getKeyFrame(0);

        boundingBox.set(x, y, keyframes[0].getRegionWidth(), keyframes[0].getRegionHeight());
    }

    @Override
    public void update(float delta) {
        // Update animation
        stateTime += delta;
        keyframe = anim.getKeyFrame(stateTime);

        // Update movement
        moveTime += delta;
        if (moveTime > (Math.random() * 5.f + 2)) {
            moveTime = 0f;
            switch ((int) (Math.random() * 3.f)) {
                case 0:
                    velocity.x = ((float) Math.random() * 2f - 1f) * max_vel_x;
                    velocity.y = 0f;
                    break;
                case 1:
                    velocity.x = 0f;
                    velocity.y = ((float) Math.random() * 2f -1f) * max_vel_y;
                    break;
                case 2:
                    velocity.x = 0f;
                    velocity.y = 0f;
            }
        }

        if      (velocity.x >  max_vel_x) velocity.x =  max_vel_x;
        else if (velocity.x < -max_vel_x) velocity.x = -max_vel_x;
        if      (velocity.y >  max_vel_y) velocity.y =  max_vel_y;
        else if (velocity.y < -max_vel_y) velocity.y = -max_vel_y;

        // Apply movement
        // TODO: remove some of this redundancy
        boundingBox.x += velocity.x * delta;
        boundingBox.y += velocity.y * delta;

        position.set(boundingBox.x + boundingBox.width / 2f,
                     boundingBox.y + boundingBox.height / 2f);

        collisionBounds.set(boundingBox.x + boundingBox.width / 2f,
                            boundingBox.y + boundingBox.height / 2f,
                            (boundingBox.width + boundingBox.height) / 4f - 3f);
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.draw(Assets.shadow, boundingBox.x, boundingBox.y - 2);
        batch.setColor(color);
        batch.draw(keyframe, boundingBox.x, boundingBox.y, boundingBox.width, boundingBox.height);
        batch.setColor(1, 1, 1, 1);

        batch.end();
        Assets.shapes.begin(ShapeRenderer.ShapeType.Filled);
        Assets.shapes.setColor(Color.RED);
        Assets.shapes.circle(position.x, position.y, 1);
        Assets.shapes.setColor(Color.MAGENTA);
        Assets.shapes.circle(boundingBox.x, boundingBox.y, 2);
        Assets.shapes.end();

        Assets.shapes.begin(ShapeRenderer.ShapeType.Line);
        Assets.shapes.setColor(Color.YELLOW);
        Assets.shapes.circle(collisionBounds.x, collisionBounds.y, collisionBounds.radius);
        Assets.shapes.setColor(Color.ORANGE);
        Assets.renderRect(boundingBox);
        Assets.shapes.end();
        batch.begin();
    }

}