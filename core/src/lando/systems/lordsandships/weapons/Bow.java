package lando.systems.lordsandships.weapons;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.equations.Cubic;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import lando.systems.lordsandships.GameInstance;
import lando.systems.lordsandships.entities.Bullet;
import lando.systems.lordsandships.tweens.ColorAccessor;
import lando.systems.lordsandships.utils.Assets;

/**
 * Brian Ploeckelman created on 7/22/2014.
 */
public class Bow extends Weapon {

    public static final String bow_type     = "Bow";
    public static final float  bow_duration = 0.6f;
    public static final float attack_cooldown = bow_duration;

    public float accum;

    public Array<Bullet> bullets;
    public Array<Bullet> bulletsToRemove;

    public float attackCooldown = 0;
    final int max_bullets = 100;

    /**
     * Constructor
     *
     * @param builder The Weapon.Builder to create this object with
     */
    public Bow(Builder builder) {
        super(builder);
        setType(bow_type);

        int total_frames = 16;
        int num_frames = 8;
        TextureRegion keyframes[] = new TextureRegion[total_frames];
        for (int i = 0; i < total_frames; ++i) {
            keyframes[i] = new TextureRegion(Assets.raphAtlas.findRegion("sBow", i % num_frames));
        }
        animation = new Animation(bow_duration / total_frames, keyframes);
        animation.setPlayMode(Animation.PlayMode.NORMAL);
        direction = new Vector2();
        color.a = 0;

        float w = animation.getKeyFrames()[0].getRegionWidth();
        float h = animation.getKeyFrames()[0].getRegionHeight();
        bounds.set(0, 0, (w + h) / 4);

        bullets = new Array<Bullet>(max_bullets);
        bulletsToRemove = new Array<Bullet>(max_bullets);
    }

    /**
     * Attack in the specified direction
     *
     * @param dir The direction in which to use the weapon
     */
    @Override
    public void attack(Vector2 origin, Vector2 dir) {
        if (attacking) return;
        attacking = true;

        color.a = 1;
        direction.set(dir);
        angle = MathUtils.radiansToDegrees * (float) Math.atan2(direction.y, direction.x);

        Assets.bow_shot1.play(1.0f);

        Tween.to(color, ColorAccessor.A, bow_duration)
                .target(0)
                .ease(Cubic.INOUT)
                .setCallback(new TweenCallback() {
                    @Override
                    public void onEvent(int type, BaseTween<?> source) {
                        attacking = false;
                    }
                })
                .start(GameInstance.tweens);

        accum = 0f;

        if ((bullets.size - 1) < max_bullets) {
            bullets.add(new Bullet(
                    origin.x + 8 - animation.getKeyFrame(accum).getRegionWidth()  / 2f,
                    origin.y + 8 - animation.getKeyFrame(accum).getRegionHeight() / 2f,
                    direction.x * Bullet.BULLET_SPEED,
                    direction.y * Bullet.BULLET_SPEED));

            attackCooldown = attack_cooldown;
        }
    }

    /**
     * Render the weapon effect
     *
     * @param batch The SpriteBatch to draw with
     * @param originX The origin point to draw from, x coordinate
     * @param originY The origin point to draw from, y coordinate
     */
    @Override
    public void render(SpriteBatch batch, float originX, float originY) {
        if (!attacking) return;

        accum += Gdx.graphics.getDeltaTime();
        TextureRegion keyframe = animation.getKeyFrame(accum);

        // Size and half size
        float w = keyframe.getRegionWidth();
        float h = keyframe.getRegionHeight();
        float hw = w / 2f;
        float hh = h / 2f;

        // Offset and position
        float ox = direction.x * hw * 0.15f;
        float oy = direction.y * hh * 0.15f;
        float px = originX - hw + ox;
        float py = originY - hh + oy;

        // Scale
        float sx = 0.5f;
        float sy = 0.5f;

        bounds.set(originX + ox, originY + oy, (w * sx + h * sy) / 4.75f);

        batch.setColor(color);
        batch.draw(keyframe, px, py, hw, hh, w, h, sx, sy, angle);
        batch.setColor(Color.WHITE);

        if (debug) {
            batch.end();
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA,
                               GL20.GL_ONE_MINUS_SRC_ALPHA);
            Assets.shapes.setColor(1, 1, 0, 1);
            Assets.shapes.begin(ShapeRenderer.ShapeType.Line);
            Assets.shapes.circle(bounds.x, bounds.y, bounds.radius);
            Assets.shapes.end();

            Assets.shapes.setColor(1, 0, 1, 0.2f);
            Assets.shapes.begin(ShapeRenderer.ShapeType.Filled);
            Assets.shapes.circle(bounds.x, bounds.y, bounds.radius);
            Assets.shapes.end();
            batch.begin();
        }
    }

    @Override
    public void update(float delta) {
        // Update bullets
        bulletsToRemove.clear();
        for (Bullet bullet : bullets) {
            if (bullet.isAlive()) bullet.update(delta);
            else                  bulletsToRemove.add(bullet);
        }
        bullets.removeAll(bulletsToRemove, true);

        if (attacking && (attackCooldown -= delta) < 0f) {
            attacking = false;
        }
    }

    @Override
    public boolean collides(Circle otherBounds) {
        return false; //attacking && Intersector.overlaps(bounds, otherBounds);
    }
}