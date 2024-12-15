package pacman.controllers.examples;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import pacman.controllers.Controller;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/*
 * Pac-Man controller as part of the starter package - simply upload this file as a zip called
 * MyPacMan.zip and you will be entered into the rankings - as simple as that! Feel free to modify 
 * it or to start from scratch, using the classes supplied with the original software. Best of luck!
 * 
 * This controller utilises 3 tactics, in order of importance:
 * 1. Get away from any non-edible ghost that is in proximity
 * 2. Go after the nearest edible ghost
 * 3. Go to the nearest pill/power pill
 */
public class PaulBurnsPacman extends Controller<MOVE>
{	
	private final Random random = new Random();
	private static  int MIN_DISTANCE=7;	//if a ghost is this close, run away
	private static  int MIN_DISTANCE_TO_EAT = 38; //if a ghost is this close, go eat it
	public MOVE getMove(Game game,long timeDue)
	{			
		int current=game.getPacmanCurrentNodeIndex();
		int level = game.getCurrentLevel();
		//Strategy 1: if any non-edible ghost is too close (less than MIN_DISTANCE), run away
		for(GHOST ghost : GHOST.values()){
			if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0)
				if(game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost))<MIN_DISTANCE)
					//return game.getNextMoveAwayFromTarget(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(ghost),DM.PATH);
					return pathFind(game);
		}
		
		boolean stillCanEat = false;
		//Strategy 2: if any edible ghost is close (less than MIN_DISTANCE_TO_EAT), run towards
		for (GHOST ghost : GHOST.values()) {
			if (game.getGhostEdibleTime(ghost) > 16 && game.getGhostEdibleTime(ghost) < 175 && game.getGhostLairTime(ghost) == 0){
				stillCanEat = true;
				if (game.getShortestPathDistance(current, game.getGhostCurrentNodeIndex(ghost)) < MIN_DISTANCE_TO_EAT)
					return game.getNextMoveTowardsTarget( game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(ghost), DM.PATH);
			}
		}
		
		//Strategy 3: go after the pills and power pills
		int[] pills=game.getPillIndices();
		int[] powerPills=game.getPowerPillIndices();		
		
		ArrayList<Integer> targets=new ArrayList<Integer>();
		
		for(int i=0;i<powerPills.length;i++)//check with power pills are available
			if(game.isPowerPillStillAvailable(i))
				targets.add(Integer.valueOf(powerPills[i]));
		
		////Prioritise close power pills when past level 1 /////////////////////////////////////////////
		
		int[] PowerArray=new int[targets.size()];//convert from ArrayList to array
		
		for(int i=0;i<PowerArray.length;i++)
			PowerArray[i]=targets.get(i);
		if(PowerArray.length!=0){
			int dis =game.getShortestPathDistance(current,game.getClosestNodeIndexFromNodeIndex(current,PowerArray,DM.PATH));
			if(level>0 && dis<37 && !stillCanEat){
				return game.getNextMoveTowardsTarget(current,game.getClosestNodeIndexFromNodeIndex(current,PowerArray,DM.PATH),DM.PATH);
			}else if(level==0){
				
			}
		}
		///////////////////////////////////////////////////////////////////////////////////////////
		for(int i=0;i<pills.length;i++)//check which pills are available			
			if(game.isPillStillAvailable(i))
				targets.add(Integer.valueOf(pills[i]));
		
		int[] targetsArray=new int[targets.size()];//convert from ArrayList to array
		
		for(int i=0;i<targetsArray.length;i++)
			targetsArray[i]=targets.get(i);
		
		//return the next direction once the closest target has been identified
		return game.getNextMoveTowardsTarget(current,game.getClosestNodeIndexFromNodeIndex(current,targetsArray,DM.PATH),DM.PATH);
	}
	public MOVE pathFind(Game game){
		int current = game.getPacmanCurrentNodeIndex();
		Game copy = game.copy();
		List<MOVE> bestMoves = new ArrayList<MOVE>();
		double bestWeight = Double.POSITIVE_INFINITY;
		Legacy2TheReckoning ghost=new Legacy2TheReckoning();
		for(MOVE move : copy.getPossibleMoves(current)){
			copy.updatePacMan(move);
			copy.updateGhosts(ghost.getMove());
			double weight = getWeight(copy);
			if (weight < bestWeight) {
				bestMoves.clear();
				bestMoves.add(move);
				bestWeight = weight;
			} else if (weight == bestWeight) {
				bestMoves.add(move);
			}
			copy = game.copy();
		}
		if(bestMoves.size()>0){
			
			return bestMoves.get(random.nextInt(bestMoves.size()));
		}
		else{
			return lastMove;
		}
		
	}
	private double getWeight(Game game) {
		double weight = 0;
		int current = game.getPacmanCurrentNodeIndex();
		int looking_dis = 39;
		int[] pills = game.getPillIndices();
		int[] powerPills = game.getPowerPillIndices();
		ArrayList<Integer> targets = new ArrayList<Integer>();

		for (int i = 0; i < powerPills.length; i++)// check with power pills are available
			if (game.isPowerPillStillAvailable(i))
				targets.add(Integer.valueOf(powerPills[i]));
		
		if (targets.size() == 0)
			for (int i = 0; i < pills.length; i++)// check which pills are available
				if (game.isPillStillAvailable(i))
					targets.add(Integer.valueOf(pills[i]));
		
		int[] targetsArray = new int[targets.size()]; // convert from ArrayList to array

		for (int i = 0; i < targetsArray.length; i++)
			targetsArray[i] = targets.get(i);
	    
		int ghostDistance = 0;
		int distance = 0;
		if(targetsArray.length!=0)//Prioritise moving towards power pills first then pills
			distance = game.getShortestPathDistance(current,game.getClosestNodeIndexFromNodeIndex(current,targetsArray,DM.PATH));
		for(GHOST ghost : GHOST.values()){
			if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0){
				int dis =game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost));
				if(dis<looking_dis && dis>0){//highly prioritise running from nearby ghosts
					ghostDistance+=500000/(dis*dis);
				}
			}
		}
		weight += ghostDistance + distance;
		return weight;
	}
}























