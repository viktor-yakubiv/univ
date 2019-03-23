import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class BusProblem {
    // List of all available cities (for random generator)
    // In this example - all capitals in the world
    final static String[] cityList = {
        "Abu Dhabi",
        "Abuja",
        "Accra",
        "Adamstown",
        "Addis Ababa",
        "Algiers",
        "Alofi",
        "Amman",
        "Amsterdam",
        "Andorra la Vella",
        "Ankara",
        "Antananarivo",
        "Apia",
        "Ashgabat",
        "Asmara",
        "Astana",
        "Asuncion",
        "Athens",
        "Avarua",
        "Baghdad",
        "Baku",
        "Bamako",
        "Bandar Seri Begawan",
        "Bangkok",
        "Bangui",
        "Banjul",
        "Basse-Terre",
        "Basseterre",
        "Beijing",
        "Beirut",
        "Belgrade",
        "Belmopan",
        "Berlin",
        "Bern",
        "Bishkek",
        "Bissau",
        "Bogotá",
        "Brasília",
        "Bratislava",
        "Brazzaville",
        "Bridgetown",
        "Brussels",
        "Bucharest",
        "Budapest",
        "Buenos Aires",
        "Bujumbura",
        "Cairo",
        "Canberra",
        "Caracas",
        "Castries",
        "Cayenne",
        "Charlotte Amalie",
        "Chişinău",
        "Cockburn Town",
        "Conakry",
        "Copenhagen",
        "Dakar",
        "Damascus",
        "Dhaka",
        "Dili",
        "Djibouti",
        "Dodoma (official, legislative)",
        "Doha",
        "Douglas",
        "Dublin",
        "Dushanbe",
        "East Jerusalem (declared)",
        "Ramallah (de facto)",
        "Edinburgh of the Seven Seas",
        "Laayoune (declared)",
        "Tifariti (de facto)",
        "Episkopi Cantonment",
        "Flying Fish Cove",
        "Fort-de-France",
        "Freetown",
        "Funafuti",
        "Gaborone",
        "George Town",
        "Georgetown",
        "Georgetown",
        "Gibraltar",
        "Guatemala City",
        "Gustavia",
        "Hagåtña",
        "Hamilton",
        "Hanga Roa",
        "Hanoi",
        "Harare",
        "Hargeisa",
        "Havana",
        "Helsinki",
        "Hong Kong",
        "Honiara",
        "Islamabad",
        "Jakarta",
        "Jamestown",
        "Jerusalem (declared, de facto)",
        "Juba",
        "Kabul",
        "Kampala",
        "Kathmandu",
        "Khartoum",
        "Kiev",
        "Kigali",
        "King Edward Point",
        "Kingston",
        "Kingston",
        "Kingstown",
        "Kinshasa",
        "Kuala Lumpur (official, legislative and royal)",
        "Putrajaya (administrative and judicial)",
        "Kuwait City",
        "Libreville",
        "Lilongwe",
        "Lima",
        "Lisbon",
        "Ljubljana",
        "Lomé",
        "London",
        "Luanda",
        "Lusaka",
        "Luxembourg",
        "Madrid",
        "Majuro",
        "Malabo",
        "Malé",
        "Mamoudzou",
        "Managua",
        "Manama",
        "Manila",
        "Maputo",
        "Marigot",
        "Maseru",
        "Mata-Utu",
        "Mbabane (administrative)",
        "Lobamba (royal and legislative)",
        "Mexico City",
        "Minsk",
        "Mogadishu",
        "Monaco",
        "Monrovia",
        "Montevideo",
        "Moroni",
        "Moscow",
        "Muscat",
        "Nairobi",
        "Nassau",
        "Naypyidaw",
        "N'Djamena",
        "New Delhi",
        "Ngerulmud",
        "Niamey",
        "Nicosia",
        "Nicosia",
        "Nouakchott",
        "Nouméa",
        "Nukuʻalofa",
        "Nuuk",
        "Oranjestad",
        "Oslo",
        "Ottawa",
        "Ouagadougou",
        "Pago Pago",
        "Palikir",
        "Panama City",
        "Papeete",
        "Paramaribo",
        "Paris",
        "Philipsburg",
        "Phnom Penh",
        "Plymouth (official)",
        "Brades Estate (de facto)",
        "Podgorica (official)",
        "Cetinje (Old Royal Capital, present seat of the President)",
        "Port Louis",
        "Port Moresby",
        "Port Vila",
        "Port-au-Prince",
        "Port of Spain",
        "Porto-Novo (official)",
        "Cotonou (de facto)",
        "Prague",
        "Praia",
        "Pretoria (executive)",
        "Bloemfontein (judicial)",
        "Cape Town (legislative)",
        "Pristina",
        "Pyongyang",
        "Quito",
        "Rabat",
        "Reykjavík",
        "Riga",
        "Riyadh",
        "Road Town",
        "Rome",
        "Roseau",
        "Saint-Denis",
        "Saipan",
        "San José",
        "San Juan",
        "San Marino",
        "San Salvador",
        "Sana'a",
        "Santiago",
        "Santo Domingo",
        "São Tomé",
        "Sarajevo",
        "Seoul",
        "Singapore",
        "Skopje",
        "Sofia",
        "Sri Jayawardenepura Kotte (official)",
        "Colombo (former capital; has some government offices)",
        "St. George's",
        "St. Helier",
        "St. John's",
        "St. Peter Port",
        "St. Pierre",
        "Stanley",
        "Stepanakert",
        "Stockholm",
        "Sucre (constitutional)",
        "La Paz (administrative)",
        "Sukhumi",
        "Suva",
        "Taipei",
        "Tallinn",
        "Tarawa Atoll",
        "Tashkent",
        "Tbilisi (official)",
        "Kutaisi (legislative)",
        "Tegucigalpa",
        "Tehran",
        "Thimphu",
        "Tirana",
        "Tiraspol",
        "Tokyo",
        "Tórshavn",
        "Tripoli",
        "Tskhinvali",
        "Tunis",
        "Ulaanbaatar",
        "Vaduz",
        "Valletta",
        "The Valley",
        "Vatican City",
        "Victoria",
        "Vienna",
        "Vientiane",
        "Vilnius",
        "Warsaw",
        "Washington, D.C.",
        "Wellington",
        "West Island",
        "Willemstad",
        "Windhoek",
        "Yamoussoukro (official)",
        "Abidjan (former capital; still has many government offices)",
        "Yaoundé",
        "Yaren (de facto)",
        "Yerevan",
        "Zagreb"
    };

    // Maximal and minimal value of one road in map (for random generator)
    final static int maxRoadValue = 10000;
    final static int minRoadValue = 100;

    // Maximal sleep time of one emulation process (for random generator)
    final static int maxSleepTime = 1000;


    // Map of roads
    RoadMap roadMap = new RoadMap();

    // Locker for safe processing of workers
    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    // Workers
    Thread[] workers = new Thread[3];

    Random generator = new Random(System.currentTimeMillis());


    public BusProblem() {
        // Create workers
        workers[0] = new RoadWorker(this::addRandomCity);
        workers[1] = new RoadWorker(this::removeRandomCity);
        workers[2] = new RoadWorker(this::changeRandomRoute);
        workers[3] = new RoadWorker(this::outputRandomPath);

        // Generate random count of random routes.
        int routesCount = generator.nextInt(1000);
        for (int i = 0; i < routesCount; ++i) {
            int startPoint = generator.nextInt(cityList.length);
            int endPoint;
            do endPoint = generator.nextInt(cityList.length); while (endPoint == startPoint);

            // Add points to available
            roadMap.addPoint(cityList[startPoint]);
            roadMap.addPoint(cityList[endPoint]);

            // Add route
            roadMap.put(new Road(cityList[startPoint], cityList[endPoint]),
                    generator.nextInt(maxRoadValue - minRoadValue) + minRoadValue);
        }
    }


    void randomSleep() {
        try {
            Thread.sleep(generator.nextInt(maxSleepTime));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void addRandomCity() {
        lock.writeLock().lock();

        // Get list of points.
        ArrayList<String> availablePoints = new ArrayList<>(roadMap.getAvailablePoints());

        if (!availablePoints.isEmpty()) {
            // Remove random city.
            String city;
            do {
                city = availablePoints.get(generator.nextInt(availablePoints.size()));
            }
            while (!roadMap.addPoint(city));

            // Emulate work.
            randomSleep();

            // Log.
            System.out.println(city + " was added to routes.");
        }

        lock.writeLock().unlock();
        Thread.yield();
    }

    void removeRandomCity() {
        lock.writeLock().lock();

        // Get list of points.
        ArrayList<String> availablePoints = new ArrayList<>(roadMap.getAvailablePoints());

        if (!availablePoints.isEmpty()) {
            // Remove random city.
            String city;
            do {
                city = availablePoints.get(generator.nextInt(availablePoints.size()));
            }
            while (!roadMap.removePoint(city));

            // Emulate work.
            randomSleep();

            // Log.
            System.out.println(city + " was removed from routes.");
        }

        lock.writeLock().unlock();
        Thread.yield();
    }

    void addRandomRoute() {
        lock.writeLock().lock();

        // All available points.
        ArrayList<String> availablePoints = new ArrayList<>(roadMap.getAvailablePoints());

        if (!availablePoints.isEmpty()) {
            // Generate different points.
            int startPoint = generator.nextInt(availablePoints.size());
            int endPoint;
            do endPoint = generator.nextInt(availablePoints.size()); while (endPoint == startPoint);

            // Generate value.
            int value = generator.nextInt(maxRoadValue - minRoadValue) + minRoadValue;

            // Generate route.
            Road road = new Road(availablePoints.get(startPoint), availablePoints.get(endPoint));
            roadMap.put(road, value);

            // Log.
            System.out.println(road + " with way cost: $" + value + " has been built.");
        }

        // Emulate work.
        randomSleep();

        lock.writeLock().unlock();
        Thread.yield();
    }

    void removeRandomRoute() {
        lock.writeLock().lock();

        // All available points.
        ArrayList<Road> routes = new ArrayList<>(roadMap.keySet());

        if (!routes.isEmpty()) {
            // Delete route.
            Road road = routes.get(generator.nextInt(roadMap.size()));
            roadMap.remove(road);

            // Log.
            System.out.println(road + " has been broken.");
        } else Thread.yield();

        // Emulate work.
        randomSleep();

        lock.writeLock().unlock();
    }

    void changeRandomRoute() {
        if (generator.nextBoolean()) addRandomRoute();
        else removeRandomRoute();
    }

    void outputRandomPath() {
        lock.readLock().lock();

        // Get list of points.
        ArrayList<String> availablePoints = new ArrayList<>(roadMap.getAvailablePoints());

        if (!availablePoints.isEmpty()) {
            // Generate different points.
            int startPoint = generator.nextInt(availablePoints.size());
            int endPoint;
            do endPoint = generator.nextInt(availablePoints.size()); while (endPoint == startPoint);

            // Get path.
            List<RoadMap.Entry> path =
                    roadMap.getPath(availablePoints.get(startPoint), availablePoints.get(endPoint));

            // Log.
//        for (RoadMap.Entry route : path) {
//            Road road = (Road) route.getKey();
//            if (road.startPoint == )
//        }

            // Emulate work.
            randomSleep();
        }

        lock.readLock().unlock();
        Thread.yield();
    }


    public synchronized void start() {
        for (Thread worker : workers) worker.start();
    }

    public void interrupt() {
        for (Thread worker : workers) worker.interrupt();
    }


    public static void main(String[] args) {
        BusProblem problem = new BusProblem();
        problem.start();

        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        problem.interrupt();
    }
}

class Road {
    public final String startPoint, endPoint;

    public Road(String startPoint, String endPoint) {
        if (startPoint.compareTo(endPoint) < 0) {
            String swapPoint = startPoint;
            startPoint = endPoint;
            endPoint = swapPoint;
        }

        this.startPoint = startPoint;
        this.endPoint = endPoint;
    }

    public boolean hasPoint(String point) {
        return (startPoint.equals(point) || endPoint.equals(point));
    }

    @Override
    public String toString() {
        return "Road between " + startPoint + " and " + endPoint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Road road = (Road) o;

        if (startPoint != null ? !startPoint.equals(road.startPoint) : road.startPoint != null) return false;
        return endPoint != null ? endPoint.equals(road.endPoint) : road.endPoint == null;

    }

    @Override
    public int hashCode() {
        int result = startPoint != null ? startPoint.hashCode() : 0;
        result = 31 * result + (endPoint != null ? endPoint.hashCode() : 0);
        return result;
    }
}


class RoadMap extends HashMap<Road, Integer> {
    Set<String> availablePoints = new HashSet<>();

    public boolean addPoint(String point) {
        return availablePoints.add(point);
    }

    public boolean removePoint(String point) {
        boolean wasAvailable = availablePoints.remove(point);
        if (!wasAvailable) return false;

        ArrayList<Road> toRemove =
                keySet().stream().filter(key -> key.hasPoint(point)).collect(Collectors.toCollection(ArrayList::new));
        toRemove.forEach(this::remove);

        return true;
    }

    public Set<String> getAvailablePoints() {
        return availablePoints;
    }

    public List<Entry> getPath(String fromPoint, String toPoint) {
        List<Entry> path = new ArrayList<>();

        if (!getPath(fromPoint, toPoint, path)) path = null;
        else Collections.reverse(path);

        return path;
    }

    private boolean getPath(String fromPoint, String toPoint, List<Entry> path) {
        // Check for simple way.
        Road road = new Road(fromPoint, toPoint);
        Integer value = get(road);
        if (value != null) {
            path.add(new AbstractMap.SimpleEntry<Road, Integer>(road, value));
            return true;
        }

        // Look up direct path.
        for (Road key : keySet()) {
            if (key.hasPoint(fromPoint) &&
                    getPath((key.startPoint.equals(fromPoint) ? key.endPoint : key.startPoint), toPoint, path)) {
                path.add(new AbstractMap.SimpleEntry<Road, Integer>(key, get(road)));
                return true;
            }
        }

        // No way.
        return false;
    }
}

class RoadWorker extends Thread {
    public RoadWorker(Runnable target) {
        super(target);
    }

    @Override
    public void run() {
        while (!isInterrupted()) super.run();
    }
}
