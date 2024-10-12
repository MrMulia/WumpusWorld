import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/* ACO494: Foundations of Artificial Intelligence
*  Project #1 Expert System
*  Authors (In alphabetical order):
*    @Aiden Cullinan
*    @Ali Alnassary
*    @Denysse Sevilla
*    @Omar Perez
*    @Vito Mulia (@Otiv)
*/

public class WumpusWorld {

    // HashMap to store categories and their corresponding lists of coordinates as int[] (x, y).
    private static Map<String, ArrayList<int[]>> categoryMap = new HashMap<>();
    private static final int GRID_SIZE_X = 4;
    private static final int GRID_SIZE_Y = 4;
    private static ArrayList<ArrayList<String>> grid;  // Change to store String
    private static int[] agentPosition = new int[]{1, 1};  // Start the agent at position (1,1)
    private static int movementsMade = 0;
    private static boolean isAlive = true;
    private static Map<String, Boolean> sensor = new HashMap<>();

    public static void main(String[] args) {
        String filePath = "testworld.txt"; // File path

        // Parse the file and fill categoryMap
        parseFile(filePath);
        
        printWorldInformation();

        initializeGrid();

        // Set legends from the parsed data
        for (int[] wumpusCoord : categoryMap.get("wumpus")) {
            setLegend("W", wumpusCoord);
        }
        for (int[] pitCoord : categoryMap.get("pit")) {
            setLegend("P", pitCoord);
        }
        for (int[] goldCoord : categoryMap.get("gold")) {
            setLegend("G", goldCoord);
        }

        //Breeze?
        for (int[] pitCoord : categoryMap.get("pit")) {
            // Check up (above the pit)
            if (pitCoord[0] > 1) {
                int[] breezeUp = new int[]{pitCoord[0] - 1, pitCoord[1]};
                setLegend("B", breezeUp);
            }

            // Check down (below the pit)
            if (pitCoord[0] < GRID_SIZE_Y) {
                int[] breezeDown = new int[]{pitCoord[0] + 1, pitCoord[1]};
                setLegend("B", breezeDown);
            }

            // Check left (left of the pit)
            if (pitCoord[1] > 1) {
                int[] breezeLeft = new int[]{pitCoord[0], pitCoord[1] - 1};
                setLegend("B", breezeLeft);
            }

            // Check right (right of the pit)
            if (pitCoord[1] < GRID_SIZE_X) {
                int[] breezeRight = new int[]{pitCoord[0], pitCoord[1] + 1};
                setLegend("B", breezeRight);
            }
        }

        // Print the final grid with legends
        initializeAgent();
        printGrid();
        startAgent();
    }

    private static String logicalProofs(String proof) {
        return "aa";
    }

    private static void startAgent() {
        Random rand = new Random();
        
        for (int i = 0; i < 10; i++) {
            int randDirection = rand.nextInt(4);

        switch (randDirection) {
            case 0:
                System.out.println("Agent moves up");
                moveAgent("up");
                break;
            case 1:
                System.out.println("Agent moves down");
                moveAgent("down");
                break;
            case 2:
                System.out.println("Agent moves left");
                moveAgent("left");
                break;
            case 3:
                System.out.println("Agent moves right");
                moveAgent("right");
                break;
            default:
                System.out.println("Invalid direction");
        }

        // Adding a small delay to observe the movements (optional)
        try {
            Thread.sleep(1000);  // Pause for 1 second
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    }

    private static void initializeAgent() {
        setLegend("A", agentPosition);
        String a_sens = agentSensor(sensor_Wumpus(agentPosition), sensor_Pit(agentPosition)).toString();
        System.out.println("Sensor: \n" + a_sens);
    }

    private static void moveAgent(String direction) {
        if (!isAlive) {
            System.out.println("Agent is dead, no more moves!");
            return;
        }

        clearPosition(agentPosition);
        switch (direction.toLowerCase()) {
            case "up":
                if (agentPosition[0] > 1) {
                    agentPosition[0] --;
                }
                break;
            case ("down"):
                if (agentPosition[0] < GRID_SIZE_Y) {
                    agentPosition[0] ++;
                }
                break;
            case ("left"):
                if (agentPosition[1] > 1) {
                    agentPosition[1] --;
                }
                break;
            case ("right"):
                if (agentPosition[1] < GRID_SIZE_X) {
                    agentPosition[1] ++;
                }
                break;
            default:
                System.out.println("Invalid direction.\n");
        }
        if (sensor_Wumpus(agentPosition)) {
            System.out.println("The agent encountered The Wumpus!");
            isAlive = false;
        } else {
            setLegend("A", agentPosition);
        }

        agentSensor(sensor_Wumpus(agentPosition), sensor_Pit(agentPosition));
        System.out.println("Sensor: \n" + sensor);
        printGrid();
    }

    private static Map<String, Boolean> agentSensor(boolean checkForWumpus, boolean checkForPit) {
        boolean sensor_Wumpus = checkForWumpus;
        boolean sensor_Pit = checkForPit;
        sensor.put("sensor_Wumpus", sensor_Wumpus);
        sensor.put("sensor_Pit", sensor_Pit);
        return sensor;
    }

    private static boolean sensor_Wumpus(int[] position) {
        ArrayList<int[]> wumpusCoordinate = categoryMap.get("wumpus");
        for (int[] wumpusCoord : wumpusCoordinate) {
            if (Arrays.equals(position, wumpusCoord)) {
                return true;
            }
        }
        return false;
    }

    private static boolean sensor_Pit(int[] position) {
        ArrayList<int[]> pitCoordinate = categoryMap.get("pit");
        for (int[] pitCoord : pitCoordinate) {
            if (Arrays.equals(position, pitCoord)) {
                return true;
            }
        }
        return false;
    }

    private static void clearPosition(int[] position) {
        int x = position[0] - 1;
        int y = position[1] - 1;
        grid.get(x).set(y, "((" + position[0] + "," + position[1] + ") E)");  // Reset to empty
    }

    private static void printWorldInformation() {
        System.out.println("###### World's Information #######");
        System.out.print("Legend Size: " + categoryMap.size() + " ");
        System.out.println(categoryMap.keySet() + "\n");

        printAllCoordinates("pit");
        for (int i = 0; i < categoryMap.get("pit").size(); i++) {
            int[] pitCoordinate = getCoordinate("pit", i);
            final int indexBalancer = i + 1;
            System.out.println("Pit #" + indexBalancer + " : (" + pitCoordinate[0] + ", " + pitCoordinate[1] + ")");
        }

        printAllCoordinates("wumpus");
        int[] wumpusCoordinate = getCoordinate("wumpus", 0);
        System.out.println("Wumpus: (" + wumpusCoordinate[0] + ", " + wumpusCoordinate[1] + ")");

        printAllCoordinates("gold");
        int[] goldCoordinate = getCoordinate("gold", 0);
        System.out.println("Gold: (" + goldCoordinate[0] + ", " + goldCoordinate[1] + ")");

        System.out.println("\n##################################\n");
    }

    // Initialize the grid with coordinates and default "E" for empty
    private static void initializeGrid() {
        grid = new ArrayList<>();
        for (int i = 1; i <= GRID_SIZE_X; i++) {
            ArrayList<String> row = new ArrayList<>();
            for (int j = 1; j <= GRID_SIZE_Y; j++) {
                row.add("((" + i + "," + j + ") E)");  // Initialize with coordinates and "E" for empty
            }
            grid.add(row);
        }
    }

    // Print the grid
    private static void printGrid() {
        for (ArrayList<String> row : grid) {
            for (String cell : row) {
                System.out.print(cell + "\t");
            }
            System.out.println();
        }
        System.out.println("movementsMade:" + movementsMade + "\n");
        movementsMade++;
    }

    // Set a legend (item) at the given coordinate
    private static void setLegend(String item, int[] coordinate) {
        int x = coordinate[0] - 1;  // Adjust for zero-based index
        int y = coordinate[1] - 1;
        grid.get(x).set(y, "((" + coordinate[0] + "," + coordinate[1] + ") " + item + ")");  // Set the item in the grid
    }

    // Parse the file and fill the HashMap
    private static void parseFile(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Split each line into parts (category and coordinates)
                String[] parts = line.split(" ");
                String category = parts[0];
                int[] coordinates = new int[]{Integer.parseInt(parts[1]), Integer.parseInt(parts[2])};

                // Add coordinates to the respective category list
                categoryMap.putIfAbsent(category, new ArrayList<>());
                categoryMap.get(category).add(coordinates);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Get coordinates for a given category and index
    private static int[] getCoordinate(String category, int index) {
        if (categoryMap.containsKey(category) && index < categoryMap.get(category).size()) {
            return categoryMap.get(category).get(index);
        } else {
            return new int[]{-1, -1}; // Return an invalid coordinate if not found
        }
    }

    // Print all coordinates for a specific category
    private static void printAllCoordinates(String category) {
        if (categoryMap.containsKey(category)) {
            System.out.print(category + ": ");
            for (int[] coordinate : categoryMap.get(category)) {
                System.out.print("(" + coordinate[0] + ", " + coordinate[1] + ") ");
            }
            System.out.println();
        } else {
            System.out.println("No coordinates found for category: " + category);
        }
    }
}
