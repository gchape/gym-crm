package tech.provokedynamic.gymcrm.entity;
@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "user")
public class User {
@jakarta.persistence.Id
@jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.SEQUENCE)
@jakarta.persistence.Column(name = "id", nullable = false)
private java.lang.Long id;

public java.lang.Long getId() {
  return id;
}public void setId(java.lang.Long id) {
  this.id = id;
}

}