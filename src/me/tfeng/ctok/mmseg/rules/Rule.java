package me.tfeng.ctok.mmseg.rules;

import me.tfeng.ctok.mmseg.MMSegDictionary;

public interface Rule {

  public int compare(MMSegDictionary dictionary, char[] text, int[] ends1, int[] ends2);
}
