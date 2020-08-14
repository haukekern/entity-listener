package de.hk.test.entitylistener.domain;

import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Data
@Embeddable
public class Inventory {
  @OneToMany(cascade = CascadeType.ALL)
  private List<Item> items = new ArrayList<>();
}
