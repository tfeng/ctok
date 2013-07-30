package me.tfeng.ctok.core;

public class Part {

  private StringBuffer buffer = new StringBuffer();
  private PartType type;
  private int start;
  private int end;

  public Part(int start) {
    end = this.start = start;
  }

  public boolean append(int ch) {
    CharType charType = CharType.getCharType(ch);
    if (charType == CharType.WHITESPACE) {
      return type == null;
    }
    if (isSamePart(charType)) {
      buffer.append((char) ch);
      if (type == null) {
        type = getPartType(charType);
      }
      end++;
      return true;
    } else {
      return false;
    }
  }

  public boolean isSamePart(CharType charType) {
    return type == null || getPartType(charType) == type;
  }

  public char[] getText() {
    return buffer.toString().toCharArray();
  }

  public int getStart() {
    return start;
  }

  public int getEnd() {
    return end;
  }

  public PartType getType() {
    return type;
  }

  public static PartType getPartType(CharType charType) {
    switch (charType) {
    case DOT:
    case LETTER:
    case NUMBER:
    case OTHER_ASCII:
    case WHITESPACE:
      return PartType.NON_CHINESE;
    default:
      return PartType.CHINESE;
    }
  }
}
