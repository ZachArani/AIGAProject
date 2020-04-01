package spacesettlers.aran0002;

import spacesettlers.actions.MoveToObjectAction;
import spacesettlers.objects.*;
import spacesettlers.simulator.Toroidal2DPhysics;

/**
 * Class for remembering what type of object to move towards (asteroid or base) and creates MoveToObjects given a ship object to control.
 */
public class GAMoveTo {

    Class moveTo;

    public GAMoveTo(Class moveToClass)
    {
        moveTo = moveToClass; //Specify object to move towards
    }

    public Class getMoveTo() {
        return moveTo;
    }

    public MoveToObjectAction generateMovement(GAState state, Ship ship, Toroidal2DPhysics space)
    {
        if(moveTo.equals(Base.class)) //If we're trying to go towards a base
        {
            for(Base base : space.getBases()) //Iterate through bases
                if(base.getTeamName().equals(ship.getTeamName())) //Find ours
                    return new MoveToObjectAction(space, ship.getPosition(), base); //get moving
        }
        else if(moveTo.equals(Asteroid.class)) //if we're going towards an asteroid
        {
            return new MoveToObjectAction(space, ship.getPosition(), state.getNearestMineableAsteroid()); //get moving
        }
        else if(moveTo.equals(Beacon.class)) //If we're moving towards a beacon
        {
            return new MoveToObjectAction(space, ship.getPosition(), state.getNearestBeacon());
        }

        return null; //God have mercy upon your soul.

    }
}
