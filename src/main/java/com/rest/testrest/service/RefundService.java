package com.rest.testrest.service;

import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;

@Service
public class RefundService {

    public String reund(String name, int numrefund, int numdeduction) {

        double refund = 0;
        double deduction = 0;
        double limit = 0;
        /*else if(a > 33000000 && a <= 70000000){
                    refund =  refunt(a);
                }*/
        if (numrefund <= 33000000) {
            limit = 740000;
        } else {
            limit = refunt(numrefund);
        }

        deduction = deductible(numdeduction);
        refund = Math.min(limit, deduction);

        JSONObject jsonObject = new JSONObject();

       // jsonObject.put("이름", name);
        jsonObject.put("한도", numberToEng(limit));
        jsonObject.put("공제액", numberToEng(deduction));
        jsonObject.put("환급액", numberToEng(refund));

        return jsonObject.toJSONString();
    }

    public double refunt(int num){

        double result = 0;

        if(num <= 70000000){
            result = 740000 - (num - 33000000) * 0.008;
            if(result <= 660000){
                result = 660000;
            }
        }else{
            result = 660000 - (num - 70000000)* 1 / 2;
            if(result <= 500000){
                result = 500000;
            }
        }

        return result;
    }

    public double deductible(int num){
        double result = 0;

        if(num <= 1300000){
            result = num * 0.55;
        }else{
            result = 715000 + (num - 1300000) * 0.3;
        }

        return result;
    }

    public String numberToEng(double db){

        DecimalFormat d = new DecimalFormat("####,####");

        String[] unit = new String[]{"", "만","억","조"};
        String[] str = d.format(db).split(",");
        String result = "";
        int cnt = 0;
        for(int i=str.length;i>0;i--) {
            if(Integer.parseInt(str[i-1]) != 0) {
                result = String.valueOf(Integer.parseInt(str[i-1])) + unit[cnt] + result;
            }
            cnt++;
        }

        return result + "원";
    }
}
