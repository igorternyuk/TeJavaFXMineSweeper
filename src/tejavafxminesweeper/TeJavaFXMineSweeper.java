 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tejavafxminesweeper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 *
 * @author igor
 */
public class TeJavaFXMineSweeper extends Application {
    private static final String TITLE_OF_PROGRAM = "MineSweeper";
    private static final int TILE_SIZE = 30;
    private static final int FIELD_X = 20;
    private static final int FIELD_Y = 15;
    private static final int WINDOW_WIDTH = FIELD_X * TILE_SIZE + 1;
    private static final int WINDOW_HEIGHT = FIELD_Y * TILE_SIZE + 1;    
    private static final int NUM_MINES = 60;
    private Tile[][] field = new Tile[FIELD_Y][FIELD_X];
    private static final Random random = new Random();
    private int numFlags = 0;
    private boolean isFirstStep = true;
    private GameState gameState = GameState.PLAY;
    private enum GameState{
      PLAY,
      VICTORY,
      DEFEAT
    };
    Text txtInfo = new Text();
    private enum TileState{
        CLOSED,
        FLAGGED,
        QUESTIONED,
        OPENED;
    };
    
    private class Tile extends StackPane {

        public Tile(int x, int y, boolean isMined) {
            this.x = x;
            this.y = y;
            this.isMined = isMined;
            borderRect.setFill(Color.BLUE);
            borderRect.setStroke(Color.BLACK);
            text.setText(isMined ? "X" : "");
            text.setFont(Font.font(18));
            text.setVisible(false);
            this.getChildren().addAll(borderRect, text);
            this.setTranslateX(x * TILE_SIZE + 1);
            this.setTranslateY(y * TILE_SIZE + 1);
            setOnMouseClicked((MouseEvent e) -> {
                //Left button click
                if (e.getButton() == MouseButton.PRIMARY) {
                    open();
                    updateView();
                } //Right button click
                else if (e.getButton() == MouseButton.SECONDARY) {
                    nextState();
                    updateView();
                }
                checkWin();
            });
        }
        
        public void open() {
            if(gameState != GameState.PLAY)
                return;
            if(state == TileState.CLOSED) {
                state = TileState.OPENED;
                if(isFirstStep){
                    setupMines();
                    isFirstStep = false;
                }
                text.setVisible(true);
                borderRect.setFill(Color.WHITE);
                if(isMined){
                    System.out.println("You lost!!!");
                    gameState = GameState.DEFEAT;
                    txtInfo.setText("YOU LOST!!!");
                    txtInfo.setFill(Color.RED);
                    txtInfo.setVisible(true);
                }
                else {
                    if(numMinesAround == 0) {
                        for (Tile tile : getNeighbours(this)) {
                            tile.open();
                        }
                    }
                }
            }         
        }

        public void nextState() {
            if(gameState != GameState.PLAY)
                return;
            switch(state){
                case CLOSED:
                    if(numFlags < NUM_MINES){
                        state = TileState.FLAGGED;
                        ++numFlags;
                    }
                    break;
                case FLAGGED:
                    --numFlags;
                    state = TileState.QUESTIONED;
                    break;
                case QUESTIONED:
                    state = TileState.CLOSED;
                    break;
                case OPENED:
                    break;
            }
            updateView();
        }
        
        private int x, y;
        private boolean isMined;
        private long numMinesAround = 0;
        TileState state = TileState.CLOSED;
        private Rectangle borderRect = new Rectangle(TILE_SIZE - 2,
                TILE_SIZE - 2);
        private Text text = new Text();
    }
    
    private boolean isCoordinatesValid(int x, int y){
        return x >= 0 && x < FIELD_X && y >= 0 && y < FIELD_Y;
    }
    
    private List<Tile> getNeighbours(Tile tile){
        List<Tile> list = new ArrayList<>();
            for(int dy = -1; dy <= 1; ++dy){
                for(int dx = -1; dx <= 1; ++dx){
                    if(dx == 0 && dy == 0)
                        continue;
                    int nx = tile.x + dx;
                    int ny = tile.y + dy;
                    //System.out.println("nx = " + nx + " ny = " + ny);
                    if(isCoordinatesValid(nx, ny)){
                        list.add(field[ny][nx]);
                    }
                }
        }
        return list;
    }
    
    private long countMinesAround(Tile tile){
        return getNeighbours(tile).stream().filter(t -> (t.isMined)).count();
    }
   
    private Parent createContent() {
        Pane root = new Pane();
        root.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        txtInfo.setTranslateX((WINDOW_WIDTH - txtInfo.getLayoutBounds().getWidth()) / 2);        
        txtInfo.setTranslateY((WINDOW_HEIGHT - txtInfo.getLayoutBounds().getHeight()) / 2);
        txtInfo.setFont(Font.font(80));
        txtInfo.setVisible(false);

        for(int y = 0; y < FIELD_Y; ++y) {
            for(int x = 0; x < FIELD_X; ++x){
                Tile tile = new Tile(x, y, false);
                field[y][x] = tile;
                root.getChildren().add(tile);
            }
        }
        root.getChildren().add(txtInfo);
        txtInfo.setTranslateX(60);
        txtInfo.setTranslateY(250);
        return root;
    }
    
    private void resetField(){
        for(int y = 0; y < FIELD_Y; ++y) {
            for(int x = 0; x < FIELD_X; ++x){
                Tile tile = new Tile(x, y, false);
                field[y][x].isMined = false;
                field[y][x].state = TileState.CLOSED;
            }
        }
    }
    
    private void setupMines(){
        for(int n = 0; n < NUM_MINES; ++n){
            int randX, randY;
            boolean isPosOK = false;
            do{
                randX = random.nextInt(FIELD_X);
                randY = random.nextInt(FIELD_Y);
                if(field[randY][randX].state == TileState.CLOSED &&
                        !field[randY][randX].isMined){
                    isPosOK = true;
                    field[randY][randX].isMined = true;
                }
            }while(!isPosOK);
        }
        
        for(int y = 0; y < FIELD_Y; ++y){
            for(int x = 0; x < FIELD_X; ++x){
                long num = countMinesAround(field[y][x]);
                field[y][x].numMinesAround = num;
                field[y][x].text.setText(field[y][x].isMined ? "X" : 
                        String.valueOf(num));
            }
        }
    }
    
    private void updateView(){
        for(int y = 0; y < FIELD_Y; ++y){
            for(int x = 0; x < FIELD_X; ++x){
                switch(field[y][x].state){
                    case CLOSED:
                        field[y][x].text.setVisible(false);
                        field[y][x].borderRect.setFill(Color.BLUE);
                        break;
                    case FLAGGED:
                        field[y][x].text.setText("F");
                        field[y][x].text.setFill(Color.RED);
                        field[y][x].text.setVisible(true);
                        field[y][x].borderRect.setFill(Color.BLUE);
                        break;
                    case QUESTIONED:
                        field[y][x].text.setText("?");
                        field[y][x].text.setFill(Color.RED);
                        field[y][x].text.setVisible(true);
                        field[y][x].borderRect.setFill(Color.BLUE);
                        break;
                    case OPENED:
                        field[y][x].text.setFill(getColorByNumOfMines(
                                field[y][x].numMinesAround));
                        if (field[y][x].isMined) {
                            field[y][x].text.setText("X");
                            field[y][x].text.setFill(Color.RED);
                        } else {
                            field[y][x].text.setText(
                                    field[y][x].numMinesAround > 0 ? 
                                            String.valueOf(field[y][x].
                                                    numMinesAround) : "");
                            field[y][x].text.setFill(getColorByNumOfMines(
                                field[y][x].numMinesAround));
                        }
                        field[y][x].text.setVisible(true);
                        field[y][x].borderRect.setFill(Color.WHITE);
                        break;
            }
            }
        }
        if(gameState != GameState.PLAY)
            txtInfo.setVisible(true);
    }
    
    private void checkWin(){
        boolean noClosedTiles = true;
        for(int y = 0; y < FIELD_Y; ++y){
            for(int x = 0; x < FIELD_X; ++x){
                if(field[y][x].state == TileState.CLOSED ||
                   field[y][x].state == TileState.QUESTIONED)
                    noClosedTiles = false;
            }
        }
        if(noClosedTiles && numFlags == NUM_MINES){
            gameState = GameState.VICTORY;
            System.out.println("You won!!!");
            txtInfo.setFill(Color.DARKGREEN);
            txtInfo.setText("YOU WON!!!");
            txtInfo.setVisible(true);
        }            
    };
    
    private Color getColorByNumOfMines(long numMines){
        switch((int)numMines){
            case 0:
                return Color.WHITE;
            case 1:
                return Color.BLUE;
            case 2:
                return Color.DARKGREEN;
            case 3:
                return Color.DARKRED;
            case 4:
                return Color.ORANGE;
            case 5:
                return Color.BLUEVIOLET;
            case 6:
                return Color.CORAL;
            case 7:
                return Color.DARKMAGENTA;
            case 8:
                return Color.DEEPPINK;
            default:
                return Color.GREEN;
        }
    }   
    
    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(createContent());
        scene.setOnKeyReleased((KeyEvent e) -> {
            if(e.getCode() == KeyCode.N){
                prepareNewGame();
            }
        });
        stage.setTitle(TITLE_OF_PROGRAM);
        stage.setScene(scene);
        stage.show();        
    } 
    
    public void prepareNewGame(){
        resetField();
        numFlags = 0;
        isFirstStep = true;
        gameState = GameState.PLAY;
        updateView();
        txtInfo.setVisible(false);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }    
}
