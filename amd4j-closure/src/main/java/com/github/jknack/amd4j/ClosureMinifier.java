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

import static org.apache.commons.io.FilenameUtils.removeExtension;
import static org.apache.commons.lang3.Validate.notNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.ClosureCodingConvention;
import com.google.javascript.jscomp.CommandLineRunner;
import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.DiagnosticGroups;
import com.google.javascript.jscomp.JSError;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.SourceFile;

/**
 * An optimizer built on top of Google Closure Compiler.
 *
 * @author edgar.espina
 * @since 0.2.0
 */
public class ClosureMinifier extends Minifier {

  /**
   * The logging system.
   */
  private final Logger logger = LoggerFactory.getLogger(getClass());

  /**
   * The compilation level.
   */
  private CompilationLevel compilationLevel;

  /**
   * The externs expected in externs.zip, in sorted order.
   * Taken from: com.google.javascript.jscomp.CommandLineRunner
   */
  private static final List<String> DEFAULT_EXTERNS_NAMES = ImmutableList.of(
      // JS externs
      "es3.js",
      "es5.js",

      // Event APIs
      "w3c_event.js",
      "w3c_event3.js",
      "gecko_event.js",
      "ie_event.js",
      "webkit_event.js",
      "w3c_device_sensor_event.js",

      // DOM apis
      "w3c_dom1.js",
      "w3c_dom2.js",
      "w3c_dom3.js",
      "gecko_dom.js",
      "ie_dom.js",
      "webkit_dom.js",

      // CSS apis
      "w3c_css.js",
      "gecko_css.js",
      "ie_css.js",
      "webkit_css.js",

      // Top-level namespaces
      "google.js",

      "chrome.js",

      "deprecated.js",
      "fileapi.js",
      "flash.js",
      "gears_symbols.js",
      "gears_types.js",
      "gecko_xml.js",
      "html5.js",
      "ie_vml.js",
      "iphone.js",
      "webstorage.js",
      "w3c_anim_timing.js",
      "w3c_css3d.js",
      "w3c_elementtraversal.js",
      "w3c_geolocation.js",
      "w3c_indexeddb.js",
      "w3c_navigation_timing.js",
      "w3c_range.js",
      "w3c_selectors.js",
      "w3c_xml.js",
      "window.js",
      "webkit_notifications.js",
      "webgl.js");

  /**
   * A cached copy of default externs source files.
   */
  List<SourceFile> defaultExterns = null;

  /**
   * Creates a new {@link ClosureMinifier}.
   *
   * @param compilationLevel The compilation level. Required.
   */
  public ClosureMinifier(final CompilationLevel compilationLevel) {
    this.compilationLevel = notNull(compilationLevel, "The compilationLevel is required.");
    try {
      this.defaultExterns = getDefaultExterns();
    } catch (IOException e) {
      logger.warn("Could not load externs.zip. No closure externs will be used.", e);
      this.defaultExterns = Collections.<SourceFile> emptyList();
    }
  }

  @Override
  public CharSequence minify(final Config config, final CharSequence source) {
    final CompilerOptions options = new CompilerOptions();
    options.setCodingConvention(new ClosureCodingConvention());
    options.setOutputCharset("UTF-8");
    options.setWarningLevel(DiagnosticGroups.CHECK_VARIABLES, CheckLevel.WARNING);
    compilationLevel.setOptionsForCompilationLevel(options);

    Compiler.setLoggingLevel(Level.SEVERE);
    Compiler compiler = new Compiler();
    compiler.disableThreads();
    compiler.initOptions(options);

    String fname = removeExtension(config.getName()) + ".js";
    Result result = compiler.compile(defaultExterns,
        Arrays.asList(SourceFile.fromCode(fname, source.toString())), options);
    if (result.success) {
      return compiler.toSource();
    }
    JSError[] errors = result.errors;
    throw new IllegalStateException(errors[0].toString());
  }

  /**
   * Build the default list of google closure external variable files.
   * Taken from: com.google.javascript.jscomp.CommandLineRunner
   *
   * @return a mutable list of source files.
   * @throws IOException On error when working with externs.zip
   */
  protected List<SourceFile> getDefaultExterns() throws IOException {
    ZipInputStream zip = null;
    try {
      InputStream input = CommandLineRunner.class.getResourceAsStream("/externs.zip");
      notNull(input, "The externs.zip file was not found within the closure classpath");

      zip = new ZipInputStream(input);
      Map<String, SourceFile> externsMap = Maps.newHashMap();
      ZipEntry entry = zip.getNextEntry();
      while (entry != null) {
        BufferedInputStream entryStream = new BufferedInputStream(
            ByteStreams.limit(zip, entry.getSize()));
        externsMap.put(entry.getName(),
            SourceFile.fromInputStream(
                // Give the files an odd prefix, so that they do not conflict
                // with the user's files.
                "externs.zip//" + entry.getName(),
                entryStream));
        entry = zip.getNextEntry();
      }

      Preconditions.checkState(
          externsMap.keySet().equals(Sets.newHashSet(DEFAULT_EXTERNS_NAMES)),
          "Externs zip must match our hard-coded list of externs.");

      // Order matters, so the resources must be added to the result list
      // in the expected order.
      List<SourceFile> externs = Lists.newArrayList();
      for (String key : DEFAULT_EXTERNS_NAMES) {
        externs.add(externsMap.get(key));
      }

      return externs;
    } finally {
      IOUtils.closeQuietly(zip);
    }
  }
}
