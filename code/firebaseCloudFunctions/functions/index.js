const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

exports.androidEventAlert = functions.firestore.document("alerts/{docId}").onCreate(
    (snapshot, context) => {
        // get data to create a notification payload
        const data = snapshot.data();
        const eventId = data.eventId;
        const title = data.title;
        const body = data.body;
        const organizerId = data.organizerId;
        const channelId = data.channelId;  // the notification channel we want to send the notification through

        const notificationPayload = {
            topic: eventId,
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
            }
        };

        admin.messaging().send(notificationPayload);
    }
);

exports.androidMilestoneAlert = functions.firestore.document("events/{eventId}").onUpdate(
    async (change, context) => {
            const data = change.after.data();

            const attendeeCount = data.attendees.length;

            const milestones = [1, 5, 10, 25, 50, 100];
            const currentMilestones = data.milestones;

            // Check if attendee count reached a milestone
            if (milestones.includes(attendeeCount) && !currentMilestones.includes(attendeeCount)) {
                const eventName = data.name;
                const eventId = data.eventId;
                const organizerId = data.organizer;

                // Determine the syntax of the message
                let attendeeString = (attendeeCount === 1) ? "attendee" : "attendees";
                let body = `${eventName} has reached ${attendeeCount} ${attendeeString}.`;

                const notificationPayload = {
                            topic: eventId,
                            notification: {
                                title: "Milestone Reached!",
                                body: body,
                            },
                            data: {
                                organizerId: organizerId,
                            },
                            android: {
                                notification: {
                                    channelId: "milestone_channel"
                                }
                            }
                        };

                // Update milestones array in Firestore
                await admin.firestore().collection("events").doc(eventId).update({
                    milestones: admin.firestore.FieldValue.arrayUnion(attendeeCount)
                });

                admin.messaging().send(notificationPayload);
            }
    }
);

exports.androidEventDelete = functions.firestore
    .document("events/{docId}")
    .onDelete((snap, context) => {
        // Get the deleted document data
        const deletedData = snap.data();
        const eventId = deletedData.eventId;
        const eventName = deletedData.name;
        const organizerId = deletedData.organizer;
        let body = `${eventName} has been cancelled.`;

        const notificationPayload = {
            topic: eventId,
            notification: {
                title: "Event Cancelled!",
                body: body,
            },
            data: {
                organizerId: organizerId,
            },
            android: {
                notification: {
                    channelId: "event_channel"
                }
            }
        };

        admin.messaging().send(notificationPayload);
    });
