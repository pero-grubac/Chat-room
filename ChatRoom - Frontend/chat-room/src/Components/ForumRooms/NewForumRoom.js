// NewForumRoom.js
import React from "react";
import PopUpComponent from "../Form/PopUpComponent";
import { createForumRoom } from "../../Services/forumRooms.Service";
import { handleCreateForumRoom } from "../Error/ErrorMessage";
//import { useDispatch } from "react-redux";
import { logout } from "../Redux/slices/userSlice";

const NewForumRoom = ({ onClose, roomName }) => {
  //const dispatch = useDispatch();

  const handleSubmit = async (values) => {
    try {
      await createForumRoom(values.name);
    } catch (error) {
      handleCreateForumRoom(error);
     // dispatch(logout());
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
