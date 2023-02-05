package Enums;

public enum ObjectTypes {
  Player(1),
  Food(2),
  Wormhole(3),
  GasCloud(4),
  AsteroidField(5),
  TorpedoSalvo(6),
  Superfood(7),
  SupernovaPickup(8),
  SupernovaBomb(9),
  Teleporter(10),
  Shield(11);

  public final Integer value;

  ObjectTypes(Integer value) {
    this.value = value;
  }

  public static ObjectTypes valueOf(Integer value) {
    for (ObjectTypes objectType : ObjectTypes.values()) {
      if (objectType.value == value) return objectType;
    }

    throw new IllegalArgumentException("Value not found");
  }
}