package Whmpus;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import Whmpus.Constants.Directions;

class World {
	
	

    private int rowCount = 10;
    private int colCount = 10;

    public boolean isWhmpusDead = false;
    private boolean isGoldTaken = false;
    public boolean isPS;

    private Coordinates whmpusPosition;
    private Coordinates goldPosition;
    private ArrayList<Coordinates> pitPositions = new ArrayList<>();
    
    public World getPrespecifiedWorld() {
    	isPS=true;

		this.whmpusPosition = new Coordinates(6,7);
		this.goldPosition = new Coordinates(3,5);
		this.pitPositions.add(new Coordinates(2 ,6));
		this.pitPositions.add(new Coordinates(3 ,4));
		this.pitPositions.add(new Coordinates(3 ,6));
		this.pitPositions.add(new Coordinates(4 ,8));
		this.pitPositions.add(new Coordinates(5 ,6));
		this.pitPositions.add(new Coordinates(7 ,3));
		this.pitPositions.add(new Coordinates(7 ,4));


		return this;
    }
    
    public World generateRandomWorld() {
    	isPS=false;
    	int wumpusCount = 0;
    	int goldCount=0;
    	
    	double wumpusProbability = 0.05;
    	double pitProbability = 0.2;
    	double goldProbability = 0.05;
    	
    	for(int i=2;i<rowCount;i++) {
    		for(int j=2;j<colCount;j++) {
    			if(Math.random()<=wumpusProbability && wumpusCount<1) {
    				this.whmpusPosition = new Coordinates(i, j);
    				wumpusCount++;
    				continue;
    			}
				if(Math.random()<=goldProbability && goldCount<1) {
					this.goldPosition = new Coordinates(i, j);
					goldCount++;
					continue;
				}
    			
    			if(Math.random()<=pitProbability) {
    				this.pitPositions.add(new Coordinates(i,j));
    				continue;
    			}

    		}
    	}
    	return this;
    }

    
    public void exportMap() {
    	
    	
    	JSONObject map = new JSONObject();
    	
    	JSONArray pitPositionsJSONArray = new JSONArray();
    	
    	for(Coordinates pit: pitPositions) {
    		pitPositionsJSONArray.put(pit.creatDirectionJSON());
    	}
    	
    	try {
			map.put("pits", pitPositionsJSONArray);
		  	map.put("whmpus", whmpusPosition.creatDirectionJSON());
	    	map.put("gold", goldPosition.creatDirectionJSON());
	    	
	    	if(isPS==true)
			{
				FileWriter file = new FileWriter("./game-map-ps.json");
				file.write(map.toString());
				file.close();
			}
	    	if(isPS==false)
			{
				FileWriter file = new FileWriter("./game-map-rand.json");
				file.write(map.toString());
				file.close();
			}
	    			
		} catch (JSONException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
      	
    }
    
    public boolean goldTaken() {
		return isGoldTaken;
	}
   
    public boolean isWhmpusDead() {
    	return isWhmpusDead;
    }
    
    public Coordinates getGoldPosition() {
		return goldPosition;
	}
    
    public Coordinates getWhmpusPosition() {
		return whmpusPosition;
	}
    
    public ArrayList<Coordinates> getPitPositions() {
		return pitPositions;
	}
    
    
    Percept getPercept(Coordinates playerPosition) {
    	
    	boolean hasStench = false;
    	boolean hasGlitter = false;
    	boolean hasbreeze = false;
    	boolean hasBump = false;
    	
    	for (Coordinates pitPosition: pitPositions) {
    		hasbreeze = checkIfAdjacent(playerPosition, pitPosition);
    		if (hasbreeze) 
    			break;
    	}
    	hasStench = checkIfAdjacent(playerPosition, whmpusPosition) && !isWhmpusDead;
    	hasGlitter = playerPosition.equals(goldPosition);
    	hasBump = checkBump(playerPosition);
    	
    	return new Percept(hasbreeze,hasBump,hasGlitter,hasStench,playerPosition);
    	
    }

	boolean isPit(Coordinates playerPosition) {

    	boolean isPit=false;

		for (Coordinates pitPosition: pitPositions) {
			if(playerPosition.equals(pitPosition))
			{
				isPit=true;
			}
		}
		return isPit;

	}

	boolean isWumpus(Coordinates playerPosition) {

		boolean isWump=false;

		if(playerPosition.equals(whmpusPosition))
		{
			isWump=true;
		}
		return isWump;

	}
    
    
    public void killWhmpus() {
    	isWhmpusDead = true;
    }
    
    public void takeGold() {
		isGoldTaken = true;
	}
    
    public ArrayList<Coordinates> getAdjacentCells(Coordinates playerPosition) {
    	
    	ArrayList<Coordinates> dangerZoneList = new ArrayList<Coordinates>();
    	
    	int playerRow = playerPosition.getRow();
    	int playerCol = playerPosition.getCol();
    	
    	
    	if(playerCol - 1 >= 1) {
    		Coordinates eastCell = new Coordinates(playerRow, playerCol - 1);
    		dangerZoneList.add(eastCell);
    	}
    	
    	if(playerCol + 1 <= colCount) {
    		Coordinates westCell = new Coordinates(playerRow, playerCol + 1);
    		dangerZoneList.add(westCell);
    	}
    	
    	if(playerRow - 1 >= 1) {
    		Coordinates southCell = new Coordinates(playerRow - 1, playerCol);
    		dangerZoneList.add(southCell);
    	}
    	
    	if(playerRow + 1 <= rowCount) {
    		Coordinates northCell = new Coordinates(playerRow + 1, playerCol);
    		dangerZoneList.add(northCell);
    	}
    	
    	return dangerZoneList;
    }
    
    private boolean checkIfAdjacent(Coordinates playerPosition, Coordinates mapElement) {
    	
    	boolean isAdjacent = false;
    	
    	int playerRow = playerPosition.getRow();
    	int playerCol = playerPosition.getCol();
    	
    	int elementRow = mapElement.getRow();
    	int elementCol = mapElement.getCol();
    	
    	if (playerRow == elementRow && ( playerCol == elementCol - 1 || playerCol == elementCol + 1)) {
    		return true;
    	} else if (playerCol == elementCol && ( playerRow == elementRow - 1 || playerRow == elementRow + 1 )) {
    		return true;
    	}
     	
    	return isAdjacent;	
    }
    
    private boolean checkBump(Coordinates playerPosition) {
    	boolean hasBump = false;
    	int playerRow = playerPosition.getRow();
    	int playerCol = playerPosition.getCol();
    	Directions direction = playerPosition.getDirection();
    	
    	if ( playerRow < 0 || playerRow > rowCount || playerCol < 0 || playerCol > colCount) {
    		
    		hasBump = true;
    		return hasBump;
    	}
    	
    	if(direction == Directions.EAST && playerCol > colCount) {
    		hasBump = true;
    	} else if (direction == Directions.WEST && playerCol < 1) {
    		hasBump = true;
    	} else if (direction == Directions.SOUTH && playerRow < 1) {
    		hasBump = true;
    	} else if (direction == Directions.NORTH && playerRow > rowCount) {
    		hasBump = true;
    	}
    	
    	return hasBump;
    
    }
    
   
}