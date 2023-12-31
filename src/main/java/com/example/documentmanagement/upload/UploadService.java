package com.example.documentmanagement.upload;

import com.example.documentmanagement.insolvencyProcess.InsolvencyProcess;
import com.example.documentmanagement.insolvencyProcess.InsolvencyProcessRepository;
import com.example.documentmanagement.insolvencyProcess.otherExpenses.OtherExpenses;
import com.example.documentmanagement.insolvencyProcess.otherExpenses.OtherExpensesService;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class UploadService {

    private final InsolvencyProcessRepository insolvencyProcessRepository;
    private final OtherExpensesService otherExpensesService;

    @Autowired
    public UploadService(InsolvencyProcessRepository insolvencyProcessRepository, OtherExpensesService otherExpensesService) {

        this.insolvencyProcessRepository = insolvencyProcessRepository;
        this.otherExpensesService = otherExpensesService;

    }

    public void handleCreditorsUpload(InputStream stream, Long processId) throws Exception {

        Workbook workbook = WorkbookFactory.create(stream);
        Sheet dataSheet = workbook.getSheetAt(0);

        int colKreditors = findSheetColumnByName("Kreditors", dataSheet);
        int colPrasijumaVeids = findSheetColumnByName("Prasījuma veids", dataSheet);
        int colPrasijums = findSheetColumnByName("Atzīts / Galvenais prasījums", dataSheet);
        int colPrasijums2 = findSheetColumnByName("Atzīts / Kopā", dataSheet);

        if (colKreditors > -1 && colPrasijumaVeids > -1 && colPrasijums > -1) {
            StringBuilder sbCreditors = new StringBuilder();
            double amountForCreditors = 0;
            String valstsIenemumuDienenstsPrasijums = "";
            String maksatnespejasKontrolesApmers = "";

            for (int i = 1; i <= dataSheet.getLastRowNum(); i++) {
                Row row = dataSheet.getRow(i);
                if (row.getCell(colKreditors) != null && !row.getCell(colKreditors).toString().isEmpty()) {
                    sbCreditors.append(row.getCell(colKreditors).getStringCellValue());
                    sbCreditors.append(';');
                }
                if (row.getCell(colPrasijumaVeids).getStringCellValue().equals("Nenodrošināts")) {
                    amountForCreditors = amountForCreditors + doubleFromString(row.getCell(colPrasijums).getStringCellValue());
                }
                if (row.getCell(colKreditors).getStringCellValue().contains("Valsts ieņēmumu dienests")) {
                    valstsIenemumuDienenstsPrasijums = row.getCell(colPrasijums).getStringCellValue();
                }

                if (row.getCell(colKreditors).getStringCellValue().contains("Maksātnespējas kontroles dienests")) {
                    maksatnespejasKontrolesApmers = row.getCell(colPrasijums2).getStringCellValue();
                }
            }

            if (insolvencyProcessRepository.findById(processId).isPresent()) {
                InsolvencyProcess insolvencyProcess = insolvencyProcessRepository.findById(processId).get();
                insolvencyProcess.setCreditorsList(sbCreditors.substring(0, sbCreditors.length() - 1));
                insolvencyProcess.setCreditorsRequest(amountForCreditors);
                insolvencyProcess.setValstsIenemumuApmers(valstsIenemumuDienenstsPrasijums);
                insolvencyProcess.setMaksatnespejasKontrolesApmers(maksatnespejasKontrolesApmers);
                insolvencyProcessRepository.save(insolvencyProcess);
            }
        }
    }

    public void handleCostsUpload(InputStream stream, Long processId) throws Exception {
        Workbook workbook = WorkbookFactory.create(stream);

        Sheet dataSheet = workbook.getSheetAt(0);
        int colIegutieNaudas = findSheetColumnByName("Iegūtie naudas līdzekļi no", dataSheet);
        int colSumma = findSheetColumnByName("Summa", dataSheet);

        double neikilataMantaSum = 0;

        if (colIegutieNaudas > -1 && colSumma > -1) {

            for (int i = 1; i <= dataSheet.getLastRowNum(); i++) {
                Row row = dataSheet.getRow(i);
                if (row.getCell(colIegutieNaudas).getStringCellValue().equals("Neieķīlātā manta")) {
                    neikilataMantaSum = neikilataMantaSum + doubleFromString(row.getCell(colSumma).getStringCellValue());
                }
            }
        }

        if (insolvencyProcessRepository.findById(processId).isPresent()) {
            InsolvencyProcess insolvencyProcess = insolvencyProcessRepository.findById(processId).get();
            insolvencyProcess.setNeikilataMantaSum(neikilataMantaSum);
            insolvencyProcessRepository.save(insolvencyProcess);
        }
    }

    public void handleMoneyUpload(InputStream stream, Long processId) throws Exception {

        Workbook workbook = WorkbookFactory.create(stream);

        Sheet dataSheet = workbook.getSheetAt(0);
        int colNaudasLidzekli = findSheetColumnByName("Naudas līdzekļi kontā perioda sākumā (EUR)", dataSheet);

        String cellNaudasLidzekli = "";

        if (colNaudasLidzekli > -1) {
            cellNaudasLidzekli = dataSheet.getRow(dataSheet.getLastRowNum()).getCell(colNaudasLidzekli).getStringCellValue();
        }

        if (insolvencyProcessRepository.findById(processId).isPresent()) {
            InsolvencyProcess insolvencyProcess = insolvencyProcessRepository.findById(processId).get();
            insolvencyProcess.setProcessMoney(cellNaudasLidzekli);
            insolvencyProcessRepository.save(insolvencyProcess);
        }
    }

    public void handleExpensesUpload(InputStream stream, Long processId) throws Exception {

        Workbook workbook = WorkbookFactory.create(stream);
        Sheet dataSheet = workbook.getSheetAt(0);
        int colIzmaksasRadusasNo = findSheetColumnByName("Izmaksas radušās no", dataSheet);
        int colPozicijas = findSheetColumnByName("Pozīcijas nosaukums", dataSheet);
        int colSanemejs = findSheetColumnByName("Saņēmējs", dataSheet);
        //   int colSniegtaisPakalpojums = findSheetColumnByName("Sniegtais pakalpojums", dataSheet);
        int colIzmaksuDatums = findSheetColumnByName("Izmaksu rašanās datums", dataSheet);
        //  int colSegtaSumma = findSheetColumnByName("Segtā summa", dataSheet);
        int colSegsanasDatums = findSheetColumnByName("Segšanas datums", dataSheet);
        int colNavApmaksatas = findSheetColumnByName("Radušās un nav apmaksātas izmaksas", dataSheet);

        int colIzmaksuSum = findSheetColumnByName("Izmaksu summa", dataSheet);

        double izmaksasTotal = 0;
        double administratorSalary = 0;

        String assetType = "";
        String expName = "";
        String sanemejs = "";
        String creatingDate = "";
        String otherDate = "";
        double sum = 0;
        double unpaid = 0;

        List<OtherExpenses> expensesList = new ArrayList<>();
        InsolvencyProcess pro = insolvencyProcessRepository.findById(processId).get();
        pro.setOtherExpenses(new ArrayList<>());
        insolvencyProcessRepository.save(pro);
        otherExpensesService.deleteAllByProcess(insolvencyProcessRepository.findById(processId).get());

        if (colIzmaksasRadusasNo > -1 && colPozicijas > -1 && colIzmaksuSum > -1) {

            for (int i = 1; i <= dataSheet.getLastRowNum() - 2; i++) {
                Row row = dataSheet.getRow(i);
                String cellIsmaksasRadusas = row.getCell(colIzmaksasRadusasNo).getStringCellValue();
                String cellPozicijas = row.getCell(colPozicijas).getStringCellValue();
                String stringExpensesValue = row.getCell(colIzmaksuSum).getStringCellValue();

                if (cellIsmaksasRadusas.equals("Neieķīlātā manta") && !cellPozicijas.contains("Administratora")) {
                    izmaksasTotal = izmaksasTotal + doubleFromString(stringExpensesValue);
                }

                if (cellIsmaksasRadusas.equals("Neieķīlātā manta") || cellIsmaksasRadusas.equals("Ieķīlātā manta")) {
                    assetType = row.getCell(colIzmaksasRadusasNo).getStringCellValue();
                    expName = row.getCell(colPozicijas).getStringCellValue();
                    sanemejs = row.getCell(colSanemejs).getStringCellValue();
                    creatingDate = row.getCell(colSegsanasDatums).getStringCellValue();
                    otherDate = row.getCell(colIzmaksuDatums).getStringCellValue();

                    if (!row.getCell(colIzmaksuSum).getStringCellValue().isEmpty()) {
                        sum = doubleFromString(row.getCell(colIzmaksuSum).getStringCellValue());

                    }
                    if (!row.getCell(colNavApmaksatas).getStringCellValue().isEmpty()) {
                        unpaid = doubleFromString(row.getCell(colNavApmaksatas).getStringCellValue());
                    }
                }

                OtherExpenses otherExpenses = new OtherExpenses();

                otherExpenses.setName(expName);
                otherExpenses.setAssetType(assetType);
                otherExpenses.setSum(sum);
                otherExpenses.setUnpaid(unpaid);
                otherExpenses.setRecipient(sanemejs);
                otherExpenses.setCreatingDate(creatingDate);
                otherExpenses.setOtherDate(otherDate);
                otherExpenses.setInsolvencyProcess(insolvencyProcessRepository.findById(processId).get());

                expensesList.add(otherExpenses);
                otherExpensesService.createOtherExpenses(otherExpenses);

            }

        }

        InsolvencyProcess insolvencyProcess = insolvencyProcessRepository.findById(processId).get();

        insolvencyProcess.setOtherExpenses(new ArrayList<>());
        insolvencyProcess.setOtherExpenses(expensesList);

        insolvencyProcess.setTotalExpenses(izmaksasTotal);
        insolvencyProcess.setAdminSalary(administratorSalary);

        insolvencyProcessRepository.save(insolvencyProcess);

    }

    public void handleAssetsUpload(InputStream stream, Long processId) throws Exception {
        Workbook workbook = WorkbookFactory.create(stream);
        Sheet dataSheet = workbook.getSheetAt(0);

        int colMantasTips = findSheetColumnByName("Mantas tips", dataSheet);
        int colNumMpaRiciba = findSheetColumnByName("MPA rīcība", dataSheet);
        int colNumApmers = findSheetColumnByName("Iegūto naudas līdzekļu apmērs", dataSheet);

        StringBuilder sbMpa_neiekilata = new StringBuilder();
        StringBuilder sbSums_neiekilata = new StringBuilder();
        StringBuilder sbMpa_iekilata = new StringBuilder();
        StringBuilder sbSums_iekilata = new StringBuilder();

        double total_neiekilata = 0;
        double total_iekilata = 0;

        if (colMantasTips > -1 && colNumMpaRiciba > -1 && colNumApmers > -1) {

            boolean securedAssets = false;

            for (int i = 1; i <= dataSheet.getLastRowNum(); i++) {
                if (dataSheet.getRow(i).getCell(colMantasTips).getStringCellValue().equals("Ieķīlātā manta")) {
                    securedAssets = true;
                    break;
                }
            }

            for (int i = 1; i <= dataSheet.getLastRowNum(); i++) {
                Row row = dataSheet.getRow(i);
                if (row.getCell(colNumMpaRiciba) != null &&
                        !row.getCell(colNumMpaRiciba).toString().isEmpty() &&
                        row.getCell(colNumApmers) != null &&
                        !row.getCell(colNumApmers).toString().isEmpty() &&
                        row.getCell(colMantasTips).getStringCellValue().equals("Ieķīlātā manta")) {

                    appendCellvalueToCsvList(row.getCell(colNumMpaRiciba), sbMpa_iekilata);
                    appendCellvalueToCsvList(row.getCell(colNumApmers), sbSums_iekilata);

                    total_iekilata = total_iekilata + doubleFromString(row.getCell(colNumApmers).getStringCellValue());

                } else if (row.getCell(colNumMpaRiciba) != null &&
                        !row.getCell(colNumMpaRiciba).toString().isEmpty() &&
                        row.getCell(colNumApmers) != null &&
                        !row.getCell(colNumApmers).toString().isEmpty()
                ) {
                    appendCellvalueToCsvList(row.getCell(colNumMpaRiciba), sbMpa_neiekilata);
                    appendCellvalueToCsvList(row.getCell(colNumApmers), sbSums_neiekilata);

                    total_neiekilata = total_neiekilata + doubleFromString(row.getCell(colNumApmers).getStringCellValue());

                }
            }
            String assets_neiekilata = "-";
            String assetSums_neiekilata = "-";
            String assets_iekilata = "-";
            String assetSums_iekilata = "-";

            if (sbMpa_neiekilata.length() > 0) {
                assets_neiekilata = sbMpa_neiekilata.substring(0, sbMpa_neiekilata.length() - 1);
            }

            if (sbSums_neiekilata.length() > 0) {
                assetSums_neiekilata = sbSums_neiekilata.substring(0, sbSums_neiekilata.length() - 1);
            }

            if (sbMpa_iekilata.length() > 0) {
                assets_iekilata = sbMpa_iekilata.substring(0, sbMpa_iekilata.length() - 1);
            }

            if (sbSums_iekilata.length() > 0) {
                assetSums_iekilata = sbSums_iekilata.substring(0, sbSums_iekilata.length() - 1);
            }

            if (insolvencyProcessRepository.findById(processId).isPresent()) {

                InsolvencyProcess insolvencyProcess = insolvencyProcessRepository.findById(processId).get();
                insolvencyProcess.setAssetsList_iekilata(assets_iekilata);
                insolvencyProcess.setAssetsList_neiekilata(assets_neiekilata);
                insolvencyProcess.setAssetsListCosts_iekilata(assetSums_iekilata);
                insolvencyProcess.setAssetsListCosts_neiekilata(assetSums_neiekilata);
                insolvencyProcess.setAssetsTotalCosts_iekilata(String.format("%1.2f", total_iekilata));
                insolvencyProcess.setAssetsTotalCosts_neiekilata(String.valueOf(total_neiekilata));

                insolvencyProcess.setSecuredAssets(securedAssets);

                insolvencyProcessRepository.save(insolvencyProcess);
            }
        }
    }

    private void appendCellvalueToCsvList(Cell cell, StringBuilder stringValue) {
        cell.setCellType(CellType.STRING);
        stringValue.append(cell.getStringCellValue());
        stringValue.append(';');

    }

    private int findSheetColumnByName(String columnName, Sheet dataSheet) {
        int columnIndex = -1;
        for (int i = 0; i <= dataSheet.getRow(0).getLastCellNum(); i++) {
            Cell cell1 = dataSheet.getRow(0).getCell(i);
            String cellValue1 = cell1.getStringCellValue();
            if (columnName.equals(cellValue1)) {
                columnIndex = i;
                break;
            }
        }
        return columnIndex;
    }

    public double doubleFromString(String string) {
        return Double.parseDouble(string.replace(',', '.'));
    }
}
