package spacesettlers.daro6715;

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
 * Our chromosome for our GA client. Can either go to base, nearest best asteroid, or energy beacon. Chooses one at random. Also keeps track of the current score of the team.
 * 
 * @author (originally) amy. Amended by Zach.
 *
 */
public class GAChromosome {
	private HashMap<GAState, GAMoveTo> policy;
	int scoreInitial;
	int resourcesInitial;
	
	public GAChromosome() {
		policy = new HashMap<GAState, GAMoveTo>();
		scoreInitial = 0;
		resourcesInitial = 0;

	}

	public GAChromosome(HashMap<GAState, GAMoveTo> policy)
	{
		this.policy = policy;
		scoreInitial = 0;
		resourcesInitial = 0;
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
			if(teamInfo.getTeamName().equals("AraniDaroSpacePeepsInc"))
				return (int)teamInfo.getScore();
		}
		return -1; //God have mercy upon your soul
	}

	public int getScoreInitial() {
		return scoreInitial;
	}

	public int getResourcesInitial() {
		return resourcesInitial;
	}

	public void setScoreInitial(int scoreInitial) {
		this.scoreInitial = scoreInitial;
	}

	public void setResourcesInitial(int resourcesInitial) {
		this.resourcesInitial = resourcesInitial;
	}

	public void setPolicy(HashMap<GAState, GAMoveTo> policy) {
		this.policy = policy;
	}

	public HashMap<GAState, GAMoveTo> getPolicy()
	{
		return policy;
	}
}
