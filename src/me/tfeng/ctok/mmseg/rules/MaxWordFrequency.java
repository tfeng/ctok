package me.tfeng.ctok.mmseg.rules;

import me.tfeng.ctok.mmseg.MMSegDictionary;
import me.tfeng.ctok.mmseg.MMSegDictionary.Node;

public class MaxWordFrequency implements Rule {

  @Override
  public int compare(MMSegDictionary dictionary, char[] text, int start, Node[] ends1,
      Node[] ends2) {
    return Long.compare(getTotalFrequency(dictionary, ends2), getTotalFrequency(dictionary, ends1));
  }

  private long getTotalFrequency(MMSegDictionary dictionary, Node[] ends) {
    long totalFrequency = 0;
    for (Node end : ends) {
      if (end == null) {
        break;
      }
      totalFrequency += end.frequency();
    }
    return totalFrequency;
  }
}
