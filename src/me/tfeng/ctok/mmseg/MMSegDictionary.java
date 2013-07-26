package me.tfeng.ctok.mmseg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class MMSegDictionary {

  public static final String COMMENT_PREFIX = "#";
  public static final String DEFAULT_CHARS_FILE = "data/chars.dict";
  public static final String DEFAULT_WORDS_FILE = "data/words.dict";
  public static final String WORD_FREQYENCY_FILE = "data/sogou/sogou-word-freq.dict";

  private Map<Character, Integer> charMap;
  private Node tree;

  public MMSegDictionary() throws IOException {
    this(new InputStreamReader(MMSegDictionary.class.getClassLoader().getResourceAsStream(DEFAULT_CHARS_FILE)),
         new InputStreamReader(MMSegDictionary.class.getClassLoader().getResourceAsStream(DEFAULT_WORDS_FILE)),
         new InputStreamReader(MMSegDictionary.class.getClassLoader().getResourceAsStream(WORD_FREQYENCY_FILE)),
         true);
  }

  private MMSegDictionary(Reader charsReader, Reader wordsReader, Reader wordFrequencyReader,
      boolean close) throws IOException {
    try {
      loadChars(charsReader);
      loadWords(wordsReader);
      loadWordFrequency(wordFrequencyReader);
    } finally {
      if (close) {
        charsReader.close();
        wordsReader.close();
      }
    }
  }

  public void loadChars(Reader reader) throws IOException {
    charMap = new HashMap<Character, Integer>();
    BufferedReader bufferedReader = new BufferedReader(reader);
    String line = bufferedReader.readLine();
    while (line != null) {
      int commentStart = line.indexOf(COMMENT_PREFIX);
      if (commentStart >= 0) {
        line = line.substring(0, commentStart);
      }
      line = line.trim();
      if (line.isEmpty()) {
        continue;
      }
      String[] parts = line.split(" ");
      charMap.put(parts[0].charAt(0), Integer.valueOf(parts[1]));
      line = bufferedReader.readLine();
    }
  }

  public void loadWords(Reader reader) throws IOException {
    tree = new Node();

    BufferedReader bufferedReader = new BufferedReader(reader);
    String line = bufferedReader.readLine();
    while (line != null) {
      int commentStart = line.indexOf(COMMENT_PREFIX);
      if (commentStart >= 0) {
        line = line.substring(0, commentStart);
      }
      line = line.trim();
      if (line.isEmpty()) {
        continue;
      }
      Node node = tree.seek(line.toCharArray(), 0, -1, true);
      node.weight = Node.DEFAULT_WEIGHT;
      line = bufferedReader.readLine();
    }
  }

  public void loadWordFrequency(Reader reader) throws IOException {
    BufferedReader bufferedReader = new BufferedReader(reader);
    String line = bufferedReader.readLine();
    while (line != null) {
      int commentStart = line.indexOf(COMMENT_PREFIX);
      if (commentStart >= 0) {
        line = line.substring(0, commentStart);
      }
      line = line.trim();
      if (line.isEmpty()) {
        continue;
      }
      String[] parts = line.split(" ");
      Node node = tree.seek(parts[0].toCharArray(), 0, -1, true);
      node.weight = Node.DEFAULT_WEIGHT;
      node.frequency = Long.parseLong(parts[1]);
      line = bufferedReader.readLine();
    }
  }

  public int getCharFrequency(char ch) {
    Integer frequency = charMap.get(ch);
    if (frequency == null) {
      return 0;
    } else {
      return frequency;
    }
  }

  public Node getTree() {
    return tree;
  }

  public static class Node {
    public static final double DEFAULT_WEIGHT = 1.0;
    public static final double NON_EXISTING_WEIGHT = -1.0;

    private Map<Character, Node> children = new HashMap<Character, Node>();
    private double weight = -1.0;
    private long frequency = 0;

    private Node() {
    }

    public Node get(char ch) {
      return children.get(ch);
    }

    public double weight() {
      return weight;
    }

    public long frequency() {
      return frequency;
    }

    public Node seek(char[] word, int start, int end, boolean createNew) {
      if (end >= 0 && start == end
          || end < 0 && start == word.length) {
        return this;
      }
      char ch = word[start];
      Node child = children.get(ch);
      if (child == null) {
        if (createNew) {
          child = new Node();
          children.put(ch, child);
        } else {
          return null;
        }
      }
      return child.seek(word, start + 1, end, createNew);
    }
  }
}
