package com.sindice.linker.web;

import com.sindice.linker.domain.UserRole;

import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RooWebScaffold(path = "admin/userroles", formBackingObject = UserRole.class)
@RequestMapping("/admin/userroles")
@Controller
public class UserRoleController {
}
