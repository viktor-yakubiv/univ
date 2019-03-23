import java.util.Random;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 *
 */
public class FirstMilitaryTask {

    // Weapons config
    final static String[] weaponNames = {
            "CG",
            "H&R Ultraslug Hunter",
            "DS",
            "Remington Spartan 100",
            "BAC",
            "Cooey 84",
            "Winchester Model 37",
            "Bandayevsky RB-12",
            "Benelli Nova",
            "Benelli Supernova",
            "Ciener Ultimate Over/Under",
            "FN TPS",
            "Heckler & Koch FABARM FP6",
            "CS",
            "Ithaca 37",
            "KAC Masterkey",
            "Kel-Tec K",
            "KS-23",
            "M26 Modular Accessory Shotgun System",
            "MAG-7",
            "Molot Bekas-M",
            "Mossberg 500",
            "Mossberg 500 2 barrels.png",
            "Mossberg 590",
            "Neostead",
            "CS",
            "Norinco HP9-1",
            "Remington 870",
            "Remington 887",
            "Remington Model 10",
            "Remington Model 17",
            "Remington Model 31",
            "RMB-93",
            "wed-off Shotgun 	",
            "TOZ-194",
            "Valtro PM-5/PM-5-350",
            "Winchester Model 1897",
            "Winchester Model 1912",
            "Winchester Model 1200",
            "Winchester Model 1887/1901",
            "Akdal MKA 1919",
            "Armscor Model 30",
            "Armsel Striker",
            "Atchisson Asult Shotgun",
            "FA",
            "Baikal MP-153",
            "Benelli M1 Super 90",
            "Benelli M3 Super 90",
            "Benelli M4 Super 90",
            "Benelli Raffaello",
            "Benelli Vinci",
            "Beretta 1201FP",
            "Beretta A303",
            "Beretta AL391",
            "Beretta Xtrema 2",
            "Blaser F3",
            "Browning Auto-5",
            "Browning Double Automatic Shotgun",
            "FN Herstal 	",
            "ENARM Pentagun",
            "Fabarm SDASS Tactical",
            "FN SLP",
            "Franchi AL-48",
            "Franchi mod .410",
            "CS",
            "Franchi SPAS-12",
            "Franchi SPAS-15",
            "Heckler & Koch HK CAWS",
            "High Standard Model 10",
            "Ithaca Mag-10",
            "M1216",
            "MAUL (weapon)",
            "MAUL shotgun.PNG",
            "Mossberg 930",
            "Pancor Jackhammer",
            "Parker Hale Rogun",
            "Remington Model 11",
            "Remington Model 11-48",
            "Remington 11-87",
            "Remington Model 58",
            "Remington Model 878",
            "Remington Model 1100",
            "Remington Model SP-10",
            "Remington Spartan 453",
            "Retay Mai Mara",
            "fir T-14",
            "iga-12",
            "Sjögren shotgun",
            "US-12",
            "FA",
            "Vepr-12",
            "Weatherby -08",
            "Beretta DT-10",
            "Browning Cynergy",
            "Browning Superposed",
            "Remington Spartan 310",
            "Stoeger Condor",
            "Beretta 682",
            "Beretta Silver Pigeon",
            "Browning Citori",
            "Coach gun",
            "Double-barreled shotgun",
            "Lupara",
            "Ruger Gold Label",
            "Stoeger Coach Gun",
            "Winchester Model 21",
            "Famars Rombo",
            "Marlin Model 55",
            "MTs-255",
            "RGA-86",
            "Fort 500"
    };
    final static int weaponMaxValue = 1000;


    // Random for generating values of weapons
    Random random = new Random(System.currentTimeMillis());

    // Warehouse where robbers get weapons
    WeaponCollection warehouse;

    // Lorry where robbers put weapons
    WeaponCollection lorry;


    // Robbers
    Producer<Weapon> producer; // Ivanov
    Consumer<Weapon> consumer; // Petrov
    Monitor          monitor;  // Necheporchuk


    /**
     * Constructs new problem with the standard parameters.
     */
    public FirstMilitaryTask() {
        // Weapons.
        warehouse = new WeaponCollection();
        for (String name : weaponNames) {
            try {
                warehouse.put(new Weapon(name, random.nextInt(weaponMaxValue)));
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
        lorry = new WeaponCollection();

        // Workers.
        producer = new Producer<>(warehouse);
        producer.setName("Ivanov");

        consumer = new Consumer<>(producer, lorry);
        consumer.setName("Petrov");

        monitor = new Monitor(lorry);
        monitor.setName("Necheporchuk");
    }

    /**
     * Starts execution of the problem.
     */
    public void start() {
        producer.start();
        consumer.start();
        monitor.start();
    }


    public static void main(String args[]) {
        FirstMilitaryTask problem = new FirstMilitaryTask();
        problem.start();
    }
}


/**
 * Weapon
 * Data object for task solution.
 */
class Weapon {
    // Name of weapon
    String name;

    // Cost of weapon
    int value;


    /**
     * Creates new Weapon with specified name and value
     *
     * @param name  name of weapon
     * @param value cost of weapon
     */
    Weapon(String name, int value) {
        setName(name);
        setValue(value);
    }


    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValue() {
        return this.value;
    }

    public void setValue(int value) {
        this.value = value > 0 ? value : 0;
    }

    @Override
    public String toString() {
        return name;
    }
}


/**
 * Weapon collection queue.
 * Collects weapons and transfers objects between workers.
 */
class WeaponCollection extends LinkedBlockingDeque<Weapon> {}


/**
 * Producer collects an element from the specified collection
 * and waits to somebody gets this element.
 * This operation is repeating while collection contains any element.
 *
 * @param <E> type of elements that producer collects
 */
class Producer<E> extends Thread {
    // Time for puller is working
    static int workingTime = 1000;


    // Collection to get elements
    BlockingQueue<E> collection;

    // Collected element
    volatile E collected;


    /**
     * Construct new collector (producer) of elements on the
     * specified queue.
     *
     * @param collection collection to get elements
     */
    public Producer(BlockingQueue<E> collection) {
        this.collection = collection;
        collected = null;
    }


    @Override
    public void run() {
        while (!collection.isEmpty()) {
            try {
                // Get element
                E element = collection.poll();

                // Emulate working
                sleep(workingTime);

                // Log.
                System.out.println(getName() + " collected a " + element.toString() + " and waiting...");

                // Put element
                putCollected(element);
            } catch (InterruptedException e) {
                System.out.println(getName() + " was interrupted.");
                return;
            }
        }
    }


    /**
     * Returns collected element of waits for its collection.
     * If nothing to collect return null.
     *
     * @return collected element of null
     * @throws InterruptedException
     */
    public synchronized E getCollected() throws InterruptedException {
        // Finish work.
        if (collection.isEmpty())
            return null;

        // Wait for collecting element.
        while (collected == null) {
            wait();
        }

        // Free collection space.
        E element = collected;
        collected = null;

        // Notify producer.
        notify();

        return element;
    }

    /**
     * Collects an element if have free space for it.
     * Otherwise waits.
     *
     * @param element an element to collect
     * @throws InterruptedException
     */
    public synchronized void putCollected(E element) throws InterruptedException {
        // Wait for free space to collecting.
        while (collected != null) {
            wait();
        }

        // Put element.
        collected = element;

        // Notify consumer.
        notify();
    }
}


/**
 * Consumer
 * Gets an element collected by Producer
 *
 * @param <E> type of collected elements
 */
class Consumer<E> extends Thread {
    // Time for consumer is working
    static int workingTime = 1000;


    // Producer giving collected elements
    final Producer<E> producer;

    // Collection to put elements from producer
    final BlockingQueue<E> collection;


    /**
     * Constructs new consumer for the specified producer and putting collection.
     *
     * @param producer producer who gives a collected elements
     * @param collection collection for putting elements
     */
    Consumer(Producer<E> producer, BlockingQueue<E> collection) {
        this.producer = producer;
        this.collection = collection;
    }


    @Override
    public void run() {
        while (true) {
            try {
                // Get element.
                E element = producer.getCollected();

                // Emulate working.
                sleep(workingTime);

                // Put element with end-working detection
                synchronized (collection) {
                    collection.put((element == null) ? collection.peek() : element);
                    collection.notify();
                }
                if (element == null) break;

                // Log.
                System.out.println(getName() + " got and put " + element.toString());
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}


/**
 * Monitors specified Weapon collection and calculates
 * sum of its elements.
 */
class Monitor extends Thread {
    // Collection to monitor
    final BlockingDeque<Weapon> collection;

    // Monitoring result
    long result = 0;


    // Current monitor iteration.
    int iteration;

    /**
     * Constructs new collection monitor on the specified deque.
     *
     * @param collection deque for monitoring elements in the end
     */
    Monitor(BlockingDeque<Weapon> collection) {
        this.collection = collection;
        iteration = this.collection.size();
    }


    @Override
    public void run() {
        while (true) {
            try {
                // New element.
                Weapon weapon;

                // Get element.
                synchronized (collection) {
                    // Wait for new element.
                    while (collection.size() == iteration) collection.wait();

                    // Get result.
                    weapon = collection.peekLast();

                    // Check end.
                    if (weapon == collection.peek()) {
                        collection.pollLast();
                        break;
                    }
                }

                // Process result.
                result += weapon.getValue();

                // Log.
                System.out.println(getName() + " added " + weapon.getValue() + " (Sum: " + getResult() + ")");

                // Next iteration
                iteration++;
            } catch (InterruptedException e) {
                System.out.println(getName() + " was interrupted.");
                break;
            }
        }

        // Log.
        System.out.println(getName() + " calculated value: " + result);
    }


    public long getResult() {
        return result;
    }
}
