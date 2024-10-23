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

    private static Map<String, ArrayList<int[]>> categoryMap = new HashMap<>();
    private static final int GRID_SIZE_X = 4;
    private static final int GRID_SIZE_Y = 4;
    private static ArrayList<ArrayList<String>> grid;
    private static ArrayList<ArrayList<String>> originalGrid;  // Stores the original state of the grid
    private static int[] agentPosition = new int[]{1, 1};
    private static int movementsMade = 0;
    private static boolean isAlive = true;
    private static Map<String, Boolean> sensor = new HashMap<>();
    private static Map<int[], String> comb = new HashMap<>();
    private static boolean won = false;
    private static ArrayList<LocationInfo> agentMap = new ArrayList<>(); //The agent's map that they use to keep track of spaces that they previously traveled to

    public static void main(String[] args) {
        String filePath = "testworld.txt";
        String filePath2 = "testworld2.txt";
        String filePath3 = "testworld3.txt";

        parseFile(filePath);
        printWorldInformation();
        initializeGrid();

        // Set legends from the parsed data
        setEnvironment();
        saveOriginalGrid();

        initializeAgent();
        printGrid();
        KB();
        System.out.println();
        startAgent_random();
    }

    private static void KB() {
        int x = GRID_SIZE_X;
        int y = GRID_SIZE_Y;
        int[] safePlace = new int[]{1, 1};

        String KB = x + "\n" + y + "\n" + Arrays.toString(safePlace) + "\n";
        System.out.println(KB);
    }

    private static void startAgent_random() {
        Random rand = new Random();
        int validMoves = 0;  // Track valid moves only
        addToAgentMap(agentPosition.clone());

        while (validMoves < 10) {
            int randDirection = rand.nextInt(4);
    
            if (!isAlive) return;  // Stop if the agent is dead
    
            boolean moved = false;  // Flag to check if the agent successfully moved
    
            switch (randDirection) {
                case 0: 
                    System.out.println("Agent moves up"); 
                    moved = moveAgent("up");  // Returns true if the agent moves
                    break;
                case 1: 
                    System.out.println("Agent moves down"); 
                    moved = moveAgent("down"); 
                    break;
                case 2: 
                    System.out.println("Agent moves left"); 
                    moved = moveAgent("left"); 
                    break;
                case 3: 
                    System.out.println("Agent moves right"); 
                    moved = moveAgent("right"); 
                    break;
                default: 
                    System.out.println("Invalid direction");
            }
    
            if (moved) {  // Only count valid moves
                validMoves++;
                testLogicalProofs();  // Run tests after each valid move
            }

            if (won) {
                System.out.println("Agent achieved its objective.");
                break;
            }
    
            try {
                Thread.sleep(1000);  // Pause for 1 second
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void testLogicalProofs() {
        if (isAlive) {
            if (logicalProof_breezeImpliesPit(sensor_Breeze(agentPosition))) {
                System.out.println("Logical Conclusion: Breeze detected, there must be a pit nearby.\n");
            } else {
                System.out.println("Logical Conclusion: No breeze detected, no nearby pit.\n");
            }
            if (logicalProof_stenchImpliesWumpus(sensor_Stench(agentPosition))) {
                System.out.println("Logical Conclusion: Stench detected, there must be a Wumpus nearby.\n");
            } else {
                System.out.println("Logical Conclusion: No stench detected, no nearby Wumpus.\n");
            }
        } else {
            System.out.println("Agent is dead.");
        }
    }

    private static boolean logicalProof_breezeImpliesPit(boolean breezeDetected) {
        return breezeDetected;
    }

    private static boolean logicalProof_stenchImpliesWumpus(boolean stenchDetected) {
        return stenchDetected;
    }

    private static void initializeAgent() {
        setLegend("A", agentPosition);
        System.out.println("Sensor: \n" + agentSensor(sensor_Wumpus(agentPosition), sensor_Pit(agentPosition), sensor_Breeze(agentPosition)));
    }

    private static Boolean agent_state() {
        return isAlive;
    }

    private static Boolean agent_dead() {
        isAlive = false;
        return isAlive;
    }

    private static boolean moveAgent(String direction) {
        clearPosition(agentPosition);  // Clear current agent position from the grid
        printAgentMap();

        if (!isAlive) {
            System.out.println("Agent is dead, no more moves!");
            return false;  // No move made if the agent is dead
        }
    
        // Track the original position before the move
        int originalX = agentPosition[0];
        int originalY = agentPosition[1];
    
        // Move the agent based on the direction
        switch (direction.toLowerCase()) {
            case "up": 
                if (agentPosition[0] > 1) {
                    agentPosition[0]--; 
                } else {
                    System.out.println("Agent can't move up, it's at the top boundary!\n");
                    return false;  // Invalid move
                }
                break;
            case "down": 
                if (agentPosition[0] < GRID_SIZE_Y) {
                    agentPosition[0]++; 
                } else {
                    System.out.println("Agent can't move down, it's at the bottom boundary!\n");
                    return false;  // Invalid move
                }
                break;
            case "left": 
                if (agentPosition[1] > 1) {
                    agentPosition[1]--; 
                } else {
                    System.out.println("Agent can't move left, it's at the left boundary!\n");
                    return false;  // Invalid move
                }
                break;
            case "right": 
                if (agentPosition[1] < GRID_SIZE_X) {
                    agentPosition[1]++; 
                } else {
                    System.out.println("Agent can't move right, it's at the right boundary!\n");
                    return false;  // Invalid move
                }
                break;
            default: 
                System.out.println("Invalid direction.\n");
                return false;  // Invalid direction
        }
        addToAgentMap(agentPosition.clone());
        // If the agent's position did not change due to boundaries, skip the rest of the method
        if (agentPosition[0] == originalX && agentPosition[1] == originalY) {
            setLegend("A", agentPosition);  // Re-add the agent to its original position
            return false;  // No move made
        }
    
        // Update the sensor values for the new position
        agentSensor(sensor_Wumpus(agentPosition), sensor_Pit(agentPosition), sensor_Breeze(agentPosition));
        System.out.println("Sensor: \n" + sensor);
    
        // Check if agent encountered Wumpus or Pit
        if (sensor_Wumpus(agentPosition)) {
            System.out.println("The agent encountered The Wumpus!");
            setLegend("A", agentPosition);  // Mark the position with Wumpus killed Agent
            agent_dead();
            printGrid();
            return true;  // A move was made, but the agent is now dead
        }
    
        if (sensor_Pit(agentPosition)) {
            System.out.println("The agent fell into an Endless Pit!");
            setLegend("A", agentPosition);  // Mark the position with Pit killed Agent
            agent_dead();
            printGrid();
            return true;  // A move was made, but the agent is now dead
        }

        if (sensor_Gold(agentPosition)) {
            System.out.println("The agent found the Gold! Glittery room, and objective achieved!");
            setLegend("A", agentPosition);  // Add agent to the new position
            setLegend("Gl", agentPosition);  // Add glitter to the room
            printGrid();  // Print the updated grid
            won = true;
            return true;  // The objective is achieved, agent can stop further exploration
        }
    
        // Place the agent in the new position, if still alive
        if (isAlive) {
            setLegend("A", agentPosition);  // Add agent to the new position
        }
    
        printGrid();  // Print the updated grid
        return true;  // A valid move was made
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

    private static boolean sensor_Stench(int[] position) {
        ArrayList<int[]> wumpusCoordinates = categoryMap.get("wumpus");
        for (int[] wumpusCoord : wumpusCoordinates) {
            if ((Math.abs(wumpusCoord[0] - position[0]) == 1 && wumpusCoord[1] == position[1]) || 
                (Math.abs(wumpusCoord[1] - position[1]) == 1 && wumpusCoord[0] == position[0])) {
                return true;
            }
        }
        return false;
    }

    private static boolean sensor_Breeze(int[] position) {
        ArrayList<int[]> pitCoordinates = categoryMap.get("pit");
        for (int[] pitCoord : pitCoordinates) {
            if ((Math.abs(pitCoord[0] - position[0]) == 1 && pitCoord[1] == position[1]) || 
                (Math.abs(pitCoord[1] - position[1]) == 1 && pitCoord[0] == position[0])) {
                return true;
            }
        }
        return false;
    }

    private static boolean sensor_Gold(int[] position) {
        ArrayList<int[]> goldCoordinates = categoryMap.get("gold");
        for (int[] goldCoord : goldCoordinates) {
            if (Arrays.equals(position, goldCoord)) {
                return true;
            }
        }
        return false;
    }

    private static Map<String, Boolean> agentSensor(boolean checkForWumpus, boolean checkForPit, boolean checkForBreeze) {
        boolean sensor_Wumpus = checkForWumpus;
        boolean sensor_Pit = checkForPit;
        boolean sensor_Breeze = checkForBreeze;
        sensor.put("sensor_Wumpus", sensor_Wumpus);
        sensor.put("sensor_Pit", sensor_Pit);
        sensor.put("sensor_Breeze", sensor_Breeze);
        return sensor;
    }

    // Clear position and restore original environment markers
    private static void clearPosition(int[] position) {
        int x = position[0] - 1;
        int y = position[1] - 1;

        // Restore the original state from the originalGrid
        grid.get(x).set(y, originalGrid.get(x).get(y));
    }

    // Save the original grid state for restoration later
    private static void saveOriginalGrid() {
        originalGrid = new ArrayList<>();
        for (ArrayList<String> row : grid) {
            ArrayList<String> originalRow = new ArrayList<>(row);
            originalGrid.add(originalRow);
        }
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

    private static void initializeGrid() {
        grid = new ArrayList<>();
        for (int i = 1; i <= GRID_SIZE_X; i++) {
            ArrayList<String> row = new ArrayList<>();
            for (int j = 1; j <= GRID_SIZE_Y; j++) {
                row.add("((" + i + "," + j + "))");  
            }
            grid.add(row);
        }
    }

    private static void addToAgentMap(int[] location) {
        boolean hasBreeze = sensor_Breeze(location);
        boolean hasStench = sensor_Stench(location);
        
        for (LocationInfo info : agentMap){
            if (Arrays.equals(location, info.getCoordinates())){
                return;
            }
        }
        agentMap.add(new LocationInfo(location.clone(), hasBreeze, hasStench));
    }

    private static Boolean checkAgentMap(int[] location){
        for (LocationInfo info : agentMap){
            if (Arrays.equals(info.getCoordinates(), location)){
                return true;
            }
        }
        return false;
    }

    private static void printAgentMap() {
        System.out.print("Agent Map: ");
        for (LocationInfo info : agentMap){
            System.out.print(info.toString());
        }
        System.out.println("\n");
    }

    private static void printGrid() {
        for (ArrayList<String> row : grid) {
            for (String cell : row) {
                System.out.print(cell + "\t");
            }
            System.out.println();
        }
        System.out.println("movementsMade:" + movementsMade);
        movementsMade++;
    }

    private static void setLegend(String item, int[] coordinate) {
        int x = coordinate[0] - 1;
        int y = coordinate[1] - 1;
        
        // Fetch the current items at the coordinate if any
        String currentLegend = grid.get(x).get(y);
        
        // If the room is empty (no legend set yet), initialize it with the current item
        if (currentLegend.contains("))")) {
            grid.get(x).set(y, "((" + coordinate[0] + "," + coordinate[1] + ") [" + item + "])");
        } 
        else {
            // If there are already items in the room, append the new item
            String newLegend = currentLegend.replace("])", "," + item + "])"); // Replace closing brackets with new item
            grid.get(x).set(y, newLegend);
        }
    }
    

    private static void setEnvironment() {
        for (int[] wumpusCoord : categoryMap.get("wumpus")) {
            setLegend("W", wumpusCoord);
        }

        for (int[] goldCoord : categoryMap.get("gold")) {
            setLegend("G", goldCoord);
        }

        for (int[] goldCoord : categoryMap.get("gold")) {
            setLegend("Gl", goldCoord);
        }

        for (int[] pitCoord : categoryMap.get("wumpus")) {
            if (pitCoord[0] > 1) 
                setLegend("S", new int[]{pitCoord[0] - 1, pitCoord[1]});
            if (pitCoord[0] < GRID_SIZE_Y) 
                setLegend("S", new int[]{pitCoord[0] + 1, pitCoord[1]});
            if (pitCoord[1] > 1) 
                setLegend("S", new int[]{pitCoord[0], pitCoord[1] - 1});
            if (pitCoord[1] < GRID_SIZE_X) 
                setLegend("S", new int[]{pitCoord[0], pitCoord[1] + 1});
        }

        for (int[] pitCoord : categoryMap.get("pit")) {
            setLegend("P", pitCoord);
        }

        for (int[] pitCoord : categoryMap.get("pit")) {
            if (pitCoord[0] > 1) 
                setLegend("B", new int[]{pitCoord[0] - 1, pitCoord[1]});
            if (pitCoord[0] < GRID_SIZE_Y) 
                setLegend("B", new int[]{pitCoord[0] + 1, pitCoord[1]});
            if (pitCoord[1] > 1) 
                setLegend("B", new int[]{pitCoord[0], pitCoord[1] - 1});
            if (pitCoord[1] < GRID_SIZE_X) 
                setLegend("B", new int[]{pitCoord[0], pitCoord[1] + 1});
        }
    }

    private static void parseFile(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ");
                String category = parts[0];
                int[] coordinates = new int[]{Integer.parseInt(parts[1]), Integer.parseInt(parts[2])};

                categoryMap.putIfAbsent(category, new ArrayList<>());
                categoryMap.get(category).add(coordinates);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int[] getCoordinate(String category, int index) {
        if (categoryMap.containsKey(category) && index < categoryMap.get(category).size()) {
            return categoryMap.get(category).get(index);
        } else {
            return new int[]{-1, -1};
        }
    }

    private static int whereToMove() {
        ArrayList<Integer> possibleMoves = new ArrayList<>();
        ArrayList<Integer> dangerousMoves = new ArrayList<>();
        possibleMoves.add(1);
        possibleMoves.add(2);
        possibleMoves.add(3);
        possibleMoves.add(4);

        //Handle if Agent is Against a Wall
        if (agentPosition[0] == 1) {
            //remove possibility to move up
            possibleMoves.removeIf(direction -> direction.equals(0));

        }
        if (agentPosition[0] == 4) {
            //remove possibility to move down
            possibleMoves.removeIf(direction -> direction.equals(1));
        }
        if (agentPosition[1] == 1) {
            //remove possibility to move left
            possibleMoves.removeIf(direction -> direction.equals(2));
        }
        if (agentPosition[1] == 4) {
            //remove possibility to move right
            possibleMoves.removeIf(direction -> direction.equals(3));
        }
        //Handle if Agent Feels a Breeze
        if (sensor_Breeze(agentPosition)){
            //Check if spaces adjacent to it were previously traveled to
                //Eliminate spaces that have been traveled to from "dangerous pits"
            dangerousMoves = avoidDangerousSpaces(dangerousMoves, "Pit");
            //Check to see if spaces that are inferentially dangerous have been traveled to and were/weren't dangerous
                //Eliminate spaces accordingly
            ArrayList<int[]> possiblePits = new ArrayList<>();
            possiblePits.add(new int[] {agentPosition[0]+1, agentPosition[1]});
            possiblePits.add(new int[] {agentPosition[0]-1, agentPosition[1]});
            possiblePits.add(new int[] {agentPosition[0], agentPosition[1]+1});
            possiblePits.add(new int[] {agentPosition[0], agentPosition[1]-1});
            possiblePits.removeIf(pit -> agentMap.contains(pit));

            ArrayList<int[]> potentialBreezes = new ArrayList<>();
            potentialBreezes.add(new int[] {agentPosition[0]+2, agentPosition[1]});
            potentialBreezes.add(new int[] {agentPosition[0]+1, agentPosition[1]-1});
            potentialBreezes.add(new int[] {agentPosition[0]+1, agentPosition[1]+1});

            potentialBreezes.add(new int[] {agentPosition[0]-2, agentPosition[1]});
            potentialBreezes.add(new int[] {agentPosition[0]-1, agentPosition[1]+1});
            potentialBreezes.add(new int[] {agentPosition[0]-1, agentPosition[1]-1});
            
            potentialBreezes.add(new int[] {agentPosition[0], agentPosition[1]+2});
            potentialBreezes.add(new int[] {agentPosition[0], agentPosition[1]-2});

            //Start storing Breeze and Stench info in the agentMap
            //If we have been to x+2 and there was no breeze, then there is no pit downward
            //If we have been to x-2 and there was no breeze, then there is no pit upwards
            //if we have been to y+2 and there was no breeze, then there is no pit to the right
            //if we have been to y-2 and there was no breeze, then there is no pit to the left



            //Move to spaces that were deemed safe

            //if no spaces were deemed safe, check reroute algorithm

            //if no reroute possible, make risky decision

        }
        //Handle if Agent Smells a Stench
        if (sensor_Stench(agentPosition)){
            avoidDangerousSpaces(dangerousMoves, "Wumpus");
        }

        //See if there are any possible safe moves left
        for (int dangerMove : dangerousMoves){
            possibleMoves.removeIf(move -> move.equals(dangerMove));
        }

        //Handle if Agent Must Reroute
            //if all possible routes from this point are dangerous
        //Handle if Agent must take a risk
            //if all possible routes from all points are dangerous
        if (possibleMoves.isEmpty()){
            if (rerouteCheck()){
                rerouteAlgorithm();
            }
            else{
                riskyDecisionAlgorithm(possibleMoves);
            }
        }

        return 0;
    }

    private static ArrayList<Integer> avoidDangerousSpaces(ArrayList<Integer> dangerousMoves, String dangerType) {
        // Logic to avoid spaces based on dangerType (e.g., pits or Wumpus)
        //Pits
            //If we have previously been to a place that could've been a source pit for the breeze then we know that it is not
            //If we have previously been to a place that could've been a breeze spot depending on the source pit for this breeze, then we can eliminate the possible source pit from contention
        if (dangerType.equals("Pit")){ 
            for (LocationInfo info : agentMap){
                int[] currentLocation = info.getCoordinates();

                int[][] adjacentLocations = {
                    {currentLocation[0] - 1, currentLocation[1]}, //Up
                    {currentLocation[0] + 1, currentLocation[1]}, //Down
                    {currentLocation[0], currentLocation[1] - 1}, //Left
                    {currentLocation[0], currentLocation[1] + 1} //Right
                }
                
                //If agent feels a breeze, check agent map to see if we have been to potential pit spaces
                for (int i = 0; i< adjacentLocations.length; i++){
                    int[] adjLocation = adjacentLocations[i];

                    if (adjLocation[0] < 1 || adjLocation[0] > GRID_SIZE_X || adjLocation[1] < 1 || adjLocation[1] > GRID_SIZE_Y){
                        continue;
                    }

                    boolean visited = checkAgentMap(adjLocation);

                    if (!visited) {
                        int moveDirection = getMoveDirection(currentLocation, adjLocation);
                        if (!dangerousMoves.contains(moveDirection)){
                            dangerousMoves.add(moveDirection);
                        }

                    }
                }
                //if agent feels a breeze, check if we have been to potential breeze spaces
                
            }
            return dangerousMoves;
        }
        
        //Wumpus
            //If we have previously been to a place that could've been a source wumpus for the stench then we know that it is not
            //If we have previously been to a place that could've been a stench spot depending on the source wumpus for this stench, then we can eliminate the possible surce wumpus from contention
            //If we have already deduced which space belongs to the wumpus, then that is the only "possible source wumpus" space that we should avoid
        if (dangerType.equals("Wumpus")){ 

        }
        return dangerousMoves;
    }

    private static int getMoveDirection(int[] current, int[] adjacent) {
        if (adjacent[0] == current[0] + 1 && adjacent[1] == current[1]) {
            return 1; // Down
        } else if (adjacent[0] == current[0] - 1 && adjacent[1] == current[1]) {
            return 0; // Up
        } else if (adjacent[0] == current[0] && adjacent[1] == current[1] + 1) {
            return 3; // Right
        } else if (adjacent[0] == current[0] && adjacent[1] == current[1] - 1) {
            return 2; // Left
        }
        return -1; // Error or invalid move
    }
    

    private static boolean rerouteCheck() {
        return false;
    }

    private static int rerouteAlgorithm() {
        return -1;  // Placeholder return value for rerouting
    }
    
    private static boolean allMovesSeemDangerous(ArrayList<Integer> possibleMoves) {
        // Logic to check if all remaining moves appear dangerous
        return false;  // Placeholder - You can implement actual checks here
    }
    
    private static int riskyDecisionAlgorithm(ArrayList<Integer> possibleMoves) {
        // Logic for making a risky decision if all moves are dangerous
        Random rand = new Random();
        return possibleMoves.get(rand.nextInt(possibleMoves.size()));  // Placeholder risky decision
    }

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

