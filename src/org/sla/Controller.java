package org.sla;


import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.FloatStringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Controller {
    Stage primaryStage;

    public ImageView avatarView;
    public Button getAvatarButton;

    public TableView<AlbumModel> tableForData;
    public TableColumn albumRankColumn;
    public TableColumn artistColumn;
    public TableColumn nameColumn;
    public TableColumn yearColumn;
    public TableColumn genreColumn;
    public TableColumn certifiedSalesColumn;
    public TableColumn claimedSalesColumn;

    public Button importButton;
    public Button addButton;
    public Button deleteButton;
    public Button saveButton;

    public Label statusLabel;

    private final String savedDataFilePath = "/tmp/AlbumsDataObjects";

    public void initialize() {
        // make data table editable
        tableForData.setEditable(true);

        // relate table columns with AlbumModel fields
        albumRankColumn.setCellValueFactory(new PropertyValueFactory<AlbumModel, Integer>("ranking"));
        albumRankColumn.setCellFactory(TextFieldTableCell.<AlbumModel, Integer>forTableColumn(new IntegerStringConverter()));
        albumRankColumn.setOnEditCommit(
                new EventHandler<CellEditEvent<AlbumModel, Integer>>() {
                    @Override
                    public void handle(CellEditEvent<AlbumModel, Integer> t) {
                        ((AlbumModel) t.getTableView().getItems().get(t.getTablePosition().getRow())).setRanking(t.getNewValue());
                    }
                }
        );
        artistColumn.setCellValueFactory(new PropertyValueFactory<AlbumModel, String>("artist"));
        artistColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        artistColumn.setOnEditCommit(
                new EventHandler<CellEditEvent<AlbumModel, String>>() {
                    @Override
                    public void handle(CellEditEvent<AlbumModel, String> t) {
                        ((AlbumModel) t.getTableView().getItems().get(t.getTablePosition().getRow())).setArtist(t.getNewValue());
                    }
                }
        );
        nameColumn.setCellValueFactory(new PropertyValueFactory<AlbumModel, String>("name"));
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setOnEditCommit(
                new EventHandler<CellEditEvent<AlbumModel, String>>() {
                    @Override
                    public void handle(CellEditEvent<AlbumModel, String> t) {
                        ((AlbumModel) t.getTableView().getItems().get(t.getTablePosition().getRow())).setName(t.getNewValue());
                    }
                }
        );
        yearColumn.setCellValueFactory(new PropertyValueFactory<AlbumModel, Integer>("year"));
        yearColumn.setCellFactory(TextFieldTableCell.<AlbumModel, Integer>forTableColumn(new IntegerStringConverter()));
        yearColumn.setOnEditCommit(
                new EventHandler<CellEditEvent<AlbumModel, Integer>>() {
                    @Override
                    public void handle(CellEditEvent<AlbumModel, Integer> t) {
                        ((AlbumModel) t.getTableView().getItems().get(t.getTablePosition().getRow())).setYear(t.getNewValue());
                    }
                }
        );
        genreColumn.setCellValueFactory(new PropertyValueFactory<AlbumModel, String>("genre"));
        genreColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        genreColumn.setOnEditCommit(
                new EventHandler<CellEditEvent<AlbumModel, String>>() {
                    @Override
                    public void handle(CellEditEvent<AlbumModel, String> t) {
                        ((AlbumModel) t.getTableView().getItems().get(t.getTablePosition().getRow())).setGenre(t.getNewValue());
                    }
                }
        );
        certifiedSalesColumn.setCellValueFactory(new PropertyValueFactory<AlbumModel, Float>("certifiedSales"));
        certifiedSalesColumn.setCellFactory(TextFieldTableCell.<AlbumModel, Float>forTableColumn(new FloatStringConverter()));
        certifiedSalesColumn.setOnEditCommit(
                new EventHandler<CellEditEvent<AlbumModel, Float>>() {
                    @Override
                    public void handle(CellEditEvent<AlbumModel, Float> t) {
                        ((AlbumModel) t.getTableView().getItems().get(t.getTablePosition().getRow())).setCertifiedSales(t.getNewValue());
                    }
                }
        );
        claimedSalesColumn.setCellValueFactory(new PropertyValueFactory<AlbumModel, Integer>("claimedSales"));
        claimedSalesColumn.setCellFactory(TextFieldTableCell.<AlbumModel, Integer>forTableColumn(new IntegerStringConverter()));
        claimedSalesColumn.setOnEditCommit(
                new EventHandler<CellEditEvent<AlbumModel, Integer>>() {
                    @Override
                    public void handle(CellEditEvent<AlbumModel, Integer> t) {
                        ((AlbumModel) t.getTableView().getItems().get(t.getTablePosition().getRow())).setClaimedSales(t.getNewValue());
                    }
                }
        );

        // Try to read saved data
        File savedDataFile = new File(savedDataFilePath);
        if (savedDataFile.exists()) {
            try {
                FileInputStream file = new FileInputStream(savedDataFilePath);
                AlbumModel.restoreData(file);
                statusLabel.setText("Data restored");
                file.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        // If saved data included an avatar, update the avatar Image
        Image avatar = AlbumModel.getAvatar();
        if (avatar != null) {
            avatarView.setImage(avatar);
            statusLabel.setText("Avatar restored from saved image");
        }

        // Write all of the data into the table
        tableForData.setItems(AlbumModel.getAlbums());
    }

    public void importData() {
        // Bring up File chooser to choose image
        FileChooser dataFileChooser = new FileChooser();
        dataFileChooser.setTitle("Choose File");
        File dataFile = dataFileChooser.showOpenDialog(this.primaryStage);
        // Check if an image was chosen
        if (dataFile != null) {
            // Update model
            AlbumModel.importAlbums(dataFile);
            // Table will be updated automatically
            // Update status
            statusLabel.setText("Imported data from " + dataFile.toURI().toString());
        }
    }

    public void deleteRow() {
        // Selected album
        AlbumModel album = tableForData.getSelectionModel().getSelectedItem();
        if (album != null) {
            // Update model
            AlbumModel.deleteAlbum(album);
            // Update status
            statusLabel.setText("Deleted " + album.getName() + " by " + album.getArtist());
        }
    }

    public void addRow() {
        // Update model
        AlbumModel.addEmptyAlbum();
        // Update status
        statusLabel.setText("Added empty album.  Go ahead and edit its data.");
    }

    public void save() {
        try {
            // Save model
            FileOutputStream file = new FileOutputStream(savedDataFilePath);
            AlbumModel.saveData(file);
            file.close();
            // Update status
            statusLabel.setText("All data saved to: " + savedDataFilePath);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void updateAvatar() {
        // Bring up File chooser to choose image
        FileChooser imageChooser = new FileChooser();
        imageChooser.setTitle("Choose Image");
        // Only let JPG and PNGs to be chosen
        imageChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png", ".gif" )
        );
        File imageFile = imageChooser.showOpenDialog(this.primaryStage);
        // Check if an image was chosen
        if (imageFile != null) {
            Image avatarImage = new Image(imageFile.toURI().toString());
            // Update UI
            avatarView.setImage(avatarImage);
            // Update model
            AlbumModel.setAvatar(avatarImage);
            // Update status
            statusLabel.setText("Updated avatar with " + imageFile.toURI().toString());
        }

    }
}
