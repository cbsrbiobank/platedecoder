package org.biobank.platedecoder.ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import java.util.Set;

import org.biobank.platedecoder.dmscanlib.CellRectangle;
import org.biobank.platedecoder.dmscanlib.DecodeOptions;
import org.biobank.platedecoder.dmscanlib.DecodeResult;
import org.biobank.platedecoder.dmscanlib.ScanLib;
import org.biobank.platedecoder.model.BarcodePosition;
import org.biobank.platedecoder.model.PlateOrientation;
import org.controlsfx.tools.Borders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: add instructions for user for how to use the "Decode" button
public class ImageAndGrid extends AbstractSceneRoot {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(ImageAndGrid.class);

    private PlateTypeChooser plateTypeChooser;

    private Optional<Image> imageMaybe;

    private ImageView imageView;

    private Group imageGroup;

    private ScrollPane imagePane;

    private BorderPane borderPane;

    private WellGrid wellGrid;

    private Label filenameLabel;

    private URL imageUrl;

    private Button continueBtn;

    public ImageAndGrid() {
        super("Align grid with barcodes");
    }

    @Override
    protected Node creatContents() {
        Pane controls = createControlsPane();
        Pane imagePane = createImagePane();

        borderPane = new BorderPane();
        borderPane.setPadding(new Insets(5, 5, 5, 5));
        borderPane.setLeft(controls);
        borderPane.setCenter(imagePane);
        return borderPane;
    }

    private Pane createControlsPane() {
        plateTypeChooser = new PlateTypeChooser();
        plateTypeChooser.addListenerToPlateTypeSelectionModel((observable, oldValue, newValue) -> {
                wellGrid.plateTypeSelectionChanged(newValue);
                addWellGrid();
            });

        GridPane grid = new GridPane();
        grid.add(plateTypeChooser, 0, 0);
        grid.add(createOrientationControls(), 0, 1);
        grid.add(createBarcodePisitionsControls(), 0, 2);
        grid.add(createDecodeButton(), 0, 3);
        grid.add(createContinueButton(), 0, 4);

        return grid;
    }

    private Node createOrientationControls() {
        final ToggleGroup toggleGroup = new ToggleGroup();

        RadioButton landscape = new RadioButton("Landscape");
        landscape.setToggleGroup(toggleGroup);
        RadioButton portrait = new RadioButton("Portrait");
        portrait.setToggleGroup(toggleGroup);

        final VBox orientationBox = new VBox(5, landscape, portrait);

        return Borders.wrap(orientationBox)
            .etchedBorder().title("Orientation").build()
            .build();
    }

    private Node createBarcodePisitionsControls() {
        final ToggleGroup toggleGroup = new ToggleGroup();

        RadioButton tubeTops = new RadioButton("Tube tops");
        tubeTops.setToggleGroup(toggleGroup);
        RadioButton tubeBottoms = new RadioButton("Tube bottoms");
        tubeBottoms.setToggleGroup(toggleGroup);

        final VBox orientationBox = new VBox(5, tubeTops, tubeBottoms);

        return Borders.wrap(orientationBox)
            .etchedBorder().title("Barcode Positions").build()
            .build();
    }

    private Node createDecodeButton() {
        Button button = new Button("Decode");
        button.setOnAction(e -> decodeImage());

        final AnchorPane anchorPane = new AnchorPane();
        AnchorPane.setTopAnchor(button, 0.0);
        AnchorPane.setRightAnchor(button, 0.0);
        anchorPane.getChildren().add(button);

        GridPane.setMargin(anchorPane, new Insets(5));

        return anchorPane;
    }

    private Node createContinueButton() {
        continueBtn = new Button("Continue");
        continueBtn.setDisable(true);

        final AnchorPane anchorPane = new AnchorPane();
        AnchorPane.setTopAnchor(continueBtn, 0.0);
        AnchorPane.setRightAnchor(continueBtn, 0.0);
        anchorPane.getChildren().add(continueBtn);

        GridPane.setMargin(anchorPane, new Insets(5));

        return anchorPane;
    }

    private Pane createImagePane() {
        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        imageGroup = new Group();
        imageGroup.getChildren().add(imageView);
        imagePane = new ScrollPane(imageGroup);

        filenameLabel = new Label("Filename:");

        GridPane grid = new GridPane();
        grid.add(imagePane, 0, 0);
        grid.add(filenameLabel, 0, 1);

        // subtract a few pixels so that scroll bars are not displayed
        imageView.fitWidthProperty().bind(grid.widthProperty().subtract(5));
        imageView.fitHeightProperty().bind(grid.heightProperty()
                                           .subtract(filenameLabel.heightProperty()).subtract(5));

        imageView.fitWidthProperty().addListener((observable, oldValue, newValue) -> {
                // the actual dimensions are in imageView.getLayoutBounds().getWidth()
                Image image = imageView.getImage();
                if (image != null) {
                    double newScale = imageView.getLayoutBounds().getWidth() / image.getWidth();
                    wellGrid.setScale(newScale);
                }
            });

        imageView.fitHeightProperty().addListener((observable, oldValue, newValue) -> {
                // the actual dimensions are in imageView.getLayoutBounds().getHeight()
                Image image = imageView.getImage();
                if (image != null) {
                    double newScale = imageView.getLayoutBounds().getHeight() / image.getHeight();
                    wellGrid.setScale(newScale);
                }
            });

        wellGrid = new WellGrid(imageGroup, imageView, plateTypeChooser.getSelection(), 0, 0, 1500, 1300);

        return grid;
    }

    @Override
    protected void onDisplay() {
    }

    public void setImageFileURI(String urlString) {
        Image image = new Image(urlString);
        imageView.setImage(image);
        imageView.setCache(true);

        try {
            imageUrl = new URL(urlString);
            File file = new File(imageUrl.toURI());

            filenameLabel.setText("Filename: " + file.toString());
        } catch (MalformedURLException | URISyntaxException ex) {
            LOG.error(ex.getMessage());
            filenameLabel.setText("");
        } finally {
        }

        imageMaybe = Optional.of(image);
        addWellGrid();
    }

    private void addWellGrid() {
        imageMaybe.ifPresent(image -> {
                imageGroup.getChildren().clear();
                imageGroup.getChildren().add(imageView);
                imageGroup.getChildren().addAll(wellGrid.getWellRectangles());
                imageGroup.getChildren().addAll(wellGrid.getResizeControls());
            });
    }

    private void decodeImage() {
        LOG.debug("image grid dimensions: {}", wellGrid);

        Set<CellRectangle> cells = CellRectangle.getCellsForBoundingBox(
            wellGrid,
            PlateOrientation.LANDSCAPE,
            plateTypeChooser.getSelection(),
            BarcodePosition.TOP);

        // for (CellRectangle cell : cells) {
        //     LOG.debug("cell: {}", cell);
        // }

        try {
            File file = new File(imageUrl.toURI());
            DecodeResult result = ScanLib.getInstance().decodeImage(
                1L,
                file.toString(),
                DecodeOptions.getDefaultDecodeOptions(),
                cells.toArray(new CellRectangle[] {}));
            LOG.debug("decode result: {}", result.getResultCode());

            continueBtn.setDisable(false);
        } catch (URISyntaxException ex) {
            LOG.error(ex.getMessage());
        }
    }

    public void onFlatbedSelectedAction(EventHandler<ActionEvent> flatbedSelectedHandler) {
        continueBtn.setOnAction(flatbedSelectedHandler);
    }
}

