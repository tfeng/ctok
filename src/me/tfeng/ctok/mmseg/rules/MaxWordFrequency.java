package me.tfeng.ctok.mmseg.rules;

import me.tfeng.ctok.mmseg.MMSegDictionary;
import me.tfeng.ctok.mmseg.MMSegDictionary.Node;

public class MaxWordFrequency implements Rule {

  @Override
  public int compare(MMSegDictionary dictionary, char[] text, int[] ends1, int[] ends2) {
    return Long.compare(getTotalFrequency(dictionary, text, ends2),
        getTotalFrequency(dictionary, text, ends1));
  }

  private long getTotalFrequency(MMSegDictionary dictionary, char[] text, int[] ends) {
    int previousEnd = 0;
    long totalFrequency = 0;
    for (int end : ends) {
      if (end <= 0) {
        break;
      }
      Node node = dictionary.getTree().seek(text, previousEnd, end, false);
      if (node != null) {
        totalFrequency += node.frequency();
      }
      previousEnd = end;
    }
    return totalFrequency;
  }
}
