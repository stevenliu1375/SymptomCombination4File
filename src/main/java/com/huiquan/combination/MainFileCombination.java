package com.huiquan.combination;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MainFileCombination {
	static StringBuffer bzms = new StringBuffer();
	static StringBuffer natureTags = new StringBuffer();
	static StringBuffer combineTags = new StringBuffer();
	static Properties props = new Properties();
	
	static {
		try {
			props.load(ClassLoader.getSystemResourceAsStream("config.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
//	public static void main(String[] args) throws IOException {
//		String fileIn = props.getProperty("fileIn");
//		String fileOut = props.getProperty("fileOut");
//		combination(fileIn, fileOut);
//	}
	
	public void combination(String fileIn, String fileOut) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(fileIn));
		BufferedWriter bw = new BufferedWriter(new FileWriter(fileOut));
		ArrayList<List<String>> combineResult = new ArrayList<List<String>>();
		ArrayList<String[]> termList = new ArrayList<String[]>();
		ArrayList<String[]> newTermList = null;
		String[] term = new String[3];
		String line = null;
		
		while ((line = br.readLine()) != null) {
			
			String lineinf = line.split("\t")[2];
			String[] infArray = lineinf.substring(2, lineinf.length() - 2).split("\', \'");
			for (String terminf : infArray) {
//				System.out.println(terminf);
				term = terminf.split("\\|");
				natureTags.append(term[1] + ",");
				termList.add(term.clone());
			}
			
			Combine combine  = new Combine();
			newTermList = combine.compress(termList);
			combineResult = combine.combine(newTermList);
			
			for (List<String> result: combineResult) {
				bw.write(line + "\t" + natureTags.substring(0, natureTags.length() - 1));
				for (String inf : result) {
					bw.write("\t" + inf);
				}
				bw.write("\n");
			}

			bzms.setLength(0);
			natureTags.setLength(0);
			combineTags.setLength(0);
			termList.clear();
			combineResult.clear();
			
		}
		
		bw.flush();
		br.close();
		bw.close();
		
		System.out.println("Combination is over!");
	}
}
