package holb6595;

import java.util.ArrayList;

import spacesettlers.objects.AbstractObject;
import spacesettlers.utilities.Position;

class Node extends AbstractObject
{
	public int nodeValue;

	// Playing field coordinates: x: (0,800), y: (0,540)
	double xmin;
	double xmax;
	double ymin;
	double ymax;
	double xcenter;
	double ycenter;
	boolean hasAsteroid = false;
	boolean hasShip = false;
	private ArrayList<Edge> edges = new ArrayList<Edge>();
	Position nodePosition;

	// We might only actually need xmax/ymax to create the center points
	Node(int node, double xmin, double xmax, double ymin, double ymax)
	{
		// TODO SUPER BIG TODO! NEED TO MANAGE HOW BIG THE RADIUS OF THIS OBJECT IS. IF ITS LARGER THAN THE NODE SIZE THIS IS A BIG PROBLEM
		// TODO NEED TO DYNAMICALLY CREATE THE RADIUS DEPENDING ON HOW MANY NODES THERE ARE.
		super(0,5);

		this.nodeValue = node;
		this.xmin = xmin;
		this.ymin = ymin;
		this.xmax = xmax;
		this.ymax = ymax;

		this.xcenter = (xmax/2);
		this.ycenter = (ymax/2);
		nodePosition = new Position(xcenter,ycenter);
		setPosition(nodePosition);
	}

	@Override
	public AbstractObject deepClone()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<Edge> getEdges()
	{
		return edges;
	}

	public void addEdge(Edge edge)
	{
		edges.add(edge);
	}
}
