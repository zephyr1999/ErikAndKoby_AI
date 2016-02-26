package holb6595;

import java.util.ArrayList;

import spacesettlers.objects.Asteroid;
import spacesettlers.objects.Base;
import spacesettlers.objects.Ship;
import spacesettlers.simulator.Toroidal2DPhysics;
import spacesettlers.utilities.Position;

public class KnowledgeRep2
{
	// This knowledge representation will be for a global perspective
	private Position myPosition;
	private Position friendlyPosition;
	private double myEnergy;
	private double friendlyEnergy;
	private Base myHomeBase;

	private ArrayList<Position> enemyHome = new ArrayList<Position>();
	private ArrayList<Position> enemyBase = new ArrayList<Position>();

	private ArrayList<Position> enemyLocList = new ArrayList<Position>();
	private ArrayList<Double> enemyEnergyList = new ArrayList<Double>();

	private ArrayList<Position> resourceLocList = new ArrayList<Position>();
	private ArrayList<Position> asteroidLocList = new ArrayList<Position>();

	private Ship nearestShip = null;
	private double distanceToNearestEnemy;
	private Asteroid nearestResource = null;
	private double distanceToNearResource;

	public boolean isGlobal = true;
	public KnowledgeRep2(Toroidal2DPhysics space, Ship ship)
	{
		// Set my position and energy
		myPosition = ship.getPosition();
		myEnergy = ship.getEnergy();

		// Set the position/energy/distance of all ships
		for(Ship othership : space.getShips())
		{
			// If the ship is ours add to our locations/energy
			// Else add it to the enemy locations
			if(othership.getTeamName().equals(ship.getTeamName()) && !(othership.getId().equals(ship.getId())))
			{
				friendlyPosition = othership.getPosition();
				friendlyEnergy = othership.getEnergy();
			}
			else
			{
				enemyLocList.add(othership.getPosition());
				enemyEnergyList.add(othership.getEnergy());

				// Set which ship is the closest to our own
				if(nearestShip == null)
				{
					nearestShip = othership;
					distanceToNearestEnemy = space.findShortestDistance(ship.getPosition(), othership.getPosition());
				}
				else
				{
					double distance = space.findShortestDistance(ship.getPosition(), othership.getPosition());
					if(distance < distanceToNearestEnemy)
					{
						nearestShip = othership;
						distanceToNearestEnemy = distance;
					}
				}
			}

			// Set the position of the asteroids/resources
			for(Asteroid asteroid : space.getAsteroids())
			{
				// if it's mineable add it to resource list
				// else add to asteroid list
				if(asteroid.isMineable())
				{
					resourceLocList.add(asteroid.getPosition());

					// Check if this resource is the closest to our ship.
					// If so update
					if(nearestResource == null)
					{
						nearestResource = asteroid;
						distanceToNearResource = space.findShortestDistance(ship.getPosition(), asteroid.getPosition());
					}
					else
					{
						double distance = space.findShortestDistance(ship.getPosition(), asteroid.getPosition());
						if(distance < distanceToNearResource)
						{
							nearestResource = asteroid;
							distanceToNearResource = distance;
						}
					}
				}
				else
					asteroidLocList.add(asteroid.getPosition());
			}
		}

		// Set positions for resources/asteroids
		for(Asteroid asteroid : space.getAsteroids())
		{
			if(asteroid.isMineable())
				resourceLocList.add(asteroid.getPosition());
			else
				asteroidLocList.add(asteroid.getPosition());

		}

		// Set base locations
		for(Base base : space.getBases())
		{
			// if base is ours set ourbase loc
			if(base.getTeamName().equals(ship.getTeamName()))
				myHomeBase = base;
			else if(!(base.isHomeBase()))
				enemyHome.add(base.getPosition());
			else
				enemyBase.add(base.getPosition());
		}
	}




	public Base getHomeBase()
	{
		return myHomeBase;
	}



	// TODO
	private ArrayList<Position> getShipsLoc(Toroidal2DPhysics space, Ship ship)
	{
		Ship nearest = null;
		for(Ship othership : space.getShips())
		{
			// I
		}
		return null;
	}
}
