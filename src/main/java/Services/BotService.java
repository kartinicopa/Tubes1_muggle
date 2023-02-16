package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

public class BotService {
    public GameObject bot;
    public PlayerAction playerAction;
    public GameState gameState;
    private static final double TORPEDO_SPEED = 8.0; // kecepatan torpedo salvo
    private static final double SHIELD_DISTANCE = 100.0; // jarak torpedo salvo dari bot ketika shield diaktifkan
    private static final double SHIELD_DURATION = 3.0; // durasi shield dalam detik
    // boolean setshield



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

    // method moveTowards for keep bot in ring
    public Position moveTowards(Position position, Position target, int distance) {
        double angle = Math.atan2(target.getY() - position.getY(), target.getX() - position.getX());
        return new Position((int) (position.getX() + distance * Math.cos(angle)), (int) (position.getY() + distance * Math.sin(angle)));
    }

    // getDistanceToCenter
    public double getDistanceToCenter(Position position, Position center) {
        return Math.sqrt(Math.pow(position.getX() - center.getX(), 2) + Math.pow(position.getY() - center.getY(), 2));
    }

    // method keep bot in ring
    public void keepBotInRing() {
        World world = gameState.getWorld();
        Position center = world.getCenterPoint();
        int radius = world.getRadius();
        Position botPosition = bot.getPosition();
        double distanceToCenter = getDistanceToCenter(botPosition, center);
        if (distanceToCenter > radius) {
            while (distanceToCenter > radius) {
                botPosition = moveTowards(botPosition, center, -1);
            }
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

    // method eatStrategy
    public void eatStrategy() {
        GameObject closestFood = getClosestFood();
        if (closestFood != null) {
            playerAction.setAction(PlayerActions.FORWARD);
            playerAction.setHeading(getHeadingBetween(closestFood));
            // if the closest food is more than 1, choose one
            if (getDistanceBetween(getBot(), closestFood) > 1) {
                playerAction.setAction(PlayerActions.FORWARD);
                playerAction.setHeading(getHeadingBetween(closestFood));
            }
        }
    }

    // method getDangerousObjects
    public List<GameObject> getDangerousObjects() {
        // GAS_CLOUD and ASTEROID_FIELD
        return this.gameState.getGameObjects().stream()
                .filter(gameObject -> gameObject.getGameObjectType() == ObjectTypes.GAS_CLOUD
                        || gameObject.getGameObjectType() == ObjectTypes.ASTEROID_FIELD)
                .collect(Collectors.toList());
    }

    // method geClosestDangerousObject
    public GameObject getClosestDangerousObject() {
        List<GameObject> dangerousObjects = getDangerousObjects();
        GameObject closestDangerousObject = null;
        double closestDangerousObjectDistance = Double.MAX_VALUE;
        for (GameObject dangerousObject : dangerousObjects) {
            double distance = getDistanceBetween(getBot(), dangerousObject);
            if (distance < closestDangerousObjectDistance) {
                closestDangerousObject = dangerousObject;
                closestDangerousObjectDistance = distance;
            }
        }
        return closestDangerousObject;
    }

    // method avoidDangerStrategy
    public void avoidDangerStrategy() {
        GameObject closestDangerousObject = getClosestDangerousObject();
        if (closestDangerousObject != null) {
            playerAction.setAction(PlayerActions.FORWARD);
            playerAction.setHeading(getHeadingBetween(closestDangerousObject) + 180);
        }
    }

    // method getOtherPlayers
    public List<GameObject> getOtherPlayers() {
        return this.gameState.getGameObjects().stream()
                .filter(gameObject -> gameObject.getGameObjectType() == ObjectTypes.PLAYER)
                .filter(gameObject -> gameObject.getId() != getBot().getId())
                .collect(Collectors.toList());
    }

    // method getClosestOtherPlayer
    public GameObject getClosestOtherPlayer() {
        List<GameObject> otherPlayers = getOtherPlayers();
        GameObject closestOtherPlayer = null;
        double closestOtherPlayerDistance = Double.MAX_VALUE;
        for (GameObject otherPlayer : otherPlayers) {
            double distance = getDistanceBetween(getBot(), otherPlayer);
            if (distance < closestOtherPlayerDistance) {
                closestOtherPlayer = otherPlayer;
                closestOtherPlayerDistance = distance;
            }
        }
        return closestOtherPlayer;
    }
    
    // method ACTIVATESHIELD IF THERE IS A TORPEDO SALVO COMING
    public void activateShieldIfDangerous() {
        List<GameObject> torpedoSalvos = getTorpedoSalvos();
        for (GameObject torpedoSalvo : torpedoSalvos) {
            double distance = getDistanceBetween(getBot(), torpedoSalvo);
            if (distance < SHIELD_DISTANCE) {
                playerAction.setAction(PlayerActions.ACTIVATE_SHIELD);
                break;
            }
        }
    }

    // method getTorpedoSalvos return TORPEDOSALVO
    public List<GameObject> getTorpedoSalvos() {
        return this.gameState.getGameObjects().stream()
                .filter(gameObject -> gameObject.getGameObjectType() == ObjectTypes.TORPEDO_SALVO)
                .collect(Collectors.toList());
    }

    // method attackStrategy
    public void attackStrategy() {
        GameObject closestOtherPlayer = getClosestOtherPlayer();
        if (closestOtherPlayer != null) {
            // if the other player is bigger than me, run away
            // INI MASIH BERESIKO SELF_DESTRUCT, PERLU PIKIR LAGI
            if (closestOtherPlayer.getSize() > getBot().getSize()) {
                playerAction.setAction(PlayerActions.FIRE_TORPEDOES);
                playerAction.setHeading(getHeadingBetween(closestOtherPlayer) + 180);

            }
            // if the other player is smaller than me, attack
            else {
                playerAction.setAction(PlayerActions.FORWARD);
                playerAction.setHeading(getHeadingBetween(closestOtherPlayer));
            }
        }
    }

    public void computeNextPlayerAction(PlayerAction playerAction) {
        // choose proper strategy
        // get the closest object to execute
        // execute the action
        // get distance between bot and closest object, do action for the closest object

        // if (!gameState.getGameObjects().isEmpty()) {
        //     if (gameState.getGameObjects().stream().anyMatch(x -> x.getGameObjectType() == ObjectTypes.FOOD)) {
        //         eatStrategy();
        //     }
        // } else {
        //     attackStrategy();
        // }

        // this.playerAction = playerAction;

        // attackStrategy();
        // avoidDangerStrategy();
        // eatStrategy();
        // if (getBot().getHealth() < 50) {
        //     avoidDangerStrategy();

        double distance1 = getDistanceBetween(getBot(), getClosestFood());
        double distance2 = getDistanceBetween(getBot(), getClosestDangerousObject());
        double distance3 = getDistanceBetween(getBot(), getClosestOtherPlayer());
        // GameObject[] closestList = {distance1, distance2, distance3};

        if ((distance1 >= distance2 && distance1 > distance3)) {
            eatStrategy();
        }
        else if (distance2 > distance1 && distance2 > distance3) {
            avoidDangerStrategy();
        }
        else if (distance3 > distance1 && distance3 > distance2) {
            attackStrategy();
        }

        this.playerAction = playerAction;
    }
    
    // public void avoidDanger() {
    //     // move away from danger
    //     var gasClouds = gameState.getGameObjects().stream().filter(gameObject -> gameObject.getGameObjectType() == ObjectTypes.GAS_CLOUD).collect(Collectors.toList());
    //     var asteroidField = gameState.getGameObjects().stream().filter(gameObject -> gameObject.getGameObjectType() == ObjectTypes.ASTEROID_FIELD).collect(Collectors.toList());
    //     var danger = Stream.concat(gasClouds.stream(), asteroidField.stream()).collect(Collectors.toList());
    //     if (!danger.isEmpty()) {
    //         // move away from danger
    //         var closestDanger = danger.get(0);
    //         var distance = getDistanceBetween(bot, closestDanger);
    //         if (distance < 100) {
    //             playerAction.setAction(PlayerActions.FORWARD);
    //             playerAction.setHeading(getHeadingBetween(closestDanger) + 180);
    //         }
    //     }
    // }


    // public void detectTorpedoSalvo() {
    //     var torpedoSalvo = gameState.getGameObjects().stream()
    //             .filter(gameObject -> gameObject.getGameObjectType() == ObjectTypes.TORPEDOSALVO)
    //             .collect(Collectors.toList());
    //     if (torpedoSalvo.size() > 0) {
    //         // if there is a torpedoSalvo, move away from it
    //         var closestTorpedoSalvo = torpedoSalvo.get(0);
    //         var distance = getDistanceBetween(bot, closestTorpedoSalvo);
    //         if (distance < 100) {
    //             // determine activate shield or move away from it
    //             if (getShield() != null) {
    //                 playerAction.setAction(PlayerActions.ACTIVATE_SHIELD);
    //                 playerAction.setHeading(getHeadingBetween(closestTorpedoSalvo) + 180);
    //             } else {
    //                 playerAction.setAction(PlayerActions.FORWARD);
    //                 playerAction.setHeading(getHeadingBetween(closestTorpedoSalvo) + 180);
    //             }
    //         } else {
    //             playerAction.setAction(PlayerActions.FORWARD);
    //             playerAction.setHeading(0);
    //         }
    //     }
    // }


    // public void protectBot() {
    //     // check if my bot is in danger if size decrease 
    //     // if my bot is in danger, ActiveShield, Teleport, StartAfterBurner, StopAfterburner, FORWARD
    //     if (bot.getSize() < 100) {
    //         var otherBots = gameState.getPlayerGameObjects().stream().filter(gameObject -> gameObject.id != bot.id).collect(Collectors.toList());
    //         var otherBotsSize = otherBots.stream().mapToInt(gameObject -> gameObject.getSize()).sum();
    //         if (otherBotsSize > bot.getSize()) {
    //             if (getShield() != null) {
    //                 playerAction.setAction(PlayerActions.ACTIVATE_SHIELD);
    //             } else {
    //                 playerAction.setAction(PlayerActions.TELEPORT);
    //                 playerAction.setHeading(getHeadingBetween(otherBots.get(0)));
    //             }
    //         } else {
    //             playerAction.setAction(PlayerActions.FORWARD);
    //             playerAction.setHeading(0);
    //         }
    //     }
    // }

    // public GameObject getShield() {
    //     for (GameObject gameObject : gameState.getPlayerGameObjects()) {
    //         if (gameObject.getGameObjectType() == ObjectTypes.SHIELD) {
    //             return gameObject;
    //         }
    //     }
    //     return null;
    // }

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

    // // resolve new action
    // public PlayerAction resolveAction() {
    //     // if dangerous object is near than food
    //     if (getClosestDangerousObject() != null && getClosestDangerousObject().getDistance() < getClosestFood().getDistance()) {
    //         // move away from dangerous object
    //         playerAction.setAction(PlayerActions.FORWARD);
    //         playerAction.setHeading(getHeadingBetween(getClosestDangerousObject()));
        

    //     return playerAction;
    // }
    // }
}