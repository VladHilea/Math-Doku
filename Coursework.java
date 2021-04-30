import javafx.animation.*;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.swing.*;
import java.io.*;
import java.lang.reflect.Array;
import java.time.format.TextStyle;
import java.util.*;
import java.util.concurrent.Flow;


public class Coursework extends Application {

    public ObjectProperty<Square> focusedSquare = new SimpleObjectProperty<>();


    ArrayList<Square> squareList = new ArrayList<Square>();
    ArrayList<GroupOfSquares> groups = new ArrayList<GroupOfSquares>();

    ArrayList<String> errors = new ArrayList<>();
    boolean showMistakes;

    int size = 6;
    int numberOfCages = 15;
    VBox flow = new VBox();
    HBox hbox = new HBox(10);
    HBox generalPane = new HBox();



    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("MATH DOKU by Vlad Hilea");


        Scene scene = new Scene(generalPane , 800 , 700);
        generalPane.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        primaryStage.minWidthProperty().bind(scene.heightProperty().multiply(1));
        primaryStage.minHeightProperty().bind(scene.widthProperty().divide(1));
        scene.getStylesheets().add("cssstylesheet.css");
        primaryStage.setScene(scene);
        primaryStage.show();


        hbox.setPadding(new Insets(10 , 10 , 10 , 10));
        hbox.setAlignment(Pos.CENTER);
        hbox.setSpacing(10);

        VBox allAddFields = new VBox();

        Button undo = new BetterButtons("Undo", "button-toolbar-1");
        Button redo = new BetterButtons("Redo", "button-toolbar-2");
        Button clear = new BetterButtons("Clear Board", "button-toolbar-3");
        Button file = new BetterButtons("Load From Files", "button-toolbar-4");
        Button text = new BetterButtons("Load From Text", "button-toolbar-5");
        Button show = new BetterButtons("Show mistakes", "button-toolbar-6");

        show.setOnAction((actionEvent -> {
            showMistakes = !showMistakes;
            if(showMistakes == true){
                reDrawAllGame();
                errors.clear();
                verifyAllRowsAndColumns();
                verifyCages();
                verifyComplete();

                if (errors.isEmpty()) {

                    Label win = new Label("YOU WON !");
                    win.setFont(Font.font(60));
                    win.setTextFill(Color.GREEN);
                    flow.getChildren().add(win);

                    Timeline timeline = new Timeline(
                            new KeyFrame(Duration.seconds(0), new KeyValue(win.textFillProperty(), Color.RED)),
                            new KeyFrame(Duration.seconds(1), new KeyValue(win.textFillProperty(), Color.GREEN)),
                            new KeyFrame(Duration.seconds(2), new KeyValue(win.textFillProperty(), Color.CORAL)),
                            new KeyFrame(Duration.seconds(3), new KeyValue(win.textFillProperty(), Color.YELLOW)),
                            new KeyFrame(Duration.seconds(4), new KeyValue(win.textFillProperty(), Color.BROWN)),
                            new KeyFrame(Duration.seconds(5), new KeyValue(win.textFillProperty(), Color.DARKORANGE)),
                            new KeyFrame(Duration.seconds(6), new KeyValue(win.textFillProperty(), Color.ORANGE)),
                            new KeyFrame(Duration.seconds(7), new KeyValue(win.textFillProperty(), Color.CYAN)),
                            new KeyFrame(Duration.seconds(8), new KeyValue(win.textFillProperty(), Color.TURQUOISE)),
                            new KeyFrame(Duration.seconds(9), new KeyValue(win.textFillProperty(), Color.BLUE)),
                            new KeyFrame(Duration.seconds(10), new KeyValue(win.textFillProperty(), Color.BLUEVIOLET)),
                            new KeyFrame(Duration.seconds(11), new KeyValue(win.textFillProperty(), Color.VIOLET)),
                            new KeyFrame(Duration.seconds(12), new KeyValue(win.textFillProperty(), Color.PURPLE))
                    );
                    timeline.setAutoReverse(true);
                    timeline.setCycleCount(Animation.INDEFINITE);
                    timeline.play();


                }
                show.setText("Unshow Mistakes");
            } else {
                show.setText("Show Mistakes");
                reDrawAllGame();

            }
        }));


        text.setOnAction((actionEvent -> {
            text.setDisable(true);
            TextArea textArea = new TextArea();
            textArea.setMinHeight(200);
            flow.getChildren().add(textArea);

            Button save = new BetterButtons("Add Game", "button-toolbar-1");
            flow.getChildren().add(save);
            save.setOnAction(actionEvent1 -> {
                flow.getChildren().removeAll(textArea, save);
                String textInArea = textArea.getText();
                File filesave = new File("temporary.txt");
                try {
                    Writer fileWriter = new FileWriter(filesave);
                    System.out.println(textInArea);
                    fileWriter.write(textInArea);
                    fileWriter.close();
                } catch ( IOException e ) {
                    e.printStackTrace();
                }
                try {
                    LoadFromFile fileSaveLoader = new LoadFromFile(filesave);
                    if(fileSaveLoader.isCorrect()) {
                        fileSaveLoader.getNewGame();
                    } else {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Text Input Fail Message");
                        alert.setHeaderText("Input failed");

                        String alerts = "";
                        for(String s : fileSaveLoader.getErrors()){
                           alerts = alerts + s + "\n\r";
                        }
                        alert.setContentText(alerts);
                        alert.showAndWait();
                    }
                } catch ( IOException e ) {
                    e.printStackTrace();
                }
                text.setDisable(false);
            });

        }));

        clear.setOnAction((actionEvent -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION ,
                    "Are you sure you want to delete everything from the board? \n\r This action can't be undone");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                reDrawAllGameFromZero();
            }
        }));


        undo.setMaxWidth(Double.MAX_VALUE);
        redo.setMaxWidth(Double.MAX_VALUE);
        clear.setMaxWidth(Double.MAX_VALUE);
        file.setMaxWidth(Double.MAX_VALUE);
        text.setMaxWidth(Double.MAX_VALUE);
        show.setMaxWidth(Double.MAX_VALUE);


        focusedSquare.addListener((obs , oldSquare , Square) -> {
            if (oldSquare != null) {
                oldSquare.setBackground(Background.EMPTY);
            }
            if (Square != null) {
                Square.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE , CornerRadii.EMPTY , Insets.EMPTY)));
                Square.toFront();
                if (Square.isUndo()) {
                    undo.setDisable(false);
                    Square.setUndo(true);
                } else {
                    undo.setDisable(true);
                    Square.setUndo(false);
                }
                if (Square.isRedo()) {
                    redo.setDisable(false);
                    Square.setRedo(true);
                } else {
                    redo.setDisable(true);
                    Square.setRedo(false);
                }
            }


        });

        scene.setOnKeyTyped(e -> {
            if (focusedSquare.get() != null) {
                focusedSquare.get().setValueUndo(Integer.toString(focusedSquare.get().getTextNumber()));
                if (Character.isDigit(e.getCharacter().charAt(0))) {
                    focusedSquare.get().getText().setText(e.getCharacter());
                    focusedSquare.get().setValueRedo(Integer.toString(focusedSquare.get().getTextNumber()));
                    focusedSquare.get().getText().setFill(Color.BLUE);
                }
                if (showMistakes) {
                    reDrawAllGame();
                    errors.clear();
                    verifyAllRowsAndColumns();
                    verifyCages();
                    verifyComplete();

                    if (errors.isEmpty()) {

                        Label win = new Label("YOU WON !");
                        win.setFont(Font.font(60));
                        win.setTextFill(Color.GREEN);
                        flow.getChildren().add(win);

                        Timeline timeline = new Timeline(
                                new KeyFrame(Duration.seconds(0), new KeyValue(win.textFillProperty(), Color.RED)),
                                new KeyFrame(Duration.seconds(1), new KeyValue(win.textFillProperty(), Color.GREEN)),
                                new KeyFrame(Duration.seconds(2), new KeyValue(win.textFillProperty(), Color.CORAL)),
                                new KeyFrame(Duration.seconds(3), new KeyValue(win.textFillProperty(), Color.YELLOW)),
                                new KeyFrame(Duration.seconds(4), new KeyValue(win.textFillProperty(), Color.BROWN)),
                                new KeyFrame(Duration.seconds(5), new KeyValue(win.textFillProperty(), Color.DARKORANGE)),
                                new KeyFrame(Duration.seconds(6), new KeyValue(win.textFillProperty(), Color.ORANGE)),
                                new KeyFrame(Duration.seconds(7), new KeyValue(win.textFillProperty(), Color.CYAN)),
                                new KeyFrame(Duration.seconds(8), new KeyValue(win.textFillProperty(), Color.TURQUOISE)),
                                new KeyFrame(Duration.seconds(9), new KeyValue(win.textFillProperty(), Color.BLUE)),
                                new KeyFrame(Duration.seconds(10), new KeyValue(win.textFillProperty(), Color.BLUEVIOLET)),
                                new KeyFrame(Duration.seconds(11), new KeyValue(win.textFillProperty(), Color.VIOLET)),
                                new KeyFrame(Duration.seconds(12), new KeyValue(win.textFillProperty(), Color.PURPLE))
                        );
                        timeline.setAutoReverse(true);
                        timeline.setCycleCount(Animation.INDEFINITE);
                        timeline.play();
                    }
                } else {
                    errors.clear();
                    verifyComplete();
                    if(!errors.isEmpty()){
                        Label win = new Label("YOU WON !");
                        win.setFont(Font.font(60));
                        win.setTextFill(Color.GREEN);
                        flow.getChildren().add(win);

                        Timeline timeline = new Timeline(
                                new KeyFrame(Duration.seconds(0), new KeyValue(win.textFillProperty(), Color.RED)),
                                new KeyFrame(Duration.seconds(1), new KeyValue(win.textFillProperty(), Color.GREEN)),
                                new KeyFrame(Duration.seconds(2), new KeyValue(win.textFillProperty(), Color.CORAL)),
                                new KeyFrame(Duration.seconds(3), new KeyValue(win.textFillProperty(), Color.YELLOW)),
                                new KeyFrame(Duration.seconds(4), new KeyValue(win.textFillProperty(), Color.BROWN)),
                                new KeyFrame(Duration.seconds(5), new KeyValue(win.textFillProperty(), Color.DARKORANGE)),
                                new KeyFrame(Duration.seconds(6), new KeyValue(win.textFillProperty(), Color.ORANGE)),
                                new KeyFrame(Duration.seconds(7), new KeyValue(win.textFillProperty(), Color.CYAN)),
                                new KeyFrame(Duration.seconds(8), new KeyValue(win.textFillProperty(), Color.TURQUOISE)),
                                new KeyFrame(Duration.seconds(9), new KeyValue(win.textFillProperty(), Color.BLUE)),
                                new KeyFrame(Duration.seconds(10), new KeyValue(win.textFillProperty(), Color.BLUEVIOLET)),
                                new KeyFrame(Duration.seconds(11), new KeyValue(win.textFillProperty(), Color.VIOLET)),
                                new KeyFrame(Duration.seconds(12), new KeyValue(win.textFillProperty(), Color.PURPLE))
                        );
                        timeline.setAutoReverse(true);
                        timeline.setCycleCount(Animation.INDEFINITE);
                        timeline.play();
                    }
                }
            }
        });

        undo.setOnAction(actionEvent -> {
            if (focusedSquare.get() != null) {
                if (Integer.parseInt(focusedSquare.get().getValueUndo()) == -1) {
                    focusedSquare.get().getText().setText("");
                } else {
                    focusedSquare.get().getText().setText(focusedSquare.get().getValueUndo());
                }
                undo.setDisable(true);
                redo.setDisable(false);

                if (showMistakes) {
                    reDrawAllGame();
                    errors.clear();
                    verifyAllRowsAndColumns();
                    verifyCages();
                    verifyComplete();

                    if (errors.isEmpty()) {

                        Label win = new Label("YOU WON !");
                        win.setFont(Font.font(60));
                        win.setTextFill(Color.GREEN);
                        flow.getChildren().add(win);

                        Timeline timeline = new Timeline(
                                new KeyFrame(Duration.seconds(0), new KeyValue(win.textFillProperty(), Color.RED)),
                                new KeyFrame(Duration.seconds(1), new KeyValue(win.textFillProperty(), Color.GREEN)),
                                new KeyFrame(Duration.seconds(2), new KeyValue(win.textFillProperty(), Color.CORAL)),
                                new KeyFrame(Duration.seconds(3), new KeyValue(win.textFillProperty(), Color.YELLOW)),
                                new KeyFrame(Duration.seconds(4), new KeyValue(win.textFillProperty(), Color.BROWN)),
                                new KeyFrame(Duration.seconds(5), new KeyValue(win.textFillProperty(), Color.DARKORANGE)),
                                new KeyFrame(Duration.seconds(6), new KeyValue(win.textFillProperty(), Color.ORANGE)),
                                new KeyFrame(Duration.seconds(7), new KeyValue(win.textFillProperty(), Color.CYAN)),
                                new KeyFrame(Duration.seconds(8), new KeyValue(win.textFillProperty(), Color.TURQUOISE)),
                                new KeyFrame(Duration.seconds(9), new KeyValue(win.textFillProperty(), Color.BLUE)),
                                new KeyFrame(Duration.seconds(10), new KeyValue(win.textFillProperty(), Color.BLUEVIOLET)),
                                new KeyFrame(Duration.seconds(11), new KeyValue(win.textFillProperty(), Color.VIOLET)),
                                new KeyFrame(Duration.seconds(12), new KeyValue(win.textFillProperty(), Color.PURPLE))
                        );
                        timeline.setAutoReverse(true);
                        timeline.setCycleCount(Animation.INDEFINITE);
                        timeline.play();
                    }
                } else {
                    errors.clear();
                    verifyComplete();
                    if(errors.isEmpty()){
                        Label win = new Label("YOU WON !");
                        win.setFont(Font.font(60));
                        win.setTextFill(Color.GREEN);
                        flow.getChildren().add(win);

                        Timeline timeline = new Timeline(
                                new KeyFrame(Duration.seconds(0), new KeyValue(win.textFillProperty(), Color.RED)),
                                new KeyFrame(Duration.seconds(1), new KeyValue(win.textFillProperty(), Color.GREEN)),
                                new KeyFrame(Duration.seconds(2), new KeyValue(win.textFillProperty(), Color.CORAL)),
                                new KeyFrame(Duration.seconds(3), new KeyValue(win.textFillProperty(), Color.YELLOW)),
                                new KeyFrame(Duration.seconds(4), new KeyValue(win.textFillProperty(), Color.BROWN)),
                                new KeyFrame(Duration.seconds(5), new KeyValue(win.textFillProperty(), Color.DARKORANGE)),
                                new KeyFrame(Duration.seconds(6), new KeyValue(win.textFillProperty(), Color.ORANGE)),
                                new KeyFrame(Duration.seconds(7), new KeyValue(win.textFillProperty(), Color.CYAN)),
                                new KeyFrame(Duration.seconds(8), new KeyValue(win.textFillProperty(), Color.TURQUOISE)),
                                new KeyFrame(Duration.seconds(9), new KeyValue(win.textFillProperty(), Color.BLUE)),
                                new KeyFrame(Duration.seconds(10), new KeyValue(win.textFillProperty(), Color.BLUEVIOLET)),
                                new KeyFrame(Duration.seconds(11), new KeyValue(win.textFillProperty(), Color.VIOLET)),
                                new KeyFrame(Duration.seconds(12), new KeyValue(win.textFillProperty(), Color.PURPLE))
                        );
                        timeline.setAutoReverse(true);
                        timeline.setCycleCount(Animation.INDEFINITE);
                        timeline.play();
                    }
                }
            }
        });

        redo.setOnAction((actionEvent -> {
            if (focusedSquare.get() != null) {
                focusedSquare.get().getText().setText(focusedSquare.get().getValueRedo());
                undo.setDisable(false);
                redo.setDisable(true);

                if (showMistakes) {
                    reDrawAllGame();
                    errors.clear();
                    verifyAllRowsAndColumns();
                    verifyCages();
                    verifyComplete();

                    if (errors.isEmpty()) {

                        Label win = new Label("YOU WON !");
                        win.setFont(Font.font(60));
                        win.setTextFill(Color.GREEN);
                        flow.getChildren().add(win);

                        Timeline timeline = new Timeline(
                                new KeyFrame(Duration.seconds(0), new KeyValue(win.textFillProperty(), Color.RED)),
                                new KeyFrame(Duration.seconds(1), new KeyValue(win.textFillProperty(), Color.GREEN)),
                                new KeyFrame(Duration.seconds(2), new KeyValue(win.textFillProperty(), Color.CORAL)),
                                new KeyFrame(Duration.seconds(3), new KeyValue(win.textFillProperty(), Color.YELLOW)),
                                new KeyFrame(Duration.seconds(4), new KeyValue(win.textFillProperty(), Color.BROWN)),
                                new KeyFrame(Duration.seconds(5), new KeyValue(win.textFillProperty(), Color.DARKORANGE)),
                                new KeyFrame(Duration.seconds(6), new KeyValue(win.textFillProperty(), Color.ORANGE)),
                                new KeyFrame(Duration.seconds(7), new KeyValue(win.textFillProperty(), Color.CYAN)),
                                new KeyFrame(Duration.seconds(8), new KeyValue(win.textFillProperty(), Color.TURQUOISE)),
                                new KeyFrame(Duration.seconds(9), new KeyValue(win.textFillProperty(), Color.BLUE)),
                                new KeyFrame(Duration.seconds(10), new KeyValue(win.textFillProperty(), Color.BLUEVIOLET)),
                                new KeyFrame(Duration.seconds(11), new KeyValue(win.textFillProperty(), Color.VIOLET)),
                                new KeyFrame(Duration.seconds(12), new KeyValue(win.textFillProperty(), Color.PURPLE))
                        );
                        timeline.setAutoReverse(true);
                        timeline.setCycleCount(Animation.INDEFINITE);
                        timeline.play();
                    }
                } else {
                    errors.clear();
                    verifyComplete();
                    if(errors.isEmpty()){
                        Label win = new Label("YOU WON !");
                        win.setFont(Font.font(60));
                        win.setTextFill(Color.GREEN);
                        flow.getChildren().add(win);

                        Timeline timeline = new Timeline(
                                new KeyFrame(Duration.seconds(0), new KeyValue(win.textFillProperty(), Color.RED)),
                                new KeyFrame(Duration.seconds(1), new KeyValue(win.textFillProperty(), Color.GREEN)),
                                new KeyFrame(Duration.seconds(2), new KeyValue(win.textFillProperty(), Color.CORAL)),
                                new KeyFrame(Duration.seconds(3), new KeyValue(win.textFillProperty(), Color.YELLOW)),
                                new KeyFrame(Duration.seconds(4), new KeyValue(win.textFillProperty(), Color.BROWN)),
                                new KeyFrame(Duration.seconds(5), new KeyValue(win.textFillProperty(), Color.DARKORANGE)),
                                new KeyFrame(Duration.seconds(6), new KeyValue(win.textFillProperty(), Color.ORANGE)),
                                new KeyFrame(Duration.seconds(7), new KeyValue(win.textFillProperty(), Color.CYAN)),
                                new KeyFrame(Duration.seconds(8), new KeyValue(win.textFillProperty(), Color.TURQUOISE)),
                                new KeyFrame(Duration.seconds(9), new KeyValue(win.textFillProperty(), Color.BLUE)),
                                new KeyFrame(Duration.seconds(10), new KeyValue(win.textFillProperty(), Color.BLUEVIOLET)),
                                new KeyFrame(Duration.seconds(11), new KeyValue(win.textFillProperty(), Color.VIOLET)),
                                new KeyFrame(Duration.seconds(12), new KeyValue(win.textFillProperty(), Color.PURPLE))
                        );
                        timeline.setAutoReverse(true);
                        timeline.setCycleCount(Animation.INDEFINITE);
                        timeline.play();
                    }
                }
            }
        }));

        scene.setOnKeyPressed(e -> {
            if (focusedSquare.get() != null) {
                if (e.getCode().equals(KeyCode.BACK_SPACE)) {
                    focusedSquare.get().getText().setText("");
                }
            }
        });

        file.setOnAction(actionEvent -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Game File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files" , "*.txt"));

            File fileRead;
            fileRead = fileChooser.showOpenDialog(primaryStage);

            try {
                LoadFromFile fileLoader = new LoadFromFile(fileRead);
                if(fileLoader.isCorrect()) {
                    fileLoader.getNewGame();
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Text Input Fail Message");
                    alert.setHeaderText("Input failed");

                    for(String s : fileLoader.getErrors()){
                        alert.setContentText(s);
                    }
                    alert.showAndWait();
                }


            } catch ( FileNotFoundException e ) {
                e.printStackTrace();
            } catch ( IOException e ) {
                e.printStackTrace();
            }


        });


        hbox.getChildren().addAll(undo , redo , clear , file , text , show);
        flow.getChildren().addAll(hbox);
        createGame();


        ArrayList<Square> square1 = new ArrayList<>();
        square1.add(getSquare(1));
        square1.add(getSquare(7));
        Square[] sq2 = new Square[]{getSquare(2) , getSquare(3)};
        Square[] sq3 = new Square[]{getSquare(4) , getSquare(10)};
        Square[] sq4 = new Square[]{getSquare(5) , getSquare(6) , getSquare(12) , getSquare(18)};
        Square[] sq5 = new Square[]{getSquare(8) , getSquare(9)};
        Square[] sq6 = new Square[]{getSquare(11) , getSquare(17)};
        Square[] sq7 = new Square[]{getSquare(13) , getSquare(14) , getSquare(19) , getSquare(20)};
        Square[] sq8 = new Square[]{getSquare(15) , getSquare(16)};
        Square[] sq9 = new Square[]{getSquare(21) , getSquare(27)};
        Square[] sq10 = new Square[]{getSquare(22) , getSquare(28) , getSquare(29)};
        Square[] sq11 = new Square[]{getSquare(23) , getSquare(24)};
        Square[] sq12 = new Square[]{getSquare(25) , getSquare(26)};
        Square[] sq13 = new Square[]{getSquare(30) , getSquare(36)};
        Square[] sq14 = new Square[]{getSquare(31) , getSquare(32) , getSquare(33)};
        Square[] sq15 = new Square[]{getSquare(34) , getSquare(35)};

        ArrayList<Square> square2 = new ArrayList<>(Arrays.asList(sq2));
        ArrayList<Square> square3 = new ArrayList<>(Arrays.asList(sq3));
        ArrayList<Square> square4 = new ArrayList<>(Arrays.asList(sq4));
        ArrayList<Square> square5 = new ArrayList<>(Arrays.asList(sq5));
        ArrayList<Square> square6 = new ArrayList<>(Arrays.asList(sq6));
        ArrayList<Square> square7 = new ArrayList<>(Arrays.asList(sq7));
        ArrayList<Square> square8 = new ArrayList<>(Arrays.asList(sq8));
        ArrayList<Square> square9 = new ArrayList<>(Arrays.asList(sq9));
        ArrayList<Square> square10 = new ArrayList<>(Arrays.asList(sq10));
        ArrayList<Square> square11 = new ArrayList<>(Arrays.asList(sq11));
        ArrayList<Square> square12 = new ArrayList<>(Arrays.asList(sq12));
        ArrayList<Square> square13 = new ArrayList<>(Arrays.asList(sq13));
        ArrayList<Square> square14 = new ArrayList<>(Arrays.asList(sq14));
        ArrayList<Square> square15 = new ArrayList<>(Arrays.asList(sq15));

        GroupOfSquares group1 = new GroupOfSquares(square1 , "11+");
        GroupOfSquares group2 = new GroupOfSquares(square2 , "2÷");
        GroupOfSquares group3 = new GroupOfSquares(square3 , "20x");
        GroupOfSquares group4 = new GroupOfSquares(square4 , "6x");
        GroupOfSquares group5 = new GroupOfSquares(square5 , "3-");
        GroupOfSquares group6 = new GroupOfSquares(square6 , "3÷");
        GroupOfSquares group7 = new GroupOfSquares(square7 , "240x");
        GroupOfSquares group8 = new GroupOfSquares(square8 , "6x");
        GroupOfSquares group9 = new GroupOfSquares(square9 , "6x");
        GroupOfSquares group10 = new GroupOfSquares(square10 , "7+");
        GroupOfSquares group11 = new GroupOfSquares(square11 , "30x");
        GroupOfSquares group12 = new GroupOfSquares(square12 , "6x");
        GroupOfSquares group13 = new GroupOfSquares(square13 , "9+");
        GroupOfSquares group14 = new GroupOfSquares(square14 , "8+");
        GroupOfSquares group15 = new GroupOfSquares(square15 , "2÷");

        groups.add(group1);
        groups.add(group2);
        groups.add(group3);
        groups.add(group4);
        groups.add(group5);
        groups.add(group6);
        groups.add(group7);
        groups.add(group8);
        groups.add(group9);
        groups.add(group10);
        groups.add(group11);
        groups.add(group12);
        groups.add(group13);
        groups.add(group14);
        groups.add(group15);


        flow.setPadding(new Insets(10 , 10 , 10 , 10));
        flow.setAlignment(Pos.CENTER);
        flow.prefWidthProperty().bind(primaryStage.widthProperty());
        flow.prefHeightProperty().bind(primaryStage.widthProperty());

        drawGraph(group1);
        drawGraph(group2);
        drawGraph(group3);
        drawGraph(group4);
        drawGraph(group5);
        drawGraph(group6);
        drawGraph(group7);
        drawGraph(group8);
        drawGraph(group9);
        drawGraph(group10);
        drawGraph(group11);
        drawGraph(group12);
        drawGraph(group13);
        drawGraph(group14);
        drawGraph(group15);


        GridPane numberPad = new GridPane();
        Button one = new NumberPadsButtons("1");
        Button two = new NumberPadsButtons("2");
        Button three = new NumberPadsButtons("3");
        Button four = new NumberPadsButtons("4");
        Button five = new NumberPadsButtons("5");
        Button six = new NumberPadsButtons("6");
        Button seven = new NumberPadsButtons("7");
        Button eight = new NumberPadsButtons("8");
        Button nine = new NumberPadsButtons("9");
        Button delete = new DeleteButton("<-");

        numberPad.add(one, 1, 1);
        numberPad.add(two, 2, 1);
        numberPad.add(three, 3, 1);
        numberPad.add(four, 1, 2);
        numberPad.add(five, 2, 2);
        numberPad.add(six, 3, 2);
        numberPad.add(seven, 1, 3);
        numberPad.add(eight, 2, 3);
        numberPad.add(nine,3, 3);
        numberPad.add(delete, 1, 4);

        numberPad.setAlignment(Pos.CENTER);
        generalPane.setPadding(new Insets(10,10,10,10));
        generalPane.setAlignment(Pos.CENTER);
        generalPane.getChildren().add(flow);
        generalPane.getChildren().add(numberPad);


    }

    public void createGame() {
        flow.getChildren().clear();
        flow.getChildren().add(hbox);
        GridPane grid = new GridPane();
        VBox.setVgrow(grid , Priority.ALWAYS);
        int t = 1;
        for (int i = 1; i <= size; i++) {
            for (int j = 1; j <= size; j++) {
                Square square = new Square(t , i , j);
                grid.add(square , j , i);
                GridPane.setHgrow(square , Priority.ALWAYS);
                GridPane.setVgrow(square, Priority.ALWAYS);
                squareList.add(square);
                t++;
            }
        }
        flow.getChildren().add(grid);
        HBox fontMenu = new HBox();

        Label fontLabel = new Label("Choose the font size you want from the list ! ");
        fontLabel.setPadding(new Insets(5, 0, 0, 0));
        Button fontSmall = new Button("Small");
        fontSmall.getStyleClass().add("button-font");
        Button fontMedium = new Button("Medium");
        fontMedium.getStyleClass().add("button-font");
        Button fontLarge = new Button("Large");
        fontLarge.getStyleClass().add("button-font");
        fontMenu.setPadding(new Insets(10,10,10,10));
        fontMenu.getChildren().addAll(fontLabel, fontSmall, fontMedium, fontLarge);
        flow.getChildren().add(fontMenu);

        fontSmall.setOnAction(actionEvent -> {

            for(Square s: getSquareList()){
                s.getText().setFont(Font.font(15));
                if(s.isLabeled()){
                    s.getLabel().setFont(Font.font(10));
                }
            }
        });

        fontMedium.setOnAction(actionEvent -> {

            for(Square s: getSquareList()){
                s.getText().setFont(Font.font(20));
                if(s.isLabeled()){
                    s.getLabel().setFont(Font.font(15));
                }
            }
        });

        fontLarge.setOnAction(actionEvent -> {

            for(Square s: getSquareList()){
                s.getText().setFont(Font.font(40));
                if(s.isLabeled()){
                    s.getLabel().setFont(Font.font(20));
                }
            }
        });
    }

    public void drawGraph(GroupOfSquares group) {

        for (Square squareOfGroup : group.getGroupSquare()) {
            int right = 0;
            int top = 0;
            int bottom = 0;
            int left = 0;
            if (group.hasSquare(group.getSquareRightNeighbour(squareOfGroup))) {
                right = 1;
            }
            if (group.hasSquare(group.getSquareLeftNeighbour(squareOfGroup))) {
                left = 1;
            }
            if (group.hasSquare(group.getSquareBottomNeighbout(squareOfGroup))) {
                bottom = 1;
            }
            if (group.hasSquare(group.getSquareTopNeighbour(squareOfGroup))) {
                top = 1;
            }


            String border = "-fx-border-width: ";
            if (top == 1) {
                border = border + "0.1 ";
            } else if (top == 2) {
                border = border + "8 ";
            } else border = border + "4 ";

            if (right == 1) {
                border = border + "0.1 ";
            } else if (right == 2) {
                border = border + "8 ";
            } else border = border + "4 ";

            if (bottom == 1) {
                border = border + "0.1 ";
            } else if (bottom == 2) {
                border = border + "8 ";
            } else border = border + "4 ";

            if (left == 1) {
                border = border + "0.1 ";
            } else if (left == 2) {
                border = border + "8 ";
            } else border = border + "4 ";

            border = border + " !important; -fx-border-color: blue !important;";
            squareOfGroup.setStyle(border);
        }
    }

    public Square getSquare(int index) {
        for (Square sqr : squareList) {
            if (sqr.getIndex() == index) {
                return sqr;
            }
        }
        return null;
    }


    public ArrayList<Square> getSquareList() {
        return squareList;
    }


    public ArrayList<Square> getRow(int x) {
        ArrayList<Square> row = new ArrayList<>();
        for (Square square : getSquareList()) {
            if (square.getX() == x) {
                row.add(square);
            }

        }
        return row;
    }

    public void verifyRow(int x) {
        ArrayList<Square> row = getRow(x);
        boolean problem = false;
        for (int i = 0; i < row.size(); i++) {
            if (row.get(i).getTextNumber() != -1) {
                if (row.get(i).getTextNumber() > size) {
                    errors.add("Row " + x + " is incorrect. Values are higher than the highest number");
                    row.get(i).getText().setFill(Color.RED);
                }
            }
        }
        for (int i = 0; i < row.size() - 1; i++) {
            boolean found = false;
            for (int j = i + 1; j < row.size(); j++) {
                if (row.get(i).getTextNumber() != -1 && row.get(j).getTextNumber() != -1) {
                    if (row.get(i).getTextNumber() == row.get(j).getTextNumber()) {
                        errors.add("Row " + x + " is incorrect. Values are the same");
                        found = true;
                        problem = true;
                    }
                }
            }
            if (found) {
                break;
            }
        }

        if (problem) {
            for (Square square : row) {
                square.setStyle("-fx-border-color:red; -fx-border-width: 1");
            }
        }
    }

    public ArrayList<Square> getColumn(int x) {
        ArrayList<Square> column = new ArrayList<>();
        for (Square square : getSquareList()) {

            if (square.getY() == x) {
                column.add(square);
            }
        }
        return column;
    }

    public void verifyColumn(int x) {
        ArrayList<Square> column = getColumn(x);
        boolean problem = false;
        boolean problem1 = false;
        for (int i = 0; i < column.size(); i++) {
            if (column.get(i).getTextNumber() != -1) {
                if (column.get(i).getTextNumber() > size) {
                    errors.add("Column " + x + " is not correct. Values are over highest number");
                    column.get(i).getText().setFill(Color.RED);
                }
            }

        }
        for (int i = 0; i < column.size() - 1; i++) {
            boolean found = false;
            for (int j = i + 1; j < column.size(); j++) {
                if (column.get(i).getTextNumber() != -1 && column.get(i).getTextNumber() != -1) {
                    if (column.get(i).getTextNumber() == column.get(j).getTextNumber()) {
                        errors.add("Column " + x + " is incorrect. Values are the same");
                        problem = true;
                        found = true;
                    }
                }
            }
            if (found) {
                break;
            }
        }

        if (problem) {
            for (Square square : column) {
                square.setStyle("-fx-border-color: red; -fx-border:red");
            }
        }
    }


    public ArrayList<GroupOfSquares> getGroups() {
        return groups;
    }

    public void reDrawAllGame() {
        for (GroupOfSquares groupOfSquares : getGroups()) {
            drawGraph(groupOfSquares);
            groupOfSquares.getPrimary().getLabel().setFill(Color.BLUE);
            for(Square s : groupOfSquares.getGroupSquare()){
                s.getText().setFill(Color.BLUE);
            }
        }
    }

    public void reDrawAllGameFromZero() {
        for (GroupOfSquares groupOfSquares : getGroups()) {
            drawGraph(groupOfSquares);
            for (Square square : groupOfSquares.getGroupSquare()) {
                square.getText().setText("");
            }
        }
    }

    public void verifyAllRowsAndColumns() {
        for (int i = 1; i <= size; i++) {
            verifyRow(i);
        }
        for (int i = 1; i <= size; i++) {
            verifyColumn(i);
        }
    }

    public void verifyComplete() {
        for (Square square : getSquareList()) {
            if (square.getTextNumber() == -1) {
                errors.add("The game is not complete. You still ned to complete some squares");
                break;
            }
        }
    }

    public void verifyCages() {
        boolean problem = false;
        for (GroupOfSquares groupOfSquares : getGroups()) {
            if(groupOfSquares.isComplete()) {
                if (!groupOfSquares.verifyInput()) {
                    groupOfSquares.getPrimary().getLabel().setFill(Color.RED);
                    for (Square s : groupOfSquares.getGroupSquare()) {
                        s.getText().setFill(Color.RED);
                    }
                    problem = true;
                }
            }
        }
        if (problem) {
            errors.add("The groups where the result is wrong the label will be colored red");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }


    public class BetterButtons extends Button {

        public BetterButtons(String s, String cssclass) {
            super(s);
            getStyleClass().add(cssclass);
            setFont(Font.font(10));
        }

    }

    public class NumberPadsButtons extends Button {
        public NumberPadsButtons(String s){
            super(s);
            setFont(Font.font(30));
            getStyleClass().add("button-numpad");
            setOnAction(actionEvent -> {
                if (focusedSquare.get() != null) {
                    focusedSquare.get().setValueUndo(Integer.toString(focusedSquare.get().getTextNumber()));
                    focusedSquare.get().getText().setText(this.getText());
                    focusedSquare.get().setValueRedo(Integer.toString(focusedSquare.get().getTextNumber()));
                    focusedSquare.get().getText().setFill(Color.BLUE);

                }
                if (showMistakes) {
                    reDrawAllGame();
                    errors.clear();
                    verifyAllRowsAndColumns();
                    verifyCages();
                    verifyComplete();

                    if (errors.isEmpty()) {

                        Label win = new Label("YOU WON !");
                        win.setFont(Font.font(60));
                        win.setTextFill(Color.GREEN);
                        flow.getChildren().add(win);

                        Timeline timeline = new Timeline(
                                new KeyFrame(Duration.seconds(0), new KeyValue(win.textFillProperty(), Color.RED)),
                                new KeyFrame(Duration.seconds(1), new KeyValue(win.textFillProperty(), Color.GREEN)),
                                new KeyFrame(Duration.seconds(2), new KeyValue(win.textFillProperty(), Color.CORAL)),
                                new KeyFrame(Duration.seconds(3), new KeyValue(win.textFillProperty(), Color.YELLOW)),
                                new KeyFrame(Duration.seconds(4), new KeyValue(win.textFillProperty(), Color.BROWN)),
                                new KeyFrame(Duration.seconds(5), new KeyValue(win.textFillProperty(), Color.DARKORANGE)),
                                new KeyFrame(Duration.seconds(6), new KeyValue(win.textFillProperty(), Color.ORANGE)),
                                new KeyFrame(Duration.seconds(7), new KeyValue(win.textFillProperty(), Color.CYAN)),
                                new KeyFrame(Duration.seconds(8), new KeyValue(win.textFillProperty(), Color.TURQUOISE)),
                                new KeyFrame(Duration.seconds(9), new KeyValue(win.textFillProperty(), Color.BLUE)),
                                new KeyFrame(Duration.seconds(10), new KeyValue(win.textFillProperty(), Color.BLUEVIOLET)),
                                new KeyFrame(Duration.seconds(11), new KeyValue(win.textFillProperty(), Color.VIOLET)),
                                new KeyFrame(Duration.seconds(12), new KeyValue(win.textFillProperty(), Color.PURPLE))
                        );
                        timeline.setAutoReverse(true);
                        timeline.setCycleCount(Animation.INDEFINITE);
                        timeline.play();
                    }
                } else {
                    errors.clear();
                    verifyComplete();
                    if(errors.isEmpty()){
                        Label win = new Label("YOU WON !");
                        win.setFont(Font.font(60));
                        win.setTextFill(Color.GREEN);
                        flow.getChildren().add(win);

                        Timeline timeline = new Timeline(
                                new KeyFrame(Duration.seconds(0), new KeyValue(win.textFillProperty(), Color.RED)),
                                new KeyFrame(Duration.seconds(1), new KeyValue(win.textFillProperty(), Color.GREEN)),
                                new KeyFrame(Duration.seconds(2), new KeyValue(win.textFillProperty(), Color.CORAL)),
                                new KeyFrame(Duration.seconds(3), new KeyValue(win.textFillProperty(), Color.YELLOW)),
                                new KeyFrame(Duration.seconds(4), new KeyValue(win.textFillProperty(), Color.BROWN)),
                                new KeyFrame(Duration.seconds(5), new KeyValue(win.textFillProperty(), Color.DARKORANGE)),
                                new KeyFrame(Duration.seconds(6), new KeyValue(win.textFillProperty(), Color.ORANGE)),
                                new KeyFrame(Duration.seconds(7), new KeyValue(win.textFillProperty(), Color.CYAN)),
                                new KeyFrame(Duration.seconds(8), new KeyValue(win.textFillProperty(), Color.TURQUOISE)),
                                new KeyFrame(Duration.seconds(9), new KeyValue(win.textFillProperty(), Color.BLUE)),
                                new KeyFrame(Duration.seconds(10), new KeyValue(win.textFillProperty(), Color.BLUEVIOLET)),
                                new KeyFrame(Duration.seconds(11), new KeyValue(win.textFillProperty(), Color.VIOLET)),
                                new KeyFrame(Duration.seconds(12), new KeyValue(win.textFillProperty(), Color.PURPLE))
                        );
                        timeline.setAutoReverse(true);
                        timeline.setCycleCount(Animation.INDEFINITE);
                        timeline.play();
                    }
                }
            });
        }
    }

    public class DeleteButton extends Button {
        public DeleteButton(String s){
            super(s);
            setFont(Font.font(20));

            getStyleClass().add("button-numpad");
            setOnAction(actionEvent -> {
                if (focusedSquare.get() != null) {
                    if (focusedSquare.get() != null) {
                        focusedSquare.get().getText().setText("");
                    }

                }
                if (showMistakes) {
                    reDrawAllGame();
                    errors.clear();
                    verifyAllRowsAndColumns();
                    verifyCages();
                    verifyComplete();

                    if (errors.isEmpty()) {

                        Label win = new Label("YOU WON !");
                        win.setFont(Font.font(60));
                        win.setTextFill(Color.GREEN);
                        flow.getChildren().add(win);

                        Timeline timeline = new Timeline(
                                new KeyFrame(Duration.seconds(0), new KeyValue(win.textFillProperty(), Color.RED)),
                                new KeyFrame(Duration.seconds(1), new KeyValue(win.textFillProperty(), Color.GREEN)),
                                new KeyFrame(Duration.seconds(2), new KeyValue(win.textFillProperty(), Color.CORAL)),
                                new KeyFrame(Duration.seconds(3), new KeyValue(win.textFillProperty(), Color.YELLOW)),
                                new KeyFrame(Duration.seconds(4), new KeyValue(win.textFillProperty(), Color.BROWN)),
                                new KeyFrame(Duration.seconds(5), new KeyValue(win.textFillProperty(), Color.DARKORANGE)),
                                new KeyFrame(Duration.seconds(6), new KeyValue(win.textFillProperty(), Color.ORANGE)),
                                new KeyFrame(Duration.seconds(7), new KeyValue(win.textFillProperty(), Color.CYAN)),
                                new KeyFrame(Duration.seconds(8), new KeyValue(win.textFillProperty(), Color.TURQUOISE)),
                                new KeyFrame(Duration.seconds(9), new KeyValue(win.textFillProperty(), Color.BLUE)),
                                new KeyFrame(Duration.seconds(10), new KeyValue(win.textFillProperty(), Color.BLUEVIOLET)),
                                new KeyFrame(Duration.seconds(11), new KeyValue(win.textFillProperty(), Color.VIOLET)),
                                new KeyFrame(Duration.seconds(12), new KeyValue(win.textFillProperty(), Color.PURPLE))
                        );
                        timeline.setAutoReverse(true);
                        timeline.setCycleCount(Animation.INDEFINITE);
                        timeline.play();
                    }
                } else {
                    errors.clear();
                    verifyComplete();
                    if(errors.isEmpty()){
                        Label win = new Label("YOU WON !");
                        win.setFont(Font.font(60));
                        win.setTextFill(Color.GREEN);
                        flow.getChildren().add(win);

                        Timeline timeline = new Timeline(
                                new KeyFrame(Duration.seconds(0), new KeyValue(win.textFillProperty(), Color.RED)),
                                new KeyFrame(Duration.seconds(1), new KeyValue(win.textFillProperty(), Color.GREEN)),
                                new KeyFrame(Duration.seconds(2), new KeyValue(win.textFillProperty(), Color.CORAL)),
                                new KeyFrame(Duration.seconds(3), new KeyValue(win.textFillProperty(), Color.YELLOW)),
                                new KeyFrame(Duration.seconds(4), new KeyValue(win.textFillProperty(), Color.BROWN)),
                                new KeyFrame(Duration.seconds(5), new KeyValue(win.textFillProperty(), Color.DARKORANGE)),
                                new KeyFrame(Duration.seconds(6), new KeyValue(win.textFillProperty(), Color.ORANGE)),
                                new KeyFrame(Duration.seconds(7), new KeyValue(win.textFillProperty(), Color.CYAN)),
                                new KeyFrame(Duration.seconds(8), new KeyValue(win.textFillProperty(), Color.TURQUOISE)),
                                new KeyFrame(Duration.seconds(9), new KeyValue(win.textFillProperty(), Color.BLUE)),
                                new KeyFrame(Duration.seconds(10), new KeyValue(win.textFillProperty(), Color.BLUEVIOLET)),
                                new KeyFrame(Duration.seconds(11), new KeyValue(win.textFillProperty(), Color.VIOLET)),
                                new KeyFrame(Duration.seconds(12), new KeyValue(win.textFillProperty(), Color.PURPLE))
                        );
                        timeline.setAutoReverse(true);
                        timeline.setCycleCount(Animation.INDEFINITE);
                        timeline.play();
                    }
                }
            });
        }
    }


    public class Square extends StackPane {

        int index;
        GroupOfSquares group;
        boolean constructionVisited = false;
        String lastValue;
        String valueUndo;
        String valueRedo;
        int x;
        int y;
        boolean labeled;
        Text text = new Text("");
        Text label = new Text();
        boolean undo = true;
        boolean redo = true;

        public Square(int index , int x , int y) {
            this.index = index;
            this.x = x;
            this.y = y;
            setPrefHeight(70);
            setPrefWidth(70);

            text.setFont(Font.font(20));
            text.setFill(Color.BLUE);
            label.setFill(Color.BLUE);
            getChildren().addAll(text);

            setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY) {
                    if (focusedSquare.get() != this) {

                        focusedSquare.set(this);
                    } else {
                        focusedSquare.set(null);
                    }
                }
            });


        }

        public boolean hasValueRedo(){
            if(valueRedo != null){
                return true;
            }
            return false;
        }

        public boolean hasValueUndo(){
            if(valueRedo != null){
                return true;
            }
            return false;
        }

        public void setLabel(String label) {
            this.labeled = true ;
            this.label.setText(label);
            this.label.setFont(Font.font(15));
            FlowPane labelPane = new FlowPane();
            labelPane.setAlignment(Pos.TOP_LEFT);
            labelPane.getChildren().addAll(this.label);
            this.getChildren().addAll(labelPane);
        }

        public int getIndex() {
            return index;
        }

        public boolean isLabeled() {
            return labeled;
        }

        public Text getText() {
            return text;
        }

        public int getTextNumber() {
            if (!text.getText().equals("")) {
                return Integer.parseInt(text.getText());
            } else return -1;
        }

        public Text getLabel() {
            return label;
        }

        public void setX(int x) {
            this.x = x;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public boolean isConstructionVisited() {
            return constructionVisited;
        }

        public void setConstructionVisited() {
            this.constructionVisited = true;
        }

        public boolean isInGroup(GroupOfSquares group) {
            if (this.group.equals(group)) {
                return true;
            }
            return false;
        }

        public void setUndo(boolean undo) {
            this.undo = undo;
        }

        public void setRedo(boolean redo) {
            this.redo = redo;
        }

        public boolean isUndo() {
            return undo;
        }

        public boolean isRedo() {
            return redo;
        }

        public String getLastValue() {
            return lastValue;
        }

        public void setLastValue(String lastValue) {
            this.lastValue = lastValue;
        }

        public String getValueRedo() {
            return valueRedo;
        }

        public void setValueRedo(String valueRedo) {
            this.valueRedo = valueRedo;
        }

        public String getValueUndo() {
            return valueUndo;
        }

        public void setValueUndo(String valueUndo) {
            this.valueUndo = valueUndo;
        }
    }

    public class GroupOfSquares {
        ArrayList<Square> groupSquare = new ArrayList<>();
        String label;
        Square primary;
        String operation;
        int number;

        public GroupOfSquares(ArrayList<Square> groupSquare , String label) {
            this.groupSquare = groupSquare;
            this.label = label;
            primary = this.groupSquare.get(0);
            primary.setLabel(label);
            operation = getOperation();
            number = getNumber();
        }

        public Square getSquareByIndex(int index) {
            for (Square sqr : groupSquare) {
                if (sqr.getIndex() == index) {
                    return sqr;
                }
            }
            return null;
        }

        public Square getSquareByX(int x) {
            for (Square sqr : groupSquare) {

                if (sqr.getX() == x) {
                    return sqr;
                }
            }
            return null;
        }

        public Square getSquareByY(int y) {
            for (Square sqr : groupSquare) {

                if (sqr.getY() == y) {
                    return sqr;
                }
            }
            return null;
        }

        public Square getSquareByXY(int x , int y) {
            for (Square sqr : groupSquare) {

                if (sqr.getY() == x && sqr.getY() == y) {
                    return sqr;
                }
            }
            return null;
        }

        public ArrayList<Square> getGroupSquare() {
            return groupSquare;
        }

        public Square getSquareRightNeighbour(Square squareToSearch) {
            int indexToSearch = squareToSearch.getIndex() + 1;
            if (getSquareByIndex(indexToSearch) != null) {
                return getSquareByIndex(indexToSearch);
            } else {
                return null;
            }
        }

        public Square getSquareLeftNeighbour(Square squareToSearch) {
            int indexToSearch = squareToSearch.getIndex() - 1;
            if (getSquareByIndex(indexToSearch) != null) {
                return getSquareByIndex(indexToSearch);
            } else {
                return null;
            }
        }

        public Square getSquareTopNeighbour(Square squareToSearch) {
            int indexToSearch = squareToSearch.getIndex() - size;
            if (getSquareByIndex(indexToSearch) != null) {
                return getSquareByIndex(indexToSearch);
            } else {
                return null;
            }
        }

        public Square getSquareBottomNeighbout(Square squareToSearch) {
            int indexToSearch = squareToSearch.getIndex() + size;
            if (getSquareByIndex(indexToSearch) != null) {
                return getSquareByIndex(indexToSearch);
            } else {
                return null;
            }
        }

        public boolean hasSquare(Square squareToSearch) {
            if (squareToSearch == null) {
                return false;
            }

            if (getGroupSquare().contains(squareToSearch)) return true;
            else return false;

        }

        public String getOperation() {
            String operation = primary.getLabel().getText();
            operation = operation.replaceAll("[0-9]" , "");
            return operation;
        }

        public int getNumber() {
            String number = primary.getLabel().getText();
            number = number.replaceAll("[^0-9]" , "");
            int numberInt = Integer.parseInt(number);
            return numberInt;
        }

        public boolean verifyInput() {
            ArrayList<Integer> valuesOfNumbers = new ArrayList<>();
            for (Square sqr : getGroupSquare()) {
                if (sqr.getTextNumber() != -1) {
                    valuesOfNumbers.add(sqr.getTextNumber());
                }
            }
            Collections.sort(valuesOfNumbers , Collections.reverseOrder());
            if (operation.equals("+")) {
                int sum = 0;
                for (int i : valuesOfNumbers) {
                    sum = sum + i;
                }
                if (sum == number) {
                    return true;
                } else return false;
            } else if (operation.equals("-")) {
                if (!valuesOfNumbers.isEmpty()) {
                    int diff = valuesOfNumbers.get(0);
                    for (int i : valuesOfNumbers) {
                        if (i != valuesOfNumbers.get(0)) {
                            diff = diff - i;
                        }
                    }
                    if (diff == number) {
                        return true;
                    } else {
                        return false;
                    }

                }
            } else if (operation.equals("÷")) {
                if (!valuesOfNumbers.isEmpty()) {
                    int div = valuesOfNumbers.get(0);
                    for (int i : valuesOfNumbers) {
                        if (i != valuesOfNumbers.get(0)) {
                            div = (int) div / i;
                        }
                    }
                    if (div == number) {
                        return true;
                    } else {
                        return false;
                    }

                }
            } else if (operation.equals("x")) {
                int multiple = 1;
                for (int i : valuesOfNumbers) {
                    multiple = multiple * i;
                }
                if (multiple == number) {
                    return true;
                }
                return false;
            } else {
                if (!valuesOfNumbers.isEmpty()) {
                    if (valuesOfNumbers.get(0) == number) {
                        return true;
                    }
                    return false;
                }
            }

            return false;
        }

        public boolean isComplete(){




            for(Square s: getGroupSquare()){
                if(s.getTextNumber() == -1){
                    return false;
                }
            }

            return true;

        }

        public Square getPrimary() {
            return primary;
        }


    }


    public class LoadFromFile {
        File fileRead;
        ArrayList<Square> squares;
        ArrayList<GroupOfSquares> groups;
        ArrayList<String> errors = new ArrayList<String>();
        boolean correct = false;

        public LoadFromFile(File fileRead) throws IOException {
            this.fileRead = fileRead;
            System.out.println(getSize());
            System.out.println(getNumberofCages());
        }

        public File getFileRead(){
            return fileRead;
        }


        public void getNewGame() throws IOException {

                BufferedReader reader1 = new BufferedReader(new FileReader(getFileRead()));

                getGroups().clear();
                getSquareList().clear();
                size = (int) Math.sqrt(getSize());
                createGame();


                while (reader1.ready()) {
                    ArrayList<Square> squaresArray = new ArrayList<>();
                    String line = reader1.readLine();

                    String lineString[] = line.split(" ");
                    String operation = lineString[0];
                    //System.out.println(operation);

                    String squares[] = lineString[1].split(",");
                    for (int i = 0; i < squares.length; i++) {
                        // System.out.println(squares[i]);
                        squaresArray.add(getSquare(Integer.parseInt(squares[i])));
                    }

                    GroupOfSquares group = new GroupOfSquares(squaresArray, operation);
                    getGroups().add(group);
                    drawGraph(group);
                }





        }

        public int getNumberofCages() throws IOException {
            int t = 0;
            BufferedReader reader = new BufferedReader(new FileReader (getFileRead()));
            while (reader.ready()) {
                String line = reader.readLine();
                t++;
            }
            return t;
        }

        public int getSize() throws IOException {
            BufferedReader reader = new BufferedReader(new FileReader (getFileRead()));
            int i = 0;
            while (reader.ready()) {
                String line = reader.readLine();

                String inputs[] = line.split(" ");
                String numbers[] = inputs[1].split(",");
                for (String s : numbers) {
                    i++;
                }
            }
            return i;
        }

        public void areNeighbours() throws IOException {

            int cage = 1;
            BufferedReader reader = new BufferedReader(new FileReader (getFileRead()));
            while (reader.ready()) {
                ArrayList<Integer> inputsIndex = new ArrayList<>();
                String line = reader.readLine();

                String inputs[] = line.split(" ");
                String numbers[] = inputs[1].split(",");
                for (int i = 0; i < numbers.length; i++) {
                    inputsIndex.add(Integer.parseInt(numbers[i]));
                }
                Collections.sort(inputsIndex);

                for (int i = 0; i < inputsIndex.size()-1; i++) {
                    int verify = 0;
                    int p = -1;
                    if(inputsIndex.size() == 1){
                        verify = 1;
                    }
                    for(int j = 0; j< inputsIndex.size(); j++) {
                        if (inputsIndex.get(i) == inputsIndex.get(j) - 1 || inputsIndex.get(i) == inputsIndex.get(j) - Math.floor(Math.sqrt(getSize())) || inputsIndex.get(i) == inputsIndex.get(j) + 1 || inputsIndex.get(i) == inputsIndex.get(j) + Math.floor(Math.sqrt(getSize()))) {
                          verify = 1;
                          break;
                        } else {
                            p = j;
                        }
                    }
                    if(verify == 0){
                        getErrors().add("Cage " + cage + "can't be created because" + inputsIndex.get(i) + " doesn't have any neighbours");
                        return;
                    }
                }
            }
        }

        public void inputsUnique() throws IOException {
            ArrayList<Integer> numbersFile = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new FileReader (getFileRead()));
            while (reader.ready()) {
                String line = reader.readLine();

                String inputs[] = line.split(" ");
                String numbers[] = inputs[1].split(",");
                for (int i = 0; i < numbers.length; i++) {
                    numbersFile.add(Integer.parseInt(numbers[i]));
                }
            }

            for (int i = 0; i < numbersFile.size() - 1; i++) {
                for (int j = i + 1; j < numbersFile.size(); j++) {
                    if (numbersFile.get(i) == numbersFile.get(j)) {
                        getErrors().add("A cell appears in two cages");
                        return;
                    }
                }
            }



        }

        public void inputsTooHigh() throws IOException {
            ArrayList<Integer> numbersFile = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new FileReader (getFileRead()));
            while (reader.ready()) {
                String line = reader.readLine();

                String inputs[] = line.split(" ");
                String numbers[] = inputs[1].split(",");
                for (int i = 0; i < numbers.length; i++) {
                    numbersFile.add(Integer.parseInt(numbers[i]));
                }
            }


            for (int i = 0; i < numbersFile.size(); i++) {
                if (numbersFile.get(i) > getSize()) {
                   getErrors().add("Cells have a higher number than needed: " + numbersFile.get(i));
                    return;
                }

            }
        }

        public void verifySquare() throws IOException {
            if (Math.sqrt(getSize()) == Math.floor(Math.sqrt(getSize()))) {

            }
            else {

                getErrors().add("There are less cells or more cells than needed");


            }
        }

        public void verifyEmpty() throws IOException {
            BufferedReader reader = new BufferedReader(new FileReader (getFileRead()));
            if(reader.ready()){

            } else {
                getErrors().add("File is empty or can't be read");

            }
        }

        public boolean isCorrect() throws IOException {
            BufferedReader reader = new BufferedReader(new FileReader (getFileRead()));
            getErrors().clear();
            verifyEmpty();
            verifySquare();
            inputsUnique();
            inputsTooHigh();
            areNeighbours();
            for(String s : getErrors()){
                System.out.println(s);
            }
            if(getErrors().isEmpty()) {
               return true;
            }



                return false;


        }

        public ArrayList<String> getErrors() {
            return errors;
        }
    }


}


