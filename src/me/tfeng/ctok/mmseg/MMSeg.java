package me.tfeng.ctok.mmseg;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;

import me.tfeng.ctok.core.Part;
import me.tfeng.ctok.core.Token;

public class MMSeg {

  private PushbackReader reader;
  private MMSegData mmSegData;
  private MMSegChunks chunks = new MMSegChunks();
  private int currentIndex;

  public MMSeg() throws IOException {
    this(new MMSegData());
  }

  public MMSeg(MMSegData mmSegData) {
    this.mmSegData = mmSegData;
  }

  public void reset(Reader reader) {
    this.reader = new PushbackReader(reader);
  }

  public Token next() throws IOException {
    if (chunks == null || !chunks.hasMoreTokens()) {
      Part part = nextPart();
      if (part == null) {
        return null;
      }
      chunks.reset(mmSegData, part);
    }
    return chunks.takeNextToken();
  }

  public Part nextPart() throws IOException {
    int nextChar = reader.read();
    if (nextChar < 0) {
      return null;
    }
    Part part = new Part(currentIndex);
    do {
      if (!part.append(nextChar)) {
        reader.unread(nextChar);
        break;
      }
      currentIndex++;
      nextChar = reader.read();
    } while (nextChar >= 0);
    return part;
  }
}
