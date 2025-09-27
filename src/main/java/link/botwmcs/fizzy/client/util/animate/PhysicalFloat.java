package link.botwmcs.fizzy.client.util.animate;

import net.minecraft.util.Mth;

import java.util.ArrayList;

public class PhysicalFloat {
    float previousValue;
    float value;

    float previousSpeed;
    float speed;
    float limit = Float.NaN;

    float mass;

    private final ArrayList<Force> forces = new ArrayList<>();

    public static PhysicalFloat create() {
        return new PhysicalFloat(1);
    }

    public static PhysicalFloat create(float mass) {
        return new PhysicalFloat(mass);
    }

    public PhysicalFloat(float mass) {
        this.mass = mass;
    }

    public PhysicalFloat startAt(double value) {
        previousValue = this.value = (float) value;
        return this;
    }

    public PhysicalFloat withDrag(double drag) {
        return addForce(new Force.Drag((float) drag));
    }

    public PhysicalFloat zeroing(double g) {
        return addForce(new Force.Zeroing((float) g));
    }

    public PhysicalFloat withLimit(float limit) {
        this.limit = limit;
        return this;
    }

    public void tick() {
        previousSpeed = speed;
        previousValue = value;

        float totalImpulse = 0;
        for (Force force : forces)
            totalImpulse += force.get(mass, value, speed) / mass;

        speed += totalImpulse;

        forces.removeIf(Force::finished);

        if (Float.isFinite(limit)) {
            speed = Mth.clamp(speed, -limit, limit);
        }

        value += speed;
    }

    public PhysicalFloat addForce(Force f) {
        forces.add(f);
        return this;
    }

    public PhysicalFloat bump(double force) {
        return addForce(new Force.Impulse((float) force));
    }

    public PhysicalFloat bump(int time, double force) {
        return addForce(new Force.OverTime(time, (float) force));
    }

    public float getValue() {
        return getValue(1);
    }

    public float getValue(float partialTicks) {
        return Mth.lerp(partialTicks, previousValue, value);
    }

    public interface Force {

        float get(float mass, float value, float speed);

        boolean finished();

        class Drag implements Force {
            final float dragFactor;

            public Drag(float dragFactor) {
                this.dragFactor = dragFactor;
            }

            @Override
            public float get(float mass, float value, float speed) {
                return -speed * dragFactor;
            }

            @Override
            public boolean finished() {
                return false;
            }
        }

        class Zeroing implements Force {
            final float g;

            public Zeroing(float g) {
                this.g = g / 20;
            }

            @Override
            public float get(float mass, float value, float speed) {
                return -Math.signum(value) * g * mass;
            }

            @Override
            public boolean finished() {
                return false;
            }
        }

        class Impulse implements Force {

            float force;

            public Impulse(float force) {
                this.force = force;
            }

            @Override
            public float get(float mass, float value, float speed) {
                return force;
            }

            @Override
            public boolean finished() {
                return true;
            }
        }

        class OverTime implements Force {
            int timeRemaining;
            float f;

            public OverTime(int time, float totalAcceleration) {
                this.timeRemaining = time;
                this.f = totalAcceleration / (float) time;
            }

            @Override
            public float get(float mass, float value, float speed) {
                timeRemaining--;
                return f;
            }

            @Override
            public boolean finished() {
                return timeRemaining <= 0;
            }
        }

        class Static implements Force {
            float force;

            public Static(float force) {
                this.force = force;
            }

            @Override
            public float get(float mass, float value, float speed) {
                return force;
            }

            @Override
            public boolean finished() {
                return false;
            }
        }
    }


}
