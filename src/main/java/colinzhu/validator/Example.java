package colinzhu.validator;

import lombok.Data;

import java.util.List;

public class Example {
    public static void main(String[] args) {
        Validator<TestBean> validator = new Validator<>();
        validator.addRule(TestBean::getId, Validator.notNull("id"));
        validator.addRule(TestBean::getStatus, Validator.notNull("status"));
        validator.addRule(TestBean::getStatus, Validator.maxSize("status", 3));
        validator.addRule(TestBean::getCurrency, Validator.size("currency", 3));
        validator.addRule(TestBean::getCurrency, Validator.validValues("currency", List.of("CNY", "GBP")));

        TestBean testBean = new TestBean();
//        testBean.setId(123L);
        testBean.setStatus("SBCx");
        testBean.setCurrency("ABCD");

        try {
            validator.validate(testBean);
            System.out.println("passed");
        } catch (Validator.ValidationException e) {
            System.out.println(e.getMessage());
        }
    }

    @Data
    public static class TestBean {
        private Long id;
        private String status;
        private String currency;
    }

}