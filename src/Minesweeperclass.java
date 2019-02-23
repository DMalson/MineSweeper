import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Random;


public class Minesweeperclass extends JFrame {

    private static Image[] mineImage;
    private static Image image1;
    private static Image imageFlag;
    private static Image imageWrong;
    private static Image imageMine;
    private static Image imageBoom;
    private static Minesweeperclass minesweepClassObj;
    private static int sizeX = 30;  //Количество столбцов
    private static int sizeY = 16;  //Количество строк
    private static int numberOfMines = 99;  //Количество мин
    private static int flagsToUse = numberOfMines;
    private static int[][][] mineField; // В [][][0] - данные о мине и окружении, в [][][1] - данные о разминировании
    private static boolean endOfTheGame = false;
    private static boolean gameStarted = false;
    private static boolean gameVictory = false;


    public static void main(String[] args) throws IOException {

        mineField = new int[sizeY][sizeX][2];
        mineImage = new Image[9];

        image1 = ImageIO.read(Minesweeperclass.class.getResourceAsStream("p_.png"));
        imageFlag = ImageIO.read(Minesweeperclass.class.getResourceAsStream("pf.png"));
        imageWrong = ImageIO.read(Minesweeperclass.class.getResourceAsStream("pw.png"));
        imageMine = ImageIO.read(Minesweeperclass.class.getResourceAsStream("pmine.png"));
        imageBoom = ImageIO.read(Minesweeperclass.class.getResourceAsStream("pmineboom.png"));
        for (int i = 0; i < 9; i++)
            mineImage[i] = ImageIO.read(Minesweeperclass.class.getResourceAsStream("p" + i + ".png"));

        minesweepClassObj = new Minesweeperclass();
        minesweepClassObj.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        minesweepClassObj.setLocation(200, 100);
        minesweepClassObj.setSize(30 * sizeX + 5, 30 * (sizeY + 1) + 5); // Компенсируем заголовок и рамки
        minesweepClassObj.setResizable(false);
        minesweepClassObj.setVisible(true);

        GameField gameField = new GameField();
        gameField.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                int myX = (e.getX() - e.getX() % 30) / 30;
                int myY = (e.getY() - e.getY() % 30) / 30;
                if (!gameStarted) {
                    fillMineField(mineField, myX, myY);
                    openMap(mineField, myX, myY);
                    gameStarted = true;
                    minesweepClassObj.setTitle("Осталось найти " + flagsToUse + " мин.");
                } else {
                    if (!endOfTheGame) {
                        switch (e.getButton()) {
                            case 3:
                                if (mineField[myY][myX][1] == -1) {
                                    mineField[myY][myX][1] = 0; //Снимаем флажок мины
                                    flagsToUse++;
                                } else if (flagsToUse > 0) {
                                    mineField[myY][myX][1] = -1; //Устанавливаем флажок мины
                                    flagsToUse--;
                                    endOfTheGame = checkMap(mineField);
                                    gameVictory = endOfTheGame;
                                }
                                minesweepClassObj.setTitle("Осталось найти " + flagsToUse + " мин.");
                                break;
                            default:
                                if (mineField[myY][myX][1] < 0) break;
                                if (mineField[myY][myX][0] < 0) {
                                    mineField[myY][myX][1] = 1;
                                    endOfTheGame = true;
                                    gameVictory = false;
                                } else {
                                    openMap(mineField, myX, myY);
                                    endOfTheGame = checkMap(mineField);
                                    gameVictory = endOfTheGame;
                                }
                        }
                    }
                }
                if (endOfTheGame) {
                    minesweepClassObj.setTitle(gameVictory ? "Поздравляем! Вы Выиграли!" : "Вы подорвались на мине. Игра окончена.");
                }
            }
        });

        minesweepClassObj.add(gameField);
        minesweepClassObj.setVisible(true);
    }

    public static void fillMineField(int[][][] mfield, int init_x, int init_y) { // Начальное заполнение минного поля
        Random rnd = new Random();
        for (int i = 0; i < sizeY; i++) {// Инициализация минного поля
            for (int j = 0; j < sizeX; j++) {
                mfield[i][j][0] = 0;
                mfield[i][j][1] = 0;
            }
        }

        for (int i = 0; i < numberOfMines; i++) {  // Цикл расстановки мин
            int mX = 0;
            int mY = 0;
            do { // Подбор незанятой клетки
                mX = rnd.nextInt(sizeX);
                mY = rnd.nextInt(sizeY);
            } while ((mX == init_x && mY == init_y) || (mfield[mY][mX][0] < 0));
            mfield[mY][mX][0] = -10;  // Устанавливаем мину

            for (int k = -1; k < 2; k++) {  //Изменение информацию о количестве мин вокруг в окружающих клетках
                for (int m = -1; m < 2; m++) {
                    if (mY + k >= 0 && mY + k < sizeY && mX + m >= 0 && mX + m < sizeX) {
                        mfield[mY + k][mX + m][0]++;
                    }
                }
            }
        }
    }

    public static void openMap(int[][][] mfield, int x, int y) { // Открытие карты при нажатии левой кнопки
        if (mfield[y][x][1] != 0) return;
        mfield[y][x][1] = 1;
        if (mfield[y][x][0] == 0) {
            for (int m = -1; m < 2; m++) {
                for (int k = -1; k < 2; k++) {
                    if (x + k >= 0 && x + k < sizeX && y + m >= 0 && y + m < sizeY) {
                        if ((mfield[y + m][x + k][0] == 0) && (mfield[y + m][x + k][1] == 0))
                            openMap(mfield, x + k, y + m);
                        mfield[y + m][x + k][1] = 1;
                    }
                }
            }
        } else {
            mfield[y][x][1] = 1;
        }
    }

    public static boolean checkMap(int[][][] mfield) {  // Проверка карты на заполненность

        int closedCells = sizeX * sizeY; // - numberOfMines;
        for (int k = 0; k < sizeY; k++) {
            for (int m = 0; m < sizeX; m++) {
                closedCells -= mfield[k][m][1] * mfield[k][m][1];
            }
        }
        return closedCells == 0;
    }

    private static class GameField extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            onRepaint(g);
            repaint();
        }
    }

    private static void onRepaint(Graphics g) {
        for (int i = 0; i < sizeY; i++)
            for (int j = 0; j < sizeX; j++)
                g.drawImage(getImage(i, j), j * 30, i * 30, null);
    }

    private static Image getImage(int i, int j) { //Выбор картинки для отрисовки ячейки в зависимости от контекста
        if (endOfTheGame) {
            if (mineField[i][j][0] < 0) {
                switch (mineField[i][j][1]) {
                    case -1:
                        return imageFlag;
                    case 1:
                        return imageBoom;
                    default:
                        return imageMine;
                }
            } else {
                switch (mineField[i][j][1]) {
                    case -1:
                        return imageWrong;
                    case 1:
                        return mineImage[mineField[i][j][0]];
                    default:
                        return image1;
                }
            }
        } else {
            switch (mineField[i][j][1]) {
                case -1:
                    return imageFlag;
                case 0:
                    return image1;
                default:
                    return mineImage[mineField[i][j][0]];
            }
        }
    }
}

