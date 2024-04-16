import React, { useState, useEffect, useRef } from "react";
import { logout } from "../Redux/slices/userSlice";
//import { useDispatch, useSelector } from "react-redux";
import { SettingOutlined } from "@ant-design/icons";
import { Space, Table, Modal, Button } from "antd";
import {
  UserAddOutlined,
  MailOutlined,
  LogoutOutlined,
} from "@ant-design/icons";
import { getApprovedUser, getUsers } from "../../Services/user.service";
import { handleGetAllUsers } from "../Error/ErrorMessage";
import "./Users.css";
import { redirect, useNavigate } from "react-router-dom";
import UserPop from "./UserPop";
import RequestedUser from "./RequestedUsers";
import RequestedComments from "../Comments/RequestedComments";
const roleMap = {
  ROLE_ADMIN: "ADMIN",
  ROLE_MODERATOR: "MODERATOR",
  ROLE_KORISNIK: "KORISNIK",
};
const Users = () => {
  //const dispatch = useDispatch();
  const [users, setUsers] = useState([]);
  const [usersUpdated, setUsersUpdated] = useState(false);
  const tableContainerRef = useRef(null);
  const [showUpdateUser, setShowUpdateUser] = useState(false);
  const [showRequestedUsers, setShowRequestedUsers] = useState(false);

  const navigate = useNavigate();
  const [role, setRole] = useState("");
  const [permissions, setPermissions] = useState([]);
  const [id, setId] = useState("");

  const columns = [
    {
      title: "Username",
      dataIndex: "name",
      key: "name",
      ellipsis: true,
      width: 200,
      // eslint-disable-next-line jsx-a11y/anchor-is-valid
      render: (text) => <a>{text}</a>,
    },
    {
      title: "Email",
      dataIndex: "email",
      key: "email",
    },
    {
      title: "Role",
      dataIndex: "role",
      key: "role",
      ellipsis: true,
      width: 160,
    },
    {
      title: "Permissions",
      dataIndex: "permissions",
      key: "permissions",
      ellipsis: true,
      width: 190,
    },
    {
      title: "Action",
      key: "action",
      render: (_, record) => (
        <Space
          size="middle"
          style={{
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
          }}
        >
          <SettingOutlined
            style={{ cursor: "pointer" }}
            onClick={() =>
              showUserPop(record.role, record.permissions, record.key)
            }
          />
        </Space>
      ),
      ellipsis: true,
      width: 100,
    },
  ];
  useEffect(() => {
    const getAllComments = async () => {
      try {
        const response = await getApprovedUser(1);
        const mappedUsers = response.data.map((user) => ({
          key: user.idUser,
          name: user.username,
          email: user.email,
          role: roleMap[user.role],
          permissions: user.permissions
            .map((permission) => permission.permission)
            .join(", "),
        }));
        setUsers(mappedUsers);
      } catch (error) {
        console.log(error);
        handleGetAllUsers(error);
        /// dispatch(logout());
      }
    };

    getAllComments();
  }, [usersUpdated]);
  useEffect(() => {
    function resizeHandler() {
      if (tableContainerRef.current) {
        const parentHeight =
          tableContainerRef.current.parentElement.clientHeight;
        const newMaxHeight = parentHeight * 0.8;
        tableContainerRef.current.style.maxHeight = `${newMaxHeight}px`;
      }
    }

    window.addEventListener("resize", resizeHandler);
    resizeHandler();

    return () => window.removeEventListener("resize", resizeHandler);
  }, [usersUpdated]);

  const handleCloseForm = () => {
    setShowUpdateUser(false);
    setUsersUpdated(!usersUpdated);
    setShowRequestedUsers(false);
  };
  const showUserPop = (r, p, i) => {
    setRole(r);
    setPermissions(p);
    setId(i);
    setShowUpdateUser(true);
  };
  const handleRequests = () => {
    setShowRequestedUsers(true);
    console.log("Show requests:", showRequestedUsers);
  };
  const goToForumRoom = () => {
    navigate("/forum_rooms");
  };
  const handleLogout = async () => {
    try {
      //dispatch(logout());
      navigate("/login", { replace: true });
    } catch (error) {
      console.error("Logout failed:", error);
    }
  };
  return (
    <div>
      {showRequestedUsers && <RequestedUser onClose={handleCloseForm} />}

      <div className="button-container">
        <Button
          className="requests-button requests-button-top"
          size="large"
          type="primary"
          shape="circle"
          onClick={goToForumRoom}
        >
          <MailOutlined style={{ fontSize: "24px" }} />
        </Button>
        <Button
          className="requests-button requests-button-top"
          size="large"
          type="primary"
          shape="circle"
          onClick={() => handleRequests()}
        >
          <UserAddOutlined style={{ fontSize: "24px" }} />
        </Button>
        <Button
          className="requests-button requests-button-top"
          size="large"
          type="primary"
          shape="circle"
          onClick={handleLogout}
        >
          <LogoutOutlined style={{ fontSize: "24px" }} />
        </Button>
      </div>
      <div className="user-container">
        {showUpdateUser && (
          <UserPop
            onClose={handleCloseForm}
            role={role}
            permissions={permissions}
            id={id}
            lblBtn={"Update"}
          />
        )}
        <h2 style={{ textAlign: "center" }}>Users</h2>
        <br />
        <div
          className="table-container"
          ref={tableContainerRef}
          style={{ overflowY: "auto" }}
        >
          <Table columns={columns} dataSource={users} pagination={false} />
        </div>
      </div>
    </div>
  );
};
export default Users;
