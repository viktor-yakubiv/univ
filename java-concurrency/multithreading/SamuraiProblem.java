import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

public class SamuraiProblem {
    // Random samurais names
    final static String[] names = {
            "Kuba Kunimichi",
            "Ozaki Toru",
            "Okimoto Tashiaki",
            "Sando Narihari",
            "Fukui Isei",
            "Minamoto Kyoshi",
            "Soma Hirokazu",
            "Yanagi Saneatsu",
            "Kinoshita Benkei",
            "Satow Benjiro",
            "Sadow Kojuro",
            "Tominaga Kyuichi",
            "Ueno Tsutomu",
            "Sando Yasujiro",
            "Yokota Yasujiro",
            "Honda Hirotaka",
            "Ikehara Kakuzo",
            "Imada Juzaburo",
            "Kida Mobumasu",
            "Kishi Ryuzaburo",
            "Ohta Munoto",
            "Asano Nissho",
            "Konno Kazu",
            "Miyashita Koki",
            "Taguchi Tasuku",
            "Soda Tadamasa",
            "Kurata Natsuo",
            "Kawakami Yoshiyuki",
            "Igarashi Norogumi",
            "Toyoda Hideyori",
            "Ohta Munoto",
            "Asano Nissho",
            "Konno Kazu",
            "Miyashita Koki",
            "Taguchi Tasuku",
            "Soda Tadamasa",
            "Kurata Natsuo",
            "Kawakami Yoshiyuki",
            "Igarashi Norogumi",
            "Toyoda Hideyori",
            "Ide Nobuyori",
            "Takashima Inejiro",
            "Kawamoto Tamasaburo",
            "Masuda Takahiro",
            "Inaba Seiki",
            "Sako Hyobe",
            "Yamasaki Kikunojo",
            "Matsuoka Masu",
            "Iwai Yushiro",
            "Nagamine Toshimichi"
    };

    // Initial count of samurais for each house
    final static int initialSamuraisCount = 50;


    // Generator
    Random random = new Random(System.currentTimeMillis());


    // Samurais houses
    ArrayList<Samurai> GuanYinHouse, GuanYangHouse;


    public SamuraiProblem() {
        // Generate samurai teams.
        GuanYinHouse = new ArrayList<>(initialSamuraisCount);
        generateTeam(GuanYinHouse, Team.GuanYin, initialSamuraisCount);
        GuanYangHouse = new ArrayList<>(initialSamuraisCount);
        generateTeam(GuanYangHouse, Team.GuanYang, initialSamuraisCount);
    }

    void generateTeam(List<Samurai> teamHouse, Team team, int amount) {
        for (int i = 0; i < initialSamuraisCount; ++i) {
            String name = names[random.nextInt(names.length)];
            int energy = (Samurai.maxEnergy - Samurai.minEnergy) / 2
                    + random.nextInt((Samurai.maxEnergy - Samurai.minEnergy) / 2);
            Samurai samurai = new Samurai(name, team, energy);
            teamHouse.add(samurai);
        }
    }


    /**
     * Starts problem execution
     */
    public void start() {
        // Fights sources and results
        BlockingDeque<Fight> fights = new LinkedBlockingDeque<>(initialSamuraisCount);
        BlockingQueue<Samurai> results = new LinkedBlockingQueue<>();

        int roundIndex = 0;
        while (!GuanYinHouse.isEmpty() && !GuanYangHouse.isEmpty()) {
            // Log.
            System.out.println("\nRound " + (roundIndex + 1));
            System.out.println("----------------------------------------");

            // Generate fight list for this round.
            generateFights(fights);

            // Execute fights.
            FightArea fightAreaLeft = new FightArea(fights, results, 0);
            FightArea fightAreaRight = new FightArea(fights, results, 1);
            fightAreaLeft.start();
            fightAreaRight.start();
            try {
                fightAreaLeft.join();
                fightAreaRight.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }

            // Process results.
            GuanYinHouse.clear();
            GuanYangHouse.clear();
            while (!results.isEmpty()) {
                Samurai winner = results.poll();
                // Return winner to home
                ((winner.getTeam() == Team.GuanYin) ? GuanYinHouse : GuanYangHouse).add(winner);
                // Add some energy after returning to home
                winner.setEnergy(winner.getEnergy() + (Samurai.maxEnergy - Samurai.minEnergy) / 2);
            }

            // Next iteration.
            roundIndex++;
        }

        // Log
        System.out.println("\n" + (GuanYinHouse.isEmpty() ? Team.GuanYang : Team.GuanYin) + " is winner!");
    }

    void generateFights(Collection<Fight> fights) {
        // Shuffle input teams.
        Collections.shuffle(GuanYinHouse);
        Collections.shuffle(GuanYangHouse);

        // Generate pairs to fight/
        for (int i = 0; (i < GuanYinHouse.size()) && (i < GuanYangHouse.size()); ++i) {
            fights.add(new Fight(GuanYinHouse.get(i), GuanYangHouse.get(i)));
        }
    }


    public static void main(String[] args) {
        SamuraiProblem problem = new SamuraiProblem();
        problem.start();
    }
}

enum Team { GuanYin, GuanYang }

class Samurai {
    // Energy range
    public final static int maxEnergy = 100;
    public final static int minEnergy = 0;

    String name;
    Team team;
    int energy = minEnergy;

    public Samurai(String name, Team team) {
        this.name = name;
        this.team = team;
    }

    public Samurai(String name, Team team, int energy) {
        this(name, team);
        this.energy = energy;
    }

    public String getName() {
        return name;
    }

    public Team getTeam() {
        return team;
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = Math.max(minEnergy, Math.min(energy, maxEnergy));
    }

    @Override
    public String toString() {
        return getName() + " (" + getEnergy() + ")";
    }
}


class Fight {
    // Pair of fighters
    final Samurai first, second;


    public Fight(Samurai first, Samurai second) {
        this.first = first;
        this.second = second;
    }


    public Samurai getFirst() {
        return first;
    }

    public Samurai getSecond() {
        return second;
    }

    public Samurai getWinner() {
        return ((first.getEnergy() > second.getEnergy()) ? first : second);
    }

    public Samurai getLooser() {
        return ((first.getEnergy() < second.getEnergy()) ? first : second);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Fight fight = (Fight) o;

        if (first != null ? !first.equals(fight.first) : fight.first != null) return false;
        return second != null ? second.equals(fight.second) : fight.second == null;

    }

    @Override
    public int hashCode() {
        int result = first != null ? first.hashCode() : 0;
        result = 31 * result + (second != null ? second.hashCode() : 0);
        return result;
    }
}

class FightArea extends Thread {
    // Random for fights
    final Random random = new Random(System.currentTimeMillis());

    // Input fights and results
    final BlockingDeque<Fight> fights;
    final BlockingQueue<Samurai> results;
    
    // Parity of current thread: odd or even
    final int parity;

    FightArea(BlockingDeque<Fight> fights, BlockingQueue<Samurai> results, int parity) {
        this.fights = fights;
        this.results = results;
        this.parity = parity;
    }

    @Override
    public void run() {
        while (true) {
            try {
                // Get fight.
                Fight current;
                synchronized (fights) {
                    if (fights.isEmpty()) break;
                    current = ((parity % 2 == 0) ? fights.pollFirst() : fights.pollLast());
                }

                // Log.
                String message = current.getFirst() + " vs " + current.getSecond() + ": ";

                // Execute and emulate fight.
                runFight(current);

                // Log.
                message += current.getWinner() + " from " + current.getWinner().getTeam() + " wins.";
                System.out.println(message);

                // Return result
                results.put(current.getWinner());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    void runFight(Fight fight) throws InterruptedException {
        while (fight.getLooser().getEnergy() > Samurai.minEnergy) {
            // Reduce upt to 50% energy.
            Samurai victim = (random.nextInt(2) == 0 ? fight.getFirst() : fight.getSecond());
            int reduceEnergy = random.nextInt((Samurai.maxEnergy - Samurai.minEnergy) / 4);
            victim.setEnergy(victim.getEnergy() - reduceEnergy);

            // Emulate fight.
            sleep(reduceEnergy);
        }
    }
}
