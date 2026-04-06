package com.denied403.Hardcourse.Chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Shuffler {
    public static String shuffleWord(String word) {
        List<Character> list = new ArrayList<>();
        for (char c : word.toCharArray()) list.add(c);

        String shuffled;
        int attempts = 0;
        do {
            Collections.shuffle(list);
            StringBuilder sb = new StringBuilder();
            for (char c : list) sb.append(c);
            shuffled = sb.toString();
            attempts++;
        } while (shuffled.equalsIgnoreCase(word) && attempts < 20);

        return shuffled;
    }
}
