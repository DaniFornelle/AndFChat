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

import android.content.Context;

import com.andfchat.R;
import com.andfchat.core.data.Chatroom;
import com.andfchat.core.data.FCharacter;
import com.google.inject.Inject;

public class Ignore extends TextCommand {

    public Ignore() {
        allowedIn = Chatroom.ChatroomType.values();
    }

    @Inject
    protected Context context;

    @Override
    public String getDescription() {
        return "*  /ignore [name] " + context.getString(R.string.command_description_ignore);
    }

    @Override
    public boolean fitToCommand(String token) {
        return token.equals("/ignore");
    }

    @Override
    public void runCommand(String token, String text) {
        if (text != null) {
            FCharacter flistChar = characterManager.findCharacter(text.trim(), false);
            if (flistChar != null){
                connection.ignore(flistChar.getName());
            }
        }
    }
}
