import { useState, useEffect, useRef } from "react";
import { logout } from "../Redux/slices/userSlice";
import { useDispatch, useSelector } from "react-redux";
import {
  CheckCircleOutlined,
  LogoutOutlined,
  MailOutlined,
  SafetyOutlined,
  SettingOutlined,
  StopOutlined,
  UserAddOutlined,
} from "@ant-design/icons";
import { Space, Table, Button, Modal, Tag } from "antd";
import { getApprovedUser, setAccess } from "../../Services/user.service";
import { getForumRooms } from "../../Services/forumRooms.Service";
import { handleGetAllUsers, handleSetAccess } from "../Error/ErrorMessage";
import { useSessionGuard } from "../Util/useSessionGuard";
import "./Users.css";
import { useNavigate } from "react-router-dom";
import UserPop from "./UserPop";
import RequestedUser from "./RequestedUsers";
import SecurityLog from "./SecurityLog";

const { confirm } = Modal;
const roleLabel = {
  ROLE_ADMIN: "ADMIN",
  ROLE_MODERATOR: "MODERATOR",
  ROLE_KORISNIK: "USER",
};

const toRow = (user) => ({
  key: user.idUser,
  id: user.idUser,
  name: user.username,
  email: user.email,
  role: user.role,
  permissions: user.permissions.map((permission) => permission.permission),
  roomIds: user.roomIds ?? [],
  active: user.isApproved === 1,
});

// Administrator part: approved and blocked accounts, their role, permissions, rooms and access
const Users = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const guard = useSessionGuard();
  const me = useSelector((state) => state.users.user);
  const [users, setUsers] = useState([]);
  const [rooms, setRooms] = useState([]);
  const [usersUpdated, setUsersUpdated] = useState(false);
  const [editedUser, setEditedUser] = useState(null);
  const [showRequestedUsers, setShowRequestedUsers] = useState(false);
  const [showSecurityLog, setShowSecurityLog] = useState(false);
  const tableContainerRef = useRef(null);

  const roomNames = (ids) =>
    rooms.filter((room) => ids.includes(room.id)).map((room) => room.name).join(", ");

  const columns = [
    { title: "Username", dataIndex: "name", key: "name", ellipsis: true, width: 110 },
    { title: "Email", dataIndex: "email", key: "email", ellipsis: true, width: 190 },
    { title: "Role", dataIndex: "role", key: "role", width: 110, render: (role) => roleLabel[role] },
    {
      title: "Permissions",
      dataIndex: "permissions",
      key: "permissions",
      width: 150,
      render: (permissions) => permissions.join(", "),
    },
    { title: "Rooms", dataIndex: "roomIds", key: "rooms", ellipsis: true, width: 130, render: roomNames },
    {
      title: "Access",
      dataIndex: "active",
      key: "active",
      width: 100,
      render: (active) => (active ? <Tag color="green">active</Tag> : <Tag color="red">blocked</Tag>),
    },
    {
      title: "Action",
      key: "action",
      width: 100,
      render: (_, record) => (
        <Space size="middle">
          {record.active && (
            <SettingOutlined
              title="Role, permissions and rooms"
              aria-label="Edit"
              style={{ cursor: "pointer" }}
              onClick={() => setEditedUser(record)}
            />
          )}
          {record.name === me ? null : record.active ? (
            <StopOutlined
              title="Block access"
              aria-label="Block"
              style={{ cursor: "pointer", color: "#cf1322" }}
              onClick={() => changeAccess(record, false)}
            />
          ) : (
            <CheckCircleOutlined
              title="Restore access"
              aria-label="Unblock"
              style={{ cursor: "pointer", color: "#389e0d" }}
              onClick={() => changeAccess(record, true)}
            />
          )}
        </Space>
      ),
    },
  ];

  useEffect(() => {
    const load = async () => {
      try {
        const [active, blocked, roomList] = await Promise.all([
          getApprovedUser(1),
          getApprovedUser(0),
          getForumRooms(),
        ]);
        // Status 0 also covers rejected registrations; only accounts that had a role were blocked
        const blockedUsers = blocked.data.filter((user) => user.role !== null);
        setUsers([...active.data, ...blockedUsers].map(toRow));
        setRooms(roomList.data);
      } catch (error) {
        handleGetAllUsers(error);
        guard(error);
      }
    };
    load();
  }, [usersUpdated, guard]);

  useEffect(() => {
    function resizeHandler() {
      if (tableContainerRef.current) {
        const parentHeight = tableContainerRef.current.parentElement.clientHeight;
        tableContainerRef.current.style.maxHeight = `${parentHeight * 0.8}px`;
      }
    }
    window.addEventListener("resize", resizeHandler);
    resizeHandler();
    return () => window.removeEventListener("resize", resizeHandler);
  }, [usersUpdated]);

  const refresh = () => setUsersUpdated((updated) => !updated);

  const changeAccess = (user, allowed) => {
    confirm({
      title: allowed
        ? `Restore access for ${user.name}?`
        : `Block ${user.name}? They are signed out at once and cannot sign in until unblocked.`,
      okText: "Yes",
      cancelText: "No",
      onOk: async () => {
        try {
          await setAccess(user.id, allowed);
          refresh();
        } catch (error) {
          handleSetAccess(error);
          guard(error);
        }
      },
    });
  };

  const handleCloseForm = () => {
    setEditedUser(null);
    setShowRequestedUsers(false);
    refresh();
  };

  const handleLogout = async () => {
    await dispatch(logout());
    navigate("/login", { replace: true });
  };

  return (
    <div>
      {showRequestedUsers && <RequestedUser onClose={handleCloseForm} />}
      {showSecurityLog && <SecurityLog onClose={() => setShowSecurityLog(false)} />}

      <div className="button-container">
        <Button
          className="requests-button requests-button-top"
          size="large"
          type="primary"
          shape="circle"
          title="Forum rooms"
          onClick={() => navigate("/forum_rooms")}
        >
          <MailOutlined style={{ fontSize: "24px" }} />
        </Button>
        <Button
          className="requests-button requests-button-top"
          size="large"
          type="primary"
          shape="circle"
          title="Registration requests"
          onClick={() => setShowRequestedUsers(true)}
        >
          <UserAddOutlined style={{ fontSize: "24px" }} />
        </Button>
        <Button
          className="requests-button requests-button-top"
          size="large"
          type="primary"
          shape="circle"
          title="Security log"
          onClick={() => setShowSecurityLog(true)}
        >
          <SafetyOutlined style={{ fontSize: "24px" }} />
        </Button>
        <Button
          className="requests-button requests-button-top"
          size="large"
          type="primary"
          shape="circle"
          title="Sign out"
          onClick={handleLogout}
        >
          <LogoutOutlined style={{ fontSize: "24px" }} />
        </Button>
      </div>
      <div className="user-container">
        {editedUser && <UserPop user={editedUser} rooms={rooms} onClose={handleCloseForm} />}
        <h2 style={{ textAlign: "center" }}>Users</h2>
        <br />
        <div className="table-container" ref={tableContainerRef} style={{ overflowY: "auto" }}>
          <Table columns={columns} dataSource={users} pagination={false} />
        </div>
      </div>
    </div>
  );
};
export default Users;
