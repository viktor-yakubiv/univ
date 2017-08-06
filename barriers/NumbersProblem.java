import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class NumbersProblem {
    // Configuration.
    final static int workersCount = 3;
    final static int dataArrayLength = 10;
    final static int dataItemMaxValue = 1000;


    // Data
    SumList[] data;

    // Barrier and workers
    CyclicBarrier barrier;
    Thread[] workers;

    // Condition for working process
    AtomicBoolean finish;
    AtomicLong average;


    /**
     * Construct new problem with randomly set data arrays.
     */
    public NumbersProblem() {
        // Generate data arrays.
        Random random = new Random(System.currentTimeMillis());
        data = new SumList[workersCount];
        for (int i = 0; i < data.length; ++i) {
            data[i] = new SumList(workersCount);
            for (int j = 0; j < dataArrayLength; ++j) {
                data[i].add(random.nextInt(dataItemMaxValue));
            }
        }

        // Conditions for process.
        average = new AtomicLong(0);
        finish = new AtomicBoolean(false);

        // Create barrier and workers.
        barrier = new CyclicBarrier(workersCount, this::updateConditions);
        workers = new Thread[workersCount];
        for (int i = 0; i < workers.length; ++i) {
            final int arrayIndex = i;
            workers[i] = new Thread(() -> updateList(arrayIndex));
        }
    }

    /**
     * Starts problem execution.
     */
    public void start() {
        // Start conditions configuration.
        updateConditions();

        // Start workers.
        for (Thread worker : workers) worker.start();
    }


    /**
     * Makes list's sum closer to average value of all list sums.
     * For this reduce or increase random element to random value.
     * Function (increment of decrement) depends on the ratio of
     * average sum and current sum.
     * @param listIndex    index of list in data[] to update
     */
    private void updateList(int listIndex) {
        Random generator = new Random(System.currentTimeMillis());
        SumList list = data[listIndex];

        while (!finish.get()) {
            int index = generator.nextInt(list.size());
            int currentValue = list.get(index);
            int nextValue = currentValue
                    + ((list.getSum() > average.get()) ? -1 : 1) * generator.nextInt(Math.abs(currentValue));
            list.set(index, nextValue);

            try {
                barrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Calculates new average sum value, checks finish working.
     * Also logs current state into console.
     */
    private void updateConditions() {
        // Calculate average.
        long sumOfSums = 0;
        for (SumList list : data) sumOfSums += list.getSum();
        long averageSum = sumOfSums / data.length;

        // Calculate finish condition.
        boolean newFinishState = true;
        for (int i = 0; (i < data.length) && newFinishState; ++i) {
            if (data[i].getSum() != averageSum) newFinishState = false;
        }

        // Update conditions.
        average.set(averageSum);
        finish.set(newFinishState);

        // Log.
        for (SumList list : data) System.out.println(list);
        System.out.println();
    }


    public static void main(String[] args) {
        NumbersProblem problem = new NumbersProblem();
        problem.start();
    }
}


/**
 * An array list with integer elements
 * that accumulates its sum.
 *
 * Class has basic (only!) overloading of parent list methods
 * for processing sum and method to get accumulated value.
 */
class SumList extends ArrayList<Integer> {
    // Sum of all elements in list
    long sum = 0;


    public SumList() {
        super();
    }

    public SumList(int initialCapacity) {
        super(initialCapacity);
    }

    @Override
    public boolean add(Integer element) {
        boolean added = super.add(element);
        if (added) sum += element;
        return added;
    }

    @Override
    public Integer remove(int index) {
        Integer removed = super.remove(index);
        sum -= removed;
        return removed;
    }

    @Override
    public Integer set(int index, Integer element) {
        sum = sum - get(index) + element;
        return super.set(index, element);
    }


    public long getSum() {
        return sum;
    }


    @Override
    public String toString() {
        String s = "";
        for (Integer e : this) s += e.toString() + " ";
        return s + "(" + getSum() + ")";
    }
}
