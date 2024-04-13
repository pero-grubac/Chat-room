import React, { useState, useEffect } from "react";
import {
  MailOutlined,
  UserOutlined,
  FormOutlined,
  SettingOutlined,
  LogoutOutlined,
  PlusOutlined,
} from "@ant-design/icons";
import { Card, Button } from "antd";
import { getForumRooms } from "../../Services/forumRooms.Service";
import { useNavigate } from "react-router-dom";
import { handleGetForumRooms } from "../Error/ErrorMessage";
import { useDispatch } from "react-redux";
import { logout } from "../Redux/slices/userSlice";
import "./ForumRoom.css";
import { ROLE_ADMIN, ROLE_USER, ROLE_MODERATOR } from "../Util/RolePermission";
const ForumRoom = () => {
  const [rooms, setRooms] = useState([]);
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const [userRole, setUserRole] = useState("");

  useEffect(() => {
    const fetchForumRooms = async () => {
      try {
        const response = await getForumRooms();
        setRooms(response.data);
      } catch (error) {
        handleGetForumRooms(error);
        navigate("/login");
      }
    };

    fetchForumRooms();
    const role = sessionStorage.getItem("role");
    setUserRole(role);
  }, [navigate]);

  const handleLogout = async () => {
    try {
      dispatch(logout());
      navigate("/login");
    } catch (error) {
      console.error("Logout failed:", error);
    }
  };

  const handleSettingAction = () => {
    // Logic for setting action
    console.log("Setting action clicked");
  };

  const handleFormAction = () => {
    // Logic for form action
    console.log("Form action clicked");
  };

  const handleNewRoomAction = () => {
    // Logic for new room action
    console.log("New room action clicked");
  };

  const handleMailAction = () => {
    console.log("Mail action clicked");
  };
  return (
    <div>
      {userRole === ROLE_ADMIN && (
        <Button
          className="forum-button forum-button-top"
          size="large"
          type="primary"
          shape="circle"
        >
          <UserOutlined style={{ fontSize: "24px" }} />
        </Button>
      )}
      <Button
        className="forum-button forum-button-bottom"
        size="large"
        type="primary"
        shape="circle"
        onClick={handleLogout}
      >
        <LogoutOutlined style={{ fontSize: "24px" }} />
      </Button>
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
              userRole === ROLE_ADMIN && <SettingOutlined key="setting" onClick={handleSettingAction} />,
              <MailOutlined key="mail" onClick={handleMailAction} />,
              userRole === ROLE_ADMIN || userRole === ROLE_MODERATOR ? (
                <FormOutlined key="form" onClick={handleFormAction}/>
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
            actions={[<PlusOutlined key="add" onClick={handleNewRoomAction}/>]}
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
