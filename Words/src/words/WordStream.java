package words;

import java.io.Reader;

public class WordStream {
    private final Reader input;
    
    WordStream(Reader r) {
	input = r;
    }

    private boolean isAlphanumeric(int c) {
	return(Character.isLetterOrDigit((char)c));
    }

    Word nextWord() throws java.io.IOException {
        String text = "";
	int c;
	while((c = input.read()) != -1 && !isAlphanumeric(c))
	    ;
	if(c != -1) {
	    do
		text += (char)c;
	    while((c = input.read()) != -1 && isAlphanumeric(c));
	}
	if(text.length() != 0)
	    return new Word(text);
	else
	    return null;
    }
}

