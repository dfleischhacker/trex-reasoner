package de.krkm.patterndebug.booleanexpressions;

import java.util.Iterator;
import java.util.Set;

/**
 * Provides methods to flatten and minimize boolean expressions.
 */
public class ExpressionMinimizer {
    /**
     * Determines and returns the DNF for the given expression
     * @param e1 expression to flatten
     * @param e2 expression to flatten
     * @return flattened equivalent expression
     */
    public static OrExpression flatten(OrExpression e1, OrExpression e2) {
        OrExpression res = new OrExpression();
        for (AndExpression and1 : e1.getExpressions()) {
            for (AndExpression and2 : e2.getExpressions()) {
                AndExpression newExp = new AndExpression(and1.getExpressions());
                newExp.getExpressions().addAll(and2.getExpressions());
                res.getExpressions().add(newExp);
            }
        }

        minimize(res);

        return res;
    }

    /**
     * Minimizes the given OrExpression
     *
     * @param expr expression to minimize
     */
    public static void minimize(OrExpression expr) {
        Set<AndExpression> expressionSet = expr.getExpressions();
        AndExpression[] expressionArray = expressionSet.toArray(new AndExpression[expressionSet.size()]);

        Iterator<AndExpression> it = expressionSet.iterator();
        while (it.hasNext()) {
            AndExpression next =  it.next();
            for (int i = 0; i < expressionArray.length; i++) {
                if (expressionArray[i] == next) {
                    continue;
                }
                if (next.isAbsorbedBy(expressionArray[i])) {
                    it.remove();
                    expressionArray = expressionSet.toArray(new AndExpression[expressionSet.size()]);
                    break;
                }
            }
        }
    }

    public static OrExpression or(AndExpression... expressions) {
        return new OrExpression(expressions);
    }

    public static AndExpression and(Literal... literals) {
        return new AndExpression(literals);
    }

    public static Literal literal(String name) {
        return new Literal(name);
    }

    public static void main(String[] args) {
        OrExpression o1 = or(and(literal("A"), literal("B"), literal("C")), and(literal("I"), literal("H"), literal("G")));
        OrExpression o2 = or(and(literal("D"), literal("E")));

        System.out.println(o1);
        System.out.println(o2);
        System.out.println(flatten(o1, o2));

        OrExpression o3 = or(and(literal("A")), and(literal("A"),literal("D"),literal("E")), and(literal("A"), literal("B"), literal("C")) , and(literal("D"), literal("E")));
        System.out.println(o3);
        minimize(o3);
        System.out.println(o3);
    }
}
