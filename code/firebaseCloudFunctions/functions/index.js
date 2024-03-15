const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

exports.androidPushNotification = functions.firestore.document("alerts/{docId}").onCreate(
    (snapshot, context) => {
        admin.messaging().sendToTopic(
            snapshot.data().eventId,
            {
                notification: {
                    title: snapshot.data().title,
                    body: snapshot.data().body
                }
            }
        );
    }
);
