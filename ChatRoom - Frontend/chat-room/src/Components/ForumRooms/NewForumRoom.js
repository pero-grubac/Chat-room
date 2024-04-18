// NewForumRoom.js
import React from "react";
import PopUpComponent from "../Form/PopUpComponent";
import { createForumRoom } from "../../Services/forumRooms.Service";
import { handleCreateForumRoom } from "../Error/ErrorMessage";
import { useDispatch } from "react-redux";
import { logout } from "../Redux/slices/userSlice";
import { useNavigate } from "react-router-dom";

const NewForumRoom = ({ onClose, roomName }) => {
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const handleSubmit = async (values) => {
    try {
      await createForumRoom(values.name);
    } catch (error) {
      handleCreateForumRoom(error);
      dispatch(logout());
      navigate("/login", { replace: true });
    }
    onClose();
  };

  return (
    <PopUpComponent
      onClose={onClose}
      roomName={roomName}
      handleSubmit={handleSubmit}
      labelName="Name"
      buttonText="Create"
    />
  );
};

export default NewForumRoom;
