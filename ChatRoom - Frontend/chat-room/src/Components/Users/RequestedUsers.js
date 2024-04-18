import React, { useState, useEffect, useRef } from "react";
import { Space, Table, Modal } from "antd";
import { CloseOutlined, CheckOutlined } from "@ant-design/icons";
import { approveUser, requests } from "../../Services/user.service";
import { handleGetRequests } from "../Error/ErrorMessage";
import AcceptUserPop from "./AcceptUserPop";
import { useDispatch } from "react-redux";
import {  useNavigate } from "react-router-dom";
import { logout } from "../Redux/slices/userSlice";

const { confirm } = Modal;

const RequestedUser = ({ onClose }) => {
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
          <CloseOutlined
            onClick={() => handleReject(record.key)}
            style={{ cursor: "pointer" }}
            key={`close_${record.key}`}
          />
          <CheckOutlined
            onClick={() => handleApprove(record.key)}
            style={{ cursor: "pointer" }}
            key={`check_${record.key}`}
          />
        </Space>
      ),
      ellipsis: true,
      width: 100,
    },
  ];
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const [users, setUsers] = useState([]);
  const tableContainerRef = useRef(null);
  const [requestsUpdated, setRequestsUpdated] = useState(false);
  const [showAcceptUser, setShowAcceptUser] = useState(false);
  const [id, setId] = useState("");
  useEffect(() => {
    const getAllRequests = async () => {
      try {
        const response = await requests();
        const mappedUsers = response.data.map((user) => ({
          key: user.idUser,
          name: user.username,
          email: user.email,
        }));
        setUsers(mappedUsers);
      } catch (error) {
        console.log(error);
        handleGetRequests(error);
        dispatch(logout());
        navigate("/login", { replace: true });
      }
    };
    getAllRequests();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [requestsUpdated]);
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
  }, [requestsUpdated]);

  const handleReject = async (i) => {
    confirm({
      title: "Are you sure you want to delete this user request?",
      okText: "Yes",
      cancelText: "No",
      onOk: async () => {
        const user = {
          id: i,
          isApproved: false,
          role: null,
          permissions: null,
          idRoom: null,
        };
        try {
          await approveUser(user);
          setRequestsUpdated(!requestsUpdated);
        } catch (error) {
          console.log(error);
          dispatch(logout());
          navigate("/login", { replace: true });
        }
      },
      onCancel() {},
    });
  };
  const handleCloseForm = () => {
    setShowAcceptUser(false);
    setRequestsUpdated(!requestsUpdated);
  };
  const handleApprove = (i) => {
    setShowAcceptUser(true);
    setId(i);
  };
  return (
    <div className="requested-user-container">
      <CloseOutlined className="close-icon" key={"close"} onClick={onClose} />
      {showAcceptUser && (
        <AcceptUserPop onClose={handleCloseForm} idUser={id} />
      )}
      <h2>Requested users</h2>
      <br />
      <div
        className="table-container"
        ref={tableContainerRef}
        style={{ overflowY: "auto" }}
      >
        <Table columns={columns} dataSource={users} pagination={false} />
      </div>
    </div>
  );
};
export default RequestedUser;
