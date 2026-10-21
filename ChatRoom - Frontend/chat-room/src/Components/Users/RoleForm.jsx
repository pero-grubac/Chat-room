import { useState } from "react";
import { Alert, Button, Checkbox, Form, Modal, Radio } from "antd";
import {
  PERMISSION_ADD,
  PERMISSION_DELETE,
  PERMISSION_UPDATE,
  ROLE_ADMIN,
  ROLE_MODERATOR,
  ROLE_USER,
} from "../Util/RolePermission";

const { Item } = Form;
const ALL_PERMISSIONS = [PERMISSION_ADD, PERMISSION_UPDATE, PERMISSION_DELETE];

/**
 * Role, permissions and rooms of an account, as the server expects them: admins hold every permission,
 * users none, and a moderator a chosen set that applies to the rooms chosen for them.
 */
export const toAssignment = (role, permissions, roomIds) => ({
  role,
  permissions: role === ROLE_ADMIN ? ALL_PERMISSIONS : role === ROLE_MODERATOR ? permissions : [],
  roomIds: role === ROLE_MODERATOR ? roomIds : [],
});

const RoleForm = ({ title, initial, rooms, submitLabel, onSubmit, onClose }) => {
  const [role, setRole] = useState(initial.role ?? null);
  const [permissions, setPermissions] = useState(initial.permissions ?? []);
  const [roomIds, setRoomIds] = useState(initial.roomIds ?? []);
  const [saving, setSaving] = useState(false);
  const isModerator = role === ROLE_MODERATOR;

  const problem = !role
    ? "Choose a role."
    : isModerator && permissions.length === 0
      ? "A moderator needs at least one permission."
      : isModerator && roomIds.length === 0
        ? "A moderator needs at least one room."
        : null;

  const handleSubmit = async () => {
    setSaving(true);
    try {
      await onSubmit(toAssignment(role, permissions, roomIds));
    } finally {
      setSaving(false);
    }
  };

  return (
    <Modal title={title} open onCancel={onClose} footer={null}>
      <Form layout="vertical">
        <Item label="Role" required>
          <Radio.Group onChange={(e) => setRole(e.target.value)} value={role}>
            <Radio.Button value={ROLE_USER}>USER</Radio.Button>
            <Radio.Button value={ROLE_MODERATOR}>MODERATOR</Radio.Button>
            <Radio.Button value={ROLE_ADMIN}>ADMIN</Radio.Button>
          </Radio.Group>
        </Item>
        {isModerator && (
          <>
            <Item label="Permissions" required>
              <Checkbox.Group onChange={setPermissions} value={permissions}>
                <Checkbox value={PERMISSION_ADD}>Publish / forbid</Checkbox>
                <Checkbox value={PERMISSION_UPDATE}>Edit</Checkbox>
                <Checkbox value={PERMISSION_DELETE}>Delete</Checkbox>
              </Checkbox.Group>
            </Item>
            <Item label="Rooms this moderator manages" required>
              <Checkbox.Group
                onChange={setRoomIds}
                value={roomIds}
                options={rooms.map((room) => ({ label: room.name, value: room.id }))}
              />
            </Item>
          </>
        )}
        {role === ROLE_ADMIN && (
          <Alert type="info" showIcon message="Administrators hold every permission in every room." />
        )}
        {problem && role && <Alert type="warning" showIcon message={problem} />}
        <Item style={{ textAlign: "center", marginTop: 16 }}>
          <Button
            type="primary"
            onClick={handleSubmit}
            disabled={Boolean(problem)}
            loading={saving}
            style={{ marginRight: 8 }}
          >
            {submitLabel}
          </Button>
          <Button onClick={onClose}>Close</Button>
        </Item>
      </Form>
    </Modal>
  );
};

export default RoleForm;
