import { useEffect, useState } from "react";
import { Modal, Select, Table, Tag } from "antd";
import { getSecurityEvents } from "../../Services/securityEvents.service";
import { formatDateTime } from "../Util/formatDateTime";
import { handleGetSecurityEvents } from "../Error/ErrorMessage";

// Events the SIEM flags as warnings (see SecurityEvent.java)
const WARNINGS = new Set([
  "LOGIN_FAILED",
  "LOGIN_NOT_APPROVED",
  "LOGIN_CODE_INVALID",
  "SESSION_REJECTED",
  "SESSION_REVOKED",
  "ACCESS_DENIED",
  "WAF_BLOCKED",
  "RATE_LIMITED",
  "USER_BLOCKED",
  "USER_DELETED",
]);

const EVENT_TYPES = [
  "LOGIN_SUCCESS",
  "LOGIN_FAILED",
  "LOGIN_CODE_INVALID",
  "LOGIN_NOT_APPROVED",
  "SESSION_REJECTED",
  "SESSION_REVOKED",
  "ACCESS_DENIED",
  "WAF_BLOCKED",
  "RATE_LIMITED",
  "USER_BLOCKED",
  "ROLE_CHANGED",
];

const columns = [
  { title: "Time", dataIndex: "createdAt", key: "time", width: 140, render: formatDateTime },
  {
    title: "Event",
    dataIndex: "type",
    key: "type",
    width: 170,
    render: (type) => <Tag color={WARNINGS.has(type) ? "red" : "blue"}>{type}</Tag>,
  },
  { title: "User", dataIndex: "username", key: "user", width: 120 },
  { title: "IP", dataIndex: "ipAddress", key: "ip", width: 120 },
  { title: "Detail", dataIndex: "message", key: "detail", ellipsis: true },
];

/** The SIEM's security log: the latest security-sensitive events, newest first. */
const SecurityLog = ({ onClose }) => {
  const [events, setEvents] = useState([]);
  const [type, setType] = useState(undefined);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let current = true;
    setLoading(true);
    getSecurityEvents(200, type)
      .then((response) => current && setEvents(response.data))
      .catch(handleGetSecurityEvents)
      .finally(() => current && setLoading(false));
    return () => {
      current = false;
    };
  }, [type]);

  return (
    <Modal title="Security log" open onCancel={onClose} footer={null} width={1000}>
      <Select
        allowClear
        placeholder="All events"
        style={{ width: 240, marginBottom: 12 }}
        value={type}
        onChange={setType}
        options={EVENT_TYPES.map((t) => ({ label: t, value: t }))}
      />
      <Table
        rowKey="id"
        size="small"
        loading={loading}
        columns={columns}
        dataSource={events}
        pagination={{ pageSize: 15 }}
      />
    </Modal>
  );
};

export default SecurityLog;
