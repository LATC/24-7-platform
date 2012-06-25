package com.sindice.linker.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/member/**")
@Controller
public class MemberController {

    @RequestMapping(value="")
    public String index(Model uiModel) {
    	uiModel.addAttribute("contextPrefix", "../");
    	return "member/index";
    }

}
