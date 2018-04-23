package words;

public class Word {
    private final String text;
    
    public Word(String s) {
	text = s;
    }
    
    public String getText() {
	return(text);
    }
    
    public boolean isEmpty() {
        return("".equals(text));
    }
    
    @Override
    public String toString() {
        return text;
    }
    
    @Override
    public boolean equals(Object other) {
        if(other == null)
            return false;
        if(other == this)
            return true;
        if(getClass() != other.getClass())
            return false;
        Word w = (Word)other;
        return text.equals(w.text);
    }
    
    @Override
    public int hashCode() {
        return text.hashCode();
    }
}
