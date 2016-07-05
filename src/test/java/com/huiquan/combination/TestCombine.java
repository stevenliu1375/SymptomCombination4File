package com.huiquan.combination;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class TestCombine {
	
	static ArrayList<List<String>> combineResult = new ArrayList<List<String>>();
	static List<String> combination = new ArrayList<String>();
	static StringBuffer bzms = new StringBuffer();
	
	public static void main(String[] args) {
		ArrayList<String[]> termList = new ArrayList<String[]>();
		termList.add(new String[]{"右侧", "BB"});
		termList.add(new String[]{"部", "BB"});
		termList.add(new String[]{"肩部", "BB"});
		termList.add(new String[]{"臀部", "M"});
		termList.add(new String[]{"疼痛", "E"});
		termList.add(new String[]{"瘙痒", "N"});
		termList.add(new String[]{"咳嗽", "S"});
		termList.add(new String[]{"头部", "B1"});
		termList.add(new String[]{"长包", "E"});
		
		ArrayList<String[]> newTermList = compress(termList);
		combineResult = combine(newTermList);
		
		for (int i = 0; i < combineResult.size(); i++) {
			System.out.println(combineResult.get(i).get(0) + " " + combineResult.get(i).get(1));
		}
		
	}
	
	/**
	 * 先压缩，在分tag树，最后进行组词
	 */
	static ArrayList<List<String>> combine(ArrayList<String[]> newTermList) {
		List<String[]> validTermList = new ArrayList<String[]>();
		List<String[]> termTree = new ArrayList<String[]>();
		
		// 先将“N”“S”抽取出来
		for(String[] term : newTermList) {
			if (term[1].equals("S")) {
				combination.add("S");
				combination.add(String.valueOf(term[0]));
				combineResult.add(new ArrayList<>(combination));
				combination.clear();
			} else if (!term[1].equals("N")) {
				validTermList.add(term);
			} 
			bzms.append(term[0]);
		}
		
		if (combineResult.isEmpty() && validTermList.isEmpty()) {
			return combineResult;
		}
//		System.out.println(bzms);
		
		for (int i = 0; i < validTermList.size(); i++) {
			// 第一个条件：避免标注错误的时候结尾没有“E”，而导致越界
			while (i + 1 < validTermList.size() && validTermList.get(i)[1].charAt(0) != 'E') {
				termTree.add(validTermList.get(i));
				i++;
			}
			// 第一个条件防止判断越界
			while (i + 1 < validTermList.size() && validTermList.get(i + 1)[1].charAt(0) == 'E') {
				termTree.add(validTermList.get(i));
				i++;
			}
			// 前两个while的第二个条件不符合后，进入该判断；是对每个有效序列子树的最后一个标签判断
			if (validTermList.get(i)[1].charAt(0) == 'E') {
				termTree.add(validTermList.get(i));
			}
			else {
				System.out.println("结束找不到“E”，当前标注有错...");
				termTree.clear();
			}
			
//			// 打印当前termTree
//			for (String[] s : termTree) {
//				System.out.println(s[0] + " " + s[1]);
//			}
//			System.out.println("---------");
			
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
	static ArrayList<String[]> compress(ArrayList<String[]> termList) {
		
		List<String> tags = Arrays.asList("B", "M", "E");
		ArrayList<String[]> newTermList = new ArrayList<String[]>();
		StringBuffer sb = null;
		
		if (termList.isEmpty()) {
			System.out.println("TermList is empty!");
			return newTermList;
		}
		
		for (int i = 0; i < termList.size(); i++) {
			String[] term = termList.get(i);
			// 不需要压缩的先添加
			if (term[1].length() == 1 || !tags.contains(term[1].charAt(1) + "")) {
				newTermList.add(term);
			} else { // 压缩处理
				sb = new StringBuffer();
				if (i + 1 < termList.size()) {
					if (termList.get(i + 1)[1] != term[1]) {
						term[1] = term[1].substring(1, term[1].length());
						newTermList.add(term);
					} else {
						sb.append(term[0]);
						while (i + 1 < termList.size() && termList.get(i + 1)[1] == term[1]) {
							sb.append(termList.get(i + 1)[0]);
							i++;  // 重复相加后，外层循环需要少遍历一次；否则会出现重复
						}
						term[1] = term[1].substring(1, term[1].length());
						newTermList.add(new String[]{String.valueOf(sb), term[1]});
					}
				} else {
					sb.append(termList.get(i)[0]);
					term[1] = term[1].substring(1, term[1].length());
					newTermList.add(new String[]{String.valueOf(sb), term[1]});
				}
			}
		}
		
		return newTermList;
	}
	
	
	/**
	 * 对已经拆分好的“树”进行组词
	 * @param newTermList
	 * @return
	 */
	static ArrayList<List<String>> combineTree(List<String[]> termTree) {
		
		ArrayList<List<String>> combineTreeResult = new ArrayList<>();
		ArrayList<String> wordList = new ArrayList<String>(); // 放置tag对应的值
		Map<String, List<String>> mapTagWord = new HashMap<>(); // tag-wordlist键值对，一个tag对应n个值
		Map<String, String> mapSimpleTagWord = new HashMap<String, String>();  // 对于简单模式B-M-E单独处理 
		
		short maxRoute = 0;  // tag的最大标签值
		short route = 0; 
		StringBuffer tempTag = new StringBuffer();
		
		// 获得最长路径的值，并添加部分值到mapTagWord中
		for (String[] term : termTree) {
			if (term[1].length() > 1) {
				route = Short.parseShort((String.valueOf(term[1].charAt(1))));
				maxRoute = maxRoute > route ? maxRoute : route;
				wordList.add(term[0]);
				mapTagWord.put(term[1], new ArrayList<>(wordList));
				wordList.clear();
			} else {
				mapSimpleTagWord.put(term[1], term[0]);
			}
		}
		
//		// 打印简单模式中的map中的键值对
//		for (Entry<String, String> entry : mapSimpleTagWord.entrySet()) {
//			System.out.println(entry.getKey() + " " + entry.getValue());
//		}
		
		// 对于没有路径标签的B-M-E简单模式的处理
		if (maxRoute == 0) {
			if (mapSimpleTagWord.containsKey("M")) {
				combination.add("B-M-E");
				combination.add(mapSimpleTagWord.get("B") + mapSimpleTagWord.get("M") + mapSimpleTagWord.get("E"));
			} else {
				combination.add("B-E");
				combination.add(mapSimpleTagWord.get("B") + mapSimpleTagWord.get("E"));
			}
			combineTreeResult.add(new ArrayList<>(combination));
			combination.clear();
			return combineTreeResult;
		}
		
		
		// 将B1,..,Bn,M1,...Mn,E1,...,En以及它们的值添加到Map中
		for (int i = 0; i < termTree.size(); i++) {
			if (termTree.get(i)[1].length() == 1) {
				for (int j = 1; j <= maxRoute; j++) {
					tempTag.append(termTree.get(i)[1]).append(String.valueOf(j));
					
					if (!mapTagWord.containsKey(String.valueOf(tempTag))) {
						wordList.add(termTree.get(i)[0]);
						mapTagWord.put(String.valueOf(tempTag), new ArrayList<>(wordList));
						wordList.clear();
					} else {
						mapTagWord.get(String.valueOf(tempTag)).add(termTree.get(i)[0]);
					}
					
					tempTag.setLength(0);
				}
			}
		}
		
//		 // 打印Map中的值
//		for (Entry<String, List<String>> entry : mapTagWord.entrySet()) {
//			System.out.print(entry.getKey() + " ");
//			for (String s : entry.getValue()) {
//				System.out.print(s + " ");
//			}
//			System.out.println();
//		}
		
		//termTree的组合结果
		String tagB, tagM, tagE;
		for (int i = 0; i < maxRoute; i++) {
			tagB = "B" + (i + 1);
			tagM = "M" + (i + 1);
			tagE = "E" + (i + 1);
			try {
				for (String wordB : mapTagWord.get(tagB)) {
					if (mapTagWord.containsKey(tagM)) {
						for (String wordM : mapTagWord.get(tagM)) {
							for (String wordE : mapTagWord.get(tagE)) {
//								System.out.println(tagB + "-" + tagM + "-" + tagE);
//								System.out.println(wordB + wordM + wordE);
								combination.add(tagB + "-" + tagM + "-" + tagE);
								combination.add(wordB + wordM + wordE);
								combineTreeResult.add(new ArrayList<>(combination));
							}
						}
					} else {
						for (String wordE : mapTagWord.get(tagE)) {
//							System.out.println(tagB + "-" + tagE);
//							System.out.println(wordB + wordE);
							combination.add(tagB + "-" + tagE);
							combination.add(wordB + wordE);
							combineTreeResult.add(new ArrayList<>(combination));
						}
					}
				}
			} catch (NullPointerException e) {
				System.out.println("\n该标签数中部分标签越界...\n");
				break;
			}
			combination.clear();
		}
		
		return combineTreeResult;
		
	}
}
