package com.miir.gaia.gen.vulcan;

import com.miir.gaia.gen.WorldGenerator;
import net.minecraft.util.math.Vec2f;

import java.awt.*;
import java.util.ArrayList;

public class Plate {
    public Point center;
    public Vec2f v;
    private ArrayList<Point> points = new ArrayList<>();
    private boolean[][] hasPoint;
    public Plate(Point c, Vec2f v) {
        this.v = v;
        this.center = c;
        hasPoint = new boolean[WorldGenerator.ATLAS_WIDTH][WorldGenerator.ATLAS_WIDTH];
    }

    public void setPoints(ArrayList<Point> points) {
        this.points = points;
    }

    public ArrayList<Point> getPoints() {
        return points;
    }

    public void addPoint(Point p) {
        points.add(p);
        hasPoint[p.x][p.y] = true;
    }
    public void removePoint(Point p) {
        points.remove(p);
        hasPoint[p.x][p.y] = false;
    }
    public boolean containsPoint(Point p) {
        return containsPoint(p.x, p.y);
    }
    public boolean containsPoint(int x, int y) {
        if (x < WorldGenerator.ATLAS_WIDTH && y < WorldGenerator.ATLAS_WIDTH) {
            return hasPoint[x][y];
        }
        return false;
    }
}
