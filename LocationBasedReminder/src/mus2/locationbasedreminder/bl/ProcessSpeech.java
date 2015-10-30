package mus2.locationbasedreminder.bl;

import java.util.List;

import mus2.locationbasedreminder.dto.SpeechResult;

public class ProcessSpeech {
	
	private List<String> sentences;
	private SpeechResult speechResult;
	
	public ProcessSpeech(List<String> sentences) {
		this.sentences = sentences;
		this.speechResult = new SpeechResult();
	}	
	
	private void processReminder(WordsAnalyzer analyzer) {
		String currentWord = analyzer.getNextWord();
		if (currentWord.equalsIgnoreCase("für")) {
			speechResult.addDescription(analyzer.getWordsUntil(new String[] { "in", "bei", "im", "beim" }));
			currentWord = analyzer.getNextWord();
			String result = analyzer.getWordsUntil("bitte");
			if (currentWord.equalsIgnoreCase("der") || currentWord.equalsIgnoreCase("die") || currentWord.equalsIgnoreCase("das")) {
				speechResult.addLocation(result);
			}
			else {
				speechResult.addLocation(currentWord + result);
			}
		} else if (currentWord.equalsIgnoreCase("in") ||
				currentWord.equalsIgnoreCase("bei") ||
				currentWord.equalsIgnoreCase("im") ||
				currentWord.equalsIgnoreCase("beim")) {
			currentWord = analyzer.getNextWord();
			String result = analyzer.getWordsUntil("für");
			if (currentWord.equalsIgnoreCase("der") || currentWord.equalsIgnoreCase("die") || currentWord.equalsIgnoreCase("das")) {
				speechResult.addLocation(result);
			}
			else {
				speechResult.addLocation(currentWord + result);
			}
			speechResult.addDescription(analyzer.getWordsUntil("bitte"));
		}
	}
	
	private void processCommandControl(WordsAnalyzer analyzer, String currentWord) {
		if (currentWord.equalsIgnoreCase("Ort")) {
			speechResult.addLocation(analyzer.getWordsUntil("Beschreibung"));
			speechResult.addDescription(analyzer.getWordsUntilEnd());
		} else if (currentWord.equalsIgnoreCase("Beschreibung")) {
			speechResult.addDescription(analyzer.getWordsUntil("Ort"));
			speechResult.addLocation(analyzer.getWordsUntilEnd());
		}
	}
	
	public void processSentence(String sentence) {
		String[] words = sentence.split("\\s+");
		
		WordsAnalyzer analyzer = new WordsAnalyzer(words);
		if (analyzer.nextWordsAre(new String[] { "Neue", "Erinnerung" }) ||
				analyzer.nextWordsAre(new String[] { "Erinnerung" }) ||
				analyzer.nextWordsAre(new String[] { "Bitte", "eine", "neue", "Erinnerung" }) ||
				analyzer.nextWordsAre(new String[] { "Bitte", "eine", "Erinnerung" }) ||
				analyzer.nextWordsAre(new String[] { "Eine", "Erinnerung" }) ||
				analyzer.nextWordsAre(new String[] { "Eine", "neue", "Erinnerung" }) ||
				analyzer.nextWordsAre(new String[] { "Bitte", "Erinnerung" })) {
			processReminder(analyzer);
		} else {

			String currentWord = analyzer.getNextWord();
			if (currentWord.equalsIgnoreCase("Ort") || currentWord.equalsIgnoreCase("Beschreibung")) {
				processCommandControl(analyzer, currentWord);
			} else {
				speechResult.addDescription(sentence);
				speechResult.addLocation("Billa");
			}
		}
	}
	
	public SpeechResult getSpeechResult() {
		for (String sentence : sentences) {
			processSentence(sentence);
		}
		return speechResult;
	}
}
