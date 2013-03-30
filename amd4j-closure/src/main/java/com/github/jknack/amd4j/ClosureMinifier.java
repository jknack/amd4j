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

import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Level;

import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.ClosureCodingConvention;
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
   * The compilation level.
   */
  private CompilationLevel compilationLevel;

  /**
   * Creates a new {@link ClosureMinifier}.
   *
   * @param compilationLevel The compilation level. Required.
   */
  public ClosureMinifier(final CompilationLevel compilationLevel) {
    this.compilationLevel = notNull(compilationLevel, "The compilationLevel is required.");
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
    Result result = compiler.compile(Collections.<SourceFile> emptyList(),
        Arrays.asList(SourceFile.fromCode(fname, source.toString())), options);
    if (result.success) {
      return compiler.toSource();
    }
    JSError[] errors = result.errors;
    throw new IllegalStateException(errors[0].toString());
  }

}
