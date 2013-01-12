/**
 * Copyright (c) 2013 Edgar Espina
 *
 * This file is part of amd4j (https://github.com/jknack/amd4j)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jknack.amd4j;

import static org.apache.commons.lang3.StringUtils.join;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Shim {

  @JsonProperty
  private Set<String> deps;

  @JsonProperty
  private String exports;

  @JsonProperty
  private String init;

  public Set<String> deps() {
    return deps;
  }

  public String exports() {
    return exports;
  }

  public String init() {
    return init;
  }

  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder();
    buffer.append("{\n");
    List<String> properties = new ArrayList<String>();
    properties.add("  \"deps\": " + depsToString());
    if (exports != null) {
      properties.add("  \"exports\": \"" + exports + "\"");
    }
    if (init != null) {
      properties.add("  \"init\": \"" + init + "\"");
    }
    buffer.append(join(properties, ",\n"));
    buffer.append("\n}");
    return buffer.toString();
  }

  private String depsToString() {
    return deps == null ? "[]" : "[\"" + join(deps, "\", \"") + "\"]";
  }

  public String shim(final Module module) {
    StringBuilder buffer = new StringBuilder();
    if (init == null) {
      if (exports == null) {
        buffer.append("\ndefine(\"").append(module.name).append("\", function(){});\n");
      } else {
        buffer.append("\ndefine(\"").append(module.name).append("\", ").append(depsToString())
            .append(", (function (global) {\n");
        buffer.append("    return function () {\n");
        buffer.append("        var ret, fn;\n");
        buffer.append("        return ret || global.").append(exports).append(";\n");
        buffer.append("    };\n");
        buffer.append("}(this)));\n");
      }
    } else {
      buffer.append("\ndefine(\"").append(module.name).append("\", ").append(depsToString())
          .append(", (function (global) {\n");
      buffer.append("    return function () {\n");
      buffer.append("        var ret, fn;\n");
      buffer.append("fn = ").append(init).append(";\n");
      buffer.append("        ret = fn.apply(global, arguments);\n");
      buffer.append("        return ret || global.").append(exports).append(";\n");
      buffer.append("    };\n");
      buffer.append("}(this)));\n");
    }
    // add dependencies
    if (deps != null) {
      module.dependencies.addAll(deps);
    }
    return buffer.toString();
  }
}
