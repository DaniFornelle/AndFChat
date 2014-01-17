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


package com.homebrewn.flistchat.core.connection.handler;

import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.homebrewn.flistchat.core.connection.FeedbackListner;
import com.homebrewn.flistchat.core.connection.ServerToken;
import com.homebrewn.flistchat.core.data.CharacterHandler;
import com.homebrewn.flistchat.core.data.ChatEntry;
import com.homebrewn.flistchat.core.data.ChatEntry.ChatEntryType;
import com.homebrewn.flistchat.core.data.Chatroom;
import com.homebrewn.flistchat.core.data.SessionData;

/**
 * Handles incoming messages for channel and Broadcasts
 * @author AndFChat
 *
 */
public class MessageHandler extends TokenHandler {

    public MessageHandler(SessionData sessionData) {
        super(sessionData);
    }

    @Override
    public void incomingMessage(ServerToken token, String msg, List<FeedbackListner> feedbackListner) {
        try {
            JSONObject jsonObject = new JSONObject(msg);
            if(token == ServerToken.MSG) {
                String character = jsonObject.getString("character");
                String message = jsonObject.getString("message");
                String channel = jsonObject.getString("channel");

                Chatroom log = ChatroomHandler.getChatroom(channel);
                log.addMessage(message, sessionData.getCharHandler().findCharacter(character), new Date());

                log.setHasNewMessage(true);
            }
            else if(token == ServerToken.BRO) {
                String message = jsonObject.getString("message");
                ChatroomHandler.addBroadcast(new ChatEntry(message, sessionData.getCharHandler().findCharacter(CharacterHandler.USER_SYSTEM), new Date(), ChatEntryType.NOTATION_SYSTEM));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public ServerToken[] getAcceptableTokens() {
        return new ServerToken[]{ServerToken.MSG, ServerToken.BRO};
    }

}
