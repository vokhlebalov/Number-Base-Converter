package converter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;

public class Main {
    private static final int SCALE = 5;

    private static final BufferedReader READER = new BufferedReader(new InputStreamReader(System.in));
    private static final List<Character> DIGITS = new ArrayList<>();
    private static Stage stage = Stage.BASE;
    private static int sourceBase;
    private static int targetBase;

    static {
        for (char i = '0'; i <= '9'; i++) {
            DIGITS.add(i);
        }

        for (char i = 'A'; i <= 'Z'; i++) {
            DIGITS.add(i);
        }
    }

    public static void main(String[] args) throws IOException {
        do {
            if (stage.equals(Stage.BASE)) {
                baseAction();
            } else if (stage.equals(Stage.NUMBER)) {
                numberAction();
                System.out.println();
            }
        } while (!Stage.EXIT.equals(stage));
    }

    private static void numberAction() throws IOException {
        System.out.printf("Enter number in base %d to convert to base %d (To go back type /back) ", sourceBase, targetBase);
        String input = READER.readLine();

        if ("/back".equals(input)) {
            stage = Stage.BASE;
            return;
        }

        if (input.trim().matches("[a-zA-Z\\d]+(\\.[a-zA-Z\\d]+)?")) {
            String numberToConvert = input.toUpperCase(Locale.ROOT);
            BigDecimal decimal = convertToDecimal(numberToConvert, sourceBase);
            String convertedNumber = convertFromDecimal(decimal, targetBase);
            System.out.printf("Conversion result: %s\n", convertedNumber);
        }
    }

    private static void baseAction() throws IOException{
        System.out.print("Enter two numbers in format: {source base} {target base} (To quit type /exit) ");
        String input = READER.readLine();
        if ("/exit".equals(input)) {
            stage = Stage.EXIT;
            return;
        }

        if (input.trim().matches("\\d+\\s+\\d+")) {
            String[] readBuffer = input.split("\\s+");
            sourceBase = Integer.parseInt(readBuffer[0]);
            targetBase = Integer.parseInt(readBuffer[1]);

            stage = Stage.NUMBER;
        }
    }

    private static BigDecimal convertToDecimal(String number, int radix) {
        BigDecimal result = BigDecimal.ZERO;
        String integerPart = number, fractionalPart = "";

        if (number.contains(".")) {
            integerPart = number.substring(0, number.indexOf('.'));
            fractionalPart = number.substring(number.indexOf('.') + 1);
        }

        for (int i = 0; i < integerPart.length(); i++) {
            char digit = integerPart.charAt(integerPart.length() - i - 1);
            result = result.add(
                    BigDecimal
                            .valueOf(radix)
                            .pow(i)
                            .multiply(BigDecimal.valueOf(DIGITS.indexOf(digit)))
            );
        }

        for (int i = 0; i < fractionalPart.length(); i++) {
            char digit = fractionalPart.charAt(i);
            result = result.add(
                    BigDecimal
                            .valueOf(DIGITS.indexOf(digit))
                            .divide(
                                    BigDecimal.valueOf(radix).pow(i + 1),
                                    SCALE + 1,
                                    RoundingMode.DOWN
                            )
            );
        }

        return result;
    }

    private static String convertFromDecimal(BigDecimal number, int radix) {
        StringBuilder result = new StringBuilder();

        BigInteger integerPart = number.toBigInteger();
        BigDecimal fractionalPart = number.remainder(BigDecimal.ONE);

        BigInteger intRadix = BigInteger.valueOf(radix);
        BigDecimal decRadix = BigDecimal.valueOf(radix);

        while (integerPart.compareTo(BigInteger.ZERO) > 0) {
            result.append(DIGITS.get(integerPart.remainder(intRadix).intValue()));
            integerPart = integerPart.divide(intRadix);
        }

        result.reverse();
        if (result.length() == 0) {
            result.append('0');
        }

        if (fractionalPart.equals(BigDecimal.ZERO)) {
            return result.toString();
        }

        result.append('.');

        int scale = 0;

        while (/*fractionalPart.compareTo(BigDecimal.ZERO) > 0 &&*/ scale < SCALE) {
            result.append(DIGITS.get(fractionalPart.multiply(decRadix).intValue()));
            fractionalPart = fractionalPart.multiply(decRadix).remainder(BigDecimal.ONE);
            scale++;
        }

        return result.toString();
    }
}
