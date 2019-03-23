import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;


public class BarberProblem {
    // Random for generating clients
    final Random clientRandom = new Random(System.currentTimeMillis());

    // Random list of names for clients
    final static String[] clientNames = {
            "Wm Strait",
            "Belinda Mehan",
            "Jarvis Ariza",
            "Sheryl Niebuhr",
            "Kathlene Wynn",
            "Talitha Culotta",
            "Toni Trembath",
            "Delilah Labrie",
            "Waylon Bridgewater",
            "Sue Byerley",
            "Camille Konkel",
            "Stacee Danks",
            "Whitney Tosh",
            "Isaias Brickner",
            "Tifany Bilbrey",
            "Alleen Vanduyne",
            "Janine Lebeau",
            "Corina Felty",
            "Freddie Chewning",
            "Marisa Damm",
            "Loriann Middlebrook",
            "Trena Kanne",
            "Arica Stogner",
            "Marco Seitz",
            "Agatha Emmons",
            "Santiago Diemer",
            "Adela In",
            "Abraham Gentry",
            "Sina Atchison",
            "Demarcus Degennaro",
            "Christia Munford",
            "Kasandra Longtin",
            "Jame Spurgeon",
            "Jerilyn Providence",
            "George Foard",
            "Lady Pines",
            "Palma Janicki",
            "Sharan Metivier",
            "Magan Kearns",
            "Sherwood Sinclair",
            "Deb Mullikin",
            "Willetta Althaus",
            "Mica Brokaw",
            "Alica Blackmer",
            "Emelina Helm",
            "Abdul Araiza",
            "Briana Pospisil",
            "Melani Brott",
            "Starla Molder",
            "Deloris Olson"
    };

    // Max hair length for random
    final static int maxHairLength = 3000;


    // Count of clients to emulate problem
    int amountClients = 0;

    // Barbershop of problem
    Barbershop shop = new Barbershop();


    /**
     * Emulate new BarberProblem with the specified count of clients.
     *
     * @param amountClients count of clients to ad to the Barbershop
     */
    public BarberProblem(int amountClients) {
        this.amountClients = amountClients;
    }

    /**
     * Starts the problem
     */
    public void start() {
        // Create and start barbershop with barber.
        shop.start();

        // Add clients
        for (int i = 0; i < amountClients; ++i) {
            // Random name
            String name = clientNames[clientRandom.nextInt(clientNames.length)];
            // and hair for the client
            int hair = clientRandom.nextInt(maxHairLength);

            shop.addClient(new Client(shop, name, hair));
        }
    }

    /**
     * Interrupts problem
     */
    public void interrupt() {
        shop.interrupt();
    }


    public static void main(String[] args) {
        // Execute problem.
        BarberProblem problem = new BarberProblem(5);
        problem.start();

        // Wait some time for the execution.
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        problem.interrupt();
    }
}


/**
 * Client states for process synchronization
 */
enum ClientState {
    // User is waiting for an operation done.
    Waiting,

    // User is ready for the next operation.
    Ready,

    // User is in process with own operation.
    Process,

    // User have done own operation.
    Done
}

/**
 * Client class
 *
 * - Waits for the barber ready to cut him.
 * - Takes the seat in the barbershop.
 * - Sleeps until barber cutting.
 * - Releases the seat and goes out from the barbershop.
 */
class Client extends Thread {

    // State of client
    ClientState state = ClientState.Waiting;

    // Barbershop where client is placed
    final Barbershop shop;

    // Client metadata
    int hair;


    /**
     * Generates new client for the specified barbershop with name and hair.
     *
     * @param shop place where client is haircutting
     * @param name name of client
     * @param hair hair length of client
     */
    Client(Barbershop shop, String name, int hair) {
        this.shop = shop;
        setName(name);
        this.hair = Math.abs(hair);
    }

    /**
     * Sets new state and notify others about.
     *
     * @param state new state for the client
     */
    protected synchronized void setState(ClientState state) {
        // Change state.
        this.state = state;

        // Log.
        System.out.println(getName() + " is " + state.toString() + ".");

        // Report state changing.
        notify();
    }

    protected synchronized void setHaircut(int hair) {
        // Log.
        System.out.print(getName() + " haircut from " + getHaircut());

        // Change haircut.
        this.hair = Math.abs(hair);

        // Log.
        System.out.print(" to " + getHaircut() + ".\n");
    }


    /**
     * Returns current client state.
     *
     * @return client state
     */
    public synchronized ClientState getClientState() {
        return state;
    }

    /**
     * Returns current haircut of client
     *
     * @return current client haircut
     */
    public synchronized int getHaircut() {
        return this.hair;
    }

    /**
     * Wait while client is not ready.
     *
     * @throws InterruptedException
     */
    public synchronized void getReady() throws InterruptedException {
        while (state != ClientState.Ready)
            wait();
    }

    /**
     * Wait while client is not done.
     *
     * @throws InterruptedException
     */
    public synchronized void getDone() throws InterruptedException {
        while (state != ClientState.Done)
            wait();
    }


    @Override
    public void run() {
        Barber barber = shop.getBarber();

        try {
            // Wait for the barber ready to this client
            barber.getReady();

            // Haircut.
            takeSeat();        // Take a seat in the barbershop
            barber.getDone();  // dnd wait for the barber done.
            releaseSeat();     // After that release seat.
        } catch (InterruptedException e) {
            System.out.println(getName() + " interrupted.");
        }
    }


    /**
     * Takes a seat and report to barber.
     *
     * @throws InterruptedException
     */
    private void takeSeat() throws InterruptedException {
        setState(ClientState.Process);
        sleep(500);
        setState(ClientState.Ready);
    }

    /**
     * Releases seat and report to barber.
     *
     * @throws InterruptedException
     */
    private void releaseSeat() throws InterruptedException {
        setState(ClientState.Process);
        sleep(500);
        setState(ClientState.Done);
    }
}

/**
 * Barber - is a client who:
 *
 * - Waits for an clients (sleeps until)
 * - Wakes up the client from the queue and waits while him takes the seat.
 * - Haircutting him.
 * - Wakes up after and waits while he keeps out.
 */
class Barber extends Client {

    // Current client of a barber
    Client client = null;

    // Random for the haircut
    Random random = new Random(System.currentTimeMillis());


    /**
     * Creates new Barber in the specified Barbershop
     *
     * @param shop barbershop where barber works (shared data container)
     */
    Barber(Barbershop shop) {
        super(shop, "Barber", 0);
    }


    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                // Get next client or wait for.
                setState(ClientState.Waiting);
                client = shop.getClient();
                client.start();
                setState(ClientState.Ready);

                // Serve client.
                client.getReady();    // Wait for the client take a seat.
                serveClient();        // Serve client.
                client.getDone();     // Wait for client release seat.
                System.out.println(); // Prettify log.
            } catch (InterruptedException e) {
                System.out.println(getName() + " interrupted.");
            }
        }
    }

    /**
     * Serves client. Haircuts and wake up him after done.
     *
     * @throws InterruptedException
     */
    void serveClient() throws InterruptedException {
        // Start work.
        setState(ClientState.Process);

        // Emulate work.
        sleep(2000);
        client.setHaircut(random.nextInt(client.getHaircut()));

        // End work.
        setState(ClientState.Done);
    }
}

/**
 * Container and shared data for the Barber and Client.
 */
class Barbershop extends Thread {

    // Barber in the barbershop.
    Barber barber = new Barber(this);

    // Clients queue.
    // Helper semaphore for the clients and barber.
    LinkedBlockingQueue<Client> clients = new LinkedBlockingQueue<>();


    /**
     * Gets next client in the queue or wait for
     *
     * @return first client from the queue
     * @throws InterruptedException
     */
    protected synchronized Client getClient() throws InterruptedException {
        while (clients.isEmpty())
            wait();

        return clients.poll();
    }

    /**
     * Returns barber of the barbershop.
     *
     * @return barber working in the barbershop
     */
    protected synchronized Barber getBarber() {
        return barber;
    }


    @Override
    public void start() {
        barber.start();
    }

    @Override
    public void interrupt() {
        barber.interrupt();
        while (!clients.isEmpty()) {
            clients.poll().interrupt();
        }
    }


    /**
     * Adds new client into barbershop queue.
     *
     * @param client a client to add
     */
    public synchronized void addClient(Client client) {
        clients.add(client);
        notifyAll();
    }
}
