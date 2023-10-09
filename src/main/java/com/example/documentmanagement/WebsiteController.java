package com.example.documentmanagement;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
public class WebsiteController {


    @Autowired
    private AdministratorService administratorService;

    @GetMapping("/")
    public String displayHomePage(){
        return"index";
    }

    @GetMapping("/create-administrator")
    public String displayCreateAdminPage(){
        return "createAdministrator";
    }


    @PostMapping("/create-administrator")
    public String handleAdministratorRegistration(Administrator administrator){
        System.out.println(administrator);
        try {
           administratorService.createAdministrator(administrator);
            return "redirect:/create-administrator?status=ADMINISTRATOR_REGISTRATION_SUCCESS";
        } catch (Exception exception) {
            return "redirect:/create-administrator?status=ADMINISTRATOR_REGISTRATION_FAILED&error=" + exception.getMessage();
        }
    }

}
