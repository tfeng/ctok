package me.tfeng.ctok.test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.text.NumberFormat;
import java.util.Date;

import me.tfeng.ctok.core.Token;
import me.tfeng.ctok.mmseg.MMSeg;

public class TestMMSeg {

  private static final NumberFormat DECIMAL_FORMAT = NumberFormat.getInstance();
  static {
    DECIMAL_FORMAT.setMinimumFractionDigits(2);
    DECIMAL_FORMAT.setMaximumFractionDigits(2);
    DECIMAL_FORMAT.setGroupingUsed(false);
  }

  public static void main(String[] args) throws IOException {
    if (args.length < 1) {
      System.err.println("Usage: java " + TestMMSeg.class.getSimpleName() + " <input> [output]");
      System.exit(1);
    }

    InputStream inputStream = new FileInputStream(args[0]);
    OutputStream outputStream;
    if (args.length >= 2) {
      outputStream = new FileOutputStream(args[1]);
    } else {
      outputStream = System.out;
    }
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
    Writer writer = new OutputStreamWriter(outputStream);
    MMSeg mmSeg = new MMSeg();

    Date startTime = new Date();
    int lineCount = 0;
    int tokenCount = 0;

    try {
      String line = reader.readLine();
      while (line != null) {
        lineCount++;
        mmSeg.reset(new StringReader(line));
        StringBuffer buffer = new StringBuffer();
        Token token = mmSeg.next();
        while (token != null) {
          tokenCount++;
          if (buffer.length() > 0) {
            buffer.append(" ");
          }
          buffer.append(token.getText());
          token = mmSeg.next();
        }
        buffer.append("\n");
        writer.write(buffer.toString());
        line = reader.readLine();
      }
    } finally {
      reader.close();
      writer.close();
    }

    Date endTime = new Date();
    double seconds = (double) (endTime.getTime() - startTime.getTime()) / 1000;
    System.out.println("Time spent: " + DECIMAL_FORMAT.format(seconds) + " seconds");
    System.out.println("Lines     : " + lineCount);
    System.out.println("Tokens    : " + tokenCount);
    System.out.println("Lines/Sec : " + DECIMAL_FORMAT.format(lineCount / seconds));
    System.out.println("Tokens/Sec: " + DECIMAL_FORMAT.format(tokenCount / seconds));
  }
}
