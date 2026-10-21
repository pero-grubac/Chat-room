import PopUpComponent from "../Form/PopUpComponent";
import { updateForumRoom } from "../../Services/forumRooms.Service";
import { handleUpdateForumRoom } from "../Error/ErrorMessage";
import { useSessionGuard } from "../Util/useSessionGuard";
import { ROOM_NAME_MAX_LENGTH } from "../Util/limits";

const UpdateForumRoom = ({ onClose, roomName, id }) => {
  const guard = useSessionGuard();

  const handleUpdate = async (values) => {
    try {
      await updateForumRoom(id, values.name.trim());
      onClose();
    } catch (error) {
      handleUpdateForumRoom(error);
      guard(error);
    }
  };

  return (
    <PopUpComponent
      onClose={onClose}
      roomName={roomName}
      handleSubmit={handleUpdate}
      labelName="Name"
      buttonText="Update"
      maxLength={ROOM_NAME_MAX_LENGTH}
    />
  );
};

export default UpdateForumRoom;
