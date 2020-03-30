package spacesettlers.aran0002;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import spacesettlers.actions.AbstractAction;
import spacesettlers.actions.MoveToObjectAction;
import spacesettlers.objects.Base;
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

	/**
	 * Returns either the action currently specified by the policy or randomly selects one if this is a new state
	 * 
	 * @param currentState
	 * @return
	 */
	public AbstractAction getCurrentAction(Toroidal2DPhysics space, Ship myShip, GAState currentState, Random rand, HashMap<UUID, UUID> shipToAsteroid) {
		if (!policy.containsKey(currentState)) {
			// randomly chose to either return to base or go to the nearest
			// asteroid.  Note this needs to be changed in a real agent as it won't learn 
			// much here!
			if (rand.nextBoolean()) {
				for(Base base : space.getBases())
				{
					if(base.getTeamName().equals("Myrrh's Team"))
					{
						Base ourBase = base;
						policy.put(currentState, new GAMoveTo(ourBase.getClass()));
						//System.out.println("Going for home base");
					}
				}
				//policy.put(currentState, new DoNothingAction());
			} else {
				//System.out.println("Moving to nearestMineable Asteroid " + myShip.getPosition() + " nearest " + currentState.getNearestMineableAsteroid().getPosition());
				policy.put(currentState, new GAMoveTo(currentState.getNearestMineableAsteroid().getClass()));
				shipToAsteroid.put(myShip.getId(),currentState.getNearestMineableAsteroid().getId()); //Keep track of ship heading towards asteroid

				//System.out.println("Going for asteroid");
			}
		}
		currentScore = currentState.getCurrentScore();
		return policy.get(currentState).generateMovement(currentState, myShip, space);

	}

	public int getCurrentScore() {
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
