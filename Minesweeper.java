// Authors: Keshav Warrier & Vinny Kaushik

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import tester.*;
import javalib.impworld.*;

import java.awt.Color;

import javalib.worldimages.*;


// to represent a cell
class Cell {
  ArrayList<Cell> neighbors;
  boolean isFlagged;
  boolean isMine;
  boolean isRevealed;

  // Constructor with all fields
  Cell(ArrayList<Cell> neighbors, boolean isFlagged, boolean isMine, boolean isRevealed) {
    this.neighbors = neighbors;
    this.isFlagged = isFlagged;
    this.isMine = isMine;
    this.isRevealed = isRevealed;
  }

  // Constructor to easily create a new Cell
  Cell(ArrayList<Cell> neighbors) {
    this(neighbors, false, false, false);
  }

  // Count mines neighboring this cell
  public int countNeighboringMines() {
    int count = 0;
    for (Cell c : neighbors) {
      if (c.isMine) {
        count++;
      }
    }
    return count;
  }

  // produces an image of the given cell
  public WorldImage draw(int size) {
    if (this.isRevealed) {
      if (this.isMine) {
        return new OverlayImage(new RectangleImage(size, size, OutlineMode.OUTLINE, Color.BLACK),
                new OverlayImage(new CircleImage((int) (size * 0.25), OutlineMode.SOLID,
                        Color.getHSBColor(0F, 1F, 1F)),
                        new RectangleImage(size, size, OutlineMode.SOLID, Color.GRAY)));
      } else if (this.countNeighboringMines() > 0) {
        return new OverlayImage(new RectangleImage(size, size, OutlineMode.OUTLINE, Color.BLACK),
                new OverlayImage(new TextImage("" + this.countNeighboringMines(), Color.blue),
                        new RectangleImage(size, size, OutlineMode.SOLID, Color.GRAY)));
      } else {
        return new OverlayImage(new RectangleImage(size, size, OutlineMode.OUTLINE, Color.BLACK),
                new RectangleImage(size, size, OutlineMode.SOLID, Color.GRAY));
      }
    } else if (this.isFlagged) {
      return new OverlayImage(new RectangleImage(size, size, OutlineMode.OUTLINE, Color.BLACK),
              new OverlayImage(new EquilateralTriangleImage((int) (size * 0.66),
                      OutlineMode.SOLID, Color.red),
                      new RectangleImage(size, size, OutlineMode.SOLID,
                              Color.getHSBColor(1.4154F, 1, .5898F))));
    } else {
      return new OverlayImage(new RectangleImage(size, size, OutlineMode.OUTLINE, Color.BLACK),
              new RectangleImage(size, size, OutlineMode.SOLID,
                      Color.getHSBColor(1.4154F, 1, .5898F)));
    }
  }

  // Reveal this cell
  public void reveal(MinesweeperWorld w2) {
    if (!this.isMine) {
      this.isRevealed = true;
      if (this.countNeighboringMines() == 0) {
        this.flood(w2);
      }
    } else {
      w2.revealAllMines();
    }
  }

  // Should reveal all bordering cells for cells that are revealed with no
  // mines neighboring them
  public void flood(MinesweeperWorld mw) {
    for (Cell c : this.neighbors) {
      if (!c.isMine && !c.isRevealed && !c.isFlagged) {
        c.reveal(mw);
      }
    }
  }

  // Add or remove a flag from this cell
  public void flag(WorldScene w, Posn pos, MinesweeperWorld w2) {
    if (this.isFlagged) {
      w.placeImageXY(
              new RectangleImage(5, 5, OutlineMode.SOLID, Color.white),
              new Util().posToCellRow(pos, w2), new Util().posToCellCol(pos, w2));
      this.isFlagged = false;
    } else {
      this.isFlagged = true;
      w.placeImageXY(
              new EquilateralTriangleImage(3, OutlineMode.SOLID, Color.red),
              new Util().posToCellRow(pos, w2), new Util().posToCellCol(pos, w2));
    }
  }
}


// Represents a Minesweeper game
class MinesweeperWorld extends World {
  ArrayList<ArrayList<Cell>> board;
  int rows;
  int cols;
  int worldWidth;
  int worldHeight;
  int mines;
  int scaleFactor = 30;
  boolean done = false;
  Random r;
  WorldScene w;

  // Constructor for real game
  MinesweeperWorld(int rows, int cols, int mines) {
    this.rows = rows;
    this.cols = cols;
    this.worldWidth = rows * this.scaleFactor;
    this.worldHeight = cols * this.scaleFactor;
    this.mines = mines;
    this.r = new Random();
    this.board = new ArrayList<ArrayList<Cell>>();
    this.w = new WorldScene(this.worldWidth, this.worldHeight);
    for (int i = 0; i < this.rows; i++) {
      this.board.add(new ArrayList<Cell>());
      for (int j = 0; j < this.cols; j++) {
        this.board.get(i).add(new Cell(new ArrayList<Cell>()));
      }
    }
    this.placeMines();
    for (int i = 0; i < this.rows; i++) {
      for (int j = 0; j < this.cols; j++) {
        this.updateNeighbors(i, j);
      }
    }
  }

  // Constructor for testing
  MinesweeperWorld(int rows, int cols, int mines, Random r) {
    this.rows = rows;
    this.cols = cols;
    this.worldWidth = rows * this.scaleFactor;
    this.worldHeight = cols * this.scaleFactor;
    this.mines = mines;
    this.r = r;
    this.board = new ArrayList<ArrayList<Cell>>();
    this.w = new WorldScene(this.worldWidth, this.worldHeight);
    for (int i = 0; i < this.rows; i++) {
      this.board.add(new ArrayList<Cell>());
      for (int j = 0; j < this.cols; j++) {
        this.board.get(i).add(new Cell(new ArrayList<Cell>()));
      }
    }
  }

  // Update all the neighbors
  void updateNeighbors(int row, int col) {
    for (int i = row - 1; i <= row + 1; i++) {
      for (int j = col - 1; j <= col + 1; j++) {
        if (i < this.board.size() && j < this.board.get(0).size() && i >= 0 && j >= 0
                && !(i == row && j == col)) {
          this.board.get(row).get(col).neighbors.add(this.board.get(i).get(j));
        }
      }
    }
  }

  // Place mines randomly
  public void placeMines() {
    int minesPlaced = 0;
    while (minesPlaced < this.mines) {
      int row = this.r.nextInt(this.rows);
      int col = this.r.nextInt(this.cols);
      if (!this.board.get(row).get(col).isMine) {
        this.board.get(row).get(col).isMine = true;
        minesPlaced++;
      }
    }
  }

  // Make the scene
  public WorldScene makeScene() {
    for (int row = 0; row < this.rows; row++) {
      for (int col = 0; col < this.cols; col++) {
        this.w.placeImageXY(
                this.board.get(row).get(col).draw(this.scaleFactor),
                row * this.scaleFactor + (this.scaleFactor / 2),
                col * this.scaleFactor + (this.scaleFactor / 2));
      }
    }
    if (this.done) {
      if (this.checkWin()) {
        this.endOfWorld("You win!");
      } else {
        this.endOfWorld("You lose!");
      }
    }
    return this.w;
  }

  // Reveal all the mines on the board
  public void revealAllMines() {
    for (int i = 0; i < this.rows; i++) {
      for (Cell c : this.board.get(i)) {
        if (c.isMine) {
          c.isRevealed = true;
        }
      }
    }
    this.done = true;
  }

  // checks if all mines are uncovered
  public boolean checkWin() {
    this.done = true;
    int count = 0;
    for (ArrayList<Cell> row : this.board) {
      for (Cell c : row) {
        if (!c.isMine && c.isRevealed) {
          count++;
        }
      }
    }
    return count == this.rows * this.cols - this.mines;
  }

  // Handle mouse clicks, calls helpers for flagging and revealing
  public void onMouseClicked(Posn pos, String button) {
    Cell c = new Util().posToCell(pos, this);
    if (button.equals("RightButton")) {
      c.flag(this.w, pos, this);
    } else {
      c.reveal(this);
    }
  }

  // End of world scene
  public WorldScene lastScene(String msg) {
    this.w.placeImageXY(new TextImage(msg, 100, Color.white),
            300, 300);/*
    this.w.placeImageXY(new TextImage("Press any key to play again.",
            25, Color.WHITE), 300, 375);*/
    return this.w;
  }

  // calculates the number of mines based on the given level
  public int calculateMines(int level) {
    int total = 0;
    if (level == 1) {
      total = (this.rows * this.cols) / 4;
    } else if (level == 2) {
      total = (int) (this.rows * this.cols / 3.5);
    } else {
      total = this.rows * this.cols / 3;
    }
    return total;
  }

  /*public void onKeyEvent(String key) {
    if (this.done && key.equals("r")) {
      new MinesweeperWorld(this.rows, this.cols, this.mines);
    }
  }*/
}

// Class for Util methods
class Util {
  // Takes a Pos and returns the cell which contains the Pos
  public Cell posToCell(Posn pos, MinesweeperWorld w) {
    int row = (int) pos.x / (w.scaleFactor);
    int col = (int) pos.y / (w.scaleFactor);
    return w.board.get(row).get(col);
  }

  // Returns the row of the cell which pos is in
  public int posToCellRow(Posn pos, MinesweeperWorld w) {
    return (int) pos.x / (w.scaleFactor);
  }

  // Returns the column of the cell which pos is in
  public int posToCellCol(Posn pos, MinesweeperWorld w) {
    return (int) pos.y / (w.scaleFactor);
  }
}

// an examples class with
class ExamplesMinesweeper {
  Cell c1;
  Cell c2;
  Cell c3;
  Cell c4;
  Cell mine1;
  Cell mine2;

  MinesweeperWorld m1;
  MinesweeperWorld m2;
  MinesweeperWorld m3;
  MinesweeperWorld m4;
  MinesweeperWorld m5;

  void reset() {
    this.c1 = new Cell(new ArrayList<Cell>());
    this.c2 = new Cell(new ArrayList<Cell>());
    this.mine1 = new Cell(new ArrayList<Cell>(), false, true, false);
    this.mine2 = new Cell(new ArrayList<Cell>(), false, true, false);
    this.c3 = new Cell(new ArrayList<Cell>(List.of(this.c1, this.c2, this.mine1, this.mine2)));
    this.c4 = new Cell(new ArrayList<Cell>(List.of(this.mine1, this.mine1, this.mine1,
            this.mine1, this.mine1, this.mine1, this.mine1, this.mine1)));

    this.m1 = new MinesweeperWorld(30, 30, 300, new Random(0));
    this.m2 = new MinesweeperWorld(2, 2, 3, new Random(0));
    this.m3 = new MinesweeperWorld(2, 2, 1, new Random(0));
    this.m4 = new MinesweeperWorld(2, 2, 3, new Random(0));
    this.m5 = new MinesweeperWorld(20, 20, 40);
  }

  // Test the countNeighboringMines method
  void testCountNeighboringMines(Tester t) {
    reset();
    t.checkExpect(c1.countNeighboringMines(), 0);
    t.checkExpect(c3.countNeighboringMines(), 2);
    t.checkExpect(c4.countNeighboringMines(), 8);
  }

  // test updateNeighbors
  void testUpdateNeighbors(Tester t) {
    reset();
    t.checkExpect(this.m1.board.get(0).get(0).neighbors.size(), 0);
    t.checkExpect(this.m1.board.get(1).get(1).neighbors.size(), 0);
    t.checkExpect(this.m1.board.get(0).get(1).neighbors.size(), 0);
    t.checkExpect(this.m1.board.get(1).get(7).neighbors.contains(
            this.m1.board.get(0).get(0)), false);

    for (int i = 0; i < this.m1.rows; i++) {
      for (int j = 0; j < this.m1.cols; j++) {
        this.m1.updateNeighbors(i, j);
      }
    }

    t.checkExpect(this.m1.board.get(0).get(0).neighbors.size(), 3);
    t.checkExpect(this.m1.board.get(1).get(1).neighbors.size(), 8);
    t.checkExpect(this.m1.board.get(0).get(1).neighbors.size(), 5);

    t.checkExpect(this.m1.board.get(0).get(0).neighbors.get(0), this.m1.board.get(0).get(1));
    t.checkExpect(this.m1.board.get(1).get(1).neighbors.get(0), this.m1.board.get(0).get(0));
    t.checkExpect(this.m1.board.get(1).get(7).neighbors.contains(
            this.m1.board.get(0).get(0)), false);
  }

  // tests placeMines
  void testPlaceMines(Tester t) {
    reset();
    t.checkExpect(this.m3.board.get(0).get(0).isMine, false);
    t.checkExpect(this.m3.board.get(0).get(1).isMine, false);
    t.checkExpect(this.m3.board.get(1).get(0).isMine, false);
    t.checkExpect(this.m3.board.get(1).get(1).isMine, false);
    this.m3.placeMines();
    t.checkExpect(this.m3.board.get(0).get(0).isMine, false);
    t.checkExpect(this.m3.board.get(0).get(1).isMine, false);
    t.checkExpect(this.m3.board.get(1).get(0).isMine, false);
    t.checkExpect(this.m3.board.get(1).get(1).isMine, true);

    t.checkExpect(this.m4.board.get(0).get(0).isMine, false);
    t.checkExpect(this.m4.board.get(0).get(1).isMine, false);
    t.checkExpect(this.m4.board.get(1).get(0).isMine, false);
    t.checkExpect(this.m4.board.get(1).get(1).isMine, false);
    this.m4.placeMines();
    t.checkExpect(this.m4.board.get(0).get(0).isMine, false);
    t.checkExpect(this.m4.board.get(0).get(1).isMine, true);
    t.checkExpect(this.m4.board.get(1).get(0).isMine, true);
    t.checkExpect(this.m4.board.get(1).get(1).isMine, true);

  }

  // tests flood
  void testFlood(Tester t) {
    reset();
    this.c1.neighbors.add(this.c2);
    this.c2.neighbors.add(this.c1);
    this.c1.neighbors.add(this.c3);
    this.c3.neighbors.add(this.c1);
    this.c2.neighbors.add(this.c3);
    this.c3.neighbors.add(this.c2);
    this.c3.neighbors.add(this.c4);
    this.c4.neighbors.add(this.c3);
    this.c3.neighbors.add(this.mine1);
    this.mine1.neighbors.add(this.c3);
    this.c1.isRevealed = true;
    t.checkExpect(this.c1.isRevealed, true);
    t.checkExpect(this.c2.isRevealed, false);
    t.checkExpect(this.c3.isRevealed, false);
    t.checkExpect(this.c4.isRevealed, false);
    this.c1.flood(this.m1);
    t.checkExpect(this.c1.isRevealed, true);
    t.checkExpect(this.c2.isRevealed, true);
    t.checkExpect(this.c3.isRevealed, true);
    t.checkExpect(this.c4.isRevealed, false);
  }

  // tests the draw method
  void testDraw(Tester t) {
    reset();
    t.checkExpect(this.c1.draw(30), new OverlayImage(
            new RectangleImage(30, 30, OutlineMode.OUTLINE, Color.BLACK),
            new RectangleImage(30, 30, OutlineMode.SOLID,
                    Color.getHSBColor(1.4154F, 1, .5898F))));
    this.c2.isRevealed = true;
    this.c2.neighbors.add(this.mine1);
    t.checkExpect(this.c2.draw(30), new OverlayImage(
            new RectangleImage(30, 30, OutlineMode.OUTLINE, Color.BLACK),
            new OverlayImage(new TextImage("1", Color.blue),
                    new RectangleImage(30, 30, OutlineMode.SOLID, Color.GRAY))));
    this.c3.isRevealed = true;
    t.checkExpect(this.c3.draw(30), new OverlayImage(
            new RectangleImage(30, 30, OutlineMode.OUTLINE, Color.BLACK),
            new OverlayImage(new TextImage("2", Color.blue),
                    new RectangleImage(30, 30, OutlineMode.SOLID, Color.GRAY))));
    this.c4.isFlagged = true;
    t.checkExpect(this.c4.draw(30), new OverlayImage(
            new RectangleImage(30, 30, OutlineMode.OUTLINE, Color.BLACK),
            new OverlayImage(new EquilateralTriangleImage(19, OutlineMode.SOLID, Color.red),
                    new RectangleImage(30, 30, OutlineMode.SOLID,
                            Color.getHSBColor(1.4154F, 1, .5898F)))));
    this.c4.isFlagged = false;
    t.checkExpect(this.c4.draw(30),
            new OverlayImage(new RectangleImage(30, 30, OutlineMode.OUTLINE, Color.BLACK),
                    new RectangleImage(30, 30, OutlineMode.SOLID,
                            Color.getHSBColor(1.4154F, 1, .5898F))));
    t.checkExpect(this.mine1.draw(30), new OverlayImage(new RectangleImage(
            30, 30, OutlineMode.OUTLINE, Color.BLACK),
            new RectangleImage(30, 30, OutlineMode.SOLID,
                    Color.getHSBColor(1.4154F, 1, .5898F))));
    this.mine2.isRevealed = true;
    t.checkExpect(this.mine2.draw(30), new OverlayImage(new RectangleImage(
            30, 30, OutlineMode.OUTLINE, Color.BLACK),
            new OverlayImage(new CircleImage((int) (30 * 0.25),
                    OutlineMode.SOLID, Color.getHSBColor(0F, 1F, 1F)),
                    new RectangleImage(30, 30, OutlineMode.SOLID, Color.GRAY))));
  }


  void testBigBang(Tester t) {
    reset();
    int worldWidth = 600;
    int worldHeight = 600;
    double tickRate = 0.0000000000002;
    this.m5.placeMines();
    this.m5.bigBang(worldWidth, worldHeight, tickRate);
  }

  // test checkWin() using the tester library
  void testCheckWin(Tester t) {
    reset();
    this.m4.placeMines();
    t.checkExpect(this.m4.checkWin(), false);
    this.m4.board.get(0).get(0).isRevealed = true;
    t.checkExpect(this.m4.checkWin(), true);
  }
}