package me.tfeng.ctok.mmseg.rules;

import me.tfeng.ctok.mmseg.MMSegDictionary;

public class MaxAvgWordLength implements Rule {

  @Override
  public int compare(MMSegDictionary dictionary, char[] text, int[] ends1, int[] ends2) {
    return Integer.compare(getWordCount(ends1), getWordCount(ends2));
  }

  private int getWordCount(int[] ends) {
    int size = ends.length;
    while (size > 0 && ends[size - 1] == 0) {
      size--;
    }
    return size;
  }
}
