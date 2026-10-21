import { useState, useEffect, useRef } from "react";
import { Space, Table, Modal } from "antd";
import { CloseOutlined, CheckOutlined } from "@ant-design/icons";
import { approveUser, requests } from "../../Services/user.service";
import { handleGetRequests, handleRejectUser } from "../Error/ErrorMessage";
import { useSessionGuard } from "../Util/useSessionGuard";
import AcceptUserPop from "./AcceptUserPop";

const { confirm } = Modal;

// Pending registrations: approve with a role, or reject; the user is emailed either way
const RequestedUser = ({ onClose }) => {
  const guard = useSessionGuard();
  const [users, setUsers] = useState([]);
  const tableContainerRef = useRef(null);
  const [requestsUpdated, setRequestsUpdated] = useState(false);
  const [approvedUser, setApprovedUser] = useState(null);

  const columns = [
    { title: "Username", dataIndex: "name", key: "name", ellipsis: true, width: 200 },
    { title: "Email", dataIndex: "email", key: "email" },
    {
      title: "Action",
      key: "action",
      render: (_, record) => (
        <Space size="middle" style={{ display: "flex", justifyContent: "center", alignItems: "center" }}>
          <CloseOutlined
            title="Reject"
            aria-label="Reject"
            onClick={() => handleReject(record)}
            style={{ cursor: "pointer" }}
          />
          <CheckOutlined
            title="Approve"
            aria-label="Approve"
            onClick={() => setApprovedUser(record)}
            style={{ cursor: "pointer" }}
          />
        </Space>
      ),
      ellipsis: true,
      width: 100,
    },
  ];

  useEffect(() => {
    requests()
      .then((response) =>
        setUsers(response.data.map((user) => ({ key: user.idUser, id: user.idUser, name: user.username, email: user.email })))
      )
      .catch((error) => {
        handleGetRequests(error);
        guard(error);
      });
  }, [requestsUpdated, guard]);

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
  }, [requestsUpdated]);

  const refresh = () => setRequestsUpdated((updated) => !updated);

  const handleReject = (user) => {
    confirm({
      title: `Reject the registration of ${user.name}? They are told by email.`,
      okText: "Yes",
      cancelText: "No",
      onOk: async () => {
        try {
          await approveUser({ id: user.id, isApproved: false, role: null, permissions: [], roomIds: [] });
          refresh();
        } catch (error) {
          handleRejectUser(error);
          guard(error);
        }
      },
    });
  };

  const handleCloseForm = () => {
    setApprovedUser(null);
    refresh();
  };

  return (
    <div className="requested-user-container">
      <CloseOutlined className="close-icon" aria-label="Close" onClick={onClose} />
      {approvedUser && <AcceptUserPop onClose={handleCloseForm} user={approvedUser} />}
      <h2>Requested users</h2>
      <br />
      <div className="table-container" ref={tableContainerRef} style={{ overflowY: "auto" }}>
        <Table columns={columns} dataSource={users} pagination={false} locale={{ emptyText: "No pending registrations" }} />
      </div>
    </div>
  );
};
export default RequestedUser;
