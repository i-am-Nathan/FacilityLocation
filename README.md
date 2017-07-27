# FacilityLocation
This is the GitHub page for our 700 project.

## Problem Description  
Graph with nodes + edges

![Graph Equation](https://latex.codecogs.com/gif.latex?G&space;=&space;(V,E,w,p,type,k))

The Graphs in our problem consists of:
 - Vertices(V)
 - Edges(E)
 - distance? ![distdef](https://latex.codecogs.com/gif.latex?\dpi{100}&space;w&space;=&space;E\rightarrow&space;\mathbb{R})
 - population ![popdef](https://latex.codecogs.com/gif.latex?\dpi{100}&space;p&space;=&space;E\rightarrow&space;\mathbb{N})
 - residential type  ![resdef](https://latex.codecogs.com/gif.latex?\inline&space;\dpi{100}&space;t:V&space;{\color{Red}&space;\textbf{(type&space;of&space;node)}}\rightarrow&space;T&space;{\color{Red}&space;\textbf{(set&space;of&space;node&space;types)}})

Output: A set of nodes of the optimal facility location ![outputdef](https://latex.codecogs.com/gif.latex?\dpi{100}&space;S&space;\subseteq&space;V)

Constraint:
 - Every ![uins](https://latex.codecogs.com/gif.latex?\dpi{100}&space;u&space;\in&space;S) must have ![industrialtype](https://latex.codecogs.com/gif.latex?\dpi{100}&space;t(w)&space;=&space;industrial) (facilities must be places on industrial zones)
 - ![seqk](https://latex.codecogs.com/gif.latex?\dpi{100}&space;\left&space;|&space;s&space;\right&space;|&space;=&space;k) 

Measure of performance: All 'client' nodes must be in a residential zone, compute the distance (u,s) minimise the sum of distance*p(u)

First priority is to use linear programming methods to solve this problem

Algorithm
1) Initialize the facility locations and increase their capacity by a certain amount e.g. 20%
2) Run an iterative allocation-location process
      a) Assign demand locations to their closest unfulfilled facility
      b) Recalculate each facility location as the cenroid of its service area using the results of stage a.
      c) Repeat a) and b) until facility locations converge or the maximum interations is reaced.
3) Swap those final locations determined in step 2 exhaustively amoung facilities and use the original capcity vales.
      a) swap the facility locations amoung facilities
      b) run the allocation-llocation iteration after each capacity swap
      c) evaluate the result by their weight or their value from the population
      d) repeat a and b until the facility locations are coupled with the  actual capacities in all possible ways
4) Choose the final solution with the minimum distance to population weight among all recorded solutions     

We will be focusing on using Local Search with single swaps and Local search with multi-swaps.
We plan on creating 2 different versions while using the algorithms. 1 using euclidean distance to measure the cost of each combination of facilities and the 2nd one will be using the network.

The algorithm itself contains 2 phases:
-	Finding the Initial K facilities
-	Swapping or multi-swapping

Euclidean Distance Version 1:
-	Find initial K using Centrality and clustering
-	Swap one facility at a time to find the best combination
Euclidean Distance Version 2:
-	Find initial K by finding furthest nodes apart to each other
-	Swap one facility at a time to find the best combination
Euclidean Distance Version 3:
-	Find initial K by using Centrality and clustering
-	Swap multiple p facilities at a time to find the best combination
Euclidean Distance Version 4:
-	Find initial K by finding the furthest nodes apart to each other
-	Swap multiple p facilities at a time to find the best combination

Using the network has the same steps however the edges will be used as the distance instead of euclidean distance.


