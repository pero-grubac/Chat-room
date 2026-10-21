import { useEffect, useState } from "react";
import "./Users.css";
import { approveUser } from "../../Services/user.service";
import { getForumRooms } from "../../Services/forumRooms.Service";
import { handleApproveUser, handleGetForumRooms } from "../Error/ErrorMessage";
import { useSessionGuard } from "../Util/useSessionGuard";
import RoleForm from "./RoleForm";

// Approves a registration with a role, permissions and (for moderators) rooms; the user is emailed
const AcceptUserPop = ({ user, onClose }) => {
  const guard = useSessionGuard();
  const [rooms, setRooms] = useState([]);

  useEffect(() => {
    getForumRooms()
      .then((response) => setRooms(response.data))
      .catch((error) => {
        handleGetForumRooms(error);
        guard(error);
      });
  }, [guard]);

  const handleApprove = async (assignment) => {
    try {
      await approveUser({ id: user.id, isApproved: true, ...assignment });
      onClose();
    } catch (error) {
      handleApproveUser(error);
      guard(error);
    }
  };

  return (
    <RoleForm
      title={`Approve ${user.name}`}
      initial={{}}
      rooms={rooms}
      submitLabel="Approve"
      onSubmit={handleApprove}
      onClose={onClose}
    />
  );
};

export default AcceptUserPop;
