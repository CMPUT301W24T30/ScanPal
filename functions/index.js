const functions = require("firebase-functions");
const admin = require("firebase-admin");

/* eslint-disable */

const { initializeApp } = require('firebase-admin/app');

var serviceAccount = require("./scanpal-15383-firebase-adminsdk-2c6i7-2f88122c18.json");


initializeApp({

  credential: admin.credential.cert(serviceAccount)

});





const sendNotification = async (deviceToken, message) => {
   /* const payload = {
        notification: {
            title: "New Announcement",
            body: "test message",
        },
    };*/

    const payload1 = {
        token: deviceToken,
        notification: {
            title: 'cloud function demo',
            body: "message"
        },
        data: {
            body: "message",
        }
    };

    try {
        //await admin.messaging().sendToDevice(deviceToken, payload);
        await admin.messaging().send(payload1);
        console.log("\nNotification sent successfully to:", deviceToken);
    } catch (error) {
        console.error("\nError sending notification:\n", error);
    }
};

exports.sendNotificationOnAnnouncement = functions.firestore
    .document('Announcements/{announcementId}')
    .onCreate(async (snapshot, context) => {
        const announcementData = snapshot.data();
        const { eventID, message } = announcementData;

        /*const attendeesSnapshot = await admin.firestore()
            .collection('Attendees')
            .where('eventID', '==', eventID)
            .get();*/

            sendNotification("fYFuphSCTvKKiACyxdvV4G:APA91bGDA5rh5feCSIlVRzA4ncSXW5rgbhxXszZSGBafxNaaR3uvXD3q9aJL28tpBA9Kg6FowviBDlry9N6gWNwxlGvFaWa_CNfJrvUlC6dZqyn75OGZbeJDvCM0dvUs0GKyiGg1d1N4", message);

        /*attendeesSnapshot.forEach((attendeeDoc) => {
            const attendeeData = attendeeDoc.data();
            const { deviceToken } = attendeeData;
        });*/
    });


    /*
exports.sendNotif = onDocumentCreated("/Announcements/{announcementId}", (event) => {
  const announcementData = event.data.data();
  const announcementId = event.params.announcementId;

  if (announcementData) {
    const {eventID, message} = announcementData;

    const db = getFirestore();
    const eventRef = db.collection("events").doc(eventID);
    const attendeesRef = db.collection("Attendees").where("eventID", "==", eventRef);

    attendeesRef.get()
        .then((querySnapshot) => {
          querySnapshot.forEach((doc) => {
            const attendeeData = doc.data();
            const userRef = attendeeData.user; // Assuming the user reference is stored in the attendee document

            // Fetch the user document
            userRef.get()
                .then((userDoc) => {
                  if (userDoc.exists) {
                    const userData = userDoc.data();
                    const deviceToken = userData.deviceToken; // Assuming the device token is stored in the user document

                    // Send notification
                    const payload = {
                      notification: {
                        title: "New Announcement",
                        body: message,
                      },
                    };

                    messaging().send({
                      token: deviceToken,
                      data: payload,
                    })
                        .then((response) => {
                          logger.log("Notification sent successfully to:", deviceToken);
                        })
                        .catch((error) => {
                          logger.error("Error sending notification:", error);
                        });
                  } else {
                    logger.error("User document does not exist:", userRef.id);
                  }
                })
                .catch((error) => {
                  logger.error("Error fetching user document:", error);
                });
          });
        })
        .catch((error) => {
          logger.error("Error querying attendees collection:", error);
        });
  } else {
    logger.error("Failed to get announcement data for:", announcementId);

    const payload = {
      notification: {
        title: "New Announcement",
        body: "test body",
      },
    };

    messaging().send({
      token: "fYFuphSCTvKKiACyxdvV4G:APA91bGDA5rh5feCSIlVRzA4ncSXW5rgbhxXszZSGBafxNaaR3uvXD3q9aJL28tpBA9Kg6FowviBDlry9N6gWNwxlGvFaWa_CNfJrvUlC6dZqyn75OGZbeJDvCM0dvUs0GKyiGg1d1N4",
      data: payload,
    })
        .then((response) => {
          logger.log("Notification sent successfully to:", deviceToken);
        })
        .catch((error) => {
          logger.error("Error sending notification:", error);
        });
  }
});
*/
/* eslint-enable */
