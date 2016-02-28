package holb6595;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import spacesettlers.objects.AbstractActionableObject;
import spacesettlers.objects.AbstractObject;
import spacesettlers.objects.Asteroid;
import spacesettlers.simulator.Toroidal2DPhysics;


/**
 *
 * @author kobypascual
 *
 */
class Graph
{
	// Koby wrote this while drinking and thinking aloud!

	// Variables of playing field
		// Variables holding the coordinates of the playing. Origin is NW of screen.
	private double xmin = 0.0; // west
	private double xmax = 800.0; // east
	private double ymin = 0.0; // north
	private double ymax = 540.0; // south
	private double totalArea = xmax * ymax;

	// Variables of nodes
	private double domain = 0;
	private double nodeArea = 0;

	// HashMap of non-valid node values
	Map<Integer, Boolean> nonValid = new HashMap<Integer, Boolean>();

	// To create a gridded graph, divide each part of the map evenly into squares. (Called nodes)
	// However large the nodeAmount is, will determine how many "boxes" the map will be cut into.
	// TODO if nodeamount doesn't divide evenly we havea big problem
	private int nodeDivisions = 0;
	private int nodeAmount = 0;

	// TODO For now I'm storing the graph in an array list that holds the nodes.
	// First of all, how am I going to store the edges? - likely just hold a list on each node of the connecting edges
	// Second, is holding the graph in an arraylist the best way to do this? Likely more optimal way
	private Map<Integer, Node> graph = new HashMap<Integer, Node>();
	Toroidal2DPhysics space;
	KnowledgeRep2 globalKnowledge;
	Graph(Toroidal2DPhysics space, int nodes, KnowledgeRep2 globalKnowledge)
	{
		this.space = space;
		this.nodeDivisions = nodes;
		this.nodeAmount = nodeDivisions*nodeDivisions;
		this.globalKnowledge = globalKnowledge;

		// Find the x and y domains that each node will have.
		double xdomain = xmax/(nodeDivisions*nodeDivisions);
		double ydomain = ymax/(nodeDivisions*nodeDivisions);
		// TODO eliminate double nodeArea = totalArea/nodeAmount;
		// TODO eliminate double domain = Math.sqrt(nodeArea);

		// Now we have the amount of nodes we want to cut the map into, divide evenly.
		// Start from totalArea 0.0,0.0
		double xstart = xmin;
		double ystart = ymin;
		// TODO NEED TO CONSIDER ROUNDING ERRORS. WHEN CHECKING WHICH BOX WE SHOULD PROBABLY FLOOR OR CEILING THE NODES
		for(int nodeValue = 0; nodeValue < nodeAmount; nodeValue++)
		{
			double xfinish = xdomain + xstart;
			double yfinish = ydomain + ystart;

			// First check if this node has an asteroid or a ship or base.
			// If not, continue;
			if(checkSpace(space, xstart, xfinish, ystart, yfinish))
			{
				if(((nodeAmount%nodeDivisions) +1) == 0)
				{
					Node currentNode = new Node(nodeValue, xstart, xfinish, ystart, yfinish);
					graph.put(nodeValue, currentNode);

					// update coordinates
					xstart = xmin;
					ystart += ydomain;
				}
				else
				{
					Node currentNode = new Node(nodeValue, xstart, xfinish, ystart, yfinish);
					graph.put(nodeValue, currentNode);
					// update coordinates
					xstart += xdomain;
				}
			}
			else
			{
				nonValid.put(nodeValue, true);
			}

			// Now that the nodes have been created, attach each edge to each node
			for(int node = 0; node < nodeAmount; node++)
			{
				boolean top = false;
				boolean left = false;
				boolean right = false;
				boolean bottom = false;

				if(node >= 0 && node < nodeDivisions) // top row
				{
					top = true;
					if(node%nodeDivisions == 0) // top,left row
					{
						left = true;
						addEdges(graph.get(node), 0, 0);
					}
					else if((node+1)%nodeDivisions == 0) // top,right row
					{
						right = true;
						addEdges(graph.get(node), 0, 1);
					}
					else // top,middle row
					{
						addTBMidEdges(graph.get(node), 0);
					}
				}
				else if(node >= (nodeAmount-nodeDivisions) && node < (nodeAmount)) // bottom row
				{
					bottom = true;
					if(node%nodeDivisions == 0) // bottom,left row
					{
						left = true;
						addEdges(graph.get(node), 1, 0);
					}
					else if((node+1)%nodeDivisions == 0) // bottom,right row
					{
						right = true;
						addEdges(graph.get(node), 1, 1);
					}
					else // bottom,middle row
					{
						addTBMidEdges(graph.get(node), 1);
					}
				}
				else if(node%nodeDivisions == 0) // Left row
				{

				}
				else if((node+1)%nodeDivisions == 0)
				{

				}
			}
		}
	}
	private void addEdges(Node node) // Simple case
	{
		int currentValue = node.nodeValue;
		// Call this if it is not an edge case
		node.addEdge(new Edge(node, graph.get(currentValue-nodeDivisions))); // North
		node.addEdge(new Edge(node, graph.get((currentValue-nodeDivisions)+1))); // NE
		node.addEdge(new Edge(node, graph.get((currentValue-nodeDivisions)-1))); // NW
		node.addEdge(new Edge(node, graph.get(currentValue+nodeDivisions))); // South
		node.addEdge(new Edge(node, graph.get((currentValue+nodeDivisions)+1))); // SE
		node.addEdge(new Edge(node, graph.get((currentValue+nodeDivisions)-1))); // SW
		node.addEdge(new Edge(node, graph.get(currentValue+1))); // East
		node.addEdge(new Edge(node, graph.get(currentValue-1))); // West
	}
	private void addTBMidEdges(Node node, int tb)
	{
		int currentValue = node.nodeValue;
		// top = 0, bottom = 1
		// Call this if it is an edge,middle case
		if(tb == 0)
		{
			node.addEdge(new Edge(node, graph.get((nodeAmount-nodeDivisions)+currentValue-1)));	// Nw
			node.addEdge(new Edge(node, graph.get((nodeAmount-nodeDivisions)+currentValue+2)));	// NE
			node.addEdge(new Edge(node, graph.get((nodeAmount-nodeDivisions)+currentValue)));	// North
			node.addEdge(new Edge(node, graph.get(currentValue+nodeDivisions)));	// South
			node.addEdge(new Edge(node, graph.get(currentValue+nodeDivisions+1)));	// SE
			node.addEdge(new Edge(node, graph.get(currentValue+nodeDivisions-1))); // SW
			node.addEdge(new Edge(node, graph.get(currentValue-1))); // West
			node.addEdge(new Edge(node, graph.get(currentValue+1))); // East
		}
		else
		{
			node.addEdge(new Edge(node, graph.get(currentValue-nodeDivisions))); // North
			node.addEdge(new Edge(node, graph.get(currentValue-nodeDivisions+1))); // NE
			node.addEdge(new Edge(node, graph.get(currentValue-nodeDivisions-1))); // NW
			node.addEdge(new Edge(node, graph.get(currentValue-nodeDivisions*(nodeDivisions-1)))); // South
			node.addEdge(new Edge(node, graph.get((currentValue-nodeDivisions*(nodeDivisions-1))+1)));	// SE
			node.addEdge(new Edge(node, graph.get((currentValue-nodeDivisions*(nodeDivisions-1))-1))); // SW
			node.addEdge(new Edge(node, graph.get((currentValue-1)))); // West
			node.addEdge(new Edge(node, graph.get((currentValue+1)))); // East
		}
	}
	private void addLRMidEdges(Node node, int lr)
	{
		int currentValue = node.nodeValue;
		if(lr == 0)
		{
			node.addEdge(new Edge(node, graph.get(currentValue+nodeDivisions-1))); // West
			node.addEdge(new Edge(node, graph.get(currentValue-1))); // NW
			node.addEdge(new Edge(node, graph.get(currentValue-nodeDivisions+1))); // NE
			node.addEdge(new Edge(node, graph.get(currentValue+1))); // East
			node.addEdge(new Edge(node, graph.get(currentValue+nodeDivisions+1))); // SE
			node.addEdge(new Edge(node, graph.get(currentValue+nodeDivisions))); // South
			node.addEdge(new Edge(node, graph.get(currentValue+(2*nodeDivisions)-1))); // SW
			node.addEdge(new Edge(node, graph.get(currentValue-nodeDivisions))); // North
		}
		else
		{
			node.addEdge(new Edge(node, graph.get(currentValue-(2*nodeDivisions)+1))); // NE
			node.addEdge(new Edge(node, graph.get(currentValue-nodeDivisions+1))); // East
			node.addEdge(new Edge(node, graph.get(currentValue+1))); // SE
			node.addEdge(new Edge(node, graph.get(currentValue+nodeDivisions))); // South
			node.addEdge(new Edge(node, graph.get(currentValue+nodeDivisions-1))); // SW
			node.addEdge(new Edge(node, graph.get(currentValue-1))); // West
			node.addEdge(new Edge(node, graph.get(currentValue-nodeDivisions-1))); // NW
			node.addEdge(new Edge(node, graph.get(currentValue-nodeDivisions))); // North
		}
	}
	private void addEdges(Node node, int tb, int lr)
	{
		int currentValue = node.nodeValue;
		// top-left = 0, bottom-right = 0
		// Call this if it is an edge,edge case
		if(tb == 0 && lr == 0)
		{
			node.addEdge(new Edge(node, graph.get(currentValue+1))); // East
			node.addEdge(new Edge(node, graph.get(currentValue+nodeDivisions+1))); // SE
			node.addEdge(new Edge(node, graph.get(currentValue+nodeDivisions))); // South
			node.addEdge(new Edge(node, graph.get((2*nodeDivisions)-1))); // SW
			node.addEdge(new Edge(node, graph.get(nodeDivisions-1))); // West
			node.addEdge(new Edge(node, graph.get(nodeAmount-1))); // NW
			node.addEdge(new Edge(node, graph.get((nodeAmount-nodeDivisions)))); // North
			node.addEdge(new Edge(node, graph.get(nodeAmount-nodeDivisions+1))); // NE
		}
		else if(tb == 0 && lr == 1)
		{
			node.addEdge(new Edge(node, graph.get(currentValue+nodeDivisions))); // South
			node.addEdge(new Edge(node, graph.get(currentValue+nodeDivisions-1))); // SW
			node.addEdge(new Edge(node, graph.get(currentValue-1))); // West
			node.addEdge(new Edge(node, graph.get(nodeAmount-2))); // Northwest
			node.addEdge(new Edge(node, graph.get(nodeAmount-1))); // North
			node.addEdge(new Edge(node, graph.get(nodeAmount-nodeDivisions))); // NE
			node.addEdge(new Edge(node, graph.get(0))); // East
			node.addEdge(new Edge(node, graph.get(currentValue+1))); // SE
		}
		else if(tb == 1 && lr == 0)
		{
			node.addEdge(new Edge(node, graph.get(currentValue-nodeDivisions))); // North
			node.addEdge(new Edge(node, graph.get(currentValue-nodeDivisions+1))); // NE
			node.addEdge(new Edge(node, graph.get(currentValue+1))); // East
			node.addEdge(new Edge(node, graph.get(1))); // SE
			node.addEdge(new Edge(node, graph.get(0))); // S
			node.addEdge(new Edge(node, graph.get(nodeDivisions-1))); // SW
			node.addEdge(new Edge(node, graph.get(nodeAmount-1))); // West
			node.addEdge(new Edge(node, graph.get(currentValue-1))); // NW
		}
		else if(tb == 1 && lr == 1)
		{
			node.addEdge(new Edge(node, graph.get(currentValue-1))); // West
			node.addEdge(new Edge(node, graph.get(currentValue-nodeDivisions-1))); // NW
			node.addEdge(new Edge(node, graph.get(currentValue-nodeDivisions))); // North
			node.addEdge(new Edge(node, graph.get(currentValue-(2*nodeDivisions)+1))); // NE
			node.addEdge(new Edge(node, graph.get(currentValue-nodeDivisions+1))); // East
			node.addEdge(new Edge(node, graph.get(0))); // SE
			node.addEdge(new Edge(node, graph.get(nodeDivisions-1))); // South
			node.addEdge(new Edge(node, graph.get(nodeDivisions-2))); // SW
		}
	}

	private boolean checkSpace(Toroidal2DPhysics space, double xstart, double xfinish, double ystart, double yfinish)
	{
		// If there is something inside of the bounds of the certain node, return false so that this node will not be added
		// to the search graph. Otherwise return true and add it to the search graph.
		ArrayList<Asteroid> allAsteroidList = globalKnowledge.getAllAsteroidList();
		for(Asteroid asteroid : allAsteroidList)
		{
			double xPos = asteroid.getPosition().getX();
			double yPos = asteroid.getPosition().getY();

			if((xPos >= xstart) && (xPos < xfinish) && (yPos >= ystart) && (yPos < yfinish)) return false;
		}

		Set<AbstractActionableObject> actionableList = globalKnowledge.getActionableList();
		for(AbstractObject actionable : actionableList)
		{
			double xPos = actionable.getPosition().getX();
			double yPos = actionable.getPosition().getY();

			if((xPos >= xstart) && (xPos < xfinish) && (yPos >= ystart) && (yPos < yfinish)) return false;
		}

		return true;
	}
	private void astar(Node start, Node goal)
	{
		// TODO

		/*
		function A*(start, goal)
	    // The set of nodes already evaluated.
	    closedSet := {}
	    // The set of currently discovered nodes still to be evaluated.
	    // Initially, only the start node is known.
	    openSet := {start}
	    // For each node, which node it can most efficient be reach from.
	    // If a node can be reached from many nodes, cameFrom will eventually contain the
	    // most efficient previous step.
	    cameFrom := the empty map

	    // For each node, the cost of getting from the start node to that node.
	    gScore := map with default value of Infinity
	    // The cost of going from start to start is zero.
	    gScore[start] := 0
	    // For each node, the total cost of getting from the start node to the goal
	    // by passing by that node. That value is partly known, partly heuristic.
	    fScore := map with default value of Infinity
	    // For the first node, that value is completely heuristic.
	    fScore[start] := heuristic_cost_estimate(start, goal)

	    while openSet is not empty
	        current := the node in openSet having the lowest fScore[] value
	        if current = goal
	            return reconstruct_path(cameFrom, goal)

	        openSet.Remove(current)
	        closedSet.Add(current)
	        for each neighbor of current
	            if neighbor in closedSet
	                continue		// Ignore the neighbor which is already evaluated.
	            // The distance from start to goal passing through current and the neighbor.
	            tentative_gScore := gScore[current] + dist_between(current, neighbor)
	            if neighbor not in openSet	// Discover a new node
	                openSet.Add(neighbor)
	            else if tentative_gScore >= gScore[neighbor]
	                continue		// This is not a better path.

	            // This path is the best until now. Record it!
	            cameFrom[neighbor] := current
	            gScore[neighbor] := tentative_gScore
	            fScore[neighbor] := gScore[neighbor] + heuristic_cost_estimate(neighbor, goal)

	    return failure

	function reconstruct_path(cameFrom, current)
	    total_path := [current]
	    while current in cameFrom.Keys:
	        current := cameFrom[current]
	        total_path.append(current)
	    return total_path
	    		*/
	}
}
