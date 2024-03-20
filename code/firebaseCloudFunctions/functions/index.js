const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

exports.androidPushNotification = functions.firestore.document("alerts/{docId}").onCreate(
    (snapshot, context) => {
        // get data to create a notification payload
        const data = snapshot.data();
        const eventId = data.eventId;
        const title = data.title;
        const body = data.body;
        const organizerId = data.organizerId;
        const channelId = data.channelId;  // the notification channel we want to send the notification through

        const notificationPayload = {
            notification: {
                title: title,
                body: body,
            },
            data: {
                organizerId: organizerId,
            },
            android: {
                notification: {
                    channelId: channelId
                }
            },
            topic: eventId
        };

        admin.messaging().send(notificationPayload);
    }
);
