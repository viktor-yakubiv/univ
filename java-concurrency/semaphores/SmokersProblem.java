import java.util.Random;
import java.util.concurrent.Semaphore;

public class SmokersProblem extends Thread {

    Smoker[] smokers;
    Dealer dealer;

    public SmokersProblem() {
        dealer = new Dealer();
        smokers = new Smoker[3];
        smokers[0] = new Smoker(dealer, CigElement.Tobacco);
        smokers[1] = new Smoker(dealer, CigElement.Paper);
        smokers[2] = new Smoker(dealer, CigElement.Match);
    }

    @Override
    public synchronized void start() {
        // Start threads.
        dealer.start();
        for (Smoker smoker: smokers) smoker.start();

        // Join threads to this.
        try {
            dealer.join();
            for (Smoker smoker: smokers)
                smoker.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void interrupt() {
        dealer.interrupt();
        for (Smoker smoker: smokers) smoker.interrupt();
    }

    public static void main(String[] args) {
        SmokersProblem problem = new SmokersProblem();
        problem.start();
        try {
            problem.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

/**
 * Elements of the cigarette.
 */
enum CigElement {
    Tobacco,
    Paper,
    Match
}


/**
 * Smoker
 *
 * Get cigarette elements and combine its with the own element.
 * After that smokes the cigarette.
 */
class Smoker extends Thread {
    // Smoker smokes one cigarette for this time
    static int smokeTime = 1000;

    // Dealer who gives elements to make cigarette
    final Dealer dealer;

    // The element of cigarette that owns this smoker
    final CigElement element;


    /**
     * Constructs new smoker with the specified element owning.
     *
     * @param ownsElement an element that this smoker owns
     */
    public Smoker(Dealer dealer, CigElement ownsElement) {
        this.dealer = dealer;
        this.element = ownsElement;
        this.setName(element.toString() + " Owner");
    }


    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                if (dealer.getElements(this.element)) {
                    // Log.
                    System.out.println(getName() + " is smoking...");

                    // Emulate smoking.
                    try {
                        sleep(smokeTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // Call back for dealer.
                    dealer.releaseElements();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}


/**
 * Dealer
 *
 * Generates new cigarette elements and notify smokers about
 *
 */
class Dealer extends Thread {
    // Dealer generates new cigarette elements for this time
    static int generatingTime = 500;


    // Elements on the table without this
    CigElement elementNotInSet;

    // Random for generating new elements
    Random random = new Random();


    // Smokers locker
    Semaphore elements = new Semaphore(1);

    // Flags for generating new elements
    Boolean smoking = false;
    Boolean needToRegenerate = true;



    public Dealer() {
        setName(getClass().getName());
    }


    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                generateElements();
                yield();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void generateElements() throws InterruptedException {
        /**
         * This block can be used without sleep().
         * But in this way we need to use wait-notify methods
         * to avoid a lot of processor operations.
            // Allow other to get cigarette elements.
            if (!needToRegenerate || !smoking) {
                yield();
                return;
            }
        */

        // Wait for smokers.
        while (!needToRegenerate)
            wait();

        // Generate new elements.
        elements.acquire();
        elementNotInSet = CigElement.values()[random.nextInt(CigElement.values().length)];
        sleep(generatingTime);
        elements.release();

        // Switch state.
        needToRegenerate = false;

        // Log.
        System.out.println(getName() + " generated pair without " + elementNotInSet.toString());

        // Notify smokers.
        notifyAll();
    }


    public synchronized boolean getElements(CigElement lastInSet) throws InterruptedException {
        // Wait for end smoking.
        while (smoking || needToRegenerate)
            wait();

        // Calculate result of getting.
        boolean allow = lastInSet == elementNotInSet;

        // Allow somebody to lock elements and get it.
        // If somebody locked elements and smoking wait for.
        if (allow) {
            elements.acquire();
            smoking = true;
        // Does not work.
        // } else if (smoking) {
        //     elements.acquire();
        } else {
            yield(); // Allow others to get elements
        }

        // Return result.
        return allow;
    }


    public synchronized void releaseElements() throws InterruptedException {
        // Make dealer to generate new elements.
        needToRegenerate = true;

        // Inform others about end of smoking.
        smoking = false;

        // Release elements and unlock others.
        elements.release();

        // Notify dealer.
        notifyAll();
    }
}
