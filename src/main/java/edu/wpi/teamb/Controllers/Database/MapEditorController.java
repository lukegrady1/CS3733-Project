package edu.wpi.teamb.Controllers.Database;

import edu.wpi.teamb.Bapp;
import edu.wpi.teamb.Controllers.Profile.SigninController;
import edu.wpi.teamb.Database.*;
import edu.wpi.teamb.Database.DAO.MapDAO;
import edu.wpi.teamb.Navigation.Navigation;
import edu.wpi.teamb.Navigation.Popup;
import edu.wpi.teamb.Navigation.Screen;
import edu.wpi.teamb.Pathfinding.Pathfinding;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXCheckbox;
import io.github.palexdev.materialfx.controls.MFXFilterComboBox;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import lombok.Getter;
import net.kurobako.gesturefx.GesturePane;

public class MapEditorController {
  @FXML GridPane gridPane;
  @FXML GridPane map;
  @FXML MFXButton editNodeButton;
  @FXML MFXButton newNodeButton;
  @FXML MFXButton viewMovesButton;
  @FXML MFXButton editLocationButton;
  @FXML MFXButton newMoveButton;
  @FXML MFXCheckbox showLocationsCheckBox;
  @Getter @FXML private AnchorPane forms;
  private List<Label> locLabels = new ArrayList<>();
  private final ObjectProperty<Circle> selectedCircle = new SimpleObjectProperty<>();
  Map<Circle, Node> nodeMap;
  AnchorPane currentPopUp;
  private static Node currentNode;
  private static Circle currentDot;
  private final int POP_UP_HEIGHT = 110;
  private GesturePane pane;
  private AnchorPane aPane;
  private double origX, origY;
  private boolean dragged;
  private boolean MOVING = false;
  private Circle edgeNode1, edgeNode2;
  private boolean creatingEdge;
  private static MapEditorController instance;
  private Map<String, List<Move>> moveMap;
  @FXML MFXFilterComboBox<String> floorCombo;
  private Map<String, String> floorMap = new HashMap<>();
  private Map<String, ImageView> imageMap = new HashMap<>();
  private Map<Node, AnchorPane> locationMap = new HashMap<>();
  private String currentFloor;
  @FXML VBox mapEditorButtons;

  @FXML MFXButton newnode;
  @FXML MFXButton newedge;
  @FXML MFXButton editlocation;
  @FXML MFXButton newLocation;
  @FXML MFXButton newmove;
  @FXML MFXButton viewmoves;
  @FXML Label timeLabel;
  @FXML Label dateLabel;

  public void initialize() {
    if (instance == null) {
      moveMap = DBSession.getIDMoves(new Date(2023, 1, 1));
    } else {
      moveMap = DBSession.getIDMoves();
    }
    instance = this;

    floorMap.put("Lower Level 2", "L2");
    floorMap.put("Lower Level 1", "L1");
    floorMap.put("Ground Floor", "G");
    floorMap.put("First Floor", "1");
    floorMap.put("Second Floor", "2");
    floorMap.put("Third Floor", "3");

    imageMap.put("L2", Bapp.lowerlevel2);
    imageMap.put("L1", Bapp.lowerlevel);
    imageMap.put("G", Bapp.groundfloor);
    imageMap.put("1", Bapp.firstfloor);
    imageMap.put("2", Bapp.secondfloor);
    imageMap.put("3", Bapp.thirdfloor);

    floorCombo.setItems(
        FXCollections.observableArrayList(
            "Lower Level 2",
            "Lower Level 1",
            "Ground Floor",
            "First Floor",
            "Second Floor",
            "Third Floor"));
    nodeMap = new HashMap<>();
    pane = new GesturePane();
    pane.setOnKeyPressed(e -> handleKeyPress(e));

    pane.setPrefHeight(714);
    pane.setPrefWidth(1168);
    pane.setScrollBarPolicy(GesturePane.ScrollBarPolicy.NEVER);
    aPane = new AnchorPane();

    aPane.setOnMouseClicked(
        e -> {
          if (e.getClickCount() == 2) {
            Node n = new Node();
            n.setBuilding("Tower");
            n.setFloor(currentFloor);
            n.setXCoord((int) e.getX());
            n.setYCoord((int) e.getY());
            n.setNodeID(n.buildID());
            DBSession.addNode(n);
            Circle c = placeNode(n);

            c.setOnMouseClicked(
                ev -> {
                  if (currentDot != null) currentDot.setFill(Color.valueOf("#21375E"));
                  displayPopUp(c);
                  c.setFill(Color.GOLD);
                  if (creatingEdge) {
                    if (edgeNode1 == null) edgeNode1 = c;
                    else if (edgeNode2 == null && c != edgeNode1) {
                      edgeNode2 = c;
                      createEdge();
                    }
                  }
                });

            nodeMap.put(c, n);
            selectedCircle.set(c);
          }
        });

    pane.setContent(aPane);
    map.getChildren().add(pane);
    // Changes floor when selecting a new floor
    floorCombo.setOnAction(
        e -> {
          changeFloor(floorMap.get(floorCombo.getValue()), pane.targetPointAtViewportCentre());
          boolean showLocations = showLocationsCheckBox.isSelected();
          for (Label loc : locLabels) {
            loc.setVisible(showLocations);
          }
        });
    pane.zoomTo(-5000, -3000, Point2D.ZERO);
    Platform.runLater(
        () -> {
          if (SigninController.currentUser.getAdmin()) {
            HBox csvBox = new HBox();
            csvBox.setSpacing(20);
            csvBox.setPrefWidth(458);
            csvBox.setPrefHeight(17);
            MFXButton write = new MFXButton();
            write.setPrefWidth(155);
            write.setPrefHeight(42);
            write.setTextFill(Paint.valueOf("#c5d3ea"));
            write.setStyle("-fx-background-color: #21357E");
            write.setFont(new Font("System", 20));
            write.setText("Write to CSV");
            write.setOnAction(
                e -> {
                  try {
                    DatabaseWriteToCSV.runWrites();
                  } catch (IOException ex) {
                    throw new RuntimeException(ex);
                  } catch (ParseException ex) {
                    throw new RuntimeException(ex);
                  }
                });

            MFXButton restore = new MFXButton();
            restore.setPrefWidth(155);
            restore.setPrefHeight(42);
            restore.setTextFill(Paint.valueOf("#c5d3ea"));
            restore.setStyle("-fx-background-color: #21357E");
            restore.setFont(new Font("System", 20));
            restore.setText("Database");
            restore.setOnAction(
                e -> {
                  try {
                    DatabaseRestore.runRestore();
                  } catch (FileNotFoundException ex) {
                    throw new RuntimeException(ex);
                  } catch (URISyntaxException ex) {
                    throw new RuntimeException(ex);
                  } catch (ParseException ex) {
                    throw new RuntimeException(ex);
                  }
                });

            csvBox.getChildren().add(write);
            csvBox.getChildren().add(restore);
            mapEditorButtons.getChildren().add(csvBox);
          }
          changeFloor("L1", new javafx.geometry.Point2D(2215, 1045));
        });

    LocalDate currentDate = LocalDate.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
    String formattedDate = currentDate.format(formatter);

    Timeline timeline =
        new Timeline(
            new KeyFrame(
                Duration.seconds(0),
                event -> {
                  LocalDateTime currentTime = LocalDateTime.now();
                  DateTimeFormatter timefmt = DateTimeFormatter.ofPattern("h:mm a");
                  timeLabel.setText(currentTime.format(timefmt));
                }),
            new KeyFrame(Duration.seconds(1)));
    timeline.setCycleCount(Timeline.INDEFINITE);
    timeline.play();
    dateLabel.setText(formattedDate);
  }

  private void setActive(MFXButton button) {
    resetButton();
    button.setStyle("-fx-background-color: #6D9BF8;");
  }

  private void resetButton() {
    newnode.setStyle("-fx-background-color:  #21357E;");
    newedge.setStyle("-fx-background-color:  #21357E;");
    editlocation.setStyle("-fx-background-color:  #21357E;");
    newLocation.setStyle("-fx-background-color:  #21357E;");
    newmove.setStyle("-fx-background-color:  #21357E;");
    viewmoves.setStyle("-fx-background-color:  #21357E;");
  }

  private void changeFloor(String floor, Point2D p) {
    currentFloor = floor;
    ImageView image;
    nodeMap.clear();

    image = imageMap.get(floor);
    image.setOnMouseClicked(e -> handleClick());

    aPane.getChildren().clear();
    aPane.getChildren().add(image);

    Map<String, Node> nodes = DBSession.getAllNodes();

    for (Node node : nodes.values()) {
      if (node.getFloor().equals(currentFloor)) {
        Circle dot = placeNode(node);
        dot.setOnMouseClicked(
            e -> {
              if (currentDot != null) currentDot.setFill(Color.valueOf("#21375E"));
              displayPopUp(dot);
              dot.setFill(Color.GOLD);
              if (creatingEdge) {
                if (edgeNode1 == null) edgeNode1 = dot;
                else if (edgeNode2 == null && dot != edgeNode1) {
                  edgeNode2 = dot;
                  createEdge();
                }
              }
            });

        nodeMap.put(dot, node);
        displayLoc(dot);
      }
    }

    Platform.runLater(
        () -> {
          pane.centreOn(p);
        });
  }

  public void displayPopUp(Circle dot) {
    clearPopUp();
    Node node = nodeMap.get(dot);

    AnchorPane popPane = new AnchorPane();
    popPane.setTranslateX(dot.getCenterX() + dot.getRadius() * 2);
    popPane.setTranslateY(dot.getCenterY() - dot.getRadius() * 2 - POP_UP_HEIGHT);
    popPane.setStyle("-fx-background-color: FFFFFF; -fx-border-color: black;");

    VBox vbox = new VBox();
    popPane.getChildren().add(vbox);

    Text id = new Text("NodeID:   " + node.getNodeID());
    Text pos = new Text("(x, y):  " + "(" + node.getXCoord() + ", " + node.getYCoord() + ")");

    List<Move> moves = DBSession.getMostRecentMoves(node.getNodeID());
    String t;
    if (moves == null) t = "NO MOVES";
    else {
      t = moves.get(0).getLocationName().getLongName();
      if (moves.size() > 1) t += "\n" + moves.get(1).getLocationName().getLongName();
    }
    Text loc = new Text(t);

    Button editButton = new Button("Edit");
    editButton.setStyle("-fx-background-color: #003AD6; -fx-text-fill: white;");
    editButton.setOnAction(
        (eventAction) -> {
          try {
            editClicked();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
    vbox.setSpacing(5);
    //    vbox.setAlignment(Pos.CENTER);
    vbox.setPadding(new Insets(10, 10, 10, 10));

    vbox.getChildren().add(id);
    vbox.getChildren().add(pos);
    vbox.getChildren().add(loc);

    HBox hbox = new HBox();
    hbox.getChildren().add(editButton);
    hbox.setAlignment(Pos.CENTER);
    vbox.getChildren().add(hbox);

    currentNode = node;
    drawEdges();
    aPane.getChildren().add(popPane);
    currentPopUp = popPane;
    currentDot = dot;
  }

  public void displayLoc(Circle dot) {
    Node node = nodeMap.get(dot);
    AnchorPane popPane = new AnchorPane();
    popPane.setTranslateX(dot.getCenterX() + dot.getRadius() * 2 - 50);
    popPane.setTranslateY(dot.getCenterY() - dot.getRadius() * 2 + 38);

    locationMap.put(node, popPane);

    VBox vbox = new VBox();
    popPane.getChildren().add(vbox);
    List<Move> l = moveMap.get(node.getNodeID());
    if (l == null) l = Arrays.asList();
    for (Move move : l) {
      if (move.getLocationName().getLocationType().equals("HALL")) continue;
      Label loc = new Label(move.getLocationName().getLongName());
      loc.setFont(new Font("Arial", 8));
      vbox.getChildren().add(loc);
      loc.setVisible(false);
      locLabels.add(loc);
    }

    HBox hbox = new HBox();
    hbox.setAlignment(Pos.CENTER);
    vbox.getChildren().add(hbox);
    aPane.getChildren().add(popPane);
  }

  public void showLocationsClicked() {

    showLocationsCheckBox.setOnAction(
        e -> {
          boolean showLocations = showLocationsCheckBox.isSelected();
          for (Label loc : locLabels) {
            loc.setVisible(showLocations);
          }
        });
  }

  private void editClicked() throws IOException {
    setActive(editNodeButton);
    forms.getChildren().clear();
    final var res = Bapp.class.getResource(Screen.NODE_EDITOR.getFilename());
    final FXMLLoader loader = new FXMLLoader(res);
    forms.getChildren().add(loader.load());
  }

  private void clearPopUp() {
    if (currentPopUp != null) {
      aPane.getChildren().remove(currentPopUp);
      currentPopUp = null;
      if (currentDot != null) currentDot.setFill(Color.valueOf("#21357E"));
      currentNode = null;
      currentDot = null;
      removeEdges();
    }
  }

  private void removeEdges() {
    List<javafx.scene.Node> children = aPane.getChildren();
    for (int i = children.size() - 1; i >= 0; i--) {
      if (children.get(i) instanceof Line) aPane.getChildren().remove(children.get(i));
    }
  }

  public Circle placeNode(Node node) {
    Circle dot = new Circle(node.getXCoord(), node.getYCoord(), 10, Color.RED);
    aPane.getChildren().add(dot);
    dot.getStyleClass().add("intersection");
    dot.setCursor(Cursor.HAND);

    dot.setOnMousePressed(
        (e) -> {
          origX = e.getSceneX();
          origY = e.getSceneY();
          if (currentDot != null) currentDot.setFill(Color.valueOf("#21357E"));
          currentDot = dot;

          pane.setGestureEnabled(false);

          Circle c = (Circle) (e.getSource());
          c.toFront();
        });

    dot.setOnMouseReleased(
        e -> {
          pane.setGestureEnabled(true);
          if (dragged) {
            updateNode(dot);
            AnchorPane popPane = locationMap.get(nodeMap.get(dot));
            if (popPane != null) {
              popPane.setTranslateX(dot.getCenterX() + dot.getRadius() * 2 - 50);
              popPane.setTranslateY(dot.getCenterY() - dot.getRadius() * 2 + 38);
            }
          }
          dragged = false;
        });

    dot.setOnMouseDragged(
        (e) -> {
          double offsetX = (e.getSceneX() - origX) / pane.getCurrentScaleX();
          double offsetY = (e.getSceneY() - origY) / pane.getCurrentScaleY();
          Circle c = (Circle) (e.getSource());
          c.setCenterX(c.getCenterX() + offsetX);
          c.setCenterY(c.getCenterY() + offsetY);
          origX = e.getSceneX();
          origY = e.getSceneY();
          dragged = true;
        });

    return dot;
  }

  public void updateNode(Circle dot) {
    Node node = nodeMap.get(dot);
    node.setXCoord((int) dot.getCenterX());
    node.setYCoord((int) dot.getCenterY());
    DBSession.updateNode(node);
    node.setNodeID(node.buildID());
    currentNode = node;
    currentDot = dot;
    Pathfinding.refreshData();
    MapDAO.refreshIDMoves(new Date(System.currentTimeMillis()));
    refreshPopUp();
  }

  public void handleClick() {
    selectedCircle.set(null);
    clearPopUp();
    if (edgeNode1 != null) {
      edgeNode1.setFill(Color.GOLD);
    }
  }

  public void editLocationClicked() throws IOException {
    setActive(editlocation);
    forms.getChildren().clear();
    final var res = Bapp.class.getResource(Screen.LOCATION_EDITOR.getFilename());
    final FXMLLoader loader = new FXMLLoader(res);
    forms.getChildren().add(loader.load());
  }

  public void home() {
    Navigation.navigate(Screen.HOME);
  }

  @FXML
  private void newNodeClicked() throws IOException {

    forms.getChildren().clear();
    final var res = Bapp.class.getResource(Screen.NODE_CREATOR.getFilename());
    final FXMLLoader loader = new FXMLLoader(res);
    forms.getChildren().add(loader.load());
    Platform.runLater(() -> setActive(newnode));
  }

  public void newEdgeClicked() throws IOException {
    setActive(newedge);
    forms.getChildren().clear();
    final var res = Bapp.class.getResource(Screen.EDGE_CLICK_CREATOR.getFilename());
    final FXMLLoader loader = new FXMLLoader(res);
    forms.getChildren().add(loader.load());
    creatingEdge = true;
  }

  public void cancelClickEdge() {
    if (edgeNode1 != null) {
      edgeNode1.setFill(Color.valueOf("#21357E"));
      edgeNode1 = null;
    }
    if (edgeNode2 != null) {
      edgeNode2.setFill(Color.valueOf("#21357E"));
      edgeNode2 = null;
    }
    clearForm();
  }

  public void clearForm() {
    resetButton();
    forms.getChildren().clear();
  }

  public void createEdge() {
    if (edgeNode1 == null || edgeNode2 == null) return;
    Edge e = new Edge();
    e.setNode1(nodeMap.get(edgeNode1));
    e.setNode2(nodeMap.get(edgeNode2));
    DBSession.addEdge(e);
    Pathfinding.refreshData();
    cancelClickEdge();
    creatingEdge = false;
  }

  public void editNodeClicked() throws IOException {
    setActive(editNodeButton);
    forms.getChildren().clear();
    final var res = Bapp.class.getResource(Screen.SIDE_NODE_EDITOR.getFilename());
    final FXMLLoader loader = new FXMLLoader(res);
    forms.getChildren().add(loader.load());
  }

  public void newMovesClicked() throws IOException {
    setActive(newmove);
    forms.getChildren().clear();
    final var res = Bapp.class.getResource(Screen.MOVE_CREATOR.getFilename());
    final FXMLLoader loader = new FXMLLoader(res);
    forms.getChildren().add(loader.load());
  }

  public void viewMovesClicked() throws IOException {
    setActive(viewmoves);
    forms.getChildren().clear();
    final var res = Bapp.class.getResource(Screen.FUTURE_MOVES.getFilename());
    final FXMLLoader loader = new FXMLLoader(res);
    forms.getChildren().add(loader.load());
  }

  public void newLocationClicked() throws IOException {
    setActive(newLocation);
    forms.getChildren().clear();
    final var res = Bapp.class.getResource(Screen.LOCATION_CREATOR.getFilename());
    final FXMLLoader loader = new FXMLLoader(res);
    forms.getChildren().add(loader.load());
  }

  static Node getCurrentNode() {
    return currentNode;
  }

  public void refreshPopUp() {
    if (!currentNode.getFloor().equals("L1")) {
      removeNode();
      return;
    }

    currentDot.setCenterX(currentNode.getXCoord());
    currentDot.setCenterY(currentNode.getYCoord());
    nodeMap.replace(currentDot, currentNode);

    if (currentPopUp != null) {
      ObservableList vboxChildren = ((VBox) (currentPopUp.getChildren().get(0))).getChildren();
      Text id = (Text) vboxChildren.get(0);
      Text pos = (Text) vboxChildren.get(1);
      Text loc = (Text) vboxChildren.get(2);
      id.setText("NodeID:   " + currentNode.buildID());
      pos.setText(
          "(x, y):  " + "(" + currentNode.getXCoord() + ", " + currentNode.getYCoord() + ")");

      List<Move> moves = DBSession.getMostRecentMoves(currentNode.getNodeID());
      String t;
      if (moves != null) {
        t = moves.get(0).getLocationName().getLongName();
        if (moves.size() > 1) t += "\n" + moves.get(1).getLocationName().getLongName();
      } else t = "NO MOVES";

      loc.setText(t);
      currentPopUp.setTranslateX(currentDot.getCenterX() + currentDot.getRadius() * 2);
      currentPopUp.setTranslateY(
          currentDot.getCenterY() - currentDot.getRadius() * 2 - POP_UP_HEIGHT);
    }
  }

  public void removeNode() {
    aPane.getChildren().remove(currentDot);
    nodeMap.remove(currentDot);
    clearPopUp();
  }

  public static MapEditorController getInstance() {
    return instance;
  }

  public static void setCurrentNode(Node currentNode) {
    MapEditorController.currentNode = currentNode;
  }

  public Circle getDot(Node n) {
    Circle c;
    for (Map.Entry<Circle, Node> entry : nodeMap.entrySet()) {
      Node value = entry.getValue();
      if (value.equals(n)) return entry.getKey();
    }
    return null;
  }

  public void setCurrentDot(Circle dot) {
    currentDot = dot;
  }

  public static Circle getCurrentDot() {
    return currentDot;
  }

  public static void promptEdgeRepair(Node node) {
    Pathfinding.refreshData();
    List<String> nodes = Pathfinding.getDirectPaths(node.getNodeID());
    Map<String, Node> allNodes = DBSession.getAllNodes();
    if (nodes.size() != 2) return;
    EdgeRepairController.setNodes(allNodes.get(nodes.get(0)), allNodes.get(nodes.get(1)));
    Popup.displayPopup(Screen.EDGE_REPAIR);
  }

  public void helpButtonClicked() {
    Popup.displayPopup(Screen.MAP_EDITOR_HELP_POP_UP);
  }

  public void drawEdges() {
    List<String> edges = Pathfinding.getDirectPaths(currentNode.getNodeID());
    Map<String, Node> map = DBSession.getAllNodes();
    // aPane.getChildren().clear();
    for (String id : edges)
      if (currentNode.getFloor().equals(map.get(id).getFloor())) {
        drawLineBetween(currentNode, map.get(id));
      }
  }

  private void drawLineBetween(Node n1, Node n2) {
    Line line = new Line(n1.getXCoord(), n1.getYCoord(), n2.getXCoord(), n2.getYCoord());
    line.setFill(Color.BLACK);
    line.setStrokeWidth(5);
    aPane.getChildren().add(line);
  }

  public void handleKeyPress(KeyEvent e) {
    if (e.getCode().equals(KeyCode.BACK_SPACE)) {
      Node n = nodeMap.get(currentDot);
      promptEdgeRepair(n);
      removeNode();
      DBSession.deleteNode(n);
    }
  }
}
