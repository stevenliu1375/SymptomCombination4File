package com.huiquan.combination;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class Combine {
	
	ArrayList<List<String>> combineResult;
	static List<String> combination;
	
	public Combine() {
		combineResult = new ArrayList<List<String>>();
		combination = new ArrayList<String>();
	}
	
	/**
	 * 先压缩，在分tag树，最后进行组词
	 */
	public ArrayList<List<String>> combine(ArrayList<String[]> newTermList) {
		List<String[]> validTermList = new ArrayList<String[]>();
		List<String[]> termTree = new ArrayList<String[]>();
		
		// 先将“N”“S”抽取出来
		for(String[] term : newTermList) {
			if (!term[1].equals("DI")) {  // DI 不显示
				if (term[2].equals("S")) {
					combination.add(term[1]);
					combination.add(String.valueOf(term[0]));
					combination.add(String.valueOf(term[0]));
					combination.add("S");
					combination.add(String.valueOf(term[0]));
					combineResult.add(new ArrayList<>(combination));
					combination.clear();
				} else if (!term[2].equals("N")) {
					validTermList.add(term);
				} 
			}
		}
		
		if (combineResult.isEmpty() && validTermList.isEmpty()) {
			return combineResult;
		}
		
		for (int i = 0; i < validTermList.size(); i++) {
			// 第一个条件：避免标注错误的时候结尾没有“E”，而导致越界
			while (i + 1 < validTermList.size() && validTermList.get(i)[2].charAt(0) != 'E') {
				termTree.add(validTermList.get(i));
				i++;
			}
			// 第一个条件防止判断越界
			while (i + 1 < validTermList.size() && validTermList.get(i + 1)[2].charAt(0) == 'E') {
				termTree.add(validTermList.get(i));
				i++;
			}
			// 前两个while的第二个条件不符合后，进入该判断；是对每个有效序列子树的最后一个标签判断
			if (validTermList.get(i)[2].charAt(0) == 'E') {
				termTree.add(validTermList.get(i));
			}
			else {
//				System.out.println("结束找不到“E”，当前标注有错...");
//				termTree.clear();
				return combineResult;
			}
			
//			// 打印当前termTree
//			System.out.println("=====当前tree: =====");
//			for (String[] s : termTree) {
//				System.out.println(s[0] + " " + s[1] + " " + s[2]);
//			}
			
			// 对每棵termTree进行组词，并将结果添加到最终结果中
			combineResult.addAll(combineTree(termTree));
			
			termTree.clear();

		}
		return combineResult;
	}
	
	
	/**
	 * 组词前的预处理，相同的Double标签进行压缩合并。
	 * @param termList
	 * @return
	 */
	public ArrayList<String[]> compress(ArrayList<String[]> termList) {
		
		List<String> tags = Arrays.asList("B", "M", "E");
		ArrayList<String[]> newTermList = new ArrayList<String[]>();
		StringBuffer wordBuffer = new StringBuffer();
		StringBuffer tagBuffer = new StringBuffer();
		
		if (termList.isEmpty()) {
			System.out.println("TermList is empty!");
			return newTermList;
		}
		
		for (int i = 0; i < termList.size(); i++) {
			String[] term = termList.get(i);
			// 不需要压缩的先添加
			if (term[2].length() == 1 || !tags.contains(term[2].charAt(1) + "")) {
				newTermList.add(term);
			} else { // 压缩处理
				wordBuffer = new StringBuffer();
				tagBuffer = new StringBuffer();
				if (i + 1 < termList.size()) {
					if (!termList.get(i + 1)[2].equals(term[2])) {  // 与下一个标签进行比较，相同则合并；不同则添加(一定要用equals！！不能用==)
						term[2] = term[2].substring(1, term[2].length());
						newTermList.add(term);
					} else {
						wordBuffer.append(term[0]);
						tagBuffer.append(term[1]);
						while (i + 1 < termList.size() && termList.get(i + 1)[2].equals(term[2])) {
							wordBuffer.append("," + termList.get(i + 1)[0]);
							tagBuffer.append("," + termList.get(i + 1)[1]);
							i++;  // 重复相加后，外层循环需要少遍历一次；否则会出现重复
						}
						term[2] = term[2].substring(1, term[2].length());
						newTermList.add(new String[]{String.valueOf(wordBuffer), String.valueOf(tagBuffer), term[2]});
					}
				} else {
					wordBuffer.append("," + termList.get(i)[0]);
					tagBuffer.append("," + termList.get(i + 1)[1]);
					term[2] = term[2].substring(1, term[2].length());
					newTermList.add(new String[]{String.valueOf(wordBuffer), String.valueOf(tagBuffer), term[2]});
				}
			}
		}
		
		return newTermList;
	}
	
	
	/**
	 * 对已经拆分好的“树”进行组词，一棵树可以有多个路径，组出多个症状
	 * @param newTermList
	 * @return
	 */
	public ArrayList<List<String>> combineTree(List<String[]> termTree) {
		
		ArrayList<List<String>> combineTreeResult = new ArrayList<>();
		Map<String, List<String[]>> mapTagWord = new HashMap<String, List<String[]>>(); // tag-wordlist键值对，一个tag对应n个string[]
		List<String[]> wordList = new ArrayList<String[]>(); // 放置tag对应的值
		Map<String, String[]> mapSimpleTagWord = new HashMap<String, String[]>();  // 对于简单模式B-M-E单独处理
		StringBuffer wordSequence = new StringBuffer();
		StringBuffer tagSequence = new StringBuffer();
		
		short maxRoute = 0;  // tag的最大标签值
		short route = 0; 
		StringBuffer tempTag = new StringBuffer();
		
		// 获得最长路径的值，并添加部分值到mapTagWord中
		for (String[] term : termTree) {
			
			// 对含有“无”开头的否定症状不予显示
			if (term[0].equals("无") && term[1].equals("DE")) {
				return combineTreeResult;
			}
			
			if (term[2].length() > 1) {
				route = Short.parseShort((String.valueOf(term[2].charAt(1))));
				maxRoute = maxRoute > route ? maxRoute : route;
				wordList.add(new String[]{term[0], term[1]});
				mapTagWord.put(term[2], new ArrayList<>(wordList));
				wordList.clear();
			} else {
				mapSimpleTagWord.put(term[2], new String[]{term[0], term[1]});
			}
		}
		
//		// 打印简单模式中的map中的键值对
//		for (Entry<String, String> entry : mapSimpleTagWord.entrySet()) {
//			System.out.println(entry.getKey() + " " + entry.getValue());
//		}
		
		// 对于没有路径标签的B-M-E简单模式的处理
		if (maxRoute == 0) {
			try {
				if (mapSimpleTagWord.containsKey("M")) {
					wordSequence.append(mapSimpleTagWord.get("B")[0]).append(",").append(mapSimpleTagWord.get("M")[0]).append(",").append(mapSimpleTagWord.get("E")[0]);
					tagSequence.append(mapSimpleTagWord.get("B")[1]).append(",").append(mapSimpleTagWord.get("M")[1]).append(",").append(mapSimpleTagWord.get("E")[1]);
					combination.add(tagSequence + "");
					combination.add(String.valueOf(wordSequence).replaceAll(",", ""));
					combination.add(wordSequence + "");
					combination.add("B-M-E");
					combination.add(mapSimpleTagWord.get("B")[0] + "-" + mapSimpleTagWord.get("M")[0] + "-" + mapSimpleTagWord.get("E")[0]);
				} else {
					wordSequence.append(mapSimpleTagWord.get("B")[0]).append(",").append(mapSimpleTagWord.get("E")[0]);
					tagSequence.append(mapSimpleTagWord.get("B")[1]).append(",").append(mapSimpleTagWord.get("E")[1]);
					combination.add(tagSequence + "");
					combination.add(String.valueOf(wordSequence).replaceAll(",", ""));
					combination.add(wordSequence + "");
					combination.add("B-E");
					combination.add(mapSimpleTagWord.get("B")[0] + "-" + mapSimpleTagWord.get("E")[0]);
				}
			} catch (NullPointerException e) {  // 对错误标注序列的处理
//				System.out.println("CombineSequence Error.");
//				for (Entry<String, String[]> entry : mapSimpleTagWord.entrySet()) {
//					System.out.println(entry.getValue()[0] + " " + entry.getValue()[1] + " " + entry.getKey());
//				}
				return combineTreeResult;
			}
			combineTreeResult.add(new ArrayList<>(combination));
			wordSequence.setLength(0);
			tagSequence.setLength(0);
			combination.clear();
			return combineTreeResult;
		}
		
		// 将B1,..,Bn,M1,...Mn,E1,...,En以及它们的值添加到Map中
		for (int i = 0; i < termTree.size(); i++) {
			if (termTree.get(i)[2].length() == 1) {
				for (int j = 1; j <= maxRoute; j++) {
					tempTag.append(termTree.get(i)[2]).append(String.valueOf(j));
					
					if (!mapTagWord.containsKey(String.valueOf(tempTag))) {
						wordList.add(new String[]{termTree.get(i)[0], termTree.get(i)[1]});
						mapTagWord.put(String.valueOf(tempTag), new ArrayList<>(wordList));
						wordList.clear();
					} else {
						mapTagWord.get(String.valueOf(tempTag)).add(new String[]{termTree.get(i)[0], termTree.get(i)[1]});
					}
					
					tempTag.setLength(0);
				}
			}
		}
		
//		 // 打印Map中的值
//		System.out.println("---当前Map中的值: ---");
//		for (Entry<String, List<String[]>> entry : mapTagWord.entrySet()) {
//			System.out.print(entry.getKey() + " ");
//			for (String[] s : entry.getValue()) {
//				for (String ss : s) {
//					System.out.print(ss + " ");
//				}
//			}
//			System.out.println();
//		}
//		System.out.println("---Map打印完毕。---\n");
		
		//termTree的组合结果
		String tagB, tagM, tagE;
		for (int i = 0; i < maxRoute; i++) {
			tagB = "B" + (i + 1);
			tagM = "M" + (i + 1);
			tagE = "E" + (i + 1);
			try {
				
				for (String[] wordB : mapTagWord.get(tagB)) {
					if (mapTagWord.containsKey(tagM)) {
						for (String[] wordM : mapTagWord.get(tagM)) {
							for (String[] wordE : mapTagWord.get(tagE)) {
								wordSequence.append(wordB[0]).append(",").append(wordM[0]).append(",").append(wordE[0]);
								tagSequence.append(wordB[1]).append(",").append(wordM[1]).append(",").append(wordE[1]);
								combination.add(String.valueOf(tagSequence));
								combination.add(String.valueOf(wordSequence).replaceAll(",", ""));
								combination.add(String.valueOf(wordSequence));
								combination.add(tagB + "-" + tagM + "-" + tagE);
								combination.add(wordB[0] + "-" + wordM[0] + "-" + wordE[0]);
								combineTreeResult.add(new ArrayList<>(combination));
								wordSequence.setLength(0);
								tagSequence.setLength(0);
								combination.clear();
							}
						}
					} else {
						for (String[] wordE : mapTagWord.get(tagE)) {
							wordSequence.append(wordB[0]).append(",").append(wordE[0]);
							tagSequence.append(wordB[1]).append(",").append(wordE[1]);
							combination.add(String.valueOf(tagSequence));
							combination.add(String.valueOf(wordSequence).replaceAll(",", ""));
							combination.add(String.valueOf(wordSequence));
							combination.add(tagB + "-" + tagE);
							combination.add(wordB[0] + "-" + wordE[0]);
							combineTreeResult.add(new ArrayList<>(combination));
							wordSequence.setLength(0);
							tagSequence.setLength(0);
							combination.clear();
						}
					}
				}
				
			} catch (NullPointerException e) {
//				System.out.println("\n该标签数中部分标签越界...\n");
//				e.printStackTrace();
				break;
			}
		}
		
		return combineTreeResult;
		
	}
	
	public static void main(String[] args) {
		ArrayList<String[]> termList = new ArrayList<String[]>();
//		termList.add(new String[]{"右侧","P", "B1"});
//		termList.add(new String[]{"noise","P", "B"});
		termList.add(new String[]{"肩部", "O", "B1"});
		termList.add(new String[]{"臀部", "O", "B2"});
		termList.add(new String[]{"疼痛", "S_AD", "M2"});
		termList.add(new String[]{"瘙痒", "S_AD", "M2"});
		termList.add(new String[]{"胸部", "O", "E"});
//		termList.add(new String[]{"腿部", "O", "E2"});
//		termList.add(new String[]{"肌肉", "O_AD", "M"});
//		termList.add(new String[]{"疼痛", "S_AD", "E"});
//		termList.add(new String[]{"酸痛", "S_AD", "E"});
//		termList.add(new String[]{"咳嗽", "S_I", "B"});
//		termList.add(new String[]{"头部", "O", "E"});
//		termList.add(new String[]{"长包", "S_AD", "E"});
//		
		Combine combine = new Combine();
		ArrayList<String[]> newTermList = combine.compress(termList);
		ArrayList<List<String>> combineResult = combine.combine(newTermList);
		
		System.out.println("+++开始打印组词结果：+++");
		for (int i = 0; i < combineResult.size(); i++) {
			for (int j = 0; j < combineResult.get(i).size(); j++) {
				System.out.println(combineResult.get(i).get(j));
			}
			System.out.println("------");
		}
		
	}
	
}
