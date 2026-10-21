import axios from "axios";
import * as commentsService from "./comments.service";
import * as forumRoomsService from "./forumRooms.Service";
import * as userService from "./user.service";
import * as securityEventsService from "./securityEvents.service";

vi.mock("axios", async () => (await import("../testUtils")).mockAxios());

const http = axios.create();

// Every service call must hit the route the Spring controllers expose
describe("API endpoints", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    ["get", "post", "put", "delete"].forEach((m) => http[m].mockResolvedValue({ data: [] }));
  });

  describe("comments", () => {
    test.each([
      ["getComments", [3], "get", "/comments/3"],
      ["getRequestedComments", [3], "get", "/comments/requests/3"],
      ["deleteComment", [9], "delete", "/comments/delete/9"],
    ])("%s", async (fn, args, method, url) => {
      await commentsService[fn](...args);
      expect(http[method]).toHaveBeenCalledWith(url);
    });

    test("createComment", async () => {
      await commentsService.createComment("Hello", 3);
      expect(http.post).toHaveBeenCalledWith("/comments/create", { text: "Hello", idForumRoom: 3 });
    });

    test("approveComment", async () => {
      await commentsService.approveComment(9, 1);
      expect(http.put).toHaveBeenCalledWith("/comments/approve", { id: 9, isApproved: 1 });
    });

    test("updateComment", async () => {
      await commentsService.updateComment(9, "Edited", 3);
      expect(http.put).toHaveBeenCalledWith("/comments/update", { idComment: 9, text: "Edited", idForumRoom: 3 });
    });
  });

  describe("forum rooms", () => {
    test.each([
      ["getForumRooms", [], "get", "/forumrooms"],
      ["deleteForumRoom", [2], "delete", "/forumrooms/delete/2"],
    ])("%s", async (fn, args, method, url) => {
      await forumRoomsService[fn](...args);
      expect(http[method]).toHaveBeenCalledWith(url);
    });

    test("createForumRoom", async () => {
      await forumRoomsService.createForumRoom("Movies");
      expect(http.post).toHaveBeenCalledWith("/forumrooms/create", { name: "Movies" });
    });

    test("updateForumRoom", async () => {
      await forumRoomsService.updateForumRoom(2, "Film");
      expect(http.put).toHaveBeenCalledWith("/forumrooms/update", { id: 2, name: "Film" });
    });
  });

  describe("security log", () => {
    test("all events", async () => {
      await securityEventsService.getSecurityEvents(100);
      expect(http.get).toHaveBeenCalledWith("/security-events", { params: { limit: 100 } });
    });

    test("one event type", async () => {
      await securityEventsService.getSecurityEvents(50, "WAF_BLOCKED");
      expect(http.get).toHaveBeenCalledWith("/security-events", { params: { limit: 50, type: "WAF_BLOCKED" } });
    });
  });

  describe("users", () => {
    test.each([
      ["requests", [], "get", "/users/requests"],
      ["getApprovedUser", [1], "get", "/users/approved/1"],
    ])("%s", async (fn, args, method, url) => {
      await userService[fn](...args);
      expect(http[method]).toHaveBeenCalledWith(url);
    });

    test("approveUser sends role, permissions and rooms", async () => {
      await userService.approveUser({
        id: 5,
        isApproved: true,
        role: "ROLE_MODERATOR",
        permissions: ["ADD"],
        roomIds: [1, 2],
        extra: "ignored",
      });
      expect(http.put).toHaveBeenCalledWith("/users/approve", {
        id: 5,
        isApproved: true,
        role: "ROLE_MODERATOR",
        permissions: ["ADD"],
        roomIds: [1, 2],
      });
    });

    test("changeRole sends the exact permission set and the rooms", async () => {
      await userService.changeRole({ id: 5, role: "ROLE_MODERATOR", permissions: ["ADD"], roomIds: [1, 3] });
      expect(http.put).toHaveBeenCalledWith("/users/changeRole", {
        id: 5,
        role: "ROLE_MODERATOR",
        permissions: ["ADD"],
        roomIds: [1, 3],
      });
    });

    test("setAccess blocks or restores an account", async () => {
      await userService.setAccess(5, false);
      expect(http.put).toHaveBeenCalledWith("/users/access", { id: 5, allowed: false });
    });
  });
});
