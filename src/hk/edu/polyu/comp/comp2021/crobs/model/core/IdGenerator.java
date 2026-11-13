package hk.edu.polyu.comp.comp2021.crobs.model.core;

import java.util.Comparator;

/**
 * Class to generate unique IDs.
 */
public class IdGenerator {
    private int counter = 0;
    private static final int NUM_ID_DIGITS = 4;
    private final String prefix;

    private static final Comparator<String> COMPARATOR = new Comparator<String>() {
        @Override
        public int compare(String a, String b) {
            int lengthComparison = Integer.compare(a.length(), b.length()); // first i compare by length as shorter ones come first
            if (lengthComparison != 0) return lengthComparison;
            return a.compareTo(b);
        }
    };

    /**
     * @param prefix to indicate what type of id it'll be. E.g. "R" to indicate rooms so IDs will be like "R000001".
     */
    public IdGenerator(String prefix) {
        this.prefix = prefix;
    }

    /**
     * @return a unique ID as String
     */
    public String next() {
        return prefix + String.format("%0"+NUM_ID_DIGITS+"d", ++counter);
    }

    /**
     * @return a Comparator to compare which ID comes first
     */
    public static Comparator<String> idOrder() {
        return COMPARATOR;
    }
}
