package com.huiquan.combination;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class TestCombineTree {
	public static void main(String[] args) {
		
		List<String[]> termList = new ArrayList<String[]>();
//		termList.add(new String[]{"上方", "B"});
//		termList.add(new String[]{"右侧", "M"});
//		termList.add(new String[]{"肿", "E"});
		
		
		termList.add(new String[]{"肩部", "B1"});
		termList.add(new String[]{"臀部", "B2"});
		termList.add(new String[]{"疼痛", "M"});
		termList.add(new String[]{"瘙痒", "E"});
		termList.add(new String[]{"皮疹", "E"});
//		termList.add(new String[]{"溃烂", "E4"});
		
		ArrayList<List<String>> newTermList = combineTree(termList);
		
//		for (int i = 0; i < newTermList.size(); i++) {
//			System.out.println(newTermList.get(i).get(0));
//			System.out.println(newTermList.get(i).get(1));
//		}
	}
	
	/**
	 * 对已经拆分好的“树”进行组词
	 * @param newTermList
	 * @return
	 */
	static ArrayList<List<String>> combineTree(List<String[]> termList) {
		ArrayList<List<String>> combineResult = new ArrayList<List<String>>();
		ArrayList<String> wordList = new ArrayList<String>();
		Map<String, List<String>> mapTagWord = new HashMap<>();
		Map<String, String> mapSimpleTagWord = new HashMap<>();
		List<String> tempCombination = new ArrayList<String>(); 
//		List<String> combination = new ArrayList<String>();
		
		short maxRoute = 0;
		short route = 0; 
		StringBuffer tempTag = new StringBuffer();
		
		// 获得最长路径的值，并添加部分值到mapTagWord中
		for (String[] s : termList) {
			if (s[1].length() > 1) {
				route = Short.parseShort((String.valueOf(s[1].charAt(1))));
				maxRoute = maxRoute > route ? maxRoute : route;
				wordList.add(s[0]);
				mapTagWord.put(s[1], new ArrayList<>(wordList));
				wordList.clear();  // 定义wordList是因为，mapTagWord取wordList的地址，clear之后，Map的value也清空了
			} else {
				mapSimpleTagWord.put(s[1], s[0]);
			}
		}
		
		// 对于无需指明路径，只有B-M-E简单标签的处理
		if (maxRoute == 0) {
			if (mapSimpleTagWord.containsKey("M")) {
				System.out.println("B-M-E");
				System.out.println(mapSimpleTagWord.get("B") + mapSimpleTagWord.get("M") + mapSimpleTagWord.get("E"));
			} else {
				System.out.println("B-E");
				System.out.println(mapSimpleTagWord.get("B") + mapSimpleTagWord.get("E"));
			}
		}
		
		
		// 将B1,..,Bn,M1,...Mn,E1,...,En以及它们的值添加到Map中
		for (int i = 0; i < termList.size(); i++) {
			
			if (termList.get(i)[1].length() == 1) {
				for (int j = 1; j <= maxRoute; j++) {
					tempTag.append(termList.get(i)[1]).append(String.valueOf(j));
					
					if (!mapTagWord.containsKey(String.valueOf(tempTag))) {
						wordList.add(termList.get(i)[0]);
						mapTagWord.put(String.valueOf(tempTag), new ArrayList<>(wordList));
						wordList.clear();
					} else {
						mapTagWord.get(String.valueOf(tempTag)).add(termList.get(i)[0]);
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
		
		//最终输出
		String tagB, tagM, tagE;
		for (int i = 0; i < maxRoute; i++) {
			tagB = "B" + String.valueOf(i + 1);
			tagM = "M" + String.valueOf(i + 1);
			tagE = "E" + String.valueOf(i + 1);
			try {
				for (String wordB : mapTagWord.get(tagB)) {
					if (mapTagWord.containsKey(tagM)) {
						for (String wordM : mapTagWord.get(tagM)) {
							for (String wordE : mapTagWord.get(tagE)) {
								System.out.println(tagB + "-" + tagM + "-" + tagE);
								System.out.println(wordB + wordM + wordE);
								tempCombination.add(tagB + "-" + tagM + "-" + tagE);
								tempCombination.add(wordB + wordM + wordE);
								combineResult.add(tempCombination);
							}
						}
					} else {
						for (String wordE : mapTagWord.get(tagE)) {
							System.out.println(tagB + "-" + tagE);
							System.out.println(wordB + wordE);
							tempCombination.add(tagB + "-" + tagE);
							tempCombination.add(wordB + wordE);
							combineResult.add(tempCombination);
						}
					}
				}
			} catch (NullPointerException e) {
				System.out.println("\n该标签数中部分标签越界...\n");
				break;
			}
		}
		
		return combineResult;
		
	}
	
}
