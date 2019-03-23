import com.sun.istack.internal.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

import static java.util.concurrent.Executors.newFixedThreadPool;

public class ReadersWritersProblem {
    // Database, problem solver
    PhoneBook book = new PhoneBook();

    /**
     * Runs user interface.
     * In infinity loop reads user commands and makes queries to database.
     */
    void runUI() {
        System.out.println("Type commands below.\n");

        Scanner consoleScanner = new Scanner(System.in);
        while (true) {
            // Get command.
            String cmdLine = consoleScanner.nextLine();
            int cmdSeparatorPos = cmdLine.indexOf(' ');
            String command =
                    cmdLine.substring(0, cmdSeparatorPos < 0 ? cmdLine.length() : cmdSeparatorPos).trim().toLowerCase();
            String arguments =
                    (cmdSeparatorPos < 0 ? "" : cmdLine.substring(cmdSeparatorPos)).trim();

            // Process command.
            switch (command) {
                case "help":
                    System.out.println("Help of commands\n" +
                            "  open <filename>\n" +
                            "  Opens database using specified file.\n\n" +

                            "  close\n" +
                            "  Closes current database.\n\n" +

                            "  add <name> :: <phone>\n" +
                            "  Adds new record with the specified name and telephone into the book.\n\n" +

                            "  remove <index>\n" +
                            "  Deletes record with the specified ID from the book.\n\n" +

                            "  name-search <name>\n" +
                            "  Searches all records with the specified name.\n\n" +

                            "  phone-search <phone>\n" +
                            "  Searches all record with the specified phone.\n\n" +

                            "  help\n" +
                            "  Outputs list of commands with description.\n\n" +

                            "  exit\n" +
                            "  Abort all operations, close database and exit.\n\n");
                    break;
                case "exit":
                    System.out.println("Exiting...");
                    this.interrupt();
                    return;
                case "open":
                    book.open(arguments);
                    break;
                case "close":
                    book.close();
                    break;
                case "name-search":
                    book.searchByName(arguments);
                    break;
                case "phone-search":
                    book.searchByPhone(arguments);
                    break;
                case "add":
                    int separatorPos = arguments.indexOf("::");
                    if (separatorPos < 0) {
                        System.out.println("Wrong command. Try 'help'");
                        break;
                    }
                    String name = arguments.substring(0, separatorPos).trim();
                    String phone = arguments.substring(separatorPos + 2).trim();
                    book.add(new PhoneBookRecord(name, phone));
                    break;
                case "remove":
                    int index = new Integer(arguments);
                    book.remove(index);
                    break;
                default:
                    System.out.println("Command not recognized. Try 'help'.");
            }
        }
    }

    /**
     * Starts problem
     */
    public void start() {
        runUI();
    }

    /**
     * Ends problem execution
     */
    public void interrupt() {
        book.close();
    }


    public static void main(String[] args) {
        // Create problem and execute problem.
        ReadersWritersProblem problem = new ReadersWritersProblem();
        problem.start();
    }
}


/**
 * Phone book
 *
 * Main database controller.
 * Executes tasks for the database and process results.
 */
class PhoneBook {
    // Amount of threads to process database tasks
    static int threadPoolSize = 4;

    // Task executor
    ExecutorService executor;

    // Task results monitor
    PhoneBookMonitor monitor;


    // Database file name if opened
    String dbFilename = null;

    // Database locker for threads synchronizing
    ReentrantReadWriteLock lock;


    /**
     * Checks is the database opened
     *
     * @return true if opened a database
     */
    public boolean isOpened() {
        return  dbFilename != null;
    }


    /**
     * Opens new database file or creates it if not exists
     *
     * @param filename name of database file
     */
    public void open(String filename) {
        try {
            // Abort operation is opened
            if (isOpened()) {
                throw new IOException("A book is opened. Close book before opening.");
            }

            // Check file exiting
            File checkFile = new File(filename);
            if (checkFile.isDirectory()) {
                throw new IOException(filename + " cannot be a directory.");
            }

            // Create file if not exists.
            if (!checkFile.exists() && !checkFile.createNewFile())
                throw new IOException("Cannot create file.");
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage() + "\n");
            return;
        }

        // Set database name.
        dbFilename = filename;

        // Create new executor, monitor and locker.
        lock = new ReentrantReadWriteLock();
        executor = newFixedThreadPool(threadPoolSize);
        monitor = new PhoneBookMonitor();
        monitor.start();

        // Log.
        System.out.println(dbFilename + " was opened.\n");
    }

    /**
     * Closes current database
     */
    public void close() {
        // End tasks and wait uo to 5 seconds for this operation.
        try {
            monitor.interrupt();
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (!executor.isTerminated())
                System.out.println();
            executor.shutdownNow();
        }

        // Log.
        if (dbFilename != null)
            System.out.println("Closed.\n");

        // Close file.
        dbFilename = null;
    }

    /**
     * Insert new record with the specified name and phone number into
     * first empty position in the database
     *
     * @param record new record to insert
     */
    public void add(PhoneBookRecord record) {
        if (!isOpened()) {
            System.out.println("Error: nothing is opened.\n");
            return;
        }

        monitor.add(executor.submit(new PhoneBookAdder(record, dbFilename, lock)));
    }

    /**
     * Removes record on specified position from database.
     *
     * @param index position of record in the database
     */
    public void remove(int index) {
        if (!isOpened()) {
            System.out.println("Error: nothing is opened.\n");
            return;
        }

        monitor.add(executor.submit(new PhoneBookRemover(index, dbFilename, lock)));
    }

    /**
     * Searches contacts by name pattern.
     *
     * @param name name to search
     */
    public void searchByName(String name) {
        if (!isOpened()) {
            System.out.println("Error: nothing is opened.\n");
            return;
        }

        monitor.add(executor.submit(new PhoneBookSearcher(name, dbFilename, lock, false)));
    }

    /**
     * Searches contacts by phone pattern.
     *
     * @param phone phone number to search
     */
    public void searchByPhone(String phone) {
        if (!isOpened()) {
            System.out.println("Error: nothing is opened.\n");
            return;
        }

        monitor.add(executor.submit(new PhoneBookSearcher(phone, dbFilename, lock, true)));
    }
}


/**
 * Phone book record
 *
 * Contains name and phone number.
 * Also saves unique index in the {@see PhoneBook}.
 */
class PhoneBookRecord {
    // Length of name field of record in bytes
    final static int nameLength = 128;

    // Length of phone field of record in bytes
    final static int phoneLength = 32;

    // Length of record byte representation
    final static int byteLength = nameLength + phoneLength;


    // Unique index of record in the PhoneBook
    int index = -1;

    // Record data
    public String name;
    public String phone;


    /**
     * Constructs new record with the specified name and phone number.
     *
     * @param name  name of person in the phone book
     * @param phone phone number for phone book record
     */
    public PhoneBookRecord(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    /**
     * Creates new record using bytes read from database file.
     *
     * @param bytes byte representation of record
     */
    public PhoneBookRecord(@NotNull byte[] bytes) {
        name = new String(Arrays.copyOfRange(bytes, 0, nameLength));
        phone = new String(Arrays.copyOfRange(bytes, nameLength, bytes.length));
    }


    /* Getters and setters */

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }


    /* Helpers and methods overrides */

    @Override
    public String toString() {
        if (isEmpty())
            return "<empty record>";

        return (index < 0 ? "" : String.format("% 4d. ", index)) + name + " - " + phone;
    }


    /**
     * Returns fixed length byte representation of record.
     *
     * @return bytes of record
     */
    public byte[] getBytes() {
        // Result
        byte[] bytes = new byte[byteLength];

        // Copy name
        byte[] nameBytes = name.getBytes();
        for (int i = 0; i < nameBytes.length && i < nameLength; ++i)
            bytes[i] = nameBytes[i];

        // Copy phone
        byte[] phoneBytes = phone.getBytes();
        for (int i = 0; i < phoneBytes.length && i < phoneLength; ++i)
            bytes[nameLength + i] = phoneBytes[i];

        return bytes;
    }

    public boolean isEmpty() {
        return (name.isEmpty() || name.charAt(0) == 0) && (phone.isEmpty() || phone.charAt(0) == 0);
    }
}


/**
 * Database tasks monitor.
 * With the specified period monitors tasks results and
 */
class PhoneBookMonitor extends Thread {
    // Monitor tasks each 300 ms
    static int monitorPeriod = 300;

    // Tasks list
    final LinkedBlockingQueue<Future<String>> taskList = new LinkedBlockingQueue<>();


    @Override
    public void run() {
        while (!isInterrupted()) {
            // Task list cleaner
            LinkedBlockingQueue<Future<String>> toRemove = new LinkedBlockingQueue<>();

            // Monitoring tasks.
            for (Future<String> task : taskList) {
                // Output results.
                if (task.isDone()) {
                    try {
                        System.out.println(task.get());
                        System.out.println();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                        return;
                    }
                }
                if (task.isDone() || task.isCancelled())
                    taskList.remove(task);
            }

            // Remove done tasks.
            taskList.removeAll(toRemove);

            // Wait for next iteration.
            try {
                sleep(monitorPeriod);
            } catch (InterruptedException e) {
                return;
            }
        }
    }


    /**
     * Adds new task to monitoring.
     *
     * @param futureTask task future object to monitoring
     */
    public synchronized void add(Future<String> futureTask) {
        taskList.add(futureTask);
    }
}


/**
 * Default callable task for PhoneBook
 */
class PhoneBookTask implements Callable<String> {
    // Task emulation time
    final static int emulationTime = 10000;


    // Data to add
    final protected Object data;

    // Name of database file
    final protected String filename;

    // Database locker
    final protected ReentrantReadWriteLock lock;


    public PhoneBookTask(Object data, String filename, ReentrantReadWriteLock lock) {
        this.data = data;
        this.filename = filename;
        this.lock = lock;
    }


    @Override
    public String call() throws Exception {
        return "Done.";
    }
}


/**
 * Callable task to add a record into database.
 */
class PhoneBookAdder extends PhoneBookTask {
    public PhoneBookAdder(PhoneBookRecord record, String filename, ReentrantReadWriteLock lock) {
        super(record, filename, lock);
    }

    @Override
    public String call() {
        try {
            // Get write position
            long toWritePos = getNullRecordPosition();

            // Access file.
            lock.writeLock().lock();
            RandomAccessFile file = new RandomAccessFile(filename, "rw");

            // Write record.
            PhoneBookRecord record = (PhoneBookRecord) data;
            file.seek(toWritePos * PhoneBookRecord.byteLength);
            file.write(record.getBytes());

            // Emulation.
            Thread.sleep(emulationTime);

            // Release file.
            file.close();
            lock.writeLock().unlock();

            return "Addition success.";
        } catch (Exception e) {
            return "Error with addition: " + e.getClass().getName() + ": " + e.getMessage();
        }
    }

    /**
     * Reads database file and returns first null record positions
     * for rewriting.
     *
     * @return relative ordinal value of null-record position
     * @throws IOException
     */
    long getNullRecordPosition() throws IOException {
        lock.readLock();
        FileInputStream inputStream = new FileInputStream(filename);

        // Start result.
        long nullRecordIndex = inputStream.available() / PhoneBookRecord.byteLength + 1;

        // Find null record in the file.
        long index = 0; // current index of record
        while (inputStream.available() > 0) {
            byte[] recordBytes = new byte[PhoneBookRecord.byteLength];
            if (inputStream.read(recordBytes) == recordBytes.length) {
                PhoneBookRecord record = new PhoneBookRecord(recordBytes);
                if (record.isEmpty()) {
                    nullRecordIndex = index;
                    break;
                }
            }
            index++;
        }

        inputStream.close();
        lock.readLock();

        return nullRecordIndex;
    }
}


/**
 * Removes record from database what has specified by constructor position.
 * E.G. Writes null-record to this position.
 */
class PhoneBookRemover extends PhoneBookTask {
    public PhoneBookRemover(Integer position, String filename, ReentrantReadWriteLock lock) {
        super(position, filename, lock);
    }

    @Override
    public String call() {
        try {
            Integer index = (Integer) data;

            // Open file.
            lock.writeLock().lock();
            RandomAccessFile file = new RandomAccessFile(filename, "rw");

            // Check index
            if (file.length() < index * PhoneBookRecord.byteLength) {
                file.close();
                return "Error removing: out of bound.";
            }


            // Writing.
            file.seek(index * PhoneBookRecord.byteLength);
            file.write(new PhoneBookRecord("", "").getBytes());

            // Emulation.
            Thread.sleep(emulationTime);

            // Release file.
            file.close();
            lock.writeLock().unlock();

            return "Removing success.";
        } catch (Exception e) {
            return "Error with removing: " + e.getClass().getName() + ": " + e.getMessage();
        }
    }
}


/**
 * Searches contacts in phone book whats matches specified in constructor pattern
 * and returns list of results as string.
 */
class PhoneBookSearcher extends PhoneBookTask {
    public PhoneBookSearcher(String pattern, String filename, ReentrantReadWriteLock lock, boolean byPhone) {
        super(pattern, filename, lock);

        // Process regular expression.
        pattern = ".*" + pattern + ".*";
        this.pattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);

        this.byPhone = byPhone;
    }


    // Pattern to search
    Pattern pattern;

    // If true search by phone else search by name
    boolean byPhone;


    @Override
    public String call() {
        try {
            // Get access to file.
            lock.readLock();
            FileInputStream inputStream = new FileInputStream(filename);

            // Process file.
            int amountFound = 0;
            String result = "";
            while (inputStream.available() > 0) {
                byte[] recordBytes = new byte[PhoneBookRecord.byteLength];
                if (inputStream.read(recordBytes) == recordBytes.length) {
                    PhoneBookRecord record = new PhoneBookRecord(recordBytes);

                    // Check phone
                    if (pattern.matcher(byPhone ? record.phone : record.name).matches()) {
                        result += "  " + record.toString() + "\n";
                        amountFound++;
                    }
                }
            }

            // Emulation.
            Thread.sleep(emulationTime);

            // Release file.
            inputStream.close();
            lock.readLock();

            return amountFound == 0
                    ? "Nothing found."
                    : "Found " + amountFound + " match" + (amountFound == 1 ? "" : "es") + ":\n" +
                    result.substring(0, result.length() - 1);
        } catch (Exception e) {
            return "Error with searching " + e.getClass().getName() + ": " + e.getMessage();
        }
    }
}

