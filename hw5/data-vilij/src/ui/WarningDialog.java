/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ui;

import java.util.Arrays;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import vilij.components.Dialog;

/**
 *
 * @author Spirors
 */
public class WarningDialog extends Stage implements Dialog {
    public enum Option {

        EXIT("Exit"), CANCEL("Cancel");

        @SuppressWarnings("unused")
        private String option;

        Option(String option) { this.option = option; }
    }
    private static WarningDialog dialog;
    
    private Label warningMessage = new Label();
    private Option selectedOption;
    
    private WarningDialog() { /* empty constructor */ }

    public static WarningDialog getDialog() {
        if (dialog == null)
            dialog = new WarningDialog();
        return dialog;
    }

    private void setWarningMessage(String message) { warningMessage.setText(message); }

    private void deleteOptionHistory() { selectedOption = null; }
    
    @Override
    public void init(Stage owner) {
        initModality(Modality.WINDOW_MODAL); // modal => messages are blocked from reaching other windows
        initOwner(owner);

        List<Button> buttons = Arrays.asList(new Button(WarningDialog.Option.EXIT.name()),
                                             new Button(WarningDialog.Option.CANCEL.name()));

        buttons.forEach(button -> button.setOnAction((ActionEvent event) -> {
            this.selectedOption = WarningDialog.Option.valueOf(((Button) event.getSource()).getText());
            this.hide();
        }));

        HBox buttonBox = new HBox(5);
        buttonBox.getChildren().addAll(buttons);

        VBox messagePane = new VBox(warningMessage, buttonBox);
        messagePane.setAlignment(Pos.CENTER);
        messagePane.setPadding(new Insets(10, 20, 20, 20));
        messagePane.setSpacing(10);

        this.setScene(new Scene(messagePane));
    }
    @Override
    public void show(String dialogTitle, String message) {
        deleteOptionHistory();           // delete any previously selected option
        setTitle(dialogTitle);           // set the title of the dialog
        setWarningMessage(message); // set the main error message
        showAndWait();                   // open the dialog and wait for the user to click the close button
    }


    public Option getSelectedOption() { return selectedOption; }
}
