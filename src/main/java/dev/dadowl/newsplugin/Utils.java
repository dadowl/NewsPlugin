package dev.dadowl.newsplugin;

import java.util.ArrayList;

public class Utils {
    public static ArrayList<String> splitText(String text, int perPage) {
        String arrWords[] = text.split(" ");
        ArrayList<String> arrPhrases = new ArrayList<>();

        StringBuilder stringBuffer = new StringBuilder();
        int cnt = 0;
        int index = 0;
        int length = arrWords.length;

        while (index != length) {
            if (cnt + arrWords[index].length() <= perPage) {
                cnt += arrWords[index].length() + 1;
                stringBuffer.append(arrWords[index]).append(" ");
                index++;
            } else {
                arrPhrases.add(stringBuffer.toString());
                stringBuffer = new StringBuilder();
                cnt = 0;
            }
        }

        if (stringBuffer.length() > 0) {
            arrPhrases.add(stringBuffer.toString());
        }

        return arrPhrases;
    }
}
