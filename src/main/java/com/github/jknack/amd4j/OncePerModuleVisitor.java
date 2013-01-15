package com.github.jknack.amd4j;

import java.util.HashSet;
import java.util.Set;

public abstract class OncePerModuleVisitor<Out> extends ModuleVisitor<Out> {

  private Set<Module> visited = new HashSet<Module>();

  @Override
  public final boolean visit(final Module module) {
    boolean visit = visited.add(module);
    if (visit) {
      return doVisit(module);
    }
    return visit;
  }

  protected boolean doVisit(final Module module) {
    return true;
  }

}
