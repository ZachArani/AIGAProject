package spacesettlers.daro6715;

import java.util.Random;

/**
 * Basically an randomly generated decision tree, gonna be used in a forest
 * 
 * @author Robert Daro
 *
 */
public class DecisionTree 
{
	//This will increase if the tree does well
	int lowerBound;
	int upperBound;
	
	Decision[][][][][] stateMap = new Decision[5][5][5][5][5]; //you aren't hallucinating that's the whole set of states
	
	public DecisionTree()
	{
		Random randy = new Random();
		
		//Okay time to iterate through and give each state a random action
		for(int a = 0; a < 5; a++)
		{
			for(int b = 0; b < 5; b++)
			{
				for(int c = 0; c < 5; c++)
				{
					for(int d = 0; d < 5; d++)
					{
						for(int e = 0; e < 5; e++)
						{
							int randNum = randy.nextInt(3);
							if(randNum == 0)
							{
								stateMap[a][b][c][d][e] = Decision.ASTEROID;
							}
							else if(randNum == 1)
							{
								stateMap[a][b][c][d][e] = Decision.BASE;
							}
							else
							{
								stateMap[a][b][c][d][e] = Decision.BEACON;
							}
						}
					}
				}
			}
		}
	}
	
	public boolean inRange(int question)
	{
		if(question >= lowerBound && question <= upperBound)
		{
			return true;
		}
		return false;
	}
}
