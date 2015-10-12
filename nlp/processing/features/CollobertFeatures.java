package nlp.processing.features;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;

import nlp.model.LabeledDataset;
import nlp.model.Sentence;
import nlp.processing.Features;

public class CollobertFeatures extends Features {
	HashMap<String, Vector> wordEmbeddings = new HashMap<String, Vector>();
	
	public CollobertFeatures(String fileName) throws ParseException, IOException {
		readWordEmbeddings(fileName);
	}
	
	private void readWordEmbeddings(String fileName) throws ParseException, IOException {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF8"));
			String rawLine;
			while ((rawLine = br.readLine()) != null) {
				String[] columns = rawLine.split("\t");
				if (columns.length != 51) {
					br.close();
					throw new ParseException("Line is incomplete!",-1);
				}
				//Collobertfeatures看样子是每行的第一个string是token,之后的为其embedding
				wordEmbeddings.put(columns[0], convertToVector(columns));
			}
			br.close();
	}
	
	private Vector convertToVector(String[] textValues) {//from string to double
		Vector result = new Vector(50);
		for (int i = 1; i <= 50; i++) {
			result.indices[i-1]=i-1;
			result.values[i-1]= Double.parseDouble(textValues[i]);
		}
		return result;
	}
	
	@Override
	protected Vector[] getFeatureVector(String token, String rawToken, int pos) {
		String key = rawToken.toLowerCase(Locale.ENGLISH).replaceAll("\\d+", "0");//用0替换任意的digit序列
		if (token.equals(Sentence.BOUNDARY)) {
			key = "PADDING";
		} else if (!wordEmbeddings.containsKey(key)) {
			key = "UNKNOWN";
		}
		Vector[] result = {wordEmbeddings.get(key)};
		
		return result;
	}
	
	@Override
	public String[][] getFeatureNames(String pos) {
		String[][] featureNames = new String[1][50]; //1行50列
		for (int i = 0; i < 50; i++) {
			featureNames[0][i]="{cw|" + pos + "}"+ (i+1); 
		}
		return featureNames;
	}

	@Override
	protected Vector[] getFeatureVector(String token) {
		// TODO Auto-generated method stub
		return null;
	}
	public void saveFeatureName() throws IOException
	{
		
	}
	public static void main(String[] args) throws ParseException, IOException {
		CollobertFeatures cw = new CollobertFeatures("cw_embeddings.txt");
		cw.printVector("", "landforms", 0);		
	}
	//by wenpeng
	public void writeFeatures(LabeledDataset trainingData){}

}