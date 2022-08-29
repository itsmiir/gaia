package com.miir.gaia.gen;

import com.miir.gaia.gen.vulcan.Vulcan;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.function.Function;

public abstract class TensorOps {
    public static float[][] gradient(float[][] arr) {
        float[][] gradient = new float[arr.length][arr[0].length];
        for (int x = 0; x < arr.length; x++) {
            for (int y = 0; y < arr[x].length; y++) {
                ArrayList<Float> neighbors = new ArrayList<>();
                gradient[x][y] = ((float) slope(arr, x, y, WorldGenerator::isValidAtlasPos));
            }
        }
        return gradient;
    }

    protected static double slope(float[][] arr, int x, int y, Function<Point, Boolean> isValid) {
        double gxl = Double.MAX_VALUE;
        double gxr = Double.MAX_VALUE;
        double gyl = Double.MAX_VALUE;
        double gyr = Double.MAX_VALUE;
        if (isValid.apply(new Point(x-1, y))) gxl = arr[x][y] - arr[x-1][y];
        if (isValid.apply(new Point(x+1, y))) gxr = arr[x+1][y] - arr[x][y];
        if (isValid.apply(new Point(x, y-1))) gyl = arr[x][y] - arr[x][y-1];
        if (isValid.apply(new Point(x, y+1))) gyr = arr[x][y+1] - arr[x][y];
        double gx = gxl == Double.MAX_VALUE ? gxr : gxr == Double.MAX_VALUE ? gxl : mean(gxl, gxr);
        double gy = gyl == Double.MAX_VALUE ? gyr : gyr == Double.MAX_VALUE ? gyl : mean(gyl, gyr);
        return Math.sqrt(Math.pow(gx, 2) + Math.pow(gy, 2));
    }

    private static double mean(double a, double b) {
        return (a + b) / 2d;
    }
    public static boolean isTrough(float[][] arr, int x, int y, int r, float offset, float multiplier, Function<Point, Boolean> isValid) {
        if (isValid.apply(new Point(x, y))) {
            float sum = 0;
            int n = 0;
            for (int i = -r; i <= r; i++) {
                for (int j = -r; j <= r; j++) {
                    if (isValid.apply(new Point(x + i, y + j))) {
                        sum += arr[x + i][y + j];
                        n++;
                    }
                }
            }
            return arr[x][y] + offset < ((sum / n) * 2 * multiplier);
        } else return false;
    }

    public static Point toPoint(Vec2f vec2f) {
        return new Point(Math.round(vec2f.x), Math.round(vec2f.y));
    }
}
