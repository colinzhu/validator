package colinzhu.validator;


import lombok.*;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Validator<T> {

    /**
     * <pre>
     * Usage:
     * 1. Define a new Validator by adding rules
     * 2a. To create a new rule, provide:
     *    a. method to get a value
     *    b. method to check the value
     *    c. err message in case check returns false
     * 2b. To create a new rule, provide:
     *    a. method to get a value
     *    b. a Checker
     *  3. Call the validate method with actual object
     *  </pre>
     * @param args
     */

    @Data
    @AllArgsConstructor
    private static class Rule<T, R> {
        private Function<T, R> valueGetter;
        private Predicate<R> checker;
        private String errMsg;
        public String validate(T t) {
            R value = valueGetter.apply(t);
            return checker.test(value) ? null : errMsg + "[" + value + "]";
        }
    }

    private List<Rule<T,?>> rules = new ArrayList<>();
    public <R> Validator addRule(Function<T, R> valueGetter, Predicate<R> checker, String errMsg) {
        rules.add(new Rule<>(valueGetter, checker, errMsg));
        return this;
    }
    public <R> Validator addRule(Function<T, R> valueGetter, Checker<R> checker) {
        rules.add(new Rule<>(valueGetter, checker, checker.getErrMsg()));
        return this;
    }

    public void validate(T t) {
        String errMsg = rules.stream().map(rule -> rule.validate(t)).filter(Objects::nonNull).collect(Collectors.joining(", "));
        if (!errMsg.isEmpty()) {
            throw new ValidationException(errMsg);
        }
    }
    public static <R> boolean nonNullMaxLen(R value, int maxLen) {
        return Objects.nonNull(value) && String.valueOf(value).length() <= maxLen;
    }

    public static <R> NotNullChecker<R> notNull(String fieldName) {
        return new NotNullChecker<>(fieldName);
    }

    public static <R> SizeChecker<R> size(String fieldName, int size) {
        return new SizeChecker<>(fieldName, size, size);
    }
    public static <R> SizeChecker<R> minSize(String fieldName, int min) {
        return new SizeChecker<>(fieldName, min, null);
    }
    public static <R> SizeChecker<R> maxSize(String fieldName, int max) {
        return new SizeChecker<>(fieldName, null, max);
    }
    public static <R> ValidValuesChecker<R> validValues(String fieldName, List<R> validValues) {
        return new ValidValuesChecker<>(fieldName, validValues);
    }

    public static class ValidationException extends RuntimeException {
        public ValidationException(String message) {
            super(message);
        }
    }

    public static abstract class Checker<R> implements Predicate<R>{
        protected String fieldName;
        public Checker(String fieldName) {
            this.fieldName = fieldName;
        }
        public String getErrMsg() {
            return fieldName + " is invalid";
        }
        @Override
        public abstract boolean test(R r);
    }

    private static class SizeChecker<R> extends Checker<R> {
        private Integer min;
        private Integer max;

        public SizeChecker(String fieldName, Integer min, Integer max) {
            super(fieldName);
            this.min = min;
            this.max = max;
        }

        @Override
        public boolean test(R value) {
            if (value == null) {
                return true;
            }
            if (min != null && max != null) {
                return value == null ? true : String.valueOf(value).length() >= min && String.valueOf(value).length() <= max;
            }
            if (min != null) {
                return value == null ? true : String.valueOf(value).length() >= min;
            }
            if (max != null) {
                return value == null ? true : String.valueOf(value).length() <= max;
            }
            throw new IllegalStateException("invalid checker for field: " + fieldName);
        }
        @Override
        public String getErrMsg() {
            if (min != null && max != null && min.equals(max)) {
                return fieldName + " size should be " + min;
            } else if (min != null && max != null) {
                return fieldName + " size should between " + min + " and " + max;
            } else if (min != null) {
                return fieldName + " size should >= " + min;
            } else if (max != null) {
                return fieldName + " size should <= " + max;
            } else {
                throw new IllegalStateException("invalid checker for field: " + fieldName);
            }
        }
    }

    private static class NotNullChecker<R> extends Checker<R> {
        public NotNullChecker(String fieldName) {
            super(fieldName);
        }

        @Override
        public boolean test(R value) {
            return Objects.nonNull(value);
        }
        @Override
        public String getErrMsg() {
            return fieldName + " cannot be null";
        }
    }

    private static class ValidValuesChecker<R> extends Checker<R> {
        private List<R> validValues;
        public ValidValuesChecker(String fieldName, List<R> validValues) {
            super(fieldName);
            this.validValues = validValues;
        }

        @Override
        public boolean test(R value) {
            return value == null ? true : validValues.stream().anyMatch(v -> v.equals(value));
        }
        @Override
        public String getErrMsg() {
            return fieldName + " valid values are " + validValues.toString();
        }
    }
}

