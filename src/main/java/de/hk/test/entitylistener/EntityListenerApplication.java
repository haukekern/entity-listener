package de.hk.test.entitylistener;

import de.hk.test.entitylistener.domain.Car;
import de.hk.test.entitylistener.domain.CarRepo;
import de.hk.test.entitylistener.domain.Inventory;
import de.hk.test.entitylistener.domain.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.EventListener;

import java.util.List;

@Slf4j
@SpringBootApplication
public class EntityListenerApplication implements CommandLineRunner {

  public static void main(String[] args) {
    SpringApplication.run(EntityListenerApplication.class, args);
  }

  @Autowired
  private CarRepo carRepo;
  @Autowired
  private GenericEntityService entityService;

  @Override
  public void run(String... args) throws Exception {
    log.info("start");
    Inventory inventory = new Inventory();
    Car car = new Car(
      "Auto",
      new Inventory()
    );

    List<Item> items = car.getInventory().getItems();

    items.add(new Item("Item 1"));
    items.add(new Item("Item 2"));
    items.add(new Item("Item 3"));

    log.info("insert");
    car = carRepo.save(car);

    Long id = car.getId();

    log.info("update 1");
    car.setTitle("Auto updated");
    car = carRepo.save(car);

    log.info("update 2");
    car.getInventory().getItems().get(0).setTitle("Item updated");
    car = carRepo.save(car);

    try {
      log.info("update 3");
      car.getInventory().getItems().clear();
      car = carRepo.save(car);
    } catch (Exception e) {
      log.error(e.getLocalizedMessage(), e);
    }

    log.info("reread und check");

    car = entityService.read(entityManager -> {
      Car carInner = entityManager.find(Car.class, id);
      carInner.getInventory().getItems().size();
      return carInner;
    });

    log.info("car items: " + car.getInventory().getItems());

    log.info("delete");
    carRepo.delete(car);

    log.info("count: {}", carRepo.count());

    log.info("ende");
  }

  @EventListener
  public void handleCar(EntityEvent<Car> event) {
    log.info("handleCar: {}", event);
    if (event.isPostSave()) {
      if (event.getEntity().getInventory().getItems().isEmpty()) {
        throw new IllegalStateException("Bla");
      }
    }
  }


  @EventListener
  public void handleItem(EntityEvent<Item> event) {
    log.info("handleItem: {}", event);
  }
}
