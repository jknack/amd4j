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

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A Json parser adapted from {@link org.mozilla.javascript.json.JsonParser}.
 *
 * @author edgar.espina
 * @since 0.2.0
 */
public final class JsonParser {

  /**
   * The current parser position.
   */
  private int pos;

  /**
   * The length of the input.
   */
  private int length;

  /**
   * The JSON input.
   */
  private StringBuilder src;

  /**
   * The file's name.
   */
  private String filename;

  /**
   * Not allowed.
   */
  private JsonParser() {
  }

  /**
   * Parse a JSON input.
   *
   * @param json The json input.
   * @param filename A file name.
   * @return A JSON object.
   * @throws IOException If the parsing process fail.
   */
  public static Object parse(final String json, final String filename) throws IOException {
    return new JsonParser().parseValue(json, filename);
  }

  /**
   * Parse a JSON input.
   *
   * @param json The json input.
   * @return A JSON object.
   * @throws IOException If the parsing process fail.
   */
  public static Object parse(final String json) throws IOException {
    return new JsonParser().parseValue(json, null);
  }

  /**
   * Parse a JSON input.
   *
   * @param json The json input.
   * @param filename The json file's name.
   * @return A JSON object.
   * @throws JsonParseException If the parsing process fail.
   */
  private Object parseValue(final String json, final String filename) throws JsonParseException {
    if (json == null) {
      throw new JsonParseException("Input string may not be null");
    }
    this.filename = filename;
    pos = 0;
    length = json.length();
    src = new StringBuilder(json);
    Object value = readValue();
    consumeWhitespace();
    return value;
  }

  /**
   * Parse a JSON value.
   *
   * @return A JSON value.
   * @throws JsonParseException If the parsing process fail.
   */
  private Object readValue() throws JsonParseException {
    consumeWhitespace();
    while (pos < length) {
      char c = src.charAt(pos++);
      switch (c) {
        case '{':
          return readObject();
        case '[':
          return readArray();
        case 't':
          return readTrue();
        case 'f':
          return readFalse();
        case '"':
          return readString();
        case 'n':
          return readNull();
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
        case '0':
        case '-':
          return readNumber(c);
        default:
          throw reportError(null, c, pos - 1);
      }
    }
    throw new JsonParseException("Empty JSON string");
  }

  /**
   * Parse a JSON object.
   *
   * @return A JSON object.
   * @throws JsonParseException If the parsing process fail.
   */
  private Object readObject() throws JsonParseException {
    Map<String, Object> object = new LinkedHashMap<String, Object>();
    String id;
    Object value;
    boolean needsComma = false;
    consumeWhitespace();
    while (pos < length) {
      char c = src.charAt(pos++);
      switch (c) {
        case '}':
          return object;
        case ',':
          if (!needsComma) {
            throw reportError(null, ",", pos - 1);
          }
          needsComma = false;
          break;
        case '"':
          if (needsComma) {
            throw reportError(",", '"', pos - 1);
          }
          id = readString();
          consume(':');
          value = readValue();

          object.put(id, value);

          needsComma = true;
          break;
        default:
          throw reportError(null, c, pos - 1);
      }
      consumeWhitespace();
    }
    throw reportError("}", pos == length ? "eof" : src.charAt(pos - 1), pos - 1);
  }

  /**
   * Parse a JSON array.
   *
   * @return A JSON array.
   * @throws JsonParseException If the parsing process fail.
   */
  private Object readArray() throws JsonParseException {
    List<Object> list = new ArrayList<Object>();
    boolean needsComma = false;
    consumeWhitespace();
    while (pos < length) {
      char c = src.charAt(pos);
      switch (c) {
        case ']':
          pos += 1;
          return list;
        case ',':
          if (!needsComma) {
            throw reportError(null, c, pos);
          }
          needsComma = false;
          pos += 1;
          break;
        default:
          if (needsComma) {
            throw reportError(",", c, pos);
          }
          list.add(readValue());
          needsComma = true;
      }
      consumeWhitespace();
    }
    throw reportError("]", pos == length ? "eof" : src.charAt(pos - 1), pos);
  }

  /**
   * Parse a JSON string.
   *
   * @return A JSON string.
   * @throws JsonParseException If the parsing process fail.
   */
  private String readString() throws JsonParseException {
    StringBuilder b = new StringBuilder();
    while (pos < length) {
      char c = src.charAt(pos++);
      if (c <= '\u001F') {
        throw reportError(null, c, pos - 1);
      }
      switch (c) {
        case '\\':
          if (pos >= length) {
            throw reportError('"', "eof", pos - 1);
          }
          c = src.charAt(pos++);
          switch (c) {
            case '"':
              b.append('"');
              break;
            case '\\':
              b.append('\\');
              break;
            case '/':
              b.append('/');
              break;
            case 'b':
              b.append('\b');
              break;
            case 'f':
              b.append('\f');
              break;
            case 'n':
              b.append('\n');
              break;
            case 'r':
              b.append('\r');
              break;
            case 't':
              b.append('\t');
              break;
            case 'u':
              if (length - pos < 5) {
                throw reportError(null, "\\u" + src.substring(pos), pos - 1);
              }
              try {
                b.append((char) Integer.parseInt(src.substring(pos, pos + 4), 16));
                pos += 4;
              } catch (NumberFormatException nfx) {
                throw reportError(null, src.substring(pos, pos + 4), pos - 1);
              }
              break;
            default:
              throw reportError(null, "'\\" + c + "'", pos - 1);
          }
          break;
        case '"':
          return b.toString();
        default:
          b.append(c);
          break;
      }
    }
    throw reportError('"', pos == length ? "eof" : src.charAt(pos - 1), pos - 1);
  }

  /**
   * Parse a JSON number.
   *
   * @param first The matched token.
   * @return A JSON number.
   * @throws JsonParseException If the parsing process fail.
   */
  private Number readNumber(final char first) throws JsonParseException {
    StringBuilder b = new StringBuilder();
    b.append(first);
    while (pos < length) {
      char c = src.charAt(pos);
      if (!Character.isDigit(c)
          && c != '-'
          && c != '+'
          && c != '.'
          && c != 'e'
          && c != 'E') {
        break;
      }
      pos += 1;
      b.append(c);
    }
    String num = b.toString();
    int numLength = num.length();
    try {
      // check for leading zeroes
      for (int i = 0; i < numLength; i++) {
        char c = num.charAt(i);
        if (Character.isDigit(c)) {
          if (c == '0'
              && numLength > i + 1
              && Character.isDigit(num.charAt(i + 1))) {
            throw reportError("number", num, pos - num.length() + 1);
          }
          break;
        }
      }
      final double dval = Double.parseDouble(num);
      final int ival = (int) dval;
      if (ival == dval) {
        return Integer.valueOf(ival);
      } else {
        return Double.valueOf(dval);
      }
    } catch (NumberFormatException nfe) {
      throw reportError(null, num, pos);
    }
  }

  /**
   * Parse a true literal.
   *
   * @return A true literal.
   * @throws JsonParseException If the parsing process fail.
   */
  private Boolean readTrue() throws JsonParseException {
    if (length - pos < 3
        || src.charAt(pos) != 'r'
        || src.charAt(pos + 1) != 'u'
        || src.charAt(pos + 2) != 'e') {
      throw reportError("true", src.substring(pos - 1, Math.min(pos + 3, length)), pos - 1);
    }
    pos += 3;
    return Boolean.TRUE;
  }

  /**
   * Parse a false literal.
   *
   * @return A false literal.
   * @throws JsonParseException If the parsing process fail.
   */

  private Boolean readFalse() throws JsonParseException {
    if (length - pos < 4
        || src.charAt(pos) != 'a'
        || src.charAt(pos + 1) != 'l'
        || src.charAt(pos + 2) != 's'
        || src.charAt(pos + 3) != 'e') {
      throw reportError("false", src.substring(pos - 1, Math.min(pos + 4, length)), pos - 1);
    }
    pos += 4;
    return Boolean.FALSE;
  }

  /**
   * Parse a null literal.
   *
   * @return A null literal.
   * @throws JsonParseException If the parsing process fail.
   */
  private Object readNull() throws JsonParseException {
    if (length - pos < 3
        || src.charAt(pos) != 'u'
        || src.charAt(pos + 1) != 'l'
        || src.charAt(pos + 2) != 'l') {
      throw reportError("null", src.substring(pos - 1, Math.min(pos + 3, length)), pos - 1);
    }
    pos += 3;
    return null;
  }

  /**
   * Consume any whitespace.
   */
  private void consumeWhitespace() {
    while (pos < length) {
      char c = src.charAt(pos);
      switch (c) {
        case ' ':
        case '\t':
        case '\r':
        case '\n':
          pos += 1;
          break;
        default:
          return;
      }
    }
  }

  /**
   * Matches a token and consume it.
   *
   * @param token The token.
   * @throws JsonParseException If the parsing process fail.
   */
  private void consume(final char token) throws JsonParseException {
    consumeWhitespace();
    if (pos >= length) {
      throw reportError(token, "eof", pos);
    }
    char c = src.charAt(pos++);
    if (c == token) {
      return;
    } else {
      throw reportError(token, c, pos);
    }
  }

  /**
   * Report a parsing error.
   *
   * @param expected The expected token.
   * @param found The found token.
   * @param position The position.
   * @return A new {@link JsonParseException}.
   */
  private JsonParseException reportError(final Object expected, final Object found,
      final int position) {
    int idx = 0;
    int line = 1;
    int lineOffset = 0;
    while (idx < position) {
      char ch = src.charAt(idx);
      if (ch == '\n') {
        line++;
        lineOffset = idx;
      }
      idx++;
    }
    int column = Math.max(1, position - lineOffset);
    String fname = filename == null ? "" : filename + ":";
    final String message;
    if (expected == null) {
      message = String.format(fname + "%s:%s: unexpected token: '%s'", line, column, found);
    } else {
      message = String.format(fname + "%s:%s: expected: '%s', found: '%s'", line, column, expected,
          found);
    }
    return new JsonParseException(message);
  }

  /**
   * A json parse exception.
   *
   * @author edgar.espina
   * @since 0.2.0
   */
  @SuppressWarnings("serial")
  public static class JsonParseException extends IOException {
    /**
     * Creates a new {@link JsonParseException}.
     *
     * @param message The error message.
     */
    public JsonParseException(final String message) {
      super(message);
    }
  }

}
