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


package com.homebrewn.flistchat.core.connection;

import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.util.Log;

public class FlistHttpClient {

    private final static String TAG = FlistHttpClient.class.getSimpleName();

    private final static String logInUri = "https://www.f-list.net/json/getApiTicket.php?account={1}&password={2}";
    private final static String addBookmarkUri = "https://www.f-list.net/json/api/bookmark-add.php?account={1}&ticket={2}&name={3}";
    private final static String removeBookmarkUri = "https://www.f-list.net/json/api/bookmark-remove.php?account={1}&ticket={2}&name={3}";


    public void logIn(String account, String password, FeedbackListner feedbackListner) {
        this.sendMessage(feedbackListner, logInUri, account, password);
    }

    public void addBookmark(String account, String ticket, String name, FeedbackListner feedbackListner) {
        this.sendMessage(feedbackListner, addBookmarkUri, account, ticket, name);
    }

    public void removeBookmark(String account, String ticket, String name, FeedbackListner feedbackListner) {
        this.sendMessage(feedbackListner, removeBookmarkUri, account, ticket, name);
    }

    private void sendMessage(FeedbackListner feedbackListner, String url, String... arguments) {
        for (int i = 1; i < arguments.length + 1; i++) {
            url = url.replace("{" + i + "}", arguments[i - 1]);
        }

        Log.d(TAG, "Open url: " + url);
        HttpWebCall webCall = new HttpWebCall(url, feedbackListner);
        webCall.execute();
    }

    private static String convertStreamToString(InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private class HttpWebCall extends AsyncTask<Void, Void, Void> {

        private final String uri;
        private final FeedbackListner feedbackListner;

        public HttpWebCall(String uri, FeedbackListner feedbackListner) {
            this.uri = uri;
            this.feedbackListner = feedbackListner;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                DefaultHttpClient client = new DefaultHttpClient();
                HttpPost request = new HttpPost(uri);

                HttpResponse response = client.execute(request);
                String responseString = convertStreamToString(response.getEntity().getContent());
                Log.d(TAG, "Got Response: " + responseString);
                feedbackListner.onResponse(responseString);

                return null;
            } catch (Exception ex) {
                ex.printStackTrace();
                feedbackListner.onError(ex);
            }
            return null;
        }
    }
}
