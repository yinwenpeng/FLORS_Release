package nlp.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nlp.model.Sentence;


//I should modify this class as so to input a file sentence by sentence

public class Parser {

	protected ArrayList<Sentence> corpus = new ArrayList<Sentence>();
	protected String delimiter = "\\s+";  //tab
	protected boolean replaceDigits = true;// previously is "false"
	protected boolean ignoreCapitalization = true;
	
	public void parseUnlabeledFile(String fileName) throws IOException {
		//先将file读到一个buffer中,然后将每一句经过替换digit和小写化后存入corpus里面
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String currentLine;

		while ((currentLine = br.readLine()) != null) {
			// Skip empty lines
			if (!currentLine.trim().isEmpty()) {
				ArrayList<String> words = new ArrayList<String>(Arrays.asList(currentLine.split(delimiter)));
				Sentence currentSentence = new Sentence();
				//because of unlabelddata, only set the tokens, not tags
				currentSentence.setTokens(words);
				currentSentence.setRawTokens(new ArrayList<String>(words));
				if (replaceDigits)
					currentSentence.replaceDigits();
				if (ignoreCapitalization)
					currentSentence.convertToLowerCase();
				corpus.add(currentSentence); //store each new sentence into corpus
				
			}
		}
		br.close();
	}

	public void clear() {
		corpus.clear();
	}

	public void parseTreebankFile(String fileName) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		Pattern pattern = Pattern.compile("\\(([^()]+)\\)");
		String currentLine;

		while ((currentLine = br.readLine()) != null) {
			// Skip empty lines
			if (!currentLine.trim().isEmpty()) {
				Sentence currentSentence = new Sentence();
				// Extract innermost brackets
				Matcher matcher = pattern.matcher(currentLine);
				while (matcher.find()) {
					// Add all matches
					String match = matcher.group(1);
					String token = match.split(" ")[1];
					String tag = match.split(" ")[0];
					currentSentence.add(token, tag);
				}

				if (replaceDigits)
					currentSentence.replaceDigits();
				if (ignoreCapitalization)
					currentSentence.convertToLowerCase();
				corpus.add(currentSentence);
			}
		}
		br.close();
	}
	//注意labeled data的区别在于其存在label,而且每行仅有两个,分别是token-tag, 一个句子完了是用一个空行表示的
	//parsing文件其实就是将文件按照句子为单位加入corpus
	public void parseLabeledFile(String fileName) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String currentLine;
		Sentence currentSentence = new Sentence();
		//int line=0;
		while ((currentLine = br.readLine()) != null) {
			if (currentLine.isEmpty()) { // meet the end of a sentence
				if (currentSentence.size() > 0) {
					if (replaceDigits)
						currentSentence.replaceDigits();
					if (ignoreCapitalization)
						currentSentence.convertToLowerCase();
					// add current sentence into corpus
					corpus.add(currentSentence);
					//if(line>=5000) break;
					currentSentence = new Sentence();
				}
			} else {
				String[] splitted = currentLine.split(delimiter);
				if (splitted.length != 2) {
					br.close();
					throw new IOException("Malformed line: " + currentLine);
				} else {
					//line++;
					//System.out.println("Parsing line "+line);
					String token = splitted[0];
					String tag = splitted[1];
					currentSentence.add(token, tag);
					currentSentence.addRawToken(token);
				}
			}
		}
		
		br.close();
	}


	public void displayTokenizedFile() {
		for (Sentence sentence : corpus) {
			System.out.println(sentence);
		}
	}

	public ArrayList<Sentence> getCorpus() {
		return corpus;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	public void writeToFile(String fileName) {
		writeToFile(fileName, corpus);
	}
	
	public static void writeToFile(String fileName, ArrayList<Sentence> corpus) {
		FileWriter fw;
		try {
			fw = new FileWriter(fileName, true);

			BufferedWriter bw = new BufferedWriter(fw);
			for (Sentence s : corpus) {
				for (int i = 0; i < s.size(); i++) {
					bw.write(s.getToken(i) + "\n");
				}
				bw.write('\n');
			}
			bw.close();
		} catch (IOException e) {
			System.out.println("Couldn't write corpus to file: " + fileName);
		}
	}

	public void replaceDigits(boolean newValue) {
		replaceDigits = newValue;
	}

	public void ignoreCapitalization(boolean value) {
		ignoreCapitalization = value;
	}
}