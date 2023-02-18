package edu.wpi.teamb.Database;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "computerrequest", schema = "iter3Testing")
@PrimaryKeyJoinColumn(
    name = "computerRequestID",
    foreignKey = @ForeignKey(name = "computerRequestIDKey_iter3Testing"))
public class ComputerRequest extends GeneralRequest {
  @Column(name = "typeofrepair", length = 60)
  @Getter
  @Setter
  private String typeOfRepair;

  @Column(name = "device", length = 60)
  @Getter
  @Setter
  private String device;

  @Column(name = "repairlocation", length = 60)
  @Getter
  @Setter
  private String repairLocation;

  public void ComputerRequest() {}
}
