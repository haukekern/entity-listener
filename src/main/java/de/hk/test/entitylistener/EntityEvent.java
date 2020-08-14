package de.hk.test.entitylistener;

import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

import javax.validation.constraints.NotNull;

public class EntityEvent<T> implements ResolvableTypeProvider {

  public enum Type {PRE_CREATE, POST_CREATE, PRE_UPDATE, POST_UPDATE, PRE_DELETE, POST_DELETE}

  @NotNull
  private final T entity;
  @NotNull
  private final Class<T> entityClass;
  @NotNull
  private final Type type;

  public EntityEvent(@NotNull T entity, T entityOld, @NotNull Class<T> entityClass, @NotNull Type type) {
    this.entity = entity;
    this.entityClass = entityClass;
    this.type = type;
  }

  public T getEntity() {
    return entity;
  }

  public Class<T> getEntityClass() {
    return entityClass;
  }

  public Type getType() {
    return type;
  }

  public boolean isPreSave() {
    return isPreCreate() || isPreUpdate();
  }

  public boolean isPostSave() {
    return isPostCreate() || isPostUpdate();
  }

  public boolean isPreCreate() {
    return type == Type.PRE_CREATE;
  }

  public boolean isPostCreate() {
    return type == Type.POST_CREATE;
  }

  public boolean isPreUpdate() {
    return type == Type.PRE_UPDATE;
  }

  public boolean isPostUpdate() {
    return type == Type.POST_UPDATE;
  }

  public boolean isPreDelete() {
    return type == Type.PRE_DELETE;
  }

  public boolean isPostDelete() {
    return type == Type.POST_DELETE;
  }

  @Override
  public ResolvableType getResolvableType() {
    return ResolvableType.forClassWithGenerics(getClass(), entityClass);
  }

  @Override
  public String toString() {
    return "EntityEvent{" +
      "entity=" + entity +
      ", entityClass=" + entityClass +
      ", type=" + type +
      '}';
  }
}
