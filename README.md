# - WumpusWorld -

## ACO494: Foundations of Artificial Intelligence 
### Arizona State University 
### Project #1 
### Authors (In alphabetical order): 
- Aiden Cullinan
- Ali Alnassary 
- Denysse Sevilla
- Omar Perez
- Vito Mulia

Programming Language: Java
    
Description:

The Wumpus World’s agent is an example of a knowledge-based agent that represents Knowledge representation, reasoning, and planning. Knowledge-based agent links general knowledge with current percepts to infer hidden characters of the current state before selecting actions. Its necessity is vital in partially observable environments.

Problem Statement:

The Wumpus world is a cave with 16 rooms (4×4). Each room is connected to others through walkways (no rooms are connected diagonally). The knowledge-based agent starts from Room[1, 1]. The cave has – some pits, a treasure, and a beast named Wumpus. The Wumpus cannot move but eats the one who enters its room. If the agent enters the pit, it gets stuck there. The goal of the agent is to take the treasure and come out of the cave. The agent is rewarded when the goal conditions are met. The agent is penalized when it falls into a pit or is eaten by the Wumpus.
    
Elements that support the agent:
- The rooms adjacent to the Wumpus' room give out a stench.
- The agent is given one arrow which it can use to kill the Wumpus when facing it (Wumpus screams when it is killed).
- The rooms adjacent to the pits' rooms let out a breeze.
- The treasure room always glitters.
    
Source:

Geeks for Geeks, "AI | The Wumpus World Description", 2020
https://www.geeksforgeeks.org/ai-the-wumpus-world-description/

### I. How to Start the Game:
  ### 1. Installation (First Way) (This requires Java 1.8 or higher)
    Step 1: Clone the repository
    - https://github.com/MrMulia/WumpusWorld.git
        Step 2: Navigate to the repository and compile the Java source code
        - javac WumpusWorld.java
        Step 3: Execute the code
        - cd WumpusWorld (directory)
        - javac *.java 
        
 ### 2. Installation (Second Way)
    Step 1: Clone the repository
        - https://github.com/MrMulia/WumpusWorld.git    
    Step 2: Open your preferred IDE (e.g. Intellij, Eclipse, or Netbeans).
        - Create a new project File -> New Project (depending on your IDE).
        - Select Java for the project type
        - This requires Java 1.8 or higher.
    Step 3: Add the Source Files:
        WumpusWorld.java
        LocationInfo.java
        testworld.txt 
    Step 4: Build Project
    Step 5: Run the Project

### II. Custom Environment
This section explains how to create a custom environment on the 4x4 grid.
### 1. Creating the txt file.
    wumpus 1 4
    gold 4 3
    pit 2 1
    pit 3 3
    pit 4 4
The code only accepts txt files, make sure to save it as a .txt file. 
### 2. Move the .txt file into the file with the source code.
### 3. Change line `44` which states `parseFile(filePath)` of the code, either swapping filePath with `"your-file-name.txt"` OR
### 4. Change line `40` which states `String filePath = "testworld.txt";` of the code, swapping `testworld.txt` with your .txt filename.

### Extra Information:
   1. File Structure
        - WumpusWorld.java (Main logic for the game)
        - LocationInfo.java (Helper function for the game)
        - testworld.txt (Creates an environment for the wumpus to find the treasure (highly customizable))
        - README.txt (Instructions for Running the game)
        - kb.txt (An output of the character's knowledge base) <br>
  2. Prerequisites
        - This requires Java 1.8 or higher
        - Git installed on your system (Optional)
        - IDE (Optional) <br>
  ## Contact
  ### For any issues or suggestions, please contact our email at:
  - group01@asu.edu
