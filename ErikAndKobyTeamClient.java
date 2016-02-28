package holb6595;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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

public class ErikAndKobyTeamClient extends TeamClient{

	private static final double MIN_ENERGY = 2000;
	private GlobalKR globalKnowledge = null;
	private UUID shipOne;
	private UUID shipTwo;

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
					globalKnowledge.update(space, ship);
				}
				if (current == null || current.isMovementFinished(space))
				{
					if (globalKnowledge.getMyEnergy() < MIN_ENERGY)
					{
						//ship.resetEnergy(); // placeholder

						// Go to base
						AbstractAction goToBase = new MoveToObjectAction(space, globalKnowledge.getMyShip().getPosition(), globalKnowledge.getHomeBase());
						actions.put(ship.getId(), goToBase);
					}
					else
					{
						AbstractAction goToResource = new MoveToObjectAction(space, globalKnowledge.getMyShip().getPosition(), globalKnowledge.getNearResource());
						actions.put(ship.getId(), goToResource);
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
	}

	@Override
	public Map<UUID, SpaceSettlersPowerupEnum> getPowerups(Toroidal2DPhysics space,
			Set<AbstractActionableObject> actionableObjects) {
		// TODO Auto-generated method stub

		// Code used from randomteamclient in order to have our ship fire
		HashMap<UUID, SpaceSettlersPowerupEnum> powerupMap = new HashMap<UUID, SpaceSettlersPowerupEnum>();

		/*
		for (AbstractObject actionable :  actionableObjects)
		{
			if (actionable instanceof Ship)
			{
				Ship ship = (Ship) actionable;
				if (myKnolwedge.enemyNear)
				{
					AbstractWeapon newBullet = ship.getNewWeapon(SpaceSettlersPowerupEnum.FIRE_MISSILE);
					if (newBullet != null)
					{
						powerupMap.put(ship.getId(), SpaceSettlersPowerupEnum.FIRE_MISSILE);
						//System.out.println("Firing!");
					}
				}
			}
		}
		return powerupMap;
		*/
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

}
