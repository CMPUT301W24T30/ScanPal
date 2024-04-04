const functions = require("firebase-functions");
const admin = require("firebase-admin");

/* eslint-disable */

const { initializeApp } = require('firebase-admin/app');

var serviceAccount = require("./scanpal-15383-firebase-adminsdk-2c6i7-2f88122c18.json");
const { QueryDocumentSnapshot } = require("firebase-admin/firestore");


initializeApp({

  credential: admin.credential.cert(serviceAccount)

});





const sendNotification = async (deviceToken, message, eventName) => {
   /* const payload = {
        notification: {
            title: "New Announcement",
            body: "test message",
        },
    };*/

    //message correctly contains the string

    const payload1 = {
        token: deviceToken,
        notification: {
            title: 'Announcement From Event: ' + eventName,
            body: message
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
            

        const eventSnapshot = await admin.firestore()
            .collection("Events")
            .doc(eventID)
            .get()

            if(eventSnapshot.exists) {
                const eventData = eventSnapshot.data();
                const eventName = eventData.name;

        const payload1 = {
                    //token: deviceToken,
                    notification: {
                        title: 'Announcement From Event: ' + eventName,
                        body: message
                    },
                };

                try {
                  admin.messaging().sendToTopic(eventID, payload1);
                  console.log("\n Announcement sent successfully Topic: " + eventID);

                } catch(error) {
                  console.error("\nError sending to topic:\n", error);

                }

                /*

                //eventually replace hardcoded devToken
                sendNotification("fYFuphSCTvKKiACyxdvV4G:APA91bGDA5rh5feCSIlVRzA4ncSXW5rgbhxXszZSGBafxNaaR3uvXD3q9aJL28tpBA9Kg6FowviBDlry9N6gWNwxlGvFaWa_CNfJrvUlC6dZqyn75OGZbeJDvCM0dvUs0GKyiGg1d1N4", message, eventName);
                */
            } else {
                console.log("Event with ID ${eventID} does not exist.");
            }


        /*attendeesSnapshot.forEach((attendeeDoc) => {
            const attendeeData = attendeeDoc.data();
            const { deviceToken } = attendeeData;
        });*/
    });



    /**
     * This Function will send notifications to the event organizer
     * at specific counts when attendees join
     */
    exports.sendMilestoneAlert = functions.firestore
    .document('Attendees/{attendeeId}')
    .onUpdate(async (change, context) => {

        const newValue = change.after.data();
        const previousValue = change.before.data();

        console.log("Attendee checked in: MILESTONE TRIGGER");


        // Check if 'checkedIn' field was changed to true
        if (newValue.checkedIn === true && previousValue.checkedIn !== true) {

            const eventRef = newValue.eventID; //ref to event doc

            console.log("Tied Event: ", eventRef.path );

            const eventSnapshot = await eventRef.get();

            if(eventSnapshot.exists) {
                const eventData = eventSnapshot.data();
                console.log("Event Title: ", eventData.name);
                console.log("Event ID:", eventRef.id);
                console.log("Event Check-Ins:", eventData.totalCheckInCount);

                const newCount = eventData.totalCheckInCount + 1;

                await eventRef.update({
                  totalCheckInCount: newCount
                });

                console.log("Event Check-In Count Updated:", newCount);


                const OrgRef = eventData.organizer;


                const orgSnapshot = await OrgRef.get();
                if(orgSnapshot.exists) {
                  const orgData = orgSnapshot.data();
                  console.log("Organizer Name: ", orgData.id);

                  //send notif to topic of userName
                  // topic actually should be eventID + 'org'

                  let message = "A user has checked-In";
                  let sendFlag = false;

                if(newCount == 1) {
                  message = "The First Attendee Has Checked In to your Event!";
                  sendFlag = true;
                }
                else if (newCount == 5) {
                  message =  "5 Attendees have checked-in to your Event!";
                  sendFlag = true;

                }
                else {
                  if(newCount%10 == 0) {
                    message = newCount + " Attendees have checked-in to your Event!";
                    sendFlag = true;

                  }
                  
              }

              if(sendFlag) {

                const payload1 = {
                  //token: deviceToken,
                  notification: {
                      title: 'Event MileStone: ' + eventData.name,
                      body: message
                  },
              };
                  
                try {
                  admin.messaging().sendToTopic( eventRef.id + "org", payload1);
                  console.log("\n Announcement sent successfully Topic: " + (eventRef.id + "org") );

                } catch(error) {
                  console.error("\nError sending to topic:\n", error);

                }
              }
              

            } else {
              console.log("Event document does not exist");
            }

            }
      }

        return null;

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
