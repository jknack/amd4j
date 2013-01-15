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
import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
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

/**
 * Optimizer configuration options.
 *
 * @author edgar.espina
 * @since 0.1.0
 */
public class Config {

  /**
   * Transform JavaScript object literals to a fully JSON object.
   * Function statements are converted to a string literal.
   *
   * @author edgar.espina
   * @since 0.1.0
   */
  private static class JsonNormalizer implements NodeVisitor {

    /**
     * The AST root.
     */
    private AstNode node;

    /**
     * Convert a JavaScript object literal to a JSON object.
     *
     * @param jsObjectLiteral The object literal.
     * @param path The source path.
     * @return A Json object.
     */
    public static String toJson(final String jsObjectLiteral, final String path) {
      AstRoot tree = new Parser().parse(jsObjectLiteral, path, 1);
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

    /**
     * Make sure string literal has double quotes.
     *
     * @param node The string literal node.
     */
    protected void visit(final StringLiteral node) {
      if (node.getQuoteCharacter() == '\'') {
        node.setQuoteCharacter('"');
      }
    }

    /**
     * Modify an object literal property. This method make sure that:
     * 1. property names are double quote string literal
     * 2. function values are converted to a double quote string literal
     * 3. transform short array literal dependencies into long object literal dependencies.
     *
     * @param node Object literal property.
     */
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
        // handle shortcut syntax where plugins don't exports anything
        ArrayLiteral array = (ArrayLiteral) value;
        ObjectLiteral object = new ObjectLiteral();

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

  /**
   * The JSON parser.
   */
  private static final ObjectMapper mapper = new ObjectMapper();

  /**
   * Mark a module as "provided", so it wont be included in the final output.
   */
  public static final String EMPTY = "empty:";

  /**
   * Configure and initialize the JSON parser.
   */
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

  /**
   * By default, all modules are located relative to this path. If baseUrl
   * is not explicitly set, then all modules are loaded relative to
   * the directory that holds the build file. If appDir is set, then
   * baseUrl should be specified as relative to the appDir.
   */
  private String baseUrl;

  /**
   * Set paths for modules. If relative paths, set relative to baseUrl above.
   * If a special value of "empty:" is used for the path value, then that
   * acts like mapping the path to an empty file. It allows the optimizer to
   * resolve the dependency to path, but then does not include it in the output.
   * Useful to map module names that are to resources on a CDN or other
   * http: URL when running in the browser and during an optimization that
   * file should be skipped because it has no dependencies.
   */
  private Map<String, String> paths;

  /**
   * Configure the dependencies and exports for older, traditional "browser globals" scripts that do
   * not use <code>define</code> to declare the dependencies and set a module value.
   */
  private Map<String, Shim> shim;

  /**
   * Allow "use strict"; be included in the JavaScript files.
   * Default is false because there are not many browsers that can properly
   * process and give errors on code for ES5 strict mode,
   * and there is a lot of legacy code that will not work in strict mode.
   */
  private boolean useStrict = false;

  /**
   * Inlines the text for any text! dependencies, to avoid the separate
   * async XMLHttpRequest calls to load those dependencies.
   */
  private boolean inlineText = true;

  /**
   * Finds <code>require()</code> dependencies inside a <code>require()</code> or
   * <code>define</code> call. By default this value is false, because those resources should be
   * considered dynamic/runtime calls.
   */
  private boolean findNestedDependencies = false;

  /**
   * The module to be optimized.
   */
  private String name;

  /**
   * The output of the optimized file.
   */
  private File out;

  /**
   * Initialize default values.
   */
  {
    shim = new LinkedHashMap<String, Shim>();
    paths = new LinkedHashMap<String, String>();
    paths.put("module", EMPTY);
    paths.put("require", EMPTY);
  }

  /**
   * Creates a new {@link Config} object.
   *
   * @param baseUrl The base url. Required.
   * @param name The module's name. Required.
   * @param out The output file. Required.
   */
  public Config(final String baseUrl, final String name, final File out) {
    setBaseUrl(baseUrl);
    setName(name);
    setOut(out);
  }

  /**
   * Creates a new {@link Config} object.
   *
   * @param name The module's name. Required.
   * @param out The output file. Required.
   */
  public Config(final String name, final File out) {
    setBaseUrl("/");
    setName(name);
    setOut(out);
  }

  /**
   * Creates a new {@link Config} object.
   *
   * @param baseUrl The base url. Required.
   * @param name The module's name. Required.
   */
  public Config(final String baseUrl, final String name) {
    setBaseUrl(baseUrl);
    setName(name);
  }

  /**
   * Creates a new {@link Config} object.
   *
   * @param name The module's name. Required.
   */
  public Config(final String name) {
    setBaseUrl("/");
    setName(name);
  }

  /**
   * Creates a new {@link Config} object.
   */
  public Config() {
  }

  /**
   * Parse a build.js file and creates a new {@link Config} object.
   *
   * @param build The build.js file. Required.
   * @return A new {@link Config} object.
   * @throws IOException If something goes wrong.
   */
  public static Config parse(final File build) throws IOException {
    notNull(build, "The build file is required.");
    Config config = parse(new FileReader(build), build.getAbsolutePath());
    if (isEmpty(config.baseUrl)) {
      // if baseUrl isn't set, use the location of the build.js file
      config.baseUrl = build.getParentFile().getAbsolutePath();
    }
    return config;
  }

  /**
   * Parse a build.js string and creates a new {@link Config} object.
   *
   * @param input The build.js string. Required.
   * @return A new {@link Config} object.
   * @throws IOException If something goes wrong.
   */
  public static Config parse(final String input) throws IOException {
    return parse(new StringReader(input));
  }

  /**
   * Parse a build.js file and creates a new {@link Config} object.
   *
   * @param reader The build.js reader. Required.
   * @return A new {@link Config} object.
   * @throws IOException If something goes wrong.
   */
  public static Config parse(final Reader reader) throws IOException {
    return parse(reader, "inline.js");
  }

  /**
   * Parse a build.js file and creates a new {@link Config} object.
   *
   * @param reader The build.js file. Required.
   * @param path The build path. Required.
   * @return A new {@link Config} object.
   * @throws IOException If something goes wrong.
   */
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

  /**
   * Resolve an alias to an absolute path.
   *
   * @param path The path's alias. Required.
   * @return An absolute path if any or the given path alias.
   */
  public String resolvePath(final String path) {
    notEmpty(path, "The path is required.");

    String realPath = paths.get(path);
    if (isEmpty(realPath)) {
      realPath = path;
    }
    return realPath;
  }

  /**
   * Configure the dependencies and exports for older, traditional "browser globals" scripts that do
   * not use <code>define</code> to declare the dependencies and set a module value.
   *
   * @param name A dependency's name. Required.
   * @return A shim option for the given dependency's name or <code>null</code>.
   */
  public Shim getShim(final String name) {
    return shim.get(notEmpty(name, "The dependency's name is required."));
  }

  /**
   * By default, all modules are located relative to this path. If baseUrl
   * is not explicitly set, then all modules are loaded relative to
   * the directory that holds the build file. If appDir is set, then
   * baseUrl should be specified as relative to the appDir.
   *
   * @return The base url.
   */
  public String getBaseUrl() {
    return baseUrl;
  }

  /**
   * The module to be optimized.
   *
   * @return The module to be optimized.
   */
  public String getName() {
    return name;
  }

  /**
   * The output of the optimized file.
   *
   * @return The output of the optimized file.
   */
  public File getOut() {
    return out;
  }

  /**
   * Finds <code>require()</code> dependencies inside a <code>require()</code> or
   * <code>define</code> call. By default this value is false, because those resources should be
   * considered dynamic/runtime calls.
   *
   * @return True, to processed nested require calls.
   */
  public boolean isFindNestedDependencies() {
    return findNestedDependencies;
  }

  /**
   * Inlines the text for any text! dependencies, to avoid the separate
   * async XMLHttpRequest calls to load those dependencies. Default is: true.
   *
   * @return True, to embedd text as AMD modules.
   */
  public boolean isInlineText() {
    return inlineText;
  }

  /**
   * Allow "use strict"; be included in the JavaScript files.
   * Default is false because there are not many browsers that can properly
   * process and give errors on code for ES5 strict mode,
   * and there is a lot of legacy code that will not work in strict mode.
   *
   * @return True, if "use strict"; is allowed.
   */
  public boolean isUseStrict() {
    return useStrict;
  }

  /**
   * Inlines the text for any text! dependencies, to avoid the separate
   * async XMLHttpRequest calls to load those dependencies.
   *
   * @param inlineText True, to embedd text as AMD modules.
   * @return This configuration object.
   */
  public Config setInlineText(final boolean inlineText) {
    this.inlineText = inlineText;
    return this;
  }

  /**
   * Finds <code>require()</code> dependencies inside a <code>require()</code> or
   * <code>define</code> call. By default this value is false, because those resources should be
   * considered dynamic/runtime calls.
   *
   * @param findNestedDependencies True, to find nested <code>require</code> calls.
   * @return This configuration object.
   */
  public Config setFindNestedDependencies(final boolean findNestedDependencies) {
    this.findNestedDependencies = findNestedDependencies;
    return this;
  }

  /**
   * Configure the dependencies and exports for older, traditional "browser globals" scripts that do
   * not use <code>define</code> to declare the dependencies and set a module value.
   *
   * @param name The dependency's name. Required.
   * @param shim The shim config. Required.
   * @return This configuration object.
   */
  public Config shim(final String name, final Shim shim) {
    notEmpty(name, "The name is required.");
    notNull(shim, "The shim is required.");

    this.shim.put(name, shim);
    return this;
  }

  /**
   * Allow "use strict"; be included in the JavaScript files.
   * Default is false because there are not many browsers that can properly
   * process and give errors on code for ES5 strict mode,
   * and there is a lot of legacy code that will not work in strict mode.
   *
   * @param useStrict True, if "use strict"; is allowed.
   * @return This configuration object.
   */
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

  /**
   * The module to be optimized.
   *
   * @param name The module to be optimized.
   * @return The module to be optimized.
   */
  public Config setName(final String name) {
    this.name = notEmpty(name, "The module's name is required.");
    return this;
  }

  /**
   * The output of the optimized file.
   *
   * @param out The output of the optimized file.
   * @return This configuration object.
   */
  public Config setOut(final File out) {
    notNull(out, "The out is required.");
    isTrue(out.getParentFile().exists(), "Directory not found: %s", out.getParentFile());
    this.out = out;
    return this;
  }

  /**
   * By default, all modules are located relative to this path. If baseUrl
   * is not explicitly set, then all modules are loaded relative to
   * the directory that holds the build file. If appDir is set, then
   * baseUrl should be specified as relative to the appDir.
   *
   * @param baseUrl The base url. Required.
   * @return This configuration object.
   */
  public Config setBaseUrl(final String baseUrl) {
    this.baseUrl = notEmpty(baseUrl, "The baseUrl is required.");
    return this;
  }

  /**
   * Set paths for modules. If relative paths, set relative to baseUrl above.
   * If a special value of "empty:" is used for the path value, then that
   * acts like mapping the path to an empty file. It allows the optimizer to
   * resolve the dependency to path, but then does not include it in the output.
   * Useful to map module names that are to resources on a CDN or other
   * http: URL when running in the browser and during an optimization that
   * file should be skipped because it has no dependencies.
   *
   * @param name The dependency's name.
   * @param path The dependency's path.
   * @return This configuration object.
   */
  public Config path(final String name, final String path) {
    paths.put(name, path);
    return this;
  }
}
