package ltlme;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import javafx.application.*;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.print.Printer;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ltlme.ListenToLightMapEditor.MainStage.MainRoot.CenterPane.MidPaneWrapper.CellPane.LCell;
import ltlme.ListenToLightMapEditor.MainStage.MainRoot.CenterPane.MidPaneWrapper.MidPane.LLine;
import javafx.scene.input.*;

public class ListenToLightMapEditor extends Application {
	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		MainStage mainStage = new MainStage();
		mainStage.show();
	}

	class MainStage extends Stage {
		MainRoot mainRoot = new MainRoot();
		Scene mainScene = new Scene(mainRoot);
		// main stage ini
		{
			mainScene.getStylesheets().add("/mainSceneStyle.css");
			this.setScene(mainScene);

		}

		class MainRoot extends BorderPane {
			GridPane topPane = new GridPane();
			MenuBar menuBar = new MenuBar();
			ToolBar toolBar = new ToolBar();
			ToggleButton addLineBtn = new ToggleButton("line");
			ToggleButton cutBtn = new ToggleButton("cut");
			ToggleButton slideBtn = new ToggleButton("slide");
			
			Button newBtn = new Button("new");
			Button openBtn = new Button("open");
			Button saveBtn = new Button("save");
			Button saveAsBtn = new Button("saveAs");
			Button exportBtn = new Button("export");
			Button exportTxtBtn = new Button("export-txt");
			Button exportFinalBtn = new Button("export-final");
			
			Button deleteBtn = new Button("delete");
			Button extendBtn = new Button("extend");
			Button mergeBtn = new Button("merge");
			Button hAlignBtn = new Button();
			Button vAlignBtn = new Button();
			Button bakeBtn = new Button("bake");
			
			CheckBox cellBox = new CheckBox("cell");
			CheckBox ocellBox = new CheckBox("ocell");
			{
				ToggleGroup tGroup = new ToggleGroup();
				cutBtn.setToggleGroup(tGroup);
				addLineBtn.setToggleGroup(tGroup);
				slideBtn.setToggleGroup(tGroup);
				toolBar.getItems().addAll(addLineBtn, cutBtn, slideBtn, new Separator(), 
						newBtn, openBtn, saveBtn, saveAsBtn, exportBtn, exportTxtBtn, exportFinalBtn, new Separator(),
						deleteBtn, extendBtn, mergeBtn, hAlignBtn,
						vAlignBtn, bakeBtn, ocellBox, cellBox);
				topPane.addRow(0, menuBar);
				topPane.addRow(0, toolBar);

				hAlignBtn.setGraphic(new Line(0, 0, 16, 0));
				vAlignBtn.setGraphic(new Line(0, 0, 0, 20));
			}

			CenterPane centerPane = new CenterPane();
			Label statusLabel = new Label();

			// main root ini
			{
				this.setTop(topPane);
				this.setCenter(centerPane);
				this.setBottom(statusLabel);
			}

			class CenterPane extends SplitPane {
				GridPane leftPane = new GridPane();
				GridPane rightPane = new GridPane();
				MidPaneWrapper midPaneWrapper = new MidPaneWrapper();
				
				// center pane ini
				{
					this.getItems().addAll(leftPane, midPaneWrapper, rightPane);
					this.setDividerPositions(0, 1);
				}

				class MidPaneWrapper extends StackPane {
					MidPane midPane = new MidPane();
					CellPane cellPane = new CellPane();
					final int PANE_WIDTH = 1000;
					final int PANE_HEIGHT = 500;
					final int CELL_WIDTH = 50;
					final int CELL_HEIGHT = 50;
					final int WIDTH_FACTOR = 5;
					final int HEIGHT_FACTOR = 10;
					class MidPane extends Pane {
						int cnt = 0;
						final int NONE = cnt++;
						final int ADDING_LINE = cnt++;
						int state = NONE;
						ArrayList<LLine> selectedLines = new ArrayList<>();
						ArrayList<LLCircle> selectedCircles = new ArrayList<>();
						ArrayList<KeyCode> downKeys = new ArrayList<>();
						Circle player = new Circle(PANE_WIDTH / 2.0, PANE_HEIGHT / 2.0, 20, Color.rgb(80, 189, 200, 0.5));
						boolean isPopup = false;
						FileManager fileManager = new FileManager();

						private void addSelectedLine(LLine line) {
							if (selectedLines.contains(line))
								return;
							LLine actLine = getActiveLine();
							if (actLine != null) {
								actLine.setId(null);
								actLine.getStyleClass().add("selected-line");
							}
							line.setId("active-line");
							selectedLines.add(line);
						}

						private void addSelectedCircle(LLCircle circle) {
							if (selectedCircles.contains(circle))
								return;
							LLCircle actCircle = getActiveCircle();
							if (actCircle != null) {
								actCircle.setId(null);
								actCircle.getStyleClass().add("selected-cir");
							}
							circle.setId("active-cir");
							selectedCircles.add(circle);
						}

						private void removeSelectedLine(LLine line) {
							LLine actLine = getActiveLine();
							if (actLine != null && line == actLine && selectedLines.size() >= 2) {
								selectedLines.get(selectedLines.size() - 2).setId("active-line");
								selectedLines.get(selectedLines.size() - 2).getStyleClass().remove("selected-line");
							}
							line.setId(null);
							line.getStyleClass().remove("selected-line");
							selectedLines.remove(line);
						}

						private void removeSelectedCircle(LLCircle circle) {
							LLCircle actCircle = getActiveCircle();
							if (actCircle != null && circle == actCircle && selectedCircles.size() >= 2) {
								selectedCircles.get(selectedCircles.size() - 2).setId("active-cir");
								selectedCircles.get(selectedCircles.size() - 2).getStyleClass().remove("selected-cir");
							}
							circle.setId(null);
							circle.getStyleClass().remove("selected-cir");
							selectedCircles.remove(circle);
						}

						private void clearSelectedLine() {
							ArrayList<LLine> tmp = new ArrayList<>();
							selectedLines.forEach(l -> tmp.add(l));
							tmp.forEach(l -> removeSelectedLine(l));
						}

						private void clearSelectedCircle() {
							ArrayList<LLCircle> tmp = new ArrayList<>();
							selectedCircles.forEach(c -> tmp.add(c));
							tmp.forEach(c -> removeSelectedCircle(c));
						}

						private LLine getActiveLine() {
							if (selectedLines.isEmpty())
								return null;
							return selectedLines.get(selectedLines.size() - 1);
						}

						private LLCircle getActiveCircle() {
							if (selectedCircles.isEmpty())
								return null;
							return selectedCircles.get(selectedCircles.size() - 1);
						}

						private void addLine(LLine line) {
							this.getChildren().add(0, line);
							addSelectedLine(line);
						}

						private void deleteLine(LLine line) {
							this.getChildren().remove(line);
							if (selectedLines.contains(line)) {
								removeSelectedLine(line);
							}
							line.destroy();
						}

						private void deleteCircle(LLCircle circle) {
							this.getChildren().remove(circle);
							if (selectedCircles.contains(circle)) {
								removeSelectedCircle(circle);
							}
							circle.destroy();
						}

						private void extendsCircle() {
							LLCircle circle = getActiveCircle();
							if (circle == null)
								return;
							LLine line = new LLine(circle.getCenterX(), circle.getCenterY());
							LLCircle c2 = new LLCircle();
							line.bindStart(circle);
							line.bindEnd(c2);
							addLine(line);
							this.getChildren().add(c2);
							state = ADDING_LINE;
						}

						private void addCircle(LLCircle circle) {
							this.getChildren().add(circle);
							addSelectedCircle(circle);
						}

						private void cancelAddLine() {
							state = NONE;
							deleteLine(getActiveLine());
						}

						private void merge(LLCircle c1, LLCircle c2) {
							c1.setCenterX(c2.getCenterX());
							c1.setCenterY(c2.getCenterY());
							ArrayList<LLine> tmp = new ArrayList<>();
							((ArrayList<LLine>) (c1.lines.clone())).forEach(l -> {
								LLCircle c = l.getOtherC(c1);
								if (c == c2)
									tmp.add(l);
								else {
									for (LLine l2 : ((ArrayList<LLine>) (c.lines.clone()))) {
										if (l2.getOtherC(c) == c2) {
											tmp.add(l);
											break;
										}
									}
								}
								l.bindCircle(l.unbindCircle(c1), c2);
							});
							for (LLine l : tmp) {
								deleteLine(l);
							}
						}

						private void merge() {
							if (selectedCircles.size() < 2)
								return;
							ArrayList<LLCircle> tmp = (ArrayList<LLCircle>) selectedCircles.clone();
							LLCircle actC = getActiveCircle();
							tmp.forEach(c -> {
								if (c != actC)
									merge(c, actC);
							});
						}

						private void hAlign() {
							LLine line = getActiveLine();
							LLCircle circle = getActiveCircle();
							if (line == null || circle == null)
								return;
							LLCircle oCircle = line.getOtherC(circle);
							if (oCircle == null)
								return;
							circle.setCenterY(oCircle.getCenterY());
						}

						private void vAlign() {
							LLine line = getActiveLine();
							LLCircle circle = getActiveCircle();
							if (line == null || circle == null)
								return;
							LLCircle oCircle = line.getOtherC(circle);
							if (oCircle == null)
								return;
							circle.setCenterX(oCircle.getCenterX());
						}

						private void deleteSelectedObj() {
							ArrayList<LLine> tmp = (ArrayList<LLine>) selectedLines.clone();
							tmp.forEach(l -> deleteLine(l));

							ArrayList<LLCircle> tmp2 = (ArrayList<LLCircle>) selectedCircles.clone();
							tmp2.forEach(c -> deleteCircle(c));
						}

						private void clearAll(){
							Object[]circles = this.lookupAll(".circle").toArray();
							for(Object circle: circles){
								deleteCircle((LLCircle)circle);
							}
						}
						
						// mid pane ini
						{
							this.setPrefSize(PANE_WIDTH, PANE_HEIGHT);
							this.setMaxSize(PANE_WIDTH, PANE_HEIGHT);
							this.setId("mid-pane");
							this.getChildren().add(player);
							LUtil.enableNodeTranslateMove(player);
							this.setOnKeyPressed(e -> {
								if (e.getCode() == KeyCode.UNDEFINED)
									return;
								if (downKeys.contains(e.getCode()))
									return;
								if (e.getCode() == KeyCode.E) {
									this.extendsCircle();
								} else if (e.getCode() == KeyCode.M) {
									this.merge();
								} else if (e.getCode() == KeyCode.L) {
									addLineBtn.setSelected(!addLineBtn.isSelected());
								} else if (e.getCode() == KeyCode.S) {
									slideBtn.setSelected(!slideBtn.isSelected());
								} else if (e.getCode() == KeyCode.C) {
									cutBtn.setSelected(!cutBtn.isSelected());
								} else if (e.getCode() == KeyCode.D) {
									deleteSelectedObj();
								}
								downKeys.add(e.getCode());
							});
							this.setOnKeyReleased(e -> {
								if (e.getCode() == KeyCode.UNDEFINED)
									return;
								if (!downKeys.contains(e.getCode()))
									return;
								downKeys.remove(e.getCode());
							});

							deleteBtn.setOnAction(e -> deleteSelectedObj());

							extendBtn.setOnAction(e -> this.extendsCircle());

							mergeBtn.setOnAction(e -> this.merge());

							hAlignBtn.setOnAction(e -> this.hAlign());

							vAlignBtn.setOnAction(e -> this.vAlign());

							this.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
								this.requestFocus();
							});

							this.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
								if (e.getButton() == MouseButton.PRIMARY) {
									if (addLineBtn.isSelected() || state == ADDING_LINE) {
										LLine line = new LLine(e.getX(), e.getY());
										LLCircle c2 = new LLCircle();
										line.bindEnd(c2);
										addCircle(c2);
										LLCircle c1 = null;
										if (state != ADDING_LINE) {
											c1 = new LLCircle();
											addCircle(c1);
											state = ADDING_LINE;
										} else {
											c1 = getActiveLine().getC2();
										}
										addLine(line);
										line.bindStart(c1);
									} else if (!slideBtn.isSelected()) {
										clearSelectedLine();
										clearSelectedCircle();
									}

									if (e.getClickCount() == 2 && !isPopup) {
										Stage utilStage = new Stage();
										StackPane utilRoot = new StackPane(midPaneWrapper);
										Scene utilScene = new Scene(utilRoot, PANE_WIDTH + 50, PANE_HEIGHT + 50);
										utilScene.getStylesheets().add("/mainSceneStyle.css");
										utilStage.setScene(utilScene);
										utilStage.initStyle(StageStyle.UTILITY);
										utilStage.setAlwaysOnTop(true);
										utilStage.setOnCloseRequest(e2 -> {
											double[] tmp = centerPane.getDividerPositions().clone();
											centerPane.getItems().set(1, midPaneWrapper);
											centerPane.setDividerPositions(tmp);
											isPopup = false;
										});
										isPopup = true;
										double[] tmp = centerPane.getDividerPositions().clone();
										centerPane.getItems().set(1, new Pane());
										centerPane.setDividerPositions(tmp);
										utilStage.show();
									}
								} else if (e.getButton() == MouseButton.SECONDARY) {
									if (state == ADDING_LINE) {
										cancelAddLine();
									}
								}
							});

							this.addEventHandler(MouseEvent.MOUSE_MOVED, e -> {
								if (state == ADDING_LINE) {
									getActiveLine().setEnd(e.getX(), e.getY());
								}
							});

						}

						class LLine extends Line {
							LLCircle c1 = null;
							LLCircle c2 = null;

							public LLine(double x1, double y1, double x2, double y2) {
								this.getStyleClass().add("line");
								setStart(x1, y1);
								setEnd(x2, y2);
								this.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
									if (cutBtn.isSelected()) {
										LLCircle newC = new LLCircle();
										LLine newL = new LLine(this.getStartX(), this.getStartY(), e.getX(), e.getY());
										newL.bindStart(this.getC1());
										newL.bindEnd(newC);
										addLine(newL);
										addCircle(newC);
										this.unbindStart();
										this.setStart(e.getX(), e.getY());
										this.bindStart(newC);
									}

									if (downKeys.contains(KeyCode.SHIFT)) {
										if (selectedLines.contains(this)) {
											removeSelectedLine(this);
										} else {
											addSelectedLine(this);
										}
									} else {
										clearSelectedLine();
										addSelectedLine(this);
									}
									e.consume();
								});

							}

							public LLine(double x, double y) {
								this(x, y, x, y);
							}

							public void setStart(double x, double y) {
								this.setStartX(x);
								this.setStartY(y);
							}

							public void setEnd(double x, double y) {
								this.setEndX(x);
								this.setEndY(y);
							}

							public void bindStart(LLCircle c1) {
								this.c1 = c1;
								c1.getLines().add(this);
								c1.centerXProperty().bindBidirectional(this.startXProperty());
								c1.centerYProperty().bindBidirectional(this.startYProperty());
							}

							public void bindEnd(LLCircle c2) {
								this.c2 = c2;
								c2.getLines().add(this);
								c2.centerXProperty().bindBidirectional(this.endXProperty());
								c2.centerYProperty().bindBidirectional(this.endYProperty());
							}

							public void unbindStart() {
								c1.getLines().remove(this);
								c1.centerXProperty().unbindBidirectional(this.startXProperty());
								c1.centerYProperty().unbindBidirectional(this.startYProperty());
								if (c1.getLines().isEmpty()) {
									deleteCircle(c1);
								}
								c1 = null;
							}

							public void unbindEnd() {
								c2.getLines().remove(this);
								c2.centerXProperty().unbindBidirectional(this.endXProperty());
								c2.centerYProperty().unbindBidirectional(this.endYProperty());
								if (c2.getLines().isEmpty()) {
									deleteCircle(c2);
								}
								c2 = null;
							}

							public boolean unbindCircle(LLCircle c) {
								if (c == c1) {
									unbindStart();
									return true;
								}
								unbindEnd();
								return false;
							}

							public void bindCircle(boolean flag, LLCircle c) {
								if (flag)
									bindStart(c);
								else
									bindEnd(c);
							}

							public LLCircle getOtherC(LLCircle c) {
								return c == c1 ? c2 : c == c2 ? c1 : null;
							}

							public LLCircle getC1() {
								return c1;
							}

							public LLCircle getC2() {
								return c2;
							}

							public void destroy() {
								unbindStart();
								unbindEnd();
							}
						}

						class LLCircle extends Circle {
							final int RADII = 5;
							private ArrayList<LLine> lines = new ArrayList<>();

							public ArrayList<LLine> getLines() {
								return lines;
							}

							public LLCircle() {
								this.setRadius(RADII);
								this.getStyleClass().add("circle");
								EventHandler<MouseEvent> pressHandler = e -> {
									if (e.getButton() == MouseButton.PRIMARY) {
										if (slideBtn.isSelected()) {
											LLine line = getActiveLine();
											if (line == null)
												return;
											LLCircle oCircle = line.getOtherC(this);
											if (oCircle == null)
												return;
											this.getProperties().put("clickpos", new Point2D(e.getX(), e.getY()));
											this.getProperties().put("oriStart",
													new Point2D(this.getCenterX(), this.getCenterY()));
											this.getProperties().put("oriEnd",
													new Point2D(oCircle.getCenterX(), oCircle.getCenterY()));
										} else {
											this.getProperties().put("lastpos", new Point2D(e.getX(), e.getY()));
										}
									}
								};

								EventHandler<MouseEvent> clickHandler = e -> {
									if (downKeys.contains(KeyCode.SHIFT)) {
										if (selectedCircles.contains(this)) {
											removeSelectedCircle(this);
										} else {
											addSelectedCircle(this);
										}
									} else {
										clearSelectedCircle();
										addSelectedCircle(this);
									}
									if (state != ADDING_LINE || slideBtn.isSelected())
										e.consume();
								};

								EventHandler<MouseEvent> dragHandler = e -> {
									if (e.getButton() != MouseButton.PRIMARY)
										return;
									Point2D curP = new Point2D(e.getX(), e.getY());
									if (!slideBtn.isSelected()) {
										Object obj = this.getProperties().get("lastpos");
										if (obj == null)
											return;
										Point2D lastP = (Point2D) obj;
										Point2D dP = new Point2D(curP.getX() - lastP.getX(),
												curP.getY() - lastP.getY());
										this.setCenterX(this.getCenterX() + dP.getX());
										this.setCenterY(this.getCenterY() + dP.getY());
										this.getProperties().put("lastpos", curP);
									} else {
										LLine line = getActiveLine();
										if (line == null)
											return;
										LLCircle oCircle = line.getOtherC(this);
										if (oCircle == null)
											return;
										Point2D oriStart = (Point2D) this.getProperties().get("oriStart");
										Point2D oriEnd = (Point2D) this.getProperties().get("oriEnd");
										double lineDx = oriEnd.getX() - oriStart.getX();
										double lineDy = oriEnd.getY() - oriStart.getY();
										double lineLen = Math.hypot(lineDx, lineDy);
										if (lineLen == 0)
											return;
										double a = Math.hypot(lineDx, lineDy);
										double b = Math.hypot(curP.getX() - oriStart.getX(),
												curP.getY() - oriStart.getY());
										double c = Math.hypot(curP.getX() - oriEnd.getX(), curP.getY() - oriEnd.getY());
										if (a == 0 || b == 0 || c == 0)
											return;
										double cosXita = (a * a + b * b - c * c) / (2 * a * b);
										double dis = b * cosXita;
										double t = dis / lineLen;
										this.setCenterX(oriStart.getX() + t * lineDx);
										this.setCenterY(oriStart.getY() + t * lineDy);
									}
								};

								this.addEventHandler(MouseEvent.MOUSE_PRESSED, pressHandler);
								this.addEventHandler(MouseEvent.MOUSE_DRAGGED, dragHandler);
								this.addEventFilter(MouseEvent.MOUSE_CLICKED, clickHandler);
							}

							public LLCircle(double cenx, double ceny){
								this();
								this.setCenterX(cenx);
								this.setCenterY(ceny);
							}
							
							public void destroy() {
								ArrayList<LLine> tmp = new ArrayList<>();
								lines.forEach(l -> tmp.add(l));
								tmp.forEach(l -> deleteLine(l));
							}
						}
					
						class FileManager{
							File curFile = null;
							File lastDir = null;
							File defaultDir = new File(System.getProperty("user.dir"));
							ExtensionFilter ltlef = new ExtensionFilter("LTL Files", "*.ltl");
							ExtensionFilter ltlmeef = new ExtensionFilter("LTLTME Files", "*.ltlme");
							FileChooser fileChooser = new FileChooser();
							public FileManager(){
								newBtn.setOnAction(e -> newFile());
								saveBtn.setOnAction(e -> save());
								saveAsBtn.setOnAction(e -> saveAs());
								openBtn.setOnAction(e -> open());
								exportBtn.setOnAction(e -> export());
								exportTxtBtn.setOnAction(e -> exportTxt());
								exportFinalBtn.setOnAction(e -> exportFinal());
								fileChooser.getExtensionFilters().addAll(ltlef);
								MainStage.this.setTitle("untitled");
							}
							
							private void newFile(){
								curFile = null;
								clearAll();
								MainStage.this.setTitle("untitled");
							}
							
							private void save(){
								if(curFile == null)saveAs();
								else writeFile();
							}
							
							private void open(){
								fileChooser.setInitialDirectory(lastDir == null? defaultDir : lastDir);
								curFile = fileChooser.showOpenDialog(MainStage.this);
								if(curFile == null)return;
								updateLastDir(LUtil.getFileDir(curFile));
								readFile();
							}
							
							private void saveAs(){
								fileChooser.setInitialDirectory(lastDir == null? defaultDir : lastDir);
								curFile = fileChooser.showSaveDialog(MainStage.this);
								if(curFile == null)return;
								updateLastDir(LUtil.getFileDir(curFile));
								MainStage.this.setTitle(curFile.getName());
								writeFile();
							}
							
							private void export(){
								
								fileChooser.getExtensionFilters().set(0, ltlmeef);
								fileChooser.setInitialDirectory(lastDir == null? defaultDir : lastDir);
								File mapFile = fileChooser.showSaveDialog(MainStage.this);
								fileChooser.getExtensionFilters().set(0, ltlef);
								if(mapFile == null)return;
								updateLastDir(LUtil.getFileDir(mapFile));
								
								try(
										DataOutputStream dos = new DataOutputStream(new FileOutputStream(mapFile));
								){
									dos.writeUTF("LTL MAP");
									
									Object[] lines = midPane.lookupAll(".line").toArray();
									dos.writeInt(lines.length);
									for(int i = 0; i < lines.length; i++){
										LLine line = (LLine)lines[i];
										dos.writeDouble(line.getStartX());
										dos.writeDouble(line.getStartY());
										dos.writeDouble(line.getEndX());
										dos.writeDouble(line.getEndY());							
										line.getProperties().put("index", i);
									}
									
									ArrayList<LCell>cells = new ArrayList<>();
									for(int i = 0; i < cellPane.rowNum; i++){
										for(int j = 0; j < cellPane.colNum; j++){
											LCell cell = cellPane.cells[i][j];
											if(cell.lines.isEmpty())continue;
											cells.add(cell);
										}
									}
									
									dos.writeInt(cells.size());
									for(LCell lcell : cells){
										dos.writeInt(lcell.r);
										dos.writeInt(lcell.c);
										dos.writeInt(lcell.lines.size());
										ArrayList<LLine>lines2 = lcell.lines;
										for(LLine line : lines2){
											dos.writeInt((Integer)line.getProperties().get("index"));
										}
									}
									
									dos.close();
								}
								catch(Exception e){
									
								}
								
							}
							
							private void exportTxt(){
								
								fileChooser.getExtensionFilters().set(0, ltlmeef);
								fileChooser.setInitialDirectory(lastDir == null? defaultDir : lastDir);
								File mapFile = fileChooser.showSaveDialog(MainStage.this);
								fileChooser.getExtensionFilters().set(0, ltlef);
								if(mapFile == null)return;
								updateLastDir(LUtil.getFileDir(mapFile));
								
								try{
									PrintWriter printer = new PrintWriter(mapFile);
									printer.println("LTL MAP");
									
									Object[] lines = midPane.lookupAll(".line").toArray();
									printer.println(lines.length);
									for(int i = 0; i < lines.length; i++){
										LLine line = (LLine)lines[i];
										printer.println(line.getStartX());
										printer.println(line.getStartY());
										printer.println(line.getEndX());
										printer.println(line.getEndY());							
										line.getProperties().put("index", i);
									}
									
									ArrayList<LCell>cells = new ArrayList<>();
									for(int i = 0; i < cellPane.rowNum; i++){
										for(int j = 0; j < cellPane.colNum; j++){
											LCell cell = cellPane.cells[i][j];
											if(cell.lines.isEmpty())continue;
											cells.add(cell);
										}
									}
									
									printer.println(cells.size());
									for(LCell lcell : cells){
										printer.println(lcell.r);
										printer.println(lcell.c);
										printer.println(lcell.lines.size());
										ArrayList<LLine>lines2 = lcell.lines;
										for(LLine line : lines2){
											printer.println((Integer)line.getProperties().get("index"));
										}
									}
									
									printer.close();
								}
								catch(Exception e){
									
								}
								
							}
							
							private void exportFinal() {
								fileChooser.getExtensionFilters().set(0, ltlmeef);
								fileChooser.setInitialDirectory(lastDir == null? defaultDir : lastDir);
								File mapFile = fileChooser.showSaveDialog(MainStage.this);
								fileChooser.getExtensionFilters().set(0, ltlef);
								if(mapFile == null)return;
								updateLastDir(LUtil.getFileDir(mapFile));
								
								try{
									PrintWriter printer = new PrintWriter(mapFile);
									printer.println("LTL MAP");
									//地图大小
									printer.println(PANE_WIDTH * WIDTH_FACTOR);
									printer.println(PANE_HEIGHT * HEIGHT_FACTOR);
									//横竖格子数目
									printer.println(PANE_WIDTH / CELL_WIDTH);
									printer.println(PANE_HEIGHT / CELL_HEIGHT);
									//玩家初始位置
									printer.println((player.getCenterX() + player.getTranslateX()) * WIDTH_FACTOR);
									printer.println((player.getCenterY() + player.getTranslateY()) * HEIGHT_FACTOR);
									Object[] lines = midPane.lookupAll(".line").toArray();
									printer.println(lines.length);
									for(int i = 0; i < lines.length; i++){
										LLine line = (LLine)lines[i];
										printer.println(line.getStartX() * WIDTH_FACTOR);
										printer.println(line.getStartY() * HEIGHT_FACTOR);
										printer.println(line.getEndX() * WIDTH_FACTOR);
										printer.println(line.getEndY() * HEIGHT_FACTOR);							
										line.getProperties().put("index", i);
									}
									
									ArrayList<LCell>cells = new ArrayList<>();
									for(int i = 0; i < cellPane.rowNum; i++){
										for(int j = 0; j < cellPane.colNum; j++){
											LCell cell = cellPane.cells[i][j];
											if(cell.lines.isEmpty())continue;
											cells.add(cell);
										}
									}
									
									printer.println(cells.size());
									for(LCell lcell : cells){
										printer.println(lcell.r);
										printer.println(lcell.c);
										printer.println(lcell.lines.size());
										ArrayList<LLine>lines2 = lcell.lines;
										for(LLine line : lines2){
											printer.println((Integer)line.getProperties().get("index"));
										}
									}
									
									printer.close();
								}
								catch(Exception e){
									
								}
							}
							
							private void readFile(){
								try(
										DataInputStream dis = new DataInputStream(new FileInputStream(curFile));
								){
									String header = dis.readUTF();
									if(!header.equals("LTL FILE")){
										Alert alert = new Alert(AlertType.ERROR);
										alert.setContentText("this isn't a ltl file");
										alert.show();
										return;
									}
									
									ArrayList<LLCircle>circles = new ArrayList<>();
									int cirNum = dis.readInt();
									for(int i = 0; i < cirNum; i++){
										double cenx = dis.readDouble();
										double ceny = dis.readDouble();
										LLCircle circle = new LLCircle(cenx, ceny);
										circles.add(circle);
										midPane.addCircle(circle);
									}
									
									int lineNum = dis.readInt();
									for(int i = 0; i < lineNum; i++){
										double startx = dis.readDouble();
										double starty = dis.readDouble();
										double endx = dis.readDouble();
										double endy = dis.readDouble();		
										LLine line = new LLine(startx, starty, endx, endy);
										int c1i = dis.readInt();
										int c2i = dis.readInt();
										LLCircle c1 = circles.get(c1i);
										LLCircle c2 = circles.get(c2i);
										line.bindStart(c1);
										line.bindEnd(c2);
										midPane.addLine(line);
									}
									
									dis.close();
								}
								catch(Exception e){
									
								}
							}
							
							private void writeFile(){
								try(
										DataOutputStream dos = new DataOutputStream(new FileOutputStream(curFile));
								){
									dos.writeUTF("LTL FILE");
									
									Object[] lines = midPane.lookupAll(".line").toArray();
									Object[] circles = midPane.lookupAll(".circle").toArray();
									dos.writeInt(circles.length);
									for(int i = 0; i < circles.length; i++){
										LLCircle circle = (LLCircle)circles[i];
										dos.writeDouble(circle.getCenterX());
										dos.writeDouble(circle.getCenterY());
										circle.getProperties().put("index", i);
									}
									
									dos.writeInt(lines.length);
									for(int i = 0; i < lines.length; i++){
										LLine line = (LLine)lines[i];
										dos.writeDouble(line.getStartX());
										dos.writeDouble(line.getStartY());
										dos.writeDouble(line.getEndX());
										dos.writeDouble(line.getEndY());		
										LLCircle c1 = line.getC1();
										LLCircle c2 = line.getC2();
										dos.writeInt((Integer)c1.getProperties().get("index"));
										dos.writeInt((Integer)c2.getProperties().get("index"));
									}
									
									dos.close();
								}
								catch(Exception e){
									
								}
							}
						
							private void updateLastDir(File file){
								if(lastDir == null || !lastDir.equals(file)){
									lastDir = file;
								}
							}
						}
					}

					class CellPane extends Pane{
						int rowNum = PANE_HEIGHT / CELL_HEIGHT;
						int colNum = PANE_WIDTH / CELL_WIDTH;
						LCell[][]cells = new LCell[rowNum][colNum];
						
						//ini cell pane
						{
							this.setPrefSize(PANE_WIDTH, PANE_HEIGHT);
							this.setMaxSize(PANE_WIDTH, PANE_HEIGHT);
							this.setId("cell-pane");
							
							for(int i = 0; i < rowNum; i++){
								for(int j = 0; j < colNum; j++){
									boolean bottom = (i == rowNum - 1);
									boolean right = (j == colNum - 1);
									cells[i][j] = new LCell(true, right, bottom, true);
									cells[i][j].setRect(j * CELL_WIDTH, i * CELL_HEIGHT, CELL_WIDTH, CELL_HEIGHT);;
									cells[i][j].visibleProperty().bind(cellBox.selectedProperty());
									cells[i][j].r = i;
									cells[i][j].c = j;
									this.getChildren().add(cells[i][j]);
									this.getChildren().add(cells[i][j].rectangle);
								}
							}
							
							bakeBtn.setOnAction(e -> bake());
							
						}
						
						Service<Void>bakeServce = new Service<Void>() {
							
							@Override
							protected Task<Void> createTask() {
								// TODO Auto-generated method stub
								return new Task<Void>(){
									
									@Override
									protected Void call() throws Exception {
										// TODO Auto-generated method stub
										Object[]lines = midPane.lookupAll(".line").toArray();
										for(LCell[] ls : cells){
											for(LCell l : ls){
												l.clearLine();
												l.rectangle.getStyleClass().remove("ocell");
											}
										}
										for(int i = 0; i < lines.length; i++){
											occupy((LLine)lines[i]);
											updateProgress(i, lines.length);
										}
										return null;
									}
									
									private void occupy(LLine line){
										double x0 = line.getStartX();
										double y0 = line.getStartY();
										double x1 = line.getEndX();
										double y1 = line.getEndY();
										int r = (int)(y0 / CELL_HEIGHT);
										int c = (int)(x0 / CELL_WIDTH);
										if(r < 0 || c < 0 || r >= rowNum || c >= colNum)return;
										LCell lcell = cells[r][c];
										Queue<LCell>queue = new LinkedList<LCell>();
										boolean[][] vis = new boolean[rowNum][colNum];
										for(int i = 0; i < rowNum; i++){
											Arrays.fill(vis[i], false);
										}
										int[][] dir = {{0, 0}, {0, 0}};
										double dx = x1 - x0;
										double dy = y1 - y0;
										if(dx > 0)dir[0][0] = 1;
										else if(dx < 0)dir[0][0] = -1;
										if(dy > 0)dir[1][1] = 1;
										else if(dy < 0)dir[1][1] = -1;
										queue.offer(lcell);
										while(!queue.isEmpty()){	
											lcell = queue.poll();
//											if(!lcell.lines.contains(line)){
//												lcell.addLine(line);
//												if(!lcell.rectangle.getStyleClass().contains("ocell")){
//													lcell.rectangle.getStyleClass().add("ocell");
//												}
//											} 
											for(int i = lcell.r - 1; i <= lcell.r + 1; i++){
												for(int j = lcell.c - 1; j <= lcell.c + 1; j++){
													if(i <0 || j < 0 || i >= rowNum || j >= colNum)continue;
													if(!cells[i][j].lines.contains(line)){
														cells[i][j].lines.add(line);
														if(!cells[i][j].rectangle.getStyleClass().contains("ocell")){
															cells[i][j].rectangle.getStyleClass().add("ocell");
														}
													}
												}
											}
											r = lcell.r;
											c = lcell.c;
											for(int i = 0; i < 2; i++){
												int nextC = c + dir[i][0];
												int nextR = r + dir[i][1];
												if(nextC < 0 || nextR < 0 || nextC >= colNum || nextR >= rowNum || vis[nextR][nextC])continue;
												vis[nextR][nextC] = true;
												lcell = cells[nextR][nextC];
												if(LUtil.lineIntersectsRect(x0, y0, x1, y1, lcell.getX(), lcell.getY(), lcell.getWidth(), lcell.getHeight())){
													queue.offer(lcell);
												}
												
											}
										}
										queue = null;
										vis = null;
									}
									
								};
							}
							
							
							{
								this.setOnSucceeded(e -> {
									bakeServce.reset();
									statusLabel.setGraphic(null);
									topPane.setDisable(false);
									centerPane.setDisable(false);
								});
					
							}
						};
						
						ProgressBar pBar = new ProgressBar();{
							pBar.progressProperty().bind(bakeServce.progressProperty());
							pBar.minWidthProperty().bind(MainRoot.this.widthProperty());
						}
						
						private void bake(){
							topPane.setDisable(true);
							centerPane.setDisable(true);
							statusLabel.setGraphic(pBar);
							if(bakeServce.getState() == State.READY)
								bakeServce.start();
						}
						
						class LCell extends Path{
							SimpleDoubleProperty x = new SimpleDoubleProperty(0);
							SimpleDoubleProperty y = new SimpleDoubleProperty(0);
							SimpleDoubleProperty width = new SimpleDoubleProperty(0);
							SimpleDoubleProperty height = new SimpleDoubleProperty(0);
							SimpleBooleanProperty hasTop = new SimpleBooleanProperty(false);
							SimpleBooleanProperty hasRight = new SimpleBooleanProperty(false);
							SimpleBooleanProperty hasBottom = new SimpleBooleanProperty(false);
							SimpleBooleanProperty hasLeft = new SimpleBooleanProperty(false);
							Rectangle rectangle = new Rectangle();
							int r;
							int c;
							ArrayList<LLine>lines = new ArrayList<>();
							public LCell(boolean top, boolean right, boolean bottom, boolean left){
								hasTop.set(top);
								hasRight.set(right);
								hasBottom.set(bottom);
								hasLeft.set(left);
								this.getStyleClass().add("lcell");
								rectangle.getStyleClass().add("rectangle");
								rectangle.visibleProperty().bind(ocellBox.selectedProperty());
							}
							
							public void setRect(double x, double y, double w, double h){
								this.x.set(x);
								this.y.set(y);
								this.width.set(w);
								this.height.set(h);
								redraw();
							}
							
							private void redraw(){
								this.getElements().clear();
								double x0 = x.get();
								double y0 = y.get();
								double x1 = x0 + width.get();
								double y1 = y0;
								double x2 = x1;
								double y2 = y1 + height.get();
								double x3 = x0;
								double y3 = y2;
								rectangle.setX(x.get());
								rectangle.setY(y.get());
								rectangle.setWidth(width.get());
								rectangle.setHeight(height.get());
								if(hasTop.get()){
									this.getElements().addAll(new MoveTo(x0, y0), new LineTo(x1, y1));
								}
								
								if(hasRight.get()){
									this.getElements().addAll(new MoveTo(x1, y1), new LineTo(x2, y2));
								}
								
								if(hasBottom.get()){
									this.getElements().addAll(new MoveTo(x2, y2), new LineTo(x3, y3));
								}
								
								if(hasLeft.get()){
									this.getElements().addAll(new MoveTo(x3, y3), new LineTo(x0, y0));
								}
							}
							
							@Override
							public String toString(){
								return "(" + r + ", " + c + ")" + "[" + x.get() + ", " + y.get() + ", " + width.get() + ", " + height.get() + "]"; 
							}
						
							public double getX(){ return x.get(); }
							public double getY(){ return y.get(); }
							public double getWidth(){ return width.get(); }
							public double getHeight(){ return height.get(); }
							public void addLine(LLine l){ 
								lines.add(l);	
							}
							public void clearLine(){ 
								lines.clear(); 
							}

						}
					
						
					}
					
					//ini mid pane wrapper
					{
						this.getChildren().addAll(cellPane, midPane);
						this.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
						this.setId("container");
					}
				}
			}
		}
	}
}
