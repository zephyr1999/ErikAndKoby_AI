package holb6595;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import spacesettlers.actions.AbstractAction;
import spacesettlers.actions.DoNothingAction;
import spacesettlers.actions.MoveToObjectAction;
import spacesettlers.actions.PurchaseCosts;
import spacesettlers.actions.PurchaseTypes;
import spacesettlers.clients.TeamClient;
import spacesettlers.graphics.SpacewarGraphics;
import spacesettlers.objects.AbstractActionableObject;
import spacesettlers.objects.AbstractObject;
import spacesettlers.objects.Ship;
import spacesettlers.objects.powerups.SpaceSettlersPowerupEnum;
import spacesettlers.objects.resources.ResourcePile;
import spacesettlers.simulator.Toroidal2DPhysics;
import spacesettlers.utilities.Position;

public class ErikAndKobyTeamClient extends TeamClient{

	private static final double MIN_ENERGY = 4000;
	private GlobalKR globalKnowledge = null;
	private UUID shipOne;
	private UUID shipTwo;

	private static final int DIVISIONS = 16;
	private static final int MOVES = 10;

	@Override
	public Map<UUID, AbstractAction> getMovementStart(Toroidal2DPhysics space,
			Set<AbstractActionableObject> actionableObjects) {

		/**
		 * Called before an action begins.  Return a hash map of ids to SpacewarActions
		 *
		 * @param space physics
		 * @param actionableObjects the ships and bases for this team
		 * @param random random number generator
		 * @return
		 */

		// this is the return set of actions to be completed
		HashMap<UUID, AbstractAction> actions = new HashMap<UUID, AbstractAction>();


		// loop through each ship
		for (AbstractObject actionable :  actionableObjects) {
			if (actionable instanceof Ship) {
				Ship ship = (Ship) actionable;
				AbstractAction current = ship.getCurrentAction();

				if (globalKnowledge == null)
				{
					//first time its called for this ship, so initialize knolwedge
					globalKnowledge = new GlobalKR(space, ship);
					shipOne = ship.getId();
				}
				else if(globalKnowledge.myID == ship.getId())
				{
					// Update the KR
					//globalKnowledge.update(space, ship);
				}
				if (current == null || current.isMovementFinished(space))
				{
					if (globalKnowledge.getMyEnergy() < MIN_ENERGY)
					{
						// low energy, find a beacon!
						if (globalKnowledge.hasNextMove())
						{
							actions.put(ship.getId(), globalKnowledge.nextMove());
						}
						else
						{
							globalKnowledge.update(space, ship);
							ArrayList<AbstractAction> targetMoves = aStar(space, globalKnowledge, globalKnowledge.getMyShip().getPosition(), globalKnowledge.getNearBeacon());
							for(AbstractAction move : targetMoves)
							{
								globalKnowledge.putMove(move);
							}
						}
					}
					else if(globalKnowledge.getMyShip().getResources().getTotal() > 550)
					{
						// If we have enough resources take them back to the base
						// Go to base to drop off resources

						if (globalKnowledge.hasNextMove())
						{
							actions.put(ship.getId(), globalKnowledge.nextMove());
						}
						else
						{
							globalKnowledge.update(space, ship);
							ArrayList<AbstractAction> targetMoves = aStar(space, globalKnowledge, globalKnowledge.getMyShip().getPosition(), globalKnowledge.getHomeBase());
							for(AbstractAction move : targetMoves)
							{
								globalKnowledge.putMove(move);
							}
						}
					}
					else
					{
						if (globalKnowledge.hasNextMove())
						{
							actions.put(ship.getId(), globalKnowledge.nextMove());
						}
						else
						{
							//System.out.println("astar");
							globalKnowledge.update(space, ship);
							ArrayList<AbstractAction> targetMoves = aStar(space, globalKnowledge, globalKnowledge.getMyShip().getPosition(), globalKnowledge.getNearResource());
							for(AbstractAction move : targetMoves)
							{
								globalKnowledge.putMove(move);
							}
						}
					}
				}
				else
				{
					actions.put(ship.getId(), ship.getCurrentAction());
				}
			}
			else
			{
				// it is a base.  Heuristically decide when to use the shield (TODO)
				actions.put(actionable.getId(), new DoNothingAction());
			}
		}
		return actions;
	}

	@Override
	public void getMovementEnd(Toroidal2DPhysics space, Set<AbstractActionableObject> actionableObjects) {
		// TODO Auto-generated method stub
		/**
		 * Called when actions end but before time advances.  Can be used to see
		 * if a ship died or other useful physics checks.
		 *
		 * @param space
		 * @param actionableObjects the ships and bases for this team
		 * @param ships
		 */

		if((globalKnowledge.getMyEnergy() > MIN_ENERGY) && globalKnowledge.nextToBase())
		{
			//we've hit the base and need to clear actions
		//	current = null;
			globalKnowledge.clearNextMoves();

		}
	}

	@Override
	public Map<UUID, SpaceSettlersPowerupEnum> getPowerups(Toroidal2DPhysics space,
			Set<AbstractActionableObject> actionableObjects) {
		// TODO Auto-generated method stub

		// Code used from randomteamclient in order to have our ship fire
		HashMap<UUID, SpaceSettlersPowerupEnum> powerupMap = new HashMap<UUID, SpaceSettlersPowerupEnum>();

		return null;
	}

	@Override
	public Map<UUID, PurchaseTypes> getTeamPurchases(Toroidal2DPhysics space,
			Set<AbstractActionableObject> actionableObjects, ResourcePile resourcesAvailable,
			PurchaseCosts purchaseCosts) {
		// TODO Auto-generated method stub
		/**
		 * Called once per turn to see if the team wants to purchase anything with its
		 * existing currently available resources.  Can only purchase one item per turn.
		 *
		 * @param space
		 * @param actionableObjects
		 * @param resourcesAvailable how much resourcesAvailable you have
		 * @param clonedPurchaseCost how much each type of purchase currently costs for this team
		 * @return
		 */
		return null;
	}

	@Override
	public void initialize(Toroidal2DPhysics space) {
		// TODO initialize myKnowledge
		/**
		 * Called when the client is created
		 */
	}

	@Override
	public void shutDown(Toroidal2DPhysics space) {
		// TODO Auto-generated method stub
		/**
		 * Called when the client is shut down (which is at the end of a game)
		 */
	}

	@Override
	public Set<SpacewarGraphics> getGraphics() {
		// TODO Auto-generated method stub
		/**
		 * Return any graphics that the team client wants to draw
		 * @return a set of objects that extend the SpacewarGraphics class
		 */
		return null;
	}


	/**
	 * takes in the space, KR, ship location and target object.
	 * Then creates a graph that will create a gridded space and each space (squares) will be a node.
	 * A* will then perform a search on these nodes to take the ship to the target in the best path.
	 * A* returns an arraylist of actions to be performed.
	 * @param space
	 * @param knowledge
	 * @param currentLocation
	 * @param goalObject
	 * @return
	 */
	private static ArrayList<AbstractAction> aStar (Toroidal2DPhysics space, GlobalKR knowledge, Position currentLocation, AbstractObject goalObject) {
		//arraylist to be returned
		ArrayList<AbstractAction> movements = new ArrayList<AbstractAction>();

		//create graph from space
		Graph g = new Graph(knowledge, goalObject.getPosition(), DIVISIONS);
		//System.out.println(g.graphSize());

		if (g.startIsSameAsFinish()) {
			// the start node and finish nodes are the same, so return a single line movement to target
			movements.add(new MoveToObjectAction(space, knowledge.getMyShip().getPosition(), goalObject));
		}
		else {
			// nodes which have been evaluated
			ArrayList<Node> closed = new ArrayList<Node>();
			//instead of doing a bullshit priority queue, just do a treemap with <f_value, Node> and sort when needed
			TreeMap<Integer, Node> fringe = new TreeMap<Integer, Node>();

			//initialize first node to search
			Node nextNode = g.getStartNode();

			//initialize fringe
			if(!nextNode.hasChildren()){
				for (Node n : nextNode.getChildren()) {
					n.setParent(nextNode);
					fringe.put(f(n, space, currentLocation, goalObject, g), n);
				}
			}

			while(!fringe.isEmpty()) {
				//update nextNode
				nextNode = fringe.get(fringe.firstKey());
				fringe.remove(fringe.firstKey());
				//if we've hit the goal, stop
				if (nextNode.getNodeGraphKey() == g.getTargetNode().getNodeGraphKey()) {

					ArrayList<Node> nodesInOrder = getNodeList(nextNode, g);
					for (int i = 0; i < MOVES; i++) {
						AbstractAction act = new MoveToObjectAction(space, nodesInOrder.get(nodesInOrder.size() - i - 1).getPosition(), nodesInOrder.get(nodesInOrder.size() - i - 2));
						movements.add(act);
					}
					break;
				}
				else if (!closed.contains(nextNode)) {
					closed.add(nextNode);
					if(nextNode.hasChildren()){
						for (Node n : nextNode.getChildren()) {
							if (!closed.contains(n)) {
								int index = new ArrayList<Node>(fringe.values()).indexOf(n);
								int fVal = f(n, space, currentLocation, goalObject, g);
								if (((int)fringe.keySet().toArray()[index]) > fVal) {
									n.setParent(nextNode);
									fringe.put(fVal, n);
								}
							}
						}
					}
				}

			}
		}

		return movements;
	}


	private static ArrayList<Node> getNodeList(Node nextNode, Graph g) {
		Node current = nextNode;
		ArrayList<Node> nodesInOrder = new ArrayList<Node>();
		nodesInOrder.add(current);
		while(current.getNodeGraphKey() != g.getStartNode().getNodeGraphKey()) {
			current = current.getParent();
			nodesInOrder.add(current);
		}
		return nodesInOrder;
	}

	private static int f(Node n, Toroidal2DPhysics space, Position currentLocation, AbstractObject goalObject, Graph g) {
		// TODO Auto-generated method stub
		return h(n, space, currentLocation, goalObject, g) + g(n, space, currentLocation, goalObject, g);
	}

	private static int g(Node n, Toroidal2DPhysics space, Position currentLocation, AbstractObject goalObject, Graph g) {
		// TODO Auto-generated method stub
		if (g.getStartNode().getNodeGraphKey() ==  n.getNodeGraphKey()) {
			return 0;
		}
		else {
			return 1 + g(n.getParent(), space, currentLocation, goalObject, g);
		}
	}

	private static int h(Node n, Toroidal2DPhysics space, Position currentLocation, AbstractObject goalObject, Graph g) {
		// TODO Auto-generated method stub
		return (int) Math.floor(space.findShortestDistance(currentLocation, goalObject.getPosition())/Math.min(g.getXMax(), g.getYMax())/DIVISIONS);
	}

}

