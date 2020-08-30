package com.example.helloworld5.tool;

import java.util.Collections;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Calculator {
    public Calculator() {
    }

    public Boolean flag_radian_degree = true;//false是360度,true是弧度
    private Integer i=0;//字符串指针

    public static void main(String[] args) {
        String str = "((-sin(pi)+(2*3))*sqrt(4))";//测试字符串
        System.out.println(str);
    }

    /**
     * 预处理
     * 1.解决负数的bug
     * 2.替换pi，让前端更美观
     * @param input 计算的字符串
     * @return  预处理后的字符串
     */
    public String preDeal(String input){
        input = myReplaceAll("\\(-","(0-",input);
        input = myReplaceAll("π","pi",input);
        return input;
    }

    /**
     * 工具函数（可重用）
     * 运用指定的正则表达式替换全部的指定字符串的指定子字符串
     * @param regex 指定的正则表达式
     * @param replace 指定的替换的子字符串
     * @param input 指定字符串
     * @return 替换后的字符串
     */
    public String myReplaceAll(String regex,String replace,String input){
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(input);
        input = m.replaceAll(replace);
        return input;
    }

    /**
     * 最重要的一个函数，双栈算法处理字符串表达式，得到计算结果
     * @param str 计算字符串
     * @return 计算结果
     */
    public Double getResult(String str){
        str = preDeal(str);
        Stack<String> ops = new Stack<>();
        Stack<Double> vals = new Stack<>();
        while (i < str.length()){
            if(str.charAt(i) == '(') ops.push("(");
            else if(str.charAt(i) == '+') ops.push("+");
            else if(str.charAt(i) == '-') {//因为有负数的存在所以-号需要特殊处理，想出更好的解决办法，-号前面补零
                    ops.push("-");
            }
            else if(str.charAt(i) == '*') ops.push("*");
            else if(str.charAt(i) == '/') ops.push("/");
            else if(str.charAt(i) == ')'){//遇到右括号就立即pop直到遇到左括号（算法核心所在）
                if(!ops.isEmpty()){
                    if(ops.lastElement().equals("(")){//处理如sqrt（）运算符的情况
                            ops.pop();
                            fun_cal(ops,vals);
                    }
                    else {
                        Stack<String> ops_temp = new Stack<>();
                        Stack<Double> vals_temp = new Stack<>();
                        while (!ops.isEmpty() && !ops.lastElement().equals("(") ) {
                            ops_temp.push(ops.pop());
                            vals_temp.push(vals.pop());
                        }
                        ops.pop();
                        vals_temp.push(vals.pop());

                        Double rwp = getResultWithPrio(ops_temp, vals_temp);//处理加减乘除的优先级问题
                        vals.push(rwp);
                        //解决tan，sin，cos，sqrt之类的问题
                        if(!ops.isEmpty() && !isBase(ops.lastElement())){
                            fun_cal(ops,vals);
                        }
                    }

                }
            }
            else if(Character.isDigit(str.charAt(i))){//添加数字
                vals.push(judgeNumber(str));
            }
            else if(isAlpha(str.charAt(i))){//添加数字或字母
                String sja = judgeAlpha(str);
                if(sja.equals("pi")){
                    vals.push(Math.PI);
                }else if(sja.equals("e")){
                    vals.push(Math.E);
                }else {
                    ops.push(sja);
                }
            }
            i += 1;
        }
        i = 0;//重要
        if(vals.size() == 1) return vals.get(0);//如果vals的长度为1，说明计算成功，返回结果
        else return null;
    }

    /**
     * 处理sqrt函数之类
     * @param ops 运算符组成的栈
     * @param vals 数值组成的栈
     */
    private void fun_cal(Stack<String> ops,Stack<Double> vals){
        String top="";
        if(!ops.isEmpty()){
            top = ops.pop();
        }
        Double v = vals.pop();
        switch (top) {
            case "sqrt":
                v = Math.sqrt(v);
                break;
            case "tan":
                if (!flag_radian_degree)
                    v = Math.tan(Math.toRadians(v));
                else v = Math.tan(v);
                break;
            case "sin":
                if (!flag_radian_degree)
                    v = Math.sin(Math.toRadians(v));
                else v = Math.sin(v);
                break;
            case "cos":
                if (!flag_radian_degree)
                    v = Math.cos(Math.toRadians(v));
                else v = Math.cos(v);
                break;
            case "atan":
                if (!flag_radian_degree)
                    v = Math.atan(Math.toRadians(v));
                else v = Math.atan(v);
                break;
            case "asin":
                if (!flag_radian_degree)
                    v = Math.asin(Math.toRadians(v));
                else v = Math.asin(v);
                break;
            case "acos":
                if (!flag_radian_degree)
                    v = Math.acos(Math.toRadians(v));
                else v = Math.acos(v);
                break;
            case "loge":
                v = Math.log(v);
                break;
            case "lg":
                v = Math.log10(v);
                break;
        }
        vals.push(v);
    }

    /**
     * 工具函数
     * @param s 指定字符串
     * @return 是否在指定数组存在
     */
    private Boolean isBase(String s){
        String[] arr= {"+","-","*","/","(",")"};
        for (String si :
                arr) {
            if(si.equals(s)) return true;
        }
        return false;
    }

    /**
     * 重要，解决加减乘除的优先级问题
     * @param ops_temp 临时的运算符栈数组
     * @param vals_temp 临时的数组栈数组
     * @return 部分的计算结果
     */
    private Double getResultWithPrio(Stack<String> ops_temp, Stack<Double> vals_temp) {
        if(ops_temp.size()!=vals_temp.size()-1) return null;
        Collections.reverse(ops_temp);
        Collections.reverse(vals_temp);
        System.out.println(vals_temp);
        System.out.println(ops_temp);
        //第一次遍历，解决掉乘法和除法
        for (int i = 0; i < ops_temp.size(); i++) {
            if(getPrio(ops_temp.elementAt(i))==0){
                if(ops_temp.get(i).equals("*")){
                    vals_temp.set(i,vals_temp.get(i)*vals_temp.get(i+1));
                    vals_temp.remove(i+1);
                    ops_temp.remove(i);
                    i--;//重要步骤，不然有bug
                }
                else if(ops_temp.get(i).equals("/")){
                    vals_temp.set(i,vals_temp.get(i)/vals_temp.get(i+1));
                    vals_temp.remove(i+1);
                    ops_temp.remove(i);
                    i--;
                }
            }
        }
        System.out.println();
        System.out.println(vals_temp);
        System.out.println(ops_temp);
        for (int i = 0; i < ops_temp.size(); i++) {
            if(getPrio(ops_temp.elementAt(i))==1){
                if(ops_temp.get(i).equals("+")){
                    vals_temp.set(i,vals_temp.get(i)+vals_temp.get(i+1));
                    vals_temp.remove(i+1);
                    ops_temp.remove(i);
                    i--;//重要步骤，不然有bug
                }
                else if(ops_temp.get(i).equals("-")){
                    vals_temp.set(i,vals_temp.get(i)-vals_temp.get(i+1));
                    vals_temp.remove(i+1);
                    ops_temp.remove(i);
                    i--;
                }
            }
        }
        if(vals_temp.size()==1) return vals_temp.get(0);
        else return null;
    }

    /**
     * 工具函数，获取加减乘除的优先级
     * @param c 加减乘除的字符
     * @return 代表优先级的int值
     */
    private int getPrio(String c){
        if(c.equals("*") || c.equals("/")) return 0;
        else if(c.equals("+") || c.equals("-")) return 1;
        else return 2;
    }

    /**
     * 获取连续的字母
     * @param s 总字符串
     * @return 切割的字符串
     */
    private String judgeAlpha(String s) {
        StringBuilder temp = new StringBuilder();
        while (i<s.length() && (isAlpha(s.charAt(i)))){
            temp.append(s.charAt(i));
            i += 1;
        }
        i -= 1;
        return temp.toString();
    }

    /**
     * 获取连续的数字
     * @param s 总字符串
     * @return 切割的数字字符串
     */
    private Double judgeNumber(String s) {
        StringBuilder temp = new StringBuilder();
        while (i<s.length() && (Character.isDigit(s.charAt(i)) || s.charAt(i) == '.')){
            temp.append(s.charAt(i));
            i += 1;
        }
        i -= 1;
        return Double.parseDouble(temp.toString());
    }

    /**
     * 判断是否为字母（可重用）
     * @param c 测试的字符
     * @return 判断是否为英文字母
     */
    public boolean isAlpha(char c) {
        return ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'));
    }
}
