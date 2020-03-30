package spacesettlers.aran0002;

import spacesettlers.actions.AbstractAction;
import spacesettlers.actions.MoveToObjectAction;
import spacesettlers.clients.examples.ExampleGAChromosome;
import spacesettlers.objects.Asteroid;
import spacesettlers.objects.Base;
import spacesettlers.simulator.Toroidal2DPhysics;
import spacesettlers.utilities.Position;

import java.util.*;
import java.util.stream.IntStream;

import static java.util.Collections.shuffle;

/**
 * Stores a whole population of individuals for genetic algorithms / evolutionary computation
 * 
 * @author amy
 *
 */
public class GAPopulation {
	private GAChromosome[] population;
	
	private int currentPopulationCounter;
	
	private double[] fitnessScores;

	/**
	 * Make a new empty population
	 */
	public GAPopulation(int populationSize) {
		super();
		
		// start at member zero
		currentPopulationCounter = 0;
		
		// make an empty population
		population = new GAChromosome[populationSize];
		
		for (int i = 0; i < populationSize; i++) {
			population[i] = new GAChromosome();
		}
		
		// make space for the fitness scores
		fitnessScores = new double[populationSize];
	}

	/**
	 * Currently scores all members as zero (the student must implement this!)
	 * 
	 * @param space
	 */
	public void evaluateFitnessForCurrentMember(Toroidal2DPhysics space) {
		fitnessScores[currentPopulationCounter] = population[currentPopulationCounter].getCurrentScore(); //Use score as temporary fitness evaluation

	}

	/**
	 * Return true if we have reached the end of this generation and false otherwise
	 * 
	 * @return
	 */
	public boolean isGenerationFinished() {
		if (currentPopulationCounter == population.length) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Return the next member of the population (handles wrapping around by going
	 * back to the start but then the assumption is that you will reset with crossover/selection/mutation
	 * 
	 * @return
	 */
	public GAChromosome getNextMember() {
		currentPopulationCounter++;
		
		return population[currentPopulationCounter % population.length];
	}

	/**
	 * Does crossover, selection, and mutation using our current population.
	 * Crossover: Take 1 of the two parents at random as the 'base parent'. If any states are shared between the base parent and the other parent, randomly decide which of the parents action to distribute. If the state is not the same between the parents, take the base parents action.
	 * Mutation: For every state brought down to the child, there is a 5% chance that instead of taking the parent action, it is inverted.
	 * Selection: Take the 10% best performers as well as 5% randomly selected from the rest of the populace.
	 */
	public void makeNextGeneration() {
		int numBest = population.length / 4; //Number of best to be selected are the top 25% (ish) of organisms
		int numRandom = population.length / 4; //Number of randomly selected organisms is 25%
		//In total this is 50% of the previous generation. We'll mate randomly twice to get the other 50% for the next generation.
		Random random = new Random();
		int[] bestPerformersIndex = getBestPerformers(numBest); //Get the indexes of the top 25% best scoring organisms

		GAChromosome[] matingChromosomes = new GAChromosome[numBest + numRandom]; //Make an array to do the mating that's the length of the selected chromosomes

		GAChromosome[] childChromosomes = new GAChromosome[matingChromosomes.length]; //Make an array to hold children

		for(int i = 0; i<bestPerformersIndex.length; i++) //For every best performer.
		{
			matingChromosomes[i] = population[bestPerformersIndex[i]]; //Set the mating chromosome equal to the popluation chromosome found at the index specified in the bestPerformers index
		}

		int[] randMem = new int[numRandom]; //To remember what we randomly pick so we don't have repeats
		for(int i = numBest; i<numBest+numRandom; i++) //To fill out the REST of the array with random organisms, start from where we added the best and then continue adding
		{
			int tempRand = random.nextInt(population.length); //A random index to pull a chromosome from
			if(!IntStream.of(bestPerformersIndex).anyMatch(x -> x==tempRand) && !IntStream.of(randMem).anyMatch(x -> x==tempRand)) //Lambda function. If the randomly selected number isn't already one of the selected values from either the best organisms or the already selected random organisms
			{
				randMem[i-numBest] = tempRand; //Remember what index we pulled (need to subtract numBest to start at 0)
				matingChromosomes[i] = population[tempRand]; //Add random organism to the mating list.
			}
		}
		int childCount = 0; //What child we're current creating
		for(int k = 0; k<2; k++)
		{

			List<GAChromosome> randomizedMates = Arrays.asList(matingChromosomes); //Convert the mates to a list so that we can...
			Collections.shuffle(randomizedMates); //Shuffle them!
			//Now that our mates are shuffled, lets begin the crossover (and mutation) process:
			for(int i = 0; i<randomizedMates.size(); i+=2) //Iterate though every other mate
			{
				//Select base parent at random
				int baseParentFactor = random.nextInt(2); //Either 0 or 1, to randomize which parent is base parent.
				int otherParentFactor = baseParentFactor==1 ? 0 : 1; //If the base parent is i+1, then other parent is i, if base parent is i, then other parent is i+1
				GAChromosome baseParent = randomizedMates.get(i+baseParentFactor);
				GAChromosome otherParent = randomizedMates.get(i+otherParentFactor);
				HashMap<GAState, GAMoveTo> childPolicy = new HashMap<>(); //Policy of child
				HashMap<GAState, GAMoveTo> baseParentPolicy = baseParent.getPolicy();
				HashMap<GAState, GAMoveTo> otherParentPolicy = otherParent.getPolicy();
				//Iterate through base parent states
				for(GAState state : baseParentPolicy.keySet())
				{
					GAMoveTo action = baseParentPolicy.get(state); //Get the action,
					if(random.nextInt(20)==19) //If we get 5% chance of mutation, invert the action
					{
						if(action.getMoveTo().equals(Base.class)) //If we were going to a base
							childPolicy.put(state, new GAMoveTo(Asteroid.class)); //Head to an asteroid instead
						if(action.getMoveTo().equals(Asteroid.class))
							childPolicy.put(state, new GAMoveTo(Base.class)); //Head to a Base instead.
					}
					else if(otherParentPolicy.containsKey(state)) //If other parent also shares the same state
					{
						if(random.nextBoolean()) //Randomly choose
							childPolicy.put(state, baseParentPolicy.get(state)); //Base parent action
						else
							childPolicy.put(state, otherParentPolicy.get(state)); //other parent action
					}
					else
						childPolicy.put(state, baseParentPolicy.get(state));

					childChromosomes[childCount] = new GAChromosome(childPolicy); //Make a child with the policy.
					childCount++; //Head to the next one.
				}
			}

		}

		//Now we need to reset our fitness scores and update our populace to be a combination of the parent and children.
		for(int i =0; i<fitnessScores.length; i++) //iterate through fitness scores
			fitnessScores[i]=0; //Set to 0

		ArrayList<GAChromosome> tempList = new ArrayList<>();
		tempList.addAll(Arrays.asList(childChromosomes)); //Add children
		tempList.addAll(Arrays.asList(matingChromosomes)); //Add parents
		Collections.shuffle(tempList); //Mix em up
		for(int i = 0; i<population.length; i++) //Iterate through population
		{
			population[i] = tempList.get(i); //set population equal to one of the parents/children of our crossover.
		}

		currentPopulationCounter = 0; //reset populace counter.
	}

	/**
	 * Return the first member of the popualtion
	 * @return
	 */
	public GAChromosome getFirstMember() {
		return population[0];
	}

	/**
	 * Return the indexes of the n best organisms by score
	 */
	public int[] getBestPerformers(int numBest)
	{
		int[] bestOrganismsIndex = new int[numBest]; //Indexes of the best scoring organisms, sorted by score.
		int numFound = 0; //How many organisms we've already found
		for(int i = 0; i<numBest; i++)
		{
			double tempBest = -1;
			int tempBestIndex = -1;
			for(int j=0; j<fitnessScores.length; j++)
			{
				int finalJ = j; //for lambda expressions since they need a final variable.
				if(fitnessScores[j] > tempBest && !IntStream.of(bestOrganismsIndex).anyMatch(x -> x== finalJ)) //If the currently looked at fitness is the greatest so far and NOT already accounted for in our list of the best scores
				{
					tempBest = fitnessScores[j]; //Keep track of best score
					tempBestIndex = j; //Keep track of best index
				}

			}

			bestOrganismsIndex[i] = tempBestIndex; //Set the nth best score index to the best scored index discovered above.

		}
		return bestOrganismsIndex;
	}
}
	

