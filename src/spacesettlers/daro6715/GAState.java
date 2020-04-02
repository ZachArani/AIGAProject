package spacesettlers.daro6715;

import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import spacesettlers.objects.Asteroid;
import spacesettlers.objects.Base;
import spacesettlers.objects.Beacon;
import spacesettlers.objects.Ship;
import spacesettlers.simulator.Toroidal2DPhysics;

/**
 * Our state representation for a GA agent. Keeps track of qualitative data for the ship's resources and distance, the nearest best asteroid (and its resources and distance) and the nearest energy beacon's distance.
 *  
 * @author amy
 *
 */
public class GAState {
	double distanceToNearestMineableAsteroid;
	Asteroid nearestMineableAsteroid;
	int qualDistanceAsteroid = -1; //Qualatative distance to object (asteroid or base). 0=Minimal (<100), 1=Small (<200), 2=Medium (<300) 3=Large (<400), 4=Extra large (>400)
	int qualValueAsteroid = -1; //Qualatative value of asteroid or resources on ship (to return to base).
	int qualValueShip = -1; //Qualitative value of resources on ship.
	int qualDistanceBase = -1; //Qualitative value of distance to home base
	Beacon nearestBeacon;
	double distanceToNearestBeacon;
	int qualDistanceBeacon = -1;
	public GAState(Toroidal2DPhysics space, Ship myShip, HashMap<UUID, UUID> shipToAsteroid, HashMap<UUID, UUID> shipToBeacon) {
		updateState(space, myShip, shipToAsteroid, shipToBeacon);
	}


	/**
	 * Update the distance to the nearest (best) mineable asteroid
	 * 
	 * @param space
	 * @param myShip
	 */
	public void updateState(Toroidal2DPhysics space, Ship myShip, HashMap<UUID, UUID> shipToAsteroid, HashMap<UUID, UUID> shipToBeacon) {
		Set<Asteroid> asteroids = space.getAsteroids();
		distanceToNearestMineableAsteroid = Integer.MAX_VALUE;
		double distance;
		int bestMoney = Integer.MIN_VALUE;
		//Find the nearest best asteroid.
		for (Asteroid asteroid : asteroids) {
			if (!shipToAsteroid.containsValue(asteroid.getId())) {
				if (asteroid.isMineable() && asteroid.getResources().getTotal() > bestMoney) {
					double dist = space.findShortestDistance(asteroid.getPosition(), myShip.getPosition());
					if (dist < distanceToNearestMineableAsteroid) {
						bestMoney = asteroid.getResources().getTotal();
						//System.out.println("Considering asteroid " + asteroid.getId() + " as a best one");
						nearestMineableAsteroid = asteroid;
						distanceToNearestMineableAsteroid = dist;
					}
				}
			}
		}

		/*for (Asteroid asteroid : asteroids) { //Find nearest asteroid not being chased after by one of our ships
			if (asteroid.isMineable() && !asteroid.isMoveable() && !shipToAsteroid.containsValue(asteroid.getId())) { //If mineable, not moving, and not being chased after.
				distance = space.findShortestDistance(myShip.getPosition(), asteroid.getPosition());
				if (distance < distanceToNearestMineableAsteroid) {
					distanceToNearestMineableAsteroid = distance;
					nearestMineableAsteroid = asteroid;
				}
			}
		}*/

		distanceToNearestBeacon = Integer.MAX_VALUE;
		for(Beacon beacon : space.getBeacons())
		{
			if(!shipToBeacon.containsValue(beacon.getId())) //If a ship isn't already chasing after this beacon.
			{
				distance = space.findShortestDistance(myShip.getPosition(), beacon.getPosition());
				if(distance < distanceToNearestBeacon)
				{
					distanceToNearestBeacon = distance;
					nearestBeacon = beacon;

				}
			}
		}
		//Calculate qualitative distance value based on rules in comments above
		qualDistanceAsteroid = distanceToNearestMineableAsteroid<400 ? (int) (distanceToNearestMineableAsteroid / 100) : 4; //If the distance is less than 400, divide by 100 to get qualValue, if greater than 400 just give it 4 (Extra large)
		//Now that we have an asteroid, find out its total resource value
		int resources = nearestMineableAsteroid.getResources().getTotal();
		//Now calculate the qualitative value for resources
		qualValueAsteroid = resources < 400 ? resources / 100 : 4; //If resources less than 400 divide by 100 to get qualitative value, otherwise set to 4 (Extra large)

		qualDistanceBeacon = distanceToNearestBeacon < 400 ? (int) (distanceToNearestBeacon / 100) : 4; //If distance to beacon less than 400, calculate its qualitative score by dividing by 100, otherwise juts set it to the max (4)

		//Now get qual distance to base
		Base ourBase = null;
		for (Base teamBase : space.getBases())
		{
			if(teamBase.getTeamName().equals(myShip.getTeamName())) {
				ourBase = teamBase;
			}
		}

		double initDistance = space.findShortestDistance(myShip.getPosition(), ourBase.getPosition());
		qualDistanceBase = initDistance < 400 ? (int) (initDistance / 100) : 4; //Same methods as above
		int shipResources = myShip.getResources().getTotal();
		qualValueShip = shipResources < 400? shipResources /100: 4; //Same as above

	}


	public Beacon getNearestBeacon() {
		return nearestBeacon;
	}

	/**
	 * Return the nearest asteroid (used for actions)
	 * 
	 * @return
	 */
	public Asteroid getNearestMineableAsteroid() {
		return nearestMineableAsteroid;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GAState gaState = (GAState) o;
		return qualDistanceAsteroid == gaState.qualDistanceAsteroid &&
				qualValueAsteroid == gaState.qualValueAsteroid &&
				qualValueShip == gaState.qualValueShip &&
				qualDistanceBase == gaState.qualDistanceBase &&
				qualDistanceBeacon == gaState.qualDistanceBeacon;
	}

	@Override
	public int hashCode() {
		return Objects.hash(qualDistanceAsteroid, qualValueAsteroid, qualValueShip, qualDistanceBase, qualDistanceBeacon);
	}
}
