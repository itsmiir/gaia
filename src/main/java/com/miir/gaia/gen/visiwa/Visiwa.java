package com.miir.gaia.gen.visiwa;


import com.miir.gaia.Gaia;
import com.miir.gaia.gen.TensorOps;
import com.miir.gaia.gen.WorldGenerator;
import com.miir.gaia.vis.MapPrinter;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Random;

/**
 * visiwa is the module that builds the world on a macroscopic scale. it handles large scale features such as mountain
 * ranges and continents
 */
public abstract class Visiwa {
    public static final int PLATE_COUNT = 25; // approximate :)
    private static final float SUBDUCTION_RATE = 0.2f;
    public static final int DRIFT_TIME = 12;
    private static final float PLATE_SPEED = 20f;
    private static final float COASTLINE_SMOOTHNESS = 0.05f;
    private static final float LAND_OFFSET = 0.55f;
    private static final float DEPOSITION_STRENGTH = 0.1f;
    private static final double PLATE_JAGGEDNESS = WorldGenerator.ATLAS_WIDTH;
    private static final ArrayList<Point> PLATE_CENTERS = new ArrayList<>();
    private static final ArrayList<Plate> TECTONIC_PLATES = new ArrayList<>();

    /**
     * the function that creates the base shape of the world
     */
    public static void build(String path) {
        if (WorldGenerator.INITIALIZED) {
            for (int i = 0; i < WorldGenerator.MAP.length; i++) {
                for (int j = 0; j < WorldGenerator.MAP[0].length; j++) {
                    WorldGenerator.MAP[i][j] = new AtlasPoint(i, j);
                }
            }
            for (int x = 0; x < WorldGenerator.ATLAS_WIDTH; x++) {
                for (int y = 0; y < WorldGenerator.ATLAS_WIDTH; y++) {
                    float cx = ((float) x) / WorldGenerator.ATLAS_WIDTH;
                    float cy = ((float) y) / WorldGenerator.ATLAS_WIDTH;
                    float f = simplexTerrain(cx*2f, cy*2f, false);
                    f += WorldGenerator.baseHeight(cx, cy);
                    WorldGenerator.MAP[x][y].setElevation(MathHelper.clamp(f, 0, 1));
                }
            }
            createPlates();
            applyVelocity();
            for (int i = 0; i < DRIFT_TIME; i++) {
                drift();
                erode(2f, 1);
            }
            erode(2f, 2);
            postProcessOcean();
            applyMoreNoise();
        } else {
            throw new IllegalStateException("non-initialized world generator!");
        }
    }

    private static void postProcessOcean() {
        for (int x = 0; x < WorldGenerator.ATLAS_WIDTH; x++) {
            for (int y = 0; y < WorldGenerator.ATLAS_WIDTH; y++) {
                if (WorldGenerator.isValidAtlasPos(x, y)) {
                    if (WorldGenerator.MAP[x][y].getElevation() < 0.5) {
                        WorldGenerator.MAP[x][y].setElevation(0.3f + (simplexTerrain(x/(float)WorldGenerator.ATLAS_WIDTH, y/(float)WorldGenerator.ATLAS_WIDTH, false) * 0.2f));
                    }
                }
            }
        }
    }

    private static void applyMoreNoise() {
        for (int x = 0; x < WorldGenerator.ATLAS_WIDTH; x++) {
            for (int y = 0; y < WorldGenerator.ATLAS_WIDTH; y++) {
                if (WorldGenerator.isValidAtlasPos(x, y)) {
                    float f = WorldGenerator.MAP[x][y].getElevation();
                    if (f > 0.5) {
                        WorldGenerator.MAP[x][y].setElevation(-simplexTerrain(
                                x/(WorldGenerator.ATLAS_WIDTH* COASTLINE_SMOOTHNESS),
                                y/(WorldGenerator.ATLAS_WIDTH* COASTLINE_SMOOTHNESS), false)
                                * DEPOSITION_STRENGTH + f*(1));
                    }
                }
            }
        }
    }

    public static void drift() {
        AtlasPoint[][] newMap = new AtlasPoint[WorldGenerator.MAP.length][WorldGenerator.MAP[0].length];
        for (int i = 0; i < newMap.length; i++) {
            for (int j = 0; j < newMap[0].length; j++) {
                newMap[i][j] = new AtlasPoint(i, j);
            }
        }
        for (int x = 0; x < WorldGenerator.ATLAS_WIDTH; x++) {
            for (int y = 0; y < WorldGenerator.ATLAS_WIDTH; y++) {
                if (WorldGenerator.isValidAtlasPos(x, y)) { // runs for each point in the world circle
                    AtlasPoint ap = WorldGenerator.MAP[x][y];
                    ap.decrement();
                    Point p = TensorOps.toPoint(ap.getVelocity().add(new Vec2f(x, y)));
                    if (WorldGenerator.isValidAtlasPos(p)) {
                        AtlasPoint newP = newMap[p.x][p.y];
                        if (newP.getElevation() == 0) { // nothing wants to be here next frame
                            newMap[p.x][p.y] = ap.clone().setLocation(p);
                        } else { // there is already something here
                            float h1 = ap.getElevation();
                            float h2 = newP.getElevation();
                            h2 = h2 < 0.5 ? h2/2f : h2;
                            h1 = h1 < 0.5 ? h1/2f : h1;
                            // transfer the velocity of this pixel to the combined pixel
                            // yes, perfectly inelastic collision. fuck you for expecting realistic
                            // softbody sim in my fucking minecraft mod
                            Vec2f momentum = ap.getVelocity().multiply(h1);
                            newP.setVelocity(momentum.multiply(1/(h2 + h1)));
                            newP.setElevation(SUBDUCTION_RATE * Math.min(h1, h2) + Math.max(h1, h2));
                        }
                    }  // the current point goes off the end of the map
                }
            }
        }
        WorldGenerator.MAP = newMap;
    }

    private static void erode(float e, int r) {
        for (int x = 0; x < WorldGenerator.ATLAS_WIDTH; x++) {
            for (int y = 0; y < WorldGenerator.ATLAS_WIDTH; y++) {
                if (WorldGenerator.isValidAtlasPos(x, y)) {
                    float avg = 0;
                    float n = 0;
                    for (int i = -r; i <= r; i++) {
                        for (int j = -r; j <= r; j++) {
                            if (WorldGenerator.isValidAtlasPos(x+i, y+j)) {
                                n++;
                                avg += WorldGenerator.MAP[x+i][y+j].getElevation();
                            }
                        }
                    }
                    WorldGenerator.MAP[x][y].setElevation((avg + e * WorldGenerator.MAP[x][y].getElevation())/(n + e));//avg / n * (1 - Math.abs(avg - 0.5f)*0.001f));
                }
            }
        }
    }

    public static void applyVelocity() {
        for (Point point :
                PLATE_CENTERS) {
            TECTONIC_PLATES.add(new Plate(point, new Vec2f(((float) (WorldGenerator.random())-0.5f)*PLATE_SPEED, ((float) (WorldGenerator.random())-0.5f)* PLATE_SPEED)));
        }
        for (int x = 0; x < WorldGenerator.ATLAS_WIDTH; x++) {
            for (int y = 0; y < WorldGenerator.ATLAS_WIDTH; y++) {
                if (WorldGenerator.isValidAtlasPos(x, y)) {
                    float dist = Short.MAX_VALUE;
                    Plate closestPlate = null;
                    for (Plate plate :
                            TECTONIC_PLATES) {
                        if (plate.center.distance(x, y) < dist + WorldGenerator.sampleSimplex(
                                (plate.center.x + x) / ((float) WorldGenerator.ATLAS_WIDTH),
                                (plate.center.y + y) / ((float) WorldGenerator.ATLAS_WIDTH)) * PLATE_JAGGEDNESS) {
                            dist = (float) plate.center.distance(x, y);
                            closestPlate = plate;
                        }
                    }
                    assert closestPlate != null;
                    closestPlate.addPoint(new Point(x, y));
                    WorldGenerator.MAP[x][y].setVelocity(closestPlate.v);
                }
            }
        }
    }

    public static void createPlates() {
        Random random = new Random(WorldGenerator.SEED);
        for (int x = 0; x < WorldGenerator.ATLAS_WIDTH; x++) {
            for (int y = 0; y < WorldGenerator.ATLAS_WIDTH; y++) {
                if (WorldGenerator.isValidAtlasPos(x, y)) {
                    if (random.nextInt(WorldGenerator.ATLAS_AREA) < PLATE_COUNT) {
                        PLATE_CENTERS.add(new Point(x, y));
                    }
                }
            }
        }
        for (int x = 0; x < WorldGenerator.ATLAS_WIDTH; x++) {
            for (int y = 0; y < WorldGenerator.ATLAS_WIDTH; y++) {
                if (WorldGenerator.isValidAtlasPos(x, y)) {
                    Point closestPoint = null;
                    float dist = 0;
                    for (Point point : PLATE_CENTERS) {
                        if (closestPoint == null) {
                            closestPoint = point;
                            dist = ((float) Math.sqrt(Math.pow(point.x - x, 2) + Math.pow(point.y - y, 2)));
                        } else {
                            float f = ((float) Math.sqrt(Math.pow(point.x - x, 2) + Math.pow(point.y - y, 2)));
                            if (f < dist + WorldGenerator.sampleSimplex(
                                    (point.x + x) / ((float) WorldGenerator.ATLAS_WIDTH),
                                    (point.y + y) / ((float) WorldGenerator.ATLAS_WIDTH)) * PLATE_JAGGEDNESS) {
                                dist = f;
                                closestPoint = point;
                            }
                        }
                    }
                }
            }
        }
    }

    private static float simplexTerrain(float x, float y, boolean pangaea) {
        float pos = 0;
        float neg = 0;
        for (int n = 0; n < WorldGenerator.HEIGHTMAP_OCTAVES; n++) {
            pos += (WorldGenerator.sampleSimplex(
                    x * 2 * (Math.pow(2, n)),
                    y * 2 * (Math.pow(2, n)))
            ) / (4*Math.pow(2, n));
            neg += (WorldGenerator.sampleSimplex(
                    (1 - x) * (Math.pow(2, n)),
                    (1 - y) * (Math.pow(2, n)))
                    - 1) / (10 * Math.pow(2, n));
        }
        return MathHelper.clamp(((pos + neg) / 2f + LAND_OFFSET) *
                (pangaea ?
                        landDensity(Math.abs(Math.sqrt(Math.pow((x-0.5)*2, 2) + Math.pow((y-0.5)*2, 2))))
                        : 1),
                0f, 1.0f);
    }

    private static float landDensity(double r) {
        return (float) (1 - Math.pow(r, 8));
    }

    public static int colorCoastalness(Point coord) {
        float coastalness = 0;
        int r = 10;
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                if (coord.x + x >= 0 && coord.x + x < WorldGenerator.ATLAS_WIDTH) {
                    if (coord.y + y >= 0 && coord.y + y < WorldGenerator.ATLAS_WIDTH) {
                        if (WorldGenerator.isValidAtlasPos(coord.x, coord.y)) {
                            if (WorldGenerator.MAP[coord.x + x][coord.y + y].getElevation() > 0.5) {
                                coastalness += (1 / ((2f * r + 1) * (2f * r + 1)));
                            }
                        }
                    }
                }
            }
        }
        return (int) (coastalness * 255);
    }

    public static int colorElevation(Point coord) {
        float h = WorldGenerator.MAP[coord.x][coord.y].getElevation();
        return MapPrinter.lerpElevationColor(h);
    }

    public static int colorWithMarkers(Point p) {
        for (Plate plate :
                TECTONIC_PLATES) {
            if (p.equals(plate.center)) {
                return 0xFF0000;
            } else if (plate.center.distance(p.x, p.y) < 20) {
                if (plate.center.distance(p.x, p.y) < 2) {
                    return 0xFF0000;
                }
                ArrayList<Point> linePoints = new ArrayList<>();
                for (int i = 0; i < 20; i++) {
                    linePoints.add(new Point(((int) (plate.center.x + plate.v.x * i)), ((int) (plate.center.y + plate.v.y * i))));
                }
                for (Point point :
                        linePoints) {
                    if (p.equals(point)) {
                        return 0xFFFF00;
                    }
                }
            }
        }
        return colorElevation(p);
    }

    public static int blockToAtlasCoord(int x) {
        int i = x + WorldGenerator.ATLAS_WIDTH/2;
        if (0 <= i && WorldGenerator.ATLAS_WIDTH > i) {
            return i;
        } else {
            return -1;
        }
    }

    public static float scaleElevation(float elevation) {
        return elevation * WorldGenerator.MAX_PLATEAU_Y;
    }

    public static boolean writeAtlas(String path) {
        try {
            Files.createDirectories(Path.of(path, Gaia.MOD_ID));
            Files.createFile(Path.of(path, Gaia.MOD_ID, Gaia.ATLAS_PATH));
            FileOutputStream fileOutputStream = new FileOutputStream(path + "\\" +Gaia.MOD_ID +"\\"+ Gaia.ATLAS_PATH);
            ObjectOutputStream fOut = new ObjectOutputStream(fileOutputStream);
            fOut.writeObject(WorldGenerator.MAP);
        } catch (FileNotFoundException e) {
            Gaia.LOGGER.error("could not write atlas to save directory!");
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static AtlasPoint[][] readAtlas(String path) {
        try {
            FileInputStream fileInputStream = new FileInputStream(path + "\\" +Gaia.MOD_ID +"\\"+ Gaia.ATLAS_PATH);
            ObjectInputStream fIn = new ObjectInputStream(fileInputStream);
            Object object = fIn.readObject();
            if (object instanceof AtlasPoint[][]) {
                Gaia.LOGGER.info("loaded atlas...");
                return (AtlasPoint[][]) object;
            } else {
                Gaia.LOGGER.warn("atlas file at \"" + path + "\\" +Gaia.MOD_ID +"\\"+ Gaia.ATLAS_PATH + "\" was malformed!");

            }
        } catch (FileNotFoundException ignored) {
        } catch (ClassNotFoundException e) {
            Gaia.LOGGER.warn("atlas file at \"" + path + Gaia.ATLAS_PATH + "\" was malformed!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        WorldGenerator.SHOULD_GENERATE = true;
        return new AtlasPoint[WorldGenerator.ATLAS_WIDTH][WorldGenerator.ATLAS_WIDTH];
    }
}
