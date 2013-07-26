package me.tfeng.ctok.core;

public enum CharType {

  DOT,
  LETTER,
  NUMBER,
  OTHER,
  OTHER_ASCII,
  PUNCTUATION,
  WHITESPACE;

  public static CharType getCharType(int ch) {
    if (ch == '.') {
      return DOT;
    }
    if (Character.isWhitespace(ch)) {
      return WHITESPACE;
    }
    switch (Character.getType(ch)) {
    case Character.UPPERCASE_LETTER:
    case Character.LOWERCASE_LETTER:
    case Character.TITLECASE_LETTER:
    case Character.MODIFIER_LETTER:
      return LETTER;
    case Character.DECIMAL_DIGIT_NUMBER:
    case Character.LETTER_NUMBER:
    case Character.OTHER_NUMBER:
      return NUMBER;
    case Character.START_PUNCTUATION:
    case Character.END_PUNCTUATION:
    case Character.INITIAL_QUOTE_PUNCTUATION:
    case Character.FINAL_QUOTE_PUNCTUATION:
    case Character.DASH_PUNCTUATION:
    case Character.CONNECTOR_PUNCTUATION:
    case Character.OTHER_PUNCTUATION:
      return PUNCTUATION;
    default:
      if (ch < 128) {
        return OTHER_ASCII;
      } else {
        return OTHER;
      }
    }
  }
}
