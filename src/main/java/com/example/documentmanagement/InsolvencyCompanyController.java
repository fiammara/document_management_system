package com.example.documentmanagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class InsolvencyCompanyController {
    private InsolvencyCompanyService insolvencyCompanyService;

    @Autowired
    public InsolvencyCompanyController(InsolvencyCompanyService insolvencyCompanyService) {
        this.insolvencyCompanyService = insolvencyCompanyService;
    }

    @PostMapping("/create-process")
    public String createInsolvencyCompany(InsolvencyCompany insolvencyCompany) {
        try {
            insolvencyCompanyService.createInsolvencyCompany(insolvencyCompany);
            return "redirect:/create-process?message=INSOLVENCY_COMPANY_CREATED_SUCCESSFULLY";
        } catch (Exception exception) {
            return "redirect:/create-process?message=INSOLVENCY_COMPANY_CREATION_FAILED&error=" + exception.getMessage();
        }

    }
    @GetMapping("/create-process")
    public String displayProcessList(@RequestParam(required = false) String message,
                                  @RequestParam(required = false) String error,
                                  Model model) {
        model.addAttribute("message", message);
        model.addAttribute("error", error);
        model.addAttribute("processList", insolvencyCompanyService.findAll());
        return "createProcess";
    }
    @GetMapping("/view-processes")
    public String displayProcessList2(@RequestParam(required = false) String message,
                                     @RequestParam(required = false) String error,
                                     Model model) {
        model.addAttribute("message", message);
        model.addAttribute("error", error);
        model.addAttribute("processList", insolvencyCompanyService.findAll());
        return "viewProcessesList";
    }


}
