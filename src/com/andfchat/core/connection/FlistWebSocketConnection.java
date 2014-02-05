/*******************************************************************************
 *     This file is part of AndFChat.
 *
 *     AndFChat is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     AndFChat is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with AndFChat.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/


package com.andfchat.core.connection;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import roboguice.util.Ln;

import com.andfchat.core.connection.handler.PrivateMessageHandler;
import com.andfchat.core.data.AppProperties;
import com.andfchat.core.data.CharStatus;
import com.andfchat.core.data.CharacterManager;
import com.andfchat.core.data.ChatEntry;
import com.andfchat.core.data.Chatroom;
import com.andfchat.core.data.ChatroomManager;
import com.andfchat.core.data.FlistChar;
import com.andfchat.core.data.SessionData;
import com.andfchat.core.data.ChatEntry.ChatEntryType;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;

@Singleton
public class FlistWebSocketConnection {

    private final static String CLIENT_NAME = "AndFChat";
    private final static String CLIENT_VERSION = "alpha01";
    private final static String SERVER_URL = "ws://chat.f-list.net:8722/";

    @Inject
    private FlistWebSocketHandler handler;
    @Inject
    private SessionData sessionData;
    @Inject
    private ChatroomManager chatroomManager;
    @Inject
    private CharacterManager characterManager;

    private final WebSocketConnection connection = new WebSocketConnection();

    public void connect() {
        try {
            connection.connect(SERVER_URL, handler);
        } catch (WebSocketException e) {
            e.printStackTrace();
            Ln.e("Exception while connecting");
        }
    }

    public void sendMessage(ClientToken token) {
        sendMessage(token, null);
    }

    public boolean isConnected() {
        return connection.isConnected();
    }

    public void sendMessage(ClientToken token, JSONObject data) {
        if (data == null) {
            Ln.i("Sending token: " + token.name());
            connection.sendTextMessage(token.name());

            if (sessionData.getSessionSettings().useDebugChannel()) {
                FlistChar systemChar = characterManager.findCharacter(CharacterManager.USER_SYSTEM_OUTPUT);
                chatroomManager.getChatroom(AppProperties.DEBUG_CHANNEL_NAME).addMessage(new ChatEntry(token.name(), systemChar, new Date(), ChatEntryType.MESSAGE));
            }
        } else {
            Ln.i("Sending message: " + token.name() + " " + data.toString());
            connection.sendTextMessage(token.name() + " " + data.toString());

            if (sessionData.getSessionSettings().useDebugChannel()) {
                FlistChar systemChar = characterManager.findCharacter(CharacterManager.USER_SYSTEM_OUTPUT);
                chatroomManager.getChatroom(AppProperties.DEBUG_CHANNEL_NAME).addMessage(new ChatEntry(token.name() + " " + data.toString(), systemChar, new Date(), ChatEntryType.MESSAGE));
            }
        }
    }

    public void registerFeedbackListner(ServerToken serverToken, FeedbackListner feedbackListner) {
        handler.addFeedbackListner(serverToken, feedbackListner);
    }

    /**
     * Identify character with the server
     */
    public void identify() {
        JSONObject data = new JSONObject();
        try {
            data.put("ticket", sessionData.getTicket());
            data.put("method", "ticket");
            data.put("cname", CLIENT_NAME);
            data.put("cversion", CLIENT_VERSION);
            data.put("account", sessionData.getAccount());
            data.put("character", sessionData.getCharacterName());
            sendMessage(ClientToken.IDN, data);
        } catch (JSONException e) {
            Ln.w("exception occured while identifying: " + e.getMessage());
        }
    }

    /**
     * Asks the server for permission to enter a channel.
     * @param channel
     */
    public void joinChannel(String channel) {
        JSONObject data = new JSONObject();
        try {
            data.put("channel", channel);
            sendMessage(ClientToken.JCH, data);
        } catch (JSONException e) {
            Ln.w("exception occured while joining channle: " + e.getMessage());
        }
    }

    /**
     * Asks the server for all public private channel.
     * @param channel
     */
    public void askForPrivateChannel() {
        sendMessage(ClientToken.ORS);
    }

    /**
     * Asks the server to leave an channel
     * @param channel
     */
    public void leaveChannel(Chatroom chatroom) {
        // TODO: Leave private chat without deleting "log"
        if (!chatroom.isPrivateChat()) {
            JSONObject data = new JSONObject();
            try {
                data.put("channel", chatroom.getId());
                sendMessage(ClientToken.LCH, data);
            } catch (JSONException e) {
                Ln.w("exception occured while leaving channle: " + e.getMessage());
            }
        } else {
            // Private chats will just be removed.
            chatroomManager.removeChatroom(chatroom.getId());
        }
    }

    /**
     * Sends a message to an channel.
     * @param channel
     * @param msg
     */
    public void sendMessageToChannel(Chatroom chatroom, String msg) {
        JSONObject data = new JSONObject();
        try {
            data.put("channel", chatroom.getId());
            data.put("character", sessionData.getCharacterName());
            data.put("message", msg);
            sendMessage(ClientToken.MSG, data);

            chatroom.addMessage(msg, characterManager.findCharacter(sessionData.getCharacterName()), new Date());

        } catch (JSONException e) {
            Ln.w("exception occured while sending message: " + e.getMessage());
        }
    }

    /**
     * Sends a private message.
     * @param recipient
     * @param msg
     */
    public void sendPrivatMessage(String recipient, String msg) {
        JSONObject data = new JSONObject();
        try {
            data.put("recipient", recipient);
            data.put("message", msg);
            sendMessage(ClientToken.PRI, data);

            String channelname = PrivateMessageHandler.PRIVATE_MESSAGE_TOKEN + recipient;
            Chatroom log = chatroomManager.getChatroom(channelname);
            log.addMessage(msg, characterManager.findCharacter(sessionData.getCharacterName()), new Date());

        } catch (JSONException e) {
            Ln.w("exception occured while sending private message: " + e.getMessage());
        }
    }

    /**
     * Set a status for the character.
     * @param status
     * @param msg
     */
    public void setStatus(CharStatus status, String msg) {
        JSONObject data = new JSONObject();
        try {
            data.put("status", status.name());
            data.put("statusmsg", msg);
            data.put("character", sessionData.getCharacterName());
            sendMessage(ClientToken.STA, data);
        } catch (JSONException e) {
            Ln.w("exception occured while sending private message: " + e.getMessage());
        }
    }

    /**
     * Asks for character information
     * @param status
     * @param msg
     */
    public void askForInfos(FlistChar flistChar) {
        JSONObject data = new JSONObject();
        try {
            data.put("character", flistChar.getName());
            sendMessage(ClientToken.PRO, data);
        } catch (JSONException e) {
            Ln.w("exception occured while sending private message: " + e.getMessage());
        }
    }

    public void requestOfficialChannels() {
        sendMessage(ClientToken.CHA);
    }

    public void closeConnection() {
        connection.disconnect();
    }

    public void createPrivateChannel(String channelname) {
        JSONObject data = new JSONObject();
        try {
            data.put("channel", channelname);
            sendMessage(ClientToken.CCR, data);
        } catch (JSONException e) {
            Ln.w("exception occured while creating a private channel: " + e.getMessage());
        }
    }

    public void bottle(Chatroom activeChat) {
        JSONObject data = new JSONObject();
        try {
            data.put("channel", activeChat.getId());
            data.put("dice", "bottle");
            sendMessage(ClientToken.RLL, data);
        } catch (JSONException e) {
            Ln.w("exception occured while botteling: " + e.getMessage());
        }
    }

    public void dice(Chatroom activeChat, String value) {
        JSONObject data = new JSONObject();
        try {
            data.put("channel", activeChat.getId());

            if (value == null || value.length() == 0) {
                value = "1d10";
            }

            data.put("dice", value);
            sendMessage(ClientToken.RLL, data);
        } catch (JSONException e) {
            Ln.w("exception occured while botteling: " + e.getMessage());
        }
    }
}