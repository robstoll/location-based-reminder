package mus2.locationbasedreminder.bl;

public class WordsAnalyzer {
	private String[] words;
	private int currentIndex;
	private int wordsLength;
	
	public WordsAnalyzer(String[] words) {
		this.words = words;
		this.currentIndex = 0;
		this.wordsLength = words.length;
	}
	
	public String getNextWord() {
		String result = "";
		if (currentIndex < wordsLength) {
			result = words[currentIndex];
			currentIndex++;
		}
		return result;
	}
	
	public boolean nextWordsAre(String[] delimiterWords) {
		boolean wordsFound = false;
		int ci = currentIndex;
		int wi = 0;
		while (ci < wordsLength && wi < delimiterWords.length && words[ci].equalsIgnoreCase(delimiterWords[wi])) {
			ci++;
			wi++;
		}
		
		if (wi >= delimiterWords.length) {
			wordsFound = true;
			currentIndex = ci + 1;
		}

		return wordsFound;
	}
	
	public String getWordsUntilEnd() {
		StringBuilder sb = new StringBuilder();
		while (currentIndex < wordsLength) {
			sb.append(words[currentIndex]);
			sb.append(" ");
			currentIndex++;
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}
	
	public String getWordsUntil(String delimiterWord) {
		StringBuilder sb = new StringBuilder();
		while (currentIndex < wordsLength && !words[currentIndex].equalsIgnoreCase(delimiterWord)) {
			sb.append(words[currentIndex]);
			sb.append(" ");
			currentIndex++;
		}
		currentIndex++;
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}
	
	public String getWordsUntil(String[] delimiterWords) {
		StringBuilder sb = new StringBuilder();
		boolean wordFound = false;
		while (currentIndex < wordsLength && !wordFound) {
			int i = 0;
			while (i < delimiterWords.length && !wordFound) {
				if (words[currentIndex].equalsIgnoreCase(delimiterWords[i])) {
					wordFound = true;
				}
				i++;
			}
			if (!wordFound) {
				sb.append(words[currentIndex]);
				sb.append(" ");
			}
			currentIndex++;
		}
		currentIndex++;
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}
}
