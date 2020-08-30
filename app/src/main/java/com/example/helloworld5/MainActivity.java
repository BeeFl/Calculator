package com.example.helloworld5;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.text.Selection;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.example.helloworld5.tool.Calculator;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Boolean shiftFlag = false;
    private Boolean calFlag = false;//每计算一次就置为true，表示已经计算过一次
    private int jingDu = 6;//默认精度值为6
    private Double miciResult;//用户输入的幂次结果
    private String answerGet;//获取用户选择的答案，默认最新的答案
    private ArrayList<String> answerStore= new ArrayList<>();
    private int[] buttonIdList = {R.id.bt_left_bracket, R.id.bt_right_bracket, R.id.bt_clear,
            R.id.bt_delete_last, R.id.bt_one, R.id.bt_two, R.id.bt_three, R.id.bt_four, R.id.bt_five,
            R.id.bt_six, R.id.bt_seven, R.id.bt_eight, R.id.bt_nine, R.id.bt_zero, R.id.bt_point,
            R.id.bt_plus, R.id.bt_minus, R.id.bt_multi, R.id.bt_div, R.id.bt_equal,
            R.id.shift,R.id.answerget};//按键列表，便于一次性添加点击事件
    private Calculator cal = new Calculator();//核心，计算器对象，计算字符串表达式

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        for (int id : buttonIdList) {
            Button btn = (Button) findViewById(id);
            btn.setOnClickListener(this);
        }
        //输入框自动获取焦点
        EditText et_input = (EditText) findViewById(R.id.expression);
        et_input.setFocusable(true);
        et_input.setFocusableInTouchMode(true);
        et_input.requestFocus();
        //让软键盘消失
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        try {
            Class<EditText> cls = EditText.class;
            Method setSoftInputShownOnFocus;
            setSoftInputShownOnFocus = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
            setSoftInputShownOnFocus.setAccessible(true);
            setSoftInputShownOnFocus.invoke(et_input, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //给光标拖动条配置监听事件
        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                EditText et = (EditText) findViewById(R.id.expression);
                Selection.setSelection(et.getText(),i);//移动光标位置
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                EditText et = (EditText) findViewById(R.id.expression);
                int length = et.getText().toString().length();
                seekBar.setMax(length);//把编辑框文本的长度设置为拖动条的最大进度

                int start = et.getSelectionStart();
                seekBar.setProgress(start);
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        //给精度拖动条配置监听事件
        SeekBar seekBarDouble = (SeekBar) findViewById(R.id.seekBarDouble);
        seekBarDouble.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                jingDu = i;
                TextView tv = findViewById(R.id.maxDouble);
                tv.setText(Integer.toString(jingDu));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        //给弧度和角度switch配置监听事件
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch swIfHuJiao = findViewById(R.id.ifHuDu);
        swIfHuJiao.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //如果为true的话，即角度模式,假的为弧度模式
                cal.flag_radian_degree = !b;
            }
        });
    }

    /**
     * 给计算器的各个按钮统一添加点击事件
     * @param view 被点击的控件对象
     */
    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onClick(View view) {
        EditText tv = (EditText) findViewById(R.id.expression);
        if(calFlag){//如果计算过了，就清空tv
            tv.setText("");
            calFlag = false;
        }
        int start = tv.getSelectionStart();
        TextView tv_ans = (TextView) findViewById(R.id.answer);
        switch (view.getId()) {
            case R.id.answerget://获取下拉列表选择的历史答案
                if(answerGet!=null){
                    Spinner spinner = (Spinner) findViewById(R.id.ansspinner);
                    answerGet = (String) spinner.getSelectedItem();
                    if(Double.parseDouble(answerGet)<0){//答案是负数
                        answerGet = addRLBracket(answerGet);
                    }
                    tv.getText().insert(start,answerGet);
                }
                break;
            case R.id.shift://上档键
                Button shift = (Button) findViewById(R.id.shift);
                //如果按钮被点击，是字母变大写
                if (!shiftFlag) {
                    shift.setText("SHIFT");
                    shift.setBackground(getResources().getDrawable(R.drawable.bt_up));
                } else {
                    shift.setText("shift");
                    shift.setBackground(getResources().getDrawable(R.drawable.bt_normal));
                }
                shiftFlag = !shiftFlag;
                break;
            case R.id.bt_left_bracket:
                tv.getText().insert(start,getString(R.string.left_bracket));
                break;
            case R.id.bt_right_bracket:
                tv.getText().insert(start,getString(R.string.right_bracket));
                break;
            case R.id.bt_clear:
                tv.setText("");
                tv_ans.setText("");
                break;
            case R.id.bt_delete_last://删除光标前的字符
                if (!tv.getText().toString().isEmpty()) {
                    tv.getText().delete(start-1,start);
                }
                break;
            case R.id.bt_one:
                if (!shiftFlag) {//没上档
                    tv.getText().insert(start,getString(R.string.one));
                } else {//上档了
                    tv.getText().insert(start,getString(R.string.sqrt)+"()");
                    int start2 = tv.getSelectionStart();
                    Selection.setSelection(tv.getText(),start2-1);
                }
                break;
            case R.id.bt_two:
                if (!shiftFlag) {//没上档
                    tv.getText().insert(start,getString(R.string.two));
                } else {//上档了
                    tv.getText().insert(start,getString(R.string.sin)+"()");
                    //把光标移动到括号里
                    int start2 = tv.getSelectionStart();
                    Selection.setSelection(tv.getText(),start2-1);
                }
                break;
            case R.id.bt_three:
                if (!shiftFlag) {//没上档
                    tv.getText().insert(start,getString(R.string.three));
                } else {//上档了
                    tv.getText().insert(start,getString(R.string.cos)+"()");
                    int start2 = tv.getSelectionStart();
                    Selection.setSelection(tv.getText(),start2-1);
                }
                break;
            case R.id.bt_plus:
                if (!shiftFlag) {//没上档
                    tv.getText().insert(start,getString(R.string.plus));
                } else {//上档了
                    tv.getText().insert(start,getString(R.string.tan)+"()");
                    int start2 = tv.getSelectionStart();
                    Selection.setSelection(tv.getText(),start2-1);
                }
                break;
            case R.id.bt_four:
                if (!shiftFlag) {//没上档
                    tv.getText().insert(start,getString(R.string.four));
                } else {//上档了
                    tv.getText().insert(start,getString(R.string.e));
                }
                break;
            case R.id.bt_five:
                if (!shiftFlag) {//没上档
                    tv.getText().insert(start,getString(R.string.five));
                } else {//上档了
                    tv.getText().insert(start,getString(R.string.pi));
                }
                break;
            case R.id.bt_six:
                if (!shiftFlag) {//没上档
                    tv.getText().insert(start,getString(R.string.six));
                } else {//计算x的平方
                    String str = tv.getText().toString();
                    Double result = getCalResult(str);
                    if(result!=null){
                        Double result2=result*result;
                        showResult(tv_ans,result2);
                    }
                }
                break;
            case R.id.bt_minus:
                if (!shiftFlag) {//没上档
                    tv.getText().insert(start,getString(R.string.minus));
                } else {//上档了
                    String str = tv.getText().toString();
                    Double result = getCalResult(str);
                    if(result!=null){
                        Double result2=result*result*result;
                        showResult(tv_ans,result2);
                    }
                }
                break;
            case R.id.bt_seven:
                if (!shiftFlag) {//没上档
                    tv.getText().insert(start,getString(R.string.seven));
                } else {//上档了
//                    System.out.println(str);
                    String str = tv.getText().toString();
                    Double result = getCalResult(str);
                    //获取用户输入的幂次，可以采用弹出输入对话框的形式
                    showInputDialog(result,tv_ans);
                }
                break;
            case R.id.bt_eight:
                if (!shiftFlag) {//没上档
                    tv.getText().insert(start,getString(R.string.eight));
                } else {//上档了
                    tv.getText().insert(start,getString(R.string.asin)+"()");
                    int start2 = tv.getSelectionStart();
                    Selection.setSelection(tv.getText(),start2-1);
                }
                break;
            case R.id.bt_nine:
                if (!shiftFlag) {//没上档
                    tv.getText().insert(start,getString(R.string.nine));
                } else {//上档了
                    tv.getText().insert(start,getString(R.string.acos)+"()");
                    int start2 = tv.getSelectionStart();
                    Selection.setSelection(tv.getText(),start2-1);
                }
                break;
            case R.id.bt_multi:
                if (!shiftFlag) {//没上档
                    tv.getText().insert(start,getString(R.string.multi));
                } else {//上档了
                    tv.getText().insert(start,getString(R.string.atan)+"()");
                    int start2 = tv.getSelectionStart();
                    Selection.setSelection(tv.getText(),start2-1);
                }
                break;
            case R.id.bt_point:
                if (!shiftFlag) {//没上档
                    tv.getText().insert(start,getString(R.string.point));
                } else {//上档了
                    tv.getText().insert(start,getString(R.string.loge)+"()");
                    int start2 = tv.getSelectionStart();
                    Selection.setSelection(tv.getText(),start2-1);
                }
                break;
            case R.id.bt_zero:
                if (!shiftFlag) {//没上档
                    tv.getText().insert(start,getString(R.string.zero));
                } else {//上档了
                    tv.getText().insert(start,"lg"+"()");
                    int start2 = tv.getSelectionStart();
                    Selection.setSelection(tv.getText(),start2-1);
                }
                break;
            case R.id.bt_div:
                tv.getText().insert(start,getString(R.string.div));
                break;
            case R.id.bt_equal:
                String str = tv.getText().toString();
                if (str.isEmpty()) {
                    break;
                } else {
                    Double result = getCalResult(str);
                    showResult(tv_ans,result);
                }
                break;
        }
        //让上档键在有其他按钮按过后就失效
        if(view.getId()!=R.id.shift && shiftFlag){
            Button shift = (Button) findViewById(R.id.shift);
            shift.setText("shift");
            shift.setBackground(getResources().getDrawable(R.drawable.bt_normal));
            shiftFlag = false;
        }
    }

    /**
     * 展示计算结果
     * @param tv_ans 展示结果的TextView对象
     * @param result 计算的结果
     */
    private void showResult(TextView tv_ans ,Double result){
        if (result != null) {
            StringBuilder formatStr = new StringBuilder();
            for (int i = 0; i < jingDu; i++) {
                formatStr.append("#");
            }
            String fs = new DecimalFormat("#."+formatStr).format(result);
            tv_ans.setText(fs);
            if(answerStore != null && answerStore.size()<=5){
                //在数组第一位插入新算出的答案
                if(answerStore.size()==5){
                    //先删掉最后一位
                    answerStore.remove(4);
                }
                answerStore.add(0,fs);
            }
            System.out.println(answerStore);
            ArrayAdapter<String> answerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, answerStore);
            answerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            Spinner spinner = (Spinner) findViewById(R.id.ansspinner);
            spinner.setAdapter(answerAdapter);
            answerGet = (String) spinner.getSelectedItem();
        }
        calFlag = true;
    }

    /**
     * 工具函数，给字符串两端加上括号
     * @param str 给定的字符串
     * @return 加上括号的字符串
     */
    private String addRLBracket(String str){
        StringBuilder sb = new StringBuilder(str);
        sb.insert(0, '(');
        sb.append(')');
        return sb.toString();
    }

    /**
     * 重要，计算表达式的结果，如果有异常则弹出警告对话框
     * @param str 字符串表达式
     * @return 计算结果
     */
    private Double getCalResult(String str) {
        String sbs = addRLBracket(str);
        Double result = null;
        try {
            result = cal.getResult(sbs);
        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = "您的表达式有误，请检查重试";
            new AlertDialog.Builder(this)
                    .setTitle("标题")
                    .setMessage(errorMessage)
                    .setPositiveButton("确定", null).show();
        }
        return result;
    }

    /**
     * 通过弹出输入框，获取幂次表达式中幂次y的值（比较low），暂时想不出好的替换方法
     * @param result 幂次表达式底数的预算结果（因为不一定只有一个数，可能是一个数学表达式）
     * @param tv_ans 显示答案的TextView控件
     */
    private void showInputDialog(final Double result, final TextView tv_ans) {
        final EditText editText = new EditText(MainActivity.this);
        AlertDialog.Builder inputDialog =
                new AlertDialog.Builder(MainActivity.this);
        inputDialog.setTitle("请输入幂次表达式：").setView(editText);
        inputDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //返回用户输入的表达式
                        String miciStr = editText.getText().toString();
                        miciResult = getCalResult(miciStr);//防止miciStr为空
                        System.out.println(miciResult);
                        if(result!=null && miciResult!=null){//等待aDouble不为空
                            Double resultxy=Math.pow(result,miciResult);
                            showResult(tv_ans,resultxy);
                            miciResult = null;
                        }
                    }
                }).show();
    }
}