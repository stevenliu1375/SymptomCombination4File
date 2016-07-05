package com.huiquan.combination;

import java.util.ArrayList;
import java.util.List;

public class TestGetTree {
	private static ArrayList<String> combination = new ArrayList<String>();
	private static ArrayList<List<String>> combineResult = new ArrayList<List<String>>();

	public static void main(String[] args) {
		ArrayList<String[]> termList = new ArrayList<String[]>();
		termList.add(new String[]{"右侧", "P", "B"});
		termList.add(new String[]{"肩部", "O", "B"});
		termList.add(new String[]{"臀部", "O", "MM"});
		termList.add(new String[]{"疼痛", "S_AD", "MM"});
		termList.add(new String[]{"瘙痒", "S_AD", "E"});
		termList.add(new String[]{"咳嗽", "S_I", "S"});
		termList.add(new String[]{"左手", "O", "B"});
		termList.add(new String[]{"头部", "O", "B"});
		termList.add(new String[]{"长包", "S_AD", "E"});
		
		combine(termList);
//		for (int i = 0; i < combineResult.size(); i++) {
//			for (int j = 0; j < combineResult.get(i).size(); j++) {
//				System.out.print(combineResult.get(i).get(j));
//			}
//			System.out.println();
//		}
		
	}
	
	/**
	 * 先压缩，在分tag树，最后进行组词
	 */
	static ArrayList<List<String>> combine(ArrayList<String[]> newTermList) {
		List<String[]> validTermList = new ArrayList<String[]>();
		List<String[]> termTree = new ArrayList<String[]>();
		
		// 先将“N”“S”抽取出来
		for(String[] term : newTermList) {
			if (term[2].equals("S")) {
				combination.add("S");
				combination.add(String.valueOf(term[0]));
				combineResult.add(new ArrayList<>(combination));
				combination.clear();
			} else if (!term[2].equals("N")) {
				validTermList.add(term);
			} 
//			bzms.append(term[0]);
		}

		if (combineResult.isEmpty() && validTermList.isEmpty()) {
			return combineResult;
		}
//		System.out.println(bzms);
		
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
				System.out.println("结束找不到“E”，当前标注有错...");
				termTree.clear();
			}
			
			// 打印当前termTree
			for (String[] s : termTree) {
				System.out.println(s[0] + " " + s[1] + " " + s[2]);
			}
			System.out.println("---------");
			
			// 对每棵termTree进行组词，并将结果添加到最终结果中
//			combineResult.addAll(combineTree(termTree));
			
			termTree.clear();

		}
		return combineResult;
	}
}
