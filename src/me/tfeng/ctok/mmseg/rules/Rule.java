package me.tfeng.ctok.mmseg.rules;

import me.tfeng.ctok.mmseg.MMSegDictionary;
import me.tfeng.ctok.mmseg.MMSegDictionary.Node;

public interface Rule {

  public int compare(MMSegDictionary dictionary, char[] text, int start, Node[] ends1,
      Node[] ends2);
}
