package com.example.documentmanagement.insolvencyprocess;

import com.example.documentmanagement.administrator.AdministratorRepository;
import com.example.documentmanagement.insolvencyprocess.InsolvencyProcess;
import com.example.documentmanagement.insolvencyprocess.InsolvencyProcessRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Service
public class InsolvencyProcessService {
    @Autowired
    private InsolvencyProcessRepository insolvencyProcessRepository;
    @Autowired
    private AdministratorRepository administratorRepository;

    @Autowired
    public InsolvencyProcessService(InsolvencyProcessRepository insolvencyProcessRepository, AdministratorRepository administratorRepository) {
        this.insolvencyProcessRepository = insolvencyProcessRepository;
        this.administratorRepository = administratorRepository;
    }

    List<InsolvencyProcess> findAll() {
        return insolvencyProcessRepository.findAll();

    }

    public InsolvencyProcess createInsolvencyProcess(InsolvencyProcess insolvencyProcess) throws Exception {
        if (insolvencyProcess.getRegistrationNumber().isEmpty() ||
                insolvencyProcess.getCompanyName().isEmpty()
                || insolvencyProcess.getCompanyAddress().isEmpty()
                || insolvencyProcess.getCourtName().isEmpty()
                || insolvencyProcess.getCourtCaseNumber().isEmpty()
                || insolvencyProcess.getE_address().isEmpty())
            throw new Exception("Some information is missing, please re-fill the form");

        else {
            insolvencyProcessRepository.saveAndFlush(insolvencyProcess);
            System.out.println(insolvencyProcess);

        }

        return insolvencyProcess;
    }

    Page<InsolvencyProcess> findPaginated(int pageNo, int pageSize, String sortField, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
        return insolvencyProcessRepository.findAll(pageable);
    }


    public void deleteInsolvencyProcess(Long id) {

        insolvencyProcessRepository.deleteById(id);
    }

    public InsolvencyProcess editInsolvencyProcess(InsolvencyProcess process) throws Exception {

        for (InsolvencyProcess currentProcess : insolvencyProcessRepository.findAll()) {

            if (currentProcess.getId().equals(process.getId())) {
                currentProcess.setRegistrationNumber(process.getRegistrationNumber());
                currentProcess.setCompanyName(process.getCompanyName());
                currentProcess.setCompanyAddress(process.getCompanyAddress());
                currentProcess.setCourtName(process.getCourtName());
                currentProcess.setCourtCaseNumber(process.getCourtCaseNumber());
                currentProcess.setCourtDecisionDate(process.getCourtDecisionDate());
                currentProcess.setE_address(process.getE_address());
                currentProcess.setAdmin(process.getAdmin());
                currentProcess.setCaseClosingDate(process.getCaseClosingDate());
                insolvencyProcessRepository.saveAndFlush(currentProcess);

                return currentProcess;
            }
        }

        throw new Exception("process not found by id!");

    }

    public InsolvencyProcess findInsolvencyProcessById(Long id) throws Exception {

        for (InsolvencyProcess process : insolvencyProcessRepository.findAll()) {
            if (process.getId().equals(id))
                return insolvencyProcessRepository.findById(id).get();
        }
        throw new Exception("Insolvency process not found");

    }

    public List<InsolvencyProcess> findClosedProcesses() throws Exception {

       return insolvencyProcessRepository.findInsolvencyProcessesByCaseClosingDateBefore(LocalDate.now());
    }
    public List<InsolvencyProcess> findActiveProcesses() throws Exception {


        return insolvencyProcessRepository.findByCaseClosingDateIsNull();
    }
}
