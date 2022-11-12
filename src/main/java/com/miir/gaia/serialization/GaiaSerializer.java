package com.miir.gaia.serialization;

import com.miir.gaia.Gaia;
import com.miir.gaia.gen.WorldGenerator;
import com.miir.gaia.gen.visiwa.AtlasPoint;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class GaiaSerializer {
    // atlas serialization methods so that visiwa does not have to rebuild the continents on every world load
    public static boolean writeAtlas(String path) {
        try {
            Files.createDirectories(Path.of(path, Gaia.MOD_ID));
            Files.createFile(Path.of(path, Gaia.MOD_ID, Gaia.ATLAS_PATH));
            FileOutputStream fileOutputStream = new FileOutputStream(path + "\\" +Gaia.MOD_ID +"\\"+ Gaia.ATLAS_PATH);
            ObjectOutputStream fOut = new ObjectOutputStream(fileOutputStream);
            fOut.writeObject(WorldGenerator.MAP);
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
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
