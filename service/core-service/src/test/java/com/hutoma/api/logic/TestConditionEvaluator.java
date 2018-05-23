package com.hutoma.api.logic;

import com.hutoma.api.containers.sub.ChatContext;
import com.hutoma.api.containers.sub.IntentConditionOperator;
import com.hutoma.api.containers.sub.IntentVariableCondition;
import com.hutoma.api.logic.chat.ConditionEvaluator;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;


public class TestConditionEvaluator {

    @Test
    public void testEvaluate_variableSet() {
        ConditionEvaluator ev = new ConditionEvaluator(Collections.singletonList(
                new IntentVariableCondition("var", IntentConditionOperator.SET, null)));
        Assert.assertTrue(ev.evaluate(buildContext("var", "")));
    }

    @Test
    public void testEvaluate_variableSet_false() {
        ConditionEvaluator ev = new ConditionEvaluator(Collections.singletonList(
                new IntentVariableCondition("var", IntentConditionOperator.SET, null)));
        Assert.assertFalse(ev.evaluate(buildContext("not_the_var", "")));
    }

    @Test
    public void testEvaluate_variableNotSet() {
        ConditionEvaluator ev = new ConditionEvaluator(Collections.singletonList(
                new IntentVariableCondition("var", IntentConditionOperator.NOT_SET, null)));
        Assert.assertTrue(ev.evaluate(buildContext("not_the_var", "")));
    }

    @Test
    public void testEvaluate_variableNotSet_false() {
        ConditionEvaluator ev = new ConditionEvaluator(Collections.singletonList(
                new IntentVariableCondition("var", IntentConditionOperator.NOT_SET, null)));
        Assert.assertFalse(ev.evaluate(buildContext("var", "")));
    }

    @Test
    public void testEvaluate_variableIsEquals() {
        ConditionEvaluator ev = new ConditionEvaluator(Collections.singletonList(
                new IntentVariableCondition("var", IntentConditionOperator.EQUALS, "aaa")));
        Assert.assertTrue(ev.evaluate(buildContext("var", "aaa")));
    }

    @Test
    public void testEvaluate_variableIsEquals_false() {
        ConditionEvaluator ev = new ConditionEvaluator(Collections.singletonList(
                new IntentVariableCondition("var", IntentConditionOperator.EQUALS, "aaa")));
        Assert.assertFalse(ev.evaluate(buildContext("var", "bbb")));
    }

    @Test
    public void testEvaluate_variableIsNotEquals() {
        ConditionEvaluator ev = new ConditionEvaluator(Collections.singletonList(
                new IntentVariableCondition("var", IntentConditionOperator.NOT_EQUALS, "aaa")));
        Assert.assertTrue(ev.evaluate(buildContext("var", "bbb")));
    }

    @Test
    public void testEvaluate_variableIsNotEquals_false() {
        ConditionEvaluator ev = new ConditionEvaluator(Collections.singletonList(
                new IntentVariableCondition("var", IntentConditionOperator.NOT_EQUALS, "aaa")));
        Assert.assertFalse(ev.evaluate(buildContext("var", "aaa")));
    }

    @Test
    public void testEvaluate_variableBiggerThan_String() {
        ConditionEvaluator ev = new ConditionEvaluator(Collections.singletonList(
                new IntentVariableCondition("var", IntentConditionOperator.BIGGER_THAN, "bbb")));
        Assert.assertTrue(ev.evaluate(buildContext("var", "aaa")));
    }

    @Test
    public void testEvaluate_variableBiggerThan_String_false() {
        ConditionEvaluator ev = new ConditionEvaluator(Collections.singletonList(
                new IntentVariableCondition("var", IntentConditionOperator.BIGGER_THAN, "aaa")));
        Assert.assertFalse(ev.evaluate(buildContext("var", "bbb")));
    }

    @Test
    public void testEvaluate_variableBiggerThan_Number() {
        ConditionEvaluator ev = new ConditionEvaluator(Collections.singletonList(
                new IntentVariableCondition("var", IntentConditionOperator.BIGGER_THAN, "100.0")));
        Assert.assertTrue(ev.evaluate(buildContext("var", "100.1")));
    }

    @Test
    public void testEvaluate_variableBiggerThan_Number_false() {
        ConditionEvaluator ev = new ConditionEvaluator(Collections.singletonList(
                new IntentVariableCondition("var", IntentConditionOperator.BIGGER_THAN, "100")));
        Assert.assertFalse(ev.evaluate(buildContext("var", "99")));
    }

    @Test
    public void testEvaluate_variableBiggerThan_String_equals() {
        ConditionEvaluator ev = new ConditionEvaluator(Collections.singletonList(
                new IntentVariableCondition("var", IntentConditionOperator.BIGGER_THAN, "abc")));
        Assert.assertFalse(ev.evaluate(buildContext("var", "abc")));
    }

    @Test
    public void testEvaluate_variableSmallerThan_Number() {
        ConditionEvaluator ev = new ConditionEvaluator(Collections.singletonList(
                new IntentVariableCondition("var", IntentConditionOperator.SMALLER_THAN, "0.01")));
        Assert.assertTrue(ev.evaluate(buildContext("var", "0.009")));
    }

    @Test
    public void testEvaluate_variableSmallerThan_Number_false() {
        ConditionEvaluator ev = new ConditionEvaluator(Collections.singletonList(
                new IntentVariableCondition("var", IntentConditionOperator.SMALLER_THAN, "0.001")));
        Assert.assertFalse(ev.evaluate(buildContext("var", "0.01")));
    }

    @Test
    public void testEvaluate_variableSmallerThan_String_equals() {
        ConditionEvaluator ev = new ConditionEvaluator(Collections.singletonList(
                new IntentVariableCondition("var", IntentConditionOperator.SMALLER_THAN, "abc")));
        Assert.assertFalse(ev.evaluate(buildContext("var", "abc")));
    }

    @Test
    public void testEvaluate_variableSmallerThan_NumberMixedWithChars_treatedAsString() {
        String conditionValue = "0.01";
        String varValue = "0.123abc1";
        ConditionEvaluator ev = new ConditionEvaluator(Collections.singletonList(
                new IntentVariableCondition("var", IntentConditionOperator.SMALLER_THAN, conditionValue)));
        Assert.assertTrue(ev.evaluate(buildContext("var", varValue)));
    }

    @Test
    public void testEvalueate_multipleConditions_allTrue() {
        ConditionEvaluator ev = new ConditionEvaluator(Arrays.asList(
                new IntentVariableCondition("var1", IntentConditionOperator.SMALLER_THAN, "100"),
                new IntentVariableCondition("var2", IntentConditionOperator.EQUALS, "true"),
                new IntentVariableCondition("var3", IntentConditionOperator.SET, "")
        ));
        Assert.assertTrue(ev.evaluate(buildContext("var1", "90", "var2", "true", "var3", "any_value_really")));
    }

    @Test
    public void testEvalueate_multipleConditions_allFalse() {
        ConditionEvaluator ev = new ConditionEvaluator(Arrays.asList(
                new IntentVariableCondition("var1", IntentConditionOperator.SMALLER_THAN, "100"),
                new IntentVariableCondition("var2", IntentConditionOperator.EQUALS, "true"),
                new IntentVariableCondition("var3", IntentConditionOperator.SET, "")
        ));
        Assert.assertFalse(ev.evaluate(buildContext("var1", "101", "var2", "false")));
    }

    @Test
    public void testEvalueate_multipleConditions_atLeastOneFalse() {
        ConditionEvaluator ev = new ConditionEvaluator(Arrays.asList(
                new IntentVariableCondition("var1", IntentConditionOperator.SMALLER_THAN, "100"),
                new IntentVariableCondition("var2", IntentConditionOperator.EQUALS, "false"),
                new IntentVariableCondition("var3", IntentConditionOperator.SET, "")
        ));
        Assert.assertTrue(ev.evaluate(buildContext("var1", "90", "var2", "true", "var3", "any_value_really")));
    }

    private ChatContext buildContext(final String ... values) {
        if (values.length % 2 != 0) {
            throw new IllegalArgumentException("values need to be in pairs of key, value");
        }

        ChatContext ctx = new ChatContext();
        for (int i = 0; i < values.length; i = i + 2) {
            ctx.setValue(values[i], values[i+1]);
        }
        return ctx;
    }
}
