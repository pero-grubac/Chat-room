import React, { useState, useEffect } from "react";
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
import {  useNavigate } from "react-router-dom";
import {
  hadnleDeleteForumRoom,
  handleGetForumRooms,
} from "../Error/ErrorMessage";
import { useDispatch } from "react-redux";
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
  const [rooms, setRooms] = useState([]);
  const navigate = useNavigate();
  const dispatch = useDispatch();

  const [showUpdateRoomForm, setShowUpdateRoomForm] = useState(false);
  const [roomName, setRoomName] = useState("");
  const [id, setid] = useState("");
  const [roomUpdated, setRoomUpdated] = useState(false);
  const [showNewRoomForm, setshowNewRoomForm] = useState(false);
  const [showComments, setShowComments] = useState(false);
  const [showRequestedComments, setShowRequestedComments] = useState(false);
  const userRole = sessionStorage.getItem("role"); //useSelector((state) => state.users.role);
  const permissions = sessionStorage.getItem("permissions"); //useSelector((state) => state.users.permissions);

  useEffect(() => {
    const fetchForumRooms = async () => {
      try {
        const response = await getForumRooms();
        setRooms(response.data);
      } catch (error) {
        console.log(error);
        handleGetForumRooms(error);
        dispatch(logout());
        navigate("/login", { replace: true });
      }
    };
    //   dispatch(authState());
    fetchForumRooms();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [  roomUpdated]);

  const handleLogout = async () => {
    try {
      dispatch(logout());
      navigate("/login", { replace: true });
    } catch (error) {
      console.error("Logout failed:", error);
    }
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
          console.log(error);
          hadnleDeleteForumRoom(error);
          dispatch(logout());
          navigate("/login", { replace: true });
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

  const handleUsers = () => {
    dispatch(authState());
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
      {(userRole === ROLE_ADMIN || userRole === ROLE_MODERATOR) &&
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
                  onClick={() => handleUpdate(room.name, room.id)}
                />
              ),
              userRole === ROLE_ADMIN && (
                <DeleteOutlined
                  ey={`delete-${room.id}`}
                  onClick={() => handleDelete(room.id)}
                />
              ),
              <MailOutlined
                key={`mail-${room.id}`}
                onClick={() => handleMailAction(room.id)}
              />,
              userRole === ROLE_ADMIN || userRole === ROLE_MODERATOR ? (
                <FormOutlined
                  key={`form-${room.id}`}
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
            actions={[<PlusOutlined key="add" onClick={handleNewRoomAction} />]}
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
