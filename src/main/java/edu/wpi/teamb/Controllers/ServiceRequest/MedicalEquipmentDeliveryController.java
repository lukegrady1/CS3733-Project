package edu.wpi.teamb.Controllers.ServiceRequest;

import edu.wpi.teamb.Database.Requests.PatientTransportationRequest;
import edu.wpi.teamb.Navigation.Popup;
import edu.wpi.teamb.Navigation.Screen;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXFilterComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Control;

public class MedicalEquipmentDeliveryController extends BaseRequestController {
  // Lists for checkboxes
  private ObservableList<String> equipmentOptions =
      FXCollections.observableArrayList("Stretcher", "Wheelchair", "Restraints", "Stair Chair");
  @FXML private MFXComboBox equipmentNeededBox;

  /** Initialize the page by declaring choice-box options */
  @FXML
  @Override
  public void initialize() {
    // initialization goes here
    // Create list of components; additionalNotesField MUST be last
    Control[] ctrl = {
      urgencyBox, assignedStaffBox, locationBox, equipmentNeededBox, additionalNotesField
    };
    components = new ArrayList<>(Arrays.asList(ctrl));
    textFields = new ArrayList<>();
    choiceBoxes = new ArrayList<>();

    // Create lists of text fields and choice boxes
    for (Control c : components) {
      if (c instanceof MFXTextField) textFields.add((MFXTextField) c);
      if (c instanceof MFXFilterComboBox) choiceBoxes.add((MFXFilterComboBox) c);
    }
    equipmentNeededBox.setItems(equipmentOptions);

    super.initialize();
  }

  /**
   * Store the data from the form in a csv file and return to home screen
   *
   * @throws IOException
   */
  @FXML
  @Override
  public void submitButtonClicked() throws IOException {
    // handle retrieving values and saving

    PatientTransportationRequest request = new PatientTransportationRequest();
    super.submit(request);

    var equipment = equipmentNeededBox.getValue();
    if (equipment == null) {
      equipment = "";
    }
    request.setEquipmentNeeded(equipment.toString());

    // may need to clear fields can be done with functions made for clear
    clearButtonClicked();
    Popup.displayPopup(Screen.SUBMISSION_SUCCESS);
  }
}