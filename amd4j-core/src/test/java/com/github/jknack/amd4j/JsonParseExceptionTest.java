package com.github.jknack.amd4j;

import java.io.IOException;

import org.junit.Test;

import com.github.jknack.amd4j.JsonParser.JsonParseException;

public class JsonParseExceptionTest {

  @Test(expected = JsonParseException.class)
  public void emptyString() throws IOException {
    JsonParser.parse("{");
  }

  @Test(expected = JsonParseException.class)
  public void unexpectedToken() throws IOException {
    JsonParser.parse("{\n x");
  }

  @Test(expected = JsonParseException.class)
  public void expectingStringDelimiter() throws IOException {
    JsonParser.parse("{\n\"x ");
  }

  @Test(expected = JsonParseException.class)
  public void expectingArrayDelimiter() throws IOException {
    JsonParser.parse("[");
  }

  @Test(expected = JsonParseException.class)
  public void expectingFalse() throws IOException {
    JsonParser.parse("\n\n [ falso]");
  }

  @Test(expected = JsonParseException.class)
  public void expectingTrue() throws IOException {
    JsonParser.parse("[truth]");
  }

  @Test(expected = JsonParseException.class)
  public void expectingNull() throws IOException {
    JsonParser.parse("[nil]");
  }

  @Test(expected = JsonParseException.class)
  public void expectingNumber() throws IOException {
    JsonParser.parse("[000123]");
  }
}
