import PopUpComponent from "../Form/PopUpComponent";
import { createForumRoom } from "../../Services/forumRooms.Service";
import { handleCreateForumRoom } from "../Error/ErrorMessage";
import { useSessionGuard } from "../Util/useSessionGuard";
import { ROOM_NAME_MAX_LENGTH } from "../Util/limits";

const NewForumRoom = ({ onClose, roomName }) => {
  const guard = useSessionGuard();

  const handleSubmit = async (values) => {
    try {
      await createForumRoom(values.name.trim());
      onClose();
    } catch (error) {
      handleCreateForumRoom(error);
      guard(error);
    }
  };

  return (
    <PopUpComponent
      onClose={onClose}
      roomName={roomName}
      handleSubmit={handleSubmit}
      labelName="Name"
      buttonText="Create"
      maxLength={ROOM_NAME_MAX_LENGTH}
    />
  );
};

export default NewForumRoom;
