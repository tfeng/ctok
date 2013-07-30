package me.tfeng.ctok.mmseg.rules;

import me.tfeng.ctok.mmseg.MMSegDictionary;
import me.tfeng.ctok.mmseg.MMSegDictionary.Node;

public class MaxAvgWordLength implements Rule {

  @Override
  public int compare(MMSegDictionary dictionary, char[] text, int start, Node[] ends1,
      Node[] ends2) {
    return Integer.compare(getWordCount(ends1), getWordCount(ends2));
  }

  private int getWordCount(Node[] ends) {
    int size = ends.length;
    while (size > 0 && ends[size - 1] == null) {
      size--;
    }
    return size;
  }
}
