package me.tfeng.ctok.mmseg;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.tfeng.ctok.core.Part;
import me.tfeng.ctok.core.PartType;
import me.tfeng.ctok.core.Token;
import me.tfeng.ctok.mmseg.MMSegDictionary.Node;
import me.tfeng.ctok.mmseg.rules.Rule;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.util.Version;

public class MMSegChunks {

  public static final int N = 3;

  private final Map<Integer, Node> maxWordCache = new HashMap<Integer, Node>();
  private final List<Token> tokens = new ArrayList<Token>();

  private final Analyzer analyzer = new Analyzer() {
    @Override
    protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
      StandardTokenizer t = new StandardTokenizer(Version.LUCENE_43, reader);
      return new TokenStreamComponents(t, new LowerCaseFilter(Version.LUCENE_43, t));
    }
  };

  private List<Rule> rules;
  private MMSegDictionary dictionary;
  private Part part;
  private char[] text;

  private int currentIndex = 0;

  public void reset(MMSegData mmSegData, Part part) {
    rules = mmSegData.getRules();
    dictionary = mmSegData.getDictionary();
    this.part = part;
    text = part.getText();

    maxWordCache.clear();
    tokens.clear();
    currentIndex = 0;

    seg();
  }

  public boolean hasMoreTokens() {
    return tokens != null && !tokens.isEmpty() && tokens.size() > currentIndex;
  }

  public Token takeNextToken() {
    if (hasMoreTokens()) {
      return tokens.get(currentIndex++);
    } else {
      return null;
    }
  }

  private void seg() {
    if (part.getType() == PartType.NON_CHINESE) {
      try {
        TokenStream tokenStream = analyzer.tokenStream(null, new CharArrayReader(text));
        tokenStream.reset();
        CharTermAttribute termAttribute = tokenStream.getAttribute(CharTermAttribute.class);
        OffsetAttribute offsetAttribute = tokenStream.getAttribute(OffsetAttribute.class);
        while (tokenStream.incrementToken()) {
          tokens.add(new Token(termAttribute.toString(),
              offsetAttribute.startOffset(), offsetAttribute.endOffset()));
        }
      } catch (IOException e) {
        throw new RuntimeException("Unable to tokenize text", e);
      }
    } else {
      int start = 0;
      while (start < text.length) {
        String token = maxNWords(start, N);
        tokens.add(new Token(token, start, start + token.length()));
        start += token.length();
      }
    }
  }

  private String maxNWords(int start, int n) {
    Node[] ends = new Node[n];
    MaxNResult result = new MaxNResult(start, n);
    maxNWordsRec(start, ends, 0, n, result);
    return String.valueOf(text, start, result.ends[0].wordLength());
  }

  private void maxNWordsRec(int start, Node[] ends, int i, int n, MaxNResult result) {
    if (start == text.length) {
      for (int j = i; j < n; j++) {
        ends[j] = null;
      }
      setResult(start, ends, result);
    } else if (i == n - 1) {
      Node end = maxWord(start);
      ends[i] = end;
      setResult(start, ends, result);
    } else {
      Node node = dictionary.newNode(text[start]);
      ends[i] = node;
      maxNWordsRec(start + 1, ends, i + 1, n, result);
      for (int j = start + 1; node != null && j < text.length; j++) {
        Node next = node.get(text[j]);
        if (next != null && next.exists()) {
          ends[i] = next;
          maxNWordsRec(j + 1, ends, i + 1, n, result);
        }
        node = next;
      }
    }
  }

  private Node maxWord(int start) {
    Node result = maxWordCache.get(start);
    if (result != null) {
      return result;
    }
    Node node = dictionary.newNode(text[start]);
    Node maxNode = node;
    for (int i = start; node != null && i < text.length; i++) {
      Node next = node.get(text[i]);
      if (next != null && next.exists()) {
        maxNode = node;
      }
      node = next;
    }
    maxWordCache.put(start, maxNode);
    return maxNode;
  }

  private void setResult(int start, Node[] ends, MaxNResult result) {
    int chunkLength = totalLength(ends);
    if (chunkLength > result.maxChunkLength
        || chunkLength == result.maxChunkLength && checkRules(start, ends, result.ends)) {
      result.maxChunkLength = chunkLength;
      System.arraycopy(ends, 0, result.ends, 0, ends.length);
    }
  }

  private int totalLength(Node[] ends) {
    int length = 0;
    for (Node end : ends) {
      if (end == null) {
        break;
      }
      length += end.wordLength();
    }
    return length;
  }

  private boolean checkRules(int start, Node[] ends1, Node[] ends2) {
    if (rules != null) {
      for (Rule rule : rules) {
        int result = rule.compare(dictionary, text, start, ends1, ends2);
        if (result < 0) {
          return true;
        } else if (result > 0) {
          return false;
        }
      }
    }
    return false;
  }

  private class MaxNResult {
    private Node[] ends;
    private int maxChunkLength;

    public MaxNResult(int start, int n) {
      ends = new Node[n];
    }
  }
}
