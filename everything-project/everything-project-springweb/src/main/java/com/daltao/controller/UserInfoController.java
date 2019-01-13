package com.daltao.controller;

import com.daltao.api.registration.UserInfoService;
import com.daltao.exception.InvalidInputException;
import com.daltao.model.Result;
import com.daltao.model.UserBO;
import com.daltao.model.UserTO;
import com.daltao.service.common.UserTransfer;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/user/info")
public class UserInfoController {
    @Resource
    private UserInfoService userInfoService;
    @Resource
    private UserTransfer userTransfer;

    @RequestMapping("/register")
    public Result<UserTO> register(@RequestBody UserTO userTO) {
        UserBO bo = userTransfer.getTo2bo().transfer(userTO, UserBO::new);
        bo = userInfoService.register(bo);
        return Result.newSuccessResult(userTransfer.getBo2to().transfer(bo, UserTO::new));
    }

    @RequestMapping("/hello")
    public Result<String> helloWorld(@RequestParam("name") String name) {
        return Result.newSuccessResult("Hello, world!" + name);
    }

    @RequestMapping("/error")
    public void error() {
        InvalidInputException.invalidField("id").throwSelf();
    }
}