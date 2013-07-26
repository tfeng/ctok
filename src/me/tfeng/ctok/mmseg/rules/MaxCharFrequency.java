package me.tfeng.ctok.mmseg.rules;

import me.tfeng.ctok.mmseg.MMSegDictionary;

public class MaxCharFrequency implements Rule {

  @Override
  public int compare(MMSegDictionary dictionary, char[] text, int[] ends1, int[] ends2) {
    return Integer.compare(getCharFrequency(dictionary, text, ends2),
                           getCharFrequency(dictionary, text, ends1));
  }

  private int getCharFrequency(MMSegDictionary dictionary, char[] text, int[] ends) {
    int previousEnd = 0;
    int frequency = 0;
    for (int i = 0; i < ends.length; i++) {
      if (ends[i] > 0) {
        int length = ends[i] - previousEnd;
        if (length == 1) {
          char ch = text[previousEnd];
          frequency += dictionary.getCharFrequency(ch);
        }
        previousEnd = ends[i];
      }
    }
    return frequency;
  }
}
