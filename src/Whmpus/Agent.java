package Whmpus;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import Whmpus.Constants.Directions;

public class Agent {

	private World world;
	
    private Set<Coordinates> visited = new HashSet<Coordinates>();
    private Map<Coordinates,Percept> knowledgeBase = new HashMap<Coordinates,Percept>();
    private ArrayList<Coordinates> moves = new ArrayList<Coordinates>();
    int [][] arr = new int[15][15];
	int [][] probability = new int[15][15];
    
    private JSONArray moveList = new JSONArray();
    
    private Coordinates currentPosition;

    boolean goldCollected = false;
    boolean onPit = false;
    boolean onWumpus =false;
    int runs = 0;
    int maxRuns = 10000;

    
    public Agent(World world ) {
    	this.world = world;
    	currentPosition = new Coordinates(1,1,Directions.EAST);
		for (int i=0;i<15;i++)
		{
			for (int j=0; j<15;j++)
			{
				arr[i][j]=0;
				probability[i][j]=0;
			}
		}

	}
    
    void moveAgent() {

        while (!goldCollected ) {

        	runs += 1;
        	
        	Coordinates oldPosition = new Coordinates(currentPosition);
        	arr[currentPosition.getRow()][currentPosition.getCol()]++;
        	
        	moveList.put(oldPosition.creatDirectionJSON());
        	
        	Percept currentPercept = world.getPercept(currentPosition);
        	knowledgeBase.put(currentPosition, currentPercept);
        	moves.add(new Coordinates(currentPosition));
			if(world.isPit(currentPosition))
			{
				System.out.println("On Pit");
				break;
			}
			if(!world.isWhmpusDead &&world.isWumpus(currentPosition) )
			{
				System.out.println("Eaten by Wumpus");
				break;
			}
        	
        	Coordinates visitedCoordinate = new Coordinates(currentPosition); 
        	visitedCoordinate.direction	= Directions.NONE;
        	visited.add(visitedCoordinate);
        	
        	currentPosition = getNextMove(currentPosition);
        	
        	System.out.print("From: ");
        	oldPosition.printPosition();
        	System.out.print(" To: ");
        	currentPosition.printPosition();
        	System.out.println();
               	
        	if (runs > maxRuns) {
        		break;
        	}
        }
        
        if(goldCollected) {
        	        	
        	System.out.println();
        	
        	//goBackToStart();
        	  	
        }
        
        JSONObject movesJSONObject = new JSONObject();
    	try {
    		
			movesJSONObject.put("moves", moveList);

			if(world.isPS==true)
			{
				FileWriter file = new FileWriter("./player-moves-ps.json");
				file.write(movesJSONObject.toString());
				file.close();
			}
			if(world.isPS==false)
			{
				FileWriter file = new FileWriter("./player-moves-rand.json");
				file.write(movesJSONObject.toString());
				file.close();
			}

			

			
			world.exportMap();
			
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		/*for (int i=0;i<11;i++)
		{
			for (int j=0; j<11;j++)
			{
				System.out.println(i+","+j+"-->"+probability[i][j]);
			}
		}*/
    	
    }
    //Hell
    
    
    private Coordinates getNextMove(Coordinates playerPosition) {
    	
    	Percept percept = world.getPercept(playerPosition);
    	boolean Deadlock=false;
    	
    	if (percept.glitter) {
    		goldCollected = true;
    		System.out.print("GOT GOLD>> ");
    		
    	} else {
    		
    		if (percept.stench) {
    			
    			Coordinates possibleWhmpusLocation = locateWhmpus();

    			
    			if (possibleWhmpusLocation != null) {
    				System.out.print("Whmpus>>");
    				possibleWhmpusLocation.direction = playerPosition.determineDirection(possibleWhmpusLocation);
    				world.killWhmpus();
    				probability[possibleWhmpusLocation.getRow()][possibleWhmpusLocation.getCol()]=0;
    				
    				return possibleWhmpusLocation;
    			}
    			
    			Coordinates safeLocation = confirmDanger(playerPosition);
    			
    			if (safeLocation != null) {
    				System.out.print("Got Safe Spot>> ");
    				return safeLocation;
    			} else {
    				System.out.print("No Safe Spot>> ");
    				return backTrack();
    			}
    			
    		} else if (percept.breeze) {
    			probability[playerPosition.getRow()+1][playerPosition.getCol()]+=25;
				probability[playerPosition.getRow()][playerPosition.getCol()+1]+=25;
				probability[playerPosition.getRow()-1][playerPosition.getCol()]+=25;
				probability[playerPosition.getRow()][playerPosition.getCol()-1]+=25;
				for (int i=1;i<11;i++)
				{
					for (int j=1; j<11;j++)
					{
						if(arr[i][j]>50)
						{
							int a =probability[playerPosition.getRow()+1][playerPosition.getCol()] ,
									b=probability[playerPosition.getRow()][playerPosition.getCol()+1]
									, c=probability[playerPosition.getRow()-1][playerPosition.getCol()], d
									=probability[playerPosition.getRow()][playerPosition.getCol()-1];
							Coordinates nextMove=null;
							int min_ab, min_cd, min;
							min_ab = a < b ? a : b;
							min_cd = c < d ? c : d;
							min = min_ab < min_cd ? min_ab : min_cd;
							if(min == a)
							{
								nextMove=new Coordinates(playerPosition.getRow()+1,playerPosition.getCol());
							}
							if(min == b)
							{
								nextMove=new Coordinates(playerPosition.getRow(),playerPosition.getCol()+1);
							}
							if(min == c)
							{
								nextMove=new Coordinates(playerPosition.getRow()-1,playerPosition.getCol());
							}
							if(min == d)
							{
								nextMove=new Coordinates(playerPosition.getRow(),playerPosition.getCol()-1);
							}
							//arr[i][j]=0;

							/*arr[playerPosition.getRow()+1][playerPosition.getCol()]=0;
							arr[playerPosition.getRow()][playerPosition.getCol()+1]=-0;
							arr[playerPosition.getRow()-1][playerPosition.getCol()]=-0;
							arr[playerPosition.getRow()][playerPosition.getCol()-1]=-0;
							arr[playerPosition.getRow()][playerPosition.getCol()]=-0;*/


							return nextMove;
						}
					}
				}



    		        			
    			Coordinates safeLocation = confirmDanger(playerPosition);
    			
    			if (safeLocation != null) {
    				System.out.print("Safe Location (Pit)>>");
    				return safeLocation;
    			}
    			
        		Coordinates playerPositionCopy = new Coordinates(playerPosition);
        		
        		playerPositionCopy.moveAhead();
        		
        		if (checkVisited(playerPositionCopy)) {
        			System.out.print("Found Ahead>> ");
        			return currentPosition.moveAhead();
        			
        		} else if ( getVisitedAdjecentCell(playerPosition) != null) {
        			
        			System.out.print("Found Adjacent>> ");
        			return getVisitedAdjecentCell(playerPosition); }
        			
        		 else {
        				return backTrack();
        		}
        		
        	} else {
        		Coordinates unvisitedCoordinate = getUnvisitedSpot(playerPosition);
        		System.out.print("Unvisited Spot>> ");
        		 return unvisitedCoordinate;
        	}
    	}
    	
    	return playerPosition;
    	
    }
    
//    void goBackToStart() {
//
//    	Coordinates position = popMove();
//    	position.printPosition();
//
//    	while(moves.size() > 0) {
//
//    		findSouthWestTiles(position);
//
//    		position = popMove();
//
//    		moveList.put(position.creatDirectionJSON());
//
//    		if (position.equals(new Coordinates(1,1))) {
//    			return;
//    		}
//    	}
//    }
    
    
//    private void findSouthWestTiles(Coordinates playerPosition) {
//
//    	System.out.print("Finding adjecent of: ");
//    	playerPosition.printPosition();
//    	System.out.println();
//
//    	int index;
//    	for(index = moves.size() - 1; index >= 0; index -= 1 ) {
//
//    		Coordinates possibleShortcutTile = moves.get(index);
//
//    		if((possibleShortcutTile.getRow() == playerPosition.getRow() - 1 )&& (possibleShortcutTile.getCol() == playerPosition.getCol())) {
//        		System.out.print("Got South: ");
//    			possibleShortcutTile.printPosition();
//    			System.out.println();
//
//    			break;
//    		} else if ((possibleShortcutTile.getCol() == playerPosition.getCol() - 1) && (possibleShortcutTile.getRow() == playerPosition.getRow())) {
//        		System.out.print("Got West: ");
//    			possibleShortcutTile.printPosition();
//    			System.out.println();
//
//    			break;
//    		}
//    	}
//
//    	for (int remIndex = moves.size() - 1; remIndex > index; remIndex -= 1) {
//
//    		moves.remove(remIndex);
//       	}
//
//    }
    
//    private Coordinates popMove() {
//    	Coordinates topMove = moves.remove(moves.size() - 1);
//    	return topMove;
//    }
    
    private Coordinates backTrack() {

    	if(moves.size() > 0) {
    		moves.remove(moves.size() - 1);

	        Coordinates basePoint = new Coordinates(moves.get(moves.size() - 1));

	        return basePoint;
    	}

    	return null;

    }
       
    private Coordinates	locateWhmpus() {
    	
    	ArrayList<Percept> whmpusPerceptList = new ArrayList<Percept>();
    	Coordinates whmpusLocation = null;
    	
    	for(Percept percept: knowledgeBase.values()) {
    		if(percept.stench) {
 
    			whmpusPerceptList.add(percept);
    		}
    	}
    	
    	if (whmpusPerceptList.size() == 2) {
    		
    		System.out.println("Found 2 percepts with stench");
    		
    		Coordinates firstPerceptLocation = new Coordinates(whmpusPerceptList.get(0).position);
    		Coordinates secondPerceptLocation = new Coordinates(whmpusPerceptList.get(1).position);
    		
    		Coordinates firstPossibleWhmpusLocation = new Coordinates(firstPerceptLocation.getRow(),secondPerceptLocation.getCol()); 
    		Coordinates secondPossibleWhmpusLocation = new Coordinates(firstPerceptLocation.getCol(),secondPerceptLocation.getRow()); 
    		
    		if (checkVisited(firstPossibleWhmpusLocation)) {
    			whmpusLocation = secondPossibleWhmpusLocation;
    		} else if (checkVisited(secondPossibleWhmpusLocation)) {
    		
    			whmpusLocation = firstPossibleWhmpusLocation;
    		}
       	}

		if (whmpusPerceptList.size() == 3) {

			System.out.println("Found 3 percepts with stench");

			Coordinates firstPerceptLocation = new Coordinates(whmpusPerceptList.get(0).position);
			Coordinates secondPerceptLocation = new Coordinates(whmpusPerceptList.get(1).position);
			Coordinates thirdPerceptLocation = new Coordinates(whmpusPerceptList.get(2).position);

			if(firstPerceptLocation.getCol()==secondPerceptLocation.getCol())
			{
				int row = (firstPerceptLocation.getRow()+secondPerceptLocation.getRow())/2;
				whmpusLocation=new Coordinates(row,firstPerceptLocation.getCol());

			}
			else if(firstPerceptLocation.getCol()==thirdPerceptLocation.getCol())
			{
				int row = (firstPerceptLocation.getRow()+thirdPerceptLocation.getRow())/2;
				whmpusLocation=new Coordinates(row,firstPerceptLocation.getCol());

			}

			else if(secondPerceptLocation.getCol()==thirdPerceptLocation.getCol())
			{
				int row = (secondPerceptLocation.getRow()+thirdPerceptLocation.getRow())/2;
				whmpusLocation=new Coordinates(row,secondPerceptLocation.getCol());

			}

			else if(firstPerceptLocation.getRow()==secondPerceptLocation.getRow())
			{
				int col = (firstPerceptLocation.getCol()+secondPerceptLocation.getCol())/2;
				whmpusLocation=new Coordinates(firstPerceptLocation.getRow(),col);

			}

			else if(firstPerceptLocation.getRow()==thirdPerceptLocation.getRow())
			{
				int col = (firstPerceptLocation.getCol()+thirdPerceptLocation.getCol())/2;
				whmpusLocation=new Coordinates(firstPerceptLocation.getRow(),col);

			}
			else if(secondPerceptLocation.getRow()==thirdPerceptLocation.getRow())
			{
				int col = (secondPerceptLocation.getCol()+thirdPerceptLocation.getCol())/2;
				whmpusLocation=new Coordinates(secondPerceptLocation.getRow(),col);

			}
		}
    	
//    	if(whmpusPerceptList.size() >= 3) {
//    		
//    		Coordinates tile1 = whmpusPerceptList.get(0).position;
//    		Coordinates tile2 = whmpusPerceptList.get(1).position;
//    		Coordinates tile3 = whmpusPerceptList.get(2).position;
//    		
//    		int whmpusRow = 0;
//    		int whmpusCol = 0;
//    		
//    		if(tile1.getRow() == tile2.getRow()) {
//    			whmpusRow = tile1.getRow();
//    			whmpusCol = tile3.getCol();
//    		} else if (tile1.getRow() == tile3.getRow()) {
//    			whmpusRow = tile1.getRow();
//    			whmpusCol = tile2.getCol();
//    		} else if (tile1.getCol() == tile2.getCol()) {
//    			whmpusCol = tile1.getCol();
//    			whmpusRow = tile3.getRow();
//    		} else if (tile1.getCol() == tile3.getCol()) {
//    			whmpusCol = tile1.getCol();
//    			whmpusRow = tile2.getRow();
//    		}
//    		
//    		if (whmpusCol != 0 && whmpusRow!=0) {
//    			
//    			
//    			for(Percept percept: knowledgeBase.values()) {
//    	    		percept.stench = false;
//    	    	}
//    			
//    			return new Coordinates(whmpusRow, whmpusCol);	
//    		}
//    	}
    	
    	if (whmpusLocation != null) {
    		clearStenchPercept();
    	}
    	return whmpusLocation;
    }

  
    private void clearStenchPercept() {
    	for(Percept percept: knowledgeBase.values()) {
    		percept.stench = false;
    	}    	
    }
    
    private Coordinates getUnvisitedSpot(Coordinates basePosition) {
    	
    	Coordinates basePositionCopy = new Coordinates(basePosition);
    	
    	if (!checkVisited(basePositionCopy.moveAhead())) {
    		
    		Percept percept = world.getPercept(basePositionCopy);
    		if (!percept.bump) {
    		return basePositionCopy;
    		} 
    	}
    	
    	basePositionCopy = new Coordinates(basePosition);
    	if (!checkVisited(basePositionCopy.moveEast())) {
    		
    		Percept percept = world.getPercept(basePositionCopy);
    		if (!percept.bump) {
    		return basePositionCopy;
    		}
    	}
    	
    	basePositionCopy = new Coordinates(basePosition);
    	if (!checkVisited(basePositionCopy.moveNorth())) {
    		Percept percept = world.getPercept(basePositionCopy);
    		if (!percept.bump) {
    		return basePositionCopy;
    		}
    	}
    	
    	basePositionCopy = new Coordinates(basePosition);
    	if (!checkVisited(basePositionCopy.moveSouth())) {
    		Percept percept = world.getPercept(basePositionCopy);
    		if (!percept.bump) {
    		return basePositionCopy;
    		}
    	}
    	
    	basePositionCopy = new Coordinates(basePosition);
    	if (!checkVisited(basePositionCopy.moveWest())) {
    		Percept percept = world.getPercept(basePositionCopy);
    		if (!percept.bump) {
    		return basePositionCopy;
    		}
    	}
    	
    	return getRandomDirection(basePosition); 
        	
    }
    
    Coordinates getRandomDirection(Coordinates playerPosition) {
    	
    	boolean hasBump = true;
    	Coordinates playerPositionCopy = new Coordinates(playerPosition);
    	while(hasBump) {
    		
    		playerPositionCopy = new Coordinates(playerPosition);
    		
    		int Min = 1, Max = 4;
        	int random = Min + (int)(Math.random() * ((Max - Min) + 1));
        	
        	switch (random) {
    		case 1:
    			playerPositionCopy.moveNorth();
    			break;
    		case 2:
    			playerPositionCopy.moveEast();
    			break;
    		case 3:
    			playerPositionCopy.moveWest();
    			break;
    		case 4:
    			playerPositionCopy.moveSouth();
    			break;
    		default:
    			break;
    		}
        	
        	Percept percept = world.getPercept(playerPositionCopy);

        	hasBump = percept.bump;
    	}
    			
    	return playerPositionCopy;
    	
    }
    
    void updateMoves(Coordinates newPoint) {
    	if (!moves.get(moves.size() - 1).equals(newPoint)) {
    		moves.add(newPoint);
    	} else {
    		System.out.println("Point already exists");
    	}
    }
    
    private Coordinates confirmDanger(Coordinates playerPosition) {
    	
    	Coordinates playerPositionCopy = new Coordinates(playerPosition);
    	
    	ArrayList<Coordinates> adjacentCellList = world.getAdjacentCells(playerPositionCopy);
    	
    	boolean isRisky = true;
    	
    	for (Coordinates possibleDangerCoordinate: adjacentCellList) {
    		isRisky = checkPreviousPercepts(possibleDangerCoordinate,playerPositionCopy);
    		if (!isRisky) {
    	   			
    			possibleDangerCoordinate.direction = playerPosition.determineDirection(possibleDangerCoordinate);
 
    			return possibleDangerCoordinate;
    		}
    	}
    	return null;
    }
    
    boolean checkPreviousPercepts(Coordinates possibleDangerCoordinate, Coordinates checkInitPosition) {
    	
    	boolean isRisky = true;
    	
    	ArrayList<Coordinates> perceptLocationList = world.getAdjacentCells(possibleDangerCoordinate);
    	
    	for (Coordinates perceptLocation: perceptLocationList) {
    		
    		if (!perceptLocation.equals(checkInitPosition) && knowledgeBase.containsKey(perceptLocation)) {
        		Percept positionPercept = knowledgeBase.get(perceptLocation);
        		
        		
        		Percept triggredPercept = knowledgeBase.get(checkInitPosition);
        		
        		if(triggredPercept.stench && !triggredPercept.breeze) {
        			if(!positionPercept.stench) {
        				isRisky = false;
        				return isRisky;
        			}
        		} else if (triggredPercept.breeze && !triggredPercept.stench) {
        			if(!positionPercept.breeze) {
        				isRisky = false;
        				return isRisky;
            		}
        		} else if (triggredPercept.breeze && triggredPercept.stench) {
        			if(!positionPercept.breeze && !positionPercept.stench) {
        				isRisky = false;
        				return isRisky;
            		}
        		}
        	
    		}
    	}
    	
    	return isRisky;
    
    }
    
    private Coordinates getVisitedAdjecentCell(Coordinates playerPosition) {
    	
    	Coordinates playerPositionCopy = new Coordinates(playerPosition);
    	
    	//EAST
    	if(checkVisited(playerPositionCopy.moveEast())) {
    		return playerPositionCopy;
    	}
    	
    	//WEST
    	playerPositionCopy = new Coordinates(playerPosition);
    	if(checkVisited(playerPositionCopy.moveWest())) {
    		return playerPositionCopy;
    	}
    	
    	//NORTH
    	playerPositionCopy = new Coordinates(playerPosition);
    	if(checkVisited(playerPositionCopy.moveNorth())) {
    		return playerPositionCopy;
    	}
    	
    	//SOUTH
    	playerPositionCopy = new Coordinates(playerPosition);
    	if(checkVisited(playerPositionCopy.moveSouth())) {
    		return playerPositionCopy;
    	}
    	
    	return null;
    }
     
   
    private boolean checkVisited(Coordinates playerPosition) {
    	
    	Coordinates playerPositionCopy = new Coordinates(playerPosition);
    	playerPositionCopy.direction = Directions.NONE;
    	return visited.contains(playerPositionCopy);
    }

}