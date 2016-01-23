package sample;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.awt.*;
import java.awt.print.*;
import java.io.*;

public class Controller{
    @FXML
    private TextField url,iccid,address,data,clientName,clientPhoneNumber,comment;
    @FXML
    MenuItem clearData;
    @FXML
    CheckBox authLoopCheckBox,fillCommentCheckBox;
    WebClient webClient = new WebClient(BrowserVersion.CHROME);
    boolean clearAllOrNotAll=false;
    HtmlTextInput Iccid,Address,Data,TimeBegin,TimeEnd,ClientName,ClientPhoneNumber;
    HtmlTextArea Comment;
    String defaultURL = "https://partner.yota.ru/rd/vox/order/edit/";
    Thread timeredLogin;
    boolean info = true;

    public void LoopAuth() throws InterruptedException { //автологин каждые 15 мин.
        if(authLoopCheckBox.isSelected()){
            timeredLogin = new Thread(()->{
                try {
                    while (true) {
                        Platform.runLater(()->{
                            try {
                                info=false;
                                LogInYOTA();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                        Thread.sleep(1000*60*15);
                    }
                }
                catch (Exception e){
                    System.out.print("End...");
                }
            });
            timeredLogin.start();
        }
        else
        if (timeredLogin!=null) {
            timeredLogin.interrupt();
            info=true;
        }
    }
    public void IncomingData() throws IOException { //получаем данные с портала
        clearAllOrNotAll=true;
        ClearAll();
        try {
            if (url.getText()==null||url.getText().equals("")){
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Ошибка!");
                alert.setHeaderText("Недостаточно данных");
                alert.setContentText("Вставьте ссылку!");

                alert.showAndWait();
            }
            else {
                if(url.getText().contains(defaultURL)){
                    HtmlPage page2 = webClient.getPage(url.getText());
                    Iccid = page2.getFirstByXPath("//input[@id='order_sim_cards_0_iccid']");
                    iccid.appendText(Iccid.getDefaultValue());
                    Address = page2.getFirstByXPath("//input[@name='order[delivery_address]']");
                    Data = page2.getFirstByXPath("//input[@name='order[expected_delivery_date]']");
                    TimeBegin  = page2.getFirstByXPath("//input[@name='order[expected_delivery_time_begin]']");
                    TimeEnd = page2.getFirstByXPath("//input[@name='order[expected_delivery_time_end]']");
                    ClientName = page2.getFirstByXPath("//input[@name='order[name_data_raw]']");
                    ClientPhoneNumber = page2.getFirstByXPath("//input[@name='order[contact_phone]']");
                    Comment = page2.getFirstByXPath("//textarea[@name='order[comment]']");
                    address.appendText(Address.getDefaultValue());
                    data.appendText(Data.getDefaultValue()+" c "+ TimeBegin.getDefaultValue()+" по "+TimeEnd.getDefaultValue());
                    clientName.appendText(ClientName.getDefaultValue());
                    clientPhoneNumber.appendText(ClientPhoneNumber.getDefaultValue());
                    comment.appendText(Comment.getDefaultValue());
                }
                else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Ошибка!");
                    alert.setHeaderText("Неверный формат ссылки.");
                    alert.setContentText("Проверьте правильность введеной ссылки.");
                    alert.showAndWait();
                }
            }
        }
        catch (NullPointerException npe) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка!");
            alert.setHeaderText("Данные не считаны.");
            alert.setContentText("Необходимо перезайти в портал.");

            alert.showAndWait();
        }
        catch (IOException ex){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка!");
            alert.setHeaderText("Проверьте интернет соединение");
            alert.setContentText("Похоже у вас оборвалось соединение с интернетом...");
            alert.showAndWait();
        }
    }
    public void ClearAll(){ //стереть все данные
        if(!clearAllOrNotAll){
            url.clear();
        }
        iccid.clear();
        address.clear();
        data.clear();
        clientName.clear();
        clientPhoneNumber.clear();
        comment.clear();
        clearAllOrNotAll=false;
    }
    public void WebBrowserSettings(){ //настраиваем клиент
        webClient.getOptions().setPrintContentOnFailingStatusCode(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setJavaScriptEnabled(false);
    }
    public void LogInYOTA() throws IOException { //авторизируемся в портале
        WebBrowserSettings();
        try {
            HtmlPage page1 = webClient.getPage("https://partner.yota.ru/rd/login");
            HtmlForm form = page1.getFirstByXPath("//form[@action='/rd/login_check']");

            HtmlTextInput login =  form.getInputByName("_username");
            HtmlPasswordInput password = form.getInputByName("_password");
            login.setValueAttribute("LOGIN");
            password.setValueAttribute("PASSWORD");
            HtmlSubmitInput button = page1.getFirstByXPath("//input[@id='send_id' and @type='submit']");
            button.click();

            if(info){
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Информация");
                alert.setHeaderText(null);
                alert.setContentText("Авторизация прошла успешно!");
                alert.showAndWait();
            }
        }
        catch (IOException ex){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка!");
            alert.setHeaderText("Проверьте интернет соединение");
            alert.setContentText("Похоже у вас оборвалось соединение с интернетом...");
            alert.showAndWait();
        }

    }
    public void PrintMemo(){ //выводим на печать памятку

        PrinterJob pjob = PrinterJob.getPrinterJob();
        pjob.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex == 0) {
                // Рисуем на graphics то, что должно быть отпечатано.
                double pageWidth = pageFormat.getImageableWidth();

                FontMetrics fm = graphics.getFontMetrics(graphics.getFont());
                int start = (int)pageWidth/2;
                graphics.setFont(new Font("default", Font.BOLD, 12));
                graphics.drawString("Памятка", start-fm.stringWidth("Памятка")/2, 50);
                graphics.drawString("ICCID:",start-fm.stringWidth("ICCID:")/2,65);
                graphics.setFont(new Font("default", Font.PLAIN, 12));
                graphics.drawString(iccid.getText(), start-fm.stringWidth(iccid.getText())/2, 80);
                graphics.setFont(new Font("default", Font.BOLD, 12));
                graphics.drawString("Адрес доставки:",start-fm.stringWidth("Адрес доставки:")/2,95);
                graphics.setFont(new Font("default", Font.PLAIN, 12));
                graphics.drawString(address.getText(),start-fm.stringWidth(address.getText())/2, 110);
                graphics.setFont(new Font("default", Font.BOLD, 12));
                graphics.drawString("Дата и время доставки:",start-fm.stringWidth("Дата и время доставки:")/2,125);
                graphics.setFont(new Font("default", Font.PLAIN, 12));
                graphics.drawString(data.getText(), start-fm.stringWidth(data.getText())/2, 140);
                graphics.setFont(new Font("default", Font.BOLD, 12));
                graphics.drawString("ФИО Клиента:",start-fm.stringWidth("ФИО Клиента:")/2,155);
                graphics.setFont(new Font("default", Font.PLAIN, 12));
                graphics.drawString(clientName.getText(), start-fm.stringWidth(clientName.getText())/2, 170);
                graphics.setFont(new Font("default", Font.BOLD, 12));
                graphics.drawString("Номер телефона:",start-fm.stringWidth("Номер телефона:")/2,185);
                graphics.setFont(new Font("default", Font.PLAIN, 12));
                graphics.drawString(clientPhoneNumber.getText(), start-fm.stringWidth(clientPhoneNumber.getText())/2, 200);
                graphics.setFont(new Font("default", Font.BOLD, 12));
                graphics.drawString("Комментарий:",start-fm.stringWidth("Комментарий:")/2,215);
                graphics.setFont(new Font("default", Font.PLAIN, 12));
                if(fillCommentCheckBox.isSelected()){
                    graphics.drawRect(195,219,200,13);
                    graphics.setColor(Color.lightGray);
                    graphics.fillRect(195,219,200,13);
                    graphics.setColor(Color.black);
                }
                graphics.drawString(comment.getText(), start-fm.stringWidth(comment.getText())/2, 230);
                graphics.setFont(new Font("default", Font.BOLD, 12));
                graphics.drawRect(245,234,100,13);
                graphics.setColor(Color.lightGray);
                graphics.fillRect(245,234,100,13);
                graphics.setColor(Color.black);
                graphics.drawString("Заполнить ПД!",start-fm.stringWidth("Заполнить ПД!")/2,245);
                graphics.setFont(new Font("default", Font.PLAIN, 12));
                graphics.drawString("Уважаемые партнеры и клиенты! Просим Вас заполнять «Заявление о присоединении к",
                        start - fm.stringWidth("Уважаемые партнеры и клиенты! Просим Вас заполнять «Заявление о присоединении к") / 2, 260);
                graphics.drawString("публичному договору» печатными буквами. В случае некорректного заполнения",
                        start-fm.stringWidth("публичному договору» печатными буквами. В случае некорректного заполнения")/2,275);
                graphics.drawString("(неполная, неразборчиво указанная информация) могут возникнуть сложности c обслуживанием",
                        start-fm.stringWidth("неполная, неразборчиво указанная информация) могут возникнуть сложности c обслуживанием")/2,290);
                graphics.drawString("sim-карты. Написать сотруднику Контактного центра можно через мобильное приложение",
                        start-fm.stringWidth("sim-карты. Написать сотруднику Контактного центра можно через мобильное приложение")/2,305);
                graphics.drawString("или через чат на сайте www.yota.ru. Приложение «YOTA» доступно для скачивания",
                        start-fm.stringWidth("или через чат на сайте www.yota.ru. Приложение «YOTA» доступно для скачивания")/2,320);
                graphics.drawString("на Google Play (Android) или на AppStore (IOS).",
                        start-fm.stringWidth("на Google Play (Android) или на AppStore (IOS).")/2,335);
                graphics.drawString("USSD-команды: 100# — проверить баланс; 101# — остаток минут; *103# — уточнить свой номер",
                        start-fm.stringWidth("USSD-команды: 100# — проверить баланс; 101# — остаток минут; *103# — уточнить свой номер")/2,350);

                return Printable.PAGE_EXISTS;
            }

            return Printable.NO_SUCH_PAGE;
        });
        //проверяем, заполнены ли все поля
        if(iccid.getText().isEmpty()||address.getText().isEmpty()||data.getText().isEmpty()||clientName.getText().isEmpty()
                ||clientPhoneNumber.getText().isEmpty()||comment.getText().isEmpty()){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Ошибка!");
            alert.setHeaderText("Не все данные получены.");
            alert.setContentText("Заполните все поля!");
            alert.showAndWait();
        }
        else {
                try {
                    pjob.print();
                } catch(PrinterException pe) {
                    System.out.println("Error printing: " + pe);
                }
        }
    }
    public void AboutShow() throws IOException { //показываем окно "О программе"
        Parent root = FXMLLoader.load(getClass().getResource("About.fxml"));
        Stage stage = new Stage();
        stage.setTitle("О программе");
        stage.setScene(new Scene(root, 400, 200));
        stage.show();

    }
}
