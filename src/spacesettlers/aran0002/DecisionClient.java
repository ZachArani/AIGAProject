package spacesettlers.aran0002;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;

import spacesettlers.actions.*;
import spacesettlers.clients.ImmutableTeamInfo;
import spacesettlers.clients.TeamClient;
import spacesettlers.graphics.SpacewarGraphics;
import spacesettlers.objects.*;
import spacesettlers.objects.powerups.SpaceSettlersPowerupEnum;
import spacesettlers.objects.resources.ResourcePile;
import spacesettlers.simulator.Toroidal2DPhysics;


public class DecisionClient extends TeamClient {
	/**
	 * The forest we will use
	 */
	private DecisionForest forest;
	
	/**
	 * How many steps each policy is evaluated for before moving to the next one
	 */
	private int evaluationSteps = 1000;
	
	/**
	 * Current step
	 */
	private int steps = 0;
	
	/**
	 * Current score total
	 */
	private int scoreTotal = 0;
	
	//This keeps track of how well we did last
	private int lastNet = 0;
	
	HashMap <UUID, Boolean> aimingForBase; //Keeps track of ships aiming for base
	HashMap <UUID, UUID> shipToAsteroid; //Keeps track of ships aiming for asteroids
	HashMap <UUID, UUID> shipToBeacon; //Keeps track of ships aiming for what Beacons
	HashMap <UUID, Integer> shipToResources; //Keeps track of ships resources (so we can look for changes when asteroids get picked up)

	@Override
	public Map<UUID, AbstractAction> getMovementStart(Toroidal2DPhysics space,
			Set<AbstractActionableObject> actionableObjects) {
		// make a hash map of actions to return
		HashMap<UUID, AbstractAction> actions = new HashMap<UUID, AbstractAction>();

		// loop through each ship and send it the current policy from the chromosome.  If the chromosome
		// hasn't seen a state before, it will pick an abstract action (you should make more interesting actions!
		// this agent choses only between doNothing and moveToNearestAsteroid)
		for (AbstractObject actionable :  actionableObjects) {
			if (actionable instanceof Ship) {
				Ship ship = (Ship) actionable;
				AbstractAction action = null;
				if (ship.getCurrentAction() == null || ship.getCurrentAction().isMovementFinished(space)) //If we're not doing anything
				{
					GAState currentState = new GAState(space, ship, shipToAsteroid, shipToBeacon);
					action = getCurrentAction(space, ship, currentState);
					if(action instanceof MoveToObjectAction && ((MoveToObjectAction) action).getGoalObject() instanceof Base) //If we're now going for a base
						aimingForBase.put(ship.getId(), true);
				//	System.out.println("Finished last action. Now going towards " + ((MoveToObjectAction)action).getGoalObject().toString());
				}
				else if(ship.getCurrentAction() instanceof MoveToObjectAction && ((MoveToObjectAction) ship.getCurrentAction()).getGoalObject() instanceof Base)
				{
					Base base = (Base) ((MoveToObjectAction) ship.getCurrentAction()).getGoalObject();
					if(ship.getResources().getTotal() == 0 && aimingForBase.containsKey(ship.getId()) && aimingForBase.get(ship.getId()) && space.findShortestDistance(ship.getPosition(), base.getPosition()) < ship.getRadius() + base.getRadius()) // Did we just touch base?
					{
						aimingForBase.put(ship.getId(), false); //We've reached the base
						GAState currentState = new GAState(space, ship, shipToAsteroid, shipToBeacon);
						action = getCurrentAction(space, ship, currentState);
					//	System.out.println("Finished returning to base. New action is " + ((MoveToObjectAction)action).getGoalObject().toString());
						if(action instanceof MoveToObjectAction && ((MoveToObjectAction) action).getGoalObject() instanceof Base) //If we're aiming for base again
							aimingForBase.put(ship.getId(), true);
					}
					else
					{
						action = ship.getCurrentAction(); //Keep on doing whatever we're currently doing
					}
				}
				else {
					action = ship.getCurrentAction(); //Keep on keepin' on
				}
				actions.put(ship.getId(), action);
			} else {
				// it is a base.  Heuristically decide when to use the shield (TODO)
				actions.put(actionable.getId(), new DoNothingAction());
			}
		}
		//System.out.println("actions are " + actions);
		return actions;

	}

	/**
	 * Returns whatever action the tree specified
	 * 
	 * @param currentState
	 * @return
	 */
	public AbstractAction getCurrentAction(Toroidal2DPhysics space, Ship ship, GAState currentState) 
	{
		Decision dec = forest.getDecision(currentState.qualDistanceAsteroid,
											currentState.qualValueAsteroid, currentState.qualValueShip,
											currentState.qualDistanceBase, currentState.qualDistanceBeacon);
		switch(dec) {
		case BASE:
			Base ourBase = null;
			for (Base teamBase : space.getBases())
			{
				if(teamBase.getTeamName().equals(ship.getTeamName())) {
					ourBase = teamBase;
				}
			}
			return new MoveToObjectAction(space, ship.getPosition(), ourBase);
		case ASTEROID:
			return new MoveToObjectAction(space, ship.getPosition(), currentState.nearestMineableAsteroid);
		case BEACON:
			return new MoveToObjectAction(space, ship.getPosition(), currentState.nearestBeacon);
		}
		return null;
	}
	
	@Override
	public void getMovementEnd(Toroidal2DPhysics space, Set<AbstractActionableObject> actionableObjects) {
		// increment the step counter
		steps++;

		// if the step counter is modulo evaluationSteps, then evaluate this member and move to the next one
		if (steps % evaluationSteps == 0) {

			int netScore = getCurrentGameScore(space) - scoreTotal;
			
			System.out.println(netScore);

			//if we did well increase the odds of picking this tree again
			if(netScore >= lastNet)
			{
				lastNet = netScore;
				forest.increaseRange();
			}
			
			forest.drawTree();
		}
		
	}

	@Override
	public Map<UUID, SpaceSettlersPowerupEnum> getPowerups(Toroidal2DPhysics space,
			Set<AbstractActionableObject> actionableObjects) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<UUID, PurchaseTypes> getTeamPurchases(Toroidal2DPhysics space,
			Set<AbstractActionableObject> actionableObjects, ResourcePile resourcesAvailable,
			PurchaseCosts purchaseCosts) {
		//If we can buy a second ship, BUY IT!
		HashMap<UUID, PurchaseTypes> purchases = new HashMap<UUID, PurchaseTypes>();
		if (purchaseCosts.canAfford(PurchaseTypes.SHIP, resourcesAvailable)) {
			for (AbstractActionableObject actionableObject : actionableObjects) {
				if (actionableObject instanceof Base) {
					Base base = (Base) actionableObject;
					purchases.put(base.getId(), PurchaseTypes.SHIP);
					break;
				}

			}

		}
		return purchases;
	}

	/**
	 * Initialize the population by either reading it from the file or making a new one from scratch
	 * 
	 * @param space
	 */
	@Override
	public void initialize(Toroidal2DPhysics space) {
		XStream xstream = new XStream();
		xstream.alias("GAPopulation", GAPopulation.class);

		// try to load the population from the existing saved file.  If that failes, start from scratch
		try { 
			forest = (DecisionForest) xstream.fromXML(new File(getKnowledgeFile()));
		} catch (XStreamException e) {
			// if you get an error, handle it other than a null pointer because
			// the error will happen the first time you run
			System.out.println("No existing decision forest found - starting a new one from scratch");
			forest = new DecisionForest(20);
		}

		aimingForBase = new HashMap<UUID, Boolean>();
		shipToAsteroid = new HashMap <UUID, UUID>();
		shipToBeacon = new HashMap<UUID, UUID>();
		shipToResources = new HashMap<>();
		
		//Getting the forest ready to answer questions
		forest.drawTree();
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

	@Override
	public void shutDown(Toroidal2DPhysics space) {
		XStream xstream = new XStream();
		xstream.alias("GAPopulation", GAPopulation.class);
		try { 
			// if you want to compress the file, change FileOuputStream to a GZIPOutputStream
			xstream.toXML(forest, new FileOutputStream(new File(getKnowledgeFile())));
		} catch (XStreamException e) {
			// if you get an error, handle it somehow as it means your knowledge didn't save
			System.out.println("Can't save knowledge file in shutdown ");
			System.out.println(e.getMessage());
		} catch (FileNotFoundException e) {
			// file is missing so start from scratch (but tell the user)
			System.out.println("Can't save knowledge file in shutdown ");
			System.out.println(e.getMessage());
		}
	}

	@Override
	public Set<SpacewarGraphics> getGraphics() {
		// TODO Auto-generated method stub
		return null;
	}
}
