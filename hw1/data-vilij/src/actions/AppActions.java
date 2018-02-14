package actions;

import java.io.File;
import java.io.FileWriter;
import vilij.components.ActionComponent;
import vilij.templates.ApplicationTemplate;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import ui.AppUI;

import vilij.components.ConfirmationDialog.Option;
import vilij.components.ConfirmationDialog;
import vilij.components.Dialog;
import vilij.propertymanager.PropertyManager;

import javafx.stage.FileChooser;
import static settings.AppPropertyTypes.*;
/**
 * This is the concrete implementation of the action handlers required by the application.
 *
 * @author Ritwik Banerjee
 */
public final class AppActions implements ActionComponent {

    /** The application to which this class of actions belongs. */
    private ApplicationTemplate applicationTemplate;

    /** Path to the data file currently active. */
    Path dataFilePath;

    public AppActions(ApplicationTemplate applicationTemplate) {
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    public void handleNewRequest() {
        try {
            if(promptToSave() == true)
                ((AppUI) applicationTemplate.getUIComponent()).clear();
        } catch (IOException ex) {
            Logger.getLogger(AppActions.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void handleSaveRequest() {
        // TODO: NOT A PART OF HW 1
    }

    @Override
    public void handleLoadRequest() {
        // TODO: NOT A PART OF HW 1
    }

    @Override
    public void handleExitRequest() {
        Platform.exit();
    }

    @Override
    public void handlePrintRequest() {
        // TODO: NOT A PART OF HW 1
    }

    public void handleScreenshotRequest() throws IOException {
        // TODO: NOT A PART OF HW 1
    }

    /**
     * This helper method verifies that the user really wants to save their unsaved work, which they might not want to
     * do. The user will be presented with three options:
     * <ol>
     * <li><code>yes</code>, indicating that the user wants to save the work and continue with the action,</li>
     * <li><code>no</code>, indicating that the user wants to continue with the action without saving the work, and</li>
     * <li><code>cancel</code>, to indicate that the user does not want to continue with the action, but also does not
     * want to save the work at this point.</li>
     * </ol>
     *
     * @return <code>false</code> if the user presses the <i>cancel</i>, and <code>true</code> otherwise.
     */
    private boolean promptToSave() throws IOException {
        PropertyManager manager = applicationTemplate.manager;
        ConfirmationDialog cdial = ((ConfirmationDialog) applicationTemplate.getDialog(Dialog.DialogType.CONFIRMATION));
        cdial.show(manager.getPropertyValue(SAVE_UNSAVED_WORK_TITLE.name()), manager.getPropertyValue(SAVE_UNSAVED_WORK.name()));
        if(cdial.getSelectedOption().equals(Option.YES)) {
            FileChooser fc = new FileChooser();
            FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter
                (manager.getPropertyValue(DATA_FILE_EXT_DESC.name()), manager.getPropertyValue(DATA_FILE_EXT.name()));
            fc.getExtensionFilters().add(filter);
            fc.setInitialFileName(manager.getPropertyValue(INITIAL_FILE_NAME.name()));
            fc.setInitialDirectory(new File
                (manager.getPropertyValue(EXTRA_DIRECTORY.name()) + manager.getPropertyValue(DATA_RESOURCE_PATH.name())));
            
            File file = fc.showSaveDialog(((AppUI)applicationTemplate.getUIComponent()).getPrimaryWindow());
            
            FileWriter fw = new FileWriter(file);
            fw.write(((AppUI)applicationTemplate.getUIComponent()).getTextArea().getText());
            fw.close();
            
            return true;
        }
        if(cdial.getSelectedOption().equals(Option.NO)) {
            return true;
        }
        return false;
    }
}
