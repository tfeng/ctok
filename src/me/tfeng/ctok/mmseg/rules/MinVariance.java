package me.tfeng.ctok.mmseg.rules;

import me.tfeng.ctok.mmseg.MMSegDictionary;

public class MinVariance implements Rule {

  @Override
  public int compare(MMSegDictionary dictionary, char[] text, int[] ends1, int[] ends2) {
    return Double.compare(variance(ends1), variance(ends2));
  }

  private double variance(int[] ends) {
    int words = 0;
    int maxLength = 0;
    for (; words < ends.length && ends[words] > 0; words++);
    maxLength = ends[words - 1];
    double avgLength = (double) maxLength / words;
    int previousEnd = 0;
    double variance2 = 0.0;
    for (int i = 0; i < words; i++) {
      int length = ends[i] - previousEnd;
      previousEnd = ends[i];
      double difference = length - avgLength;
      variance2 += difference * difference;
    }
    return variance2;
  }
}
