package de.hk.test.entitylistener;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.function.Consumer;
import java.util.function.Function;


@Service
public class GenericEntityService {

  @PersistenceContext
  private EntityManager entityManager;

  @Transactional(readOnly = true)
  public <T> T read(Function<EntityManager, T> handler) {
    return handler.apply(entityManager);
  }

  @Transactional(readOnly = true)
  public void read(Consumer<EntityManager> handler) {
    handler.accept(entityManager);
  }

  @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
  public <T> T readInNewTransaction(Function<EntityManager, T> handler) {
    return handler.apply(entityManager);
  }

  @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
  public void readInNewTransaction(Consumer<EntityManager> handler) {
    handler.accept(entityManager);
  }

  @Transactional
  public <T> T execute(Function<EntityManager, T> handler) {
    return handler.apply(entityManager);
  }

  @Transactional
  public void execute(Consumer<EntityManager> handler) {
    handler.accept(entityManager);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public <T> T executeInNewTransaction(Function<EntityManager, T> handler) {
    return handler.apply(entityManager);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void executeInNewTransaction(Consumer<EntityManager> handler) {
    handler.accept(entityManager);
  }
}
