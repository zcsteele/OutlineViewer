package edu.wpi.first.outlineviewer.controller;

import edu.wpi.first.outlineviewer.NetworkTableUtilities;
import edu.wpi.first.outlineviewer.Preferences;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Controller for the connection indicator at the bottom of the main window.
 */
public class ConnectionIndicatorController {

  private static final PseudoClass CLIENT = PseudoClass.getPseudoClass("client");
  private static final PseudoClass SERVER = PseudoClass.getPseudoClass("server");
  private static final PseudoClass FAILED = PseudoClass.getPseudoClass("failed");

  @FXML
  private Pane root;
  @FXML
  private Label connectionLabel;

  @FXML
  private void initialize() {
    NetworkTableUtilities.getNetworkTableInstance().addConnectionListener(listener
        -> updateConnectionLabel(), true);
    Preferences.serverProperty().addListener(__ -> updateConnectionLabel());
    Executors.newSingleThreadScheduledExecutor(r -> {
      Thread thread = new Thread(r);
      thread.setDaemon(true);
      return thread;
    }).scheduleAtFixedRate(this::updateConnectionLabel, 0L, 1000, TimeUnit.MILLISECONDS);
  }

  /**
   * Updates the connection label based on the current network mode of ntcore.
   */
  void updateConnectionLabel() {
    if (!Platform.isFxApplicationThread()) {
      Platform.runLater(this::updateConnectionLabel);
      return;
    }
    if (NetworkTableUtilities.isRunning()) {
      if (NetworkTableUtilities.isServer()) {
        if (NetworkTableUtilities.failed()) {
          serverFail();
        } else if (NetworkTableUtilities.starting()) {
          serverStarting();
        } else { // success
          serverSuccess();
        }
      } else if (NetworkTableUtilities.isClient()) {
        if (NetworkTableUtilities.failed()) {
          clientFail();
        } else if (NetworkTableUtilities.starting()) {
          clientStarting();
        } else { // success
          clientSuccess();
        }
      } else {
        // Running, but not in server or client mode
        generalFailure();
      }
    } else {
      // Not running anything
      generalFailure();
    }
  }

  private void clientStarting() {
    connectionLabel.setText("Connecting to " + Preferences.getIp() + "...");
    root.pseudoClassStateChanged(CLIENT, true);
    root.pseudoClassStateChanged(SERVER, false);
    root.pseudoClassStateChanged(FAILED, false);
  }

  private void clientFail() {
    StringBuffer text = new StringBuffer("No connection");
    String addr = Preferences.getIp();
    if (addr != null) {
      text.append(" to ").append(addr);
    }
    connectionLabel.setText(text.toString());
    root.pseudoClassStateChanged(CLIENT, true);
    root.pseudoClassStateChanged(SERVER, false);
    root.pseudoClassStateChanged(FAILED, true);
  }

  private void clientSuccess() {
    connectionLabel.setText("Connected to server at " + Preferences.getIp());
    root.pseudoClassStateChanged(CLIENT, true);
    root.pseudoClassStateChanged(SERVER, false);
    root.pseudoClassStateChanged(FAILED, false);
  }

  private void serverStarting() {
    connectionLabel.setText("Starting server...");
    root.pseudoClassStateChanged(CLIENT, false);
    root.pseudoClassStateChanged(SERVER, true);
    root.pseudoClassStateChanged(FAILED, false);
  }

  private void serverFail() {
    connectionLabel.setText("Could not run server");
    root.pseudoClassStateChanged(CLIENT, false);
    root.pseudoClassStateChanged(SERVER, true);
    root.pseudoClassStateChanged(FAILED, true);
  }

  private void serverSuccess() {
    StringBuffer text = new StringBuffer("Running server");
    int numClients = NetworkTableUtilities.getNetworkTableInstance().getConnections().length;
    switch (numClients) {
      case 0:
        text.append(" (No clients)");
        break;
      case 1:
        text.append(" (1 client)");
        break;
      default:
        text.append(" (").append(numClients).append(" clients)");
        break;
    }
    connectionLabel.setText(text.toString());
    root.pseudoClassStateChanged(CLIENT, false);
    root.pseudoClassStateChanged(SERVER, true);
    root.pseudoClassStateChanged(FAILED, false);
  }

  private void generalFailure() {
    connectionLabel.setText("Something went terribly wrong");
    root.pseudoClassStateChanged(CLIENT, false);
    root.pseudoClassStateChanged(SERVER, false);
    root.pseudoClassStateChanged(FAILED, true);
  }

}
