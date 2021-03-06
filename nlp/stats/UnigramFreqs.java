package nlp.stats;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Set;

import nlp.data.Pair;
import nlp.data.Parser;
import nlp.model.Sentence;

public class UnigramFreqs {
	//unigram还有两个映射表,从词到index,反之从index到token.
	public LinkedHashMap<String, Integer> wordToId = new LinkedHashMap<String, Integer>();
	HashMap<Integer, String> idToWord = new HashMap<Integer, String>();
	public static final int MAX_WORDS = 100000000;   // 1000000 previously
	public static final int TOP_WORDS = 501;
	//unigram关键就是一个一维数组,存放每个token的发生次数.所以输入就是一个corpus,而结果就是一个整数数组
	int[] frequencyTable = new int[MAX_WORDS];
	
	

	
	public UnigramFreqs(ArrayList<Sentence> corpus) {
		this();
		addCorpus(corpus);
	}

	public UnigramFreqs() {
		createIdentifier(Sentence.BOUNDARY);
	}

	public Set<String> getVocabulary() {
		return wordToId.keySet();
	}
	
	public String[] getVocabularyAsArray() {
		return getVocabulary().toArray(new String[getVocabulary().size()]);
	}
	//将corpus输入.然后进行unigram的统计, unigram only consider the lowercase
	public void addCorpus(ArrayList<Sentence> corpus) {
		for (Sentence sentence : corpus) {
			for (String token : sentence.getTokens()) {
				createIdentifier(token);
				incrementTable(token);
			}
			// Count if there were two <BOUNDARY> tokens on each side of the sentence
			incrementTable(Sentence.BOUNDARY);
			incrementTable(Sentence.BOUNDARY);
		}
	}
	
	public void addFile(String filePath) throws IOException
	{
		//本函数假设输入文件中一句为一行,单词间用空格分开
		String delimiter="\\s+";
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		String currentLine;
		int lineCo=0;
		while ((currentLine = br.readLine()) != null) 
		{
			// Skip empty lines
			if (!currentLine.trim().isEmpty()) 
			{
				System.out.println("unigram updating..."+lineCo+"...");
				lineCo++;
				String[] splitted = currentLine.split(delimiter);
				for (String token : splitted) {				
					createIdentifier(token.toLowerCase(Locale.ENGLISH).replaceAll("\\d", "0"));
					incrementTable(token.toLowerCase(Locale.ENGLISH).replaceAll("\\d", "0"));
				}
				// Count if there were two <BOUNDARY> tokens on each side of the sentence
				incrementTable(Sentence.BOUNDARY);
				incrementTable(Sentence.BOUNDARY);
			}
			
		}
		br.close();
	}
	
	//下面是遇到一个词word,首先看它是不是新来的,然后计算她的index,新来的总是放到最后.然后添加到两个映射表wordtoid和idtoword
	private void createIdentifier(String word) {
		if (!wordToId.containsKey(word)) {
			int id = wordToId.size();
			wordToId.put(word, id);
			idToWord.put(id, word);
		}
	}

	public int translate(String word) {
		return wordToId.get(word);
	}

	public String translate(int id) {
		return idToWord.get(id);
	}

	private void incrementTable(String word) {
		frequencyTable[translate(word)]++;
	}

	public int getFrequency(String word) {
		if (wordToId.containsKey(word)) {
			return frequencyTable[translate(word)];
		} else {
			return 0;
		}
	}
	//先将频率表拷贝到一个可以进行排序的pairs数组中,排序后,把前n个对象拷贝到topNtokens里面返回
	public ArrayList<String> getNmostFrequentTokens(int n) {
		Pair[] pairs = new Pair[wordToId.size()];
		
		for (int i = 0; i < wordToId.size(); i++) {
			pairs[i]=new Pair(i, frequencyTable[i]);
		}
		
		ArrayList<String> topNTokens = new ArrayList<String>();

		Arrays.sort(pairs);//ascending order
		for (int i = 0; i < Math.min(n, wordToId.size()); i++) {
			topNTokens.add(translate(pairs[i].index));
		}
		return topNTokens;
	}

	public static void main(String[] args) throws IOException {
		ArrayList<String> testTokens = new ArrayList<String>();

		final int testSize = 1500;
		for (int i = 1; i <= testSize; i++) {
			for (int j = 1; j <= i; j++) {
				testTokens.add(Integer.toString(i));
			}
			testTokens.add("\n");
		}
		Collections.shuffle(testTokens);
		testTokens.add("\n");
		
		FileWriter fstream = new FileWriter("unigramTest.txt");
		BufferedWriter out = new BufferedWriter(fstream);
		for (int i = 0; i < testTokens.size() - 1; i++) {
			String token = testTokens.get(i);
			String nextToken = testTokens.get(i + 1);
			out.write(token);
			if (!nextToken.equals("\n") && !token.equals("\n")) {
				out.write("\t");
			}
		}
		out.close();

		Parser test = new Parser();
		test.parseUnlabeledFile("unigramTest.txt");

		UnigramFreqs testFreq = new UnigramFreqs();
		testFreq.addCorpus(test.getCorpus());
		ArrayList<String> resultList = testFreq.getNmostFrequentTokens(testSize+1);
		for (int i = 0; i < resultList.size(); i++) {
			String token = resultList.get(i);
			if (i == 0) {
				assert (testFreq.getFrequency(token) > testSize) : "Fehler bei <BOUNDARY>";
			} else {
				assert (testFreq.getFrequency(token) == testSize - i + 1) : "Fehler bei Token " + token + " (i=" + i + ")!";
				assert (testFreq.getFrequency(token) == Integer.parseInt(token));
			}
			
		}
		
		int index = 0;
		String[] vocabAsArray = testFreq.getVocabularyAsArray();
		for (String word : testFreq.getVocabulary()) {
			assert (vocabAsArray[index].equals(word)) : "Vokabularfehler bei " + word;
			index++;
		}
			
		
		System.out.println("Test completed!");
	}
}
