# concurrent_rectangle_drawing

## Description
The goal here is to use ##multiple threads## efficiently to draw rectangles on an image. 

Rectangles are axis-aligned, drawn with a 1-pixel thick black border, but filled in with a random colour. 20 Each thread repeatedly attempts to draw a random rectangle. It chooses a random spot to start (a corner or center, up to you) and a random size (within the image limits), and ensures the resulting rectangle at that location will not overlap with other in-progress rectangles, and draws it. Once a thread starts drawing a rectangle it must fully complete the process.

concurrent_rectangle_drawing.java that accepts the following command-line arguments, width, height, n, and k. It should launch n threads to draw a total of k rectangles on a width x height image. Choose w,h,k such that the program typically takes a few seconds (or at least several hundreds of milliseconds) to run with n = 1. 

Timing code is added (using System.currentTimeMillis) to time the program from the point the threads are launched to the point when all threads have completed their work. Once all threads have completed, the time taken in milliseconds should be emitted as console output and the image output to a file, named outputimage.png.

A performance plot of the relative speedup of the multithreaded versions over the single-threaded version for 2, 3, and 4 threads are provided with a brief explanation. 
 
## Usage
In terminal, type the commands below to run this program properly:
```
javac concurrent_rectangle_drawing.java
java concurrent_rectangle_drawing width height #threads #rectangles  
```

e.g.
```
javac concurrent_rectangle_drawing.java
java concurrent_rectangle_drawing 500 500 3 180 
```
