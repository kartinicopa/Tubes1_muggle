package Enums;

public enum PlayerActions {
  Forward(1),
  Stop(2),
  StartAfterburner(3),
  StopAfterburner(4),
  FireTorpedoes(5),
  FireSupernova(6),
  DetonateSupernova(7),
  FireTeleport(8),
  Teleport(9),
  ActivateShield(10);

  public final Integer value;

  private PlayerActions(Integer value) {
    this.value = value;
  }
}
