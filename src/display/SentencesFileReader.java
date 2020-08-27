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
	}
	
	public String toString() {
		
		String s = "";
		Iterator it = list.iterator();
		while (it.hasNext()) s += it.next() + "\n";
		return s;
		
	}
	
	public String[] fetchNewPair() {
		Random random = new Random();
		int n = random.nextInt(list.size());
		if (n%2 == 1) n--;
		String[] ss = new String[2];
		ss[0] = list.get(n);
		ss[1] = list.get(n+1);
		return ss;
	}
	
	public static void main(String[] args) throws IOException {
		
		SentencesFileReader sfr = new SentencesFileReader();
		System.out.println(sfr);
		
	}

}
