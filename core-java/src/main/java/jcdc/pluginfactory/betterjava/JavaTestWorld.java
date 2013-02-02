package jcdc.pluginfactory.betterjava;

import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.Collection;
import java.util.LinkedList;

public abstract class JavaTestWorld implements World {
  @Override
  public <T extends Entity> Collection<T> getEntitiesByClass(Class<T>... classes) {
    return new LinkedList<T>();
  }

  @Override
  public Collection<Entity> getEntitiesByClasses(Class<?>... classes) {
    return new LinkedList<Entity>();
  }
}
