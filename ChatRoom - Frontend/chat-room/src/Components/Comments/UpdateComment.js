import React from "react";
import PopUpComponent from "../Form/PopUpComponent";
import { useNavigate } from "react-router-dom";
import { useDispatch } from "react-redux";
import { logout } from "../Redux/slices/userSlice";
import { handleUpdateComments } from "../Error/ErrorMessage";
import { updateComment } from "../../Services/comments.service";

const UpdateComment = ({ onClose, text, idComment, idForumRoom }) => {
  const navigate = useNavigate();
  const dispatch = useDispatch();

  const handleUpdate = async (values) => {
    try {
      console.log(values.name + " " + idComment + " " + idForumRoom);
      await updateComment(idComment, values.name, idForumRoom);
    } catch (error) {
      handleUpdateComments(error);
      dispatch(logout());
      navigate("/login", { replace: true });
    }
    onClose();
  };

  return (
    <PopUpComponent
      onClose={onClose}
      roomName={text}
      handleSubmit={handleUpdate}
      labelName="Comment"
      buttonText="Update"
    />
  );
};

export default UpdateComment;
