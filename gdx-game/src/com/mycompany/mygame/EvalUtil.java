package com.mycompany.mygame;
import java.nio.file.attribute.*;
import java.util.*;

/**
 * The Eval class.
 *
 * <pre>
 * Grammar:
 *     expression = term | expression `+` term | expression `-` term
 *     term = factor | term `*` factor | term `/` factor
 *     factor = `+` factor | `-` factor | `(` expression `)` | number | functionName factor | factor `^` factor | x
 * </pre>
 *
 * @author Michal Spimr
 * inspired by https://stackoverflow.com/questions/3422673/how-to-evaluate-a-math-expression-given-in-string-form/26227947#26227947
 */
public class EvalUtil
{

    private static final Vector<String> SUPPORTED_FUNCTIONS = new Vector<String>();
    {
        SUPPORTED_FUNCTIONS.add("sqrt");
        SUPPORTED_FUNCTIONS.add("sin");
        SUPPORTED_FUNCTIONS.add("cos");
        SUPPORTED_FUNCTIONS.add("tan");
        SUPPORTED_FUNCTIONS.add("abs");
        SUPPORTED_FUNCTIONS.add("log");
    }

    private String expression;

    private double x;

    private int position = -1;

    private int ch;

    private  EvalUtil(String expression, double x)
    {
        this.expression = expression;
        this.x = x;
    }

    private void nextChar()
    {
        position++;
        ch = position < expression.length() ? expression.charAt(position) : -1;
    }

    private boolean eat(int charToEat)
    {
        boolean result = false;
        while (ch == ' ')
        {
            nextChar();
        }
        if (ch == charToEat)
        {
            nextChar();
            result = true;
        }
        return result;
    }

    private double parse() throws SyntaxException
    {
        nextChar();
        double result = parseExpression();
        if (position < expression.length())
        {
            throw new UnexpectedCharException(ch, position);
        }
        return result;
    }

    private double parseExpression() throws SyntaxException
    {
        double result = parseTerm();
        while (true)
        {
            // addition
            if (eat('+'))
            {
                result += parseTerm();
            }

            // subtraction
            else if (eat('-'))
            {
                result -= parseTerm();
            }

            // term
            else
            {
                break;
            }
        }
        return result;
    }

    private double parseTerm() throws SyntaxException
    {
        double result = parseFactor();
        while (true)
        {
            // multiplication
            if (eat('*'))
            {
                result *= parseFactor();
            }

            // division
            else if (eat('/'))
            {
                result /= parseFactor();
            }
            else
            {
                break;
            }
        }
        return result;
    }

    private double parseFactor() throws SyntaxException
    {
        double result;
        int startPos = this.position;

        // unary plus
        if (eat('+'))
        {
            result = parseFactor();
        }

        // unary minus
        else if (eat('-'))
        {
            result = -parseFactor();
        }

        // parentheses
        else
        {
            if (eat('('))
            {
                result = parseExpression();
                if (!eat(')'))
                {
                    throw new SyntaxException("Please close the bracket \")\" at position " + position);
                }
            }

            // numbers
            else if (ch >= '0' && ch <= '9' || ch == '.')
            {
                while (ch >= '0' && ch <= '9' || ch == '.')
                {
                    nextChar();
                }
                String numberAsString = expression.substring(startPos, this.position);
                try
                {
                    result = Double.parseDouble(numberAsString);
                }
                catch (NumberFormatException ex)
                {
                    throw new UnexpectedCharException(ch, position);
                }
            }

            // param X
            else if (eat('x'))
            {
                result = x;
            }

            // functions
            else if (ch >= 'a' && ch <= 'z')
            {
                while (ch >= 'a' && ch <= 'z')
                {
                    nextChar();
                }


                String functionName = expression.substring(startPos, this.position);
                if (!SUPPORTED_FUNCTIONS.contains(functionName))
                {
                    throw new UnknownFunctionException(functionName, position);
                }
                
                if (eat('('))
                {
                    result = parseExpression();
                    if (!eat(')'))
                    {
                        throw new SyntaxException("Please close the bracket \")\" at position " + position);
                    }
                }
                else
                {
                    throw new SyntaxException("Please open the backet \"(\" at position " + position);
                }
                
                if (functionName.equals("sqrt"))
                {
                    result = Math.sqrt(result);
                }
                else if (functionName.equals("sin"))
                {
                    result = Math.sin(result);
                }
                else if (functionName.equals("cos"))
                {
                    result = Math.cos(result);
                }
                else if (functionName.equals("tan"))
                {
                    result = Math.tan(result);
                }
                else if (functionName.equals("abs"))
                {
                    result = Math.abs(result);
                }
                else if (functionName.equals("log"))
                {
                    result = Math.log10(result);
                }
                else
                {
                    throw new UnknownFunctionException(functionName, position);
                }
            }
            else
            {
                throw new UnexpectedCharException(ch, position);
            }

            // exponentiation
            if (eat('^'))
            {
                result = Math.pow(result, parseFactor());
            }
        }

        return result;
    }

    public static class SyntaxException extends Exception
    {
        public SyntaxException(String message)
        {
            super(message);
        }
    }
    public static class UnexpectedCharException extends SyntaxException
    {
        public UnexpectedCharException(int ch, int position)
        {
            super(ch == -1 ? //
                  "Please complete the expression..." : //
                  "Unexpected \"" + (char) ch + "\" at position " + position);
        }
    }

    public static class UnknownFunctionException extends SyntaxException
    {
        public UnknownFunctionException(String functionName, int position)
        {
            super("Unknown function \"" + functionName + "\" at position " + position);
        }
    }

    private static double evalEx(String expression, double xParam) throws SyntaxException
    {
        double result;
        EvalUtil e = new EvalUtil(expression, xParam);
        result = e.parse();
        return result;
    }

    private static float evalEx(String expression, float xParam) throws SyntaxException
    {
        float result;
        EvalUtil e = new EvalUtil(expression, xParam);
        result = (float) e.parse();
        return result;
    }

    public static double eval(String expression, double xParam)
    {
        double result;
        try
        {
            result = evalEx(expression, xParam);
        }
        catch (SyntaxException ex)
        {
            result = Double.NaN;
        }
        return result;
    }

    public static float eval(String expression, float xParam)
    {
        float result;
        try
        {
            result = evalEx(expression, xParam);
        }
        catch (SyntaxException ex)
        {
            result = Float.NaN;
        }
        return result;
    }

    public static void check(String expression) throws SyntaxException
    {
        evalEx(expression, 0d);
    }

    public static void main(String[] args)
    {
        String expr;
        expr = "( (4 - 2^3 + 1) * -sqrt(3*3+4*4) ) / x";
        double x = 2;
        System.out.println("x=" + x);
        System.out.println("y=" + expr + "=" + eval(expr, x));
    }

}
