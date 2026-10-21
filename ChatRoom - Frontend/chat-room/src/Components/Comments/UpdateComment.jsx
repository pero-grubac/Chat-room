import PopUpComponent from "../Form/PopUpComponent";
import { updateComment } from "../../Services/comments.service";
import { handleUpdateComments } from "../Error/ErrorMessage";
import { useSessionGuard } from "../Util/useSessionGuard";
import { COMMENT_MAX_LENGTH } from "../Util/limits";

// A moderator adjusts a pending comment before publishing it
const UpdateComment = ({ onClose, text, idComment, idForumRoom }) => {
  const guard = useSessionGuard();

  const handleUpdate = async (values) => {
    try {
      await updateComment(idComment, values.name.trim(), idForumRoom);
      onClose();
    } catch (error) {
      handleUpdateComments(error);
      guard(error);
    }
  };

  return (
    <PopUpComponent
      onClose={onClose}
      roomName={text}
      handleSubmit={handleUpdate}
      labelName="Comment"
      buttonText="Update"
      maxLength={COMMENT_MAX_LENGTH}
    />
  );
};

export default UpdateComment;
