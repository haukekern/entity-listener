package de.hk.test.entitylistener;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.*;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;
import java.io.Serializable;

@Component
@Slf4j
public class HibernateEntityListenerHook implements
  PreInsertEventListener, PostInsertEventListener,
  PreUpdateEventListener, PostUpdateEventListener,
  PreDeleteEventListener, PostDeleteEventListener,
  PreCollectionUpdateEventListener, PostCollectionUpdateEventListener

{

  @Autowired
  private EntityManagerFactory entityManagerFactory;
  @Autowired
  private ApplicationEventPublisher eventPublisher;

  @PostConstruct
  private void init() {
    SessionFactoryImpl sessionFactory = entityManagerFactory.unwrap(SessionFactoryImpl.class);
    EventListenerRegistry registry = sessionFactory.getServiceRegistry().getService(EventListenerRegistry.class);

    registry.getEventListenerGroup(EventType.PRE_INSERT).appendListener(this);
    registry.getEventListenerGroup(EventType.POST_INSERT).appendListener(this);

    registry.getEventListenerGroup(EventType.PRE_UPDATE).appendListener(this);
    registry.getEventListenerGroup(EventType.POST_UPDATE).appendListener(this);
    registry.getEventListenerGroup(EventType.PRE_COLLECTION_UPDATE).appendListener(this);
    registry.getEventListenerGroup(EventType.POST_COLLECTION_UPDATE).appendListener(this);

    registry.getEventListenerGroup(EventType.PRE_DELETE).appendListener(this);
    registry.getEventListenerGroup(EventType.POST_DELETE).appendListener(this);


  }

  @Override
  public boolean onPreInsert(PreInsertEvent event) {
    publish(event.getEntity(), EntityEvent.Type.PRE_CREATE);
    return false;
  }

  @Override
  public void onPostInsert(PostInsertEvent event) {
    publish(event.getEntity(), EntityEvent.Type.POST_CREATE);
  }

  @Override
  public boolean onPreUpdate(PreUpdateEvent event) {
    publish(event.getEntity(), EntityEvent.Type.PRE_UPDATE, event.getPersister(), event.getId(), event.getOldState());
    return false;
  }

  @Override
  public void onPostUpdate(PostUpdateEvent event) {
    publish(event.getEntity(), EntityEvent.Type.POST_UPDATE, event.getPersister(), event.getId(), event.getOldState());
  }

  @Override
  public void onPreUpdateCollection(PreCollectionUpdateEvent event) {
    Object entity = event.getAffectedOwnerOrNull();
    if(entity != null) {
      publish(entity, EntityEvent.Type.PRE_UPDATE);
    }
  }

  @Override
  public void onPostUpdateCollection(PostCollectionUpdateEvent event) {
    Object entity = event.getAffectedOwnerOrNull();
    if(entity != null) {
      publish(entity, EntityEvent.Type.POST_UPDATE);
    }
  }

  @Override
  public boolean onPreDelete(PreDeleteEvent event) {
    publish(event.getEntity(), EntityEvent.Type.PRE_DELETE);
    return false;
  }

  @Override
  public void onPostDelete(PostDeleteEvent event) {
    publish(event.getEntity(), EntityEvent.Type.POST_DELETE);
  }

  @Override
  public boolean requiresPostCommitHanding(EntityPersister persister) {
    return true;
  }


  private void publish(Object entity, EntityEvent.Type type) {
    publish(entity, null, type);
  }

  private void publish(Object entity, EntityEvent.Type type, EntityPersister persister, Serializable id, Object[] oldState) {
    Object entityOld = null;
    if(persister != null && id != null && oldState != null) {
      try {
        entityOld = entity.getClass().newInstance();
        persister.setPropertyValues(entityOld, oldState);
        persister.setIdentifier(entityOld, id, null);
      } catch (Exception e) {
        log.error(null, e);
      }
    }
    publish(entity, entityOld, type);
  }

  private void publish(Object entity, Object entityOld, EntityEvent.Type type) {
    eventPublisher.publishEvent(new EntityEvent(
      entity,
      entityOld,
      entity.getClass(),
      type
    ));
  }


}
