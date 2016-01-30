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


package com.andfchat.core.connection.handler;

import com.andfchat.core.connection.FeedbackListener;
import com.andfchat.core.connection.ServerToken;
import com.andfchat.core.data.Chatroom;
import com.andfchat.core.data.messages.ChatEntry;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import roboguice.util.Ln;

/**
 * Handles notifications for timeouts in official and unofficial channels.
 * @author AndFChat-Pandora
 */
public class TimeoutHandler extends TokenHandler {

    @Override
    public void incomingMessage(ServerToken token, String msg, List<FeedbackListener> feedbackListener) throws JSONException {
        if (token == ServerToken.CTU) {
            JSONObject json = new JSONObject(msg);
            String channel = json.getString("channel");
            String character = json.getString("character");
            String operator = json.getString("operator");
            String length = json.getString("length");

            Chatroom chatroom = chatroomManager.getChatroom(channel);

            if (chatroom != null) {
                if (character.equals(sessionData.getCharacterName())) {
                    ChatEntry entry = entryFactory.getNotation(characterManager.findCharacter(operator), " has banned you from " + chatroom.getName() + " for " + length + "minutes.");
                    this.addChatEntryToActiveChat(entry);
                } else if (chatroom.isChannelMod(characterManager.findCharacter(sessionData.getCharacterName()))) {
                    ChatEntry entry = entryFactory.getNotation(characterManager.findCharacter(character), " has been banned from " + chatroom.getName() + " for " + length + "minutes.");
                    this.addChatEntryToActiveChat(entry);
                }
            }
            else {
                Ln.e("Timeout is for a unknown channel: " + channel);
            }
        }
    }

    @Override
    public ServerToken[] getAcceptableTokens() {
        return new ServerToken[] {ServerToken.CTU};
    }
}
