package com.github.jknack.amd4j;


public abstract class ModuleVisitor<Out> {

  public abstract Out walk(Module module);

  public boolean visit(final Module module) {
    return true;
  }

  public void endvisit(final Module module) {
  }
}
