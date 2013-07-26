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

  private final Map<Integer, Integer> maxWordCache = new HashMap<Integer, Integer>();
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
    int[] ends = new int[n];
    MaxNResult result = new MaxNResult(n);
    maxNWordsRec(ends, start, 0, n, result);
    return String.valueOf(text, start, result.ends[0] - start);
  }

  private void maxNWordsRec(int[] ends, int start, int i, int n, MaxNResult result) {
    if (start == text.length) {
      for (int j = i; j < n; j++) {
        ends[j] = 0;
      }
      setResult(ends, start, result);
    } else if (i == n - 1) {
      int end = maxWord(start);
      ends[i] = end;
      setResult(ends, end, result);
    } else {
      Node node = dictionary.getTree();
      int end = start + 1;
      ends[i] = end;
      maxNWordsRec(ends, end, i + 1, n, result);
      for (int j = start; node != null && j < text.length; j++) {
        Node next = node.get(text[j]);
        if (next != null && next.weight() > 0) {
          ends[i] = j + 1;
          maxNWordsRec(ends, j + 1, i + 1, n, result);
        }
        node = next;
      }
    }
  }

  private int maxWord(int start) {
    Integer result = maxWordCache.get(start);
    if (result != null) {
      return result;
    }
    Node node = dictionary.getTree();
    int maxEnd = start + 1;
    for (int i = start; node != null && i < text.length; i++) {
      Node next = node.get(text[i]);
      if (next != null && next.weight() > 0) {
        maxEnd = i + 1;
      }
      node = next;
    }
    maxWordCache.put(start, maxEnd);
    return maxEnd;
  }

  private void setResult(int[] ends, int chunkEnd, MaxNResult result) {
    if (chunkEnd > result.maxChunkEnd
        || chunkEnd == result.maxChunkEnd && checkRules(ends, result.ends)) {
      result.maxChunkEnd = chunkEnd;
      System.arraycopy(ends, 0, result.ends, 0, ends.length);
    }
  }

  private boolean checkRules(int[] ends1, int[] ends2) {
    if (rules != null) {
      for (Rule rule : rules) {
        int result = rule.compare(dictionary, text, ends1, ends2);
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
    private int[] ends;
    private int maxChunkEnd;

    public MaxNResult(int n) {
      ends = new int[n];
    }
  }
}
