package me.tfeng.ctok.mmseg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.tfeng.ctok.mmseg.rules.MaxAvgWordLength;
import me.tfeng.ctok.mmseg.rules.MaxCharFrequency;
import me.tfeng.ctok.mmseg.rules.MaxWordFrequency;
import me.tfeng.ctok.mmseg.rules.MinVariance;
import me.tfeng.ctok.mmseg.rules.Rule;

public class MMSegData {

  public static final List<Rule> DEFAULT_RULES;
  static {
    DEFAULT_RULES = new ArrayList<Rule>();
    DEFAULT_RULES.add(new MaxWordFrequency());
    DEFAULT_RULES.add(new MaxAvgWordLength());
    DEFAULT_RULES.add(new MinVariance());
    DEFAULT_RULES.add(new MaxCharFrequency());
  }

  private List<Rule> rules = DEFAULT_RULES;
  private MMSegDictionary dictionary;

  public MMSegData() throws IOException {
    dictionary = new MMSegDictionary();
  }

  public List<Rule> getRules() {
    return rules;
  }

  public MMSegDictionary getDictionary() {
    return dictionary;
  }
}
