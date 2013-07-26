package me.tfeng.ctok.core;

import java.io.Serializable;

public class Token implements Serializable {

  private static final long serialVersionUID = 1L;

  private String text;
  private int start;
  private int end;

  public Token(String text, int start, int end) {
    this.text = text;
    this.start = start;
    this.end = end;
  }

  public String getText() {
    return text;
  }

  public int getStart() {
    return start;
  }

  public int getEnd() {
    return end;
  }
}
