package com.denied403.Hardcourse.Chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Shuffler {
    public static String shuffleWord(String word){
        List<Character> list = new ArrayList<Character>();
        for (char c : word.toCharArray()){
            list.add(c);
        }
        Collections.shuffle(list);
        StringBuilder builder = new StringBuilder();
        for(char c : list){
            builder.append(c);
        }
        String shuffledWord = builder.toString();
        if (shuffledWord.equalsIgnoreCase(word)) {
            return shuffleWord(word);
        }
        return shuffledWord;
    }
}
