package com.github.jknack.amd4j;

import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

import java.io.File;
import java.net.URI;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Resource location.
 *
 * @author edgar.espina
 * @since 0.3.0
 */
public final class ResourceURI {

  /**
   * The resource path. Required.
   */
  private String path;

  /**
   * The resource prefix. Optional.
   */
  private String prefix;

  /**
   * Creates a new {@link ResourceURI}.
   *
   * @param prefix The resource prefix. Optional.
   * @param path The resource path. Required.
   */
  private ResourceURI(final String prefix, final String path) {
    this.prefix = prefix;
    this.path = notNull(path, "The path is required.");
  }

  /**
   * The resource path.
   *
   * @return The resource path.
   */
  public String getPath() {
    return path;
  }

  /**
   * The resource prefix.
   *
   * @return The resource prefix.
   */
  public String getPrefix() {
    return prefix;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof ResourceURI) {
      ResourceURI that = (ResourceURI) obj;
      return new EqualsBuilder().append(prefix, that.prefix).append(path, that.path).isEquals();
    }
    return false;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(prefix).append(path).hashCode();
  }

  @Override
  public String toString() {
    return prefix == null ? path : prefix + '!' + path;
  }

  /**
   * Creates a {@link ResourceURI}.
   *
   * @param baseUrl The base url.
   * @param path The dependency's path. It might be prefixed with: <code>prefix!</code> where
   *        <code>prefix</code> is usually a plugin.
   * @return A new {@link ResourceURI}.
   */
  public static ResourceURI create(final String baseUrl, final String path) {
    notEmpty(baseUrl, "The baseUrl is required.");
    String normBaseUrl = baseUrl;
    if (".".equals(normBaseUrl)) {
      normBaseUrl = File.separator;
    }
    if (!normBaseUrl.startsWith(File.separator)) {
      normBaseUrl = File.separator + normBaseUrl;
    }
    if (!normBaseUrl.endsWith(File.separator)) {
      normBaseUrl += File.separator;
    }
    int idx = Math.max(0, path.indexOf('!') + 1);
    StringBuilder uri = new StringBuilder(path);
    if (uri.charAt(idx) == File.separatorChar) {
      uri.deleteCharAt(idx);
    }
    uri.insert(idx, normBaseUrl);
    return create(uri.toString());
  }

  /**
   * Creates a {@link URI}.
   *
   * @param path The dependency's path. It might be preffixed with: <code>schema!</code> where
   *        <code>schema</code> is usually a plugin.
   * @return A new {@link ResourceURI}.
   */
  public static ResourceURI create(final String path) {
    notEmpty(path, "The path is required.");
    int idx = path.indexOf('!');
    return new ResourceURI(idx > 0 ? path.substring(0, idx) : null,
        idx > 0 ? path.substring(idx + 1) : path);
  }
}
