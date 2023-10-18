package com.example.documentmanagement.insolvencyprocess;

import com.example.documentmanagement.administrator.Administrator;
import com.example.documentmanagement.administrator.AdministratorRepository;
import com.example.documentmanagement.administrator.AdministratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
public class InsolvencyProcessController {
    @Autowired
    private InsolvencyProcessService insolvencyProcessService;
    @Autowired
    private AdministratorService administratorService;
    @Autowired
    private AdministratorRepository administratorRepository;
    @Autowired
    private InsolvencyProcessRepository insolvencyProcessRepository;


    @GetMapping("/create-process")
    public String displayCreateProcessPage(@RequestParam(required = false) String message,
                                           @RequestParam(required = false) String error,
                                           Model model) {
        List<Administrator> adminList = administratorRepository.findAll();
        model.addAttribute("message", message);
        model.addAttribute("error", error);
        model.addAttribute("process", new InsolvencyProcess());
        model.addAttribute("processList", insolvencyProcessService.findAll());
        model.addAttribute("adminList", adminList);
        model.addAttribute("page",findPaginated(1, "id", "asc", model));
        return "createProcess";
    }

    @PostMapping("/create-process")
    public String createProcessPage(InsolvencyProcess insolvencyProcess) {
        try {
            insolvencyProcessRepository.save(insolvencyProcess);


            return "redirect:/create-process?message=INSOLVENCY_PROCESS_CREATED_SUCCESSFULLY";
        } catch (Exception exception) {
            return "redirect:/create-process?message=INSOLVENCY_PROCESS_CREATION_FAILED&error=" + exception.getMessage();
        }
    }

    @GetMapping("/edit-process/{id}")
    public String showEditProcessPage(@PathVariable Long id, Model model) {
        try {
            List<Administrator> adminList = administratorRepository.findAll();
            InsolvencyProcess selectedProcess = insolvencyProcessService.findInsolvencyProcessById(id);
            model.addAttribute("process", selectedProcess);
            model.addAttribute("admin", adminList);
            return "editProcess";
        } catch (Exception exception) {
            return "redirect:/?message=PROCESS_EDIT_FAILED&error=" + exception.getMessage();
        }
    }

    @PostMapping("/edit-process/{id}")
    public String editProcess(@PathVariable Long id, Model model, InsolvencyProcess insolvencyProcess) {
        try {
            insolvencyProcess.setId(id);
            System.out.println(insolvencyProcess);
            insolvencyProcessService.editInsolvencyProcess(insolvencyProcess);
            List<Administrator> adminList = administratorRepository.findAll();
            model.addAttribute("admin", adminList);
            return "redirect:/view-processes?message=INSOLVENCY_PROCESS_EDITED_SUCCESSFULLY";
        } catch (Exception exception) {
            return "redirect:/?message=INSOLVENCY_PROCESS_EDIT_FAILED&error=" + exception.getMessage();
        }
    }

    @GetMapping(path="/search")
    public String search(@RequestParam(required = false) String message,
                         @RequestParam(required = false) String error,
                                           InsolvencyProcess insolvencyProcess,
                                           LocalDate localDate,
                                           Model model) {
        model.addAttribute("search", insolvencyProcessRepository.findByCaseClosingDate(localDate));
        return "/viewProcessesList";

//    @GetMapping( "/view-processes/inactive")
//    public String displayFilteredProcessesInactive(@RequestParam(required = false) String message,
//                                           @RequestParam(required = false) String error,
//                                           LocalDate caseClosingDate,
//                                           Model model) {
//        List<InsolvencyProcess> inactiveCases = insolvencyProcessService.sortProcesses(caseClosingDate);
//
//        model.addAttribute("processList", insolvencyProcessService.findAll());
//        model.addAttribute("message", message);
//        model.addAttribute("error", error);
//        return "viewProcessesInactive";
//
    }

    @GetMapping("/view-processes")
    public String displayProcessesList(@RequestParam(required = false) String message,
                                       @RequestParam(required = false) String error,
                                       Model model) {

        model.addAttribute("message", message);
        model.addAttribute("error", error);
        model.addAttribute("processList", insolvencyProcessService.findAll());
        return findPaginated(1, "id", "asc", model);
    }

    @GetMapping("/process-documents")
    public String displayProcessDocumentPage(@RequestParam(required = false) String message,
                                             @RequestParam(required = false) String error,
                                             Model model) {
        model.addAttribute("message", message);
        model.addAttribute("error", error);
        model.addAttribute("processList", insolvencyProcessService.findAll());
        return "processDocumentPage";
    }

    @GetMapping("/process-documents/{id}")
    public String displayProcessDocumentPage(@PathVariable() Long id, Model model) {

        try {
            InsolvencyProcess process = insolvencyProcessService.findInsolvencyProcessById(id);
            model.addAttribute("process", process);
            return "processDocumentPage";
        } catch (Exception exception) {

            return "redirect:/?message=VIEW_PROCESS_FAILED&error=" + exception.getMessage();
        }


    }
    @GetMapping("/delete-process/{id}")
    public String deleteProcess(@PathVariable Long id){
        try{
            insolvencyProcessRepository.deleteById(id);
            return "redirect:/view-processes?message=PROCESS_DELETED_SUCCESSFULLY";
        } catch(Exception ex){
            return "redirect:/view-processes?message=PROCESS_DELETE_FAILED&error="+ ex.getMessage();
        }
    }
    @GetMapping("/page/{pageNo}")
    public String findPaginated(@PathVariable(value="pageNo") int pageNo,
                                @RequestParam("sortField") String sortField,
                                @RequestParam("sortDir") String sortDir, Model model ){
        int pageSize = 5;
        Page<InsolvencyProcess> page = insolvencyProcessService.findPaginated(pageNo, pageSize, sortField, sortDir);
        List<InsolvencyProcess> processList = page.getContent();

        model.addAttribute("currentPage",pageNo );
        model.addAttribute("totalPages",page.getTotalPages());
        model.addAttribute("totalItems",page.getTotalElements());

        model.addAttribute("sortField",sortField);
        model.addAttribute("sortDir",sortDir);
        model.addAttribute("reverseSortDirection",sortDir.equals("asc") ? "desc" : "asc");

        model.addAttribute("processList", processList);

        return "viewProcessesList";
    }

}
