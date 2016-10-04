package com.locus.game.network;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.Timer;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.locus.game.ProjectLocus;
import com.locus.game.screens.LobbyScreen;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Divya Mamgai on 10/3/2016.
 * Game Client
 */

public class GameClient {

    private Client client;
    private ProjectLocus projectLocus;
    private LobbyScreen lobbyScreen;
    private int connectionID;
    private String hostAddress;
    private HashMap<Integer, Player> playerMap;
    private Timer timer;

    private class GameClientConnectRunnable implements Runnable {

        private LobbyScreen lobbyJoinScreen;

        GameClientConnectRunnable(LobbyScreen lobbyScreen) {
            this.lobbyJoinScreen = lobbyScreen;
        }

        @Override
        public void run() {
            List<InetAddress> addressList = client.discoverHosts(Network.SERVER_UDP_PORT,
                    Network.CONNECTION_TIMEOUT);
            Gdx.app.log("Lobby Client", addressList.toString());
            lobbyJoinScreen.state = LobbyScreen.State.Connecting;
            for (InetAddress address : addressList) {
                try {
                    // We only need IPv4 addresses.
                    if (address instanceof Inet4Address) {
                        client.connect(Network.CONNECTION_TIMEOUT,
                                address.getHostAddress(), Network.SERVER_TCP_PORT,
                                Network.SERVER_UDP_PORT);
                        Gdx.app.log("Lobby Client", "Connected To Host @ " +
                                address.getHostAddress() + ":" + Network.SERVER_TCP_PORT);
                        // For now break at the first successfully connected server.
                        lobbyJoinScreen.state = LobbyScreen.State.Connected;
                    }
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // Check if the connecting failed with everyone.
            if (lobbyJoinScreen.state == LobbyScreen.State.Connecting) {
                lobbyJoinScreen.state = LobbyScreen.State.Failed;
            }
        }
    }

    public GameClient(ProjectLocus projectLocus) {

        this.projectLocus = projectLocus;

        client = new Client();
        Network.registerClasses(client);

        timer = new Timer();

        playerMap = new HashMap<Integer, Player>();

    }

    public void start(LobbyScreen lobbyScreen) {

        this.lobbyScreen = lobbyScreen;

        client.addListener(new ClientListener(this));
        client.start();

        // Launch a dirty |:P} Thread to retrieve list of the IP Addresses.
        new Thread(new GameClientConnectRunnable(lobbyScreen)).start();

    }

    void onConnected(Connection connection) {
        connectionID = connection.getID();
        hostAddress = connection.getRemoteAddressTCP().toString();
        client.sendTCP(new Network.PlayerJoinRequest(projectLocus.playerShipProperty));
        client.updateReturnTripTime();
    }

    void onReceived(Connection connection, Object object) {
        if (object instanceof Network.UpdateLobby) {
            Network.UpdateLobby updateLobby = (Network.UpdateLobby) object;
            lobbyScreen.playerMap = playerMap = updateLobby.playerMap;
            lobbyScreen.isLobbyToBeUpdated = true;
            Gdx.app.log("Client", "Accepted Player Count : " + String.valueOf(playerMap.size()));
        } else if (object instanceof Network.PlayerJoinRequestRejected) {
            Network.PlayerJoinRequestRejected playerJoinRequestRejected =
                    (Network.PlayerJoinRequestRejected) object;
            Gdx.app.log("Client", playerJoinRequestRejected.reason);
        } else if (object instanceof Network.LevelProperty) {
            lobbyScreen.levelProperty = ((Network.LevelProperty) object).levelProperty;
            lobbyScreen.initializePlayScreen = true;
            ready();
            Gdx.app.log("Client", "Received Level Property");
        } else if (object instanceof Network.StartGame) {
            Network.StartGame startGame = (Network.StartGame) object;
//            float timeout = startGame.timeout -
//                    ((float) (TimeUtils.millis() - startGame.serverStartTime) / 1000f);
//            Gdx.app.log("Client", "Received Start Game, Starting Game In " + timeout);
//            timer.scheduleTask(new Timer.Task() {
//                @Override
//                public void run() {
//                    Gdx.app.log("Client", "Switching...");
//                    projectLocus.setScreen(lobbyScreen.multiPlayerPlayScreen);
//                }
//            }, 10f);
        }
    }

    void onDisconnected(Connection connection) {

    }

    private void ready() {
        client.sendTCP(new Network.PlayerReadyRequest(true));
        client.updateReturnTripTime();
    }

    public void stop() {
        client.close();
        client.stop();
    }

}