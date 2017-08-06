import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;

public class StringsProblem {
    public static int workersCount = 4;

    static int stringLength = 100;

    // Data
    StringBuilder[] data;

    // Barrier and workers
    CyclicBarrier barrier;
    Thread[] workers;

    // Condition for working process
    AtomicBoolean barrierCondition;


    public StringsProblem() {
        // Generate data strings.
        Random random = new Random(System.currentTimeMillis());
        data = new StringBuilder[workersCount];
        for (int i = 0; i < data.length; ++i) {
            data[i] = new StringBuilder();
            for (int j = 0; j < stringLength; ++j) {
                data[i].append((char) (random.nextInt(workersCount) + 'A'));
            }
        }

        // Set condition.
        barrierCondition = new AtomicBoolean(false);

        // Create barrier and workers.
        barrier = new CyclicBarrier(workersCount, new CharsChecker(data, barrierCondition));
        workers = new Thread[workersCount];
        for (int i = 0; i < workers.length; ++i) {
            workers[i] = new Thread(new CharChanger(barrier, data[i], barrierCondition));
        }
    }


    public void start() {
        // Log start data sequences.
        for (StringBuilder string: data) System.out.println(string);
        System.out.println();

        // Start workers.
        for (Thread worker : workers) worker.start();
    }


    public static void main(String[] args) {
        StringsProblem problem = new StringsProblem();
        problem.start();
    }
}


class CharChanger implements Runnable {
    final CyclicBarrier barrier;
    final StringBuilder string;
    final AtomicBoolean done;

    CharChanger(CyclicBarrier barrier, StringBuilder string, AtomicBoolean doneChecker) {
        this.barrier = barrier;
        this.string = string;
        this.done = doneChecker;
    }

    @Override
    public void run() {
        // Amount of chars available in the string
        final int amount = StringsProblem.workersCount;
        // Increment to invert character
        final int inc = amount / 2;

        final Random random = new Random(System.currentTimeMillis());

        // Chars processing.
        while (!done.get()) {
            // Inverse random char.
            int charPos = random.nextInt(string.length());
            char invertedChar = (char) ((string.charAt(charPos) - 'A' + inc) % amount + 'A');
            string.setCharAt(charPos, invertedChar);

            // Report to checker.
            try {
                barrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }
}


class CharsChecker implements Runnable {
    final StringBuilder[] data;
    final AtomicBoolean result;

    CharsChecker(StringBuilder[] data, AtomicBoolean result) {
        this.data = data;
        this.result = result;
    }

    @Override
    public void run() {
        // Log new states.
        for (StringBuilder string : data) System.out.println(string);
        System.out.println();

        // Calculate count of each char in each string.
        // In 'charCounts' row is a single character counts,
        // column is list of character counts for a single string.
        int charCounts[][] = new int[data.length][data.length];
        for (int i = 0; i < data.length; ++i) {
            for (int j = 0; j < data[i].length(); ++j) {
                int charIndex = data[i].charAt(j) - 'A';
                charCounts[charIndex][i]++;
            }
        }

        // Check counts of chars for the end of processing.
        // Stop condition: in 75% of strings counts of half of chars are equals,
        // e.g. for the 4 strings counts of the 'A' and 'B' are equals.
        boolean countsEqual = true;
        for (int i = 0; (i < charCounts.length / 2) && countsEqual; ++i) {
            countsEqual = checkEquality(charCounts[i], (int) (0.75 * charCounts[i].length));
        }
        result.set(countsEqual);
    }


    /**
     * Checks all combinations with specified length for equality of all items inside.
     *
     * @param values array of values for generating combinations and checking
     * @param amountEquals size of each combination
     * @return true if any combination has equals numbers inside
     */
    boolean checkEquality(int[] values, int amountEquals) {
        // Calculate counts
        HashSet<Integer> valuesSet = new HashSet<>();
        for (int current : values) valuesSet.add(current);

        return (valuesSet.size() <= values.length - amountEquals + 1);
    }
}
