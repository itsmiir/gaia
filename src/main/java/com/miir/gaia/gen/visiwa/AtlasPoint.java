package com.miir.gaia.gen.visiwa;

import net.minecraft.util.math.Vec2f;

import java.awt.*;
import java.io.Serializable;

public class AtlasPoint implements Cloneable, Serializable {

    public AtlasPoint(int x, int y) {
        this.elevation = 0;
        this.location = new Point(x, y);
    }
    public AtlasPoint(float elevation, int x, int y) {
        this(x, y);
        this.elevation = elevation;
    }

    public float getHumidity() {
        return humidity;
    }
    public void setHumidity(float humidity) {
        this.humidity = humidity;
    }
    public float getTemperature() {
        return temperature;
    }
    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    private float elevation;
    private float humidity;
    private float temperature;
    private transient Plate plate;
    private transient int n = Visiwa.DRIFT_TIME;
    private transient Vec2f velocity;
    private transient Point location;

    public Plate getPlate() {
        return plate;
    }
    public AtlasPoint setPlate(Plate plate) {
        this.plate = plate;
        return this;
    }
    public Vec2f getVelocity() {

        return velocity == null ? new Vec2f(0, 0) : velocity.multiply(n / ((float) Visiwa.DRIFT_TIME));
    }
    public AtlasPoint setVelocity(Vec2f velocity) {
        this.velocity = velocity;
        if (this.plate != null) {
            int n = this.plate.getPoints().size();
            this.plate.v.multiply((n-1f)/n);
        }
        return this;
    }

    public Point getLocation() {
        return location;
    }
    public AtlasPoint setLocation(Point location) {
        this.location = location;
        return this;
    }
    public float getElevation() {
        return elevation;
    }
    public AtlasPoint setElevation(float elevation) {
        this.elevation = elevation;
        return this;
    }

    public Point findCollisionDirection() {
        int x = velocity.x >= 0.5 ? 1 : velocity.x >= -0.5 ? 0 : 1;
        int y = velocity.y >= 0.5 ? 1 : velocity.y >= -0.5 ? 0 : 1;
        return new Point(x+this.location.x, y+this.location.y);
    }
    public AtlasPoint decrement() {
        this.n--;
        return this;
    }

    @Override
    public AtlasPoint clone() {
        try {
            return (AtlasPoint) super.clone();
        } catch (CloneNotSupportedException e) {
            AtlasPoint clone = new AtlasPoint(this.elevation, this.location.x, this.location.y);
            clone.plate = this.plate;
            clone.n = this.n;
            clone.velocity = this.velocity;
            return clone;
        }
    }
}
