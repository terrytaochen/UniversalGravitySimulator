Ussage:
java Collisions <Number of Thread> <Number of bodies> <Size of Body> <time stamp> <Type>

Note:
Type is required, and only can be 0, 1, 2
1 -> Sequential
2 -> Multi-thread
3 -> GUI

Example:

Sequential:

java Collisions 3 30 20 1 0

Note:
When choose type 0, we will ignore number of thread, you cold enter any int

multi-thread

java Collisions 3 30 20 1 1

GUI:

java Collisions 3 30 20 1 2

Note:
When choose type 2, we will ignore all of these arguments, you will need to input in GUI view again. 
