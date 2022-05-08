package com.rest.testrest.controller.szs;

import com.rest.testrest.entity.User;
import com.rest.testrest.repo.UserJpaRepo;
import com.rest.testrest.response.ListResult;
import com.rest.testrest.response.SingleResult;
import com.rest.testrest.service.ResponseService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = {"2. User"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/szs")
public class UserController {

    private final UserJpaRepo userJpaRepo;
    private final ResponseService responseService;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 acess_token",
            required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "회원조회", notes = "모든 회원을 조회한다.")
    @GetMapping(value = "/user")
    public ListResult<User> findAllUser(){
        return responseService.getListResult(userJpaRepo.findAll());
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 access_token",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "한명 조회", notes = "한명만 조회")
    @GetMapping(value = "/user/{id}")
    public SingleResult<User> findUserById(@ApiParam(value = "회원ID",required = true) @PathVariable long id){
        return responseService.getSingleResult(userJpaRepo.findById(id).orElse(null));
    }

    @ApiOperation(value = "회원생성", notes = "신규 회원을 생성한다.")
    @PostMapping(value = "/user")
    public SingleResult<User> save(@ApiParam(value = "회원아이디", required = true)@RequestParam String userId,
                     @ApiParam(value = "회원이름", required = true) @RequestParam String name,
                     @ApiParam(value = "비밀번호", required = true) @RequestParam String password,
                     @ApiParam(value = "주민번호", required = true) @RequestParam String regNo){
        User user = User.builder()
                .userId(userId)
                .name(name)
                .password(password)
                .regNo(regNo)
                .build();

        return responseService.getSingleResult(userJpaRepo.save(user));
    }

}
