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

- Players are assigned roles (seeker or hider) through a distributed algorithm.
- Hiders aim to reach the home base without getting tagged by the seeker.
- Heart rate data is periodically sent to the administration server to ensure players' safety.
- The game ends when all hiders are either safe or tagged.

## System Architecture

- **Players' Network** - Autonomous peer-to-peer system for coordination.
- **Administration Server** - Centralized server for player management and health monitoring.
- **Administration Client** - Interface for game management and health status monitoring.
- **MQTT Broker** - For communication of game status and messages.
