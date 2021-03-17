package com.dfsek.noise.swing;

import com.dfsek.noise.config.ColorConfigTemplate;
import com.dfsek.noise.config.NoiseConfigTemplate;
import com.dfsek.tectonic.exception.ConfigException;
import com.dfsek.tectonic.loading.ConfigLoader;
import com.dfsek.terra.api.math.noise.NoiseSampler;
import com.dfsek.terra.api.util.collections.ProbabilityCollection;
import com.dfsek.terra.api.util.mutable.MutableBoolean;
import com.dfsek.terra.api.util.seeded.NoiseSeeded;
import com.dfsek.terra.config.GenericLoaders;
import com.dfsek.terra.config.fileloaders.FolderLoader;
import com.dfsek.terra.config.loaders.ProbabilityCollectionLoader;
import com.dfsek.terra.config.loaders.config.BufferedImageLoader;
import com.dfsek.terra.config.loaders.config.sampler.NoiseSamplerBuilderLoader;
import com.dfsek.terra.registry.config.NoiseRegistry;
import net.jafama.FastMath;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Paths;

public class NoisePanel extends JPanel {
    private final RSyntaxTextArea textArea;
    private final JLabel image;
    private final JTextArea statisticsPanel;
    private final NoiseDistributionPanel distributionPanel;
    private MutableBoolean chunk = new MutableBoolean();

    public NoisePanel(RSyntaxTextArea textArea, JTextArea statisticsPanel, NoiseDistributionPanel distributionPanel) {
        this.textArea = textArea;
        this.statisticsPanel = statisticsPanel;
        this.distributionPanel = distributionPanel;
        this.image = new JLabel();
        add(image);
    }

    public void update() {
        try {
            image.setIcon(new ImageIcon(getImage(2403)));
        } catch (Exception e) {
            e.printStackTrace();
            image.setIcon(new TextIcon(this, "An error occurred. "));
            statisticsPanel.setText("An error occurred.");
            distributionPanel.error();
        }
    }

    public MutableBoolean getChunk() {
        return chunk;
    }

    private BufferedImage getImage(long seed) throws ConfigException, FileNotFoundException {

        System.out.println("Rendering noise with seed " + seed);

        FolderLoader folderLoader = new FolderLoader(Paths.get("./"));

        ConfigLoader loader = new ConfigLoader();
        loader.registerLoader(NoiseSeeded.class, new NoiseSamplerBuilderLoader(new NoiseRegistry()))
                .registerLoader(BufferedImage.class, new BufferedImageLoader(folderLoader))
                .registerLoader(ProbabilityCollection.class, new ProbabilityCollectionLoader());

        new GenericLoaders(null).register(loader);
        NoiseConfigTemplate template = new NoiseConfigTemplate();

        File colorFile = new File("./color.yml");


        boolean colors = false;
        ColorConfigTemplate color = new ColorConfigTemplate();
        if(colorFile.exists()) {
            loader.load(color, new FileInputStream(colorFile));
            colors = color.enable();
        }
        ProbabilityCollection<Integer> colorCollection = color.getColors();

        loader.load(template, textArea.getText());
        System.out.println(template.getBuilder().getDimensions() + " Dimensions.");
        NoiseSampler noise = template.getBuilder().apply((long) seed);


        int sizeX = getWidth();
        int sizeY = getHeight();

        BufferedImage image = new BufferedImage(sizeX, sizeY, BufferedImage.TYPE_INT_ARGB);

        double[][] noiseVals = new double[sizeX][sizeY];
        int[][] rgbVals = new int[sizeX][sizeY];
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;

        int[] buckets = new int[sizeX];

        long s = System.nanoTime();

        for(int x = 0; x < noiseVals.length; x++) {
            for(int z = 0; z < noiseVals[x].length; z++) {
                double n = noise.getNoise(x, z);
                noiseVals[x][z] = n;
                max = Math.max(n, max);
                min = Math.min(n, min);
                if(colors) rgbVals[x][z] = colorCollection.get(noise, x, z);
            }
        }

        long time = System.nanoTime() - s;

        double ms = time / 1000000d;

        for(int x = 0; x < noiseVals.length; x++) {
            for(int z = 0; z < noiseVals[x].length; z++) {
                if(colors) image.setRGB(x, z, rgbVals[x][z] + (255 << 24));
                else image.setRGB(x, z, buildRGBA(normal(noiseVals[x][z], 255, min, max)));
                buckets[normal(noiseVals[x][z], sizeX - 1, min, max)]++;
            }
        }

        if(chunk.get()) {
            for(int x = 0; x < FastMath.floorDiv(image.getWidth(), 16); x++) {
                for(int y = 0; y < image.getHeight(); y++) image.setRGB(x*16, y, buildRGBA(0));
            }
            for(int y = 0; y < FastMath.floorDiv(image.getHeight(), 16); y++) {
                for(int x = 0; x < image.getWidth(); x++) image.setRGB(x, y*16, buildRGBA(0));
            }
        }

        statisticsPanel.setText("min: " + min + "\nmax: " + max + "\nseed: " + seed + "\ntime: " + ms + "ms");
        distributionPanel.update(buckets);

        System.out.println("Rendered " + sizeX*sizeY + " points in " + ms + "ms.");

        return image;
    }

    private static int normal(double in, double out, double min, double max) {
        double range = max - min;
        return (int) (((in - min) * out) / range);
    }

    private static int buildRGBA(int in) {
        return (255 << 24)
                + (in << 16)
                + (in << 8)
                + in;
    }
}
