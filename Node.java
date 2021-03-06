package holb6595;

import java.util.ArrayList;

import spacesettlers.objects.AbstractObject;
import spacesettlers.utilities.Position;

class Node extends AbstractObject
{
	private int nodeGraphKey;

	private double xmin;
	private double xmax;
	private double ymin;
	private double ymax;
	private double xcenter;
	private double ycenter;
	private boolean hasAsteroid = false;
	private boolean hasShip = false;
	private Node parent = null;
	private ArrayList<Edge> edges = new ArrayList<Edge>();
	Position nodePosition;


	Node()
	{
		super(10,10);
	}
	// TODO We might only actually need xmax/ymax to create the center points
	Node(int nodeKey, double xmin, double xmax, double ymin, double ymax)
	{
		// TODO Note we need to make sure our radius is not larger than the node boundaries itself.
		// Reference radius: Ship = 15
		// Super(int mass, int radius);
		super(0,10);

		this.nodeGraphKey = nodeKey;
		this.xmin = xmin;
		this.ymin = ymin;
		this.xmax = xmax;
		this.ymax = ymax;

		this.xcenter = (xmax/2);
		this.ycenter = (ymax/2);
		nodePosition = new Position(xcenter,ycenter);
		setPosition(nodePosition);
	}

	/**
	 * AbstractObject interface required this method - not used
	 */
	@Override
	public AbstractObject deepClone()
	{
		// TODO Auto-generated method stub
		return null;
	}

	//
	// Mutators
	/**
	 * Adds an edge to this node.
	 * @param edge
	 */
	public void addEdge(Edge edge)
	{
		// First check if the edge is valid.
		edges.add(edge);
	}

	//
	// Accessors
	/**
	 * Returns the array list of edges for this node
	 * @return
	 */
	public ArrayList<Edge> getEdges()
	{
		return edges;
	}
	/**
	 * Returns the key that allows you to find this node in the graph
	 * @return
	 */
	public int getNodeGraphKey()
	{
		return nodeGraphKey;
	}

	/**
	 * @return a list of all children nodes
	 */
	public ArrayList<Node> getChildren() {
		ArrayList<Node> children = new ArrayList<Node>();

		if(edges.size() > 0)
		{
				for (Edge e : edges) {
				children.add(e.getEndNode());
			}
		}

		return children;
	}

	public double getXCenter() {
		return xcenter;
	}

	public double getYCenter() {
		return ycenter;
	}

	public void setParent(Node nextNode) {
		parent = nextNode;

	}

	public Node getParent() {
		// TODO Auto-generated method stub
		return parent;
	}

	public boolean hasChildren()
	{
		return edges.isEmpty();
	}
}

