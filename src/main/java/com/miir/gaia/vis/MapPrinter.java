package com.miir.gaia.vis;

import com.miir.gaia.Gaia;
import com.miir.gaia.gen.WorldGenerator;
import com.miir.gaia.gen.visiwa.Visiwa;
import net.minecraft.util.math.MathHelper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.function.Function;

public abstract class MapPrinter {
    public static void toPng(float[][] map, String path) {

    }

    public static int clean(double x) {
        return ((int) MathHelper.clamp(x, 0, 255));
    }
    public static int grayscale(Point p) {
        float h = Visiwa.MAP[p.x][p.y].getValue();
        int c = (int)(h * 255);
        return c << 16 | c << 8 | c;
    }
    public static int grayscaleCoastline(Point p) {
        float h = Visiwa.MAP[p.x][p.y].getValue();
        if (h < 0.51 & h > 0.49) {
            return 0xafa96e;
        } else {
            int c = (int)(h * 255);
            return c << 16 | c << 8 | c;
        }
    }
    public static int grayscaleWithMarkers(Point p) {
        float h = Visiwa.MAP[p.x][p.y].getValue();
        if (h < 0.51 & h > 0.49) {
            return 0xafa96e; // coastline
        } else if (h < 0.34 & h > 0.32) {
            return 0xFF5500; // TODO
        } else {
        int c = (int)(h * 255);
            return c << 16 | c << 8 | c;
        }
    }

    public static void printAtlas(String path, Function<Point, Integer> colorFunction) {
        BufferedImage img = new BufferedImage(WorldGenerator.ATLAS_WIDTH, WorldGenerator.ATLAS_WIDTH, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < WorldGenerator.ATLAS_WIDTH; y++) {
            for (int x = 0; x < WorldGenerator.ATLAS_WIDTH; x++) {
                int c = colorFunction.apply(new Point(x, y));
                img.setRGB(x, y, c);
            }
        }
        try {
            ImageIO.write(img, "png", new File(path + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
            Gaia.LOGGER.error("could not print a map!");
        }
        Gaia.LOGGER.info("logged map to "+path+".png");
    }

    public static int lerpElevationColor(float h) {
        int color0 = 0x190c62;
        int color1 = 0x112B70;
        int color2 = 0x262976;
        int color3 = 0x2c4173;
        int color4 = 0x2c5273;

        int color5 = 0x2c7332;
        int color6 = 0x87772d;
        int color7 = 0x875e2d;
        int color8 = 0x9e5723;
        int color9 = 0xd3d3d3;

        int r, g, b;
        if (h == 0) {
            return 0x000000;
        }
        if (h < 0.125) {
            h /= .125;
            r = clean((color0 >> 16 & 0xFF)*(1- h) +   (color1 >> 16 & 0xFF)* h);
            g = clean((color0 >> 8 & 0xFF)*(1- h) +    (color1 >> 8 & 0xFF)* h);
            b = clean((color0 & 0xFF)*(1- h) +         (color1 & 0xFF)* h);
        } else if (h <= .25) {
            h -= .125;
            h /= .125;
            r = clean((color1 >> 16 & 0xFF) * (1- h) + (color2 >> 16 & 0xFF) * h);
            g = clean((color1 >> 8 & 0xFF) * (1- h) +  (color2 >> 8 & 0xFF) * h);
            b = clean((color1 & 0xFF) * (1- h) +       (color2 & 0xFF) * h);
        } else if (h <= 0.375) {
            h -= .25;
            h /= .125;
            r = clean((color2 >> 16 & 0xFF)*(1- h) +  (color3 >> 16 & 0xFF)* h);
            g = clean((color2 >> 8 & 0xFF)*(1- h) +   (color3 >> 8 & 0xFF)* h);
            b = clean((color2 & 0xFF)*(1- h) +        (color3 & 0xFF)* h);
        } else if (h <= 0.5) {
            h -= .375;
            h /= .125;
            r = clean((color3 >> 16 & 0xFF)*(1- h) +  (color4 >> 16 & 0xFF)* h);
            g = clean((color3 >> 8 & 0xFF)*(1- h) +   (color4 >> 8 & 0xFF)* h);
            b = clean((color3 & 0xFF)*(1- h) +        (color4 & 0xFF)* h);
        } else if (h <= 0.625) {
            h -= .5;
            h /= .125;
            r = clean((color5 >> 16 & 0xFF)*(1- h) +  (color6 >> 16 & 0xFF)* h);
            g = clean((color5 >> 8 & 0xFF)*(1- h) +   (color6 >> 8 & 0xFF)* h);
            b = clean((color5 & 0xFF)*(1- h) +        (color6 & 0xFF)* h);
        } else if (h <= 0.75) {
            h -= .625;
            h /= .125;
            r = clean((color6 >> 16 & 0xFF)*(1- h) +  (color7 >> 16 & 0xFF)* h);
            g = clean((color6 >> 8 & 0xFF)*(1- h) +   (color7 >> 8 & 0xFF)* h);
            b = clean((color6 & 0xFF)*(1- h) +        (color7 & 0xFF)* h);
        } else if (h <= 0.875) {
            h -= .75;
            h /= .125;
            r = clean((color7 >> 16 & 0xFF)*(1- h) +  (color8 >> 16 & 0xFF)* h);
            g = clean((color7 >> 8 & 0xFF)*(1- h) +   (color8 >> 8 & 0xFF)* h);
            b = clean((color7 & 0xFF)*(1- h) +        (color8 & 0xFF)* h);
        } else {
            h -= .875;
            h /= .125;
            r = clean((color8 >> 16 & 0xFF)*(1- h) +  (color9 >> 16 & 0xFF)* h);
            g = clean((color8 >> 8 & 0xFF)*(1- h) +   (color9 >> 8 & 0xFF)* h);
            b = clean((color8 & 0xFF)*(1- h) +        (color9 & 0xFF)* h);
        }
        return r << 16 | g << 8 | b;
    }
}
