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
