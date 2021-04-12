package com.koven.iris.util;

public interface Constants {
    /**
     * Shared preference file name to store user specific values
     */
    String SHARED_PREFERENCE_FILE_NAME = "iris";

    /**
     * User info string name for shared preference to store user login info
     */
    String USER_INFO = "user";

    /**
     * User info
     */
    String USERNAME = "username";
    String USER_ID = "userId";
    String PIC_URL = "picUrl";

    /**
     * Fire store references
     */
    String USERS_COLLECTION = "users";
    String CHATS_COLLECTION = "chat_logs";

    /**
     * Message fields
     */
    String MESSAGE_ID = "messageId";
    String MESSAGE_TEXT = "messageText";
    String MESSAGE_SENDER_ID = "senderId";
    String MESSAGE_RECEIVER_ID = "receiverId";
}
