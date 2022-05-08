package com.rest.testrest.service;

import com.rest.testrest.response.CommonResult;
import com.rest.testrest.response.ListResult;
import com.rest.testrest.response.SingleResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResponseService {

    @Getter
    @RequiredArgsConstructor
    @AllArgsConstructor
    public enum CommonResponse{
        SUCCESS(0, "성공"),
        FAIL(-1, "실패");

        private int code;
        private String message;
   }

   public <T>SingleResult<T> getSingleResult(T data){
        SingleResult<T> result = new SingleResult<>();
        result.setData(data);
        setSuccessResult(result);
        return result;
   }


   public <T>ListResult<T> getListResult(List<T> list){
        ListResult<T> result = new ListResult<>();
        result.setList(list);
        setSuccessResult(result);
        return result;
   }

   public CommonResult getSuccessResult(){
        CommonResult result = new CommonResult();
        setSuccessResult(result);
        return  result;
   }

   public CommonResult getFailResult(){
        CommonResult result = new CommonResult();
        result.setSuccess(false);
        result.setCode(CommonResponse.FAIL.getCode());
        result.setMessage(CommonResponse.FAIL.getMessage());
        return  result;
   }

    private void setSuccessResult(CommonResult result) {
        result.setSuccess(true);
        result.setCode(CommonResponse.SUCCESS.getCode());
        result.setMessage(CommonResponse.SUCCESS.getMessage());
    }

}
