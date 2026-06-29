package com.jobtrak.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

	@GetMapping("/")
	public String home() {
		return "JobTrak backend is running.";
	}

	@GetMapping("/api/test")
	public String test() {
		return "JobTrak API test endpoint is working.";
	}
}
