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

import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.Validate.notNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class ResourceResolver {

  static class ResolvedResource {

    public final ResourceURI uri;
    public final String content;

    public ResolvedResource(final ResourceURI candidate, final String content) {
      this.uri = candidate;
      this.content = content;
    }
  }

  private ResourceLoader delegate;

  public ResourceResolver(final ResourceLoader delegate) {
    this.delegate = notNull(delegate, "The delegate is required.");
  }

  public ResolvedResource resolve(final ResourceURI uri) throws IOException {
    String path = uri.path;
    List<ResourceURI> candidates = new ArrayList<ResourceURI>();
    if (isEmpty(getExtension(path))) {
      candidates.add(new ResourceURI(uri.baseUrl, uri.schema, path + ".js"));
    }
    candidates.add(uri);
    candidates.add(new ResourceURI(uri.baseUrl, uri.schema, path + ".js"));
    for (ResourceURI candidate : candidates) {
      try {
        String content = delegate.load(candidate);
        return new ResolvedResource(candidate, content);
      } catch (FileNotFoundException ex) {
        // try next or fail at the end.
      }
    }
    throw new FileNotFoundException(uri.getFullPath());
  }

}
