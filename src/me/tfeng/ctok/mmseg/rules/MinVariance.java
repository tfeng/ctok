package me.tfeng.ctok.mmseg.rules;

import me.tfeng.ctok.mmseg.MMSegDictionary;
import me.tfeng.ctok.mmseg.MMSegDictionary.Node;

public class MinVariance implements Rule {

  @Override
  public int compare(MMSegDictionary dictionary, char[] text, int start, Node[] ends1,
      Node[] ends2) {
    return Double.compare(variance(ends1), variance(ends2));
  }

  private double variance(Node[] ends) {
    int words = 0;
    int maxLength = 0;
    for (; words < ends.length && ends[words] != null;
        maxLength += ends[words].wordLength(), words++);
    double avgLength = (double) maxLength / words;
    double variance2 = 0.0;
    for (Node end : ends) {
      if (end == null) {
        break;
      }
      double difference = end.wordLength() - avgLength;
      variance2 += difference * difference;
    }
    return variance2;
  }
}
