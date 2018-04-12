package actions;

import dataprocessors.TSDProcessor;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import settings.AppPropertyTypes;
import vilij.components.ActionComponent;
import vilij.components.ConfirmationDialog;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import vilij.settings.PropertyTypes;
import vilij.templates.ApplicationTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import javafx.scene.SnapshotParameters;
import ui.AppUI;
import static vilij.settings.PropertyTypes.SAVE_WORK_TITLE;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javax.imageio.ImageIO;

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

    /** The boolean property marking whether or not there are any unsaved changes. */
    SimpleBooleanProperty isUnsaved;

    public AppActions(ApplicationTemplate applicationTemplate) {
        this.applicationTemplate = applicationTemplate;
        this.isUnsaved = new SimpleBooleanProperty(false);
    }

    public void setIsUnsavedProperty(boolean property) { isUnsaved.set(property); }

    @Override
    public void handleNewRequest() {
        PropertyManager manager = applicationTemplate.manager;
        dataFilePath = null;
        AppUI ui = ((AppUI) applicationTemplate.getUIComponent());
        ui.showData();
        ui.getTextArea().setDisable(false);
        ui.showToggle();
        
        
        
//        try {
//            if (!isUnsaved.get() || promptToSave()) {
//                applicationTemplate.getDataComponent().clear();
//                applicationTemplate.getUIComponent().clear();
//                ((AppUI) applicationTemplate.getUIComponent()).getScreenShotButton().setDisable(true);
//                isUnsaved.set(false);
//                dataFilePath = null;
//            }
//        }catch (Exception e){}
    }

    @Override
    public void handleSaveRequest() {
        PropertyManager manager = applicationTemplate.manager;
        TSDProcessor processor = new TSDProcessor();
        String t = ((AppUI)applicationTemplate.getUIComponent()).getCurrentText();
        try {
            processor.processString(t);
            if(dataFilePath != null) {
                save();
                ((AppUI) applicationTemplate.getUIComponent()).getSaveButton().setDisable(true);
            }    
            else{
                File selected = openFC(manager.getPropertyValue(AppPropertyTypes.SAVE_OPTION.name()));    
                if (selected != null) {
                    dataFilePath = selected.toPath();
                    save();
                    ((AppUI) applicationTemplate.getUIComponent()).getSaveButton().setDisable(true);
                }
            }
        }
        catch (Exception e){
            if(processor.getDupeLine() != -1){
                ErrorDialog     ddialog   = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                ddialog.show(manager.getPropertyValue(PropertyTypes.SAVE_ERROR_TITLE.name()), 
                        manager.getPropertyValue(AppPropertyTypes.DUPLICATE_AT.name())+ processor.getErrorLines().get(0)+processor.getDupeName());
            }else{
                if(!processor.getErrorLines().isEmpty()){
                    ErrorDialog     edialog   = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                    edialog.show(manager.getPropertyValue(PropertyTypes.SAVE_ERROR_TITLE.name()), 
                            manager.getPropertyValue(AppPropertyTypes.ERROR_AT.name())+ processor.getErrorLines().get(0));
                }else   
                    errorHandlingHelper();
            }
        }
    }

    @Override
    public void handleLoadRequest() {   
        PropertyManager manager = applicationTemplate.manager;
        try {
            File selected = openFC(manager.getPropertyValue(AppPropertyTypes.OPEN_OPTION.name()));
            if (selected != null) {
                dataFilePath = selected.toPath();
                load();
                AppUI ui = ((AppUI) applicationTemplate.getUIComponent());
                ui.getSaveButton().setDisable(true);
            }
        } catch (IOException ex){errorHandlingHelper();}
    }

    @Override
    public void handleExitRequest() {
        try {
            if(((AppUI) applicationTemplate.getUIComponent()).getCurrentText() == null)
                System.exit(0);
            if (!isUnsaved.get() || promptToSave())
                System.exit(0);
        } catch (IOException e) { errorHandlingHelper(); }
    }

    @Override
    public void handlePrintRequest() {
        // TODO: NOT A PART OF HW 1
    }

    public void handleScreenshotRequest() throws IOException {
        PropertyManager manager = applicationTemplate.manager;
        FileChooser fileChooser = new FileChooser();
        String dataDirPath = manager.getPropertyValue(AppPropertyTypes.DATA_RESOURCE_PATH.name());
        
        fileChooser.setInitialDirectory(new File(dataDirPath));
        fileChooser.setTitle(manager.getPropertyValue(AppPropertyTypes.SAVE_IMAGE_TITLE.name()));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNG", 
                    manager.getPropertyValue(AppPropertyTypes.PNG_EXT.name())));
        
        Image image = ((AppUI) applicationTemplate.getUIComponent()).getChart().snapshot(new SnapshotParameters(), null);
        ImageView imageView = new ImageView(image);
        File selected = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
        if (selected != null) {
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(imageView.getImage(), null), "png", selected);
            } catch (IOException ex) {
                errorHandlingHelper();       
            }
        }
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
        PropertyManager    manager = applicationTemplate.manager;
        ConfirmationDialog dialog  = ConfirmationDialog.getDialog();
        dialog.show(manager.getPropertyValue(AppPropertyTypes.SAVE_UNSAVED_WORK_TITLE.name()),
                    manager.getPropertyValue(AppPropertyTypes.SAVE_UNSAVED_WORK.name()));

        if (dialog.getSelectedOption() == null) return false; // if user closes dialog using the window's close button

        if (dialog.getSelectedOption().equals(ConfirmationDialog.Option.YES)) {
            TSDProcessor processor = new TSDProcessor();
            String t = ((AppUI)applicationTemplate.getUIComponent()).getCurrentText();
            try {
                processor.processString(t);
            
            }catch (Exception e){
                if(processor.getDupeLine() != -1){
                    ErrorDialog     ddialog   = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                    ddialog.show(manager.getPropertyValue(PropertyTypes.SAVE_ERROR_TITLE.name()), 
                            manager.getPropertyValue(AppPropertyTypes.DUPLICATE_AT.name())+ processor.getErrorLines().get(0)+processor.getDupeName());
                }else{
                    if(!processor.getErrorLines().isEmpty()){
                        ErrorDialog     edialog   = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                        edialog.show(manager.getPropertyValue(PropertyTypes.SAVE_ERROR_TITLE.name()), 
                                manager.getPropertyValue(AppPropertyTypes.ERROR_AT.name())+ processor.getErrorLines().get(0));
                    }else   
                        errorHandlingHelper();
                }
                return false;
            }
            if (dataFilePath == null) {
                File selected = openFC(manager.getPropertyValue(AppPropertyTypes.SAVE_OPTION.name()));
                if (selected != null) {
                    dataFilePath = selected.toPath();
                    save();
                } else return false; // if user presses escape after initially selecting 'yes'
            } else
                save();
        }
        return !dialog.getSelectedOption().equals(ConfirmationDialog.Option.CANCEL);
    }

    private void save() throws IOException {
        applicationTemplate.getDataComponent().saveData(dataFilePath);
        isUnsaved.set(false);
    }
    private void load() throws IOException {
        applicationTemplate.getDataComponent().loadData(dataFilePath);
    }

    private void errorHandlingHelper() {
        ErrorDialog     dialog   = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
        PropertyManager manager  = applicationTemplate.manager;
        String          errTitle = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_TITLE.name());
        String          errMsg   = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_MSG.name());
        String          errInput = manager.getPropertyValue(AppPropertyTypes.SPECIFIED_FILE.name());
        dialog.show(errTitle, errMsg + errInput);
    }
    private File openFC(String s) throws IOException {
        PropertyManager    manager = applicationTemplate.manager;
        FileChooser fileChooser = new FileChooser();
        String dataDirPath = manager.getPropertyValue(AppPropertyTypes.DATA_RESOURCE_PATH.name());
          
        if (dataDirPath == null)
            throw new FileNotFoundException(manager.getPropertyValue(AppPropertyTypes.RESOURCE_SUBDIR_NOT_FOUND.name()));
            
        fileChooser.setInitialDirectory(new File(dataDirPath));
        if(s.equals(manager.getPropertyValue(AppPropertyTypes.SAVE_OPTION.name()))){
            fileChooser.setTitle(manager.getPropertyValue(SAVE_WORK_TITLE.name()));
        }
        if(s.equals(manager.getPropertyValue(AppPropertyTypes.OPEN_OPTION.name()))){
            fileChooser.setTitle(manager.getPropertyValue(AppPropertyTypes.OPEN_WORK_TITLE.name()));
        }
        fileChooser.setTitle(manager.getPropertyValue(SAVE_WORK_TITLE.name()));
            
        String description = manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT_DESC.name());
        String extension   = manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT.name());
        ExtensionFilter extFilter = new ExtensionFilter(description, extension);
            
        fileChooser.getExtensionFilters().add(extFilter);
        File selected = null;
        if(s.equals(manager.getPropertyValue(AppPropertyTypes.SAVE_OPTION.name()))){
           selected = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
           return selected;
        }
        if(s.equals(manager.getPropertyValue(AppPropertyTypes.OPEN_OPTION.name()))){
            selected = fileChooser.showOpenDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
            return selected;
        }
        return selected;
    }
}
