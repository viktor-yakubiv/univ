import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 *
 */
public class FirstWinnieProblem {

    int forestWidth = 100;
    int forestHeight = 100;

    int partsVertical = 5;
    int partsHorizontal = 5;

    int amountBees = 5;


    /* Problem model */

    Canvas forest;
    Beehive beehive;


    public FirstWinnieProblem() {}

    public void start() {
        // Log
        System.out.println("Start " + this.getClass().getName() + "...");

        // Generating.
        forest = new Canvas(10, 10);
        beehive = new Beehive(forest, 2);
        beehive.setParts(2, 2);

        // Set Winnie;
        forest.set(5, 8, 5);

        // Starting.
        beehive.start();
    }

    public void interrupt() {
        beehive.interrupt();
        System.out.println(this.getClass().getName() + " was interrupted.");
    }

    public static void main(String[] args) {
        // Execution of problem solver.
        FirstWinnieProblem problem = new FirstWinnieProblem();
        problem.start();
    }
}

/**
 * Forest
 * Concurrent canvas width synchronized methods.
 */


/**
 * Master
 */
class Beehive extends Thread {
    /* Beehive configuration */

    // Forest for checking for Winnie
    final Canvas forest;

    // Count of bees (threads) for checking forest
    int amountBees = 4;

    int partsHorizontal = 2;
    int partsVertical = 2;


    // Workers
    ThreadPoolExecutor bees;

    // Last result of forest area checking
    Boolean lastResult;

    // Index of area to processing with next task
    int currentAreaIndex = 0;


    /**
     * Creates new Beehive object defined on forest (canvas)
     *
     * @param forest canvas to checking for Winnie availability
     */
    public Beehive(Canvas forest) {
        super();

        this.forest = forest;
    }

    /**
     * Creates new Beehive object with specified bees count
     *
     * @param forest     canvas to checking for Winnie availability
     * @param amountBees count of bees in beehive, e.g count of thread to execute for checking
     */
    public Beehive(Canvas forest, int amountBees) {
        this(forest);
        this.amountBees = amountBees;
    }


    @Override
    public void run() {
        // Create thread pool and results pool.
        bees = (ThreadPoolExecutor) Executors.newFixedThreadPool(amountBees);

        // Process count of threads
        int maxBees = partsHorizontal * partsVertical;
        if (amountBees > maxBees) {
            amountBees = maxBees;
        }

        // Adding started list of tasks
        for (int i = 0; i < amountBees; ++i) {
            bees.execute(getNextTask());
        }

        // Count of completed tasks for bees
        int completed = 0;

        while (completed < partsHorizontal * partsVertical) {
            try {
                // Check result
                boolean winnieFound = getResult();
                completed++;
                System.out.println("" + completed + ". Got result: " + winnieFound);
                if (winnieFound) {
                    bees.shutdownNow();
                    break;
                }

                // Put next task
                Bee task = getNextTask();
                if (task != null) bees.execute(task);
            } catch (InterruptedException e) {
                System.out.println(this.getClass().getName() + " was interrupted.");
                break;
            }
        }
    }


    private Bee getNextTask() {
        // Get count of areas
        int amountAreas = partsHorizontal * partsVertical;

        // Inc current area index
        currentAreaIndex++;

        // Check for overflow
        if (currentAreaIndex > amountAreas) {
            return null;
        }

        // Getting indexes
        int row = currentAreaIndex / partsHorizontal;
        int col = currentAreaIndex % partsHorizontal;

        // Calculating width
        int width = forest.getWidth() / partsHorizontal;
        int height = forest.getHeight() / partsVertical;

        // Generating next area
        int offsetTop = row * height;
        int offsetLeft = col * width;
        int amountVertical = (row == partsVertical) ? forest.getHeight() - offsetTop : height;
        int amountHorizontal = (col == partsHorizontal) ? forest.getWidth() - offsetLeft : width;

        // Return worker
        return new Bee(this, forest, offsetLeft, offsetTop, amountHorizontal, amountVertical);
    }

    /**
     * Returns result from results queue.
     *
     * @return result of processing area on canvas
     * @throws InterruptedException
     */
    private synchronized boolean getResult() throws InterruptedException {
        while (lastResult == null)
            wait();
        boolean result = lastResult;
        lastResult = null;
        notify();
        return result;
    }

    /**
     * Puts a result to results queue.
     *
     * @param result a result of processing area on canvas
     * @throws InterruptedException
     */
    public synchronized void putResult(boolean result) throws InterruptedException {
        while (lastResult != null)
            wait();
        lastResult = result;
        notify();
    }

    /**
     * Sets amount of parts to divide forest.
     *
     * @param vertical   amount vertical parts to divide forest
     * @param horizontal amount horizontal parts to divide forest
     */
    public void setParts(int vertical, int horizontal) {
        this.partsVertical = vertical;
        this.partsHorizontal = horizontal;
    }
}


/**
 * Worker
 */
class Bee implements Runnable {
    /**
     * Emulates processing of founding Winnie.
     * Time for emulating.
     */
    public static int sleepTime = 10;


    /**
     * Parent object is used for getting tasks
     * and pushing results of checking.
     */
    final Beehive parent;


    // Link to field matrix
    final Canvas canvas;

    // Offsets from top and left
    int top = 0, left = 0;

    // Width and height of specified field area
    int width = 0, height = 0;


    /**
     * Construct new worker task with specified parameter to check part of field (canvas)
     *
     * @param parent           beehive where bee lives, e.g object gets results
     * @param forest           canvas (forest) to check for Winnie availability
     * @param offsetLeft       left offset of area for worker
     * @param offsetTop        top offset of area for worker
     * @param amountHorizontal width of worker's area for checking
     * @param amountVertical   height of worker's area for checking
     */
    public Bee(Beehive parent,
               Canvas forest, int offsetLeft, int offsetTop, int amountHorizontal, int amountVertical) {
        this.parent = parent;
        this.canvas = forest;
        this.left = offsetLeft;
        this.top = offsetTop;
        this.width = amountHorizontal;
        this.height = amountVertical;
    }

    /**
     * Execute worker task.
     * Checks worker area in canvas for Winnie availability.
     */
    @Override
    public void run() {
        try {
            // Result of checking
            boolean winnieFound = false;

            // Checking.
            for (int i = 0; !winnieFound && (i < height); ++i) {
                for (int j = 0; !winnieFound && (j < width); ++j) {
                    // Read table.
                    int currentValue;
                    synchronized (canvas) {
                        currentValue = canvas.get(i, j);
                    }
                    System.out.println(currentValue);

                    // Emulate processing.
                    Thread.sleep(sleepTime);

                    // Checking Winnie
                    winnieFound = currentValue > 0;
                }
            }

            // Winnie punishment
            if (winnieFound) {
                System.out.println("Well done! Winnie has been punished.");

                // Emulate punishment
                Thread.sleep(sleepTime);
            }

            // Pushing result
            parent.putResult(winnieFound);
        } catch (InterruptedException e) {
            System.out.println(getClass().getName() + " was interrupted.");
        }
    }
}
