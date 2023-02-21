package edu.wpi.teamb.Controllers.ServiceRequest.SubmittedRequests;

import edu.wpi.teamb.Database.DBSession;
import edu.wpi.teamb.Database.Requests.GeneralRequest;
import edu.wpi.teamb.Database.Requests.SanitationRequest;
import edu.wpi.teamb.Entities.RequestStatus;
import edu.wpi.teamb.Entities.Urgency;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class SubmittedSanitationRequestTable extends SubmittedBaseRequestTable {

  @FXML private TableColumn typeOfCleanUp = new TableColumn<>();
  @FXML private TableColumn cleanUpLocation = new TableColumn<>();

  @Override
  public void initialize() {
    super.initialize();
    //    addCol(typeOfCleanUp, "typeOfCleanUp");
    //    addCol(cleanUpLocation, "cleanUpLocation");
    setTable();
  }

  @Override
  public TableView getTable(
      RequestStatus status, String Employee, Urgency urgency, Boolean myRequests) {
    table.getItems().clear();
    super.filterTable(status, Employee, convertObj(), urgency, myRequests);
    return table;
  }

  private List<GeneralRequest> convertObj() {
    List<GeneralRequest> grList = new ArrayList<>();
    List<SanitationRequest> objectList = DBSession.getAllSanRequests();
    objectList.forEach(
        (value) -> {
          grList.add(value);
        });
    return grList;
  }
}
