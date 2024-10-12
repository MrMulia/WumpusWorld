# WumpusWorld
## ACO494: Foundations of Artificial Intelligence <br>
## Arizona State University <br>
### Project #1 <br>
#### Authors (In alphabetical order):
#### - Aiden Cullinan
#### - Ali Alnassary
#### - Denysse Sevilla
#### - Omar Perez
#### - Vito Mulia (@Otiv)
The Wumpus World’s agent is an example of a knowledge-based agent that represents Knowledge representation, reasoning and planning. Knowledge-Based agent links general knowledge with current percepts to infer hidden characters of current state before selecting actions. Its necessity is vital in partially observable environments.

Problem Statement:
The Wumpus world is a cave with 16 rooms (4×4). Each room is connected to others through walkways (no rooms are connected diagonally). The knowledge-based agent starts from Room[1, 1]. The cave has – some pits, a treasure and a beast named Wumpus. The Wumpus can not move but eats the one who enters its room. If the agent enters the pit, it gets stuck there. The goal of the agent is to take the treasure and come out of the cave. The agent is rewarded, when the goal conditions are met. The agent is penalized, when it falls into a pit or being eaten by the Wumpus.
Some elements support the agent to explore the cave, like -The wumpus’s adjacent rooms are stenchy. -The agent is given one arrow which it can use to kill the wumpus when facing it (Wumpus screams when it is killed). – The adjacent rooms of the room with pits are filled with breeze. -The treasure room is always glittery.

Source: 
  Geeks for Geeks, "AI | The Wumpus World Description", 2020
  https://www.geeksforgeeks.org/ai-the-wumpus-world-description/
