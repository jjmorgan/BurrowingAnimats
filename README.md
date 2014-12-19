BurrowingAnimats
================

Simulates the evolution of simple robots that dig burrows to survive in a hostile environment. Requires Java 6 to run.

Project for CS 263C, UCLA Fall 2014.

## Contents

* src
  * Main.java - Main entry point
  * env - Source files for simulation display and control
  * nn - Source files for neural network construction handling
* lib
  * ini4j-0.5.2.jar - Ini4j (http://ini4j.sourceforge.net/)
* burrowinganimats.jar - Compiled executable
* run.bat - Script that runs the executable
* config.ini - Configuration file that points to the prey and predator NN files
* preynn.txt - Default architecture of the prey neural network
* preynn\_type\_1.txt - Prey that does not dig burrows
* preynn\_type\_2.txt - Prey that digs burrows
* preynn\_type\_3.txt - Prey that digs burrows and warns other pey of nearby predators
* predatornn.txt - Default architecture of the predator neural network

## Controls

* p - Pause/unpause the simulation
* n - Skip immediately to the next generation
* d - Cycle to the next debug display:
  1. None
  2. Show all gradients
  3. Show gradients detectable by prey
  4. Show gradients of shouting prey
* w - Save a full report of prey and hole statistics in CSV format to a file.

Clicking on an object in the simulation window will show the object's status in the bottom left corner. Clicking a predator (red) or prey (light blue) will also show the state of its neural network.
