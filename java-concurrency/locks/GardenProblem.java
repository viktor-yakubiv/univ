import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Random;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GardenProblem {
    // Garden parameters
    static int gardenWidth = 10;
    static int gardenHeight = 5;

    // Garden, shared data object
    Garden garden;

    // Garden processes
    GardenProcess gardener;      // Gardener, waters the plants
    GardenProcess nature;        // Nature, randomly changes plant health points

    // Garden monitors
    GardenMonitor screenMonitor; // outputs garden to screen
    GardenMonitor fileMonitor;   // outputs garden to file


    /**
     * Constructs new garden problem
     */
    public GardenProblem() {
        // Create garden.
        garden = new Garden(gardenWidth, gardenHeight);

        // Create garden processes.
        gardener = new GardenProcess(garden, new Gardener());
        nature = new GardenProcess(garden, new Nature());

        // Create monitors.
        screenMonitor = new GardenMonitor(garden, System.out);
        try {
            fileMonitor = new GardenMonitor(garden, new PrintStream("out/garden.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    /**
     * Starts problem and all processes
     */
    public void start() {
        gardener.start();
        nature.start();
        screenMonitor.start();
        fileMonitor.start();
    }

    /**
     * Interrupts all processes and problem
     */
    public void interrupt() {
        gardener.interrupt();
        nature.interrupt();
        screenMonitor.interrupt();
        fileMonitor.interrupt();
    }


    public static void main(String[] args) {
        // Config.
        Nature.changeWeatherIteration = (int) (0.3 * gardenWidth * gardenHeight); // Change weather through 30% garden
        GardenMonitor.period = 5000; // Output after each 5s

        // Create and start problem.
        GardenProblem problem = new GardenProblem();
        problem.start();

        // One minute sleep for viewing the problem.
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Interrupt problem.
        problem.interrupt();
    }
}


/**
 * Garden of plants.
 * Is a two-dimensional array with a plant at each cell.
 *
 * Plant is represented as integer value of its health.
 * This values is configured by minValue (e.g. minimum health point)
 * and maxValue (e.g. maximum health point).
 */
class Garden {
    // Minimum health point for each plant
    static int minValue = 0;

    // Maximum health point
    static int maxValue = 100;

    // Garden plants
    int[][] data = null;

    // Mutex for safe processing
    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();


    /**
     * Constructs new garden with the specified width and height
     *
     * @param width  amount of columns in the garden
     * @param height amount of rows in the garden
     */
    public Garden(int width, int height) {
        if (width == 0 || height == 0)
            return;

        // New data matrix.
        data = new int[height][width];

        // Initial data state.
        for (int i = 0; i < height; ++i)
            for (int j = 0; j < width; ++j)
                data[i][j] = (maxValue - minValue) / 2;
    }

    /**
     * Gets width of the garden.
     *
     * @return amount of columns in the garden
     */
    public synchronized int getWidth() {
        return data.length > 0 ? data[0].length : 0;
    }

    /**
     * Gets height of the garden.
     *
     * @return amount of rows in the garden
     */
    public synchronized int getHeight() {
        return data.length;
    }

    /**
     * Gets health point of a plant with the specified coordinates
     * in a Garden.
     *
     * @param x horizontal coordinate of plant
     * @param y vertical coordinate of plant
     * @return health point of plant
     */
    public synchronized int get(int x, int y) throws InterruptedException {
        lock.readLock().lockInterruptibly();
        int value = data[y][x];
        lock.readLock().unlock();

        return value;
    }

    /**
     * Sets new health point for the plant with the specified coordinates
     * in the Garden.
     *
     * @param x     horizontal coordinate of plant
     * @param y     vertical coordinate of plant
     * @param value new health point of plant
     */
    public synchronized void set(int x, int y, int value) throws InterruptedException {
        lock.writeLock().lockInterruptibly();

        int currentValue = data[y][x];
        if (value != currentValue) {
            Thread.sleep(Math.abs(value - currentValue)); // make long changes writing
            data[y][x] = Math.max(minValue, Math.min(value, maxValue));
        }

        lock.writeLock().unlock();
    }
}


/**
 * Default garden processor for the specified Garden.
 *
 * Main interface for the other garden-specific thread.
 */
class GardenProcess extends Thread {
    // Garden where process is working
    final Garden garden;

    // Controller for each plant in garden
    final GardenPlantController controller;


    /**
     * Creates new garden process with the specified controller.
     *
     * @param garden      garden to processing, not-null pointer
     * @param controller  controller that process plants
     */
    GardenProcess(Garden garden, GardenPlantController controller) {
        this.garden = garden;
        this.controller = controller;
    }


    @Override
    public void run() {
        while (!isInterrupted()) {
            for (int y = 0; y < garden.getHeight(); ++y)
                for (int x = 0; x < garden.getWidth(); ++x) {
                    if (controller != null)
                        try {
                            garden.set(x, y, controller.processPlant(garden.get(x, y)));
                        } catch (InterruptedException e) {
                            System.err.println(controller.getClass().getName() + " was interrupted.");
                            return;
                        }
                }
        }
    }
}

interface GardenPlantController {
    /**
     * Change health point for the specified plant.
     *
     * @param healthPoint source health point of the plant
     * @return new health point for the plant
     */
    int processPlant(int healthPoint);
}


/**
 * Gardener
 * Monitors garden and waters wilted plants.
 */
class Gardener implements GardenPlantController {
    @Override
    public int processPlant(int healthPoint) {
        // Skip if health point is 0% or greater than 75%
        if (healthPoint == Garden.minValue ||
            healthPoint >= 0.75 * (Garden.maxValue - Garden.minValue) + Garden.minValue)
            return healthPoint;

        double modifier;
        // health point less then 25%
        if (healthPoint < 0.25 * (Garden.maxValue - Garden.minValue) + Garden.minValue)
            modifier = 0.5;
        // health point less then 50%
        else if (healthPoint < 0.5 * (Garden.maxValue - Garden.minValue) + Garden.minValue)
            modifier = 0.25;
        // health point is greater than 50%
        else
            modifier = 0.1;

        // Calculate and return.
        return (int) (healthPoint + modifier * (Garden.maxValue - Garden.minValue));
    }
}

/**
 * Nature
 * Monitors garden and makes plants to random health.
 */
class Nature implements GardenPlantController {
    static int changeWeatherIteration = 30;

    // Weather
    enum Weather {
        // no changes to health point
        Neutral,

        // Negative effect to health point
        Sun,

        // Positive effect to health point
        Rain
    }
    Weather currentWeather = Weather.Neutral;

    // Random for the nature methods
    Random random = new Random(System.currentTimeMillis());

    // Iteration for weather changing
    int iteration = 0;


    @Override
    public int processPlant(int healthPoint) {
        // Change weather if needed and increment iteration.
        if (iteration++ % changeWeatherIteration == 0)
            currentWeather = Weather.values()[random.nextInt(Weather.values().length)];


        // Modify and write new health point.
        int modifier = currentWeather == Weather.Neutral
                ? 0
                : random.nextInt((Garden.maxValue - Garden.minValue) / 25); // add up to 25% health point

        return healthPoint + (currentWeather == Weather.Sun ? -1 : 1) * modifier;
    }
}


/**
 * Monitor
 * Periodically prints garden state to specified PrintStream.
 */
class GardenMonitor extends GardenProcess {
    // Waiting period between outputs
    static int period = 3000;

    // Output stream
    final PrintStream out;

    /**
     * Construct new garden monitor for the specified garden and stream.
     *
     * @param garden Garden object to monitor
     * @param stream Stream for outputting
     */
    GardenMonitor(Garden garden, PrintStream stream) {
        super(garden, null);
        this.out = stream;
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                // Output.
                for (int y = 0; y < garden.getHeight(); ++y) {
                    for (int x = 0; x < garden.getWidth(); ++x) {
                        out.format("% 4d", garden.get(x, y));
                    }
                    out.println();
                }
                out.println();

                // Wait period for the next output.
                sleep(period);
            } catch (InterruptedException e) {
                System.err.println(getClass().getName() + " was interrupted.");
                return;
            }
        }
    }
}
