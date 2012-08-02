package com.sindice.linker.web;

import com.sindice.linker.domain.Role;

import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RooWebScaffold(path = "admin/roles", formBackingObject = Role.class)
@RequestMapping("/admin/roles")
@Controller
public class RoleController {
}
