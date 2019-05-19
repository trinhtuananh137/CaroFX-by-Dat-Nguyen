/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CaroFXbyDatNguyen;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.Pane;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

/**
 * This panel lets two users play Go Moku against each other. Black always
 * starts the game.
 */
public class CaroFX extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    //---------------------------------------------------------------------
    private static final int // Constants to represent possible contents 
            EMPTY = 0, //     of squares on the board.  The constants
            BLACK = 1, //     BLACK and WHITE are also used to
            WHITE = 2;       //     represent the current player.
    public Integer seconds = 0;
    public Integer minutes = 0;
    public Integer hours = 0;
    public Timeline clock = null;

    private chessBoard board; // A canvas on which a checker board is drawn,
    // defined by a static nested subclass.  Much of
    // the game logic is defined in this class.
    public List<Point> listUndo = new ArrayList<>();

    private int boardEdge = 30;
    private int boardEdgePixels = 602;
    private Button btnNewGame;  // Button for starting a new game.

    private Button btnResign;   // Button that a player can use to end 
    // the game by resigning.

    private Button btnPlayWithCom;

    private Button btnUndo;

    private Label message;  // Label for displaying messages to the user.
    public static Label lbTime = new Label();

    private boolean isPlayWithCom = false;
    public EvalBoard eval = new EvalBoard();
    private final Random rand = new Random();
    private static int maxDepth; //Độ sâu lớn nhất
    public static int maxMove = 3; //
    public int depth = 0; //Độ sâu
    //public static final int SIZE = 25; // độ dài cạnh 1 ô vuông caro
    public static final int INT_MAX = Integer.MAX_VALUE;

    public int[] DScore = new int[]{0, 1, 9, 81, 729};
    public int[] AScore = new int[]{0, 2, 18, 162, 1458};

    public String[] caseX = {"11", "101", "1112", "2111", "1011", "1101", "111", "11011", "10111", "11101", "11112", "21111", "1111", "11111"};
    public String[] caseO = {"22", "202", "2221", "1222", "2022", "2202", "222", "22022", "20222", "22202", "22221", "12222", "2222", "22222"};
    public int[] pointArr = {5, 5, 10, 10, 500, 500, 500, 600, 600, 600, 600, 600, 5000, 5000};

    /**
     * The constructor creates the Board (which in turn creates and manages the
     * buttons and message label), adds all the components, and sets the bounds
     * of the components. A null layout is used. (This is the only thing that is
     * done in the main Checkers class.)
     */
    public void start(Stage stage) {
        clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            updateTimer();
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
        listUndo.clear();
        message = new Label("Click \"New Game\" to begin.");
        message.setTextFill(Color.rgb(100, 255, 100)); // Light green.
        message.setFont(Font.font(null, FontWeight.BOLD, 18));

        lbTime.setFont(new Font("TimeNewRoman", 20));
        lbTime.setTextFill(Color.YELLOW);
        lbTime.setLayoutX(650);
        lbTime.setLayoutY(300);

        btnNewGame = new Button("New Game");
        btnResign = new Button("Resign");
        btnPlayWithCom = new Button("playWithCom");
        btnUndo = new Button("Undo");
        board = new chessBoard();
        board.drawBoard();

        btnPlayWithCom.setOnAction(e -> board.doPlayWithCom());

        btnNewGame.setOnAction(e -> board.doNewGame());
        btnResign.setOnAction(e -> board.doResign());
        btnUndo.setOnAction(e -> board.doUndoMove());
        board.setOnMousePressed(e -> board.mousePressed(e));

        board.relocate(20, 20);
        btnNewGame.relocate(650, 60);
        btnResign.relocate(650, 120);
        btnUndo.relocate(650, 240);

        btnPlayWithCom.relocate(650, 180);
        message.relocate(20, 650);

        btnResign.setManaged(false);
        btnResign.resize(100, 30);
        btnNewGame.setManaged(false);
        btnNewGame.resize(100, 30);

        btnPlayWithCom.setManaged(false);
        btnPlayWithCom.resize(100, 30);

        Pane root = new Pane();

        root.setPrefWidth(800);
        root.setPrefHeight(700);

        root.getChildren().addAll(board, btnNewGame, btnResign, message, btnPlayWithCom, lbTime, btnUndo);
        clock.play();
        root.setStyle("-fx-background-color: darkgreen; "
                + "-fx-border-color: darkred; -fx-border-width:3");
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Game Caro");
        stage.show();

    } // end start()

    private class chessBoard extends Canvas {

        int[][] boardData;

        boolean gameInProgress;

        int currentPlayer;

        int win_r1, win_c1, win_r2, win_c2;

        chessBoard() {

            super(boardEdgePixels, boardEdgePixels);
            doNewGame();
        }

        // che do choi vs may
        void doPlayWithCom() {
            btnPlayWithCom.setDisable(true);
            if (isPlayWithCom == true) {
                seconds = 0;
                minutes = 0;
                hours = 0;
                clock.play();
            }
            isPlayWithCom = true;
            if (gameInProgress == true) {
                message.setText(" ");
                return;
            }
            boardData = new int[boardEdge][boardEdge];
            currentPlayer = BLACK;
            message.setText("BLACK: make your move");
            gameInProgress = true;
            btnNewGame.setDisable(false);
            btnResign.setDisable(false);
            btnPlayWithCom.setDisable(true);
            drawBoard();

        }

        // che do choi voi nguoi
        void doNewGame() {
            listUndo.clear();
            isPlayWithCom = false;
            if (gameInProgress == true) {
                // This should not be possible, but it doesn't hurt to check.
                message.setText("Finish the current game first!");
                return;
            }
            boardData = new int[boardEdge][boardEdge];   // Start the game with an empty board.
            //  This relies on the fact that EMPTY = 0.
            currentPlayer = BLACK;   // BLACK moves first.
            message.setText("Black:  Make your move.");
            gameInProgress = true;
            btnNewGame.setDisable(true);
            btnResign.setDisable(false);
            btnPlayWithCom.setDisable(false);
            seconds = 0;
            minutes = 0;
            hours = 0;
            clock.play();
            drawBoard();
        }

        void doResign() {
            if (gameInProgress == false) {  // Should be impossible.
                message.setText("There is no game in progress!");
                return;
            }
            if (currentPlayer == WHITE) {
                gameOver("WHITE resigns.  BLACK wins.");
            } else {
                gameOver("BLACK resigns.  WHITE wins.");
            }
            clock.pause();
        }

        void doUndoMove() {
            clock.play();
            int len = listUndo.size();
            if (len > 0 && isPlayWithCom == true) {
                Point p = listUndo.get(len - 1);
                Point _p = listUndo.get(len - 2);

                if (board.boardData[_p.x][_p.y] == 1) {
                    board.currentPlayer = BLACK;
                    message.setText("BLACK:  Make your move.");
                }
                if (board.boardData[_p.x][_p.y] == 2) {
                    board.currentPlayer = WHITE;
                    message.setText("WHITE:  Make your move.");
                }

                board.boardData[p.x][p.y] = 0;
                board.boardData[_p.x][_p.y] = 0;
                listUndo.remove(listUndo.size() - 1);
                listUndo.remove(listUndo.size() - 1);
                board.drawBoard();
                board.gameInProgress = true;
                btnNewGame.setDisable(false);
                btnResign.setDisable(false);
//                btnPlayWithCom.setDisable(false);
            }
            if (len > 0) {
                Point p = listUndo.get(len - 1);
                if (board.boardData[p.x][p.y] == 1) {
                    board.currentPlayer = BLACK;
                    message.setText("BLACK:  Make your move.");
                }
                if (board.boardData[p.x][p.y] == 2) {
                    board.currentPlayer = WHITE;
                    message.setText("WHITE:  Make your move.");
                }
                board.boardData[p.x][p.y] = 0;
                listUndo.remove(listUndo.size() - 1);
                board.drawBoard();
                board.gameInProgress = true;
                btnNewGame.setDisable(false);
                btnResign.setDisable(false);
                btnPlayWithCom.setDisable(false);
            } else {
                return;
            }

        }

        void gameOver(String str) {
            message.setText(str);
            btnNewGame.setDisable(false);
            btnResign.setDisable(true);
            gameInProgress = false;
            clock.pause();
        }

        void doClickSquare(int row, int col, boolean mode) {

            if (boardData[row][col] != EMPTY) {
                if (currentPlayer == BLACK) {
                    message.setText("BLACK:  Please click an empty square.");
                } else {
                    message.setText("WHITE:  Please click an empty square.");
                }
                return;
            }

            boardData[row][col] = currentPlayer;  // Make the move.
            listUndo.add(new Point(row, col));
            drawBoard();

            if (winner(row, col)) {  // First, check for a winner.
                if (currentPlayer == WHITE) {
                    gameOver("WHITE wins the game!");
                } else {
                    gameOver("BLACK wins the game!");
                }
                drawWinLine();
                return;
            }

            boolean emptySpace = false;     // Check if the board is full.
            for (int i = 0; i < boardEdge; i++) {
                for (int j = 0; j < boardEdge; j++) {
                    if (boardData[i][j] == EMPTY) {
                        emptySpace = true;
                    }
                }
            }
            if (emptySpace == false) {
                gameOver("The game ends in a draw.");
                return;
            }

            if (currentPlayer == BLACK) {
                currentPlayer = WHITE;
                message.setText("WHITE:  Make your move.");
            } else {
                currentPlayer = BLACK;
                message.setText("BLACK:  Make your move.");
            }
            // kiem tra che do choi neu bang 1 thi may tu dong danh
            if (mode == true) {
                // danh gia ban co
                EvalChessBoard(currentPlayer, eval, boardData);
                Point temp = new Point();
                temp = findMoveOfCom(currentPlayer, boardData);
                row = temp.x;
                col = temp.y;
                // make move
                boardData[row][col] = currentPlayer;
                listUndo.add(new Point(row, col));
                drawBoard();

                // kiem tra chien thang
                if (winner(row, col) == true) {
                    gameOver("Computer is win");
                    drawWinLine();
                    return;
                }
                // kiem tra day
                for (int i = 0; i < boardEdge; i++) {
                    for (int j = 0; j < boardEdge; j++) {
                        if (boardData[i][j] == EMPTY) {
                            emptySpace = true;
                        }
                    }
                }
                currentPlayer = BLACK;
                message.setText("BLACK: make your move");
            }

        }  // end doClickSquare()

        // ham danh gia ban co 
        private boolean winner(int row, int col) {

            if (count(boardData[row][col], row, col, 1, 0) >= 5) {
                return true;
            }
            if (count(boardData[row][col], row, col, 0, 1) >= 5) {
                return true;
            }
            if (count(boardData[row][col], row, col, 1, -1) >= 5) {
                return true;
            }
            if (count(boardData[row][col], row, col, 1, 1) >= 5) {
                return true;
            }

            /* When we get to this point, we know that the game is not won.*/
            return false;

        }  // end winner()

        private int count(int player, int row, int col, int dirX, int dirY) {

            int ct = 1;  // Number of pieces in a row belonging to the player.

            int r, c;    // A row and column to be examined

            r = row + dirX;  // Look at square in specified direction.
            c = col + dirY;
            while (r > row - 5 && r < row + 5 && c > col - 5 && c < col + 5 && boardData[r][c] == player) {
                // Square is on the board and contains one of the players's pieces.
                ct++;
                r += dirX;  // Go on to next square in this direction.
                c += dirY;
            }

            win_r1 = r - dirX;  // The next-to-last square looked at.
            win_c1 = c - dirY;  //    (The LAST one looked at was off the board or
            //    did not contain one of the player's pieces.

            r = row - dirX;  // Look in the opposite direction.
            c = col - dirY;
            while (r > row - 5 && r < row + 5 && c > col - 5 && c < col + 5 && boardData[r][c] == player) {
                // Square is on the board and contains one of the players's pieces.
                ct++;
                r -= dirX;   // Go on to next square in this direction.
                c -= dirY;
            }

            win_r2 = r + dirX;
            win_c2 = c + dirY;

            // At this point, (win_r1,win_c1) and (win_r2,win_c2) mark the endpoints
            // of the line of pieces belonging to the player.
            return ct;

        }  // end count()

        /**
         * Draws the board and the pieces on the board. This method does NOT
         * draw the red line through the winning pieces after the game has been
         * won.
         */
        public void drawBoard() {

            GraphicsContext g = getGraphicsContext2D();
            g.setFill(Color.LIGHTGRAY);  // fill canvas with light gray
            g.fillRect(0, 0, 720, 720);

            /* Draw lines separating the square and along the edges of the canvas.  */
            g.setStroke(Color.BLACK);
            g.setLineWidth(2);
            for (int i = 0; i <= boardEdge; i++) {
                g.strokeLine(1 + 24 * i, 0, 1 + 24 * i, boardEdgePixels);
                g.strokeLine(0, 1 + 24 * i, boardEdgePixels, 1 + 24 * i);
            }

            /* Draw the pieces that are on the board. */
            for (int row = 0; row < boardEdge; row++) {
                for (int col = 0; col < boardEdge; col++) {
                    if (boardData[row][col] != EMPTY) {
                        drawPiece(g, boardData[row][col], row, col);
                    }
                }
            }

        }  // end paintComponent()

        private void drawPiece(GraphicsContext g, int piece, int row, int col) {
            if (piece == WHITE) {
                g.setFill(Color.WHITE);
                g.fillOval(4 + 24 * col, 4 + 24 * row, 18, 18);
                g.setStroke(Color.BLACK);
                g.setLineWidth(1);
                g.strokeOval(4 + 24 * col, 4 + 24 * row, 18, 18);
            } else {
                g.setFill(Color.BLACK);
                g.fillOval(4 + 24 * col, 4 + 24 * row, 18, 18);
            }
        }

        private void drawWinLine() {
            GraphicsContext g = getGraphicsContext2D();
            g.setStroke(Color.RED);
            g.setLineWidth(4);
            g.strokeLine(13 + 24 * win_c1, 13 + 24 * win_r1, 13 + 24 * win_c2, 13 + 24 * win_r2);
        }

        public void mousePressed(MouseEvent evt) {
            if (gameInProgress == false) {
                message.setText("Click \"New Game\" to start a new game.");
            } else {
                int col = (int) ((evt.getX() - 2) / 24);
                int row = (int) ((evt.getY() - 2) / 24);
                if (col >= 0 && col < boardEdge && row >= 0 && row < boardEdge) {
                    doClickSquare(row, col, isPlayWithCom);
                }
            }
        }

        private void EvalChessBoard(int player, EvalBoard eBoard, int[][] boardArr) {
            int rw, cl, ePC, eHuman;
            //resetBoard(eBoard);
            //Danh gia theo hang
            for (rw = 0; rw < boardEdge; rw++) {
                for (cl = 0; cl < boardEdge - 4; cl++) {
                    ePC = 0;
                    eHuman = 0;
                    for (int i = 0; i < 5; i++) {
                        if (boardArr[rw][cl + i] == 1) {
                            eHuman++;
                        }
                        if (boardArr[rw][cl + i] == 2) {
                            ePC++;
                        }
                    }
                    if (eHuman * ePC == 0 && eHuman != ePC) {
                        for (int i = 0; i < 5; i++) {
                            if (boardArr[rw][cl + i] == 0) {
                                if (eHuman == 0) {
                                    if (player == 1) {
                                        eBoard.eBoard[rw][cl + i] += DScore[ePC];
                                    } else {
                                        eBoard.eBoard[rw][cl + i] += AScore[ePC];
                                    }
                                }
                                if (ePC == 0) {
                                    if (player == 2) {
                                        eBoard.eBoard[rw][cl + i] += DScore[eHuman];
                                    } else {
                                        eBoard.eBoard[rw][cl + i] += AScore[eHuman];
                                    }
                                }
                                if (eHuman == 4 || ePC == 4) {
                                    eBoard.eBoard[rw][cl + i] *= 2;
                                }
                            }
                        }
                    }
                }
            }
            //Danh gia theo cot
            for (cl = 0; cl < boardEdge; cl++) {
                for (rw = 0; rw < boardEdge - 4; rw++) {
                    ePC = 0;
                    eHuman = 0;
                    for (int i = 0; i < 5; i++) {
                        if (boardArr[rw + i][cl] == 1) {
                            eHuman++;
                        }
                        if (boardArr[rw + i][cl] == 2) {
                            ePC++;
                        }
                    }
                    if (eHuman * ePC == 0 && eHuman != ePC) {
                        for (int i = 0; i < 5; i++) {
                            if (boardArr[rw + i][cl] == 0) {
                                if (eHuman == 0) {
                                    if (player == 1) {
                                        eBoard.eBoard[rw + i][cl] += DScore[ePC];
                                    } else {
                                        eBoard.eBoard[rw + i][cl] += AScore[ePC];
                                    }
                                }
                                if (ePC == 0) {
                                    if (player == 2) {
                                        eBoard.eBoard[rw + i][cl] += DScore[eHuman];
                                    } else {
                                        eBoard.eBoard[rw + i][cl] += AScore[eHuman];
                                    }
                                }
                                if (eHuman == 4 || ePC == 4) {
                                    eBoard.eBoard[rw + i][cl] *= 2;
                                }
                            }
                        }

                    }
                }
            }
            //Danh gia duong cheo xuoi
            for (cl = 0; cl < boardEdge - 4; cl++) {
                for (rw = 0; rw < boardEdge - 4; rw++) {
                    ePC = 0;
                    eHuman = 0;
                    for (int i = 0; i < 5; i++) {
                        if (boardArr[rw + i][cl + i] == 1) {
                            eHuman++;
                        }
                        if (boardArr[rw + i][cl + i] == 2) {
                            ePC++;
                        }
                    }
                    if (eHuman * ePC == 0 && eHuman != ePC) {
                        for (int i = 0; i < 5; i++) {
                            if (boardArr[rw + i][cl + i] == 0) {
                                if (eHuman == 0) {
                                    if (player == 1) {
                                        eBoard.eBoard[rw + i][cl + i] += DScore[ePC];
                                    } else {
                                        eBoard.eBoard[rw + i][cl + i] += AScore[ePC];
                                    }
                                }
                                if (ePC == 0) {
                                    if (player == 2) {
                                        eBoard.eBoard[rw + i][cl + i] += DScore[eHuman];
                                    } else {
                                        eBoard.eBoard[rw + i][cl + i] += AScore[eHuman];
                                    }
                                }
                                if (eHuman == 4 || ePC == 4) {
                                    eBoard.eBoard[rw + i][cl + i] *= 2;
                                }
                            }
                        }
                    }
                }
            }
            //Danh gia duong cheo nguoc
            for (rw = 4; rw < boardEdge; rw++) {
                for (cl = 0; cl < boardEdge - 4; cl++) {
                    ePC = 0;
                    eHuman = 0;
                    for (int i = 0; i < 5; i++) {
                        if (boardArr[rw - i][cl + i] == 1) {
                            eHuman++;
                        }
                        if (boardArr[rw - i][cl + i] == 2) {
                            ePC++;
                        }
                    }
                    if (eHuman * ePC == 0 && eHuman != ePC) {
                        for (int i = 0; i < 5; i++) {
                            if (boardArr[rw - i][cl + i] == 0) {
                                if (eHuman == 0) {
                                    if (player == 1) {
                                        eBoard.eBoard[rw - i][cl + i] += DScore[ePC];
                                    } else {
                                        eBoard.eBoard[rw - i][cl + i] += AScore[ePC];
                                    }
                                }
                                if (ePC == 0) {
                                    if (player == 2) {
                                        eBoard.eBoard[rw - i][cl + i] += DScore[eHuman];
                                    } else {
                                        eBoard.eBoard[rw - i][cl + i] += AScore[eHuman];
                                    }
                                }
                                if (eHuman == 4 || ePC == 4) {
                                    eBoard.eBoard[rw - i][cl + i] *= 2;
                                }
                            }
                        }

                    }
                }
            }
        }

        private int EvalDangerous(int[][] b) {
            int n = boardEdge;
            String s = "";
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    s += b[i][j];
                }
                s += ";";
                for (int j = 0; j < n; j++) {
                    s += b[j][i];
                }
                s += ";";

            }
            for (int i = 0; i < n - 4; i++) {
                for (int j = 0; j < n - i; j++) {
                    s += b[j][i + j];
                }
                s += ";";
            }
            for (int i = n - 5; i > 0; i--) {
                for (int j = 0; j < n - i; j++) {
                    s += b[i + j][j];
                }
                s += ";";
            }
            for (int i = 4; i < n; i++) {
                for (int j = 0; j <= i; j++) {
                    s += b[i - j][j];
                }
                s += ";";
            }
            for (int i = n - 5; i > 0; i--) {
                for (int j = n - 1; j >= i; j--) {
                    s += b[j][i + n - j - 1];
                }
                s += ";\n";
            }
            Pattern pattern1, pattern2;
            int diem = 0;
            for (int i = 0; i < caseO.length; i++) {
                pattern1 = Pattern.compile(caseX[i]);
                pattern2 = Pattern.compile(caseO[i]);
                Matcher m1 = pattern1.matcher(s);
                Matcher m2 = pattern2.matcher(s);
                int count1 = 0;
                int count2 = 0;
                while (m1.find()) {
                    count1++;
                }
                while (m2.find()) {
                    count2++;
                }
                diem += pointArr[i] * count2;
                diem -= pointArr[i] * count1;
            }
            System.out.println("Diem: " + diem);
            return diem;
        }

        private Point getMaxPoint() {
            ArrayList<Point> list = new ArrayList<>();
            int t = -INT_MAX;
            for (int i = 0; i < boardEdge; i++) {
                for (int j = 0; j < boardEdge; j++) {
                    if (t < eval.eBoard[i][j]) {
                        t = eval.eBoard[i][j];
                        list.clear();
                        list.add(new Point(i, j));
                    } else if (t == eval.eBoard[i][j]) {
                        list.add(new Point(i, j));
                    }
                }
            }
            for (int i = 0; i < list.size(); i++) {
                eval.eBoard[list.get(i).x][list.get(i).y] = 0;
            }
            int x = rand.nextInt(list.size());
            return list.get(x);
        }

        public Point findMoveOfCom(int player, int[][] arr) {
            int n = boardEdge;
            int[][] b = new int[n][n];
            for (int i = 0; i < n; i++) {
                System.arraycopy(arr[i], 0, b[i], 0, n);
            }
//        playerFlag = player;
            EvalChessBoard(2, eval, b);
            ArrayList<Point> list = new ArrayList<>();
            for (int i = 0; i < maxMove; i++) {
                list.add(getMaxPoint());
            }
            int maxp = -INT_MAX;
            ArrayList<Point> ListChoose = new ArrayList<>();
            for (Point list1 : list) {
                b[list1.x][list1.y] = player;
                int t = minVal(b, -INT_MAX, INT_MAX, 0);
                if (maxp < t) {
                    maxp = t;
                    ListChoose.clear();
                    ListChoose.add(list1);
                } else if (maxp == t) {
                    ListChoose.add(list1);
                }
                b[list1.x][list1.y] = 0;
            }
            int x = rand.nextInt(ListChoose.size());
            return ListChoose.get(x);
        }

        private int maxVal(int[][] arrBoard, int alpha, int beta, int depth) {
            int val = EvalDangerous(arrBoard);
            if (depth >= maxDepth || Math.abs(val) > 5000) {
                return val;
            }
            EvalChessBoard(2, eval, arrBoard);
            ArrayList<Point> list = new ArrayList<>();
            for (int i = 0; i < maxMove; i++) {
                list.add(getMaxPoint());
            }
            for (Point list1 : list) {
                arrBoard[list1.x][list1.y] = 2;
                alpha = Math.max(alpha, minVal(arrBoard, alpha, beta, depth + 1));
                arrBoard[list1.x][list1.y] = 0;
                if (alpha > beta) {
                    break;
                }
            }
            return alpha;
        }

        private int minVal(int[][] arrBoard, int alpha, int beta, int depth) {
            int val = EvalDangerous(arrBoard);
            if (depth >= maxDepth || Math.abs(val) > 5000) {
                return val;
            }
            EvalChessBoard(1, eval, arrBoard);
            ArrayList<Point> list = new ArrayList<>();
            for (int i = 0; i < maxMove; i++) {
                list.add(getMaxPoint());
            }
            for (Point list1 : list) {
                arrBoard[list1.x][list1.y] = 1;
                beta = Math.min(beta, maxVal(arrBoard, alpha, beta, depth + 1));
                arrBoard[list1.x][list1.y] = 0;
                if (alpha >= beta) {

                    break;
                }
            }
            return beta;
        }
    }  // end nested class Board

    public class EvalBoard {

        public int height, width;
        public int[][] eBoard;

        public EvalBoard() {
            height = boardEdge;
            width = boardEdge;
            eBoard = new int[boardEdge][boardEdge];
        }

    }

    void updateTimer() {
        String _seconds;
        String _minutes;
        String _hours;
        if (seconds == 60) {
            seconds = 00;
            minutes++;
        }
        if (minutes == 60) {
            minutes = 00;
            hours++;
        }

        if (seconds < 10) {
            _seconds = "0" + seconds;
        } else {
            _seconds = seconds.toString();
        }

        if (minutes < 10) {
            _minutes = "0" + minutes;
        } else {
            _minutes = minutes.toString();
        }
        if (hours < 10) {
            _hours = "0" + hours;
        } else {
            _hours = hours.toString();
        }
        lbTime.setText(String.valueOf("Time : " + _hours + ":" + _minutes + ":" + _seconds));

        seconds++;
    }

} // end class CaroFX
