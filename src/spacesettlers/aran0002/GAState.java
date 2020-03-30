package spacesettlers.aran0002;

import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import spacesettlers.clients.ImmutableTeamInfo;
import spacesettlers.objects.Asteroid;
import spacesettlers.objects.Base;
import spacesettlers.objects.Ship;
import spacesettlers.simulator.Toroidal2DPhysics;

/**
 * Example state representation for a GA agent.  Note this would need to be significantly 
 * modified/augmented to work in the full case (it just looks at one aspect of the 
 * information available to it).  Note also that this representation ignores which ship
 * it is on the team and just looks for the nearest asteroid.
 *  
 * @author amy
 *
 */
public class GAState {
	double distanceToNearestMineableAsteroid;
	Asteroid nearestMineableAsteroid;
	int qualDistanceAsteroid = -1; //Qualatative distance to object (asteroid or base). 0=Minimal (<100), 1=Small (<200), 2=Medium (<300) 3=Large (<400), 4=Extra large (>400)
	int qualValueAsteroid = -1; //Qualatative value of asteroid or resources on ship (to return to base).
	int qualValueShip = -1;
	int qualDistanceBase = -1;
	int currentScore = 0;

	public GAState(Toroidal2DPhysics space, Ship myShip, HashMap<UUID, UUID> shipToAsteroid) {
		updateState(space, myShip, shipToAsteroid);
	}


	/**
	 * Update the distance to the nearest mineable asteroid
	 * 
	 * @param space
	 * @param myShip
	 */
	public void updateState(Toroidal2DPhysics space, Ship myShip, HashMap<UUID, UUID> shipToAsteroid) {
		Set<Asteroid> asteroids = space.getAsteroids();
		distanceToNearestMineableAsteroid = Integer.MAX_VALUE;
		double distance;

		for (Asteroid asteroid : asteroids) { //Find nearest asteroid not being chased after by one of our ships
			if (asteroid.isMineable() && !asteroid.isMoveable() && !shipToAsteroid.containsValue(asteroid.getId())) { //If mineable, not moving, and not being chased after.
				distance = space.findShortestDistance(myShip.getPosition(), asteroid.getPosition());
				if (distance < distanceToNearestMineableAsteroid) {
					distanceToNearestMineableAsteroid = distance;
					nearestMineableAsteroid = asteroid;
				}
			}
		}
		//Calculate qualitative distance value based on rules in comments above
		qualDistanceAsteroid = distanceToNearestMineableAsteroid<400 ? (int) (distanceToNearestMineableAsteroid / 100) : 4; //If the distance is less than 400, divide by 100 to get qualValue, if greater than 400 just give it 4 (Extra large)
		//Now that we have an asteroid, find out its total resource value
		int resources = nearestMineableAsteroid.getResources().getTotal();
		//Now calculate the qualitative value for resources
		qualValueAsteroid = resources < 400 ? resources / 100 : 4; //If resources less than 400 divide by 100 to get qualitative value, otherwise set to 4 (Extra large)
		//Now get qual distance to base
		Base ourBase = null;
		for (Base teamBase : space.getBases())
		{
			if(teamBase.getTeamName().equals(myShip.getTeamName())) {
				ourBase = teamBase;
			}
		}
		for(ImmutableTeamInfo teamInfo : space.getTeamInfo())
		{
			if(teamInfo.getTeamName().equals("Myrrh's Team"))
				currentScore = (int)teamInfo.getScore();
		}
		double initDistance = space.findShortestDistance(myShip.getPosition(), ourBase.getPosition());
		qualDistanceBase = initDistance < 400 ? (int) (initDistance / 100) : 4; //Same methods as above
		int shipResources = myShip.getResources().getTotal();
		qualValueShip = shipResources < 400? resources /100: 4; //Same as above

	}

	
	/**
	 * Return the nearest asteroid (used for actions)
	 * 
	 * @return
	 */
	public Asteroid getNearestMineableAsteroid() {
		return nearestMineableAsteroid;
	}

	public int getCurrentScore(){
		return currentScore;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GAState that = (GAState) o;
		return qualDistanceAsteroid == that.qualDistanceAsteroid &&
				qualValueAsteroid == that.qualValueAsteroid &&
				qualValueShip == that.qualValueShip &&
				qualDistanceBase == that.qualDistanceBase;
	}

	@Override
	public int hashCode() {
		return Objects.hash(qualDistanceAsteroid, qualValueAsteroid, qualValueShip, qualDistanceBase);
	}
}
