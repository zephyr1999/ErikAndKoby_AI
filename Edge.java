package holb6595;

import java.util.UUID;

public class Edge
{
	// Edge will basically just be a pointer to the nodes that it connects
	private UUID startID;
	private Node startNode; // basically just the node that is looking at it's edges
	private Node endNode; // Node that the ship will use an edge to travel to
	Edge(Node start, Node end)
	{
		startNode = start;
		endNode = end;
	}
	
	public Node getEndNode() {
		return endNode;
	}
}

