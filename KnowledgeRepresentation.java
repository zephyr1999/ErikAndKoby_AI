package holb6595;

import spacesettlers.objects.Asteroid;
import spacesettlers.objects.Base;
import spacesettlers.objects.Ship;
import spacesettlers.simulator.Toroidal2DPhysics;
import spacesettlers.utilities.Position;

public class KnowledgeRepresentation {
	//This class is implimented with a egocentric model
	//Koby, you can make the second one with a global viewpoint
	// basically, you could leave the fields the same and just modify how they are set in the methods
	//TODO necessary fields:
	/*
	 * myVelocity
	 * enemyVelocity
	 * resourceVelocity
	 */

	final double NEAR_DISTANCE = 50000.0;
	Position myPosition;
	double myEnergy;
	boolean enemyNear;
	Position enemyPosition;
	Ship nearestShip;
	boolean resourceNear;
	Position resourcePosition;
	Asteroid nearestResource;
	Base myHomeBase;


	//TODO necessary methods
	/*
	 * getters & setters
	 */
	public KnowledgeRepresentation(Toroidal2DPhysics space, Ship ship) {
		/*this.myPosition = ship.getPosition();
		this.myEnergy = ship.getEnergy();
		this.nearestShip = getNearestShip(space, ship);
		this.enemyNear = (space.findShortestDistance(ship.getPosition(), this.nearestShip.getPosition()) < NEAR_DISTANCE);
		this.nearestResource = getNearestResource(space,ship);
		this.resourceNear = (space.findShortestDistance(ship.getPosition(), this.nearestResource.getPosition()) < NEAR_DISTANCE);
		this.enemyPosition = nearestShip.getPosition();
		this.resourcePosition = nearestResource.getPosition();*/
		update(space,ship);
	}

	public Asteroid getNearestResource(Toroidal2DPhysics space, Ship ship) {
		double minDistance = Double.POSITIVE_INFINITY;
		Asteroid ast = null;
		for(Asteroid a : space.getAsteroids()) {
			if(a.isMineable())
			{
				double distance = space.findShortestDistance(ship.getPosition(), a.getPosition());
				if (distance < minDistance) {
					minDistance = distance;
					ast = a;
				}
			}
		}

		return ast;
	}

	public Ship getNearestShip(Toroidal2DPhysics space, Ship ship){
		double minDistance = Double.POSITIVE_INFINITY;
		Ship nearestShip = null;

		for (Ship othership : space.getShips()) {
			// don't aim for our own team (or ourself)
			if (othership.getTeamName().equals(ship.getTeamName())) {
				continue;
			}

			double distance = space.findShortestDistance(ship.getPosition(), othership.getPosition());
			if (distance < minDistance) {
				minDistance = distance;
				nearestShip = othership;
			}
		}
		return nearestShip;
	}


	public void update(Toroidal2DPhysics space, Ship ship)
	{
		this.myPosition = ship.getPosition();
		this.myEnergy = ship.getEnergy();
		this.nearestShip = getNearestShip(space, ship);
		this.enemyNear = (space.findShortestDistance(ship.getPosition(), this.nearestShip.getPosition()) < NEAR_DISTANCE);
		this.nearestResource = getNearestResource(space,ship);
		this.resourceNear = (space.findShortestDistance(ship.getPosition(), this.nearestResource.getPosition()) < NEAR_DISTANCE);
		this.enemyPosition = nearestShip.getPosition();
		this.resourcePosition = nearestResource.getPosition();

		// Set base locations
		for(Base base : space.getBases())
		{
			// if base is ours set ourbase loc
			if(base.getTeamName().equals(ship.getTeamName()))
				myHomeBase = base;
		}
	}
}
