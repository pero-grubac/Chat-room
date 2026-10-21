import { useState, useEffect, useRef } from "react";
import { useSessionGuard } from "../Util/useSessionGuard";
import "./Comments.css";
import { Space, Table, Modal } from "antd";
import UpdateComment from "./UpdateComment";

import { CloseOutlined, CheckOutlined, EditOutlined, StopOutlined } from "@ant-design/icons";
import { formatDateTime } from "../Util/formatDateTime";
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
  const guard = useSessionGuard();
  const columns = [
    {
      title: "Username",
      dataIndex: "name",
      key: "name",
      ellipsis: true,
      width: 150,
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
          {permissions.includes(PERMISSION_ADD) && (
            <CheckOutlined
              title="Publish"
              aria-label="Publish"
              onClick={() => handleDecision(record.key, 1)}
              style={{ cursor: "pointer" }}
            />
          )}
          {permissions.includes(PERMISSION_ADD) && (
            <StopOutlined
              title="Forbid"
              aria-label="Forbid"
              onClick={() => handleDecision(record.key, 0)}
              style={{ cursor: "pointer" }}
            />
          )}
          {permissions.includes(PERMISSION_UPDATE) && (
            <EditOutlined
              title="Adjust before publishing"
              aria-label="Adjust"
              onClick={() => hadleUpdatePopUp(record.key, record.message)}
              style={{ cursor: "pointer" }}
            />
          )}
          {permissions.includes(PERMISSION_DELETE) && (
            <CloseOutlined
              title="Delete"
              aria-label="Delete"
              onClick={() => handleDelete(record.key)}
              style={{ cursor: "pointer" }}
            />
          )}
        </Space>
      ),
      ellipsis: true,
      width: 201,
    },
  ];

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
        handleGetRequestedComments(error);
        guard(error);
      }
    };
    getAllComments();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [idRoom, requestsUpdated]);
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
          handleDeleteComments(error);
        }
      },
      onCancel() {},
    });
  };
  // 1 publishes the comment, 0 forbids it; either way it leaves the queue
  const handleDecision = async (messageId, isApproved) => {
    confirm({
      title: isApproved
        ? "Publish this comment?"
        : "Forbid this comment? It will never be shown.",
      okText: "Yes",
      cancelText: "No",
      onOk: async () => {
        try {
          await approveComment(messageId, isApproved);
          setRequestsUpdated(!requestsUpdated);
        } catch (error) {
          handleApproveComments(error);
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
