package com.rest.testrest.controller.szs;

import com.rest.testrest.config.JwtTokenProvider;
import com.rest.testrest.entity.User;
import com.rest.testrest.entity.UserInfo;
import com.rest.testrest.exception.CUserNotFoundException;
import com.rest.testrest.repo.UserInfoJpaRepo;
import com.rest.testrest.repo.UserJpaRepo;
import com.rest.testrest.response.CommonResult;
import com.rest.testrest.response.SingleResult;
import com.rest.testrest.service.AES256Util;
import com.rest.testrest.service.ResponseService;
import com.rest.testrest.service.RefundService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.*;

@Api(tags = {"1. Sign"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/szs")
public class SignController {

    private final UserJpaRepo userJpaRepo;
    private final UserInfoJpaRepo userInfoJpaRepo;
    private final JwtTokenProvider jwtTokenProvider;
    private final ResponseService responseService;
    private final PasswordEncoder passwordEncoder;
    private final RefundService refundService;
    private final AES256Util aes256Util;

    @ApiOperation(value = "로그인", notes = "회원 로그인")
    @PostMapping(value = "/login")
    public SingleResult<String> login(@ApiParam(value = "회원ID", required = true) @RequestParam String userId,
                                      @ApiParam(value = "비밀번호", required = true) @RequestParam String password){

        User user = userJpaRepo.findByUserId(userId).orElseThrow(CUserNotFoundException::new);
        System.out.println(password);
        System.out.println(user.getPassword());
        if(!passwordEncoder.matches(password, user.getPassword())){
            throw new CUserNotFoundException();
        }

        return responseService.getSingleResult(jwtTokenProvider.createToken(String.valueOf(user.getId()), user.getRoles()));

    }

    @ApiOperation(value = "회원가입", notes = "회원가입을 한다")
    @PostMapping(value = "/signup")
    public CommonResult singnup(@ApiParam(value = "회원ID", required = true) @RequestParam String userId,
                                @ApiParam(value = "비밀번호", required = true) @RequestParam String password,
                                @ApiParam(value = "이름", required = true) @RequestParam String name,
                                @ApiParam(value = "주민등록번호", required = true) @RequestParam String regNo) throws GeneralSecurityException, UnsupportedEncodingException {

        userJpaRepo.save(User.builder()
                .userId(userId)
                .password(passwordEncoder.encode(password))
                .name(name)
                .regNo(aes256Util.encrypt(regNo))
                .roles(Collections.singletonList("ROLE_USER"))
                .build());

        return responseService.getSuccessResult();

    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 access_token",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "한명 조회", notes = "한명만 조회")
    @GetMapping(value = "/me")
    public SingleResult<User> findUserById(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        return responseService.getSingleResult(userJpaRepo.findByUserId(userId).orElseThrow(CUserNotFoundException::new));

    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 access_token",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "데이터 스크랩", notes = "필요 데이터 스크랩")
    @PostMapping(value = "/scrap")
    public  SingleResult<String> scrap() throws ParseException, GeneralSecurityException, UnsupportedEncodingException {
        RestTemplate restTemplate = new RestTemplate();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        Optional<User> user = userJpaRepo.findByUserId(userId);
        String name = user.get().getName();
        String regNo = user.get().getRegNo();

        String url = "https://codetest.3o3.co.kr/v1/scrap";

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        body.add("name",name);
        body.add("regNo", aes256Util.decrypt(regNo));
        System.out.println(body);
        HttpEntity<?> requestMessage = new HttpEntity<>(body, httpHeaders);

        HttpEntity<JSONObject> response = restTemplate.postForEntity(url, requestMessage, JSONObject.class);

        JSONParser jsonParser = new JSONParser();

        JSONObject jsonObject = (JSONObject) jsonParser.parse(String.valueOf(response.getBody()));
        JSONObject dataObject = (JSONObject) jsonObject.get("data");
        JSONObject listJson = (JSONObject) dataObject.get("jsonList");
        JSONArray scrap1Object = (JSONArray) listJson.get("scrap001");
        JSONArray scrap2Object = (JSONArray) listJson.get("scrap002");
        JSONObject deductionObject = (JSONObject) scrap2Object.get(0);
        JSONObject refundObject = (JSONObject) scrap1Object.get(0);

        String refund = String.valueOf(refundObject.get("총지급액"));
        String deduction = String.valueOf(deductionObject.get("총사용금액"));

        System.out.println(refund);
        System.out.println(deduction);

        System.out.println(Integer.parseInt(refund.replaceAll(",","").replace(".","")));
        System.out.println(Integer.parseInt(deduction.replace(",","").replace(".","")));

        userInfoJpaRepo.save(UserInfo.builder()
                .name(name)
                .regNo(regNo)
                .userId(userId)
                .refund(Integer.parseInt(refund.replaceAll(",","").replace(".","")))
                .deduction(Integer.parseInt(deduction.replace(",","").replace(".","")))
                .build());

        return responseService.getSingleResult(response.getBody().toJSONString());
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 acess_token",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "환급액", notes = "환급액 계산")
    @GetMapping(value = "/refund")
    public String findRefund(){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        System.out.println(userId);
        Optional<UserInfo> userInfo = userInfoJpaRepo.findByUserId(userId);
        String name = userInfo.get().getName();
        int refund = userInfo.get().getRefund();
        int deduction = userInfo.get().getDeduction();

        return refundService.reund(name, refund, deduction);
    }



}
