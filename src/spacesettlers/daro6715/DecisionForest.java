package spacesettlers.daro6715;

import java.util.Random;
/**
 * Some number of random trees with chances to be drawn. The chances will be modified based on performance.
 * 
 * @author Robert Daro
 *
 */
public class DecisionForest 
{
	private DecisionTree[] forest;
	
	//Currently drawn tree
	private DecisionTree choice;
	private int indexOfChoice;
	
	//Makes a random forest
	public DecisionForest(int size)
	{
		forest = new DecisionTree[size];
		
		for(int i = 0; i < size; i++)
		{
			forest[i] = new DecisionTree(); //this is a random thing
			forest[i].lowerBound = i;
			forest[i].upperBound = i;
		}
	}
	
	public void drawTree()
	{
		//Basically, get a random value within the range of our deck
		int randInt = new Random().nextInt(forest[forest.length-1].upperBound+1);
		
		//See what tree we get
		for(int i = 0; i < forest.length; i++)
		{
			DecisionTree tree = forest[i];
			if(tree.inRange(randInt))
			{
				choice = tree;
				indexOfChoice = i;
				return;
			}
		}
	}
	
	//Happens if the tree does well
	public void increaseRange()
	{
		//Asymmetric growth increases the chance of being chosen
		choice.upperBound++;
		for(int i = indexOfChoice+1; i < forest.length; i++)
		{
			//Symmetric growth of bound leaves smaller chance to be chosen
			DecisionTree tree = forest[i];
			tree.lowerBound++;
			tree.upperBound++;
		}
	}
	
	//Gets the decision from our choice of tree
	public Decision getDecision(int qualDistanceAsteroid, int qualValueAsteroid, int qualValueShip,
									int qualDistanceBase, int qualDistanceBeacon)
	{
		return choice.stateMap[qualDistanceAsteroid][qualValueAsteroid][qualValueShip][qualDistanceBase][qualDistanceBeacon];
	}
}
