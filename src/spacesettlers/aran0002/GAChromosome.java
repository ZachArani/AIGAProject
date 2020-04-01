package spacesettlers.aran0002;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import spacesettlers.actions.AbstractAction;
import spacesettlers.clients.ImmutableTeamInfo;
import spacesettlers.objects.Asteroid;
import spacesettlers.objects.Base;
import spacesettlers.objects.Beacon;
import spacesettlers.objects.Ship;
import spacesettlers.simulator.Toroidal2DPhysics;

/**
 * An example chromosome for a space settlers agent using genetic algorithms / evolutionary computation
 * 
 * @author amy
 *
 */
public class GAChromosome {
	private HashMap<GAState, GAMoveTo> policy;
	int currentScore;
	
	public GAChromosome() {
		policy = new HashMap<GAState, GAMoveTo>();
		currentScore = 0;
	}

	public GAChromosome(HashMap<GAState, GAMoveTo> policy)
	{
		this.policy = policy;
		currentScore = 0;
	}

	public void setCurrentScore(int currentScore) {
		this.currentScore = currentScore;
	}

	/**
	 * Returns either the action currently specified by the policy or randomly selects one if this is a new state
	 * 
	 * @param currentState
	 * @return
	 */
	public AbstractAction getCurrentAction(Toroidal2DPhysics space, Ship myShip, GAState currentState, Random rand, HashMap<UUID, UUID> shipToAsteroid, HashMap<UUID,UUID> shipToBeacon) {
		if (!policy.containsKey(currentState)) {
			// randomly chose to: Return to base, head towards the nearest (non-moving) asteroid, or head to the nearest Beacon
			int decider = rand.nextInt(3);
			if (decider==0) { //Head for base

				policy.put(currentState, new GAMoveTo(Base.class));
				//System.out.println("Going for home base");
			}
			else if (decider==1){ //Head for asteroid
				//System.out.println("Moving to nearestMineable Asteroid " + myShip.getPosition() + " nearest " + currentState.getNearestMineableAsteroid().getPosition());
				policy.put(currentState, new GAMoveTo(Asteroid.class));
				shipToAsteroid.put(myShip.getId(),currentState.getNearestMineableAsteroid().getId()); //Keep track of ship heading towards asteroid

				//System.out.println("Going for asteroid");
			}
			else //Head for beacon
			{
				policy.put(currentState, new GAMoveTo(Beacon.class));
				shipToBeacon.put(myShip.getId(), currentState.getNearestBeacon().getId());
				//System.out.println("Going for Beacon");
			}
		}
		return policy.get(currentState).generateMovement(currentState, myShip, space);

	}

	//Get whatever our team's score is right now.
	public int getCurrentGameScore(Toroidal2DPhysics space) {
		for(ImmutableTeamInfo teamInfo : space.getTeamInfo())
		{
			if(teamInfo.getTeamName().equals("Myrrh's Team"))
				return (int)teamInfo.getScore();
		}
		return -1; //God have mercy upon your soul
	}

	public void incCurrentScore(int amount) {currentScore += amount;}

	public int getCurrentScore(){
		return currentScore;
	}

	public void setPolicy(HashMap<GAState, GAMoveTo> policy) {
		this.policy = policy;
	}

	public HashMap<GAState, GAMoveTo> getPolicy()
	{
		return policy;
	}
}
