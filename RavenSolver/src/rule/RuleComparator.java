package rule;

import java.util.Comparator;

public class RuleComparator implements Comparator<Rule> {
    
    // Returns < 0 if r1 is better than r2, > 0 if r1 is worse than r2,
    // and 0 if both have the same quality.
    public int compare(Rule r1, Rule r2) {
        return (r1.cost - r2.cost);
    }

}
