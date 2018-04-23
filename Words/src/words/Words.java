package words;

import java.io.*;

public class Words {
    static public void main(String [] args) {
	if (args.length != 1) {
	    System.err.println ("Usage: Words <filename>");
	    System.exit(1);
	}
	FileReader rdr = null;
	try {
	    rdr = new FileReader(args[0]);
	} catch (java.io.FileNotFoundException x) {
	    System.err.println ("File '" + args[0] + "' not found");
	    System.exit(1);
	}
	WordStream ws = new WordStream(rdr);
	WordCounter wc = new WordCounter();

	try {
	    Word w;
	    while((w = ws.nextWord()) != null && !w.isEmpty())
		wc.countWord(w);
	    rdr.close();
	} catch (java.io.IOException x) {
	    System.err.println ("I/O error");
	    System.exit(1);
	}
	CountedWord [] words = wc.sortWords();
        for(CountedWord cw : words) {
	    System.out.print (cw.getCount());
	    System.out.print ("\t");
	    System.out.println(cw.getWord());
	}
    }
}

