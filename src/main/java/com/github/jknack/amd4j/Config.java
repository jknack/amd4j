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

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.ArrayLiteral;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.ObjectLiteral;
import org.mozilla.javascript.ast.ObjectProperty;
import org.mozilla.javascript.ast.StringLiteral;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Config {

  private static class JsonNormalizer implements NodeVisitor {
    private AstNode node;

    public static String toJson(final String javaScript, final String path) {
      AstRoot tree = new Parser().parse(javaScript, path, 1);
      JsonNormalizer normalizer = new JsonNormalizer();
      tree.visit(normalizer);
      return normalizer.node.toSource();
    }

    @Override
    public boolean visit(final AstNode node) {
      if (node.getType() == Token.OBJECTLIT) {
        if (this.node == null) {
          this.node = node;
        }
      }
      if (this.node != null) {
        if (node instanceof ObjectProperty) {
          visit((ObjectProperty) node);
        } else if (node instanceof StringLiteral) {
          visit((StringLiteral) node);
        }
      }
      return true;
    }

    protected void visit(final StringLiteral node) {
      if (node.getQuoteCharacter() == '\'') {
        node.setQuoteCharacter('"');
      }
    }

    protected void visit(final ObjectProperty node) {
      AstNode name = node.getLeft();
      // name
      StringLiteral propertyName = new StringLiteral();
      propertyName.setQuoteCharacter('"');
      if (name instanceof Name) {
        propertyName.setValue(((Name) name).getIdentifier());
      } else {
        propertyName.setValue(((StringLiteral) name).getValue());
      }
      node.setLeft(propertyName);
      // value
      AstNode value = node.getRight();
      AstNode newValue = value;
      if (value instanceof FunctionNode) {
        StringLiteral literal = new StringLiteral();
        literal.setQuoteCharacter('"');
        literal.setValue(value.toSource());
        newValue = literal;
      } else if (value instanceof StringLiteral) {
        StringLiteral literal = new StringLiteral();
        literal.setQuoteCharacter('"');
        literal.setValue(((StringLiteral) value).getValue());
        newValue = literal;
      } else if (value instanceof ArrayLiteral && node.depth() == 6) {
        // handle sortcut syntax where plugins don't exports anything
        ArrayLiteral array = (ArrayLiteral) value;
        ObjectLiteral object = new ObjectLiteral();
        ;
        // convert array dependencies to a shim object literal.
        ObjectProperty deps = new ObjectProperty();
        StringLiteral depsProperty = new StringLiteral();
        depsProperty.setQuoteCharacter('"');
        depsProperty.setValue("deps");
        deps.setLeft(depsProperty);
        deps.setRight(array);
        object.addElement(deps);
        newValue = object;
      }
      if (value != newValue) {
        node.setRight(newValue);
      }
    }

  }

  private static final ObjectMapper mapper = new ObjectMapper();

  static {
    mapper.setVisibilityChecker(
        mapper.getVisibilityChecker()
            .withFieldVisibility(Visibility.ANY)
            .withGetterVisibility(Visibility.NONE)
            .withSetterVisibility(Visibility.NONE)
        );
    mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    mapper.setSerializationInclusion(Include.NON_NULL);
  }

  private String baseUrl;

  private Map<String, String> paths;

  private Map<String, Shim> shim;

  private boolean useStrict = false;

  private boolean inlineText = true;

  private boolean findNestedDependencies = false;

  private String name;

  private String out;

  {
    shim = new HashMap<String, Shim>();
    paths = new HashMap<String, String>();
    paths.put("module", Optimizer.EMPTY);
    paths.put("require", Optimizer.EMPTY);
  }

  public Config(final String baseUrl, final String name, final String out,
      final Map<String, String> paths) {
    setBaseUrl(baseUrl);
    this.name = name;
    this.out = out;
    this.paths.putAll(paths);
  }

  public Config(final String baseUrl, final String name, final String out) {
    this(baseUrl, name, out, new LinkedHashMap<String, String>());
  }

  protected Config() {
  }

  public String resolvePath(final String path) {
    String realPath = paths.get(path);
    if (isEmpty(realPath)) {
      realPath = path;
    }
    return realPath;
  }

  public Shim getShim(final String name) {
    return shim.get(name);
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public String getName() {
    return name;
  }

  public String getOut() {
    return out;
  }

  public Map<String, String> getPaths() {
    return paths;
  }

  public boolean isFindNestedDependencies() {
    return findNestedDependencies;
  }

  public boolean isInlineText() {
    return inlineText;
  }

  public boolean isUseStrict() {
    return useStrict;
  }

  public Config setInlineText(final boolean inlineText) {
    this.inlineText = inlineText;
    return this;
  }

  public Config setFindNestedDependencies(final boolean findNestedDependencies) {
    this.findNestedDependencies = findNestedDependencies;
    return this;
  }

  public Config setShim(final Map<String, Shim> shim) {
    this.shim = shim;
    return this;
  }

  public Config setUseStrict(final boolean useStrict) {
    this.useStrict = useStrict;
    return this;
  }

  @Override
  public String toString() {
    try {
      return mapper.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

  }

  public static Config parse(final File input) throws IOException {
    return parse(new FileReader(input), input.getAbsolutePath());
  }

  public static Config parse(final String input) throws IOException {
    return parse(new StringReader(input));
  }

  public static Config parse(final Reader reader) throws IOException {
    return parse(reader, "config.js");
  }

  private static Config parse(final Reader reader, final String path) throws IOException {
    try {
      notNull(reader, "The input is required.");
      notEmpty(path, "The path is required.");
      String javaScript = IOUtils.toString(reader).trim();
      if (javaScript.startsWith("{") && javaScript.endsWith("}")) {
        javaScript = "(" + javaScript + ")";
      }
      String json = JsonNormalizer.toJson(javaScript, path);
      Config config = mapper.readValue(json, Config.class);
      return config;
    } finally {
      IOUtils.closeQuietly(reader);
    }
  }

  public Map<String, Shim> getShim() {
    return shim;
  }

  public Config setName(final String name) {
    this.name = name;
    return this;
  }

  public Config setOut(final String out) {
    this.out = out;
    return this;
  }

  public Config setBaseUrl(final String baseUrl) {
    this.baseUrl = baseUrl;
    return this;
  }

  public Config newPath(final String name, final String path) {
    paths.put(name, path);
    return this;
  }
}
