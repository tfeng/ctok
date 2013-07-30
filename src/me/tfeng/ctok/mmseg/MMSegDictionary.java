package me.tfeng.ctok.mmseg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class MMSegDictionary {

  public static final String COMMENT_PREFIX = "#";
  public static final String DEFAULT_WORDS_FILE = "data/dictionary.tab";
  public static final String WORD_FREQYENCY_FILE = "data/sogou/sogou-word-freq.dict";

  private Node tree = new Node(null, '\0', 0);

  public MMSegDictionary() throws IOException {
    this(new InputStreamReader(MMSegDictionary.class.getClassLoader().getResourceAsStream(DEFAULT_WORDS_FILE)),
         new InputStreamReader(MMSegDictionary.class.getClassLoader().getResourceAsStream(WORD_FREQYENCY_FILE)),
         true);
  }

  private MMSegDictionary(Reader wordsReader, Reader wordFrequencyReader,
      boolean close) throws IOException {
    try {
      loadDictionary(wordsReader);
      loadDictionary(wordFrequencyReader);
    } finally {
      if (close) {
        wordsReader.close();
        wordFrequencyReader.close();
      }
    }
  }

  public Node newNode(char ch) {
    return tree.seek(ch, true);
  }

  public Node newNode(char[] word) {
    return tree.seek(word, 0, -1, true);
  }

  public void loadDictionary(Reader reader) throws IOException {
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
      Node node = newNode(parts[0].toCharArray());
      if (parts.length > 1) {
        node.frequency = Long.parseLong(parts[1]);
      }
      line = bufferedReader.readLine();
    }
  }

  public Node getTree() {
    return tree;
  }

  public static class Node {
    private Map<Character, Node> children = new HashMap<Character, Node>();
    private char ch;
    private Node parent;
    private int depth;
    private double weight = 1.0;
    private long frequency = 0;
    private boolean exists;

    private Node(Node parent, char ch, int depth) {
      this.parent = parent;
      this.ch = ch;
      this.depth = depth;
    }

    public Node get(char ch) {
      return children.get(ch);
    }

    public Node parent() {
      return parent;
    }

    public char getChar() {
      return ch;
    }

    public double weight() {
      return weight;
    }

    public long frequency() {
      return frequency;
    }

    public boolean exists() {
      return exists;
    }

    public int wordLength() {
      return exists ? depth : -1;
    }

    public Node seek(char ch, boolean createNew) {
      Node child = children.get(ch);
      if (createNew) {
        if (child == null) {
          child = new Node(this, ch, depth + 1);
          children.put(ch, child);
        }
        child.exists = true;
      }
      return child;
    }

    public Node seek(char[] word, int start, int end, boolean createNew) {
      if (end >= 0 && start == end || end < 0 && start == word.length) {
        if (createNew) {
          exists = true;
        }
        return this;
      }
      char ch = word[start];
      Node child = children.get(ch);
      if (child == null) {
        if (createNew) {
          child = new Node(this, ch, depth + 1);
          children.put(ch, child);
        } else {
          return null;
        }
      }
      return child.seek(word, start + 1, end, createNew);
    }

    public String getWord() {
      StringBuffer buffer = new StringBuffer(depth);
      Node node = this;
      while (node.parent != null) {
        buffer.append(node.ch);
        node = node.parent;
      }
      return buffer.reverse().toString();
    }

    @Override
    public String toString() {
      return getWord();
    }
  }
}
