package com.joshcough.minecraft.betterjava;

import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.Collection;
import java.util.LinkedList;

public abstract class JavaTestWorld implements World {
  @Override
  public <T extends Entity> Collection<T> getEntitiesByClass(Class<T>... classes) {
    return new LinkedList<>();
  }

  @Override
  public Collection<Entity> getEntitiesByClasses(Class<?>... classes) {
    return new LinkedList<>();
  }
}
