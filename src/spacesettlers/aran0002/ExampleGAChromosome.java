package spacesettlers.aran0002;

import java.util.HashMap;
import java.util.Random;

import spacesettlers.actions.AbstractAction;
import spacesettlers.actions.DoNothingAction;
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
public class ExampleGAChromosome {
	private HashMap<ExampleGAState, AbstractAction> policy;
	
	public ExampleGAChromosome() {
		policy = new HashMap<ExampleGAState, AbstractAction>();
	}

	/**
	 * Returns either the action currently specified by the policy or randomly selects one if this is a new state
	 * 
	 * @param currentState
	 * @return
	 */
	public AbstractAction getCurrentAction(Toroidal2DPhysics space, Ship myShip, ExampleGAState currentState, Random rand) {
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
						policy.put(currentState, new MoveToObjectAction(space, myShip.getPosition(), ourBase));
						//System.out.println("Going for home base");
					}
				}
				//policy.put(currentState, new DoNothingAction());
			} else {
				//System.out.println("Moving to nearestMineable Asteroid " + myShip.getPosition() + " nearest " + currentState.getNearestMineableAsteroid().getPosition());
				policy.put(currentState, new MoveToObjectAction(space, myShip.getPosition(), currentState.getNearestMineableAsteroid()));
				//System.out.println("Going for asteroid");
			}
		}

		return policy.get(currentState);

	}
	
	
}
