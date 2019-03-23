import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class SoldiersProblem {

    static int amountWorkers = 2;
    static int dataPerWorker = 60;

    CyclicBarrier barrier;
    Thread[] workers;

    SoldiersArray soldiers;


    public SoldiersProblem() {
        soldiers = new SoldiersArray(amountWorkers * dataPerWorker);

        barrier = new CyclicBarrier(amountWorkers, new SoldierChecker(barrier, soldiers));

        workers = new Thread[amountWorkers];
        for (int i = 0; i < amountWorkers; ++i) {
            int start = i * dataPerWorker;
            int end = (i + 1) * dataPerWorker;
            workers[i] = new Thread(new SoldierTurner(barrier, soldiers, start, end));
        }
    }

    /**
     * Starts problem execution
     */
    void start() {
        // Generate values random values for both buffers.
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < soldiers.size(); ++i) {
            SoldierPosition position = SoldierPosition.values()[random.nextInt(SoldierPosition.values().length)];
            soldiers.set(i, position);
        }
        soldiers.switchBuffers();
        for (int i = 0; i < soldiers.size(); ++i) soldiers.set(i, soldiers.get(i));
        soldiers.switchBuffers();

        // Start combination log.
        System.out.println(soldiers);

        // Start threads.
        for (Thread worker: workers) worker.start();
    }


    public static void main(String[] args) {
        SoldiersProblem problem = new SoldiersProblem();
        problem.start();
    }
}


enum SoldierPosition {
    // Soldier turned left
    Left,

    // Soldier turned right
    Right;

    /**
     * Turns soldier on 180 degrees
     * @param position start soldier's position
     * @return new soldier's position
     */
    public static SoldierPosition inversePosition(SoldierPosition position) {
        return (position == Left) ? Right : Left;
    }
}

class SoldiersArray {
    SoldierPosition[] statesFirst, statesSecond;

    boolean switchState = false;

    boolean doneState = false;

    public SoldiersArray(int size) {
        statesFirst = new SoldierPosition[size];
        statesSecond = new SoldierPosition[size];
    }

    public synchronized int size() {
        return statesFirst.length;
    }

    public synchronized SoldierPosition get(int i) {
        return statesFirst[i];
    }

    public synchronized void set(int i, SoldierPosition value) {
        statesFirst[i] = value;
    }

    public synchronized void switchBuffers() {
        switchState = !switchState;
    }

    public synchronized boolean isDone() {
        return doneState;
    }

    public synchronized void makeDone() {
        doneState = true;
    }

    @Override
    public String toString() {
        String s = "";
        for (int i = 0; i < size(); ++i) s += (get(i) == SoldierPosition.Left) ? "L" : "R";
        return s;
    }
}


class SoldierTurner implements Runnable {
    final CyclicBarrier barrier;

    final SoldiersArray soldiers;

    final int start, end;


    SoldierTurner(CyclicBarrier barrier, SoldiersArray soldiers, int start, int end) {
        this.barrier = barrier;
        this.soldiers = soldiers;
        this.start = start;
        this.end = end;
    }


    @Override
    public void run() {
        while (!soldiers.isDone()) {
            for (int i = start; i < end; ++i) {
                if (i == 0) continue;

                SoldierPosition left = soldiers.get(i - 1);
                SoldierPosition right = soldiers.get(i);
                if (left != right && left == SoldierPosition.Right) {
                    soldiers.set(i - 1, SoldierPosition.inversePosition(left));
                    soldiers.set(i, SoldierPosition.inversePosition(right));
                }
            }

            try {
                barrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }
}


class SoldierChecker implements Runnable {
    final CyclicBarrier barrier;

    final SoldiersArray soldiers;

    SoldierChecker(CyclicBarrier barrier, SoldiersArray soldiers) {
        this.barrier = barrier;
        this.soldiers = soldiers;
    }

    @Override
    public void run() {
        soldiers.switchBuffers();

        boolean finished = true;
        for (int i = 1; i < soldiers.size() && finished; ++i) {
            SoldierPosition left = soldiers.get(i - 1);
            SoldierPosition right = soldiers.get(i);
            if (left != right && left == SoldierPosition.Right) finished = false;
        }
        if (finished) soldiers.makeDone();

        // Log.
        System.out.println(soldiers);
    }
}