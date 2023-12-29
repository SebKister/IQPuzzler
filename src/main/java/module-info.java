module com.arianesline.iqpuzzle {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.arianesline.iqpuzzle to javafx.fxml;
    exports com.arianesline.iqpuzzle;
}