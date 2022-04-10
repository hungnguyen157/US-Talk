package com.example.ustalk.utilities;

import java.util.HashMap;

public class Constants {
    public static final String KEY_COLLECTION_USER = "users";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_PREFERENCE_NAME = "usTalkAppPreference";
    public static final String KEY_IS_SIGNED_IN = "isSignedIn";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_IMAGE = "image";

    public static final String KEY_MSG_AUTHORIZATION = "Authorization";
    public static final String KEY_MSG_CONTENT_TYPE = "Content-Type";

    public static final String KEY_MSG_TYPE = "type";
    public static final String KEY_MSG_INVITATION = "invitation";
    public static final String KEY_MSG_MEETING_TYPE = "meetingType";
    public static final String KEY_MSG_INVITER_TOKEN = "inviterToken";
    public static final String KEY_MSG_DATA = "data";
    public static final String KEY_MSG_REGISTRATION_IDS = "registration_ids";

    public static final String KEY_MSG_INVITATION_RESPONSE = "invitationResponse";

    public static final String KEY_MSG_INVITATION_ACCEPTED = "accepted";
    public static final String KEY_MSG_INVITATION_REJECTED = "rejected";
    public static final String KEY_MSG_INVITATION_CANCELED = "canceled";

    public static HashMap<String, String> getRemoteMessageHeaders(){
        HashMap<String, String> header = new HashMap<>();
        header.put(
                Constants.KEY_MSG_AUTHORIZATION,
                "key=AAAAfFAKHSg:APA91bFihyTKsgfLDvBAymYxZbZvsLb4Rax7iEx7imaxejEFcefc36Q9PSTSUa2KuzO_LOe12XBo09CmAZnGfVuK0SegeWcdVx0gahWyiq8MM3G_wd-lXAtqJEfpgUlKgYsNtDxWKqEb"
        );
        header.put(Constants.KEY_MSG_CONTENT_TYPE, "application/json");
        return header;
    }
}
