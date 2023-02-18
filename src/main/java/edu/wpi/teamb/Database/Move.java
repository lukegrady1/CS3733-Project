package edu.wpi.teamb.Database;

import jakarta.persistence.*;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;

@Entity
@Table(name = "move", schema = "iter3Testing")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Move {

  @Id
  @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
  @JoinColumn(
      name = "node",
      nullable = false,
      foreignKey =
          @ForeignKey(
              name = "move_node_fk_iter3Testing",
              foreignKeyDefinition =
                  "FOREIGN KEY (node) REFERENCES iter3Testing.Node(nodeID) ON UPDATE CASCADE ON DELETE CASCADE"))
  @ManyToOne
  @Setter
  @Getter
  private Node node;

  @Id
  @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
  @JoinColumn(
      name = "locationName",
      nullable = false,
      foreignKey =
          @ForeignKey(
              name = "move_locationName_fk_iter3Testing",
              foreignKeyDefinition =
                  "FOREIGN KEY (locationName) REFERENCES iter3Testing.LocationName(longName) ON UPDATE CASCADE ON DELETE CASCADE"))
  @ManyToOne
  @Setter
  @Getter
  private LocationName locationName;

  @Id
  @Column(name = "movedate", length = 20)
  @Getter
  @Setter
  private Date moveDate;

  public Move(Node node, LocationName locationName, Date moveDate) {
    this.node = node;
    this.locationName = locationName;
    this.moveDate = moveDate;
  }

  public Move() {}
}
