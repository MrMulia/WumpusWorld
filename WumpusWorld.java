import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Scanner;

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
    private static final String kb = "kb.txt";

    public static void main(String[] args) {
        String filePath = "testworld.txt";
        String filePath2 = "testworld2.txt";
        String filePath3 = "testworld3.txt";

        parseFile(filePath3);
        printWorldInformation();
        initializeGrid();

        // Set legends from the parsed data
        setEnvironment();
        saveOriginalGrid();

        initializeAgent();
        printGrid();
        KB(kb);
        System.out.println();
        // CHOOSE YOUR AGENT
        //startAgent_random();
        startAgent_expert_v1();

        Scanner scanner = new Scanner(System.in);
        String choice;
        
        System.out.println("Do you want to test the agent's chamber knowledge? (Y/n) ");
        String reply = scanner.nextLine();  // Use nextLine() to capture the full line of input

        do {
            
            if (reply.equalsIgnoreCase("Y")) {
                // Prompt the user for coordinates to query
                System.out.println("Enter a coordinate to check the agent's knowledge:");
                System.out.print("x = ");
                int x = scanner.nextInt();  // Read integer for x
                System.out.print("y = ");
                int y = scanner.nextInt();  // Read integer for y
                scanner.nextLine();  // Consume the remaining newline
        
                // Check the status of the entered coordinates
                String result = checkChamberStatus(x, y);
                System.out.println(result);
        
                // Ask the user if they want to query more coordinates
                System.out.print("Do you want to check another coordinate? (Y/n): ");
                choice = scanner.nextLine();  // Capture user input for next choice
        
                if (!choice.equalsIgnoreCase("Y")) {
                    break;  // Exit the loop if user enters anything other than 'Y'
                }
            } else {
                break;  // Exit if the user chooses not to test the knowledge
            }
        } while (true);
        
        System.out.println("Exiting program...");
        
    }

    private static void KB(String kbFilePath) {
        Map<String, String> knowledgeBase = new HashMap<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(kbFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ", 2);
                if (parts.length == 2) {
                    String key = parts[0];
                    String value = parts[1];
                    knowledgeBase.put(key, value);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    
        if (knowledgeBase.containsKey("GRID_SIZE")) {
            String[] gridSize = knowledgeBase.get("GRID_SIZE").split(" ");
            int gridSizeX = Integer.parseInt(gridSize[0]);
            int gridSizeY = Integer.parseInt(gridSize[1]);
            System.out.println("Grid Size: " + gridSizeX + "x" + gridSizeY);
        }
    
        if (knowledgeBase.containsKey("SAFE_PLACE")) {
            String[] safePlace = knowledgeBase.get("SAFE_PLACE").split(" ");
            int safeX = Integer.parseInt(safePlace[0]);
            int safeY = Integer.parseInt(safePlace[1]);
            System.out.println("Safe Place: (" + safeX + "," + safeY + ")");
        }
    
        for (Map.Entry<String, String> entry : knowledgeBase.entrySet()) {
            if (entry.getKey().equals("MOVE")) {
                System.out.println("Agent Move: " + entry.getValue());
            }
        }
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
                    System.out.println("\nAgent moves up"); 
                    moved = moveAgent("up");  // Returns true if the agent moves
                    break;
                case 1: 
                    System.out.println("\nAgent moves down"); 
                    moved = moveAgent("down"); 
                    break;
                case 2: 
                    System.out.println("\nAgent moves left"); 
                    moved = moveAgent("left"); 
                    break;
                case 3: 
                    System.out.println("\nAgent moves right"); 
                    moved = moveAgent("right"); 
                    break;
                default: 
                    System.out.println("\nInvalid direction");
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

    private static void startAgent_expert_v1() {
        int validMoves = 0;  // Track valid moves only
        addToAgentMap(agentPosition.clone());
        
        while (!won && isAlive && validMoves < 10) {
            if (!isAlive) return;  // Stop if the agent is dead
            
            // Use logic to determine the best move based on KB, rules, and sensors
            String bestMove = determineBestMove();
    
            boolean moved = false;  // Flag to check if the agent successfully moved
    
            if (bestMove != null) {
                System.out.println("\nAgent moves " + bestMove);
                moved = moveAgent(bestMove);  // Move in the direction determined by the logic
            } else {
                System.out.println("No valid safe move found, attempting risky move.");
                ArrayList<Integer> dangerousMoves = getValidMoves();  // Get valid directions (e.g., up, down, left, right)
    
                if (!dangerousMoves.isEmpty()) {
                    int riskyMove = riskyDecisionAlgorithm(dangerousMoves);  // Pick a risky move
                    switch (riskyMove) {
                        case 0:
                            moved = moveAgent("up");
                            break;
                        case 1:
                            moved = moveAgent("down");
                            break;
                        case 2:
                            moved = moveAgent("left");
                            break;
                        case 3:
                            moved = moveAgent("right");
                            break;
                        default:
                            System.out.println("Error: Invalid risky move.");
                            moved = false;
                    }
                } else {
                    System.out.println("No risky moves possible, agent is stuck.");
                    return;  // If no risky moves are possible, stop the agent
                }
            }
    
            if (moved) {  // Only count valid moves
                validMoves++;
                testLogicalProofs();  // Run tests after each valid move
                exportWorldState("current_world_state.txt");
            }
    
            // Exit if agent wins or dies
            if (won || !isAlive) {
                break;
            }
    
            try {
                Thread.sleep(1000);  // Pause for 1 second
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    
        if (validMoves >= 10) {
            System.out.println("Maximum move limit reached. Exiting...");
        }
    }    

    public static void exportWorldState(String fileName) {
        int move = 0;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
    
            writer.write("### Wumpus World State ###\n");
            writer.write("Agent's Current Position: (" + agentPosition[0] + ", " + agentPosition[1] + ")\n");
            writer.write("Movements Made: " + movementsMade + "\n");
            writer.write("Agent State: " + agent_state() + "\n");
            writer.write("World Won: " + won + "\n\n");
    
            writer.write("### Grid State ###\n");
            for (int i = 0; i < grid.size(); i++) {
                for (int j = 0; j < grid.get(i).size(); j++) {
                    writer.write(grid.get(i).get(j) + "\t");
                }
                writer.write("\n");
            }
    
            writer.write("\n### Agent Knowledge ###\n");
            for (LocationInfo info : agentMap) {
                writer.write("\nMovement Number: " + move + "\n");
                writer.write(info.toString());
                int[] loc = info.getCoordinates();
                if (info.hasBreeze()) {
                    writer.write("Logical Conclusion: Breeze => Pit nearby.\n");
                }
                if (info.hasStench()) {
                    writer.write("Logical Conclusion: Stench => Wumpus nearby.\n");
                }
                writer.write("Probability of Pit: " + String.format( "%.2f", calculatePitProbability(loc[0], loc[1])) + "\n");
                writer.write("Probability of Wumpus: " + String.format( "%.2f", calculateWumpusProbability(loc[0], loc[1])) + "\n");
                move++;
            }
    
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
    
    private static String determineBestMove() {
        // Step 1: Evaluate surrounding rooms based on current sensors
        makeLogicalDeduction();
    
        // Step 2: Check the adjacent rooms for safety based on the current room's sensor data.
        ArrayList<String> safeMoves = new ArrayList<>();

        // Low Risk Moves
        ArrayList<String> LRMoves = new ArrayList<>();
        // Medium Risk Moves
        ArrayList<String> MRMoves = new ArrayList<>();
        // High Risk Moves
        ArrayList<String> HRMoves = new ArrayList<>();
        // Absolute Death Moves
        ArrayList<String> ADMoves = new ArrayList<>();
    
        // Check each possible move direction, deducing safety based on the current room's sensor information
        if (isSafeToMove("up")) { safeMoves.add("up"); } 
        else { LRMoves.add("up"); }

        if (isSafeToMove("down")) { safeMoves.add("down"); } 
        else { LRMoves.add("down"); }

        if (isSafeToMove("left")) { safeMoves.add("left"); } 
        else { LRMoves.add("left"); }

        if (isSafeToMove("right")) { safeMoves.add("right"); } 
        else { LRMoves.add("right"); }
    
        if (!safeMoves.isEmpty()) {
            Random rand = new Random();
            int sM_size = safeMoves.size();
            return safeMoves.get(rand.nextInt(0, sM_size));
        }
        // Step 3: If no safe move is found, make a risky decision
        return null;  // Indicating that no safe moves are available
    }
    
    
    private static boolean isMoveSafe(String direction) {
        int[] newPosition = calculateNewPosition(direction);
    
        if (!isLocationValid(newPosition)) {
            return false;  // Move is invalid or dangerous
        }
    
        return true;  // Safe to move
    }

    private static boolean isSafeToMove(String direction) {
        // Calculate the new position based on the given direction
        int[] newPosition = calculateNewPosition(direction);
    
        // Check if the new position is within bounds (handle the grid boundary)
        if (!isWithinBounds(newPosition)) {
            return false;
        }
    
        // Deduce safety based on current sensors:
        // - Breeze implies a pit is nearby, so avoid moving into rooms adjacent to a pit
        if (sensor_Breeze(agentPosition)) {
            return false;  // If there's a breeze in the current room, adjacent rooms might have a pit
        }
        
        // - Stench implies the Wumpus is nearby, so avoid adjacent rooms
        if (sensor_Stench(agentPosition)) {
            return false;  // If there's a stench, adjacent rooms might have a Wumpus
        }
    
        // If none of the conditions apply, assume it's safe
        return true;
    }

    private static boolean isWithinBounds(int[] position) {
        int x = position[0];  // The row number
        int y = position[1];  // The column number
    
        if (x >= 1 && x <= GRID_SIZE_X && y >= 1 && y <= GRID_SIZE_Y) {
            return true;  // The position is within bounds
        } else {
            return false;  // The position is out of bounds
        }
    }    

    private static int[] calculateNewPosition(String direction) {
        int[] newPosition = agentPosition.clone();
    
        switch (direction.toLowerCase()) {
            case "up":
                newPosition[0]--;  // Move up, decrease the row number
                break;
            case "down":
                newPosition[0]++;  // Move down, increase the row number
                break;
            case "left":
                newPosition[1]--;  // Move left, decrease the column number
                break;
            case "right":
                newPosition[1]++;  // Move right, increase the column number
                break;
            default:
                System.out.println("Invalid direction: " + direction);
        }
    
        return newPosition;  // Return the new calculated position
    }

    private static void clearKnowledgeBase() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(kb, false))) {  // 'false' to overwrite the file
            // Write the basic knowledge back to the file after clearing it
            bw.write("GRID_SIZE 4 4\n");
            bw.write("SAFE_PLACE 1 1\n");
            bw.write("RULE Breeze => Pit nearby\n");
            bw.write("RULE Stench => Wumpus nearby\n");
            bw.write("MOVE Start (1,1)\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    private static void logKnowledgeRule(String rule) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(kb, true))) {
            bw.write("Knowledge Rule: " + rule + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void makeLogicalDeduction() {
        if (sensor_Breeze(agentPosition)) {
            logKnowledgeRule("Breeze => Pit nearby");
            // Mark adjacent cells as potentially dangerous (Pit)
        } else {
            logKnowledgeRule("No Breeze => No Pit nearby");
        }
    
        if (sensor_Stench(agentPosition)) {
            logKnowledgeRule("Stench => Wumpus nearby");
            // Mark adjacent cells as potentially dangerous (Wumpus)
        } else {
            logKnowledgeRule("No Stench => No Wumpus nearby");
        }
    }

    private static boolean makeDecisionAndMove() {
        // Step 1: Check surroundings and sensors
        makeLogicalDeduction();
    
        // Step 2: Determine the best move based on knowledge and sensors
        String bestMove = determineBestMove();
        
        if (bestMove != null) {
            System.out.println("Agent moves " + bestMove);
            return moveAgent(bestMove);
        } else {
            System.out.println("No valid safe move found, making a risky decision.");
            ArrayList<Integer> dangerousMoves = getValidMoves();
            if (!dangerousMoves.isEmpty()) {
                int riskyMove = riskyDecisionAlgorithm(dangerousMoves);  // Pick a risky move
                switch (riskyMove) {
                    case 0:
                        return moveAgent("up");
                    case 1:
                        return moveAgent("down");
                    case 2:
                        return moveAgent("left");
                    case 3:
                        return moveAgent("right");
                    default:
                        System.out.println("Error: Invalid risky move.");
                        return false;
                }
            } else {
                System.out.println("No risky moves possible, agent is stuck.");
                return false;  // If no risky move is possible
            }
        }
    }
    
    

    private static void testLogicalProofs() {
        if (isAlive) {
            KB(kb);  // Log current knowledge base
            
            // Logical conclusions based on sensors
            if (logicalProof_breezeImpliesPit(sensor_Breeze(agentPosition))) {
                System.out.println("Logical Conclusion: Breeze detected, there must be a pit nearby.");
            } else {
                System.out.println("Logical Conclusion: No breeze detected, no nearby pit.");
            }
            
            if (logicalProof_stenchImpliesWumpus(sensor_Stench(agentPosition))) {
                System.out.println("Logical Conclusion: Stench detected, there must be a Wumpus nearby.");
            } else {
                System.out.println("Logical Conclusion: No stench detected, no nearby Wumpus.");
            }
            
            // Check if rerouting is necessary
            if (rerouteCheck()) {
                System.out.println("Logical Conclusion: Rerouting due to unsafe environment.");
            } else {
                System.out.println("Logical Conclusion: No rerouting needed.");
            }
            
            // Check if no safe moves and risky decision is necessary
            ArrayList<Integer> possibleMoves = getValidMoves();
            if (possibleMoves.isEmpty()) {
                System.out.println("Logical Conclusion: No safe moves, risky decision required.");
                riskyDecisionAlgorithm(new ArrayList<>());  // Perform risky decision
            }

            // There is a best move.
            if (determineBestMove() != null) {
                System.out.println("Logical Conclusion: Safe moves available.");
            }

        } else {
            System.out.println("Agent is dead.");
        }
    }

    private static ArrayList<Integer> getValidMoves() {
        ArrayList<Integer> possibleMoves = new ArrayList<>(Arrays.asList(0, 1, 2, 3));  // 0: up, 1: down, 2: left, 3: right
        
        // Handle boundaries
        if (agentPosition[0] == 1) possibleMoves.remove(Integer.valueOf(0));  // Remove "up"
        if (agentPosition[0] == GRID_SIZE_Y) possibleMoves.remove(Integer.valueOf(1));  // Remove "down"
        if (agentPosition[1] == 1) possibleMoves.remove(Integer.valueOf(2));  // Remove "left"
        if (agentPosition[1] == GRID_SIZE_X) possibleMoves.remove(Integer.valueOf(3));  // Remove "right"
        
        // Handle dangerous spaces
        if (sensor_Breeze(agentPosition)) possibleMoves = avoidDangerousSpaces(possibleMoves, "Pit");
        if (sensor_Stench(agentPosition)) possibleMoves = avoidDangerousSpaces(possibleMoves, "Wumpus");
        
        return possibleMoves;
    }   

    private static boolean logicalProof_breezeImpliesPit(boolean breezeDetected) {
        return breezeDetected;
    }

    private static boolean logicalProof_stenchImpliesWumpus(boolean stenchDetected) {
        return stenchDetected;
    }

    private static void initializeAgent() {
        clearKnowledgeBase();

        setLegend("A", agentPosition);
        int[] pos = agentPosition.clone();
        System.out.println("Sensor: \n" + 
            agentSensor(
            sensor_Wumpus(pos), 
            sensor_Pit(pos), 
            sensor_Breeze(pos), 
            sensor_Stench(pos),
            sensor_Glitter(pos),
            sensor_Gold(pos)
            )
        );
    }

    private static String agent_state() {
        if (isAlive) {
            return "Alive";
        } else {
            return "Dead";
        }
    }

    private static Boolean agent_dead() {
        isAlive = false;
        logDead();
        return isAlive;
    }

    private static boolean moveAgent(String direction) {
        clearPosition(agentPosition);  // Clear current agent position from the grid
        //printAgentMap();

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
                    logMove("up");
                } else {
                    System.out.println("Agent can't move up, it's at the top boundary!\n");
                    return false;  // Invalid move
                }
                break;
            case "down": 
                if (agentPosition[0] < GRID_SIZE_Y) {
                    agentPosition[0]++; 
                    logMove("down");
                } else {
                    System.out.println("Agent can't move down, it's at the bottom boundary!\n");
                    return false;  // Invalid move
                }
                break;
            case "left": 
                if (agentPosition[1] > 1) {
                    agentPosition[1]--; 
                    logMove("left");
                } else {
                    System.out.println("Agent can't move left, it's at the left boundary!\n");
                    return false;  // Invalid move
                }
                break;
            case "right": 
                if (agentPosition[1] < GRID_SIZE_X) {
                    agentPosition[1]++; 
                    logMove("right");
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
        updateSensors();
        System.out.println("\nSensor: \n" + sensor);
    
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
            logWin();
            return true;  // The objective is achieved, agent can stop further exploration
        }
    
        // Place the agent in the new position, if still alive
        if (isAlive) {
            setLegend("A", agentPosition);  // Add agent to the new position
        }
    
        printGrid();  // Print the updated grid
        return true;  // A valid move was made
    }

    private static void logWin() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(kb, true))) {
            bw.write("WIN\n");  // Append "WIN" to the knowledge base
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void logDead() {
        // Appends "DEAD" to the knowledge base when the agent dies
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(kb, true))) {
            bw.write("DEAD\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }    

    private static void logMove(String direction) {
        // Appends the agent's move and sensor readings (W, B, S, P) to the knowledge base
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(kb, true))) {
            StringBuilder moveLog = new StringBuilder();
    
            // Log the move with direction and position
            moveLog.append("MOVE ").append(direction).append(" (")
                   .append(agentPosition[0]).append(",").append(agentPosition[1]).append(")");
    
            // Add sensor readings (Wumpus, Breeze, Stench, Pit)
            boolean wumpusDetected = sensor_Wumpus(agentPosition);
            boolean breezeDetected = sensor_Breeze(agentPosition);
            boolean stenchDetected = sensor_Stench(agentPosition);
            boolean pitDetected = sensor_Pit(agentPosition);
    
            if (wumpusDetected) moveLog.append(" W");
            if (breezeDetected) moveLog.append(" B");
            if (stenchDetected) moveLog.append(" S");
            if (pitDetected) moveLog.append(" P");
    
            // Write to the knowledge base file
            bw.write(moveLog.toString() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private static boolean sensor_Glitter(int[] position) {
        ArrayList<int[]> goldCoordinates = categoryMap.get("gold");
        for (int[] goldCoord : goldCoordinates) {
            if (Arrays.equals(position, goldCoord)) {
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

    private static Map<String, Boolean> agentSensor(boolean checkForWumpus, boolean checkForPit, 
                                                    boolean checkForBreeze, boolean checkForStench, 
                                                    boolean checkForGlitter, boolean checkForGold) {
        boolean sensor_Wumpus = checkForWumpus;
        boolean sensor_Pit = checkForPit;
        boolean sensor_Breeze = checkForBreeze;
        boolean sensor_Stench = checkForStench;
        boolean sensor_Glitter = checkForGlitter;
        boolean sensor_Gold = checkForGold;
        sensor.put("sensor_Wumpus", sensor_Wumpus);
        sensor.put("sensor_Pit", sensor_Pit);
        sensor.put("sensor_Breeze", sensor_Breeze);
        sensor.put("sensor_Stench", sensor_Stench);
        sensor.put("sensor_Glitter", sensor_Glitter);
        sensor.put("sensor_Gold", sensor_Gold);
        return sensor;
    }

    private static void updateSensors() {
        // Update sensors based on the agent's current position
        sensor = agentSensor(
            sensor_Wumpus(agentPosition),
            sensor_Pit(agentPosition),
            sensor_Breeze(agentPosition),
            sensor_Stench(agentPosition),
            sensor_Glitter(agentPosition),
            sensor_Gold(agentPosition)
        );
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

        for (int i = 0; i < categoryMap.get("pit").size(); i++) {
            int[] pitCoordinate = getCoordinate("pit", i);
            final int indexBalancer = i + 1;
            System.out.println("Pit #" + indexBalancer + " : (" + pitCoordinate[0] + ", " + pitCoordinate[1] + ")");
        }

        int[] wumpusCoordinate = getCoordinate("wumpus", 0);
        System.out.println("Wumpus: (" + wumpusCoordinate[0] + ", " + wumpusCoordinate[1] + ")");

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
        boolean hasPit = sensor_Pit(location);
        boolean hasWumpus = sensor_Wumpus(location);
        boolean hasGlitter = sensor_Glitter(location);
        boolean hasGold = sensor_Gold(location);
    
        for (LocationInfo info : agentMap){
            if (Arrays.equals(location, info.getCoordinates())) {
                // Already exists, no need to add again
                return;
            }
        }
    
        // Add new location information with corresponding sensor data
        agentMap.add(new LocationInfo(location.clone(), hasBreeze, hasStench, hasPit, hasWumpus, hasGlitter, hasGold));
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
        ArrayList<Integer> possibleMoves = new ArrayList<>(Arrays.asList(0, 1, 2, 3));  // 0: up, 1: down, 2: left, 3: right
        ArrayList<Integer> dangerousMoves = new ArrayList<>();
    
        // Handle if Agent is Against a Wall
        if (agentPosition[0] == 1) {
            possibleMoves.removeIf(move -> move == 0);  // Remove "up"
        }
        if (agentPosition[0] == GRID_SIZE_Y) {
            possibleMoves.removeIf(move -> move == 1);  // Remove "down"
        }
        if (agentPosition[1] == 1) {
            possibleMoves.removeIf(move -> move == 2);  // Remove "left"
        }
        if (agentPosition[1] == GRID_SIZE_X) {
            possibleMoves.removeIf(move -> move == 3);  // Remove "right"
        }
    
        // Handle if Agent Feels a Breeze
        if (sensor_Breeze(agentPosition)) {
            dangerousMoves = avoidDangerousSpaces(dangerousMoves, "Pit");
        }
        
        // Handle if Agent Smells a Stench
        if (sensor_Stench(agentPosition)) {
            dangerousMoves = avoidDangerousSpaces(dangerousMoves, "Wumpus");
        }
    
        // Remove dangerous moves from the list of possible moves
        possibleMoves.removeAll(dangerousMoves);
    
        // Check if no safe moves are available
        if (possibleMoves.isEmpty()) {
            if (rerouteCheck()) {
                return rerouteAlgorithm();
            } else {
                return riskyDecisionAlgorithm(dangerousMoves);
            }
        }
    
        // Return a random safe move (you can prioritize safer moves based on additional logic)
        Random rand = new Random();
        return possibleMoves.get(rand.nextInt(possibleMoves.size()));
    }

    private static boolean isAdjacentToPit(int[] location) {
        ArrayList<int[]> pitCoordinates = categoryMap.get("pit");
        for (int[] pitCoord : pitCoordinates) {
            if (Math.abs(pitCoord[0] - location[0]) == 1 && pitCoord[1] == location[1]) {
                return true;  // Pit is vertically adjacent
            }
            if (Math.abs(pitCoord[1] - location[1]) == 1 && pitCoord[0] == location[0]) {
                return true;  // Pit is horizontally adjacent
            }
        }
        return false;
    }

    private static boolean isAdjacentToWumpus(int[] location) {
        ArrayList<int[]> wumpusCoordinates = categoryMap.get("wumpus");
        for (int[] wumpusCoord : wumpusCoordinates) {
            if (Math.abs(wumpusCoord[0] - location[0]) == 1 && wumpusCoord[1] == location[1]) {
                return true;  // Wumpus is vertically adjacent
            }
            if (Math.abs(wumpusCoord[1] - location[1]) == 1 && wumpusCoord[0] == location[0]) {
                return true;  // Wumpus is horizontally adjacent
            }
        }
        return false;
    }

    private static LocationInfo getLocationInfo(int[] location) {
        for (LocationInfo info : agentMap) {
            if (Arrays.equals(info.getCoordinates(), location)) {
                return info;
            }
        }
        return null;  // Location not found in the agent's map
    }    

    private static boolean isLocationValid(int[] location) {
        int x = location[0];
        int y = location[1];
    
        // 1. Check if the location is within the grid bounds
        if (x < 1 || x > GRID_SIZE_X || y < 1 || y > GRID_SIZE_Y) {
            return false;  // Out of bounds
        }
    
        // 2. Check if the location has been visited and is known to be safe
        if (checkAgentMap(location)) {
            // If it's a previously visited location and the agent knows it is safe, allow revisiting
            LocationInfo visitedInfo = getLocationInfo(location);
            if (visitedInfo != null && !visitedInfo.hasBreeze() && !visitedInfo.hasStench()) {
                return true;  // Safe to revisit
            }
            // If the location is dangerous, avoid it
            return false;
        }
    
        // 3. Avoid known dangerous locations (Wumpus or Pit)
        if (sensor_Wumpus(location) || sensor_Pit(location)) {
            return false;  // Unsafe due to Wumpus or Pit
        }
    
        // 4. Avoid potential dangerous locations based on sensor data
        if (sensor_Breeze(agentPosition)) {
            if (isAdjacentToPit(location)) {
                return false;  // Potential pit nearby
            }
        }
        if (sensor_Stench(agentPosition)) {
            if (isAdjacentToWumpus(location)) {
                return false;  // Potential Wumpus nearby
            }
        }
    
        return true;  // If all checks pass, the location is valid
    }

    private static ArrayList<Integer> avoidDangerousSpaces(ArrayList<Integer> dangerousMoves, String dangerType) {
        if (dangerType.equals("Pit")) {
            for (LocationInfo info : agentMap) {
                int[] currentLocation = info.getCoordinates();
                int[][] adjacentLocations = {
                    {currentLocation[0] - 1, currentLocation[1]},  // Up
                    {currentLocation[0] + 1, currentLocation[1]},  // Down
                    {currentLocation[0], currentLocation[1] - 1},  // Left
                    {currentLocation[0], currentLocation[1] + 1}   // Right
                };
                for (int[] adjLocation : adjacentLocations) {
                    if (isLocationValid(adjLocation) && !checkAgentMap(adjLocation)) {
                        int moveDirection = getMoveDirection(currentLocation, adjLocation);
                        dangerousMoves.add(moveDirection);
                    }
                }
            }
        } else if (dangerType.equals("Wumpus")) {
            // Similar logic for Wumpus based on stench
            for (LocationInfo info : agentMap) {
                int[] currentLocation = info.getCoordinates();
                if (info.hasStench()) {
                    int[][] adjacentLocations = {
                        {currentLocation[0] - 1, currentLocation[1]},  // Up
                        {currentLocation[0] + 1, currentLocation[1]},  // Down
                        {currentLocation[0], currentLocation[1] - 1},  // Left
                        {currentLocation[0], currentLocation[1] + 1}   // Right
                    };
                    for (int[] adjLocation : adjacentLocations) {
                        if (isLocationValid(adjLocation) && !checkAgentMap(adjLocation)) {
                            int moveDirection = getMoveDirection(currentLocation, adjLocation);
                            dangerousMoves.add(moveDirection);
                        }
                    }
                }
            }
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
        // Check if the agent has previously visited any safe places that could be rerouted to
        for (LocationInfo info : agentMap) {
            if (!info.hasBreeze() && !info.hasStench()) {
                return true;  // A safe reroute exists
            }
        }
        return false;  // No reroute available
    }    

    private static int rerouteAlgorithm() {
        // Find the nearest previously visited safe location and move towards it
        for (LocationInfo info : agentMap) {
            if (!info.hasBreeze() && !info.hasStench()) {
                int[] safeLocation = info.getCoordinates();
                return getMoveDirection(agentPosition, safeLocation);  // Calculate the move direction to the safe place
            }
        }
        return -1;  // No reroute possible
    }
    
    
    private static boolean allMovesSeemDangerous(ArrayList<Integer> possibleMoves) {
        // Logic to check if all remaining moves appear dangerous
        return false;  // Placeholder - You can implement actual checks here
    }
    
    private static int riskyDecisionAlgorithm(ArrayList<Integer> dangerousMoves) {
        // When forced to make a risky decision, pick randomly from dangerous moves
        Random rand = new Random();
        int riskyMove = dangerousMoves.get(rand.nextInt(dangerousMoves.size()));
        System.out.println("Logical Conclusion: Taking a risky move in direction: " + riskyMove);
        return riskyMove;
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

    public static String checkChamberStatus(int x, int y) {
        // Check if the chamber is within bounds
        if (x < 1 || x > GRID_SIZE_X || y < 1 || y > GRID_SIZE_Y) {
            return "Invalid chamber coordinates.";
        }
    
        // Check if the chamber has been visited or has known information
        for (LocationInfo info : agentMap) {
            if (Arrays.equals(info.getCoordinates(), new int[]{x, y})) {
                if (info.hasBreeze()) {
                    return "Breeze detected at (" + x + "," + y + "). Probability of Pit: " + calculatePitProbability(x, y);
                } else if (info.hasStench()) {
                    return "Stench detected at (" + x + "," + y + "). Probability of Wumpus: " + calculateWumpusProbability(x, y);
                } else {
                    return "SAFE. No Wumpus, no Pit at (" + x + "," + y + ").";
                }
            }
        }
    
        // If no information is available, return UNKNOWN
        return "UNKNOWN. No information about chamber (" + x + "," + y + ").";
    }
    
    public static double calculatePitProbability(int x, int y) {
        // Check if the current room has a breeze (meaning adjacent rooms could have pits)
        if (sensor_Breeze(new int[]{x, y})) {
            ArrayList<int[]> adjacentRooms = getAdjacentRooms(x, y);
            int possiblePitLocations = adjacentRooms.size();  // Adjacent rooms are the possible pit locations
            
            // Each adjacent room should share equal probability of having the pit
            return possiblePitLocations > 0 ? 1.0 / possiblePitLocations : 0.0;
        } else if (sensor_Pit(new int[]{x, y})) {
            return 1;
        } else {
            // No breeze means no pit in adjacent rooms
            return 0.0;
        }
    }
    
    
    public static double calculateWumpusProbability(int x, int y) {
        // Check if the current room has a stench (meaning adjacent rooms could have the Wumpus)
        if (sensor_Stench(new int[]{x, y})) {
            ArrayList<int[]> adjacentRooms = getAdjacentRooms(x, y);
            int possibleWumpusLocations = adjacentRooms.size();  // Adjacent rooms are the possible Wumpus locations
            
            // Each adjacent room should share equal probability of having the Wumpus
            return possibleWumpusLocations > 0 ? 1.0 / possibleWumpusLocations : 0.0;
        } else if (sensor_Wumpus(new int[]{x, y})) {
            return 1;
        } else {
            // No stench means no Wumpus in adjacent rooms
            return 0.0;
        }
    }
    
    public static ArrayList<int[]> getAdjacentRooms(int x, int y) {
        ArrayList<int[]> adjacentRooms = new ArrayList<>();
    
        if (x > 1) adjacentRooms.add(new int[]{x - 1, y});  // Up
        if (x < GRID_SIZE_X) adjacentRooms.add(new int[]{x + 1, y});  // Down
        if (y > 1) adjacentRooms.add(new int[]{x, y - 1});  // Left
        if (y < GRID_SIZE_Y) adjacentRooms.add(new int[]{x, y + 1});  // Right
    
        return adjacentRooms;
    }
    

}