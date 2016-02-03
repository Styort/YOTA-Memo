package sample;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.print.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ControllerStatement extends ControllerMemo {
    @FXML
    TextField url, iccid, fio, dataBdayAndLocation, phoneNumber;
    HtmlTextInput DateBdayAndLocation;
    boolean clearAllOrNotAll = false;

    public void LoopAuthSt() throws InterruptedException { //авторизируемся каждые 15мин.
        LoopAuth();
    }
    public void LogInYOTASt() throws IOException { //авторизируемся
        LogInYOTA();
    }
    public void ShowAbout() throws IOException { //выводим окно о программе
        AboutShow();
    }
    public void ClearAll(){ //очищаем текстбоксы
        if (!clearAllOrNotAll) {
            url.clear();
        }
        iccid.clear();
        fio.clear();
        dataBdayAndLocation.clear();
        phoneNumber.clear();
        clearAllOrNotAll = false;
    }
    public void GetData(){ //парсим данные с портала
        clearAllOrNotAll = true;
        ClearAll();
        try {
            if (url.getText() == null || url.getText().equals("")) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Ошибка!");
                alert.setHeaderText("Недостаточно данных");
                alert.setContentText("Вставьте ссылку!");

                alert.showAndWait();
            } else {
                if (url.getText().contains(defaultURL)) {
                    //order_birth_data_raw
                    HtmlPage page2 = webClient.getPage(url.getText());
                    Iccid = page2.getFirstByXPath("//input[@id='order_sim_cards_0_iccid']");
                    iccid.appendText(Iccid.getDefaultValue());
                    ClientName = page2.getFirstByXPath("//input[@name='order[name_data_raw]']");
                    DateBdayAndLocation = page2.getFirstByXPath("//input[@id='order_birth_data_raw']");
                    ClientPhoneNumber = page2.getFirstByXPath("//input[@name='order[contact_phone]']");
                    fio.appendText(ClientName.getDefaultValue());
                    String data = DateBdayAndLocation.getDefaultValue().replaceAll("[\\.]","").substring(0,8);
                    String location = DateBdayAndLocation.getDefaultValue().substring(11,DateBdayAndLocation.getDefaultValue().length());
                    dataBdayAndLocation.appendText(data+"              "+location);
                    String ph = ClientPhoneNumber.getDefaultValue().replaceAll("[\\+\\(\\)]",""); //убираем из номера телефона символы + ( )
                    phoneNumber.appendText(ph.substring(1,ph.length())); //добавляем в текстбокс номер телефона без 7-ки.
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Ошибка!");
                    alert.setHeaderText("Неверный формат ссылки.");
                    alert.setContentText("Проверьте правильность введеной ссылки.");
                    alert.showAndWait();
                }
            }
        } catch (NullPointerException npe) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка!");
            alert.setHeaderText("Данные не считаны.");
            alert.setContentText("Необходимо перезайти в портал.");

            alert.showAndWait();
        } catch (IOException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка!");
            alert.setHeaderText("Проверьте интернет соединение");
            alert.setContentText("Похоже у вас оборвалось соединение с интернетом...");
            alert.showAndWait();
        }
    }
    public void PrintStatement() { //выводим на печать памятку
        PrinterJob pjob = PrinterJob.getPrinterJob();
        pjob.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex == 0) {
                Map<TextAttribute, Object> attributes = new HashMap<TextAttribute, Object>();
                attributes.put(TextAttribute.TRACKING, 0.2F);
                Font arial = new Font("Arial", Font.PLAIN, 12).deriveFont(attributes);
                graphics.setFont(arial);

                // Рисуем на graphics то, что должно быть отпечатано.
                //graphics.setFont(new Font("default", Font.PLAIN, 12));
                graphics.drawString(iccid.getText(),50,20);
                graphics.drawString(fio.getText(),60,500);
                graphics.drawString(dataBdayAndLocation.getText(),50,550);
                graphics.drawString(phoneNumber.getText(),65,600);
                return Printable.PAGE_EXISTS;
            }
            return Printable.NO_SUCH_PAGE;
        });
        //проверяем, заполнены ли все поля
        if (iccid.getText().isEmpty() || fio.getText().isEmpty() || phoneNumber.getText().isEmpty() ||
                dataBdayAndLocation.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Ошибка!");
            alert.setHeaderText("Не все данные получены.");
            alert.setContentText("Заполните все поля!");
            alert.showAndWait();
        } else {
            if (pjob.printDialog()){
                try {
                    pjob.print();
                } catch (PrinterException pe) {
                    System.out.println("Error printing: " + pe);
                }
            }
        }
    }
}
