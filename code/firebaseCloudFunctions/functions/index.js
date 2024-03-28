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

exports.removeUserFromTopic = functions.firestore
    .document("events/{eventId}")
    .onUpdate(async (change, context) => {
          const beforeData = change.before.data();
          const afterData = change.after.data();
          const eventId = change.after.data().eventId;

          // Check if the attendees array has been changed, in the case that an admin deletes an attendee
          //    from an event
          if (beforeData.attendees.length > afterData.attendees.length) {
              // Determine which attendee was removed
              const removedAttendee = beforeData.attendees.find(attendee => !afterData.attendees.includes(attendee));

              const fcmTokensRef = admin.firestore().collection('fcmTokens');
              const query = fcmTokensRef.doc(removedAttendee);
              const snapshot = await query.get();

              const attendeesRef = admin.firestore().collection('attendees');
              const attendeeQuery = attendeesRef.doc(removedAttendee);
              const snap = await attendeeQuery.get();

              if (snap.exists) {
              // get the registration token and unsubscribe the user from the event's topic so that they
              //    no longer receive notifications from the event that they were removed from
                  const data = snap.data();
                  const uUid = data.uUid;

                  // Query the fcmTokens collection to find the document with the same uUid as removedAttendee
                  const fcmTokensRef = admin.firestore().collection('fcmTokens');
                  const query = fcmTokensRef.doc(uUid);
                  const snapshot = await query.get();
                  if (snapshot.exists) {
                        const data = snapshot.data();
                        const token = snapshot.registrationToken;
                        try {
                              // Unsubscribe token from topic
                              await admin.messaging().unsubscribeFromTopic(token, eventId);
                              console.log(`Token ${token} unsubscribed from topic.`);
                          } catch (error) {
                              console.error(`Error unsubscribing token ${token}:`, error);
                          }
                  }

              }
          }
    });
