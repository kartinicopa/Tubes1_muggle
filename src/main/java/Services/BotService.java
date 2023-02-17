package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

public class BotService {
    public GameObject bot;
    public PlayerAction playerAction;
    public GameState gameState;
    public static final double SHIELD_DISTANCE = 100.0; 
    // WORLD RADIUS
    public static final double WORLD_RADIUS = 900.0;



    public BotService() {
        this.playerAction = new PlayerAction();
        this.gameState = new GameState();
    }

    public GameObject getBot() {
        return this.bot;
    }

    public void setBot(GameObject bot) {
        this.bot = bot;
    }

    public PlayerAction getPlayerAction() {
        return this.playerAction;
    }

    public void setPlayerAction(PlayerAction playerAction) {
        this.playerAction = playerAction;
    }

    public void computeNextPlayerAction(PlayerAction playerAction) {        
        if (!gameState.getGameObjects().isEmpty()) {    
            stayInsideTheRing();
        }     
        this.playerAction = playerAction; 
    }

    public void stayInsideTheRing() {
        var currentWorld = gameState.getWorld();
        var xPos = currentWorld.getCenterPoint().x;
        var yPos = currentWorld.getCenterPoint().y;
        var radius = currentWorld.getRadius();

        var triangleX = Math.abs(xPos - bot.getPosition().x);
        var triangleY = Math.abs(yPos - bot.getPosition().y);

        var distanceFromCenter = Math.sqrt(triangleX * triangleX + triangleY * triangleY);
        var direction = (toDegrees(Math.atan2(yPos - bot.getPosition().y, xPos - bot.getPosition().x)) + 360) % 360;

        if (distanceFromCenter > radius) {
            playerAction.setAction(PlayerActions.FORWARD);
            playerAction.setHeading(direction);
        }
        else {
            attackStrategy();
        }
    }

    public void attackStrategy() {
        var currentTick = gameState.getWorld().getCurrentTick();
        var playerList = gameState.getPlayerGameObjects()
                    .stream().filter(item -> item.getId() != bot.getId())
                    .sorted(Comparator
                            .comparing(item -> getDistanceBetween(bot, item)))
                    .collect(Collectors.toList());
        if (currentTick > 10 && (currentTick % 10 == 0) && bot.getSize() > 10) {
            if (playerList.get(0).getSize() >= bot.size) {
                playerAction.setAction(PlayerActions.FIRETORPEDOES);
                playerAction.setHeading(getHeadingBetween(playerList.get(0)));
            }
            else {
                playerAction.setAction(PlayerActions.FORWARD);
                playerAction.setHeading(getHeadingBetween(playerList.get(0)));
            }
        }
        else if (getSupernova() != null) {
            playerAction.setAction(PlayerActions.FORWARD);
            playerAction.setHeading(getHeadingBetween(getSupernova()));
        }
        else if (isGetSupernova()) {
            playerAction.setAction(PlayerActions.FIRESUPERNOVA);
            playerAction.setHeading(getHeadingBetween(playerList.get(0)));
        }
        else if (isSupernovaBomb() != null) {
            var distance = getDistanceBetween(bot, isSupernovaBomb());
            if (distance > 100) {
                playerAction.setAction(PlayerActions.DETONATESUPERNOVA);
                playerAction.setHeading(getHeadingBetween(playerList.get(0)));
            }
        }
        else {
            eatStrategy();
            protectBot();
            avoidDanger();
        }
    }

    public GameObject getSupernova() {
        for (GameObject gameObject : gameState.getGameObjects()) {
            if (gameObject.getGameObjectType() == ObjectTypes.SUPERNOVAPICKUP) {
                return gameObject;
            }
        }
        return null;
    }

    public boolean isGetSupernova() {
        for (GameObject gameObject : gameState.getPlayerGameObjects()) {
            if (gameObject.getGameObjectType() == ObjectTypes.SUPERNOVAPICKUP) {
                return true;
            }
        }
        return false;
    }

    public GameObject isSupernovaBomb() {
        for (GameObject gameObject : gameState.getGameObjects()) {
            if (gameObject.getGameObjectType() == ObjectTypes.SUPERNOVABOMB) {
                return gameObject;
            }
        }
        return null;
    }

    // method eatStrategy and update position agar tidak keluar dari world
    public void eatStrategy() {
        GameObject closestFood = getClosestFood();
        if (closestFood != null) {
            // makan food yang terdekat pertama
            playerAction.setAction(PlayerActions.FORWARD);
            playerAction.setHeading(getHeadingBetween(closestFood));
        }
    }
    
    // method getFoods
    public List<GameObject> getFoods() {
        return this.gameState.getGameObjects().stream()
                .filter(gameObject -> gameObject.getGameObjectType() == ObjectTypes.FOOD
                        || gameObject.getGameObjectType() == ObjectTypes.SUPERFOOD)
                .collect(Collectors.toList());
    }

    // method getClosestFood
    public GameObject getClosestFood() {
        List<GameObject> foods = getFoods();
        GameObject closestFood = null;
        double closestFoodDistance = Double.MAX_VALUE;
        for (GameObject food : foods) {
            double distance = getDistanceBetween(getBot(), food);
            if (distance < closestFoodDistance) {
                closestFood = food;
                closestFoodDistance = distance;
            }
        }
        return closestFood;
    }

    public void protectBot() {
        var otherBots = gameState.getPlayerGameObjects().stream().filter(gameObject -> gameObject.id != bot.id).collect(Collectors.toList());
        // if getTorpedoSalvos is coming from otherBots
        var torpedoSalvos = getTorpedoSalvos();
        var torpedoSalvosComing = torpedoSalvos.stream().filter(gameObject -> gameObject.getId() != bot.id).collect(Collectors.toList());
        if (torpedoSalvosComing.size() > 0) {
            if (getShield() != null) {
                playerAction.setAction(PlayerActions.ACTIVATESHIELD);
            } else {
                playerAction.setAction(PlayerActions.TELEPORT);
                playerAction.setHeading(getHeadingBetween(otherBots.get(0)));
            }
        }
    }

    // method getTorpedoSalvos return TORPEDOSALVO
    public List<GameObject> getTorpedoSalvos() {
        return this.gameState.getGameObjects().stream()
                .filter(gameObject -> gameObject.getGameObjectType() == ObjectTypes.TORPEDOSALVO)
                .collect(Collectors.toList());
    }

    private GameObject getShield() {
        for (GameObject gameObject : gameState.getPlayerGameObjects()) {
            if (gameObject.getGameObjectType() == ObjectTypes.SHIELD) {
                return gameObject;
            }
        }
        return null;
    }
    
    public void avoidDanger() {
        // move away from danger
        var gasClouds = gameState.getGameObjects().stream().filter(gameObject -> gameObject.getGameObjectType() == ObjectTypes.GASCLOUD).collect(Collectors.toList());
        var asteroidField = gameState.getGameObjects().stream().filter(gameObject -> gameObject.getGameObjectType() == ObjectTypes.ASTEROIDFIELD).collect(Collectors.toList());
        var danger = new ArrayList<GameObject>();
        danger.addAll(gasClouds);
        danger.addAll(asteroidField);
        var closestDanger = danger.stream().min(Comparator.comparing(gameObject -> getDistanceBetween(bot, gameObject))).orElse(null);
        if (closestDanger != null) {
            var distance = getDistanceBetween(bot, closestDanger);
            if (distance < 100) {
                playerAction.setAction(PlayerActions.FORWARD);
                playerAction.setHeading(getHeadingBetween(closestDanger) + 180);
            } 
        }
    }

    public GameState getGameState() {
        return this.gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        updateSelfState();
    }

    public void updateSelfState() {  
        Optional<GameObject> optionalBot = gameState.getPlayerGameObjects().stream().filter(gameObject -> gameObject.id.equals(bot.id)).findAny();
        optionalBot.ifPresent(bot -> this.bot = bot);
    }

    public int getHeadingBetween(GameObject otherObject) { // heading between two objects
        var direction = toDegrees(Math.atan2(otherObject.getPosition().y - bot.getPosition().y,
                otherObject.getPosition().x - bot.getPosition().x));
        return (direction + 360) % 360;
    }

    public int toDegrees(double v) {
        return (int) (v * (180 / Math.PI));
    }

    // method getDistance
    public double getDistanceBetween(GameObject object1, GameObject object2) {
        var triangleX = Math.abs(object1.getPosition().x - object2.getPosition().x);
        var triangleY = Math.abs(object1.getPosition().y - object2.getPosition().y);
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }
}