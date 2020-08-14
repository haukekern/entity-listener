package de.hk.test.entitylistener;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.util.CollectionUtils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.*;

@Slf4j
public class PersistenceUtils {
  private static void unproxyAndInitialize(Object value, List<Object> handledObjects) {
    if (value != null && !isProxy(value) && !containsTotallyEqual(handledObjects, value)) {
      handledObjects.add(value);
      if (value instanceof Iterable) {
        for (Object item : (Iterable<?>) value) {
          unproxyAndInitialize(item, handledObjects);
        }
      } else if (value.getClass().isArray()) {
        for (Object item : (Object[]) value) {
          unproxyAndInitialize(item, handledObjects);
        }
      }
      BeanInfo beanInfo = null;
      try {
        beanInfo = Introspector.getBeanInfo(value.getClass());
      } catch (IntrospectionException e) {
        log.warn(e.getMessage(), e);
      }
      if (beanInfo != null) {
        for (PropertyDescriptor property : beanInfo.getPropertyDescriptors()) {
          try {
            if (property.getWriteMethod() != null && property.getReadMethod() != null) {
              Object fieldValue = property.getReadMethod().invoke(value);
              if (isProxy(fieldValue)) {
                fieldValue = unproxyObject(fieldValue);
                property.getWriteMethod().invoke(value, fieldValue);
              }
              unproxyAndInitialize(fieldValue, handledObjects);
            }
          } catch (Exception e) {
            log.warn(e.getMessage(), e);
          }
        }
      }
    }
  }

  // TODO Listen, Sets, Maps etc. werden nicht entpackt
  public static <T> T unproxyAndInitialize(T value) {
    T result = unproxyObject(value);
    unproxyAndInitialize(result, new ArrayList<Object>());
    return result;
  }

  private static boolean containsTotallyEqual(Collection<?> collection, Object value) {
    if (CollectionUtils.isEmpty(collection)) {
      return false;
    }
    for (Object object : collection) {
      if (object == value) {
        return true;
      }
    }
    return false;
  }

  public static boolean isProxy(Object value) {
    if (value == null) {
      return false;
    }
    if ((value instanceof HibernateProxy)) {
      return true;
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  private static <T> T unproxyObject(T object) {
    return (T) Hibernate.unproxy(object);
  }

}
