
package fi.derpnet.derpbot.connector;

import fi.derpnet.derpbot.bean.MatrixMessage;
import fi.derpnet.derpbot.bean.Message;
import fi.derpnet.derpbot.controller.MainController;
import io.github.ma1uta.matrix.client.StandaloneClient;
import io.github.ma1uta.matrix.client.model.sync.InvitedRoom;
import io.github.ma1uta.matrix.client.model.sync.JoinedRoom;
import io.github.ma1uta.matrix.client.model.sync.SyncResponse;
import io.github.ma1uta.matrix.client.rest.blocked.ServerDiscoveryApi;
import io.github.ma1uta.matrix.client.sync.SyncLoop;
import io.github.ma1uta.matrix.client.sync.SyncParams;
import io.github.ma1uta.matrix.event.Event;
import io.github.ma1uta.matrix.event.RoomMember;
import io.github.ma1uta.matrix.event.RoomMessage;
import io.github.ma1uta.matrix.event.content.EventContent;
import io.github.ma1uta.matrix.event.content.RoomMessageContent;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import org.apache.log4j.Logger;

public class MatrixConnector implements Connector {
    
    private final String hostname;
    private final String loginname;
    private final String password;
    private final String username;
    private StandaloneClient matrixClient;
    private List<String> channels;
    private List<String> quieterChannels;
    private SyncLoop syncLoop;
    private final Function<Message, List<Message>> messageFunction;
    private ExecutorService service;
    private boolean disconnect = false;
    private boolean firstSync = true;
    private static final Logger LOG = Logger.getLogger(MatrixConnector.class);
    
    public MatrixConnector(String hostname, String loginname, String password, String username, MainController controller){
        this.hostname = hostname;
        this.loginname = loginname;
        this.password = password;
        this.username = username;
        this.messageFunction = msg -> controller.handleIncoming(this, msg);
    }

    @Override
    public void connect() throws IOException {
        matrixClient = new StandaloneClient.Builder().domain(hostname).build();
        matrixClient.getConnectionInfo().setAccessToken(password);
                
        matrixClient.auth().login(username, password.toCharArray());
        matrixClient.profile().setDisplayName(username);
        channels = matrixClient.room().joinedRooms().getJoinedRooms();
        
        SyncResponse initialSyncResponse = matrixClient.sync().sync(null, null, false, null, 30000L);
        
        syncLoop = new SyncLoop(matrixClient.sync(), (syncResponse, syncParams) -> {
            // Handle new messages
            for (Entry<String, JoinedRoom> room : syncResponse.getRooms().getJoin().entrySet()){
                for (Event event : room.getValue().getTimeline().getEvents()){
                    if (event instanceof RoomMessage) {
                        EventContent eventContent = ((RoomMessage)event).getContent();
                        if (eventContent instanceof RoomMessageContent && !matrixClient.getUserId().equals(((RoomMessage)event).getSender())){
                            MatrixMessage msg = new MatrixMessage(((RoomMessageContent)eventContent).getBody(), room.getKey());
                            for (Message response : messageFunction.apply(msg)){
                                if (response instanceof MatrixMessage) {
                                    matrixClient.event().sendFormattedMessage(room.getKey(), response.toString(), response.toString());
                                }
                                else {
                                    matrixClient.event().sendMessage(room.getKey(), response.toString());
                                }
                            }
                        }
                    }
                }
            }
            // Handle invites
            for (Entry<String, InvitedRoom> room : syncResponse.getRooms().getInvite().entrySet()){
                for (Event event : room.getValue().getInviteState().getEvents()){
                    if (event instanceof RoomMember){
                        if ("invite".equals(((RoomMember)event).getContent().getMembership())){
                            if (!matrixClient.room().joinedRooms().getJoinedRooms().contains(room.getKey())){
                                 matrixClient.room().joinById(room.getKey(), null);
                            }
                        }
                    }
                }
            }
            syncParams.setFullState(false);
            if (disconnect){
                syncParams.setTerminate(true);
            }
        });
        SyncParams params = SyncParams.builder()
                .nextBatch(initialSyncResponse.getNextBatch())
                .fullState(false)
                .timeout(30000L)
                .build();
        syncLoop.setInit(params);
        
        service = Executors.newFixedThreadPool(1);
        service.submit(syncLoop);
    }

    @Override
    public void disconnect() {
        disconnect = true;
        service.shutdown();
    }

    @Override
    public void send(Message msg) {
        if (msg instanceof MatrixMessage){
            matrixClient.event().sendMessage(((MatrixMessage) msg).getRoomId(), msg.toString());
        }
    }

    @Override
    public void setChannels(List<String> channels, boolean join) {
        for (String channel : channels) {
            if (!matrixClient.room().joinedRooms().getJoinedRooms().contains(channel)){
                matrixClient.room().joinByIdOrAlias(channel, null, null).getRoomId();
            }
        }
    }

    @Override
    public List<String> getQuieterChannels() {
        return quieterChannels;
    }

    @Override
    public void setQuieterChannels(List<String> quieterChannels) {
        for (String channel : channels) {
            if (!matrixClient.room().joinedRooms().getJoinedRooms().contains(channel)){
                matrixClient.room().joinByIdOrAlias(channel, null, null).getRoomId();
            }
        }
    }
    
    public String getHostname() {
        return hostname;
    }
    
}
