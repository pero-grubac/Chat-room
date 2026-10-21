import "./Users.css";
import { changeRole } from "../../Services/user.service";
import { handleChangeRole } from "../Error/ErrorMessage";
import { useSessionGuard } from "../Util/useSessionGuard";
import RoleForm from "./RoleForm";

// Changes the role, permissions and rooms of an approved account
const UserPop = ({ user, rooms, onClose }) => {
  const guard = useSessionGuard();

  const handleSave = async (assignment) => {
    try {
      await changeRole({ id: user.id, ...assignment });
      onClose();
    } catch (error) {
      handleChangeRole(error);
      guard(error);
    }
  };

  return (
    <RoleForm
      title={`Role and permissions of ${user.name}`}
      initial={user}
      rooms={rooms}
      submitLabel="Save"
      onSubmit={handleSave}
      onClose={onClose}
    />
  );
};

export default UserPop;
