package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

public class BotService {
    private GameObject bot;
    private PlayerAction playerAction;
    private GameState gameState;

    public BotService() {
        this.playerAction = new PlayerAction();
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

    // method getClosestFood
    private GameObject getClosestFood() {
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

    // method getFoods
    private List<GameObject> getFoods() {
        // FOOD and SuperFood
        return this.gameState.getGameObjects().stream()
                .filter(gameObject -> gameObject.getGameObjectType() == ObjectTypes.Food
                        || gameObject.getGameObjectType() == ObjectTypes.Superfood)
                .collect(Collectors.toList());
    }
    // strategi makan
    private void eat() {
        // 1. Determine the closest food
        GameObject closestFood = getClosestFood();
        if (closestFood != null) {
            // 2. Determine the angle to the food
            double angleToFood = getHeadingBetween(closestFood); // getHeadingBetween(getBot());
            // 3. Determine the angle to the food relative to the bot's current heading
            double angleToFoodRelative = angleToFood - getHeadingBetween(bot);
            // 4. If the food is in front of the bot, move forward
            if (Math.abs(angleToFoodRelative) < 90) {
                // playerAction -> Forward
                playerAction.setAction(PlayerActions.Forward);
            }
            // 5. If the food is behind the bot, move backward
            else {
                playerAction.setAction(PlayerActions.Stop);
            }
            // 6. If the food is to the left of the bot, turn left
            if (angleToFoodRelative > 0) {
                playerAction.setAction(PlayerActions.Stop);
            }
            // 7. If the food is to the right of the bot, turn right
            else {
                playerAction.setAction(PlayerActions.Stop);
            }
        }
    }

        
    private void computeNextPlayerAction(PlayerAction playerAction) { 
        this.playerAction = playerAction;

    }

    private void avoidDanger() {
        // move away from danger
        var gasClouds = gameState.getGameObjects().stream().filter(gameObject -> gameObject.getGameObjectType() == ObjectTypes.GasCloud).collect(Collectors.toList());
        var asteroidField = gameState.getGameObjects().stream().filter(gameObject -> gameObject.getGameObjectType() == ObjectTypes.AsteroidField).collect(Collectors.toList());
        var danger = new ArrayList<GameObject>();
        danger.addAll(gasClouds);
        danger.addAll(asteroidField);
        var closestDanger = danger.stream().min(Comparator.comparing(gameObject -> getDistanceBetween(bot, gameObject))).orElse(null);
        if (closestDanger != null) {
            var distance = getDistanceBetween(bot, closestDanger);
            if (distance < 100) {
                playerAction.setAction(PlayerActions.Forward);
                playerAction.setHeading(getHeadingBetween(closestDanger) + 180);
            } else {
                playerAction.setAction(PlayerActions.Forward);
                playerAction.setHeading(0);
            }
        } else {
            playerAction.setAction(PlayerActions.Forward);
            playerAction.setHeading(0);
        }
    }

    public void protectBot() {
        // check if my bot is in danger if size decrease 
        // if my bot is in danger, ActiveShield, Teleport, StartAfterBurner, StopAfterburner, Forward
        if (bot.getSize() < 100) {
            var otherBots = gameState.getPlayerGameObjects().stream().filter(gameObject -> gameObject.id != bot.id).collect(Collectors.toList());
            var otherBotsSize = otherBots.stream().mapToInt(gameObject -> gameObject.getSize()).sum();
            if (otherBotsSize > bot.getSize()) {
                if (getShield() != null) {
                    playerAction.setAction(PlayerActions.ActivateShield);
                } else {
                    playerAction.setAction(PlayerActions.Teleport);
                    playerAction.setHeading(getHeadingBetween(otherBots.get(0)));
                }
            } else {
                playerAction.setAction(PlayerActions.Forward);
                playerAction.setHeading(0);
            }
        }
    }

    private GameObject getShield() {
        for (GameObject gameObject : gameState.getPlayerGameObjects()) {
            if (gameObject.getGameObjectType() == ObjectTypes.Shield) {
                return gameObject;
            }
        }
        return null;
    }

    public GameState getGameState() {
        return this.gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        updateSelfState();
    }

    private void updateSelfState() {  
        Optional<GameObject> optionalBot = gameState.getPlayerGameObjects().stream().filter(gameObject -> gameObject.id.equals(bot.id)).findAny();
        optionalBot.ifPresent(bot -> this.bot = bot);
    }

    private double getDistanceBetween(GameObject object1, GameObject object2) { // distance between two objects
        var triangleX = Math.abs(object1.getPosition().x - object2.getPosition().x);
        var triangleY = Math.abs(object1.getPosition().y - object2.getPosition().y);
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }

    private int getHeadingBetween(GameObject otherObject) { // heading between two objects
        var direction = toDegrees(Math.atan2(otherObject.getPosition().y - bot.getPosition().y,
                otherObject.getPosition().x - bot.getPosition().x));
        return (direction + 360) % 360;
    }

    private int toDegrees(double v) {
        return (int) (v * (180 / Math.PI));
    }
}