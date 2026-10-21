import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useSessionGuard } from "../Util/useSessionGuard";
import {
  MailOutlined,
  UserOutlined,
  FormOutlined,
  SettingOutlined,
  LogoutOutlined,
  PlusOutlined,
  DeleteOutlined,
} from "@ant-design/icons";
import { Card, Button } from "antd";
import {
  deleteForumRoom,
  getForumRooms,
} from "../../Services/forumRooms.Service";
import {
  hadnleDeleteForumRoom,
  handleGetForumRooms,
} from "../Error/ErrorMessage";
import { useDispatch, useSelector } from "react-redux";
import { authState, logout } from "../Redux/slices/userSlice";
import "./ForumRoom.css";
import { ROLE_ADMIN, ROLE_MODERATOR } from "../Util/RolePermission";
import UpdateForumRoom from "./UpdateForumRoom";
import NewForumRoom from "./NewForumRoom";
import { Modal } from "antd";
import Comments from "../Comments/Comments";
import RequestedComments from "../Comments/RequestedComments";

const { confirm } = Modal;
const ForumRoom = () => {
  const guard = useSessionGuard();
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const [rooms, setRooms] = useState([]);

  const [showUpdateRoomForm, setShowUpdateRoomForm] = useState(false);
  const [roomName, setRoomName] = useState("");
  const [id, setid] = useState("");
  const [roomUpdated, setRoomUpdated] = useState(false);
  const [showNewRoomForm, setshowNewRoomForm] = useState(false);
  const [showComments, setShowComments] = useState(false);
  const [showRequestedComments, setShowRequestedComments] = useState(false);
  const userRole = useSelector((state) => state.users.role);
  const permissions = useSelector((state) => state.users.permissions);
  const myRooms = useSelector((state) => state.users.rooms);
  // Admins moderate every room, moderators only the rooms assigned to them
  const moderates = (roomId) =>
    userRole === ROLE_ADMIN || (userRole === ROLE_MODERATOR && myRooms.includes(roomId));

  useEffect(() => {
    const fetchForumRooms = async () => {
      try {
        const response = await getForumRooms();
        setRooms(response.data);
      } catch (error) {
        handleGetForumRooms(error);
        guard(error);
      }
    };
    fetchForumRooms();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [  roomUpdated]);

  const handleLogout = async () => {
    await dispatch(logout());
    navigate("/login", { replace: true });
  };

  const handleUpdate = (name, id) => {
    setRoomName(name);
    setid(id);
    setShowUpdateRoomForm(true);
  };

  const handleAllowAction = (id) => {
    setid(id);
    setShowRequestedComments(true);
  };

  const handleNewRoomAction = (id) => {
    setRoomName();
    setshowNewRoomForm(true);
  };

  const handleMailAction = (id) => {
    setid(id);
    setShowComments(true);
  };

  const handleDelete = async (id) => {
    confirm({
      title: "Are you sure you want to delete this room?",
      okText: "Yes",
      cancelText: "No",
      onOk: async () => {
        try {
          await deleteForumRoom(id);
          setRooms(rooms.filter((room) => room.id !== id));
        } catch (error) {
          hadnleDeleteForumRoom(error);
          guard(error);
        }
      },
      onCancel() {},
    });
  };

  const handleCloseForm = () => {
    setShowUpdateRoomForm(false);
    setshowNewRoomForm(false);
    setRoomUpdated(!roomUpdated);
    setShowComments(false);
    setShowRequestedComments(false);
  };

  const handleUsers = async () => {
    await dispatch(authState());
    navigate("/users");
  };

  return (
    <div>
      {userRole === ROLE_ADMIN && showUpdateRoomForm && (
        <UpdateForumRoom
          onClose={handleCloseForm}
          roomName={roomName}
          id={id}
        />
      )}
      {moderates(id) &&
        showRequestedComments && (
          <RequestedComments
            permissions={permissions}
            onClose={handleCloseForm}
            idRoom={id}
          />
        )}
      {userRole === ROLE_ADMIN && showNewRoomForm && (
        <NewForumRoom onClose={handleCloseForm} roomName={roomName} />
      )}
      {showComments && <Comments onClose={handleCloseForm} idroom={id} />}
      <div className="button-container">
        {userRole === ROLE_ADMIN && (
          <Button
            className="forum-button forum-button-top"
            size="large"
            type="primary"
            shape="circle"
            onClick={handleUsers}
          >
            <UserOutlined style={{ fontSize: "24px" }} />
          </Button>
        )}

        <Button
          className="forum-button forum-button-top"
          size="large"
          type="primary"
          shape="circle"
          onClick={handleLogout}
        >
          <LogoutOutlined style={{ fontSize: "24px" }} />
        </Button>
      </div>
      <div
        style={{
          display: "flex",
          justifyContent: "center",
          flexWrap: "wrap",
          padding: "20px",
        }}
      >
        {rooms.map((room) => (
          <Card
            key={room.id}
            style={{
              width: 300,
              margin: 10,
              background: "transparent",
              backdropFilter: "blur(7px)",
            }}
            actions={[
              userRole === ROLE_ADMIN && (
                <SettingOutlined
                  key={`setting-${room.id}`}
                  title="Rename room"
                  onClick={() => handleUpdate(room.name, room.id)}
                />
              ),
              userRole === ROLE_ADMIN && (
                <DeleteOutlined
                  key={`delete-${room.id}`}
                  title="Delete room"
                  onClick={() => handleDelete(room.id)}
                />
              ),
              <MailOutlined
                key={`mail-${room.id}`}
                title="Comments"
                onClick={() => handleMailAction(room.id)}
              />,
              moderates(room.id) ? (
                <FormOutlined
                  key={`form-${room.id}`}
                  title="Moderation queue"
                  onClick={() => handleAllowAction(room.id)}
                />
              ) : null,
            ]}
          >
            <Card.Meta
              title={<div style={{ textAlign: "center" }}>{room.name}</div>}
            />
          </Card>
        ))}
        {userRole === ROLE_ADMIN && (
          <Card
            style={{
              width: 300,
              margin: 10,
              background: "transparent",
              backdropFilter: "blur(7px)",
            }}
            actions={[<PlusOutlined key="add" title="New room" onClick={handleNewRoomAction} />]}
          >
            <Card.Meta
              title={<div style={{ textAlign: "center" }}>New room</div>}
            />
          </Card>
        )}
      </div>
    </div>
  );
};

export default ForumRoom;
