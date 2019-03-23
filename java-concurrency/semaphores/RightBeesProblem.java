import java.util.ArrayList;

public class RightBeesProblem {

    // Default problem parameters

    public static int defaultBeesCount = 5;
    public static int defaultPotCapacity = 10;


    // Problem objects

    HoneyPot honeyPot;
    Bear winnie;
    Beehive beehive;


    // Constructors

    RightBeesProblem() {
        this(defaultBeesCount, defaultPotCapacity);
    }

    RightBeesProblem(int amountBees, int potCapacity) {
        honeyPot = new HoneyPot(potCapacity);
        beehive = new Beehive(amountBees, honeyPot);
        winnie = new Bear(honeyPot);
        winnie.setName("Winnie the Pooh");
    }


    // Managing methods

    public void start() {
        beehive.start();
        winnie.start();
    }

    public void interrupt() {
        beehive.interrupt();
        winnie.interrupt();
    }


    // Main procedure

    public static void main(String[] args) {
        // Set some problem parameters.
         Bee.carryLength = 1;
         Bee.carryTime = 750;
         Bear.drinkGulpLength = 1;
         Bear.drinkGulpTime = 250;
        int potCapacity = 25;
        int amountBees = 10;

        // Problem execution.
        RightBeesProblem problem = new RightBeesProblem(amountBees, potCapacity);
        problem.start();
    }
}


/**
 * Pot
 * Allows to feel or empty oneself.
 */
class Pot {

    // Pot capacity
    // Max length of the pot
    final int maxValue;

    // Current content (value) of the pot
    volatile int value = 0;

    /**
     * Creates new Pot with the specified maximal length (capacity).
     *
     * @param capacity Maximal length of the pot
     */
    Pot(int capacity) {
        this.maxValue = capacity;
    }

    /**
     * Checks is the pot full.
     *
     * @return true if pot is full
     */
    public boolean isFull() {
        return value == maxValue;
    }

    /**
     * Checks is the pot empty
     *
     * @return true if pot is empty
     */
    public boolean isEmpty() {
        return value == 0;
    }


    /**
     * Adds some content to the pot.
     *
     * @param value how mush content add to the pot
     * @return amount of content has been added
     */
    public int addValue(int value) {
        this.value += value;
        if (this.value > maxValue) {
            this.value = maxValue;
            return maxValue - this.value + value;
        }
        return value;
    }

    /**
     * Removes some content from the pot.
     *
     * @param value how much content to remove
     * @return amount of content has been removed
     */
    public int remValue(int value) {
        this.value -= value;
        if (this.value < 0) {
            this.value = 0;
            return this.value + value;
        }
        return value;
    }

    /**
     * Gets value of the pot.
     *
     * @return current pot value
     */
    public int getValue() {
        return this.value;
    }
}


/**
 * Pot of honey
 * Synchronized helper with semaphores for Bear and Bee.
 */
class HoneyPot extends Pot {

    /**
     * Creates new Pot with the specified maximal length (capacity).
     *
     * @param capacity Maximal length of the pot
     */
    HoneyPot(int capacity) {
        super(capacity);
    }


    /* Semaphores */

    // Semaphore to drink honey
    int drinkers = 0;

    // Semaphore to carry in honey
    int fillers = 0;

    /**
     * Locks drinking semaphore.
     */
    public synchronized void startDrinking() {
        while (drinkers >  0 || fillers > 0 || !isFull()) { // only one bear can drink honey per time
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        drinkers++;
    }

    /**
     * Releases drinking semaphore.
     */
    public synchronized void endDrinking() {
        drinkers--;
        if (drinkers < 0) drinkers = 0;
        notifyAll();
    }

    /**
     * Locks filling semaphore.
     */
    public synchronized void startFilling() {
        while (drinkers > 0 || isFull()) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        fillers++;
    }

    /**
     * Releases filling semaphore.
     */
    public synchronized void endFilling() {
        fillers--;
        if (fillers < 0) fillers = 0;
        notifyAll();
    }


    /* Override super methods to synchronized */

    @Override
    public synchronized boolean isFull() {
        return super.isFull();
    }

    @Override
    public synchronized boolean isEmpty() {
        return super.isEmpty();
    }

    @Override
    public synchronized int addValue(int value) {
        return super.addValue(value);
    }

    @Override
    public synchronized int remValue(int value) {
        return super.remValue(value);
    }

    @Override
    public synchronized int getValue() {
        return super.getValue();
    }
}




/**
 * Worker Bee
 * Carrying in honey into the HoneyPot
 */
class Bee extends Thread {

    // Carry parameters for Bee
    public static int carryLength = 1;
    public static int carryTime = 300;

    // Pot to carry in honey
    final HoneyPot pot;


    /**
     * Creates new Bee carrying in honey to the specified pot.
     *
     * @param honeyPot pot to carry in honey
     */
    Bee(HoneyPot honeyPot) {
        this.pot = honeyPot;
    }


    @Override
    public void run() {
        while (!isInterrupted()) {
            carryHoney();
        }
    }

    /**
     * Carry honey to the pot while it is not full.
     */
    private void carryHoney() {
        // Lock semaphore.
        pot.startFilling();

        // Fill pot.
        while (!pot.isFull()) {
            // Carry honey.
            pot.addValue(carryLength);

            // Log.
            System.out.println(getName() + " carried " + carryLength + " gulp" + (carryLength > 1 ? "s" : "") + ". " +
                "Pot contains " + pot.getValue() + " gulps of honey.");

            // Emulate work.
            try {
                sleep(carryTime);
            } catch (InterruptedException e) {
                pot.endFilling(); // Release semaphore
                e.printStackTrace();
            }
        }

        // Release semaphore.
        pot.endFilling();
    }
}


/**
 * Simple executor of lot of worker bees.
 */
class Beehive {

    // Workers in beehive
    ArrayList<Bee> bees;

    // Shared workers data
    HoneyPot pot;


    /**
     * Constructs new Beehive with the specified count of workers and shared data.
     *
     * @param capacity count of workers in Beehive
     * @param honeyPot shared data object for workers
     */
    Beehive(int capacity, HoneyPot honeyPot) {
        // Pot
        this.pot = honeyPot;

        // Generate bees.
        bees = new ArrayList<>(capacity);
        for (int i = 0; i < capacity; ++i) {
            Bee bee = new Bee(pot);
            bee.setName("Bee " + (i + 1));

            bees.add(bee);
        }
    }

    /**
     * Starts all workers inside Beehive
     */
    public void start() {
        bees.forEach(Bee::start);
    }

    /**
     * Interrupts all workers inside Beehive
     */
    public void interrupt() {
        bees.forEach(Bee::interrupt);
    }
}




/**
 * Beer. Drinks honey from an Pot.
 * When pot is not full beer is sleeping. After pot filling drink honey, wakes bees and go to sleep.
 */
class Bear extends Thread {

    // Bear drink parameters
    public static int drinkGulpLength = 1;
    public static int drinkGulpTime = 100;

    // Pot to drink honey
    final HoneyPot pot;


    /**
     * Creates new Bear drinks honey from the specified pot.
     *
     * @param honeyPot pot where bear is drinking
     */
    Bear(HoneyPot honeyPot) {
        this.pot = honeyPot;
    }


    @Override
    public void run() {
        while (!isInterrupted()) {
            drinkHoney();
        }
    }

    /**
     * Drink honey from the pot.
     */
    private void drinkHoney() {
        // Lock semaphore.
        pot.startDrinking();

        // Empty pot (drink honey).
        while (!pot.isEmpty()) {
            // Drink honey.
            pot.remValue(drinkGulpLength);

            // Log.
            System.out.println(getName() + " is drinking. Pot contains " + pot.getValue() + " gulps of honey.");

            // Emulate work.
            try {
                sleep(drinkGulpTime);
            } catch (InterruptedException e) {
                pot.endDrinking();
                e.printStackTrace();
            }
        }

        // Release semaphore.
        pot.endDrinking();
    }
}
