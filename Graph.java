package holb6595;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import spacesettlers.objects.AbstractActionableObject;
import spacesettlers.objects.AbstractObject;
import spacesettlers.objects.Asteroid;
import spacesettlers.utilities.Position;


/**
 *
 * @author kobypascual
 *
 */
class Graph
{
	// Koby wrote this while drinking and thinking aloud!

	/**
	 * Coordinates of playing field.
	 * xmin = west most
	 */
	private double xmin = 0.0;

	/**
	 * Coordinates of playing field.
	 * xmax = east most
	 */
	private double xmax = 800.0;
	//private double xmax = 1230;

	/**
	 * Coordinates of playing field.
	 * ymin = north most
	 */
	private double ymin = 0.0;

	/**
	 * Coordinates of playing field.
	 * ymax = south most
	 */
	private double ymax = 540.0;
//	private double ymax = 750.0;

	/**
	 * 	We pass in an integer value 'divisions' to determine how many squares we want to create.
	 *	Divisions = #ofColumns and #ofRows
	 */
	private int nodeDivisions = 0;

	/**
	 * Total Nodes will be created based on divisions.
	 * Total Nodes = nodeDivisions*nodeDivisions
	 */
	private int totalNodes = 0;

	/**
	 * Pass in the target position to find the node it lies in.
	 */
	Position targetPosition;

	/**
	 * Value will hold startNode when found.
	 */
	private Node startNode = null;

	/**
	 * Value will hold endNode when found.
	 */
	private Node targetNode = null;

	/**
	 * Global Knowledge Representation we'll use.
	 */
	private GlobalKR globalKnowledge;

	/**
	 * Graph that holds all nodes.
	 * Stored as a hashmap.
	 * Integer values from 0-(totalNodes-1)
	 * Non-valid nodes not stored here.
	 */
	private Map<Integer, Node> graph = new HashMap<Integer, Node>();

	/**
	 * Boolean indicating start and finish are the same node. should be checked during aStar.
	 */
	private Boolean startAndFinishAreSame = false;

	Graph(GlobalKR globalKnowledge, Position target, int divisions)
	{
		this.nodeDivisions = divisions;
		this.totalNodes = nodeDivisions*nodeDivisions;
		this.globalKnowledge = globalKnowledge;
		this.targetPosition = target;

		// Find the x and y domains that each node will have.
		// xdomain = distance from left boundary to right boundary (for a single node)
		// ydomain = distance from top boundary to bottom boundary (for a single node)
		double xdomain = xmax/nodeDivisions;
		double ydomain = ymax/nodeDivisions;

		int startID =  (divisions * (int)Math.floor(globalKnowledge.getMyShip().getPosition().getY()/ydomain)) + (int)Math.floor(globalKnowledge.getMyShip().getPosition().getX() / xdomain);
		double startX = Math.floor(globalKnowledge.getMyShip().getPosition().getX() / xdomain) * xdomain;
		double endX = startX + xdomain;
		double startY = Math.floor(globalKnowledge.getMyShip().getPosition().getY() / ydomain) * ydomain;
		double endY = startY + ydomain;
		startNode = new Node(startID, startX, endX, startY, endY);
		graph.put(startID, startNode);

		int targetID =  (divisions * (int)Math.floor(globalKnowledge.getMyShip().getPosition().getY()/ydomain)) + (int)Math.floor(globalKnowledge.getMyShip().getPosition().getX() / xdomain);
		startX = Math.floor(targetPosition.getX() / xdomain) * xdomain;
		endX = startX + xdomain;
		startY = Math.floor(targetPosition.getY() / ydomain) * ydomain;
		endY = startY + ydomain;
		targetNode = new Node(targetID, startX, endX, startY, endY);
		graph.put(targetID, targetNode);
		if(targetID == startID) startAndFinishAreSame = true;

		// Now we have the amount of nodes we want to cut the map into. Create that many nodes

		// Start from origin (0.0,0.0)
		double xstart = xmin;
		double ystart = ymin;

		// Loop through every potential node. Create and add them to the graph.
		for(int currentNodeGraphKey = 0; currentNodeGraphKey < totalNodes; currentNodeGraphKey++)
		{
			if(currentNodeGraphKey == targetID || currentNodeGraphKey == startID) continue;

			double xfinish = xdomain + xstart;
			double yfinish = ydomain + ystart;

			// First check the content of the node.
			// If checkNodeContent = 0: node is empty, create node, add to graph
			// If checkNodeContent = 1: node is start, create node, add to graph
			// If checkNodeContent = 2: node is target, create node, add to graph
			// If checkNodeContent = 3: node is start and target, NO NEED TO SEARCH. JUST A SIMPLE MOVE TO EXACT TARGET COORDINATE
			// If checkNodeContent = -1: node is obstructed, don't create node
			int nodeContent = checkNodeContent(xstart, xfinish, ystart, yfinish);

			if(nodeContent == 0)
			{
				if((((currentNodeGraphKey+1)%nodeDivisions)) == 0)
				{
					// This node is on the far right. Create, add
					Node currentNode = new Node(currentNodeGraphKey, xstart, xfinish, ystart, yfinish);
					graph.put(currentNodeGraphKey, currentNode);

					// update coordinates
					// At the end increment the xstart to the xorigin, ystart down 1 ydomain
					xstart = xmin;
					ystart += ydomain;
				}
				else
				{
					// This node is not on the far right yet. Create, add
					Node currentNode = new Node(currentNodeGraphKey, xstart, xfinish, ystart, yfinish);
					graph.put(currentNodeGraphKey, currentNode);

					// update coordinates
					// ystart stays pat, xstart incremented to the right by 1 xdomain
					xstart += xdomain;
				}
			}
			else if(nodeContent == 1)
			{
				System.out.println("start");
				if((((currentNodeGraphKey+1)%nodeDivisions)) == 0)
				{
					// This node is on the far right. Create, add
					// Also, this is the startNode
					startNode = new Node(currentNodeGraphKey, xstart, xfinish, ystart, yfinish);
					graph.put(currentNodeGraphKey, startNode);

					// update coordinates
					// At the end increment the xstart to the xorigin, ystart down 1 ydomain
					xstart = xmin;
					ystart += ydomain;
				}
				else
				{
					// This node is not on the far right yet. Create, add
					// Also, this is the startNode
					Node startNode = new Node(currentNodeGraphKey, xstart, xfinish, ystart, yfinish);
					graph.put(currentNodeGraphKey, startNode);

					// update coordinates
					// ystart stays pat, xstart incremented to the right by 1 xdomain
					xstart += xdomain;
				}
			}
			else if(nodeContent == 2)
			{
				System.out.println("target");
				if((((currentNodeGraphKey+1)%nodeDivisions)) == 0)
				{
					// This node is on the far right. Create, add
					// Also, this is the targetNode
					targetNode = new Node(currentNodeGraphKey, xstart, xfinish, ystart, yfinish);
					graph.put(currentNodeGraphKey, targetNode);

					// update coordinates
					// At the end increment the xstart to the xorigin, ystart down 1 ydomain
					xstart = xmin;
					ystart += ydomain;
				}
				else
				{
					// This node is not on the far right yet. Create, add
					// Also, this is the targetNode
					Node targetNode = new Node(currentNodeGraphKey, xstart, xfinish, ystart, yfinish);
					graph.put(currentNodeGraphKey, targetNode);

					// update coordinates
					// ystart stays pat, xstart incremented to the right by 1 xdomain
					xstart += xdomain;
				}
			}
			else if(nodeContent == 3)
			{
				// start AND target node
				// if this happens we need to quit this somehow
				// shouldn't call astar
				// just make simple action to targets position
				startAndFinishAreSame = true;
			}
			else
			{
				// Node is Obstructed. Do nothing.
			}
		}

		// Now that the nodes have been created, attach each edge to each node
		for(int currentNodeGraphKey : graph.keySet())
		{
			// This is the current node we are going to add edges to.
			// CurrentNodeGraphKey is just the key that is used to map the node to the hashmap.
			Node currentNode = graph.get(currentNodeGraphKey);
			if(currentNodeGraphKey >= 0 && currentNodeGraphKey < nodeDivisions)
			{
				if(currentNodeGraphKey%nodeDivisions == 0)
				{
					// Case: Special. Top Left corner node
					// Node is on top row, left column.
					if(isNodeValid(currentNodeGraphKey+1))
						currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey+1))); 									// East
					if(isNodeValid(currentNodeGraphKey+nodeDivisions+1))
						currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey+nodeDivisions+1))); 					// SE
					if(isNodeValid(currentNodeGraphKey+nodeDivisions))
						currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey+nodeDivisions))); 						// South
					if(isNodeValid((2*nodeDivisions)-1))
						currentNode.addEdge(new Edge(currentNode, graph.get((2*nodeDivisions)-1))); 									// SW
					if(isNodeValid(nodeDivisions-1))
						currentNode.addEdge(new Edge(currentNode, graph.get(nodeDivisions-1))); 										// West
					if(isNodeValid(totalNodes-1))
						currentNode.addEdge(new Edge(currentNode, graph.get(totalNodes-1))); 											// NW
					if(isNodeValid(totalNodes-nodeDivisions))
						currentNode.addEdge(new Edge(currentNode, graph.get(totalNodes-nodeDivisions))); 								// North
					if(isNodeValid(totalNodes-nodeDivisions+1))
						currentNode.addEdge(new Edge(currentNode, graph.get(totalNodes-nodeDivisions+1))); 								// NE
				}
				else if((currentNodeGraphKey+1)%nodeDivisions == 0)
				{
					// Case: Special. Top right corner node
					// Node is on top row, right column
					if(isNodeValid(currentNodeGraphKey+nodeDivisions))
						currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey+nodeDivisions))); 						// South
					if(isNodeValid(currentNodeGraphKey+nodeDivisions-1))
						currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey+nodeDivisions-1))); 					// SW
					if(isNodeValid(currentNodeGraphKey-1))
						currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey-1))); 									// West
					if(isNodeValid(totalNodes-2))
						currentNode.addEdge(new Edge(currentNode, graph.get(totalNodes-2))); 											// NW
					if(isNodeValid(totalNodes-1))
						currentNode.addEdge(new Edge(currentNode, graph.get(totalNodes-1))); 											// North
					if(isNodeValid(totalNodes-nodeDivisions))
						currentNode.addEdge(new Edge(currentNode, graph.get(totalNodes-nodeDivisions))); 								// NE
					if(isNodeValid(0))
						currentNode.addEdge(new Edge(currentNode, graph.get(0))); 														// East
					if(isNodeValid(currentNodeGraphKey+1))
						currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey+1))); 									// SE
				}
				else
				{
					// Case: Top row only
					// Node is on top row, but not a corner node.
					if(isNodeValid((totalNodes-nodeDivisions)+currentNodeGraphKey-1))
						currentNode.addEdge(new Edge(currentNode, graph.get((totalNodes-nodeDivisions)+currentNodeGraphKey-1)));		// NW
					if(isNodeValid((totalNodes-nodeDivisions)+currentNodeGraphKey+2))
						currentNode.addEdge(new Edge(currentNode, graph.get((totalNodes-nodeDivisions)+currentNodeGraphKey+2)));		// NE
					if(isNodeValid((totalNodes-nodeDivisions)+currentNodeGraphKey))
						currentNode.addEdge(new Edge(currentNode, graph.get((totalNodes-nodeDivisions)+currentNodeGraphKey)));			// North
					if(isNodeValid(currentNodeGraphKey+nodeDivisions))
						currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey+nodeDivisions)));						// South
					if(isNodeValid(currentNodeGraphKey+nodeDivisions+1))
						currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey+nodeDivisions+1)));						// SE
					if(isNodeValid(currentNodeGraphKey+nodeDivisions-1))
						currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey+nodeDivisions-1))); 					// SW
					if(isNodeValid(currentNodeGraphKey-1))
						currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey-1))); 									// West
					if(isNodeValid(currentNodeGraphKey+1))
						currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey+1))); 									// East
				}

			}
			else if(currentNodeGraphKey >= (totalNodes-nodeDivisions) && currentNodeGraphKey < (totalNodes))
			{
				if(currentNodeGraphKey%nodeDivisions == 0)
				{
					// Case: Special. Bottom Left corner node
					// Node is on bottom row, left column.
					if(isNodeValid(currentNodeGraphKey-nodeDivisions))
						currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey-nodeDivisions))); 						// North
					if(isNodeValid(currentNodeGraphKey-nodeDivisions+1))
						currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey-nodeDivisions+1))); 					// NE
					if(isNodeValid(currentNodeGraphKey+1))
						currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey+1))); 									// East
					if(isNodeValid(1))
						currentNode.addEdge(new Edge(currentNode, graph.get(1))); 														// SE
					if(isNodeValid(0))
						currentNode.addEdge(new Edge(currentNode, graph.get(0))); 														// South
					if(isNodeValid(nodeDivisions-1))
						currentNode.addEdge(new Edge(currentNode, graph.get(nodeDivisions-1))); 										// SW
					if(isNodeValid(totalNodes-1))
						currentNode.addEdge(new Edge(currentNode, graph.get(totalNodes-1))); 											// West
					if(isNodeValid(currentNodeGraphKey-1))
						currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey-1))); 									// NW
				}
				else if((currentNodeGraphKey+1)%nodeDivisions == 0)
				{
					// Case: Special. Bottom Right corner node
					// Node is on bottom row, right column.
					if(isNodeValid(currentNodeGraphKey-1))
						currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey-1))); 									// West
					if(isNodeValid(currentNodeGraphKey-nodeDivisions-1))
						currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey-nodeDivisions-1))); 					// NW
					if(isNodeValid(currentNodeGraphKey-nodeDivisions))
						currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey-nodeDivisions))); 						// North
					if(isNodeValid(currentNodeGraphKey-(2*nodeDivisions)+1))
						currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey-(2*nodeDivisions)+1))); 				// NE
					if(isNodeValid(currentNodeGraphKey-nodeDivisions+1))
						currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey-nodeDivisions+1))); 					// East
					if(isNodeValid(0))
						currentNode.addEdge(new Edge(currentNode, graph.get(0))); 														// SE
					if(isNodeValid(nodeDivisions-1))
						currentNode.addEdge(new Edge(currentNode, graph.get(nodeDivisions-1))); 										// South
					if(isNodeValid(nodeDivisions-2))
						currentNode.addEdge(new Edge(currentNode, graph.get(nodeDivisions-2))); 										// SW
				}
				else
				{
					// Case: Bottom row only
					// Node is on bottom row, but not a corner node.
					if(isNodeValid(currentNodeGraphKey-nodeDivisions))
						currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey-nodeDivisions))); 						// North
					if(isNodeValid(currentNodeGraphKey-nodeDivisions+1))
						currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey-nodeDivisions+1))); 					// NE
					if(isNodeValid(currentNodeGraphKey-nodeDivisions-1))
						currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey-nodeDivisions-1))); 					// NW
					if(isNodeValid(currentNodeGraphKey-nodeDivisions*(nodeDivisions-1)))
						currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey-nodeDivisions*(nodeDivisions-1)))); 	// South
					if(isNodeValid((currentNodeGraphKey-nodeDivisions*(nodeDivisions-1))+1))
						currentNode.addEdge(new Edge(currentNode, graph.get((currentNodeGraphKey-nodeDivisions*(nodeDivisions-1))+1)));	// SE
					if(isNodeValid((currentNodeGraphKey-nodeDivisions*(nodeDivisions-1))-1))
						currentNode.addEdge(new Edge(currentNode, graph.get((currentNodeGraphKey-nodeDivisions*(nodeDivisions-1))-1))); // SW
					if(isNodeValid(currentNodeGraphKey-1))
						currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey-1))); 									// West
					if(isNodeValid(currentNodeGraphKey+1))
						currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey+1))); 									// East
				}
			}
			else if(currentNodeGraphKey%nodeDivisions == 0)
			{
				// Case: Left column only
				// Node is on leftmost column, but not a corner node.
				if(isNodeValid(currentNodeGraphKey+nodeDivisions-1))
					currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey+nodeDivisions-1))); 						// West
				if(isNodeValid(currentNodeGraphKey-1))
					currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey-1))); 			   							// NW
				if(isNodeValid(currentNodeGraphKey-nodeDivisions+1))
					currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey-nodeDivisions+1))); 						// NE
				if(isNodeValid(currentNodeGraphKey+1))
					currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey+1))); 			   							// East
				if(isNodeValid(currentNodeGraphKey+nodeDivisions+1))
					currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey+nodeDivisions+1))); 						// SE
				if(isNodeValid(currentNodeGraphKey+nodeDivisions))
					currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey+nodeDivisions)));							// South
				if(isNodeValid(currentNodeGraphKey+(2*nodeDivisions)-1))
					currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey+(2*nodeDivisions)-1)));  					// SW
				if(isNodeValid(currentNodeGraphKey-nodeDivisions))
					currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey-nodeDivisions))); 							// North
			}
			else if((currentNodeGraphKey+1)%nodeDivisions == 0)
			{
				// Case: Right column only
				// Node is on rightmost column, but not a corner node.
				if(isNodeValid(currentNodeGraphKey-(2*nodeDivisions)+1))
					currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey-(2*nodeDivisions)+1))); 					// NE
				if(isNodeValid(currentNodeGraphKey-nodeDivisions+1))
					currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey-nodeDivisions+1))); 						// East
				if(isNodeValid(currentNodeGraphKey+1))
					currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey+1))); 										// SE
				if(isNodeValid(currentNodeGraphKey+nodeDivisions))
					currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey+nodeDivisions))); 							// South
				if(isNodeValid(currentNodeGraphKey+nodeDivisions-1))
					currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey+nodeDivisions-1))); 						// SW
				if(isNodeValid(currentNodeGraphKey-1))
					currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey-1))); 										// West
				if(isNodeValid(currentNodeGraphKey-nodeDivisions-1))
					currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey-nodeDivisions-1))); 						// NW
				if(isNodeValid(currentNodeGraphKey-nodeDivisions))
					currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey-nodeDivisions))); 							// North
			}
			else
			{
				// Case: Simple
				// If node not on the outer edge of game screen
				if(isNodeValid(currentNodeGraphKey-nodeDivisions))
					currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey-nodeDivisions))); 							// North
				if(isNodeValid((currentNodeGraphKey-nodeDivisions)+1))
					currentNode.addEdge(new Edge(currentNode, graph.get((currentNodeGraphKey-nodeDivisions)+1))); 						// NE
				if(isNodeValid((currentNodeGraphKey-nodeDivisions)-1))
					currentNode.addEdge(new Edge(currentNode, graph.get((currentNodeGraphKey-nodeDivisions)-1))); 						// NW
				if(isNodeValid(currentNodeGraphKey+nodeDivisions))
					currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey+nodeDivisions))); 							// South
				if(isNodeValid((currentNodeGraphKey+nodeDivisions)+1))
					currentNode.addEdge(new Edge(currentNode, graph.get((currentNodeGraphKey+nodeDivisions)+1))); 						// SE
				if(isNodeValid((currentNodeGraphKey+nodeDivisions)-1))
					currentNode.addEdge(new Edge(currentNode, graph.get((currentNodeGraphKey+nodeDivisions)-1))); 						// SW
				if(isNodeValid(currentNodeGraphKey+1))
					currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey+1))); 										// East
				if(isNodeValid(currentNodeGraphKey-1))
					currentNode.addEdge(new Edge(currentNode, graph.get(currentNodeGraphKey-1))); 										// West
			}
		}

	}

	/**
	 * Returns the start node
	 * @return
	 */
	public Node getStartNode()
	{
		return startNode;
	}

	/**
	 * Returns the target node
	 * @return
	 */
	public Node getTargetNode()
	{
		return targetNode;
	}

	/**
	 * Takes a potential node and it's coordinates.
	 * return 3 if both your ship and your target are in this node
	 * return 2 if you target is in this node
	 * return 1 if your ship is in this node
	 * return 0 if this node is clear of obstructions
	 * return -1 if any other obstruction is in this node
	 * @param space
	 * @param xstart
	 * @param xfinish
	 * @param ystart
	 * @param yfinish
	 * @return
	 */
	private int checkNodeContent(double xstart, double xfinish, double ystart, double yfinish)
	{
		// 1. Check if your ship is inside of this nodes boundary. If so, goto 2, else goto 3.
		// 2. Check if your target is inside of this nodes boundary. If so, return 3, else return 1.
		// 3. Check if your target is inside of this nodes boundary. If so, return 2, else goto 4.
		// 4. Check if any other obstruction is in this nodes boundary. If so, return -1, else goto 5.
		// 5. Nothing occupies this nodes boundaries. return 0.

		/*
		double shipX = globalKnowledge.getMyShip().getPosition().getX();
		double shipY = globalKnowledge.getMyShip().getPosition().getY();
		double targetX = targetPosition.getX();
		double targetY = targetPosition.getY();

		boolean hasMyShip = false;
		boolean hasMyTarget = false;

		if((shipX >= xstart) && (shipX < xfinish) && (shipY >= ystart) && (shipY < yfinish)) {
			hasMyShip = true;				// Check if ships contained
		}
		if((targetX >= xstart) && (targetX < xfinish) && (targetY >= ystart) && (targetY < yfinish)) {
			hasMyTarget = true;	// Check if target contained
		}
		if(hasMyShip == true && hasMyTarget == true) {
			return 3;																// Both ship & target.  return 3
		}
		else if(hasMyShip == true && hasMyTarget == false) {
			return 1;														// Only ship.			return 1
		}
		else if(hasMyShip == false && hasMyTarget == true) {
			return 2;														// Only target.			return 2
		}
		*/

		ArrayList<Asteroid> allAsteroidList = globalKnowledge.getAllAsteroidList();
		for(Asteroid asteroid : allAsteroidList)
		{
			double xPos = asteroid.getPosition().getX();
			double yPos = asteroid.getPosition().getY();

			if((xPos >= xstart) && (xPos < xfinish) && (yPos >= ystart) && (yPos < yfinish)) return -1;					// Holds obstruction.	return -1
		}

		Set<AbstractActionableObject> actionableList = globalKnowledge.getActionableList();
		for(AbstractObject actionable : actionableList)
		{
			double xPos = actionable.getPosition().getX();
			double yPos = actionable.getPosition().getY();

			if((xPos >= xstart) && (xPos < xfinish) && (yPos >= ystart) && (yPos < yfinish)) return -1;					// Holds obstruction.	return -1
		}

		return 0;																										// Empty node.			return 0
	}

	/**
	 * When checking if a node is valid, pass in its graph search key.
	 * If that graph search key returns null, it isn't in the map. return false
	 * Otherwise, return true
	 * @param currentGraphSearchKey
	 * @return
	 */
	private boolean isNodeValid(int currentGraphSearchKey)
	{
		if(graph.get(currentGraphSearchKey) == null) return false;
		else return true;
	}

	public Boolean startIsSameAsFinish () {
		return startAndFinishAreSame;
	}

	public double getXMax() {
		return xmax;
	}

	public double getYMax() {
		return ymax;
	}
	public int graphSize()
	{
		return graph.size();
	}
}

