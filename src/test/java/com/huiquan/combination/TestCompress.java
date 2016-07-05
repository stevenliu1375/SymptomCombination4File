package com.huiquan.combination;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class TestCompress {
	public static void main(String[] args) {
		
		ArrayList<String[]> termList = new ArrayList<String[]>();
		termList.add(new String[]{"上方", "P", "BB"});
		termList.add(new String[]{"右侧", "P", "BB"});
		termList.add(new String[]{"肩部", "O", "BB"});
		termList.add(new String[]{"臀部", "O", "M"});
		termList.add(new String[]{"疼痛", "S_AD", "EE"});
		termList.add(new String[]{"瘙痒", "S_AD", "EE"});
		termList.add(new String[]{"皮疹", "S_AD", "BB"});
		termList.add(new String[]{"溃烂", "S_AD", "E"});
		
		ArrayList<String[]> newTermList = compress(termList);;
		
		for (int i = 0; i < newTermList.size(); i++) {
			for (int j = 0; j < newTermList.get(i).length; j++) {
				System.out.println(newTermList.get(i)[j]);
//				System.out.println(newTermList.get(i)[1]);
//				System.out.println(newTermList.get(i)[2]);
				
			}
		}
	}

	/**
	 * 组词前的预处理，相同的Double标签进行压缩合并。
	 * @param termList
	 * @return
	 */
	static ArrayList<String[]> compress(ArrayList<String[]> termList) {
		
		List<String> tags = Arrays.asList("B", "M", "E");
		ArrayList<String[]> newTermList = new ArrayList<String[]>();
		StringBuffer wordBuffer = null;
		StringBuffer tagBuffer = null;
		
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
					if (!termList.get(i + 1)[2].equals(term[2])) {
						term[2] = term[2].substring(1, term[2].length());
						newTermList.add(term);
					} else {
						wordBuffer.append(term[0]);
						tagBuffer.append(term[1]);
						while (i + 1 < termList.size() && termList.get(i + 1)[2].equals(term[2])) {
							wordBuffer.append(termList.get(i + 1)[0]);
							tagBuffer.append("," + termList.get(i + 1)[1]);
							i++;  // 重复相加后，外层循环需要少遍历一次；否则会出现重复
						}
						term[2] = term[2].substring(1, term[2].length());
						newTermList.add(new String[]{String.valueOf(wordBuffer), String.valueOf(tagBuffer), term[2]});
					}
				} else {
					wordBuffer.append(termList.get(i)[0]);
					tagBuffer.append("," + termList.get(i + 1)[1]);
					term[2] = term[2].substring(1, term[2].length());
					newTermList.add(new String[]{String.valueOf(wordBuffer), String.valueOf(tagBuffer), term[2]});
				}
			}
		}
		
		return newTermList;
	}
	
}
