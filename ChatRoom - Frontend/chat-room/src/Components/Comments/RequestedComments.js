import React, { useState, useEffect, useRef } from "react";
import "./Comments.css";
//import { useDispatch } from "react-redux";
import { logout } from "../Redux/slices/userSlice";
import { Space, Table, Modal } from "antd";
import UpdateComment from "./UpdateComment";
import { CloseOutlined, CheckOutlined, EditOutlined } from "@ant-design/icons";
import {
  PERMISSION_ADD,
  PERMISSION_DELETE,
  PERMISSION_UPDATE,
} from "../Util/RolePermission";

import {
  approveComment,
  deleteComment,
  getRequestedComments,
} from "../../Services/comments.service";
import {
  handleApproveComments,
  handleDeleteComments,
  handleGetRequestedComments,
} from "../Error/ErrorMessage";
const { confirm } = Modal;

const RequestedComments = ({ permissions, onClose, idRoom }) => {
  const columns = [
    {
      title: "Username",
      dataIndex: "name",
      key: "name",
      ellipsis: true,
      width: 150,
      // eslint-disable-next-line jsx-a11y/anchor-is-valid
      render: (text) => <a>{text}</a>,
    },
    {
      title: "Date",
      dataIndex: "date",
      key: "date",
      ellipsis: true,
      width: 150,
    },
    {
      title: "Message",
      dataIndex: "message",
      key: "message",
    },
    {
      title: "Action",
      key: "action",
      render: (_, record) => (
        <Space size="middle">
          {permissions.includes(PERMISSION_DELETE) && (
            <CloseOutlined
              onClick={() => handleDelete(record.key)}
              style={{ cursor: "pointer" }}
            />
          )}
          {permissions.includes(PERMISSION_ADD) && (
            <CheckOutlined
              onClick={() => handleApprove(record.key)}
              style={{ cursor: "pointer" }}
            />
          )}
          {permissions.includes(PERMISSION_UPDATE) && (
            <EditOutlined
              onClick={() => hadleUpdatePopUp(record.key, record.message)}
              style={{ cursor: "pointer" }}
            />
          )}
        </Space>
      ),
      ellipsis: true,
      width: 201,
    },
  ];
  //const dispatch = useDispatch();
  const [comments, setComments] = useState([]);
  const tableContainerRef = useRef(null);
  const [requestsUpdated, setRequestsUpdated] = useState(false);
  const [showUpdateComment, setShowUpdateComment] = useState(false);
  const [idComment, setIdComment] = useState("");
  const [message, setMessage] = useState("");
  useEffect(() => {
    const getAllComments = async () => {
      try {
        const response = await getRequestedComments(idRoom);
        const mappedComments = response.data.map((comment) => ({
          key: comment.idComment,
          name: comment.user.username,
          date: formatDateTime(comment.createdAt),
          message: comment.text,
        }));
        setComments(mappedComments);
      } catch (error) {
        console.log(error);
        handleGetRequestedComments(error);
       // dispatch(logout());
      }
    };
    getAllComments();
  }, [ idRoom, requestsUpdated]);
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
  function formatDateTime(dateTimeString) {
    const date = new Date(dateTimeString);
    const day = date.getDate();
    const month = date.getMonth() + 1;
    const year = date.getFullYear();
    const hours = date.getHours();
    const minutes = date.getMinutes();

    const formattedDate = `${day}.${month}.${year}`;
    const formattedTime = `${hours}:${minutes.toString().padStart(2, "0")}`;

    return `${formattedDate} ${formattedTime}`;
  }
  const handleDelete = async (messageId) => {
    confirm({
      title: "Are you sure you want to delete this comment request?",
      okText: "Yes",
      cancelText: "No",
      onOk: async () => {
        try {
          await deleteComment(messageId);
          setRequestsUpdated(!requestsUpdated);
        } catch (error) {
          console.log(error);
          handleDeleteComments(error);
          //dispatch(logout());
        }
      },
      onCancel() {},
    });
  };
  const handleApprove = async (messageId) => {
    confirm({
      title: "Are you sure you want to approve this comment request?",
      okText: "Yes",
      cancelText: "No",
      onOk: async () => {
        try {
          await approveComment(messageId, 1);
          setRequestsUpdated(!requestsUpdated);
        } catch (error) {
          console.log(error);
          handleApproveComments(error);
         // dispatch(logout());
        }
      },
      onCancel() {},
    });
  };
  const hadleUpdatePopUp = (idComment, comment) => {
    setIdComment(idComment);
    setMessage(comment);
    setShowUpdateComment(!showUpdateComment);
  };
  const handleCloseForm = () => {
    setShowUpdateComment(false);
    setRequestsUpdated(!requestsUpdated);
  };
  return (
    <div className="requested-comments-container">
      {showUpdateComment && (
        <UpdateComment
          onClose={handleCloseForm}
          text={message}
          idComment={idComment}
          idForumRoom={idRoom}
        />
      )}
      <CloseOutlined className="close-icon" key={"close"} onClick={onClose} />
      <h2>Requested comments</h2>
      <br />
      <div
        className="table-container"
        ref={tableContainerRef}
        style={{ overflowY: "auto" }}
      >
        <Table columns={columns} dataSource={comments} pagination={false} />
      </div>
    </div>
  );
};
export default RequestedComments;
