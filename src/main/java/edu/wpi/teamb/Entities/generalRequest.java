package edu.wpi.teamb.Entities;

import lombok.Getter;
import lombok.Setter;

public class generalRequest {
  @Getter @Setter private String firstName;
  @Getter @Setter private String LastName;
  @Getter @Setter private String email;
  @Getter @Setter private String EmployeeID;
  @Getter @Setter private String Urgency;
  @Getter @Setter private String Notes;

  public void generalRequest() {}
}
