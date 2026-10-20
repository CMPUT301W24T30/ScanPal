const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");
const vm = require("node:vm");

const loadFunctions = (firestore, send) => {
  const exported = {};
  const sandbox = {
    exports: exported,
    console: {log() {}, error() {}},
    require: (id) => {
      if (id === "firebase-admin") {
        return {
          credential: {cert: () => ({})},
          firestore,
          messaging: () => ({send}),
        };
      }
      if (id === "firebase-admin/app") {
        return {initializeApp() {}};
      }
      if (id.includes("firebase-adminsdk")) return {};
      return require(id);
    },
  };
  const source = fs.readFileSync(path.join(__dirname, "index.js"), "utf8");
  vm.runInNewContext(source, sandbox, {filename: "index.js"});
  return exported;
};

test("announcement uses FCM send with its event topic and awaits delivery",
    async () => {
      let release;
      let sent;
      const pending = new Promise((resolve) => {
        release = resolve;
      });
      const firestore = () => ({
        collection: () => ({
          doc: () => ({
            get: async () => ({
              exists: true,
              data: () => ({name: "Example event"}),
            }),
          }),
        }),
      });
      const functions = loadFunctions(firestore, (message) => {
        sent = message;
        return pending;
      });
      const snapshot = {
        data: () => ({eventID: "event-123", message: "Doors open"}),
      };
      let completed = false;
      const result = functions.sendNotificationOnAnnouncement.run(snapshot, {});
      result.then(() => {
        completed = true;
      });
      await new Promise((resolve) => setImmediate(resolve));
      assert.equal(sent.topic, "event-123");
      assert.equal(sent.notification.title,
          "Announcement From Event: Example event");
      assert.equal(sent.notification.body, "Doors open");
      assert.equal(completed, false);
      release("message-id");
      await result;
      assert.equal(completed, true);
    });

test("milestone uses FCM send with the organizer topic", async () => {
  const sent = [];
  const updates = [];
  const eventRef = {
    id: "event-123",
    path: "Events/event-123",
    get: async () => ({
      exists: true,
      data: () => ({
        name: "Example event",
        totalCheckInCount: 4,
        organizer: {
          id: "organizer",
          get: async () => ({exists: true, data: () => ({})}),
        },
      }),
    }),
    update: async (values) => updates.push(values.totalCheckInCount),
  };
  const functions = loadFunctions(() => ({}), async (message) => {
    sent.push(message);
  });
  const change = {
    before: {data: () => ({checkedIn: false})},
    after: {data: () => ({checkedIn: true, eventID: eventRef})},
  };
  await functions.sendMilestoneAlert.run(change, {});
  assert.deepEqual(updates, [5]);
  assert.equal(sent.length, 1);
  assert.equal(sent[0].topic, "event-123org");
  assert.equal(sent[0].notification.title, "Event MileStone: Example event");
  assert.equal(sent[0].notification.body,
      "5 Attendees have checked-in to your Event!");
  await functions.sendMilestoneAlert.run({
    before: {data: () => ({checkedIn: true})},
    after: {data: () => ({checkedIn: true, eventID: eventRef})},
  }, {});
  assert.deepEqual(updates, [5]);
  assert.equal(sent.length, 1);
});

test("installed SDKs expose the APIs used by the functions", async () => {
  const {initializeApp, deleteApp} = require("firebase-admin/app");
  const {getMessaging} = require("firebase-admin/messaging");
  const {getFirestore} = require("firebase-admin/firestore");
  const functions = require("firebase-functions/v1");
  const app = initializeApp({projectId: "offline-security-validation"},
      "security-validation");
  assert.equal(typeof getMessaging(app).send, "function");
  assert.equal(typeof getFirestore(app).collection, "function");
  assert.equal(typeof functions.firestore.document, "function");
  await deleteApp(app);
});
