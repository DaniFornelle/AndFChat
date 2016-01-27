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


package com.andfchat.core.util.commands;

import com.andfchat.core.data.Chatroom;
import com.andfchat.core.data.FCharacter;

public class Timeout extends TextCommand {

    public Timeout() {
        allowedIn = new Chatroom.ChatroomType[]{Chatroom.ChatroomType.PRIVATE_CHANNEL, Chatroom.ChatroomType.PUBLIC_CHANNEL};
    }

    @Override
    public String getDescription() {
        return "*  /timeout [user] | Temporarily bans a character from the room for 30 minutes.";
    }

    @Override
    public boolean fitToCommand(String token) {
        return token.equals("/timeout");
    }

    @Override
    public void runCommand(String token, String text) {
        if (text != null) {
            FCharacter flistChar = characterManager.findCharacter(text.trim(), false);
            if (flistChar != null){
                connection.timeout(flistChar.getName(), chatroomManager.getActiveChat());
            }
        }
    }
}