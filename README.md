# Hide and seek - the simulation

## Game description

**Hide and seek** is a game/simulation where each player is equipped with a smartwatch (simulated, in this case) running a Java application specifically designed for the game. The processes coordinate to choose the hiders and the seeker, and they monitor players' heart rates using a simulated photoplethysmography sensor to ensure their health is tracked throughout the game. A game manager can monitor the health status of participants and intervene if necessary via an administration client.

## Technologies Used

- **Java** - For the main Player application running on the smartwatches.
- **gRPC** - For communication between players.
- **MQTT** - For game notifications.
- **REST** - For the administration server and client interactions.

## Project Components

- **Player/Game Application** - Java process running on each smartwatch, handling game logic and communication.
- **Administration Server** - A REST server for managing players and collecting health data.
- **Administration Client** - A client interface for the game manager to monitor health data and control the game.

## Game Flow

- Players are assigned roles (seeker or hider) through a distributed election algorithm (Bully with some modifications).
- Hiders aim to reach the home base without getting tagged by the seeker.
- Hiders negotiate access to the home base with distributed mutual exclusion algorithm (Ricart & Agrawala)
- Heart rate data is periodically sent to the administration server to ensure players' safety.
- The game ends when all hiders are either safe or tagged.

## System Architecture

- **Players' Network** - Autonomous peer-to-peer system for coordination.
- **Administration Server** - Centralized server for player management and health monitoring.
- **Administration Client** - Interface for game management and health status monitoring.
- **MQTT Broker** - For communication of game status and messages.

![image](https://github.com/heyimjustalex/HideAndSeekTheGame/assets/21158649/90915b49-8172-44a4-a18e-b12d870f57bc) <br/>

## REST Administration Server architecture

- **Controllers** - REST endpoints defined
- **Services** - business logic
- **Repositories** - Data access layer with relevant collections
- **Models** - Model classes for serialization and deserialization of Player and Measurement
- **Schemas** - Model classes for HTTP requests and responses

![image](https://github.com/heyimjustalex/HideAndSeekTheGame/assets/21158649/8426260a-75e1-4906-bb6e-c3dfcb1fe55a) <br/>

## How do the Game/Player processes work?

**Game can be in one of these states (GameState):** BEFORE_ELECTION, ELECTION_STARTED, ELECTION_MESSAGES_SENT ELECTION_ENDED, GAME_ENDED

**Player can be in on of these states (PlayerState):** AFTER_ELECTION, WAITING_FOR_LOCK, GOING_TO_BASE, WINNER, TAGGED

**There are different messsages swapped with gRPC between the Player processes:**

_ELECTION MESSAGE TYPES:_ GREETING, GREETING_OK, ELECTION, ELECTION_OK, COORDINATOR

_MUTUAL EXCLUSION MESSAGE TYPES:_ REQUEST_RESOURCE, RESOURCE_GRANTED, RESOURCE_NOT_GRANTED, ACK, SEEKER_ASKING, SEEKER_TAGGING

Players make decisions based on their state, state of their game and the message type that comes to their process.

### Bully election algorithm with modifications

**Stage 1 - Greeting (prerequisites)**

- Players join, add their data to AdministrationServer, which - in case of non-confilicting ids - responds by giving them data of all players (both previously registered and the current one, along with an assigned position on the grid)
- Player greets all the players (GREETING) he was given information about
- Players examine their state, add the Player if they didn't know about him, and respond accordingly to the following logic (Player that is greeted perspective): <br/>
     - If I have started election process <br/>
       - Compare election priorities and if my election priority is lower, then cancel my election if I am trying to be the Seeker. Then send ACK. (edge case) <br/>
       - Else send GREETING_OK <br/>
     - Else just send GREETING_OK <br/>
- Player that started the greeting reacts to the responses: <br/>
     - If I got GREETING_OK <br/>
       - If ELECTION_ENDED just modify one of the players to be the Seeker <br/>
       - Set any higher gameState than mine <br/>
       - Cancel trying to be Seeker in case other party has election going on and his priority is higher  <br/>
     - Else (if I got ACK): <br/>
       - Just set the ELECTION_STARTED, so I start the election process <br/>

![image](https://github.com/heyimjustalex/HideAndSeekTheGame/assets/21158649/ff34a70f-0ae8-4b0c-8798-e06fb84b6aa3) <br/>

![image](https://github.com/heyimjustalex/HideAndSeekTheGame/assets/21158649/1035b88c-fdac-4175-94b7-224d1c4ea17a) <br/>

**Stage 2 - election with Bully**

- If you are the only player just elect yourself the Seeker, set gameState to ELECTION_ENDED 
- Else <br/>
    - Place Future that in 12s will make you the Seeker and you will send COORDINATOR messages to all of the players (edge case if I missed other's election messages and other will cancel when greeting) <br/>
    - Call gRPC election service of the players with higher priority sending them ELECTION and your distnace/priority <br/>
    - If you get ELECTION_OK message then cancel election and set role to HIDER <br/>

  - If you got on your gRPC service election message then: <br/>
      - Check if you have newest state in case you lost MQTT message <br/>
      - Place Future that in 12s will make you Seeker and you will send COORDINATOR messages <br/>
      - If your priority is higher then respond with ELECTION_OK to the other party <br/>
      - Else, cancel your election <br/>
  - If you have become SEEKER then send COORDINATOR messages and set your role to SEEKER and gameState ELECTION_ENDED <br/>
  - If you got COORDINATOR message then set the player you got it from in your local collection to SEEKER, set yourself to HIDER and set gameState to ELECTION_ENDED <br/>

![image](https://github.com/heyimjustalex/HideAndSeekTheGame/assets/21158649/53478039-1198-4722-a6e6-517d4575cdec) <br/>
![image](https://github.com/heyimjustalex/HideAndSeekTheGame/assets/21158649/27e0892b-e1f0-44c3-96c8-268e026f0b3b) <br/>
![image](https://github.com/heyimjustalex/HideAndSeekTheGame/assets/21158649/f163dff5-9a0d-42e4-95ac-aa35fff88f4c) <br />



### Mutual-exclusion algorithm Ricart & Agrawala algorithm with modifications (total order assumption)*
**Stage 3 - Process cooperation and playing**

- If you are HIDER:  <br/>
    - Send resource request to all HIDERS <br/>
    - If you get as many ACCESS_GRANTED as you have sent requests, then try going to Base (shared resource) <br/>
    - Else wait and answer other with your gRPC according to this logic: <br/>
      - If coming request has lower timestamp than my request's timestamp, then send ACCESS_GRANTED <br/>
      - Else, put request in queue and when you finish working on the shared resource (exit the base), send responses to all of the waiting HIDERS <br/>
      
![image](https://github.com/heyimjustalex/HideAndSeekTheGame/assets/21158649/d27d8c83-0501-4c56-98f0-45b10badd263) <br/>
![image](https://github.com/heyimjustalex/HideAndSeekTheGame/assets/21158649/639b1ee1-f04f-44e2-aeb2-832f7d908484)  <br/>
![image](https://github.com/heyimjustalex/HideAndSeekTheGame/assets/21158649/06a74cf5-3c41-42db-8950-75a54598fce1) <br/>
![image](https://github.com/heyimjustalex/HideAndSeekTheGame/assets/21158649/343fa527-a7d2-4144-a882-30b4fd55101c) <br/>

- If you are SEEKER: <br/>
    - You don't take part in mutual exclusion algorithm <br/>
    - Instead you send SEEKER_ASKING message to get to know the state of the HIDERS <br/>
    - You choose the one that is available (not TAGGED and not WINNER) and closest to you and when you reach his position you try to send him SEEKER_TAGGING <br/>
      - If he moved by this time, then he responds with WINNER state <br/>
      - Else he responds with TAGGED state and the Seeker eliminated the hider <br/>
    - You try all of these steps until all player have either WINNER or TAGGED states (playerState) <br/>
- The game ends

![image](https://github.com/heyimjustalex/HideAndSeekTheGame/assets/21158649/740b87c3-9449-4430-b285-a41ed0346b0d)<br/>
![image](https://github.com/heyimjustalex/HideAndSeekTheGame/assets/21158649/568f34ea-6c2e-4874-af6e-db6811ac67d3)<br/>

