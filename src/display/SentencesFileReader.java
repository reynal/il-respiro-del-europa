package display;

import java.io.*;
import java.util.*;

public class SentencesFileReader {
	
	public final static String FILENAME = "sentences.txt";
	private Vector<String> list = new Vector<String>();
	
	SentencesFileReader() throws IOException {
		
		BufferedReader reader = new BufferedReader(new FileReader(FILENAME));
		String line;
		while ((line = reader.readLine()) != null) {
			if (!line.isBlank()) list.add(line);
		}
		reader.close();
	}
	
	public String toString() {
		
		String s = "";
		Iterator<String> it = list.iterator();
		while (it.hasNext()) s += it.next() + "\n";
		return s;
		
	}
	
	public String[] fetchNewPair() {
		Random random = new Random();
		int n = random.nextInt(list.size());
		String[] ss = new String[2];
		if (n%2 == 1) {
			ss[0] = list.get(n-1); // 0
			ss[1] = list.get(n); // 1
		}
		else {
			ss[0] = list.get(n+1); // 1
			ss[1] = list.get(n); // 0
		}
		return ss;
	}
	
	public static void main(String[] args) throws IOException {
		
		SentencesFileReader sfr = new SentencesFileReader();
		System.out.println(sfr);
		
	}

}
