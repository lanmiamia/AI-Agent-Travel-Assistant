package com.yupi.yup_agent.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ Gareth Bale
 * @ version 1.0
 */

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public String healthCheck(){
        return "ok";
    }

}
